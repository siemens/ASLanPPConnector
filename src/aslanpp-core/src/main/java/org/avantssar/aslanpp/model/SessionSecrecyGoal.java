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

import java.util.List;
import org.avantssar.aslanpp.visitors.IASLanPPVisitable;
import org.avantssar.aslanpp.visitors.IASLanPPVisitor;
import org.avantssar.commons.LocationInfo;

public class SessionSecrecyGoal extends AbstractOwned implements IASLanPPVisitable, ISecrecyGoal {

	private final GenericSecrecyGoal secr;

	public int used = 0;
	
	public SessionSecrecyGoal(LocationInfo location, Entity ent, String name, List<ITerm> agents) {
		super(ent, name);
		this.secr = new GenericSecrecyGoal(location, name, agents);
	}

	public LocationInfo getLocation() {
		return secr.getLocation();
	}

	public List<ITerm> getAgents() {
		return secr.getAgents();
	}

	public void setAgents(List<ITerm> agents) {
		secr.setAgents(agents);
	}

	public String getSecrecyGoalName() {
		return secr.getSecrecyGoalName();
	}

	public String getSecrecyProtocolName() {
		return secr.getSecrecyProtocolName();
	}

	public FunctionSymbol getSetSymbol() {
		return secr.getSetSymbol();
	}

	public void setSetSymbol(FunctionSymbol setSymbol) {
		secr.setSetSymbol(setSymbol);
	}

	public String getSetFunctionName() {
		return secr.getSetFunctionName();
	}

	@Override
	public void accept(IASLanPPVisitor visitor) {
		visitor.visit(this);
	}

}
