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
import java.util.Set;
import org.avantssar.aslan.IASLanSpec;
import org.avantssar.aslan.RewriteRule;
import org.avantssar.aslanpp.model.SymbolsState;

public interface IVisitable {

	Counters assignIndexes(Counters counters);

	void renderGraphviz(PrintStream out);

	int gatherTransitions(IASLanSpec spec, int nextTransitionIndex);

	int gatherTransitionsLumped(IASLanSpec spec, RewriteRule soFar, SymbolsState symState, Set<Integer> visitedStates, int nextTransitionIndex, int sourceNodeIndex, TransitionsRecorder rec);

	void clearVisited();

}
