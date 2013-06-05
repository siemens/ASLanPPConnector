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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PrettyPrinter implements IASLanVisitor {

	private final StringBuffer sb = new StringBuffer();
	private int indent = 0;
	private final boolean stripOutput;

	public PrettyPrinter() {
		this(false);
	}

	public PrettyPrinter(boolean stripOutput) {
		this.stripOutput = stripOutput;
	}

	@Override
	public void visit(IASLanSpec spec) {
		listComments(spec);
		endLine();

		startLine();
		sb.append("section signature:");
		endLine();
		endLine();
		indent();
		for (PrimitiveType t : spec.getPrimitiveTypes()) {
			if (!t.isPrelude() && t.getSuperType() != null) {
				startLine();
				if (t.getSuperType() != null) {
					sb.append(t.getSuperType().getName());
					sb.append(" > ");
				}
				sb.append(t.getName());
				endLine();
			}
		}
		for (Function f : spec.getFunctions()) {
			if (!f.isPrelude()) {
				f.accept(this);
			}
		}
		unindent();
		endLine();
		
		startLine();
		sb.append("section types:");
		endLine();
		endLine();
		indent();
		List<IType> var_types = new ArrayList<IType>();
		InitialState init = spec.getInitialStates().get(0); // TODO assuming exactly one initial state
		for (Variable v : spec.getVariables()) {
			v.accept(this);
			// add iknows(iknows_empty_ST) facts for every set type ST used for variables
			IType vt = v.getType();
			if(vt.toString().startsWith("set(") && !var_types.contains(v.getType())) {
				var_types.add(vt);
				String cn = "empty_".concat(vt.toString().replace('(','_').replace(')','_').replaceAll(", ", "___")); // TODO consolidate with org.avantssar.aslanpp.model.IType.IType.getDummyName();
				/* not needed: 
				startLine();
				sb.append(cn);
				sb.append(" : ");
				vt.accept(this); */
				init.addFact(IASLanSpec.IKNOWS.term(spec.constant(cn, vt).term()));
				endLine(); 
			}
		}
		for (Constant c : spec.getConstants()) {
			if (!c.isPrelude()) {
				c.accept(this);
			}
		}
		unindent();
		endLine();
		
		if (spec.getEquations().size() > 0) {
			startLine();
			sb.append("section equations:");
			endLine();
			indent();
			for (Equation eq : spec.getEquations()) {
				eq.accept(this);
			}
			unindent();
			endLine();
		}

		startLine();
		sb.append("section inits:");
		endLine();
		for (InitialState is : spec.getInitialStates()) {
			is.accept(this);
		}
		endLine();
		
		startLine();
		sb.append("section hornClauses:");
		endLine();
		List<HornClause> hcClone = new ArrayList<HornClause>();
		hcClone.addAll(spec.getHornClauses());
		if (stripOutput) {
			Collections.sort(hcClone);
		}
		for (HornClause hc : hcClone) {
			hc.accept(this);
		}
		endLine();
		
		startLine();
		sb.append("section rules:");
		endLine();
		for (RewriteRule r : spec.getRules()) {
			r.accept(this);
		}
		endLine();
		
		if (spec.getConstraints().size() > 0) {
			startLine();
			sb.append("section constraints:");
			endLine();
			for (Constraint c : spec.getConstraints()) {
				c.accept(this);
			}
			endLine();
		}
		
		startLine();
		sb.append("section goals:");
		endLine();
		for (AttackState s : spec.getAttackStates()) {
			s.accept(this);
		}
		for (Goal s : spec.getGoals()) {
			s.accept(this);
		}
	}

	@Override
	public void visit(PrimitiveType type) {
		sb.append(type.getName());
	}

	@Override
	public void visit(PairType type) {
		sb.append("pair(");
		type.getLeft().accept(this);
		sb.append(", ");
		type.getRight().accept(this);
		sb.append(")");
	}

	@Override
	public void visit(CompoundType type) {
		sb.append(type.getName());
		sb.append("(");
		boolean first = true;
		for (IType t : type.getBaseTypes()) {
			if (!first) {
				sb.append(", ");
			}
			t.accept(this);
			first = false;
		}
		sb.append(")");
	}

	@Override
	public void visit(SetType type) {
		sb.append("set");
		sb.append("(");
		type.getBaseType().accept(this);
		sb.append(")");
	}

	@Override
	public void visit(Function fnc) {
		listComments(fnc);
		startLine();
		sb.append(fnc.getName());
		sb.append(" : ");
		boolean first = true;
		for (IType t : fnc.getArgumentsTypes()) {
			if (!first) {
				sb.append(" * ");
			}
			t.accept(this);
			first = false;
		}
		sb.append(" -> ");
		fnc.getType().accept(this);
		endLine();
	}

	@Override
	public void visit(Constant cnst) {
		listComments(cnst);
		startLine();
		sb.append(cnst.getName());
		sb.append(" : ");
		cnst.getType().accept(this);
		endLine();
	}

	@Override
	public void visit(Variable var) {
		listComments(var);
		startLine();
		sb.append(var.getName());
		sb.append(" : ");
		var.getType().accept(this);
		endLine();
	}

	public void visit(Equation eq) {
		startLine();
		eq.getLeftTerm().accept(this);
		sb.append(" = ");
		eq.getRightTerm().accept(this);
		endLine();
	}

	@Override
	public void visit(InitialState init) {
		fillHeadLine(init, "initial_state");
		indent();
		fillTerms(init, ".");
		unindent();
	}

	@Override
	public void visit(HornClause clause) {
		fillHeadLine(clause, "hc");
		indent();
		startLine();
		clause.getHead().accept(this);
		if (clause.getTerms().size() > 0) {
			sb.append(" :-");
		}
		endLine();
		indent();
		fillTerms(clause, ",");
		unindent();
		unindent();
	}

	@Override
	public void visit(Constraint constraint) {
		fillHeadLine(constraint, "constraint");
		indent();
		startLine();
		constraint.getFormula().accept(this);
		endLine();
		unindent();
	}

	@Override
	public void visit(Goal goal) {
		fillHeadLine(goal, "goal");
		indent();
		startLine();
		goal.getFormula().accept(this);
		endLine();
		unindent();
	}

	public void visit(AttackState attack) {
		fillHeadLine(attack, "attack_state");
		indent();
		boolean first = true;
		for (ITerm t : attack.getTerms()) {
			if (!t.isCondition()) {
				if (!first) {
					sb.append(".");
					endLine();
				}
				startLine();
				t.accept(this);
				first = false;
			}
		}
		for (ITerm t : attack.getTerms()) {
			if (t.isCondition()) {
				if (!first) {
					sb.append(" &");
					endLine();
				}
				startLine();
				t.accept(this);
				first = false;
			}
		}
		endLine();
		unindent();
	}

	@Override
	public void visit(RewriteRule rule) {
		fillHeadLine(rule, "step");
		indent();
		List<Variable> fresh = rule.getExists();
		boolean first = true;
		for (ITerm t : rule.getLHS()) {
			if (!t.isCondition()) {
				if (!first) {
					sb.append(".");
					endLine();
				}
				startLine();
				t.accept(this);
				first = false;
			}
		}
		for (ITerm t : rule.getLHS()) {
			if (t.isCondition()) {
				if (!first) {
					sb.append(" &");
					endLine();
				}
				startLine();
				t.accept(this);
				first = false;
			}
		}
		endLine();
		startLine();
		sb.append("=");
		if (fresh.size() > 0) {
			sb.append("[exists ");
			first = true;
			for (Variable sym : fresh) {
				if (!first) {
					sb.append(", ");
				}
				sb.append(sym.getName());
				first = false;
			}
			sb.append("]=");
		}
		sb.append(">");
		endLine();
		first = true;
		for (ITerm t : rule.getRHS()) {
			if (!first) {
				sb.append(".");
				endLine();
			}
			startLine();
			t.accept(this);
			first = false;
		}
		endLine();
		unindent();
	}

	private void fillHeadLine(IParameterized th, String keyword) {
		endLine();
		listComments(th);
		startLine();
		sb.append(keyword);
		sb.append(" ");
		if (stripOutput) {
			// drop the '__line' suffix, otherwise string comparison
			// does not work for testing
			String s = th.getName();
			int idx = s.indexOf("__line");
			if (idx > 0) {
				s = s.substring(0, idx);
			}
			sb.append(s);
		}
		else {
			sb.append(th.getName());
		}
		boolean first = true;
		if (th.getParameters().size() > 0) {
			sb.append("(");
			List<Variable> ordered = new ArrayList<Variable>();
			for (Variable par : th.getExplicitParameters()) {
				for (Variable v : th.getParameters()) {
					if (par.equals(v)) {
						ordered.add(v);
						break;
					}
				}
			}
			for (Variable v : th.getParameters()) {
				if (!ordered.contains(v)) {
					ordered.add(v);
				}
			}
			for (ISymbol s : ordered) {
				if (!first) {
					sb.append(", ");
				}
				sb.append(s.getName());
				first = false;
			}
			sb.append(")");
		}
		sb.append(" :=");
		endLine();
	}

	private void fillTerms(TermsHolder th, String separator) {
		boolean first = true;
		for (ITerm t : th.getTerms()) {
			if (!first) {
				sb.append(separator);
				endLine();
			}
			startLine();
			t.accept(this);
			first = false;
		}
		endLine();
	}

	@Override
	public void visit(ConstantTerm term) {
		sb.append(term.getSymbol().getName());
	}

	public void visit(NumericTerm term) {
		sb.append(term.getValue());
	}

	@Override
	public void visit(FunctionTerm term) {
		sb.append(term.getSymbol().getName());
		sb.append("(");
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
		sb.append(term.getSymbol().getName());
	}

	@Override
	public void visit(VariableTerm term) {
		sb.append(term.getSymbol().getName());
	}

	@Override
	public void visit(NegatedTerm term) {
		sb.append("not(");
		term.getBaseTerm().accept(this);
		sb.append(")");
	}

	public void visit(QuantifiedTerm term) {
		sb.append(term.isUniversal() ? "forall" : "exists");
		sb.append(" ");
		sb.append(term.getSymbol().getName());
		sb.append(" . ");
		term.getBaseTerm().accept(this);
	}

	private void listComments(IRepresentable r) {
		if (!stripOutput) {
			for (ICommentEntry s : r.getCommentLines()) {
				startLine();
				sb.append("%").append(s.getLine());
				endLine();
			}
		}
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
