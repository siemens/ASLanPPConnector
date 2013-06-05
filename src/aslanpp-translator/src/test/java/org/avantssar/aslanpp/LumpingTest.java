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

package org.avantssar.aslanpp;

import java.util.ArrayList;
import java.util.TreeSet;
import junit.framework.Assert;
import junit.framework.TestCase;
import org.avantssar.aslan.RewriteRule;
import org.avantssar.aslanpp.flow.ASLanBuilder;
import org.avantssar.aslanpp.flow.AssignmentEdge;
import org.avantssar.aslanpp.flow.Counters;
import org.avantssar.aslanpp.flow.GuardedEdge;
import org.avantssar.aslanpp.flow.INode;
import org.avantssar.aslanpp.flow.IntroduceRetractEdge;
import org.avantssar.aslanpp.flow.NewInstanceEdge;
import org.avantssar.aslanpp.flow.Node;
import org.avantssar.aslanpp.flow.SymbolicInstanceEdge;
import org.avantssar.aslanpp.flow.TransitionsRecorder;
import org.avantssar.aslanpp.model.ASLanPPSpecification;
import org.avantssar.aslanpp.model.ConcatTerm;
import org.avantssar.aslanpp.model.ConstantSymbol;
import org.avantssar.aslanpp.model.Entity;
import org.avantssar.aslanpp.model.EntityManager;
import org.avantssar.aslanpp.model.FunctionSymbol;
import org.avantssar.aslanpp.model.IExpression;
import org.avantssar.aslanpp.model.ITerm;
import org.avantssar.aslanpp.model.IType;
import org.avantssar.aslanpp.model.Prelude;
import org.avantssar.aslanpp.model.SetLiteralTerm;
import org.avantssar.aslanpp.model.SymbolsState;
import org.avantssar.aslanpp.model.TupleTerm;
import org.avantssar.aslanpp.model.VariableSymbol;
import org.avantssar.aslanpp.model.VariableTerm;
import org.avantssar.aslanpp.visitors.PostProcessor;
import org.avantssar.commons.ChannelModel;
import org.avantssar.commons.ErrorGatherer;
import org.avantssar.commons.TranslatorOptions;

public class LumpingTest extends TestCase {

	private ASLanPPSpecification spec;
	private Entity ent;
	private Node firstNode;
	private MockTransition mockt;
	private TranslatorOptions options;
	private ErrorGatherer err;

	private IType tMessage;
	private IType tFact;
	private IType tAgent;
	private IType tNat;

	private ASLanBuilder builder;

	private EntityManager manager;

	public LumpingTest(String testName) {
		super(testName);
	}

	@Override
	public void setUp() {
		options = new TranslatorOptions();
		manager = new EntityManager();
		spec = new ASLanPPSpecification(manager, "spec", ChannelModel.CCM);
		builder = new ASLanBuilder(options, "", "");

		tMessage = spec.findType(Prelude.MESSAGE);
		tFact = spec.findType(Prelude.FACT);
		tAgent = spec.findType(Prelude.AGENT);
		tNat = spec.findType(Prelude.NAT);

		ent = spec.entity("Env");
		firstNode = new Node();
		mockt = new MockTransition("step_001_" + ent.getName());
	}

	// TODO: the first fresh symbol should not be listed in "exists"
	// A := fresh();
	// A := fresh();
	// myFact(A);
	// A := fresh();
	// myFact(A);
	@SuppressWarnings("unused")
	public void testFresh() {
		VariableSymbol varA = ent.addStateVariable("A", tMessage);
		VariableSymbol varA1 = ent.addVariable("A_1", tMessage);
		VariableSymbol varA2 = ent.addVariable("A_2", tMessage);
		VariableSymbol varA3 = ent.addVariable("A_3", tMessage);
		FunctionSymbol fncMyFact = ent.addFunction("myFact", tFact, tMessage);
		AssignmentEdge f1 = new AssignmentEdge(ent, firstNode, varA.term(), varA1.freshTerm(), true, null, builder);
		AssignmentEdge f2 = new AssignmentEdge(ent, f1.getTargetNode(), varA.term(), varA2.freshTerm(), true, null, builder);
		IntroduceRetractEdge ir3 = new IntroduceRetractEdge(ent, f2.getTargetNode(), fncMyFact.term(varA.term()), true, null, builder);
		AssignmentEdge f4 = new AssignmentEdge(ent, ir3.getTargetNode(), varA.term(), varA3.freshTerm(), true, null, builder);
		IntroduceRetractEdge ir5 = new IntroduceRetractEdge(ent, f4.getTargetNode(), fncMyFact.term(varA.term()), true, null, builder);

		mockt.addLhsStateItem(ent.getName(), new String[] { ent.getActorSymbol().getName(), ent.getIDSymbol().getName(), "1", varA.getName() });
		mockt.addRhsStateItem(ent.getName(), new String[] { ent.getActorSymbol().getName(), ent.getIDSymbol().getName(), "6", varA3.getName() });
		mockt.addRhsItem("myFact", new String[] { varA2.getName() });
		mockt.addRhsItem("myFact", new String[] { varA3.getName() });
		mockt.addParameters(new String[] { ent.getActorSymbol().getName(), ent.getIDSymbol().getName(), varA.getName(), varA2.getName(), varA3.getName() });
		mockt.addFreshVars(new String[] { varA2.getName(), varA3.getName() });
	}

