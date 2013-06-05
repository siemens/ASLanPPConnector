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

import org.avantssar.aslanpp.model.ASLanPPSpecification;
import org.avantssar.aslanpp.model.BlockStatement;
import org.avantssar.aslanpp.model.ConstantSymbol;
import org.avantssar.aslanpp.model.Entity;
import org.avantssar.aslanpp.model.FunctionSymbol;
import org.avantssar.aslanpp.model.Goal;
import org.avantssar.aslanpp.model.SelectStatement;
import org.avantssar.aslanpp.model.SimpleType;
import org.avantssar.aslanpp.model.VariableSymbol;
import avantssar.aslanpp.testing.Specification;

@Specification
public class SimpleMessageExchangeWithGuard extends SimpleMessageExchangeWithReceive {

	@Override
	public ASLanPPSpecification getASLanPPSpecification() {
		ASLanPPSpecification spec = startASLanPPSpec();

		SimpleType tSpecialAgent = env.type("special_agent", tppAgent);
		SimpleType tSpecialMessage = env.type("special_message", tppMessage);
		ConstantSymbol cAlice = env.constants(tSpecialAgent, "alice");
		cAlice.setNonPublic(true);
		ConstantSymbol cBob = env.constants(tSpecialAgent, "bob");
		cBob.setNonPublic(true);
		env.group(cAlice, cBob);
		ConstantSymbol cToken = env.constants(tSpecialMessage, "token");
		cToken.setNonPublic(true);

		Entity alice = env.entity("Alice");
		alice.addParameter(Entity.ACTOR_PREFIX, tSpecialAgent);
		VariableSymbol vAliceB = alice.addParameter("B", tSpecialAgent);
		alice.group(alice.getActorSymbol(), vAliceB);
		BlockStatement aliceBody = alice.body(alice.block());
		aliceBody.add(alice.comm(alice.getActorSymbol().term(), vAliceB.term(), cToken.term(), null, channelType, false, false, false));

		Entity bob = env.entity("Bob");
		bob.addParameter(Entity.ACTOR_PREFIX, tSpecialAgent);
		VariableSymbol vBobPartner = bob.addStateVariable("Partner", tSpecialAgent);
		FunctionSymbol fDone = bob.addFunction("done", tppFact, tSpecialAgent);
		fDone.setNonInvertible(true);
		fDone.setNonPublic(true);
		BlockStatement bobBody = bob.body(bob.block());
		SelectStatement bobSel = bobBody.add(bob.select());
		bobSel.choice(bob.communication(vBobPartner.matchedTerm(), bob.getActorSymbol().term(), cToken.term(), null, channelType, true, false, false).expression(), bob.introduce(fDone.term(bob
				.getActorSymbol().term())));
		Goal gNeverDone = bob.goal("never_done");
		gNeverDone.setFormula(fDone.term(cBob.term()).expression().not());

		BlockStatement envBody = env.body(env.block());
		envBody.add(env.newInstance(alice, cAlice.term(), cBob.term()));
		envBody.add(env.newInstance(bob, cBob.term()));

		return spec;
	}

}
