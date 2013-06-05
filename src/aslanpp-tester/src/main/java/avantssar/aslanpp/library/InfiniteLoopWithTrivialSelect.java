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
import org.avantssar.aslanpp.model.LoopStatement;
import org.avantssar.aslanpp.model.Prelude;
import org.avantssar.aslanpp.model.SelectStatement;
import org.avantssar.aslanpp.model.VariableSymbol;
import org.avantssar.commons.TranslatorOptions;
import avantssar.aslan.backends.CLAtSeBackendRunner;
import avantssar.aslanpp.testing.BackendParameters;
import avantssar.aslanpp.testing.Specification;

@Specification
@BackendParameters(backend = CLAtSeBackendRunner.NAME, parameters = { "--nb", "2" })
public class InfiniteLoopWithTrivialSelect extends AbstractSpecProvider {

	protected org.avantssar.aslan.Constant cDummyMessage;

	@Override
	public ASLanPPSpecification getASLanPPSpecification() {
		ASLanPPSpecification spec = startASLanPPSpec();

		ConstantSymbol peter = env.constants(env.findType(Prelude.AGENT), "peter");
		peter.setNonPublic(true);
		FunctionSymbol empStarted = env.addFunction("tagged", env.findType(Prelude.FACT), env.findType(Prelude.AGENT), env.findType(Prelude.MESSAGE));

		Entity employee = env.entity("Employee");
		employee.addParameter(Entity.ACTOR_PREFIX, env.findType(Prelude.AGENT));
		VariableSymbol empToken = employee.addStateVariable("Token", env.findType(Prelude.MESSAGE));
		BlockStatement body = employee.body(employee.block());
		LoopStatement loop = body.add(employee.loop(cppTrue.term().expression()));
		BlockStatement loopBody = loop.body(employee.block());
		SelectStatement select = loopBody.add(employee.select());
		BlockStatement selectBody = select.choice(cppTrue.term().expression(), employee.block());
		selectBody.add(employee.fresh(empToken.term()));
		selectBody.add(employee.introduce(empStarted.term(employee.getActorSymbol().term(), empToken.term())));

		BlockStatement envBody = env.body(env.block());
		envBody.add(env.newInstance(employee, peter.term()));

		Goal g = env.goal("no_double_tagging");
		VariableSymbol e1 = g.addUntypedVariable("E");
		VariableSymbol t1 = g.addUntypedVariable("T1");
		VariableSymbol t2 = g.addUntypedVariable("T2");
		g.setFormula(empStarted.term(e1.term(), t1.term()).expression().and(empStarted.term(e1.term(), t2.term()).expression()).and(t1.term().equality(t2.term()).not()).exists(e1, t1, t2).not());
		return spec;
	}

	@Override
	public IASLanSpec getExpectedASLanTranslation(TranslatorOptions options) {
		IASLanSpec spec = startASLanSpec();

		cDummyMessage = spec.constant("dummy_message_1", IASLanSpec.MESSAGE);

		Function fsEnv = spec.function(getStateFunctionName("Environment"), IASLanSpec.FACT, IASLanSpec.AGENT, IASLanSpec.NAT, IASLanSpec.NAT);
		Function fsEmployee = spec.function(getStateFunctionName("Employee"), IASLanSpec.FACT, IASLanSpec.AGENT, IASLanSpec.NAT, IASLanSpec.NAT, IASLanSpec.MESSAGE);
		Function fTagged = spec.function("tagged", IASLanSpec.FACT, IASLanSpec.AGENT, IASLanSpec.MESSAGE);

		Variable vEnvActor = spec.variable("Actor", IASLanSpec.AGENT);
		Variable vEnvIID = spec.variable("IID", IASLanSpec.NAT);
		Variable vEnvSL = spec.variable("SL", IASLanSpec.NAT);
		Variable vEnvIID_1 = spec.variable("IID_1", IASLanSpec.NAT);
		Variable vEmployeeActor = spec.variable("E_E_Actor", IASLanSpec.AGENT);
		Variable vEmployeeIID = spec.variable("E_E_IID", IASLanSpec.NAT);
	//	Variable vEmployeeSL = spec.variable("E_E_SL", IASLanSpec.NAT);
		Variable vEmployeeToken = spec.variable("Token", IASLanSpec.MESSAGE);
		Variable vEmployeeTokenMatch = spec.variable("Token_1", IASLanSpec.MESSAGE);
		Variable vGoalE = spec.variable("E", IASLanSpec.AGENT);
		Variable vGoalT1 = spec.variable("T1", IASLanSpec.MESSAGE);
		Variable vGoalT2 = spec.variable("T2", IASLanSpec.MESSAGE);
		Constant cPeter = spec.constant("peter", IASLanSpec.AGENT);

		InitialState init = spec.initialState("init");
		prefillInitialState(spec, init);
		init.addFact(fsEnv.term(cRoot.term(), spec.numericTerm(0), spec.numericTerm(1)));
		// init.addFact(IASLanSpec.IKNOWS.term(cDummyMessage.term()));
		// init.addFact(fIsAgent.term(cPeter.term()));

		RewriteRule step1 = spec.rule(getNextStepName("Environment"));
		step1.addLHS(fsEnv.term(vEnvActor.term(), vEnvIID.term(), spec.numericTerm(1)));
		step1.addRHS(fsEnv.term(vEnvActor.term(), vEnvIID.term(), spec.numericTerm(2)));
		step1.addRHS(fChild.term(vEnvIID.term(), vEnvIID_1.term()));
		step1.addRHS(fsEmployee.term(cPeter.term(), vEnvIID_1.term(), spec.numericTerm(1), cDummyMessage.term()));
		step1.addExists(vEnvIID_1);

		RewriteRule step2 = spec.rule(getNextStepName("Employee"));
		step2.addLHS(fsEmployee.term(vEmployeeActor.term(), vEmployeeIID.term(), spec.numericTerm(1), vEmployeeToken.term()));
		step2.addRHS(fTagged.term(vEmployeeActor.term(), vEmployeeTokenMatch.term()));
		step2.addRHS(fsEmployee.term(vEmployeeActor.term(), vEmployeeIID.term(), spec.numericTerm(1), vEmployeeTokenMatch.term()));
		step2.addExists(vEmployeeTokenMatch);
		step2.addLHS(fDishonest.term(vEmployeeActor.term()).negate());

		AttackState as = spec.attackState("no_double_tagging");
		as.addTerm(fTagged.term(vGoalE.term(), vGoalT1.term()));
		as.addTerm(fTagged.term(vGoalE.term(), vGoalT2.term()));
		as.addTerm(IASLanSpec.EQUAL.term(vGoalT1.term(), vGoalT2.term()).negate());
        as.addTerm(fsEnv.term(vEnvActor.term(), vEnvIID.term(), vEnvSL.term()));

		return spec;
	}
}
