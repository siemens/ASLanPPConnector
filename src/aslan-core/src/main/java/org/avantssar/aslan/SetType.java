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

import javax.xml.bind.annotation.XmlAnyElement;
import org.avantssar.commons.ErrorGatherer;
import org.avantssar.commons.LocationInfo;

public class SetType extends AbstractRepresentable implements IType {

	private IType baseType;

	public SetType(LocationInfo location, ErrorGatherer err, IType baseType) {
		super(location, err);
		this.baseType = baseType;
	}

	@XmlAnyElement
	public IType getBaseType() {
		return baseType;
	}

	public void setBaseType(IType baseType) {
		this.baseType = baseType;
	}

	public void accept(IASLanVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	public boolean isAssignableFrom(IType subType, IASLanSpec spec) {
		if (subType instanceof SetType) {
			return (this).getBaseType().isAssignableFrom(((SetType) subType).getBaseType(), spec);
		}
		else {
			return false;
		}
	}

}
