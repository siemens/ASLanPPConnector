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
import java.util.Collections;
import java.util.List;
import org.avantssar.commons.ErrorGatherer;
import org.avantssar.commons.LocationInfo;

public abstract class TermsHolder extends AbstractNamed implements IParameterized {

	private final List<ITerm> terms = new ArrayList<ITerm>();
	private final TermContext ctx = new TermContext();
	private final List<Variable> explicitParameters = new ArrayList<Variable>();

	protected TermsHolder(LocationInfo location, ErrorGatherer err, String name) {
		super(location, err, name, new LowerNameValidator());
	}

	protected void addOneTerm(ITerm term) {
		terms.add(term);
		Collections.sort(terms);
		buildContext();
	}

	public List<Variable> getParameters() {
		ArrayList<Variable> al = new ArrayList<Variable>();
		al.addAll(ctx.getParameters());
		return al;
	}

	public void addParameter(Variable name) {
		explicitParameters.add(name);
	}

	public List<Variable> getExplicitParameters() {
		return explicitParameters;
	}

	protected List<ITerm> getTerms() {
		return terms;
	}

	protected TermContext getContext() {
		return ctx;
	}

	protected void buildContext() {
		ctx.purge();
		for (ITerm t : terms) {
			t.buildContext(ctx, false);
		}
	}

}
