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

import java.util.List;

import org.avantssar.aslanpp.visitors.IASLanPPVisitor;
import org.avantssar.commons.LocationInfo;

public class AssertStatement extends AbstractScopedStatement {

	private IExpression guard;
	private FunctionSymbol checkFunction;
	private List<ITerm> firstTerms;

	protected AssertStatement(LocationInfo location, IScope owner, String name) {
		super(location, owner, name);
	}

	public List<ITerm> getFirstTerms() {
		return firstTerms;
	}

	public void setFirstTerms(List<ITerm> firstTerms) {
		this.firstTerms = firstTerms;
	}

	public FunctionSymbol getCheckFunction() {
		return checkFunction;
	}

	public void setCheckFunction(FunctionSymbol checkFunction) {
		this.checkFunction = checkFunction;
	}

	public IExpression getGuard() {
		return guard;
	}

	public void setGuard(IExpression guard) {
		this.guard = guard;
	}

	@Override
	public void accept(IASLanPPVisitor visitor) {
		visitor.visit(this);
	}

}
