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

import java.util.Arrays;
import java.util.List;

import org.avantssar.aslanpp.visitors.IASLanPPVisitor;
import org.avantssar.commons.LocationInfo;

public class MacroTerm extends AbstractTerm {

	private final MacroSymbol macro;
	private final List<ITerm> arguments;

	protected MacroTerm(LocationInfo location, IScope scope, MacroSymbol macro,
			ITerm... arguments) {
		super(location, scope, false);
		if (arguments.length != macro.getArguments().size()) {
			macro.getErrorGatherer().addException(location,
					ErrorMessages.WRONG_NUMBER_OF_PARAMETERS, "Macro",
					macro.getOriginalName(), macro.getArguments().size(),
					arguments.length);
		}
		this.macro = macro;
		this.arguments = Arrays.asList(arguments);
	}

	public MacroSymbol getMacro() {
		return macro;
	}

	public List<ITerm> getArguments() {
		return arguments;
	}

	@Override
	public IType inferType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ITerm reduce(SymbolsState symState) {
		// we should never get here
		return this;
	}

	@Override
	public ITerm accept(IASLanPPVisitor visitor) {
		return visitor.visit(this);
	}
}
