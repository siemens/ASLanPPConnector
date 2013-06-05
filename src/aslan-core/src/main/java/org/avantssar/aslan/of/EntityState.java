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

import java.util.TreeMap;
import org.avantssar.aslan.IASLanSpec;
import org.avantssar.aslan.of.AssignmentOrFreshEvent.Kind;
import org.avantssar.commons.ErrorGatherer;

public class EntityState extends TreeMap<String, IGroundTerm> {

	private static final String ACTOR = "Actor";
	private static final String IID = "IID";
	private static final String SL = "SL";

	private static final long serialVersionUID = 3263300033309649066L;

	private final String entity;

	public EntityState(NewEntityInstanceEvent newInstance, ErrorGatherer err) {
		this.entity = newInstance.getNewEntityName();
		this.putAll(newInstance.getParameters());
	}

	public IGroundTerm getActor() {
		return get(ACTOR);
	}

	public IGroundTerm getIID() {
		return get(IID);
	}

	public IGroundTerm getSL() {
		return get(SL);
	}

	public String describe(IASLanSpec aslanSpec) {
		StringBuffer sb = new StringBuffer();
		sb.append(entity).append("[");
		// sb.append(getActor().getRepresentation(aslanSpec)).append(", ");
		sb.append(getIID().getRepresentation(aslanSpec));
		// sb.append(", ").append(getSL().getRepresentation(aslanSpec));
		sb.append("]");
		return sb.toString();
	}

	public boolean answersTo(String name, IGroundTerm iid) {
		return (entity.equals(name) && getIID().equals(iid));
	}

	public void assign(ISetProvider setProvider, IASLanSpec aslanSpec, String variable, IGroundTerm value, String what, String abbrev, int line, Kind kind, StringBuffer sb, boolean dontPrint) {
		if (!dontPrint) {
			if (kind != Kind.StepLabel) {
				sb.append(abbrev + "  " + describe(aslanSpec) + "." + variable + " := " + value.getRepresentation(aslanSpec) + "  % from " + what + " on line " + line).append("\n");
			}
		}
		put(variable, value);
		// System.out.println(toString());
	}

	public String getRepresentationNice(ISetProvider setProvider, IASLanSpec aslanSpec) {
		StringBuffer sb = new StringBuffer();
		sb.append(entity);
		sb.append("[Actor=");
		sb.append(getActor().getRepresentation(aslanSpec));
		sb.append(", IID=").append(getIID().getRepresentation(aslanSpec));
		sb.append(", SL=").append(getSL().getRepresentation(aslanSpec));
		for (String var : keySet()) {
			if (!var.equals(ACTOR) && !var.equals(IID) && !var.equals(SL)) {
				IGroundTerm t = get(var);
				sb.append(", ");
				sb.append(var);
				sb.append("=");
				sb.append(t.getRepresentationNice(setProvider, aslanSpec));
			}
		}
		sb.append("]");
		return sb.toString();
	}
}
