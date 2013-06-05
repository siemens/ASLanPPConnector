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

public class FunctionSymbol extends ImplicitExplicitSymbol {

	private final IType[] argumentsTypes;
	private boolean nonPublic;
	private boolean nonInvertible;
	private final List<VariableSymbol> hcSymbols = new ArrayList<VariableSymbol>();
	private boolean internalUse = false;

	protected FunctionSymbol(IScope owner, LocationInfo location, String name,
			IType type, IType... argumentsTypes) {
		super(owner, location, name, type);
		this.argumentsTypes = argumentsTypes;
	}

	public List<IType> getArgumentsTypes() {
		return Arrays.asList(argumentsTypes);
	}

	public boolean isNonPublic() {
		return nonPublic;
	}

	public void setNonPublic(boolean nonPublic) {
		this.nonPublic = nonPublic;
	}

	public boolean isNonInvertible() {
		return nonInvertible;
	}

	public void setNonInvertible(boolean nonInvertible) {
		this.nonInvertible = nonInvertible;
	}

	public boolean isInternalUse() {
		return internalUse;
	}

	public void setInternalUse(boolean internalUse) {
		this.internalUse = internalUse;
	}

	public List<VariableSymbol> getHCSymbols() {
		return hcSymbols;
	}

	public void setHCSymbols(List<VariableSymbol> hcSymbols) {
		this.hcSymbols.addAll(hcSymbols);
	}

	public boolean respectsSignature(IType returnType, List<IType> argTypes) {
		if (!getType().equals(returnType)) {
			return false;
		}
		if (argumentsTypes.length != argTypes.size()) {
			return false;
		}
		for (int i = 0; i < argumentsTypes.length; i++) {
			IType myArg = argumentsTypes[i];
			IType outArg = argTypes.get(i);
			if (!myArg.equals(outArg)) {
				return false;
			}
		}
		return true;
	}

	public FunctionTerm oopTerm(ITerm... args) {
		return oopTerm(null, args);
	}

	public FunctionTerm oopTerm(LocationInfo location, ITerm... args) {
		FunctionTerm term = term(location, getOwner()/* TODO is is really correct to re-use the declaration owner of the symbol? */, args);
		term.setOOPStyle(true);
		return term;
	}

	public FunctionTerm term(ITerm... args) {
		return term(null, getOwner()/* TODO is is really correct to re-use the declaration owner of the symbol? */, args);
	}

	public FunctionTerm term(LocationInfo location, IScope owner, ITerm... args) {
		if (argumentsTypes.length != args.length) {
			getOwner().getErrorGatherer().addException(location,
					ErrorMessages.WRONG_NUMBER_OF_PARAMETERS, "Function",
					getOriginalName(), argumentsTypes.length, args.length);
		}
		return new FunctionTerm(location, owner, this, false, args);
	}

	public boolean isSetFunction() {
		return getName().equals(Prelude.ADD)
				|| getName().equals(Prelude.REMOVE)
				|| getName().equals(Prelude.CONTAINS);
	}

	public void accept(IASLanPPVisitor visitor) {
		visitor.visit(this);
	}

}
