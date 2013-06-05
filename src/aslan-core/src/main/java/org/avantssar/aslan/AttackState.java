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

package org.avantssar.aslan;

import java.util.ArrayList;
import java.util.List;
import org.avantssar.commons.ErrorGatherer;
import org.avantssar.commons.LocationInfo;

public class AttackState extends TermsHolder {

	protected AttackState(LocationInfo location, ErrorGatherer err, String name) {
		super(location, err, name);
	}

	public AttackState addTerm(ITerm term) {
		if (!term.getType().equals(IASLanSpec.FACT)) {
			getErrorGatherer().addException(term.getLocation(), ASLanErrorMessages.CONSTRUCT_EXPECTS_ONLY_FACTS, "Attack state", term.getRepresentation(), term.getType());
		}
		super.addOneTerm(term);
		return this;
	}

	public List<ITerm> getFacts() {
		List<ITerm> facts = new ArrayList<ITerm>();
		for (ITerm t : getTerms()) {
			if (!t.isCondition()) {
				facts.add(t);
			}
		}
		return facts;
	}

	public List<ITerm> getConditions() {
		List<ITerm> conditions = new ArrayList<ITerm>();
		for (ITerm t : getTerms()) {
			if (t.isCondition()) {
				conditions.add(t);
			}
		}
		return conditions;
	}

	public void accept(IASLanVisitor visitor) {
		visitor.visit(this);
	}

}
