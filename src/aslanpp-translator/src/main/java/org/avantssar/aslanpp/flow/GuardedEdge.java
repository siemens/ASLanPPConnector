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

import java.util.ArrayList;
import java.util.List;
import org.avantssar.aslan.RewriteRule;
import org.avantssar.aslanpp.model.Entity;
import org.avantssar.aslanpp.model.ExpressionContext;
import org.avantssar.aslanpp.model.IExpression;
import org.avantssar.aslanpp.model.ITerm;
import org.avantssar.aslanpp.model.SymbolsState;
import org.avantssar.commons.LocationInfo;

public class GuardedEdge extends GenericEdge {

	private final IExpression guard;
	public List<ITerm> introducedFacts = new ArrayList<ITerm>();

	public GuardedEdge(Entity ownerEntity, INode sourceNode, IExpression guard, LocationInfo location, ASLanBuilder builder) {
		super(ownerEntity, sourceNode, location, builder);
		this.guard = guard;
	}

	public GuardedEdge(Entity ownerEntity, INode sourceNode, INode targetNode, IExpression guard, LocationInfo location, ASLanBuilder builder) {
		super(ownerEntity, sourceNode, targetNode, location, builder);
		this.guard = guard;
	}

	private GuardedEdge(GuardedEdge old, INode sourceNode) {
		super(old, sourceNode);
		guard = old.guard;
		introducedFacts.addAll(old.introducedFacts);
	}

	@Override
	public boolean canBeDropped() {
		return (guard == null || guard.isAlwaysTrue()) && (introducedFacts.size() == 0);
	}

	@Override
	public boolean canNeverBeTaken() {
		return guard != null && guard.isAlwaysFalse();
	}

	public void addFact(ITerm fact) {
		introducedFacts.add(fact);
	}

	public void addFacts(List<ITerm> facts) {
		introducedFacts.addAll(facts);
	}

	@Override
	protected String describe() {
		StringBuffer sb = new StringBuffer();
		if (guard != null) {
			sb.append(guard.getRepresentation());
		}
		else {
			sb.append("no guard");
		}
		if (introducedFacts.size() > 0) {
			sb.append(" introduce");
			for (ITerm fact : introducedFacts) {
				sb.append(" ");
				sb.append(fact.getRepresentation());
			}
		}
		return sb.toString();
	}

	@Override
	protected String getGraphvizPrefix() {
		return "G";
	}

	@Override
	protected IEdge recreate(INode sourceNode) {
		return new GuardedEdge(this, sourceNode);
	}

	@Override
	protected void doIt(RewriteRule rule, SymbolsState symState, boolean contribute) {
		setTransitionLHS(rule, symState);
		addGuard(rule, guard, symState);
		for (ITerm fact : introducedFacts) {
			ExpressionContext ctx = new ExpressionContext();
			fact.buildContext(ctx, false);
			addRHS(rule, solveReduce(fact, symState, false), ctx, symState, false, false);
		}
		setOrUpdateTransitionRHS(rule, symState);
	}

	public boolean hasBreakpoint(List<String> breakpoints) {
		for (ITerm f : introducedFacts) {
			ExpressionContext ctx = new ExpressionContext(false, breakpoints);
			f.buildContext(ctx, false);
			if (ctx.hasBreakpoint()) {
				return true;
			}
		}
		ExpressionContext ctx = new ExpressionContext(false, breakpoints);
		guard.buildContext(ctx, false);
		return ctx.hasBreakpoint();
	}

	@Override
	protected boolean canBeContributedAfter() {
		return true;
	}

	@Override
	protected boolean willContributeToTransition() {
		return false;
	}

	@Override
	public boolean mustStartNewTransitionOnLumping() {
		return true;
	}

}
