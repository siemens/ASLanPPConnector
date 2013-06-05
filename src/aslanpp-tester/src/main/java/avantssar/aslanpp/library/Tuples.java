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
import org.avantssar.aslanpp.model.FunctionSymbol;
import org.avantssar.aslanpp.model.Goal;
import org.avantssar.aslanpp.model.HornClause;
import org.avantssar.aslanpp.model.Prelude;
import org.avantssar.aslanpp.model.SimpleType;
import org.avantssar.aslanpp.model.TupleTerm;
import org.avantssar.aslanpp.model.TupleType;
import org.avantssar.aslanpp.model.VariableSymbol;
import org.avantssar.commons.TranslatorOptions;
import avantssar.aslanpp.testing.Specification;

@Specification
public class Tuples extends AbstractSpecProvider {

	@Override
	public ASLanPPSpecification getASLanPPSpecification() {
		ASLanPPSpecification spec = startASLanPPSpec();

		SimpleType verdict = env.type("verdict");
		verdict.setSuperType(spec.findType(Prelude.MESSAGE));
		FunctionSymbol fMarked = env.addFunction("marked", env.findType(Prelude.FACT), new TupleType(env.findType(Prelude.AGENT), env.findType(Prelude.TEXT), verdict));
		fMarked.setNonPublic(true);
		fMarked.setNonInvertible(true);
		FunctionSymbol fRemarked = env.addFunction("remarked", env.findType(Prelude.FACT), new TupleType(env.findType(Prelude.AGENT), env.findType(Prelude.TEXT), verdict));
		fRemarked.setNonPublic(true);
		fRemarked.setNonInvertible(true);
		ConstantSymbol cAccepted = env.constants(verdict, "accepted");
		cAccepted.setNonPublic(true);
		ConstantSymbol cAlice = env.constants(env.findType(Prelude.AGENT), "alice");
		cAlice.setNonPublic(true);
		ConstantSymbol cToken = env.constants(env.findType(Prelude.TEXT), "token");
		cToken.setNonPublic(true);

		HornClause rem = env.hornClause("rem");
		VariableSymbol vWho = rem.argument("Who");
		VariableSymbol vWhat = rem.argument("What");
		VariableSymbol vHow = rem.argument("How");
		rem.setHead(fRemarked.term(TupleTerm.tuple(env, vWho.term(), vWhat.term(), vHow.term())));
		rem.addBody(fMarked.term(TupleTerm.tuple(env, vWho.term(), vWhat.term(), vHow.term())));

		BlockStatement envBlock = env.body(env.block());
		envBlock.add(env.introduce(fMarked.term(TupleTerm.tuple(env, cAlice.term(), cToken.term(), cAccepted.term()))));

		Goal g = env.goal("not_remarked");
		VariableSymbol gWho = g.addUntypedVariable("Who");
		VariableSymbol gWhat = g.addUntypedVariable("What");
		VariableSymbol gHow = g.addUntypedVariable("How");
		g.setFormula(fRemarked.term(TupleTerm.tuple(env, gWho.term(), gWhat.term(), gHow.term())).expression().exists(gWho, gWhat, gHow).not());

		return spec;
	}

	@Override
	public IASLanSpec getExpectedASLanTranslation(TranslatorOptions options) {
		IASLanSpec spec = startASLanSpec();

		org.avantssar.aslan.PrimitiveType tVerdict = spec.primitiveType("verdict");
		tVerdict.setSuperType(IASLanSpec.MESSAGE);

		org.avantssar.aslan.IType tTuple = spec.pairType(IASLanSpec.AGENT, spec.pairType(IASLanSpec.TEXT, tVerdict));

		Function fsEnv = spec.function(getStateFunctionName("Environment"), IASLanSpec.FACT, IASLanSpec.AGENT, IASLanSpec.NAT, IASLanSpec.NAT);
		Function fMarked = spec.function("marked", IASLanSpec.FACT, tTuple);
		Function fRemarked = spec.function("remarked", IASLanSpec.FACT, tTuple);
		Constant cAccepted = spec.constant("accepted", tVerdict);
		Constant cAlice = spec.constant("alice", IASLanSpec.AGENT);
		Constant cToken = spec.constant("token", IASLanSpec.TEXT);
		Variable vActor = spec.variable("Actor", IASLanSpec.AGENT);
		Variable vIID = spec.variable("IID", IASLanSpec.NAT);
		Variable vSL = spec.variable("SL", IASLanSpec.NAT);
		Variable vWho = spec.variable("Who", IASLanSpec.AGENT);
		Variable vWhat = spec.variable("What", IASLanSpec.TEXT);
		Variable vHow = spec.variable("How", tVerdict);
		Variable vGoalWho = spec.variable("E_nr_Who", IASLanSpec.AGENT);
		Variable vGoalWhat = spec.variable("E_nr_What", IASLanSpec.TEXT);
		Variable vGoalHow = spec.variable("E_nr_How", tVerdict);

		org.avantssar.aslan.HornClause hc = spec.hornClause("rem", fRemarked.term(buildTuple(vWho.term(), vWhat.term(), vHow.term())));
		hc.addBodyFact(fMarked.term(buildTuple(vWho.term(), vWhat.term(), vHow.term())));

		InitialState init = spec.initialState("init");
		prefillInitialState(spec, init);
		// init.addFact(fIsAgent.term(cAlice.term()));
		init.addFact(fsEnv.term(cRoot.term(), spec.numericTerm(0), spec.numericTerm(1)));

		RewriteRule step1 = spec.rule(getNextStepName("Environment"));
		step1.addLHS(fsEnv.term(vActor.term(), vIID.term(), spec.numericTerm(1)));
		step1.addRHS(fsEnv.term(vActor.term(), vIID.term(), spec.numericTerm(2)));
		step1.addRHS(fMarked.term(buildTuple(cAlice.term(), cToken.term(), cAccepted.term())));

		AttackState as = spec.attackState("not_remarked");
		as.addTerm(fRemarked.term(buildTuple(vGoalWho.term(), vGoalWhat.term(), vGoalHow.term())));
        as.addTerm(fsEnv.term(vActor.term(), vIID.term(), vSL.term()));

		return spec;
	}

	private org.avantssar.aslan.ITerm buildTuple(org.avantssar.aslan.ITerm... terms) {
		org.avantssar.aslan.ITerm result = terms[terms.length - 1];
		for (int i = terms.length - 2; i >= 0; i--) {
			result = IASLanSpec.PAIR.term(terms[i], result);
		}
		return result;
	}

}
