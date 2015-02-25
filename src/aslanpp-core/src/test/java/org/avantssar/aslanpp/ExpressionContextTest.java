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

import junit.framework.TestCase;
import org.avantssar.aslanpp.model.ASLanPPSpecification;
import org.avantssar.aslanpp.model.ConcatTerm;
import org.avantssar.aslanpp.model.ConstantSymbol;
import org.avantssar.aslanpp.model.ConstantTerm;
import org.avantssar.aslanpp.model.DefaultPseudonymTerm;
import org.avantssar.aslanpp.model.Entity;
import org.avantssar.aslanpp.model.EntityManager;
import org.avantssar.aslanpp.model.ErrorMessages;
import org.avantssar.aslanpp.model.ExpressionContext;
import org.avantssar.aslanpp.model.FunctionSymbol;
import org.avantssar.aslanpp.model.FunctionTerm;
import org.avantssar.aslanpp.model.ITerm;
import org.avantssar.aslanpp.model.IType;
import org.avantssar.aslanpp.model.Prelude;
import org.avantssar.aslanpp.model.PseudonymTerm;
import org.avantssar.aslanpp.model.SetLiteralTerm;
import org.avantssar.aslanpp.model.SymbolsState;
import org.avantssar.aslanpp.model.TupleTerm;
import org.avantssar.aslanpp.model.UnnamedMatchTerm;
import org.avantssar.aslanpp.model.VariableSymbol;
import org.avantssar.aslanpp.model.VariableTerm;
import org.avantssar.aslanpp.visitors.DummySymbolsCreator;
import org.avantssar.aslanpp.visitors.PostProcessor;
import org.avantssar.commons.ChannelModel;
import org.avantssar.commons.ErrorGatherer;

public class ExpressionContextTest extends TestCase {

	private Entity ent;
	private ExpressionContext ctx;
	private SymbolsState symState;
	private ASLanPPSpecification spec;
	private ErrorGatherer err;

	private IType tMessage;
	private IType tText;
	private IType tAgent;
	private IType tFact;

	private EntityManager manager;

	public ExpressionContextTest(String testName) {
		super(testName);
	}

	@Override
	public void setUp() {
		manager = new EntityManager();
		spec = new ASLanPPSpecification(manager, "spec", ChannelModel.CCM);
		err = new ErrorGatherer(ErrorMessages.DEFAULT);
		ent = spec.entity("Ent");
		ctx = new ExpressionContext();
		symState = new SymbolsState();

		tMessage = spec.findType(Prelude.MESSAGE);
		tText = spec.findType(Prelude.TEXT);
		tAgent = spec.findType(Prelude.AGENT);
		tFact = spec.findType(Prelude.FACT);

		assertEquals(false, ctx.hasBreakpoint());
		assertEquals(0, ctx.getAuxiliaryTerms().size());
		assertEquals(0, ctx.getOwners().size());
		assertEquals(0, ctx.getCommunicationStatistics().receives);
		assertEquals(0, ctx.getCommunicationStatistics().sends);
	}

	public void testConstantTerm() {
		ConstantSymbol constA = ent.constants(tMessage, "a");
		ConstantTerm term = constA.term();
		term.buildContext(ctx, false);
		term.useContext(ctx, symState);

		assertEquals(term.getRepresentation(), term.reduce(symState).getRepresentation());
		System.out.println(term.getRepresentation() + " <-> " + term.reduce(symState).getRepresentation());

		expect(false, false, 0, 0, 0, 0, 0, 0);
	}

	public void testConstantTermWithBreakpoints() {
		ctx = new ExpressionContext(false, new String[] { "a" });

		ConstantSymbol constA = ent.constants(tMessage, "a");
		ConstantTerm term = constA.term();
		term.buildContext(ctx, false);
		term.useContext(ctx, symState);

		assertEquals(term.getRepresentation(), term.reduce(symState).getRepresentation());
		System.out.println(term.getRepresentation() + " <-> " + term.reduce(symState).getRepresentation());

		expect(true, false, 0, 0, 0, 0, 0, 0);
	}

	public void testVariableTermRegular() {
		VariableSymbol varA = ent.addStateVariable("A", tMessage);
		VariableTerm term = varA.term();
		term.buildContext(ctx, false);
		term.useContext(ctx, symState);

		assertEquals(term.getRepresentation(), term.reduce(symState).getRepresentation());
		System.out.println(term.getRepresentation() + " <-> " + term.reduce(symState).getRepresentation());

		expect(false, false, 0, 0, 1, 1, 0, 0);
		assertTrue(ctx.getOwners().contains(ent));
	}

