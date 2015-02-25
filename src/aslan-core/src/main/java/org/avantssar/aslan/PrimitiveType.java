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

import org.avantssar.commons.ErrorGatherer;
import org.avantssar.commons.LocationInfo;

public class PrimitiveType extends AbstractNamed implements IType {

	private PrimitiveType superType;

	private final boolean prelude;

	protected PrimitiveType(LocationInfo location, ErrorGatherer err, String name, boolean prelude) {
		this(location, err, name, null, prelude);
	}

	protected PrimitiveType(LocationInfo location, ErrorGatherer err, String name, PrimitiveType superType, boolean prelude) {
		super(location, err, name, new LowerNameValidator("simple type"));
		this.superType = superType;
		this.prelude = prelude;
	}

	public PrimitiveType getSuperType() {
		return superType;
	}

	public void setSuperType(PrimitiveType superType) {
		this.superType = superType;
	}

	public boolean isPrelude() {
		return prelude;
	}

	public void accept(IASLanVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	public boolean isAssignableFrom(IType subType, IASLanSpec spec) {
		if (subType instanceof PrimitiveType) {
			PrimitiveType ss = (PrimitiveType) subType;
			return equals(ss) || isAssignableFrom(ss.getSuperType(), spec);
		}
		else if (subType instanceof SetType) {
			return equals(IASLanSpec.MESSAGE);
		}
		else if (subType instanceof PairType) {
			return equals(IASLanSpec.MESSAGE);
		}
		else if (subType instanceof CompoundType) {
			String fname = ((CompoundType) subType).getName();
			Function fnc = spec.findFunction(fname);
			return fnc != null && isAssignableFrom(fnc.getType(), spec);
		}
		else {
			return false;
		}
	}
	
	public String toString() {
		return getName();
	}
}