	// if (!A = ?B.?C) {
	// D := B;
	// E := C;
	// myFact(A, B, C, D, E);
	// }
	@SuppressWarnings("unused")
	public void testNegativeMatchInGuard() {
		VariableSymbol varA = ent.addStateVariable("A", tMessage);
		VariableSymbol varB = ent.addStateVariable("B", tMessage);
		VariableSymbol varC = ent.addStateVariable("C", tMessage);
		VariableSymbol varD = ent.addStateVariable("D", tMessage);
		VariableSymbol varE = ent.addStateVariable("E", tMessage);
		FunctionSymbol fncMyFact = ent.addFunction("myFact", tFact, tMessage, tMessage, tMessage, tMessage, tMessage);
		VariableTerm matchB = varB.matchedTerm();
		matchB.accept(new PostProcessor(err));
		VariableTerm matchC = varC.matchedTerm();
		matchC.accept(new PostProcessor(err));

		IExpression guard = varA.term().equality(TupleTerm.tuple(ent, matchB, matchC)).not();
		GuardedEdge g1 = new GuardedEdge(ent, firstNode, guard, null, builder);
		AssignmentEdge f2 = new AssignmentEdge(ent, g1.getTargetNode(), varD.term(), varB.term(), false, null, builder);
		AssignmentEdge f3 = new AssignmentEdge(ent, f2.getTargetNode(), varE.term(), varC.term(), false, null, builder);
		IntroduceRetractEdge ir4 = new IntroduceRetractEdge(ent, f3.getTargetNode(), fncMyFact.term(varA.term(), varB.term(), varC.term(), varD.term(), varE.term()), true, null, builder);

		mockt.addLhsStateItem(ent.getName(), new String[] { ent.getActorSymbol().getName(), ent.getIDSymbol().getName(), "1", varA.getName(), varB.getName(), varC.getName(), varD.getName(),
				varE.getName() });
		mockt.addNegatedCondition("equal",
				new String[] { varA.getName(), MockTransition.renderFunction("pair", new String[] { matchB.getDummySymbol().getName(), matchC.getDummySymbol().getName() }) });
		mockt.addRhsStateItem(ent.getName(), new String[] { ent.getActorSymbol().getName(), ent.getIDSymbol().getName(), "5", varA.getName(), varB.getName(), varC.getName(), varB.getName(),
				varC.getName() });
		mockt.addRhsItem(fncMyFact.getName(), new String[] { varA.getName(), varB.getName(), varC.getName(), varB.getName(), varC.getName() });
		mockt.addParameters(new String[] { ent.getActorSymbol().getName(), ent.getIDSymbol().getName(), varA.getName(), varB.getName(), matchB.getDummySymbol().getName(), varC.getName(),
				matchC.getDummySymbol().getName(), varD.getName(), varE.getName() });
	}

	@SuppressWarnings("unused")
	public void testNegativeMatchInRetract() {
		VariableSymbol varA = ent.addStateVariable("A", tMessage);
		VariableSymbol varB = ent.addStateVariable("B", tMessage);
		VariableSymbol varC = ent.addStateVariable("C", tMessage);
		VariableSymbol varD = ent.addStateVariable("D", tMessage);
		VariableSymbol varE = ent.addStateVariable("E", tMessage);
		FunctionSymbol fncInput = ent.addFunction("input", tFact, tMessage, tMessage);
		FunctionSymbol fncMyFact = ent.addFunction("myFact", tFact, tMessage, tMessage, tMessage, tMessage, tMessage);
		VariableTerm matchB = varB.matchedTerm();
		matchB.accept(new PostProcessor(err));
		VariableTerm matchC = varC.matchedTerm();
		matchC.accept(new PostProcessor(err));

		ITerm tt = fncInput.term(matchB, matchC);
		IntroduceRetractEdge g1 = new IntroduceRetractEdge(ent, firstNode, tt, false, null, builder);
		AssignmentEdge f2 = new AssignmentEdge(ent, g1.getTargetNode(), varD.term(), varB.term(), false, null, builder);
		AssignmentEdge f3 = new AssignmentEdge(ent, f2.getTargetNode(), varE.term(), varC.term(), false, null, builder);
		IntroduceRetractEdge ir4 = new IntroduceRetractEdge(ent, f3.getTargetNode(), fncMyFact.term(varA.term(), varB.term(), varC.term(), varD.term(), varE.term()), true, null, builder);

		mockt.addLhsStateItem(ent.getName(), new String[] { ent.getActorSymbol().getName(), ent.getIDSymbol().getName(), "1", varA.getName(), varB.getName(), varC.getName(), varD.getName(),
				varE.getName() });
		mockt.addLhsItem(fncInput.getName(), new String[] { matchB.getDummySymbol().getName(), matchC.getDummySymbol().getName() });
		mockt.addRhsStateItem(ent.getName(), new String[] { ent.getActorSymbol().getName(), ent.getIDSymbol().getName(), "5", varA.getName(), matchB.getDummySymbol().getName(),
				matchC.getDummySymbol().getName(), matchB.getDummySymbol().getName(), matchC.getDummySymbol().getName() });
		mockt.addRhsItem(fncMyFact.getName(), new String[] { varA.getName(), matchB.getDummySymbol().getName(), matchC.getDummySymbol().getName(), matchB.getDummySymbol().getName(),
				matchC.getDummySymbol().getName() });
		mockt.addParameters(new String[] { ent.getActorSymbol().getName(), ent.getIDSymbol().getName(), varA.getName(), varB.getName(), matchB.getDummySymbol().getName(), varC.getName(),
				matchC.getDummySymbol().getName(), varD.getName(), varE.getName() });
	}

	// if (A = ?B.?C) {
	// D := B;
	// E := C;
	// myFact(A, B, C, D, E);
	// }
	@SuppressWarnings("unused")
	public void testPositiveMatchInGuard() {
		VariableSymbol varA = ent.addStateVariable("A", tMessage);
		VariableSymbol varB = ent.addStateVariable("B", tMessage);
		VariableSymbol varC = ent.addStateVariable("C", tMessage);
		VariableSymbol varD = ent.addStateVariable("D", tMessage);
		VariableSymbol varE = ent.addStateVariable("E", tMessage);
		FunctionSymbol fncMyFact = ent.addFunction("myFact", tFact, tMessage, tMessage, tMessage, tMessage, tMessage);
		VariableTerm matchB = varB.matchedTerm();
		matchB.accept(new PostProcessor(err));
		VariableTerm matchC = varC.matchedTerm();
		matchC.accept(new PostProcessor(err));

		IExpression guard = varA.term().equality(TupleTerm.tuple(ent, matchB, matchC));
		GuardedEdge g1 = new GuardedEdge(ent, firstNode, guard, null, builder);
		AssignmentEdge f2 = new AssignmentEdge(ent, g1.getTargetNode(), varD.term(), varB.term(), false, null, builder);
		AssignmentEdge f3 = new AssignmentEdge(ent, f2.getTargetNode(), varE.term(), varC.term(), false, null, builder);
		IntroduceRetractEdge ir4 = new IntroduceRetractEdge(ent, f3.getTargetNode(), fncMyFact.term(varA.term(), varB.term(), varC.term(), varD.term(), varE.term()), true, null, builder);

		mockt.addLhsStateItem(ent.getName(), new String[] { ent.getActorSymbol().getName(), ent.getIDSymbol().getName(), "1", varA.getName(), varB.getName(), varC.getName(), varD.getName(),
				varE.getName() });
		mockt.addCondition("equal", new String[] { varA.getName(), MockTransition.renderFunction("pair", new String[] { matchB.getDummySymbol().getName(), matchC.getDummySymbol().getName() }) });
		mockt.addRhsStateItem(ent.getName(), new String[] { ent.getActorSymbol().getName(), ent.getIDSymbol().getName(), "5", varA.getName(), matchB.getDummySymbol().getName(),
				matchC.getDummySymbol().getName(), matchB.getDummySymbol().getName(), matchC.getDummySymbol().getName() });
		mockt.addRhsItem(fncMyFact.getName(), new String[] { varA.getName(), matchB.getDummySymbol().getName(), matchC.getDummySymbol().getName(), matchB.getDummySymbol().getName(),
				matchC.getDummySymbol().getName() });
		mockt.addParameters(new String[] { ent.getActorSymbol().getName(), ent.getIDSymbol().getName(), varA.getName(), varB.getName(), matchB.getDummySymbol().getName(), varC.getName(),
				matchC.getDummySymbol().getName(), varD.getName(), varE.getName() });
	}

