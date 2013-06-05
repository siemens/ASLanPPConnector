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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;
import org.apache.xerces.dom.DocumentImpl;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

@SuppressWarnings("deprecation")
public class ASLanXMLSerializer implements IASLanVisitor {

	public static final String NAMESPACE = "http://avantssar.eu/";
	public static final String PARAMETER = "parameter";
	public static final String FLAG = "flag";
	public static final String METAINFO = "metainfo";
	public static final String COMMENT_LINE = "commentLine";
	public static final String COMMENTS = "comments";
	public static final String VALUE = "value";
	public static final String SIGNATURE = "signature";
	public static final String FORALL = "forall";
	public static final String NOT = "not";
	public static final String FUNCTION_CONSTANT = "functionConstant";
	public static final String NUMBER = "number";
	public static final String ATTACK_STATE = "attackState";
	public static final String GOAL = "goal";
	public static final String CONSTRAINT = "constraint";
	public static final String RHS = "rhs";
	public static final String EXISTS = "exists";
	public static final String CONDITIONS = "conditions";
	public static final String FACTS = "facts";
	public static final String LHS = "lhs";
	public static final String STEP = "step";
	public static final String BODY = "body";
	public static final String HEAD = "head";
	public static final String PARAMETERS = "parameters";
	public static final String HORN_CLAUSE = "hornClause";
	public static final String INITIAL_STATE = "initialState";
	public static final String EQUATION = "equation";
	public static final String VARIABLE = "variable";
	public static final String CONSTANT = "constant";
	public static final String PARAMETER_TYPES = "parameterTypes";
	public static final String RETURN_TYPE = "returnType";
	public static final String FUNCTION = "function";
	public static final String SET_TYPE = "setType";
	public static final String COMPOUND_TYPE = "compoundType";
	public static final String RIGHT = "right";
	public static final String LEFT = "left";
	public static final String PAIR_TYPE = "pairType";
	public static final String GOALS = "goals";
	public static final String CONSTRAINTS = "constraints";
	public static final String RULES = "rules";
	public static final String INITS = "inits";
	public static final String EQUATIONS = "equations";
	public static final String HORN_CLAUSES = "hornClauses";
	public static final String SUPER_TYPE = "superType";
	public static final String NAME = "name";
	public static final String TYPE = "type";
	public static final String TYPES = "types";
	public static final String ASLAN = "aslan";
	public static final String LTL = "ltl";
	public static final String POSITION = "position";
	public static final String FORMULA = "formula";

	private final Document doc;
	private final Stack<Element> stack = new Stack<Element>();

	public ASLanXMLSerializer() {
		doc = new DocumentImpl();
	}

	public String getDocument() {
		OutputFormat of = new OutputFormat("XML", "ISO-8859-1", true);
		of.setIndent(1);
		of.setIndenting(true);
		// of.setDoctype(null, "users.dtd");
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream(baos);
		XMLSerializer serializer = new XMLSerializer(ps, of);
		try {
			serializer.asDOMSerializer();
			serializer.serialize(doc.getDocumentElement());
			ps.close();
			return baos.toString();
		}
		catch (IOException e) {
			return null;
		}
	}

	@Override
	public void visit(IASLanSpec spec) {
		push(ASLAN);
		current().setAttribute("xmlns", NAMESPACE);
		addComments(spec);
		int superTypes = 0;
		for (PrimitiveType t : spec.getPrimitiveTypes()) {
			if (!t.isPrelude() && t.getSuperType() != null) {
				superTypes++;
			}
		}
		int nonPreludeFunctions = 0;
		for (Function f : spec.getFunctions()) {
			if (!f.isPrelude()) {
				nonPreludeFunctions++;
			}
		}
		if (superTypes > 0 || nonPreludeFunctions > 0) {
			push(SIGNATURE);
			for (PrimitiveType t : spec.getPrimitiveTypes()) {
				if (!t.isPrelude() && t.getSuperType() != null) {
					push(TYPE);
					current().setAttribute(NAME, t.getName());
					current().setAttribute(SUPER_TYPE, t.getSuperType().getName());
					pop();
				}
			}
			for (Function f : spec.getFunctions()) {
				if (!f.isPrelude()) {
					f.accept(this);
				}
			}
			pop();
		}
		int nonPreludeConstants = 0;
		for (Constant c : spec.getConstants()) {
			if (!c.isPrelude()) {
				nonPreludeConstants++;
			}
		}
		if (spec.getVariables().size() > 0 || nonPreludeConstants > 0) {
			push(TYPES);
			for (Variable v : spec.getVariables()) {
				v.accept(this);
			}
			for (Constant c : spec.getConstants()) {
				if (!c.isPrelude()) {
					c.accept(this);
				}
			}
			pop();
		}
		if (spec.getEquations().size() > 0) {
			push(EQUATIONS);
			for (Equation eq : spec.getEquations()) {
				eq.accept(this);
			}
			pop();
		}
		if (spec.getInitialStates().size() > 0) {
			push(INITS);
			for (InitialState is : spec.getInitialStates()) {
				is.accept(this);
			}
			pop();
		}
		if (spec.getHornClauses().size() > 0) {
			push(HORN_CLAUSES);
			for (HornClause hc : spec.getHornClauses()) {
				hc.accept(this);
			}
			pop();
		}
		if (spec.getRules().size() > 0) {
			push(RULES);
			for (RewriteRule rr : spec.getRules()) {
				rr.accept(this);
			}
			pop();
		}
		if (spec.getAttackStates().size() > 0 || spec.getGoals().size() > 0) {
			push(GOALS);
			for (AttackState as : spec.getAttackStates()) {
				as.accept(this);
			}
			for (Goal g : spec.getGoals()) {
				g.accept(this);
			}
			pop();
		}
		pop();
	}

