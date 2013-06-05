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

package org.avantssar.aslanpp.visitors;

import java.util.List;
import java.util.Stack;
import org.avantssar.aslanpp.model.ASLanPPSpecification;
import org.avantssar.aslanpp.model.AssertStatement;
import org.avantssar.aslanpp.model.AssignmentStatement;
import org.avantssar.aslanpp.model.BaseExpression;
import org.avantssar.aslanpp.model.BlockStatement;
import org.avantssar.aslanpp.model.BranchStatement;
import org.avantssar.aslanpp.model.ChannelGoal;
import org.avantssar.aslanpp.model.CommunicationTerm;
import org.avantssar.aslanpp.model.CompoundType;
import org.avantssar.aslanpp.model.ConcatTerm;
import org.avantssar.aslanpp.model.ConjunctionExpression;
import org.avantssar.aslanpp.model.ConstantSymbol;
import org.avantssar.aslanpp.model.ConstantTerm;
import org.avantssar.aslanpp.model.Constraint;
import org.avantssar.aslanpp.model.DeclarationGroup;
import org.avantssar.aslanpp.model.DefaultPseudonymTerm;
import org.avantssar.aslanpp.model.DisjunctionExpression;
import org.avantssar.aslanpp.model.Entity;
import org.avantssar.aslanpp.model.EqualityExpression;
import org.avantssar.aslanpp.model.Equation;
import org.avantssar.aslanpp.model.ErrorMessages;
import org.avantssar.aslanpp.model.ExistsExpression;
import org.avantssar.aslanpp.model.ForallExpression;
import org.avantssar.aslanpp.model.FreshStatement;
import org.avantssar.aslanpp.model.FunctionSymbol;
import org.avantssar.aslanpp.model.FunctionTerm;
import org.avantssar.aslanpp.model.Goal;
import org.avantssar.aslanpp.model.HornClause;
import org.avantssar.aslanpp.model.IExpression;
import org.avantssar.aslanpp.model.IStatement;
import org.avantssar.aslanpp.model.ITerm;
import org.avantssar.aslanpp.model.IType;
import org.avantssar.aslanpp.model.ImplicationExpression;
import org.avantssar.aslanpp.model.InequalityExpression;
import org.avantssar.aslanpp.model.IntroduceStatement;
import org.avantssar.aslanpp.model.LTLExpression;
import org.avantssar.aslanpp.model.LoopStatement;
import org.avantssar.aslanpp.model.MacroSymbol;
import org.avantssar.aslanpp.model.MacroTerm;
import org.avantssar.aslanpp.model.NegationExpression;
import org.avantssar.aslanpp.model.NewEntityInstanceStatement;
import org.avantssar.aslanpp.model.NumericTerm;
import org.avantssar.aslanpp.model.Prelude;
import org.avantssar.aslanpp.model.PseudonymTerm;
import org.avantssar.aslanpp.model.RetractStatement;
import org.avantssar.aslanpp.model.SecrecyGoalStatement;
import org.avantssar.aslanpp.model.SelectStatement;
import org.avantssar.aslanpp.model.SessionChannelGoal;
import org.avantssar.aslanpp.model.SessionSecrecyGoal;
import org.avantssar.aslanpp.model.SetLiteralTerm;
import org.avantssar.aslanpp.model.SetType;
import org.avantssar.aslanpp.model.SimpleType;
import org.avantssar.aslanpp.model.SymbolicInstanceStatement;
import org.avantssar.aslanpp.model.TupleTerm;
import org.avantssar.aslanpp.model.TupleType;
import org.avantssar.aslanpp.model.UnnamedMatchTerm;
import org.avantssar.aslanpp.model.VariableSymbol;
import org.avantssar.aslanpp.model.VariableTerm;
import org.avantssar.commons.ErrorGatherer;

/**
 * Visits an ASLan++ specification (or a part of it) and assigns types to all
 * untyped variables. Any typing related errors that are encountered are made
 * available at the end.
 * 
 * @author gabi
 */
