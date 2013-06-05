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

import org.avantssar.aslan.IASLanSpec;
import org.avantssar.aslan.InitialState;
import org.avantssar.aslan.RewriteRule;
import org.avantssar.aslanpp.model.ASLanPPSpecification;
import org.avantssar.aslanpp.model.BlockStatement;
import org.avantssar.aslanpp.model.ConstantSymbol;
import org.avantssar.aslanpp.model.Entity;
import org.avantssar.aslanpp.model.FunctionSymbol;
import org.avantssar.aslanpp.model.Goal;
import org.avantssar.aslanpp.model.ITerm;
import org.avantssar.aslanpp.model.MacroSymbol;
import org.avantssar.aslanpp.model.VariableSymbol;
import org.avantssar.commons.TranslatorOptions;
import avantssar.aslan.backends.Verdict;
import avantssar.aslanpp.testing.Specification;

@Specification(expectedVerdict = Verdict.NoAttack)
public class MacrosWithoutAttack extends AbstractSpecProvider {

	protected org.avantssar.aslan.Constant cDummyMessage0;
	protected org.avantssar.aslan.Constant cDummyMessage1;

	org.avantssar.aslan.Variable vEnvActor;

	@Override
	public ASLanPPSpecification getASLanPPSpecification() {
		ASLanPPSpecification spec = super.startASLanPPSpec();

		MacroSymbol mExpand = env.addMacro(null, "expand");
		VariableSymbol vExpandM = mExpand.addArgument("M");
		mExpand.setBody(fppPair.term(vExpandM.term(), vExpandM.term()));

		MacroSymbol mDoubleExpand = env.addMacro(null, "doubleExpand");
		VariableSymbol vDoubleExpandM = mDoubleExpand.addArgument("M");
		mDoubleExpand.setBody(mExpand.term(mExpand.term(vDoubleExpandM.term())));

		FunctionSymbol fMark = env.addFunction("mark", tppFact, tppMessage, tppAgent);
		fMark.setNonInvertible(true);
		fMark.setNonPublic(true);
		ConstantSymbol cToken = env.constants(tppMessage, "token");
		cToken.setNonPublic(true);
		VariableSymbol vEnvE = env.addStateVariable("E", tppMessage);

		addASLanPPActor();

		Entity inner = env.entity("Inner");
		inner.addParameter(Entity.ACTOR_PREFIX, tppAgent);

		MacroSymbol mInnerExpand = inner.addMacro(null, "expand");
		VariableSymbol vInnerExpandM = mInnerExpand.addArgument("M");
		mInnerExpand.setBody(mDoubleExpand.term(mDoubleExpand.term(vInnerExpandM.term())));

		VariableSymbol vInnerE = inner.addStateVariable("E", tppMessage);
		BlockStatement innerBody = inner.body(inner.block());
		innerBody.add(inner.assign(vInnerE.term(), mInnerExpand.term(cToken.term())));
		innerBody.add(inner.introduce(fMark.term(vInnerE.term(), inner.getActorSymbol().term())));

		BlockStatement envBody = env.body(env.block());
		envBody.add(env.assign(vEnvE.term(), mDoubleExpand.term(cToken.term())));
		envBody.add(env.assign(vEnvE.term(), mExpand.term(vEnvE.term())));
		envBody.add(env.assign(vEnvE.term(), mExpand.term(vEnvE.term())));
		envBody.add(env.introduce(fMark.term(vEnvE.term(), env.getActorSymbol().term())));
		envBody.add(env.newInstance(inner, getASLanPPActor()));

		Goal g = env.goal("no_double_marking");
		VariableSymbol gA1 = g.addUntypedVariable("Ag1");
		VariableSymbol gA2 = g.addUntypedVariable("Ag2");
		VariableSymbol gE1 = g.addUntypedVariable("E1");
		VariableSymbol gE2 = g.addUntypedVariable("E2");
		VariableSymbol gM = g.addUntypedVariable("Msg");
		g.setFormula(gA1.term().equality(gA2.term()).not().and(gE1.term().equality(mDoubleExpand.term(mDoubleExpand.term(gM.term())))).and(
				gE2.term().equality(mDoubleExpand.term(mDoubleExpand.term(gM.term())))).and(fMark.term(gE1.term(), gA1.term()).expression()).and(fMark.term(gE2.term(), gA2.term()).expression())
				.exists(gA1, gA2, gE1, gE2, gM).not());

		return spec;
	}

