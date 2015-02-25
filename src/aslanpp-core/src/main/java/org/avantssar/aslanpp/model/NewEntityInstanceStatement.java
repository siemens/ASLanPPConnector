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

public class NewEntityInstanceStatement extends AbstractStatement {

	private final Entity ent;
	private final List<ITerm> args = new ArrayList<ITerm>();
	private VariableSymbol newIDSymbol;
	private final Map<VariableSymbol, ConstantSymbol> dummyValues = new HashMap<VariableSymbol, ConstantSymbol>();

	protected NewEntityInstanceStatement(LocationInfo location, Entity ent, ITerm... args) {
		super(location);
		this.ent = ent;
		setParameters(args);
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

	public List<ITerm> getParameters() {
		return args;
	}

	public void setParameters(ITerm... args) {
		if (ent.getParametersCount() != args.length) {
			ent.getErrorGatherer().addException(getLocation(), ErrorMessages.WRONG_NUMBER_OF_PARAMETERS, "Entity", ent.getOriginalName(), ent.getParametersCount(), args.length);
		}
		this.args.clear();
		for (ITerm a : args) {
			this.args.add(a);
		}
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
