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

package avantssar.aslanpp.library;

import org.avantssar.aslan.Constant;
import org.avantssar.aslan.IASLanSpec;
import org.avantssar.aslan.Variable;
import org.avantssar.aslanpp.model.ConstantSymbol;
import org.avantssar.aslanpp.model.IntroduceStatement;
import org.avantssar.aslanpp.model.Prelude;
import org.avantssar.aslanpp.model.VariableSymbol;
import org.avantssar.commons.ChannelEntry.Type;
import avantssar.aslanpp.testing.Specification;

@Specification
public class OneWay_PseudoAgentToAgent extends OneWay_AgentToAgent {

	@Override
	protected VariableSymbol aslanppBobPartner() {
		return bob.addStateVariable("Partner", bob.findType(Prelude.PUBLIC_KEY));
	}

	@Override
	protected IntroduceStatement aslanppSend() {
		ConstantSymbol cPseudoA = alice.constants(alice.findType(Prelude.PUBLIC_KEY), "pseudo_a");
		cPseudoA.setNonPublic(true);
		return alice.comm(alice.getActorSymbol().term().pseudonym(cPseudoA.term()), vAliceB.term(), cppToken.term(), null, channelType, false, false, false);
	}

	@Override
	protected IntroduceStatement aslanppReceive() {
		return bob.comm(bob.unnamedMatch().pseudonym(vBobPartner.matchedTerm()), bob.getActorSymbol().term(), cppToken.term(), null, channelType, true, false, false);
	}

	@Override
	protected org.avantssar.aslan.IType getBobPartnerType() {
		return IASLanSpec.PUBLIC_KEY;
	}

	@Override
	protected org.avantssar.aslan.ITerm getBobStatePartner() {
		return channelType.type == Type.Regular || channelType.type == Type.Confidential ? vBob_Partner.term() : vBob_PartnerMatch.term();
	}

	@Override
	protected org.avantssar.aslan.ITerm translateSend() {
		Constant cPseudoA = spec.constant("pseudo_a", IASLanSpec.PUBLIC_KEY);
		return doSend(cm, vAlice_Actor.term(), cPseudoA.term(), vAlice_B.term(), cToken.term());
	}

	@Override
	protected org.avantssar.aslan.ITerm translateReceive() {
		Variable vDummy = spec.variable("Dummy", IASLanSpec.AGENT);
		return doReceive(cm, vBob_Actor.term(), vDummy.term(), vBob_PartnerMatch.term(), cToken.term());
	}

}
