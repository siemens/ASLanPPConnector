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

package org.avantssar.aslanpp.flow;

import org.avantssar.aslanpp.model.AbstractTerm;
import org.avantssar.aslanpp.model.Entity;
import org.avantssar.aslanpp.model.ITerm;
import org.avantssar.aslanpp.model.IType;
import org.avantssar.aslanpp.model.Prelude;
import org.avantssar.aslanpp.model.SymbolsState;
import org.avantssar.aslanpp.visitors.IASLanPPVisitor;

public class NodeTerm extends AbstractTerm {

	private final INode node;
	private final Entity ent;

	public NodeTerm(Entity ent, INode node) {
		super(null, ent, false);
		this.ent = ent;
		this.node = node;
	}

	public void enforceType(IType type, boolean dontCheckTypes) {}

	public IType inferType() {
		return ent.findType(Prelude.NAT);
	}

	public INode getNode() {
		return node;
	}

	@Override
	public String getRepresentation() {
		return Integer.toString(node.getNodeIndex());
	}

	public ITerm reduce(SymbolsState symState) {
		// node terms don't get reduced
		return this;
	}

	@Override
	public ITerm accept(IASLanPPVisitor visitor) {
		if (visitor instanceof ASLanBuilder) {
			return ((ASLanBuilder) visitor).visit(this);
		}
		else {
			return this;
		}
	}
}
