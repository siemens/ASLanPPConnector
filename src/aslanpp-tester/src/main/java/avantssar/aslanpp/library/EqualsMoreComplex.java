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
import org.avantssar.aslanpp.model.ConcatTerm;
import org.avantssar.aslanpp.model.ConstantSymbol;
import org.avantssar.aslanpp.model.FunctionSymbol;
import org.avantssar.aslanpp.model.Goal;
import org.avantssar.aslanpp.model.IExpression;
import org.avantssar.aslanpp.model.LoopStatement;
import org.avantssar.aslanpp.model.Prelude;
import org.avantssar.aslanpp.model.SelectStatement;
import org.avantssar.aslanpp.model.SimpleType;
import org.avantssar.aslanpp.model.VariableSymbol;
import org.avantssar.commons.TranslatorOptions;
import avantssar.aslan.backends.CLAtSeBackendRunner;
import avantssar.aslanpp.testing.BackendParameters;
import avantssar.aslanpp.testing.Specification;

@Specification
@BackendParameters(backend = CLAtSeBackendRunner.NAME, parameters = { "--nb", "2" })
public class EqualsMoreComplex extends AbstractSpecProvider {

	@Override
	public ASLanPPSpecification getASLanPPSpecification() {
		ASLanPPSpecification spec = startASLanPPSpec();
		SimpleType tPerson = env.type("person", tppMessage);
		SimpleType tDocument = env.type("document", tppMessage);
		SimpleType tDecision = env.type("decision", tppMessage);

		ConstantSymbol cAlice = env.constants(tPerson, "alice");
		cAlice.setNonPublic(true);
		ConstantSymbol cBob = env.constants(tPerson, "bob");
		cBob.setNonPublic(true);
		env.group(cAlice, cBob);

		ConstantSymbol cDoc1 = env.constants(tDocument, "doc1");
		cDoc1.setNonPublic(true);
		ConstantSymbol cDoc2 = env.constants(tDocument, "doc2");
		cDoc2.setNonPublic(true);
		env.group(cDoc1, cDoc2);

		ConstantSymbol cCorrect = env.constants(tDecision, "correct");
		cCorrect.setNonPublic(true);
		ConstantSymbol cIncorrect = env.constants(tDecision, "incorrect");
		cIncorrect.setNonPublic(true);
		env.group(cCorrect, cIncorrect);

		FunctionSymbol fMarked = env.addFunction("marked", tppFact, tppMessage);
		fMarked.setNonInvertible(true);
		fMarked.setNonPublic(true);

		FunctionSymbol fAccepted = env.addFunction("accepted", tppFact, tPerson, tDocument);
		fAccepted.setNonInvertible(true);
		fAccepted.setNonPublic(true);

		FunctionSymbol fRejected = env.addFunction("rejected", tppFact, tPerson, tDocument);
		fRejected.setNonInvertible(true);
		fRejected.setNonPublic(true);

		VariableSymbol vEntry = env.addStateVariable("Entry", tppMessage);
		VariableSymbol vSubEntry = env.addStateVariable("SubEntry", tppMessage);
		VariableSymbol vWho = env.addStateVariable("Who", tPerson);
		VariableSymbol vDoc = env.addStateVariable("Doc", tDocument);
		VariableSymbol vDecision = env.addStateVariable("Decision", tDecision);

		ConstantSymbol cTrue = env.findConstant(Prelude.TRUE);

		BlockStatement envBody = env.body(env.block());
		envBody.add(env.introduce(fMarked.term(ConcatTerm.concat(env, cAlice.term(), cDoc1.term(), cCorrect.term()))));
		envBody.add(env.introduce(fMarked.term(ConcatTerm.concat(env, cBob.term(), cDoc2.term(), cIncorrect.term()))));
		LoopStatement infLoop = envBody.add(env.loop(cTrue.expr()));
		SelectStatement sel = infLoop.body(env.select());
		BranchStatement filter = sel.choice(fMarked.term(vEntry.matchedTerm()).expression(), env.branch(vEntry.term().equality(ConcatTerm.concat(env, vWho.matchedTerm(), vSubEntry.matchedTerm()))));
		BranchStatement filter2 = filter.branchTrue(env.branch(vSubEntry.term().equality(ConcatTerm.concat(env, vDoc.matchedTerm(), vDecision.matchedTerm()))));
		BranchStatement split = filter2.branchTrue(env.branch(vDecision.term().equality(cCorrect.term())));
		split.branchTrue(env.introduce(fAccepted.term(vWho.term(), vDoc.term())));
		split.branchFalse(env.introduce(fRejected.term(vWho.term(), vDoc.term())));

		Goal g = env.goal("s");
		VariableSymbol vWho1 = g.addUntypedVariable("Who1");
		VariableSymbol vWho2 = g.addUntypedVariable("Who2");
		VariableSymbol vDoc1 = g.addUntypedVariable("Doc1");
		VariableSymbol vDoc2 = g.addUntypedVariable("Doc2");
		IExpression f = fAccepted.term(vWho1.term(), vDoc1.term()).expression();
		f = f.and(fRejected.term(vWho2.term(), vDoc2.term()).expression());
		f = f.and(vWho1.term().equality(vWho2.term()).not());
		f = f.and(vDoc1.term().equality(vDoc2.term()).not());
		g.setFormula(f.exists(vWho1, vWho2, vDoc1, vDoc2).not());
		return spec;
	}

