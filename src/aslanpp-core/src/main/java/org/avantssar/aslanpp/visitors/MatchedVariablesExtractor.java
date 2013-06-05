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

import java.util.HashMap;
import java.util.Map;
import org.avantssar.aslanpp.model.ASLanPPSpecification;
import org.avantssar.aslanpp.model.AssertStatement;
import org.avantssar.aslanpp.model.AssignmentStatement;
import org.avantssar.aslanpp.model.BaseExpression;
import org.avantssar.aslanpp.model.BlockStatement;
import org.avantssar.aslanpp.model.BranchStatement;
import org.avantssar.aslanpp.model.ChannelGoal;
import org.avantssar.aslanpp.model.CommunicationTerm;
import org.avantssar.aslanpp.model.CompoundType;
import org.avantssar.aslanpp.model.ConcatTerm;
import org.avantssar.aslanpp.model.ConjunctionExpression;
import org.avantssar.aslanpp.model.ConstantSymbol;
import org.avantssar.aslanpp.model.ConstantTerm;
import org.avantssar.aslanpp.model.Constraint;
import org.avantssar.aslanpp.model.DeclarationGroup;
import org.avantssar.aslanpp.model.DefaultPseudonymTerm;
import org.avantssar.aslanpp.model.DisjunctionExpression;
import org.avantssar.aslanpp.model.Entity;
import org.avantssar.aslanpp.model.EqualityExpression;
import org.avantssar.aslanpp.model.Equation;
import org.avantssar.aslanpp.model.ExistsExpression;
import org.avantssar.aslanpp.model.ForallExpression;
import org.avantssar.aslanpp.model.FreshStatement;
import org.avantssar.aslanpp.model.FunctionSymbol;
import org.avantssar.aslanpp.model.FunctionTerm;
import org.avantssar.aslanpp.model.Goal;
import org.avantssar.aslanpp.model.HornClause;
import org.avantssar.aslanpp.model.IExpression;
import org.avantssar.aslanpp.model.ITerm;
import org.avantssar.aslanpp.model.ImplicationExpression;
import org.avantssar.aslanpp.model.InequalityExpression;
import org.avantssar.aslanpp.model.IntroduceStatement;
import org.avantssar.aslanpp.model.LTLExpression;
import org.avantssar.aslanpp.model.LoopStatement;
import org.avantssar.aslanpp.model.MacroSymbol;
import org.avantssar.aslanpp.model.MacroTerm;
import org.avantssar.aslanpp.model.NegationExpression;
import org.avantssar.aslanpp.model.NewEntityInstanceStatement;
import org.avantssar.aslanpp.model.NumericTerm;
import org.avantssar.aslanpp.model.PseudonymTerm;
import org.avantssar.aslanpp.model.RetractStatement;
import org.avantssar.aslanpp.model.SecrecyGoalStatement;
import org.avantssar.aslanpp.model.SelectStatement;
import org.avantssar.aslanpp.model.SessionChannelGoal;
import org.avantssar.aslanpp.model.SessionSecrecyGoal;
import org.avantssar.aslanpp.model.SetLiteralTerm;
import org.avantssar.aslanpp.model.SetType;
import org.avantssar.aslanpp.model.SimpleType;
import org.avantssar.aslanpp.model.SymbolicInstanceStatement;
import org.avantssar.aslanpp.model.TupleTerm;
import org.avantssar.aslanpp.model.TupleType;
import org.avantssar.aslanpp.model.UnnamedMatchTerm;
import org.avantssar.aslanpp.model.VariableSymbol;
import org.avantssar.aslanpp.model.VariableTerm;

public class MatchedVariablesExtractor implements IASLanPPVisitor {

	private final Map<VariableSymbol, Boolean> matchedVars = new HashMap<VariableSymbol, Boolean>();
	private boolean negated = false;

	public Map<VariableSymbol, Boolean> getMatchedVariables() {
		return matchedVars;
	}

	@Override
	public void visit(ASLanPPSpecification spec) {}

	@Override
	public void visit(Entity ent) {}

	@Override
	public void visit(SimpleType type) {}

	@Override
	public void visit(CompoundType type) {}

	@Override
	public void visit(SetType type) {}

	@Override
	public void visit(TupleType type) {}

	@Override
	public void visit(DeclarationGroup gr) {}

	@Override
	public void visit(VariableSymbol var) {}

	@Override
	public void visit(ConstantSymbol cnst) {}

	@Override
	public void visit(FunctionSymbol fnc) {}

	@Override
	public void visit(MacroSymbol macro) {}

