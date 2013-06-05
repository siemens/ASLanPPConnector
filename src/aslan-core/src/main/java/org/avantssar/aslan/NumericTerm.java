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

package org.avantssar.aslan;

import java.util.HashMap;
import java.util.Map;
import org.avantssar.aslan.ImplicitExplicitSymbol.ImplicitExplicitState;
import org.avantssar.commons.ErrorGatherer;
import org.avantssar.commons.LocationInfo;

public class NumericTerm extends AbstractRepresentable implements ITerm {

	private static Map<Integer, NumericTerm> valuesToTerms = new HashMap<Integer, NumericTerm>();

	protected static NumericTerm fromValue(int value, ErrorGatherer err) {
		if (valuesToTerms.containsKey(value)) {
			return valuesToTerms.get(value);
		}
		else {
			NumericTerm term = new NumericTerm(null, err, value);
			valuesToTerms.put(value, term);
			return term;
		}
	}

	private final int value;

	protected NumericTerm(LocationInfo location, ErrorGatherer err, int value) {
		super(location, err);
		this.value = value;
	}

	public int getValue() {
		return value;
	}

	@Override
	public void buildContext(TermContext ctx, boolean isInNegatedTerm) {
	// nothing to do here
	}

	@Override
	public IType getType() {
		return IASLanSpec.NAT;
	}

	@Override
	public boolean isCondition() {
		return false;
	}

	public ITerm negate() {
		return negate(null);
	}

	public ITerm negate(LocationInfo location) {
		return new NegatedTerm(location, getErrorGatherer(), this);
	}

	public ITerm exists(Variable v) {
		return exists(null, v);
	}

	public ITerm exists(LocationInfo location, Variable v) {
		return new QuantifiedTerm(location, getErrorGatherer(), v, this, false);
	}

	public ITerm forall(Variable v) {
		return forall(null, v);
	}

	public ITerm forall(LocationInfo location, Variable v) {
		return new QuantifiedTerm(location, getErrorGatherer(), v, this, true);
	}

	public void accept(IASLanVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	public void setImplicitExplicitState(ImplicitExplicitState state) {}

}
