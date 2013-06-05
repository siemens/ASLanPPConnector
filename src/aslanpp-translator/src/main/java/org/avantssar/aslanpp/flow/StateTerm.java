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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.avantssar.aslan.MetaInfo;
import org.avantssar.aslanpp.model.AbstractSymbolTerm;
import org.avantssar.aslanpp.model.ConstantSymbol;
import org.avantssar.aslanpp.model.Entity;
import org.avantssar.aslanpp.model.ExpressionContext;
import org.avantssar.aslanpp.model.FunctionSymbol;
import org.avantssar.aslanpp.model.ISymbol;
import org.avantssar.aslanpp.model.ITerm;
import org.avantssar.aslanpp.model.SymbolsState;
import org.avantssar.aslanpp.model.VariableSymbol;
import org.avantssar.aslanpp.visitors.IASLanPPVisitor;
import org.avantssar.commons.Term;

public class StateTerm extends AbstractSymbolTerm<FunctionSymbol> {

	private final boolean isBigStep;
	private final Entity ent;

	private final Map<ISymbol, ITerm> values = new HashMap<ISymbol, ITerm>();

	private final boolean useBangOperator;

	public StateTerm(Entity ent, boolean isBigStep) {
		super(null, ent, ent.getStateFunction(), false);
		this.ent = ent;
		this.isBigStep = isBigStep;
		// TODO: make this correctly
		this.useBangOperator = false;
	}

	public Entity getEntity() {
		return ent;
	}

	public void setActor(ITerm term) {
		setValue(ent.getActorSymbol(), term);
	}

	public void setStep(ITerm term) {
		setValue(ent.getStepSymbol(), term);
	}

	public ITerm getStep() {
		if (values.containsKey(ent.getStepSymbol())) {
			return values.get(ent.getStepSymbol());
		}
		else {
			return ent.getStepSymbol().term();
		}
	}

	public void setID(ITerm term) {
		setValue(ent.getIDSymbol(), term);
	}

	public ITerm getIID() {
		if (values.containsKey(ent.getIDSymbol())) {
			return values.get(ent.getIDSymbol());
		}
		else {
			return ent.getIDSymbol().term();
		}
	}

	public void setValue(ISymbol symbol, ITerm value) {
		if (ent.getStateSymbols().contains(symbol)) {
			values.put(symbol, value);
		}
	}

	public void setParameters(List<ITerm> parValues) {
		if (ent.getParameters().size() != parValues.size()) {
			throw new RuntimeException("This state fact expects " + ent.getParameters().size() + " parameters and receives " + parValues.size());
		}
		for (int i = 0; i < ent.getParameters().size(); i++) {
			ISymbol param = ent.getParameters().get(i);
			ITerm val = parValues.get(i);
			setValue(param, val);
		}
	}

	public void setRestToDummy(Map<VariableSymbol, ConstantSymbol> dummyValues) {
		for (VariableSymbol sym : ent.getStateSymbols()) {
			if (!ent.getParameters().contains(sym)) {
				if (!values.containsKey(sym)) {
					if (dummyValues != null && dummyValues.containsKey(sym)) {
						setValue(sym, dummyValues.get(sym).term());
					}
					else {
						setValue(sym, ent.getDummyConstant(sym.getType()).term());
					}
				}
			}
		}
	}

	public List<ITerm> getParameters() {
		List<ITerm> pars = new ArrayList<ITerm>();
		for (VariableSymbol v : ent.getStateSymbols()) {
			if (values.containsKey(v)) {
				pars.add(values.get(v));
			}
			else {
				pars.add(v.term());
			}
		}
		return pars;
	}

	public void fillMetaInfo(MetaInfo mi, ASLanBuilder builder) {
		mi.addParameter(MetaInfo.NEW_ENTITY, ent.getOriginalName());
		for (VariableSymbol v : ent.getStateSymbols()) {
			if (values.containsKey(v)) {
				mi.addParameter(v.getOriginalName(), builder.transform(values.get(v)).getRepresentation());
			}
			else {
				mi.addFlag(v.getOriginalName());
			}
		}
	}

	@Override
	public String getRepresentation() {
		StringBuffer sb = new StringBuffer();
		sb.append(Term.STATE_PREFIX);
		if (!isBigStep && useBangOperator) {
			sb.append("!");
		}
		sb.append("_").append(getEntity().getName()).append("(");
		boolean first = true;
		for (ISymbol sym : ent.getStateSymbols()) {
			if (!first) {
				sb.append(", ");
			}
			if (values.containsKey(sym)) {
				ITerm val = values.get(sym);
				sb.append(val.getRepresentation());
			}
			else {
				sb.append(sym.getName());
			}
			first = false;
		}
		sb.append(")");
		return sb.toString();
	}

	@Override
	public StateTerm reduce(SymbolsState symState) {
		StateTerm sf = new StateTerm(ent, isBigStep);
		for (ISymbol sym : ent.getStateSymbols()) {
			if (values.containsKey(sym)) {
				ITerm term = values.get(sym);
				sf.setValue(sym, term.reduce(symState));
			}
			else {
				if (symState.isAssigned(sym)) {
					sf.setValue(sym, symState.getAssignedValue(sym));
				}
			}
		}
		return sf;
	}

	@Override
	public ITerm accept(IASLanPPVisitor visitor) {
		if (visitor instanceof ASLanBuilder) {
			return ((ASLanBuilder) visitor).visit(this);
		}
		else {
			return this;
		}
	}

	@Override
	public void useContext(ExpressionContext ctx, SymbolsState symState) {
		for (ISymbol sym : ent.getStateSymbols()) {
			if (values.containsKey(sym)) {
				values.get(sym).useContext(ctx, symState);
			}
		}
	}

	@Override
	public void buildContext(ExpressionContext ctx, boolean isInNegatedCondition) {
		super.buildContext(ctx, isInNegatedCondition);
		for (ISymbol sym : ent.getStateSymbols()) {
			if (values.containsKey(sym)) {
				values.get(sym).buildContext(ctx, isInNegatedCondition);
			}
		}
	}

}
