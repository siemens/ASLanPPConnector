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
import org.avantssar.aslanpp.model.Entity;
import org.avantssar.aslanpp.model.EntityManager;
import org.avantssar.aslanpp.model.EqualityExpression;
import org.avantssar.aslanpp.model.ErrorMessages;
import org.avantssar.aslanpp.model.IExpression;
import org.avantssar.aslanpp.model.Prelude;
import org.avantssar.aslanpp.model.SetLiteralTerm;
import org.avantssar.aslanpp.model.TupleTerm;
import org.avantssar.aslanpp.model.TupleType;
import org.avantssar.aslanpp.model.UnnamedMatchTerm;
import org.avantssar.aslanpp.model.VariableSymbol;
import org.avantssar.aslanpp.visitors.DummySymbolsCreator;
import org.avantssar.aslanpp.visitors.TypeAssigner;
import org.avantssar.commons.ChannelModel;
import org.avantssar.commons.ErrorGatherer;

public class TypeAssignmentTest extends TestCase {

	private ASLanPPSpecification spec;
	private Entity ent;
	private ErrorGatherer err;
	private EntityManager manager;

	public TypeAssignmentTest(String testName) {
		super(testName);
	}

	@Override
	public void setUp() {
		manager = new EntityManager();
		spec = new ASLanPPSpecification(manager, "spec", ChannelModel.CCM);
		ent = spec.entity("scope");
		err = new ErrorGatherer(ErrorMessages.DEFAULT);
	}

	private TypeAssigner fix(IExpression ee) {
		TypeAssigner ta = new TypeAssigner(err);
		ee.accept(new DummySymbolsCreator(err));
		ee.accept(ta);
		ta.getErrorGatherer().report(System.out);
		return ta;
	}

	// ? = (A:agent,B:message,C:text)
	public void testEquality_UnknownVar_KnownTuple() {
		UnnamedMatchTerm m = ent.unnamedMatch();
		VariableSymbol vA = ent.addVariable("A", ent.findType(Prelude.AGENT));
		VariableSymbol vB = ent.addVariable("B", ent.findType(Prelude.MESSAGE));
		VariableSymbol vC = ent.addVariable("C", ent.findType(Prelude.TEXT));
		EqualityExpression ee = m.equality(TupleTerm.tuple(ent, vA.term(), vB.term(), vC.term()));
		TypeAssigner ta = fix(ee);
		assertEquals(0, ta.getErrorGatherer().size());
		assertFalse(m.isTypeCertain());
		assertEquals(new TupleType(ent.findType(Prelude.AGENT), ent.findType(Prelude.MESSAGE), ent.findType(Prelude.TEXT)).getRepresentation(), m.inferType().getRepresentation());
	}

	// (A:agent,B:message,C:text) = ?
	public void testEquality_KnownTuple_UnknownVar() {
		UnnamedMatchTerm m = ent.unnamedMatch();
		VariableSymbol vA = ent.addVariable("A", ent.findType(Prelude.AGENT));
		VariableSymbol vB = ent.addVariable("B", ent.findType(Prelude.MESSAGE));
		VariableSymbol vC = ent.addVariable("C", ent.findType(Prelude.TEXT));
		EqualityExpression ee = TupleTerm.tuple(ent, vA.term(), vB.term(), vC.term()).equality(m);
		TypeAssigner ta = fix(ee);
		assertEquals(0, ta.getErrorGatherer().size());
		assertFalse(m.isTypeCertain());
		assertEquals(new TupleType(ent.findType(Prelude.AGENT), ent.findType(Prelude.MESSAGE), ent.findType(Prelude.TEXT)).getRepresentation(), m.inferType().getRepresentation());
	}

	// ? = (?,?,?)
	public void testEquality_UnknownVar_UnknownTuple() {
		UnnamedMatchTerm umA = ent.unnamedMatch();
		UnnamedMatchTerm umB = ent.unnamedMatch();
		UnnamedMatchTerm umC = ent.unnamedMatch();
		UnnamedMatchTerm umD = ent.unnamedMatch();
		EqualityExpression ee = umA.equality(TupleTerm.tuple(ent, umB, umC, umD));
		TypeAssigner ta = fix(ee);
		ta.getErrorGatherer().report(System.out);
		assertEquals(0, ta.getErrorGatherer().size());
		assertFalse(umA.isTypeCertain());
		assertFalse(umB.isTypeCertain());
		assertFalse(umC.isTypeCertain());
		assertFalse(umD.isTypeCertain());
		assertEquals(ent.findType(Prelude.MESSAGE).getRepresentation(), umA.getDummySymbol().getType().getRepresentation());
		assertEquals(ent.findType(Prelude.MESSAGE).getRepresentation(), umB.getDummySymbol().getType().getRepresentation());
		assertEquals(ent.findType(Prelude.MESSAGE).getRepresentation(), umC.getDummySymbol().getType().getRepresentation());
		assertEquals(ent.findType(Prelude.MESSAGE).getRepresentation(), umD.getDummySymbol().getType().getRepresentation());
	}

