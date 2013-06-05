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
import org.avantssar.aslanpp.model.IType;
import org.avantssar.aslanpp.model.LTLExpression;
import org.avantssar.aslanpp.model.Prelude;
import org.avantssar.aslanpp.model.VariableSymbol;
import org.avantssar.commons.TranslatorOptions;
import avantssar.aslanpp.testing.Specification;

@Specification
public class TransmissionAnnotated extends AbstractChannelledSpecProvider {

	@Override
	public ASLanPPSpecification getASLanPPSpecification() {
		ASLanPPSpecification spec = startASLanPPSpec();

		IType tAgent = spec.findType(Prelude.AGENT);
		IType tMessage = spec.findType(Prelude.MESSAGE);

		ConstantSymbol alice = env.constants(tAgent, "alice");
		ConstantSymbol bob = env.constants(tAgent, "bob");
		ConstantSymbol token = env.constants(tMessage, "token");
		FunctionSymbol tagged = env.addFunction("tagged", spec.findType(Prelude.FACT), tMessage);
		token.setNonPublic(true);
		env.group(alice, bob);

		Entity entAlice = env.entity("Alice");
		entAlice.addParameter(Entity.ACTOR_PREFIX, tAgent);
		VariableSymbol vAliceBob = entAlice.addParameter("B", tAgent);
		entAlice.group(entAlice.getActorSymbol(), vAliceBob);
		BlockStatement aliceBody = entAlice.body(entAlice.block());
		aliceBody.add(entAlice.comm(entAlice.getActorSymbol().term(), vAliceBob.term(), token.term(), null, channelType, false, isRenderAsFunction(), isRenderOOPStyle()));

		Entity entBob = env.entity("Bob");
		VariableSymbol vBobAlice = entBob.addParameter("A", tAgent);
		entBob.addParameter(Entity.ACTOR_PREFIX, tAgent);
		entBob.group(vBobAlice, entBob.getActorSymbol());
		VariableSymbol vBobM = entBob.addStateVariable("M", tMessage);
		BlockStatement bobBody = entBob.body(entBob.block());
		bobBody.add(entBob.comm(vBobAlice.term(), entBob.getActorSymbol().term(), vBobM.matchedTerm(), null, channelType, true, isRenderAsFunction(), isRenderOOPStyle()));
		bobBody.add(entBob.introduce(tagged.term(vBobM.term())));

		BlockStatement envBody = env.body(env.block());
		envBody.add(env.newInstance(entAlice, alice.term(), bob.term()));
		envBody.add(env.newInstance(entBob, alice.term(), bob.term()));

		Goal g = env.goal("not_tagged");
		LTLExpression ltl = new LTLExpression(LTLExpression.GLOBALLY, tagged.term(token.term()).expression().not());
		g.setFormula(ltl);

		return spec;
	}

	protected boolean isRenderAsFunction() {
		return false;
	}

	protected boolean isRenderOOPStyle() {
		return false;
	}

