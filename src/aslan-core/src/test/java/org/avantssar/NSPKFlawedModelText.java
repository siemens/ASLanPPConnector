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

package org.avantssar;

public class NSPKFlawedModelText extends GenericModelTest {

	@Override
	public org.avantssar.aslan.IASLanSpec getASLanSpec() {
		org.avantssar.aslan.IASLanSpec spec = org.avantssar.aslan.ASLanSpecificationBuilder.instance().createASLanSpecification();

		// supertypes

		// variables
		spec.variable("A", spec.primitiveType("agent"));
		spec.variable("A_1", spec.primitiveType("agent"));
		spec.variable("B", spec.primitiveType("agent"));
		spec.variable("IID", spec.primitiveType("nat"));
		spec.variable("M", spec.primitiveType("text"));
		spec.variable("Na", spec.primitiveType("text"));
		spec.variable("Na_1", spec.primitiveType("text"));
		spec.variable("Nb", spec.primitiveType("text"));
		spec.variable("Nb_1", spec.primitiveType("text"));
		spec.variable("PID", spec.primitiveType("protocol_id"));
		spec.variable("S", spec.setType(spec.primitiveType("agent")));

		// constants
		spec.constant("alice", spec.primitiveType("agent"));
		spec.constant("bob", spec.primitiveType("agent"));
		spec.constant("dummy_agent", spec.primitiveType("agent"));
		spec.constant("dummy_text", spec.primitiveType("text"));
		spec.constant("secret_na", spec.primitiveType("protocol_id"));
		spec.constant("secret_nb", spec.primitiveType("protocol_id"));

		// functions
		spec.function("set_Alice", spec.setType(spec.primitiveType("agent")), spec.primitiveType("nat"));
		spec.function("set_Bob", spec.setType(spec.primitiveType("agent")), spec.primitiveType("nat"));
		spec.function("state_Alice", spec.primitiveType("fact"), spec.primitiveType("agent"), spec.primitiveType("nat"), spec.primitiveType("nat"), spec.primitiveType("text"), spec
				.primitiveType("text"), spec.primitiveType("agent"));
		spec.function("state_Bob", spec.primitiveType("fact"), spec.primitiveType("agent"), spec.primitiveType("nat"), spec.primitiveType("nat"), spec.primitiveType("text"), spec
				.primitiveType("text"), spec.primitiveType("agent"));

		// equations

		// initial states
		spec.initialState("init").addFact(spec.findFunction("iknows").term(spec.findConstant("alice").term())).addFact(spec.findFunction("iknows").term(spec.findConstant("bob").term())).addFact(
				spec.findFunction("iknows").term(spec.findConstant("i").term())).addFact(
				spec.findFunction("iknows").term(spec.findFunction("inv").term(spec.findFunction("pk").term(spec.findConstant("i").term())))).addFact(
				spec.findFunction("iknows").term(spec.findFunction("pk").term(spec.findConstant("alice").term()))).addFact(
				spec.findFunction("iknows").term(spec.findFunction("pk").term(spec.findConstant("bob").term()))).addFact(
				spec.findFunction("iknows").term(spec.findFunction("pk").term(spec.findConstant("i").term()))).addFact(
				spec.findFunction("state_Alice").term(spec.findConstant("alice").term(), spec.numericTerm(100), spec.numericTerm(1), spec.findConstant("dummy_text").term(),
						spec.findConstant("dummy_text").term(), spec.findConstant("bob").term())).addFact(
				spec.findFunction("state_Alice").term(spec.findConstant("alice").term(), spec.numericTerm(101), spec.numericTerm(1), spec.findConstant("dummy_text").term(),
						spec.findConstant("dummy_text").term(), spec.findConstant("i").term())).addFact(
				spec.findFunction("state_Bob").term(spec.findConstant("bob").term(), spec.numericTerm(200), spec.numericTerm(1), spec.findConstant("dummy_text").term(),
						spec.findConstant("dummy_text").term(), spec.findConstant("dummy_agent").term())).addFact(
				spec.findFunction("state_Bob").term(spec.findConstant("bob").term(), spec.numericTerm(201), spec.numericTerm(1), spec.findConstant("dummy_text").term(),
						spec.findConstant("dummy_text").term(), spec.findConstant("dummy_agent").term()));

		// Horn clauses

		// rewrite rules
		spec.rule("step_1").addLHS(
				spec.findFunction("state_Alice").term(spec.findVariable("A").term(), spec.findVariable("IID").term(), spec.numericTerm(1), spec.findVariable("Na").term(),
						spec.findVariable("Nb").term(), spec.findVariable("B").term())).addExists(spec.findVariable("Na_1")).addRHS(
				spec.findFunction("contains").term(spec.findVariable("A").term(), spec.findFunction("set_Alice").term(spec.findVariable("IID").term()))).addRHS(
				spec.findFunction("contains").term(spec.findVariable("B").term(), spec.findFunction("set_Alice").term(spec.findVariable("IID").term()))).addRHS(
				spec.findFunction("iknows").term(
						spec.findFunction("crypt").term(spec.findFunction("pk").term(spec.findVariable("B").term()),
								spec.findFunction("pair").term(spec.findVariable("Na_1").term(), spec.findVariable("A").term())))).addRHS(
				spec.findFunction("secret").term(spec.findVariable("Na_1").term(), spec.findConstant("secret_na").term(), spec.findFunction("set_Alice").term(spec.findVariable("IID").term())))
				.addRHS(
						spec.findFunction("state_Alice").term(spec.findVariable("A").term(), spec.findVariable("IID").term(), spec.numericTerm(2), spec.findVariable("Na_1").term(),
								spec.findVariable("Nb").term(), spec.findVariable("B").term()));
		spec.rule("step_2").addLHS(
				spec.findFunction("iknows").term(
						spec.findFunction("crypt").term(spec.findFunction("pk").term(spec.findVariable("B").term()),
								spec.findFunction("pair").term(spec.findVariable("Na_1").term(), spec.findVariable("A_1").term())))).addLHS(
				spec.findFunction("state_Bob").term(spec.findVariable("B").term(), spec.findVariable("IID").term(), spec.numericTerm(1), spec.findVariable("Nb").term(),
						spec.findVariable("Na").term(), spec.findVariable("A").term())).addExists(spec.findVariable("Nb_1")).addRHS(
				spec.findFunction("contains").term(spec.findVariable("A_1").term(), spec.findFunction("set_Bob").term(spec.findVariable("IID").term()))).addRHS(
				spec.findFunction("contains").term(spec.findVariable("B").term(), spec.findFunction("set_Bob").term(spec.findVariable("IID").term()))).addRHS(
				spec.findFunction("iknows").term(
						spec.findFunction("crypt").term(spec.findFunction("pk").term(spec.findVariable("A_1").term()),
								spec.findFunction("pair").term(spec.findVariable("Na_1").term(), spec.findVariable("Nb_1").term())))).addRHS(
				spec.findFunction("secret").term(spec.findVariable("Nb_1").term(), spec.findConstant("secret_nb").term(), spec.findFunction("set_Bob").term(spec.findVariable("IID").term()))).addRHS(
				spec.findFunction("state_Bob").term(spec.findVariable("B").term(), spec.findVariable("IID").term(), spec.numericTerm(2), spec.findVariable("Nb_1").term(),
						spec.findVariable("Na_1").term(), spec.findVariable("A_1").term()));
		spec.rule("step_3").addLHS(
				spec.findFunction("iknows").term(
						spec.findFunction("crypt").term(spec.findFunction("pk").term(spec.findVariable("A").term()),
								spec.findFunction("pair").term(spec.findVariable("Na").term(), spec.findVariable("Nb_1").term())))).addLHS(
				spec.findFunction("state_Alice").term(spec.findVariable("A").term(), spec.findVariable("IID").term(), spec.numericTerm(2), spec.findVariable("Na").term(),
						spec.findVariable("Nb").term(), spec.findVariable("B").term())).addRHS(
				spec.findFunction("iknows").term(spec.findFunction("crypt").term(spec.findFunction("pk").term(spec.findVariable("B").term()), spec.findVariable("Nb_1").term()))).addRHS(
				spec.findFunction("state_Alice").term(spec.findVariable("A").term(), spec.findVariable("IID").term(), spec.numericTerm(3), spec.findVariable("Na").term(),
						spec.findVariable("Nb_1").term(), spec.findVariable("B").term()));

		// attack states
		spec.attackState("compromised").addTerm(spec.findFunction("iknows").term(spec.findVariable("M").term())).addTerm(
				spec.findFunction("contains").term(spec.findConstant("i").term(), spec.findVariable("S").term()).negate()).addTerm(
				spec.findFunction("secret").term(spec.findVariable("M").term(), spec.findVariable("PID").term(), spec.findVariable("S").term()));

		// goals

		return spec;
	}

}
