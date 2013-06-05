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
import org.avantssar.aslanpp.model.HornClause;
import org.avantssar.aslanpp.model.LoopStatement;
import org.avantssar.aslanpp.model.Prelude;
import org.avantssar.aslanpp.model.SetLiteralTerm;
import org.avantssar.aslanpp.model.VariableSymbol;
import org.avantssar.commons.TranslatorOptions;
import avantssar.aslanpp.testing.Specification;

@Specification
public class SetsAndHornClauses extends AbstractSpecProvider {

	@Override
	public ASLanPPSpecification getASLanPPSpecification() {
		ASLanPPSpecification spec = startASLanPPSpec();

		FunctionSymbol fContains = spec.findFunction(Prelude.CONTAINS);
		FunctionSymbol fAdd = spec.findFunction(Prelude.ADD);
		FunctionSymbol fRemove = spec.findFunction(Prelude.REMOVE);

		Entity env = spec.entity("Environment");
		ConstantSymbol cA = env.constants(env.findType(Prelude.TEXT), "a");
		cA.setNonPublic(true);
		ConstantSymbol cB = env.constants(env.findType(Prelude.TEXT), "b");
		cB.setNonPublic(true);
		ConstantSymbol cC = env.constants(env.findType(Prelude.TEXT), "c");
		cC.setNonPublic(true);
		env.group(cA, cB, cC);
		VariableSymbol vM = env.addStateVariable("M", env.findType(Prelude.TEXT));
		VariableSymbol vS = env.addStateVariable("S", Prelude.getSetOf(env.findType(Prelude.TEXT)));
		VariableSymbol vSPrime = env.addStateVariable("S'", Prelude.getSetOf(env.findType(Prelude.TEXT)));
		env.group(vS, vSPrime);
		FunctionSymbol fVisited = env.addFunction("visited", env.findType(Prelude.FACT), env.findType(Prelude.TEXT));
		fVisited.setNonInvertible(true);
		fVisited.setNonPublic(true);
		FunctionSymbol fMember = env.addFunction("member", env.findType(Prelude.FACT), env.findType(Prelude.TEXT));
		fMember.setNonInvertible(true);
		fMember.setNonPublic(true);

		HornClause hcMembership = env.hornClause("membership");
		VariableSymbol hcVarS = hcMembership.argument("Set");
		VariableSymbol hcVarM = hcMembership.argument("Member");
		hcMembership.setHead(fMember.term(hcVarM.term()));
		hcMembership.addBody(fContains.oopTerm(hcVarS.term(), hcVarM.term()));

		BlockStatement envBody = env.body(env.block());
		envBody.add(env.assign(vS.term(), SetLiteralTerm.set(env, cA.term(), cB.term(), cC.term())));
		LoopStatement whRemove = envBody.add(env.loop(fContains.oopTerm(vS.term(), vM.matchedTerm()).expression()));
		BlockStatement whRemoveBody = whRemove.body(env.block());
		whRemoveBody.add(env.introduce(fRemove.oopTerm(vS.term(), vM.term())));
		whRemoveBody.add(env.introduce(fAdd.oopTerm(vSPrime.term(), vM.term())));
		whRemoveBody.add(env.introduce(fVisited.term(vM.term())));
		LoopStatement whAddBack = envBody.add(env.loop(fContains.oopTerm(vSPrime.term(), vM.matchedTerm()).expression()));
		BlockStatement whAddBackBody = whAddBack.body(env.block());
		whAddBackBody.add(env.introduce(fRemove.oopTerm(vSPrime.term(), vM.term())));
		whAddBackBody.add(env.introduce(fAdd.oopTerm(vS.term(), vM.term())));

		Goal g = env.goal("not_removed_and_added_back");
		VariableSymbol gVarM = g.addUntypedVariable("K");
		g.setFormula(fMember.term(gVarM.term()).expression().and(fVisited.term(gVarM.term()).expression()).exists(gVarM).not());

		return spec;
	}

