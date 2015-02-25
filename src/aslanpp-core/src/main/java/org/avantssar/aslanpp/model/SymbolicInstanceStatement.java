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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.avantssar.aslanpp.visitors.IASLanPPVisitor;
import org.avantssar.commons.LocationInfo;

public class SymbolicInstanceStatement extends AbstractScopedStatement {

	private final Entity ent;
	private final List<ITerm> args = new ArrayList<ITerm>();
	private IExpression guard;
	private final List<VariableSymbol> anys = new ArrayList<VariableSymbol>();
	private VariableSymbol newIDSymbol;
	private final Map<VariableSymbol, ConstantSymbol> dummyValues = new HashMap<VariableSymbol, ConstantSymbol>();

	public SymbolicInstanceStatement(LocationInfo location, Entity owner, Entity ent) {
		super(location, owner, owner.getFreshNamesGenerator().getFreshName("symbolic_instance_" + ent.getName(), SymbolicInstanceStatement.class));
		this.ent = ent;
	}

	public VariableSymbol getNewIDSymbol() {
		return newIDSymbol;
	}

	public void setNewIDSymbol(VariableSymbol newIDSymbol) {
		this.newIDSymbol = newIDSymbol;
	}

	public Entity getEntity() {
		return ent;
	}

	public List<VariableSymbol> getUniversallyQuantified() {
		return anys;
	}

	public VariableSymbol any(String name) {
		VariableSymbol v = addUntypedVariable(name);
		anys.add(v);
		return v;
	}

	public List<ITerm> getParameters() {
		return args;
	}

	public void setArgs(ITerm... args) {
		if (ent.getParametersCount() != args.length) {
			getOwner().getErrorGatherer().addException(getLocation(), ErrorMessages.WRONG_NUMBER_OF_PARAMETERS, "Entity", ent.getOriginalName(), ent.getParametersCount(), args.length);
		}
		this.args.clear();
		for (ITerm a : args) {
			this.args.add(a);
		}
	}

	public IExpression getGuard() {
		return guard;
	}

	public void setGuard(IExpression guard) {
		this.guard = guard;
	}

	public void buildDummyValues() {
		ent.buildDummyValues(dummyValues);
	}

	public Map<VariableSymbol, ConstantSymbol> getDummyValues() {
		return dummyValues;
	}

	@Override
	public void accept(IASLanPPVisitor visitor) {
		visitor.visit(this);
	}
}
