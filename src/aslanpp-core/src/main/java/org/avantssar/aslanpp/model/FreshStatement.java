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

public class FreshStatement extends AbstractStatement {

	private final VariableTerm symbolTerm;
	private VariableSymbol freshSymbol;

	protected FreshStatement(LocationInfo location, VariableTerm symbolTerm) {
		super(location);
		this.symbolTerm = symbolTerm;
	}

	public VariableSymbol getSymbol() {
		return symbolTerm.getSymbol();
	}

	public VariableTerm getSymbolTerm() {
		return symbolTerm;
	}

	public VariableSymbol getFreshSymbol() {
		return freshSymbol;
	}

	public void setFreshSymbol(VariableSymbol freshSymbol) {
		this.freshSymbol = freshSymbol;
	}

	@Override
	public void accept(IASLanPPVisitor visitor) {
		visitor.visit(this);
	}

}
