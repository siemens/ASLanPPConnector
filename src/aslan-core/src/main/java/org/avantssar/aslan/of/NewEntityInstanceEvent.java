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
import java.util.TreeMap;
import org.avantssar.aslan.IASLanSpec;
import org.avantssar.aslan.MetaInfo;
import org.avantssar.aslan.Variable;
import org.avantssar.commons.ErrorGatherer;
import org.avantssar.commons.LocationInfo;

public class NewEntityInstanceEvent extends AbstractExecutionEvent {

	private static final String LABEL = "new entity instance";

	private final String newEntityName;
	private final Map<String, IGroundTerm> parameters = new TreeMap<String, IGroundTerm>();

	protected NewEntityInstanceEvent(String entity, IGroundTerm iid, int lineNumber, String newEntityName, Map<String, IGroundTerm> parameters) {
		super(entity, iid, lineNumber);
		this.newEntityName = newEntityName;
		this.parameters.putAll(parameters);
	}

	public String getNewEntityName() {
		return newEntityName;
	}

	public Map<String, IGroundTerm> getParameters() {
		return parameters;
	}

	@Override
	public void execute(ExecutionScenario exec, IASLanSpec aslanSpec, ErrorGatherer err, StringBuffer sb, boolean dontPrint) {
		// System.out.println("executing " + toString());
		EntityState ent = new EntityState(this, err);
		exec.addEntity(ent, getLineNumber(), sb, dontPrint);
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("[").append(LABEL).append("; ");
		super.fillBasic(sb);
		sb.append("new entity: ").append(newEntityName).append("; ");
		for (String var : parameters.keySet()) {
			sb.append(var).append(": ").append(parameters.get(var).getRepresentation(null)).append("; ");
		}
		sb.append("]");
		return sb.toString();
	}

	public static NewEntityInstanceEvent fromMetaInfo(MetaInfo mi, ErrorGatherer err, Map<Variable, IGroundTerm> assigned) {
		LocationInfo loc = mi.getLocation(err);
		Bundle b = extractBasicFromMetaInfo(mi, err, assigned, new String[] { MetaInfo.NEW_INSTANCE }, LABEL);
		String newEntityName = null;
		Map<String, IGroundTerm> vars = new TreeMap<String, IGroundTerm>();
		for (String key : mi.getParameters().keySet()) {
			String value = mi.getParameters().get(key);
			if (key.equals(MetaInfo.NEW_ENTITY)) {
				newEntityName = value;
			}
			else if (Character.isUpperCase(key.charAt(0))) {
				IGroundTerm t = GroundTermBuilder.fromString(value);
				if (t == null) {
					err.addException(loc, OutputFormatErrorMessages.INVALID_TERM_ENCODING, value);
				}
				t = t.reduce(assigned);
				vars.put(key, t);
			}
		}
		if (newEntityName == null) {
			err.addException(loc, OutputFormatErrorMessages.ENTITY_NAME_MISSING);
		}
		return new NewEntityInstanceEvent(b.entity, b.iid, b.lineNumber, newEntityName, vars);
	}

}
