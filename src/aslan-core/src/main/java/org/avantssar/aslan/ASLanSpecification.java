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

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.avantssar.commons.ErrorGatherer;
import org.avantssar.commons.LocationInfo;

public class ASLanSpecification extends AbstractRepresentable implements IASLanSpec {

	private final Map<String, PrimitiveType> primitiveTypesByName = new TreeMap<String, PrimitiveType>();
	private final Map<String, Variable> variablesByName = new TreeMap<String, Variable>();
	private final Map<String, Constant> constantsByName = new TreeMap<String, Constant>();
	private final Map<String, Function> functionsByName = new TreeMap<String, Function>();
	private final List<HornClause> hornClauses = new ArrayList<HornClause>();
	private final List<Equation> equations = new ArrayList<Equation>();
	private List<InitialState> initialStates = new ArrayList<InitialState>();
	private List<RewriteRule> rules = new ArrayList<RewriteRule>();
	private List<AttackState> attackStates = new ArrayList<AttackState>();
	private List<Goal> goals = new ArrayList<Goal>();
	private List<Constraint> constraints = new ArrayList<Constraint>();

	protected ASLanSpecification(ErrorGatherer err, ISymbolsProvider... extraDefaults) {
		super(null, err);
		registerPrelude();
		if (extraDefaults != null) {
			for (ISymbolsProvider prov : extraDefaults) {
				for (IType t : prov.getPrimitiveTypes()) {
					checkPrimitiveTypesRecursive(t);
				}
				for (Function f : prov.getFunctions(getErrorGatherer())) {
					functionsByName.put(f.getName(), f);
				}
			}
		}
	}

	public void finish() {
		Validator v = new Validator(getErrorGatherer());
		accept(v);
	}

	public PrimitiveType primitiveType(String name) {
		return primitiveType(null, name);
	}

	public PrimitiveType primitiveType(LocationInfo location, String name) {
		if (primitiveTypesByName.containsKey(name)) {
			return primitiveTypesByName.get(name);
		}
		else {
			PrimitiveType t = new PrimitiveType(location, getErrorGatherer(), name, false);
			primitiveTypesByName.put(t.getName(), t);
			return t;
		}
	}

	public List<PrimitiveType> getPrimitiveTypes() {
		List<PrimitiveType> result = new ArrayList<PrimitiveType>();
		result.addAll(primitiveTypesByName.values());
		return result;
	}

	public PairType pairType(IType left, IType right) {
		return pairType(null, left, right);
	}

	public PairType pairType(LocationInfo location, IType left, IType right) {
		checkPrimitiveTypesRecursive(left);
		checkPrimitiveTypesRecursive(right);
		return new PairType(location, getErrorGatherer(), left, right);
	}

	public CompoundType compoundType(String operator, IType... argTypes) {
		return compoundType(null, operator, argTypes);
	}

	public CompoundType compoundType(LocationInfo location, String operator, IType... argTypes) {
		for (IType t : argTypes) {
			checkPrimitiveTypesRecursive(t);
		}
		return new CompoundType(location, getErrorGatherer(), operator, argTypes);
	}

	public SetType setType(IType baseType) {
		return setType(null, baseType);
	}

	public SetType setType(LocationInfo location, IType baseType) {
		checkPrimitiveTypesRecursive(baseType);
		return new SetType(location, getErrorGatherer(), baseType);
	}

	private void checkPrimitiveTypesRecursive(IType type) {
		if (type instanceof PrimitiveType) {
			PrimitiveType pt = (PrimitiveType) type;
			if (!primitiveTypesByName.containsKey(pt.getName())) {
				primitiveTypesByName.put(pt.getName(), pt);
			}
		}
		else if (type instanceof CompoundType) {
			for (IType t : ((CompoundType) type).getBaseTypes()) {
				checkPrimitiveTypesRecursive(t);
			}
		}
		else if (type instanceof SetType) {
			checkPrimitiveTypesRecursive(((SetType) type).getBaseType());
		}
	}

	public Variable variable(String name, IType type) {
		return variable(null, name, type);
	}

	public Variable variable(LocationInfo location, String name, IType type) {
		Variable existing = findVariable(name);
		if (existing != null) {
			if (existing.getName().equals(name)) {
				if (existing.getType().equals(type)) {
					return existing;
				}
				else {
					getErrorGatherer().addException(location, ASLanErrorMessages.DUPLICATE_SYMBOL, "variable", name);
				}
			}
		}
		Variable v = new Variable(location, getErrorGatherer(), name, type);
		variablesByName.put(v.getName(), v);
		return v;
	}

