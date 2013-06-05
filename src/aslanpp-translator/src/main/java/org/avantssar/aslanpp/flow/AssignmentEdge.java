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
import org.avantssar.aslan.MetaInfo;
import org.avantssar.aslan.RewriteRule;
import org.avantssar.aslan.Variable;
import org.avantssar.aslanpp.model.Entity;
import org.avantssar.aslanpp.model.ExpressionContext;
import org.avantssar.aslanpp.model.ITerm;
import org.avantssar.aslanpp.model.SymbolsState;
import org.avantssar.aslanpp.model.VariableTerm;
import org.avantssar.commons.LocationInfo;

public class AssignmentEdge extends GenericEdge {

	private final VariableTerm assignedSymbol;
	private final ITerm assignedValue;
	private final boolean isFresh;

	public AssignmentEdge(Entity ownerEntity, INode sourceNode, VariableTerm assignedSymbol, ITerm assignedValue, boolean isFresh, LocationInfo location, ASLanBuilder builder) {
		super(ownerEntity, sourceNode, location, builder);
		this.assignedSymbol = assignedSymbol;
		this.assignedValue = assignedValue;
		this.isFresh = isFresh;
	}

	private AssignmentEdge(AssignmentEdge old, INode sourceNode) {
		super(old, sourceNode);
		assignedSymbol = old.assignedSymbol;
		assignedValue = old.assignedValue;
		isFresh = old.isFresh;
	}

	@Override
	protected String describe() {
		StringBuffer sb = new StringBuffer();
		sb.append(assignedSymbol.getSymbol().getName());
		sb.append(" := ");
		sb.append(assignedValue.getRepresentation());
		return sb.toString();
	}

	@Override
	protected String getGraphvizPrefix() {
		return isFresh ? "F" : "A";
	}

	@Override
	protected IEdge recreate(INode sourceNode) {
		return new AssignmentEdge(this, sourceNode);
	}

	@Override
	protected void doIt(RewriteRule rule, SymbolsState symState, boolean contribute) {
		if (!contribute) {
			setTransitionLHS(rule, symState);
		}

		if (isFresh) {
			if (assignedValue instanceof VariableTerm) {
				VariableTerm at = (VariableTerm) assignedValue;
				Variable v = builder.getASLanSpecification().findVariable(at.getSymbol().getName());
				rule.addExists(v);
			}
		}

		ExpressionContext ctx = new ExpressionContext();
		assignedValue.buildContext(ctx, false);
		assignedValue.useContext(ctx, symState);

		MetaInfo assignInfo = startMetaInfo(rule, isFresh ? MetaInfo.FRESH : MetaInfo.ASSIGNMENT);
		assignInfo.addParameter(MetaInfo.VARIABLE, assignedSymbol.getSymbol().getOriginalName());
		assignInfo.addParameter(MetaInfo.TERM, builder.transform(solveReduce(assignedValue, symState, false)).getRepresentation());

		symState.assign(assignedSymbol.getSymbol(), assignedValue);

		ExpressionContext assCtx = new ExpressionContext();
		assignedSymbol.buildContext(assCtx, false);
		addAdditional(rule, false, assCtx, symState);
		ExpressionContext EmptyCtx = new ExpressionContext();
		addMatchesAndAuxiliary(EmptyCtx, rule, assignedSymbol, symState);

		addMatchesAndAuxiliary(EmptyCtx, rule, assignedValue, symState);

		Entity owner = (Entity) assignedSymbol.getSymbol().getOwner();
		if (owner != null && !owner.equals(getOwnerEntity())) {
			StateTerm ownerState = new StateTerm(owner, true);
			StateTerm reducedOwner = (StateTerm) solveReduce(ownerState, symState, false);
			changeStateFactRHS(rule, reducedOwner, symState);
			// ITerm descendant = generateDescendantFact(getOwnerEntity(),
			// owner.getIDSymbol(), getOwnerEntity().getIDSymbol());
			// ExpressionContext descCtx = new ExpressionContext();
			// descendant.buildContext(descCtx, false);
			// addLHS(rule, descendant, descCtx, symState, false, false);
			assignInfo.addParameter(MetaInfo.OWNER, owner.getOriginalName());
			assignInfo.addParameter(MetaInfo.OWNER_IID, owner.getIDSymbol().getOriginalName());
		}

		setOrUpdateTransitionRHS(rule, symState);
	}

	public boolean hasBreakpoint(List<String> breakpoints) {
		// If the assigned symbol belongs to another entity, then it is a
		// big-state.
		// return
		// !assignedSymbol.getOwner().getName().equals(getOwnerEntity().getName());
		// TODO: it used to be different here, based on Zebs short paper on
		// small
		// steps semantics
		return false;
	}

	@Override
	protected boolean canBeContributedAfter() {
		return true;
	}

	@Override
	protected boolean willContributeToTransition() {
		return true;
	}

}
