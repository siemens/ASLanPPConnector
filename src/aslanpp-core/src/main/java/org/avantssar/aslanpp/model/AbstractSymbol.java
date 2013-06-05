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

import org.avantssar.commons.LocationInfo;

public abstract class AbstractSymbol extends AbstractOwned implements ISymbol {

	protected static final String SYMBOLS_FILE = "symbols";

	protected IType type;
	private boolean partOfPrelude;
	private final LocationInfo location;

	protected AbstractSymbol(IScope owner, LocationInfo location, String name,
			IType type) {
		super(owner, name);
		this.type = type;
		this.partOfPrelude = false;
		this.location = location;
	}

	public IType getType() {
		return type;
	}

	public void changeType(IType newType) {
		type = newType;
	}

	public boolean isPartOfPrelude() {
		return partOfPrelude;
	}

	public void setPartOfPrelude(boolean partOfPrelude) {
		this.partOfPrelude = partOfPrelude;
	}

	public LocationInfo getLocation() {
		return location;
	}

	@Override
	public String toString() {
		return "<" + getOriginalName() + ">_<" + getName() + ">";
	}

}
