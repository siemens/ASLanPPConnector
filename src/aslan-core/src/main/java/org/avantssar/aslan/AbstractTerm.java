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

import org.avantssar.aslan.ImplicitExplicitSymbol.ImplicitExplicitState;
import org.avantssar.commons.ErrorGatherer;
import org.avantssar.commons.LocationInfo;

public abstract class AbstractTerm<T extends ISymbol> extends AbstractRepresentable implements ITerm {

	private final T symbol;

	protected AbstractTerm(LocationInfo location, ErrorGatherer err, T symbol) {
		super(location, err);
		this.symbol = symbol;
	}

	@Override
	public IType getType() {
		return symbol.getType();
	}

	protected T getSymbol() {
		return symbol;
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

	@Override
	public boolean isCondition() {
		return false;
	}

	public void setImplicitExplicitState(ImplicitExplicitState state) {}

}
