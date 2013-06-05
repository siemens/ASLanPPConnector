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

package org.avantssar.aslan.of;

import org.avantssar.aslan.ICommentEntry;
import org.avantssar.aslan.IRepresentable;
import org.avantssar.aslan.MetaInfo;
import org.avantssar.commons.LocationInfo;

public abstract class AbstractGroundTerm implements IGroundTerm {

	private final LocationInfo location;

	protected AbstractGroundTerm(LocationInfo location) {
		this.location = location;
	}

	public LocationInfo getLocation() {
		return location;
	}

	@Override
	public String toString() {
		return getRepresentation(null);
	}

	@Override
	public int compareTo(IGroundTerm o) {
		return this.getRepresentation(null).compareTo(o.getRepresentation(null));
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof IGroundTerm) {
			return this.compareTo((IGroundTerm) o) == 0;
		}
		return false;
	}

	@Override
	public int hashCode() {
		return getRepresentation(null).hashCode();
	}

	protected String getOriginalName(IRepresentable repr) {
		if (repr != null) {
			for (ICommentEntry ce : repr.getCommentLines()) {
				if (ce instanceof MetaInfo) {
					MetaInfo mi = (MetaInfo) ce;
					if (mi.getName().equals(MetaInfo.ORIGINAL_NAME)) {
						return mi.getParameters().get(MetaInfo.NAME);
					}
				}
			}
		}
		return null;
	}

}
