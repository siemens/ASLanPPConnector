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
import org.avantssar.aslanpp.model.IStatement;
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
import org.avantssar.commons.ErrorGatherer;

public class DummySymbolsCreator implements IASLanPPVisitor {

	private final ErrorGatherer err;

	public DummySymbolsCreator(ErrorGatherer err) {
		this.err = err;
	}

	public ErrorGatherer getErrorGatherer() {
		return err;
	}

	@Override
	public void visit(ASLanPPSpecification spec) {
		if (spec.getRootEntity() != null) {
			spec.getRootEntity().accept(this);
		}
	}

	@Override
	public void visit(Entity ent) {
		for (HornClause clause : ent.getEntriesByType(HornClause.class)) {
			clause.accept(this);
		}
		for (Equation eq : ent.getEntriesByType(Equation.class)) {
			eq.accept(this);
		}
		for (Constraint constraint : ent.getEntriesByType(Constraint.class)) {
			constraint.accept(this);
		}
		for (Goal goal : ent.getEntriesByType(Goal.class)) {
			goal.accept(this);
		}
		for (SessionChannelGoal chGoal : ent.getEntriesByType(SessionChannelGoal.class)) {
			chGoal.accept(this);
		}
		for (SessionSecrecyGoal secrGoal : ent.getEntriesByType(SessionSecrecyGoal.class)) {
			secrGoal.accept(this);
		}
		if (ent.getBodyStatement() != null) {
			ent.getBodyStatement().accept(this);
		}
		for (Entity child : ent.getEntriesByType(Entity.class)) {
			child.accept(this);
		}
	}

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
	public void visit(HornClause clause) {
		clause.getHead().accept(this);
		for (ITerm t : clause.getBody()) {
			t.accept(this);
		}
		for (IExpression e : clause.getEqualities()) {
			e.accept(this);
		}
	}

	@Override
	public void visit(Equation equation) {
		equation.getLeftTerm().accept(this);
		equation.getRightTerm().accept(this);
	}

	@Override
	public void visit(Constraint constraint) {
		constraint.getFormula().accept(this);
	}

	@Override
	public void visit(Goal goal) {
		goal.getFormula().accept(this);
	}

	@Override
	public void visit(SessionChannelGoal chGoal) {
		chGoal.getSender().accept(this);
		chGoal.getReceiver().accept(this);
	}

	@Override
	public void visit(SessionSecrecyGoal secrGoal) {
		for (ITerm t : secrGoal.getAgents()) {
			t.accept(this);
		}
	}

	@Override
	public void visit(AssignmentStatement stmt) {
		stmt.getTerm().accept(this);
	}

	@Override
	public void visit(AssertStatement stmt) {
		if (stmt.getGuard() != null) {
			stmt.getGuard().accept(this);
		}
	}

	@Override
	public void visit(BlockStatement stmt) {
		for (IStatement s : stmt.getStatements()) {
			s.accept(this);
		}
	}

	@Override
	public void visit(FreshStatement stmt) {}

	@Override
	public void visit(LoopStatement stmt) {
		stmt.getGuard().accept(this);
		if (stmt.getBody() != null) {
			stmt.getBody().accept(this);
		}
	}

	@Override
	public void visit(IntroduceStatement stmt) {
		stmt.getTerm().accept(this);
	}

	@Override
	public void visit(RetractStatement stmt) {
		stmt.getTerm().accept(this);
	}

	@Override
	public void visit(SelectStatement stmt) {
		for (IExpression e : stmt.getChoices().keySet()) {
			IStatement s = stmt.getChoices().get(e);
			e.accept(this);
			s.accept(this);
		}
	}

	@Override
	public void visit(BranchStatement stmt) {
		stmt.getGuard().accept(this);
		if (stmt.getTrueBranch() != null) {
			stmt.getTrueBranch().accept(this);
		}
		if (stmt.getFalseBranch() != null) {
			stmt.getFalseBranch().accept(this);
		}
	}

	@Override
	public void visit(NewEntityInstanceStatement stmt) {
		for (ITerm t : stmt.getParameters()) {
			t.accept(this);
		}
	}

	@Override
	public void visit(SymbolicInstanceStatement stmt) {
		for (ITerm t : stmt.getParameters()) {
			t.accept(this);
		}
		if (stmt.getGuard() != null) {
			stmt.getGuard().accept(this);
		}
	}

	@Override
	public void visit(SecrecyGoalStatement stmt) {
		for (ITerm t : stmt.getAgents()) {
			t.accept(this);
		}
		stmt.getPayload().accept(this);
	}

	@Override
	public void visit(ChannelGoal goal) {
		goal.getSender().accept(this);
		goal.getReceiver().accept(this);
		goal.getPayload().accept(this);
	}

	@Override
	public void visit(NegationExpression expr) {
		expr.getBaseExpression().accept(this);
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
		for (IExpression e : expr.getChildExpressions()) {
			e.accept(this);
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
		for (ITerm t : term.getTerms()) {
			t.accept(this);
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
		for (ITerm t : term.getArguments()) {
			t.accept(this);
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
		for (ITerm t : term.getTerms()) {
			t.accept(this);
		}
		return term;
	}

	@Override
	public ITerm visit(TupleTerm term) {
		for (ITerm t : term.getTerms()) {
			t.accept(this);
		}
		return term;
	}

	@Override
	public ITerm visit(VariableTerm term) {
		return term;
	}

	@Override
	public ITerm visit(UnnamedMatchTerm term) {
		String dummyName = null;
		do {
			dummyName = term.getScope().getFreshNamesGenerator().getFreshName("Dummy", VariableSymbol.class);
		}
		while (term.getScope().isNameClash(dummyName, VariableSymbol.class));
		VariableSymbol dummySymbol = term.getScope().addUntypedVariable(dummyName, term.getLocation());
		term.setDummySymbol(dummySymbol);
		return term;
	}

	@Override
	public ITerm visit(NumericTerm term) {
		return term;
	}

}
