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

import org.avantssar.aslan.AttackState;
import org.avantssar.aslan.IASLanSpec;
import org.avantssar.aslanpp.model.Entity;
import org.avantssar.aslanpp.model.Goal;
import avantssar.aslanpp.testing.Specification;

@Specification
public class SelectsWithAttack extends SelectsNoAttack {

	@Override
	protected void addGoalPP(Entity env) {
		Goal g = env.goal("safe");
		g.setFormula(x1.expr().and(x2.expr().not()).and(x3.expr()).not());
	}

	@Override
	protected void addGoal(IASLanSpec spec) {
		AttackState as = spec.attackState("safe");
		as.addTerm(cX1.term());
		as.addTerm(cX2.term().negate());
		as.addTerm(cX3.term());
        as.addTerm(fsEnv.term(vActor.term(), vIID.term(), vSL.term()));
	}

}
