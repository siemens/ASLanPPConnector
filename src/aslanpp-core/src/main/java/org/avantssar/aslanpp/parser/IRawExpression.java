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
import org.avantssar.aslanpp.model.ITerm;
import org.avantssar.commons.LocationInfo;

public interface IRawExpression {

	LocationInfo getLocation();

	String getRepresentation();

	IExpression getFormula();

	IExpression getGuard(boolean allowReceive);

	CommunicationTerm getTransmission();

	ITerm getTerm(boolean allowTransmission, boolean strictVarCheck);

	boolean isComparison();

	IExpression getComparison();

	boolean isChannelGoal();

	RawChannelGoalInfo getChannelGoal();

	void accept(IRawExpressionVisitor visitor);

}
