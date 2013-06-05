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

import org.avantssar.aslanpp.visitors.IASLanPPVisitor;
import org.avantssar.commons.LocationInfo;

public class MacroSymbol extends GenericScope {

	private final List<VariableSymbol> argsOrder = new ArrayList<VariableSymbol>();
	private ITerm body;
	private final LocationInfo location;

	protected MacroSymbol(LocationInfo location, IScope owner, String name) {
		super(owner, name);
		this.location = location;
	}

	public LocationInfo getLocation() {
		return location;
	}

	public VariableSymbol addArgument(String name) {
		VariableSymbol var = addUntypedVariable(name);
		argsOrder.add(var);
		return var;
	}

	public List<VariableSymbol> getArguments() {
		return argsOrder;
	}

	public VariableSymbol getArgument(String name) {
		return findVariable(name);
	}

	public void setBody(ITerm body) {
		this.body = body;
	}

	public ITerm getBody() {
		return body;
	}

	public MacroTerm term(ITerm... arguments) {
		return term(null, arguments);
	}

	public MacroTerm term(LocationInfo location, ITerm... arguments) {
		return term(location, getOwner(), arguments);
	}

	public MacroTerm term(LocationInfo location, IScope scope, ITerm... arguments) {
		return new MacroTerm(location, scope, this, arguments);
	}

	public void accept(IASLanPPVisitor visitor) {
		visitor.visit(this);
	}

}
