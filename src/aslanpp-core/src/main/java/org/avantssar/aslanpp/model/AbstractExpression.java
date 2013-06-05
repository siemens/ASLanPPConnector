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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.avantssar.aslanpp.visitors.PrettyPrinter;
import org.avantssar.commons.LocationInfo;
import org.avantssar.aslanpp.visitors.MatchedVariablesExtractor;

public abstract class AbstractExpression implements IExpression {

	private final List<IExpression> childrenExpressions = new ArrayList<IExpression>();
	private final List<ChannelGoal> channelGoals = new ArrayList<ChannelGoal>();
	private final LocationInfo location;

	protected AbstractExpression(LocationInfo location) {
		this.location = location;
	}

	public LocationInfo getLocation() {
		return location;
	}

	protected void addChildExpression(IExpression e) {
		childrenExpressions.add(e);
	}

	public IExpression dropOuterForall() {
		return this;
	}

	public IExpression toDNF() {
		IExpression newExpr = pushDownNegations().applyDeMorgan();
		for (ChannelGoal g : channelGoals) {
			newExpr.attachChannelGoal(g);
		}
		return newExpr;
	}

	public List<IExpression> getConjunctions() {
		List<IExpression> conjunctions = new ArrayList<IExpression>();
		conjunctions.add(duplicate());
		return conjunctions;
	}

	public boolean isPositive() {
		return true;
	}

	public boolean isCondition() {
		return false;
	}

	public IExpression getGloballySubterm() {
		return null;
	}
	
	public boolean discardOnRHS() {
		return false;
	}

	public List<IExpression> getAtomicExpressions(boolean forAttackState) {
		List<IExpression> result = new ArrayList<IExpression>();
		result.add(duplicate());
		return result;
	}

	public int compareTo(IExpression o) {
		return getRepresentation().compareTo(o.getRepresentation());
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof IExpression) {
			IExpression ot = (IExpression) other;
			return compareTo(ot) == 0;
		} else {
			return false;
		}
	}

	public void buildContext(ExpressionContext ctx, boolean isInNegatedCondition) {
		for (IExpression e : childrenExpressions) {
			e.buildContext(ctx, isInNegatedCondition);
		}
	}

	public void useContext(ExpressionContext ctx, SymbolsState symState) {
		for (IExpression e : childrenExpressions) {
			e.useContext(ctx, symState);
		}
	}

	public ConjunctionExpression and(IExpression other) {
		return new ConjunctionExpression(this, other);
	}

	public DisjunctionExpression or(IExpression other) {
		return new DisjunctionExpression(this, other);
	}

	public ImplicationExpression implies(IExpression other) {
		return new ImplicationExpression(this, other);
	}

	public ForallExpression forall(VariableSymbol... symbols) {
		return new ForallExpression(Arrays.asList(symbols), this);
	}

	public ExistsExpression exists(VariableSymbol... symbols) {
		return new ExistsExpression(Arrays.asList(symbols), this);
	}

	public NegationExpression not() {
		return new NegationExpression(this);
	}

	public String getOriginalRepresentation() {
		PrettyPrinter pp = new PrettyPrinter(false);
		accept(pp);
		return pp.toString();
	}

	@Override
	public String getRepresentation() {
		PrettyPrinter pp = new PrettyPrinter(true);
		accept(pp);
		return pp.toString();
	}

	@Override
	public String toString() {
		return getRepresentation();
	}

	public void attachChannelGoal(ChannelGoal goal) {
		channelGoals.add(goal);
	}

	public List<ChannelGoal> getChannelGoals() {
		return channelGoals;
	}

	public IExpression reduceForAttackState() {
		return this.duplicate();
	}

  public Map<VariableSymbol, Boolean> getMatchedVariables() {
    MatchedVariablesExtractor matchesExtractor = new MatchedVariablesExtractor();
    accept(matchesExtractor);
		return matchesExtractor.getMatchedVariables();
  }
}
