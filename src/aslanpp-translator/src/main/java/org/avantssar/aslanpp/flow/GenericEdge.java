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
import org.avantssar.aslan.ICommentEntry;
import org.avantssar.aslan.MetaInfo;
import org.avantssar.aslan.RewriteRule;
import org.avantssar.aslanpp.IReduceable;
import org.avantssar.aslanpp.model.BaseExpression;
import org.avantssar.aslanpp.model.CommunicationTerm;
import org.avantssar.aslanpp.model.Entity;
import org.avantssar.aslanpp.model.ExpressionContext;
import org.avantssar.aslanpp.model.FunctionSymbol;
import org.avantssar.aslanpp.model.IExpression;
import org.avantssar.aslanpp.model.IScope;
import org.avantssar.aslanpp.model.ISymbol;
import org.avantssar.aslanpp.model.ITerm;
import org.avantssar.aslanpp.model.Prelude;
import org.avantssar.aslanpp.model.SymbolsState;
import org.avantssar.aslanpp.model.VariableSymbol;
import org.avantssar.aslanpp.model.ExpressionContext.NegatedConditionState;
import org.avantssar.commons.LocationInfo;

public abstract class GenericEdge extends AbstractEdge {

	private final Entity ownerEntity;
	private final LocationInfo location;

	public GenericEdge(Entity ownerEntity, INode sourceNode, LocationInfo location, ASLanBuilder builder) {
		super(sourceNode, builder);
		this.ownerEntity = ownerEntity;
		this.location = location;
	}

	protected GenericEdge(GenericEdge old, INode sourceNode) {
		super(old, sourceNode);
		ownerEntity = old.ownerEntity;
		location = old.location;
		// this(old.ownerEntity, sourceNode, null, old.lineNumber);
	}

	public GenericEdge(Entity ownerEntity, INode sourceNode, INode targetNode, LocationInfo location, ASLanBuilder builder) {
		super(sourceNode, targetNode, builder);
		this.ownerEntity = ownerEntity;
		this.location = location;
	}

	public Entity getOwnerEntity() {
		return ownerEntity;
	}

	protected int getLineNumber() {
		return location != null ? location.line : 0;
	}

	protected void setTransitionLHS(RewriteRule tr, SymbolsState symState) {
		StateTerm entState = new StateTerm(getOwnerEntity(), getSourceNode().isBigState());
		entState.setStep(getSourceNode().getStateIndexTerm(getOwnerEntity()));
		ExpressionContext ctx = new ExpressionContext();
		entState.buildContext(ctx, false);
		addLHS(tr, entState.reduce(symState), ctx, symState, false, false);
		// add the !dishonest(Actor) if starting from first node
		// (but not to the root entity)
		if (getSourceNode().getNodeIndex() == 1 && !getOwnerEntity().equals(getOwnerEntity().findRootEntity())) {
			FunctionSymbol fDishonest = getOwnerEntity().findFunction(Prelude.DISHONEST);
			addLHS(tr, fDishonest.term(getOwnerEntity().getActorSymbol().term()).expression().negate(), new ExpressionContext(), symState, false, false);
		}
	}

	protected void setOrUpdateTransitionRHS(RewriteRule rule, SymbolsState symState) {
		StateTerm entState = new StateTerm(getOwnerEntity(), getSourceNode().isBigState());
		entState.setStep(getTargetNode().getStateIndexTerm(getOwnerEntity()));
		changeStateFactRHS(rule, entState.reduce(symState), symState);
		List<MetaInfo> toRemoveMeta = new ArrayList<MetaInfo>();
		for (ICommentEntry ce : rule.getCommentLines()) {
			if (ce instanceof MetaInfo) {
				MetaInfo mi = (MetaInfo) ce;
				if (mi.getName().equals(MetaInfo.STEP_LABEL) && mi.getParameters().get(MetaInfo.ENTITY).equals(entState.getEntity().getOriginalName())) {
					toRemoveMeta.add(mi);
				}
			}
		}
		rule.getCommentLines().removeAll(toRemoveMeta);
		MetaInfo mi = rule.addMetaInfo(MetaInfo.STEP_LABEL);
		mi.addParameter(MetaInfo.ENTITY, entState.getEntity().getOriginalName());
		mi.addParameter(MetaInfo.IID, builder.transform(entState.getIID()).getRepresentation());
		mi.addParameter(MetaInfo.LINE, Integer.toString(getLineNumber()));
		mi.addParameter(MetaInfo.VARIABLE, entState.getEntity().getStepSymbol().getOriginalName());
		mi.addParameter(MetaInfo.TERM, builder.transform(entState.getStep()).getRepresentation());
	}

