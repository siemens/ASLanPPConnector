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

import java.util.ArrayList;
import java.util.List;
import org.avantssar.aslanpp.model.CommunicationTerm;
import org.avantssar.aslanpp.model.ConcatTerm;
import org.avantssar.aslanpp.model.ErrorMessages;
import org.avantssar.aslanpp.model.IExpression;
import org.avantssar.aslanpp.model.IScope;
import org.avantssar.aslanpp.model.ITerm;
import org.avantssar.aslanpp.model.TupleTerm;
import org.avantssar.commons.ErrorGatherer;
import org.avantssar.commons.LocationInfo;

public class RawConcatenatedExpression extends AbstractRawExpression {

	private final List<IRawExpression> items = new ArrayList<IRawExpression>();
	private final boolean tuple;

	private ITerm clean;

	public RawConcatenatedExpression(IScope scope, LocationInfo location, ErrorGatherer err, List<IRawExpression> items, boolean tuple) {
		super(scope, location, err);
		this.items.addAll(items);
		this.tuple = tuple;
	}

	public List<IRawExpression> getItems() {
		return items;
	}

	public boolean isTuple() {
		return tuple;
	}

	@Override
	public void accept(IRawExpressionVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	public IExpression getFormula() {
		getErrorGatherer().addException(getLocation(), ErrorMessages.ITEM_NOT_ALLOWED_IN_THIS_PLACE, "Term concatenation", getRepresentation(), "formula");
		return null;
	}

	@Override
	public IExpression getGuard(boolean allowReceive) {
		getErrorGatherer().addException(getLocation(), ErrorMessages.ITEM_NOT_ALLOWED_IN_THIS_PLACE, "Term concatenation", getRepresentation(), "guard");
		return null;
	}

	@Override
	public ITerm getTerm(boolean allowTransmission, boolean strictVarCheck) {
		if (clean == null) {
			ITerm[] exprs = new ITerm[items.size()];
			for (int i = 0; i < items.size(); i++) {
				exprs[i] = items.get(i).getTerm(false, strictVarCheck);
			}
			if (tuple) {
				clean = TupleTerm.tuple(getLocation(), getScope(), exprs);
			}
			else {
				clean = ConcatTerm.concat(getLocation(), getScope(), exprs);
			}
		}
		return clean;
	}

	@Override
	public CommunicationTerm getTransmission() {
		getErrorGatherer().addException(getLocation(), ErrorMessages.ITEM_NOT_ALLOWED_IN_THIS_PLACE, "Term concatenation", getRepresentation(), "transmission");
		return null;
	}

	@Override
	public IExpression getComparison() {
		getErrorGatherer().addException(getLocation(), ErrorMessages.ITEM_NOT_ALLOWED_IN_THIS_PLACE, "Term concatenation", getRepresentation(), "(in)equality");
		return null;
	}

	@Override
	public RawChannelGoalInfo getChannelGoal() {
		getErrorGatherer().addException(getLocation(), ErrorMessages.ITEM_NOT_ALLOWED_IN_THIS_PLACE, "Term concatenation", getRepresentation(), "channel goal");
		return null;
	}

}
