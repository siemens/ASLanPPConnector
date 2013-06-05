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

import java.util.List;
import org.avantssar.aslan.RewriteRule;
import org.avantssar.aslanpp.model.Entity;
import org.avantssar.aslanpp.model.SymbolsState;
import org.avantssar.commons.LocationInfo;

public class EmptyEdge extends GenericEdge {

	public EmptyEdge(Entity ownerEntity, INode sourceNode, LocationInfo location, ASLanBuilder builder) {
		super(ownerEntity, sourceNode, location, builder);
	}

	public EmptyEdge(Entity ownerEntity, INode sourceNode, INode targetNode, LocationInfo location, ASLanBuilder builder) {
		super(ownerEntity, sourceNode, targetNode, location, builder);
	}

	private EmptyEdge(EmptyEdge old, INode sourceNode) {
		super(old, sourceNode);
	}

	@Override
	protected String getGraphvizColor() {
		return "red";
	}

	@Override
	protected String getGraphvizPrefix() {
		return "E";
	}

	@Override
	public boolean canBeDropped() {
		return true;
	}

	@Override
	protected IEdge recreate(INode sourceNode) {
		return new EmptyEdge(this, sourceNode);
	}

	// @Override
	// public RewriteRule getTransition(IASLanSpec spec, int index, SymbolsState
	// symState) {
	// RewriteRule rule = spec.rule(buildName(index));
	// rule.addCommentLine("line " + getLineNumber());
	// return rule;
	// }

	@Override
	protected void doIt(RewriteRule rule, SymbolsState symState, boolean contribute) {
		rule.addCommentLine("empty transition");
		setTransitionLHS(rule, symState);
		setOrUpdateTransitionRHS(rule, symState);
	}

	public boolean hasBreakpoint(List<String> breakpoints) {
		return false;
	}

	// @Override
	// protected boolean contributeToTransition(RewriteRule soFar, SymbolsState
	// symState) {
	// // Empty edges just disappear. Although the NOPS optimization should
	// // remove all empty edges.
	// return true;
	// }

	@Override
	protected boolean canBeContributedAfter() {
		return true;
	}

	@Override
	protected boolean willContributeToTransition() {
		return true;
	}

}