	// (?,?,?) = ?
	public void testEquality_UnknownTuple_UnknownVar() {
		UnnamedMatchTerm umA = ent.unnamedMatch();
		UnnamedMatchTerm umB = ent.unnamedMatch();
		UnnamedMatchTerm umC = ent.unnamedMatch();
		UnnamedMatchTerm umD = ent.unnamedMatch();
		EqualityExpression ee = TupleTerm.tuple(ent, umA, umB, umC).equality(umD);
		TypeAssigner ta = fix(ee);
		ta.getErrorGatherer().report(System.out);
		assertEquals(0, ta.getErrorGatherer().size());
		assertFalse(umA.isTypeCertain());
		assertFalse(umB.isTypeCertain());
		assertFalse(umC.isTypeCertain());
		assertFalse(umD.isTypeCertain());
		assertEquals(ent.findType(Prelude.MESSAGE).getRepresentation(), umA.getDummySymbol().getType().getRepresentation());
		assertEquals(ent.findType(Prelude.MESSAGE).getRepresentation(), umB.getDummySymbol().getType().getRepresentation());
		assertEquals(ent.findType(Prelude.MESSAGE).getRepresentation(), umC.getDummySymbol().getType().getRepresentation());
		assertEquals(ent.findType(Prelude.MESSAGE).getRepresentation(), umD.getDummySymbol().getType().getRepresentation());
	}

	// (?,?)=(C:agent,D:text)
	public void testEquality_UnknownTuple_KnownTuple() {
		VariableSymbol vA = ent.addUntypedVariable("A");
		VariableSymbol vB = ent.addUntypedVariable("B");
		VariableSymbol vC = ent.addVariable("C", ent.findType(Prelude.AGENT));
		VariableSymbol vD = ent.addVariable("D", ent.findType(Prelude.TEXT));
		EqualityExpression ee = TupleTerm.tuple(ent, vA.term(), vB.term()).equality(TupleTerm.tuple(ent, vC.term(), vD.term()));
		TypeAssigner ta = fix(ee);
		assertEquals(0, ta.getErrorGatherer().size());
		assertEquals(ent.findType(Prelude.AGENT).getRepresentation(), vA.getType().getRepresentation());
		assertEquals(ent.findType(Prelude.TEXT).getRepresentation(), vB.getType().getRepresentation());
	}

	// (A:agent,B:text)=(?,?)
	public void testEquality_KnownTuple_UnknownTuple() {
		VariableSymbol vA = ent.addVariable("C", ent.findType(Prelude.AGENT));
		VariableSymbol vB = ent.addVariable("D", ent.findType(Prelude.TEXT));
		VariableSymbol vC = ent.addUntypedVariable("A");
		VariableSymbol vD = ent.addUntypedVariable("B");
		EqualityExpression ee = TupleTerm.tuple(ent, vA.term(), vB.term()).equality(TupleTerm.tuple(ent, vC.term(), vD.term()));
		TypeAssigner ta = fix(ee);
		assertEquals(0, ta.getErrorGatherer().size());
		assertEquals(ent.findType(Prelude.AGENT).getRepresentation(), vC.getType().getRepresentation());
		assertEquals(ent.findType(Prelude.TEXT).getRepresentation(), vD.getType().getRepresentation());
	}

	// ? = (?,B:text,?)
	public void testEquality_UnknownVar_PartiallyKnownTuple() {
		UnnamedMatchTerm umA = ent.unnamedMatch();
		UnnamedMatchTerm umB = ent.unnamedMatch();
		VariableSymbol vC = ent.addVariable("C", ent.findType(Prelude.TEXT));
		UnnamedMatchTerm umD = ent.unnamedMatch();
		EqualityExpression ee = umA.equality(TupleTerm.tuple(ent, umB, vC.term(), umD));
		TypeAssigner ta = fix(ee);
		assertEquals(0, ta.getErrorGatherer().size());
		assertFalse(umA.isTypeCertain());
		assertFalse(umB.isTypeCertain());
		assertFalse(umD.isTypeCertain());
		assertEquals(ent.findType(Prelude.MESSAGE).getRepresentation(), umA.inferType().getRepresentation());
		assertEquals(ent.findType(Prelude.MESSAGE).getRepresentation(), umB.inferType().getRepresentation());
		assertEquals(ent.findType(Prelude.MESSAGE).getRepresentation(), umD.inferType().getRepresentation());
	}

