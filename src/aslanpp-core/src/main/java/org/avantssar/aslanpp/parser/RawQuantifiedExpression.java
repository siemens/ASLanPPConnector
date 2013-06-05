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

import java.util.LinkedHashMap;
import org.avantssar.aslanpp.model.CommunicationTerm;
import org.avantssar.aslanpp.model.ErrorMessages;
import org.avantssar.aslanpp.model.IExpression;
import org.avantssar.aslanpp.model.IScope;
import org.avantssar.aslanpp.model.ITerm;
import org.avantssar.aslanpp.model.VariableSymbol;
import org.avantssar.commons.ErrorGatherer;
import org.avantssar.commons.LocationInfo;

public class RawQuantifiedExpression extends AbstractRawExpression {

	private final LinkedHashMap<String, LocationInfo> vars = new LinkedHashMap<String, LocationInfo>();
	private final IRawExpression base;
	private final boolean universal;

	private IExpression clean;

	public RawQuantifiedExpression(IScope scope, LocationInfo location, ErrorGatherer err, boolean universal, LinkedHashMap<String, LocationInfo> vars, IRawExpression base) {
		super(scope, location, err);
		this.universal = universal;
		this.vars.putAll(vars);
		this.base = base;
	}

	public boolean getUniversal() {
		return universal;
	}

	public LinkedHashMap<String, LocationInfo> getVars() {
		return vars;
	}

	public IRawExpression getBase() {
		return base;
	}

	@Override
	public void accept(IRawExpressionVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	public IExpression getFormula() {
		if (clean == null) {
			VariableSymbol[] ucs = new VariableSymbol[vars.size()];
			int i = 0;
			for (String s : vars.keySet()) {
				LocationInfo loc = vars.get(s);
				VariableSymbol v = getScope().addUntypedVariable(s, loc);
				ucs[i++] = v;
			}
			if (universal) {
				clean = base.getFormula().forall(ucs);
			}
			else {
				clean = base.getFormula().exists(ucs);
			}
		}
		return clean;
	}

	@Override
	public IExpression getGuard(boolean allowReceive) {
		getErrorGatherer().addException(getLocation(), ErrorMessages.ITEM_NOT_ALLOWED_IN_THIS_PLACE, "Quantified expression", getRepresentation(), "guard");
		return null;
	}

	@Override
	public ITerm getTerm(boolean allowTransmission, boolean strictVarCheck) {
		getErrorGatherer().addException(getLocation(), ErrorMessages.ITEM_NOT_ALLOWED_IN_THIS_PLACE, "Quantified expression", getRepresentation(), "term");
		return null;
	}

	@Override
	public CommunicationTerm getTransmission() {
		getErrorGatherer().addException(getLocation(), ErrorMessages.ITEM_NOT_ALLOWED_IN_THIS_PLACE, "Quantified expression", getRepresentation(), "transmission");
		return null;
	}

	@Override
	public IExpression getComparison() {
		getErrorGatherer().addException(getLocation(), ErrorMessages.ITEM_NOT_ALLOWED_IN_THIS_PLACE, "Quantified expression", getRepresentation(), "(in)equality");
		return null;
	}

	@Override
	public RawChannelGoalInfo getChannelGoal() {
		getErrorGatherer().addException(getLocation(), ErrorMessages.ITEM_NOT_ALLOWED_IN_THIS_PLACE, "Quantified expression", getRepresentation(), "channel goal");
		return null;
	}

}
