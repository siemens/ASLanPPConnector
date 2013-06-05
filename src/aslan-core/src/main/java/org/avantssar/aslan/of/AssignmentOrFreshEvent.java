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

public class AssignmentOrFreshEvent extends AbstractExecutionEvent {

	public enum Kind {
		Assign, Fresh, Match, StepLabel
	}

	private static final String LABEL = "assignment/fresh/match";

	private final String variableName;
	private final IGroundTerm term;
	private final Kind kind;
	private final String owner;
	private final IGroundTerm ownerIID;

	protected AssignmentOrFreshEvent(String entity, IGroundTerm iid, int lineNumber, String variableName, IGroundTerm term, Kind kind) {
		this(entity, iid, lineNumber, variableName, term, kind, null, null);
	}

	protected AssignmentOrFreshEvent(String entity, IGroundTerm iid, int lineNumber, String variableName, IGroundTerm term, Kind kind, String owner, IGroundTerm ownerIID) {
		super(entity, iid, lineNumber);
		this.variableName = variableName;
		this.term = term;
		this.kind = kind;
		this.owner = owner;
		this.ownerIID = ownerIID;
	}

	public String getVariableName() {
		return variableName;
	}

	public IGroundTerm getTerm() {
		return term;
	}

	public Kind getKind() {
		return kind;
	}

	public String getOwner() {
		return owner;
	}

	public IGroundTerm getOwnerIID() {
		return ownerIID;
	}

	@Override
	public void execute(ExecutionScenario exec, IASLanSpec aslanSpec, ErrorGatherer err, StringBuffer sb, boolean dontPrint) {
		// System.out.println("executing " + toString());
		String ownerName = getOwner();
		IGroundTerm ownerIID = getOwnerIID();
		if (ownerName == null) {
			ownerName = getEntity();
			ownerIID = getIID();
		}
		EntityState ent = exec.findEntity(ownerName, ownerIID);
		if (ent == null) {
			err.addException(OutputFormatErrorMessages.CANNOT_FIND_ENTITY_INSTANCE, ownerName, ownerIID.getRepresentation(aslanSpec));
		}
		ent.assign(exec, aslanSpec, variableName, term, getWhatByKind(), getAbbrevByKind(), getLineNumber(), kind, sb, dontPrint);
	}

	private String getWhatByKind() {
		if (kind == Kind.Assign) {
			return "assignment";
		}
		else if (kind == Kind.Match) {
			return "matching";
		}
		else if (kind == Kind.Fresh) {
			return "fresh";
		}
		else {
			return "step label update";
		}
	}

	private String getAbbrevByKind() {
		if (kind == Kind.Assign) {
			return "a";
		}
		else if (kind == Kind.Match) {
			return "m";
		}
		else if (kind == Kind.Fresh) {
			return "f";
		}
		else {
			return "a";
		}
	}

	private static Kind meta2kind(String meta) {
		if (meta.equals(MetaInfo.ASSIGNMENT)) {
			return Kind.Assign;
		}
		else if (meta.equals(MetaInfo.MATCH)) {
			return Kind.Match;
		}
		else if (meta.equals(MetaInfo.FRESH)) {
			return Kind.Fresh;
		}
		else {
			return Kind.StepLabel;
		}
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("[").append(LABEL).append("; ");
		super.fillBasic(sb);
		sb.append("variable: ").append(variableName).append("; ");
		sb.append("term: ").append(term.getRepresentation(null)).append("; ");
		if (owner != null) {
			sb.append("owner: ").append(owner).append("; ");
			sb.append("owner IID: ").append(ownerIID).append("; ");
		}
		sb.append("]");
		return sb.toString();
	}

	public static AssignmentOrFreshEvent fromMetaInfo(MetaInfo mi, ErrorGatherer err, Map<Variable, IGroundTerm> assigned) {
		LocationInfo loc = mi.getLocation(err);
		Bundle b = extractBasicFromMetaInfo(mi, err, assigned, new String[] { MetaInfo.ASSIGNMENT, MetaInfo.FRESH, MetaInfo.MATCH, MetaInfo.STEP_LABEL }, LABEL);
		IGroundTerm term = str2termEx(mi, MetaInfo.TERM, err, assigned, "Term", LABEL);
		String var = mi.getParameters().get(MetaInfo.VARIABLE);
		if (var == null) {
			err.addException(loc, OutputFormatErrorMessages.MISSING_AT, "Variable", LABEL);
		}
		String ownerName = mi.getParameters().get(MetaInfo.OWNER);
		String ownerIIDStr = mi.getParameters().get(MetaInfo.OWNER_IID);
		if ((ownerName != null) ^ (ownerIIDStr != null)) {
			err.addException(loc, OutputFormatErrorMessages.MISSING_AT, "Owner name or IID", LABEL);
		}
		IGroundTerm ownerIID = null;
		if (ownerName != null) {
			ownerIID = str2term(ownerIIDStr, err, assigned);
		}
		return new AssignmentOrFreshEvent(b.entity, b.iid, b.lineNumber, var, term, meta2kind(mi.getName()), ownerName, ownerIID);
	}

}
