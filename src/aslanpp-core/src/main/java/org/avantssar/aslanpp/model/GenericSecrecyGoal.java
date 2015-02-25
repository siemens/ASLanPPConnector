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
import org.avantssar.aslanpp.Util;
import org.avantssar.commons.LocationInfo;

public class GenericSecrecyGoal extends AbstractNamed {

	private final LocationInfo location;
	private final List<ITerm> agents = new ArrayList<ITerm>();
	private FunctionSymbol setSymbol;

	public GenericSecrecyGoal(LocationInfo location, String name, List<ITerm> agents) {
		super(name);
		this.location = location;
		this.agents.addAll(agents);
	}

	public LocationInfo getLocation() {
		return location;
	}

	public List<ITerm> getAgents() {
		return agents;
	}

	public void setAgents(List<ITerm> agents) {
		this.agents.clear();
		this.agents.addAll(agents);
	}

	public String getSetFunctionName() {
		return getSetFunctionName(getName());
	}

	public static String getSetFunctionName(String baseName) {
		return Util.lowerFirst(baseName + "_set");
	}

	public String getSecrecyGoalName() {
		return Util.lowerFirst(getName());
	}

	public String getSecrecyProtocolName() {
		return Util.lowerFirst(getName());
	}

	public FunctionSymbol getSetSymbol() {
		return setSymbol;
	}

	public void setSetSymbol(FunctionSymbol setSymbol) {
		this.setSymbol = setSymbol;
	}

}
