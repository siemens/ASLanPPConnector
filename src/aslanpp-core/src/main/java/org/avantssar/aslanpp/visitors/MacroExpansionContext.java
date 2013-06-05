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

package org.avantssar.aslanpp.visitors;

import java.util.TreeMap;
import org.avantssar.aslanpp.model.ITerm;
import org.avantssar.aslanpp.model.MacroSymbol;
import org.avantssar.aslanpp.model.VariableSymbol;

public class MacroExpansionContext extends TreeMap<VariableSymbol, ITerm> {

	private static final long serialVersionUID = 7302775805837172019L;

	private final MacroSymbol macro;

	protected MacroExpansionContext(MacroSymbol macro) {
		this.macro = macro;
	}

	public MacroSymbol getMacro() {
		return macro;
	}

	public String getSignature() {
		StringBuffer sb = new StringBuffer();
		sb.append(macro.getName());
		if (size() > 0) {
			sb.append("(");
			boolean first = true;
			for (VariableSymbol sym : keySet()) {
				ITerm t = get(sym);
				if (!first) {
					sb.append(",");
				}
				sb.append(sym.getName()).append("=").append(t.getRepresentation());
				first = false;
			}
			sb.append(")");
		}
		return sb.toString();
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof MacroExpansionContext) {
			return this.getSignature().equals(((MacroExpansionContext) other).getSignature());
		}
		else {
			return false;
		}
	}

	@Override
	public String toString() {
		return getSignature();
	}
}
