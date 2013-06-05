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
import org.avantssar.commons.ChannelEntry;
import org.avantssar.commons.ErrorGatherer;
import org.avantssar.commons.LocationInfo;

public class RawLogicalExpression extends AbstractRawExpression {

	public enum LogicalOperator {
		Implication, Disjunction, Conjunction
	}

	private final IRawExpression left;
	private final IRawExpression right;
	private final LogicalOperator operator;

	public RawLogicalExpression(IScope scope, LocationInfo location, ErrorGatherer err, IRawExpression left, IRawExpression right, LogicalOperator operator) {
		super(scope, location, err);
		this.left = left;
		this.right = right;
		this.operator = operator;
	}

	public IRawExpression getLeft() {
		return left;
	}

	public IRawExpression getRight() {
		return right;
	}

	public LogicalOperator getOperator() {
		return operator;
	}

	@Override
	public void accept(IRawExpressionVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	public IExpression getFormula() {
		if (operator == LogicalOperator.Conjunction) {
			return getLeft().getFormula().and(getRight().getFormula());
		}
		else if (operator == LogicalOperator.Disjunction) {
			return getLeft().getFormula().or(getRight().getFormula());
		}
		else {
			return getLeft().getFormula().implies(getRight().getFormula());
		}
	}

	@Override
	public IExpression getGuard(boolean allowReceive) {
		// if (getRight() instanceof RawAnnotatedTransmissionExpression) {
		// ITerm sender = getLeft().getTerm(false, true);
		// RawAnnotatedTransmissionExpression rAnn =
		// (RawAnnotatedTransmissionExpression) getRight();
		// ITerm receiver = rAnn.getChannel().getTerm(false, true);
		// ITerm payload = rAnn.getPayload().getTerm(false, true);
		// ChannelEntry type = ChannelEntry.resilientRegular;
		// CommunicationTerm ct = getScope().communication(getLocation(),
		// sender, receiver, payload, type, false, false);
		// if (ct.isReceive()) {
		// if (allowReceive) {
		// return ct.expression();
		// }
		// else {
		// getErrorGatherer().addException(getLocation(),
		// ErrorMessages.ITEM_NOT_ALLOWED_IN_THIS_PLACE, "Receive",
		// getRepresentation(), "guard without receive");
		// return null;
		// }
		// }
		// else {
		// getErrorGatherer().addException(getLocation(),
		// ErrorMessages.ITEM_NOT_ALLOWED_IN_THIS_PLACE, "Send",
		// getRepresentation(), "guard");
		// return null;
		// }
		// }
		// else {
		if (operator == LogicalOperator.Conjunction) {
			return getLeft().getGuard(allowReceive).and(getRight().getGuard(allowReceive));
		}
		else if (operator == LogicalOperator.Disjunction) {
			return getLeft().getGuard(allowReceive).or(getRight().getGuard(allowReceive));
		}
		else {
			return getLeft().getGuard(allowReceive).implies(getRight().getGuard(allowReceive));
		}
		// }
	}

	@Override
	public ITerm getTerm(boolean allowTransmission, boolean strictVarCheck) {
		if (allowTransmission) {
			return getTransmission();
		}
		else {
			getErrorGatherer().addException(getLocation(), ErrorMessages.ITEM_NOT_ALLOWED_IN_THIS_PLACE, "Logical formula", getRepresentation(), "term");
			return null;
		}
	}

	@Override
	public CommunicationTerm getTransmission() {
		if (getRight() instanceof RawAnnotatedTransmissionExpression) {
			ITerm sender = getLeft().getTerm(false, true);
			RawAnnotatedTransmissionExpression rAnn = (RawAnnotatedTransmissionExpression) getRight();
			ITerm receiver = rAnn.getChannel().getTerm(false, true);
			ITerm payload = rAnn.getPayload().getTerm(false, true);
			ChannelEntry type = ChannelEntry.resilientRegular;
			CommunicationTerm ct = getScope().communication(getLocation(), sender, receiver, payload, null, type, false, false, false);
			return ct;
		}
		else {
			getErrorGatherer().addException(getLocation(), ErrorMessages.ITEM_NOT_ALLOWED_IN_THIS_PLACE, "Logical formula", getRepresentation(), "transmission");
			return null;
		}
	}

	@Override
	public IExpression getComparison() {
		getErrorGatherer().addException(getLocation(), ErrorMessages.ITEM_NOT_ALLOWED_IN_THIS_PLACE, "Logical formula", getRepresentation(), "(in)equality");
		return null;
	}

	@Override
	public boolean isChannelGoal() {
		return operator == LogicalOperator.Implication;
	}

	@Override
	public RawChannelGoalInfo getChannelGoal() {
		if (operator == LogicalOperator.Implication) {
			return new RawChannelGoalInfo(left, right, ChannelEntry.resilientRegular.arrow);
		}
		else {
			return null;
		}
	}

}