	public Variable findVariable(String name) {
		return variablesByName.get(AbstractNamed.escape(name));
	}

	public List<Variable> getVariables() {
		List<Variable> result = new ArrayList<Variable>();
		result.addAll(variablesByName.values());
		return result;
	}

	public Constant constant(String name, IType type) {
		return constant(null, name, type);
	}

	public Constant constant(LocationInfo location, String name, IType type) {
		Constant existing = findConstant(name);
		if (existing != null) {
			if (existing.getName().equals(name)) {
				if (existing.getType().equals(type)) {
					return existing;
				}
				else {
					getErrorGatherer().addException(location, ASLanErrorMessages.DUPLICATE_SYMBOL, "constant", name);
				}
			}
		}
		Function f = findFunction(name);
		if (f != null) {
			if (f.getName().equals(name)) {
				getErrorGatherer().addException(location, ASLanErrorMessages.NAME_IS_ALREADY_USED, "function", name);
			}
		}
		Constant c = new Constant(location, getErrorGatherer(), name, type, false);
		constantsByName.put(c.getName(), c);
		return c;
	}

	public Constant findConstant(String name) {
		return constantsByName.get(AbstractNamed.escape(name));
	}

	public List<Constant> getConstants() {
		List<Constant> result = new ArrayList<Constant>();
		result.addAll(constantsByName.values());
		return result;
	}

	public Function function(String name, IType returnType, IType... argTypes) {
		return function(null, name, returnType, argTypes);
	}

	public Function function(LocationInfo location, String name, IType returnType, IType... argTypes) {
		Function existing = findFunction(name);
		if (existing != null) {
			if (existing.getName().equals(name)) {
				if (existing.matchesSignature(returnType, argTypes)) {
					return existing;
				}
				else {
					getErrorGatherer().addException(location, ASLanErrorMessages.DUPLICATE_SYMBOL, "function", name);
				}
			}
		}
		Constant c = findConstant(name);
		if (c != null) {
			if (c.getName().equals(name)) {
				getErrorGatherer().addException(location, ASLanErrorMessages.NAME_IS_ALREADY_USED, "constant", name);
			}
		}
		Function f = new Function(location, getErrorGatherer(), name, returnType, false, argTypes);
		functionsByName.put(f.getName(), f);
		return f;
	}

	public Function findFunction(String name) {
		return functionsByName.get(AbstractNamed.escape(name));
	}

	public List<Function> getFunctions() {
		List<Function> result = new ArrayList<Function>();
		result.addAll(functionsByName.values());
		return result;
	}

	public Equation equation(ITerm leftTerm, ITerm rightTerm) {
		return equation(null, leftTerm, rightTerm);
	}

	public Equation equation(LocationInfo location, ITerm leftTerm, ITerm rightTerm) {
		Equation e = new Equation(location, getErrorGatherer(), leftTerm, rightTerm);
		equations.add(e);
		return e;
	}

	public List<Equation> getEquations() {
		return equations;
	}

	public RewriteRule rule(String name) {
		return rule(null, name);
	}

	public RewriteRule rule(LocationInfo location, String name) {
		RewriteRule rule = new RewriteRule(location, getErrorGatherer(), name);
		rules.add(rule);
		return rule;
	}

	public List<RewriteRule> getRules() {
		return rules;
	}

	public RewriteRule findRule(String name) {
		for (RewriteRule r : rules) {
			if (r.getName().equals(name)) {
				return r;
			}
		}
		return null;
	}

	public void setRules(List<RewriteRule> rules) {
		this.rules = rules;
	}

	public AttackState attackState(String name) {
		return attackState(null, name);
	}

	public AttackState attackState(LocationInfo location, String name) {
		AttackState att = new AttackState(location, getErrorGatherer(), name);
		attackStates.add(att);
		Collections.sort(attackStates);
		return att;
	}

	public AttackState findAttackState(String name) {
		for (AttackState as : attackStates) {
			if (as.getName().equals(name)) {
				return as;
			}
		}
		return null;
	}

	public List<AttackState> getAttackStates() {
		return attackStates;
	}

	public void setAttackStates(List<AttackState> attackStates) {
		this.attackStates = attackStates;
	}

	public Goal goal(String name, ITerm formula) {
		return goal(null, name, formula);
	}

	public Goal goal(LocationInfo location, String name, ITerm formula) {
		Goal g = new Goal(location, getErrorGatherer(), name, formula);
		goals.add(g);
		Collections.sort(goals);
		return g;
	}

