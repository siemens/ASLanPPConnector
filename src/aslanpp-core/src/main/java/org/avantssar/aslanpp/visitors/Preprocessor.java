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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
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
import org.avantssar.aslanpp.model.ErrorMessages;
import org.avantssar.aslanpp.model.ExistsExpression;
import org.avantssar.aslanpp.model.ForallExpression;
import org.avantssar.aslanpp.model.FreshStatement;
import org.avantssar.aslanpp.model.FunctionSymbol;
import org.avantssar.aslanpp.model.FunctionTerm;
import org.avantssar.aslanpp.model.Goal;
import org.avantssar.aslanpp.model.HornClause;
import org.avantssar.aslanpp.model.IChannelGoalHolder;
import org.avantssar.aslanpp.model.IExpression;
import org.avantssar.aslanpp.model.IOwned;
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
import org.avantssar.commons.LocationInfo;

/**
 * Performs macro substitution in an ASLan++ specification (or a part of it). If
 * it is used on an entire ASLan++ specification or on an entity, it will also
 * remove the replaced macros from the specification/entity and all its child
 * entities.
 * 
 * @author gabi
 */
public class Preprocessor implements IASLanPPVisitor {

	private final ArrayDeque<MacroExpansionContext> macroContexts = new ArrayDeque<MacroExpansionContext>();

	private final ErrorGatherer err;

	public Preprocessor(ErrorGatherer err) {
		this.err = err;
	}

	private boolean inGuard   = false; // TODO maybe cleaner if this was a parameter of accept()
	private boolean inPayload = false; // TODO maybe cleaner if this was a parameter of accept()
	
	@Override
	public void visit(ASLanPPSpecification spec) {
		if (spec.getRootEntity() != null) {
			spec.getRootEntity().accept(this);
		}
	}

