// Copyright 2010-2013 (c) IeAT, Siemens AG, AVANTSSAR and SPaCIoS consortia.
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.avantssar.aslan;

import java.util.List;
import java.util.Stack;
import org.avantssar.commons.ErrorGatherer;
import org.avantssar.commons.Term;

public class Validator implements IASLanVisitor {

	private final Stack<IType> expectedTypes = new Stack<IType>();
	private final ErrorGatherer err;
	private IASLanSpec spec;

	public Validator(ErrorGatherer err) {
		this.err = err;
	}

	public ErrorGatherer getErrorGatherer() {
		return err;
	}

	@Override
	public void visit(IASLanSpec spec) {
		this.spec = spec;

		for (Function fnc : spec.getFunctions()) {
			fnc.accept(this);
		}

		for (Equation eq : spec.getEquations()) {
			eq.accept(this);
		}

		for (HornClause hc : spec.getHornClauses()) {
			hc.accept(this);
		}

		for (InitialState is : spec.getInitialStates()) {
			is.accept(this);
		}

		for (RewriteRule rr : spec.getRules()) {
			rr.accept(this);
		}
		
		for (Constraint cc : spec.getConstraints()) {
			cc.accept(this);
		}

		for (AttackState as : spec.getAttackStates()) {
			as.accept(this);
		}

		for (Goal gg : spec.getGoals()) {
			gg.accept(this);
		}
	}

	@Override
	public void visit(PrimitiveType type) {}

	@Override
	public void visit(PairType type) {}

	@Override
	public void visit(CompoundType type) {
		String fname = type.getName();
		Function fnc = spec.findFunction(fname);
		if (fnc == null) {
			err.addError(type.getLocation(), ASLanErrorMessages.UNKNOWN_FUNCTION_USED_IN_COMPOUND_TYPE, type.getRepresentation(), fname);
		}
		else {
			if (type.getBaseTypes().size() != fnc.getArgumentsTypes().size()) {
				err.addError(type.getLocation(), ASLanErrorMessages.FUNCTION_IN_COMPOUND_TYPE_WRONG_NUMBER_OF_ARGUMENTS, 
						type.getRepresentation(), fnc.getArgumentsTypes().size(), type.getBaseTypes().size());
			}
			else {
				for (int i = 0; i < type.getBaseTypes().size(); i++) {
					IType ctArg = type.getBaseTypes().get(i);
					IType fncArg = fnc.getArgumentsTypes().get(i);
					if (!ctArg.equals(fncArg)) {
						err.addError(type.getLocation(), ASLanErrorMessages.COMPOUND_TYPE_ARGUMENT_DOES_NOT_MATCH, 
								i, type.getRepresentation(), ctArg.getRepresentation(), fncArg.getRepresentation());
					}
				}
			}
		}
	}

	@Override
	public void visit(SetType type) {}

	@Override
	public void visit(Function fnc) {
		if (fnc.getName().startsWith(Term.STATE_PREFIX+"_") && !fnc.getName().equals("state_OrchestrationGoal")) {
			if (fnc.getArgumentsTypes().size() < 3) {
				err.addError(fnc.getLocation(), ASLanErrorMessages.STATE_FUNCTION_TOO_FEW_ARGUMENTS, fnc.getName(), fnc.getArgumentsTypes().size());
			}
			else if (!IASLanSpec.AGENT.isAssignableFrom(fnc.getArgumentsTypes().get(0), spec) || !IASLanSpec.NAT.equals(fnc.getArgumentsTypes().get(1))
					|| !IASLanSpec.NAT.equals(fnc.getArgumentsTypes().get(2))) {
				err.addError(fnc.getLocation(), ASLanErrorMessages.STATE_FUNCTION_WRONG_TYPED_ARGUMENTS, 
						fnc.getName(), fnc.getArgumentsTypes().get(0).getRepresentation(), 
						fnc.getArgumentsTypes().get(1).getRepresentation(), fnc.getArgumentsTypes().get(2).getRepresentation());
			}
		}
	}

	@Override
	public void visit(Constant cnst) {}