	// Note: this is not really supported by the grammar, but is useful for
	// testing.
	// 
	// not(myFact(?B, ?C));
	// A := B;
	// D := C;
	// public void testNegativeMatchInFact() {
	// VariableSymbol varA = ent.addStateVariable("A", tMessage);
	// VariableSymbol varB = ent.addStateVariable("B", tMessage);
	// VariableSymbol varC = ent.addStateVariable("C", tMessage);
	// VariableSymbol varD = ent.addStateVariable("D", tMessage);
	// FunctionSymbol fncMyFact = ent.addFunction("myFact", tFact, tMessage,
	// tMessage);
	// FunctionSymbol fncNot = ent.findFunction(Prelude.NOT);
	// VariableTerm matchB = varB.matchedTerm();
	// matchB.accept(new PostProcessor());
	// VariableTerm matchC = varC.matchedTerm();
	// matchC.accept(new PostProcessor());
	//
	// IntroduceRetractEdge ir1 = new IntroduceRetractEdge(ent, firstNode, new
	// NegatedFact(fncMyFact.term(matchB, matchC)), false, 0);
	// AssignmentEdge f2 = new AssignmentEdge(ent, ir1.getTargetNode(), varA,
	// varB.term(), false, 0);
	// AssignmentEdge f3 = new AssignmentEdge(ent, f2.getTargetNode(), varD,
	// varC.term(), false, 0);
	//
	// mockt.addLhsStateItem(ent.getName(), new String[] {
	// ent.getActorSymbol().getName(), ent.getIDSymbol().getName(), "1",
	// varA.getName(), varB.getName(), varC.getName(), varD.getName() });
	// mockt.addNegatedLhsItem(fncMyFact.getName(), new String[] {
	// matchB.getDummySymbol().getName(), matchC.getDummySymbol().getName() });
	// mockt.addRhsStateItem(ent.getName(), new String[] {
	// ent.getActorSymbol().getName(), ent.getIDSymbol().getName(), "4",
	// varB.getName(), varB.getName(), varC.getName(), varC.getName() });
	// mockt.addParameters(new String[] { ent.getActorSymbol().getName(),
	// ent.getIDSymbol().getName(), varA.getName(), varB.getName(),
	// matchB.getDummySymbol().getName(), varC.getName(),
	// matchC.getDummySymbol().getName(), varD.getName() });
	// }

	// myFact(?B, ?C);
	// A := B;
	// D := C;
	@SuppressWarnings("unused")
	public void testPositiveMatchInFact() {
		VariableSymbol varA = ent.addStateVariable("A", tMessage);
		VariableSymbol varB = ent.addStateVariable("B", tMessage);
		VariableSymbol varC = ent.addStateVariable("C", tMessage);
		VariableSymbol varD = ent.addStateVariable("D", tMessage);
		FunctionSymbol fncMyFact = ent.addFunction("myFact", tFact, tMessage, tMessage);
		VariableTerm matchB = varB.matchedTerm();
		matchB.accept(new PostProcessor(err));
		VariableTerm matchC = varC.matchedTerm();
		matchC.accept(new PostProcessor(err));

		IntroduceRetractEdge ir1 = new IntroduceRetractEdge(ent, firstNode, fncMyFact.term(matchB, matchC), false, null, builder);
		AssignmentEdge f2 = new AssignmentEdge(ent, ir1.getTargetNode(), varA.term(), varB.term(), false, null, builder);
		AssignmentEdge f3 = new AssignmentEdge(ent, f2.getTargetNode(), varD.term(), varC.term(), false, null, builder);

		mockt.addLhsStateItem(ent.getName(), new String[] { ent.getActorSymbol().getName(), ent.getIDSymbol().getName(), "1", varA.getName(), varB.getName(), varC.getName(), varD.getName() });
		mockt.addLhsItem(fncMyFact.getName(), new String[] { matchB.getDummySymbol().getName(), matchC.getDummySymbol().getName() });
		mockt.addRhsStateItem(ent.getName(), new String[] { ent.getActorSymbol().getName(), ent.getIDSymbol().getName(), "4", matchB.getDummySymbol().getName(), matchB.getDummySymbol().getName(),
				matchC.getDummySymbol().getName(), matchC.getDummySymbol().getName() });
		mockt.addParameters(new String[] { ent.getActorSymbol().getName(), ent.getIDSymbol().getName(), varA.getName(), varB.getName(), matchB.getDummySymbol().getName(), varC.getName(),
				matchC.getDummySymbol().getName(), varD.getName() });
	}

