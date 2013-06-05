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

import java.util.Arrays;
import java.util.List;
import org.avantssar.aslanpp.visitors.IASLanPPVisitor;
import org.avantssar.commons.LocationInfo;

public class SecrecyGoalStatement extends AbstractStatement implements ISecrecyGoal {

	private final GenericSecrecyGoal secr;
	private ITerm payload;
	private final Entity ent;

	protected SecrecyGoalStatement(LocationInfo location, Entity ent, String name, ITerm payload, ITerm... knowers) {
		super(location);
		this.secr = new GenericSecrecyGoal(location, name, Arrays.asList(knowers));
		this.payload = payload;
		this.ent = ent;
	}

	public ITerm getPayload() {
		return payload;
	}

	public void setPayload(ITerm term) {
		payload = term;
	}

	public List<ITerm> getAgents() {
		return secr.getAgents();
	}

	public void setAgents(List<ITerm> agents) {
		secr.setAgents(agents);
	}

	public String getName() {
		return secr.getName();
	}

	public Entity getOwner() {
		return ent;
	}

	public String getSecrecyGoalName() {
		return secr.getSecrecyGoalName();
	}

	public String getSecrecyProtocolName() {
		return secr.getSecrecyProtocolName();
	}

	public FunctionSymbol getSetFunction() {
		return secr.getSetFunction();
	}

	public void setSetFunction(FunctionSymbol setFunction) {
		secr.setSetFunction(setFunction);
	}

	public String getSetFunctionName() {
		return secr.getSetFunctionName();
	}

	@Override
	public void accept(IASLanPPVisitor visitor) {
		visitor.visit(this);
	}

}
