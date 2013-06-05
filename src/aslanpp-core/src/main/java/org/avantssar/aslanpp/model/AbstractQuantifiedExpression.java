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

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractQuantifiedExpression extends AbstractExpression {

	@SuppressWarnings("unused")
	private final String operator;
	private final List<VariableSymbol> symbols = new ArrayList<VariableSymbol>();
	private final IExpression baseExpression;

	public AbstractQuantifiedExpression(String operator, List<VariableSymbol> symbols, IExpression baseExpression) {
		super(baseExpression.getLocation());
		this.operator = operator;
		if (symbols != null) {
			this.symbols.addAll(symbols);
		}
		this.baseExpression = baseExpression;

		super.addChildExpression(baseExpression);
	}

	public List<VariableSymbol> getSymbols() {
		return symbols;
	}

	public IExpression getBaseExpression() {
		return baseExpression;
	}

	@Override
	public List<IExpression> getAtomicExpressions(boolean forAttackState) {
		if (forAttackState) {
			return baseExpression.getAtomicExpressions(forAttackState);
		}
		else {
			return super.getAtomicExpressions(forAttackState);
		}
	}

	public boolean canHandleDeMorgan() {
		return false;
	}

	@Override
	public void buildContext(ExpressionContext ctx, boolean isInNegatedCondition) {
		super.buildContext(ctx, isInNegatedCondition);
		for (ISymbol sym : symbols) {
			if (sym.getOwner().participatesForSymbol(sym)) {
				ctx.addOwner(sym.getOwner());
			}
		}
	}

}
