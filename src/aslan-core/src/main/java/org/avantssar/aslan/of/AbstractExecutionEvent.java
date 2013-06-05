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
import org.avantssar.aslan.MetaInfo;
import org.avantssar.aslan.Variable;
import org.avantssar.commons.ErrorGatherer;
import org.avantssar.commons.LocationInfo;

public abstract class AbstractExecutionEvent implements IExecutionEvent {

	protected static class Bundle {

		public String entity;
		public IGroundTerm iid;
		public int lineNumber;

		protected Bundle(String entity, IGroundTerm iid, int lineNumber) {
			this.entity = entity;
			this.iid = iid;
			this.lineNumber = lineNumber;
		}
	}

	private final String entity;
	private final IGroundTerm iid;
	private final int lineNumber;

	protected AbstractExecutionEvent(String entity, IGroundTerm iid, int lineNumber) {
		this.entity = entity;
		this.iid = iid;
		this.lineNumber = lineNumber;
	}

	public String getEntity() {
		return entity;
	}

	public IGroundTerm getIID() {
		return iid;
	}

	public int getLineNumber() {
		return lineNumber;
	}

	protected static Bundle extractBasicFromMetaInfo(MetaInfo mi, ErrorGatherer err, Map<Variable, IGroundTerm> assigned, String[] accepted, String label) {
//		LocationInfo loc = mi.getLocation(err);
		boolean found = false;
		for (String s : accepted) {
			if (mi.getName().equals(s)) {
				found = true;
				break;
			}
		}
		if (!found) {
			throw new IllegalArgumentException("Cannot load " + label + " from metainfo '" + mi.getName() + "'.");
		}
		String entityName = mi.getParameters().get(MetaInfo.ENTITY);
		String iidStr = mi.getParameters().get(MetaInfo.IID);
		IGroundTerm iid = null;
		if (iidStr != null) {
			iid = str2term(iidStr, err, assigned);
		}
		int lineNumber = mi.getIntParameter(MetaInfo.LINE, err);
		return new Bundle(entityName, iid, lineNumber);
	}

	protected static IGroundTerm str2termEx(MetaInfo mi, String key, ErrorGatherer err, Map<Variable, IGroundTerm> assigned, String what, String where) {
		LocationInfo loc = mi.getLocation(err);
		String str = mi.getParameters().get(key);
		if (str == null) {
			err.addException(loc, OutputFormatErrorMessages.MISSING_AT, what, where);
		}
		return str2term(str, err, assigned);
	}

	protected static IGroundTerm str2term(String str, ErrorGatherer err, Map<Variable, IGroundTerm> assigned) {
		IGroundTerm t = GroundTermBuilder.fromString(str);
		if (t == null) {
			err.addException(OutputFormatErrorMessages.INVALID_TERM_ENCODING, str);
		}
		t = t.reduce(assigned);
		return t;
	}

	protected void fillBasic(StringBuffer sb) {
		if (entity != null) {
			sb.append("entity: ").append(entity).append("; ");
		}
		if (iid != null) {
			sb.append("iid: ").append(iid).append("; ");
		}
		if (lineNumber != 0) {
			sb.append("line: ").append(lineNumber).append("; ");
		}
	}
}
