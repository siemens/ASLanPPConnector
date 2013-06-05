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
import org.avantssar.aslan.FunctionTerm;
import org.avantssar.aslan.IASLanSpec;
import org.avantssar.aslan.InitialState;
import org.avantssar.aslan.RewriteRule;
import org.avantssar.aslan.Variable;
import org.avantssar.aslanpp.model.ASLanPPSpecification;
import org.avantssar.aslanpp.model.BlockStatement;
import org.avantssar.aslanpp.model.ConstantSymbol;
import org.avantssar.aslanpp.model.Entity;
import org.avantssar.aslanpp.model.IType;
import org.avantssar.aslanpp.model.Prelude;
import org.avantssar.aslanpp.model.VariableSymbol;
import org.avantssar.commons.ChannelModel;
import org.avantssar.commons.TranslatorOptions;
import org.avantssar.commons.ChannelEntry.Type;
import avantssar.aslan.backends.Verdict;
import avantssar.aslanpp.testing.Specification;

@Specification
public class SecrecyGoal extends AbstractChannelledSpecProvider {

	ConstantSymbol alice;
	ConstantSymbol bob;
	Entity entAlice;
	BlockStatement aliceBody;
	VariableSymbol vAliceBob;
	VariableSymbol vAliceM;
	Entity entBob;
	BlockStatement bobBody;
	VariableSymbol vBobAlice;
	VariableSymbol vBobM;

	Variable vEnvIID;
	Function fsAlice;
	Function fsBob;
	Variable vAliceActor;
	Variable vAliceIID;
	Variable vAliceSL;
	Variable vAliceMaslan;
	Variable vAliceMFresh;
	Variable vAliceB;
	Variable vBobActor;
	Variable vBobIID;
	Variable vBobSL;
	Variable vBobMaslan;
	Variable vBobMMatched;
	Variable vBobA;

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

	protected String getPPprotName() {
		return "secret_payload";
	}

	protected String getProtName() {
		return getPPprotName();
	}

	protected String getGoalName() {
		return getProtName();
	}

	protected int getDeltaSteps() {
		return 1;
	}

	@Override
	public ASLanPPSpecification getASLanPPSpecification() {
		ASLanPPSpecification spec = startASLanPPSpec();

		IType tAgent = spec.findType(Prelude.AGENT);
		IType tMessage = spec.findType(Prelude.MESSAGE);

		alice = env.constants(tAgent, "alice");
		bob = env.constants(tAgent, "bob");
		env.group(alice, bob);

		entAlice = env.entity("Alice");
		entAlice.addParameter(Entity.ACTOR_PREFIX, tAgent);
		vAliceBob = entAlice.addParameter("B", tAgent);
		entAlice.group(entAlice.getActorSymbol(), vAliceBob);
		vAliceM = entAlice.addStateVariable("M", tMessage);
		aliceBody = entAlice.body(entAlice.block());

		entBob = env.entity("Bob");
		vBobAlice = entBob.addParameter("A", tAgent);
		entBob.addParameter(Entity.ACTOR_PREFIX, tAgent);
		entBob.group(vBobAlice, entBob.getActorSymbol());
		vBobM = entBob.addStateVariable("M", tMessage);
		bobBody = entBob.body(entBob.block());

		BlockStatement envBody = env.body(env.block());
		envBody.add(env.newInstance(entAlice, alice.term(), bob.term()));
		envBody.add(env.newInstance(entBob, alice.term(), bob.term()));

		finishPPmodel();
		return spec;
	}

	protected void finishPPmodel() {
		aliceBody.add(entAlice.fresh(vAliceM.term()));
		aliceBody.add(entAlice.comm(entAlice.getActorSymbol().term(), vAliceBob.term(), vAliceM.term(), null, channelType, false, false, false));
		aliceBody.add(entAlice.secrecyGoal(getPPprotName(), vAliceM.term(), entAlice.getActorSymbol().term(), vAliceBob.term()));

		bobBody.add(entBob.comm(vBobAlice.term(), entBob.getActorSymbol().term(), vBobM.matchedTerm(), null, channelType, true, false, false));
		bobBody.add(entBob.secrecyGoal(getPPprotName(), vBobM.term(), entBob.getActorSymbol().term(), vBobAlice.term()));
	}