	@Override
	public IASLanSpec getExpectedASLanTranslation(TranslatorOptions options) {
		IASLanSpec spec = startASLanSpec();

		org.avantssar.aslan.PrimitiveType tPerson = spec.primitiveType("person");
		tPerson.setSuperType(IASLanSpec.MESSAGE);
		org.avantssar.aslan.PrimitiveType tDocument = spec.primitiveType("document");
		tDocument.setSuperType(IASLanSpec.MESSAGE);
		org.avantssar.aslan.PrimitiveType tDecision = spec.primitiveType("decision");
		tDecision.setSuperType(IASLanSpec.MESSAGE);

		Constant cDummyMessage = spec.constant("dummy_message", IASLanSpec.MESSAGE);
		Constant cDummyPerson = spec.constant("dummy_person", tPerson);
		Constant cDummyDocument = spec.constant("dummy_document", tDocument);
		Constant cDummyDecision = spec.constant("dummy_decision", tDecision);

		Function fsEnv = spec.function(getStateFunctionName("Environment"), IASLanSpec.FACT, IASLanSpec.AGENT, IASLanSpec.NAT, IASLanSpec.NAT, IASLanSpec.MESSAGE, IASLanSpec.MESSAGE, tPerson,
				tDocument, tDecision);
		Function fAccepted = spec.function("accepted", IASLanSpec.FACT, tPerson, tDocument);
		Function fRejected = spec.function("rejected", IASLanSpec.FACT, tPerson, tDocument);
		Function fMarked = spec.function("marked", IASLanSpec.FACT, IASLanSpec.MESSAGE);

		Constant cAlice = spec.constant("alice", tPerson);
		Constant cBob = spec.constant("bob", tPerson);
		Constant cDoc1 = spec.constant("doc1", tDocument);
		Constant cDoc2 = spec.constant("doc2", tDocument);
		Constant cCorrect = spec.constant("correct", tDecision);
		Constant cIncorrect = spec.constant("incorrect", tDecision);

		Variable vActor = spec.variable("Actor", IASLanSpec.AGENT);
		Variable vIID = spec.variable("IID", IASLanSpec.NAT);
		Variable vSL = spec.variable("SL", IASLanSpec.NAT);
		Variable vEntry = spec.variable("Entry", IASLanSpec.MESSAGE);
		Variable vSubEntry = spec.variable("SubEntry", IASLanSpec.MESSAGE);
		Variable vWho = spec.variable("Who", tPerson);
		Variable vDoc = spec.variable("Doc", tDocument);
		Variable vDecision = spec.variable("Decision", tDecision);
		Variable vEntryMatched = spec.variable("Entry_1", IASLanSpec.MESSAGE);
		Variable vSubEntryMatched = spec.variable("SubEntry_1", IASLanSpec.MESSAGE);
		Variable vWhoMatched = spec.variable("Who_1", tPerson);
		Variable vDocMatched = spec.variable("Doc_1", tDocument);
		Variable vDecisionMatched = spec.variable("Decision_1", tDecision);
		Variable vWho1 = spec.variable("Who1", tPerson);
		Variable vDoc1 = spec.variable("Doc1", tDocument);
		Variable vWho2 = spec.variable("Who2", tPerson);
		Variable vDoc2 = spec.variable("Doc2", tDocument);

		InitialState init = spec.initialState("init");
		prefillInitialState(spec, init);
		init.addFact(fsEnv.term(cRoot.term(), spec.numericTerm(0), spec.numericTerm(1), cDummyMessage.term(), cDummyMessage.term(), cDummyPerson.term(), cDummyDocument.term(), cDummyDecision.term()));

		RewriteRule step1 = spec.rule(getNextStepName("Environment"));
		step1.addLHS(fsEnv.term(vActor.term(), vIID.term(), spec.numericTerm(1), vEntry.term(), vSubEntry.term(), vWho.term(), vDoc.term(), vDecision.term()));
		step1.addRHS(fsEnv.term(vActor.term(), vIID.term(), spec.numericTerm(3), vEntry.term(), vSubEntry.term(), vWho.term(), vDoc.term(), vDecision.term()));
		step1.addRHS(fMarked.term(IASLanSpec.PAIR.term(cAlice.term(), IASLanSpec.PAIR.term(cDoc1.term(), cCorrect.term()))));
		step1.addRHS(fMarked.term(IASLanSpec.PAIR.term(cBob.term(), IASLanSpec.PAIR.term(cDoc2.term(), cIncorrect.term()))));

		RewriteRule step2 = spec.rule(getNextStepName("Environment"));
		step2.addLHS(fsEnv.term(vActor.term(), vIID.term(), spec.numericTerm(3), vEntry.term(), vSubEntry.term(), vWho.term(), vDoc.term(), vDecision.term()));
		step2.addLHS(fMarked.term(vEntryMatched.term()));
		step2.addRHS(fsEnv.term(vActor.term(), vIID.term(), spec.numericTerm(4), vEntryMatched.term(), vSubEntry.term(), vWho.term(), vDoc.term(), vDecision.term()));
		step2.addRHS(fMarked.term(vEntryMatched.term()));

		RewriteRule step3 = spec.rule(getNextStepName("Environment"));
		step3.addLHS(fsEnv.term(vActor.term(), vIID.term(), spec.numericTerm(4), vEntry.term(), vSubEntry.term(), vWho.term(), vDoc.term(), vDecision.term()));
		step3.addLHS(IASLanSpec.EQUAL.term(vEntry.term(), IASLanSpec.PAIR.term(vWhoMatched.term(), vSubEntryMatched.term())));
		step3.addRHS(fsEnv.term(vActor.term(), vIID.term(), spec.numericTerm(5), vEntry.term(), vSubEntryMatched.term(), vWhoMatched.term(), vDoc.term(), vDecision.term()));

		RewriteRule step4 = spec.rule(getNextStepName("Environment"));
		step4.addLHS(fsEnv.term(vActor.term(), vIID.term(), spec.numericTerm(5), vEntry.term(), vSubEntry.term(), vWho.term(), vDoc.term(), vDecision.term()));
		step4.addLHS(IASLanSpec.EQUAL.term(vSubEntry.term(), IASLanSpec.PAIR.term(vDocMatched.term(), vDecisionMatched.term())));
		step4.addRHS(fsEnv.term(vActor.term(), vIID.term(), spec.numericTerm(6), vEntry.term(), vSubEntry.term(), vWho.term(), vDocMatched.term(), vDecisionMatched.term()));

		RewriteRule step5 = spec.rule(getNextStepName("Environment"));
		step5.addLHS(fsEnv.term(vActor.term(), vIID.term(), spec.numericTerm(6), vEntry.term(), vSubEntry.term(), vWho.term(), vDoc.term(), vDecision.term()));
		step5.addLHS(IASLanSpec.EQUAL.term(vDecision.term(), cCorrect.term()));
		step5.addRHS(fsEnv.term(vActor.term(), vIID.term(), spec.numericTerm(3), vEntry.term(), vSubEntry.term(), vWho.term(), vDoc.term(), vDecision.term()));
		step5.addRHS(fAccepted.term(vWho.term(), vDoc.term()));

		RewriteRule step6 = spec.rule(getNextStepName("Environment"));
		step6.addLHS(fsEnv.term(vActor.term(), vIID.term(), spec.numericTerm(6), vEntry.term(), vSubEntry.term(), vWho.term(), vDoc.term(), vDecision.term()));
		step6.addLHS(IASLanSpec.EQUAL.term(vDecision.term(), cCorrect.term()).negate());
		step6.addRHS(fsEnv.term(vActor.term(), vIID.term(), spec.numericTerm(3), vEntry.term(), vSubEntry.term(), vWho.term(), vDoc.term(), vDecision.term()));
		step6.addRHS(fRejected.term(vWho.term(), vDoc.term()));

		RewriteRule step7 = spec.rule(getNextStepName("Environment"));
		step7.addLHS(fsEnv.term(vActor.term(), vIID.term(), spec.numericTerm(5), vEntry.term(), vSubEntry.term(), vWho.term(), vDoc.term(), vDecision.term()));
		step7.addLHS(IASLanSpec.EQUAL.term(vSubEntry.term(), IASLanSpec.PAIR.term(vDocMatched.term(), vDecisionMatched.term())).negate());
		step7.addRHS(fsEnv.term(vActor.term(), vIID.term(), spec.numericTerm(3), vEntry.term(), vSubEntry.term(), vWho.term(), vDoc.term(), vDecision.term()));

		RewriteRule step8 = spec.rule(getNextStepName("Environment"));
		step8.addLHS(fsEnv.term(vActor.term(), vIID.term(), spec.numericTerm(4), vEntry.term(), vSubEntry.term(), vWho.term(), vDoc.term(), vDecision.term()));
		step8.addLHS(IASLanSpec.EQUAL.term(vEntry.term(), IASLanSpec.PAIR.term(vWhoMatched.term(), vSubEntryMatched.term())).negate());
		step8.addRHS(fsEnv.term(vActor.term(), vIID.term(), spec.numericTerm(3), vEntry.term(), vSubEntry.term(), vWho.term(), vDoc.term(), vDecision.term()));

		AttackState as = spec.attackState("s");
		as.addTerm(fAccepted.term(vWho1.term(), vDoc1.term()));
		as.addTerm(fRejected.term(vWho2.term(), vDoc2.term()));
		as.addTerm(IASLanSpec.EQUAL.term(vWho1.term(), vWho2.term()).negate());
		as.addTerm(IASLanSpec.EQUAL.term(vDoc1.term(), vDoc2.term()).negate());
        as.addTerm(fsEnv.term(vActor.term(), vIID.term(), vSL.term(), vEntry.term(), vSubEntry.term(), vWho.term(), vDoc.term(), vDecision.term()));

		return spec;
	}
}
