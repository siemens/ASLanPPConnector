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

import org.avantssar.aslan.Constant;
import org.avantssar.aslan.Function;
import org.avantssar.aslan.IASLanSpec;
import org.avantssar.aslan.InitialState;
import org.avantssar.aslan.RewriteRule;
import org.avantssar.aslan.Variable;
import org.avantssar.aslanpp.LTLHelper;
import org.avantssar.aslanpp.model.ASLanPPSpecification;
import org.avantssar.aslanpp.model.BlockStatement;
import org.avantssar.aslanpp.model.ConstantSymbol;
import org.avantssar.aslanpp.model.Goal;
import org.avantssar.aslanpp.model.LTLExpression;
import org.avantssar.commons.TranslatorOptions;
import avantssar.aslan.backends.SATMCBackendRunner;
import avantssar.aslanpp.testing.BackendParameters;
import avantssar.aslanpp.testing.Specification;

@Specification
@BackendParameters(backend = SATMCBackendRunner.NAME, parameters = { "--enc=ltl-gp" })
public class LTLGlobally extends AbstractSpecProvider {

	ConstantSymbol prop;
	ConstantSymbol tick1;
	ConstantSymbol tick2;
	ConstantSymbol tick3;

	@Override
	public ASLanPPSpecification getASLanPPSpecification() {
		ASLanPPSpecification spec = startASLanPPSpec();

		prop = env.constants(tppFact, "prop");
		tick1 = env.constants(tppFact, "tick1");
		tick2 = env.constants(tppFact, "tick2");
		tick3 = env.constants(tppFact, "tick3");
		env.addBreakpoints(prop.getName(), tick1.getName(), tick2.getName(), tick3.getName());

		BlockStatement envBody = env.body(env.block());
		complete(envBody);

		addGoal();

		return spec;
	}

	protected void complete(BlockStatement envBody) {
		envBody.add(env.introduce(prop.term()));
		envBody.add(env.introduce(tick1.term()));
		envBody.add(env.introduce(tick2.term()));
		envBody.add(env.retract(prop.term()));
		envBody.add(env.introduce(tick3.term()));
	}

	protected void addGoal() {
		Goal g = env.goal("g");
		g.setFormula(new LTLExpression(LTLExpression.GLOBALLY, tick1.expr().and(prop.expr().not()).not()));
	}

	@Override
	public IASLanSpec getExpectedASLanTranslation(TranslatorOptions options) {
		IASLanSpec spec = startASLanSpec();

		Function fsEnv = spec.function(getStateFunctionName("Environment"), IASLanSpec.FACT, IASLanSpec.AGENT, IASLanSpec.NAT, IASLanSpec.NAT);

		Constant cProp = spec.constant("prop", IASLanSpec.FACT);
		Constant cTick1 = spec.constant("tick1", IASLanSpec.FACT);
		Constant cTick2 = spec.constant("tick2", IASLanSpec.FACT);
		Constant cTick3 = spec.constant("tick3", IASLanSpec.FACT);
		Variable vActor = spec.variable("Actor", IASLanSpec.AGENT);
		Variable vIID = spec.variable("IID", IASLanSpec.NAT);
		Variable vSL = spec.variable("SL", IASLanSpec.NAT);

		// LTL functions
		for (String s : LTLHelper.getInstance().unaryLTLOps) {
			spec.function(LTLExpression.convertOp(s), IASLanSpec.FACT, IASLanSpec.FACT).setPrelude(true);
		}
		for (String s : LTLHelper.getInstance().binaryLTLOps) {
			spec.function(LTLExpression.convertOp(s), IASLanSpec.FACT, IASLanSpec.FACT, IASLanSpec.FACT).setPrelude(true);
		}

		InitialState init = spec.initialState("init");
		prefillInitialState(spec, init);
		init.addFact(fsEnv.term(cRoot.term(), spec.numericTerm(0), spec.numericTerm(1)));

		RewriteRule step1 = spec.rule(getNextStepName("Environment"));
		step1.addLHS(fsEnv.term(vActor.term(), vIID.term(), spec.numericTerm(1)));
		step1.addRHS(fsEnv.term(vActor.term(), vIID.term(), spec.numericTerm(2)));
		step1.addRHS(cProp.term());

		RewriteRule step2 = spec.rule(getNextStepName("Environment"));
		step2.addLHS(fsEnv.term(vActor.term(), vIID.term(), spec.numericTerm(2)));
		step2.addRHS(fsEnv.term(vActor.term(), vIID.term(), spec.numericTerm(3)));
		step2.addRHS(cTick1.term());

		RewriteRule step3 = spec.rule(getNextStepName("Environment"));
		step3.addLHS(fsEnv.term(vActor.term(), vIID.term(), spec.numericTerm(3)));
		step3.addRHS(fsEnv.term(vActor.term(), vIID.term(), spec.numericTerm(4)));
		step3.addRHS(cTick2.term());

		RewriteRule step4 = spec.rule(getNextStepName("Environment"));
		step4.addLHS(fsEnv.term(vActor.term(), vIID.term(), spec.numericTerm(4)));
		step4.addLHS(cProp.term());
		step4.addRHS(fsEnv.term(vActor.term(), vIID.term(), spec.numericTerm(5)));

		RewriteRule step5 = spec.rule(getNextStepName("Environment"));
		step5.addLHS(fsEnv.term(vActor.term(), vIID.term(), spec.numericTerm(5)));
		step5.addRHS(fsEnv.term(vActor.term(), vIID.term(), spec.numericTerm(6)));
		step5.addRHS(cTick3.term());

	//	Function ltlG = spec.findFunction("G");
	//	Function ltlX = spec.findFunction("X");
		org.avantssar.aslan.AttackState as = spec.attackState("g");
		as.addTerm(cProp.term().negate());
		as.addTerm(cTick1.term());
        as.addTerm(fsEnv.term(vActor.term(), vIID.term(), vSL.term()));
        
		return spec;
	}
}
