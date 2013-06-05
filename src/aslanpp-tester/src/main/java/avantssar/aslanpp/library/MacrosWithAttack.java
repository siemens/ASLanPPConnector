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

import org.avantssar.aslan.IASLanSpec;
import org.avantssar.aslan.InitialState;
import org.avantssar.aslanpp.model.ConstantSymbol;
import org.avantssar.aslanpp.model.ITerm;
import avantssar.aslanpp.testing.Specification;

@Specification
public class MacrosWithAttack extends MacrosWithoutAttack {

	private ConstantSymbol innerActorPP;
	private org.avantssar.aslan.Constant innerActor;

	@Override
	protected void addASLanPPActor() {
		innerActorPP = env.constants(tppAgent, "another_agent");
		innerActorPP.setNonPublic(true);
	}

	@Override
	protected ITerm getASLanPPActor() {
		return innerActorPP.term();
	}

	@Override
	protected void addASLanActor(IASLanSpec spec, InitialState init) {
		innerActor = spec.constant(innerActorPP.getName(), IASLanSpec.AGENT);
		// init.addFact(fIsAgent.term(innerActor.term()));
	}

	@Override
	protected org.avantssar.aslan.ITerm getASLanActor() {
		return innerActor.term();
	}

}
