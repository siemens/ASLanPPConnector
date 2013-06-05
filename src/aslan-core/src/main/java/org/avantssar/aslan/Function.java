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
import org.avantssar.commons.ErrorGatherer;
import org.avantssar.commons.LocationInfo;

public class Function extends ImplicitExplicitSymbol {

	private final List<IType> argTypes;

	boolean prelude;

	private FunctionConstantTerm constTerm;

	protected Function(LocationInfo location, ErrorGatherer err, String name, IType type, boolean prelude, IType... argTypes) {
		this(location, err, name, type, prelude, Arrays.asList(argTypes));
	}

	protected Function(LocationInfo location, ErrorGatherer err, String name, IType type, boolean prelude, List<IType> argTypes) {
		super(location, err, name, type, new FunctionNameValidator());
		this.argTypes = argTypes;
		this.prelude = prelude;
	}

	public List<IType> getArgumentsTypes() {
		return argTypes;
	}

	public FunctionTerm term(ITerm... args) {
		return term(null, args);
	}

	public FunctionTerm term(LocationInfo location, ITerm... args) {
		return new FunctionTerm(location, getErrorGatherer(), this, args);
	}

	public FunctionConstantTerm constantTerm() {
		if (constTerm == null) {
			constTerm = new FunctionConstantTerm(null, getErrorGatherer(), this);
		}
		return constTerm;
	}

	public FunctionConstantTerm constantTerm(LocationInfo location) {
		return new FunctionConstantTerm(location, getErrorGatherer(), this);
	}

	public boolean isPrelude() {
		return prelude;
	}

	public void setPrelude(boolean prelude) {
		this.prelude = prelude;
	}

	public boolean matchesSignature(IType returnType, IType... argTypes) {
		if (!getType().equals(returnType)) {
			return false;
		}
		if (this.argTypes.size() != argTypes.length) {
			return false;
		}
		for (int i = 0; i < this.argTypes.size(); i++) {
			if (!this.argTypes.get(i).equals(argTypes[i])) {
				return false;
			}
		}
		return true;
	}

	public void accept(IASLanVisitor visitor) {
		visitor.visit(this);
	}
}
