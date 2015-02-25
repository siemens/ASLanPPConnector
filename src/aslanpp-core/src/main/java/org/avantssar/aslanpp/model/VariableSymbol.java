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

package org.avantssar.aslanpp.model;

import org.avantssar.aslanpp.model.VariableTerm.Kind;
import org.avantssar.aslanpp.visitors.IASLanPPVisitor;
import org.avantssar.commons.LocationInfo;

public class VariableSymbol extends AbstractSymbol {

	private final boolean unknownType;
	private boolean typeSet;
	private VariableSymbol referenceSymbol;
	private boolean freshSymbol;
	private boolean matchedSymbol;

	protected VariableSymbol(IScope owner, LocationInfo location, String name) {
		super(owner, location, name, owner.findType(Prelude.MESSAGE));
		typeSet = false;
		unknownType = true;
	}

	protected VariableSymbol(IScope owner, LocationInfo location, String name, IType type) {
		super(owner, location, name, type);
		if (getOwner().findType(Prelude.FACT).isAssignableFrom(type)) {
			getOwner().getErrorGatherer().addError(getLocation(), ErrorMessages.ELEMENT_OF_TYPE_FACT_NOT_ACCEPTED, getOriginalName(), type.getRepresentation());
		}
		typeSet = true;
		unknownType = false;
	}

	public boolean wasTypeSet() {
		return typeSet;
	}

	public boolean wasUntyped() {
		return unknownType;
	}

	public VariableTerm term() {
		return term(null, getOwner());
	}

	public VariableTerm term(LocationInfo location, IScope scope) {
		return new VariableTerm(location, scope, this, Kind.Regular);
	}

	public VariableTerm freshTerm() {
		return freshTerm(null);
	}

	public VariableTerm freshTerm(LocationInfo location) {
		return new VariableTerm(location, getOwner(), this, Kind.Fresh);
	}

	public VariableTerm matchedTerm() {
		return matchedTerm(null);
	}

	public VariableTerm matchedTerm(LocationInfo location) {
		return new VariableTerm(location, getOwner(), this, Kind.Matched);
	}

	// @Override
	// public ISymbol toASLan(IASLanSpec spec) {
	// return spec.variable(getName(), getType().toASLan(spec));
	// }

	public VariableSymbol getReferenceSymbol() {
		return referenceSymbol;
	}

	public void setReferenceSymbol(VariableSymbol referenceSymbol) {
		this.referenceSymbol = referenceSymbol;
	}

	public boolean isFreshSymbol() {
		return freshSymbol;
	}

	public void setFreshSymbol(boolean freshSymbol) {
		this.freshSymbol = freshSymbol;
	}

	public boolean isMatchedSymbol() {
		return matchedSymbol;
	}

	public void setMatchedSymbol(boolean matchedSymbol) {
		this.matchedSymbol = matchedSymbol;
	}

	public void setType(IType newType) {
		if (!unknownType) {
			getOwner().getErrorGatherer().addError(getLocation(), ErrorMessages.CANNOT_CHANGE_TYPE_OF_VARIABLE, getOriginalName(), getType().getRepresentation(), newType.getRepresentation());
		}
		if (getOwner().findType(Prelude.FACT).isAssignableFrom(newType)) {
			getOwner().getErrorGatherer().addError(getLocation(), ErrorMessages.ELEMENT_OF_TYPE_FACT_NOT_ACCEPTED, getOriginalName(), newType.getRepresentation());
		}
		type = newType;
		typeSet = true;
	}

	public void accept(IASLanPPVisitor visitor) {
		visitor.visit(this);
	}

}
