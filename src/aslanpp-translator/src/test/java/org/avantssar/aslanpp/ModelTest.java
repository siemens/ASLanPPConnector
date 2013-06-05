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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import junit.framework.TestCase;
import org.avantssar.aslanpp.model.ASLanPPSpecification;
import org.avantssar.aslanpp.model.BlockStatement;
import org.avantssar.aslanpp.model.BranchStatement;
import org.avantssar.aslanpp.model.ConstantSymbol;
import org.avantssar.aslanpp.model.Entity;
import org.avantssar.aslanpp.model.EntityManager;
import org.avantssar.aslanpp.model.Equation;
import org.avantssar.aslanpp.model.FunctionSymbol;
import org.avantssar.aslanpp.model.Goal;
import org.avantssar.aslanpp.model.HornClause;
import org.avantssar.aslanpp.model.IType;
import org.avantssar.aslanpp.model.MacroSymbol;
import org.avantssar.aslanpp.model.Prelude;
import org.avantssar.aslanpp.model.VariableSymbol;
import org.avantssar.aslanpp.visitors.PrettyPrinter;
import org.avantssar.commons.ChannelModel;

public class ModelTest extends TestCase {

	protected ASLanPPSpecification spec;
	protected String prefix = "target/";

	private EntityManager manager;

	public ModelTest(String testName) {
		super(testName);
	}

	@Override
	public void setUp() {
		manager = new EntityManager();
	}

