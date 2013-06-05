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
import org.avantssar.aslanpp.model.LoopStatement;
import org.avantssar.aslanpp.model.Prelude;
import org.avantssar.aslanpp.model.SelectStatement;
import org.avantssar.commons.TranslatorOptions;
import avantssar.aslan.backends.Verdict;
import avantssar.aslanpp.testing.Specification;

@Specification(expectedVerdict = Verdict.NoAttack)
public class SelectsNoAttack extends AbstractSpecProvider {

	protected ConstantSymbol x1;
	protected ConstantSymbol x2;
	protected ConstantSymbol x3;
	protected ConstantSymbol y1;
	protected ConstantSymbol y2;
	protected ConstantSymbol y3;

	Constant cX1;
	Constant cX2;
	Constant cX3;
	Constant cY1;
	Constant cY2;
	Constant cY3;

    Function fsEnv;
    Variable vActor;
    Variable vIID;
    Variable vSL;

	@Override
	public ASLanPPSpecification getASLanPPSpecification() {
		ASLanPPSpecification spec = startASLanPPSpec();

		ConstantSymbol cTrue = spec.findConstant(Prelude.TRUE);

		Entity env = spec.entity("Environment");
		ConstantSymbol a1 = env.constants(env.findType(Prelude.FACT), "a1");
		ConstantSymbol a2 = env.constants(env.findType(Prelude.FACT), "a2");
		ConstantSymbol a3 = env.constants(env.findType(Prelude.FACT), "a3");
		ConstantSymbol a4 = env.constants(env.findType(Prelude.FACT), "a4");
		env.group(a1, a2, a3, a4);
		ConstantSymbol b1 = env.constants(env.findType(Prelude.FACT), "b1");
		ConstantSymbol b2 = env.constants(env.findType(Prelude.FACT), "b2");
		ConstantSymbol b3 = env.constants(env.findType(Prelude.FACT), "b3");
		ConstantSymbol b4 = env.constants(env.findType(Prelude.FACT), "b4");
		env.group(b1, b2, b3, b4);
		x1 = env.constants(env.findType(Prelude.FACT), "x1");
		x2 = env.constants(env.findType(Prelude.FACT), "x2");
		x3 = env.constants(env.findType(Prelude.FACT), "x3");
		env.group(x1, x2, x3);
		y1 = env.constants(env.findType(Prelude.FACT), "y1");
		y2 = env.constants(env.findType(Prelude.FACT), "y2");
		y3 = env.constants(env.findType(Prelude.FACT), "y3");
		env.group(y1, y2, y3);

		BlockStatement envBody = env.body(env.block());
		envBody.add(env.introduce(a1.term()));
		envBody.add(env.introduce(a3.term()));
		envBody.add(env.introduce(a4.term()));
		LoopStatement loopA = envBody.add(env.loop(cTrue.expr()));
		SelectStatement selA = loopA.body(env.select());
		selA.choice(a1.expr().and(a3.expr().or(a4.expr())), env.introduce(x1.term()));
		selA.choice(a2.expr().and(a3.expr().or(a4.expr())), env.introduce(x2.term()));
		selA.choice(a3.expr().and(a4.expr()), env.introduce(x3.term()));
		envBody.add(env.introduce(b2.term()));
		envBody.add(env.introduce(b3.term()));
		LoopStatement loopB = envBody.add(env.loop(cTrue.expr()));
		SelectStatement selB = loopB.body(env.select());
		selB.choice(b1.expr().or(b2.expr()).or(b3.expr()).or(b4.expr()), env.introduce(y1.term()));
		selB.choice(b2.expr().and(b3.expr().or(b4.expr())), env.introduce(y2.term()));
		selB.choice(b1.expr().and(b4.expr()), env.introduce(y3.term()));

		addGoalPP(env);

		return spec;
	}

	protected void addGoalPP(Entity env) {
		Goal g = env.goal("safe");
		g.setFormula(x1.expr().and(x2.expr().not()).and(x3.expr()).and(y1.expr()).and(y2.expr()).and(y3.expr().not()).not());
	}

