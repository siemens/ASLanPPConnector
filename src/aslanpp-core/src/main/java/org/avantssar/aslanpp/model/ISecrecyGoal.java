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

//TODO maybe consolidate with GenericSecrecyGoal
package org.avantssar.aslanpp.model;


import org.avantssar.aslanpp.model.FunctionSymbol;
import org.avantssar.commons.LocationInfo;

public interface ISecrecyGoal {
	public IScope getOwner();
	public LocationInfo getLocation();
	public String getSecrecyGoalName();
	public String getSecrecyProtocolName();
	public String getSetFunctionName();
	public FunctionSymbol getSetFunction();
	public void setSetFunction(FunctionSymbol setFunction);
}
