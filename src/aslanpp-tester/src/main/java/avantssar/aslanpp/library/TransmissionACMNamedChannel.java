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
import org.avantssar.aslan.InitialState;
import org.avantssar.aslan.RewriteRule;
import org.avantssar.aslan.Variable;
import org.avantssar.aslanpp.model.ASLanPPSpecification;
import org.avantssar.aslanpp.model.BlockStatement;
import org.avantssar.aslanpp.model.ConstantSymbol;
import org.avantssar.aslanpp.model.Entity;
import org.avantssar.aslanpp.model.FunctionSymbol;
import org.avantssar.aslanpp.model.Goal;
import org.avantssar.aslanpp.model.Prelude;
import org.avantssar.aslanpp.model.VariableSymbol;
import org.avantssar.commons.ChannelEntry;
import org.avantssar.commons.ChannelModel;
import org.avantssar.commons.TranslatorOptions;
import org.avantssar.commons.ChannelEntry.Type;
import avantssar.aslanpp.testing.Specification;

@Specification
public class TransmissionACMNamedChannel extends AbstractSpecProvider {

	@Override
	public ASLanPPSpecification getASLanPPSpecification() {
		ASLanPPSpecification spec = startASLanPPSpec();

		FunctionSymbol fncIssued = env.addFunction("issued", env.findType(Prelude.FACT), env.findType(Prelude.MESSAGE));
		fncIssued.setNonInvertible(true);
		fncIssued.setNonPublic(true);
		FunctionSymbol fncRevealed = env.addFunction("revealed", env.findType(Prelude.FACT), env.findType(Prelude.MESSAGE));
		fncRevealed.setNonInvertible(true);
		fncRevealed.setNonPublic(true);

		Entity entAlice = env.entity("Alice");
		entAlice.addParameter(Entity.ACTOR_PREFIX, env.findType(Prelude.AGENT));
		VariableSymbol alicePartner = entAlice.addParameter("Partner", env.findType(Prelude.AGENT));
		VariableSymbol aliceChannel = entAlice.addParameter("Ch_Out", env.findType(Prelude.CHANNEL));
		VariableSymbol aliceToken = entAlice.addStateVariable("Token", env.findType(Prelude.MESSAGE));
		BlockStatement aliceBody = entAlice.block();
		entAlice.body(aliceBody);
		aliceBody.add(entAlice.fresh(aliceToken.term()));
		ChannelEntry aliceCE;
		if (cm == ChannelModel.ACM) {
			aliceCE = ChannelEntry.from(Type.Regular, false, false, false, aliceChannel.getName());
		}
		else {
			aliceCE = ChannelEntry.from(Type.Regular, false, false, false, null);
		}
		aliceBody.add(entAlice.comm(entAlice.getActorSymbol().term(), alicePartner.term(), aliceToken.term(), null, aliceCE, false, false, false));
		aliceBody.add(entAlice.introduce(fncIssued.term(aliceToken.term())));

		Entity entBob = env.entity("Bob");
		entBob.addParameter(Entity.ACTOR_PREFIX, env.findType(Prelude.AGENT));
		VariableSymbol bobPartner = entBob.addParameter("Partner", env.findType(Prelude.AGENT));
		VariableSymbol bobChannel = entBob.addParameter("Ch_In", env.findType(Prelude.CHANNEL));
		VariableSymbol bobToken = entBob.addStateVariable("Token", env.findType(Prelude.MESSAGE));
		BlockStatement bobBody = entBob.block();
		entBob.body(bobBody);
		ChannelEntry bobCE;
		if (cm == ChannelModel.ACM) {
			bobCE = ChannelEntry.from(Type.Regular, false, false, false, bobChannel.getName());
		}
		else {
			bobCE = ChannelEntry.from(Type.Regular, false, false, false, null);
		}
		bobBody.add(entBob.comm(bobPartner.term(), entBob.getActorSymbol().term(), bobToken.matchedTerm(), null, bobCE, false, false, false));
		bobBody.add(entBob.introduce(fncRevealed.term(bobToken.term())));

		ConstantSymbol cAlice = env.constants(env.findType(Prelude.AGENT), "alice");
		ConstantSymbol cBob = env.constants(env.findType(Prelude.AGENT), "bob");
		ConstantSymbol channel = env.constants(env.findType(Prelude.CHANNEL), "ch_a2b");

		BlockStatement main = env.block();
		main.add(env.newInstance(entAlice, cAlice.term(), cBob.term(), channel.term()));
		main.add(env.newInstance(entBob, cBob.term(), cAlice.term(), channel.term()));
		env.body(main);

		Goal g = env.goal("never_revealed");
		VariableSymbol v = g.addUntypedVariable("X");
		g.setFormula(fncIssued.term(v.term()).expression().and(fncRevealed.term(v.term()).expression()).not().forall(v));

		return spec;
	}

