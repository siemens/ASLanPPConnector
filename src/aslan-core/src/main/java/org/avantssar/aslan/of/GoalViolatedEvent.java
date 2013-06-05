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
import org.avantssar.aslan.IParameterized;
import org.avantssar.aslan.MetaInfo;
import org.avantssar.aslan.Variable;
import org.avantssar.commons.ErrorGatherer;
import org.avantssar.commons.LocationInfo;

public class GoalViolatedEvent extends TreeMap<String, IGroundTerm> {

	private static final long serialVersionUID = -7496643498850659174L;

//	private static final String LABEL = "Goal";

	private final String originalName;
	private final String translatedName;
	private final int line;

	public GoalViolatedEvent(String originalName, String translatedName, int line) {
		this.originalName = originalName;
		this.translatedName = translatedName;
		this.line = line;
	}

	public String getOriginalName() {
		return originalName;
	}

	public String getTranslatedName() {
		return translatedName;
	}

	public int getLine() {
		return line;
	}

	public void execute(ExecutionScenario exec, IASLanSpec aslanSpec, StringBuffer sb) {
		sb.append("   ");
		sb.append(originalName != null ? originalName : translatedName);
		if (size() > 0) {
			sb.append("[");
			boolean first = true;
			for (String var : keySet()) {
				IGroundTerm t = get(var);
				if (!first) {
					sb.append(", ");
				}
				sb.append(var);
				sb.append("=");
				sb.append(t.getRepresentationNice(exec, aslanSpec));
				first = false;
			}
			sb.append("]");
		}
		if (line > 0) {
			sb.append("  % on line " + line).append("\n");
		}
		// sb.append("\n");
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(originalName != null ? originalName : translatedName);
		if (size() > 0) {
			sb.append("[");
			boolean first = true;
			for (String var : keySet()) {
				IGroundTerm t = get(var);
				if (!first) {
					sb.append(", ");
				}
				sb.append(var);
				sb.append("=");
				sb.append(t.getRepresentation(null));
				first = false;
			}
			sb.append("]");
		}
		return sb.toString();
	}

	public static GoalViolatedEvent fromMetaInfo(IParameterized clause, MetaInfo mi, ErrorGatherer err, Map<Variable, IGroundTerm> assigned) {
		String name = mi.getParameters().get(MetaInfo.NAME);
		LocationInfo loc = mi.getLocation(err);
		GoalViolatedEvent clEvent = new GoalViolatedEvent(name, clause.getName(), loc.line);
		for (String key : mi.getParameters().keySet()) {
			String value = mi.getParameters().get(key);
			if (Character.isUpperCase(key.charAt(0))) {
				IGroundTerm t = GroundTermBuilder.fromString(value);
				if (t == null) {
					err.addException(loc, OutputFormatErrorMessages.INVALID_TERM_ENCODING, value);
				}
				t = t.reduce(assigned);
				clEvent.put(key, t);
			}
		}
		return clEvent;
	}
}
