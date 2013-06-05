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
import org.avantssar.aslanpp.model.VariableSymbol;
import org.avantssar.commons.TranslatorOptions;
import avantssar.aslan.backends.Verdict;
import avantssar.aslanpp.testing.Specification;

@Specification(expectedVerdict = Verdict.NoAttack)
public class VariablesFromParentsNoAttack extends AbstractSpecProvider {

	FunctionSymbol fMarkEnv;
	FunctionSymbol fMarkFather;

	@Override
	public ASLanPPSpecification getASLanPPSpecification() {
		ASLanPPSpecification spec = startASLanPPSpec();

		VariableSymbol vEnvA = env.addStateVariable("A", tppText);
		ConstantSymbol cA = env.constants(tppText, "a");
		cA.setNonPublic(true);
		ConstantSymbol cB = env.constants(tppText, "b");
		cB.setNonPublic(true);
		fMarkEnv = env.addFunction("mark_env", tppFact, tppText);
		fMarkFather = env.addFunction("mark_father", tppFact, tppText);
		addBreakpoints(env);

		Entity father = env.entity("Father");
		VariableSymbol vFatherB = father.addStateVariable("B", tppText);
		addBreakpoints(father);

		Entity child = father.entity("Child");

		BlockStatement envBody = env.body(env.block());
		envBody.add(env.newInstance(father));
		envBody.add(env.introduce(fMarkEnv.term(vEnvA.term())));

		BlockStatement fatherBody = father.body(father.block());
		fatherBody.add(father.newInstance(child));
		fatherBody.add(father.introduce(fMarkFather.term(vFatherB.term())));

		BlockStatement childBody = child.body(child.block());
		childBody.add(child.assign(vEnvA.term(), cA.term()));
		childBody.add(child.assign(vFatherB.term(), cB.term()));

		Goal g = env.goal("constants_not_marked");
		g.setFormula(fMarkEnv.term(cA.term()).expression().and(fMarkFather.term(cB.term()).expression()).negate());

		return spec;
	}

	protected void addBreakpoints(Entity ent) {}

