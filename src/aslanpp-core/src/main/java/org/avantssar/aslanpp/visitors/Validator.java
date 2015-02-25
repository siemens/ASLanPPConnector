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
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
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
import org.avantssar.aslanpp.model.ExpressionContext;
import org.avantssar.aslanpp.model.ForallExpression;
import org.avantssar.aslanpp.model.FreshStatement;
import org.avantssar.aslanpp.model.FunctionSymbol;
import org.avantssar.aslanpp.model.FunctionTerm;
import org.avantssar.aslanpp.model.Goal;
import org.avantssar.aslanpp.model.HornClause;
import org.avantssar.aslanpp.model.IChannelGoalHolder;
import org.avantssar.aslanpp.model.IExpression;
import org.avantssar.aslanpp.model.IStatement;
import org.avantssar.aslanpp.model.ISymbol;
import org.avantssar.aslanpp.model.ITerm;
import org.avantssar.aslanpp.model.IType;
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
import org.avantssar.aslanpp.model.Prelude;
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
 * Validates an ASLan++ specification (or a part of it).
 * 
 * @author gabi
 */
public class Validator implements IASLanPPVisitor {

	// matches only allowed within guards
	// in IFs and WHILEs no send and no receive
	// in SELECTs no send
	// in general in guards no send
	// no sends/receives in goals (including assertions)

	// channel goals cannot be used as goals

	// if (!getEntriesByType(Entity.class).contains(ent)) {
	// throw new ASLanSemanticErrorException("Entity '" + getOriginalName() +
	// "' cannot instantiate entity '" + ent.getOriginalName() +
	// "' because it is not its direct descendant.");
	// }

	// - things to validate
	// - no variable has type FACT or a composed type that contains FACT
	// - in communication term sender and receiver should be agents and the
	// payload should be message

	public enum NegatedConditionState {
		Unknown, OnlyPositive, OnlyNegative, Both
	};

	private class ValidationContext {

		public int sends;
		public int receives;
		public final Map<VariableSymbol, List<VariableTerm>> varsUsage;
		public final Map<VariableSymbol, NegatedConditionState> varsState;
		public boolean negated;

		public ValidationContext() {
			sends = 0;
			receives = 0;
			varsUsage = new TreeMap<VariableSymbol, List<VariableTerm>>();
			varsState = new HashMap<VariableSymbol, NegatedConditionState>();
			negated = false;
		}

		public void addVarUsage(VariableSymbol var, VariableTerm term) {
			if (!varsUsage.containsKey(var)) {
				List<VariableTerm> terms = new ArrayList<VariableTerm>();
				terms.add(term);
				varsUsage.put(var, terms);
			}
			else {
				List<VariableTerm> terms = varsUsage.get(var);
				terms.add(term);
			}

			NegatedConditionState isInNegatedCondition = varsState.get(var);
			if (isInNegatedCondition == null) {
				isInNegatedCondition = NegatedConditionState.Unknown;
			}
			if (negated) {
				if (isInNegatedCondition == NegatedConditionState.Unknown) {
					isInNegatedCondition = NegatedConditionState.OnlyNegative;
				}
				else if (isInNegatedCondition == NegatedConditionState.OnlyPositive) {
					isInNegatedCondition = NegatedConditionState.Both;
				}
			}
			else {
				if (isInNegatedCondition == NegatedConditionState.Unknown) {
					isInNegatedCondition = NegatedConditionState.OnlyPositive;
				}
				else if (isInNegatedCondition == NegatedConditionState.OnlyNegative) {
					isInNegatedCondition = NegatedConditionState.Both;
				}
			}
			varsState.put(var, isInNegatedCondition);
		}

		public boolean hasMatches() {
			for (VariableSymbol var : varsUsage.keySet()) {
				List<VariableTerm> terms = varsUsage.get(var);
				for (VariableTerm t : terms) {
					if (t.isMatched()) {
						return true;
					}
				}
			}
			return false;
		}

