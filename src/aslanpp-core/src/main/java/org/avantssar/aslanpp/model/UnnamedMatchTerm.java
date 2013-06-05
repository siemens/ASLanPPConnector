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

public class UnnamedMatchTerm extends AbstractTerm {

	private VariableSymbol dummySymbol;

	protected UnnamedMatchTerm(LocationInfo location, IScope scope) {
		super(location, scope, false);
	}

	public VariableSymbol getDummySymbol() {
		return dummySymbol;
	}

	public void setDummySymbol(VariableSymbol dummy) {
		dummySymbol = dummy;
	}

	@Override
	public ITerm accept(IASLanPPVisitor visitor) {
		return visitor.visit(this);
	}

	@Override
	public IType inferType() {
		return getDummySymbol().getType();
	}

	@Override
	public ITerm reduce(SymbolsState symState) {
		return this;
	}

	@Override
	public boolean isTypeCertain() {
		return false;
	}

}
