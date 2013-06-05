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
import org.avantssar.aslanpp.model.BranchStatement;
import org.avantssar.aslanpp.model.ConstantSymbol;
import org.avantssar.aslanpp.model.FunctionSymbol;
import org.avantssar.aslanpp.model.Goal;
import org.avantssar.aslanpp.model.HornClause;
import org.avantssar.aslanpp.model.IType;
import org.avantssar.aslanpp.model.Prelude;
import org.avantssar.aslanpp.model.VariableSymbol;
import org.avantssar.commons.TranslatorOptions;
import avantssar.aslanpp.testing.Specification;

@Specification
public class HornClauses extends AbstractSpecProvider {

	@Override
	public ASLanPPSpecification getASLanPPSpecification() {
		ASLanPPSpecification spec = startASLanPPSpec();

		IType adt = env.type("adt", env.findType(Prelude.MESSAGE));

		FunctionSymbol fLT = env.addFunction("lt", env.findType(Prelude.FACT), adt, adt);
		fLT.setNonInvertible(true);
		fLT.setNonPublic(true);

		VariableSymbol vA = env.addStateVariable("A", adt);
		VariableSymbol vAprime = env.addStateVariable("A'", adt);
		ConstantSymbol cA = env.constants(adt, "a");
		cA.setNonPublic(true);
		FunctionSymbol fInitial = env.addFunction("initial", env.findType(Prelude.FACT), adt);
		fInitial.setNonInvertible(true);
		fInitial.setNonPublic(true);
		FunctionSymbol fFinal = env.addFunction("final", env.findType(Prelude.FACT), adt);
		fFinal.setNonInvertible(true);
		fFinal.setNonPublic(true);

		HornClause refl = env.hornClause("lt_refl");
		VariableSymbol reflA = refl.universallyQuantified("J");
		refl.setHead(fLT.oopTerm(reflA.term(), reflA.term()));

		HornClause trans = env.hornClause("lt_trans");
		VariableSymbol transA = trans.argument("K");
		VariableSymbol transB = trans.argument("L");
		VariableSymbol transC = trans.argument("M");
		trans.setHead(fLT.oopTerm(transA.term(), transC.term()));
		trans.addBody(fLT.oopTerm(transA.term(), transB.term()));
		trans.addBody(fLT.oopTerm(transB.term(), transC.term()));

		BlockStatement envBody = env.body(env.block());
		envBody.add(env.assign(vA.term(), cA.term()));
		envBody.add(env.introduce(fInitial.term(vA.term())));
		BranchStatement branch = envBody.add(env.branch(fLT.oopTerm(vA.term(), vAprime.matchedTerm()).expression()));
		branch.branchTrue(env.introduce(fFinal.term(vAprime.term())));

		Goal negatedLT = env.goal("negated_lt_okay");
		VariableSymbol vA1 = negatedLT.addUntypedVariable("P");
		VariableSymbol vA2 = negatedLT.addUntypedVariable("Q");
		negatedLT.setFormula(fInitial.term(vA1.term()).expression().and(fFinal.term(vA2.term()).expression()).implies(fLT.oopTerm(vA1.term(), vA2.term()).expression().not()).forall(vA1, vA2));
		return spec;
	}

	@Override
	public IASLanSpec getExpectedASLanTranslation(TranslatorOptions options) {
		IASLanSpec spec = startASLanSpec();

		org.avantssar.aslan.PrimitiveType tADT = spec.primitiveType("adt");
		tADT.setSuperType(IASLanSpec.MESSAGE);

		Function fsEnv = spec.function(getStateFunctionName("Environment"), IASLanSpec.FACT, IASLanSpec.AGENT, IASLanSpec.NAT, IASLanSpec.NAT, tADT, tADT);
		Function fLT = spec.function("lt", IASLanSpec.FACT, tADT, tADT);
		Function fInitial = spec.function("initial", IASLanSpec.FACT, tADT);
		Function fFinal = spec.function("final", IASLanSpec.FACT, tADT);
		Constant cA = spec.constant("a", tADT);
		Variable vA = spec.variable("A", tADT);
		Variable vAPrime = spec.variable("A_", tADT);
		Variable vAPrimeMatched = spec.variable("A__1", tADT);
	//	Variable vJ = spec.variable("J", tADT);
		Variable vK = spec.variable("K", tADT);
		Variable vL = spec.variable("L", tADT);
		Variable vM = spec.variable("M", tADT);
		Variable vP = spec.variable("P", tADT);
		Variable vQ = spec.variable("Q", tADT);
		Variable vActor = spec.variable("Actor", IASLanSpec.AGENT);
		Variable vIID = spec.variable("IID", IASLanSpec.NAT);
		Variable vSL = spec.variable("SL", IASLanSpec.NAT);
		Constant cDummyADT = spec.constant("dummy_adt", tADT);

	//	org.avantssar.aslan.HornClause hcRefl = spec.hornClause("lt_refl", fLT.term(vJ.term(), vJ.term()));

		org.avantssar.aslan.HornClause hcTrans = spec.hornClause("lt_trans", fLT.term(vK.term(), vM.term()));
		hcTrans.addBodyFact(fLT.term(vK.term(), vL.term()));
		hcTrans.addBodyFact(fLT.term(vL.term(), vM.term()));

		InitialState init = spec.initialState("init");
		prefillInitialState(spec, init);
		init.addFact(fsEnv.term(cRoot.term(), spec.numericTerm(0), spec.numericTerm(1), cDummyADT.term(), cDummyADT.term()));

		RewriteRule step1 = spec.rule(getNextStepName("Environment"));
		step1.addLHS(fsEnv.term(vActor.term(), vIID.term(), spec.numericTerm(1), vA.term(), vAPrime.term()));
		step1.addRHS(fsEnv.term(vActor.term(), vIID.term(), spec.numericTerm(3), cA.term(), vAPrime.term()));
		step1.addRHS(fInitial.term(cA.term()));

		RewriteRule step2 = spec.rule(getNextStepName("Environment"));
		step2.addLHS(fsEnv.term(vActor.term(), vIID.term(), spec.numericTerm(3), vA.term(), vAPrime.term()));
		step2.addLHS(fLT.term(vA.term(), vAPrimeMatched.term()));
		step2.addRHS(fsEnv.term(vActor.term(), vIID.term(), spec.numericTerm(5), vA.term(), vAPrimeMatched.term()));
		step2.addRHS(fFinal.term(vAPrimeMatched.term()));

		RewriteRule step3 = spec.rule(getNextStepName("Environment"));
		step3.addLHS(fsEnv.term(vActor.term(), vIID.term(), spec.numericTerm(3), vA.term(), vAPrime.term()));
		step3.addLHS(fLT.term(vA.term(), vAPrimeMatched.term()).negate());
		step3.addRHS(fsEnv.term(vActor.term(), vIID.term(), spec.numericTerm(5), vA.term(), vAPrime.term()));

		org.avantssar.aslan.AttackState as = spec.attackState("negated_lt_okay");
		as.addTerm(fInitial.term(vP.term()));
		as.addTerm(fFinal.term(vQ.term()));
		as.addTerm(fLT.term(vP.term(), vQ.term()));
        as.addTerm(fsEnv.term(vActor.term(), vIID.term(), vSL.term(), vA.term(), vAPrime.term()));

		return spec;
	}
}
