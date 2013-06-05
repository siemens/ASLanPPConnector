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

public class BaseExpression extends AbstractExpression {

	private ITerm baseTerm;

	protected BaseExpression(ITerm baseTerm) {
		super(baseTerm.getLocation());
		this.baseTerm = baseTerm;
	}

	public ITerm getBaseTerm() {
		return baseTerm;
	}

	public void setBaseTerm(ITerm term) {
		baseTerm = term;
	}

	public IExpression negate() {
		return new NegationExpression(duplicate());
	}

	public IExpression pushDownNegations() {
		return duplicate();
	}

	public IExpression applyDeMorgan() {
		return duplicate();
	}

	public IExpression duplicate() {
		return new BaseExpression(baseTerm);
	}

	@Override
	public boolean discardOnRHS() {
		return baseTerm.discardOnRHS();
	}

	public IExpression expandAuxiliaryTerms() {
		IExpression result = duplicate();
		ExpressionContext ctx = new ExpressionContext();
		baseTerm.buildContext(ctx, false);
		List<ITerm> aux = ctx.getAuxiliaryTerms();
		if (aux != null) {
			for (ITerm term : aux) {
				result = new ConjunctionExpression(result, new BaseExpression(term));
			}
		}
		return result;
	}

	public boolean isAlwaysFalse() {
		if (baseTerm instanceof ConstantTerm) {
			ConstantTerm ct = (ConstantTerm) baseTerm;
			if (ct.getSymbol().getName().equals(Prelude.FALSE)) {
				return true;
			}
		}
		return false;
	}

	public boolean isAlwaysTrue() {
		if (baseTerm instanceof ConstantTerm) {
			ConstantTerm ct = (ConstantTerm) baseTerm;
			if (ct.getSymbol().getName().equals(Prelude.TRUE)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void buildContext(ExpressionContext ctx, boolean isInNegatedCondition) {
		baseTerm.buildContext(ctx, isInNegatedCondition);
	}

	public boolean canHandleDeMorgan() {
		return true;
	}

	@Override
	public void useContext(ExpressionContext ctx, SymbolsState symState) {
		baseTerm.useContext(ctx, symState);
	}

	public IExpression reduce(SymbolsState symState) {
		return new BaseExpression(baseTerm.reduce(symState));
	}

	@Override
	public void accept(IASLanPPVisitor visitor) {
		visitor.visit(this);
	}

}
