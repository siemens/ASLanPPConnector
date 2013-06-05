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
import org.avantssar.aslanpp.model.FunctionSymbol;
import org.avantssar.aslanpp.model.Goal;
import org.avantssar.aslanpp.model.IType;
import org.avantssar.aslanpp.model.LTLExpression;
import org.avantssar.aslanpp.model.Prelude;
import org.avantssar.aslanpp.model.VariableSymbol;
import org.avantssar.commons.TranslatorOptions;
import avantssar.aslanpp.testing.Specification;

@Specification
public class TransmissionFunctionalNoActor extends AbstractChannelledSpecProvider {

	@Override
	public ASLanPPSpecification getASLanPPSpecification() {
		ASLanPPSpecification spec = startASLanPPSpec();

		IType tAgent = spec.findType(Prelude.AGENT);
		IType tMessage = spec.findType(Prelude.MESSAGE);

		ConstantSymbol alice = env.constants(tAgent, "alice");
		ConstantSymbol bob = env.constants(tAgent, "bob");
		ConstantSymbol token = env.constants(tMessage, "token");
		FunctionSymbol tagged = env.addFunction("tagged", spec.findType(Prelude.FACT), tMessage);
		VariableSymbol vM = env.addStateVariable("M", tMessage);

		token.setNonPublic(true);
		env.group(alice, bob);

		BlockStatement envBody = env.body(env.block());
		envBody.add(env.comm(alice.term(), bob.term(), token.term(), null, channelType, false, isRenderAsFunction(), isRenderOOPStyle()));
		envBody.add(env.comm(alice.term(), bob.term(), vM.matchedTerm(), null, channelType, true, isRenderAsFunction(), isRenderOOPStyle()));
		envBody.add(env.introduce(tagged.term(vM.term())));

		Goal g = env.goal("not_tagged");
		LTLExpression ltl = new LTLExpression(LTLExpression.GLOBALLY, tagged.term(token.term()).expression().not());
		g.setFormula(ltl);

		return spec;
	}

	protected boolean isRenderAsFunction() {
		return true;
	}

	protected boolean isRenderOOPStyle() {
		return false;
	}

	@Override
	public IASLanSpec getExpectedASLanTranslation(TranslatorOptions options) {
		IASLanSpec spec = startASLanSpec();

		Constant cDummyMessage = spec.constant("dummy_message", IASLanSpec.MESSAGE);

		Function fsEnv = spec.function(getStateFunctionName("Environment"), IASLanSpec.FACT, IASLanSpec.AGENT, IASLanSpec.NAT, IASLanSpec.NAT, IASLanSpec.MESSAGE);
		Function fTagged = spec.function("tagged", IASLanSpec.FACT, IASLanSpec.MESSAGE);

		Variable vEnvActor = spec.variable("Actor", IASLanSpec.AGENT);
		Variable vEnvIID = spec.variable("IID", IASLanSpec.NAT);
		Variable vEnvSL = spec.variable("SL", IASLanSpec.NAT);
		Variable vM = spec.variable("M", IASLanSpec.MESSAGE);
		Variable vMMatched = spec.variable("M_1", IASLanSpec.MESSAGE);

		Constant cAlice = spec.constant("alice", IASLanSpec.AGENT);
		Constant cBob = spec.constant("bob", IASLanSpec.AGENT);
		Constant cToken = spec.constant("token", IASLanSpec.MESSAGE);

		InitialState init = spec.initialState("init");
		prefillInitialState(spec, init);
		init.addFact(fsEnv.term(cRoot.term(), spec.numericTerm(0), spec.numericTerm(1), cDummyMessage.term()));
		init.addFact(IASLanSpec.IKNOWS.term(cAlice.term()));
		init.addFact(IASLanSpec.IKNOWS.term(cBob.term()));

		RewriteRule step1 = spec.rule(getNextStepName("Environment"));
		step1.addLHS(fsEnv.term(vEnvActor.term(), vEnvIID.term(), spec.numericTerm(1), vM.term()));
		step1.addRHS(fsEnv.term(vEnvActor.term(), vEnvIID.term(), spec.numericTerm(2), vM.term()));
		step1.addRHS(doSend(cm, cAlice.term(), cAlice.term(), cBob.term(), cToken.term()));

		RewriteRule step2 = spec.rule(getNextStepName("Environment"));
		step2.addLHS(fsEnv.term(vEnvActor.term(), vEnvIID.term(), spec.numericTerm(2), vM.term()));
		step2.addRHS(fsEnv.term(vEnvActor.term(), vEnvIID.term(), spec.numericTerm(4), vMMatched.term()));
		step2.addLHS(doReceive(cm, cBob.term(), cAlice.term(), cAlice.term(), vMMatched.term()));
		step2.addRHS(fTagged.term(vMMatched.term()));

		AttackState as = spec.attackState("not_tagged");
		as.addTerm(fTagged.term(cToken.term()));
        as.addTerm(fsEnv.term(vEnvActor.term(), vEnvIID.term(), vEnvSL.term(), vM.term()));

		return spec;
	}

}
