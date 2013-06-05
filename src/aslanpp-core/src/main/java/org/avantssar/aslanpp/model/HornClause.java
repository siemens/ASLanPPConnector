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

package org.avantssar.aslanpp.model;

import java.util.ArrayList;
import java.util.List;

import org.avantssar.aslanpp.model.ImplicitExplicitSymbol.ImplicitExplicitState;
import org.avantssar.aslanpp.visitors.IASLanPPVisitor;
import org.avantssar.commons.LocationInfo;

public class HornClause extends GenericScope {

	private final List<VariableSymbol> arguments = new ArrayList<VariableSymbol>();
	private final List<VariableSymbol> universallyQuantified = new ArrayList<VariableSymbol>();
	private ITerm head;
	private final List<ITerm> body = new ArrayList<ITerm>();
	private final List<IExpression> equalities = new ArrayList<IExpression>();
	private boolean partOfPrelude;
	private final LocationInfo location;

	public HornClause(IScope owner, LocationInfo location, String name) {
		super(owner, name);
		this.location = location;
	}

	public LocationInfo getLocation() {
		return location;
	}

	public VariableSymbol universallyQuantified(String name) {
		return universallyQuantified(name, null);
	}

	public VariableSymbol universallyQuantified(String name,
			LocationInfo location) {
		for (VariableSymbol arg : arguments) {
			if (arg.getOriginalName().equals(name)) {
				getErrorGatherer()
						.addException(
								location,
								ErrorMessages.HORN_CLAUSE_ARGUMENT_CANNOT_BE_UNIVERSALLY_QUANTIFIED,
								name, getOriginalName());
			}
		}
		VariableSymbol var = addUntypedVariable(name, location);
		universallyQuantified.add(var);
		return var;
	}

	public VariableSymbol argument(String name) {
		return argument(name, null);
	}

	public VariableSymbol argument(String name, LocationInfo location) {
		VariableSymbol var = addUntypedVariable(name, location);
		addArgument(var);
		return var;
	}

	public void addArgument(VariableSymbol var) {
		arguments.add(var);
	}

	public ITerm getHead() {
		return head;
	}

	public void setHead(ITerm term) {
		this.head = term;
		this.head.setImplicitExplicitState(ImplicitExplicitState.implicit);
	}

	public List<ITerm> getBody() {
		return body;
	}

	public void addBody(ITerm term) {
		this.body.add(term);
	}

	public void addEquality(IExpression eq) {
		if (!(eq instanceof EqualityExpression)
				&& !(eq instanceof InequalityExpression)) {
			getErrorGatherer().addException(getLocation(),
					ErrorMessages.EQUALITY_EXPECTED_IN_HORN_CLAUSE,
					eq.getRepresentation());
		}
		this.equalities.add(eq);
	}

	public List<IExpression> getEqualities() {
		return equalities;
	}

	public void setBody(List<ITerm> body) {
		this.body.clear();
		this.body.addAll(body);
	}

	public void setEqualities(List<IExpression> eq) {
		this.equalities.clear();
		this.equalities.addAll(eq);
	}

	public List<VariableSymbol> getArguments() {
		return arguments;
	}

	public List<VariableSymbol> getUniversallyQuantified() {
		return universallyQuantified;
	}

	// public String getRepresentation(TextOutputSettings settings) {
	// StringBuffer sb = new StringBuffer();
	// sb.append(settings.getLineStart());
	// sb.append(getOriginalName());
	// if (arguments.size() > 0) {
	// sb.append("(");
	// boolean first = true;
	// for (VariableSymbol v : arguments) {
	// if (!first) sb.append(", ");
	// sb.append(v.getOriginalName());
	// first = false;
	// }
	// sb.append(")");
	// }
	// sb.append(" : ");
	// sb.append("\n").append(settings.indent().getLineStart());
	// if (universallyQuantified.size() > 0) {
	// sb.append("forall");
	// for (VariableSymbol v : universallyQuantified)
	// sb.append(" ").append(v.getOriginalName());
	// sb.append(" . ");
	// }
	// sb.append(head.getRepresentation(settings.unindentAll()));
	// if (body.size() > 0) {
	// sb.append(" :-");
	// boolean first = true;
	// for (ITerm t : body) {
	// if (!first) sb.append(" &");
	// sb.append("\n");
	// sb.append(settings.indent().indent().getLineStart()).append(t.getRepresentation(settings.unindentAll()));
	// first = false;
	// }
	// }
	// sb.append(";");
	// return sb.toString();
	// }

	public void accept(IASLanPPVisitor visitor) {
		visitor.visit(this);
	}

	public boolean isPartOfPrelude() {
		return partOfPrelude;
	}

	public void setPartOfPrelude(boolean partOfPrelude) {
		this.partOfPrelude = partOfPrelude;
	}
}
