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

import org.avantssar.aslan.Function;
import org.avantssar.aslan.IASLanSpec;
import org.avantssar.aslan.InitialState;
import org.avantssar.aslan.RewriteRule;
import org.avantssar.aslanpp.model.ASLanPPSpecification;
import org.avantssar.aslanpp.model.BlockStatement;
import org.avantssar.aslanpp.model.ConstantSymbol;
import org.avantssar.aslanpp.model.Entity;
import org.avantssar.aslanpp.model.FunctionSymbol;
import org.avantssar.aslanpp.model.Goal;
import org.avantssar.aslanpp.model.SimpleType;
import org.avantssar.aslanpp.model.VariableSymbol;
import org.avantssar.commons.ChannelModel;
import org.avantssar.commons.TranslatorOptions;
import org.avantssar.commons.ChannelEntry.Type;
import avantssar.aslanpp.testing.Specification;

@Specification
public class SimpleMessageExchangeWithReceive extends AbstractChannelledSpecProvider {

	@Override
	public ASLanPPSpecification getASLanPPSpecification() {
		ASLanPPSpecification spec = startASLanPPSpec();

		SimpleType tSpecialAgent = env.type("special_agent", tppAgent);
		SimpleType tSpecialMessage = env.type("special_message", tppMessage);
		ConstantSymbol cAlice = env.constants(tSpecialAgent, "alice");
		cAlice.setNonPublic(true);
		ConstantSymbol cBob = env.constants(tSpecialAgent, "bob");
		cBob.setNonPublic(true);
		env.group(cAlice, cBob);
		ConstantSymbol cToken = env.constants(tSpecialMessage, "token");
		cToken.setNonPublic(true);

		Entity alice = env.entity("Alice");
		alice.addParameter(Entity.ACTOR_PREFIX, tSpecialAgent);
		VariableSymbol vAliceB = alice.addParameter("B", tSpecialAgent);
		alice.group(alice.getActorSymbol(), vAliceB);
		BlockStatement aliceBody = alice.body(alice.block());
		aliceBody.add(alice.comm(alice.getActorSymbol().term(), vAliceB.term(), cToken.term(), null, channelType, false, false, false));

		Entity bob = env.entity("Bob");
		bob.addParameter(Entity.ACTOR_PREFIX, tSpecialAgent);
		VariableSymbol vBobPartner = bob.addStateVariable("Partner", tSpecialAgent);
		FunctionSymbol fDone = bob.addFunction("done", tppFact, tSpecialAgent);
		fDone.setNonInvertible(true);
		fDone.setNonPublic(true);
		BlockStatement bobBody = bob.body(bob.block());
		bobBody.add(bob.comm(vBobPartner.matchedTerm(), bob.getActorSymbol().term(), cToken.term(), null, channelType, true, false, false));
		bobBody.add(bob.introduce(fDone.term(bob.getActorSymbol().term())));
		Goal gNeverDone = bob.goal("never_done");
		gNeverDone.setFormula(fDone.term(cBob.term()).expression().not());

		BlockStatement envBody = env.body(env.block());
		envBody.add(env.newInstance(alice, cAlice.term(), cBob.term()));
		envBody.add(env.newInstance(bob, cBob.term()));

		return spec;
	}

