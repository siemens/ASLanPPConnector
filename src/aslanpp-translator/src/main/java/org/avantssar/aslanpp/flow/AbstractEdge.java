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

import java.io.PrintStream;
import java.util.List;
import java.util.Set;
import org.avantssar.aslan.IASLanSpec;
import org.avantssar.aslan.RewriteRule;
import org.avantssar.aslanpp.Debug;
import org.avantssar.aslanpp.IReduceable;
import org.avantssar.aslanpp.model.ExpressionContext;
import org.avantssar.aslanpp.model.FunctionTerm;
import org.avantssar.aslanpp.model.IExpression;
import org.avantssar.aslanpp.model.ITerm;
import org.avantssar.aslanpp.model.NegationExpression;
import org.avantssar.aslanpp.model.Prelude;
import org.avantssar.aslanpp.model.SymbolsState;

public abstract class AbstractEdge implements IEdge {

	private static final String STEP_PREFIX = "step";
	private static int GRAPHVIZ_LABEL_LENGTH = 15;

	private final INode sourceNode;
	private INode targetNode;
	private int index;
	protected final ASLanBuilder builder;

	protected AbstractEdge(INode sourceNode, INode targetNode, ASLanBuilder builder) {
		this.sourceNode = sourceNode;
		sourceNode.addOutgoingEdge(this);
		this.targetNode = targetNode;
		this.builder = builder;
	}

	protected AbstractEdge(INode sourceNode, ASLanBuilder builder) {
		this.sourceNode = sourceNode;
		sourceNode.addOutgoingEdge(this);
		targetNode = new Node(this);
		this.builder = builder;
	}

	protected AbstractEdge(AbstractEdge old, INode sourceNode) {
		this(sourceNode, null, old.builder);
	}

	public INode getSourceNode() {
		return sourceNode;
	}

	public INode getTargetNode() {
		return targetNode;
	}

	public int getIndex() {
		return index;
	}

	public IEdge optimize(INode sourceNode) {
		INode farthestNode = getFarthestNode();
		IEdge newEdge = recreate(sourceNode);
		((AbstractEdge) newEdge).targetNode = farthestNode.optimize(newEdge);
		return newEdge;
	}

	protected abstract IEdge recreate(INode sourceNode);

	private INode getFarthestNode() {
		INode finalNode = getTargetNode();
		boolean over = false;
		while (!over) {
			if (finalNode.wasOptimized()) {
				// it's a loop, we need to stop
				over = true;
			}
			else {
				if (finalNode.getOutgoingEdges().size() != 1) {
					// A branch comes down the line, or the chain ends. either
					// way we stop.
					over = true;
				}
				else {
					GenericEdge edge = (GenericEdge) finalNode.getOutgoingEdges().get(0);
					if (edge.canBeDropped()) {
						// Move on to the next (single) edge.
						finalNode = edge.getTargetNode();
					}
					else {
						// The edge cannot be dropped. We stop.
						over = true;
					}
				}
			}
		}
		return finalNode;
	}

	public boolean canBeDropped() {
		return false;
	}

	public boolean canNeverBeTaken() {
		return false;
	}

	public Counters assignIndexes(Counters counters) {
		index = counters.edges;
		int nextNodeIndex = targetNode.wasVisited() ? targetNode.getNodeIndex() : counters.nodes + 1;
		if (!targetNode.wasVisited()) {
			return targetNode.assignIndexes(new Counters(nextNodeIndex, counters.edges + 1));
		}
		else {
			return new Counters(counters.nodes, counters.edges + 1);
		}
	}

	public void renderGraphviz(PrintStream out) {
		toGraphviz(out);
		if (!targetNode.wasVisited()) {
			targetNode.renderGraphviz(out);
		}
	}

	public int gatherTransitions(IASLanSpec spec, int nextTransitionIndex) {
		int idx = nextTransitionIndex;
		getTransition(spec, idx++, new SymbolsState());
		if (!targetNode.wasVisited()) {
			idx = targetNode.gatherTransitions(spec, idx);
		}
		return idx;
	}

