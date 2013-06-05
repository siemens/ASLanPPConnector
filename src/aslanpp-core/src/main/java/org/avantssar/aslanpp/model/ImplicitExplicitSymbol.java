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

import org.avantssar.commons.LocationInfo;

public abstract class ImplicitExplicitSymbol extends AbstractSymbol {

	public static enum ImplicitExplicitState {
		implicit, explicit, unknown
	}

	private ImplicitExplicitState state = ImplicitExplicitState.unknown;
	private LocationInfo previousLocation = null;

	protected ImplicitExplicitSymbol(IScope owner, LocationInfo location,
			String name, IType type) {
		super(owner, location, name, type);
	}

	public ImplicitExplicitState getState() {
		return state;
	}

	public boolean isImplicit() {
		return state == ImplicitExplicitState.implicit;
	}

	public void setState(ImplicitExplicitState newState, LocationInfo location) {
		// iknows is an exception
		if (!Prelude.IKNOWS.equals(getOriginalName())) {
			if (newState == ImplicitExplicitState.unknown) {
				throw new IllegalArgumentException(
						"Cannot set the implicit/explicit state to unknown.");
			}
			if (state != newState && state != ImplicitExplicitState.unknown) {
				if (previousLocation != null) {
					getOwner()
							.getErrorGatherer()
							.addError(
									location,
									ErrorMessages.IMPLICIT_EXPLICIT_CONFLICT_KNOWN_LOCATION,
									getOriginalName(), newState.toString(),
									state.toString(), previousLocation);
				} else {
					getOwner()
							.getErrorGatherer()
							.addException(
									location,
									ErrorMessages.IMPLICIT_EXPLICIT_CONFLICT_UNKNOWN_LOCATION,
									getOriginalName(), newState.toString(),
									state.toString());
				}
			} else {
				state = newState;
				if (previousLocation == null && location != null) {
					previousLocation = location;
				}
			}
		}
	}

}
