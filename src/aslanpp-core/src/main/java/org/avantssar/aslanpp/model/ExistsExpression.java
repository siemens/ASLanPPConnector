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

import java.util.List;
import org.avantssar.aslanpp.visitors.IASLanPPVisitor;

public class ExistsExpression extends AbstractQuantifiedExpression {

	protected ExistsExpression(List<VariableSymbol> symbols, IExpression baseFormula) {
		super("exists", symbols, baseFormula);
	}

	public IExpression negate() {
		IExpression negatedBase = new NegationExpression(getBaseExpression().duplicate());
		return new ForallExpression(getSymbols(), negatedBase.pushDownNegations());
	}

	public IExpression pushDownNegations() {
		return new ExistsExpression(getSymbols(), getBaseExpression().pushDownNegations());
	}

	public IExpression applyDeMorgan() {
		return duplicate();
	}

	public IExpression duplicate() {
		return new ExistsExpression(getSymbols(), getBaseExpression().duplicate());
	}

	public IExpression expandAuxiliaryTerms() {
		return new ExistsExpression(getSymbols(), getBaseExpression().expandAuxiliaryTerms());
	}

	public boolean isAlwaysTrue() {
		return getBaseExpression().isAlwaysTrue();
	}

	public boolean isAlwaysFalse() {
		return getBaseExpression().isAlwaysFalse();
	}

	public IExpression reduce(SymbolsState symState) {
		return new ExistsExpression(getSymbols(), getBaseExpression().reduce(symState));
	}

	@Override
	public void accept(IASLanPPVisitor visitor) {
		visitor.visit(this);
	}
}