public class TypeAssigner implements IASLanPPVisitor {

	private final Stack<IType> expectedTypes = new Stack<IType>();
	private final ErrorGatherer err;
	private Entity ent;

	public TypeAssigner(ErrorGatherer err) {
		this.err = err;
	}

	public ErrorGatherer getErrorGatherer() {
		return err;
	}

	@Override
	public void visit(ASLanPPSpecification spec) {
		if (spec.getRootEntity() != null) {
			spec.getRootEntity().accept(this);
		}
	}

	@Override
	public void visit(Entity ent) {
		Entity oldEnt = this.ent;
		this.ent = ent;

		for (HornClause clause : ent.getEntriesByType(HornClause.class)) {
			clause.accept(this);
		}
		for (Equation eq : ent.getEntriesByType(Equation.class)) {
			eq.accept(this);
		}
		for (Constraint constraint : ent.getEntriesByType(Constraint.class)) {
			constraint.accept(this);
		}
		for (Goal goal : ent.getEntriesByType(Goal.class)) {
			goal.accept(this);
		}
		for (SessionChannelGoal chGoal : ent.getEntriesByType(SessionChannelGoal.class)) {
			chGoal.accept(this);
		}
		for (SessionSecrecyGoal secrGoal : ent.getEntriesByType(SessionSecrecyGoal.class)) {
			secrGoal.accept(this);
		}
		for (Entity child : ent.getEntriesByType(Entity.class)) {
			child.accept(this);
		}
		if (ent.getBodyStatement() != null) {
			ent.getBodyStatement().accept(this);
		}

		this.ent = oldEnt;
	}

	@Override
	public void visit(SimpleType type) {}

	@Override
	public void visit(CompoundType type) {}

	@Override
	public void visit(SetType type) {}

	@Override
	public void visit(TupleType type) {}

	@Override
	public void visit(DeclarationGroup gr) {}

	@Override
	public void visit(VariableSymbol var) {}

	@Override
	public void visit(ConstantSymbol cnst) {}

	@Override
	public void visit(FunctionSymbol fnc) {}

	@Override
	public void visit(MacroSymbol macro) {}

	@Override
	public void visit(HornClause clause) {
		expectedTypes.push(clause.findType(Prelude.FACT));
		clause.getHead().accept(this);
		for (ITerm t : clause.getBody()) {
			t.accept(this);
		}
		expectedTypes.pop();
		for (IExpression e : clause.getEqualities()) {
			e.accept(this);
		}
	}

	public void visit(Equation equation) {
		checkEmpty();
		equation.getLeftTerm().accept(this);
		equation.getRightTerm().accept(this);
		IType commonType = null;
		if (equation.getLeftTerm().isTypeCertain()) {
			commonType = equation.getLeftTerm().inferType();
		}
		else if (equation.getRightTerm().isTypeCertain()) {
			commonType = equation.getRightTerm().inferType();
		}
		else {
			commonType = equation.getOwner().findType(Prelude.MESSAGE);
		}
		expectedTypes.push(commonType);
		equation.getLeftTerm().accept(this);
		equation.getRightTerm().accept(this);
		expectedTypes.pop();
	}

	@Override
	public void visit(Constraint constraint) {
		constraint.getFormula().accept(this);
	}

	@Override
	public void visit(Goal goal) {
		goal.getFormula().accept(this);
	}

	@Override
	public void visit(SessionChannelGoal chGoal) {
		expectedTypes.push(chGoal.getOwner().findType(Prelude.AGENT));
		chGoal.getSender().accept(this);
		chGoal.getReceiver().accept(this);
		expectedTypes.pop();
	}

	@Override
	public void visit(SessionSecrecyGoal secrGoal) {
		expectedTypes.push(secrGoal.getOwner().findType(Prelude.AGENT));
		for (ITerm t : secrGoal.getAgents()) {
			t.accept(this);
		}
		expectedTypes.pop();
	}

