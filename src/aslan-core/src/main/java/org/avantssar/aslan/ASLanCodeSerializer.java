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

public class ASLanCodeSerializer implements IASLanVisitor {

	private static final String SPEC = "spec";

	private final StringBuffer sb = new StringBuffer();
	private int indent = 0;

	public String getCode() {
		return sb.toString();
	}

	@Override
	public void visit(IASLanSpec spec) {
		sb.append("public ");
		sb.append(IASLanSpec.class.getCanonicalName());
		sb.append(" getASLanSpec()");
		endLine();
		sb.append("{");
		endLine();
		indent();

		startLine();
		sb.append(IASLanSpec.class.getCanonicalName());
		sb.append(" ");
		sb.append(SPEC);
		sb.append(" = ");
		sb.append(ASLanSpecificationBuilder.class.getCanonicalName());
		sb.append(".instance().createASLanSpecification();");
		endLine();

		endLine();
		startLine();
		sb.append("// supertypes");
		endLine();
		for (PrimitiveType t : spec.getPrimitiveTypes()) {
			if (!t.isPrelude() && t.getSuperType() != null) {
				startLine();
				sb.append("spec.primitiveType(\"").append(t.getName()).append("\")");
				sb.append(".setSuperType(");
				sb.append("spec.primitiveType(\"").append(t.getSuperType().getName()).append("\")");
				sb.append(");");
				endLine();
			}
		}

		endLine();
		startLine();
		sb.append("// variables");
		endLine();
		for (Variable v : spec.getVariables()) {
			v.accept(this);
		}

		endLine();
		startLine();
		sb.append("// constants");
		endLine();
		for (Constant c : spec.getConstants()) {
			if (!c.isPrelude()) {
				c.accept(this);
			}
		}

		endLine();
		startLine();
		sb.append("// functions");
		endLine();
		for (Function f : spec.getFunctions()) {
			if (!f.isPrelude()) {
				f.accept(this);
			}
		}

		endLine();
		startLine();
		sb.append("// equations");
		endLine();
		for (Equation eq : spec.getEquations()) {
			eq.accept(this);
		}

		endLine();
		startLine();
		sb.append("// initial states");
		endLine();
		for (InitialState is : spec.getInitialStates()) {
			is.accept(this);
		}

		endLine();
		startLine();
		sb.append("// Horn clauses");
		endLine();
		for (HornClause hc : spec.getHornClauses()) {
			hc.accept(this);
		}

		endLine();
		startLine();
		sb.append("// rewrite rules");
		endLine();
		for (RewriteRule rr : spec.getRules()) {
			rr.accept(this);
		}

		endLine();
		startLine();
		sb.append("// constraints");
		endLine();
		for (Constraint c : spec.getConstraints()) {
			c.accept(this);
		}

		endLine();
		startLine();
		sb.append("// attack states");
		endLine();
		for (AttackState as : spec.getAttackStates()) {
			as.accept(this);
		}

		endLine();
		startLine();
		sb.append("// goals");
		endLine();
		for (Goal g : spec.getGoals()) {
			g.accept(this);
		}

		endLine();
		startLine();
		sb.append("return ").append(SPEC).append(";");
		endLine();

		unindent();
		sb.append("}");
		endLine();
	}

	@Override
	public void visit(PrimitiveType type) {
		sb.append(SPEC).append(".primitiveType(\"").append(type.getName()).append("\")");
	}

	@Override
	public void visit(PairType type) {
		sb.append(SPEC).append(".pairType(");
		type.getLeft().accept(this);
		sb.append(", ");
		type.getRight().accept(this);
		sb.append(")");
	}

	@Override
	public void visit(CompoundType type) {
		sb.append(SPEC).append(".compoundType(");
		sb.append("\"").append(type.getName()).append("\"");
		for (IType t : type.getBaseTypes()) {
			sb.append(", ");
			t.accept(this);
		}
		sb.append(")");

	}

	@Override
	public void visit(SetType type) {
		sb.append(SPEC).append(".setType(");
		type.getBaseType().accept(this);
		sb.append(")");
	}

	@Override
	public void visit(Function fnc) {
		startLine();
		sb.append(SPEC).append(".function(\"").append(fnc.getName()).append("\"");
		sb.append(", ");
		fnc.getType().accept(this);
		for (IType t : fnc.getArgumentsTypes()) {
			sb.append(", ");
			t.accept(this);
		}
		sb.append(");");
		endLine();
	}

	@Override
	public void visit(Constant cnst) {
		startLine();
		sb.append(SPEC).append(".constant(\"").append(cnst.getName()).append("\"");
		sb.append(", ");
		cnst.getType().accept(this);
		sb.append(");");
		endLine();
	}

	@Override
	public void visit(Variable var) {
		startLine();
		sb.append(SPEC).append(".variable(\"").append(var.getName()).append("\"");
		sb.append(", ");
		var.getType().accept(this);
		sb.append(");");
		endLine();
	}