	@Override
	public IASLanSpec getExpectedASLanTranslation(TranslatorOptions options) {
		IASLanSpec spec = startASLanSpec();

		Constant cDummyMessage1 = spec.constant("dummy_message_1", IASLanSpec.MESSAGE);
		Constant cDummyMessage2 = spec.constant("dummy_message_2", IASLanSpec.MESSAGE);

		Function fsEnv = spec.function(getStateFunctionName("Environment"), IASLanSpec.FACT, IASLanSpec.AGENT, IASLanSpec.NAT, IASLanSpec.NAT);
		Function fsAlice = spec.function(getStateFunctionName("Alice"), IASLanSpec.FACT, IASLanSpec.AGENT, IASLanSpec.NAT, IASLanSpec.NAT, IASLanSpec.AGENT, IASLanSpec.CHANNEL, IASLanSpec.MESSAGE);
		Function fsBob = spec.function(getStateFunctionName("Bob"), IASLanSpec.FACT, IASLanSpec.AGENT, IASLanSpec.NAT, IASLanSpec.NAT, IASLanSpec.AGENT, IASLanSpec.CHANNEL, IASLanSpec.MESSAGE);
		Function fsIssued = spec.function("issued", IASLanSpec.FACT, IASLanSpec.MESSAGE);
		Function fsRevealed = spec.function("revealed", IASLanSpec.FACT, IASLanSpec.MESSAGE);

		Variable vEnvActor = spec.variable("Actor", IASLanSpec.AGENT);
		Variable vEnvIID = spec.variable("IID", IASLanSpec.NAT);
		Variable vEnvIID_1 = spec.variable("IID_1", IASLanSpec.NAT);
		Variable vEnvIID_2 = spec.variable("IID_2", IASLanSpec.NAT);
        Variable vEnvSL = spec.variable("SL", IASLanSpec.NAT);
		Variable vAliceActor = spec.variable("E_A_Actor", IASLanSpec.AGENT);
		Variable vAliceIID = spec.variable("E_A_IID", IASLanSpec.NAT);
	//	Variable vAliceSL = spec.variable("E_A_SL", IASLanSpec.NAT);
		Variable vAlicePartner = spec.variable("Partner", IASLanSpec.AGENT);
		Variable vAliceChannel = spec.variable("Ch_Out", IASLanSpec.CHANNEL);
		Variable vAliceToken = spec.variable("Token", IASLanSpec.MESSAGE);
		Variable vAliceTokenFresh = spec.variable("Token_1", IASLanSpec.MESSAGE);
		Variable vBobActor = spec.variable("E_B_Actor", IASLanSpec.AGENT);
		Variable vBobIID = spec.variable("E_B_IID", IASLanSpec.NAT);
	//	Variable vBobSL = spec.variable("E_B_SL", IASLanSpec.NAT);
		Variable vBobPartner = spec.variable("E_B_Partner", IASLanSpec.AGENT);
		Variable vBobChannel = spec.variable("Ch_In", IASLanSpec.CHANNEL);
		Variable vBobToken = spec.variable("E_B_Token", IASLanSpec.MESSAGE);
		Variable vBobTokenMatched = spec.variable("E_B_Token_1", IASLanSpec.MESSAGE);
		Constant cAlice = spec.constant("alice", IASLanSpec.AGENT);
		Constant cBob = spec.constant("bob", IASLanSpec.AGENT);
		Constant channelAliceBob = spec.constant("ch_a2b", IASLanSpec.CHANNEL);
		Variable vX = spec.variable("X", IASLanSpec.MESSAGE);

		InitialState init = spec.initialState("init");
		prefillInitialState(spec, init);
		init.addFact(fsEnv.term(cRoot.term(), spec.numericTerm(0), spec.numericTerm(1)));
		init.addFact(IASLanSpec.IKNOWS.term(cAlice.term()));
		init.addFact(IASLanSpec.IKNOWS.term(cBob.term()));

		RewriteRule step1 = spec.rule(getNextStepName("Environment"));
		step1.addLHS(fsEnv.term(vEnvActor.term(), vEnvIID.term(), spec.numericTerm(1)));
		step1.addRHS(fsEnv.term(vEnvActor.term(), vEnvIID.term(), spec.numericTerm(3)));
		step1.addRHS(fsAlice.term(cAlice.term(), vEnvIID_1.term(), spec.numericTerm(1), cBob.term(), channelAliceBob.term(), cDummyMessage1.term()));
		step1.addRHS(fsBob.term(cBob.term(), vEnvIID_2.term(), spec.numericTerm(1), cAlice.term(), channelAliceBob.term(), cDummyMessage2.term()));
		step1.addExists(vEnvIID_1);
		step1.addExists(vEnvIID_2);
		step1.addRHS(fChild.term(vEnvIID.term(), vEnvIID_1.term()));
		step1.addRHS(fChild.term(vEnvIID.term(), vEnvIID_2.term()));

		RewriteRule step2 = spec.rule(getNextStepName("Alice"));
		step2.addLHS(fsAlice.term(vAliceActor.term(), vAliceIID.term(), spec.numericTerm(1), vAlicePartner.term(), vAliceChannel.term(), vAliceToken.term()));
		step2.addRHS(fsAlice.term(vAliceActor.term(), vAliceIID.term(), spec.numericTerm(4), vAlicePartner.term(), vAliceChannel.term(), vAliceTokenFresh.term()));
		if (cm == ChannelModel.ACM) {
			step2.addRHS(acmSent.term(vAliceActor.term(), vAliceActor.term(), vAlicePartner.term(), vAliceTokenFresh.term(), vAliceChannel.term()));
		}
		else {
			step2.addRHS(IASLanSpec.IKNOWS.term(vAliceTokenFresh.term()));
		}
		step2.addExists(vAliceTokenFresh);
		step2.addLHS(fDishonest.term(vAliceActor.term()).negate());
		step2.addRHS(fsIssued.term(vAliceTokenFresh.term()));

		RewriteRule step3 = spec.rule(getNextStepName("Bob"));
		step3.addLHS(fsBob.term(vBobActor.term(), vBobIID.term(), spec.numericTerm(1), vBobPartner.term(), vBobChannel.term(), vBobToken.term()));
		if (cm == ChannelModel.ACM) {
			step3.addLHS(acmRcvd.term(vBobActor.term(), vBobPartner.term(), vBobTokenMatched.term(), vBobChannel.term()));
		}
		else {
			step3.addLHS(IASLanSpec.IKNOWS.term(vBobTokenMatched.term()));
		}
		step3.addRHS(fsBob.term(vBobActor.term(), vBobIID.term(), spec.numericTerm(3), vBobPartner.term(), vBobChannel.term(), vBobTokenMatched.term()));
		step3.addLHS(fDishonest.term(vBobActor.term()).negate());
		step3.addRHS(fsRevealed.term(vBobTokenMatched.term()));

		AttackState as = spec.attackState("never_revealed");
		as.addTerm(fsIssued.term(vX.term()));
		as.addTerm(fsRevealed.term(vX.term()));
        as.addTerm(fsEnv.term(vEnvActor.term(), vEnvIID.term(), vEnvSL.term()));

		return spec;
	}
}
