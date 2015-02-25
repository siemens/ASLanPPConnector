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

package org.avantssar.aslanpp.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import org.avantssar.aslanpp.model.CommunicationTerm.Statistics;

public class ExpressionContext {

	public enum NegatedConditionState {
		Unknown, OnlyPositive, OnlyNegative, Both
	};

	private final Map<VariableSymbol, NegatedConditionState> varState = new TreeMap<VariableSymbol, NegatedConditionState>();
	private final Map<VariableSymbol, IScope> varOwner = new TreeMap<VariableSymbol, IScope>();
	private boolean forceReplacementOfMatches;
	private final SortedSet<IScope> owners = new TreeSet<IScope>();
	private final SortedSet<String> breakpoints = new TreeSet<String>();
	private boolean hasBreakpoint;
	private final Statistics commStats = new Statistics();
	private final List<ITerm> auxiliaryTerms = new ArrayList<ITerm>();
	private final Set<String> auxiliaryTermsAdded = new TreeSet<String>();
	private final List<String> setLiteralNames = new ArrayList<String>();
	private final List<ITerm>  setLiterals = new ArrayList<ITerm>(); // used for assertions
	private final Map<ISymbol, ISymbol> matches = new TreeMap<ISymbol, ISymbol>();
	private final List<ITerm> sessionGoalsTerms = new ArrayList<ITerm>();

	public ExpressionContext() {
		this(false, (Collection<String>) null);
	}

	public ExpressionContext(boolean forceReplacementOfMatches) {
		this(forceReplacementOfMatches, (Collection<String>) null);
	}

	public ExpressionContext(boolean forceReplacementOfMatches, String[] breakpoints) {
		this(forceReplacementOfMatches, breakpoints != null ? Arrays.asList(breakpoints) : null);
	}

	public ExpressionContext(boolean forceReplacementOfMatches, Collection<String> breakpoints) {
		this.forceReplacementOfMatches = forceReplacementOfMatches;
		if (breakpoints != null) {
			this.breakpoints.addAll(breakpoints);
		}
	}

	public List<VariableSymbol> getVariables() {
		List<VariableSymbol> vars = new ArrayList<VariableSymbol>();
		vars.addAll(varState.keySet());
		return vars;
	}

	public NegatedConditionState getState(VariableSymbol sym) {
		if (varState.containsKey(sym)) {
			NegatedConditionState state = varState.get(sym);
			return state;
		}
		else {
			return NegatedConditionState.Unknown;
		}
	}

	public void setState(VariableSymbol sym, NegatedConditionState state) {
		varState.put(sym, state);
	}

	public boolean isForceReplacementOfMatches() {
		return forceReplacementOfMatches;
	}

	public void setForceReplacementOfMatches(boolean forceReplacementOfMatches) {
		this.forceReplacementOfMatches = forceReplacementOfMatches;
	}

	public void setOwner(VariableSymbol var, IScope owner) {
		varOwner.put(var, owner);
		addOwner(owner);
	}

	public void addOwner(IScope owner) {
		owners.add(owner);
	}

	public IScope getOwner(VariableSymbol var) {
		return varOwner.get(var);
	}

	public Collection<IScope> getOwners() {
		return owners;
	}

	public boolean hasBreakpoint() {
		return hasBreakpoint;
	}

	public void setBreakpoint() {
		hasBreakpoint = true;
	}

	public boolean isBreakpoint(String op) {
		return breakpoints.contains(op);
	}

	public void updateCommunicationStatistics(Statistics st) {
		commStats.add(st);
	}

	public Statistics getCommunicationStatistics() {
		return commStats;
	}

	public void addSessionGoalTerm(ITerm t) {
		sessionGoalsTerms.add(t);
	}

	public List<ITerm> getSessionGoalTerms() {
		return sessionGoalsTerms;
	}

	public void addAuxiliaryTerm(ITerm t) {
		auxiliaryTerms.add(t);
	}

	public List<ITerm> getAuxiliaryTerms() {
		return auxiliaryTerms;
	}

	public void markAuxiliaryTermsAdded(String tag) {
		auxiliaryTermsAdded.add(tag);
	}

	public boolean wereAuxiliaryTermsAdded(String tag) {
		return auxiliaryTermsAdded.contains(tag);
	}

	public void addSetLiteralName(String n) {
		setLiteralNames.add(n);
	}

	public String[] getSetLiteralNames() {
		return setLiteralNames.toArray(new String[setLiteralNames.size()]);
	}

	public void addSetLiteral(ITerm t) {
		setLiterals.add(t);
	}

	public ITerm[] getSetLiterals() {
		return setLiterals.toArray(new ITerm[setLiterals.size()]);
	}

	public void addMatch(ISymbol sym, ISymbol dummy) {
		matches.put(sym, dummy);
	}

	public boolean isMatched(ISymbol sym) {
		return matches.containsKey(sym);
	}

	public Map<ISymbol, ISymbol> getMatches() {
		return matches;
	}

}