	@Override
	public void visit(Variable var) {
		if (IASLanSpec.FACT.isAssignableFrom(var.getType(), spec)) {
			err.addError(var.getLocation(), ASLanErrorMessages.ELEMENT_OF_UNACCEPTED_TYPE, var.getName(), var.getType().getRepresentation(), IASLanSpec.FACT.getRepresentation());
		}
	}

	@Override
	public void visit(Equation eq) {
		checkEmpty();
		expectedTypes.push(IASLanSpec.MESSAGE);
		eq.getLeftTerm().accept(this);
		eq.getRightTerm().accept(this);
		expectedTypes.pop();

	}

	@Override
	public void visit(InitialState init) {
		verifyFacts(init.getFacts());
	}

	private void verifyFact(ITerm term) {
		checkEmpty();
		expectedTypes.push(IASLanSpec.FACT);
		term.accept(this);
		expectedTypes.pop();
	}

	private void verifyFacts(List<ITerm> terms) {
		for (ITerm t : terms) {
			verifyFact(t);
		}
	}

	@Override
	public void visit(HornClause clause) {
		verifyFact(clause.getHead());
		verifyFacts(clause.getBodyFacts());
	}

	@Override
	public void visit(RewriteRule rule) {
		verifyFacts(rule.getLHS());
		verifyFacts(rule.getRHS());
	}

	@Override
	public void visit(Constraint constraint) {
		verifyFact(constraint.getFormula());
	}

	@Override
	public void visit(Goal goal) {
		verifyFact(goal.getFormula());
	}

	@Override
	public void visit(AttackState attack) {
		verifyFacts(attack.getFacts());
		verifyFacts(attack.getConditions());
	}

	@Override
	public void visit(ConstantTerm term) {
		checkActualType(term.getType(), term);
	}

	@Override
	public void visit(NumericTerm term) {
		checkActualType(term.getType(), term);
	}

	@Override
	public void visit(FunctionTerm term) {
		//if (term.getSymbol().getName() != "pair") { // TODO maybe add better check if tuple is assignable to expected type
			checkActualType(term.getType(), term);
		//}
		if (term.getFunction().getArgumentsTypes().size() != term.getParameters().size()) {
			err.addError(term.getLocation(), ASLanErrorMessages.WRONG_NUMBER_OF_PARAMETERS, "Function", 
					term.getFunction().getName(), term.getFunction().getArgumentsTypes().size(), term.getParameters().size());
		}
		else {
			for (int i = 0; i < term.getParameters().size(); i++) {
				ITerm arg  = term.getParameters().get(i);
				IType argT = term.getFunction().getArgumentsTypes().get(i);
				if (term.getSymbol().getName() != "pair") {
					expectedTypes.push(argT);
					arg.accept(this);
					expectedTypes.pop();
				}
				else { // TODO maybe add better check if tuple argument is assignable to expected type
					if (IASLanSpec.FACT.isAssignableFrom(argT, spec)) {
						err.addError(arg.getLocation(), ASLanErrorMessages.ELEMENT_OF_UNACCEPTED_TYPE, arg, argT.getRepresentation(), IASLanSpec.FACT.getRepresentation());
					}
				}
			}
		}
	}

	@Override
	public void visit(FunctionConstantTerm term) {
		checkActualType(term.getType(), term);
	}

	@Override
	public void visit(VariableTerm term) {
		checkActualType(term.getType(), term);
	}

	@Override
	public void visit(NegatedTerm term) {
		term.getBaseTerm().accept(this);
	}

	@Override
	public void visit(QuantifiedTerm term) {
		term.getBaseTerm().accept(this);
	}

	private void checkEmpty() {
		if (!expectedTypes.empty()) {
			err.addException("INTERNAL ERROR: The stack of expected types should be empty at this point.");
		}
	}

	private void checkActualType(IType actualType, ITerm term) {
		if (!expectedTypes.empty()) {
			if (!expectedTypes.peek().isAssignableFrom(actualType, spec)) {
				err.addError(term.getLocation(), ASLanErrorMessages.WRONG_TYPE_FOR_TERM, actualType, expectedTypes.peek(), term.toString());
			}
		}
	}

}
