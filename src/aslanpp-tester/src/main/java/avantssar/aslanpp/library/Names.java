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
import org.avantssar.aslanpp.model.IExpression;
import org.avantssar.aslanpp.model.VariableSymbol;
import org.avantssar.commons.TranslatorOptions;
import avantssar.aslanpp.testing.Specification;

@Specification
public class Names extends AbstractSpecProvider {

	@Override
	public ASLanPPSpecification getASLanPPSpecification() {
		ASLanPPSpecification spec = startASLanPPSpec();

		VariableSymbol vA = env.addStateVariable("A", tppMessage);
		VariableSymbol vAone = env.addStateVariable("A1", tppMessage);
		VariableSymbol vAu = env.addStateVariable("A_", tppMessage);
		VariableSymbol vAprime = env.addStateVariable("A'", tppMessage);
		ConstantSymbol cTokenPrime = env.constants(tppMessage, "token'");
		cTokenPrime.setNonPublic(true);
		FunctionSymbol fOnePrime = env.addFunction("f1'", tppFact, tppMessage);
		FunctionSymbol fTwoPrime = env.addFunction("f2'", tppFact, tppMessage);
		FunctionSymbol fThreePrime = env.addFunction("f3'", tppFact, tppMessage);
		FunctionSymbol fFourPrime = env.addFunction("f4'", tppFact, tppMessage);

		BlockStatement envBody = env.body(env.block());
		envBody.add(env.assign(vA.term(), cTokenPrime.term()));
		envBody.add(env.assign(vAone.term(), cTokenPrime.term()));
		envBody.add(env.assign(vAu.term(), cTokenPrime.term()));
		envBody.add(env.assign(vAprime.term(), cTokenPrime.term()));
		envBody.add(env.introduce(fOnePrime.term(vA.term())));
		envBody.add(env.introduce(fTwoPrime.term(vAone.term())));
		envBody.add(env.introduce(fThreePrime.term(vAu.term())));
		envBody.add(env.introduce(fFourPrime.term(vAprime.term())));

		Goal g = env.goal("Goal_Uppercase_Name'");
		IExpression f = fOnePrime.term(cTokenPrime.term()).expression();
		f = f.and(fTwoPrime.term(cTokenPrime.term()).expression());
		f = f.and(fThreePrime.term(cTokenPrime.term()).expression());
		f = f.and(fFourPrime.term(cTokenPrime.term()).expression());
		g.setFormula(f.not());

		return spec;
	}

	@Override
	public IASLanSpec getExpectedASLanTranslation(TranslatorOptions options) {
		IASLanSpec spec = startASLanSpec();

		Constant cDummyMessage = spec.constant("dummy_message", IASLanSpec.MESSAGE);
		Function fsEnv = spec.function(getStateFunctionName("Environment"), IASLanSpec.FACT, IASLanSpec.AGENT, IASLanSpec.NAT, IASLanSpec.NAT, IASLanSpec.MESSAGE, IASLanSpec.MESSAGE,
				IASLanSpec.MESSAGE, IASLanSpec.MESSAGE);
		Variable vA = spec.variable("A", IASLanSpec.MESSAGE);
		Variable vAone = spec.variable("A1", IASLanSpec.MESSAGE);
		Variable vAu = spec.variable("A_", IASLanSpec.MESSAGE);
		Variable vAprime = spec.variable("E_A_", IASLanSpec.MESSAGE);
		Constant cTokenPrime = spec.constant("token_", IASLanSpec.MESSAGE);
		Function f1prime = spec.function("f1_", IASLanSpec.FACT, IASLanSpec.MESSAGE);
		Function f2prime = spec.function("f2_", IASLanSpec.FACT, IASLanSpec.MESSAGE);
		Function f3prime = spec.function("f3_", IASLanSpec.FACT, IASLanSpec.MESSAGE);
		Function f4prime = spec.function("f4_", IASLanSpec.FACT, IASLanSpec.MESSAGE);
		Variable vActor = spec.variable("Actor", IASLanSpec.AGENT);
		Variable vIID = spec.variable("IID", IASLanSpec.NAT);
		Variable vSL = spec.variable("SL", IASLanSpec.NAT);

		InitialState init = spec.initialState("init");
		prefillInitialState(spec, init);
		init.addFact(fsEnv.term(cRoot.term(), spec.numericTerm(0), spec.numericTerm(1), cDummyMessage.term(), cDummyMessage.term(), cDummyMessage.term(), cDummyMessage.term()));

		RewriteRule step1 = spec.rule(getNextStepName("Environment"));
		step1.addLHS(fsEnv.term(vActor.term(), vIID.term(), spec.numericTerm(1), vA.term(), vAone.term(), vAu.term(), vAprime.term()));
		step1.addRHS(fsEnv.term(vActor.term(), vIID.term(), spec.numericTerm(9), cTokenPrime.term(), cTokenPrime.term(), cTokenPrime.term(), cTokenPrime.term()));
		step1.addRHS(f1prime.term(cTokenPrime.term()));
		step1.addRHS(f2prime.term(cTokenPrime.term()));
		step1.addRHS(f3prime.term(cTokenPrime.term()));
		step1.addRHS(f4prime.term(cTokenPrime.term()));

		AttackState as = spec.attackState("goal_Uppercase_Name_");
		as.addTerm(f1prime.term(cTokenPrime.term()));
		as.addTerm(f2prime.term(cTokenPrime.term()));
		as.addTerm(f3prime.term(cTokenPrime.term()));
		as.addTerm(f4prime.term(cTokenPrime.term()));
        as.addTerm(fsEnv.term(vActor.term(), vIID.term(), vSL.term(), vA.term(), vAone.term(), vAu.term(), vAprime.term()));

		return spec;
	}

}
