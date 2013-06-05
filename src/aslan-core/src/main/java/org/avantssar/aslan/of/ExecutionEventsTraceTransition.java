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

import java.util.ArrayList;

public class ExecutionEventsTraceTransition extends ArrayList<ExecutionEventsMacroStep> {

	private static final long serialVersionUID = 5249551853565383636L;

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("-- start transition\n");
		for (ExecutionEventsMacroStep ms : this) {
			sb.append(ms.toString());
		}
		sb.append("-- end transition\n");
		return sb.toString();
	}
}
