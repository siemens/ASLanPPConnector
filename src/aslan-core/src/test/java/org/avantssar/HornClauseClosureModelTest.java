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

public class HornClauseClosureModelTest extends GenericModelTest {

	@Override
	public org.avantssar.aslan.IASLanSpec getASLanSpec() {
		org.avantssar.aslan.IASLanSpec spec = org.avantssar.aslan.ASLanSpecificationBuilder.instance().createASLanSpecification();

		// supertypes

		// variables
		spec.variable("A", spec.primitiveType("text"));
		spec.variable("B", spec.primitiveType("text"));
		spec.variable("C", spec.primitiveType("text"));

		// constants
		spec.constant("a", spec.primitiveType("text"));
		spec.constant("b", spec.primitiveType("text"));
		spec.constant("c", spec.primitiveType("text"));
		spec.constant("start", spec.primitiveType("message"));

		// functions
		spec.function("base_relation", spec.primitiveType("fact"), spec.primitiveType("text"), spec.primitiveType("text"));
		spec.function("relation", spec.primitiveType("fact"), spec.primitiveType("text"), spec.primitiveType("text"));

		// equations

		// initial states
		spec.initialState("init").addFact(spec.findFunction("iknows").term(spec.findConstant("start").term()));

		// Horn clauses
		spec.hornClause("closure", spec.findFunction("relation").term(spec.findVariable("A").term(), spec.findVariable("C").term())).addBodyFact(
				spec.findFunction("relation").term(spec.findVariable("A").term(), spec.findVariable("B").term())).addBodyFact(
				spec.findFunction("relation").term(spec.findVariable("B").term(), spec.findVariable("C").term()));
		spec.hornClause("base", spec.findFunction("relation").term(spec.findVariable("A").term(), spec.findVariable("B").term())).addBodyFact(
				spec.findFunction("base_relation").term(spec.findVariable("A").term(), spec.findVariable("B").term()));

		// rewrite rules
		spec.rule("step_1").addLHS(spec.findFunction("iknows").term(spec.findConstant("start").term())).addRHS(
				spec.findFunction("base_relation").term(spec.findConstant("a").term(), spec.findConstant("b").term())).addRHS(
				spec.findFunction("base_relation").term(spec.findConstant("b").term(), spec.findConstant("c").term()));

		// attack states
		spec.attackState("transitive").addTerm(spec.findFunction("relation").term(spec.findConstant("a").term(), spec.findConstant("c").term()));

		// goals

		return spec;
	}

}
