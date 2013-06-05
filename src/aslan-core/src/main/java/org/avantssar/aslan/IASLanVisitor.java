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

public interface IASLanVisitor {

	void visit(IASLanSpec spec);

	void visit(PrimitiveType type);

	void visit(PairType type);

	void visit(CompoundType type);

	void visit(SetType type);

	void visit(Function fnc);

	void visit(Constant cnst);

	void visit(Variable var);

	void visit(Equation eq);

	void visit(InitialState init);

	void visit(HornClause clause);

	void visit(RewriteRule rule);

	void visit(Constraint constraint);

	void visit(Goal goal);

	void visit(AttackState attack);

	void visit(ConstantTerm term);

	void visit(NumericTerm term);

	void visit(FunctionTerm term);

	void visit(FunctionConstantTerm term);

	void visit(VariableTerm term);

	void visit(NegatedTerm term);

	void visit(QuantifiedTerm term);

}
