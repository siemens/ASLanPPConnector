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

public class ConjunctionExpression extends AbstractBinaryExpression {

	protected ConjunctionExpression(IExpression leftExpression, IExpression rightExpression) {
		super(leftExpression, rightExpression, "&");
	}

	public IExpression pushDownNegations() {
		return new ConjunctionExpression(getLeftExpression().pushDownNegations(), getRightExpression().pushDownNegations());
	}

	public IExpression negate() {
		NegationExpression  leftNegated = new NegationExpression( getLeftExpression().duplicate());
		NegationExpression rightNegated = new NegationExpression(getRightExpression().duplicate());
		return new DisjunctionExpression(leftNegated.pushDownNegations(), rightNegated.pushDownNegations());
	}

	public IExpression applyDeMorgan() {
		IExpression  leftApplied =  getLeftExpression().applyDeMorgan();
		IExpression rightApplied = getRightExpression().applyDeMorgan();
		if (rightApplied instanceof DisjunctionExpression) {
			DisjunctionExpression rightDisj= (DisjunctionExpression) rightApplied;
			ConjunctionExpression newLeft  = new ConjunctionExpression(leftApplied.duplicate(), rightDisj. getLeftExpression().duplicate());
			ConjunctionExpression newRight = new ConjunctionExpression(leftApplied.duplicate(), rightDisj.getRightExpression().duplicate());
			return new DisjunctionExpression(newLeft.applyDeMorgan(), newRight.applyDeMorgan());
		}
		else if (leftApplied instanceof DisjunctionExpression) {
			DisjunctionExpression leftDisj = (DisjunctionExpression)  leftApplied;
			ConjunctionExpression newLeft  = new ConjunctionExpression(rightApplied.duplicate(), leftDisj. getLeftExpression().duplicate());
			ConjunctionExpression newRight = new ConjunctionExpression(rightApplied.duplicate(), leftDisj.getRightExpression().duplicate());
			return new DisjunctionExpression(newLeft.applyDeMorgan(), newRight.applyDeMorgan());
		}
		else {
			return duplicate();
		}
	}

	public IExpression duplicate() {
		return new ConjunctionExpression(getLeftExpression().duplicate(), getRightExpression().duplicate());
	}

	public IExpression expandAuxiliaryTerms() {
		return new ConjunctionExpression(getLeftExpression().expandAuxiliaryTerms(), getRightExpression().expandAuxiliaryTerms());
	}

	public boolean isAlwaysTrue() {
		return getLeftExpression().isAlwaysTrue() && getRightExpression().isAlwaysTrue();
	}

	public boolean isAlwaysFalse() {
		return getLeftExpression().isAlwaysFalse() || getRightExpression().isAlwaysFalse();
	}

	public IExpression reduce(SymbolsState symState) {
		return new ConjunctionExpression(getLeftExpression().reduce(symState), getRightExpression().reduce(symState));
	}

	@Override
	public void accept(IASLanPPVisitor visitor) {
		visitor.visit(this);
	}

}
