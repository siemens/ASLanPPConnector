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

import java.util.List;
import org.avantssar.aslan.ImplicitExplicitSymbol.ImplicitExplicitState;
import org.avantssar.commons.ErrorGatherer;
import org.avantssar.commons.LocationInfo;

public class HornClause extends TermsHolder {

	private final ITerm head;

	protected HornClause(LocationInfo location, ErrorGatherer err, String name, ITerm head) {
		super(location, err, name);
		if (!head.getType().equals(IASLanSpec.FACT)) {
			getErrorGatherer().addError(head.getLocation(), ASLanErrorMessages.CONSTRUCT_EXPECTS_ONLY_FACTS, "Clause", head.getRepresentation(), head.getType());
		}
		this.head = head;
		this.head.setImplicitExplicitState(ImplicitExplicitState.Implicit);
		buildContext();
	}

	public ITerm getHead() {
		return head;
	}

	public HornClause addBodyFact(ITerm term) {
		if (!term.getType().equals(IASLanSpec.FACT)) {
			getErrorGatherer().addError(term.getLocation(), ASLanErrorMessages.CONSTRUCT_EXPECTS_ONLY_FACTS, "Clause", term.getRepresentation(), term.getType());
		}
		super.addOneTerm(term);
		return this;
	}

	public List<ITerm> getBodyFacts() {
		return getTerms();
	}

	@Override
	protected void buildContext() {
		super.buildContext();
		head.buildContext(getContext(), false);
	}

	public void accept(IASLanVisitor visitor) {
		visitor.visit(this);
	}

}
