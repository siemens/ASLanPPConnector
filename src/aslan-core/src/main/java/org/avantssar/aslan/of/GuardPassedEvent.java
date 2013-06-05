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
import org.avantssar.aslan.IASLanSpec;
import org.avantssar.aslan.MetaInfo;
import org.avantssar.aslan.Variable;
import org.avantssar.commons.ErrorGatherer;

public class GuardPassedEvent extends AbstractExecutionEvent {

	private static final String LABEL = "guard";

	private final IGroundTerm test;

	protected GuardPassedEvent(String entity, IGroundTerm iid, int lineNumber, IGroundTerm test) {
		super(entity, iid, lineNumber);
		this.test = test;
	}

	public IGroundTerm getTest() {
		return test;
	}

	@Override
	public void execute(ExecutionScenario exec, IASLanSpec aslanSpec, ErrorGatherer err, StringBuffer sb, boolean dontPrint) {
		// System.out.println("executing " + toString());
		if (!dontPrint) {
			sb.append("g  " + test.getRepresentationNice(exec, aslanSpec) + "  % on line " + getLineNumber()).append("\n");
		}
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("[").append(LABEL).append("; ");
		super.fillBasic(sb);
		sb.append("test: ").append(test.getRepresentation(null)).append("; ");
		sb.append("]");
		return sb.toString();
	}

	public static GuardPassedEvent fromMetaInfo(MetaInfo mi, ErrorGatherer err, Map<Variable, IGroundTerm> assigned) {
		Bundle b = extractBasicFromMetaInfo(mi, err, assigned, new String[] { MetaInfo.GUARD }, LABEL);
		IGroundTerm test = str2termEx(mi, MetaInfo.TEST, err, assigned, "Test", LABEL);
		return new GuardPassedEvent(b.entity, b.iid, b.lineNumber, test);
	}
}