		public void checkAndFixMatches(IExpression expr) {
			for (VariableSymbol var : varsUsage.keySet()) {
				List<VariableTerm> terms = varsUsage.get(var);
				boolean isAnyMatch = false;
				VariableSymbol dummy = null;
				for (VariableTerm t : terms) {
					if (t.isMatched()) {
						isAnyMatch = true;
						dummy = t.getDummySymbol();
						break;
					}
				}
				if (isAnyMatch) {
					for (VariableTerm t : terms) {
						if (!t.isMatched()) {
							err.addError(t.getLocation(), ErrorMessages.VARIABLE_NOT_UNIFORMLY_MATCHES, var.getOriginalName(), t);
							t.setMatched(dummy);
						}
						else {
							// make sure all appearances use the same dummy symbol
							VariableSymbol oldDummy = t.getDummySymbol();
							if (/*oldDummy != null && */!oldDummy.equals(dummy)) {
								t.setMatched(dummy);
								// remove unused dummy symbol
								oldDummy.transferTo(null);
							}
						}
					}
				}
			}
		}

		public void checkOwners() {
			for (VariableSymbol var : varsUsage.keySet()) {
				if (var.getOwner().participatesForSymbol(var)) {
					List<VariableTerm> terms = varsUsage.get(var);
					for (VariableTerm t : terms) {
						checkVariableOwner(var, t.getLocation());
					}
				}
			}
		}

	}

	private final ErrorGatherer err;
	private final Deque<ValidationContext> stack = new ArrayDeque<ValidationContext>();
	private Entity ent;

	private final String[] notRetractable = new String[] { Prelude.DISHONEST, Prelude.IKNOWS };

	public Validator(ErrorGatherer err) {
		this.err = err;
	}

	public ErrorGatherer getErrorGatherer() {
		return err;
	}

	@Override
	public void visit(ASLanPPSpecification spec) {
		if (spec.getRootEntity() != null) {
			if (spec.getRootEntity().getParameters().size() > 0) {
				err.addException(ErrorMessages.ROOT_ENTITY_NO_PARAMETERS, spec.getRootEntity().getOriginalName());
			}

			spec.getRootEntity().accept(this);
		}
	}

