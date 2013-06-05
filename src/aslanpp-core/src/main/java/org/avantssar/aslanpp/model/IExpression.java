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

package org.avantssar.aslanpp.model;

import java.util.List;
import java.util.Map;

import org.avantssar.aslanpp.IReduceable;
import org.avantssar.aslanpp.visitors.IASLanPPVisitor;
import org.avantssar.commons.LocationInfo;

public interface IExpression extends IReduceable<IExpression>,
		IChannelGoalHolder, Comparable<IExpression> {

	LocationInfo getLocation();

	boolean canHandleDeMorgan();

	IExpression duplicate();

	IExpression pushDownNegations();

	IExpression dropOuterForall();

	IExpression negate();

	IExpression applyDeMorgan();

	IExpression toDNF();

	IExpression expandAuxiliaryTerms();

	List<IExpression> getConjunctions();

	List<IExpression> getAtomicExpressions(boolean forAttackState);

	boolean isPositive();

	boolean isCondition();

	IExpression getGloballySubterm();
	
	boolean discardOnRHS();

	boolean isAlwaysTrue();

	boolean isAlwaysFalse();

	ConjunctionExpression and(IExpression other);

	DisjunctionExpression or(IExpression other);

	ImplicationExpression implies(IExpression other);

	ForallExpression forall(VariableSymbol... symbols);

	ExistsExpression exists(VariableSymbol... symbols);

	NegationExpression not();

	void accept(IASLanPPVisitor visitor);

	String getOriginalRepresentation();

	String getRepresentation();

	IExpression reduceForAttackState();

  Map<VariableSymbol, Boolean> getMatchedVariables();
}
