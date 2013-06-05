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

public class RawNumericExpression extends AbstractRawExpression {

	private int value;

	private ITerm clean;

	public RawNumericExpression(IScope scope, LocationInfo location, ErrorGatherer err, String valueAsString) {
		super(scope, location, err);
		try {
			this.value = Integer.parseInt(valueAsString);
		}
		catch (NumberFormatException ex) {
			getErrorGatherer().addException(location, ErrorMessages.INVALID_NUMERIC_FORMAT, valueAsString);
		}
	}

	public RawNumericExpression(IScope scope, LocationInfo location, ErrorGatherer err, int value) {
		super(scope, location, err);
		this.value = value;
	}

	public int getValue() {
		return value;
	}

	@Override
	public void accept(IRawExpressionVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	public IExpression getFormula() {
		getErrorGatherer().addException(getLocation(), ErrorMessages.ITEM_NOT_ALLOWED_IN_THIS_PLACE, "Numeral", getRepresentation(), "formula");
		return null;
	}

	@Override
	public IExpression getGuard(boolean allowReceive) {
		getErrorGatherer().addException(getLocation(), ErrorMessages.ITEM_NOT_ALLOWED_IN_THIS_PLACE, "Numeral", getRepresentation(), "guard");
		return null;
	}

	@Override
	public ITerm getTerm(boolean allowTransmission, boolean strictVarCheck) {
		if (clean == null) {
			clean = getScope().numericTerm(value, getLocation());
		}
		return clean;
	}

	@Override
	public CommunicationTerm getTransmission() {
		getErrorGatherer().addException(getLocation(), ErrorMessages.ITEM_NOT_ALLOWED_IN_THIS_PLACE, "Numeral", getRepresentation(), "transmission");
		return null;
	}

	@Override
	public IExpression getComparison() {
		getErrorGatherer().addException(getLocation(), ErrorMessages.ITEM_NOT_ALLOWED_IN_THIS_PLACE, "Numeral", getRepresentation(), "comparison");
		return null;
	}

	@Override
	public RawChannelGoalInfo getChannelGoal() {
		getErrorGatherer().addException(getLocation(), ErrorMessages.ITEM_NOT_ALLOWED_IN_THIS_PLACE, "Numeral", getRepresentation(), "channel goal");
		return null;
	}

}
