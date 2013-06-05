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
import org.avantssar.aslanpp.model.BranchStatement;
import org.avantssar.aslanpp.model.ConstantSymbol;
import org.avantssar.aslanpp.model.Entity;
import org.avantssar.aslanpp.model.FunctionSymbol;
import org.avantssar.aslanpp.model.Goal;
import org.avantssar.aslanpp.model.Prelude;
import org.avantssar.aslanpp.model.SimpleType;
import org.avantssar.aslanpp.model.SymbolicInstanceStatement;
import org.avantssar.aslanpp.model.VariableSymbol;
import org.avantssar.commons.TranslatorOptions;
import avantssar.aslanpp.testing.Specification;

@Specification
public class SymbolicInstances extends AbstractChannelledSpecProvider {

	@Override
	public ASLanPPSpecification getASLanPPSpecification() {
		ASLanPPSpecification spec = startASLanPPSpec();

		SimpleType color = env.type("color", env.findType(Prelude.MESSAGE));
		ConstantSymbol cAlice = env.constants(env.findType(Prelude.AGENT), "alice");
		// cAlice.setNonPublic(true);
		ConstantSymbol cMary = env.constants(env.findType(Prelude.AGENT), "mary");
		// cMary.setNonPublic(true);
		ConstantSymbol cMelinda = env.constants(env.findType(Prelude.AGENT), "melinda");
		// cMelinda.setNonPublic(true);
		ConstantSymbol cLaura = env.constants(env.findType(Prelude.AGENT), "laura");
		// cLaura.setNonPublic(true);
		env.group(cAlice, cMary, cMelinda, cLaura);
		ConstantSymbol cBob = env.constants(env.findType(Prelude.AGENT), "bob");
		// cBob.setNonPublic(true);
		ConstantSymbol cCooper = env.constants(env.findType(Prelude.AGENT), "cooper");
		// cCooper.setNonPublic(true);
		ConstantSymbol cSamuel = env.constants(env.findType(Prelude.AGENT), "samuel");
		// cSamuel.setNonPublic(true);
		ConstantSymbol cPeter = env.constants(env.findType(Prelude.AGENT), "peter");
		// cPeter.setNonPublic(true);
		env.group(cBob, cCooper, cSamuel, cPeter);
		ConstantSymbol cBlue = env.constants(color, "blue");
		cBlue.setNonPublic(true);
		ConstantSymbol cYellow = env.constants(color, "yellow");
		cYellow.setNonPublic(true);
		ConstantSymbol cOrange = env.constants(color, "orange");
		cOrange.setNonPublic(true);
		ConstantSymbol cGreen = env.constants(color, "green");
		cGreen.setNonPublic(true);
		ConstantSymbol cBlack = env.constants(color, "black");
		cBlack.setNonPublic(true);
		ConstantSymbol cWhite = env.constants(color, "white");
		cWhite.setNonPublic(true);
		ConstantSymbol cCyan = env.constants(color, "cyan");
		cCyan.setNonPublic(true);
		env.group(cBlue, cYellow, cOrange, cGreen, cBlack, cWhite, cCyan);
		FunctionSymbol fFemale = env.addFunction("female", env.findType(Prelude.FACT), env.findType(Prelude.AGENT));
		fFemale.setNonInvertible(true);
		fFemale.setNonPublic(true);
		FunctionSymbol fMale = env.addFunction("male", env.findType(Prelude.FACT), env.findType(Prelude.AGENT));
		fMale.setNonInvertible(true);
		fMale.setNonPublic(true);
		FunctionSymbol fLikes = env.addFunction("likes", env.findType(Prelude.FACT), env.findType(Prelude.AGENT), color);
		fLikes.setNonInvertible(true);
		fLikes.setNonPublic(true);
		FunctionSymbol fMatch = env.addFunction("match", env.findType(Prelude.FACT), env.findType(Prelude.AGENT), env.findType(Prelude.AGENT));
		fMatch.setNonInvertible(true);
		fMatch.setNonPublic(true);

		Entity session = env.entity("Session");
		VariableSymbol vSessionF = session.addParameter("F", env.findType(Prelude.AGENT));
		VariableSymbol vSessionM = session.addParameter("M", env.findType(Prelude.AGENT));
		session.group(vSessionF, vSessionM);

		Entity female = session.entity("Female");
		female.addParameter(Entity.ACTOR_PREFIX, env.findType(Prelude.AGENT));
		VariableSymbol vFemaleM = female.addParameter("M", env.findType(Prelude.AGENT));
		female.group(female.getActorSymbol(), vFemaleM);
		VariableSymbol vFemaleC = female.addStateVariable("C", color);
		BlockStatement femaleBody = female.body(female.block());
		BranchStatement filterMale = femaleBody.add(female.branch(fMale.term(vFemaleM.term()).expression()));
		BranchStatement filterFemaleColor = filterMale.branchTrue(female.branch(fLikes.oopTerm(female.getActorSymbol().term(), vFemaleC.matchedTerm()).expression()));
		filterFemaleColor.branchTrue(female.comm(female.getActorSymbol().term(), vFemaleM.term(), vFemaleC.term(), null, channelType, false, false, false));

		Entity male = session.entity("Male");
		VariableSymbol vMaleF = male.addParameter("F", env.findType(Prelude.AGENT));
		male.addParameter(Entity.ACTOR_PREFIX, env.findType(Prelude.AGENT));
		male.group(vMaleF, male.getActorSymbol());
		VariableSymbol vMaleOwnC = male.addStateVariable("OwnC", color);
		VariableSymbol vMaleOtherC = male.addStateVariable("OtherC", color);
		BlockStatement maleBody = male.body(male.block());
		BranchStatement filterFemale = maleBody.add(male.branch(fFemale.term(vMaleF.term()).expression()));
		BlockStatement filterFemaleTrue = filterFemale.branchTrue(male.block());
		filterFemaleTrue.add(male.comm(vMaleF.term(), male.getActorSymbol().term(), vMaleOtherC.matchedTerm(), null, channelType, true, false, false));
		BranchStatement filterMaleColor = filterFemaleTrue.add(male.branch(fLikes.term(male.getActorSymbol().term(), vMaleOwnC.matchedTerm()).expression()));
		BranchStatement filterSameColor = filterMaleColor.branchTrue(male.branch(vMaleOwnC.term().equality(vMaleOtherC.term())));
		filterSameColor.branchTrue(male.introduce(fMatch.term(male.getActorSymbol().term(), vMaleF.term())));

		BlockStatement sessionBody = session.body(session.block());
		sessionBody.add(session.newInstance(female, vSessionF.term(), vSessionM.term()));
		sessionBody.add(session.newInstance(male, vSessionF.term(), vSessionM.term()));

		BlockStatement envBody = env.body(env.block());
		envBody.add(env.introduce(fFemale.term(cAlice.term())));
		envBody.add(env.introduce(fLikes.term(cAlice.term(), cBlue.term())));
		envBody.add(env.introduce(fFemale.term(cMary.term())));
		envBody.add(env.introduce(fLikes.term(cMary.term(), cYellow.term())));
		envBody.add(env.introduce(fFemale.term(cMelinda.term())));
		envBody.add(env.introduce(fLikes.term(cMelinda.term(), cOrange.term())));
		envBody.add(env.introduce(fFemale.term(cLaura.term())));
		envBody.add(env.introduce(fLikes.term(cLaura.term(), cGreen.term())));
		envBody.add(env.introduce(fMale.term(cBob.term())));
		envBody.add(env.introduce(fLikes.term(cBob.term(), cBlack.term())));
		envBody.add(env.introduce(fMale.term(cCooper.term())));
		envBody.add(env.introduce(fLikes.term(cCooper.term(), cWhite.term())));
		envBody.add(env.introduce(fMale.term(cSamuel.term())));
		envBody.add(env.introduce(fLikes.term(cSamuel.term(), cCyan.term())));
		envBody.add(env.introduce(fMale.term(cPeter.term())));
		envBody.add(env.introduce(fLikes.term(cPeter.term(), cOrange.term())));
		SymbolicInstanceStatement symSession = envBody.add(env.symbolicInstance(session));
		VariableSymbol symF = symSession.any("A");
		VariableSymbol symM = symSession.any("B");
		symSession.setArgs(symF.term(), symM.term());
		symSession.setGuard(fFemale.term(symF.term()).expression().and(fMale.term(symM.term()).expression()));

		Goal gNoMatch = env.goal("no_match");
		VariableSymbol gF = gNoMatch.addUntypedVariable("F");
		VariableSymbol gM = gNoMatch.addUntypedVariable("M");
		gNoMatch.setFormula(fMatch.term(gM.term(), gF.term()).expression().exists(gF, gM).not());
		return spec;
	}

