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

package org.avantssar.aslanpp.parser;

import org.avantssar.aslanpp.model.CommunicationTerm;
import org.avantssar.aslanpp.model.ErrorMessages;
import org.avantssar.aslanpp.model.IExpression;
import org.avantssar.aslanpp.model.IScope;
import org.avantssar.aslanpp.model.ITerm;
import org.avantssar.commons.ErrorGatherer;
import org.avantssar.commons.LocationInfo;

public class RawComparisonExpression extends AbstractRawExpression {

	private final IRawExpression left, right;
	private final boolean equality;

	private IExpression clean;

	public RawComparisonExpression(IScope scope, LocationInfo location, ErrorGatherer err, IRawExpression left, IRawExpression right, boolean equality) {
		super(scope, location, err);
		this.left = left;
		this.right = right;
		this.equality = equality;
	}

	public IRawExpression getLeft() {
		return left;
	}

	public IRawExpression getRight() {
		return right;
	}

	public boolean getEquality() {
		return equality;
	}

	@Override
	public boolean isComparison() {
		return true;
	}

	@Override
	public void accept(IRawExpressionVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	public IExpression getFormula() {
		if (clean == null) {
			ITerm lt = getLeft().getTerm(false, true);
			ITerm rt = getRight().getTerm(false, true);
			if (equality) {
				clean = lt.equality(rt);
			}
			else {
				clean = lt.inequality(rt);
			}
		}
		return clean;
	}

	@Override
	public IExpression getGuard(boolean allowReceive) {
		return getFormula();
	}

	@Override
	public ITerm getTerm(boolean allowTransmission, boolean strictVarCheck) {
		getErrorGatherer().addException(getLocation(), ErrorMessages.ITEM_NOT_ALLOWED_IN_THIS_PLACE, "(In)Equality", getRepresentation(), "term");
		return null;
	}

	@Override
	public CommunicationTerm getTransmission() {
		getErrorGatherer().addException(getLocation(), ErrorMessages.ITEM_NOT_ALLOWED_IN_THIS_PLACE, "(In)Equality", getRepresentation(), "transmission");
		return null;
	}

	@Override
	public IExpression getComparison() {
		return getFormula();
	}

	@Override
	public RawChannelGoalInfo getChannelGoal() {
		getErrorGatherer().addException(getLocation(), ErrorMessages.ITEM_NOT_ALLOWED_IN_THIS_PLACE, "(In)Equality", getRepresentation(), "channel goal");
		return null;
	}

}
