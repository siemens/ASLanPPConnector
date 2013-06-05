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
import org.avantssar.commons.LocationInfo;

public class IntroducedOrRetractFactEvent extends AbstractExecutionEvent {

	private static final String LABEL = "fact introduction/retraction";

	private final IGroundTerm fact;
	private final boolean introduce;

	protected IntroducedOrRetractFactEvent(String entity, IGroundTerm iid, int lineNumber, IGroundTerm fact, boolean introduce) {
		super(entity, iid, lineNumber);
		this.fact = fact;
		this.introduce = introduce;
	}

	public IGroundTerm getFact() {
		return fact;
	}

	public boolean isIntroduced() {
		return introduce;
	}

	@Override
	public void execute(ExecutionScenario exec, IASLanSpec aslanSpec, ErrorGatherer err, StringBuffer sb, boolean dontPrint) {
		// System.out.println("!executing " + toString());
		if (introduce) {
			exec.addFact(fact, getLineNumber(), sb, dontPrint);
		}
		else {
			exec.removeFact(fact, getLineNumber(), sb, dontPrint);
		}
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("[").append(LABEL).append("; ");
		super.fillBasic(sb);
		sb.append("fact: ").append(fact.getRepresentation(null)).append("; ");
		sb.append("]");
		return sb.toString();
	}

	public static IntroducedOrRetractFactEvent fromMetaInfo(MetaInfo mi, ErrorGatherer err, Map<Variable, IGroundTerm> assigned) {
		LocationInfo loc = mi.getLocation(err);
		Bundle b = extractBasicFromMetaInfo(mi, err, assigned, new String[] { MetaInfo.INTRODUCE, MetaInfo.RETRACT }, LABEL);
		String factStr = mi.getParameters().get(MetaInfo.FACT);
		if (factStr == null) {
			err.addException(loc, OutputFormatErrorMessages.MISSING_AT, "Fact", LABEL);
		}
		IGroundTerm t = str2term(factStr, err, assigned);
		return new IntroducedOrRetractFactEvent(b.entity, b.iid, b.lineNumber, t, mi.getName().equals(MetaInfo.INTRODUCE));
	}

}
