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

import java.io.IOException;
import java.util.List;
import org.avantssar.commons.LocationInfo;

public interface IASLanSpec extends IRepresentable {

	static PrimitiveType MESSAGE = new PrimitiveType(null, null, "message", true);
	static PrimitiveType NAT = new PrimitiveType(null, null, "nat", MESSAGE, true);
	static PrimitiveType TEXT = new PrimitiveType(null, null, "text", MESSAGE, true);
	static PrimitiveType FACT = new PrimitiveType(null, null, "fact", true);
	static PrimitiveType PROTOCOL_ID = new PrimitiveType(null, null, "protocol_id", MESSAGE, true);
	static PrimitiveType AGENT = new PrimitiveType(null, null, "agent", MESSAGE, true);
	static PrimitiveType PUBLIC_KEY = new PrimitiveType(null, null, "public_key", AGENT, true);
	static PrimitiveType PRIVATE_KEY = new PrimitiveType(null, null, "private_key", MESSAGE, true);
	static PrimitiveType SYMMETRIC_KEY = new PrimitiveType(null, null, "symmetric_key", MESSAGE, true);
//  static PrimitiveType FUNCTION = new PrimitiveType(null, null, "function", MESSAGE, true);
	static PrimitiveType CHANNEL = new PrimitiveType(null, null, "channel", true);

	static Function IKNOWS = new Function(null, null, "iknows", FACT, true, MESSAGE);
	static Function PK = new Function(null, null, "pk", PUBLIC_KEY, false, AGENT);
	static Function INV = new Function(null, null, "inv", PRIVATE_KEY, true, PUBLIC_KEY);
	static Function CRYPT = new Function(null, null, "crypt", MESSAGE, true, PUBLIC_KEY, MESSAGE);
	static Function SIGN = new Function(null, null, "sign", MESSAGE, true, PRIVATE_KEY, MESSAGE);
	static Function SCRYPT = new Function(null, null, "scrypt", MESSAGE, true, SYMMETRIC_KEY, MESSAGE);
//	static Function HASH = new Function(null, null, "hash", MESSAGE, false, MESSAGE);
	static Function PAIR = new Function(null, null, "pair", MESSAGE, true, MESSAGE, MESSAGE);
	static Function SECRET = new Function(null, null, "secret", FACT, true, MESSAGE, PROTOCOL_ID, new SetType(null, null, AGENT));
	static Function REQUEST = new Function(null, null, "request", FACT, true, AGENT, AGENT, PROTOCOL_ID, MESSAGE, NAT);
	static Function WITNESS = new Function(null, null, "witness", FACT, true, AGENT, AGENT, PROTOCOL_ID, MESSAGE);
	static Function CONTAINS = new Function(null, null, "contains", FACT, true, MESSAGE, new SetType(null, null, MESSAGE));
	static Function NOT = new Function(null, null, "not", FACT, true, FACT);
	static Function AND = new Function(null, null, "and", FACT, true, FACT, FACT);
	static Function OR = new Function(null, null, "or", FACT, true, FACT, FACT);
	static Function IMPLIES = new Function(null, null, "implies", FACT, true, FACT, FACT);
	static Function EQUAL = new Function(null, null, "equal", FACT, true, MESSAGE, MESSAGE);
	static Function LEQ = new Function(null, null, "leq", FACT, true, MESSAGE, MESSAGE);
//	static Function APPLY = new Function(null, null, "apply", MESSAGE, true, FUNCTION, MESSAGE);	