	// myFact(A, B);
	// myFact(A, B);
	// retract myFact(A, B);
	@SuppressWarnings("unused")
	public void testIntroduceRetractSameFactNoChange() {
		VariableSymbol varA = ent.addStateVariable("A", tMessage);
		VariableSymbol varB = ent.addStateVariable("B", tMessage);
		VariableSymbol varC = ent.addStateVariable("C", tMessage);
		FunctionSymbol fncMyFact = ent.addFunction("myFact", tFact, tMessage, tMessage);

		IntroduceRetractEdge ir1 = new IntroduceRetractEdge(ent, firstNode, fncMyFact.term(varA.term(), varB.term()), true, null, builder);
		IntroduceRetractEdge ir2 = new IntroduceRetractEdge(ent, ir1.getTargetNode(), fncMyFact.term(varA.term(), varB.term()), true, null, builder);
		IntroduceRetractEdge ir3 = new IntroduceRetractEdge(ent, ir2.getTargetNode(), fncMyFact.term(varA.term(), varB.term()), false, null, builder);

		mockt.addLhsStateItem(ent.getName(), new String[] { ent.getActorSymbol().getName(), ent.getIDSymbol().getName(), "1", varA.getName(), varB.getName(), varC.getName() });
		mockt.addRhsStateItem(ent.getName(), new String[] { ent.getActorSymbol().getName(), ent.getIDSymbol().getName(), "4", varA.getName(), varB.getName(), varC.getName() });
		mockt.addRhsItem(fncMyFact.getName(), new String[] { varA.getName(), varB.getName() });
		mockt.addParameters(new String[] { ent.getActorSymbol().getName(), ent.getIDSymbol().getName(), varA.getName(), varB.getName(), varC.getName() });
	}

	// myFact(A, B);
	// myFact(A, B);
	// A := B;
	// retract myFact(A, B);
	@SuppressWarnings("unused")
	public void testIntroduceRetractSameFactWithChange() {
		VariableSymbol varA = ent.addStateVariable("A", tMessage);
		VariableSymbol varB = ent.addStateVariable("B", tMessage);
		VariableSymbol varC = ent.addStateVariable("C", tMessage);
		FunctionSymbol fncMyFact = ent.addFunction("myFact", tFact, tMessage, tMessage);

		IntroduceRetractEdge ir1 = new IntroduceRetractEdge(ent, firstNode, fncMyFact.term(varA.term(), varB.term()), true, null, builder);
		IntroduceRetractEdge ir2 = new IntroduceRetractEdge(ent, ir1.getTargetNode(), fncMyFact.term(varA.term(), varB.term()), true, null, builder);
		AssignmentEdge f3 = new AssignmentEdge(ent, ir2.getTargetNode(), varA.term(), varB.term(), false, null, builder);
		IntroduceRetractEdge ir4 = new IntroduceRetractEdge(ent, f3.getTargetNode(), fncMyFact.term(varA.term(), varB.term()), false, null, builder);

		mockt.addLhsStateItem(ent.getName(), new String[] { ent.getActorSymbol().getName(), ent.getIDSymbol().getName(), "1", varA.getName(), varB.getName(), varC.getName() });
		mockt.addRhsStateItem(ent.getName(), new String[] { ent.getActorSymbol().getName(), ent.getIDSymbol().getName(), "5", varB.getName(), varB.getName(), varC.getName() });
		mockt.addLhsItem(fncMyFact.getName(), new String[] { varB.getName(), varB.getName() });
		mockt.addRhsItem(fncMyFact.getName(), new String[] { varA.getName(), varB.getName() });
		mockt.addRhsItem(fncMyFact.getName(), new String[] { varA.getName(), varB.getName() });
		mockt.addParameters(new String[] { ent.getActorSymbol().getName(), ent.getIDSymbol().getName(), varA.getName(), varB.getName(), varC.getName() });
	}

	// while (Set->contains(?Elem)) {
	// Set->remove(Elem);
	// myFact(Elem);
	// }
	@SuppressWarnings("unused")
	public void testSetVisitElements() {
		VariableSymbol varSet = ent.addStateVariable("Set", Prelude.getSetOf(tMessage));
		VariableSymbol varElem = ent.addStateVariable("Elem", tMessage);
		FunctionSymbol fncContains = spec.findFunction(Prelude.CONTAINS);
		FunctionSymbol fncMyFact = ent.addFunction("myFact", tFact, tMessage);
		VariableTerm matchElem = varElem.matchedTerm();
		matchElem.accept(new PostProcessor(err));

		IExpression guard = fncContains.term(varSet.term(), matchElem).expression();
		GuardedEdge g1 = new GuardedEdge(ent, firstNode, guard, null, builder);
		IntroduceRetractEdge ir2 = new IntroduceRetractEdge(ent, g1.getTargetNode(), fncContains.term(varSet.term(), varElem.term()), false, null, builder);
		IntroduceRetractEdge ir3 = new IntroduceRetractEdge(ent, ir2.getTargetNode(), fncMyFact.term(varElem.term()), true, null, builder);

		mockt.addLhsStateItem(ent.getName(), new String[] { ent.getActorSymbol().getName(), ent.getIDSymbol().getName(), "1", varSet.getName(), varElem.getName() });
		mockt.addLhsItem(fncContains.getName(), new String[] { matchElem.getDummySymbol().getName(), varSet.getName() });
		mockt.addRhsStateItem(ent.getName(), new String[] { ent.getActorSymbol().getName(), ent.getIDSymbol().getName(), "4", varSet.getName(), matchElem.getDummySymbol().getName() });
		mockt.addRhsItem(fncMyFact.getName(), new String[] { matchElem.getDummySymbol().getName() });
		mockt.addParameters(new String[] { ent.getActorSymbol().getName(), ent.getIDSymbol().getName(), varSet.getName(), varElem.getName(), matchElem.getDummySymbol().getName() });
	}

