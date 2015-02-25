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
import org.avantssar.aslanpp.Util;
import org.avantssar.aslanpp.visitors.IASLanPPVisitor;

public class TupleType implements IType {

	private final List<IType> types;

	public TupleType(IType... types) {
		this(Arrays.asList(types));
	}

	public TupleType(List<IType> types) {
		this.types = CompoundType.flattenTupleOrConcatTypes(true, types);
	}

	public List<IType> getBaseTypes() {
		return types;
	}

	@Override
	public String getDummyName() {
		StringBuffer sb = new StringBuffer();
		sb.append(DUMMY_PREFIX).append("tuple");
		for (IType t : types) {
			sb.append("_").append(t.getDummyName().substring(DUMMY_PREFIX.length()));
		}
		return sb.toString();
	}

	@Override
	public boolean isAssignableFrom(IType subType) {
		if (subType instanceof TupleType /* || 
			(subType instanceof CompoundType && ((CompoundType) subType).getName() == CompoundType.CONCAT)*/) {
			List<IType> types = subType instanceof TupleType ? ((TupleType) subType).getBaseTypes() : 
				                                               ((CompoundType) subType).getArgumentTypes();
			if (types.size() != this.getBaseTypes().size()) {
				return false;
			}
			for (int i = 0; i < this.getBaseTypes().size(); i++) {
				if (!this.getBaseTypes().get(i).isAssignableFrom(types.get(i))) {
					return false;
				}
			}
			return true;
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
