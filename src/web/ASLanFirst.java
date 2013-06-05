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

import java.io.FileOutputStream;
import java.io.PrintStream;
import org.avantssar.aslan.ASLanSpecificationBuilder;
import org.avantssar.aslan.AttackState;
import org.avantssar.aslan.Constant;
import org.avantssar.aslan.Function;
import org.avantssar.aslan.HornClause;
import org.avantssar.aslan.IASLanSpec;
import org.avantssar.aslan.InitialState;
import org.avantssar.aslan.RewriteRule;
import org.avantssar.aslan.Variable;

public class ASLanFirst {

	public static void main(String[] args) throws Exception {
		// create a fresh ASLan specification
		IASLanSpec spec = ASLanSpecificationBuilder.instance().createASLanSpecification();
		// define a variable
		Variable m = spec.variable("M", IASLanSpec.MESSAGE);
		// define a constant
		Constant a = spec.constant("a", IASLanSpec.MESSAGE);
		// define two functions:
		// premise : message -> fact
		// conclusion : message -> fact
		Function premise = spec.function("premise", IASLanSpec.FACT, IASLanSpec.MESSAGE);
		Function conclusion = spec.function("conclusion", IASLanSpec.FACT, IASLanSpec.MESSAGE);
		// define a Horn clause:
		// premise(M) :- iknows(M)
		HornClause hc = spec.hornClause("basic_premise", premise.term(m.term()));
		hc.addBodyFact(IASLanSpec.IKNOWS.term(m.term()));
		// define a rewrite rule:
		// premise(M) => conclusion(M)
		RewriteRule rr = spec.rule("inference");
		rr.addLHS(premise.term(m.term()));
		rr.addRHS(conclusion.term(m.term()));
		// add an initial state:
		// iknows(a)
		InitialState is = spec.initialState("init");
		is.addFact(IASLanSpec.IKNOWS.term(a.term()));
		// add an attack state:
		// !conclusion(a)
		AttackState as = spec.attackState("trouble");
		as.addTerm(conclusion.term(a.term()));

		// write the ASLan specification to a file
		PrintStream out = new PrintStream(new FileOutputStream("simple.aslan"));
		out.print(spec.toPlainText());
		out.close();
	}
}
