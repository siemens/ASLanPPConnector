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

import java.io.IOException;
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
import org.avantssar.aslanpp.model.IExpression;
import org.avantssar.aslanpp.model.IType;
import org.avantssar.aslanpp.model.LoopStatement;
import org.avantssar.aslanpp.model.Prelude;
import org.avantssar.aslanpp.model.SetLiteralTerm;
import org.avantssar.aslanpp.model.VariableSymbol;
import org.avantssar.commons.TranslatorOptions;
import avantssar.aslan.backends.CLAtSeBackendRunner;
import avantssar.aslanpp.testing.BackendParameters;
import avantssar.aslanpp.testing.Specification;

@Specification
@BackendParameters(backend = CLAtSeBackendRunner.NAME, parameters = { "--nb", "2" })
public class NumericSetsWithAttack extends AbstractSpecProvider {

	@Override
	public ASLanPPSpecification getASLanPPSpecification() {
		ASLanPPSpecification spec = startASLanPPSpec();

		IType tppNat = env.findType(Prelude.NAT);

		ConstantSymbol cTrue = env.findConstant(Prelude.TRUE);
		FunctionSymbol fRemove = env.findFunction(Prelude.REMOVE);
		FunctionSymbol fAdd = env.findFunction(Prelude.ADD);
		FunctionSymbol fContains = env.findFunction(Prelude.CONTAINS);

		VariableSymbol vNumbers = env.addStateVariable("Numbers", Prelude.getSetOf(tppNat));
		FunctionSymbol fNext = env.addFunction("next", tppNat, tppNat);
		fNext.setNonPublic(true);
		fNext.setNonInvertible(true);

		Entity agent = env.entity("Agent");
		VariableSymbol vN = agent.addParameter("N", tppNat);
		VariableSymbol vS = agent.addParameter("S", Prelude.getSetOf(tppNat));

		BlockStatement agentBody = agent.body(agent.block());
		LoopStatement agentLoop = agentBody.add(agent.loop(cTrue.expr()));
		BlockStatement loopBody = agentLoop.body(agent.block());
		if (doRemove()) {
			loopBody.add(agent.introduce(fRemove.term(vS.term(), vN.term())));
		}
		loopBody.add(agent.assign(vN.term(), fNext.term(fNext.term(vN.term()))));
		loopBody.add(agent.introduce(fAdd.term(vS.term(), vN.term())));

		BlockStatement envBody = env.body(env.block());
		envBody.add(env.assign(vNumbers.term(), SetLiteralTerm.set(env, env.numericTerm(0), env.numericTerm(1))));
		envBody.add(env.newInstance(agent, env.numericTerm(0), vNumbers.term()));
		envBody.add(env.newInstance(agent, env.numericTerm(1), vNumbers.term()));

		Goal g = env.goal("First_Six");
		VariableSymbol vSet = g.addUntypedVariable("Numbers");
		IExpression f = fContains.term(vSet.term(), env.numericTerm(0)).expression();
		f = f.and(fContains.term(vSet.term(), env.numericTerm(1)).expression());
		f = f.and(fContains.term(vSet.term(), fNext.term(fNext.term(env.numericTerm(0)))).expression());
		f = f.and(fContains.term(vSet.term(), fNext.term(fNext.term(env.numericTerm(1)))).expression());
		g.setFormula(f.not().forall(vSet));
		return spec;
	}