	@Override
	public void visit(HornClause clause) {}

	@Override
	public void visit(Equation equation) {}

	@Override
	public void visit(Constraint constraint) {}

	@Override
	public void visit(Goal goal) {}

	@Override
	public void visit(SessionChannelGoal chGoal) {}

	@Override
	public void visit(SessionSecrecyGoal secrGoal) {}

	@Override
	public void visit(AssignmentStatement stmt) {}

	@Override
	public void visit(AssertStatement stmt) {}

	@Override
	public void visit(BlockStatement stmt) {}

	@Override
	public void visit(FreshStatement stmt) {}

	@Override
	public void visit(LoopStatement stmt) {}

	@Override
	public void visit(IntroduceStatement stmt) {}

	@Override
	public void visit(RetractStatement stmt) {}

	@Override
	public void visit(SelectStatement stmt) {}

	@Override
	public void visit(BranchStatement stmt) {}

	@Override
	public void visit(NewEntityInstanceStatement stmt) {}

	@Override
	public void visit(SymbolicInstanceStatement stmt) {}

	@Override
	public void visit(SecrecyGoalStatement stmt) {}

	@Override
	public void visit(ChannelGoal goal) {}

	@Override
	public void visit(NegationExpression expr) {
		negated = !negated;
		expr.getBaseExpression().accept(this);
		negated = !negated;
	}

	@Override
	public void visit(ConjunctionExpression expr) {
		expr.getLeftExpression().accept(this);
		expr.getRightExpression().accept(this);
	}

	@Override
	public void visit(DisjunctionExpression expr) {
		expr.getLeftExpression().accept(this);
		expr.getRightExpression().accept(this);
	}

	@Override
	public void visit(ExistsExpression expr) {
		expr.getBaseExpression().accept(this);
	}

	@Override
	public void visit(ForallExpression expr) {
		expr.getBaseExpression().accept(this);
	}

	@Override
	public void visit(ImplicationExpression expr) {
		expr.getLeftExpression().accept(this);
		expr.getRightExpression().accept(this);
	}

	@Override
	public void visit(LTLExpression expr) {
		for (IExpression child : expr.getChildExpressions()) {
			child.accept(this);
		}
	}

	@Override
	public void visit(EqualityExpression expr) {
		expr.getLeftTerm().accept(this);
		expr.getRightTerm().accept(this);
	}

	@Override
	public void visit(InequalityExpression expr) {
		expr.getLeftTerm().accept(this);
		expr.getRightTerm().accept(this);
	}

	@Override
	public void visit(BaseExpression expr) {
		expr.getBaseTerm().accept(this);
	}

	@Override
	public ITerm visit(CommunicationTerm term) {
		term.getSender().accept(this);
		term.getReceiver().accept(this);
		term.getPayload().accept(this);
		return term;
	}

	@Override
	public ITerm visit(ConcatTerm term) {
		for (ITerm part : term.getTerms()) {
			part.accept(this);
		}
		return term;
	}

	@Override
	public ITerm visit(ConstantTerm term) {
		return term;
	}

	@Override
	public ITerm visit(DefaultPseudonymTerm term) {
		term.getBaseTerm().accept(this);
		return term;
	}

	@Override
	public ITerm visit(FunctionTerm term) {
		for (ITerm arg : term.getArguments()) {
			arg.accept(this);
		}
		return term;
	}

	@Override
	public ITerm visit(MacroTerm term) {
		return term;
	}

	@Override
	public ITerm visit(PseudonymTerm term) {
		term.getBaseTerm().accept(this);
		term.getPseudonym().accept(this);
		return term;
	}

	@Override
	public ITerm visit(SetLiteralTerm term) {
		for (ITerm member : term.getTerms()) {
			member.accept(this);
		}
		return term;
	}

	@Override
	public ITerm visit(TupleTerm term) {
		for (ITerm part : term.getTerms()) {
			part.accept(this);
		}
		return term;
	}

	@Override
	public ITerm visit(VariableTerm term) {
		if (term.isMatched()) {
			VariableSymbol var = term.getSymbol();
			boolean isUniversallyQuantified = negated;
			if (matchedVars.containsKey(var)) {
				boolean wasUniversallyQuantified = matchedVars.get(var);
				isUniversallyQuantified &= wasUniversallyQuantified;
			}
			matchedVars.put(var, isUniversallyQuantified);
		}
		return term;
	}

	@Override
	public ITerm visit(UnnamedMatchTerm term) {
		return term;
	}

	@Override
	public ITerm visit(NumericTerm term) {
		return term;
	}

}
