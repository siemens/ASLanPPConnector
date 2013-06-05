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

import java.util.Map;
import java.util.Set;
import org.avantssar.aslan.Constant;
import org.avantssar.aslan.IASLanSpec;
import org.avantssar.aslan.IRepresentable;
import org.avantssar.aslan.SetType;
import org.avantssar.aslan.Variable;
import org.avantssar.commons.LocationInfo;

public class GroundConstant extends AbstractGroundTerm {

	private final String name;

	protected GroundConstant(LocationInfo location, String name) {
		super(location);
		if (name.indexOf('(') >= 0) {
			throw new IllegalArgumentException("Invalid constant name: " + name + ".");
		}
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void accept(IAnalysisResultVisitor visitor) {
		visitor.visit(this);
	}

	protected String expandSet(ISetProvider setProvider, IASLanSpec aslanSpec) {
		Set<IGroundTerm> items = setProvider.getSet(this);
		StringBuffer sb = new StringBuffer();
		sb.append("{");
		boolean first = true;
		if (items != null) {
			for (IGroundTerm t : items) {
				if (!first) {
					sb.append(", ");
				}
				sb.append(t.getRepresentationNice(setProvider, aslanSpec));
				first = false;
			}
		}
		sb.append("}");
		return sb.toString();
	}

	@Override
	public String getRepresentation(IASLanSpec aslanSpec) {
    if (name.length() > 4 && name.substring(0,4).equals("IID_"))
        return name; // knowing the index is important to distinguish session instances
		if (aslanSpec != null) {
			IRepresentable c;
			if (isVariable()) {
				c = aslanSpec.findVariable(name);
			}
			else {
				c = aslanSpec.findConstant(name);
			}
			String origName = getOriginalName(c);
			if (origName != null) {
				return origName;
			}
		}
		return name;
	}

	public boolean isSet(IASLanSpec aslanSpec) {
		if (aslanSpec != null) {
			Constant c = aslanSpec.findConstant(getName());
			if (c != null) {
				if (c.getType() instanceof SetType) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public String getRepresentationNice(ISetProvider setProvider, IASLanSpec aslanSpec) {
		if (getName().startsWith("Dummy")) {
			return "?";
		}
		else {
			if (setProvider != null && isSet(aslanSpec)) {
				return expandSet(setProvider, aslanSpec);
			}
			else {
				return getRepresentation(aslanSpec);
			}
		}
	}

	@Override
	public IGroundTerm reduce(Map<Variable, IGroundTerm> assigned) {
		if (isVariable()) {
			for (Variable v : assigned.keySet()) {
				if (v.getName().equals(name)) {
					return assigned.get(v);
				}
			}
			return this;
		}
		else {
			return this;
		}
	}

	protected boolean isVariable() {
		return Character.isUpperCase(name.charAt(0));
	}
}
