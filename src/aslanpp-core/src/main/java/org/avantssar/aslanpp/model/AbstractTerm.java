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
import java.util.List;

import org.avantssar.aslanpp.model.ImplicitExplicitSymbol.ImplicitExplicitState;
import org.avantssar.aslanpp.visitors.PrettyPrinter;
import org.avantssar.commons.LocationInfo;

public abstract class AbstractTerm implements ITerm {

	private IScope scope;
	private LocationInfo location;
	public static final String TERMS_FILE = "terms";
	private boolean discardOnRHS;

	private final List<ITerm> childrenTerms = new ArrayList<ITerm>();
	private boolean isExpandedReceive;

	private final List<ITerm> specialTerms = new ArrayList<ITerm>();

	// in order from inner to outer, if there are several annotations
	private List<Annotation> annotations = new ArrayList<Annotation>();

	protected AbstractTerm(LocationInfo location, IScope scope, boolean discardOnRHS) {
		this.location = location;
		this.scope = scope;
		this.discardOnRHS = discardOnRHS;
	}

	public void addSessionGoalTerm(ITerm term) {
		specialTerms.add(term);
	}

	protected void clearChildren() {
		childrenTerms.clear();
	}

	protected void addChildrenTerm(ITerm term) {
		childrenTerms.add(term);
	}

	public IScope getScope() {
		return scope;
	}

	public LocationInfo getLocation() {
		return location;
	}

	public boolean discardOnRHS() {
		return discardOnRHS;
	}

	public void setDiscardOnRHS(boolean discardOnRHS) {
		this.discardOnRHS = discardOnRHS;
	}

	public void buildContext(ExpressionContext ctx, boolean isInNegatedCondition) {
		for (Annotation ann : annotations) {
			ann.buildGoalContext(ctx, this);
		}
		for (ITerm t : specialTerms) {
			ctx.addSessionGoalTerm(t);
		}
		for (ITerm t : childrenTerms) {
			t.buildContext(ctx, isInNegatedCondition);
		}
		// if (isExpandedReceive() && ctx.isBreakpoint(Prelude.RECEIVE)) {
		// ctx.setBreakpoint();
		// }
	}

	public int compareTo(ITerm other) {
		return getRepresentation().compareTo(other.getRepresentation());
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof ITerm) {
			ITerm ot = (ITerm) other;
			return compareTo(ot) == 0;
		}
		else {
			return false;
		}
	}

	public void useContext(ExpressionContext ctx, SymbolsState symState) {
		for (ITerm t : childrenTerms) {
			t.useContext(ctx, symState);
		}
	}

	public boolean holdsActor() {
		return false;
	}

	public PseudonymTerm pseudonym(ITerm pseudonym) {
		return new PseudonymTerm(pseudonym.getLocation(), getScope(), this, pseudonym);
	}

	public DefaultPseudonymTerm defaultPseudonym() {
		return new DefaultPseudonymTerm(getLocation(), getScope(), this);
	}

	public BaseExpression expression() {
		return new BaseExpression(this);
	}

	public EqualityExpression equality(ITerm other) {
		return new EqualityExpression(this, other);
	}

	public InequalityExpression inequality(ITerm other) {
		return new InequalityExpression(this, other);
	}

	public boolean isTypeCertain() {
		return true;
	}

	public boolean isTypeCertainAll(List <ITerm> terms) {
		boolean result = true;
		for (ITerm t : terms) {
			if (!t.isTypeCertain()) {
				result = false;
				break;
			}
		}
		return result;
	}

	public boolean wasTypeSet() {
		return true;
	}

	public boolean wasTypeSetAll(List <ITerm> terms) {
		boolean result = true;
		for (ITerm t : terms) {
			if (!t.wasTypeSet()) {
				result = false;
				break;
			}
		}
		return result;
	}

	@Override
	public String getRepresentation() {
		PrettyPrinter pp = new PrettyPrinter(true);
		accept(pp);
		return pp.toString();
	}

	public void setImplicitExplicitState(ImplicitExplicitState state) {}

	@Override
	public boolean isImplicit() {
		return false;
	}

	public boolean isExpandedReceive() {
		return isExpandedReceive;
	}

	public void setExpandedReceive(boolean isExpandedReceive) {
		this.isExpandedReceive = isExpandedReceive;
	}

	public List<Annotation> getAnnotations() {
		return annotations;
	}

	public void copyAnnotations(ITerm term) {
		annotations = term.getAnnotations();
	}

	public void copyLocationScope(ITerm term) {
		location = term.getLocation();
		scope    = term.getScope();
	}
	
	public ITerm annotate(String name) {
		for (Annotation ann : annotations) {
			if (ann.toString().contentEquals(name)) {
				scope.getErrorGatherer().addError(getLocation(), ErrorMessages.DUPLICATE_ANNOTATION, ann);
				return this;
			}
		}
		annotations.add(new Annotation(name));
		return this;
	}

	public void visit() {
		for (Annotation ann : annotations) {
			ann.visit(this);
		}
	}

	@Override
	public String toString() {
		//return getRepresentation();
		PrettyPrinter pp = new PrettyPrinter(false);
		accept(pp);
		return pp.toString();
	}

	public static AbstractTerm constVar(IScope scope, String s) {
		if (Character.isLowerCase(s.charAt(0))) {
			ConstantSymbol c = scope.findConstant(s);
			if(c != null)
				return c.term();
		}
		else {
			VariableSymbol v = scope.findVariable(s);
			if(v != null)
				return v.term();
		}
		return null;
	}
}