	@Override
	public void visit(AssignmentStatement stmt) {
		expectedTypes.push(stmt.getSymbol().getType());
		stmt.getTerm().accept(this);
		expectedTypes.pop();
	}

	@Override
	public void visit(AssertStatement stmt) {
		if (stmt.getGuard() != null) {
			stmt.getGuard().accept(this);
		}
	}

	@Override
	public void visit(BlockStatement stmt) {
		for (IStatement s : stmt.getStatements()) {
			s.accept(this);
		}
	}

	@Override
	public void visit(FreshStatement stmt) {}

	@Override
	public void visit(LoopStatement stmt) {
		stmt.getGuard().accept(this);
		if (stmt.getBody() != null) {
			stmt.getBody().accept(this);
		}
	}

	@Override
	public void visit(IntroduceStatement stmt) {
		expectedTypes.push(stmt.getTerm().getScope().findType(Prelude.FACT));
		stmt.getTerm().accept(this);
		expectedTypes.pop();
	}

	@Override
	public void visit(RetractStatement stmt) {
		expectedTypes.push(stmt.getTerm().getScope().findType(Prelude.FACT));
		stmt.getTerm().accept(this);
		expectedTypes.pop();
	}

	@Override
	public void visit(SelectStatement stmt) {
		for (IExpression e : stmt.getChoices().keySet()) {
			IStatement s = stmt.getChoices().get(e);
			e.accept(this);
			s.accept(this);
		}
	}

	@Override
	public void visit(BranchStatement stmt) {
		stmt.getGuard().accept(this);
		if (stmt.getTrueBranch() != null) {
			stmt.getTrueBranch().accept(this);
		}
		if (stmt.getFalseBranch() != null) {
			stmt.getFalseBranch().accept(this);
		}
	}

	@Override
	public void visit(NewEntityInstanceStatement stmt) {
		if (stmt.getEntity().getParameters().size() != stmt.getParameters().size()) {
			err.addError(stmt.getLocation(), ErrorMessages.WRONG_NUMBER_OF_PARAMETERS, "Entity", stmt.getEntity().getOriginalName(), stmt.getEntity().getParameters().size(), stmt.getParameters()
					.size());
		}
		else {
			for (int i = 0; i < stmt.getEntity().getParameters().size(); i++) {
				IType type = stmt.getEntity().getParameters().get(i).getType();
				ITerm term = stmt.getParameters().get(i);
				expectedTypes.push(type);
				term.accept(this);
				expectedTypes.pop();
			}
		}
	}

	@Override
	public void visit(SymbolicInstanceStatement stmt) {
		if (stmt.getEntity().getParameters().size() != stmt.getParameters().size()) {
			err.addError(stmt.getLocation(), ErrorMessages.WRONG_NUMBER_OF_PARAMETERS, "Entity", stmt.getEntity().getOriginalName(), stmt.getEntity().getParameters().size(), stmt.getParameters()
					.size());
		}
		else {
			for (int i = 0; i < stmt.getEntity().getParameters().size(); i++) {
				IType type = stmt.getEntity().getParameters().get(i).getType();
				ITerm term = stmt.getParameters().get(i);
				expectedTypes.push(type);
				term.accept(this);
				expectedTypes.pop();
			}
		}
		if (stmt.getGuard() != null) {
			stmt.getGuard().accept(this);
		}
	}

	@Override
	public void visit(SecrecyGoalStatement stmt) {
		expectedTypes.push(ent.findType(Prelude.AGENT));
		for (ITerm t : stmt.getAgents()) {
			t.accept(this);
		}
		expectedTypes.pop();
		expectedTypes.push(ent.findType(Prelude.MESSAGE));
		stmt.getPayload().accept(this);
		expectedTypes.pop();
	}

	public void visit(ChannelGoal goal) {
		expectedTypes.push(goal.getOwner().findType(Prelude.AGENT));
		goal.getSender().accept(this);
		goal.getReceiver().accept(this);
		expectedTypes.pop();
		expectedTypes.push(goal.getOwner().findType(Prelude.MESSAGE));
		goal.getPayload().accept(this);
		expectedTypes.pop();
	}

