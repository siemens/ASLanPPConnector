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

public class NegationExpression extends AbstractExpression {

	private final IExpression baseExpression;

	public NegationExpression(IExpression baseExpression) {
		super(baseExpression.getLocation());
		this.baseExpression = baseExpression;
		super.addChildExpression(baseExpression);
	}

	public IExpression getBaseExpression() {
		return baseExpression;
	}

	public IExpression negate() {
		return baseExpression.pushDownNegations();
	}

	public IExpression pushDownNegations() {
		return baseExpression.negate();
	}

	public IExpression applyDeMorgan() {
		return duplicate();
	}

	public IExpression duplicate() {
		return new NegationExpression(baseExpression.duplicate());
	}

	@Override
	public boolean isPositive() {
		return !baseExpression.isPositive();
	}

	@Override
	public boolean isCondition() {
		return baseExpression.isCondition();
	}

	public IExpression expandAuxiliaryTerms() {
		return new NegationExpression(baseExpression.expandAuxiliaryTerms());
	}

	public boolean isAlwaysFalse() {
		return baseExpression.isAlwaysTrue();
	}

	public boolean isAlwaysTrue() {
		return baseExpression.isAlwaysFalse();
	}

	@Override
	public void buildContext(ExpressionContext ctx, boolean isInNegatedCondition) {
		baseExpression.buildContext(ctx, !isInNegatedCondition);
	}

	public boolean canHandleDeMorgan() {
		return baseExpression.canHandleDeMorgan();
	}

	public IExpression reduce(SymbolsState symState) {
		return new NegationExpression(baseExpression.reduce(symState));
	}

	@Override
	public void accept(IASLanPPVisitor visitor) {
		visitor.visit(this);
	}

}
