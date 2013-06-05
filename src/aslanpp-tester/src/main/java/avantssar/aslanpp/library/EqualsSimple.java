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
import org.avantssar.aslanpp.model.ConcatTerm;
import org.avantssar.aslanpp.model.ConstantSymbol;
import org.avantssar.aslanpp.model.FunctionSymbol;
import org.avantssar.aslanpp.model.Goal;
import org.avantssar.aslanpp.model.IExpression;
import org.avantssar.aslanpp.model.SelectStatement;
import org.avantssar.aslanpp.model.VariableSymbol;
import org.avantssar.commons.TranslatorOptions;
import avantssar.aslanpp.testing.Specification;

@Specification
public class EqualsSimple extends AbstractSpecProvider {

	@Override
	public ASLanPPSpecification getASLanPPSpecification() {
		ASLanPPSpecification spec = startASLanPPSpec();

		ConstantSymbol part1 = env.constants(tppMessage, "part1");
		part1.setNonPublic(true);
		ConstantSymbol part2 = env.constants(tppMessage, "part2");
		part2.setNonPublic(true);
		ConstantSymbol part3 = env.constants(tppMessage, "part3");
		part3.setNonPublic(true);
		VariableSymbol m = env.addStateVariable("M", tppMessage);
		VariableSymbol p1 = env.addStateVariable("P1", tppMessage);
		VariableSymbol p2 = env.addStateVariable("P2", tppMessage);
		FunctionSymbol fSeen = env.addFunction("seen", tppFact, tppMessage);
		fSeen.setNonInvertible(true);
		fSeen.setNonPublic(true);

		BlockStatement envBody = env.body(env.block());
		envBody.add(env.assign(m.term(), ConcatTerm.concat(env, part1.term(), part2.term())));
		SelectStatement sel = envBody.add(env.select());
		IExpression cond = m.term().equality(ConcatTerm.concat(env, p1.matchedTerm(), p2.matchedTerm()));
		BlockStatement selBody = sel.choice(cond, env.block());
		selBody.add(env.introduce(fSeen.term(p1.term())));
		selBody.add(env.introduce(fSeen.term(p2.term())));

		Goal g = env.goal("g");
		g.setFormula(fSeen.term(part1.term()).expression().and(fSeen.term(part2.term()).expression()).not());

		return spec;
	}

	@Override
	public IASLanSpec getExpectedASLanTranslation(TranslatorOptions options) {
		IASLanSpec spec = startASLanSpec();

		Constant cDummyMessage = spec.constant("dummy_message", IASLanSpec.MESSAGE);

		Function fsEnv = spec.function(getStateFunctionName("Environment"), IASLanSpec.FACT, IASLanSpec.AGENT, IASLanSpec.NAT, IASLanSpec.NAT, IASLanSpec.MESSAGE, IASLanSpec.MESSAGE,
				IASLanSpec.MESSAGE);
		Function fSeen = spec.function("seen", IASLanSpec.FACT, IASLanSpec.MESSAGE);
		Constant part1 = spec.constant("part1", IASLanSpec.MESSAGE);
		Constant part2 = spec.constant("part2", IASLanSpec.MESSAGE);
	//	Constant part3 = spec.constant("part3", IASLanSpec.MESSAGE);
		Variable m = spec.variable("M", IASLanSpec.MESSAGE);
		Variable p1 = spec.variable("P1", IASLanSpec.MESSAGE);
		Variable p1match = spec.variable("P1_1", IASLanSpec.MESSAGE);
		Variable p2 = spec.variable("P2", IASLanSpec.MESSAGE);
		Variable p2match = spec.variable("P2_1", IASLanSpec.MESSAGE);
		Variable vActor = spec.variable("Actor", IASLanSpec.AGENT);
		Variable vIID = spec.variable("IID", IASLanSpec.NAT);
		Variable vSL = spec.variable("SL", IASLanSpec.NAT);

		InitialState init = spec.initialState("init");
		prefillInitialState(spec, init);
		init.addFact(fsEnv.term(cRoot.term(), spec.numericTerm(0), spec.numericTerm(1), cDummyMessage.term(), cDummyMessage.term(), cDummyMessage.term()));

		RewriteRule step1 = spec.rule(getNextStepName("Environment"));
		step1.addLHS(fsEnv.term(vActor.term(), vIID.term(), spec.numericTerm(1), m.term(), p1.term(), p2.term()));
		step1.addRHS(fsEnv.term(vActor.term(), vIID.term(), spec.numericTerm(2), IASLanSpec.PAIR.term(part1.term(), part2.term()), p1.term(), p2.term()));

		RewriteRule step2 = spec.rule(getNextStepName("Environment"));
		step2.addLHS(fsEnv.term(vActor.term(), vIID.term(), spec.numericTerm(2), m.term(), p1.term(), p2.term()));
		step2.addLHS(IASLanSpec.EQUAL.term(m.term(), IASLanSpec.PAIR.term(p1match.term(), p2match.term())));
		step2.addRHS(fsEnv.term(vActor.term(), vIID.term(), spec.numericTerm(5), m.term(), p1match.term(), p2match.term()));
		step2.addRHS(fSeen.term(p1match.term()));
		step2.addRHS(fSeen.term(p2match.term()));

		AttackState as = spec.attackState("g");
		as.addTerm(fSeen.term(part1.term()));
		as.addTerm(fSeen.term(part2.term()));
        as.addTerm(fsEnv.term(vActor.term(), vIID.term(), vSL.term(), m.term(), p1.term(), p2.term()));

		return spec;
	}

}