	// for ACM:
	static Function SENT = new Function(null, null, "sent", FACT, false, AGENT, AGENT, AGENT, MESSAGE, CHANNEL);
	static Function RCVD = new Function(null, null, "rcvd", FACT, false,        AGENT, AGENT, MESSAGE, CHANNEL);
/*	static Function CONFIDENTIAL_TO = new Function(null, null, "confidential_to", FACT, true, CHANNEL, AGENT);
	static Function WEAKLY_CONFIDENTIAL = new Function(null, null, "weakly_confidential", FACT, true, CHANNEL);
	static Function AUTHENTIC_ON = new Function(null, null, "authentic_on", FACT, true, CHANNEL, AGENT);
	static Function WEAKLY_AUTHENTIC = new Function(null, null, "weakly_authentic", FACT, true, CHANNEL);
	static Function RESILIENT = new Function(null, null, "resilient", FACT, true, CHANNEL);
	static Function LINK = new Function(null, null, "link", FACT, true, CHANNEL, CHANNEL);
	static Function BILATERAL = new Function(null, null, "bilateral_conf_auth", FACT, true, CHANNEL, CHANNEL, AGENT, AGENT);
	static Function UNILATERAL = new Function(null, null, "unilateral_conf_auth", FACT, true, CHANNEL, CHANNEL, AGENT);
*/
	// LTL unary operators
	static Function LTL_NEXT = new Function(null, null, "X", FACT, true, FACT);
	static Function LTL_YESTERDAY = new Function(null, null, "Y", FACT, true, FACT);
	static Function LTL_FINALLY = new Function(null, null, "F", FACT, true, FACT);
	static Function LTL_ONCE = new Function(null, null, "O", FACT, true, FACT);
	static Function LTL_GLOBALLY = new Function(null, null, "G", FACT, true, FACT);
	static Function LTL_HISTORICALLY = new Function(null, null, "H", FACT, true, FACT);
	// LTL binary operators
	static Function LTL_UNTIL = new Function(null, null, "U", FACT, true, FACT, FACT);
	static Function LTL_RELEASE = new Function(null, null, "R", FACT, true, FACT, FACT);
	static Function LTL_SINCE = new Function(null, null, "S", FACT, true, FACT, FACT);

	static Constant INTRUDER = new Constant(null, null, "i", AGENT, true);

	void finish();

	PrimitiveType primitiveType(String name);

	PrimitiveType primitiveType(LocationInfo location, String name);

	List<PrimitiveType> getPrimitiveTypes();

	PairType pairType(IType left, IType right);

	PairType pairType(LocationInfo location, IType left, IType right);

	CompoundType compoundType(String operator, IType... argTypes);

	CompoundType compoundType(LocationInfo location, String operator, IType... argTypes);

	SetType setType(IType baseType);

	SetType setType(LocationInfo location, IType baseType);

	Variable variable(String name, IType type);

	Variable variable(LocationInfo location, String name, IType type);

	Variable findVariable(String name);

	List<Variable> getVariables();

	Constant constant(String name, IType type);

	Constant constant(LocationInfo location, String name, IType type);

	Constant findConstant(String name);

	List<Constant> getConstants();

	Function function(String name, IType returnType, IType... argTypes);

	Function function(LocationInfo location, String name, IType returnType, IType... argTypes);

	Function findFunction(String name);

	List<Function> getFunctions();

	Equation equation(ITerm leftTerm, ITerm rightTerm);

	Equation equation(LocationInfo location, ITerm leftTerm, ITerm rightTerm);

	List<Equation> getEquations();

	RewriteRule rule(String name);

	RewriteRule rule(LocationInfo location, String name);

	List<RewriteRule> getRules();

	RewriteRule findRule(String name);

	AttackState attackState(String name);

	AttackState attackState(LocationInfo location, String name);

	AttackState findAttackState(String name);

	List<AttackState> getAttackStates();

	Constraint constraint(String name, ITerm formula);

	Goal goal(String name, ITerm formula);

	Goal goal(LocationInfo location, String name, ITerm formula);

	Goal findGoal(String name);

	List<Constraint> getConstraints();

	List<Goal> getGoals();

	InitialState initialState(String name);

	InitialState initialState(LocationInfo location, String name);

	List<InitialState> getInitialStates();

	HornClause hornClause(String name, ITerm head);

	HornClause hornClause(LocationInfo location, String name, ITerm head);

	HornClause findHornClause(String name);

	List<HornClause> getHornClauses();

	NumericTerm numericTerm(int value);

	NumericTerm numericTerm(LocationInfo location, int value);

	void toFile(String fileName) throws IOException;

	String toPlainText();

	String toXML();

	String toJavaCode();

	String getStrippedRepresentation();

}
