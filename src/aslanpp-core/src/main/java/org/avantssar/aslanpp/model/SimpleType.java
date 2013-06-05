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

import org.avantssar.aslanpp.Util;
import org.avantssar.aslanpp.visitors.IASLanPPVisitor;
import org.avantssar.commons.LocationInfo;

public class SimpleType extends AbstractOwned implements IType {

	private SimpleType superType;
	private final boolean partOfPrelude;
	private final LocationInfo location;

	public SimpleType(LocationInfo location, IScope scope, String name, boolean partOfPrelude) {
		this(location, scope, name, partOfPrelude, null);
	}

	public SimpleType(LocationInfo location, IScope scope, String name, boolean partOfPrelude, SimpleType superType) {
		super(scope, name);
		this.superType = superType;
		this.partOfPrelude = partOfPrelude;
		this.location = location;
	}

	public LocationInfo getLocation() {
		return location;
	}

	public SimpleType getSuperType() {
		return superType;
	}

	public void setSuperType(SimpleType superType) {
		this.superType = superType;
	}

	public boolean isPartOfPrelude() {
		return partOfPrelude;
	}

	public String getDummyName() {
		return DUMMY_PREFIX + getName();
	}

	public boolean answersTo(String name) {
		return this.getName().equals(name);
	}

	public boolean isAssignableFrom(IType subType) {
		if (subType instanceof SimpleType) {
			SimpleType ss = (SimpleType) subType;
			if (equals(ss)) {
				return true;
			}
			else {
				return isAssignableFrom(ss.getSuperType());
			}
		}
		else if (subType instanceof SetType || subType instanceof TupleType) {
			if (getName().equals(Prelude.MESSAGE)) {
				return true;
			}
			else {
				return false;
			}
		}
		else if (subType instanceof CompoundType) {
			String fname = ((CompoundType) subType).getName();
			IType ftype = getOwner().findCompoundType(fname);
			return isAssignableFrom(ftype);
		}
		else {
			return false;
		}
	}

	public void accept(IASLanPPVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	public String getRepresentation() {
		return Util.represent(this);
	}

	@Override
	public String toString() {
		return getRepresentation();
	}

}