	@Override
	public IASLanSpec getExpectedASLanTranslation(TranslatorOptions options) {
		IASLanSpec spec = startASLanSpec();

		Constant cDummyAgent1 = spec.constant("dummy_agent_1", IASLanSpec.AGENT);
		Constant cDummyAgent2 = spec.constant("dummy_agent_2", IASLanSpec.AGENT);
		Constant cDummyText0 = spec.constant("dummy_text", IASLanSpec.TEXT);
		Constant cDummyText1 = spec.constant("dummy_text_1", IASLanSpec.TEXT);

		Function fsEnv = spec.function(getStateFunctionName("Environment"), IASLanSpec.FACT, IASLanSpec.AGENT, IASLanSpec.NAT, IASLanSpec.NAT, IASLanSpec.TEXT);
		Function fsFather = spec.function(getStateFunctionName("Father"), IASLanSpec.FACT, IASLanSpec.AGENT, IASLanSpec.NAT, IASLanSpec.NAT, IASLanSpec.TEXT);
		Function fsChild = spec.function(getStateFunctionName("Child"), IASLanSpec.FACT, IASLanSpec.AGENT, IASLanSpec.NAT, IASLanSpec.NAT);
		Function fMarkEnv = spec.function("mark_env", IASLanSpec.FACT, IASLanSpec.TEXT);
		Function fMarkFather = spec.function("mark_father", IASLanSpec.FACT, IASLanSpec.TEXT);
		Constant cA = spec.constant("a", IASLanSpec.TEXT);
		Constant cB = spec.constant("b", IASLanSpec.TEXT);
		Variable vEnvActor = spec.variable("Actor", IASLanSpec.AGENT);
		Variable vEnvIID = spec.variable("IID", IASLanSpec.NAT);
		Variable vEnvSL = spec.variable("SL", IASLanSpec.NAT);
		Variable vEnvIID_1 = spec.variable("IID_1", IASLanSpec.NAT);
		Variable vFatherIID_2 = spec.variable("IID_2", IASLanSpec.NAT);
		Variable vFatherActor = spec.variable("E_F_Actor", IASLanSpec.AGENT);
		Variable vFatherIID = spec.variable("E_F_IID", IASLanSpec.NAT);
		Variable vFatherSL = spec.variable("E_F_SL", IASLanSpec.NAT);
		Variable vChildActor = spec.variable("E_F_C_Actor", IASLanSpec.AGENT);
		Variable vChildIID = spec.variable("E_F_C_IID", IASLanSpec.NAT);
	//	Variable vChildSL = spec.variable("E_F_C_SL", IASLanSpec.NAT);
		Variable vA = spec.variable("A", IASLanSpec.TEXT);
		Variable vB = spec.variable("B", IASLanSpec.TEXT);

		InitialState init = spec.initialState("init");
		prefillInitialState(spec, init);
		init.addFact(fsEnv.term(cRoot.term(), spec.numericTerm(0), spec.numericTerm(1), cDummyText0.term()));
		// init.addFact(IASLanSpec.IKNOWS.term(cDummyAgent1.term()));
		// init.addFact(IASLanSpec.IKNOWS.term(cDummyAgent2.term()));
		// init.addFact(IASLanSpec.IKNOWS.term(cDummyText1.term()));

		AttackState as = spec.attackState("constants_not_marked");
		as.addTerm(fMarkEnv.term(cA.term()));
		as.addTerm(fMarkFather.term(cB.term()));
        as.addTerm(fsEnv.term(vEnvActor.term(), vEnvIID.term(), vEnvSL.term(), vA.term()));

		RewriteRule step1 = spec.rule(getNextStepName("Environment"));
		step1.addLHS(fsEnv.term(vEnvActor.term(), vEnvIID.term(), spec.numericTerm(1), vA.term()));
		step1.addRHS(fsEnv.term(vEnvActor.term(), vEnvIID.term(), spec.numericTerm(3), vA.term()));
		step1.addRHS(fChild.term(vEnvIID.term(), vEnvIID_1.term()));
		step1.addRHS(fsFather.term(cDummyAgent1.term(), vEnvIID_1.term(), spec.numericTerm(1), cDummyText1.term()));
		step1.addRHS(fMarkEnv.term(vA.term()));
		step1.addExists(vEnvIID_1);

		RewriteRule step2 = spec.rule(getNextStepName("Father"));
		step2.addLHS(fsFather.term(vFatherActor.term(), vFatherIID.term(), spec.numericTerm(1), vB.term()));
		step2.addRHS(fsFather.term(vFatherActor.term(), vFatherIID.term(), spec.numericTerm(3), vB.term()));
		step2.addRHS(fChild.term(vFatherIID.term(), vFatherIID_2.term()));
		step2.addRHS(fsChild.term(cDummyAgent2.term(), vFatherIID_2.term(), spec.numericTerm(1)));
		step2.addRHS(fMarkFather.term(vB.term()));
		step2.addExists(vFatherIID_2);
		step2.addLHS(fDishonest.term(vFatherActor.term()).negate());

		RewriteRule step3 = spec.rule(getNextStepName("Child"));
		step3.addLHS(fsChild.term(vChildActor.term(), vChildIID.term(), spec.numericTerm(1)));
		step3.addLHS(fChild.term(vEnvIID.term(), vFatherIID.term()));
		step3.addRHS(fChild.term(vEnvIID.term(), vFatherIID.term()));
		step3.addLHS(fChild.term(vFatherIID.term(), vChildIID.term()));
		step3.addRHS(fChild.term(vFatherIID.term(), vChildIID.term()));
		step3.addLHS(fsEnv.term(vEnvActor.term(), vEnvIID.term(), vEnvSL.term(), vA.term()));
		step3.addLHS(fsFather.term(vFatherActor.term(), vFatherIID.term(), vFatherSL.term(), vB.term()));
		step3.addRHS(fsChild.term(vChildActor.term(), vChildIID.term(), spec.numericTerm(3)));
		step3.addRHS(fsEnv.term(vEnvActor.term(), vEnvIID.term(), vEnvSL.term(), cA.term()));
		step3.addRHS(fsFather.term(vFatherActor.term(), vFatherIID.term(), vFatherSL.term(), cB.term()));
		step3.addLHS(fDishonest.term(vChildActor.term()).negate());

		return spec;
	}
}
