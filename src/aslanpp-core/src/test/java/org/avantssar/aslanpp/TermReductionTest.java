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

//import java.util.ArrayList;
//import java.util.List;
import junit.framework.TestCase;
import org.avantssar.aslanpp.model.ASLanPPSpecification;
import org.avantssar.aslanpp.model.ConstantSymbol;
import org.avantssar.aslanpp.model.Entity;
import org.avantssar.aslanpp.model.EntityManager;
import org.avantssar.aslanpp.model.ExpressionContext;
import org.avantssar.aslanpp.model.FunctionSymbol;
import org.avantssar.aslanpp.model.ITerm;
import org.avantssar.aslanpp.model.IType;
import org.avantssar.aslanpp.model.Prelude;
import org.avantssar.aslanpp.model.SymbolsState;
import org.avantssar.aslanpp.model.VariableSymbol;
import org.avantssar.aslanpp.model.VariableTerm;
import org.avantssar.commons.ChannelModel;

public class TermReductionTest extends TestCase {

	private SymbolsState symState;
	private ExpressionContext ctx;
	private ASLanPPSpecification spec;
	private Entity ent;

	private IType tMessage;
	private IType tFact;

	private EntityManager manager;

	public TermReductionTest(String testName) {
		super(testName);
	}

	@Override
	public void setUp() {
		symState = new SymbolsState();
		manager = new EntityManager();
		spec = new ASLanPPSpecification(manager, "spec", ChannelModel.CCM);
		ent = spec.entity("Environment");

		tMessage = spec.findType(Prelude.MESSAGE);
		tFact = spec.findType(Prelude.FACT);
	}

	public void xtestOne() {
		ConstantSymbol a = ent.constants(tMessage, "a");
		ConstantSymbol b = ent.constants(tMessage, "b");
		ConstantSymbol c = ent.constants(tMessage, "c");
	//	List<IType> argTypes = new ArrayList<IType>();
		VariableSymbol varA = ent.addVariable("A", tMessage);
		VariableSymbol varB = ent.addVariable("B", tMessage);
		VariableSymbol varC = ent.addVariable("C", tMessage);
		FunctionSymbol fncMyFact = ent.addFunction("myfact", tFact, tMessage, tMessage, tMessage);
		ITerm original = fncMyFact.term(new ITerm[] { varA.term(), varB.term(), varC.term() });
		System.out.println("original: " + original.getRepresentation());
		System.out.println(symState);
		System.out.println("reduced: " + original.reduce(symState).getRepresentation());
		symState.assign(varA, a.term());
		System.out.println(symState);
		System.out.println("reduced: " + original.reduce(symState).getRepresentation());
		symState.assign(varB, b.term());
		System.out.println(symState);
		System.out.println("reduced: " + original.reduce(symState).getRepresentation());
		symState.assign(varC, c.term());
		System.out.println(symState);
		System.out.println("reduced: " + original.reduce(symState).getRepresentation());
		symState.assign(varA, varB.term());
		System.out.println(symState);
		System.out.println("reduced: " + original.reduce(symState).getRepresentation());
		symState.assign(varB, varC.term());
		System.out.println(symState);
		System.out.println("reduced: " + original.reduce(symState).getRepresentation());
		symState.assign(varC, varA.term());
		System.out.println(symState);
		System.out.println("reduced: " + original.reduce(symState).getRepresentation());
		symState.assign(varA, original);
		System.out.println(symState);
		System.out.println("reduced: " + original.reduce(symState).getRepresentation());
		symState.assign(varA, original);
		System.out.println(symState);
		System.out.println("reduced: " + original.reduce(symState).getRepresentation());
	}

	public void xtestTwo() {
		VariableSymbol varA = ent.addVariable("A", tMessage);
		VariableSymbol varB = ent.addVariable("B", tMessage);
		VariableSymbol varC = ent.addVariable("C", tMessage);
		VariableSymbol varD = ent.addVariable("D", tMessage);
		VariableTerm termBMatched = varB.matchedTerm();
		VariableTerm termCMatched = varC.matchedTerm();
		FunctionSymbol fncMyFact = ent.addFunction("my_fact", tFact, tMessage, tMessage, tMessage, tMessage);
		FunctionSymbol fncPrevFact = ent.addFunction("prev_fact", tFact, tMessage, tMessage, tMessage, tMessage);
		ITerm prevTerm = fncPrevFact.term(new ITerm[] { varA.term(), varB.term(), varC.term(), varD.term() });
		ITerm myTerm = fncMyFact.term(new ITerm[] { varA.term(), termBMatched, termCMatched, varD.term() });
		System.out.println("prev: " + prevTerm.getRepresentation());
		System.out.println("my: " + myTerm.getRepresentation());
		System.out.println(symState);
		myTerm.useContext(ctx, symState);
		System.out.println("contributed");
		System.out.println(symState);
		System.out.println("prev reduced: " + prevTerm.reduce(symState).getRepresentation());
		System.out.println("my reduced: " + myTerm.reduce(symState).getRepresentation());
		symState.assign(varD, varB.term());
		System.out.println(symState);
		System.out.println("prev reduced: " + prevTerm.reduce(symState).getRepresentation());
		System.out.println("my reduced: " + myTerm.reduce(symState).getRepresentation());
		symState.clear();
		System.out.println(symState);
		System.out.println("prev reduced: " + prevTerm.reduce(symState).getRepresentation());
		System.out.println("my reduced: " + myTerm.reduce(symState).getRepresentation());
	}

	public void testPushPop() {
		VariableSymbol varA = ent.addVariable("A", tMessage);
		VariableSymbol varB = ent.addVariable("B", tMessage);
		System.out.println(symState);
		symState.push();
		symState.assign(varA, varA.term());
		System.out.println(symState);
		symState.push();
		symState.assign(varB, varB.term());
		System.out.println(symState);
		symState.pop();
		System.out.println(symState);
		symState.pop();
		System.out.println(symState);
	}
}
