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

public class SetLiteralTerm extends AbstractTerm {

	private final List<ITerm> terms;
	private String symbolName;
	private ITerm setTerm;
	private IType elementsType;

	private final String nameHint;

	public static SetLiteralTerm set(IScope scope, ITerm... terms) {
		return new SetLiteralTerm(null, scope, terms, null);
	}

	public SetLiteralTerm(LocationInfo location, IScope owner, ITerm[] terms, String nameHint) {
		this(location, owner, Arrays.asList(terms), nameHint);
	}

	public SetLiteralTerm(LocationInfo location, IScope owner, List<ITerm> terms, String nameHint) {
		super(location, owner, false);
		this.terms = terms;
		this.nameHint = nameHint;
		for (ITerm t : terms) {
			super.addChildrenTerm(t);
		}
	}

	public String getNameHint() {
		return nameHint;
	}

	public List<ITerm> getTerms() {
		return terms;
	}

	public String getSymbolName() {
		return symbolName;
	}

	public IType getElementsType() {
		return elementsType;
	}

	public void setElementsType(IType type) {
		this.elementsType = type;
	}

	public void setSymbolNameAndTerm(String sym, ITerm term) {
		this.symbolName = sym;
		this.setTerm = term;
	}

	public ITerm getSetTerm() {
		return setTerm;
	}

	public ITerm toTerm() {
		return setTerm;
	}

	public IType inferType() {
		if (terms.size() > 0) {
			return new SetType(terms.get(0).inferType());
		}
		else {
			return Prelude.getSetOf(getScope().findType(Prelude.MESSAGE));
		}
	}

	@Override
	public void buildContext(ExpressionContext ctx, boolean isInNegatedCondition) {
		super.buildContext(ctx, isInNegatedCondition);
		if (!ctx.wereAuxiliaryTermsAdded(symbolName)) {
			FunctionSymbol fncContains = getScope().findFunction(Prelude.CONTAINS);
			for (ITerm t : terms) {
				ITerm containsTerm = new FunctionTerm(getLocation(), getScope(), fncContains, false, new ITerm[] { setTerm, t });
				ctx.addAuxiliaryTerm(containsTerm);
			}
			ctx.markAuxiliaryTermsAdded(symbolName);
		}
		ctx.addSetLiteralName(symbolName); // TODO better not add for secrecy goal statements
		ctx.addSetLiteral(this);
	}

	public ITerm reduce(SymbolsState symState) {
		List<ITerm> reducedTerms = new ArrayList<ITerm>();
		for (ITerm t : terms) {
			reducedTerms.add(t.reduce(symState));
		}
		SetLiteralTerm newTerm = new SetLiteralTerm(getLocation(), getScope(), reducedTerms, symbolName);
		newTerm.setSymbolNameAndTerm(symbolName, setTerm);
		return newTerm;
	}

	@Override
	public ITerm accept(IASLanPPVisitor visitor) {
		return visitor.visit(this);
	}

	@Override
	public boolean isTypeCertain() {
		return isTypeCertainAll(terms);
	}

	@Override
	public boolean wasTypeSet() {
		return wasTypeSetAll(terms);
	}

}