	// ? = {B:agent, C:agent, D:agent}
	public void testEquality_UnknownVar_KnownSet() {
		UnnamedMatchTerm uA = ent.unnamedMatch();
		VariableSymbol vB = ent.addVariable("B", ent.findType(Prelude.AGENT));
		VariableSymbol vC = ent.addVariable("C", ent.findType(Prelude.AGENT));
		VariableSymbol vD = ent.addVariable("D", ent.findType(Prelude.AGENT));

		SetLiteralTerm tBCD = SetLiteralTerm.set(ent, vB.term(), vC.term(), vD.term());

		EqualityExpression ee = uA.equality(tBCD);
		TypeAssigner ta = fix(ee);
		ta.getErrorGatherer().report(System.out);

		assertEquals(0, ta.getErrorGatherer().size());
		assertFalse(uA.isTypeCertain());
		assertTrue(tBCD.isTypeCertain());
		assertEquals(Prelude.getSetOf(ent.findType(Prelude.AGENT)).getRepresentation(), uA.inferType().getRepresentation());
	}

	// {B:agent, C:agent, D:agent} = ?
	public void testEquality_KnownSet_UnknownVar() {
		VariableSymbol vA = ent.addVariable("A", ent.findType(Prelude.AGENT));
		VariableSymbol vB = ent.addVariable("B", ent.findType(Prelude.AGENT));
		VariableSymbol vC = ent.addVariable("C", ent.findType(Prelude.AGENT));
		UnnamedMatchTerm uD = ent.unnamedMatch();

		SetLiteralTerm tABC = SetLiteralTerm.set(ent, vA.term(), vB.term(), vC.term());

		EqualityExpression ee = tABC.equality(uD);
		TypeAssigner ta = fix(ee);
		ta.getErrorGatherer().report(System.out);

		assertEquals(0, ta.getErrorGatherer().size());
		assertFalse(uD.isTypeCertain());
		assertTrue(tABC.isTypeCertain());
		assertEquals(Prelude.getSetOf(ent.findType(Prelude.AGENT)).getRepresentation(), uD.inferType().getRepresentation());
	}

	// {?,?} = {C:agent, D:agent, E:agent}
	public void testEquality_UnknownSet_KnownSet() {
		UnnamedMatchTerm uA = ent.unnamedMatch();
		UnnamedMatchTerm uB = ent.unnamedMatch();
		VariableSymbol vC = ent.addVariable("C", ent.findType(Prelude.AGENT));
		VariableSymbol vD = ent.addVariable("D", ent.findType(Prelude.AGENT));
		VariableSymbol vE = ent.addVariable("E", ent.findType(Prelude.AGENT));

		SetLiteralTerm tCDE = SetLiteralTerm.set(ent, vC.term(), vD.term(), vE.term());
		SetLiteralTerm tAB = SetLiteralTerm.set(ent, uA, uB);

		EqualityExpression ee = tAB.equality(tCDE);
		TypeAssigner ta = fix(ee);
		ta.getErrorGatherer().report(System.out);

		assertEquals(0, ta.getErrorGatherer().size());
		assertFalse(uA.isTypeCertain());
		assertFalse(uB.isTypeCertain());
		assertTrue(tCDE.isTypeCertain());
		assertEquals(Prelude.getSetOf(ent.findType(Prelude.AGENT)).getRepresentation(), tAB.inferType().getRepresentation());
		assertEquals(ent.findType(Prelude.AGENT).getRepresentation(), uA.inferType().getRepresentation());
		assertEquals(ent.findType(Prelude.AGENT).getRepresentation(), uB.inferType().getRepresentation());
	}

	// {A:agent, B:agent, C:agent} = {?, ?}
	public void testEquality_KnownSet_UnknownSet() {
		VariableSymbol vA = ent.addVariable("C", ent.findType(Prelude.AGENT));
		VariableSymbol vB = ent.addVariable("D", ent.findType(Prelude.AGENT));
		VariableSymbol vC = ent.addVariable("E", ent.findType(Prelude.AGENT));
		UnnamedMatchTerm uD = ent.unnamedMatch();
		UnnamedMatchTerm uE = ent.unnamedMatch();

		SetLiteralTerm tABC = SetLiteralTerm.set(ent, vA.term(), vB.term(), vC.term());
		SetLiteralTerm tDE = SetLiteralTerm.set(ent, uD, uE);

		EqualityExpression ee = tABC.equality(tDE);
		TypeAssigner ta = fix(ee);
		ta.getErrorGatherer().report(System.out);

		assertEquals(0, ta.getErrorGatherer().size());
		assertFalse(uD.isTypeCertain());
		assertFalse(uE.isTypeCertain());
		assertTrue(tABC.isTypeCertain());
		assertEquals(Prelude.getSetOf(ent.findType(Prelude.AGENT)).getRepresentation(), tDE.inferType().getRepresentation());
		assertEquals(ent.findType(Prelude.AGENT).getRepresentation(), uD.inferType().getRepresentation());
		assertEquals(ent.findType(Prelude.AGENT).getRepresentation(), uE.inferType().getRepresentation());
	}
}
