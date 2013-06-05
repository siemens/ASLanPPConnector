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
import org.avantssar.aslanpp.model.Prelude;
import org.avantssar.commons.ChannelEntry;
import org.avantssar.commons.ErrorGatherer;
import org.avantssar.commons.LocationInfo;

public class RawFunctionTransmissionExpression extends AbstractRawExpression {

	private final IRawExpression base;
	private final String type;

	private CommunicationTerm clean;

	public RawFunctionTransmissionExpression(IScope scope, LocationInfo location, ErrorGatherer err, IRawExpression base, String type) {
		super(scope, location, err);
		this.base = base;
		this.type = type;
	}

	public IRawExpression getBase() {
		return base;
	}

	public String getType() {
		return type;
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
			ITerm payload = null;
			ITerm channel = null;
			boolean receive = false;
			boolean renderOOP = false;
			IRawExpression ch = base;
			while (ch instanceof RawParenthesisExpression) {
				ch = ((RawParenthesisExpression) ch).getBase();
			}
			if (ch instanceof RawOOPCallExpression) {
				RawOOPCallExpression rOOP = (RawOOPCallExpression) ch;
				sender = rOOP.getCaller().getTerm(false, true);
				IRawExpression wh = rOOP.getWhat();
				while (wh instanceof RawParenthesisExpression) {
					wh = ((RawParenthesisExpression) wh).getBase();
				}
				if (wh instanceof RawFunctionExpression) {
					RawFunctionExpression rFnc = (RawFunctionExpression) wh;
					if (rFnc.getName().equals(Prelude.SEND) || rFnc.getName().equals(Prelude.RECEIVE)) {
						if (rFnc.getParameters().size() != 2) {
							getErrorGatherer().addException(rFnc.getLocation(), ErrorMessages.WRONG_NUMBER_OF_PARAMETERS, "Function", rFnc.getName(), 2, rFnc.getParameters().size());
						}
						receiver = rFnc.getParameters().get(0).getTerm(false, true);
						payload = rFnc.getParameters().get(1).getTerm(false, true);
						receive = rFnc.getName().equals(Prelude.RECEIVE);
						renderOOP = true;
					}
					else {
						getErrorGatherer().addException(getLocation(), ErrorMessages.DIFFERENT_ITEM_EXPECTED, "send/receive", rFnc.getRepresentation());
					}
				}
				else {
					getErrorGatherer().addException(getLocation(), ErrorMessages.DIFFERENT_ITEM_EXPECTED, "send/receive", rOOP.getWhat().getRepresentation());
				}
			}
			else if (ch instanceof RawFunctionExpression) {
				RawFunctionExpression rFnc = (RawFunctionExpression) ch;
				if (rFnc.getName().equals(Prelude.SEND) || rFnc.getName().equals(Prelude.RECEIVE)) {
					if (rFnc.getParameters().size() != 3) {
						getErrorGatherer().addException(rFnc.getLocation(), ErrorMessages.WRONG_NUMBER_OF_PARAMETERS, "Function", rFnc.getName(), 3, rFnc.getParameters().size());
					}
					sender = rFnc.getParameters().get(0).getTerm(false, true);
					receiver = rFnc.getParameters().get(1).getTerm(false, true);
					payload = rFnc.getParameters().get(2).getTerm(false, true);
					receive = rFnc.getName().equals(Prelude.RECEIVE);
				}
				else {
					getErrorGatherer().addException(getLocation(), ErrorMessages.DIFFERENT_ITEM_EXPECTED, "send/receive", rFnc.getRepresentation());
				}
			}
			else {
				getErrorGatherer().addException(getLocation(), ErrorMessages.DIFFERENT_ITEM_EXPECTED, "send/receive", base.getRepresentation());
			}
			// first try the default channel types
			ChannelEntry type = ChannelEntry.getByKey(getType(), false);
			// if not found, then try with a named channel
			if (type == null) {
				channel = AbstractTerm.constVar(getScope(), getType());
				if (channel == null) {
					getErrorGatherer().addException(getLocation(), ErrorMessages.UNDEFINED_CHANNEL, getType(), getScope().getOriginalName());
				}
				type = ChannelEntry.getByKey(getType(), true);
			}
			if (receive) {
				ITerm aux = sender;
				sender = receiver;
				receiver = aux;
			}
			clean = getScope().communication(getLocation(), sender, receiver, payload, channel, type, receive, true, renderOOP);
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
