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

public class RawPseudonymExpression extends AbstractRawExpression {

	private final IRawExpression real;
	private final IRawExpression pseudonym;

	private ITerm clean;

	public RawPseudonymExpression(IScope scope, LocationInfo location, ErrorGatherer err, IRawExpression real) {
		this(scope, location, err, real, null);
	}

	public RawPseudonymExpression(IScope scope, LocationInfo location, ErrorGatherer err, IRawExpression real, IRawExpression pseudonym) {
		super(scope, location, err);
		this.real = real;
		this.pseudonym = pseudonym;
	}

	public IRawExpression getReal() {
		return real;
	}

	public IRawExpression getPseudonym() {
		return pseudonym;
	}

	@Override
	public void accept(IRawExpressionVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	public IExpression getFormula() {
		getErrorGatherer().addException(getLocation(), ErrorMessages.ITEM_NOT_ALLOWED_IN_THIS_PLACE, "Pseudonym", getRepresentation(), "formula");
		return null;
	}

	@Override
	public IExpression getGuard(boolean allowReceive) {
		getErrorGatherer().addException(getLocation(), ErrorMessages.ITEM_NOT_ALLOWED_IN_THIS_PLACE, "Pseudonym", getRepresentation(), "guard");
		return null;
	}

	@Override
	public ITerm getTerm(boolean allowTransmission, boolean strictVarCheck) {
		if (clean == null) {
			if (pseudonym == null) {
				clean = real.getTerm(false, strictVarCheck).defaultPseudonym();
			}
			else {
				clean = real.getTerm(false, strictVarCheck).pseudonym(pseudonym.getTerm(false, strictVarCheck));
			}
		}
		return clean;
	}

	@Override
	public CommunicationTerm getTransmission() {
		getErrorGatherer().addException(getLocation(), ErrorMessages.ITEM_NOT_ALLOWED_IN_THIS_PLACE, "Pseudonym", getRepresentation(), "transmission");
		return null;
	}

	@Override
	public IExpression getComparison() {
		getErrorGatherer().addException(getLocation(), ErrorMessages.ITEM_NOT_ALLOWED_IN_THIS_PLACE, "Pseudonym", getRepresentation(), "(in)equality");
		return null;
	}

	@Override
	public RawChannelGoalInfo getChannelGoal() {
		getErrorGatherer().addException(getLocation(), ErrorMessages.ITEM_NOT_ALLOWED_IN_THIS_PLACE, "Pseudonym", getRepresentation(), "channel goal");
		return null;
	}

}