	@Override
	public IASLanSpec getExpectedASLanTranslation(TranslatorOptions options) {
		IASLanSpec spec = startASLanSpec();

		Function fContains = spec.findFunction("contains");

		Constant cDummyText = spec.constant("dummy_text", IASLanSpec.TEXT);
		Constant cDummyTextSet = spec.constant("dummy_set_text", spec.setType(IASLanSpec.TEXT));

		Function fsEnv = spec.function(getStateFunctionName("Environment"), IASLanSpec.FACT, IASLanSpec.AGENT, IASLanSpec.NAT, IASLanSpec.NAT, IASLanSpec.TEXT, spec.setType(IASLanSpec.TEXT), spec
				.setType(IASLanSpec.TEXT));

		Function fVisited = spec.function("visited", IASLanSpec.FACT, IASLanSpec.TEXT);
		Function fMember = spec.function("member", IASLanSpec.FACT, IASLanSpec.TEXT);
		Constant cA = spec.constant("a", IASLanSpec.TEXT);
		Constant cB = spec.constant("b", IASLanSpec.TEXT);
		Constant cC = spec.constant("c", IASLanSpec.TEXT);
		Variable vM = spec.variable("M", IASLanSpec.TEXT);
		Variable vMMatched = spec.variable("M_1", IASLanSpec.TEXT);
		Variable vMMatchedAgain = spec.variable("M_2", IASLanSpec.TEXT);
		Variable vS = spec.variable("S", spec.setType(IASLanSpec.TEXT));
		Variable vSPrime = spec.variable("S_", spec.setType(IASLanSpec.TEXT));
		Variable vSet = spec.variable("Set", spec.setType(IASLanSpec.TEXT));
		Variable vMember = spec.variable("Member", IASLanSpec.TEXT);
		Variable vK = spec.variable("K", IASLanSpec.TEXT);
		Function fSet1 = spec.function("set_1", spec.setType(IASLanSpec.TEXT), IASLanSpec.NAT);
		Variable vActor = spec.variable("Actor", IASLanSpec.AGENT);
		Variable vIID = spec.variable("IID", IASLanSpec.NAT);
		Variable vSL = spec.variable("SL", IASLanSpec.NAT);

		org.avantssar.aslan.HornClause hcMembership = spec.hornClause("membership", fMember.term(vMember.term()));
		hcMembership.addBodyFact(fContains.term(vMember.term(), vSet.term()));

		InitialState init = spec.initialState("init");
		prefillInitialState(spec, init);
		init.addFact(fsEnv.term(cRoot.term(), spec.numericTerm(0), spec.numericTerm(1), cDummyText.term(), cDummyTextSet.term(), cDummyTextSet.term()));

		RewriteRule step1 = spec.rule(getNextStepName("Environment"));
		step1.addLHS(fsEnv.term(vActor.term(), vIID.term(), spec.numericTerm(1), vM.term(), vS.term(), vSPrime.term()));
		step1.addRHS(fsEnv.term(vActor.term(), vIID.term(), spec.numericTerm(2), vM.term(), fSet1.term(vIID.term()), vSPrime.term()));
		step1.addRHS(fContains.term(cA.term(), fSet1.term(vIID.term())));
		step1.addRHS(fContains.term(cB.term(), fSet1.term(vIID.term())));
		step1.addRHS(fContains.term(cC.term(), fSet1.term(vIID.term())));

		RewriteRule step2 = spec.rule(getNextStepName("Environment"));
		step2.addLHS(fsEnv.term(vActor.term(), vIID.term(), spec.numericTerm(2), vM.term(), vS.term(), vSPrime.term()));
		step2.addLHS(fContains.term(vMMatched.term(), vS.term()));
		step2.addRHS(fsEnv.term(vActor.term(), vIID.term(), spec.numericTerm(2), vMMatched.term(), vS.term(), vSPrime.term()));
		step2.addRHS(fContains.term(vMMatched.term(), vSPrime.term()));
		step2.addRHS(fVisited.term(vMMatched.term()));

		RewriteRule step3 = spec.rule(getNextStepName("Environment"));
		step3.addLHS(fsEnv.term(vActor.term(), vIID.term(), spec.numericTerm(2), vM.term(), vS.term(), vSPrime.term()));
		step3.addLHS(fContains.term(vMMatched.term(), vS.term()).negate());
		step3.addRHS(fsEnv.term(vActor.term(), vIID.term(), spec.numericTerm(6), vM.term(), vS.term(), vSPrime.term()));

		RewriteRule step4 = spec.rule(getNextStepName("Environment"));
		step4.addLHS(fsEnv.term(vActor.term(), vIID.term(), spec.numericTerm(6), vM.term(), vS.term(), vSPrime.term()));
		step4.addLHS(fContains.term(vMMatchedAgain.term(), vSPrime.term()));
		step4.addRHS(fsEnv.term(vActor.term(), vIID.term(), spec.numericTerm(6), vMMatchedAgain.term(), vS.term(), vSPrime.term()));
		step4.addRHS(fContains.term(vMMatchedAgain.term(), vS.term()));

		RewriteRule step5 = spec.rule(getNextStepName("Environment"));
		step5.addLHS(fsEnv.term(vActor.term(), vIID.term(), spec.numericTerm(6), vM.term(), vS.term(), vSPrime.term()));
		step5.addLHS(fContains.term(vMMatchedAgain.term(), vSPrime.term()).negate());
		step5.addRHS(fsEnv.term(vActor.term(), vIID.term(), spec.numericTerm(9), vM.term(), vS.term(), vSPrime.term()));

		AttackState as = spec.attackState("not_removed_and_added_back");
		as.addTerm(fMember.term(vK.term()));
		as.addTerm(fVisited.term(vK.term()));
        as.addTerm(fsEnv.term(vActor.term(), vIID.term(), vSL.term(), vM.term(), vS.term(), vSPrime.term()));

		return spec;
	}
}
