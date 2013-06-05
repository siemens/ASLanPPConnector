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

import java.util.List;
import java.util.Set;
import org.avantssar.aslanpp.model.Entity;
import org.avantssar.aslanpp.model.ITerm;

public interface INode extends IVisitable {

	IEdge getIncomingEdge();

	List<IEdge> getOutgoingEdges();

	void addOutgoingEdge(IEdge edge);

	boolean wasVisited();

	int getNodeIndex();

	ITerm getStateIndexTerm(Entity ent);

	boolean wasOptimized();

	INode optimize(IEdge incomingEdge);

	boolean wasBigSmallStateComputed();

	void computeBigSmallState(List<String> breakpoints);

	boolean isBigState();

	void setBigState();

	INode getLastNode(Set<Integer> visited);

	boolean mustStartNewTransitionOnLumping();

	void markNewTransitionStartOnLumpingAfterOptimizations();

}
