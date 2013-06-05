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

import org.avantssar.aslanpp.model.ImplicitExplicitSymbol.ImplicitExplicitState;
import org.avantssar.aslanpp.visitors.IASLanPPVisitor;
import org.avantssar.commons.LocationInfo;

public class FunctionTerm extends AbstractSymbolTerm<FunctionSymbol> {

	protected List<ITerm> argumentsValues;
	private boolean oopStyle;

	protected FunctionTerm(LocationInfo location, IScope scope,
			FunctionSymbol symbol, boolean discardOnRHS,
			ITerm... argumentsValues) {
		this(location, scope, symbol, discardOnRHS, Arrays
				.asList(argumentsValues));
	}

	protected FunctionTerm(LocationInfo location, IScope scope,
			FunctionSymbol symbol, boolean discardOnRHS,
			List<ITerm> argumentsValues) {
		super(location, scope, symbol, discardOnRHS);
		if (argumentsValues == null) {
			throw new IllegalArgumentException(
					"Cannot create a function term with null arguments.");
		}
		setArguments(argumentsValues);
	}

	public void setOOPStyle(boolean flag) {
		this.oopStyle = flag;
	}

	public boolean isOOPStyle() {
		return oopStyle;
	}

	public List<ITerm> getArguments() {
		return argumentsValues;
	}

	// TODO: this is needed as a hack for asserts. handle this more nicely.
	public void setArguments(List<ITerm> argumentsValues) {
		this.argumentsValues = argumentsValues;
		super.clearChildren();
		for (ITerm t : argumentsValues) {
			super.addChildrenTerm(t);
		}
	}

	@Override
	public IType inferType() {
		FunctionSymbol fsym = getSymbol();
		return fsym.getType();
	}

	@Override
	public void buildContext(ExpressionContext ctx, boolean isInNegatedCondition) {
		super.buildContext(ctx, isInNegatedCondition);
		if (ctx.isBreakpoint(getSymbol().getOriginalName())) {
			ctx.setBreakpoint();
		}
	}

	public ITerm reduce(SymbolsState symState) {
		List<ITerm> reducedTerms = new ArrayList<ITerm>();
		for (ITerm t : argumentsValues) {
			reducedTerms.add(t.reduce(symState));
		}
		return new FunctionTerm(getLocation(), getScope(), getSymbol(),
				discardOnRHS(), reducedTerms);
	}

	@Override
	public ITerm accept(IASLanPPVisitor visitor) {
		return visitor.visit(this);
	}

	@Override
	public void setImplicitExplicitState(ImplicitExplicitState state) {
		getSymbol().setState(state, getLocation());
	}

	@Override
	public boolean isImplicit() {
		return getSymbol().isImplicit();
	}

}
