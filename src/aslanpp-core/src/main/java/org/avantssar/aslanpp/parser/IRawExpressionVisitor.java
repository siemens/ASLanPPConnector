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

public interface IRawExpressionVisitor {

	void visit(RawAnnotatedTransmissionExpression e);

	void visit(RawChannelExpression e);

	void visit(RawComparisonExpression e);

	void visit(RawConcatenatedExpression e);

	void visit(RawConstVarExpression e);

	void visit(RawFunctionExpression e);

	void visit(RawFunctionTransmissionExpression e);

	void visit(RawLogicalExpression e);

	void visit(RawMatchedExpression e);

	void visit(RawNegatedExpression e);

	void visit(RawNumericExpression e);

	void visit(RawOOPCallExpression e);

	void visit(RawParenthesisExpression e);

	void visit(RawPseudonymExpression e);

	void visit(RawQuantifiedExpression e);

	void visit(RawSetExpression e);

	void visit(RawAnnotatedExpression e);

}