	public int gatherTransitionsLumped(IASLanSpec spec, RewriteRule soFar, SymbolsState symState, Set<Integer> visitedStates, int nextTransitionIndex, int sourceNodeIndex, TransitionsRecorder rec) {

		Debug.logger.debug("Starting getTransitionLumped");
		Debug.logger.debug("\tedge=" + this.describeForGraphviz());
		Debug.logger.debug("\tnext idx=" + nextTransitionIndex);
		Debug.logger.debug("\tvisited states=" + visitedStates.size() + " " + visitedStates);
		Debug.logger.debug("\tso far=" + (soFar != null ? soFar.getRepresentation() : "N/A"));

		int idx = nextTransitionIndex;
		if (soFar == null) {
			Debug.logger.debug("\tnothing so far");
			soFar = getTransition(spec, idx++, symState);
		}
		else {
			Debug.logger.debug("\tsomething so far");
			boolean couldContribute = contributeToTransition(soFar, symState);
			if (!couldContribute) {
				Debug.logger.debug("\tcould not contribute further, restarting");
				symState.clear();
				soFar = getTransition(spec, idx++, symState);
			}
			else {
				Debug.logger.debug("\tcontributed further\n");
			}
		}

		boolean continued = false;
		int origIdx = idx;
		if (targetNode.isBigState() || targetNode.mustStartNewTransitionOnLumping()) {
			symState.clear();
			if (!targetNode.wasVisited()) {
				idx = targetNode.gatherTransitionsLumped(spec, null, symState, visitedStates, idx, targetNode.getNodeIndex(), rec);
			}
		}
		else {
			if (!visitedStates.contains(new Integer(targetNode.getNodeIndex()))) {
				if (!canBeContributedAfter()) {
					symState.clear();
					idx = targetNode.gatherTransitionsLumped(spec, null, symState, visitedStates, idx, targetNode.getNodeIndex(), rec);
				}
				else {
					continued = true;
					idx = targetNode.gatherTransitionsLumped(spec, soFar, symState, visitedStates, idx, sourceNodeIndex, rec);
				}
			}
			else {
				symState.clear();
			}
		}

		if (!continued) {
			rec.record(sourceNodeIndex, targetNode.getNodeIndex(), origIdx - 1);
		}

		return idx;
	}

	public boolean mustStartNewTransitionOnLumping() {
		return false;
	}

	protected abstract RewriteRule getTransition(IASLanSpec spec, int index, SymbolsState symState);

	protected abstract boolean contributeToTransition(RewriteRule soFar, SymbolsState symState);

	protected abstract boolean canBeContributedAfter();

	public void clearVisited() {
		if (targetNode.wasVisited()) {
			targetNode.clearVisited();
		}
	}

	protected void toGraphviz(PrintStream out) {
		StringBuffer sb = new StringBuffer();
		sb.append(sourceNode.getNodeIndex());
		sb.append(" -> ");
		sb.append(targetNode.getNodeIndex());
		sb.append(" [");
		sb.append("label=\"");
		sb.append("step_");
		sb.append(index);
		sb.append("\\n");
		sb.append(getGraphvizPrefix());
		// sb.append(", line ");
		// sb.append(lineNumber);
		String desc = describeForGraphviz();
		if (desc.length() > 0) {
			sb.append("\\n");
			if (desc.length() > GRAPHVIZ_LABEL_LENGTH) {
				desc = desc.substring(0, GRAPHVIZ_LABEL_LENGTH) + "...";
			}
			sb.append(desc);
		}
		sb.append("\"");
		sb.append(", ");
		sb.append("color=");
		sb.append(getGraphvizColor());
		sb.append("] ;");
		out.println(sb.toString());
	}

	protected String getGraphvizColor() {
		return "black";
	}

	protected abstract String getGraphvizPrefix();

	protected String describe() {
		return getClass().getSimpleName();
	}

	protected String describeForGraphviz() {
		return describe();
	}

	protected String buildName(String name, int index, int lineNumber) {
		StringBuffer sb = new StringBuffer();
		sb.append(STEP_PREFIX);
		sb.append("_");
		sb.append(String.format("%03d", index));
		sb.append("_");
		sb.append(name);
		if (lineNumber > 0/* && TranslatorOptions.rulesNoLineNumber()*/) {
			sb.append("__line_");
			sb.append(lineNumber);
		}
		return sb.toString();
	}

	protected void retract(RewriteRule rule, ITerm term, ExpressionContext ctx, SymbolsState symState) {
		addLHS(rule, term, ctx, symState, false, true);
	}

	protected void introduce(RewriteRule rule, ITerm term, ExpressionContext ctx, SymbolsState symState) {
		addRHS(rule, term, ctx, symState, false, true);
	}

	protected void addLHS(RewriteRule rule, ITerm term, ExpressionContext ctx, SymbolsState symState, boolean avoidDuplicates, boolean checkOtherSide) {
		addEx(rule, true, term, ctx, symState, avoidDuplicates, checkOtherSide);
	}

	protected void addLHS(RewriteRule rule, IExpression expr, ExpressionContext ctx, SymbolsState symState, boolean avoidDuplicates, boolean checkOtherSide) {
		addEx(rule, true, expr, ctx, symState, avoidDuplicates, checkOtherSide);
	}

	protected void addRHS(RewriteRule rule, ITerm term, ExpressionContext ctx, SymbolsState symState, boolean avoidDuplicates, boolean checkOtherSide) {
		addEx(rule, false, term, ctx, symState, avoidDuplicates, checkOtherSide);
	}

