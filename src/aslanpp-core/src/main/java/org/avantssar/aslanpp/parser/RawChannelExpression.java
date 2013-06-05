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

public class RawChannelExpression extends AbstractRawExpression {

	private final IRawExpression sender;
	private final IRawExpression receiver;
	private final String type;
	private final boolean named;

	public RawChannelExpression(IScope scope, LocationInfo location, ErrorGatherer err, IRawExpression sender, IRawExpression receiver, String type, boolean named) {
		super(scope, location, err);
		this.sender = sender;
		this.receiver = receiver;
		this.type = type;
		this.named = named;
	}

	public IRawExpression getSender() {
		return sender;
	}

	public IRawExpression getReceiver() {
		return receiver;
	}

	public String getType() {
		return type;
	}

	public boolean isNamed() {
		return named;
	}

	@Override
	public void accept(IRawExpressionVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	public IExpression getFormula() {
		getErrorGatherer().addException(getLocation(), ErrorMessages.INCOMPLETE_TRANSMISSION_TERM, getRepresentation());
		return null;
	}

	@Override
	public IExpression getGuard(boolean allowReceive) {
		getErrorGatherer().addException(getLocation(), ErrorMessages.INCOMPLETE_TRANSMISSION_TERM, getRepresentation());
		return null;
	}

	@Override
	public ITerm getTerm(boolean allowTransmission, boolean strictVarCheck) {
		getErrorGatherer().addException(getLocation(), ErrorMessages.INCOMPLETE_TRANSMISSION_TERM, getRepresentation());
		return null;
	}

	@Override
	public CommunicationTerm getTransmission() {
		getErrorGatherer().addException(getLocation(), ErrorMessages.INCOMPLETE_TRANSMISSION_TERM, getRepresentation());
		return null;
	}

	@Override
	public IExpression getComparison() {
		getErrorGatherer().addException(getLocation(), ErrorMessages.INCOMPLETE_TRANSMISSION_TERM, getRepresentation());
		return null;
	}

	@Override
	public boolean isChannelGoal() {
		return !named;
	}

	@Override
	public RawChannelGoalInfo getChannelGoal() {
		if (!named) {
			return new RawChannelGoalInfo(sender, receiver, type);
		}
		else {
			getErrorGatherer().addException(getLocation(), ErrorMessages.INCOMPLETE_TRANSMISSION_TERM, getRepresentation());
			return null;
		}
	}

}