	// if (?Requestor -> Actor : myFact(Requestor, ?Token))
	// myNewFact(Token);
	// public void testMultipleMatches() {
	// VariableSymbol varRequestor = ent.addStateVariable("Requestor", tAgent);
	// VariableSymbol varToken = ent.addStateVariable("Token", tMessage);
	// FunctionSymbol fncMyFact = ent.addFunction("myFact", tFact, tAgent,
	// tMessage);
	// FunctionSymbol fncMyNewFact = ent.addFunction("myNewFact", tFact,
	// tMessage);
	// VariableTerm matchRequestor = varRequestor.matchedTerm();
	// matchRequestor.accept(new PostProcessor());
	// VariableTerm matchToken = varToken.matchedTerm();
	// matchToken.accept(new PostProcessor());
	//
	// IExpression guard = ent.communication(matchRequestor,
	// ent.getActorSymbol().term(), fncMyFact.term(varRequestor.term(),
	// matchToken), Type.Regular, false, false, false).expression();
	// guard.accept(new PostProcessor());
	// GuardedEdge g1 = new GuardedEdge(ent, firstNode, guard, 0, builder);
	// IntroduceRetractEdge ie2 = new IntroduceRetractEdge(ent,
	// g1.getTargetNode(), fncMyNewFact.term(varToken.term()), true, 0,
	// builder);
	//
	// mockt.addLhsStateItem(ent.getName(), new String[] {
	// ent.getActorSymbol().getName(), ent.getIDSymbol().getName(), "1",
	// varRequestor.getName(), varToken.getName() });
	// mockt.addLhsItem("iknows",
	// new String[] { MockTransition.renderFunction(fncMyFact.getName(), new
	// String[] { matchRequestor.getDummySymbol().getName(),
	// matchToken.getDummySymbol().getName() }) });
	// mockt.addRhsStateItem(ent.getName(), new String[] {
	// ent.getActorSymbol().getName(), ent.getIDSymbol().getName(), "3",
	// matchRequestor.getDummySymbol().getName(),
	// matchToken.getDummySymbol().getName() });
	// mockt.addRhsItem(fncMyNewFact.getName(), new String[] {
	// matchToken.getDummySymbol().getName() });
	// mockt.addParameters(new String[] { ent.getActorSymbol().getName(),
	// ent.getIDSymbol().getName(), varRequestor.getName(),
	// matchRequestor.getDummySymbol().getName(), varToken.getName(),
	// matchToken.getDummySymbol().getName() });
	// }

	@SuppressWarnings("unused")
	public void testIntroduceWithMatch() {
		VariableSymbol vA = ent.addStateVariable("A", tNat);
		VariableSymbol vB = ent.addStateVariable("B", tAgent);
		FunctionSymbol myFnc = ent.addFunction("myFnc", tFact, tNat, tAgent);
		VariableTerm matchA = vA.matchedTerm();
		matchA.accept(new PostProcessor(err));
		VariableTerm matchB = vB.matchedTerm();
		matchB.accept(new PostProcessor(err));

		IntroduceRetractEdge ie = new IntroduceRetractEdge(ent, firstNode, myFnc.term(matchA, matchB), false, null, builder);

		mockt.addLhsStateItem(ent.getName(), new String[] { ent.getActorSymbol().getName(), ent.getIDSymbol().getName(), "1", vA.getName(), vB.getName() });
		mockt.addLhsItem(myFnc.getName(), new String[] { matchA.getDummySymbol().getName(), matchB.getDummySymbol().getName() });
		mockt.addRhsStateItem(ent.getName(), new String[] { ent.getActorSymbol().getName(), ent.getIDSymbol().getName(), "2", matchA.getDummySymbol().getName(), matchB.getDummySymbol().getName() });
		mockt.addParameters(new String[] { ent.getActorSymbol().getName(), ent.getIDSymbol().getName(), vA.getName(), matchA.getDummySymbol().getName(), vB.getName(),
				matchB.getDummySymbol().getName() });
	}

	@SuppressWarnings("unused")
	public void testGuardWithMatch() {
		VariableSymbol vA = ent.addStateVariable("A", tNat);
		VariableSymbol vB = ent.addStateVariable("B", tAgent);
		FunctionSymbol myFnc = ent.addFunction("myFnc", tFact, tNat, tAgent);
		VariableTerm matchA = vA.matchedTerm();
		matchA.accept(new PostProcessor(err));
		VariableTerm matchB = vB.matchedTerm();
		matchB.accept(new PostProcessor(err));

		GuardedEdge ge = new GuardedEdge(ent, firstNode, myFnc.term(matchA, matchB).expression(), null, builder);

		mockt.addLhsStateItem(ent.getName(), new String[] { ent.getActorSymbol().getName(), ent.getIDSymbol().getName(), "1", vA.getName(), vB.getName() });
		mockt.addLhsItem(myFnc.getName(), new String[] { matchA.getDummySymbol().getName(), matchB.getDummySymbol().getName() });
		mockt.addRhsStateItem(ent.getName(), new String[] { ent.getActorSymbol().getName(), ent.getIDSymbol().getName(), "2", matchA.getDummySymbol().getName(), matchB.getDummySymbol().getName() });
		mockt.addRhsItem(myFnc.getName(), new String[] { matchA.getDummySymbol().getName(), matchB.getDummySymbol().getName() });
		mockt.addParameters(new String[] { ent.getActorSymbol().getName(), ent.getIDSymbol().getName(), vA.getName(), matchA.getDummySymbol().getName(), vB.getName(),
				matchB.getDummySymbol().getName() });
	}

	// public void testIntroduceWithCommunication() {
	// VariableSymbol varRequestor = ent.addStateVariable("Requestor", tAgent);
	// VariableSymbol varToken = ent.addStateVariable("Token", tMessage);
	// FunctionSymbol fncMyFact = ent.addFunction("myFact", tFact, tAgent,
	// tMessage);
	// FunctionSymbol fncMyNewFact = ent.addFunction("myNewFact", tFact,
	// tMessage);
	// VariableTerm matchRequestor = varRequestor.matchedTerm();
	// matchRequestor.accept(new PostProcessor());
	// VariableTerm matchToken = varToken.matchedTerm();
	// matchToken.accept(new PostProcessor());
	//
	// ITerm term = ent.communication(matchRequestor,
	// ent.getActorSymbol().term(), fncMyFact.term(varRequestor.term(),
	// matchToken), Type.Regular, false, false, false);
	// term.accept(new PostProcessor());
	// IntroduceRetractEdge ie = new IntroduceRetractEdge(ent, firstNode, term,
	// false, 0, builder);
	//
	// mockt.addLhsStateItem(ent.getName(), new String[] {
	// ent.getActorSymbol().getName(), ent.getIDSymbol().getName(), "1",
	// varRequestor.getName(), varToken.getName() });
	// mockt.addLhsItem("iknows",
	// new String[] { MockTransition.renderFunction(fncMyFact.getName(), new
	// String[] { matchRequestor.getDummySymbol().getName(),
	// matchToken.getDummySymbol().getName() }) });
	// mockt.addRhsStateItem(ent.getName(), new String[] {
	// ent.getActorSymbol().getName(), ent.getIDSymbol().getName(), "2",
	// matchRequestor.getDummySymbol().getName(),
	// matchToken.getDummySymbol().getName() });
	// mockt.addParameters(new String[] { ent.getActorSymbol().getName(),
	// ent.getIDSymbol().getName(), varRequestor.getName(),
	// matchRequestor.getDummySymbol().getName(), varToken.getName(),
	// matchToken.getDummySymbol().getName() });
	// }

