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

public class ImplicationExpression extends AbstractBinaryExpression {

	protected ImplicationExpression(IExpression leftExpression, IExpression rightExpression) {
		super(leftExpression, rightExpression, "=>");
	}

	public IExpression applyDeMorgan() {
		return unroll().applyDeMorgan();
	}

	public IExpression negate() {
		return unroll().negate();
	}

	public IExpression pushDownNegations() {
		return unroll().pushDownNegations();
	}

	public IExpression duplicate() {
		return new ImplicationExpression(getLeftExpression().duplicate(), getRightExpression().duplicate());
	}

	public IExpression unroll() {
		return new DisjunctionExpression(new NegationExpression(getLeftExpression().duplicate()), getRightExpression().duplicate());
	}

	public IExpression expandAuxiliaryTerms() {
		return new ImplicationExpression(getLeftExpression().expandAuxiliaryTerms(), getRightExpression().expandAuxiliaryTerms());
	}

	public boolean isAlwaysFalse() {
		return unroll().isAlwaysFalse();
	}

	public boolean isAlwaysTrue() {
		return unroll().isAlwaysTrue();
	}

	public IExpression reduce(SymbolsState symState) {
		return new ImplicationExpression(getLeftExpression().reduce(symState), getRightExpression().reduce(symState));
	}

	@Override
	public void accept(IASLanPPVisitor visitor) {
		visitor.visit(this);
	}

}
