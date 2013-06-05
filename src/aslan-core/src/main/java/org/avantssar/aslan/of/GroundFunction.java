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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.avantssar.aslan.Function;
import org.avantssar.aslan.IASLanSpec;
import org.avantssar.aslan.IRepresentable;
import org.avantssar.aslan.SetType;
import org.avantssar.aslan.Variable;
import org.avantssar.commons.LocationInfo;

public class GroundFunction extends GroundConstant {

	private final List<IGroundTerm> parameters = new ArrayList<IGroundTerm>();

	protected GroundFunction(LocationInfo location, String name, List<IGroundTerm> parameters) {
		super(location, name);
		this.parameters.addAll(parameters);
	}

	protected GroundFunction(LocationInfo location, String name, IGroundTerm parameters) {
		this(location, name, Arrays.asList(parameters));
	}

	public List<IGroundTerm> getParameters() {
		return parameters;
	}

	@Override
	public void accept(IAnalysisResultVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	public String getRepresentation(IASLanSpec aslanSpec) {
		return getRepresentationNice(null, aslanSpec);
	}

	@Override
	public boolean isSet(IASLanSpec aslanSpec) {
		if (aslanSpec != null) {
			Function fnc = aslanSpec.findFunction(getName());
			if (fnc != null) {
				if (fnc.getType() instanceof SetType) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public String getRepresentationNice(ISetProvider setProvider, IASLanSpec aslanSpec) {
		if (setProvider != null && isSet(aslanSpec)) {
			return expandSet(setProvider, aslanSpec);
		}
		else {
			StringBuffer sb = new StringBuffer();
			if (IASLanSpec.CONTAINS.getName().equals(getName())) {
				sb.append(parameters.get(1).getRepresentation(aslanSpec));
				sb.append("->").append(IASLanSpec.CONTAINS.getName()).append("(");
				sb.append(parameters.get(0).getRepresentationNice(setProvider, aslanSpec));
				sb.append(")");
			}
			else if (IASLanSpec.PAIR.getName().equals(getName())) {
				sb.append(parameters.get(0).getRepresentationNice(setProvider, aslanSpec));
				sb.append(".");
				sb.append(parameters.get(1).getRepresentationNice(setProvider, aslanSpec));
			}
			else if (IASLanSpec.SIGN.getName().equals(getName()) && parameters.get(0) instanceof GroundFunction && ((GroundFunction) parameters.get(0)).getName().equals(IASLanSpec.INV.getName())) {
				sb.append("{");
				sb.append(parameters.get(1).getRepresentationNice(setProvider, aslanSpec));
				sb.append("}_inv(");
				GroundFunction key = (GroundFunction) parameters.get(0);
				sb.append(key.parameters.get(0).getRepresentationNice(setProvider, aslanSpec));
				sb.append(")");
			}
			else if (IASLanSpec.CRYPT.getName().equals(getName())) {
				sb.append("{");
				sb.append(parameters.get(1).getRepresentationNice(setProvider, aslanSpec));
				sb.append("}_");
				sb.append(parameters.get(0).getRepresentationNice(setProvider, aslanSpec));
			}
			else if (IASLanSpec.SCRYPT.getName().equals(getName())) {
				sb.append("{|");
				sb.append(parameters.get(1).getRepresentationNice(setProvider, aslanSpec));
				sb.append("|}_");
				sb.append(parameters.get(0).getRepresentationNice(setProvider, aslanSpec));
			}
			else if (IASLanSpec.AND.getName().equals(getName())) {
				sb.append(parameters.get(0).getRepresentationNice(setProvider, aslanSpec));
				sb.append(" & \n    ");
				sb.append(parameters.get(1).getRepresentationNice(setProvider, aslanSpec));
			}
			else if (IASLanSpec.EQUAL.getName().equals(getName())) {
				sb.append(parameters.get(0).getRepresentationNice(setProvider, aslanSpec));
				sb.append(" = ");
				sb.append(parameters.get(1).getRepresentationNice(setProvider, aslanSpec));
			}
			else if ("defaultPseudonym".equals(getName())) {
				sb.append("[");
				sb.append(parameters.get(0).getRepresentationNice(setProvider, aslanSpec));
				sb.append(",");
				sb.append(parameters.get(1).getRepresentationNice(setProvider, aslanSpec));
				sb.append("]");
			}
			else {
				boolean handled = false;
				if ("not".equals(getName())) {
					IGroundTerm first = parameters.get(0);
					if (first instanceof GroundFunction) {
						GroundFunction base = (GroundFunction) first;
						if (IASLanSpec.EQUAL.getName().equals(base.getName())) {
							sb.append(base.parameters.get(0).getRepresentation(aslanSpec));
							sb.append(" != ");
							sb.append(base.parameters.get(1).getRepresentation(aslanSpec));
							handled = true;
						}
					}
				}
				if (!handled) {
					String origName = null;
					if (aslanSpec != null) {
						IRepresentable c;
						if (isVariable()) {
							c = aslanSpec.findVariable(getName());
						}
						else {
							c = aslanSpec.findConstant(getName());
						}
						origName = getOriginalName(c);
					}
					if (origName == null) {
						origName = getName();
					}
					sb.append(origName);
					sb.append("(");
					boolean first = true;
					for (IGroundTerm t : parameters) {
						if (!first) {
							sb.append(",");
						}
						if(t == null)	sb.append("DUMMY"); else // more robustness on wrong e.g. OFMC output
						if (t.isSet(aslanSpec)) {
							sb.append(t.getRepresentation(aslanSpec));
						}
						else {
							sb.append(t.getRepresentationNice(setProvider, aslanSpec));
						}
						first = false;
					}
					sb.append(")");
				}
			}
			return sb.toString();
		}
	}

	@Override
	public IGroundTerm reduce(Map<Variable, IGroundTerm> assigned) {
		List<IGroundTerm> redArgs = new ArrayList<IGroundTerm>();
		for (IGroundTerm t : parameters) {
			redArgs.add(t.reduce(assigned));
		}
		return new GroundFunction(getLocation(), getName(), redArgs);
	}

}