	// public void testGuardWithCommunication() {
	// VariableSymbol varRequestor = ent.addStateVariable("Requestor", tAgent);
	// VariableSymbol varToken = ent.addStateVariable("Token", tMessage);
	// FunctionSymbol fncMyFact = ent.addFunction("myFact", tFact, tAgent,
	// tMessage);
	// FunctionSymbol fncMyNewFact = ent.addFunction("myNewFact", tFact,
	// tMessage);
	// VariableTerm matchRequestor = varRequestor.matchedTerm();
	// matchRequestor.accept(new PostProcessor());
	// VariableTerm matchToken = varToken.matchedTerm();
	// matchToken.accept(new PostProcessor());
	//
	// IExpression guard = ent.communication(matchRequestor,
	// ent.getActorSymbol().term(), fncMyFact.term(varRequestor.term(),
	// matchToken), Type.Regular, false, false, false).expression();
	// guard.accept(new PostProcessor());
	// GuardedEdge g1 = new GuardedEdge(ent, firstNode, guard, 0, builder);
	//
	// mockt.addLhsStateItem(ent.getName(), new String[] {
	// ent.getActorSymbol().getName(), ent.getIDSymbol().getName(), "1",
	// varRequestor.getName(), varToken.getName() });
	// mockt.addLhsItem("iknows",
	// new String[] { MockTransition.renderFunction(fncMyFact.getName(), new
	// String[] { matchRequestor.getDummySymbol().getName(),
	// matchToken.getDummySymbol().getName() }) });
	// mockt.addRhsStateItem(ent.getName(), new String[] {
	// ent.getActorSymbol().getName(), ent.getIDSymbol().getName(), "2",
	// matchRequestor.getDummySymbol().getName(),
	// matchToken.getDummySymbol().getName() });
	// mockt.addParameters(new String[] { ent.getActorSymbol().getName(),
	// ent.getIDSymbol().getName(), varRequestor.getName(),
	// matchRequestor.getDummySymbol().getName(), varToken.getName(),
	// matchToken.getDummySymbol().getName() });
	// }

	// A := a;
	// myFact(A);
	// A := b;
	// myFact(A);
	// A := c;
	// myFact(A);
	@SuppressWarnings("unused")
	public void testWithConstants() {
		ConstantSymbol constA = ent.constants(tMessage, "a");
		ConstantSymbol constB = ent.constants(tMessage, "b");
		ConstantSymbol constC = ent.constants(tMessage, "c");
		VariableSymbol varA = ent.addStateVariable("A", tMessage);
		FunctionSymbol fncMyFact = ent.addFunction("myFact", tFact, tMessage);

		AssignmentEdge a1 = new AssignmentEdge(ent, firstNode, varA.term(), constA.term(), false, null, builder);
		IntroduceRetractEdge ir2 = new IntroduceRetractEdge(ent, a1.getTargetNode(), fncMyFact.term(varA.term()), true, null, builder);
		AssignmentEdge a3 = new AssignmentEdge(ent, ir2.getTargetNode(), varA.term(), constB.term(), false, null, builder);
		IntroduceRetractEdge ir4 = new IntroduceRetractEdge(ent, a3.getTargetNode(), fncMyFact.term(varA.term()), true, null, builder);
		AssignmentEdge a5 = new AssignmentEdge(ent, ir4.getTargetNode(), varA.term(), constC.term(), false, null, builder);
		IntroduceRetractEdge ir6 = new IntroduceRetractEdge(ent, a5.getTargetNode(), fncMyFact.term(varA.term()), true, null, builder);

		mockt.addLhsStateItem(ent.getName(), new String[] { ent.getActorSymbol().getName(), ent.getIDSymbol().getName(), "1", varA.getName() });
		mockt.addRhsStateItem(ent.getName(), new String[] { ent.getActorSymbol().getName(), ent.getIDSymbol().getName(), "7", constC.getName() });
		mockt.addRhsItem(fncMyFact.getName(), new String[] { constA.getName() });
		mockt.addRhsItem(fncMyFact.getName(), new String[] { constB.getName() });
		mockt.addRhsItem(fncMyFact.getName(), new String[] { constC.getName() });
		mockt.addParameters(new String[] { ent.getActorSymbol().getName(), ent.getIDSymbol().getName(), varA.getName() });
	}

	// A := a;
	// B := b;
	// C := c;
	// myFact(A, B, C);
	// A := B;
	// B := C;
	// C := A;
	// myFact(A, B, C);
	@SuppressWarnings("unused")
	public void testWithConstantsMixedWithVars() {
		ConstantSymbol constA = ent.constants(tMessage, "a");
		ConstantSymbol constB = ent.constants(tMessage, "b");
		ConstantSymbol constC = ent.constants(tMessage, "c");
		VariableSymbol varA = ent.addStateVariable("A", tMessage);
		VariableSymbol varB = ent.addStateVariable("B", tMessage);
		VariableSymbol varC = ent.addStateVariable("C", tMessage);
		FunctionSymbol fncMyFact = ent.addFunction("myFact", tFact, tMessage, tMessage, tMessage);

		AssignmentEdge a1 = new AssignmentEdge(ent, firstNode, varA.term(), constA.term(), false, null, builder);
		AssignmentEdge a2 = new AssignmentEdge(ent, a1.getTargetNode(), varB.term(), constB.term(), false, null, builder);
		AssignmentEdge a3 = new AssignmentEdge(ent, a2.getTargetNode(), varC.term(), constC.term(), false, null, builder);
		IntroduceRetractEdge ir4 = new IntroduceRetractEdge(ent, a3.getTargetNode(), fncMyFact.term(varA.term(), varB.term(), varC.term()), true, null, builder);
		AssignmentEdge a5 = new AssignmentEdge(ent, ir4.getTargetNode(), varA.term(), varB.term(), false, null, builder);
		AssignmentEdge a6 = new AssignmentEdge(ent, a5.getTargetNode(), varB.term(), varC.term(), false, null, builder);
		AssignmentEdge a7 = new AssignmentEdge(ent, a6.getTargetNode(), varC.term(), varA.term(), false, null, builder);
		IntroduceRetractEdge ir8 = new IntroduceRetractEdge(ent, a7.getTargetNode(), fncMyFact.term(varA.term(), varB.term(), varC.term()), true, null, builder);

		mockt.addLhsStateItem(ent.getName(), new String[] { ent.getActorSymbol().getName(), ent.getIDSymbol().getName(), "1", varA.getName(), varB.getName(), varC.getName() });
		mockt.addRhsStateItem(ent.getName(), new String[] { ent.getActorSymbol().getName(), ent.getIDSymbol().getName(), "9", constB.getName(), constC.getName(), constB.getName() });
		mockt.addRhsItem(fncMyFact.getName(), new String[] { constA.getName(), constB.getName(), constC.getName() });
		mockt.addRhsItem(fncMyFact.getName(), new String[] { constB.getName(), constC.getName(), constB.getName() });
		mockt.addParameters(new String[] { ent.getActorSymbol().getName(), ent.getIDSymbol().getName(), varA.getName(), varB.getName(), varC.getName() });
	}

