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

import org.avantssar.commons.ChannelEntry;
import avantssar.aslanpp.testing.Specification;

@Specification
public class SessionChannelGoalSecrecyAndAuthentication extends ChannelGoalSecrecyAndAuthentication {

	@Override
	protected void finishPPmodel() {
		aliceBody.add(entAlice.fresh(vAliceM.term().annotate(getPPAuth()).annotate(getPPConf())));
	//	IntroduceStatement snd = aliceBody.add(entAlice.comm(entAlice.getActorSymbol().term(), vAliceBob.term(), vAliceM.term(), null, channelType, false, false, false));

	//	IntroduceStatement rcv = bobBody.add(entBob.comm(vBobAlice.term(), entBob.getActorSymbol().term(), vBobM.matchedTerm().annotate(getPPAuth()).annotate(getPPConf()), null, channelType, true, false,	false));

		env.sessionChannelGoal(getPPConf(), alice.term(), bob.term(), ChannelEntry.confidential.arrow);
		env.sessionChannelGoal(getPPAuth(), alice.term(), bob.term(), ChannelEntry.authentic.arrow);
	}

	private String getPPConf() {
		return "secret_payload";
	}

	private String getPPAuth() {
		return "auth_payload";
	}
}
