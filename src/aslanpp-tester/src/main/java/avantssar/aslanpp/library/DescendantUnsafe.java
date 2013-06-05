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
import org.avantssar.aslanpp.model.IExpression;
import org.avantssar.aslanpp.model.VariableSymbol;
import org.avantssar.commons.TranslatorOptions;
import avantssar.aslanpp.testing.Specification;

@Specification
public class DescendantUnsafe extends AbstractSpecProvider {

	ConstantSymbol cGFpp;
	ConstantSymbol cFpp;
	ConstantSymbol cCpp;
	FunctionSymbol fTaggedPP;
	FunctionSymbol fOwnerPP;
	BlockStatement envBody;
	Entity grandfather;

	Function fOwner;
	Function fTagged;
	Constant cGF;
	Constant cF;
	Constant cC;

	@Override
	public ASLanPPSpecification getASLanPPSpecification() {
		ASLanPPSpecification spec = startASLanPPSpec();

		cGFpp = env.constants(tppAgent, "gf");
		cGFpp.setNonPublic(true);
		cFpp = env.constants(tppAgent, "f");
		cFpp.setNonPublic(true);
		cCpp = env.constants(tppAgent, "c");
		cCpp.setNonPublic(true);
		fTaggedPP = env.addFunction("tagged", tppFact, tppText, tppText);
		fTaggedPP.setNonPublic(true);
		fTaggedPP.setNonInvertible(true);
		fOwnerPP = env.addFunction("owner", tppFact, tppAgent, tppText);
		fOwnerPP.setNonPublic(true);
		fOwnerPP.setNonInvertible(true);

		grandfather = env.entity("Grandfather");
		grandfather.addParameter(Entity.ACTOR_PREFIX, tppAgent);
		VariableSymbol vGfFather = grandfather.addParameter("Father", tppAgent);
		VariableSymbol vGfChild = grandfather.addParameter("Child", tppAgent);
		VariableSymbol vGfM = grandfather.addStateVariable("GF_M", tppText);

		Entity father = grandfather.entity("Father");
		father.addParameter(Entity.ACTOR_PREFIX, tppAgent);
		VariableSymbol vFChild = father.addParameter("Child", tppAgent);
		VariableSymbol vFM = father.addStateVariable("F_M", tppText);

		Entity child = father.entity("Child");
		child.addParameter(Entity.ACTOR_PREFIX, tppAgent);
		VariableSymbol vCM = child.addStateVariable("C_M", tppText);

		BlockStatement cBody = child.body(child.block());
		cBody.add(child.fresh(vCM.term()));
		cBody.add(child.introduce(fOwnerPP.term(child.getActorSymbol().term(), vCM.term())));
		cBody.add(child.introduce(fTaggedPP.term(vGfM.term(), vCM.term())));
		cBody.add(child.introduce(fTaggedPP.term(vFM.term(), vCM.term())));

		BlockStatement fBody = father.body(father.block());
		fBody.add(father.fresh(vFM.term()));
		fBody.add(father.introduce(fOwnerPP.term(father.getActorSymbol().term(), vFM.term())));
		fBody.add(father.newInstance(child, vFChild.term()));

		BlockStatement gfBody = grandfather.body(grandfather.block());
		gfBody.add(grandfather.fresh(vGfM.term()));
		gfBody.add(grandfather.introduce(fOwnerPP.term(grandfather.getActorSymbol().term(), vGfM.term())));
		gfBody.add(grandfather.newInstance(father, vGfFather.term(), vGfChild.term()));

		envBody = env.body(env.block());
		envBody.add(env.newInstance(grandfather, cGFpp.term(), cFpp.term(), cCpp.term()));
		extendIfNeeded();

		addGoal();

		return spec;
	}

	protected void extendIfNeeded() {}

	protected void addGoal() {
		Goal g = env.goal("no_hierarchy");
		VariableSymbol vM1 = g.addUntypedVariable("M1");
		VariableSymbol vM2 = g.addUntypedVariable("M2");
		VariableSymbol vM3 = g.addUntypedVariable("M3");
		IExpression f = fOwnerPP.term(cGFpp.term(), vM1.term()).expression();
		f = f.and(fOwnerPP.term(cFpp.term(), vM2.term()).expression());
		f = f.and(fOwnerPP.term(cCpp.term(), vM3.term()).expression());
		f = f.and(fTaggedPP.term(vM1.term(), vM3.term()).expression());
		f = f.and(fTaggedPP.term(vM2.term(), vM3.term()).expression());
		g.setFormula(f.exists(vM1, vM2, vM3).not());
	}

