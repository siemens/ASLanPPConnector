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

public class EqualityExpression extends AbstractExpression {

	private ITerm leftTerm;
	private ITerm rightTerm;

	protected EqualityExpression(ITerm leftTerm, ITerm rightTerm) {
		super(leftTerm.getLocation());
		this.leftTerm = leftTerm;
		this.rightTerm = rightTerm;
	}

	public ITerm getLeftTerm() {
		return leftTerm;
	}

	public void setLeftTerm(ITerm term) {
		this.leftTerm = term;
	}

	public ITerm getRightTerm() {
		return rightTerm;
	}

	public void setRightTerm(ITerm term) {
		this.rightTerm = term;
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
		return new EqualityExpression(leftTerm, rightTerm);
	}

	@Override
	public boolean isCondition() {
		return true;
	}

	@Override
	public void buildContext(ExpressionContext ctx, boolean isInNegatedCondition) {
		leftTerm.buildContext(ctx, isInNegatedCondition);
		rightTerm.buildContext(ctx, isInNegatedCondition);
	}

	public IExpression expandAuxiliaryTerms() {
		IExpression result = duplicate();
		ExpressionContext ctx = new ExpressionContext();
		leftTerm.buildContext(ctx, false);
		List<ITerm> aux = ctx.getAuxiliaryTerms();
		if (aux != null) {
			for (ITerm term : aux) {
				result = new ConjunctionExpression(result, new BaseExpression(term));
			}
		}
		ctx = new ExpressionContext();
		rightTerm.buildContext(ctx, false);
		aux = ctx.getAuxiliaryTerms();
		if (aux != null) {
			for (ITerm term : aux) {
				result = new ConjunctionExpression(result, new BaseExpression(term));
			}
		}
		return result;
	}

	public boolean isAlwaysFalse() {
		return false;
	}

	public boolean isAlwaysTrue() {
		return (leftTerm.compareTo(rightTerm) == 0);
	}

	public boolean canHandleDeMorgan() {
		return true;
	}

	@Override
	public void useContext(ExpressionContext ctx, SymbolsState symState) {
		leftTerm.useContext(ctx, symState);
		rightTerm.useContext(ctx, symState);
	}

	public IExpression reduce(SymbolsState symState) {
		return new EqualityExpression(leftTerm.reduce(symState), rightTerm.reduce(symState));
	}

	@Override
	public void accept(IASLanPPVisitor visitor) {
		visitor.visit(this);
	}

}