	public IASLanSpec getExpectedASLanTranslation(TranslatorOptions options) {
		IASLanSpec spec = startASLanSpec();

		org.avantssar.aslan.PrimitiveType tSpecialAgent = spec.primitiveType("special_agent");
		tSpecialAgent.setSuperType(IASLanSpec.AGENT);
		org.avantssar.aslan.PrimitiveType tSpecialMessage = spec.primitiveType("special_message");
		tSpecialMessage.setSuperType(IASLanSpec.MESSAGE);
		org.avantssar.aslan.Constant cDummySpecialAgent = spec.constant("dummy_special_agent_1", tSpecialAgent);

		Function fsEnv = spec.function(getStateFunctionName("Environment"), IASLanSpec.FACT, IASLanSpec.AGENT, IASLanSpec.NAT, IASLanSpec.NAT);
		Function fsAlice = spec.function(getStateFunctionName("Alice"), IASLanSpec.FACT, tSpecialAgent, IASLanSpec.NAT, IASLanSpec.NAT, tSpecialAgent);
		Function fsBob = spec.function(getStateFunctionName("Bob"), IASLanSpec.FACT, tSpecialAgent, IASLanSpec.NAT, IASLanSpec.NAT, tSpecialAgent);

		org.avantssar.aslan.Constant cAlice = spec.constant("alice", tSpecialAgent);
		org.avantssar.aslan.Constant cBob = spec.constant("bob", tSpecialAgent);
		org.avantssar.aslan.Constant cToken = spec.constant("token", tSpecialMessage);

		org.avantssar.aslan.Variable vEnv_Actor = spec.variable("Actor", IASLanSpec.AGENT);
		org.avantssar.aslan.Variable vEnv_ID = spec.variable("IID", IASLanSpec.NAT);
	//	org.avantssar.aslan.Variable vEnv_Step = spec.variable("SL", IASLanSpec.NAT);

		org.avantssar.aslan.Variable vAlice_Actor = spec.variable("E_A_Actor", tSpecialAgent);
		org.avantssar.aslan.Variable vAlice_ID = spec.variable("E_A_IID", IASLanSpec.NAT);
	//	org.avantssar.aslan.Variable vAlice_Step = spec.variable("E_A_SL", IASLanSpec.NAT);
		org.avantssar.aslan.Variable vAlice_B = spec.variable("B", tSpecialAgent);

		org.avantssar.aslan.Variable vBob_Actor = spec.variable("E_B_Actor", tSpecialAgent);
		org.avantssar.aslan.Variable vBob_ID = spec.variable("E_B_IID", IASLanSpec.NAT);
		org.avantssar.aslan.Variable vBob_Step = spec.variable("E_B_SL", IASLanSpec.NAT);
		org.avantssar.aslan.Variable vBob_Partner = spec.variable("Partner", tSpecialAgent);
		org.avantssar.aslan.Variable vBob_PartnerMatch = spec.variable("Partner_1", tSpecialAgent);
		org.avantssar.aslan.Function fBob_Done = spec.function("done", IASLanSpec.FACT, tSpecialAgent);

		InitialState init = spec.initialState("init");
		prefillInitialState(spec, init);
		init.addFact(fsEnv.term(cRoot.term(), spec.numericTerm(0), spec.numericTerm(1)));
		// init.addFact(fIsAgent.term(cAlice.term()));
		// init.addFact(fIsAgent.term(cBob.term()));
		// init.addFact(IASLanSpec.IKNOWS.term(cDummySpecialAgent.term()));

		RewriteRule step1 = spec.rule(getNextStepName("Environment"));
		org.avantssar.aslan.Variable vIID1 = spec.variable("IID_1", IASLanSpec.NAT);
		org.avantssar.aslan.Variable vIID2 = spec.variable("IID_2", IASLanSpec.NAT);
		step1.addLHS(fsEnv.term(vEnv_Actor.term(), vEnv_ID.term(), spec.numericTerm(1)));
		step1.addRHS(fsEnv.term(vEnv_Actor.term(), vEnv_ID.term(), spec.numericTerm(3)));
		step1.addRHS(fsAlice.term(cAlice.term(), vIID1.term(), spec.numericTerm(1), cBob.term()));
		step1.addRHS(fsBob.term(cBob.term(), vIID2.term(), spec.numericTerm(1), cDummySpecialAgent.term()));
		step1.addRHS(fChild.term(vEnv_ID.term(), vIID1.term()));
		step1.addRHS(fChild.term(vEnv_ID.term(), vIID2.term()));
		step1.addExists(vIID1);
		step1.addExists(vIID2);

		RewriteRule step2 = spec.rule(getNextStepName("Alice"));
		step2.addLHS(fsAlice.term(vAlice_Actor.term(), vAlice_ID.term(), spec.numericTerm(1), vAlice_B.term()));
		step2.addRHS(fsAlice.term(vAlice_Actor.term(), vAlice_ID.term(), spec.numericTerm(2), vAlice_B.term()));
		step2.addRHS(doSend(cm, vAlice_Actor.term(), vAlice_Actor.term(), vAlice_B.term(), cToken.term()));
		step2.addLHS(fDishonest.term(vAlice_Actor.term()).negate());

		RewriteRule step3 = spec.rule(getNextStepName("Bob"));
		step3.addLHS(fsBob.term(vBob_Actor.term(), vBob_ID.term(), spec.numericTerm(1), vBob_Partner.term()));
		step3.addLHS(doReceive(cm, vBob_Actor.term(), vBob_PartnerMatch.term(), vBob_PartnerMatch.term(), cToken.term()));
		if (cm == ChannelModel.CCM) {
			step3.addRHS(fsBob.term(vBob_Actor.term(), vBob_ID.term(), spec.numericTerm(3), channelType.type == Type.Regular || channelType.type == Type.Confidential ? vBob_Partner.term()
					: vBob_PartnerMatch.term()));
		}
		else {
			step3.addRHS(fsBob.term(vBob_Actor.term(), vBob_ID.term(), spec.numericTerm(3), vBob_PartnerMatch.term()));
		}
		step3.addRHS(fBob_Done.term(vBob_Actor.term()));
		step3.addLHS(fDishonest.term(vBob_Actor.term()).negate());

		org.avantssar.aslan.AttackState g = spec.attackState("never_done");
		g.addTerm(fBob_Done.term(cBob.term()));
        g.addTerm(fsBob.term(vBob_Actor.term(), vBob_ID.term(), vBob_Step.term(), vBob_Partner.term()));

		return spec;
	}
}