	public void testVariableTermRegularActor() {
		VariableSymbol varA = ent.getActorSymbol();
		VariableTerm term = varA.term();
		term.buildContext(ctx, false);
		term.useContext(ctx, symState);

		assertEquals(term.getRepresentation(), term.reduce(symState).getRepresentation());
		System.out.println(term.getRepresentation() + " <-> " + term.reduce(symState).getRepresentation());

		expect(false, true, 0, 0, 1, 1, 0, 0);
		assertTrue(ctx.getOwners().contains(ent));
	}

	public void testVariableTermFresh() {
		VariableSymbol varA = ent.addStateVariable("A", tMessage);
		VariableTerm term = varA.freshTerm();
		term.buildContext(ctx, false);
		term.useContext(ctx, symState);

		assertEquals(term.getRepresentation(), term.reduce(symState).getRepresentation());
		System.out.println(term.getRepresentation() + " <-> " + term.reduce(symState).getRepresentation());

		expect(false, false, 0, 1, 1, 0, 0, 0);
		assertTrue(ctx.getOwners().contains(ent));
	}

	public void testVariableTermMatchedNotForced() {
		VariableSymbol varA = ent.addStateVariable("A", tMessage);
		VariableTerm term = varA.matchedTerm();
		term.accept(new PostProcessor(err));
		term.buildContext(ctx, false);
		term.useContext(ctx, symState);

		assertEquals(term.getDummySymbol().term().getRepresentation(), term.reduce(symState).getRepresentation());
		System.out.println(term.getRepresentation() + " <-> " + term.reduce(symState).getRepresentation());

		expect(false, false, 0, 0, 1, 2, 0, 0);
		assertTrue(ctx.getOwners().contains(ent));
	}

	public void testVariableTermMatchedForced() {
		ctx = new ExpressionContext(true);

		VariableSymbol varA = ent.addStateVariable("A", tMessage);
		VariableTerm term = varA.matchedTerm();
		term.accept(new PostProcessor(err));
		term.buildContext(ctx, false);
		term.useContext(ctx, symState);

		assertEquals(term.getDummySymbol().term().getRepresentation(), term.reduce(symState).getRepresentation());
		System.out.println(term.getRepresentation() + " <-> " + term.reduce(symState).getRepresentation());

		expect(false, false, 0, 0, 1, 2, 0, 0);
		assertTrue(ctx.getOwners().contains(ent));
	}

	public void testVariableTermMatchedNegatedNotForced() {
		VariableSymbol varA = ent.addStateVariable("A", tMessage);
		VariableTerm term = varA.matchedTerm();
		term.accept(new PostProcessor(err));
		term.buildContext(ctx, true);
		term.useContext(ctx, symState);

		assertEquals(term.getRepresentation(), term.reduce(symState).getRepresentation());
		System.out.println(term.getRepresentation() + " <-> " + term.reduce(symState).getRepresentation());

		// TODO: negated match not forced should bring on just one parameter
		expect(false, false, 0, 0, 1, 2, 0, 0);
		assertTrue(ctx.getOwners().contains(ent));
	}

	public void testVariableTermMatchedNegatedForced() {
		ctx = new ExpressionContext(true);

		VariableSymbol varA = ent.addStateVariable("A", tMessage);
		VariableTerm term = varA.matchedTerm();
		term.accept(new PostProcessor(err));
		term.buildContext(ctx, true);
		term.useContext(ctx, symState);

		assertEquals(term.getDummySymbol().term().getRepresentation(), term.reduce(symState).getRepresentation());
		System.out.println(term.getRepresentation() + " <-> " + term.reduce(symState).getRepresentation());

		// TODO: negated match not forced should bring on just one parameter
		expect(false, false, 0, 0, 1, 2, 0, 0);
		assertTrue(ctx.getOwners().contains(ent));
	}

	public void testUntypedVariableTerm() {
		VariableSymbol varA = ent.addUntypedVariable("A");
		varA.setType(tText);
		VariableTerm term = varA.term();
		term.buildContext(ctx, false);
		term.useContext(ctx, symState);

		assertEquals(term.getRepresentation(), term.reduce(symState).getRepresentation());
		System.out.println(term.getRepresentation() + " <-> " + term.reduce(symState).getRepresentation());

		expect(false, false, 0, 0, 0, 1, 0, 0);
	}

