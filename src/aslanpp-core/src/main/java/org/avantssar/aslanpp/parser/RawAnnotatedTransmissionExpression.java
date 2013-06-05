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

import org.avantssar.aslanpp.model.AbstractTerm;
import org.avantssar.aslanpp.model.CommunicationTerm;
import org.avantssar.aslanpp.model.ErrorMessages;
import org.avantssar.aslanpp.model.IExpression;
import org.avantssar.aslanpp.model.IScope;
import org.avantssar.aslanpp.model.ITerm;
import org.avantssar.aslanpp.parser.RawLogicalExpression.LogicalOperator;
import org.avantssar.commons.ChannelEntry;
import org.avantssar.commons.ErrorGatherer;
import org.avantssar.commons.LocationInfo;

public class RawAnnotatedTransmissionExpression extends AbstractRawExpression {

	private final IRawExpression channel;
	private final IRawExpression payload;

	private CommunicationTerm clean;

	public RawAnnotatedTransmissionExpression(IScope scope, LocationInfo location, ErrorGatherer err, IRawExpression channel, IRawExpression payload) {
		super(scope, location, err);
		this.channel = channel;
		this.payload = payload;
	}

	public IRawExpression getChannel() {
		return channel;
	}

	public IRawExpression getPayload() {
		return payload;
	}

	@Override
	public void accept(IRawExpressionVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	public IExpression getFormula() {
		getErrorGatherer().addException(getLocation(), ErrorMessages.ITEM_NOT_ALLOWED_IN_THIS_PLACE, "Transmission", getRepresentation(), "formula");
		return null;
	}

	@Override
	public IExpression getGuard(boolean allowReceive) {
		CommunicationTerm ct = getTransmission();
		if (ct.isReceive()) {
			if (allowReceive) {
				return ct.expression();
			}
			else {
				getErrorGatherer().addException(getLocation(), ErrorMessages.ITEM_NOT_ALLOWED_IN_THIS_PLACE, "Receive", getRepresentation(), "guard without receive");
				return null;
			}
		}
		else {
			getErrorGatherer().addException(getLocation(), ErrorMessages.ITEM_NOT_ALLOWED_IN_THIS_PLACE, "Send", getRepresentation(), "guard");
			return null;
		}
	}

	@Override
	public ITerm getTerm(boolean allowTransmission, boolean strictVarCheck) {
		if (allowTransmission) {
			return getTransmission();
		}
		else {
			getErrorGatherer().addException(getLocation(), ErrorMessages.ITEM_NOT_ALLOWED_IN_THIS_PLACE, "Transmission", getRepresentation(), "term without transmission");
			return null;
		}
	}

	@Override
	public CommunicationTerm getTransmission() {
		if (clean == null) {
			ITerm sender = null;
			ITerm receiver = null;
			ITerm channel_ = null;
			ChannelEntry type = ChannelEntry.regular;
			IRawExpression ch = channel;
			while (ch instanceof RawParenthesisExpression) {
				ch = ((RawParenthesisExpression) ch).getBase();
			}
			if (ch instanceof RawChannelExpression) {
				RawChannelExpression rCh = (RawChannelExpression) ch;
				if (rCh.isNamed()) {
					channel_ = AbstractTerm.constVar(getScope(), rCh.getType());
					if (channel_ == null) {
						getErrorGatherer().addException(getLocation(), ErrorMessages.UNDEFINED_CHANNEL, rCh.getType(), getScope().getOriginalName());
					}
				}
				sender = rCh.getSender().getTerm(false, true);
				receiver = rCh.getReceiver().getTerm(false, true);
				type = ChannelEntry.getByKey(rCh.getType(), rCh.isNamed());
			}
			else if (ch instanceof RawOOPCallExpression) {
				RawOOPCallExpression rOOP = (RawOOPCallExpression) ch;
				sender = rOOP.getCaller().getTerm(false, true);
				receiver = rOOP.getWhat().getTerm(false, true);
				type = ChannelEntry.regular;
			}
			else if (ch instanceof RawLogicalExpression) {
				RawLogicalExpression rLog = (RawLogicalExpression) ch;
				if (rLog.getOperator() == LogicalOperator.Implication) {
					sender = rLog.getLeft().getTerm(false, true);
					receiver = rLog.getRight().getTerm(false, true);
					type = ChannelEntry.resilientRegular;
				}
				else {
					getErrorGatherer().addException(getLocation(), ErrorMessages.DIFFERENT_ITEM_EXPECTED, "channel", channel.getRepresentation());
				}
			}
			else {
				getErrorGatherer().addException(getLocation(), ErrorMessages.DIFFERENT_ITEM_EXPECTED, "channel", channel.getRepresentation());
			}
			ITerm payloadClean = payload.getTerm(false, true);
			clean = getScope().communication(getLocation(), sender, receiver, payloadClean, channel_, type, false, false, false);
		}
		return clean;
	}

	@Override
	public IExpression getComparison() {
		getErrorGatherer().addException(getLocation(), ErrorMessages.ITEM_NOT_ALLOWED_IN_THIS_PLACE, "Transmission", getRepresentation(), "(in)equality");
		return null;
	}

	@Override
	public RawChannelGoalInfo getChannelGoal() {
		getErrorGatherer().addException(getLocation(), ErrorMessages.ITEM_NOT_ALLOWED_IN_THIS_PLACE, "Transmission", getRepresentation(), "channel goal");
		return null;
	}

}