	@Override
	public IASLanSpec getExpectedASLanTranslation(TranslatorOptions options) {
		IASLanSpec spec = startASLanSpec();

		Function fsEnv = spec.function(getStateFunctionName("Environment"), IASLanSpec.FACT, IASLanSpec.AGENT, IASLanSpec.NAT, IASLanSpec.NAT, spec.setType(IASLanSpec.NAT));
		Function fsAgent = spec.function(getStateFunctionName("Agent"), IASLanSpec.FACT, IASLanSpec.AGENT, IASLanSpec.NAT, IASLanSpec.NAT, IASLanSpec.NAT, spec.setType(IASLanSpec.NAT));

		Function fNext = spec.function("next", IASLanSpec.NAT, IASLanSpec.NAT);
		Constant cDummyAgent1 = spec.constant("dummy_agent_1", IASLanSpec.AGENT);
		Constant cDummyAgent2 = spec.constant("dummy_agent_2", IASLanSpec.AGENT);
		Constant cDummyNatSet = spec.constant("dummy_set_nat", spec.setType(IASLanSpec.NAT));
		Variable vActor = spec.variable("Actor", IASLanSpec.AGENT);
		Variable vIID = spec.variable("IID", IASLanSpec.NAT);
		Variable vSL = spec.variable("SL", IASLanSpec.NAT);
		Variable vIID_1 = spec.variable("IID_1", IASLanSpec.NAT);
		Variable vIID_2 = spec.variable("IID_2", IASLanSpec.NAT);
		Variable vNumbers = spec.variable("Numbers", spec.setType(IASLanSpec.NAT));
		Variable vAgentActor = spec.variable("E_A_Actor", IASLanSpec.AGENT);
		Variable vAgentIID = spec.variable("E_A_IID", IASLanSpec.NAT);
	//	Variable vAgentSL = spec.variable("E_A_SL", IASLanSpec.NAT);
		Variable vS = spec.variable("S", spec.setType(IASLanSpec.NAT));
		Variable vN = spec.variable("N", IASLanSpec.NAT);
		Variable vGoalSet = spec.variable("E_FS_Numbers", spec.setType(IASLanSpec.NAT));
		Function fSet = spec.function("set_1", spec.setType(IASLanSpec.NAT), IASLanSpec.NAT);

		InitialState init = spec.initialState("init");
		prefillInitialState(spec, init);
		init.addFact(fsEnv.term(cRoot.term(), spec.numericTerm(0), spec.numericTerm(1), cDummyNatSet.term()));
		// init.addFact(IASLanSpec.IKNOWS.term(cDummyAgent1.term()));
		// init.addFact(IASLanSpec.IKNOWS.term(cDummyAgent2.term()));

		RewriteRule step1 = spec.rule(getNextStepName("Environment"));
		step1.addLHS(fsEnv.term(vActor.term(), vIID.term(), spec.numericTerm(1), vNumbers.term()));
		step1.addRHS(fsEnv.term(vActor.term(), vIID.term(), spec.numericTerm(4), fSet.term(vIID.term())));
		step1.addRHS(fChild.term(vIID.term(), vIID_1.term()));
		step1.addRHS(fChild.term(vIID.term(), vIID_2.term()));
		step1.addRHS(fsAgent.term(cDummyAgent1.term(), vIID_1.term(), spec.numericTerm(1), spec.numericTerm(0), fSet.term(vIID.term())));
		step1.addRHS(fsAgent.term(cDummyAgent2.term(), vIID_2.term(), spec.numericTerm(1), spec.numericTerm(1), fSet.term(vIID.term())));
		step1.addRHS(IASLanSpec.CONTAINS.term(spec.numericTerm(0), fSet.term(vIID.term())));
		step1.addRHS(IASLanSpec.CONTAINS.term(spec.numericTerm(1), fSet.term(vIID.term())));
		step1.addExists(vIID_1);
		step1.addExists(vIID_2);

		RewriteRule step2 = spec.rule(getNextStepName("Agent"));
		step2.addLHS(fsAgent.term(vAgentActor.term(), vAgentIID.term(), spec.numericTerm(1), vN.term(), vS.term()));
		if (doRemove()) {
			step2.addLHS(IASLanSpec.CONTAINS.term(vN.term(), vS.term()));
		}
		step2.addRHS(IASLanSpec.CONTAINS.term(fNext.term(fNext.term(vN.term())), vS.term()));
		step2.addRHS(fsAgent.term(vAgentActor.term(), vAgentIID.term(), spec.numericTerm(1), fNext.term(fNext.term(vN.term())), vS.term()));
		step2.addLHS(fDishonest.term(vAgentActor.term()).negate());

		AttackState as = spec.attackState("first_Six");
		as.addTerm(IASLanSpec.CONTAINS.term(spec.numericTerm(0), vGoalSet.term()));
		as.addTerm(IASLanSpec.CONTAINS.term(spec.numericTerm(1), vGoalSet.term()));
		as.addTerm(IASLanSpec.CONTAINS.term(fNext.term(fNext.term(spec.numericTerm(0))), vGoalSet.term()));
		as.addTerm(IASLanSpec.CONTAINS.term(fNext.term(fNext.term(spec.numericTerm(1))), vGoalSet.term()));
        as.addTerm(fsEnv.term(vActor.term(), vIID.term(), vSL.term(), vNumbers.term()));

		return spec;
	}

	protected boolean doRemove() {
		return false;
	}

	public static void main(String[] args) {
		NumericSetsNoAttack ns = new NumericSetsNoAttack();
		ASLanPPSpecification spec = ns.getASLanPPSpecification();
		try {
			spec.toFile("/tmp/out.aslan++");
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println(spec);
		TranslatorOptions to = new TranslatorOptions();
		to.setGoalsAsAttackStates(true);
		IASLanSpec as = ns.getExpectedASLanTranslation(to);
		System.out.println(as);
	}

}
