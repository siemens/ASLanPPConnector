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

import org.avantssar.aslan.FunctionTerm;
import org.avantssar.aslan.IASLanSpec;
import org.avantssar.aslan.RewriteRule;
import org.avantssar.aslanpp.model.IntroduceStatement;
import org.avantssar.commons.ChannelEntry;
import org.avantssar.commons.ChannelModel;
import org.avantssar.commons.ChannelEntry.Type;
import avantssar.aslan.backends.Verdict;
import avantssar.aslanpp.testing.Specification;

@Specification
public class ChannelGoalSecrecy extends SecrecyGoal {

	@Override
	public Verdict getExpectedVerdict() {
		if (cm == ChannelModel.CCM) {
			if (channelType.type == Type.Secure || channelType.type == Type.Confidential) {
				return Verdict.NoAttack;
			}
			else {
				return Verdict.Attack;
			}
		}
		else {
			return Verdict.NoAttack;
		}
	}

	@Override
	protected void finishPPmodel() {
		aliceBody.add(entAlice.fresh(vAliceM.term()));
		IntroduceStatement snd = aliceBody.add(entAlice.comm(entAlice.getActorSymbol().term(), vAliceBob.term(), vAliceM.term(), null, channelType, false, false, false));
		snd.attachChannelGoal(entAlice.chGoal(getPPprotName(), entAlice.getActorSymbol().term(), vAliceBob.term(), vAliceM.term(), ChannelEntry.confidential));

		IntroduceStatement rcv = bobBody.add(entBob.comm(vBobAlice.term(), entBob.getActorSymbol().term(), vBobM.matchedTerm(), null, channelType, true, false, false));
		rcv.attachChannelGoal(entBob.chGoal(getPPprotName(), vBobAlice.term(), entBob.getActorSymbol().term(), vBobM.term(), ChannelEntry.confidential));
	}

	@Override
	protected void decorateReceive(RewriteRule rr, FunctionTerm setTerm) {
		rr.addRHS(IASLanSpec.CONTAINS.term(IASLanSpec.INTRUDER.term(), setTerm));
	}

	@Override
	protected int getDeltaSteps() {
		return 0;
	}

	@Override
	protected String getPPprotName() {
		return "secret_payload";
	}

	@Override
	protected String getProtName() {
		return "secr_secret_payload";
	}

	@Override
	protected String getGoalName() {
		return "secr_secret_payload";
	}

}