	@Override
	public void tearDown() {
		try {
			String file = prefix + spec.getSpecificationName() + ".mem.aslan++";

			// Serialize in-memory model.
			PrettyPrinter ppInMemoryRaw = spec.toFile(file);
			spec.finalize(false);
			PrettyPrinter ppInMemoryPreprocessed = spec.toFile(prefix + spec.getSpecificationName() + ".mem.pp.aslan++", true);

			// Load back from file.
			cleanup();
			ASLanPPSpecification specFile = ASLanPPSpecification.fromStream(manager, file, new FileInputStream(file), null);

			// Serialize loaded model.
			PrettyPrinter ppFromFileRaw = specFile.toFile(prefix + specFile.getSpecificationName() + ".file.aslan++");
			// System.out.println(ppFromFileRaw);
			specFile.finalize(false);
			PrettyPrinter ppFromFilePreprocessed = specFile.toFile(prefix + spec.getSpecificationName() + ".file.pp.aslan++", true);

			assertEquals(ppInMemoryRaw.toString(), ppFromFileRaw.toString());
			assertEquals(ppInMemoryPreprocessed.toString(), ppFromFilePreprocessed.toString());
		}
		catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	public void testNasty() {
		spec = new ASLanPPSpecification(manager, "Scoping", ChannelModel.CCM);

		ConstantSymbol intruder = spec.findConstant(Prelude.INTRUDER);

		Entity env = spec.entity("Environment");
		env.constants(env.findType(Prelude.AGENT), "a");
		env.constants(env.findType(Prelude.AGENT), "b");

		Entity session = env.entity("Session");
		session.addParameter("A", env.findType(Prelude.AGENT));
		session.addParameter("B", env.findType(Prelude.AGENT));

		Entity alice = session.entity("Alice");
		alice.addParameter(Entity.ACTOR_PREFIX, env.findType(Prelude.AGENT));
		alice.addParameter("B", env.findType(Prelude.AGENT));
		ConstantSymbol aliceClash = alice.constants(env.findType(Prelude.MESSAGE), "clash");
		MacroSymbol aliceMacroClash = alice.addMacro(null, "clash");
		aliceMacroClash.setBody(aliceClash.term());
		MacroSymbol aliceMacroClash2 = alice.addMacro(null, "clash2");
		aliceMacroClash2.setBody(intruder.term());

		Entity aliceInner = alice.entity("Alice");
		aliceInner.addParameter(Entity.ACTOR_PREFIX, env.findType(Prelude.AGENT));
		ConstantSymbol aliceInnerClash = aliceInner.constants(env.findType(Prelude.MESSAGE), "clash");
		MacroSymbol aliceInnerMacroClash = aliceInner.addMacro(null, "clash");
		aliceInnerMacroClash.setBody(aliceInnerClash.term());
		MacroSymbol aliceInnerMacroClash2 = aliceInner.addMacro(null, "clash2");
		aliceInnerMacroClash2.setBody(intruder.term());

		Entity bob = session.entity("Bob");
		bob.addParameter("A", env.findType(Prelude.AGENT));
		bob.addParameter(Entity.ACTOR_PREFIX, env.findType(Prelude.AGENT));
		ConstantSymbol bobClash = bob.constants(env.findType(Prelude.AGENT), "clash");
		MacroSymbol bobMacroClash = bob.addMacro(null, "clash");
		bobMacroClash.setBody(bobClash.term());
		MacroSymbol bobMacroClash2 = bob.addMacro(null, "clash2");
		bobMacroClash2.setBody(intruder.term());
	}

	public void testBranches() {
		spec = new ASLanPPSpecification(manager, "Branches", ChannelModel.CCM);

		Entity env = spec.entity("Environment");
		ConstantSymbol f1 = env.constants(env.findType(Prelude.FACT), "f1");
		ConstantSymbol f2 = env.constants(env.findType(Prelude.FACT), "f2");
		ConstantSymbol f3 = env.constants(env.findType(Prelude.FACT), "f3");
		env.group(f1, f2, f3);
		ConstantSymbol g1 = env.constants(env.findType(Prelude.FACT), "g1");
		ConstantSymbol g2 = env.constants(env.findType(Prelude.FACT), "g2");
		env.group(g1, g2);
		ConstantSymbol c1 = env.constants(env.findType(Prelude.FACT), "c1");
		ConstantSymbol c2 = env.constants(env.findType(Prelude.FACT), "c2");
		ConstantSymbol c3 = env.constants(env.findType(Prelude.FACT), "c3");
		ConstantSymbol c4 = env.constants(env.findType(Prelude.FACT), "c4");
		env.group(c1, c2, c3, c4);
		ConstantSymbol myThen1 = env.constants(env.findType(Prelude.FACT), "myThen1");
		ConstantSymbol myElse1 = env.constants(env.findType(Prelude.FACT), "myElse1");
		ConstantSymbol myThen2 = env.constants(env.findType(Prelude.FACT), "myThen2");
		ConstantSymbol myElse2 = env.constants(env.findType(Prelude.FACT), "myElse2");
		env.group(myThen1, myElse1, myThen2, myElse2);

		BlockStatement envBody = env.body(env.block());
		envBody.add(env.introduce(f1.term()));
		envBody.add(env.introduce(f2.term()));
		BranchStatement ifF1 = envBody.add(env.branch(f1.term().expression()));
		BranchStatement ifF2 = ifF1.branchTrue(env.branch(f2.term().expression()));
		BranchStatement ifF3 = ifF2.branchTrue(env.branch(f3.term().expression()));
		ifF3.branchTrue(env.introduce(g1.term()));
		ifF3.branchFalse(env.introduce(g2.term()));
		envBody.add(env.introduce(c1.term()));
		envBody.add(env.introduce(c3.term()));
		BranchStatement ifC123 = envBody.add(env.branch(c1.term().expression().and(c2.term().expression().or(c3.term().expression()))));
		ifC123.branchTrue(env.introduce(myThen1.term()));
		ifC123.branchFalse(env.introduce(myElse1.term()));
		BranchStatement ifC4 = envBody.add(env.branch(c4.term().expression()));
		ifC4.branchTrue(env.introduce(myThen2.term()));
		ifC4.branchFalse(env.introduce(myElse2.term()));

		Goal safe = env.goal("safe");
		safe.setFormula(g2.term().expression().and(g1.term().expression().not()).and(myThen1.term().expression()).and(myThen2.term().expression().not()).and(myElse1.term().expression().not()).and(
				myElse2.term().expression()).not());
	}

	public void testConstantOverridesMacro() {
		String commonName = "same_name";

		spec = new ASLanPPSpecification(manager, "Constant_Over_Macro", ChannelModel.CCM);

		FunctionSymbol fPair = spec.findFunction(Prelude.PAIR);

		Entity env = spec.entity("Environment");
		MacroSymbol m = env.addMacro(null, commonName);
		ConstantSymbol cA = env.constants(env.findType(Prelude.MESSAGE), "a");
		ConstantSymbol cB = env.constants(env.findType(Prelude.MESSAGE), "b");
		m.setBody(fPair.term(cA.term(), cB.term()));

		Entity child = env.entity("Child");
		ConstantSymbol c = child.constants(env.findType(Prelude.FACT), commonName);

		BlockStatement childBody = child.body(child.block());
		childBody.add(child.introduce(c.term()));
	}

	public void testMacroOverridesConstant() {
		String commonName = "same_name";

		spec = new ASLanPPSpecification(manager, "Macro_Over_Constant", ChannelModel.CCM);

		Entity env = spec.entity("Environment");
		FunctionSymbol fPair = env.addFunction("myPair", spec.findType(Prelude.FACT), spec.findType(Prelude.MESSAGE), spec.findType(Prelude.MESSAGE));
		env.constants(env.findType(Prelude.FACT), commonName);
		ConstantSymbol cA = env.constants(env.findType(Prelude.MESSAGE), "a");
		ConstantSymbol cB = env.constants(env.findType(Prelude.MESSAGE), "b");

		Entity child = env.entity("Child");
		MacroSymbol m = child.addMacro(null, commonName);
		m.setBody(fPair.term(cA.term(), cB.term()));

		BlockStatement childBody = child.body(child.block());
		childBody.add(child.introduce(m.term()));
	}

	public void testFunctionOverridesMacro() {
		String commonName = "same_name";

		spec = new ASLanPPSpecification(manager, "Function_Over_Macro", ChannelModel.CCM);

		FunctionSymbol fPair = spec.findFunction(Prelude.PAIR);

		Entity env = spec.entity("Environment");
		MacroSymbol m = env.addMacro(null, commonName);
		VariableSymbol mA = m.addArgument("A");
		VariableSymbol mB = m.addArgument("B");
		m.setBody(fPair.term(mA.term(), mB.term()));

		Entity child = env.entity("Child");
		FunctionSymbol f = child.addFunction(commonName, child.findType(Prelude.FACT), child.findType(Prelude.MESSAGE), child.findType(Prelude.MESSAGE));
		ConstantSymbol cA = child.constants(env.findType(Prelude.MESSAGE), "a");
		ConstantSymbol cB = child.constants(env.findType(Prelude.MESSAGE), "b");

		BlockStatement childBody = child.body(child.block());
		childBody.add(child.introduce(f.term(cA.term(), cB.term())));
	}

	public void testMacroOverridesFunction() {
		String commonName = "same_name";

		spec = new ASLanPPSpecification(manager, "Macro_Over_Function", ChannelModel.CCM);

		Entity env = spec.entity("Environment");
		FunctionSymbol fPair = env.addFunction("myPair", spec.findType(Prelude.FACT), spec.findType(Prelude.MESSAGE), spec.findType(Prelude.MESSAGE));
		env.addFunction(commonName, env.findType(Prelude.MESSAGE), env.findType(Prelude.MESSAGE), env.findType(Prelude.MESSAGE));

		Entity child = env.entity("Child");
		MacroSymbol m = child.addMacro(null, commonName);
		VariableSymbol mA = m.addArgument("A");
		VariableSymbol mB = m.addArgument("B");
		m.setBody(fPair.term(mA.term(), mB.term()));
		ConstantSymbol cA = child.constants(env.findType(Prelude.MESSAGE), "a");
		ConstantSymbol cB = child.constants(env.findType(Prelude.MESSAGE), "b");

		BlockStatement childBody = child.body(child.block());
		childBody.add(child.introduce(m.term(cA.term(), cB.term())));
	}

	public void testEquationsWithMacros() {
		spec = new ASLanPPSpecification(manager, "Equations_Macros", ChannelModel.CCM);

		IType tt = spec.findType(Prelude.TEXT);
		Entity env = spec.entity("Environment");
		FunctionSymbol aug = env.addFunction("augment", tt, tt);
		MacroSymbol m = env.addMacro(null, "a");
		VariableSymbol mM = m.addArgument("M");
		m.setBody(aug.term(mM.term()));
		FunctionSymbol f1 = env.addFunction("f1", tt, tt, tt);
		FunctionSymbol f2 = env.addFunction("f2", tt, tt, tt);
		Equation eq = env.equation();
		VariableSymbol eqA = eq.addUntypedVariable("A");
		VariableSymbol eqB = eq.addUntypedVariable("B");
		eq.setLeftTerm(f1.term(m.term(eqA.term()), m.term(eqB.term())));
		eq.setRightTerm(f2.term(m.term(eqA.term()), m.term(eqB.term())));
	}

	public void testImports() {
		String cNameOne = "key_agent";
		String fNameOne = "replacement";
		String mNameOne = "mo";
		String hcNameOne = "clause_one";
		String gNameOne = "key_agent_not_tagged";
		ASLanPPSpecification moduleOne = new ASLanPPSpecification(manager, "Module_One", ChannelModel.CCM);
		IType tAgent = moduleOne.findType(Prelude.AGENT);
		Entity m_one = moduleOne.entity("ModuleOne");
		ConstantSymbol m_one_const = m_one.constants(tAgent, cNameOne);
		FunctionSymbol m_one_fnc = m_one.addFunction(fNameOne, tAgent, tAgent);
		MacroSymbol m_one_macro = m_one.addMacro(null, mNameOne);
		VariableSymbol m_one_macro_agent = m_one_macro.addArgument("Agent");
		FunctionSymbol fTagged = m_one.addFunction("tagged", moduleOne.findType(Prelude.FACT), tAgent);
		m_one_macro.setBody(m_one_fnc.term(m_one_macro_agent.term()));
		HornClause m_one_hc = m_one.hornClause(hcNameOne);
		VariableSymbol m_one_hc_agent = m_one_hc.argument("A");
		m_one_hc.setHead(fTagged.term(m_one_macro.term(m_one_hc_agent.term())));
		m_one_hc.addBody(fTagged.term(m_one_hc_agent.term()));
		Goal m_one_goal = m_one.goal(gNameOne);
		m_one_goal.setFormula(fTagged.term(m_one_const.term()).expression().not());
		try {
			m_one.toFile(prefix + m_one.getOriginalName() + ".aslan++");
		}
		catch (IOException e) {
			e.printStackTrace();
			fail();
		}

		spec = new ASLanPPSpecification(manager, "Imports", ChannelModel.CCM);
		Entity env = spec.entity("Environment");
		env.addImports(m_one.getOriginalName());
	}

	public void testImportsCircular() {
		ASLanPPSpecification moduleOne = new ASLanPPSpecification(manager, "Module_One", ChannelModel.CCM);
		Entity m_one = moduleOne.entity("ModuleOne");
		try {
			m_one.toFile(prefix + m_one.getOriginalName() + ".aslan++");
		}
		catch (IOException e) {
			e.printStackTrace();
			fail();
		}

		cleanup();
		spec = new ASLanPPSpecification(manager, "Imports", ChannelModel.CCM);
		Entity env = spec.entity("Environment");
		env.addImports(m_one.getOriginalName());
		try {
			env.toFile(prefix + env.getOriginalName() + ".aslan++");
		}
		catch (IOException e) {
			e.printStackTrace();
			fail();
		}

		cleanup();
		moduleOne = new ASLanPPSpecification(manager, "Module_One", ChannelModel.CCM);
		m_one = moduleOne.entity("ModuleOne");
		try {
			m_one.addImports(env.getOriginalName());
			fail("Should have been a circular dependency.");
		}
		catch (Exception ex) {
			assertTrue(true);
		}
	}

	private void cleanup() {
		System.setProperty(EntityManager.ASLAN_ENVVAR, new File(prefix).getAbsolutePath());
		try {
			manager.purge();
			manager.loadASLanPath();
		}
		catch (IOException e) {
			fail("Failed to reload ASLANPATH.");
		}
	}

}