	@Override
	public void visit(Entity ent) {
		Entity oldEnt = this.ent;
		this.ent = ent;

		// check if breakpoints are defined symbols
		if (ent.getBreakpoints() != null) {
			for (String bp : ent.getBreakpoints()) {
				ISymbol bpSym = ent.findConstant(bp);
				if (bpSym == null) {
					bpSym = ent.findFunction(bp);
					if (bpSym == null) {
						err.addError(ent.getBodyStatement().getLocation(), ErrorMessages.UNDEFINED_BREAKPOINT, bp, ent.getOriginalName());
					}
				}
			}
		}

		for (SimpleType st : ent.getEntriesByType(SimpleType.class)) {
			st.accept(this);
		}

		for (VariableSymbol v : ent.getEntriesByType(VariableSymbol.class)) {
			v.accept(this);
		}

		for (ConstantSymbol c : ent.getEntriesByType(ConstantSymbol.class)) {
			c.accept(this);
		}

		for (FunctionSymbol f : ent.getEntriesByType(FunctionSymbol.class)) {
			f.accept(this);
		}

		for (HornClause cl : ent.getEntriesByType(HornClause.class)) {
			cl.accept(this);
		}

		for (Equation eq : ent.getEntriesByType(Equation.class)) {
			eq.accept(this);
		}

		for (Constraint c : ent.getEntriesByType(Constraint.class)) {
			c.accept(this);
		}
		for (Goal g : ent.getEntriesByType(Goal.class)) {
			g.accept(this);
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

		this.ent = oldEnt;
	}

	@Override
	public void visit(SimpleType type) {
		if (type.wasDisambiguated()) {
			ent.getErrorGatherer().addException(type.getLocation(), ErrorMessages.DUPLICATE_TYPE, type.getOriginalName());
		}
	}

	@Override
	public void visit(CompoundType type) {
		FunctionSymbol fnc = ent.findFunction(type.getName());
		if (type.getName() != CompoundType.CONCAT) {
			if (fnc == null) {
				ent.getErrorGatherer().addException(type.getLocation(), ErrorMessages.UNKNOWN_FUNCTION_USED_IN_COMPOUND_TYPE, type);
			}
			if (fnc.getArgumentsTypes().size() != type.getArgumentTypes().size()) {
				ent.getErrorGatherer().addException(type.getLocation(), ErrorMessages.FUNCTION_IN_COMPOUND_TYPE_WRONG_NUMBER_OF_ARGUMENTS, 
						fnc.getOriginalName(), fnc.getArgumentsTypes().size(), type.getArgumentTypes().size());
			}
		}
		// TODO: check arguments types
		for (IType t : type.getArgumentTypes()) {
			t.accept(this);
		}
	}

	@Override
	public void visit(SetType type) {
		type.getBaseType().accept(this);
	}

	@Override
	public void visit(TupleType type) {
		for (IType t : type.getBaseTypes()) {
			t.accept(this);
		}
	}

	@Override
	public void visit(DeclarationGroup gr) {}

	@Override
	public void visit(VariableSymbol var) {
		var.getType().accept(this);
		if (ent.findType(Prelude.FACT).isAssignableFrom(var.getType())) {
			ent.getErrorGatherer().addError(var.getLocation(), ErrorMessages.ELEMENT_OF_TYPE_FACT_NOT_ACCEPTED, var.getName(), var.getType());
		}
	}

	@Override
	public void visit(ConstantSymbol cnst) {
		cnst.getType().accept(this);
		Preprocessor.checkNameClashWithAncestors(cnst.getOwner().findFirstEntity().getOwner().findFirstEntity(), cnst, err, cnst.getLocation(), "constant");
	}

	@Override
	public void visit(FunctionSymbol fnc) {
		fnc.getType().accept(this);
		Preprocessor.checkNameClashWithAncestors(fnc.getOwner().findFirstEntity().getOwner().findFirstEntity(), fnc, err, fnc.getLocation(), "function");
	}

	@Override
	public void visit(MacroSymbol macro) {}

	@Override
	public void visit(HornClause clause) {
		ExpressionContext ctxh = new ExpressionContext();
		clause.getHead().buildContext(ctxh, false);
		List<VariableSymbol> headVars = ctxh.getVariables();
		List<VariableSymbol> bodyVars = new ArrayList<VariableSymbol>();
		for (ITerm term : clause.getBody()) {
			ExpressionContext ctx = new ExpressionContext();
			term.buildContext(ctx, false);
			bodyVars.addAll(ctx.getVariables());
			if (!ent.findType(Prelude.FACT).isAssignableFrom(term.inferType())) {
				err.addError(term.getLocation(), ErrorMessages.ONLY_FACTS_CAN, "appear in clauses", term, term.inferType());
			}
			validateSimpleTerm(term);
		}
		for (IExpression ex : clause.getEqualities()) {
			ExpressionContext ctx = new ExpressionContext();
			ex.buildContext(ctx, false);
			bodyVars.addAll(ctx.getVariables());
			// raise warning if (in-)equalities are used
			err.addWarning(ex.getLocation(), ErrorMessages.EQUALITIES_IN_HORN_CLAUSES_ARE_EXPERIMENTAL);
		}
		// check usage of universally quantified variables
		for (VariableSymbol v : clause.getUniversallyQuantified()) {
			if (!headVars.contains(v)) {
				err.addWarning(clause.getHead().getLocation(), ErrorMessages.UNIVERSALLY_QUANTIFIED_VARIABLE_UNUSED_IN_HEAD_OF_HORN_CLAUSE, v.getOriginalName(), clause.getOriginalName());
			}
			if (bodyVars.contains(v)) {
				err.addError(v.getLocation(), ErrorMessages.UNIVERSALLY_QUANTIFIED_VARIABLE_CANNOT_BE_USED_IN_BODY_OF_HORN_CLAUSE, v.getOriginalName(), clause.getOriginalName());
			}
		}
/* disabled because not really needed
		// check variables in head
		// TODO for non-local non-inherited variables, this check is superseded by 'Variable ".." is not defined in the scope of ".."'.
		List<VariableSymbol> ds = new ArrayList<VariableSymbol>();
		ds.addAll(clause.getArguments()); 
		ds.addAll(clause.getUniversallyQuantified());
		for (VariableSymbol v : headVars) {
			if (!ds.contains(v)) {
				err.addError(v.getLocation(), ErrorMessages.VARIABLE_MUST_APPEAR_QUANTIFIED_OR_ARG_OF_HORN_CLAUSE, v.getOriginalName(), clause.getOriginalName());
			}
		}
*/
		// check variables in body
		// TODO for non-local non-inherited variables, this check is superseded by 'Variable ".." is not defined in the scope of ".."'.
		List<VariableSymbol> hs = new ArrayList<VariableSymbol>();
		hs.addAll(clause.getArguments()); 
		hs.addAll(headVars);
		for (VariableSymbol v : bodyVars) {
			if (!hs.contains(v)) {
				err.addError(v.getLocation(), ErrorMessages.VARIABLE_MUST_APPEAR_IN_HEAD_OR_ARGS_OF_HORN_CLAUSE, v.getOriginalName(), clause.getOriginalName());
			}
		}
		// check usage of arguments
		for (VariableSymbol v : clause.getArguments()) {
			if (!(headVars.contains(v) || bodyVars.contains(v))) {
				err.addWarning(clause.getLocation(), ErrorMessages.PARAMETER_SHOULD_BE_USED_IN_HORN_CLAUSE, v.getOriginalName(), clause.getOriginalName());
			}
		}
		checkFactTrueFalse(clause.getHead(), "appear in the head of a clause");
		if (clause.getHead() instanceof FunctionTerm) {
			FunctionTerm cterm = (FunctionTerm) clause.getHead();
			if (cterm.getSymbol().getName().equals(Prelude.WITNESS) || 
				cterm.getSymbol().getName().equals(Prelude.REQUEST) || 
				cterm.getSymbol().getName().equals(Prelude.SECRET)) {
				ent.getErrorGatherer().addError(clause.getHead().getLocation(), ErrorMessages.CANNOT_APPEAR_IN_HEAD_OF_HORN_CLAUSE, cterm.getSymbol().getName());
			}
		}
		validateSimpleTerm(clause.getHead());
	}

	@Override
	public void visit(Equation equation) {
		if (!equation.getOwner().findFirstEntity().equals(equation.getOwner().findRootEntity())) {
			err.addException(equation.getLocation(), ErrorMessages.EQUATIONS_ALLOWED_ONLY_IN_ROOT_ENTITY);
		}
		validateSimpleTerm(equation.getLeftTerm());
		validateSimpleTerm(equation.getRightTerm());
	}

	@Override
	public void visit(Constraint constraint) {
		if (!constraint.getOwner().findFirstEntity().equals(constraint.getOwner().findRootEntity())) {
			err.addException(constraint.getLocation(), ErrorMessages.CONSTRAINTS_ALLOWED_ONLY_IN_ROOT_ENTITY);
		}
		// TODO: add location
		validateGoal(constraint.getFormula(), null);
	}

	@Override
	public void visit(Goal goal) {
		// TODO: add location
		validateGoal(goal.getFormula(), null);
	}

	@Override
	public void visit(SessionChannelGoal chGoal) {}

	@Override
	public void visit(SessionSecrecyGoal secrGoal) {}

	@Override
	public void visit(AssignmentStatement stmt) {
		validateSimpleTerm(stmt.getTerm());
		checkActorAssignment(stmt.getSymbolTerm());
		checkVariableOwner(stmt.getSymbol(), stmt.getLocation());
	}

	private void validateSimpleTerm(ITerm term) {
		validateSimpleTerm(term, true);
	}
	
	private void validateSimpleTerm(ITerm term, boolean forbidMatches) {
		ValidationContext ctx = new ValidationContext();
		stack.push(ctx);
		term.accept(this);
		if (ctx.sends > 0) {
			err.addError(term.getLocation(), ErrorMessages.SENDS_ONLY_IN_TRANSMISSIONS, term);
		}
		if (ctx.receives > 0) {
			err.addError(term.getLocation(), ErrorMessages.RECEIVES_ONLY_IN_SELECT_GUARDS_AND_TRANSMISSIONS, term);
		}
		if (forbidMatches && ctx.hasMatches()) {
			err.addError(term.getLocation(), ErrorMessages.MATCHES_CAN_ONLY_BE_USED_IN_GUARDS_AND_RECEIVES, term);
		}
		ctx.checkOwners();
		stack.pop();
	}

	@Override
	public void visit(AssertStatement stmt) {
		if (stmt.getGuard() != null) {
			validateGoal(stmt.getGuard(), stmt.getLocation());
		}
	}

	private void validateGoal(IExpression expr, LocationInfo location) {
		ValidationContext ctx = new ValidationContext();
		stack.push(ctx);
		expr.accept(this);
		if (ctx.sends > 0) {
			err.addError(location, ErrorMessages.SENDS_ONLY_IN_TRANSMISSIONS, expr);
		}
		if (ctx.receives > 0) {
			err.addError(location, ErrorMessages.RECEIVES_ONLY_IN_SELECT_GUARDS_AND_TRANSMISSIONS, expr);
		}
		if (ctx.hasMatches()) {
			err.addError(location, ErrorMessages.MATCHES_CAN_ONLY_BE_USED_IN_GUARDS_AND_RECEIVES, expr);
		}
		ctx.checkOwners();
		stack.pop();
	}

	@Override
	public void visit(BlockStatement stmt) {
		for (IStatement s : stmt.getStatements()) {
			s.accept(this);
		}
	}

	@Override
	public void visit(FreshStatement stmt) {
		checkActorAssignment(stmt.getSymbolTerm());
		checkVariableOwner(stmt.getSymbol(), stmt.getLocation());
	}

	@Override
	public void visit(LoopStatement stmt) {
		validateGuard(stmt.getGuard(), stmt.getLocation(), true);
		if (stmt.getBody() != null) {
			stmt.getBody().accept(this);
		}
	}

	private void validateGuard(IExpression expr, LocationInfo location, boolean noReceive) {
		//TODO consolidate this with validateSimpleTerm
		ValidationContext ctx = new ValidationContext();
		stack.push(ctx);
		expr.accept(this);
		if (ctx.sends > 0) {
			err.addError(location, ErrorMessages.SENDS_ONLY_IN_TRANSMISSIONS, expr);
		}
		if (noReceive && ctx.receives > 0) {
			err.addError(location, ErrorMessages.RECEIVES_ONLY_IN_SELECT_GUARDS_AND_TRANSMISSIONS, expr);
		}
		if (ctx.sends + ctx.receives != 1 && expr.getChannelGoals().size() > 0) {
			err.addError(expr.getLocation(), ErrorMessages.CHANNEL_GOALS_ONE_TRANSMISSION, expr);
		}
		ctx.checkAndFixMatches(expr);
		ctx.checkOwners();
		stack.pop();
	}

	@Override
	public void visit(IntroduceStatement stmt) {
		checkFactTrueFalse(stmt.getTerm(), "be introduced");
		
		//TODO consolidate the following with validateSimpleTerm
		ValidationContext ctx = new ValidationContext();
		stack.push(ctx);
		ITerm t = stmt.getTerm();
		t.accept(this);
		if (ctx.sends + ctx.receives != 1 && stmt.getChannelGoals().size() > 0) {
			err.addError(stmt.getLocation(), ErrorMessages.CHANNEL_GOALS_ONE_TRANSMISSION, t);
		}
		if (t instanceof CommunicationTerm) {
			CommunicationTerm ct = (CommunicationTerm)t;
			if (!ct.isReceive() &&  ct.getReceiver() instanceof UnnamedMatchTerm && !ct.getSender().holdsActor()) {
				err.addError(stmt.getLocation(), ErrorMessages.DUMMY_RECEIVER_ONLY_FOR_SENDING);
			}
		} 
		if (ctx.hasMatches() && ctx.receives == 0) {
			err.addError(stmt.getLocation(), ErrorMessages.MATCHES_CAN_ONLY_BE_USED_IN_GUARDS_AND_RECEIVES, t);
		}
		ctx.checkOwners();
		stack.pop();
		
		checkChannelGoals(stmt);
	}

	private void checkChannelGoals(IChannelGoalHolder holder) {
		for (ChannelGoal cg : holder.getChannelGoals()) {
			cg.accept(this);
		}
	}

	private void checkFactTrueFalse(ITerm term, String key) {
		if (term.inferType() != null && 
				!ent.findType(Prelude.FACT).isAssignableFrom(term.inferType())) {
			ent.getErrorGatherer().addError(term.getLocation(), ErrorMessages.ONLY_FACTS_CAN, key, 
					term, term.inferType());
		}
		if (term instanceof ConstantTerm) {
			ConstantTerm cterm = (ConstantTerm) term;
			if (cterm.getSymbol().getName().equals(Prelude.TRUE) || 
				cterm.getSymbol().getName().equals(Prelude.FALSE)) {
				ent.getErrorGatherer().addError(term.getLocation(), ErrorMessages.TRUE_FALSE_CANNOT, key);
			}
		}
	}

	@Override
	public void visit(RetractStatement stmt) {
		checkFactTrueFalse(stmt.getTerm(), "be retracted");
		checkNotRetractable(stmt.getTerm());
		validateSimpleTerm(stmt.getTerm(), false);
	}

	private void checkNotRetractable(ITerm term) {
		if (term instanceof FunctionTerm) {
			FunctionTerm fterm = (FunctionTerm) term;
			boolean found = false;
			for (String s : notRetractable) {
				if (fterm.getSymbol().getName().equals(s)) {
					found = true;
					break;
				}
			}
			if (found) {
				ent.getErrorGatherer().addException(term.getLocation(), ErrorMessages.TERM_CANNOT_BE_RETRACTED, term);
			}
		}
	}

	@Override
	public void visit(SelectStatement stmt) {
		for (IExpression e : stmt.getChoices().keySet()) {
			checkChannelGoals(e);
			validateGuard(e, stmt.getLocation(), false);
			IStatement s = stmt.getChoices().get(e);
			s.accept(this);
		}
	}

	@Override
	public void visit(BranchStatement stmt) {
		validateGuard(stmt.getGuard(), stmt.getLocation(), true);
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
			validateSimpleTerm(t);
		}
	}

