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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.avantssar.aslan.IASLanSpec;
import org.avantssar.aslan.RewriteRule;
import org.avantssar.aslanpp.model.Entity;
import org.avantssar.aslanpp.model.ITerm;
import org.avantssar.aslanpp.model.SymbolsState;

public class Node implements INode {

	private final IEdge incomingEdge;
	private final List<IEdge> outgoingEdges;
	private boolean isBigState;

	private boolean visited = false;
	private int nodeIndex;
	private INode optimizedNode;

	private boolean bigSmallStateComputed;

	private boolean mustStartNewTransitionAfterOptimization;

	// For the first node in the flow graph.
	public Node() {
		this(null);
	}

	public Node(IEdge incomingEdge) {
		this.incomingEdge = incomingEdge;
		outgoingEdges = new ArrayList<IEdge>();
	}

	public IEdge getIncomingEdge() {
		return incomingEdge;
	}

	public List<IEdge> getOutgoingEdges() {
		return outgoingEdges;
	}

	public void addOutgoingEdge(IEdge edge) {
		outgoingEdges.add(edge);
	}

	public Counters assignIndexes(Counters counters) {
		visited = true;
		nodeIndex = counters.nodes;
		Counters next = counters;
		for (IEdge edge : outgoingEdges) {
			next = edge.assignIndexes(next);
		}
		return next;
	}

	public void renderGraphviz(PrintStream out) {
		visited = true;
		toGraphviz(out);
		for (IEdge edge : outgoingEdges) {
			edge.renderGraphviz(out);
		}
	}

	public int gatherTransitions(IASLanSpec spec, int nextTransitionIndex) {
		visited = true;
		int idx = nextTransitionIndex;
		for (IEdge edge : outgoingEdges) {
			idx = edge.gatherTransitions(spec, idx);
		}
		return idx;
	}

	public int gatherTransitionsLumped(IASLanSpec spec, RewriteRule soFar, SymbolsState symState, Set<Integer> visitedStates, int nextTransitionIndex, int sourceNodeIndex, TransitionsRecorder rec) {
		visited = true;
		int idx = nextTransitionIndex;
		for (IEdge edge : outgoingEdges) {
			Set<Integer> copy = new HashSet<Integer>(visitedStates);
			copy.add(new Integer(getNodeIndex()));
			symState.push();
			idx = edge.gatherTransitionsLumped(spec, soFar, symState, copy, idx, sourceNodeIndex, rec);
			symState.pop();
		}
		return idx;
	}

	public boolean wasVisited() {
		return visited;
	}

	public void clearVisited() {
		visited = false;
		for (IEdge edge : outgoingEdges) {
			edge.clearVisited();
		}
	}

	public int getNodeIndex() {
		return nodeIndex;
	}

	public boolean wasOptimized() {
		return optimizedNode != null;
	}

	public INode optimize(IEdge incomingEdge) {
		if (optimizedNode == null) {
			//optimized = true;
			boolean skipped = false;
			int count = 0;
			GenericEdge first = null;
			for (IEdge e : outgoingEdges) {
				GenericEdge edge = (GenericEdge) e;
				if (!edge.canNeverBeTaken()) {
					if (count == 0) {
						first = edge;
					}
					count++;
				}
			}
			// We skip nodes with only one output edge which is always true.
			if (count == 1) {
				if (first.canBeDropped()) {
					INode next = first.getTargetNode();
					if(!(next.getOutgoingEdges().size() == 1 && 
						 next.getOutgoingEdges().get(0) instanceof EmptyEdge && 
						 next.getOutgoingEdges().get(0).getTargetNode()==this))
					   // avoids infinite recursion on while(true) {}
					{
						optimizedNode = next.optimize(incomingEdge);
						if (first.mustStartNewTransitionOnLumping()) {
							optimizedNode.markNewTransitionStartOnLumpingAfterOptimizations();
						}
						skipped = true;
					}
				}
			}

			if (!skipped) {
				if (incomingEdge == null) {
					optimizedNode = new Node();
				}
				else {
					optimizedNode = new Node(incomingEdge);
				}
				((Node) optimizedNode).isBigState = isBigState;
				for (IEdge edge : outgoingEdges) {
					if (!((GenericEdge) edge).canNeverBeTaken()) {
						edge.optimize(optimizedNode);
					}
				}
			}
		}
		return optimizedNode;
	}

	public boolean wasBigSmallStateComputed() {
		return bigSmallStateComputed;
	}

	public void computeBigSmallState(List<String> breakpoints) {
		bigSmallStateComputed = true;

		boolean needsBigState = false;
		for (IEdge edge : outgoingEdges) {
			if (edge.hasBreakpoint(breakpoints)) {
				needsBigState = true;
				break;
			}
		}
		// Once turned into big-state, it cannot become small-state anymore.
		isBigState |= needsBigState;
		// End points are big-state.
		isBigState |= outgoingEdges.size() == 0;

		for (IEdge edge : outgoingEdges) {
			if (!edge.getTargetNode().wasBigSmallStateComputed()) {
				edge.getTargetNode().computeBigSmallState(breakpoints);
			}
		}
	}

	public boolean isBigState() {
		return isBigState;
	}

	public void setBigState() {
		isBigState = true;
	}

	public ITerm getStateIndexTerm(Entity ent) {
		return new NodeTerm(ent, this);
	}

	public INode getLastNode(Set<Integer> visited) {
		if (visited.contains(getNodeIndex())) {
			return null;
		}
		visited.add(getNodeIndex());

		if (outgoingEdges.size() == 0) {
			return this;
		}
		else if (outgoingEdges.size() > 1) {
			return null;
		}
		else {
			return outgoingEdges.get(0).getTargetNode().getLastNode(visited);
		}
	}

	private void toGraphviz(PrintStream out) {
		out.print(getNodeIndex());
		out.print(" [style = ");
		if (isBigState) {
			out.print("solid");
		}
		else {
			out.print("dashed");
		}
		out.println("] ;");
	}

	@Override
	public String toString() {
		return Integer.toString(getNodeIndex());
	}

	@Override
	public boolean mustStartNewTransitionOnLumping() {
		if (mustStartNewTransitionAfterOptimization) {
			return true;
		}
		if (outgoingEdges.size() > 1) {
			return true;
		}
		else if (outgoingEdges.size() == 1) {
			IEdge e = outgoingEdges.get(0);
			if (e.mustStartNewTransitionOnLumping()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void markNewTransitionStartOnLumpingAfterOptimizations() {
		mustStartNewTransitionAfterOptimization = true;
	}
}
