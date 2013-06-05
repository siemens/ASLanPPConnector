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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.avantssar.commons.ErrorGatherer;
import org.avantssar.commons.LocationInfo;

public class CompoundType extends AbstractNamed implements IType {

	private final List<IType> componentTypes = new ArrayList<IType>();

	public CompoundType(LocationInfo location, ErrorGatherer err, String operator, IType[] componentTypes) {
		this(location, err, operator, Arrays.asList(componentTypes));
	}

	public CompoundType(LocationInfo location, ErrorGatherer err, String operator, List<IType> componentTypes) {
		super(location, err, operator, new LowerNameValidator("compound type"));
		this.componentTypes.addAll(componentTypes);
	}

	public List<IType> getBaseTypes() {
		return componentTypes;
	}

	public void accept(IASLanVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	public boolean isAssignableFrom(IType subType, IASLanSpec spec) {
		if (subType instanceof CompoundType) {
			CompoundType ct = (CompoundType) subType;
			if (!ct.getName().equals(this.getName())) {
				return false;
			}
			if (ct.getBaseTypes().size() != this.getBaseTypes().size()) {
				return false;
			}
			for (int i = 0; i < this.getBaseTypes().size(); i++) {
				if (!this.getBaseTypes().get(i).isAssignableFrom(ct.getBaseTypes().get(i), spec)) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

}