	@Override
	public void visit(PrimitiveType type) {
		push(TYPE);
		current().setAttribute(NAME, type.getName());
		pop();
	}

	@Override
	public void visit(PairType type) {
		push(PAIR_TYPE);
		push(LEFT);
		type.getLeft().accept(this);
		pop();
		push(RIGHT);
		type.getRight().accept(this);
		pop();
		pop();
	}

	@Override
	public void visit(CompoundType type) {
		push(COMPOUND_TYPE);
		current().setAttribute(NAME, type.getName());
		for (IType t : type.getBaseTypes()) {
			t.accept(this);
		}
		pop();
	}

	@Override
	public void visit(SetType type) {
		push(SET_TYPE);
		type.getBaseType().accept(this);
		pop();
	}

	@Override
	public void visit(Function fnc) {
		push(FUNCTION);
		current().setAttribute(NAME, fnc.getName());
		addComments(fnc);
		push(RETURN_TYPE);
		fnc.getType().accept(this);
		pop();
		push(PARAMETER_TYPES);
		for (IType t : fnc.getArgumentsTypes()) {
			t.accept(this);
		}
		pop();
		pop();
	}

	@Override
	public void visit(Constant cnst) {
		push(CONSTANT);
		current().setAttribute(NAME, cnst.getName());
		addComments(cnst);
		cnst.getType().accept(this);
		pop();
	}

	@Override
	public void visit(Variable var) {
		push(VARIABLE);
		current().setAttribute(NAME, var.getName());
		addComments(var);
		var.getType().accept(this);
		pop();
	}

	@Override
	public void visit(Equation eq) {
		push(EQUATION);
		push(LEFT);
		eq.getLeftTerm().accept(this);
		pop();
		push(RIGHT);
		eq.getRightTerm().accept(this);
		pop();
		pop();
	}

	@Override
	public void visit(InitialState init) {
		push(INITIAL_STATE);
		current().setAttribute(NAME, init.getName());
		addComments(init);
		push(FACTS);
		for (ITerm t : init.getTerms()) {
			t.accept(this);
		}
		pop();
		pop();
	}

	@Override
	public void visit(HornClause clause) {
		push(HORN_CLAUSE);
		current().setAttribute(NAME, clause.getName());
		addComments(clause);
		outputParameters(clause);
		push(HEAD);
		clause.getHead().accept(this);
		pop();
		if (clause.getBodyFacts().size() > 0) {
			push(BODY);
			for (ITerm t : clause.getBodyFacts()) {
				t.accept(this);
			}
			pop();
		}
		pop();
	}

	private void outputRuleVar(Variable v, int index) {
		push(VARIABLE);
		current().setAttribute(NAME, v.getName());
		current().setAttribute(POSITION, Integer.toString(index));
		pop();
	}

	private void outputParameters(IParameterized p) {
		if (p.getParameters().size() > 0) {
			push(PARAMETERS);
			Set<Variable> used = new TreeSet<Variable>();
			int idx = 1;
			for (Variable par : p.getExplicitParameters()) {
				for (Variable v : p.getParameters()) {
					if (v.equals(par)) {
						used.add(v);
						outputRuleVar(v, idx++);
						break;
					}
				}
			}
			for (Variable v : p.getParameters()) {
				if (!used.contains(v)) {
					outputRuleVar(v, idx++);
				}
			}
			pop();
		}

	}

