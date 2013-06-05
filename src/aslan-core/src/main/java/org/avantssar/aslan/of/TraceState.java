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

package org.avantssar.aslan.of;

public class TraceState {

	private final int index;
	/* Rules are applied before reaching this state. */
	private Rules rules;
	/* Clauses are applied after reaching this state. */
	private Clauses clauses;

	public TraceState(int index) {
		this.index = index;
	}

	public Rules getRules() {
		return rules;
	}

	public void setRules(Rules rules) {
		this.rules = rules;
	}

	public Clauses getClauses() {
		return clauses;
	}

	public void setClauses(Clauses clauses) {
		this.clauses = clauses;
	}

	public int getIndex() {
		return index;
	}

	public void accept(IAnalysisResultVisitor visitor) {
		visitor.visit(this);
	}

}