	@Override
	public void visit(SymbolicInstanceStatement stmt) {
		for (ITerm t : stmt.getParameters()) {
			validateSimpleTerm(t);
		}
		if (stmt.getGuard() != null) {
			validateGuard(stmt.getGuard(), stmt.getLocation(), false);
		}
	}

	@Override
	public void visit(SecrecyGoalStatement stmt) {
		err.addWarning(stmt.getLocation(), ErrorMessages.INLINE_SECRECY_GOALS_DEPRECATED);
		for (ITerm t : stmt.getAgents()) {
			validateSimpleTerm(t);
		}
		validateSimpleTerm(stmt.getPayload());
	}

	@Override
	public void visit(ChannelGoal goal) {
		err.addWarning(goal.getLocation(), ErrorMessages.INLINE_CHANNEL_GOALS_DEPRECATED);
		validateSimpleTerm(goal.getSender());
		validateSimpleTerm(goal.getReceiver());
		validateSimpleTerm(goal.getPayload());
	}

	@Override
	public void visit(NegationExpression expr) {
		ValidationContext ctx = stack.peek();
		ctx.negated = !ctx.negated;
		expr.getBaseExpression().accept(this);
		ctx.negated = !ctx.negated;
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
		visitEqualityOrInequality(expr);
	}