	@Override
	public IASLanSpec getExpectedASLanTranslation(TranslatorOptions options) {
		IASLanSpec spec = startASLanSpec();

		Constant cDummyMessage1 = spec.constant("dummy_message_1", IASLanSpec.MESSAGE);
		Constant cDummyMessage2 = spec.constant("dummy_message_2", IASLanSpec.MESSAGE);

		Function fsEnv = spec.function(getStateFunctionName("Environment"), IASLanSpec.FACT, IASLanSpec.AGENT, IASLanSpec.NAT, IASLanSpec.NAT);
		fsAlice = spec.function(getStateFunctionName("Alice"), IASLanSpec.FACT, IASLanSpec.AGENT, IASLanSpec.NAT, IASLanSpec.NAT, IASLanSpec.AGENT, IASLanSpec.MESSAGE);
		fsBob = spec.function(getStateFunctionName("Bob"), IASLanSpec.FACT, IASLanSpec.AGENT, IASLanSpec.NAT, IASLanSpec.NAT, IASLanSpec.AGENT, IASLanSpec.MESSAGE);

		Variable vEnvActor = spec.variable("Actor", IASLanSpec.AGENT);
		vEnvIID = spec.variable("IID", IASLanSpec.NAT);
	//	Variable vEnvSL = spec.variable("SL", IASLanSpec.NAT);
		Variable vEnvIID_1 = spec.variable("IID_1", IASLanSpec.NAT);
		Variable vEnvIID_2 = spec.variable("IID_2", IASLanSpec.NAT);
		vAliceActor = spec.variable("E_A_Actor", IASLanSpec.AGENT);
		vAliceIID = spec.variable("E_A_IID", IASLanSpec.NAT);
		vAliceSL = spec.variable("E_A_SL", IASLanSpec.NAT);
		vAliceMaslan = spec.variable("M", IASLanSpec.MESSAGE);
		vAliceMFresh = spec.variable("M_1", IASLanSpec.MESSAGE);
		vAliceB = spec.variable("B", IASLanSpec.AGENT);
		vBobActor = spec.variable("E_B_Actor", IASLanSpec.AGENT);
		vBobIID = spec.variable("E_B_IID", IASLanSpec.NAT);
		vBobSL = spec.variable("E_B_SL", IASLanSpec.NAT);
		vBobMaslan = spec.variable("E_B_M", IASLanSpec.MESSAGE);
		vBobMMatched = spec.variable("E_B_M_1", IASLanSpec.MESSAGE);
		vBobA = spec.variable("A", IASLanSpec.AGENT);

		Constant cAlice = spec.constant("alice", IASLanSpec.AGENT);
		Constant cBob = spec.constant("bob", IASLanSpec.AGENT);

		InitialState init = spec.initialState("init");
		prefillInitialState(spec, init);
		// init.addFact(fIsAgent.term(cAlice.term()));
		// init.addFact(fIsAgent.term(cBob.term()));
		init.addFact(fsEnv.term(cRoot.term(), spec.numericTerm(0), spec.numericTerm(1)));
		init.addFact(IASLanSpec.IKNOWS.term(cAlice.term()));
		init.addFact(IASLanSpec.IKNOWS.term(cBob.term()));
		// init.addFact(IASLanSpec.IKNOWS.term(cDummyMessage1.term()));
		// init.addFact(IASLanSpec.IKNOWS.term(cDummyMessage2.term()));

		RewriteRule step1 = spec.rule(getNextStepName("Environment"));
		step1.addLHS(fsEnv.term(vEnvActor.term(), vEnvIID.term(), spec.numericTerm(1)));
		step1.addRHS(fsEnv.term(vEnvActor.term(), vEnvIID.term(), spec.numericTerm(3)));
		step1.addRHS(fChild.term(vEnvIID.term(), vEnvIID_1.term()));
		step1.addRHS(fsAlice.term(cAlice.term(), vEnvIID_1.term(), spec.numericTerm(1), cBob.term(), cDummyMessage1.term()));
		step1.addRHS(fChild.term(vEnvIID.term(), vEnvIID_2.term()));
		step1.addRHS(fsBob.term(cBob.term(), vEnvIID_2.term(), spec.numericTerm(1), cAlice.term(), cDummyMessage2.term()));
		step1.addExists(vEnvIID_1);
		step1.addExists(vEnvIID_2);

		finishModel(spec, cm);

		addGoals(spec);

		return spec;
	}

