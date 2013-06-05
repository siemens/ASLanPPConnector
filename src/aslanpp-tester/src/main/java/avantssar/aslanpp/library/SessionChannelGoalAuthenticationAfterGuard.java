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

import org.avantssar.aslanpp.model.CommunicationTerm;
import org.avantssar.aslanpp.model.IExpression;
import org.avantssar.aslanpp.model.SelectStatement;
import org.avantssar.commons.ChannelEntry;
import avantssar.aslanpp.testing.Specification;

@Specification
public class SessionChannelGoalAuthenticationAfterGuard extends ChannelGoalAuthenticationAfterGuard {

	@Override
	protected void finishPPmodel() {
		aliceBody.add(entAlice.fresh(vAliceM.term().annotate(getPPprotName())));
	//	IntroduceStatement snd = aliceBody.add(entAlice.comm(entAlice.getActorSymbol().term(), vAliceBob.term(), vAliceM.term(), null, channelType, false, false, false));

		SelectStatement bobSel = bobBody.add(entBob.select());
		CommunicationTerm g = entBob.communication(vBobAlice.term(), entBob.getActorSymbol().term(), vBobM.matchedTerm().annotate(getPPprotName()), null, channelType, true, false, false);
		IExpression expr = g.expression();
		bobSel.choice(expr, entBob.block());

		env.sessionChannelGoal(getPPprotName(), alice.term(), bob.term(), ChannelEntry.authentic.arrow);
	}

}
