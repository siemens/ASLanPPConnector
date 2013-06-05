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

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.avantssar.aslan.RewriteRule;
import org.avantssar.aslanpp.model.ConstantSymbol;
import org.avantssar.aslanpp.model.Entity;
import org.avantssar.aslanpp.model.ErrorMessages;
import org.avantssar.aslanpp.model.ExpressionContext;
import org.avantssar.aslanpp.model.FunctionTerm;
import org.avantssar.aslanpp.model.IExpression;
import org.avantssar.aslanpp.model.ISymbol;
import org.avantssar.aslanpp.model.ITerm;
import org.avantssar.aslanpp.model.Prelude;
import org.avantssar.aslanpp.model.SymbolsState;
import org.avantssar.aslanpp.model.VariableSymbol;
import org.avantssar.aslanpp.model.VariableTerm;
import org.avantssar.commons.LocationInfo;

public class SymbolicInstanceEdge extends NewInstanceEdge {

	private final List<VariableSymbol> symbols;
	private final IExpression guard;

	public SymbolicInstanceEdge(Entity ownerEntity, INode sourceNode, Entity newEntity, ITerm[] newEntityParameters, VariableSymbol[] symbols, IExpression guard, VariableSymbol freshIDSymbol,
			Map<VariableSymbol, ConstantSymbol> dummyValues, LocationInfo location, ASLanBuilder builder) {
		this(ownerEntity, sourceNode, newEntity, Arrays.asList(newEntityParameters), Arrays.asList(symbols), guard, freshIDSymbol, dummyValues, location, builder);
	}

	public SymbolicInstanceEdge(Entity ownerEntity, INode sourceNode, Entity newEntity, List<ITerm> newEntityParameters, List<VariableSymbol> symbols, IExpression guard, VariableSymbol freshIDSymbol,
			Map<VariableSymbol, ConstantSymbol> dummyValues, LocationInfo location, ASLanBuilder builder) {
		super(ownerEntity, sourceNode, newEntity, newEntityParameters, freshIDSymbol, dummyValues, location, builder);
		this.symbols = symbols;
		this.guard = guard;
	}

	public SymbolicInstanceEdge(Entity ownerEntity, INode sourceNode, INode targetNode, Entity newEntity, List<ITerm> newEntityParameters, List<VariableSymbol> symbols, IExpression guard,
			VariableSymbol freshIDSymbol, Map<VariableSymbol, ConstantSymbol> dummyValues, LocationInfo location, ASLanBuilder builder) {
		super(ownerEntity, sourceNode, targetNode, newEntity, newEntityParameters, freshIDSymbol, dummyValues, location, builder);
		this.symbols = symbols;
		this.guard = guard;
	}

	private SymbolicInstanceEdge(SymbolicInstanceEdge old, INode sourceNode) {
		super(old, sourceNode);
		symbols = old.symbols;
		guard = old.guard;
	}

	@Override
	protected String getGraphvizPrefix() {
		return "S";
	}

	@Override
	protected IEdge recreate(INode sourceNode) {
		return new SymbolicInstanceEdge(this, sourceNode);
	}

	@Override
	protected void doIt(RewriteRule tr, SymbolsState symState, boolean contribute) {
		if (guard != null) {
			// MetaInfo guardInfo = startMetaInfo(tr, MetaInfo.GUARD);
			// IExpression red = solveReduce(guard, symState, true);
			addGuard(tr, guard, symState);
			// guardInfo.addParameter(MetaInfo.TEST,
			// builder.transform(red).getRepresentation());
			// addMatchesAndAuxiliary(tr, guard, symState);
		}

		decorateBasicNewInstance(tr, symState, freshIDSymbol, contribute);

		for (ISymbol sym : symbols) {
			VariableTerm vterm = ((VariableSymbol) sym).term();
			FunctionTerm fterm = getOwnerEntity().findFunction(Prelude.IKNOWS).term(vterm);
			ExpressionContext symCtx = new ExpressionContext();
			fterm.buildContext(symCtx, false);
			addLHS(tr, fterm, symCtx, symState, true, false);
			addRHS(tr, fterm, symCtx, symState, true, false);
		}
	}

	@Override
	protected boolean contributeToTransition(RewriteRule soFar, SymbolsState symState) {
		// TODO tentatively solved bug 178 - it is not always safe to lump with "where" clause
	    if (guard != null && !guard.isAlwaysTrue() && soFar.getLHS().size()+soFar.getRHS().size() > 2/* && options.getOptimizationLevel() == OptimizationLevel.LUMP*/) {
            getOwnerEntity().getErrorGatherer().addWarning(guard.getLocation(), ErrorMessages.LUMPING_ASSUMING_SATISFIABLE_GUARD, 
						guard.getOriginalRepresentation());
		}
		super.contributeToTransition(soFar, symState);
		return true;
	}

}