	public void testFunctionTerm() {
		VariableSymbol varA = ent.addStateVariable("A", tText);
		VariableSymbol varB = ent.addStateVariable("B", tAgent);
		ConstantSymbol constC = ent.constants(tMessage, "c");
		VariableSymbol varD = ent.addUntypedVariable("D");
		varD.setType(tText);
		FunctionSymbol fncMyFact = ent.addFunction("myFact", tFact, tText, tAgent, tMessage, tText);

		VariableTerm matchB = varB.matchedTerm();
		matchB.accept(new PostProcessor(err));

		FunctionTerm term = fncMyFact.term(new ITerm[] { varA.term(), matchB, constC.term(), varD.term() });
		term.buildContext(ctx, false);
		term.useContext(ctx, symState);

		FunctionTerm redTerm = fncMyFact.term(new ITerm[] { varA.term(), matchB.getDummySymbol().term(), constC.term(), varD.term() });
		assertEquals(redTerm.getRepresentation(), term.reduce(symState).getRepresentation());
		System.out.println(term.getRepresentation() + " <-> " + term.reduce(symState).getRepresentation());

		expect(false, false, 0, 0, 1, 4, 0, 0);
		assertTrue(ctx.getOwners().contains(ent));
	}

	public void testFunctionTermNestedWithBreakpoints() {
		ctx = new ExpressionContext(false, new String[] { "one", "two" });

		VariableSymbol varA = ent.addStateVariable("A", tText);
		VariableSymbol varB = ent.addStateVariable("B", tAgent);
		ConstantSymbol constC = ent.constants(tMessage, "c");
		VariableSymbol varD = ent.addUntypedVariable("D");
		varD.setType(tText);
		FunctionSymbol fncOne = ent.addFunction("one", tText, tText, tText);
		FunctionSymbol fncTwo = ent.addFunction("two", tText, tMessage, tText);
		FunctionSymbol fncMyFact = ent.addFunction("myFact", tFact, tText, tMessage);

		VariableTerm matchB = varB.matchedTerm();
		matchB.accept(new PostProcessor(err));

		FunctionTerm termOne = fncOne.term(new ITerm[] { varA.freshTerm(), matchB });
		FunctionTerm termTwo = fncTwo.term(new ITerm[] { constC.term(), varD.term() });
		FunctionTerm term = fncMyFact.term(new ITerm[] { termOne, termTwo });
		term.buildContext(ctx, false);
		term.useContext(ctx, symState);

		FunctionTerm redTerm = fncMyFact.term(new ITerm[] { fncOne.term(new ITerm[] { varA.freshTerm(), matchB.getDummySymbol().term() }), termTwo });
		assertEquals(redTerm.getRepresentation(), term.reduce(symState).getRepresentation());
		System.out.println(term.getRepresentation() + " <-> " + term.reduce(symState).getRepresentation());

		expect(true, false, 0, 1, 1, 3, 0, 0);
		assertTrue(ctx.getOwners().contains(ent));
	}

	public void testUnnamedMatchTerm() {
		UnnamedMatchTerm term = ent.unnamedMatch();
		term.accept(new DummySymbolsCreator(err));
		term.buildContext(ctx, false);
		term.useContext(ctx, symState);

		assertEquals(term.getRepresentation(), term.reduce(symState).getRepresentation());
		System.out.println(term.getRepresentation() + " <-> " + term.reduce(symState).getRepresentation());

		expect(false, false, 0, 0, 0, 1, 0, 0);
	}

	public void testDefaultPseudonymTerm() {
		VariableSymbol varActor = ent.getActorSymbol();

		DefaultPseudonymTerm term = varActor.term().defaultPseudonym();
		term.buildContext(ctx, false);
		term.useContext(ctx, symState);

		assertEquals(term.getRepresentation(), term.reduce(symState).getRepresentation());
		System.out.println(term.getRepresentation() + " <-> " + term.reduce(symState).getRepresentation());

		expect(false, true, 0, 0, 1, 1, 0, 0);
		assertTrue(ctx.getOwners().contains(ent));
	}

