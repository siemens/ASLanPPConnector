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
import org.avantssar.aslan.IASLanSpec;
import org.avantssar.aslan.RewriteRule;
import org.avantssar.aslanpp.model.ExpressionContext;
import org.avantssar.aslanpp.model.IScope;
import org.avantssar.aslanpp.model.ITerm;
import org.avantssar.aslanpp.model.SymbolsState;
import org.avantssar.aslanpp.model.VariableSymbol;

public class FactToFactEdge extends AbstractEdge {

	@SuppressWarnings("unused")
	private IScope scope;
	private final List<ITerm> left = new ArrayList<ITerm>();
	private final List<ITerm> right = new ArrayList<ITerm>();
	private final List<VariableSymbol> fresh = new ArrayList<VariableSymbol>();
	private final String name;

	private FactToFactEdge(IScope scope, String name, INode node, ITerm left, ITerm right, List<VariableSymbol> fresh, ASLanBuilder builder) {
		super(node, node, builder);
		this.scope = scope;
		this.name = name;
		this.left.add(left);
		this.right.add(right);
		this.fresh.addAll(fresh);
	}

	public FactToFactEdge(IScope scope, String name, ITerm left, ITerm right, List<VariableSymbol> fresh, ASLanBuilder builder) {
		this(scope, name, new Node(), left, right, fresh, builder);
	}

	private FactToFactEdge(IScope scope, String name, INode node, List<ITerm> left, List<ITerm> right, List<VariableSymbol> fresh, ASLanBuilder builder) {
		super(node, node, builder);
		this.scope = scope;
		this.name = name;
		this.left.addAll(left);
		this.right.addAll(right);
		this.fresh.addAll(fresh);
	}

	public FactToFactEdge(IScope scope, String name, List<ITerm> left, List<ITerm> right, List<VariableSymbol> fresh, ASLanBuilder builder) {
		this(scope, name, new Node(), left, right, fresh, builder);
	}

	private FactToFactEdge(FactToFactEdge old, INode sourceNode) {
		super(old, sourceNode);
		name = old.name;
		left.addAll(old.left);
		right.addAll(old.right);
		fresh.addAll(old.fresh);
	}

	@Override
	protected RewriteRule getTransition(IASLanSpec spec, int index, SymbolsState symState) {
		RewriteRule tr = spec.rule(buildName(name, index, 0));
		tr.addCommentLine("retract facts");
		for (ITerm f : left) {
			tr.addCommentLine(f.getRepresentation());
		}
		tr.addCommentLine("introduce facts");
		for (ITerm f : right) {
			tr.addCommentLine(f.getRepresentation());
		}
		for (ITerm f : left) {
			ExpressionContext ctx = new ExpressionContext();
			f.buildContext(ctx, false);
			addLHS(tr, f, ctx, symState, false, false);
		}
		for (ITerm f : right) {
			ExpressionContext ctx = new ExpressionContext();
			f.buildContext(ctx, false);
			addRHS(tr, f, ctx, symState, false, false);
		}
		for (VariableSymbol v : fresh) {
			tr.addExists(spec.findVariable(v.getName()));
		}
		return tr;
	}

	@Override
	protected String getGraphvizPrefix() {
		return "FF";
	}

	@Override
	protected IEdge recreate(INode sourceNode) {
		return new FactToFactEdge(this, sourceNode);
	}

	public boolean hasBreakpoint(List<String> breakpoints) {
		for (ITerm f : left) {
			ExpressionContext ctx = new ExpressionContext(false, breakpoints);
			f.buildContext(ctx, false);
			if (ctx.hasBreakpoint()) {
				return true;
			}
		}
		for (ITerm f : right) {
			ExpressionContext ctx = new ExpressionContext(false, breakpoints);
			f.buildContext(ctx, false);
			if (ctx.hasBreakpoint()) {
				return true;
			}
		}
		return false;
	}

	@Override
	protected boolean contributeToTransition(RewriteRule soFar, SymbolsState symState) {
		// Independent edges cannot be lumped for now.
		// They are used only for ACM and symbolic entity instantiation.
		return false;
	}

	@Override
	protected boolean canBeContributedAfter() {
		return false;
	}

}
