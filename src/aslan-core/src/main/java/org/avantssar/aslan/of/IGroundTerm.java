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

package org.avantssar.aslan.of;

import java.util.Map;
import org.avantssar.aslan.IASLanSpec;
import org.avantssar.aslan.Variable;
import org.avantssar.commons.LocationInfo;

public interface IGroundTerm extends Comparable<IGroundTerm> {

	LocationInfo getLocation();

	void accept(IAnalysisResultVisitor visitor);

	String getRepresentation(IASLanSpec aslanSpec);

	String getRepresentationNice(ISetProvider setProvider, IASLanSpec aslanSpec);

	IGroundTerm reduce(Map<Variable, IGroundTerm> assigned);

	boolean isSet(IASLanSpec aslanSpec);

}
