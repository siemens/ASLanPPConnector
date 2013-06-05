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

package org.avantssar.aslanpp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class MockTransition {

	private final String name;
	private final List<String> commentLines = new ArrayList<String>();
	private final List<String> conditions = new ArrayList<String>();
	private final List<String> lhsItems = new ArrayList<String>();
	private final List<String> rhsItems = new ArrayList<String>();
	private final List<String> parameters = new ArrayList<String>();
	private final List<String> freshVars = new ArrayList<String>();

	public MockTransition(String name) {
		this.name = name;
	}

	public MockTransition addCommentLine(String s) {
		commentLines.add(s);
		Collections.sort(commentLines);
		return this;
	}

	public MockTransition addCondition(String name, String[] args) {
		conditions.add(renderFunction(name, args));
		Collections.sort(conditions);
		return this;
	}

	public MockTransition addNegatedCondition(String name, String[] args) {
		conditions.add(renderFunction("not", new String[] { renderFunction(name, args) }));
		Collections.sort(conditions);
		return this;
	}

	public MockTransition addLhsItem(String name, String[] args) {
		lhsItems.add(renderFunction(name, args));
		Collections.sort(lhsItems);
		return this;
	}

	public MockTransition addLhsStateItem(String name, String[] args) {
		lhsItems.add(renderFunction("state_" + name, args));
		Collections.sort(lhsItems);
		return this;
	}

	public MockTransition addNegatedLhsItem(String name, String[] args) {
		lhsItems.add(renderFunction("not", new String[] { renderFunction(name, args) }));
		Collections.sort(lhsItems);
		return this;
	}

	public MockTransition addRhsItem(String name, String[] args) {
		rhsItems.add(renderFunction(name, args));
		Collections.sort(rhsItems);
		return this;
	}

	public MockTransition addRhsStateItem(String name, String[] args) {
		rhsItems.add(renderFunction("state_" + name, args));
		Collections.sort(rhsItems);
		return this;
	}

	public MockTransition addNegatedRhsItem(String name, String[] args) {
		rhsItems.add(renderFunction("not", new String[] { renderFunction(name, args) }));
		Collections.sort(rhsItems);
		return this;
	}

	public MockTransition addParameter(String s) {
		parameters.add(s);
		Collections.sort(parameters);
		return this;
	}

	public MockTransition addParameters(String[] pars) {
		for (String s : pars) {
			addParameter(s);
		}
		return this;
	}

	public MockTransition addFreshVar(String s) {
		freshVars.add(s);
		Collections.sort(freshVars);
		return this;
	}

	public MockTransition addFreshVars(String[] vars) {
		for (String s : vars) {
			addFreshVar(s);
		}
		return this;
	}

	public static String renderFunction(String name, String[] args) {
		StringBuffer sb = new StringBuffer();
		sb.append(name).append("(");
		boolean first = true;
		for (String s : args) {
			if (!first) {
				sb.append(", ");
			}
			sb.append(s);
			first = false;
		}
		sb.append(")");
		return sb.toString();
	}

	public Collection<String> getCommentLines() {
		return commentLines;
	}

	public Collection<String> getConditions() {
		return conditions;
	}

	public Collection<String> getExistsVarNames() {
		return freshVars;
	}

	public Collection<String> getLhsItems() {
		return lhsItems;
	}

	public String getName() {
		return name;
	}

	public Collection<String> getParameters() {
		return parameters;
	}

	public Collection<String> getRhsItems() {
		return rhsItems;
	}

	@Override
	public String toString() {
		return getRepresentation();
	}

	public String getRepresentation() {
		boolean first = true;
		StringBuffer sb = new StringBuffer();
		// if (t.getCommentLines() != null && t.getCommentLines().size() > 0) {
		// for (String s : t.getCommentLines()) {
		// sb.append("% ").append(s).append("\n");
		// }
		// }
		sb.append("\nstep ").append(getName());
		first = true;
		if (getParameters() != null && getParameters().size() > 0) {
			sb.append("(");
			for (String s : getParameters()) {
				if (!first) {
					sb.append(", ");
				}
				sb.append(s);
				first = false;
			}
			sb.append(")");
		}
		sb.append(" :=\n");
		first = true;
		if (getLhsItems() != null && getLhsItems().size() > 0) {
			for (String s : getLhsItems()) {
				if (!first) {
					sb.append(".\n");
				}
				sb.append("\t").append(s);
				first = false;
			}
		}
		if (getConditions() != null && getConditions().size() > 0) {
			for (String s : getConditions()) {
				if (!first) {
					sb.append(" &\n");
				}
				sb.append("\t").append(s);
				first = false;
			}
		}
		sb.append("\n\t=");
		first = true;
		if (getExistsVarNames() != null && getExistsVarNames().size() > 0) {
			sb.append("[exists ");
			for (String s : getExistsVarNames()) {
				if (!first) {
					sb.append(", ");
				}
				sb.append(s);
				first = false;
			}
			sb.append("]=");
		}
		sb.append(">\n");
		first = true;
		if (getRhsItems() != null && getRhsItems().size() > 0) {
			for (String s : getRhsItems()) {
				if (!first) {
					sb.append(".\n");
				}
				first = false;
				sb.append("\t").append(s);
			}
		}
		sb.append("\n");
		return sb.toString();
	}
}
