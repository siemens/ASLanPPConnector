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

import org.avantssar.aslan.AttackState;
import org.avantssar.aslan.Constant;
import org.avantssar.aslan.Function;
import org.avantssar.aslan.IASLanSpec;
import org.avantssar.aslan.RewriteRule;
import org.avantssar.aslan.Variable;
import org.avantssar.aslanpp.model.IntroduceStatement;
import org.avantssar.aslanpp.model.Prelude;
import org.avantssar.commons.ChannelEntry;
import org.avantssar.commons.ChannelModel;
import org.avantssar.commons.ChannelEntry.Type;
import avantssar.aslan.backends.Verdict;
import avantssar.aslanpp.testing.Specification;

@Specification
public class ChannelGoalSecrecyAndAuthentication extends ChannelGoalSecrecy {

	protected Function fRequest;
	protected Function fWitness;

	@Override
	public Verdict getExpectedVerdict() {
		if (cm == ChannelModel.CCM) {
			if (channelType.type == Type.Secure) {
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
		snd.attachChannelGoal(entAlice.chGoal(super.getPPprotName(), entAlice.getActorSymbol().term(), vAliceBob.term(), vAliceM.term(), ChannelEntry.confidential));
		snd.attachChannelGoal(entAlice.chGoal(this.getPPprotName(), entAlice.getActorSymbol().term(), vAliceBob.term(), vAliceM.term(), ChannelEntry.authentic));

		IntroduceStatement rcv = bobBody.add(entBob.comm(vBobAlice.term(), entBob.getActorSymbol().term(), vBobM.matchedTerm(), null, channelType, true, false, false));
		rcv.attachChannelGoal(entBob.chGoal(super.getPPprotName(), vBobAlice.term(), entBob.getActorSymbol().term(), vBobM.term(), ChannelEntry.confidential));
		rcv.attachChannelGoal(entBob.chGoal(this.getPPprotName(), vBobAlice.term(), entBob.getActorSymbol().term(), vBobM.term(), ChannelEntry.authentic));
	}

	@Override
	protected void finishModel(IASLanSpec spec, ChannelModel cm) {
		fRequest = spec.findFunction("request");
		fWitness = spec.findFunction("witness");

		Function fAliceSet = spec.function(super.getProtName() + "_set", spec.setType(IASLanSpec.AGENT), IASLanSpec.NAT);
		Function fBobSet = spec.function(super.getProtName() + "_set", spec.setType(IASLanSpec.AGENT), IASLanSpec.NAT);
		Constant cSecProt = spec.constant(super.getProtName(), IASLanSpec.PROTOCOL_ID);
		Constant cAuthProt = spec.constant(this.getProtName(), IASLanSpec.PROTOCOL_ID);

		Function fChild = spec.findFunction(Prelude.CHILD);

		RewriteRule step2 = spec.rule(getNextStepName("Alice"));
		step2.addLHS(fsAlice.term(vAliceActor.term(), vAliceIID.term(), spec.numericTerm(1), vAliceB.term(), vAliceMaslan.term()));
		step2.addRHS(fsAlice.term(vAliceActor.term(), vAliceIID.term(), spec.numericTerm(3 + getDeltaSteps()), vAliceB.term(), vAliceMFresh.term()));
		step2.addRHS(doSend(cm, vAliceActor.term(), vAliceActor.term(), vAliceB.term(), vAliceMFresh.term()));
		step2.addRHS(IASLanSpec.SECRET.term(vAliceMFresh.term(), cSecProt.term(), fAliceSet.term(vEnvIID.term())));
		step2.addRHS(IASLanSpec.CONTAINS.term(vAliceActor.term(), fAliceSet.term(vEnvIID.term())));
		step2.addRHS(IASLanSpec.CONTAINS.term(vAliceB.term(), fAliceSet.term(vEnvIID.term())));
		step2.addRHS(fWitness.term(vAliceActor.term(), IASLanSpec.INTRUDER.term(), cAuthProt.term(), vAliceMFresh.term()));
		step2.addLHS(fChild.term(vEnvIID.term(), vAliceIID.term()));
		step2.addRHS(fChild.term(vEnvIID.term(), vAliceIID.term()));
		step2.addExists(vAliceMFresh);
		step2.addLHS(fDishonest.term(vAliceActor.term()).negate());

		RewriteRule step3 = spec.rule(getNextStepName("Bob"));
		step3.addLHS(fsBob.term(vBobActor.term(), vBobIID.term(), spec.numericTerm(1), vBobA.term(), vBobMaslan.term()));
		step3.addLHS(doReceive(cm, vBobActor.term(), vBobA.term(), vBobA.term(), vBobMMatched.term()));
		step3.addRHS(fsBob.term(vBobActor.term(), vBobIID.term(), spec.numericTerm(2 + getDeltaSteps()), vBobA.term(), vBobMMatched.term()));
		step3.addRHS(IASLanSpec.SECRET.term(vBobMMatched.term(), cSecProt.term(), fBobSet.term(vEnvIID.term())));
		step3.addRHS(IASLanSpec.CONTAINS.term(vBobActor.term(), fBobSet.term(vEnvIID.term())));
		step3.addRHS(IASLanSpec.CONTAINS.term(vBobA.term(), fBobSet.term(vEnvIID.term())));
		step3.addRHS(fRequest.term(vBobActor.term(), vBobA.term(), cAuthProt.term(), vBobMMatched.term(), vBobIID.term()));
		step3.addLHS(fChild.term(vEnvIID.term(), vBobIID.term()));
		step3.addRHS(fChild.term(vEnvIID.term(), vBobIID.term()));
		step3.addLHS(fDishonest.term(vBobActor.term()).negate());
		decorateReceive(step3, fBobSet.term(vEnvIID.term()));
	}

	@Override
	protected void addGoals(IASLanSpec spec) {
		addSecrecyGoal(spec);
		addAuthGoal(spec);
	}

	protected void addAuthGoal(IASLanSpec spec) {
		Variable vMsg = spec.variable("E_aap_Msg", IASLanSpec.MESSAGE);
		Variable vReq = spec.variable("Req", IASLanSpec.AGENT);
		Variable vWit = spec.variable("Wit", IASLanSpec.AGENT);
		Variable vIID = spec.variable("E_aap_IID", IASLanSpec.NAT);
		Constant cProt = spec.findConstant(getProtName());

		AttackState as = spec.attackState(getGoalName());
		as.addTerm(IASLanSpec.REQUEST.term(vReq.term(), vWit.term(), cProt.term(), vMsg.term(), vIID.term()));
		as.addTerm(IASLanSpec.WITNESS.term(vWit.term(), IASLanSpec.INTRUDER.term(), cProt.term(), vMsg.term()).negate());
		as.addTerm(fDishonest.term(vWit.term()).negate());
	}

	@Override
	protected void addSecrecyGoal(IASLanSpec spec) {
		Variable vMsg = spec.variable("Msg", IASLanSpec.MESSAGE);
		Variable vKnowers = spec.variable("Knowers", spec.setType(IASLanSpec.AGENT));
		Constant cProt = spec.findConstant(super.getProtName());
		AttackState as = spec.attackState(super.getGoalName());
		as.addTerm(IASLanSpec.SECRET.term(vMsg.term(), cProt.term(), vKnowers.term()));
		as.addTerm(IASLanSpec.IKNOWS.term(vMsg.term()));
		as.addTerm(IASLanSpec.CONTAINS.term(IASLanSpec.INTRUDER.term(), vKnowers.term()).negate());
	}

	@Override
	protected String getPPprotName() {
		return "auth_payload";
	}

	@Override
	protected String getProtName() {
		return "auth_auth_payload";
	}

	@Override
	protected String getGoalName() {
		return "auth_auth_payload";
	}

}