	@Override
	public void visit(InequalityExpression expr) {
		visitEqualityOrInequality(expr);
	}

	private void visitEqualityOrInequality(EqualityExpression expr) {
		expr.getLeftTerm().accept(this);
		expr.getRightTerm().accept(this);
	}

	@Override
	public void visit(BaseExpression expr) {
		expr.getBaseTerm().accept(this);
	}

	@Override
	public ITerm visit(CommunicationTerm term) {
		ValidationContext ctx = stack.peek();
		if (term.isReceive()) {
			ctx.receives++;
		}
		else {
			ctx.sends++;
		}
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
		// should never get here
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
		if (term.isMatched()) {
			checkActorAssignment(term);
		}
		ValidationContext ctx = stack.peek();
		ctx.addVarUsage(term.getSymbol(), term);
		return term;
	}

	@Override
	public ITerm visit(UnnamedMatchTerm term) {
		return term;
	}

	public NumericTerm visit(NumericTerm term) {
		return term;
	}
	
	private void checkActorAssignment(VariableTerm var) {
		if (var.getSymbol().getName().equals(Entity.ACTOR_PREFIX)) {
			err.addError(var.getLocation(), ErrorMessages.ACTOR_ASSIGNED, var.getSymbol().getOriginalName());
		}
	}
	
	private void checkVariableOwner(VariableSymbol var, LocationInfo location) {
		Entity varEnt = var.getOwner().findFirstEntity();
		if (!varEnt.equals(ent)) {
			err.addWarning(location, ErrorMessages.INHERITED_VARIABLE_USED, var.getOriginalName(), varEnt.getOriginalName(), ent.getOriginalName());
		}
	}

}
