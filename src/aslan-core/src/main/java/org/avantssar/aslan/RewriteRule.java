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
import org.avantssar.aslan.ImplicitExplicitSymbol.ImplicitExplicitState;
import org.avantssar.commons.ErrorGatherer;
import org.avantssar.commons.LocationInfo;

public class RewriteRule extends AbstractNamed implements IParameterized {

	private final List<Variable> explicitParameters = new ArrayList<Variable>();
	private final List<Variable> parameters = new ArrayList<Variable>();
	private final List<ITerm> lhsTerms = new ArrayList<ITerm>();
	private final List<Variable> exists = new ArrayList<Variable>();
	private final List<ITerm> rhsTerms = new ArrayList<ITerm>();

	protected RewriteRule(LocationInfo location, ErrorGatherer err, String name) {
		super(location, err, name, new LowerNameValidator("rewrite rule"));
	}

	public List<Variable> getParameters() {
		return parameters;
	}

	public List<Variable> getExists() {
		List<Variable> real = new ArrayList<Variable>();
		for (Variable v : exists) {
			if (parameters.contains(v)) {
				real.add(v);
			}
		}
		return real;
	}

	public RewriteRule addExists(Variable v) {
		exists.add(v);
		Collections.sort(exists);
		return this;
	}

	public void addParameter(Variable name) {
		explicitParameters.add(name);
	}

	public List<Variable> getExplicitParameters() {
		return explicitParameters;
	}

	public List<ITerm> getLHS() {
		return lhsTerms;
	}

	public List<ITerm> getRHS() {
		return rhsTerms;
	}

	public List<ITerm> getTerms(boolean toLeft) {
		return toLeft ? getLHS() : getRHS();
	}

	protected void buildContext() {
		TermContext ctx = new TermContext();
		for (ITerm t : lhsTerms) {
			t.buildContext(ctx, false);
		}
		for (ITerm t : rhsTerms) {
			t.buildContext(ctx, false);
		}
		parameters.clear();
		parameters.addAll(ctx.getParameters());
	}

	public RewriteRule addLHS(ITerm term) {
		if (!term.getType().equals(IASLanSpec.FACT)) {
			getErrorGatherer().addException(term.getLocation(), ASLanErrorMessages.CONSTRUCT_EXPECTS_ONLY_FACTS, "Rewrite rule", term.getRepresentation(), term.getType());
		}
		lhsTerms.add(term);
		Collections.sort(lhsTerms);
		buildContext();
		return this;
	}

	public RewriteRule addRHS(ITerm term) {
		if (!term.getType().equals(IASLanSpec.FACT)) {
			getErrorGatherer().addException(term.getLocation(), ASLanErrorMessages.CONSTRUCT_EXPECTS_ONLY_FACTS, "Rewrite rule", term.getRepresentation(), term.getType());
		}
		rhsTerms.add(term);
		Collections.sort(rhsTerms);
		buildContext();
		term.setImplicitExplicitState(ImplicitExplicitState.Explicit);
		return this;
	}

	public RewriteRule add(ITerm term, boolean toLeft) {
		return toLeft ? addLHS(term) : addRHS(term);
	}

	public RewriteRule remove(ITerm term, boolean toLeft) {
		if (toLeft) {
			lhsTerms.remove(term);
		}
		else {
			rhsTerms.remove(term);
		}
		buildContext();
		return this;
	}

	public boolean contains(ITerm term, boolean toLeft) {
		return toLeft ? lhsTerms.contains(term) : rhsTerms.contains(term);
	}

	public void accept(IASLanVisitor visitor) {
		visitor.visit(this);
	}

}
