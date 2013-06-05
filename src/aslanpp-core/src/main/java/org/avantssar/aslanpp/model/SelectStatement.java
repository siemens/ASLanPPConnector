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

import java.util.LinkedHashMap;
import java.util.Map;

import org.avantssar.aslanpp.visitors.IASLanPPVisitor;
import org.avantssar.commons.LocationInfo;

public class SelectStatement extends AbstractStatement {

	private final Map<IExpression, IStatement> choices = new LinkedHashMap<IExpression, IStatement>();

	protected SelectStatement(LocationInfo location) {
		super(location);
	}

	public <T extends IStatement> T choice(IExpression guard, T stmt) {
		choices.put(guard, stmt);
		return stmt;
	}

	public Map<IExpression, IStatement> getChoices() {
		return choices;
	}

	@Override
	public void accept(IASLanPPVisitor visitor) {
		visitor.visit(this);
	}
}