	@Override
	public void visit(NegationExpression expr) {
		expr.getBaseExpression().accept(this);
	}

	@Override
	public void visit(ConjunctionExpression expr) {
		expr.getLeftExpression().accept(this);
		expr.getRightExpression().accept(this);
	}

	@Override
	public void visit(DisjunctionExpression expr) {
		expr.getLeftExpression().accept(this);
		expr.getRightExpression().accept(this);
	}

	@Override
	public void visit(ExistsExpression expr) {
		expr.getBaseExpression().accept(this);
	}

	@Override
	public void visit(ForallExpression expr) {
		expr.getBaseExpression().accept(this);
	}

	@Override
	public void visit(ImplicationExpression expr) {
		expr.getLeftExpression().accept(this);
		expr.getRightExpression().accept(this);
	}

	@Override
	public void visit(LTLExpression expr) {
		for (IExpression e : expr.getChildExpressions()) {
			e.accept(this);
		}
	}

	@Override
	public void visit(EqualityExpression expr) {
		IType commonType = visitEqualityOrInequlity(expr);
		expectedTypes.push(commonType);
		expr.getLeftTerm().accept(this);
		expr.getRightTerm().accept(this);
		expectedTypes.pop();
	}

	@Override
	public void visit(InequalityExpression expr) {
		visitEqualityOrInequlity(expr);
		expectedTypes.push(expr.getLeftTerm().getScope().findType(Prelude.MESSAGE));
		expr.getLeftTerm().accept(this);
		expr.getRightTerm().accept(this);
		expectedTypes.pop();
	}

	private IType visitEqualityOrInequlity(EqualityExpression expr) {
		checkEmpty();
		expr.getLeftTerm().accept(this);
		expr.getRightTerm().accept(this);
		IType commonType = null;
		if (expr.getLeftTerm().isTypeCertain()) {
			commonType = expr.getLeftTerm().inferType();
		}
		if (expr.getRightTerm().isTypeCertain()) {
			if (commonType == null || expr.getRightTerm().inferType().isAssignableFrom(commonType)) {
				commonType = expr.getRightTerm().inferType();
			}
		}
		if (commonType == null) {
			commonType = expr.getLeftTerm().getScope().findType(Prelude.MESSAGE);
		}
		return commonType;
	}

	@Override
	public void visit(BaseExpression expr) {
		checkEmpty();
		expectedTypes.push(ent.findType(Prelude.FACT));
		expr.getBaseTerm().accept(this);
		expectedTypes.pop();
	}

	@Override
	public ITerm visit(CommunicationTerm term) {
		checkActualType(term.getScope().findType(Prelude.FACT), term);
		expectedTypes.push(term.getScope().findType(Prelude.AGENT));
		term.getSender().accept(this);
		term.getReceiver().accept(this);
		expectedTypes.pop();
		expectedTypes.push(term.getScope().findType(Prelude.MESSAGE));
		term.getPayload().accept(this);
		expectedTypes.pop();
		return term;
	}

	@Override
	public ITerm visit(ConstantTerm term) {
		checkActualType(term.getSymbol().getType(), term);
		return term;
	}

	@Override
	public ITerm visit(DefaultPseudonymTerm term) {
		checkActualType(term.getScope().findType(Prelude.PUBLIC_KEY), term);
		expectedTypes.push(term.getScope().findType(Prelude.AGENT));
		term.getBaseTerm().accept(this);
		expectedTypes.pop();
		return term;
	}