	@Override
	public void visit(Equation eq) {
		startLine();
		sb.append(SPEC).append(".equation(");
		eq.getLeftTerm().accept(this);
		sb.append(", ");
		eq.getRightTerm().accept(this);
		sb.append(");");
		endLine();
	}

	@Override
	public void visit(InitialState init) {
		startLine();
		sb.append(SPEC).append(".initialState(");
		sb.append("\"").append(init.getName()).append("\")");
		for (ITerm t : init.getFacts()) {
			sb.append(".addFact(");
			t.accept(this);
			sb.append(")");
		}
		sb.append(";");
		endLine();
	}

	@Override
	public void visit(HornClause clause) {
		startLine();
		sb.append(SPEC).append(".hornClause(");
		sb.append("\"").append(clause.getName()).append("\"");
		sb.append(", ");
		clause.getHead().accept(this);
		sb.append(")");
		for (ITerm t : clause.getBodyFacts()) {
			sb.append(".addBodyFact(");
			t.accept(this);
			sb.append(")");
		}
		sb.append(";");
		endLine();
	}

	@Override
	public void visit(RewriteRule rule) {
		startLine();
		sb.append(SPEC).append(".rule(");
		sb.append("\"").append(rule.getName()).append("\"");
		sb.append(")");
		for (ITerm t : rule.getLHS()) {
			sb.append(".addLHS(");
			t.accept(this);
			sb.append(")");
		}
		for (Variable v : rule.getExists()) {
			sb.append(".addExists(");
			sb.append(SPEC).append(".findVariable(");
			sb.append("\"").append(v.getName()).append("\"");
			sb.append("))");
		}
		for (ITerm t : rule.getRHS()) {
			sb.append(".addRHS(");
			t.accept(this);
			sb.append(")");
		}
		sb.append(";");
		endLine();
	}

	@Override
	public void visit(Constraint constraint) {
		startLine();
		sb.append(SPEC).append(".constraint(");
		sb.append("\"").append(constraint.getName()).append("\"");
		sb.append(", ");
		constraint.getFormula().accept(this);
		sb.append(");");
		endLine();
	}

	@Override
	public void visit(Goal goal) {
		startLine();
		sb.append(SPEC).append(".goal(");
		sb.append("\"").append(goal.getName()).append("\"");
		sb.append(", ");
		goal.getFormula().accept(this);
		sb.append(");");
		endLine();
	}

	@Override
	public void visit(AttackState attack) {
		startLine();
		sb.append(SPEC).append(".attackState(");
		sb.append("\"").append(attack.getName()).append("\"");
		sb.append(")");
		for (ITerm t : attack.getFacts()) {
			sb.append(".addTerm(");
			t.accept(this);
			sb.append(")");
		}
		sb.append(";");
		endLine();
	}

	@Override
	public void visit(ConstantTerm term) {
		sb.append(SPEC).append(".findConstant(");
		sb.append("\"").append(term.getSymbol().getName()).append("\"");
		sb.append(").term()");
	}

	@Override
	public void visit(NumericTerm term) {
		sb.append(SPEC).append(".numericTerm(");
		sb.append(term.getValue()).append(")");
	}

	@Override
	public void visit(FunctionTerm term) {
		sb.append(SPEC).append(".findFunction(");
		sb.append("\"").append(term.getSymbol().getName()).append("\"");
		sb.append(").term(");
		boolean first = true;
		for (ITerm t : term.getParameters()) {
			if (!first) {
				sb.append(", ");
			}
			t.accept(this);
			first = false;
		}
		sb.append(")");
	}

	@Override
	public void visit(FunctionConstantTerm term) {
		sb.append(SPEC).append(".findFunction(");
		sb.append("\"").append(term.getSymbol().getName()).append("\"");
		sb.append(").constantTerm()");
	}

	@Override
	public void visit(VariableTerm term) {
		sb.append(SPEC).append(".findVariable(");
		sb.append("\"").append(term.getSymbol().getName()).append("\"");
		sb.append(").term()");
	}

	@Override
	public void visit(NegatedTerm term) {
		term.getBaseTerm().accept(this);
		sb.append(".negate()");
	}

	@Override
	public void visit(QuantifiedTerm term) {
		term.getBaseTerm().accept(this);
		sb.append(".");
		if (term.isUniversal()) {
			sb.append("forall");
		}
		else {
			sb.append("exists");
		}
		sb.append("(").append(SPEC).append(".findVariable(");
		sb.append("\"").append(term.getSymbol().getName()).append("\"");
		sb.append("))");
	}

	@Override
	public String toString() {
		return sb.toString();
	}

	private void indent() {
		indent++;
	}

	private void unindent() {
		indent--;
	}

	private void startLine() {
		for (int i = 0; i < indent; i++) {
			sb.append("\t");
		}
	}

	private void endLine() {
		sb.append("\n");
	}

}
