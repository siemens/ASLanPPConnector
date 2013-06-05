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
import org.avantssar.aslan.Function;
import org.avantssar.aslan.IASLanSpec;
import org.avantssar.aslan.InitialState;
import org.avantssar.aslan.RewriteRule;
import org.avantssar.aslanpp.model.ASLanPPSpecification;
import org.avantssar.aslanpp.model.BlockStatement;
import org.avantssar.aslanpp.model.ConstantSymbol;
import org.avantssar.aslanpp.model.Entity;
import org.avantssar.aslanpp.model.Goal;
import org.avantssar.aslanpp.model.IntroduceStatement;
import org.avantssar.aslanpp.model.Prelude;
import org.avantssar.aslanpp.model.VariableSymbol;
import org.avantssar.commons.ChannelModel;
import org.avantssar.commons.TranslatorOptions;
import org.avantssar.commons.ChannelEntry.Type;
import avantssar.aslan.backends.Verdict;
import avantssar.aslanpp.testing.Specification;

@Specification
public class OneWay_AgentToAgent extends AbstractChannelledSpecProvider {

	protected Entity alice, bob;
	protected VariableSymbol vAliceB, vBobPartner;
	protected ConstantSymbol cppToken;

	protected org.avantssar.aslan.Variable vAlice_Actor, vAlice_ID, vAlice_B, vBob_Actor, vBob_Partner, vBob_PartnerMatch;
	protected org.avantssar.aslan.Constant cToken;

	protected IASLanSpec spec;

