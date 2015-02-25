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
import org.avantssar.aslanpp.model.Prelude;
import org.avantssar.aslanpp.model.VariableSymbol;
import org.avantssar.commons.TranslatorOptions;
import avantssar.aslanpp.testing.Specification;

@Specification
public class HashFunction extends AbstractSpecProvider {

	@Override
	public ASLanPPSpecification getASLanPPSpecification() {
		ASLanPPSpecification spec = startASLanPPSpec();

		ConstantSymbol token = env.constants(env.findType(Prelude.MESSAGE), "token");
		token.setNonPublic(true);
		FunctionSymbol tagged = env.addFunction("tagged", env.findType(Prelude.FACT), env.findType(Prelude.MESSAGE));

		BlockStatement envBody = env.body(env.block());
		envBody.add(env.introduce(tagged.term(env.findFunction(Prelude.HASH).term(token.term()))));

		Goal g = env.goal("no_tagging");
		VariableSymbol e1 = g.addUntypedVariable("E");
		g.setFormula(tagged.term(e1.term()).expression().exists(e1).not());
		return spec;
	}

	@Override
	public IASLanSpec getExpectedASLanTranslation(TranslatorOptions options) {
		IASLanSpec spec = startASLanSpec();

		Function fsEnv = spec.function(getStateFunctionName("Environment"), IASLanSpec.FACT, IASLanSpec.AGENT, IASLanSpec.NAT, IASLanSpec.NAT);
		Function fTagged = spec.function("tagged", IASLanSpec.FACT, IASLanSpec.MESSAGE);

		Variable vEnvActor = spec.variable("Actor", IASLanSpec.AGENT);
		Variable vEnvIID = spec.variable("IID", IASLanSpec.NAT);
		Variable vEnvSL = spec.variable("SL", IASLanSpec.NAT);
		Variable vGoalE = spec.variable("E", IASLanSpec.MESSAGE);
		Constant cPeter = spec.constant("token", IASLanSpec.MESSAGE);

		InitialState init = spec.initialState("init");
		prefillInitialState(spec, init);
		init.addFact(fsEnv.term(cRoot.term(), spec.numericTerm(0), spec.numericTerm(1)));

		RewriteRule step1 = spec.rule(getNextStepName("Environment"));
		step1.addLHS(fsEnv.term(vEnvActor.term(), vEnvIID.term(), spec.numericTerm(1)));
		step1.addRHS(fsEnv.term(vEnvActor.term(), vEnvIID.term(), spec.numericTerm(2)));
		step1.addRHS(fTagged.term(IASLanSpec.PK.term(cPeter.term())));

		AttackState as = spec.attackState("no_tagging");
		as.addTerm(fTagged.term(vGoalE.term()));
        as.addTerm(fsEnv.term(vEnvActor.term(), vEnvIID.term(), vEnvSL.term()));

		return spec;
	}

}
