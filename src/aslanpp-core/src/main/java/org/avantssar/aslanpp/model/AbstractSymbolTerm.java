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

import org.avantssar.commons.LocationInfo;

public abstract class AbstractSymbolTerm<T extends ISymbol> extends
		AbstractTerm {

	private final T symbol;

	protected AbstractSymbolTerm(LocationInfo location, IScope scope, T symbol,
			boolean discardOnRHS) {
		super(location, scope, discardOnRHS);
		if (symbol == null) {
			throw new IllegalArgumentException(
					"Cannot instantiate term based on null symbol.");
		}
		this.symbol = symbol;
	}

	public T getSymbol() {
		return symbol;
	}

	public IType inferType() {
		return symbol.getType();
	}

}
