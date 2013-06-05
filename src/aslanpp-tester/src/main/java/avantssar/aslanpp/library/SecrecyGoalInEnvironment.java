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
import org.avantssar.aslanpp.model.IType;
import org.avantssar.aslanpp.model.Prelude;
import org.avantssar.aslanpp.model.VariableSymbol;
import org.avantssar.commons.TranslatorOptions;
import avantssar.aslanpp.testing.Specification;

@Specification
public class SecrecyGoalInEnvironment extends AbstractSpecProvider {

	@Override
	public ASLanPPSpecification getASLanPPSpecification() {
		ASLanPPSpecification spec = startASLanPPSpec();

		IType tMessage = spec.findType(Prelude.MESSAGE);

		VariableSymbol vToken = env.addStateVariable("Token", tMessage);

		BlockStatement envBody = env.body(env.block());
		envBody.add(env.fresh(vToken.term()));
		envBody.add(env.introduce(env.findFunction(Prelude.IKNOWS).term(vToken.term())));
		envBody.add(env.secrecyGoal(getPPprotName(), vToken.term(), spec.findConstant(Prelude.ROOT).term()));

		return spec;
	}

	@Override
	public IASLanSpec getExpectedASLanTranslation(TranslatorOptions options) {
		IASLanSpec spec = startASLanSpec();

		Constant cDummyMessage = spec.constant("dummy_message", IASLanSpec.MESSAGE);
		Constant cProt = spec.constant(getProtName(), IASLanSpec.PROTOCOL_ID);

		Function fsEnv = spec.function(getStateFunctionName("Environment"), IASLanSpec.FACT, IASLanSpec.AGENT, IASLanSpec.NAT, IASLanSpec.NAT, IASLanSpec.MESSAGE);

		Variable vEnvActor = spec.variable("Actor", IASLanSpec.AGENT);
		Variable vEnvIID = spec.variable("IID", IASLanSpec.NAT);
	//	Variable vEnvSL = spec.variable("SL", IASLanSpec.NAT);
		Variable vEnvToken = spec.variable("Token", IASLanSpec.MESSAGE);
		Variable vEnvTokenFresh = spec.variable("Token_1", IASLanSpec.MESSAGE);
		Function fEnvSet = spec.function(getProtName() + "_set", spec.setType(IASLanSpec.AGENT), IASLanSpec.NAT);

		InitialState init = spec.initialState("init");
		prefillInitialState(spec, init);
		init.addFact(fsEnv.term(cRoot.term(), spec.numericTerm(0), spec.numericTerm(1), cDummyMessage.term()));

		RewriteRule step1 = spec.rule(getNextStepName("Environment"));
		step1.addLHS(fsEnv.term(vEnvActor.term(), vEnvIID.term(), spec.numericTerm(1), vEnvToken.term()));
		step1.addRHS(fsEnv.term(vEnvActor.term(), vEnvIID.term(), spec.numericTerm(4), vEnvTokenFresh.term()));
		step1.addRHS(IASLanSpec.IKNOWS.term(vEnvTokenFresh.term()));
		step1.addRHS(IASLanSpec.SECRET.term(vEnvTokenFresh.term(), cProt.term(), fEnvSet.term(cDummyNat.term())));
		step1.addRHS(IASLanSpec.CONTAINS.term(cRoot.term(), fEnvSet.term(cDummyNat.term())));
		step1.addLHS(fChild.term(cDummyNat.term(), vEnvIID.term()));
		step1.addRHS(fChild.term(cDummyNat.term(), vEnvIID.term()));
		step1.addExists(vEnvTokenFresh);

		Variable vMsg = spec.variable("Msg", IASLanSpec.MESSAGE);
		Variable vKnowers = spec.variable("Knowers", spec.setType(IASLanSpec.AGENT));
		AttackState as = spec.attackState(getGoalName());
		as.addTerm(IASLanSpec.SECRET.term(vMsg.term(), cProt.term(), vKnowers.term()));
		as.addTerm(IASLanSpec.IKNOWS.term(vMsg.term()));
		as.addTerm(IASLanSpec.CONTAINS.term(IASLanSpec.INTRUDER.term(), vKnowers.term()).negate());
		return spec;
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

}
