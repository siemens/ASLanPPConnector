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

import org.avantssar.commons.ErrorGatherer;
import org.avantssar.commons.LocationInfo;

public abstract class ImplicitExplicitSymbol extends AbstractSymbol {

	public static enum ImplicitExplicitState {
		Implicit, Explicit, Unknown
	}

	private ImplicitExplicitState state = ImplicitExplicitState.Unknown;

	protected ImplicitExplicitSymbol(LocationInfo location, ErrorGatherer err, String name, IType type, INameValidator validator) {
		super(location, err, name, type, validator);
	}

	public ImplicitExplicitState getState() {
		return state;
	}

	public boolean isImplicit() {
		return state == ImplicitExplicitState.Implicit;
	}

	public void setState(ImplicitExplicitState newState, LocationInfo location) {
		// iknows is an exception
		if (!IASLanSpec.IKNOWS.getName().equals(getName())) {
			if (newState == ImplicitExplicitState.Unknown) {
				getErrorGatherer().addException(location, ASLanErrorMessages.INVALID_EXPL_IMPL_STATE);
			}
			if (state != newState && state != ImplicitExplicitState.Unknown) {
				getErrorGatherer().addError(location, ASLanErrorMessages.IMPL_EXPL_STATE_CONFLICT, getName(), newState.toString(), state.toString());
			}
			else {
				state = newState;
			}
		}
	}

}
