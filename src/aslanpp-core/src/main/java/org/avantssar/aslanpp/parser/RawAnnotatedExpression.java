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

import org.avantssar.aslanpp.model.CommunicationTerm;
import org.avantssar.aslanpp.model.IExpression;
import org.avantssar.aslanpp.model.IScope;
import org.avantssar.aslanpp.model.ITerm;
import org.avantssar.commons.ErrorGatherer;
import org.avantssar.commons.LocationInfo;

public class RawAnnotatedExpression extends AbstractRawExpression {

	private final String annotation;
	private final IRawExpression base;

	public RawAnnotatedExpression(IScope scope, LocationInfo location, ErrorGatherer err, String annotation, IRawExpression base) {
		super(scope, location, err);
		this.annotation = annotation;
		this.base = base;
	}

	public String getAnnotation() {
		return annotation;
	}

	public IRawExpression getBase() {
		return base;
	}

	@Override
	public void accept(IRawExpressionVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	public IExpression getFormula() {
		return base.getFormula();
	}

	@Override
	public IExpression getGuard(boolean allowReceive) {
		return base.getGuard(allowReceive);
	}

	@Override
	public ITerm getTerm(boolean allowTransmission, boolean strictVarCheck) {
		return base.getTerm(allowTransmission, strictVarCheck).annotate(annotation);
	}

	@Override
	public CommunicationTerm getTransmission() {
		return base.getTransmission();
	}

	@Override
	public IExpression getComparison() {
		return base.getComparison();
	}

	@Override
	public boolean isComparison() {
		return base.isComparison();
	}

	@Override
	public boolean isChannelGoal() {
		return base.isChannelGoal();
	}

	@Override
	public RawChannelGoalInfo getChannelGoal() {
		return base.getChannelGoal();
	}

}
