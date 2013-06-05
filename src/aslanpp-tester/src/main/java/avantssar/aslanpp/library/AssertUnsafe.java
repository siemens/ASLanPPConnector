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
import org.avantssar.aslanpp.model.AssertStatement;
import org.avantssar.aslanpp.model.BlockStatement;
import org.avantssar.aslanpp.model.ConstantSymbol;
import org.avantssar.aslanpp.model.Entity;
import org.avantssar.aslanpp.model.Prelude;
import org.avantssar.aslanpp.model.VariableSymbol;
import org.avantssar.commons.TranslatorOptions;
import avantssar.aslanpp.testing.Specification;

@Specification
public class AssertUnsafe extends AbstractChannelledSpecProvider {

	protected ConstantSymbol cToken1;
	protected ConstantSymbol cToken2;

	@Override
	public ASLanPPSpecification getASLanPPSpecification() {
		ASLanPPSpecification spec = startASLanPPSpec();

		ConstantSymbol cAlice = env.constants(env.findType(Prelude.AGENT), "alice");
		cAlice.setNonPublic(areConstantsNonPublic());
		ConstantSymbol cBob = env.constants(env.findType(Prelude.AGENT), "bob");
		cBob.setNonPublic(areConstantsNonPublic());
		env.group(cAlice, cBob);
		cToken1 = env.constants(env.findType(Prelude.MESSAGE), "token1");
		cToken1.setNonPublic(areConstantsNonPublic());
		cToken2 = env.constants(env.findType(Prelude.MESSAGE), "token2");
		cToken2.setNonPublic(areConstantsNonPublic());
		env.group(cToken1, cToken2);

		Entity alice = env.entity("Alice");
		alice.addParameter(Entity.ACTOR_PREFIX, env.findType(Prelude.AGENT));
		VariableSymbol vAliceB = alice.addParameter("B", env.findType(Prelude.AGENT));
		alice.group(alice.getActorSymbol(), vAliceB);
		alice.body(alice.block()).add(alice.comm(alice.getActorSymbol().term(), vAliceB.term(), cToken1.term(), null, channelType, false, false, false));

		Entity bob = env.entity("Bob");
		VariableSymbol vBobA = bob.addParameter("A", env.findType(Prelude.AGENT));
		bob.addParameter(Entity.ACTOR_PREFIX, env.findType(Prelude.AGENT));
		bob.group(vBobA, bob.getActorSymbol());
		VariableSymbol vBobM = bob.addStateVariable("M", env.findType(Prelude.MESSAGE));
		BlockStatement bobBody = bob.body(bob.block());
		bobBody.add(bob.comm(vBobA.term(), bob.getActorSymbol().term(), vBobM.matchedTerm(), null, channelType, true, false, false));
		AssertStatement bobRM = bobBody.add(bob.assertion("Right_Message"));
		bobRM.setGuard(vBobM.term().equality(getPPtoken().term()));

		BlockStatement envBody = env.body(env.block());
		envBody.add(env.newInstance(alice, cAlice.term(), cBob.term()));
		envBody.add(env.newInstance(bob, cAlice.term(), cBob.term()));

		return spec;
	}

	protected ConstantSymbol getPPtoken() {
		return cToken2;
	}

	protected boolean areConstantsNonPublic() {
		return false;
	}