	@Override
	public IASLanSpec getExpectedASLanTranslation(TranslatorOptions options) {
		IASLanSpec spec = startASLanSpec();

		fsEnv = spec.function(getStateFunctionName("Environment"), IASLanSpec.FACT, IASLanSpec.AGENT, IASLanSpec.NAT, IASLanSpec.NAT);
		Constant a1 = spec.constant("a1", IASLanSpec.FACT);
		Constant a2 = spec.constant("a2", IASLanSpec.FACT);
		Constant a3 = spec.constant("a3", IASLanSpec.FACT);
		Constant a4 = spec.constant("a4", IASLanSpec.FACT);
	//	Constant b1 = spec.constant("b1", IASLanSpec.FACT);
	//	Constant b2 = spec.constant("b2", IASLanSpec.FACT);
	//	Constant b3 = spec.constant("b3", IASLanSpec.FACT);
	//	Constant b4 = spec.constant("b4", IASLanSpec.FACT);
		cX1 = spec.constant("x1", IASLanSpec.FACT);
		cX2 = spec.constant("x2", IASLanSpec.FACT);
		cX3 = spec.constant("x3", IASLanSpec.FACT);
		cY1 = spec.constant("y1", IASLanSpec.FACT);
		cY2 = spec.constant("y2", IASLanSpec.FACT);
		cY3 = spec.constant("y3", IASLanSpec.FACT);
		vActor = spec.variable("Actor", IASLanSpec.AGENT);
		vIID = spec.variable("IID", IASLanSpec.NAT);
		vSL = spec.variable("SL", IASLanSpec.NAT);

		InitialState init = spec.initialState("init");
		prefillInitialState(spec, init);
		init.addFact(fsEnv.term(cRoot.term(), spec.numericTerm(0), spec.numericTerm(1)));

		RewriteRule step1 = spec.rule(getNextStepName("Environment"));
		step1.addLHS(fsEnv.term(vActor.term(), vIID.term(), spec.numericTerm(1)));
		step1.addRHS(fsEnv.term(vActor.term(), vIID.term(), spec.numericTerm(4)));
		step1.addRHS(a1.term());
		step1.addRHS(a3.term());
		step1.addRHS(a4.term());

		RewriteRule step2 = spec.rule(getNextStepName("Environment"));
		step2.addLHS(fsEnv.term(vActor.term(), vIID.term(), spec.numericTerm(4)));
		step2.addLHS(a1.term());
		step2.addLHS(a3.term());
		step2.addRHS(fsEnv.term(vActor.term(), vIID.term(), spec.numericTerm(4)));
		step2.addRHS(a1.term());
		step2.addRHS(a3.term());
		step2.addRHS(cX1.term());

		RewriteRule step3 = spec.rule(getNextStepName("Environment"));
		step3.addLHS(fsEnv.term(vActor.term(), vIID.term(), spec.numericTerm(4)));
		step3.addLHS(a1.term());
		step3.addLHS(a4.term());
		step3.addRHS(fsEnv.term(vActor.term(), vIID.term(), spec.numericTerm(4)));
		step3.addRHS(a1.term());
		step3.addRHS(a4.term());
		step3.addRHS(cX1.term());

		RewriteRule step4 = spec.rule(getNextStepName("Environment"));
		step4.addLHS(fsEnv.term(vActor.term(), vIID.term(), spec.numericTerm(4)));
		step4.addLHS(a2.term());
		step4.addLHS(a3.term());
		step4.addRHS(fsEnv.term(vActor.term(), vIID.term(), spec.numericTerm(4)));
		step4.addRHS(a2.term());
		step4.addRHS(a3.term());
		step4.addRHS(cX2.term());

		RewriteRule step5 = spec.rule(getNextStepName("Environment"));
		step5.addLHS(fsEnv.term(vActor.term(), vIID.term(), spec.numericTerm(4)));
		step5.addLHS(a2.term());
		step5.addLHS(a4.term());
		step5.addRHS(fsEnv.term(vActor.term(), vIID.term(), spec.numericTerm(4)));
		step5.addRHS(a2.term());
		step5.addRHS(a4.term());
		step5.addRHS(cX2.term());

		RewriteRule step6 = spec.rule(getNextStepName("Environment"));
		step6.addLHS(fsEnv.term(vActor.term(), vIID.term(), spec.numericTerm(4)));
		step6.addLHS(a3.term());
		step6.addLHS(a4.term());
		step6.addRHS(fsEnv.term(vActor.term(), vIID.term(), spec.numericTerm(4)));
		step6.addRHS(a3.term());
		step6.addRHS(a4.term());
		step6.addRHS(cX3.term());

		addGoal(spec);

		return spec;
	}

	protected void addGoal(IASLanSpec spec) {
		AttackState as = spec.attackState("safe");
		as.addTerm(cX1.term());
		as.addTerm(cX2.term().negate());
		as.addTerm(cX3.term());
		as.addTerm(cY1.term());
		as.addTerm(cY2.term());
		as.addTerm(cY3.term().negate());
        as.addTerm(fsEnv.term(vActor.term(), vIID.term(), vSL.term()));
	}

}