	@Override
	public IASLanSpec getExpectedASLanTranslation(TranslatorOptions options) {
		IASLanSpec spec = startASLanSpec();

		Constant cDummyAgent = spec.constant("dummy_agent_1", IASLanSpec.AGENT);

		org.avantssar.aslan.PrimitiveType tColor = spec.primitiveType("color");
		tColor.setSuperType(IASLanSpec.MESSAGE);
		Constant cDummyColor1 = spec.constant("dummy_color_1", tColor);
		Constant cDummyColor2 = spec.constant("dummy_color_2", tColor);
		Constant cDummyColor3 = spec.constant("dummy_color_3", tColor);

		Function fsEnv = spec.function(getStateFunctionName("Environment"), IASLanSpec.FACT, IASLanSpec.AGENT, IASLanSpec.NAT, IASLanSpec.NAT);
		Function fsSession = spec.function(getStateFunctionName("Session"), IASLanSpec.FACT, IASLanSpec.AGENT, IASLanSpec.NAT, IASLanSpec.NAT, IASLanSpec.AGENT, IASLanSpec.AGENT);
		Function fsFemale = spec.function(getStateFunctionName("Female"), IASLanSpec.FACT, IASLanSpec.AGENT, IASLanSpec.NAT, IASLanSpec.NAT, IASLanSpec.AGENT, tColor);
		Function fsMale = spec.function(getStateFunctionName("Male"), IASLanSpec.FACT, IASLanSpec.AGENT, IASLanSpec.NAT, IASLanSpec.NAT, IASLanSpec.AGENT, tColor, tColor);
		Function fMale = spec.function("male", IASLanSpec.FACT, IASLanSpec.AGENT);
		Function fFemale = spec.function("female", IASLanSpec.FACT, IASLanSpec.AGENT);
		Function fLikes = spec.function("likes", IASLanSpec.FACT, IASLanSpec.AGENT, tColor);
		Function fMatch = spec.function("match", IASLanSpec.FACT, IASLanSpec.AGENT, IASLanSpec.AGENT);
		Constant cAlice = spec.constant("alice", IASLanSpec.AGENT);
		Constant cMary = spec.constant("mary", IASLanSpec.AGENT);
		Constant cMelinda = spec.constant("melinda", IASLanSpec.AGENT);
		Constant cLaura = spec.constant("laura", IASLanSpec.AGENT);
		Constant cBob = spec.constant("bob", IASLanSpec.AGENT);
		Constant cCooper = spec.constant("cooper", IASLanSpec.AGENT);
		Constant cSamuel = spec.constant("samuel", IASLanSpec.AGENT);
		Constant cPeter = spec.constant("peter", IASLanSpec.AGENT);
		Constant cBlue = spec.constant("blue", tColor);
		Constant cYellow = spec.constant("yellow", tColor);
		Constant cOrange = spec.constant("orange", tColor);
		Constant cGreen = spec.constant("green", tColor);
		Constant cBlack = spec.constant("black", tColor);
		Constant cWhite = spec.constant("white", tColor);
		Constant cCyan = spec.constant("cyan", tColor);
		Variable vEnvActor = spec.variable("Actor", IASLanSpec.AGENT);
		Variable vEnvIID = spec.variable("IID", IASLanSpec.NAT);
		Variable vEnvSL = spec.variable("SL", IASLanSpec.NAT);
		Variable vEnvA = spec.variable("A", IASLanSpec.AGENT);
		Variable vEnvB = spec.variable("B", IASLanSpec.AGENT);
		Variable vEnvIID_1 = spec.variable("IID_1", IASLanSpec.NAT);
		Variable vSessionActor = spec.variable("E_S_Actor", IASLanSpec.AGENT);
		Variable vSessionIID = spec.variable("E_S_IID", IASLanSpec.NAT);
	//	Variable vSessionSL = spec.variable("E_S_SL", IASLanSpec.NAT);
		Variable vSessionF = spec.variable("F", IASLanSpec.AGENT);
		Variable vSessionM = spec.variable("M", IASLanSpec.AGENT);
		Variable vSessionIID_2 = spec.variable("IID_2", IASLanSpec.NAT);
		Variable vSessionIID_3 = spec.variable("IID_3", IASLanSpec.NAT);
		Variable vFemaleActor = spec.variable("E_S_F_Actor", IASLanSpec.AGENT);
		Variable vFemaleIID = spec.variable("E_S_F_IID", IASLanSpec.NAT);
	//	Variable vFemaleSL = spec.variable("E_S_F_SL", IASLanSpec.NAT);
		Variable vFemaleM = spec.variable("E_S_F_M", IASLanSpec.AGENT);
		Variable vFemaleC = spec.variable("C", tColor);
		Variable vFemaleCMatched = spec.variable("C_1", tColor);
		Variable vMaleActor = spec.variable("E_S_M_Actor", IASLanSpec.AGENT);
		Variable vMaleIID = spec.variable("E_S_M_IID", IASLanSpec.NAT);
	//	Variable vMaleSL = spec.variable("E_S_M_SL", IASLanSpec.NAT);
		Variable vMaleF = spec.variable("E_S_M_F", IASLanSpec.AGENT);
		Variable vMaleOwnC = spec.variable("OwnC", tColor);
		Variable vMaleOwnCMatched = spec.variable("OwnC_1", tColor);
		Variable vMaleOtherC = spec.variable("OtherC", tColor);
		Variable vMaleOtherCMatched = spec.variable("OtherC_1", tColor);
		Variable vGoalF = spec.variable("E_nm_F", IASLanSpec.AGENT);
		Variable vGoalM = spec.variable("E_nm_M", IASLanSpec.AGENT);
		Constant cSymA = spec.constant("symbolic_A", IASLanSpec.AGENT);
		Constant cSymB = spec.constant("symbolic_B", IASLanSpec.AGENT);

		InitialState init = spec.initialState("init");
		prefillInitialState(spec, init);
		init.addFact(fsEnv.term(cRoot.term(), spec.numericTerm(0), spec.numericTerm(1)));
		init.addFact(IASLanSpec.IKNOWS.term(cAlice.term()));
		init.addFact(IASLanSpec.IKNOWS.term(cMary.term()));
		init.addFact(IASLanSpec.IKNOWS.term(cMelinda.term()));
		init.addFact(IASLanSpec.IKNOWS.term(cLaura.term()));
		init.addFact(IASLanSpec.IKNOWS.term(cBob.term()));
		init.addFact(IASLanSpec.IKNOWS.term(cCooper.term()));
		init.addFact(IASLanSpec.IKNOWS.term(cSamuel.term()));
		init.addFact(IASLanSpec.IKNOWS.term(cPeter.term()));
		init.addFact(IASLanSpec.IKNOWS.term(cSymA.term()));
		init.addFact(IASLanSpec.IKNOWS.term(cSymB.term()));
		// init.addFact(IASLanSpec.IKNOWS.term(cDummyAgent.term()));
		// init.addFact(IASLanSpec.IKNOWS.term(cDummyColor1.term()));
		// init.addFact(IASLanSpec.IKNOWS.term(cDummyColor2.term()));
		// init.addFact(IASLanSpec.IKNOWS.term(cDummyColor3.term()));

		RewriteRule step1 = spec.rule(getNextStepName("Environment"));
		step1.addLHS(fsEnv.term(vEnvActor.term(), vEnvIID.term(), spec.numericTerm(1)));
		step1.addRHS(fsEnv.term(vEnvActor.term(), vEnvIID.term(), spec.numericTerm(17)));
		step1.addRHS(fFemale.term(cAlice.term()));
		step1.addRHS(fLikes.term(cAlice.term(), cBlue.term()));
		step1.addRHS(fFemale.term(cMary.term()));
		step1.addRHS(fLikes.term(cMary.term(), cYellow.term()));
		step1.addRHS(fFemale.term(cMelinda.term()));
		step1.addRHS(fLikes.term(cMelinda.term(), cOrange.term()));
		step1.addRHS(fFemale.term(cLaura.term()));
		step1.addRHS(fLikes.term(cLaura.term(), cGreen.term()));
		step1.addRHS(fMale.term(cBob.term()));
		step1.addRHS(fLikes.term(cBob.term(), cBlack.term()));
		step1.addRHS(fMale.term(cCooper.term()));
		step1.addRHS(fLikes.term(cCooper.term(), cWhite.term()));
		step1.addRHS(fMale.term(cSamuel.term()));
		step1.addRHS(fLikes.term(cSamuel.term(), cCyan.term()));
		step1.addRHS(fMale.term(cPeter.term()));
		step1.addRHS(fLikes.term(cPeter.term(), cOrange.term()));

		RewriteRule step2 = spec.rule(getNextStepName("Environment"));
		step2.addLHS(fsEnv.term(vEnvActor.term(), vEnvIID.term(), spec.numericTerm(17)));
		step2.addLHS(fFemale.term(vEnvA.term()));
		step2.addLHS(fMale.term(vEnvB.term()));
		step2.addLHS(IASLanSpec.IKNOWS.term(vEnvA.term()));
		step2.addLHS(IASLanSpec.IKNOWS.term(vEnvB.term()));
		step2.addRHS(fsEnv.term(vEnvActor.term(), vEnvIID.term(), spec.numericTerm(18)));
		step2.addRHS(fFemale.term(vEnvA.term()));
		step2.addRHS(fMale.term(vEnvB.term()));
		step2.addRHS(IASLanSpec.IKNOWS.term(vEnvA.term()));
		step2.addRHS(IASLanSpec.IKNOWS.term(vEnvB.term()));
		step2.addRHS(fChild.term(vEnvIID.term(), vEnvIID_1.term()));
		step2.addRHS(fsSession.term(cDummyAgent.term(), vEnvIID_1.term(), spec.numericTerm(1), vEnvA.term(), vEnvB.term()));
		step2.addExists(vEnvIID_1);

		RewriteRule step3 = spec.rule(getNextStepName("Session"));
		step3.addLHS(fsSession.term(vSessionActor.term(), vSessionIID.term(), spec.numericTerm(1), vSessionF.term(), vSessionM.term()));
		step3.addRHS(fsSession.term(vSessionActor.term(), vSessionIID.term(), spec.numericTerm(3), vSessionF.term(), vSessionM.term()));
		step3.addRHS(fChild.term(vSessionIID.term(), vSessionIID_2.term()));
		step3.addRHS(fChild.term(vSessionIID.term(), vSessionIID_3.term()));
		step3.addRHS(fsFemale.term(vSessionF.term(), vSessionIID_2.term(), spec.numericTerm(1), vSessionM.term(), cDummyColor1.term()));
		step3.addRHS(fsMale.term(vSessionM.term(), vSessionIID_3.term(), spec.numericTerm(1), vSessionF.term(), cDummyColor2.term(), cDummyColor3.term()));
		step3.addExists(vSessionIID_2);
		step3.addExists(vSessionIID_3);
		step3.addLHS(fDishonest.term(vSessionActor.term()).negate());

		RewriteRule step4 = spec.rule(getNextStepName("Female"));
		step4.addLHS(fsFemale.term(vFemaleActor.term(), vFemaleIID.term(), spec.numericTerm(1), vFemaleM.term(), vFemaleC.term()));
		step4.addLHS(fMale.term(vFemaleM.term()));
		step4.addRHS(fsFemale.term(vFemaleActor.term(), vFemaleIID.term(), spec.numericTerm(2), vFemaleM.term(), vFemaleC.term()));
		step4.addRHS(fMale.term(vFemaleM.term()));
		step4.addLHS(fDishonest.term(vFemaleActor.term()).negate());

		RewriteRule step5 = spec.rule(getNextStepName("Female"));
		step5.addLHS(fsFemale.term(vFemaleActor.term(), vFemaleIID.term(), spec.numericTerm(2), vFemaleM.term(), vFemaleC.term()));
		step5.addLHS(fLikes.term(vFemaleActor.term(), vFemaleCMatched.term()));
		step5.addRHS(fsFemale.term(vFemaleActor.term(), vFemaleIID.term(), spec.numericTerm(4), vFemaleM.term(), vFemaleCMatched.term()));
		step5.addRHS(fLikes.term(vFemaleActor.term(), vFemaleCMatched.term()));
		step5.addRHS(doSend(cm, vFemaleActor.term(), vFemaleActor.term(), vFemaleM.term(), vFemaleCMatched.term()));

		RewriteRule step6 = spec.rule(getNextStepName("Female"));
		step6.addLHS(fsFemale.term(vFemaleActor.term(), vFemaleIID.term(), spec.numericTerm(2), vFemaleM.term(), vFemaleC.term()));
		step6.addLHS(fLikes.term(vFemaleActor.term(), vFemaleCMatched.term()).negate());
		step6.addRHS(fsFemale.term(vFemaleActor.term(), vFemaleIID.term(), spec.numericTerm(4), vFemaleM.term(), vFemaleC.term()));

		RewriteRule step7 = spec.rule(getNextStepName("Female"));
		step7.addLHS(fsFemale.term(vFemaleActor.term(), vFemaleIID.term(), spec.numericTerm(1), vFemaleM.term(), vFemaleC.term()));
		step7.addLHS(fMale.term(vFemaleM.term()).negate());
		step7.addRHS(fsFemale.term(vFemaleActor.term(), vFemaleIID.term(), spec.numericTerm(4), vFemaleM.term(), vFemaleC.term()));
		step7.addLHS(fDishonest.term(vFemaleActor.term()).negate());

		RewriteRule step8 = spec.rule(getNextStepName("Male"));
		step8.addLHS(fsMale.term(vMaleActor.term(), vMaleIID.term(), spec.numericTerm(1), vMaleF.term(), vMaleOwnC.term(), vMaleOtherC.term()));
		step8.addLHS(fFemale.term(vMaleF.term()));
		step8.addRHS(fsMale.term(vMaleActor.term(), vMaleIID.term(), spec.numericTerm(2), vMaleF.term(), vMaleOwnC.term(), vMaleOtherC.term()));
		step8.addRHS(fFemale.term(vMaleF.term()));
		step8.addLHS(fDishonest.term(vMaleActor.term()).negate());

		RewriteRule step9 = spec.rule(getNextStepName("Male"));
		step9.addLHS(fsMale.term(vMaleActor.term(), vMaleIID.term(), spec.numericTerm(2), vMaleF.term(), vMaleOwnC.term(), vMaleOtherC.term()));
		step9.addLHS(doReceive(cm, vMaleActor.term(), vMaleF.term(), vMaleF.term(), vMaleOtherCMatched.term()));
		step9.addRHS(fsMale.term(vMaleActor.term(), vMaleIID.term(), spec.numericTerm(3), vMaleF.term(), vMaleOwnC.term(), vMaleOtherCMatched.term()));

		RewriteRule step10 = spec.rule(getNextStepName("Male"));
		step10.addLHS(fsMale.term(vMaleActor.term(), vMaleIID.term(), spec.numericTerm(3), vMaleF.term(), vMaleOwnC.term(), vMaleOtherC.term()));
		step10.addLHS(fLikes.term(vMaleActor.term(), vMaleOwnCMatched.term()));
		step10.addRHS(fsMale.term(vMaleActor.term(), vMaleIID.term(), spec.numericTerm(4), vMaleF.term(), vMaleOwnCMatched.term(), vMaleOtherC.term()));
		step10.addRHS(fLikes.term(vMaleActor.term(), vMaleOwnCMatched.term()));

		RewriteRule step11 = spec.rule(getNextStepName("Male"));
		step11.addLHS(fsMale.term(vMaleActor.term(), vMaleIID.term(), spec.numericTerm(4), vMaleF.term(), vMaleOwnC.term(), vMaleOtherC.term()));
		step11.addLHS(IASLanSpec.EQUAL.term(vMaleOwnC.term(), vMaleOtherC.term()));
		step11.addRHS(fsMale.term(vMaleActor.term(), vMaleIID.term(), spec.numericTerm(6), vMaleF.term(), vMaleOwnC.term(), vMaleOtherC.term()));
		step11.addRHS(fMatch.term(vMaleActor.term(), vMaleF.term()));

		RewriteRule step12 = spec.rule(getNextStepName("Male"));
		step12.addLHS(fsMale.term(vMaleActor.term(), vMaleIID.term(), spec.numericTerm(4), vMaleF.term(), vMaleOwnC.term(), vMaleOtherC.term()));
		step12.addLHS(IASLanSpec.EQUAL.term(vMaleOwnC.term(), vMaleOtherC.term()).negate());
		step12.addRHS(fsMale.term(vMaleActor.term(), vMaleIID.term(), spec.numericTerm(6), vMaleF.term(), vMaleOwnC.term(), vMaleOtherC.term()));

		RewriteRule step13 = spec.rule(getNextStepName("Male"));
		step13.addLHS(fsMale.term(vMaleActor.term(), vMaleIID.term(), spec.numericTerm(3), vMaleF.term(), vMaleOwnC.term(), vMaleOtherC.term()));
		step13.addLHS(fLikes.term(vMaleActor.term(), vMaleOwnCMatched.term()).negate());
		step13.addRHS(fsMale.term(vMaleActor.term(), vMaleIID.term(), spec.numericTerm(6), vMaleF.term(), vMaleOwnC.term(), vMaleOtherC.term()));

		RewriteRule step14 = spec.rule(getNextStepName("Male"));
		step14.addLHS(fsMale.term(vMaleActor.term(), vMaleIID.term(), spec.numericTerm(1), vMaleF.term(), vMaleOwnC.term(), vMaleOtherC.term()));
		step14.addLHS(fFemale.term(vMaleF.term()).negate());
		step14.addRHS(fsMale.term(vMaleActor.term(), vMaleIID.term(), spec.numericTerm(6), vMaleF.term(), vMaleOwnC.term(), vMaleOtherC.term()));
		step14.addLHS(fDishonest.term(vMaleActor.term()).negate());

		AttackState as = spec.attackState("no_match");
		as.addTerm(fMatch.term(vGoalM.term(), vGoalF.term()));
        as.addTerm(fsEnv.term(vEnvActor.term(), vEnvIID.term(), vEnvSL.term()));

		return spec;
	}

	@Override
	protected boolean hasSymbolicInstances() {
		return true;
	}

}