	@Override
	public IASLanSpec getExpectedASLanTranslation(TranslatorOptions options) {
		IASLanSpec spec = startASLanSpec();

		cDummyMessage0 = spec.constant("dummy_message", IASLanSpec.MESSAGE);
		cDummyMessage1 = spec.constant("dummy_message_1", IASLanSpec.MESSAGE);

		org.avantssar.aslan.Function fsEnv = spec.function(getStateFunctionName("Environment"), IASLanSpec.FACT, IASLanSpec.AGENT, IASLanSpec.NAT, IASLanSpec.NAT, IASLanSpec.MESSAGE);
		org.avantssar.aslan.Function fsInner = spec.function(getStateFunctionName("Inner"), IASLanSpec.FACT, IASLanSpec.AGENT, IASLanSpec.NAT, IASLanSpec.NAT, IASLanSpec.MESSAGE);
		org.avantssar.aslan.Function fMark = spec.function("mark", IASLanSpec.FACT, IASLanSpec.MESSAGE, IASLanSpec.AGENT);

		org.avantssar.aslan.Constant cToken = spec.constant("token", IASLanSpec.MESSAGE);
		vEnvActor = spec.variable("Actor", IASLanSpec.AGENT);
		org.avantssar.aslan.Variable vEnvID = spec.variable("IID", IASLanSpec.NAT);
		org.avantssar.aslan.Variable vEnvSL = spec.variable("SL", IASLanSpec.NAT);
		org.avantssar.aslan.Variable vEnvE = spec.variable("E", IASLanSpec.MESSAGE);
		org.avantssar.aslan.Variable vEnvE1 = spec.variable("E1", IASLanSpec.MESSAGE);
		org.avantssar.aslan.Variable vEnvE2 = spec.variable("E2", IASLanSpec.MESSAGE);
		org.avantssar.aslan.Variable vEnvAg1 = spec.variable("Ag1", IASLanSpec.AGENT);
		org.avantssar.aslan.Variable vEnvAg2 = spec.variable("Ag2", IASLanSpec.AGENT);
		org.avantssar.aslan.Variable vInnerActor = spec.variable("E_I_Actor", IASLanSpec.AGENT);
		org.avantssar.aslan.Variable vInnerID = spec.variable("E_I_IID", IASLanSpec.NAT);
	//	org.avantssar.aslan.Variable vInnerSL = spec.variable("E_I_SL", IASLanSpec.NAT);
		org.avantssar.aslan.Variable vInnerE = spec.variable("E_I_E", IASLanSpec.MESSAGE);
		org.avantssar.aslan.Variable vEnvMsg = spec.variable("Msg", IASLanSpec.MESSAGE);
		org.avantssar.aslan.Variable vEnvIID_1 = spec.variable("IID_1", IASLanSpec.NAT);

		org.avantssar.aslan.InitialState init = spec.initialState("init");
		prefillInitialState(spec, init);
		init.addFact(fsEnv.term(cRoot.term(), spec.numericTerm(0), spec.numericTerm(1), cDummyMessage0.term()));
		// init.addFact(IASLanSpec.IKNOWS.term(cDummyMessage1.term()));
		addASLanActor(spec, init);

		RewriteRule step1 = spec.rule(getNextStepName("Environment"));
		step1.addLHS(fsEnv.term(vEnvActor.term(), vEnvID.term(), spec.numericTerm(1), vEnvE.term()));
		step1.addRHS(fsEnv.term(vEnvActor.term(), vEnvID.term(), spec.numericTerm(6), buildPairs(cToken.term())));
		step1.addRHS(fMark.term(buildPairs(cToken.term()), vEnvActor.term()));
		step1.addRHS(fsInner.term(getASLanActor(), vEnvIID_1.term(), spec.numericTerm(1), cDummyMessage1.term()));
		step1.addRHS(fChild.term(vEnvID.term(), vEnvIID_1.term()));
		step1.addExists(vEnvIID_1);

		RewriteRule step2 = spec.rule(getNextStepName("Inner"));
		step2.addLHS(fsInner.term(vInnerActor.term(), vInnerID.term(), spec.numericTerm(1), vInnerE.term()));
		step2.addRHS(fsInner.term(vInnerActor.term(), vInnerID.term(), spec.numericTerm(3), buildPairs(cToken.term())));
		step2.addRHS(fMark.term(buildPairs(cToken.term()), vInnerActor.term()));
		step2.addLHS(fDishonest.term(vInnerActor.term()).negate());

		org.avantssar.aslan.AttackState g = spec.attackState("no_double_marking");
		g.addTerm(IASLanSpec.EQUAL.term(vEnvE1.term(), buildPairs(vEnvMsg.term())));
		g.addTerm(IASLanSpec.EQUAL.term(vEnvE2.term(), buildPairs(vEnvMsg.term())));
		g.addTerm(fMark.term(vEnvE1.term(), vEnvAg1.term()));
		g.addTerm(fMark.term(vEnvE2.term(), vEnvAg2.term()));
		g.addTerm(IASLanSpec.EQUAL.term(vEnvAg1.term(), vEnvAg2.term()).negate());
        g.addTerm(fsEnv.term(vEnvActor.term(), vEnvID.term(), vEnvSL.term(), vEnvE.term()));

		return spec;
	}

	protected void addASLanPPActor() {
	// do nothing here
	}

	protected ITerm getASLanPPActor() {
		return env.getActorSymbol().term();
	}

	protected void addASLanActor(IASLanSpec spec, InitialState init) {
	// do nothing here
	}

	protected org.avantssar.aslan.ITerm getASLanActor() {
		return vEnvActor.term();
	}

	private org.avantssar.aslan.ITerm buildPairs(org.avantssar.aslan.ITerm t) {
		org.avantssar.aslan.ITerm result = buildPair(t);
		result = buildPair(result);
		result = buildPair(result);
		result = buildPair(result);
		return result;
	}

	private org.avantssar.aslan.ITerm buildPair(org.avantssar.aslan.ITerm t) {
		return IASLanSpec.PAIR.term(t, t);
	}

}
