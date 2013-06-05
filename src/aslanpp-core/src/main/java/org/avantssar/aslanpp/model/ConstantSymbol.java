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

import org.avantssar.aslanpp.visitors.IASLanPPVisitor;
import org.avantssar.commons.LocationInfo;

public class ConstantSymbol extends ImplicitExplicitSymbol {

	private final ConstantTerm term;
	private boolean nonPublic;
	private ISymbol referenceSymbol;

	protected ConstantSymbol(IScope owner, LocationInfo location, String name, IType type) {
		super(owner, location, name, type);
		term = new ConstantTerm(null, getOwner(), this);
	}

	public boolean isNonPublic() {
		return nonPublic;
	}

	public void setNonPublic(boolean nonPublic) {
		this.nonPublic = nonPublic;
	}

	public BaseExpression expr() {
		return term().expression();
	}

	public ConstantTerm term() {
		return term;
	}

	public ConstantTerm term(LocationInfo location, IScope scope) {
		return new ConstantTerm(location, scope, this);
	}

	public ISymbol getReferenceSymbol() {
		return referenceSymbol;
	}

	public void setReferenceSymbol(ISymbol referenceSymbol) {
		this.referenceSymbol = referenceSymbol;
	}

	public void accept(IASLanPPVisitor visitor) {
		visitor.visit(this);
	}

}
