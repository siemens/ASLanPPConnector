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

import java.util.Set;
import java.util.SortedMap;
import java.util.Stack;
import java.util.TreeMap;

public class SymbolsState {

	private final SortedMap<ISymbol, ITerm> currentValues = new TreeMap<ISymbol, ITerm>();

	private final Stack<SymbolsState> history = new Stack<SymbolsState>();

	public void assign(ISymbol sym, ITerm term) {
		currentValues.put(sym, term.reduce(this));
	}

	public boolean isAssigned(ISymbol sym) {
		return currentValues.containsKey(sym);
	}

	public ITerm getAssignedValue(ISymbol sym) {
		return currentValues.get(sym);
	}

	public Set<ISymbol> getAssignedSymbols() {
		return currentValues.keySet();
	}

	public void clear() {
		currentValues.clear();
	}

	private SymbolsState duplicate() {
		SymbolsState dup = new SymbolsState();
		dup.currentValues.putAll(currentValues);
		return dup;
	}

	private void copyFrom(SymbolsState other) {
		currentValues.clear();
		currentValues.putAll(other.currentValues);
	}

	public void push() {
		history.push(duplicate());
	}

	public void pop() {
		SymbolsState version = history.pop();
		copyFrom(version);
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("[symbols state:\n");
		for (ISymbol sym : currentValues.keySet()) {
			ITerm val = currentValues.get(sym);
			sb.append("\t");
			sb.append(sym.getName());
			sb.append("=");
			sb.append(val.getRepresentation());
			sb.append("\n");
		}
		sb.append("]");
		return sb.toString();
	}

}