	public Goal findGoal(String name) {
		for (Goal g : goals) {
			if (g.getName().equals(name)) {
				return g;
			}
		}
		return null;
	}

	public List<Goal> getGoals() {
		return goals;
	}

	public void setGoals(List<Goal> goals) {
		this.goals = goals;
	}

	public Constraint constraint(String name, ITerm formula) {
		return constraint(null, name, formula);
	}

	public Constraint constraint(LocationInfo location, String name, ITerm formula) {
		Constraint c = new Constraint(location, getErrorGatherer(), name, formula);
		constraints.add(c);
		Collections.sort(constraints);
		return c;
	}

	public Constraint findConstraint(String name) {
		for (Constraint c : constraints) {
			if (c.getName().equals(name)) {
				return c;
			}
		}
		return null;
	}

	public List<Constraint> getConstraints() {
		return constraints;
	}

	public void setConstraints(List<Constraint> constraints) {
		this.constraints = constraints;
	}

	public InitialState initialState(String name) {
		return initialState(null, name);
	}

	public InitialState initialState(LocationInfo location, String name) {
		InitialState is = new InitialState(location, getErrorGatherer(), name);
		initialStates.add(is);
		return is;
	}

	public List<InitialState> getInitialStates() {
		return initialStates;
	}

	public void setInitialStates(List<InitialState> initialStates) {
		this.initialStates = initialStates;
	}

	public HornClause hornClause(String name, ITerm head) {
		return hornClause(null, name, head);
	}

	public HornClause hornClause(LocationInfo location, String name, ITerm head) {
		HornClause hc = new HornClause(location, getErrorGatherer(), name, head);
		hornClauses.add(hc);
		return hc;
	}

	public List<HornClause> getHornClauses() {
		return hornClauses;
	}

	public HornClause findHornClause(String name) {
		for (HornClause cl : hornClauses) {
			if (cl.getName().equals(name)) {
				return cl;
			}
		}
		return null;
	}

	public NumericTerm numericTerm(int value) {
		return NumericTerm.fromValue(value, getErrorGatherer());
	}

	public NumericTerm numericTerm(LocationInfo location, int value) {
		return new NumericTerm(location, getErrorGatherer(), value);
	}

	private void registerPrelude() {
		Field[] predefinedTypes = IASLanSpec.class.getDeclaredFields();
		for (Field f : predefinedTypes) {
			if (Modifier.isStatic(f.getModifiers()) && Modifier.isPublic(f.getModifiers())) {
				if (IType.class.isAssignableFrom(f.getType())) {
					try {
						IType t = (IType) f.get(null);
						checkPrimitiveTypesRecursive(t);
					}
					catch (Exception e) {
						System.out.println("Failed to register prelude type '" + f.getName() + "': " + e.getMessage());
					}
				}
				else if (Function.class.isAssignableFrom(f.getType())) {
					try {
						Function fnc = (Function) f.get(null);
						functionsByName.put(fnc.getName(), fnc);
					}
					catch (Exception e) {
						System.out.println("Failed to register prelude function '" + f.getName() + "': " + e.getMessage());
					}
				}
				else if (Constant.class.isAssignableFrom(f.getType())) {
					try {
						Constant cnst = (Constant) f.get(null);
						constantsByName.put(cnst.getName(), cnst);
					}
					catch (Exception e) {
						System.out.println("Failed to register prelude constant '" + f.getName() + "': " + e.getMessage());
					}
				}
			}
		}
	}

	@Override
	public void toFile(String fileName) throws IOException {
		FileOutputStream fos = new FileOutputStream(fileName);
		toStream(fos);
	}

	public void toStream(OutputStream out) throws IOException {
		String content = toPlainText();
		out.write(content.getBytes());
	}

	public String toPlainText() {
		PrettyPrinter pp = new PrettyPrinter(false);
		accept(pp);
		return pp.toString();
	}

	public String toXML() {
		ASLanXMLSerializer serializer = new ASLanXMLSerializer();
		accept(serializer);
		return serializer.getDocument();
	}

	public String toJavaCode() {
		ASLanCodeSerializer serializer = new ASLanCodeSerializer();
		accept(serializer);
		return serializer.getCode();
	}

	public String getStrippedRepresentation() {
		PrettyPrinter pp = new PrettyPrinter(true);
		accept(pp);
		return pp.toString();
	}

	public void accept(IASLanVisitor visitor) {
		visitor.visit(this);
	}
}