	protected void changeStateFactRHS(RewriteRule rule, StateTerm entState, SymbolsState symState) {
		List<org.avantssar.aslan.ITerm> toRemove = new ArrayList<org.avantssar.aslan.ITerm>();
		for (org.avantssar.aslan.ITerm aslanTerm : rule.getRHS()) {
			if (aslanTerm instanceof org.avantssar.aslan.FunctionTerm) {
				org.avantssar.aslan.FunctionTerm ft = (org.avantssar.aslan.FunctionTerm) aslanTerm;
				if (ft.getFunction().getName().equals(entState.getEntity().getStateFunction().getName())) {
					toRemove.add(aslanTerm);
				}
			}
		}
		for (org.avantssar.aslan.ITerm t : toRemove) {
			rule.remove(t, false);
		}
		ExpressionContext ctx = new ExpressionContext();
		entState.buildContext(ctx, false);
		addRHS(rule, entState, ctx, symState, false, false);
	}

	protected void blockEntityOnRHS(RewriteRule rule, Entity ent) {

	}

	protected String buildName(int index) {
		return buildName(getOwnerEntity().getName(), index, getLineNumber());
	}

	@Override
	public RewriteRule getTransition(IASLanSpec spec, int index, SymbolsState symState) {
		RewriteRule rule = spec.rule(buildName(index));
		// MetaInfo lineInfo = rule.addMetaInfo(MetaInfo.LINE);
		// lineInfo.addFlag(Integer.toString(getLineNumber()));
		doIt(rule, symState, false);
		return rule;
	}

	protected MetaInfo startMetaInfo(RewriteRule rule, String keyword) {
		MetaInfo info = rule.addMetaInfo(keyword);
		info.addParameter(MetaInfo.ENTITY, getOwnerEntity().getName());
		info.addParameter(MetaInfo.IID, getOwnerEntity().getIDSymbol().getName());
		info.addParameter(MetaInfo.LINE, Integer.toString(getLineNumber()));
		return info;
	}

	@Override
	protected boolean contributeToTransition(RewriteRule soFar, SymbolsState symState) {
		 // symState appears to be a substitution of variable names according to assignments in current rule
		if (willContributeToTransition()) {
			// MetaInfo lineInfo = soFar.addMetaInfo(MetaInfo.LINE);
			// lineInfo.addFlag(Integer.toString(getLineNumber()));
			doIt(soFar, symState, true);
			return true;
		}
		else {
			return false;
		}
	}

	protected abstract void doIt(RewriteRule rule, SymbolsState symState, boolean contribute);

	protected abstract boolean willContributeToTransition();

	@Override
	protected <T> void addAdditional(RewriteRule rule, boolean toLeft, ExpressionContext ctx, SymbolsState symState) {
		super.addAdditional(rule, toLeft, ctx, symState);
		for (IScope scope : ctx.getOwners()) {
			if (scope instanceof Entity) {
				Entity e = (Entity) scope;
				if (!e.equals(ownerEntity)) {
					checkOneSide(rule, toLeft, e);
					checkOneSide(rule, !toLeft, e);
				}
			}
		}
	}

	private void addMatches(ExpressionContext ctx, RewriteRule tr) {
		for (ISymbol sym : ctx.getMatches().keySet()) {
			if (ctx.getState((VariableSymbol) sym) != NegatedConditionState.OnlyNegative) {
				ISymbol dummy = ctx.getMatches().get(sym);
				MetaInfo matchInfo = startMetaInfo(tr, MetaInfo.MATCH);
				matchInfo.addParameter(MetaInfo.VARIABLE, sym.getOriginalName());
				matchInfo.addParameter(MetaInfo.TERM, dummy.getName());
				Entity owner = (Entity) sym.getOwner();
				if (owner != null && !owner.equals(getOwnerEntity())) {
					matchInfo.addParameter(MetaInfo.OWNER, owner.getOriginalName());
					matchInfo.addParameter(MetaInfo.OWNER_IID, owner.getIDSymbol().getOriginalName());
				}
			}
		}
	}

	@SuppressWarnings("rawtypes")
	protected void addMatchesAndAuxiliary(ExpressionContext outerCtx, RewriteRule tr, IReduceable fact, SymbolsState symState) {
		addMatches(outerCtx, tr); // used for matched variable assignment in goals
		ExpressionContext ctx = new ExpressionContext();
		fact.buildContext(ctx, false);
		addMatches(ctx, tr);
		for (String n : ctx.getSetLiteralNames()) {
			org.avantssar.aslan.Variable v = builder.getASLanSpecification().findVariable(n);
			// skip set literals of secrecy goal statements, which are translated as functions
			// TODO better not use addSetLiteralName() for secrecy goal statements
			if (v != null) { 
				tr.addExists(v);
			}
		}
		for (ITerm t : ctx.getAuxiliaryTerms()) {
			MetaInfo addInfo = startMetaInfo(tr, MetaInfo.INTRODUCE);
			addInfo.addParameter(MetaInfo.FACT, builder.transform(solveReduce(t, symState, false)).getRepresentation());
		}
		for (ITerm t : ctx.getSessionGoalTerms()) {
			boolean special = isSpecialSessionGoalTerm(t);
			if (!special) {
				MetaInfo addInfo = startMetaInfo(tr, MetaInfo.INTRODUCE);
				addInfo.addParameter(MetaInfo.FACT, builder.transform(solveReduce(t, symState, false)).getRepresentation());
				ExpressionContext sgtCtx = new ExpressionContext();
				t.buildContext(sgtCtx, false);
				for (ITerm auxT : sgtCtx.getAuxiliaryTerms()) {
					MetaInfo auxInfo = startMetaInfo(tr, MetaInfo.INTRODUCE);
					auxInfo.addParameter(MetaInfo.FACT, builder.transform(solveReduce(auxT, symState, false)).getRepresentation());
				}
			}
		}
	}

