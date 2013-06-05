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

import org.avantssar.aslanpp.model.ImplicitExplicitSymbol.ImplicitExplicitState;
import org.avantssar.aslanpp.visitors.IASLanPPVisitor;
import org.avantssar.commons.LocationInfo;

public class ConstantTerm extends AbstractSymbolTerm<ConstantSymbol> {

	protected ConstantTerm(LocationInfo location, IScope scope, ConstantSymbol symbol) {
		super(location, scope, symbol, false);
	}

	@Override
	public void buildContext(ExpressionContext ctx, boolean isNegated) {
		super.buildContext(ctx, isNegated);
		if (ctx.isBreakpoint(getSymbol().getOriginalName())) {
			ctx.setBreakpoint();
		}
	}

	public ITerm reduce(SymbolsState symState) {
		return this;
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