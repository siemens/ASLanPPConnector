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
import org.avantssar.aslanpp.model.Goal;
import org.avantssar.commons.TranslatorOptions;
import avantssar.aslan.backends.Verdict;
import avantssar.aslanpp.testing.Specification;

@Specification(expectedVerdict = Verdict.NoAttack)
public class LumpingFactsSafe extends AbstractSpecProvider {

	ConstantSymbol cPhasePP;
	ConstantSymbol f;

	Function fsTimer;
	Variable vTimerActor;
	Variable vTimerIID;
	Variable vTimerSL;
	Constant cPhase;

	@Override
	public ASLanPPSpecification getASLanPPSpecification() {
		ASLanPPSpecification spec = startASLanPPSpec();

		cPhasePP = env.constants(tppFact, "submission_phase_open");
		f = env.constants(tppFact, "f");

		Entity timer = env.entity("SubmissionTimer");
		setBreakpoints(timer);
		BlockStatement timerBody = timer.body(timer.block());
		timerBody.add(timer.introduce(cPhasePP.term()));
		timerBody.add(timer.retract(cPhasePP.term()));

		Entity other = env.entity("Other");
		other.body(other.introduce(f.term()));

		BlockStatement envBody = env.body(env.block());
		envBody.add(env.newInstance(timer));
		envBody.add(env.newInstance(other));

		Goal g = env.goal("in_phase");
		g.setFormula(cPhasePP.expr().and(f.expr()).not());

		return spec;
	}

	protected void setBreakpoints(Entity timer) {}

	@Override
	public IASLanSpec getExpectedASLanTranslation(TranslatorOptions options) {
		IASLanSpec spec = startASLanSpec();

		Constant cDummyAgent1 = spec.constant("dummy_agent_1", IASLanSpec.AGENT);
		Constant cDummyAgent2 = spec.constant("dummy_agent_2", IASLanSpec.AGENT);

		Function fsEnv = spec.function(getStateFunctionName("Environment"), IASLanSpec.FACT, IASLanSpec.AGENT, IASLanSpec.NAT, IASLanSpec.NAT);
		fsTimer = spec.function(getStateFunctionName("SubmissionTimer"), IASLanSpec.FACT, IASLanSpec.AGENT, IASLanSpec.NAT, IASLanSpec.NAT);
		Function fsOther = spec.function(getStateFunctionName("Other"), IASLanSpec.FACT, IASLanSpec.AGENT, IASLanSpec.NAT, IASLanSpec.NAT);

		cPhase = spec.constant("submission_phase_open", IASLanSpec.FACT);
		Constant cF = spec.constant("f", IASLanSpec.FACT);
		Variable vActor = spec.variable("Actor", IASLanSpec.AGENT);
		Variable vIID = spec.variable("IID", IASLanSpec.NAT);
		Variable vSL = spec.variable("SL", IASLanSpec.NAT);
		vTimerActor = spec.variable("E_ST_Actor", IASLanSpec.AGENT);
		vTimerIID = spec.variable("E_ST_IID", IASLanSpec.NAT);
		vTimerSL = spec.variable("E_ST_SL", IASLanSpec.NAT);
		Variable vOtherActor = spec.variable("E_O_Actor", IASLanSpec.AGENT);
		Variable vOtherIID = spec.variable("E_O_IID", IASLanSpec.NAT);
	//	Variable vOtherSL = spec.variable("E_O_SL", IASLanSpec.NAT);
		Variable vIID_1 = spec.variable("IID_1", IASLanSpec.NAT);
		Variable vIID_2 = spec.variable("IID_2", IASLanSpec.NAT);

		InitialState init = spec.initialState("init");
		prefillInitialState(spec, init);
		init.addFact(fsEnv.term(cRoot.term(), spec.numericTerm(0), spec.numericTerm(1)));
		// init.addFact(IASLanSpec.IKNOWS.term(cDummyAgent1.term()));
		// init.addFact(IASLanSpec.IKNOWS.term(cDummyAgent2.term()));

		RewriteRule step1 = spec.rule(getNextStepName("Environment"));
		step1.addLHS(fsEnv.term(vActor.term(), vIID.term(), spec.numericTerm(1)));
		step1.addRHS(fsEnv.term(vActor.term(), vIID.term(), spec.numericTerm(3)));
		step1.addRHS(fChild.term(vIID.term(), vIID_1.term()));
		step1.addRHS(fsTimer.term(cDummyAgent1.term(), vIID_1.term(), spec.numericTerm(1)));
		step1.addRHS(fChild.term(vIID.term(), vIID_2.term()));
		step1.addRHS(fsOther.term(cDummyAgent2.term(), vIID_2.term(), spec.numericTerm(1)));
		step1.addExists(vIID_1);
		step1.addExists(vIID_2);

		addTimerSteps(spec);

		RewriteRule stepX = spec.rule(getNextStepName("Other"));
		stepX.addLHS(fsOther.term(vOtherActor.term(), vOtherIID.term(), spec.numericTerm(1)));
		stepX.addRHS(fsOther.term(vOtherActor.term(), vOtherIID.term(), spec.numericTerm(2)));
		stepX.addRHS(cF.term());
		stepX.addLHS(fDishonest.term(vOtherActor.term()).negate());

		AttackState as = spec.attackState("in_phase");
		as.addTerm(cPhase.term());
		as.addTerm(cF.term());
        as.addTerm(fsEnv.term(vActor.term(), vIID.term(), vSL.term()));

		return spec;
	}

	protected void addTimerSteps(IASLanSpec spec) {
		RewriteRule step2 = spec.rule(getNextStepName("SubmissionTimer"));
		step2.addLHS(fsTimer.term(vTimerActor.term(), vTimerIID.term(), spec.numericTerm(1)));
		step2.addRHS(fsTimer.term(vTimerActor.term(), vTimerIID.term(), spec.numericTerm(3)));
		step2.addLHS(fDishonest.term(vTimerActor.term()).negate());
	}
}
