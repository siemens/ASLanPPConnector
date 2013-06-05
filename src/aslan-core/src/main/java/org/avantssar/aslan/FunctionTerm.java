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

import java.util.Arrays;
import java.util.List;
import org.avantssar.aslan.ImplicitExplicitSymbol.ImplicitExplicitState;
import org.avantssar.commons.ErrorGatherer;
import org.avantssar.commons.LocationInfo;

public class FunctionTerm extends AbstractTerm<Function> {

	private final List<ITerm> args;

	protected FunctionTerm(LocationInfo location, ErrorGatherer err, Function fnc, ITerm[] args) {
		this(location, err, fnc, Arrays.asList(args));
	}

	protected FunctionTerm(LocationInfo location, ErrorGatherer err, Function fnc, List<ITerm> args) {
		super(location, err, fnc);
		if (fnc.getArgumentsTypes().size() != args.size()) {
			getErrorGatherer().addException(location, ASLanErrorMessages.WRONG_NUMBER_OF_PARAMETERS, "Function", fnc.getName(), fnc.getArgumentsTypes().size(), args.size());
		}
		this.args = args;
	}

	public Function getFunction() {
		return getSymbol();
	}

	public List<ITerm> getParameters() {
		return args;
	}

	@Override
	public void buildContext(TermContext ctx, boolean isInNegatedTerm) {
		for (ITerm t : args) {
			t.buildContext(ctx, isInNegatedTerm);
		}
	}

	@Override
	public boolean isCondition() {
		return getSymbol().equals(IASLanSpec.EQUAL);
	}

	public void accept(IASLanVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	public void setImplicitExplicitState(ImplicitExplicitState state) {
		getSymbol().setState(state, getLocation());
	}

}
