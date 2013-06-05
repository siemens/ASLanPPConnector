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
import java.util.Arrays;
import java.util.List;

import org.avantssar.aslanpp.visitors.IASLanPPVisitor;
import org.avantssar.commons.LocationInfo;

public class ConcatTerm extends AbstractTerm {

	private final List<ITerm> terms;

	public static ConcatTerm concat(IScope scope, ITerm... terms) {
		return concat(null, scope, terms);
	}

	public static ConcatTerm concat(LocationInfo location, IScope scope,
			ITerm... terms) {
		return new ConcatTerm(location, scope, terms);
	}

	protected ConcatTerm(LocationInfo location, IScope scope, ITerm... terms) {
		this(location, scope, Arrays.asList(terms));
	}

	protected ConcatTerm(LocationInfo location, IScope scope, List<ITerm> terms) {
		super(location, scope, false);
		this.terms = terms;
		for (ITerm t : terms) {
			super.addChildrenTerm(t);
		}
	}

	public List<ITerm> getTerms() {
		return terms;
	}

	public IType inferType() {
		return getScope().findType(Prelude.MESSAGE);
	}

	public ITerm reduce(SymbolsState symState) {
		List<ITerm> reducedTerms = new ArrayList<ITerm>();
		for (ITerm t : terms) {
			reducedTerms.add(t.reduce(symState));
		}
		return new ConcatTerm(getLocation(), getScope(), reducedTerms);
	}

	@Override
	public ITerm accept(IASLanPPVisitor visitor) {
		return visitor.visit(this);
	}
}
