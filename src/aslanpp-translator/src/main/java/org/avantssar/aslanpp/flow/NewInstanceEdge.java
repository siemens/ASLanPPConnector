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
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.avantssar.aslan.MetaInfo;
import org.avantssar.aslan.RewriteRule;
import org.avantssar.aslan.Variable;
import org.avantssar.aslanpp.model.ConstantSymbol;
import org.avantssar.aslanpp.model.Entity;
import org.avantssar.aslanpp.model.ExpressionContext;
import org.avantssar.aslanpp.model.ITerm;
import org.avantssar.aslanpp.model.Prelude;
import org.avantssar.aslanpp.model.SymbolsState;
import org.avantssar.aslanpp.model.VariableSymbol;
import org.avantssar.commons.LocationInfo;

public class NewInstanceEdge extends GenericEdge {

	protected Entity newEntity;
	protected List<ITerm> newEntityParameters;
	protected VariableSymbol freshIDSymbol;
	protected Map<VariableSymbol, ConstantSymbol> dummyValues;

	public NewInstanceEdge(Entity ownerEntity, INode sourceNode, Entity newEntity, ITerm[] newEntityParameters, VariableSymbol freshIDSymbol, Map<VariableSymbol, ConstantSymbol> dummyValues,
			LocationInfo location, ASLanBuilder builder) {
		this(ownerEntity, sourceNode, newEntity, Arrays.asList(newEntityParameters), freshIDSymbol, dummyValues, location, builder);
	}

	public NewInstanceEdge(Entity ownerEntity, INode sourceNode, Entity newEntity, List<ITerm> newEntityParameters, VariableSymbol freshIDSymbol, Map<VariableSymbol, ConstantSymbol> dummyValues,
			LocationInfo location, ASLanBuilder builder) {
		super(ownerEntity, sourceNode, location, builder);
		this.newEntity = newEntity;
		this.newEntityParameters = newEntityParameters;
		this.freshIDSymbol = freshIDSymbol;
		this.dummyValues = dummyValues;
	}

	public NewInstanceEdge(Entity ownerEntity, INode sourceNode, INode targetNode, Entity newEntity, List<ITerm> newEntityParameters, VariableSymbol freshIDSymbol,
			Map<VariableSymbol, ConstantSymbol> dummyValues, LocationInfo location, ASLanBuilder builder) {
		super(ownerEntity, sourceNode, targetNode, location, builder);
		this.newEntity = newEntity;
		this.newEntityParameters = newEntityParameters;
		this.freshIDSymbol = freshIDSymbol;
		this.dummyValues = dummyValues;
	}

	protected NewInstanceEdge(NewInstanceEdge old, INode sourceNode) {
		super(old, sourceNode);
		newEntity = old.newEntity;
		newEntityParameters = old.newEntityParameters;
		freshIDSymbol = old.freshIDSymbol;
		dummyValues = old.dummyValues;
	}

	@Override
	protected String describe() {
		return newEntity.getName();
	}

	@Override
	protected String getGraphvizPrefix() {
		return "N";
	}

	@Override
	protected IEdge recreate(INode sourceNode) {
		return new NewInstanceEdge(this, sourceNode);
	}

	// @Override
	// public RewriteRule getTransition(IASLanSpec spec, int index, SymbolsState
	// symState) {
	// RewriteRule tr = spec.rule(buildName(index));
	// tr.addCommentLine("line " + getLineNumber());
	// doIt(tr, symState, false);
	// return tr;
	// }

	@Override
	protected void doIt(RewriteRule soFar, SymbolsState symState, boolean contribute) {
		// soFar.addCommentLine("new instance");
		decorateBasicNewInstance(soFar, symState, freshIDSymbol, contribute);
	}

	protected void decorateBasicNewInstance(RewriteRule tr, SymbolsState symState, VariableSymbol freshIID, boolean skipLHS) {
		if (!skipLHS) {
			setTransitionLHS(tr, symState);
		}

		StateTerm newEntState = new StateTerm(newEntity, true);
		// TODO: fix this nicely
		Node nn = new Node();
		nn.assignIndexes(new Counters(1, 1));
		newEntState.setStep(nn.getStateIndexTerm(newEntity));
		newEntState.setID(freshIID.freshTerm());
		List<ITerm> redPars = new ArrayList<ITerm>();
		for (ITerm p : newEntityParameters) {
			redPars.add(solveReduce(p, symState, false));
		}
		newEntState.setParameters(redPars);
		newEntState.setRestToDummy(dummyValues);

		ExpressionContext ctx = new ExpressionContext();
		newEntState.buildContext(ctx, false);
		addRHS(tr, newEntState, ctx, symState, false, false);
		ExpressionContext EmptyCtx = new ExpressionContext();
		addMatchesAndAuxiliary(EmptyCtx, tr, newEntState, symState);
		blockEntityOnRHS(tr, newEntity);

		MetaInfo newinstInfo = startMetaInfo(tr, MetaInfo.NEW_INSTANCE);
		newEntState.fillMetaInfo(newinstInfo, builder);
		for (ITerm p : newEntityParameters) {
			addMatchesAndAuxiliary(EmptyCtx, tr, p, symState);
		}

		ITerm child = getOwnerEntity().findFunction(Prelude.CHILD).term(getOwnerEntity().getIDSymbol().term(), freshIID.term());
		addRHS(tr, child, new ExpressionContext(), symState, false, false);
		Variable vFresh = builder.getASLanSpecification().findVariable(freshIID.getName());
		tr.addExists(vFresh);

		setOrUpdateTransitionRHS(tr, symState);
	}

	public boolean hasBreakpoint(List<String> breakpoints) {
		// new instance always leads to big-step state
		// TODO: it used to be true here, based on Zebs short paper on small
		// steps semantics
		return false;
	}

	// @Override
	// protected boolean contributeToTransition(RewriteRule soFar, SymbolsState
	// symState) {
	// // New instance edges cannot be lumped (for now). Actually the steps
	// // before new instance edges should be big-steps, so a new instance edge
	// // should not even be requested to lump itself.
	// soFar.addCommentLine("lumped line " + getLineNumber() +
	// " (skipped step label " + getSourceNode().getNodeIndex() + ")");
	// doIt(soFar, symState, true);
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