	protected void addRHS(RewriteRule rule, IExpression expr, ExpressionContext ctx, SymbolsState symState, boolean avoidDuplicates, boolean checkOtherSide) {
		addEx(rule, false, expr, ctx, symState, avoidDuplicates, checkOtherSide);
	}

	private void addEx(RewriteRule rule, boolean toLeft, IExpression expr, ExpressionContext ctx, SymbolsState symState, boolean avoidDuplicates, boolean checkOtherSide) {
		org.avantssar.aslan.ITerm aslanTerm = null;
		if (expr instanceof NegationExpression) {
			IExpression baseExpr = ((NegationExpression) expr).getBaseExpression();
			aslanTerm = builder.transform(baseExpr).negate();
		}
		else {
			aslanTerm = builder.transform(expr);
		}
		addInternal(rule, toLeft, aslanTerm, avoidDuplicates, checkOtherSide);
		addAdditional(rule, toLeft, ctx, symState);
	}

	private void addEx(RewriteRule rule, boolean toLeft, ITerm term, ExpressionContext ctx, SymbolsState symState, boolean avoidDuplicates, boolean checkOtherSide) {
		addInternal(rule, toLeft, builder.transform(term), avoidDuplicates, checkOtherSide);
		addAdditional(rule, toLeft, ctx, symState);
	}

	// TODO rename Reduce to Substitute
	protected <T> T solveReduce(IReduceable<T> aa, SymbolsState symState, boolean forceReplacementOfMatches) {
		T result = null;
		ExpressionContext ctx;
		if (forceReplacementOfMatches) {
			ctx = new ExpressionContext(true);
			aa.buildContext(ctx, false);
			symState.push();
			aa.useContext(ctx, symState);
			result = aa.reduce(symState);
			symState.pop();
		}
		ctx = new ExpressionContext();
		aa.buildContext(ctx, false);
		aa.useContext(ctx, symState);
		if (!forceReplacementOfMatches) {
			result = aa.reduce(symState);
		}
		return result;
	}

	private static String[] noAdditional = new String[] { Prelude.CHILD };

	protected boolean isSpecialSessionGoalTerm(ITerm term) {
		boolean special = false;
		if (term instanceof FunctionTerm) {
			FunctionTerm fterm = (FunctionTerm) term;
			String fname = fterm.getSymbol().getOriginalName();
			boolean found = false;
			for (String s : noAdditional) {
				if (s.equals(fname)) {
					found = true;
					break;
				}
			}
			if (found) {
				special = true;
			}
		}
		return special;
	}

	protected <T> void addAdditional(RewriteRule rule, boolean toLeft, ExpressionContext ctx, SymbolsState symState) {
		List<ITerm> auxiliaryTerms = ctx.getAuxiliaryTerms();
		if (auxiliaryTerms != null) {
			for (ITerm term : auxiliaryTerms) {
				ITerm red = solveReduce(term, symState, false);
				addInternal(rule, toLeft, builder.transform(red), true, false);
			}
		}
		List<ITerm> sessionGoalTerms = ctx.getSessionGoalTerms();
		if (sessionGoalTerms != null) {
			for (ITerm term : sessionGoalTerms) {
				// bloody special case: for certain function symbols 
				// we don't add additional stuff and we avoid duplicates
				boolean special = isSpecialSessionGoalTerm(term);
				// session goal terms are always rendered on RHS
				ITerm red = solveReduce(term, symState, true);
				org.avantssar.aslan.ITerm aslanRed = builder.transform(red);
				addInternal(rule, false, aslanRed, true, false);
				if (special) {
					addInternal(rule, true, aslanRed, true, false);
				}
				// session goal terms may have additional terms (e.g. secrecy terms)
				ExpressionContext sgtCtx = new ExpressionContext();
				term.buildContext(sgtCtx, false);
				if (sgtCtx.getAuxiliaryTerms() != null) {
					for (ITerm auxTerm : sgtCtx.getAuxiliaryTerms()) {
						ITerm auxRed = solveReduce(auxTerm, symState, false);
						addInternal(rule, false, builder.transform(auxRed), true, false);
					}
				}
			}
		}
	}

	protected void addInternal(RewriteRule rule, boolean toLeft, org.avantssar.aslan.ITerm term, boolean avoidDuplicates, boolean checkOtherSide) {
		if (checkOtherSide && rule.contains(term, !toLeft)) {
			rule.remove(term, !toLeft);
		}
		else if (!avoidDuplicates || !rule.contains(term, toLeft)) {
			rule.add(term, toLeft);
		}
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(sourceNode.getNodeIndex());
		sb.append(" -> ");
		sb.append(targetNode.getNodeIndex());
		sb.append("::");
		sb.append(index);
		sb.append("::");
		sb.append(getGraphvizPrefix());
		return sb.toString();
	}
}
