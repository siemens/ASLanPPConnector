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

public class SetType implements IType {

	private final IType baseType;

	public SetType(IType baseType) {
		this.baseType = baseType;
	}

	public IType getBaseType() {
		return baseType;
	}

	@Override
	public String getDummyName() {
		String bd = baseType.getDummyName();
		StringBuffer sb = new StringBuffer(bd);
		sb.insert(DUMMY_PREFIX.length(), "set_");
		return sb.toString();
	}

	public boolean isAssignableFrom(IType subType) {
		if (subType instanceof SetType) {
			return (this).getBaseType().isAssignableFrom(((SetType) subType).getBaseType());
		}
		else {
			return false;
		}
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
		IType bt = getBaseType();
		String s = bt.toString();
		boolean paren = !(bt instanceof org.avantssar.aslan.PrimitiveType);
		return (paren ? "(" : "") + s + (paren ? ")" : "") + " set";
	}

}
