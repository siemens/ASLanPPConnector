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
import org.avantssar.aslanpp.model.Prelude;
import org.avantssar.aslanpp.model.SimpleType;
import org.avantssar.aslanpp.model.VariableSymbol;
import org.avantssar.commons.TranslatorOptions;
import avantssar.aslanpp.testing.Specification;

@Specification
public class ActorSubtypeAgent extends AbstractSpecProvider {

	@Override
	public ASLanPPSpecification getASLanPPSpecification() {
		ASLanPPSpecification spec = startASLanPPSpec();

		SimpleType tAgentZero = env.type("agent0");
		tAgentZero.setSuperType(env.findType(Prelude.AGENT));

		ConstantSymbol cAZero = env.constants(tAgentZero, "a0");
		cAZero.setNonPublic(true);

		Entity entAgentZero = env.entity("Agent0");
		entAgentZero.addParameter(Entity.ACTOR_PREFIX, tAgentZero);
		VariableSymbol vAZero = entAgentZero.addStateVariable("A0", tAgentZero);
		BlockStatement entAgentZeroBody = entAgentZero.body(entAgentZero.block());
		entAgentZeroBody.add(entAgentZero.assign(vAZero.term(), entAgentZero.getActorSymbol().term()));
		entAgentZeroBody.add(entAgentZero.assertion("step_reached")).setGuard(spec.findConstant(Prelude.FALSE).term().expression());

		env.body(env.newInstance(entAgentZero, cAZero.term()));

		return spec;
	}

	@Override
	public IASLanSpec getExpectedASLanTranslation(TranslatorOptions options) {
		IASLanSpec spec = startASLanSpec();

		org.avantssar.aslan.PrimitiveType tAgentZero = spec.primitiveType("agent0");
		tAgentZero.setSuperType(IASLanSpec.AGENT);
		Constant cDummyAgentZero = spec.constant("dummy_agent0_1", tAgentZero);

		Function fsEnv = spec.function(getStateFunctionName("Environment"), IASLanSpec.FACT, IASLanSpec.AGENT, IASLanSpec.NAT, IASLanSpec.NAT);
		Function fsAgentZero = spec.function(getStateFunctionName("Agent0"), IASLanSpec.FACT, tAgentZero, IASLanSpec.NAT, IASLanSpec.NAT, tAgentZero);

		Constant cAZero = spec.constant("a0", tAgentZero);
		Variable vActor = spec.variable("Actor", IASLanSpec.AGENT);
		Variable vIID = spec.variable("IID", IASLanSpec.NAT);
	  //Variable vSL = spec.variable("SL", IASLanSpec.NAT);
		Variable vIID_1 = spec.variable("IID_1", IASLanSpec.NAT);
		Variable vA0Actor = spec.variable("E_A0_Actor", tAgentZero);
		Variable vA0IID = spec.variable("E_A0_IID", IASLanSpec.NAT);
		Variable vA0SL = spec.variable("E_A0_SL", IASLanSpec.NAT);
		Variable vA0AZero = spec.variable("A0", tAgentZero);
		Function fAssert = spec.function("check_step_reached", IASLanSpec.FACT, IASLanSpec.NAT, IASLanSpec.NAT);

		InitialState init = spec.initialState("init");
		prefillInitialState(spec, init);
		init.addFact(fsEnv.term(cRoot.term(), spec.numericTerm(0), spec.numericTerm(1)));
		// init.addFact(IASLanSpec.IKNOWS.term(cDummyAgentZero.term()));
		// init.addFact(fIsAgent.term(cAZero.term()));

		RewriteRule step1 = spec.rule(getNextStepName("Environment"));
		step1.addLHS(fsEnv.term(vActor.term(), vIID.term(), spec.numericTerm(1)));
		step1.addRHS(fsEnv.term(vActor.term(), vIID.term(), spec.numericTerm(2)));
		step1.addRHS(fChild.term(vIID.term(), vIID_1.term()));
		step1.addRHS(fsAgentZero.term(cAZero.term(), vIID_1.term(), spec.numericTerm(1), cDummyAgentZero.term()));
		step1.addExists(vIID_1);

		RewriteRule step2 = spec.rule(getNextStepName("Agent0"));
		step2.addLHS(fsAgentZero.term(vA0Actor.term(), vA0IID.term(), spec.numericTerm(1), vA0AZero.term()));
		step2.addRHS(fsAgentZero.term(vA0Actor.term(), vA0IID.term(), spec.numericTerm(3), vA0Actor.term()));
		step2.addRHS(fAssert.term(vA0IID.term(), spec.numericTerm(3)));
		step2.addLHS(fDishonest.term(vA0Actor.term()).negate());

		RewriteRule step3 = spec.rule(getNextStepName("Agent0"));
		step3.addLHS(fsAgentZero.term(vA0Actor.term(), vA0IID.term(), spec.numericTerm(3), vA0AZero.term()));
		step3.addLHS(fAssert.term(vA0IID.term(), spec.numericTerm(3)));
		step3.addRHS(fsAgentZero.term(vA0Actor.term(), vA0IID.term(), spec.numericTerm(4), vA0AZero.term()));

		AttackState as = spec.attackState("step_reached");
		as.addTerm(fAssert.term(vA0IID.term(), vA0SL.term()));
		as.addTerm(spec.findConstant("false").term().negate());

		return spec;
	}
}