	// A := a;
	// B := b;
	// C := c;
	// D := {A, B, C};
	// myFact(D);
	@SuppressWarnings("unused")
	public void testSetAssignment() {
		ConstantSymbol constA = ent.constants(tMessage, "a");
		ConstantSymbol constB = ent.constants(tMessage, "b");
		ConstantSymbol constC = ent.constants(tMessage, "c");
		VariableSymbol varA = ent.addStateVariable("A", tMessage);
		VariableSymbol varB = ent.addStateVariable("B", tMessage);
		VariableSymbol varC = ent.addStateVariable("C", tMessage);
		VariableSymbol varD = ent.addStateVariable("D", Prelude.getSetOf(tMessage));
		FunctionSymbol fncMyFact = ent.addFunction("myFact", tFact, Prelude.getSetOf(tMessage));

		SetLiteralTerm set = new SetLiteralTerm(null, ent, new ITerm[] { varA.term(), varB.term(), varC.term() }, null);
		set.accept(new PostProcessor(err));
		AssignmentEdge a1 = new AssignmentEdge(ent, firstNode, varA.term(), constA.term(), false, null, builder);
		AssignmentEdge a2 = new AssignmentEdge(ent, a1.getTargetNode(), varB.term(), constB.term(), false, null, builder);
		AssignmentEdge a3 = new AssignmentEdge(ent, a2.getTargetNode(), varC.term(), constC.term(), false, null, builder);
		AssignmentEdge a4 = new AssignmentEdge(ent, a3.getTargetNode(), varD.term(), set, false, null, builder);
		IntroduceRetractEdge ir4 = new IntroduceRetractEdge(ent, a4.getTargetNode(), fncMyFact.term(varD.term()), true, null, builder);

		String setTerm = MockTransition.renderFunction(set.getSymbol().getName(), new String[] { ent.getIDSymbol().getName() });
		mockt.addLhsStateItem(ent.getName(), new String[] { ent.getActorSymbol().getName(), ent.getIDSymbol().getName(), "1", varA.getName(), varB.getName(), varC.getName(), varD.getName() });
		mockt.addRhsStateItem(ent.getName(), new String[] { ent.getActorSymbol().getName(), ent.getIDSymbol().getName(), "6", constA.getName(), constB.getName(), constC.getName(), setTerm });
		mockt.addRhsItem(Prelude.CONTAINS, new String[] { constA.getName(), setTerm });
		mockt.addRhsItem(Prelude.CONTAINS, new String[] { constB.getName(), setTerm });
		mockt.addRhsItem(Prelude.CONTAINS, new String[] { constC.getName(), setTerm });
		mockt.addRhsItem(fncMyFact.getName(), new String[] { setTerm });
		mockt.addParameters(new String[] { ent.getActorSymbol().getName(), ent.getIDSymbol().getName(), varA.getName(), varB.getName(), varC.getName(), varD.getName() });
	}

	// % Env has variable A
	// % Child has variables B and C
	// A := a;
	// new Child(A, c);
	// A := b;
	// new Child(A, c);
	// A := c;
	// new Child(A, b.c);
	@SuppressWarnings("unused")
	public void testNewInstance() {
		Entity childEnt = ent.entity("Child");
		// childEnt.assignIndexes(1);

		VariableSymbol varA = ent.addStateVariable("A", tMessage);
		VariableSymbol varB = childEnt.addParameter("B", tMessage);
		VariableSymbol varC = childEnt.addParameter("C", tMessage);
		ConstantSymbol constA = ent.constants(tMessage, "a");
		ConstantSymbol constB = ent.constants(tMessage, "b");
		ConstantSymbol constC = ent.constants(tMessage, "c");
		VariableSymbol freshID1 = ent.addVariable("IID_1", tNat);
		VariableSymbol freshID2 = ent.addVariable("IID_2", tNat);
		VariableSymbol freshID3 = ent.addVariable("IID_3", tNat);

		AssignmentEdge a1 = new AssignmentEdge(ent, firstNode, varA.term(), constA.term(), false, null, builder);
		NewInstanceEdge ni2 = new NewInstanceEdge(ent, a1.getTargetNode(), childEnt, new ITerm[] { varA.term(), constC.term() }, freshID1, null, null, builder);
		AssignmentEdge a3 = new AssignmentEdge(ent, ni2.getTargetNode(), varA.term(), constB.term(), false, null, builder);
		NewInstanceEdge ni4 = new NewInstanceEdge(ent, a3.getTargetNode(), childEnt, new ITerm[] { varA.term(), constC.term() }, freshID2, null, null, builder);
		AssignmentEdge a5 = new AssignmentEdge(ent, ni4.getTargetNode(), varA.term(), constC.term(), false, null, builder);
		NewInstanceEdge ni6 = new NewInstanceEdge(ent, a5.getTargetNode(), childEnt, new ITerm[] { varA.term(), ConcatTerm.concat(ent, constB.term(), constC.term()) }, freshID3, null, null, builder);

		mockt.addLhsStateItem(ent.getName(), new String[] { ent.getActorSymbol().getName(), ent.getIDSymbol().getName(), "1", varA.getName() });
		mockt.addRhsStateItem(ent.getName(), new String[] { ent.getActorSymbol().getName(), ent.getIDSymbol().getName(), "7", constC.getName() });
		mockt.addRhsStateItem(childEnt.getName(), new String[] { "dummy_agent", freshID1.getName(), "1", constA.getName(), constC.getName() });
		mockt.addRhsStateItem(childEnt.getName(), new String[] { "dummy_agent", freshID2.getName(), "1", constB.getName(), constC.getName() });
		mockt.addRhsStateItem(childEnt.getName(), new String[] { "dummy_agent", freshID3.getName(), "1", constC.getName(),
				MockTransition.renderFunction(Prelude.PAIR, new String[] { constB.getName(), constC.getName() }) });
		mockt.addRhsItem(Prelude.CHILD, new String[] { ent.getIDSymbol().getName(), freshID1.getName() });
		mockt.addRhsItem(Prelude.CHILD, new String[] { ent.getIDSymbol().getName(), freshID2.getName() });
		mockt.addRhsItem(Prelude.CHILD, new String[] { ent.getIDSymbol().getName(), freshID3.getName() });
		mockt.addFreshVar(freshID1.getName());
		mockt.addFreshVar(freshID2.getName());
		mockt.addFreshVar(freshID3.getName());
		mockt.addParameters(new String[] { ent.getActorSymbol().getName(), ent.getIDSymbol().getName(), varA.getName(), freshID1.getName(), freshID2.getName(), freshID3.getName() });
	}

