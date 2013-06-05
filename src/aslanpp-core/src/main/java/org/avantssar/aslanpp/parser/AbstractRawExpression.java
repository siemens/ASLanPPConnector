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

package org.avantssar.aslanpp.parser;

import org.avantssar.aslanpp.model.IScope;
import org.avantssar.commons.ErrorGatherer;
import org.avantssar.commons.LocationInfo;

public abstract class AbstractRawExpression implements IRawExpression {

	private final IScope scope;
	private final LocationInfo location;
	private final ErrorGatherer err;

	protected AbstractRawExpression(IScope scope, LocationInfo location, ErrorGatherer err) {
		this.scope = scope;
		this.location = location;
		this.err = err;
	}

	public IScope getScope() {
		return scope;
	}

	public LocationInfo getLocation() {
		return location;
	}

	public ErrorGatherer getErrorGatherer() {
		return err;
	}

	@Override
	public String getRepresentation() {
		RawPrettyPrinter rpp = new RawPrettyPrinter();
		this.accept(rpp);
		return rpp.toString();
	}

	@Override
	public boolean isComparison() {
		return false;
	}

	@Override
	public boolean isChannelGoal() {
		return false;
	}

	@Override
	public String toString() {
		return getRepresentation();
	}

}
