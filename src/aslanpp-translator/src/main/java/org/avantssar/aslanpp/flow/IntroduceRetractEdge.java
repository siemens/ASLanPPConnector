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
import org.avantssar.aslan.MetaInfo;
import org.avantssar.aslan.RewriteRule;
import org.avantssar.aslanpp.model.CommunicationTerm;
import org.avantssar.aslanpp.model.Entity;
import org.avantssar.aslanpp.model.ExpressionContext;
import org.avantssar.aslanpp.model.ITerm;
import org.avantssar.aslanpp.model.SymbolsState;
import org.avantssar.commons.LocationInfo;

public class IntroduceRetractEdge extends GenericEdge {

	private final List<ITerm> introducedFacts = new ArrayList<ITerm>();
	private final List<ITerm> retractedFacts = new ArrayList<ITerm>();
	private boolean standalone = false;

	public IntroduceRetractEdge(Entity ownerEntity, INode sourceNode, ITerm fact, boolean introduce, LocationInfo location, ASLanBuilder builder) {
		super(ownerEntity, sourceNode, location, builder);
		(introduce ? introducedFacts : retractedFacts).add(fact);
	}

	public IntroduceRetractEdge(Entity ownerEntity, INode sourceNode, List<ITerm> facts, boolean introduce, LocationInfo location, ASLanBuilder builder) {
		super(ownerEntity, sourceNode, location, builder);
		(introduce ? introducedFacts : retractedFacts).addAll(facts);
	}

	public IntroduceRetractEdge(Entity ownerEntity, INode sourceNode, INode targetNode, ITerm fact, boolean introduce, LocationInfo location, ASLanBuilder builder) {
		super(ownerEntity, sourceNode, targetNode, location, builder);
		(introduce ? introducedFacts : retractedFacts).add(fact);
	}

	public IntroduceRetractEdge(Entity ownerEntity, INode sourceNode, INode targetNode, List<ITerm> facts, boolean introduce, LocationInfo location, ASLanBuilder builder) {
		super(ownerEntity, sourceNode, targetNode, location, builder);
		(introduce ? introducedFacts : retractedFacts).addAll(facts);
	}

	protected IntroduceRetractEdge(IntroduceRetractEdge old, INode sourceNode) {
		super(old, sourceNode);
		introducedFacts.addAll(old.introducedFacts);
		retractedFacts.addAll(old.retractedFacts);
		standalone = old.standalone;
	}

	public void makeStandalone() {
		standalone = true;
	}

	@Override
	protected String describe() {
		StringBuffer sb = new StringBuffer();
		if (introducedFacts.size() > 0) {
			sb.append("introduce");
			for (ITerm fact : introducedFacts) {
				sb.append(" ");
				sb.append(fact.getRepresentation());
			}
		}
		if (retractedFacts.size() > 0) {
			sb.append("retract");
			for (ITerm fact : retractedFacts) {
				sb.append(" ");
				sb.append(fact.getRepresentation());
			}
		}
		return sb.toString();
	}

	@Override
	protected String getGraphvizPrefix() {
		return "IR";
	}

	@Override
	protected String describeForGraphviz() {
		StringBuffer sb = new StringBuffer();
		for (ITerm fact : introducedFacts) {
			sb.append(" ");
			sb.append(fact.getRepresentation());
		}
		for (ITerm fact : retractedFacts) {
			sb.append(" ");
			sb.append(fact.getRepresentation());
		}
		return sb.toString();
	}

	@Override
	protected IEdge recreate(INode sourceNode) {
		return new IntroduceRetractEdge(this, sourceNode);
	}

	public void addFact(ITerm fact, boolean introduce) {
		(introduce ? introducedFacts : retractedFacts).add(fact);
	}

	public void addFacts(List<ITerm> facts, boolean introduce) {
		(introduce ? introducedFacts : retractedFacts).addAll(facts);
	}

	protected boolean skipMetaInfo() {
		return false;
	}

	private void doFact(ITerm fact, boolean retract, RewriteRule tr, SymbolsState symState) {
		ITerm red;
		if (fact instanceof CommunicationTerm) {
			CommunicationTerm ct = (CommunicationTerm) fact;
			red = solveReduce(ct.getProcessedTerm(), symState, true);
		}
		else {
			red = solveReduce(fact, symState, true);
		}
		ExpressionContext ctx = new ExpressionContext();
		fact.buildContext(ctx, false);
		if (retract)
			retract(tr, red, ctx, symState);
		else
			introduce(tr, red, ctx, symState);
		if (!skipMetaInfo()) {
			MetaInfo info = null;
			if (fact instanceof CommunicationTerm) {
				CommunicationTerm ct = (CommunicationTerm) fact;
				info = startMetaInfo(tr, MetaInfo.COMMUNICATION);
				decorateCommunicationMetaInfo(info, ct, symState);
			}
			if (info == null) {
				info = startMetaInfo(tr, retract ? MetaInfo.RETRACT : MetaInfo.INTRODUCE);
				info.addParameter(MetaInfo.FACT, builder.transform(red).getRepresentation());
			}
			ExpressionContext EmptyCtx = new ExpressionContext();
			addMatchesAndAuxiliary(EmptyCtx, tr, fact, symState);
		}
	}
	@Override
	protected void doIt(RewriteRule tr, SymbolsState symState, boolean contribute) {
		if (!contribute) {
			setTransitionLHS(tr, symState);
		}
		for (ITerm fact : retractedFacts) {
			doFact(fact, true, tr, symState);
		}
		for (ITerm fact : introducedFacts) {
			doFact(fact, false, tr, symState);
		}
		setOrUpdateTransitionRHS(tr, symState);
	}

	@Override
	protected boolean willContributeToTransition() {
		return !standalone;
	}

	public boolean hasBreakpoint(List<String> breakpoints) {
		// this is used for 'asserts' and for receives
		if (standalone) {
			return true;
		}
/* commented out according to bug 61: breakpoint facts should not break when being retracted
		for (ITerm f : retractedFacts) {
			ExpressionContext ctx = new ExpressionContext(false, breakpoints);
			f.buildContext(ctx, false);
			if (ctx.hasBreakpoint()) {
				return true;
			}
		}
		*/
		for (ITerm f : introducedFacts) {
			ExpressionContext ctx = new ExpressionContext(false, breakpoints);
			f.buildContext(ctx, false);
			if (ctx.hasBreakpoint()) {
				return true;
			}
		}
		return false;
	}

	@Override
	protected boolean canBeContributedAfter() {
		return true;
	}
}
