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
import org.avantssar.aslanpp.model.Equation;
import org.avantssar.aslanpp.model.FunctionSymbol;
import org.avantssar.aslanpp.model.Goal;
import org.avantssar.aslanpp.model.Prelude;
import org.avantssar.aslanpp.model.VariableSymbol;
import org.avantssar.commons.TranslatorOptions;
import avantssar.aslanpp.testing.Specification;

@Specification
public class Equations extends AbstractSpecProvider {

	@Override
	public ASLanPPSpecification getASLanPPSpecification() {
		ASLanPPSpecification spec = startASLanPPSpec();

		FunctionSymbol f1 = env.addFunction("f1", tppText, tppText, tppText);
		f1.setNonInvertible(true);
		f1.setNonPublic(true);
		FunctionSymbol f2 = env.addFunction("f2", tppText, tppText, tppText);
		f2.setNonPublic(true);
		f2.setNonInvertible(true);
		ConstantSymbol cA = env.constants(tppText, "a");
		cA.setNonPublic(true);
		ConstantSymbol cB = env.constants(tppText, "b");
		cB.setNonPublic(true);

		Equation eq = env.equation();
		VariableSymbol eqA = eq.addUntypedVariable("A");
		VariableSymbol eqB = eq.addUntypedVariable("B");
		eq.setLeftTerm(f1.term(eqA.term(), eqB.term()));
		eq.setRightTerm(f2.term(eqB.term(), eqA.term()));

		FunctionSymbol fIknows = env.findFunction(Prelude.IKNOWS);

		BlockStatement envBody = env.body(env.block());
		envBody.add(env.introduce(fIknows.term(f1.term(cA.term(), cB.term()))));

		Goal g = env.goal("not_f2");
		g.setFormula(fIknows.term(f2.term(cB.term(), cA.term())).expression().not());
		return spec;
	}

	@Override
	public IASLanSpec getExpectedASLanTranslation(TranslatorOptions options) {
		IASLanSpec spec = startASLanSpec();

		Function fsEnv = spec.function(getStateFunctionName("Environment"), IASLanSpec.FACT, IASLanSpec.AGENT, IASLanSpec.NAT, IASLanSpec.NAT);
		Function f1 = spec.function("f1", IASLanSpec.TEXT, IASLanSpec.TEXT, IASLanSpec.TEXT);
		Function f2 = spec.function("f2", IASLanSpec.TEXT, IASLanSpec.TEXT, IASLanSpec.TEXT);
		Constant cA = spec.constant("a", IASLanSpec.TEXT);
		Constant cB = spec.constant("b", IASLanSpec.TEXT);
	//	Variable vA = spec.variable("A", IASLanSpec.TEXT);
	//	Variable vB = spec.variable("B", IASLanSpec.TEXT);
		Variable vActor = spec.variable("Actor", IASLanSpec.AGENT);
		Variable vIID = spec.variable("IID", IASLanSpec.NAT);
		Variable vSL = spec.variable("SL", IASLanSpec.NAT);

	//	org.avantssar.aslan.Equation eq = spec.equation(f1.term(vA.term(), vB.term()), f2.term(vB.term(), vA.term()));

		InitialState init = spec.initialState("init");
		prefillInitialState(spec, init);
		init.addFact(fsEnv.term(cRoot.term(), spec.numericTerm(0), spec.numericTerm(1)));

		RewriteRule step1 = spec.rule(getNextStepName("Environment"));
		step1.addLHS(fsEnv.term(vActor.term(), vIID.term(), spec.numericTerm(1)));
		step1.addRHS(fsEnv.term(vActor.term(), vIID.term(), spec.numericTerm(2)));
		step1.addRHS(IASLanSpec.IKNOWS.term(f1.term(cA.term(), cB.term())));

		AttackState as = spec.attackState("not_f2");
		as.addTerm(IASLanSpec.IKNOWS.term(f2.term(cB.term(), cA.term())));
        as.addTerm(fsEnv.term(vActor.term(), vIID.term(), vSL.term()));

		return spec;
	}
}
