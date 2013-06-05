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

import org.avantssar.aslanpp.visitors.IASLanPPVisitor;
import org.avantssar.commons.LocationInfo;

public class Equation extends GenericScope {

	private ITerm leftTerm;
	private ITerm rightTerm;
	private final LocationInfo location;

	public Equation(IScope owner, LocationInfo location) {
		super(owner, owner.getFreshNamesGenerator().getFreshName("eq",
				Equation.class));
		this.location = location;
	}

	public ITerm getLeftTerm() {
		return leftTerm;
	}

	public void setLeftTerm(ITerm t) {
		leftTerm = t;
	}

	public ITerm getRightTerm() {
		return rightTerm;
	}

	public void setRightTerm(ITerm t) {
		rightTerm = t;
	}

	public LocationInfo getLocation() {
		return location;
	}

	public void accept(IASLanPPVisitor visitor) {
		visitor.visit(this);
	}
}