	public void testPseudonymTerm() {
		VariableSymbol varActor = ent.getActorSymbol();
		VariableSymbol varB = ent.addVariable("B", tAgent);

		PseudonymTerm term = varActor.term().pseudonym(varB.term());
		term.buildContext(ctx, false);
		term.useContext(ctx, symState);

		assertEquals(term.getRepresentation(), term.reduce(symState).getRepresentation());
		System.out.println(term.getRepresentation() + " <-> " + term.reduce(symState).getRepresentation());

		expect(false, true, 0, 0, 1, 2, 0, 0);
		assertTrue(ctx.getOwners().contains(ent));
	}

	public void testPseudonymWithActorInPseudonymTerm() {
		VariableSymbol varActor = ent.getActorSymbol();
		VariableSymbol varB = ent.addVariable("B", tAgent);

		PseudonymTerm term = varB.term().pseudonym(varActor.term());
		term.buildContext(ctx, false);
		term.useContext(ctx, symState);

		assertEquals(term.getRepresentation(), term.reduce(symState).getRepresentation());
		System.out.println(term.getRepresentation() + " <-> " + term.reduce(symState).getRepresentation());

		expect(false, true, 0, 0, 1, 2, 0, 0);
		assertTrue(ctx.getOwners().contains(ent));
	}

	public void testConcatTerm() {
		VariableSymbol varA = ent.addStateVariable("A", tText);
		VariableSymbol varB = ent.addStateVariable("B", tAgent);
		ConstantSymbol constC = ent.constants(tMessage, "c");
		VariableSymbol varD = ent.addUntypedVariable("D");
		varD.setType(tText);

		VariableTerm matchB = varB.matchedTerm();
		matchB.accept(new PostProcessor(err));

		ConcatTerm term = ConcatTerm.concat(ent, varA.freshTerm(), matchB, constC.term(), varD.term(), ent.getActorSymbol().term());
		term.buildContext(ctx, false);
		term.useContext(ctx, symState);

		ConcatTerm redTerm = ConcatTerm.concat(ent, varA.freshTerm(), matchB.getDummySymbol().term(), constC.term(), varD.term(), ent.getActorSymbol().term());
		assertEquals(redTerm.getRepresentation(), term.reduce(symState).getRepresentation());
		System.out.println(term.getRepresentation() + " <-> " + term.reduce(symState).getRepresentation());

		expect(false, true, 0, 1, 1, 4, 0, 0);
		assertTrue(ctx.getOwners().contains(ent));
	}

	public void testTupleTerm() {
		VariableSymbol varA = ent.addStateVariable("A", tText);
		VariableSymbol varB = ent.addStateVariable("B", tAgent);
		ConstantSymbol constC = ent.constants(tMessage, "c");
		VariableSymbol varD = ent.addUntypedVariable("D");
		varD.setType(tText);

		VariableTerm matchB = varB.matchedTerm();
		matchB.accept(new PostProcessor(err));

		TupleTerm term = TupleTerm.tuple(ent, varA.freshTerm(), matchB, constC.term(), varD.term(), ent.getActorSymbol().term());
		term.buildContext(ctx, false);
		term.useContext(ctx, symState);

		TupleTerm redTerm = TupleTerm.tuple(ent, varA.freshTerm(), matchB.getDummySymbol().term(), constC.term(), varD.term(), ent.getActorSymbol().term());
		assertEquals(redTerm.getRepresentation(), term.reduce(symState).getRepresentation());
		System.out.println(term.getRepresentation() + " <-> " + term.reduce(symState).getRepresentation());

		expect(false, true, 0, 1, 1, 4, 0, 0);
		assertTrue(ctx.getOwners().contains(ent));
	}