	@Override
	public ITerm visit(FunctionTerm term) {
		boolean handled = false;
		if (!expectedTypes.empty()) {
			IType expectedType = expectedTypes.peek();
			if (expectedType instanceof CompoundType) {
				PrettyPrinter pp = new PrettyPrinter();
				term.accept(pp);
				CompoundType expCT = (CompoundType) expectedType;
				if (expCT.getName().equals(term.getSymbol().getName())) {
					if (expCT.getArgumentTypes().size() == term.getArguments().size()) {
						for (int i = 0; i < expCT.getArgumentTypes().size(); i++) {
							IType argType = expCT.getArgumentTypes().get(i);
							ITerm arg = term.getArguments().get(i);
							expectedTypes.push(argType);
							arg.accept(this);
							expectedTypes.pop();
						}
					}
					else {
						err.addException(term.getLocation(), ErrorMessages.WRONG_COMPOUND_TYPE_FOR_TERM, expCT, pp.toString(), expCT);
					}
				}
				else {
					err.addException(term.getLocation(), ErrorMessages.WRONG_COMPOUND_TYPE_FOR_TERM, expCT, pp.toString(), expCT);
				}
				handled = true;
			}
		}
		if (!handled) {
			checkActualType(term.getSymbol().getType(), term);
		}
		if (term.getSymbol().getArgumentsTypes().size() != term.getArguments().size()) {
			err.addError(term.getLocation(), ErrorMessages.WRONG_NUMBER_OF_PARAMETERS, "Function", term.getSymbol().getOriginalName(), term.getSymbol().getArgumentsTypes().size(), term.getArguments()
					.size());
		}
		else {
			if (term.getSymbol().isSetFunction()) {
				ITerm setTerm = term.getArguments().get(0);
				ITerm memberTerm = term.getArguments().get(1);
				if (setTerm.isTypeCertain()) {
					IType infType = setTerm.inferType();
					if (infType instanceof SetType) {
						SetType st = (SetType) infType;
						expectedTypes.push(st);
						setTerm.accept(this);
						expectedTypes.pop();
						expectedTypes.push(st.getBaseType());
						memberTerm.accept(this);
						expectedTypes.pop();
					}
					else {
						err.addError(term.getLocation(), ErrorMessages.WRONG_TYPE_FOR_TERM, infType, "set", setTerm);
					}
				}
				else {
					IType infType = memberTerm.inferType();
					expectedTypes.push(Prelude.getSetOf(infType));
					setTerm.accept(this);
					expectedTypes.pop();
					expectedTypes.push(infType);
					memberTerm.accept(this);
					expectedTypes.pop();
				}
			}
			else {
				for (int i = 0; i < term.getSymbol().getArgumentsTypes().size(); i++) {
					IType type = term.getSymbol().getArgumentsTypes().get(i);
					ITerm t = term.getArguments().get(i);
					expectedTypes.push(type);
					t.accept(this);
					expectedTypes.pop();
				}
			}
		}
		return term;
	}

	@Override
	public ITerm visit(MacroTerm term) {
		return term;
	}

	@Override
	public ITerm visit(PseudonymTerm term) {
		checkActualType(term.getScope().findType(Prelude.AGENT), term);
		expectedTypes.push(term.getScope().findType(Prelude.AGENT));
		term.getBaseTerm().accept(this);
		expectedTypes.pop();
		expectedTypes.push(term.getScope().findType(Prelude.PUBLIC_KEY));
		term.getPseudonym().accept(this);
		expectedTypes.pop();
		return term;
	}

	@Override
	public ITerm visit(SetLiteralTerm term) {
		if (!expectedTypes.empty()) {
			PrettyPrinter pp = new PrettyPrinter();
			term.accept(pp);
			IType et = expectedTypes.peek();
			if (et instanceof SetType) {
				SetType st = (SetType) et;
				term.setElementsType(st.getBaseType());
				expectedTypes.push(st.getBaseType());
				for (ITerm t : term.getTerms()) {
					t.accept(this);
				}
				expectedTypes.pop();
			}
			else if (!et.equals(term.getScope().findType(Prelude.MESSAGE))) {
				err.addError(term.getLocation(), ErrorMessages.WRONG_TYPE_FOR_TERM, "set", expectedTypes.peek(), pp.toString());
			}
		}
		else {
			for (ITerm t : term.getTerms()) {
				t.accept(this);
			}
		}
		return term;
	}