	// % Env has variable A
	// % Child has variables B and C
	// any E F . Child(E, F) where E = A;
	// A := b;
	// any E . Child(A, E);
	@SuppressWarnings("unused")
	public void testSymbolicInstance() {
		Entity childEnt = ent.entity("Child");
		// childEnt.assignIndexes(1);

		VariableSymbol varA = ent.addStateVariable("A", tMessage);
		VariableSymbol varB = childEnt.addParameter("B", tMessage);
		VariableSymbol varC = childEnt.addParameter("C", tMessage);
		ConstantSymbol constA = ent.constants(tMessage, "a");
		ConstantSymbol constB = ent.constants(tMessage, "b");
		ConstantSymbol constC = ent.constants(tMessage, "c");
		VariableSymbol varE = ent.addUntypedVariable("E");
		VariableSymbol varF = ent.addUntypedVariable("F");
		VariableSymbol freshID1 = ent.addVariable("IID_1", tNat);
		VariableSymbol freshID2 = ent.addVariable("IID_2", tNat);

		VariableTerm termE = varE.term();
		VariableTerm termF = varF.term();
		IExpression guard = termE.equality(varA.term());

		SymbolicInstanceEdge si1 = new SymbolicInstanceEdge(ent, firstNode, childEnt, new ITerm[] { termE, termF }, new VariableSymbol[] { varE, varF }, guard, freshID1, null, null, builder);
		AssignmentEdge a2 = new AssignmentEdge(ent, si1.getTargetNode(), varA.term(), constB.term(), false, null, builder);
		SymbolicInstanceEdge si3 = new SymbolicInstanceEdge(ent, a2.getTargetNode(), childEnt, new ITerm[] { varA.term(), termE }, new VariableSymbol[] { varE }, null, freshID2, null, null, builder);

		mockt.addLhsStateItem(ent.getName(), new String[] { ent.getActorSymbol().getName(), ent.getIDSymbol().getName(), "1", varA.getName() });
		mockt.addRhsStateItem(ent.getName(), new String[] { ent.getActorSymbol().getName(), ent.getIDSymbol().getName(), "4", constB.getName() });
		mockt.addRhsStateItem(childEnt.getName(), new String[] { "dummy_agent", freshID1.getName(), "1", varE.getName(), varF.getName() });
		mockt.addRhsStateItem(childEnt.getName(), new String[] { "dummy_agent", freshID2.getName(), "1", constB.getName(), varE.getName() });
		mockt.addRhsItem(Prelude.CHILD, new String[] { ent.getIDSymbol().getName(), freshID1.getName() });
		mockt.addRhsItem(Prelude.CHILD, new String[] { ent.getIDSymbol().getName(), freshID2.getName() });
		mockt.addFreshVar(Entity.ID_PREFIX + "_1");
		mockt.addFreshVar(Entity.ID_PREFIX + "_2");
		mockt.addParameters(new String[] { ent.getActorSymbol().getName(), ent.getIDSymbol().getName(), varA.getName(), freshID1.getName(), freshID2.getName(), varE.getName(), varF.getName() });
		mockt.addLhsItem(Prelude.IKNOWS, new String[] { varE.getName() });
		mockt.addLhsItem(Prelude.IKNOWS, new String[] { varF.getName() });
		mockt.addCondition("equal", new String[] { varE.getName(), varA.getName() });
		mockt.addRhsItem(Prelude.IKNOWS, new String[] { varE.getName() });
		mockt.addRhsItem(Prelude.IKNOWS, new String[] { varF.getName() });
	}

	@Override
	public void tearDown() {
		spec.accept(new PostProcessor(err));
		spec.accept(builder);

		SymbolsState symState = new SymbolsState();
		INode optNode = firstNode.optimize(null);
		optNode.assignIndexes(new Counters(1, 1));
		optNode.computeBigSmallState(new ArrayList<String>());
		optNode.clearVisited();
		optNode.gatherTransitionsLumped(builder.getASLanSpecification(), null, symState, new TreeSet<Integer>(), 1, 1, new TransitionsRecorder());

		Assert.assertEquals("There should be exactly one transition", 1, builder.getASLanSpecification().getRules().size());
		RewriteRule rr = builder.getASLanSpecification().getRules().get(0);

		System.out.println(mockt);
		System.out.println();
		org.avantssar.aslan.PrettyPrinter rulePP = new org.avantssar.aslan.PrettyPrinter(true);
		rr.accept(rulePP);
		System.out.println(rulePP.toString());

		Assert.assertEquals(mockt.getRepresentation(), rulePP.toString());
	}
}
