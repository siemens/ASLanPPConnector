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

package org.avantssar.aslan;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.antlr.runtime.ANTLRInputStream;
import org.antlr.runtime.CommonTokenStream;
import org.avantssar.aslan.metainfoParser.metainfo_return;
import org.avantssar.aslan.of.OutputFormatErrorMessages;
import org.avantssar.commons.ErrorGatherer;
import org.avantssar.commons.LocationInfo;

public class MetaInfo implements ICommentEntry {

	public static final String SPECIFICATION = "specification";
	public static final String CHANNEL_MODEL = "channel_model";
	public static final String CONNECTOR_NAME = "connector_name";
	public static final String CONNECTOR_VERSION = "connector_version";
	public static final String NAME = "name";
	public static final String VERSION = "version";
	public static final String CONNECTOR_OPTIONS = "connector_options";
	public static final String IID = "iid";
	public static final String LINE = "line";
	public static final String COL = "col";
	public static final String NEXT = "next";
	public static final String ENTITY = "entity";
	public static final String NEW_INSTANCE = "new_instance";
	public static final String SYMBOLIC_INSTANCE = "symbolic_instance";
	public static final String ASSIGNMENT = "assignment";
	public static final String FRESH = "fresh";
	// public static final String CHILD = "child";
	public static final String NEW_ENTITY = "new_entity";
	// TODO: what kind of guard: if, else, while, out-of-while, or select?
	public static final String GUARD = "guard";
	public static final String COMMUNICATION_GUARD = "communication_guard";
	public static final String MATCH = "match";
	public static final String TEST = "test";
	public static final String INTRODUCE = "introduce";
	public static final String RETRACT = "retract";
	public static final String FACT = "fact";
	public static final String VARIABLE = "variable";
	public static final String TERM = "term";
	public static final String OWNER = "owner";
	public static final String OWNER_IID = "owner_iid";
	public static final String COMMUNICATION = "communication";
	public static final String SENDER = "sender";
	public static final String RECEIVER = "receiver";
	public static final String PAYLOAD = "payload";
	public static final String CHANNEL = "channel";
	public static final String DIRECTION = "direction";
	public static final String SEND = "send";
	public static final String RECEIVE = "receive";
	public static final String HORN_CLAUSE = "horn_clause";
	public static final String GOAL = "goal";
	public static final String STEP_LABEL = "step_label";
	public static final String ORIGINAL_NAME = "original_name";

	private static final String SEPARATOR = ";";
	private static final String PARAM_ASSIGNMENT = "=";

	private final String name;
	private final List<String> flags = new ArrayList<String>();
	private final Map<String, String> parameters = new LinkedHashMap<String, String>();

	public MetaInfo(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void addFlag(String flag) {
		flags.add(flag);
	}

	public List<String> getFlags() {
		return flags;
	}

	public void addParameter(String key, String value) {
		parameters.put(key, value);
	}

	public Map<String, String> getParameters() {
		return parameters;
	}

	@Override
	public String getLine() {
		StringBuffer sb = new StringBuffer();
		sb.append(" @").append(name);
		if (flags.size() > 0 || parameters.size() > 0) {
			sb.append("(");
			boolean first = true;
			for (String f : flags) {
				if (!first) {
					sb.append(SEPARATOR).append(" ");
				}
				sb.append(f);
				first = false;
			}
			for (String k : parameters.keySet()) {
				if (!first) {
					sb.append(SEPARATOR).append(" ");
				}
				sb.append(k);
				sb.append(PARAM_ASSIGNMENT);
				sb.append(parameters.get(k));
				first = false;
			}
			sb.append(")");
		}
		return sb.toString();
	}

	@Override
	public String toString() {
		return getLine();
	}

	public static MetaInfo fromString(String commentLine) {
		ByteArrayInputStream bais = new ByteArrayInputStream(commentLine.getBytes());
		try {
			ANTLRInputStream antlrStream = new ANTLRInputStream(bais);
			metainfoLexer lexer = new metainfoLexer(antlrStream);
			CommonTokenStream tokens = new CommonTokenStream(lexer);
			metainfoParser parser = new metainfoParser(tokens);
			metainfo_return mr = parser.metainfo();
			if (!lexer.wasAnyError && !parser.wasAnyError) {
				MetaInfo mi = new MetaInfo(mr.name);
				for (String f : mr.flags) {
					mi.addFlag(f);
				}
				for (String k : mr.parameters.keySet()) {
					String v = mr.parameters.get(k);
					mi.addParameter(k, v);
				}
				return mi;
			}
			else {
				return null;
			}
		}
		catch (Exception e) {
			// silently ignore any errors for now
			return null;
		}
	}
	
	public int getIntParameter(String key, ErrorGatherer err) {
		String str = getParameters().get(key);
		if (str != null) {
			try {
				return Integer.parseInt(str);
			}
			catch (NumberFormatException e) {
				err.addError(OutputFormatErrorMessages.INVALID_NUMBER, str);
			}
		}
		return 0; // TODO or better -1 ?;
	}
	
	public LocationInfo getLocation(ErrorGatherer err) {
		return new LocationInfo(getIntParameter(MetaInfo.LINE, err),
				                getIntParameter(MetaInfo.COL , err),
                                getIntParameter(MetaInfo.NEXT, err));
	}

	public static void main(String[] args) {
		MetaInfo mi = MetaInfo
				.fromString(" @communication_guard(entity=Bob; iid=E_S_B_IID; line=42; col=17; next=18; sender=E_S_B_A; receiver=E_S_B_Actor; payload=crypt(pk(E_S_B_Actor), E_S_B_Nb); fact=iknows(crypt(pk(E_S_B_Actor), E_S_B_Nb)); direction=receive)");
		System.out.println(mi.getLine());
	}

}