	@Override
	public IASLanSpec getExpectedASLanTranslation(TranslatorOptions options) {
		IASLanSpec spec = startASLanSpec();

		Constant cDummyText1 = spec.constant("dummy_text_1", IASLanSpec.TEXT);
		Constant cDummyText2 = spec.constant("dummy_text_2", IASLanSpec.TEXT);
		Constant cDummyText3 = spec.constant("dummy_text_3", IASLanSpec.TEXT);

		Function fsEnv = spec.function(getStateFunctionName("Environment"), IASLanSpec.FACT, IASLanSpec.AGENT, IASLanSpec.NAT, IASLanSpec.NAT);
		Function fsGrandfather = spec.function(getStateFunctionName("Grandfather"), IASLanSpec.FACT, IASLanSpec.AGENT, IASLanSpec.NAT, IASLanSpec.NAT, IASLanSpec.AGENT, IASLanSpec.AGENT,
				IASLanSpec.TEXT);
		Function fsFather = spec.function(getStateFunctionName("Father"), IASLanSpec.FACT, IASLanSpec.AGENT, IASLanSpec.NAT, IASLanSpec.NAT, IASLanSpec.AGENT, IASLanSpec.TEXT);
		Function fsChild = spec.function(getStateFunctionName("Child"), IASLanSpec.FACT, IASLanSpec.AGENT, IASLanSpec.NAT, IASLanSpec.NAT, IASLanSpec.TEXT);

		fOwner = spec.function("owner", IASLanSpec.FACT, IASLanSpec.AGENT, IASLanSpec.TEXT);
		fTagged = spec.function("tagged", IASLanSpec.FACT, IASLanSpec.TEXT, IASLanSpec.TEXT);

		cGF = spec.constant("gf", IASLanSpec.AGENT);
		cF = spec.constant("f", IASLanSpec.AGENT);
		cC = spec.constant("c", IASLanSpec.AGENT);

		Variable vEnvActor = spec.variable("Actor", IASLanSpec.AGENT);
		Variable vEnvIID = spec.variable("IID", IASLanSpec.NAT);
		Variable vEnvSL = spec.variable("SL", IASLanSpec.NAT);
		Variable vGfActor = spec.variable("E_G_Actor", IASLanSpec.AGENT);
		Variable vGfIID = spec.variable("E_G_IID", IASLanSpec.NAT);
		Variable vGfSL = spec.variable("E_G_SL", IASLanSpec.NAT);
		Variable vGfFather = spec.variable("Father", IASLanSpec.AGENT);
		Variable vGfChild = spec.variable("Child", IASLanSpec.AGENT);
		Variable vGfM = spec.variable("GF_M", IASLanSpec.TEXT);
		Variable vGfMfresh = spec.variable("GF_M_1", IASLanSpec.TEXT);
		Variable vFActor = spec.variable("E_G_F_Actor", IASLanSpec.AGENT);
		Variable vFIID = spec.variable("E_G_F_IID", IASLanSpec.NAT);
		Variable vFSL = spec.variable("E_G_F_SL", IASLanSpec.NAT);
		Variable vFChild = spec.variable("E_G_F_Child", IASLanSpec.AGENT);
		Variable vFM = spec.variable("F_M", IASLanSpec.TEXT);
		Variable vFMfresh = spec.variable("F_M_1", IASLanSpec.TEXT);
		Variable vCActor = spec.variable("E_G_F_C_Actor", IASLanSpec.AGENT);
		Variable vCIID = spec.variable("E_G_F_C_IID", IASLanSpec.NAT);
	//  Variable vCSL = spec.variable("E_G_F_C_SL", IASLanSpec.NAT);
		Variable vCM = spec.variable("C_M", IASLanSpec.TEXT);
		Variable vCMfresh = spec.variable("C_M_1", IASLanSpec.TEXT);
		Variable vIID_1 = spec.variable("IID_1", IASLanSpec.NAT);
		Variable vIID_2 = spec.variable("IID_2", IASLanSpec.NAT);
		Variable vIID_3 = spec.variable("IID_3", IASLanSpec.NAT);

		InitialState init = spec.initialState("init");
		prefillInitialState(spec, init);
		// init.addFact(fIsAgent.term(cGF.term()));
		// init.addFact(fIsAgent.term(cF.term()));
		// init.addFact(fIsAgent.term(cC.term()));
		init.addFact(fsEnv.term(cRoot.term(), spec.numericTerm(0), spec.numericTerm(1)));
		// init.addFact(IASLanSpec.IKNOWS.term(cDummyText1.term()));
		// init.addFact(IASLanSpec.IKNOWS.term(cDummyText2.term()));
		// init.addFact(IASLanSpec.IKNOWS.term(cDummyText3.term()));

		RewriteRule step1 = spec.rule(getNextStepName("Environment"));
		step1.addLHS(fsEnv.term(vEnvActor.term(), vEnvIID.term(), spec.numericTerm(1)));
		step1.addRHS(fsEnv.term(vEnvActor.term(), vEnvIID.term(), spec.numericTerm(2)));
		step1.addRHS(fChild.term(vEnvIID.term(), vIID_1.term()));
		step1.addRHS(fsGrandfather.term(cGF.term(), vIID_1.term(), spec.numericTerm(1), cF.term(), cC.term(), cDummyText1.term()));
		step1.addExists(vIID_1);

		RewriteRule step2 = spec.rule(getNextStepName("Grandfather"));
		step2.addLHS(fsGrandfather.term(vGfActor.term(), vGfIID.term(), spec.numericTerm(1), vGfFather.term(), vGfChild.term(), vGfM.term()));
		step2.addRHS(fsGrandfather.term(vGfActor.term(), vGfIID.term(), spec.numericTerm(4), vGfFather.term(), vGfChild.term(), vGfMfresh.term()));
		step2.addRHS(fChild.term(vGfIID.term(), vIID_2.term()));
		step2.addRHS(fsFather.term(vGfFather.term(), vIID_2.term(), spec.numericTerm(1), vGfChild.term(), cDummyText2.term()));
		step2.addRHS(fOwner.term(vGfActor.term(), vGfMfresh.term()));
		step2.addExists(vIID_2);
		step2.addExists(vGfMfresh);
		step2.addLHS(fDishonest.term(vGfActor.term()).negate());

		RewriteRule step3 = spec.rule(getNextStepName("Father"));
		step3.addLHS(fsFather.term(vFActor.term(), vFIID.term(), spec.numericTerm(1), vFChild.term(), vFM.term()));
		step3.addRHS(fsFather.term(vFActor.term(), vFIID.term(), spec.numericTerm(4), vFChild.term(), vFMfresh.term()));
		step3.addRHS(fChild.term(vFIID.term(), vIID_3.term()));
		step3.addRHS(fsChild.term(vFChild.term(), vIID_3.term(), spec.numericTerm(1), cDummyText3.term()));
		step3.addRHS(fOwner.term(vFActor.term(), vFMfresh.term()));
		step3.addExists(vIID_3);
		step3.addExists(vFMfresh);
		step3.addLHS(fDishonest.term(vFActor.term()).negate());

		RewriteRule step4 = spec.rule(getNextStepName("Child"));
		step4.addLHS(fsChild.term(vCActor.term(), vCIID.term(), spec.numericTerm(1), vCM.term()));
		step4.addLHS(fsGrandfather.term(vGfActor.term(), vGfIID.term(), vGfSL.term(), vGfFather.term(), vGfChild.term(), vGfM.term()));
		step4.addLHS(fChild.term(vGfIID.term(), vFIID.term()));
		step4.addRHS(fChild.term(vGfIID.term(), vFIID.term()));
		step4.addLHS(fsFather.term(vFActor.term(), vFIID.term(), vFSL.term(), vFChild.term(), vFM.term()));
		step4.addLHS(fChild.term(vFIID.term(), vCIID.term()));
		step4.addRHS(fChild.term(vFIID.term(), vCIID.term()));
		step4.addRHS(fsChild.term(vCActor.term(), vCIID.term(), spec.numericTerm(5), vCMfresh.term()));
		step4.addRHS(fOwner.term(vCActor.term(), vCMfresh.term()));
		step4.addRHS(fTagged.term(vGfM.term(), vCMfresh.term()));
		step4.addRHS(fsGrandfather.term(vGfActor.term(), vGfIID.term(), vGfSL.term(), vGfFather.term(), vGfChild.term(), vGfM.term()));
		step4.addRHS(fTagged.term(vFM.term(), vCMfresh.term()));
		step4.addRHS(fsFather.term(vFActor.term(), vFIID.term(), vFSL.term(), vFChild.term(), vFM.term()));
		step4.addExists(vCMfresh);
		step4.addLHS(fDishonest.term(vCActor.term()).negate());

		Variable gM1 = spec.variable("M1", IASLanSpec.TEXT);
		Variable gM2 = spec.variable("M2", IASLanSpec.TEXT);
		Variable gM3 = spec.variable("M3", IASLanSpec.TEXT);

		AttackState as = spec.attackState("no_hierarchy");
		as.addTerm(fOwner.term(cGF.term(), gM1.term()));
		as.addTerm(fOwner.term(cF.term(), gM2.term()));
		as.addTerm(fOwner.term(cC.term(), gM3.term()));
		as.addTerm(fTagged.term(gM1.term(), gM3.term()));
		as.addTerm(fTagged.term(gM2.term(), gM3.term()));
        as.addTerm(fsEnv.term(vEnvActor.term(), vEnvIID.term(), vEnvSL.term()));

		return spec;
	}
}
