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
import org.avantssar.aslanpp.model.ASLanPPSpecification;
import org.avantssar.aslanpp.model.BlockStatement;
import org.avantssar.aslanpp.model.ConstantSymbol;
import org.avantssar.aslanpp.model.FunctionSymbol;
import org.avantssar.aslanpp.model.Goal;
import org.avantssar.aslanpp.model.HornClause;
import org.avantssar.aslanpp.model.MacroSymbol;
import org.avantssar.aslanpp.model.Prelude;
import org.avantssar.aslanpp.model.VariableSymbol;
import org.avantssar.commons.TranslatorOptions;
import avantssar.aslanpp.testing.Specification;

@Specification
public class MacrosInHornClauses extends AbstractSpecProvider {

	@Override
	public ASLanPPSpecification getASLanPPSpecification() {
		ASLanPPSpecification spec = startASLanPPSpec();

		FunctionSymbol fRel = env.addFunction("some_relation", env.findType(Prelude.FACT), env.findType(Prelude.MESSAGE), env.findType(Prelude.MESSAGE));
		fRel.setNonInvertible(true);
		fRel.setNonPublic(true);
		FunctionSymbol fRelDirect = env.addFunction("some_relation_direct", env.findType(Prelude.FACT), env.findType(Prelude.MESSAGE), env.findType(Prelude.MESSAGE));
		fRelDirect.setNonInvertible(true);
		fRelDirect.setNonPublic(true);
		FunctionSymbol fDecorate = env.addFunction("decorate", env.findType(Prelude.MESSAGE), env.findType(Prelude.MESSAGE));
		fDecorate.setNonInvertible(true);
		fDecorate.setNonPublic(true);
		MacroSymbol mRel = env.addMacro(null, "relate");
		VariableSymbol mRelA = mRel.addArgument("Am");
		VariableSymbol mRelB = mRel.addArgument("Bm");
		mRel.setBody(fRel.term(fDecorate.term(mRelA.term()), fDecorate.term(mRelB.term())));
		MacroSymbol mRelDirect = env.addMacro(null, "relate_direct");
		VariableSymbol mRelDirA = mRelDirect.addArgument("Am");
		VariableSymbol mRelDirB = mRelDirect.addArgument("Bm");
		mRelDirect.setBody(fRelDirect.term(fDecorate.term(mRelDirA.term()), fDecorate.term(mRelDirB.term())));
		HornClause trans = env.hornClause("transitive");
		VariableSymbol transA = trans.argument("A");
		VariableSymbol transB = trans.argument("B");
		VariableSymbol transC = trans.argument("C");
		trans.setHead(mRel.term(transA.term(), transC.term()));
		trans.addBody(mRel.term(transA.term(), transB.term()));
		trans.addBody(mRel.term(transB.term(), transC.term()));
		HornClause transDirect = env.hornClause("direct");
		VariableSymbol transDirA = transDirect.argument("A");
		VariableSymbol transDirB = transDirect.argument("B");
		transDirect.setHead(mRel.term(transDirA.term(), transDirB.term()));
		transDirect.addBody(mRelDirect.term(transDirA.term(), transDirB.term()));
		ConstantSymbol cA = env.constants(env.findType(Prelude.MESSAGE), "a");
		cA.setNonPublic(true);
		ConstantSymbol cB = env.constants(env.findType(Prelude.MESSAGE), "b");
		cB.setNonPublic(true);
		ConstantSymbol cC = env.constants(env.findType(Prelude.MESSAGE), "c");
		cC.setNonPublic(true);

		BlockStatement envBody = env.body(env.block());
		envBody.add(env.introduce(mRelDirect.term(cA.term(), cB.term())));
		envBody.add(env.introduce(mRelDirect.term(cB.term(), cC.term())));

		Goal g = env.goal("not_a_rel_c");
		g.setFormula(mRel.term(cA.term(), cC.term()).expression().not());
		return spec;
	}

	@Override
	public IASLanSpec getExpectedASLanTranslation(TranslatorOptions options) {
		IASLanSpec spec = startASLanSpec();

		Function fsEnv = spec.function(getStateFunctionName("Environment"), IASLanSpec.FACT, IASLanSpec.AGENT, IASLanSpec.NAT, IASLanSpec.NAT);

		org.avantssar.aslan.Function fDecorate = spec.function("decorate", IASLanSpec.MESSAGE, IASLanSpec.MESSAGE);
		Function fRelate = spec.function("some_relation", IASLanSpec.FACT, IASLanSpec.MESSAGE, IASLanSpec.MESSAGE);
		Function fRelateDirect = spec.function("some_relation_direct", IASLanSpec.FACT, IASLanSpec.MESSAGE, IASLanSpec.MESSAGE);
		Variable vA = spec.variable("A", IASLanSpec.MESSAGE);
		Variable vB = spec.variable("B", IASLanSpec.MESSAGE);
		Variable vC = spec.variable("C", IASLanSpec.MESSAGE);
		Constant cA = spec.constant("a", IASLanSpec.MESSAGE);
		Constant cB = spec.constant("b", IASLanSpec.MESSAGE);
		Constant cC = spec.constant("c", IASLanSpec.MESSAGE);
		Variable vEnvActor = spec.variable("Actor", IASLanSpec.AGENT);
		Variable vEnvIID = spec.variable("IID", IASLanSpec.NAT);
		Variable vEnvSL = spec.variable("SL", IASLanSpec.NAT);
		Variable vEdA = spec.variable("E_d_A", IASLanSpec.MESSAGE);
		Variable vEdB = spec.variable("E_d_B", IASLanSpec.MESSAGE);

		org.avantssar.aslan.HornClause hcRelate = spec.hornClause("transitive", fRelate.term(fDecorate.term(vA.term()), fDecorate.term(vC.term())));
		hcRelate.addBodyFact(fRelate.term(fDecorate.term(vA.term()), fDecorate.term(vB.term())));
		hcRelate.addBodyFact(fRelate.term(fDecorate.term(vB.term()), fDecorate.term(vC.term())));

		org.avantssar.aslan.HornClause hcRelateDirect = spec.hornClause("direct", fRelate.term(fDecorate.term(vEdA.term()), fDecorate.term(vEdB.term())));
		hcRelateDirect.addBodyFact(fRelateDirect.term(fDecorate.term(vEdA.term()), fDecorate.term(vEdB.term())));

		InitialState init = spec.initialState("init");
		prefillInitialState(spec, init);
		init.addFact(fsEnv.term(cRoot.term(), spec.numericTerm(0), spec.numericTerm(1)));

		RewriteRule step1 = spec.rule(getNextStepName("Environment"));
		step1.addLHS(fsEnv.term(vEnvActor.term(), vEnvIID.term(), spec.numericTerm(1)));
		step1.addRHS(fsEnv.term(vEnvActor.term(), vEnvIID.term(), spec.numericTerm(3)));
		step1.addRHS(fRelateDirect.term(fDecorate.term(cA.term()), fDecorate.term(cB.term())));
		step1.addRHS(fRelateDirect.term(fDecorate.term(cB.term()), fDecorate.term(cC.term())));

		org.avantssar.aslan.AttackState as = spec.attackState("not_a_rel_c");
		as.addTerm(fRelate.term(fDecorate.term(cA.term()), fDecorate.term(cC.term())));
        as.addTerm(fsEnv.term(vEnvActor.term(), vEnvIID.term(), vEnvSL.term()));

		return spec;
	}
}
