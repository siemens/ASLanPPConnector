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

package org.avantssar.aslanpp.parser;

import org.avantssar.aslanpp.parser.RawLogicalExpression.LogicalOperator;

public class RawPrettyPrinter implements IRawExpressionVisitor {

	private final StringBuffer sb = new StringBuffer();
//	private int indent = 0;

	@Override
	public void visit(RawAnnotatedTransmissionExpression e) {
		e.getChannel().accept(this);
		sb.append(" : ");
		e.getPayload().accept(this);
	}

	@Override
	public void visit(RawChannelExpression e) {
		e.getSender().accept(this);
		sb.append(" ").append(e.getType()).append(" ");
		e.getReceiver().accept(this);
	}

	@Override
	public void visit(RawComparisonExpression e) {
		e.getLeft().accept(this);
		sb.append(" ");
		if (e.getEquality()) {
			sb.append("=");
		}
		else {
			sb.append("!=");
		}
		sb.append(" ");
		e.getRight().accept(this);
	}

	@Override
	public void visit(RawConcatenatedExpression e) {
		boolean first = true;
		if (e.isTuple()) {
			sb.append("(");
		}
		for (IRawExpression expr : e.getItems()) {
			if (!first) {
				if (e.isTuple()) {
					sb.append(",");
				}
				else {
					sb.append(".");
				}
			}
			expr.accept(this);
			first = false;
		}
		if (e.isTuple()) {
			sb.append(")");
		}
	}

	@Override
	public void visit(RawConstVarExpression e) {
		sb.append(e.getName());
	}

	@Override
	public void visit(RawFunctionExpression e) {
		sb.append(e.getName());
		sb.append("(");
		boolean first = true;
		for (IRawExpression expr : e.getParameters()) {
			if (!first) {
				sb.append(",");
			}
			expr.accept(this);
			first = false;
		}
		sb.append(")");
	}

	@Override
	public void visit(RawFunctionTransmissionExpression e) {
		e.getBase().accept(this);
		sb.append(" ").append("over").append(" ");
		sb.append(e.getType());
	}

	@Override
	public void visit(RawLogicalExpression e) {
		e.getLeft().accept(this);
		if (e.getOperator() == LogicalOperator.Conjunction) {
			sb.append("&");
		}
		else if (e.getOperator() == LogicalOperator.Disjunction) {
			sb.append("|");
		}
		else {
			sb.append("=>");
		}
		e.getRight().accept(this);
	}

	@Override
	public void visit(RawMatchedExpression e) {
		sb.append("?");
		if (e.getMatched() != null) {
			sb.append(e.getMatched());
		}
	}

	@Override
	public void visit(RawNegatedExpression e) {
		sb.append("!");
		e.getBase().accept(this);
	}

	@Override
	public void visit(RawNumericExpression e) {
		sb.append(e.getValue());
	}

	@Override
	public void visit(RawOOPCallExpression e) {
		e.getCaller().accept(this);
		sb.append("->");
		e.getWhat().accept(this);
	}

	@Override
	public void visit(RawParenthesisExpression e) {
		sb.append("(");
		e.getBase().accept(this);
		sb.append(")");
	}

	@Override
	public void visit(RawPseudonymExpression e) {
		sb.append("[");
		e.getReal().accept(this);
		if (e.getPseudonym() != null) {
			sb.append("]_[");
			e.getPseudonym().accept(this);
		}
		sb.append("]");
	}

	@Override
	public void visit(RawQuantifiedExpression e) {
		if (e.getUniversal()) {
			sb.append("forall");
		}
		else {
			sb.append("exists");
		}
		for (String v : e.getVars().keySet()) {
			sb.append(" ").append(v);
		}
		sb.append(" ").append(".").append(" ");
		e.getBase().accept(this);
	}

	@Override
	public void visit(RawSetExpression e) {
		sb.append("{");
		boolean first = true;
		for (IRawExpression expr : e.getItems()) {
			if (!first) {
				sb.append(",");
			}
			expr.accept(this);
			first = false;
		}
		sb.append("}");
	}

	@Override
	public void visit(RawAnnotatedExpression e) {
		sb.append(e.getAnnotation());
		sb.append(":(");
		e.getBase().accept(this);
		sb.append(")");
	}

	@Override
	public String toString() {
		return sb.toString();
	}
/*
	private void indent() {
		indent++;
	}

	private void unindent() {
		indent--;
	}

	private void startLine() {
		for (int i = 0; i < indent; i++) {
			sb.append("\t");
		}
	}

	private void endLine() {
		sb.append("\n");
	}
*/
}
