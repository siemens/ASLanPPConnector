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

import org.avantssar.commons.ChannelEntry;
import org.avantssar.commons.LocationInfo;
import org.avantssar.commons.ChannelEntry.Type;

public abstract class AbstractChannelGoal extends AbstractOwned implements ISecrecyGoal {

	private final LocationInfo location;
	private ITerm sender;
	private ITerm receiver;
	private final ChannelEntry type;
	private FunctionSymbol setFunction;

	public AbstractChannelGoal(LocationInfo location, IScope scope, String name, ITerm sender, ITerm receiver, ChannelEntry type) {
		super(scope, name);
		this.location = location;
		this.sender = sender;
		this.receiver = receiver;
		this.type = type;
	}

	public LocationInfo getLocation() {
		return location;
	}

	public ITerm getSender() {
		return sender;
	}

	public void setSender(ITerm sender) {
		this.sender = sender;
	}

	public ITerm getReceiver() {
		return receiver;
	}

	public void setReceiver(ITerm receiver) {
		this.receiver = receiver;
	}

	public ChannelEntry getType() {
		return type;
	}

	public boolean hasSecrecy() {
		return type.type == Type.Confidential || type.type == Type.Secure;
	}

	public boolean hasAuthentication() {
		return type.type == Type.Authentic || type.type == Type.Secure;
	}

	public boolean hasUndirectedAuthentication() {
		return hasAuthentication() && type.undirected;
	}

	public boolean hasFreshness() {
		return /*hasAuthentication() && */type.fresh;
	}

	public String getSecrecyGoalName() {
		return getSecrecyProtocolName();
	}

	public String getAuthenticationGoalName() {
		return (hasUndirectedAuthentication() ? "undirected_" : "") + getAuthenticationProtocolName();
	}

	public String getFreshnessGoalName() {
		return getFreshnessProtocolName();
	}

	public String getSecrecyProtocolName() {
		return getSecrecyProtocolName(getOriginalName());
	}

	public String getAuthenticationProtocolName() {
		return "auth_" + getOriginalName();
	}

	public String getFreshnessProtocolName() {
		return "fresh_" + getOriginalName();
	}

	public String getSetFunctionName() {
		return getSetFunctionName(getOriginalName());
	}

	public static String getSetFunctionName(String baseName) {
		return getSecrecyProtocolName(baseName) + "_set";
	}

	public static String getSecrecyProtocolName(String baseName) {
		return "secr_" + baseName;
	}

	public FunctionSymbol getSetFunction() {
		return setFunction;
	}

	public void setSetFunction(FunctionSymbol setFunction) {
		this.setFunction = setFunction;
	}

}