	public void testSetLiteralTerm() {
		VariableSymbol varA = ent.addStateVariable("A", tText);
		VariableSymbol varB = ent.addStateVariable("B", tAgent);
		ConstantSymbol constC = ent.constants(tMessage, "c");
		VariableSymbol varD = ent.addUntypedVariable("D");
		varD.setType(tText);

		FunctionSymbol fncContains = spec.findFunction(Prelude.CONTAINS);

		VariableTerm matchB = varB.matchedTerm();
		matchB.accept(new PostProcessor(err));

		SetLiteralTerm term = new SetLiteralTerm(null, ent, new ITerm[] { varA.freshTerm(), matchB, constC.term(), varD.term(), ent.getActorSymbol().term() }, null);
		term.accept(new PostProcessor(err));
		term.buildContext(ctx, false);
		term.useContext(ctx, symState);

		SetLiteralTerm redTerm = new SetLiteralTerm(null, ent, new ITerm[] { varA.freshTerm(), matchB.getDummySymbol().term(), constC.term(), varD.term(), ent.getActorSymbol().term() }, term
				.getSymbolName());
		System.out.println(redTerm.getRepresentation() + "|");
		System.out.println(term.reduce(symState).getRepresentation() + "|");
		assertEquals(redTerm.getRepresentation(), term.reduce(symState).getRepresentation());
		System.out.println(term.getRepresentation() + " <-> " + term.reduce(symState).getRepresentation());

		expect(false, true, 5, 1, 1, 4, 0, 0);
		assertTrue(ctx.getOwners().contains(ent));
		assertTrue(ctx.getAuxiliaryTerms().contains(fncContains.term(new ITerm[] { term.toTerm(), varA.term() })));
		assertTrue(ctx.getAuxiliaryTerms().contains(fncContains.term(new ITerm[] { term.toTerm(), matchB })));
		assertTrue(ctx.getAuxiliaryTerms().contains(fncContains.term(new ITerm[] { term.toTerm(), constC.term() })));
		assertTrue(ctx.getAuxiliaryTerms().contains(fncContains.term(new ITerm[] { term.toTerm(), varD.term() })));
		assertTrue(ctx.getAuxiliaryTerms().contains(fncContains.term(new ITerm[] { term.toTerm(), ent.getActorSymbol().term() })));
	}

	// public void testCommunicationTermActorSender() {
	// VariableSymbol varPartner = ent.addStateVariable("Partner", tAgent);
	// VariableSymbol varPayload = ent.addStateVariable("Payload", tMessage);
	//
	// CommunicationTerm term = ent.communication(ent.getActorSymbol().term(),
	// varPartner.term(), varPayload.term(), Type.Secure, false, false, false);
	// term = term.accept(new ASLanBuilder(new TranslatorOptions()));
	// term.buildContext(ctx, false);
	// term.useContext(ctx, symState);
	//
	// assertEquals(term.getRepresentation(),
	// term.reduce(symState).getRepresentation());
	// System.out.println(term.getRepresentation() + " <-> " +
	// term.reduce(symState).getRepresentation());
	//
	// expect(false, true, 0, 0, 1, 3, 0, 1);
	// assertTrue(ctx.getOwners().contains(ent));
	// }
	//
	// public void testCommunicationTermActorReceiver() {
	// VariableSymbol varPartner = ent.addStateVariable("Partner", tAgent);
	// VariableSymbol varPayload = ent.addStateVariable("Payload", tMessage);
	//
	// CommunicationTerm term = ent.communication(varPartner.term(),
	// ent.getActorSymbol().term(), varPayload.term(), Type.Secure, false,
	// false, false);
	// term.buildContext(ctx, false);
	// term.useContext(ctx, symState);
	//
	// assertEquals(term.getRepresentation(),
	// term.reduce(symState).getRepresentation());
	// System.out.println(term.getRepresentation() + " <-> " +
	// term.reduce(symState).getRepresentation());
	//
	// expect(false, true, 0, 0, 1, 3, 1, 0);
	// assertTrue(ctx.getOwners().contains(ent));
	// }
	//
	// public void testCommunicationTermNoActor() {
	// VariableSymbol varSender = ent.addStateVariable("Sender", tAgent);
	// VariableSymbol varReceiver = ent.addStateVariable("Receiver", tAgent);
	// VariableSymbol varPayload = ent.addStateVariable("Payload", tMessage);
	//
	// CommunicationTerm term = ent.communication(varSender.term(),
	// varReceiver.term(), varPayload.term(), Type.Secure, false, false, false);
	// term.buildContext(ctx, false);
	// term.useContext(ctx, symState);
	//
	// assertEquals(term.getRepresentation(),
	// term.reduce(symState).getRepresentation());
	// System.out.println(term.getRepresentation() + " <-> " +
	// term.reduce(symState).getRepresentation());
	//
	// expect(false, false, 0, 0, 1, 3, 0, 1);
	// assertTrue(ctx.getOwners().contains(ent));
	// }

	private void expect(boolean breakpoint, boolean actor, int aux, int fresh, int owners, int pars, int receives, int sends) {
		assertEquals(breakpoint, ctx.hasBreakpoint());
		assertEquals(aux, ctx.getAuxiliaryTerms().size());
		assertEquals(owners, ctx.getOwners().size());
		assertEquals(receives, ctx.getCommunicationStatistics().receives);
		assertEquals(sends, ctx.getCommunicationStatistics().sends);
	}
}
