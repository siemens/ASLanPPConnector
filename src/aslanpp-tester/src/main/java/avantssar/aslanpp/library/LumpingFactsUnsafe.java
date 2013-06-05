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
import org.avantssar.aslan.RewriteRule;
import org.avantssar.aslanpp.model.Entity;
import avantssar.aslanpp.testing.Specification;

@Specification
public class LumpingFactsUnsafe extends LumpingFactsSafe {

	@Override
	protected void setBreakpoints(Entity timer) {
		timer.addBreakpoints(cPhasePP.getOriginalName());
	}

	@Override
	protected void addTimerSteps(IASLanSpec spec) {
		RewriteRule step2 = spec.rule(getNextStepName("SubmissionTimer"));
		step2.addLHS(fsTimer.term(vTimerActor.term(), vTimerIID.term(), spec.numericTerm(1)));
		step2.addRHS(fsTimer.term(vTimerActor.term(), vTimerIID.term(), spec.numericTerm(2)));
		step2.addRHS(cPhase.term());
		step2.addLHS(fDishonest.term(vTimerActor.term()).negate());

		RewriteRule step3 = spec.rule(getNextStepName("SubmissionTimer"));
		step3.addLHS(fsTimer.term(vTimerActor.term(), vTimerIID.term(), spec.numericTerm(2)));
		step3.addLHS(cPhase.term());
		step3.addRHS(fsTimer.term(vTimerActor.term(), vTimerIID.term(), spec.numericTerm(3)));
	}

}
