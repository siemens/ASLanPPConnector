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

package org.avantssar.aslan;

import org.avantssar.aslan.ImplicitExplicitSymbol.ImplicitExplicitState;
import org.avantssar.commons.LocationInfo;

public interface ITerm extends IRepresentable {

	IType getType();

	void buildContext(TermContext ctx, boolean isInNegatedTerm);

	ITerm negate();

	ITerm negate(LocationInfo location);

	ITerm exists(Variable v);

	ITerm exists(LocationInfo location, Variable v);

	ITerm forall(Variable v);

	ITerm forall(LocationInfo location, Variable v);

	boolean isCondition();

	void setImplicitExplicitState(ImplicitExplicitState state);

}
