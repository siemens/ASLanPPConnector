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
import org.avantssar.aslanpp.visitors.IASLanPPVisitor;
import org.avantssar.commons.LocationInfo;

public class CompoundType extends AbstractNamed implements IType {

	public final static String CONCAT = "'"; // internal name of message concatenation
	private final List<IType> argumentTypes;
	private final LocationInfo location;

	public static List<IType> flattenTupleOrConcatTypes(boolean tuple, List<IType> orig) {
		List<IType> types = new ArrayList<IType>(orig); // clone it, because we modify it
		while (types.size() >= 2 && (  tuple && types.get(types.size()-1) instanceof TupleType ||
				                     (!tuple && types.get(types.size()-1) instanceof CompoundType
                             && ((CompoundType) types.get(types.size()-1)).getName() == CONCAT))) {
			IType t = types.get(types.size()-1);
			List<IType> ts = (t instanceof TupleType ? ((   TupleType) t).getBaseTypes() 
			                                         : ((CompoundType) t).getArgumentTypes());
			types.remove(types.size()-1);
			types.addAll(ts);
		}
		return types;
	}
	
	public CompoundType(LocationInfo location, String name, List<IType> argTypes) {
		super(name);
		argumentTypes = name == CONCAT ? flattenTupleOrConcatTypes(false, argTypes) // normalize concatenation of messages
				                       : argTypes;
		this.location = location;
	}

	public LocationInfo getLocation() {
		return location;
	}

	public List<IType> getArgumentTypes() {
		return argumentTypes;
	}

	public IType getArgumentType(int index) {
		return argumentTypes.get(index);
	}

	@Override
	public String getDummyName() {
		StringBuffer sb = new StringBuffer();
		sb.append(DUMMY_PREFIX);
		sb.append(getName());
		for (IType t : argumentTypes) {
			sb.append("_");
			sb.append(t.getDummyName().substring(DUMMY_PREFIX.length()));
		}
		return sb.toString();
	}

	@Override
	public boolean isAssignableFrom(IType subType) {
		if (subType instanceof CompoundType) {
			CompoundType ct = (CompoundType) subType;
			if (!ct.getName().equals(this.getName())) {
				return false;
			}
			List<IType> types = getArgumentTypes();
			List<IType> subTypes = ct.getArgumentTypes();
			if (getName() == CONCAT && types.size() == 0 && subTypes.size() == 0) {
				return true;
			} else if (getName() == CONCAT && types.size() >= 1 && subTypes.size() >= 1) {
				if (!types.get(0).isAssignableFrom(subTypes.get(0))) {
					return false;
				}
				return (new CompoundType(null, CONCAT,    types.subList(1,    types.size())).isAssignableFrom(
					    new CompoundType(null, CONCAT, subTypes.subList(1, subTypes.size()))));
			} else {
				if (subTypes.size() != types.size()) {
					return false;
				}
				for (int i = 0; i < types.size(); i++) {
					if (!types.get(i).isAssignableFrom(subTypes.get(i))) {
						return false;
					}
				}
				return true;
			}
		}
		return false;
	}

	@Override
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
