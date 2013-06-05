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

import java.util.ArrayList;
import java.util.List;
import org.avantssar.commons.ErrorGatherer;

public class SATMCSymbolsProvider implements ISymbolsProvider {

	private static PrimitiveType CHANNEL = new PrimitiveType(null, null, "channel", true);

	@Override
	public List<PrimitiveType> getPrimitiveTypes() {
		List<PrimitiveType> types = new ArrayList<PrimitiveType>();
		types.add(CHANNEL);
		return types;
	}

	@Override
	public List<Function> getFunctions(ErrorGatherer err) {
		List<Function> syms = new ArrayList<Function>();
//		syms.add(new Function(null, err, "dy", IASLanSpec.FACT, true, CHANNEL));
		syms.add(new Function(null, err, "unilateral_conf_auth", IASLanSpec.FACT, true, CHANNEL, CHANNEL, IASLanSpec.AGENT));
		syms.add(new Function(null, err, "bilateral_conf_auth", IASLanSpec.FACT, true, CHANNEL, CHANNEL, IASLanSpec.AGENT, IASLanSpec.AGENT));
		syms.add(new Function(null, err, "link", IASLanSpec.FACT, true, CHANNEL, CHANNEL));
		syms.add(new Function(null, err, "confidential_to", IASLanSpec.FACT, true, CHANNEL, IASLanSpec.AGENT));
		syms.add(new Function(null, err, "weakly_confidential", IASLanSpec.FACT, true, CHANNEL));
		syms.add(new Function(null, err, "authentic_on", IASLanSpec.FACT, true, CHANNEL, IASLanSpec.AGENT));
		syms.add(new Function(null, err, "weakly_authentic", IASLanSpec.FACT, true, CHANNEL));
		syms.add(new Function(null, err, "resilient", IASLanSpec.FACT, true, CHANNEL));
//		syms.add(new Function(null, err, "ak", IASLanSpec.FACT, true, IASLanSpec.AGENT, IASLanSpec.MESSAGE));
		syms.add(new Function(null, err, "rcvd", IASLanSpec.FACT, true, IASLanSpec.AGENT, IASLanSpec.AGENT, IASLanSpec.MESSAGE, CHANNEL));
		syms.add(new Function(null, err, "sent", IASLanSpec.FACT, true, IASLanSpec.AGENT, IASLanSpec.AGENT, IASLanSpec.AGENT, IASLanSpec.MESSAGE, CHANNEL));
//		syms.add(new Function(null, err, "rdebug", IASLanSpec.FACT, true, IASLanSpec.AGENT, IASLanSpec.NAT, IASLanSpec.AGENT, IASLanSpec.MESSAGE, CHANNEL));
//		syms.add(new Function(null, err, "sdebug", IASLanSpec.FACT, true, IASLanSpec.AGENT, IASLanSpec.NAT, IASLanSpec.AGENT, IASLanSpec.MESSAGE, CHANNEL));
		return syms;
	}
}
