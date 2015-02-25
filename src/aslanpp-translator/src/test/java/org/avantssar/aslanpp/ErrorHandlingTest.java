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
import org.avantssar.aslanpp.model.ConstantSymbol;
import org.avantssar.aslanpp.model.Entity;
import org.avantssar.aslanpp.model.EntityManager;
import org.avantssar.aslanpp.model.ErrorMessages;
import org.avantssar.aslanpp.model.MacroSymbol;
import org.avantssar.aslanpp.model.Prelude;
import org.avantssar.commons.ChannelModel;
import org.avantssar.commons.TranslatorOptions;

public class ErrorHandlingTest extends TestCase {

	private ASLanPPSpecification spec;
	private EntityManager manager;

	@Override
	public void setUp() {
		manager = new EntityManager();
		spec = new ASLanPPSpecification(manager, "spec", ChannelModel.CCM);
	}

	public void testActorTermOnBothSidesOfChannelGoal() {
		try {
			Entity env = spec.entity("Environment");
			Entity father = env.entity("Father");
			Entity child = father.entity("Child");
			env.body(env.newInstance(child));
			spec.finalize(false);
			spec.getErrorGatherer().report(System.out);

			ASLanPPConnectorImpl translator = new ASLanPPConnectorImpl();
			TranslatorOutput result = translator.translate(new TranslatorOptions(), "spec", spec.toString());
//			assertEquals(1, result.getErrors().size());
			assertEquals(0, result.getWarnings().size());
		}
		catch (Exception e) {
			fail("Should have raised no exception.");
		}
	}

	public void testDuplicateVariable() {

		try {
			Entity env = spec.entity("Environment");
			env.addStateVariable("A", env.findType(Prelude.MESSAGE));
			env.addStateVariable("A", env.findType(Prelude.MESSAGE));
//			fail("Should have raised an exception");
//		}
//		catch (ASLanPPException ae) {
			assertEquals(ErrorMessages.DUPLICATE_SYMBOL_IN_SCOPE, env.getErrorGatherer().remove(0).getCode());
		}
		catch (Throwable e) {
			fail("Wrong type of exception");
		}

	}

	public void testDuplicateConstant() {

		try {
			Entity env = spec.entity("Environment");
			env.constants(env.findType(Prelude.MESSAGE), "a");
			env.constants(env.findType(Prelude.MESSAGE), "a");
//			fail("Should have raised an exception");
//		}
//		catch (ASLanPPException ae) {
			assertEquals(ErrorMessages.DUPLICATE_SYMBOL_IN_SCOPE, env.getErrorGatherer().remove(0).getCode());
		}
		catch (Throwable e) {
			fail("Wrong type of exception");
		}

	}

	public void testDuplicateFunction() {

		try {
			Entity env = spec.entity("Environment");
			env.addFunction("f", env.findType(Prelude.MESSAGE), env.findType(Prelude.AGENT));
			env.addFunction("f", env.findType(Prelude.MESSAGE), env.findType(Prelude.AGENT));
//			fail("Should have raised an exception");
//		}
//		catch (ASLanPPException ae) {
			assertEquals(ErrorMessages.DUPLICATE_SYMBOL_IN_SCOPE, env.getErrorGatherer().remove(0).getCode());
		}
		catch (Throwable e) {
			fail("Expected DUPLICATE_SYMBOL_IN_SCOPE error");
		}

	}

	public void testDuplicateMacro() {

		try {
			Entity env = spec.entity("Environment");
			ConstantSymbol c = env.constants(env.findType(Prelude.MESSAGE), "c");
			MacroSymbol m1 = env.addMacro(null, "a");
			m1.setBody(c.term());
			MacroSymbol m2 = env.addMacro(null, "a");
			m2.setBody(c.term());
//			fail("Should have raised an exception");
//		}
//		catch (ASLanPPException ae) {
			assertEquals(ErrorMessages.DUPLICATE_SYMBOL_IN_SCOPE, env.getErrorGatherer().remove(0).getCode());
		}
		catch (Throwable e) {
			fail("Wrong type of exception");
		}

	}
}