	@Override
	public Verdict getExpectedVerdict() {
		if (cm == ChannelModel.CCM) {
			if (channelType.type == Type.Confidential || channelType.type == Type.Secure) {
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
	public ASLanPPSpecification getASLanPPSpecification() {
		ASLanPPSpecification spec = startASLanPPSpec();

		ConstantSymbol cAlice = env.constants(tppAgent, "alice");
		ConstantSymbol cBob = env.constants(tppAgent, "bob");
		env.group(cAlice, cBob);
		cppToken = env.constants(tppMessage, "token");
		cppToken.setNonPublic(true);

		alice = env.entity("Alice");
		alice.addParameter(Entity.ACTOR_PREFIX, tppAgent);
		vAliceB = alice.addParameter("B", tppAgent);
		alice.group(alice.getActorSymbol(), vAliceB);
		BlockStatement aliceBody = alice.body(alice.block());
		aliceBody.add(aslanppSend());

		bob = env.entity("Bob");
		bob.addParameter(Entity.ACTOR_PREFIX, tppAgent);
		vBobPartner = aslanppBobPartner();
		BlockStatement bobBody = bob.body(bob.block());
		bobBody.add(aslanppReceive());
		Goal gNeverDone = bob.goal("safe");
		gNeverDone.setFormula(spec.findFunction(Prelude.IKNOWS).term(cppToken.term()).expression().not());

		BlockStatement envBody = env.body(env.block());
		envBody.add(env.newInstance(alice, cAlice.term(), cBob.term()));
		envBody.add(env.newInstance(bob, cBob.term()));

		return spec;
	}

	protected VariableSymbol aslanppBobPartner() {
		return bob.addStateVariable("Partner", tppAgent);
	}

	protected IntroduceStatement aslanppSend() {
		return alice.comm(alice.getActorSymbol().term(), vAliceB.term(), cppToken.term(), null, channelType, false, false, false);
	}

	protected IntroduceStatement aslanppReceive() {
		return bob.comm(vBobPartner.matchedTerm(), bob.getActorSymbol().term(), cppToken.term(), null, channelType, true, false, false);
	}

	@Override
	public IASLanSpec getExpectedASLanTranslation(TranslatorOptions options) {
		spec = startASLanSpec();

		Function fsEnv = spec.function(getStateFunctionName("Environment"), IASLanSpec.FACT, IASLanSpec.AGENT, IASLanSpec.NAT, IASLanSpec.NAT);
		Function fsAlice = spec.function(getStateFunctionName("Alice"), IASLanSpec.FACT, IASLanSpec.AGENT, IASLanSpec.NAT, IASLanSpec.NAT, IASLanSpec.AGENT);
		Function fsBob = spec.function(getStateFunctionName("Bob"), IASLanSpec.FACT, IASLanSpec.AGENT, IASLanSpec.NAT, IASLanSpec.NAT, getBobPartnerType());

		org.avantssar.aslan.Constant cAlice = spec.constant("alice", IASLanSpec.AGENT);
		org.avantssar.aslan.Constant cBob = spec.constant("bob", IASLanSpec.AGENT);
		cToken = spec.constant("token", IASLanSpec.MESSAGE);

		org.avantssar.aslan.Variable vEnv_Actor = spec.variable("Actor", IASLanSpec.AGENT);
		org.avantssar.aslan.Variable vEnv_ID = spec.variable("IID", IASLanSpec.NAT);
		spec.variable("SL", IASLanSpec.NAT);

		vAlice_Actor = spec.variable("E_A_Actor", IASLanSpec.AGENT);
		vAlice_ID = spec.variable("E_A_IID", IASLanSpec.NAT);
		spec.variable("E_A_SL", IASLanSpec.NAT);
		vAlice_B = spec.variable("B", IASLanSpec.AGENT);

		vBob_Actor = spec.variable("E_B_Actor", IASLanSpec.AGENT);
        org.avantssar.aslan.Variable vBob_SL = spec.variable("E_B_SL", IASLanSpec.NAT);
		org.avantssar.aslan.Variable vBob_ID = spec.variable("E_B_IID", IASLanSpec.NAT);
		spec.variable("E_B_SL", IASLanSpec.NAT);
		vBob_Partner = spec.variable("Partner", getBobPartnerType());
		vBob_PartnerMatch = spec.variable(getBobPartnerName(), getBobPartnerType());

		Constant cDummyPartner = spec.constant("dummy_" + getBobPartnerType().getRepresentation() + "_1", getBobPartnerType());

		InitialState init = spec.initialState("init");
		prefillInitialState(spec, init);
		init.addFact(IASLanSpec.IKNOWS.term(cAlice.term()));
		init.addFact(IASLanSpec.IKNOWS.term(cBob.term()));
		init.addFact(fsEnv.term(cRoot.term(), spec.numericTerm(0), spec.numericTerm(1)));
		// init.addFact(IASLanSpec.IKNOWS.term(cDummyPartner.term()));

		RewriteRule step1 = spec.rule(getNextStepName("Environment"));
		org.avantssar.aslan.Variable vIID1 = spec.variable("IID_1", IASLanSpec.NAT);
		org.avantssar.aslan.Variable vIID2 = spec.variable("IID_2", IASLanSpec.NAT);
		step1.addLHS(fsEnv.term(vEnv_Actor.term(), vEnv_ID.term(), spec.numericTerm(1)));
		step1.addRHS(fsEnv.term(vEnv_Actor.term(), vEnv_ID.term(), spec.numericTerm(3)));
		step1.addRHS(fsAlice.term(cAlice.term(), vIID1.term(), spec.numericTerm(1), cBob.term()));
		step1.addRHS(fsBob.term(cBob.term(), vIID2.term(), spec.numericTerm(1), cDummyPartner.term()));
		step1.addRHS(fChild.term(vEnv_ID.term(), vIID1.term()));
		step1.addRHS(fChild.term(vEnv_ID.term(), vIID2.term()));
		step1.addExists(vIID1);
		step1.addExists(vIID2);

		RewriteRule step2 = spec.rule(getNextStepName("Alice"));
		step2.addLHS(fsAlice.term(vAlice_Actor.term(), vAlice_ID.term(), spec.numericTerm(1), vAlice_B.term()));
		step2.addRHS(fsAlice.term(vAlice_Actor.term(), vAlice_ID.term(), spec.numericTerm(2), vAlice_B.term()));
		step2.addRHS(translateSend());
		step2.addLHS(fDishonest.term(vAlice_Actor.term()).negate());

		RewriteRule step3 = spec.rule(getNextStepName("Bob"));
		step3.addLHS(fsBob.term(vBob_Actor.term(), vBob_ID.term(), spec.numericTerm(1), vBob_Partner.term()));
		step3.addLHS(translateReceive());
		if (cm == ChannelModel.CCM) {
			step3.addRHS(fsBob.term(vBob_Actor.term(), vBob_ID.term(), spec.numericTerm(2), getBobStatePartner()));
		}
		else {
			step3.addRHS(fsBob.term(vBob_Actor.term(), vBob_ID.term(), spec.numericTerm(2), vBob_PartnerMatch.term()));
		}
		step3.addLHS(fDishonest.term(vBob_Actor.term()).negate());

		org.avantssar.aslan.AttackState g = spec.attackState("safe");
		g.addTerm(spec.findFunction("iknows").term(cToken.term()));
        g.addTerm(fsBob.term(vBob_Actor.term(), vBob_ID.term(), vBob_SL.term(), vBob_Partner.term()));

		return spec;
	}

	protected String getBobPartnerName() {
		return "Partner_1";
	}

	protected org.avantssar.aslan.IType getBobPartnerType() {
		return IASLanSpec.AGENT;
	}

	protected org.avantssar.aslan.ITerm getBobStatePartner() {
		return channelType.type == Type.Regular || channelType.type == Type.Confidential ? vBob_Partner.term() : vBob_PartnerMatch.term();
	}

	protected org.avantssar.aslan.ITerm translateSend() {
		return doSend(cm, vAlice_Actor.term(), vAlice_Actor.term(), vAlice_B.term(), cToken.term());
	}

	protected org.avantssar.aslan.ITerm translateReceive() {
		return doReceive(cm, vBob_Actor.term(), vBob_PartnerMatch.term(), vBob_PartnerMatch.term(), cToken.term());
	}
}