	@Override
	public void visit(Entity ent) {
		List<MacroSymbol> toRemove = new ArrayList<MacroSymbol>();
		for (MacroSymbol macro : ent.getEntriesByType(MacroSymbol.class)) {
			toRemove.add(macro);
			macro.accept(this);
		}
		for (HornClause clause : ent.getEntriesByType(HornClause.class)) {
			clause.accept(this);
		}
		for (Equation eq : ent.getEntriesByType(Equation.class)) {
			eq.accept(this);
		}
		for (Entity child : ent.getEntriesByType(Entity.class)) {
			child.accept(this);
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

		for (MacroSymbol macro : toRemove) {
			ent.removeEntry(macro);
		}
	}

	public void visit(SimpleType type) {}

	public void visit(CompoundType type) {}

	public void visit(SetType type) {}

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
	public void visit(MacroSymbol macro) {
		checkNameClashWithAncestors(macro.getOwner().findFirstEntity().getOwner().findFirstEntity(), macro, err, macro.getLocation(), "macro");
		ITerm newBody = macro.getBody().accept(this);
		macro.setBody(newBody);
	}

	@Override
	public void visit(HornClause clause) {
		ITerm newHead = clause.getHead().accept(this);
		List<ITerm> newBody = new ArrayList<ITerm>();
		for (ITerm t : clause.getBody()) {
			newBody.add(t.accept(this));
		}
		List<IExpression> newEqs = new ArrayList<IExpression>();
		for (IExpression e : clause.getEqualities()) {
			if (e instanceof InequalityExpression) {
				InequalityExpression eq = (InequalityExpression) e;
				newEqs.add(eq.getLeftTerm().accept(this).inequality(eq.getRightTerm().accept(this)));
			}
			else if (e instanceof EqualityExpression) {
				EqualityExpression eq = (EqualityExpression) e;
				newEqs.add(eq.getLeftTerm().accept(this).equality(eq.getRightTerm().accept(this)));
			}
		}
		clause.setHead(newHead);
		clause.setBody(newBody);
		clause.setEqualities(newEqs);
	}

	public void visit(Equation equation) {
		ITerm newLeft = equation.getLeftTerm().accept(this);
		ITerm newRight = equation.getRightTerm().accept(this);
		equation.setLeftTerm(newLeft);
		equation.setRightTerm(newRight);
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
		chGoal.setSender(chGoal.getSender().accept(this));
		chGoal.setReceiver(chGoal.getReceiver().accept(this));
	}

	@Override
	public void visit(SessionSecrecyGoal secrGoal) {
		List<ITerm> ppa = new ArrayList<ITerm>();
		for (ITerm t : secrGoal.getAgents()) {
			ppa.add(t.accept(this));
		}
		secrGoal.setAgents(ppa);
	}

	@Override
	public void visit(AssignmentStatement stmt) {
		ITerm newTerm = stmt.getTerm().accept(this);
		stmt.setTerm(newTerm);
	}

	@Override
	public void visit(AssertStatement stmt) {
		inGuard = true;
		stmt.getGuard().accept(this);
		inGuard = false;
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
		inGuard = true;
		stmt.getGuard().accept(this);
		inGuard = false;
		if (stmt.getBody() != null) {
			stmt.getBody().accept(this);
		}
	}

	@Override
	public void visit(IntroduceStatement stmt) {
		ITerm newTerm = stmt.getTerm().accept(this);
		stmt.setTerm(newTerm);
		visitChannelGoals(stmt);
	}

	private void visitChannelGoals(IChannelGoalHolder stmt) {
		for (ChannelGoal cg : stmt.getChannelGoals()) {
			cg.accept(this);
		}
	}

	@Override
	public void visit(RetractStatement stmt) {
		ITerm newTerm = stmt.getTerm().accept(this);
		stmt.setTerm(newTerm);
		visitChannelGoals(stmt);
	}

	@Override
	public void visit(SelectStatement stmt) {
		for (IExpression e : stmt.getChoices().keySet()) {
			IStatement s = stmt.getChoices().get(e);
			inGuard = true;
			e.accept(this);
			inGuard = false;
			visitChannelGoals(e);
			s.accept(this);
		}
	}

	@Override
	public void visit(BranchStatement stmt) {
		inGuard = true;
		stmt.getGuard().accept(this);
		inGuard = false;
		if (stmt.getTrueBranch() != null) {
			stmt.getTrueBranch().accept(this);
		}
		if (stmt.getFalseBranch() != null) {
			stmt.getFalseBranch().accept(this);
		}
	}

	@Override
	public void visit(NewEntityInstanceStatement stmt) {
		List<ITerm> newParameters = new ArrayList<ITerm>();
		for (ITerm t : stmt.getParameters()) {
			newParameters.add(t.accept(this));
		}
		stmt.setParameters(newParameters.toArray(new ITerm[newParameters.size()]));
	}

	@Override
	public void visit(SymbolicInstanceStatement stmt) {
		List<ITerm> newParameters = new ArrayList<ITerm>();
		for (ITerm t : stmt.getParameters()) {
			newParameters.add(t.accept(this));
		}
		stmt.setArgs(newParameters.toArray(new ITerm[newParameters.size()]));
		if (stmt.getGuard() != null) {
			inGuard = true;
			stmt.getGuard().accept(this);
			inGuard = false;
		}
	}

	public void visit(SecrecyGoalStatement stmt) {
		List<ITerm> newAgents = new ArrayList<ITerm>();
		for (ITerm t : stmt.getAgents()) {
			newAgents.add(t.accept(this));
		}
		stmt.setAgents(newAgents);
		stmt.setPayload(stmt.getPayload().accept(this));
	}

	public void visit(ChannelGoal goal) {
		goal.setSender(goal.getSender().accept(this));
		goal.setReceiver(goal.getReceiver().accept(this));
		goal.setPayload(goal.getPayload().accept(this));
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
		ITerm newLeftTerm = expr.getLeftTerm().accept(this);
		expr.setLeftTerm(newLeftTerm);
		ITerm newRightTerm = expr.getRightTerm().accept(this);
		expr.setRightTerm(newRightTerm);
	}

	@Override
	public void visit(InequalityExpression expr) {
		ITerm newLeftTerm = expr.getLeftTerm().accept(this);
		expr.setLeftTerm(newLeftTerm);
		ITerm newRightTerm = expr.getRightTerm().accept(this);
		expr.setRightTerm(newRightTerm);
	}

	@Override
	public void visit(BaseExpression expr) {
		ITerm newTerm = expr.getBaseTerm().accept(this);
		expr.setBaseTerm(newTerm);
	}

	@Override
	public CommunicationTerm visit(CommunicationTerm term) {
		ITerm newSender = term.getSender().accept(this);
		ITerm newReceiver = term.getReceiver().accept(this);
		inPayload = true;
		ITerm newPayload = term.getPayload().accept(this);
		inPayload = false;
		ITerm newChannel = (term.getChannel() != null ? term.getChannel().accept(this) : null);
		CommunicationTerm t = new CommunicationTerm(term.getLocation(), term.getScope(), newSender, newReceiver, newPayload, newChannel, term.getChannelType(), term.getReceiveHint(), term.isRenderAsFunction(), term
				.isRenderOOPStyle());
		t.copyAnnotations(term);
		return t;
				
	}

	@Override
	public ConcatTerm visit(ConcatTerm term) {
		List<ITerm> newTerms = new ArrayList<ITerm>();
		for (ITerm t : term.getTerms()) {
			newTerms.add(t.accept(this));
		}
		ConcatTerm t = ConcatTerm.concat(term.getLocation(), term.getScope(), newTerms.toArray(new ITerm[newTerms.size()]));
		t.copyAnnotations(term);
		return t;
	}

	@Override
	public ConstantTerm visit(ConstantTerm term) {
		return term;
	}

	@Override
	public DefaultPseudonymTerm visit(DefaultPseudonymTerm term) {
		ITerm newBase = term.getBaseTerm().accept(this);
		return newBase.defaultPseudonym();
	}

	@Override
	public FunctionTerm visit(FunctionTerm term) {
		List<ITerm> newParameters = new ArrayList<ITerm>();
		for (ITerm t : term.getArguments()) {
			newParameters.add(t.accept(this));
		}
		FunctionTerm t = term.getSymbol().term(term.getLocation(), term.getScope(), newParameters.toArray(new ITerm[newParameters.size()]));
		t.copyAnnotations(term);
		t.setOOPStyle(term.isOOPStyle());
		return t;
	}

	@Override
	public ITerm visit(MacroTerm term) {
		MacroExpansionContext newCtx = new MacroExpansionContext(term.getMacro());
		for (int i = 0; i < term.getArguments().size(); i++) {
			newCtx.put(term.getMacro().getArguments().get(i), term.getArguments().get(i).accept(this));
		}
		if (macroContexts.contains(newCtx)) {
			ASLanPreprocessorException ex = new ASLanPreprocessorException("Loop detected while expanding macros. Call stack is: " + getCallStack(newCtx) + ".", 0, 0);
			throw ex;
		}
		macroContexts.push(newCtx);
		ITerm expanded = term.getMacro().getBody().accept(this);
		expanded.copyLocationScope(term);
		expanded.copyAnnotations(term);
		macroContexts.pop();
		return expanded;
	}

	private String getCallStack(MacroExpansionContext ctx) {
		StringBuffer sb = new StringBuffer();
		Iterator<MacroExpansionContext> iter = macroContexts.descendingIterator();
		while (iter.hasNext()) {
			MacroExpansionContext s = iter.next();
			sb.append(s.getSignature()).append(" -> ");
		}
		sb.append(ctx.getSignature());
		return sb.toString();
	}

	@Override
	public PseudonymTerm visit(PseudonymTerm term) {
		ITerm newBase = term.getBaseTerm().accept(this);
		ITerm newPseudonym = term.getPseudonym().accept(this);
		return newBase.pseudonym(newPseudonym);
	}

	@Override
	public SetLiteralTerm visit(SetLiteralTerm term) {
		List<ITerm> newTerms = new ArrayList<ITerm>();
		for (ITerm t : term.getTerms()) {
			newTerms.add(t.accept(this));
		}
		SetLiteralTerm t = new SetLiteralTerm(term.getLocation(), term.getScope(), newTerms, term.getNameHint());
		t.copyAnnotations(term);
		if (inGuard) {
			err.addError(term.getLocation(), ErrorMessages.SET_LITERAL_IN_GUARD, t);
		}
		if (inPayload) {
			err.addError(term.getLocation(), ErrorMessages.SET_LITERAL_IN_PAYLOAD, t);
		}
		return t;
	}

	@Override
	public TupleTerm visit(TupleTerm term) {
		List<ITerm> newTerms = new ArrayList<ITerm>();
		for (ITerm t : term.getTerms()) {
			newTerms.add(t.accept(this));
		}
		TupleTerm t = TupleTerm.tuple(term.getLocation(), term.getScope(), newTerms.toArray(new ITerm[newTerms.size()]));
		t.copyAnnotations(term);
		return t;
	}

	@Override
	public ITerm visit(VariableTerm term) {
		ITerm replacement = null;
		VariableSymbol key = term.getSymbol();
		Iterator<MacroExpansionContext> iter = macroContexts.iterator();
		while (iter.hasNext()) {
			MacroExpansionContext ctx = iter.next();
			if (ctx.containsKey(key)) {
				replacement = ctx.get(key);
				break;
			}
		}
		if (replacement != null) {
			return replacement;
		}
		else {
			return term;
		}
	}

	public UnnamedMatchTerm visit(UnnamedMatchTerm term) {
		return term;
	}

	public NumericTerm visit(NumericTerm term) {
		return term;
	}

	protected static void checkNameClashWithAncestors(Entity ent, IOwned item, 
			ErrorGatherer err, LocationInfo location, String symbolType) {
		if (ent != null) {
			boolean found = false;
			for (IOwned candidate : ent.getEntriesByType(item.getClass())) {
				if (candidate.getOriginalName().equals(item.getOriginalName())) {
					err.addWarning(location, ErrorMessages.SYMBOL_HIDES_ANOTHER_SYMBOL_IN_ANCESTOR_ENTITY, 
							symbolType.substring(0, 1).toUpperCase() + symbolType.substring(1), item.getOriginalName(),
							item.getOwner().getOriginalName(), symbolType, ent.getOriginalName());
					found = true;
				}
			}
			if (!found) {
				checkNameClashWithAncestors(ent.getOwner().findFirstEntity(), item, err, location, symbolType);
			}
		}
	}
}