	protected void finishModel(IASLanSpec spec, ChannelModel cm) {
		Function fAliceSet = spec.function(getProtName() + "_set", spec.setType(IASLanSpec.AGENT), IASLanSpec.NAT);
		Function fBobSet = spec.function(getProtName() + "_set", spec.setType(IASLanSpec.AGENT), IASLanSpec.NAT);
		Constant cProt = spec.constant(getProtName(), IASLanSpec.PROTOCOL_ID);

		Function fChild = spec.findFunction(Prelude.CHILD);

		RewriteRule step2 = spec.rule(getNextStepName("Alice"));
		step2.addLHS(fsAlice.term(vAliceActor.term(), vAliceIID.term(), spec.numericTerm(1), vAliceB.term(), vAliceMaslan.term()));
		step2.addRHS(fsAlice.term(vAliceActor.term(), vAliceIID.term(), spec.numericTerm(3 + getDeltaSteps()), vAliceB.term(), vAliceMFresh.term()));
		step2.addRHS(doSend(cm, vAliceActor.term(), vAliceActor.term(), vAliceB.term(), vAliceMFresh.term()));
		step2.addRHS(IASLanSpec.SECRET.term(vAliceMFresh.term(), cProt.term(), fAliceSet.term(vEnvIID.term())));
		step2.addRHS(IASLanSpec.CONTAINS.term(vAliceActor.term(), fAliceSet.term(vEnvIID.term())));
		step2.addRHS(IASLanSpec.CONTAINS.term(vAliceB.term(), fAliceSet.term(vEnvIID.term())));
		step2.addLHS(fChild.term(vEnvIID.term(), vAliceIID.term()));
		step2.addRHS(fChild.term(vEnvIID.term(), vAliceIID.term()));
		step2.addExists(vAliceMFresh);
		step2.addLHS(fDishonest.term(vAliceActor.term()).negate());

		RewriteRule step3 = spec.rule(getNextStepName("Bob"));
		step3.addLHS(fsBob.term(vBobActor.term(), vBobIID.term(), spec.numericTerm(1), vBobA.term(), vBobMaslan.term()));
		step3.addLHS(doReceive(cm, vBobActor.term(), vBobA.term(), vBobA.term(), vBobMMatched.term()));
		step3.addRHS(fsBob.term(vBobActor.term(), vBobIID.term(), spec.numericTerm(2 + getDeltaSteps()), vBobA.term(), vBobMMatched.term()));
		step3.addRHS(IASLanSpec.SECRET.term(vBobMMatched.term(), cProt.term(), fBobSet.term(vEnvIID.term())));
		step3.addRHS(IASLanSpec.CONTAINS.term(vBobActor.term(), fBobSet.term(vEnvIID.term())));
		step3.addRHS(IASLanSpec.CONTAINS.term(vBobA.term(), fBobSet.term(vEnvIID.term())));
		step3.addLHS(fChild.term(vEnvIID.term(), vBobIID.term()));
		step3.addRHS(fChild.term(vEnvIID.term(), vBobIID.term()));
		step3.addLHS(fDishonest.term(vBobActor.term()).negate());
		decorateReceive(step3, fBobSet.term(vEnvIID.term()));
	}

	protected void decorateReceive(RewriteRule rr, FunctionTerm setTerm) {

	}

	protected void addGoals(IASLanSpec spec) {
		addSecrecyGoal(spec);
	}

	protected void addSecrecyGoal(IASLanSpec spec) {
		Variable vMsg = spec.variable("Msg", IASLanSpec.MESSAGE);
		Variable vKnowers = spec.variable("Knowers", spec.setType(IASLanSpec.AGENT));
		Constant cProt = spec.findConstant(getProtName());
		AttackState as = spec.attackState(getGoalName());
		as.addTerm(IASLanSpec.SECRET.term(vMsg.term(), cProt.term(), vKnowers.term()));
		as.addTerm(IASLanSpec.IKNOWS.term(vMsg.term()));
		as.addTerm(IASLanSpec.CONTAINS.term(IASLanSpec.INTRUDER.term(), vKnowers.term()).negate());
	}
}