	@Override
	public IASLanSpec getExpectedASLanTranslation(TranslatorOptions options) {
		IASLanSpec spec = startASLanSpec();

		Constant cDummyMessage = spec.constant("dummy_message_1", IASLanSpec.MESSAGE);

		Function fsEnv = spec.function(getStateFunctionName("Environment"), IASLanSpec.FACT, IASLanSpec.AGENT, IASLanSpec.NAT, IASLanSpec.NAT);
		Function fsAlice = spec.function(getStateFunctionName("Alice"), IASLanSpec.FACT, IASLanSpec.AGENT, IASLanSpec.NAT, IASLanSpec.NAT, IASLanSpec.AGENT);
		Function fsBob = spec.function(getStateFunctionName("Bob"), IASLanSpec.FACT, IASLanSpec.AGENT, IASLanSpec.NAT, IASLanSpec.NAT, IASLanSpec.AGENT, IASLanSpec.MESSAGE);
		Constant cAlice = spec.constant("alice", IASLanSpec.AGENT);
		Constant cBob = spec.constant("bob", IASLanSpec.AGENT);
		Constant cToken1 = spec.constant("token1", IASLanSpec.MESSAGE);
		Constant cToken2 = spec.constant("token2", IASLanSpec.MESSAGE);
		Variable vEnvActor = spec.variable("Actor", IASLanSpec.AGENT);
		Variable vEnvIID = spec.variable("IID", IASLanSpec.NAT);
	//  Variable vEnvSL = spec.variable("SL", IASLanSpec.NAT);
		Variable vEnvIID_1 = spec.variable("IID_1", IASLanSpec.NAT);
		Variable vEnvIID_2 = spec.variable("IID_2", IASLanSpec.NAT);
		Variable vAliceActor = spec.variable("E_A_Actor", IASLanSpec.AGENT);
		Variable vAliceIID = spec.variable("E_A_IID", IASLanSpec.NAT);
	//	Variable vAliceSL = spec.variable("E_A_SL", IASLanSpec.NAT);
		Variable vAliceB = spec.variable("B", IASLanSpec.AGENT);
		Variable vBobActor = spec.variable("E_B_Actor", IASLanSpec.AGENT);
		Variable vBobIID = spec.variable("E_B_IID", IASLanSpec.NAT);
		Variable vBobSL = spec.variable("E_B_SL", IASLanSpec.NAT);
		Variable vBobA = spec.variable("A", IASLanSpec.AGENT);
		Variable vBobM = spec.variable("M", IASLanSpec.MESSAGE);
		Variable vBobMMatched = spec.variable("M_1", IASLanSpec.MESSAGE);
		Function fAssertCheck = spec.function("check_Right_Message", IASLanSpec.FACT, IASLanSpec.MESSAGE, IASLanSpec.NAT, IASLanSpec.NAT);

		InitialState init = spec.initialState("init");
		prefillInitialState(spec, init);
		// init.addFact(fIsAgent.term(cAlice.term()));
		// init.addFact(fIsAgent.term(cBob.term()));
		init.addFact(fsEnv.term(cRoot.term(), spec.numericTerm(0), spec.numericTerm(1)));
		// init.addFact(IASLanSpec.IKNOWS.term(cDummyMessage.term()));
		if (!areConstantsNonPublic()) {
			init.addFact(IASLanSpec.IKNOWS.term(cAlice.term()));
			init.addFact(IASLanSpec.IKNOWS.term(cBob.term()));
			init.addFact(IASLanSpec.IKNOWS.term(cToken1.term()));
			init.addFact(IASLanSpec.IKNOWS.term(cToken2.term()));
		}

		RewriteRule step1 = spec.rule(getNextStepName("Environment"));
		step1.addLHS(fsEnv.term(vEnvActor.term(), vEnvIID.term(), spec.numericTerm(1)));
		step1.addRHS(fsEnv.term(vEnvActor.term(), vEnvIID.term(), spec.numericTerm(3)));
		step1.addRHS(fChild.term(vEnvIID.term(), vEnvIID_1.term()));
		step1.addRHS(fChild.term(vEnvIID.term(), vEnvIID_2.term()));
		step1.addRHS(fsAlice.term(cAlice.term(), vEnvIID_1.term(), spec.numericTerm(1), cBob.term()));
		step1.addRHS(fsBob.term(cBob.term(), vEnvIID_2.term(), spec.numericTerm(1), cAlice.term(), cDummyMessage.term()));
		step1.addExists(vEnvIID_1);
		step1.addExists(vEnvIID_2);

		RewriteRule step2 = spec.rule(getNextStepName("Alice"));
		step2.addLHS(fsAlice.term(vAliceActor.term(), vAliceIID.term(), spec.numericTerm(1), vAliceB.term()));
		step2.addRHS(fsAlice.term(vAliceActor.term(), vAliceIID.term(), spec.numericTerm(2), vAliceB.term()));
		step2.addRHS(doSend(cm, vAliceActor.term(), vAliceActor.term(), vAliceB.term(), cToken1.term()));
		step2.addLHS(fDishonest.term(vAliceActor.term()).negate());

		RewriteRule step3 = spec.rule(getNextStepName("Bob"));
		step3.addLHS(fsBob.term(vBobActor.term(), vBobIID.term(), spec.numericTerm(1), vBobA.term(), vBobM.term()));
		step3.addLHS(doReceive(cm, vBobActor.term(), vBobA.term(), vBobA.term(), vBobMMatched.term()));
		step3.addRHS(fsBob.term(vBobActor.term(), vBobIID.term(), spec.numericTerm(3), vBobA.term(), vBobMMatched.term()));
		step3.addRHS(fAssertCheck.term(vBobMMatched.term(), vBobIID.term(), spec.numericTerm(3)));
		step3.addLHS(fDishonest.term(vBobActor.term()).negate());

		RewriteRule step4 = spec.rule(getNextStepName("Bob"));
		step4.addLHS(fsBob.term(vBobActor.term(), vBobIID.term(), spec.numericTerm(3), vBobA.term(), vBobM.term()));
		step4.addLHS(fAssertCheck.term(vBobM.term(), vBobIID.term(), spec.numericTerm(3)));
		step4.addRHS(fsBob.term(vBobActor.term(), vBobIID.term(), spec.numericTerm(4), vBobA.term(), vBobM.term()));

		AttackState as = spec.attackState("right_Message");
		as.addTerm(fAssertCheck.term(vBobM.term(), vBobIID.term(), vBobSL.term()));
		as.addTerm(IASLanSpec.EQUAL.term(vBobM.term(), getToken(spec).term()).negate());

		return spec;
	}

	private Constant getToken(IASLanSpec spec) {
		return spec.findConstant(getPPtoken().getOriginalName());
	}
}