	private void visitTupleOrConcat(TupleType tt, ITerm term, List<ITerm> terms) {
		if (tt.getBaseTypes().size() != terms.size()) {
			PrettyPrinter pp = new PrettyPrinter();
			term.accept(pp);
			err.addError(term.getLocation(), ErrorMessages.WRONG_ARITY_FOR_TUPLE_TYPE, terms.size(), tt.getBaseTypes().size(), pp.toString());
		}
		else {
			for (int i = 0; i < tt.getBaseTypes().size(); i++) {
				IType currType = tt.getBaseTypes().get(i);
				ITerm currTerm = terms.get(i);
				expectedTypes.push(currType);
				currTerm.accept(this);
				expectedTypes.pop();
			}
		}
	}

	@Override
	public ITerm visit(ConcatTerm term) {
		checkActualType(term.getScope().findType(Prelude.MESSAGE), term);
		expectedTypes.push(term.getScope().findType(Prelude.MESSAGE));
		for (ITerm t : term.getTerms()) {
			t.accept(this);
		}
		expectedTypes.pop();
		return term;
	}

	@Override
	public ITerm visit(TupleTerm term) {
		boolean handled = false;
		if (!expectedTypes.empty()) {
			IType et = expectedTypes.peek();
			if (et instanceof TupleType) {
				TupleType tt = (TupleType) et;
				visitTupleOrConcat(tt, term, term.getTerms());
				handled = true;
			}
			else if (!et.isAssignableFrom(term.inferType())) {
				PrettyPrinter pp = new PrettyPrinter();
				term.accept(pp);
				err.addError(term.getLocation(), ErrorMessages.WRONG_TYPE_FOR_TERM, "tuple", expectedTypes.peek(), pp.toString());
			}
		}
		if (!handled) {
			expectedTypes.push(term.getScope().findType(Prelude.MESSAGE));
			for (ITerm t : term.getTerms()) {
				t.accept(this);
			}
			expectedTypes.pop();
		}
		return term;
	}

	@Override
	public ITerm visit(VariableTerm term) {
		if (!expectedTypes.empty()) {
			VariableSymbol var = term.getSymbol();
			if (!var.wasUntyped()) {
				checkActualType(var.getType(), term);
			}
			else {
				IType et = expectedTypes.peek();
				if (!var.wasTypeSet()) {
					var.setType(et);
				}
				else {
					// type refinement if possible
					if (var.getType().isAssignableFrom(et)) {
						var.setType(et);
					}
					else {
						checkActualType(var.getType(), term);
					}
				}
			}
		}
		return term;
	}

	@Override
	public UnnamedMatchTerm visit(UnnamedMatchTerm term) {
		if (!expectedTypes.empty()) {
			VariableSymbol var = term.getDummySymbol();
			IType et = expectedTypes.peek();
			if (!var.wasTypeSet()) {
				var.setType(et);
			}
			else {
				// type refinement if possible
				if (var.getType().isAssignableFrom(et)) {
					var.setType(et);
				}
				else {
					checkActualType(var.getType(), term);
				}
			}
		}
		return term;
	}

	public NumericTerm visit(NumericTerm term) {
		checkActualType(term.getScope().findType(Prelude.NAT), term);
		return term;
	}

	private void checkEmpty() {
		if (!expectedTypes.empty()) {
			throw new RuntimeException("The stack of expected types should be empty at this point.");
		}
	}

	private void checkActualType(IType actualType, ITerm term) {
		if (!expectedTypes.empty()) {
			if (!expectedTypes.peek().isAssignableFrom(actualType)) {
				PrettyPrinter pp = new PrettyPrinter();
				term.accept(pp);
				err.addError(term.getLocation(), ErrorMessages.WRONG_TYPE_FOR_TERM, actualType, expectedTypes.peek(), pp.toString());
			}
		}
	}
}