	@Override
	public void visit(RewriteRule rule) {
		push(STEP);
		current().setAttribute(NAME, rule.getName());
		addComments(rule);
		outputParameters(rule);
		push(LHS);
		int facts = 0;
		int conds = 0;
		for (ITerm t : rule.getLHS()) {
			if (t.isCondition()) {
				conds++;
			}
			else {
				facts++;
			}
		}
		if (facts > 0) {
			push(FACTS);
			for (ITerm t : rule.getLHS()) {
				if (!t.isCondition()) {
					t.accept(this);
				}
			}
			pop();
		}
		if (conds > 0) {
			push(CONDITIONS);
			for (ITerm t : rule.getLHS()) {
				if (t.isCondition()) {
					t.accept(this);
				}
			}
			pop();
		}
		pop();
		if (rule.getExists().size() > 0) {
			push(EXISTS);
			for (Variable v : rule.getExists()) {
				push(VARIABLE);
				current().setAttribute(NAME, v.getName());
				pop();
			}
			pop();
		}
		push(RHS);
		push(FACTS);
		for (ITerm t : rule.getRHS()) {
			t.accept(this);
		}
		pop();
		pop();
		pop();
	}

	@Override
	public void visit(Constraint constraint) {
		push(CONSTRAINT);
		current().setAttribute(NAME, constraint.getName());
		addComments(constraint);
		outputParameters(constraint);
		push(FORMULA);
		constraint.getFormula().accept(this);
		pop();
		pop();
	}

	@Override
	public void visit(Goal goal) {
		push(GOAL);
		current().setAttribute(NAME, goal.getName());
		addComments(goal);
		outputParameters(goal);
		push(FORMULA);
		goal.getFormula().accept(this);
		pop();
		pop();
	}

	@Override
	public void visit(AttackState attack) {
		push(ATTACK_STATE);
		current().setAttribute(NAME, attack.getName());
		addComments(attack);
		outputParameters(attack);
		int facts = 0;
		int conds = 0;
		for (ITerm t : attack.getTerms()) {
			if (t.isCondition()) {
				conds++;
			}
			else {
				facts++;
			}
		}
		if (facts > 0) {
			push(FACTS);
			for (ITerm t : attack.getTerms()) {
				if (!t.isCondition()) {
					t.accept(this);
				}
			}
			pop();
		}
		if (conds > 0) {
			push(CONDITIONS);
			for (ITerm t : attack.getTerms()) {
				if (t.isCondition()) {
					t.accept(this);
				}
			}
			pop();
		}
		pop();
	}

	@Override
	public void visit(ConstantTerm term) {
		push(CONSTANT);
		current().setAttribute(NAME, term.getSymbol().getName());
		pop();
	}

	@Override
	public void visit(NumericTerm term) {
		push(NUMBER);
		current().setAttribute(VALUE, Integer.toString(term.getValue()));
		pop();
	}

	@Override
	public void visit(FunctionTerm term) {
		if (Character.isUpperCase(term.getSymbol().getName().charAt(0))) {
			push(LTL);
		}
		else {
			push(FUNCTION);
		}
		current().setAttribute(NAME, term.getSymbol().getName());
		push(PARAMETERS);
		for (ITerm t : term.getParameters()) {
			t.accept(this);
		}
		pop();
		pop();
	}

	@Override
	public void visit(FunctionConstantTerm term) {
		push(FUNCTION_CONSTANT);
		current().setAttribute(NAME, term.getSymbol().getName());
		pop();
	}

	@Override
	public void visit(VariableTerm term) {
		push(VARIABLE);
		current().setAttribute(NAME, term.getSymbol().getName());
		pop();
	}

	@Override
	public void visit(NegatedTerm term) {
		push(NOT);
		term.getBaseTerm().accept(this);
		pop();
	}

	@Override
	public void visit(QuantifiedTerm term) {
		if (term.isUniversal()) {
			push(FORALL);
		}
		else {
			push(EXISTS);
		}
		current().setAttribute(VARIABLE, term.getSymbol().getName());
		term.getBaseTerm().accept(this);
		pop();
	}

	private void addComments(IRepresentable repr) {
		if (repr.getCommentLines().size() > 0) {
			push(COMMENTS);
			for (ICommentEntry s : repr.getCommentLines()) {
				if (s instanceof MetaInfo) {
					MetaInfo mi = (MetaInfo) s;
					push(METAINFO);
					current().setAttribute(NAME, mi.getName());
					for (String f : mi.getFlags()) {
						push(FLAG);
						current().appendChild(doc.createCDATASection(f));
						pop();
					}
					for (String k : mi.getParameters().keySet()) {
						push(PARAMETER);
						push(NAME);
						current().appendChild(doc.createCDATASection(k));
						pop();
						push(VALUE);
						current().appendChild(doc.createCDATASection(mi.getParameters().get(k)));
						pop();
						pop();
					}
					pop();
				}
				else {
					push(COMMENT_LINE);
					current().appendChild(doc.createCDATASection(s.getLine()));
					pop();
				}
			}
			pop();
		}
	}

	private void push(String s) {
		Element el = doc.createElement(s);
		if (!stack.isEmpty()) {
			current().appendChild(el);
		}
		else {
			doc.appendChild(el);
		}
		stack.push(el);
	}

	private Element current() {
		return stack.peek();
	}

	private Element pop() {
		return stack.pop();
	}
}
