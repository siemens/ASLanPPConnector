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

import org.avantssar.aslanpp.visitors.IASLanPPVisitor;
import org.avantssar.commons.LocationInfo;

public class PseudonymTerm extends AbstractTerm {

	private final ITerm baseTerm;
	private final ITerm pseudonym;

	protected PseudonymTerm(LocationInfo location, IScope scope, ITerm baseTerm, ITerm pseudonym) {
		super(location, scope, false);
		this.baseTerm = baseTerm;
		this.pseudonym = pseudonym;
		super.addChildrenTerm(baseTerm);
		super.addChildrenTerm(pseudonym);
	}

	public ITerm getBaseTerm() {
		return baseTerm;
	}

	public ITerm getPseudonym() {
		return pseudonym;
	}

	public IType inferType() {
		return pseudonym.inferType();
	}

	public ITerm getOriginalAgent() {
		if (baseTerm instanceof PseudonymTerm) {
			PseudonymTerm pseudoBase = (PseudonymTerm) baseTerm;
			return pseudoBase.getOriginalAgent();
		}
		else {
			return baseTerm;
		}
	}

	public ITerm reduce(SymbolsState symState) {
		return new PseudonymTerm(getLocation(), getScope(), baseTerm.reduce(symState), pseudonym.reduce(symState));
	}

	@Override
	public boolean holdsActor() {
		return baseTerm.holdsActor();
	}

	@Override
	public ITerm accept(IASLanPPVisitor visitor) {
		return visitor.visit(this);
	}

}
