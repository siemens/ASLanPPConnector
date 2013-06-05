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

public abstract class AbstractBinaryExpression extends AbstractExpression {

	private final IExpression leftExpression;
	private final IExpression rightExpression;
	private final String operator;

	protected AbstractBinaryExpression(IExpression leftExpression, IExpression rightExpression, String operator) {
		super(leftExpression.getLocation());
		this.leftExpression = leftExpression;
		this.rightExpression = rightExpression;
		this.operator = operator;

		super.addChildExpression(leftExpression);
		super.addChildExpression(rightExpression);
	}

	public IExpression getLeftExpression() {
		return leftExpression;
	}

	public IExpression getRightExpression() {
		return rightExpression;
	}

	protected String getOperator() {
		return operator;
	}

	@Override
	public List<IExpression> getAtomicExpressions(boolean forAttackState) {
		List<IExpression> result = new ArrayList<IExpression>();
		result.addAll(leftExpression.getAtomicExpressions(forAttackState));
		result.addAll(rightExpression.getAtomicExpressions(forAttackState));
		return result;
	}

	public boolean canHandleDeMorgan() {
		return leftExpression.canHandleDeMorgan() && rightExpression.canHandleDeMorgan();
	}

}
