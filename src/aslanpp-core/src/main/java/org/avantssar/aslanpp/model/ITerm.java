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

import java.util.List;
import org.avantssar.aslanpp.IReduceable;
import org.avantssar.aslanpp.model.ImplicitExplicitSymbol.ImplicitExplicitState;
import org.avantssar.aslanpp.visitors.IASLanPPVisitor;
import org.avantssar.commons.LocationInfo;

public interface ITerm extends IReduceable<ITerm>, Comparable<ITerm> {

	LocationInfo getLocation();

	boolean discardOnRHS();

	void setDiscardOnRHS(boolean discardOnRHS);

	boolean holdsActor();

	IType inferType();

	PseudonymTerm pseudonym(ITerm pseudonym);

	DefaultPseudonymTerm defaultPseudonym();

	BaseExpression expression();

	EqualityExpression equality(ITerm other);

	InequalityExpression inequality(ITerm other);

	ITerm accept(IASLanPPVisitor visitor);

	boolean isTypeCertain();

	boolean wasTypeSet();

	IScope getScope();

	String getRepresentation();

	void setImplicitExplicitState(ImplicitExplicitState state);

	boolean isImplicit();

	boolean isExpandedReceive();

	void setExpandedReceive(boolean isExpandedReceive);

	List<Annotation> getAnnotations();

	void copyAnnotations(ITerm term);

	void copyLocationScope(ITerm term);

	void visit();

	ITerm annotate(String annotation);

	void addSessionGoalTerm(ITerm term);
}
