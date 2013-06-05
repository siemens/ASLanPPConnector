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

public class LTLExpression extends AbstractExpression {

	public static final String GLOBALLY = "G";
	public static final String HISTORICALLY = "H";

	private final String operator;
	private final List<IExpression> expressions = new ArrayList<IExpression>();

	/**
	 * For unary LTL expressions.
	 */
	public LTLExpression(String operator, IExpression expression) {
		super(expression.getLocation());
		this.operator = operator;
		expressions.add(expression);
		super.addChildExpression(expression);
	}

	/**
	 * For binary LTL expressions.
	 */
	public LTLExpression(String operator, IExpression firstExpression, IExpression secondExpression) {
		super(firstExpression.getLocation());
		this.operator = operator;
		expressions.add(firstExpression);
		expressions.add(secondExpression);
		super.addChildExpression(firstExpression);
		super.addChildExpression(secondExpression);
	}

	private LTLExpression(String operator, List<IExpression> expressions) {
		super(expressions.size() > 0 ? expressions.get(0).getLocation() : null);
		this.operator = operator;
		for (IExpression e : expressions) {
			this.expressions.add(e);
		}
		for (IExpression e : expressions) {
			super.addChildExpression(e);
		}
	}

	public String getOperator() {
		return operator;
	}

	public List<IExpression> getChildExpressions() {
		return expressions;
	}

	public IExpression expandAuxiliaryTerms() {
		List<IExpression> expExpressions = new ArrayList<IExpression>();
		for (IExpression e : expressions) {
			expExpressions.add(e.expandAuxiliaryTerms());
		}
		return new LTLExpression(operator, expExpressions);
	}

	public boolean isAlwaysFalse() {
		return false;
	}

	public boolean isAlwaysTrue() {
		return false;
	}

	public IExpression applyDeMorgan() {
		return duplicate();
	}

	public IExpression duplicate() {
		List<IExpression> dupExpressions = new ArrayList<IExpression>();
		for (IExpression e : expressions) {
			dupExpressions.add(e.duplicate());
		}
		return new LTLExpression(operator, expressions);
	}

	public IExpression negate() {
		return new NegationExpression(duplicate());
	}

	public IExpression pushDownNegations() {
		List<IExpression> pdnExpressions = new ArrayList<IExpression>();
		for (IExpression e : expressions) {
			pdnExpressions.add(e.pushDownNegations());
		}
		return new LTLExpression(operator, pdnExpressions);
	}

	public boolean canHandleDeMorgan() {
		return false;
	}

	public static String convertOp(String op) {
		if (op.equals("[]")) {
			return GLOBALLY;
		}
		else if (op.equals("[-]")) {
			return HISTORICALLY;
		}
		else if (op.equals("<>")) {
			return "F";
		}
		else if (op.equals("<->")) {
			return "O";
		}
		else {
			return op;
		}
	}

	public static String convertOpBack(String op) {
		if (op.equals(GLOBALLY)) {
			return "[]";
		}
		else if (op.equals(HISTORICALLY)) {
			return "[-]";
		}
		else if (op.equals("F")) {
			return "<>";
		}
		else if (op.equals("O")) {
			return "<->";
		}
		else {
			return op;
		}
	}

	public IExpression reduce(SymbolsState symState) {
		List<IExpression> reducedExpressions = new ArrayList<IExpression>();
		for (IExpression e : expressions) {
			reducedExpressions.add(e.reduce(symState));
		}
		return new LTLExpression(operator, reducedExpressions);
	}

	@Override
	public void accept(IASLanPPVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	public IExpression getGloballySubterm() {
		return (convertOp(operator).equals(GLOBALLY) ? expressions.get(0) : null);
	}
	
	@Override
	public IExpression reduceForAttackState() {
		if (convertOp(operator).equals(GLOBALLY)) {
			return expressions.get(0).reduceForAttackState();
		}
		else {
			return this;
		}
	}

}
