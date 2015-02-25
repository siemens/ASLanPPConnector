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

public class PairType extends AbstractRepresentable implements IType {

	private final IType left;
	private final IType right;

	// protected PairType() {}

	protected PairType(LocationInfo location, ErrorGatherer err, IType left, IType right) {
		super(location, err);
		this.left = left;
		this.right = right;
	}

	public IType getLeft() {
		return left;
	}

	public IType getRight() {
		return right;
	}

	public void accept(IASLanVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	public boolean isAssignableFrom(IType subType, IASLanSpec spec) {
		if (subType instanceof PairType) {
			PairType pt = (PairType) subType;
			if (left.isAssignableFrom(pt.left, spec) && right.isAssignableFrom(pt.right, spec)) {
				return true;
			}
		}
		return false;
	}

	public String toString() {
		String right = getRight().toString();
		if (getRight() instanceof PairType) {
			right = right.substring(1, right.length()-1);
		}
		return "("+getLeft().toString()+", "+right+")";
	}
}