	protected void decorateCommunicationMetaInfo(MetaInfo mi, CommunicationTerm ct, SymbolsState symState) {
		symState.push();
		mi.addParameter(MetaInfo.SENDER  , builder.transform(solveReduce(ct.getSender   (), symState, false)).getRepresentation());
		mi.addParameter(MetaInfo.RECEIVER, builder.transform(solveReduce(ct.getReceiver (), symState, false)).getRepresentation());
		mi.addParameter(MetaInfo.PAYLOAD , builder.transform(solveReduce(ct.getPayload  (), symState, false)).getRepresentation());
		mi.addParameter(MetaInfo.CHANNEL ,(ct.getChannel() != null ?
										   builder.transform(solveReduce(ct.getChannel  (), symState, false)).getRepresentation()
										 : ct.getChannelType().nonArrow));
		mi.addParameter(MetaInfo.FACT, builder.transform(solveReduce(ct.getProcessedTerm(), symState, false)).getRepresentation());
		mi.addParameter(MetaInfo.DIRECTION, ct.isReceive() ? MetaInfo.RECEIVE : MetaInfo.SEND);
		symState.pop();
	}

	protected void addGuard(RewriteRule rule, IExpression expr, SymbolsState symState) {
		for (IExpression e : expr.getAtomicExpressions(true)) {
			IExpression toAdd = null;
			if (e instanceof BaseExpression) {
				BaseExpression be = (BaseExpression) e;
				if (be.getBaseTerm() instanceof CommunicationTerm) {
					CommunicationTerm ct = (CommunicationTerm) be.getBaseTerm();
					toAdd = ct.getProcessedTerm().expression();
				}
			}
			if (toAdd == null) {
				toAdd = e;
			}
			ExpressionContext ctx = new ExpressionContext();
			toAdd.buildContext(ctx, false);
			toAdd = solveReduce(toAdd, symState, true);
			addLHS(rule, toAdd, ctx, symState, false, false);

			MetaInfo guardInfo = null;
			if (e instanceof BaseExpression) {
				BaseExpression be = (BaseExpression) e;
				if (be.getBaseTerm() instanceof CommunicationTerm) {
					CommunicationTerm ct = (CommunicationTerm) be.getBaseTerm();
					guardInfo = startMetaInfo(rule, MetaInfo.COMMUNICATION_GUARD);
					decorateCommunicationMetaInfo(guardInfo, ct, symState);
				}
			}
			if (guardInfo == null) {
				guardInfo = startMetaInfo(rule, MetaInfo.GUARD);
				guardInfo.addParameter(MetaInfo.TEST, builder.transform(toAdd).getRepresentation());
			}
			addMatchesAndAuxiliary(ctx, rule, toAdd, symState);

			boolean skipImplicit = false;
			if (toAdd instanceof BaseExpression) {
				BaseExpression be = (BaseExpression) toAdd;
				if (be.getBaseTerm().isImplicit()) {
					skipImplicit = true;
				}
			}
			if (!skipImplicit && toAdd.isPositive() && !toAdd.isCondition() && !toAdd.discardOnRHS()) {
				addRHS(rule, toAdd, ctx, symState, false, false);
			}
		}
	}

	private void checkOneSide(RewriteRule rule, boolean toLeft, Entity e) {
		List<org.avantssar.aslan.ITerm> existing = rule.getTerms(toLeft);
		boolean found = false;
		for (org.avantssar.aslan.ITerm candidate : existing) {
			if (candidate instanceof org.avantssar.aslan.FunctionTerm) {
				org.avantssar.aslan.FunctionTerm ft = (org.avantssar.aslan.FunctionTerm) candidate;
				if (ft.getFunction().getName().startsWith(e.getStateFunction().getName())) {
					found = true;
					break;
				}
			}
		}
		if (!found) {
			org.avantssar.aslan.Function stateFnc = builder.getASLanSpecification().findFunction(e.getStateFunction().getName());
			List<org.avantssar.aslan.VariableTerm> args = new ArrayList<org.avantssar.aslan.VariableTerm>();
			for (VariableSymbol p : e.getStateSymbols()) {
				org.avantssar.aslan.Variable v = builder.getASLanSpecification().findVariable(p.getName());
				args.add(v.term());
			}
			org.avantssar.aslan.FunctionTerm stateTerm = stateFnc.term(args.toArray(new org.avantssar.aslan.ITerm[args.size()]));
			addInternal(rule, toLeft, stateTerm, true, false);

			if (toLeft) {
				for (org.avantssar.aslan.ITerm t : e.childChain(builder.getASLanSpecification(), location, ownerEntity)) {
					addInternal(rule, false, t, true, false);
					addInternal(rule, true , t, true, false);
				}
				
			}
		}
	}
	
}
