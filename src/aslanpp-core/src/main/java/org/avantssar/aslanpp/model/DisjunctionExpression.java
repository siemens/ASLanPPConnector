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
import org.avantssar.aslanpp.visitors.IASLanPPVisitor;

public class DisjunctionExpression extends AbstractBinaryExpression {

	protected DisjunctionExpression(IExpression leftExpression, IExpression rightExpression) {
		super(leftExpression, rightExpression, "|");
	}

	public IExpression pushDownNegations() {
		return new DisjunctionExpression(getLeftExpression().pushDownNegations(), getRightExpression().pushDownNegations());
	}

	public IExpression negate() {
		NegationExpression leftNegated = new NegationExpression(getLeftExpression().duplicate());
		NegationExpression rightNegated = new NegationExpression(getRightExpression().duplicate());
		return new ConjunctionExpression(leftNegated.pushDownNegations(), rightNegated.pushDownNegations());
	}

	public IExpression applyDeMorgan() {
		return duplicate();
	}

	public IExpression duplicate() {
		return new DisjunctionExpression(getLeftExpression().duplicate(), getRightExpression().duplicate());
	}

	@Override
	public List<IExpression> getConjunctions() {
		List<IExpression> conjunctions = new ArrayList<IExpression>();
		conjunctions.addAll(getLeftExpression().getConjunctions());
		conjunctions.addAll(getRightExpression().getConjunctions());
		return conjunctions;
	}

	public IExpression expandAuxiliaryTerms() {
		return new DisjunctionExpression(getLeftExpression().expandAuxiliaryTerms(), getRightExpression().expandAuxiliaryTerms());
	}

	public boolean isAlwaysTrue() {
		return getLeftExpression().isAlwaysTrue() || getRightExpression().isAlwaysTrue();
	}

	public boolean isAlwaysFalse() {
		return getLeftExpression().isAlwaysFalse() && getRightExpression().isAlwaysFalse();
	}

	public IExpression reduce(SymbolsState symState) {
		return new DisjunctionExpression(getLeftExpression().reduce(symState), getRightExpression().reduce(symState));
	}

	@Override
	public void accept(IASLanPPVisitor visitor) {
		visitor.visit(this);
	}

}