	@Override
	public IASLanSpec getExpectedASLanTranslation(TranslatorOptions options) {
		IASLanSpec spec = startASLanSpec();

		Constant cDummyMessage = spec.constant("dummy_message_1", IASLanSpec.MESSAGE);

		Function fsEnv = spec.function(getStateFunctionName("Environment"), IASLanSpec.FACT, IASLanSpec.AGENT, IASLanSpec.NAT, IASLanSpec.NAT);
		Function fsAlice = spec.function(getStateFunctionName("Alice"), IASLanSpec.FACT, IASLanSpec.AGENT, IASLanSpec.NAT, IASLanSpec.NAT, IASLanSpec.AGENT);
		Function fsBob = spec.function(getStateFunctionName("Bob"), IASLanSpec.FACT, IASLanSpec.AGENT, IASLanSpec.NAT, IASLanSpec.NAT, IASLanSpec.AGENT, IASLanSpec.MESSAGE);
		Function fTagged = spec.function("tagged", IASLanSpec.FACT, IASLanSpec.MESSAGE);

		Variable vEnvActor = spec.variable("Actor", IASLanSpec.AGENT);
		Variable vEnvIID = spec.variable("IID", IASLanSpec.NAT);
		Variable vEnvSL = spec.variable("SL", IASLanSpec.NAT);
		Variable vEnvIID_1 = spec.variable("IID_1", IASLanSpec.NAT);
		Variable vEnvIID_2 = spec.variable("IID_2", IASLanSpec.NAT);
		Variable vAliceActor = spec.variable("E_A_Actor", IASLanSpec.AGENT);
		Variable vAliceIID = spec.variable("E_A_IID", IASLanSpec.NAT);
	//	Variable vAliceSL = spec.variable("E_A_SL", IASLanSpec.NAT);
		Variable vAliceB = spec.variable("B", IASLanSpec.AGENT);
		Variable vBobActor = spec.variable("E_B_Actor", IASLanSpec.AGENT);
		Variable vBobIID = spec.variable("E_B_IID", IASLanSpec.NAT);
	//	Variable vBobSL = spec.variable("E_B_SL", IASLanSpec.NAT);
		Variable vBobMaslan = spec.variable("M", IASLanSpec.MESSAGE);
		Variable vBobMMatched = spec.variable("M_1", IASLanSpec.MESSAGE);
		Variable vBobA = spec.variable("A", IASLanSpec.AGENT);

		Constant cAlice = spec.constant("alice", IASLanSpec.AGENT);
		Constant cBob = spec.constant("bob", IASLanSpec.AGENT);
		Constant cToken = spec.constant("token", IASLanSpec.MESSAGE);

		InitialState init = spec.initialState("init");
		prefillInitialState(spec, init);
		init.addFact(fsEnv.term(cRoot.term(), spec.numericTerm(0), spec.numericTerm(1)));
		init.addFact(IASLanSpec.IKNOWS.term(cAlice.term()));
		init.addFact(IASLanSpec.IKNOWS.term(cBob.term()));
		// init.addFact(IASLanSpec.IKNOWS.term(cDummyMessage.term()));

		RewriteRule step1 = spec.rule(getNextStepName("Environment"));
		step1.addLHS(fsEnv.term(vEnvActor.term(), vEnvIID.term(), spec.numericTerm(1)));
		step1.addRHS(fsEnv.term(vEnvActor.term(), vEnvIID.term(), spec.numericTerm(3)));
		step1.addRHS(fChild.term(vEnvIID.term(), vEnvIID_1.term()));
		step1.addRHS(fsAlice.term(cAlice.term(), vEnvIID_1.term(), spec.numericTerm(1), cBob.term()));
		step1.addRHS(fChild.term(vEnvIID.term(), vEnvIID_2.term()));
		step1.addRHS(fsBob.term(cBob.term(), vEnvIID_2.term(), spec.numericTerm(1), cAlice.term(), cDummyMessage.term()));
		step1.addExists(vEnvIID_1);
		step1.addExists(vEnvIID_2);

		RewriteRule step2 = spec.rule(getNextStepName("Alice"));
		step2.addLHS(fsAlice.term(vAliceActor.term(), vAliceIID.term(), spec.numericTerm(1), vAliceB.term()));
		step2.addRHS(fsAlice.term(vAliceActor.term(), vAliceIID.term(), spec.numericTerm(2), vAliceB.term()));
		step2.addRHS(doSend(cm, vAliceActor.term(), vAliceActor.term(), vAliceB.term(), cToken.term()));
		step2.addLHS(fDishonest.term(vAliceActor.term()).negate());

		RewriteRule step3 = spec.rule(getNextStepName("Bob"));
		step3.addLHS(fsBob.term(vBobActor.term(), vBobIID.term(), spec.numericTerm(1), vBobA.term(), vBobMaslan.term()));
		step3.addLHS(doReceive(cm, vBobActor.term(), vBobA.term(), vBobA.term(), vBobMMatched.term()));
		step3.addRHS(fsBob.term(vBobActor.term(), vBobIID.term(), spec.numericTerm(3), vBobA.term(), vBobMMatched.term()));
		step3.addRHS(fTagged.term(vBobMMatched.term()));
		step3.addLHS(fDishonest.term(vBobActor.term()).negate());

		AttackState as = spec.attackState("not_tagged");
		as.addTerm(fTagged.term(cToken.term()));
        as.addTerm(fsEnv.term(vEnvActor.term(), vEnvIID.term(), vEnvSL.term()));

		return spec;
	}

}
