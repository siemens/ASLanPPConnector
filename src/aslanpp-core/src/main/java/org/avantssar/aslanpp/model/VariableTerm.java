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

import org.avantssar.aslanpp.model.ExpressionContext.NegatedConditionState;
import org.avantssar.aslanpp.visitors.IASLanPPVisitor;
import org.avantssar.commons.LocationInfo;

public class VariableTerm extends AbstractSymbolTerm<VariableSymbol> {

	protected static enum Kind {
		Regular, Matched, Fresh
	}

	private Kind kind;
	private VariableSymbol dummySymbol;

	protected VariableTerm(LocationInfo location, IScope scope, VariableSymbol symbol, Kind kind) {
		super(location, scope, symbol, false);
		this.kind = kind;
	}

	public boolean isMatched() {
		return kind == Kind.Matched;
	}

	public void setMatched(VariableSymbol dummySymbol) {
		this.kind = Kind.Matched;
		setDummySymbol(dummySymbol);
		dummySymbol.setReferenceSymbol(getSymbol());
		dummySymbol.setMatchedSymbol(true);
	}

	public boolean isFresh() {
		return kind == Kind.Fresh;
	}

	public VariableSymbol getDummySymbol() {
		return dummySymbol;
	}

	public void setDummySymbol(VariableSymbol dummy) {
		dummySymbol = dummy;
	}

	@Override
	public void buildContext(ExpressionContext ctx, boolean newNegatedCondition) {
		super.buildContext(ctx, newNegatedCondition);
		NegatedConditionState isInNegatedCondition = ctx.getState(getSymbol());
		if (newNegatedCondition) {
			if (isInNegatedCondition == NegatedConditionState.Unknown) {
				isInNegatedCondition = NegatedConditionState.OnlyNegative;
			}
			else if (isInNegatedCondition == NegatedConditionState.OnlyPositive) {
				isInNegatedCondition = NegatedConditionState.Both;
			}
		}
		else {
			if (isInNegatedCondition == NegatedConditionState.Unknown) {
				isInNegatedCondition = NegatedConditionState.OnlyPositive;
			}
			else if (isInNegatedCondition == NegatedConditionState.OnlyNegative) {
				isInNegatedCondition = NegatedConditionState.Both;
			}
		}
		ctx.setState(getSymbol(), isInNegatedCondition);

		if (kind == Kind.Matched) {
			ctx.addMatch(getSymbol(), getDummySymbol());
		}

		IScope owner = getSymbol().getOwner();
		if (owner != null && owner.participatesForSymbol(getSymbol())) {
			ctx.setOwner(getSymbol(), owner);
		}

	}

	@Override
	public boolean holdsActor() {
		return Entity.ACTOR_PREFIX.equals(getSymbol().getOriginalName());
	}

	public ITerm reduce(SymbolsState symState) {	// TODO rename reduce to substitute
		if (symState.isAssigned(getSymbol())) {
			ITerm assignedTerm = symState.getAssignedValue(getSymbol());
			if (!assignedTerm.equals(this)) {
				return assignedTerm;
			}
		}
		return this;
	}

	@Override
	public void useContext(ExpressionContext ctx, SymbolsState symState) {
		if (kind == Kind.Matched) {
			if (ctx.isForceReplacementOfMatches() || ctx.getState(getSymbol()) != NegatedConditionState.OnlyNegative) {
				if (!symState.isAssigned(getSymbol())) {
					symState.assign(getSymbol(), dummySymbol.term());
				}
			}
		}
	}

	@Override
	public ITerm accept(IASLanPPVisitor visitor) {
		return visitor.visit(this);
	}

	@Override
	public boolean wasTypeSet() {
		return getSymbol().wasTypeSet();
	}

	@Override
	public boolean isTypeCertain() {
		return !getSymbol().wasUntyped();
	}

}
