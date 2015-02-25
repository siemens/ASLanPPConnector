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

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import org.avantssar.aslanpp.model.ASLanPPSpecification;
import org.avantssar.aslanpp.model.AbstractChannelGoal;
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
import org.avantssar.aslanpp.model.IExpression;
import org.avantssar.aslanpp.model.IScope;
import org.avantssar.aslanpp.model.ISecrecyGoal;
import org.avantssar.aslanpp.model.IStatement;
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
import org.avantssar.commons.ChannelEntry;
import org.avantssar.commons.ChannelModel;
import org.avantssar.commons.ErrorGatherer;
import org.avantssar.commons.LocationInfo;
import org.avantssar.commons.Term;
import org.avantssar.commons.TranslatorOptions;

public class PostProcessor implements IASLanPPVisitor {

	private IScope ent;
	private ChannelModel cm;
	@SuppressWarnings("unused")
	private ASLanPPSpecification spec; // TODO not really used
	private final ErrorGatherer err;
	private boolean skipPublicAndInvertibleHornClauses;

	public PostProcessor(ErrorGatherer err) {
		this(ChannelModel.CCM, err);
	}

	public PostProcessor(ChannelModel cm, ErrorGatherer err) {
		this.cm = cm;
		this.err = err;
	}

	public boolean isSkipPublicAndInvertibleHornClauses() {
		return skipPublicAndInvertibleHornClauses;
	}

	public void setSkipPublicAndInvertibleHornClauses(boolean skipPublicAndInvertibleHornClauses) {
		this.skipPublicAndInvertibleHornClauses = skipPublicAndInvertibleHornClauses;
	}

	@Override
	public void visit(ASLanPPSpecification spec) {
		this.cm = spec.getChannelModel();
		this.spec = spec;
		Entity rootEnt = spec.getRootEntity();
		IScope root = spec.findRoot(); // = spec

		if (rootEnt != null) {

			visitSpecOrEntity(spec);

			// symbols and link constraint for ACM, if needed
			if (cm == ChannelModel.ACM) {
				spec.addVariable("ACM_OS", root.findType(Prelude.AGENT));
				spec.addVariable("ACM_RS", root.findType(Prelude.AGENT));
				spec.addVariable("ACM_Rcv", root.findType(Prelude.AGENT));
				spec.addVariable("ACM_Msg", root.findType(Prelude.MESSAGE));
				spec.addVariable("ACM_Ch", root.findType(Prelude.CHANNEL));
/*
				Constraint c = rootEnt.constraint(null, Prelude.ACM_CONFIDENTIAL_TO);
				VariableSymbol cCh = c.addVariable("Ch", root.findType(Prelude.CHANNEL));
				VariableSymbol cP  = c.addVariable("P" , root.findType(Prelude.AGENT));
				VariableSymbol cA  = c.addVariable("A" , root.findType(Prelude.AGENT));
				VariableSymbol cB  = c.addVariable("B" , root.findType(Prelude.AGENT));
				VariableSymbol cM  = c.addVariable("M" , root.findType(Prelude.MESSAGE));
				FunctionSymbol fnConfidential_to = root.findFunction(Prelude.ACM_CONFIDENTIAL_TO);
				FunctionSymbol fnRcvd = root.findFunction(Prelude.RCVD);
				IExpression fConf = fnConfidential_to.term(cCh.term(), cP.term()).expression();
				IExpression fRest = fnRcvd.term(cB.term(), cA.term(), cM.term(), cCh.term()).expression();
				fRest = fRest.implies(cB.term().equality(cP.term()));
				fRest = fConf.implies(fRest);
				fRest = new LTLExpression(LTLExpression.GLOBALLY, fRest).forall(cCh, cP, cA, cB, cM);
				c.setFormula(fRest);
*/
			}

			/* unused
			// Horn clauses for descendant
			HornClause hcDesc = rootEnt.hornClause("descendant_closure");
			hcDesc.setPartOfPrelude(true);
			String descPrefix = "Descendant_arg";
            VariableSymbol hcDescA1 = hcDesc.addVariable(spec.getFreshNamesGenerator().getFreshNameNumbered(descPrefix, VariableSymbol.class), root.findType(Prelude.NAT));
            VariableSymbol hcDescA2 = hcDesc.addVariable(spec.getFreshNamesGenerator().getFreshNameNumbered(descPrefix, VariableSymbol.class), root.findType(Prelude.NAT));
            VariableSymbol hcDescA3 = hcDesc.addVariable(spec.getFreshNamesGenerator().getFreshNameNumbered(descPrefix, VariableSymbol.class), root.findType(Prelude.NAT));
			hcDesc.addArgument(hcDescA1);
			hcDesc.addArgument(hcDescA2);
			hcDesc.addArgument(hcDescA3);
			hcDesc.setHead(root.findFunction(Prelude.DESCENDANT).term(hcDescA1.term(), hcDescA3.term()));
			hcDesc.addBody(root.findFunction(Prelude.DESCENDANT).term(hcDescA1.term(), hcDescA2.term()));
			hcDesc.addBody(root.findFunction(Prelude.DESCENDANT).term(hcDescA2.term(), hcDescA3.term()));
			
			HornClause hcDescDirect = rootEnt.hornClause("descendant_direct");
			hcDescDirect.setPartOfPrelude(true);
			hcDescDirect.addArgument(hcDescA1);
			hcDescDirect.addArgument(hcDescA2);
			hcDescDirect.setHead(root.findFunction(Prelude.DESCENDANT).term(hcDescA1.term(), hcDescA2.term()));
			hcDescDirect.addBody(root.findFunction(Prelude.CHILD).term(hcDescA1.term(), hcDescA2.term()));
*/
			if (TranslatorOptions.setsAsMessages) {
				HornClause hcContains = rootEnt.hornClause("iknows_contains");
				hcContains.setPartOfPrelude(true);
				String containsPrefix = "Contains_arg";
				IType message = root.findType(Prelude.MESSAGE); 
				VariableSymbol hcContainsE = hcContains.addVariable(spec.getFreshNamesGenerator().getFreshNameNumbered(containsPrefix, VariableSymbol.class), message);
				VariableSymbol hcContainsS = hcContains.addVariable(spec.getFreshNamesGenerator().getFreshNameNumbered(containsPrefix, VariableSymbol.class), new SetType(message));
				hcContains.addArgument(hcContainsE);
				hcContains.addArgument(hcContainsS);
				hcContains.setHead(root.findFunction(Prelude.IKNOWS).term(hcContainsE.term()));
				hcContains.addBody(root.findFunction(Prelude.CONTAINS).term(hcContainsS.term(), hcContainsE.term()));
				hcContains.addBody(root.findFunction(Prelude.IKNOWS).term(hcContainsS.term()));
			}
			rootEnt.accept(this);
		}
	}

	@Override
	public void visit(Entity ent) {
		IScope oldEnt = this.ent;
		this.ent = ent;
		IScope root = ent.findRoot();

		// generate state function state_(...)
		List<IType> argTypes = new ArrayList<IType>();
		argTypes.add(ent.getActorSymbol().getType());
		argTypes.add(ent.getIDSymbol().getType());
		argTypes.add(ent.getStepSymbol().getType());
		for (VariableSymbol var : ent.getStateSymbols()) {
			if (!skip(ent, var)) {
				argTypes.add(var.getType());
			}
		}
		FunctionSymbol fstate = ent.addFunction(Term.STATE_PREFIX + "_" + ent.getName(), root.findType(Prelude.FACT), argTypes.toArray(new IType[argTypes.size()]));
		fstate.setNonInvertible(true);
		fstate.setNonPublic(true);
		ent.setStateFunction(fstate);

		visitSpecOrEntity(ent);

		if (ent.getBodyStatement() != null) {
			ent.getBodyStatement().accept(this);
		}

		for (Entity child : ent.getEntriesByType(Entity.class)) {
			child.accept(this);
		}

		for (SessionChannelGoal chGoal : ent.getEntriesByType(SessionChannelGoal.class)) {
			if (chGoal.usedSender == 0) {
				err.addError(chGoal.getLocation(), ErrorMessages.CHANNEL_GOAL_UNUSED_SENDER, chGoal.getOriginalName());			
			}
			if (chGoal.usedReceiver == 0) {
				err.addError(chGoal.getLocation(), ErrorMessages.CHANNEL_GOAL_UNUSED_RECEIVER, chGoal.getOriginalName());			
			}
		}
		for (SessionSecrecyGoal secrGoal : ent.getEntriesByType(SessionSecrecyGoal.class)) {
			if (secrGoal.used == 0) {
				err.addError(secrGoal.getLocation(), ErrorMessages.SECRECY_GOAL_UNUSED, secrGoal.getOriginalName());			
			}
			if (secrGoal.used < secrGoal.getAgents().size()) {
				err.addWarning(secrGoal.getLocation(), ErrorMessages.SECRECY_GOAL_NUM_USED, secrGoal.getOriginalName(), secrGoal.getAgents().size(), secrGoal.used);			
			}
		}

		this.ent = oldEnt;
	}

	private void visitSpecOrEntity(IScope scope) {
		this.ent = scope;

		for (FunctionSymbol f : scope.getEntriesByType(FunctionSymbol.class)) {
			f.accept(this);
		}
		for (HornClause clause : scope.getEntriesByType(HornClause.class)) {
			clause.accept(this);
		}
		for (Equation eq : scope.getEntriesByType(Equation.class)) {
			eq.accept(this);
		}
		for (Goal goal : scope.getEntriesByType(Goal.class)) {
			goal.accept(this);
		}
		for (SessionChannelGoal chGoal : scope.getEntriesByType(SessionChannelGoal.class)) {
			chGoal.accept(this);
		}
		for (SessionSecrecyGoal secrGoal : scope.getEntriesByType(SessionSecrecyGoal.class)) {
			secrGoal.accept(this);
		}
	}

	private boolean skip(Entity ent, VariableSymbol var) {
		if (var.wasTransfered()) {
			return true;
		}
		if (ent.getActorSymbol().equals(var)) {
			return true;
		}
		if (ent.getIDSymbol().equals(var)) {
			return true;
		}
		if (ent.getStepSymbol().equals(var)) {
			return true;
		}
		return false;
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

	private boolean typeOK(IScope scope, IType t) {
		// allow sets also here
		return scope.findType(Prelude.MESSAGE).isAssignableFrom(t) || (t instanceof SetType);
	}

	@Override
	public void visit(FunctionSymbol fnc) {
		IScope root = fnc.getOwner().findRoot();
		if (!isSkipPublicAndInvertibleHornClauses()) {
			if (!fnc.isPartOfPrelude() && typeOK(fnc.getOwner(), fnc.getType()) && (!fnc.isNonInvertible() || !fnc.isNonPublic())) {
				List<VariableSymbol> hcSymbols = new ArrayList<VariableSymbol>();
				List<VariableTerm> hcTerms = new ArrayList<VariableTerm>();
				boolean allAreMessagesOrSets = true;
				for (IType t : fnc.getArgumentsTypes()) {
						// TODO: ugly to have these globalized argument names; is this unavoidable because of their type declarations?
					String prefix = /*"Arg";*/fnc.getOriginalName().substring(0, 1).toUpperCase() + fnc.getOriginalName().substring(1) + "_arg";
					String dummyName = fnc.getOwner().getFreshNamesGenerator().getFreshNameNumbered(prefix, VariableSymbol.class);
					VariableSymbol hcVar = fnc.getOwner().addVariable(dummyName, t);
					hcSymbols.add(hcVar);
					// if not all parameters are of type message, we don't generate the HT for public
					if (!typeOK(fnc.getOwner(), hcVar.getType())) {
						allAreMessagesOrSets = false;
					}
					hcTerms.add(hcVar.term());
				}
				fnc.setHCSymbols(hcSymbols);
				FunctionTerm bigTerm = root.findFunction(Prelude.IKNOWS).term(fnc.term(hcTerms.toArray(new VariableTerm[hcTerms.size()])));
				if (!fnc.isNonPublic() && allAreMessagesOrSets) {
					HornClause hcPublic = ent.hornClause(fnc.getOwner().getFreshNamesGenerator().getFreshName(getPublicName(fnc.getName()), HornClause.class));
					for (VariableSymbol a : hcSymbols) {
						hcPublic.addArgument(a);
					}
					hcPublic.setHead(bigTerm);
					for (VariableTerm vt : hcTerms) {
						hcPublic.addBody(root.findFunction(Prelude.IKNOWS).term(vt));
					}
				}
				if (!fnc.isNonInvertible()) {
					for (VariableSymbol v : hcSymbols) {
						// if the variable is not subtype of message, then don't generate anything
						if (typeOK(fnc.getOwner(), v.getType())) {
							HornClause hcInv = ent.hornClause(fnc.getOwner().getFreshNamesGenerator().getFreshNameNumbered(getInvertibleName(fnc.getName()), HornClause.class));
							for (VariableSymbol a: hcSymbols) {
								hcInv.addArgument(a);
							}
							hcInv.setHead(root.findFunction(Prelude.IKNOWS).term(v.term()));
							hcInv.addBody(bigTerm);
						}
					}
				}
				// special case for sign
				// else if (fnc.getOriginalName().equals(Prelude.SIGN)) {
				// HornClause hcInv =
				// ent.hornClause(fnc.getOwner().getFreshNamesGenerator().getFreshName(getInvertibleName(fnc.getName()),
				// HornClause.class));
				// hcInv.setHead(root.findFunction(Prelude.IKNOWS).term(hcTerms.get(1)));
				// hcInv.addBody(bigTerm);
				// }
			}
		}
	}

	private String getPublicName(String base) {
		return "public_" + base;
	}

	private String getInvertibleName(String base) {
		return "inv_" + base;
	}

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
	public void visit(SessionChannelGoal goal) {
		goal.getSender  ().accept(this);
		goal.getReceiver().accept(this);
		handleChannelGoal(goal, true);
	}

	@Override
	public void visit(SessionSecrecyGoal goal) {
		for (ITerm t : goal.getAgents()) {
			t.accept(this);
		}
		handleSecrecyGoal(goal, true);
	}

	@Override
	public void visit(AssignmentStatement stmt) {
		stmt.getSymbolTerm().accept(this);
		stmt.getTerm().accept(this);
	}

	@Override
	public void visit(AssertStatement stmt) {
		stmt.getGuard().accept(this);
		Entity firstEnt = ent.findFirstEntity();
		IScope root = stmt.getOwner().findRoot();

		// gather variables visible in this scope
		ExpressionContext ctx = new ExpressionContext();
		stmt.getGuard().buildContext(ctx, false);
		List<VariableSymbol> visible = new ArrayList<VariableSymbol>();
		gatherVisibleVars(stmt.getOwner(), visible);
		SortedSet<VariableSymbol> used = new TreeSet<VariableSymbol>();
		used.addAll(ctx.getVariables());
		List<IType> argTypes = new ArrayList<IType>();
		List<VariableTerm> pars = new ArrayList<VariableTerm>();
		List<       ITerm> args = new ArrayList<       ITerm>();
		for (VariableSymbol v : used) {
			if (visible.contains(v) && v.getOriginalName() != "IID") {
				argTypes.add(v.getType());
				pars.add(v.term());
				args.add(v.term());
			}
		}
		for (String n : ctx.getSetLiteralNames()) {
			VariableSymbol v = root.findVariable(n);   
			argTypes.add(v.getType());
			pars.add(v.term());
		}
		for (ITerm t : ctx.getSetLiterals()) {
			args.add(t);
		}
		VariableSymbol IID = firstEnt.getIDSymbol();  
		argTypes.add(IID.getType());
 		pars.add(IID.term());
 		args.add(IID.term());
 		VariableSymbol SL = firstEnt.getStepSymbol(); 
		argTypes.add(SL.getType());
		pars.add(SL.term());
		args.add(SL.term());
		String checkName = stmt.getOwner().getFreshNamesGenerator().getFreshName("check_" + stmt.getName(), FunctionSymbol.class);
		FunctionSymbol fncCheck = stmt.getOwner().addFunction(checkName, root.findType(Prelude.FACT), argTypes.toArray(new IType[argTypes.size()]));
		stmt.setCheckFunction(fncCheck);
		stmt.setFirstTerms(args.subList(0, args.size() - 1));

		// add goal
		Goal g = firstEnt.goal(stmt.getLocation(), stmt.getName());
		IExpression impl = fncCheck.term(stmt.getLocation(), stmt.getOwner(), pars.toArray(new VariableTerm[pars.size()])).expression().implies(stmt.getGuard());
		LTLExpression ltl = new LTLExpression(LTLExpression.GLOBALLY, impl);
		g.setFormula(ltl);
	}

	private void gatherVisibleVars(IScope scope, List<VariableSymbol> visible) {
		if (scope != null) {
			if (scope instanceof Entity) {
				Entity e = (Entity) scope;
				List<VariableSymbol> vars = e.getStateSymbols();
				visible.addAll(vars);
			}
			gatherVisibleVars(scope.getOwner(), visible);
		}
	}

	@Override
	public void visit(BlockStatement stmt) {
		for (IStatement s : stmt.getStatements()) {
			s.accept(this);
		}

	}

	@Override
	public void visit(FreshStatement stmt) {
		stmt.getSymbolTerm().accept(this);
		String freshName = stmt.getSymbol().getOwner().getFreshNamesGenerator().getFreshNameNumbered(stmt.getSymbol().getName(), VariableSymbol.class);
		VariableSymbol freshVar = stmt.getSymbol().getOwner().addVariable(freshName, stmt.getSymbol().getType(), stmt.getLocation());
		stmt.setFreshSymbol(freshVar);
		freshVar.setReferenceSymbol(stmt.getSymbol());
		freshVar.setFreshSymbol(true);
	}

	@Override
	public void visit(LoopStatement stmt) {
		stmt.getGuard().accept(this);
		visitChannelGoals(stmt.getGuard().getChannelGoals());
		if (stmt.getBody() != null) {
			stmt.getBody().accept(this);
		}
	}

	private void visitChannelGoals(List<ChannelGoal> goals) {
		for (ChannelGoal g : goals) {
			g.accept(this);
		}
	}

	@Override
	public void visit(IntroduceStatement stmt) {
		stmt.getTerm().accept(this);
		visitChannelGoals(stmt.getChannelGoals());
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
			visitChannelGoals(e.getChannelGoals());
		}
	}

	@Override
	public void visit(BranchStatement stmt) {
		stmt.getGuard().accept(this);
		visitChannelGoals(stmt.getGuard().getChannelGoals());
		if (stmt.getTrueBranch() != null) {
			stmt.getTrueBranch().accept(this);
		}
		if (stmt.getFalseBranch() != null) {
			stmt.getFalseBranch().accept(this);
		}
	}

	@Override
	public void visit(NewEntityInstanceStatement stmt) {
		IScope root = stmt.getEntity().findRoot();
		VariableSymbol newIDSymbol = stmt.getEntity().addVariable(stmt.getEntity().getFreshNamesGenerator().getFreshNameNumbered("IID", VariableSymbol.class), root.findType(Prelude.NAT));
		newIDSymbol.setReferenceSymbol(stmt.getEntity().getIDSymbol());
		newIDSymbol.setFreshSymbol(true);
		stmt.setNewIDSymbol(newIDSymbol);
		stmt.buildDummyValues();
		for (ITerm t : stmt.getParameters()) {
			t.accept(this);
		}
	}

	@Override
	public void visit(SymbolicInstanceStatement stmt) {
		IScope root = stmt.getEntity().findRoot();
		VariableSymbol newIDSymbol = stmt.getEntity().addVariable(stmt.getEntity().getFreshNamesGenerator().getFreshNameNumbered("IID", VariableSymbol.class), root.findType(Prelude.NAT));
		newIDSymbol.setReferenceSymbol(stmt.getEntity().getIDSymbol());
		newIDSymbol.setFreshSymbol(true);
		stmt.setNewIDSymbol(newIDSymbol);
		stmt.buildDummyValues();
		for (ITerm t : stmt.getParameters()) {
			t.accept(this);
		}
		if (stmt.getGuard() != null) {
			stmt.getGuard().accept(this);
		}
        Entity rootEnt = stmt.getEntity().findRootEntity();
		for (VariableSymbol v : stmt.getUniversallyQuantified()) {
			String freshName = rootEnt.getFreshNamesGenerator().getFreshName("symbolic_" + v.getName(), ConstantSymbol.class);
			ConstantSymbol symC = rootEnt.constants(v.getType(), freshName);
			symC.setReferenceSymbol(v);
		}
		// if (!rootEnt.getHasSymbolicInst()) {
		// // VariableSymbol symVar = root.addVariable("Sym_A",
		// // root.findType(Prelude.AGENT));
		// root.setHasSymbolicInst(symVar);
		// }
	}

	public void visit(SecrecyGoalStatement goal) {
		for (ITerm t : goal.getAgents()) {
			t.accept(this);
		}
		goal.getPayload().accept(this);
		handleSecrecyGoal(goal, false);
	}

	public void visit(ChannelGoal goal) {
		goal.getSender().accept(this);
		goal.getReceiver().accept(this);
		goal.getPayload().accept(this);
		handleChannelGoal(goal, false);
	}

	private void handleChannelAuthentication(AbstractChannelGoal goal) {
		Entity rootEnt = goal.getOwner().findRootEntity();
		IScope root = goal.getOwner().findRoot();
		String authProtocolName = goal.getAuthenticationProtocolName();
		String authGoalName = goal.getAuthenticationGoalName();
		ConstantSymbol cProt = rootEnt.findConstant(authProtocolName);
		if (cProt == null) {
			cProt = rootEnt.constants(root.findType(Prelude.PROTOCOL_ID), authProtocolName);
		}
		if (goal instanceof SessionChannelGoal || goal.getReceiver().holdsActor()) {
		  //Goal g = rootEnt.getEntryInHierarchy(authGoalName, Goal.class);
			Goal g = rootEnt.goal(goal.getLocation(), authGoalName);
			VariableSymbol gMsg = g.addVariable("AM", root.findType(Prelude.MESSAGE));
			VariableSymbol gReq = g.addVariable("AR", root.findType(Prelude.AGENT));
			VariableSymbol gWit = g.addVariable("AW", root.findType(Prelude.AGENT));
			VariableSymbol gIID = g.addVariable("IID", root.findType(Prelude.NAT));

			FunctionSymbol fRequest = root.findFunction(Prelude.REQUEST);
			FunctionSymbol fWitness = root.findFunction(Prelude.WITNESS);
			FunctionSymbol fDishonest = root.findFunction(Prelude.DISHONEST);

			IExpression f = fRequest.term(gReq.term(), gWit.term(), cProt.term(), gMsg.term(), gIID.term()).expression();
			f = f.implies(fWitness.term(gWit.term(), goal.hasUndirectedAuthentication() ? 
					root.findConstant(Prelude.INTRUDER).term() : gReq.term(), cProt.term(), gMsg.term()).expression().or(fDishonest.term(gWit.term()).expression()));

			g.setFormula(new LTLExpression(LTLExpression.GLOBALLY, f));
		}
	}

	private void handleChannelFreshness(AbstractChannelGoal goal) {
        Entity rootEnt = goal.getOwner().findRootEntity();
		IScope root = goal.getOwner().findRoot();
		String freshProtocolName = goal.getFreshnessProtocolName();
		String freshGoalName = goal.getFreshnessGoalName();
		ConstantSymbol cProt = rootEnt.findConstant(freshProtocolName);
		if (cProt == null) {
			cProt = rootEnt.constants(root.findType(Prelude.PROTOCOL_ID), freshProtocolName);
		}
		//Goal g = rootEnt.getEntryInHierarchy(freshGoalName, Goal.class);
		if (goal.hasAuthentication()) {
			Goal g = rootEnt.goal(goal.getLocation(), freshGoalName);
			VariableSymbol gMsg = g.addVariable("FM", root.findType(Prelude.MESSAGE));
			VariableSymbol gReq = g.addVariable("FR", root.findType(Prelude.AGENT));
			VariableSymbol gWit = g.addVariable("FW", root.findType(Prelude.AGENT));
			VariableSymbol gIID = g.addVariable("IID1", root.findType(Prelude.NAT));
			VariableSymbol gIID1 = g.addVariable("IID2", root.findType(Prelude.NAT));

			FunctionSymbol fRequest = root.findFunction(Prelude.REQUEST);
			FunctionSymbol fDishonest = root.findFunction(Prelude.DISHONEST);

			IExpression f = fRequest.term(gReq.term(), gWit.term(), cProt.term(), gMsg.term(), gIID.term()).expression();
			IExpression fRest = fRequest.term(gReq.term(), gWit.term(), cProt.term(), gMsg.term(), gIID1.term()).expression();
			fRest = fRest.and(gIID.term().inequality(gIID1.term()));
			fRest = fRest.not().or(fDishonest.term(gWit.term()).expression());
			f = f.implies(fRest);

			g.setFormula(new LTLExpression(LTLExpression.GLOBALLY, f));
		}
		else {
			err.addError(goal.getLocation(), ErrorMessages.FRESHNESS_GOAL_WITHOUT_AUTHENTICATION);
		}
			
	}

	private void handleSecrecyGoal(ISecrecyGoal sg, boolean useExisting) {
		IScope owner = sg.getOwner();
        Entity rootEnt = owner.findRootEntity();
		IScope root = owner.findRoot();
		LocationInfo location = sg.getLocation();
		String protName = sg.getSecrecyProtocolName();
		String goalName = sg.getSecrecyGoalName();
		String  setName = sg.getSetFunctionName();	
		FunctionSymbol fset = rootEnt.getSetFunction(setName, Prelude.getSetOf(root.findType(Prelude.AGENT)), location);

		Goal g = rootEnt.getEntryInHierarchy(goalName, Goal.class);
		if (g == null) {
			g = rootEnt.goal(location, goalName); //TODO if useExisting, maybe try not to duplicate SessionChannelGoal and SessionSecrecyGoal 
			VariableSymbol gM = g.addVariable("Msg", root.findType(Prelude.MESSAGE));
			VariableSymbol gS = g.addVariable("Knowers", Prelude.getSetOf(root.findType(Prelude.AGENT)));

			FunctionSymbol fSecret = root.findFunction(Prelude.SECRET);
			FunctionSymbol fIknows = root.findFunction(Prelude.IKNOWS);
			FunctionSymbol fContains = root.findFunction(Prelude.CONTAINS);
			ConstantSymbol cIntruder = root.findConstant(Prelude.INTRUDER);

			ConstantSymbol cProt = rootEnt.findConstant(protName);
			if (cProt == null) {
				cProt = rootEnt.constants(root.findType(Prelude.PROTOCOL_ID), protName);
			}
			g.setFormula(new LTLExpression(LTLExpression.GLOBALLY, 
					fSecret.term(gM.term(), cProt.term(), gS.term()).expression().
					and(fIknows.term(gM.term()).expression()).
					implies(fContains.term(gS.term(), cIntruder.term()).expression())));
		}
	    sg.setSetSymbol(fset);
	}

	private void handleChannelGoal(AbstractChannelGoal goal, boolean useExisting) {
		if (goal.hasSecrecy()) {
			handleSecrecyGoal(goal, useExisting);
		}
		if (goal.hasAuthentication()) {
			handleChannelAuthentication(goal);
		}
		if (goal.hasFreshness()) {
			handleChannelFreshness(goal);
		}
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
	public CommunicationTerm visit(CommunicationTerm term) {
		term.getSender().accept(this);
		term.getReceiver().accept(this);
	    // TODO: clean up the below dirty trick, converting the static variable "active" e.g. to recursive argument 
		CommunicationTerm.active = term;
		{
			term.getPayload().accept(this);
			term.visit();
		}
		CommunicationTerm.active = null;
		ChannelEntry type = term.getChannelType();
		if (type.fresh || type.resilient) {
			err.addError(term.getLocation(), ErrorMessages.CHANNEL_MUST_NOT_BE_FRESH_OR_RESILIENT, type.arrow);
		}
		return term;
	}

	@Override
	public ConcatTerm visit(ConcatTerm term) {
		for (ITerm t : term.getTerms()) {
			t.accept(this);
		}
		term.visit();
		return term;
	}

	@Override
	public ConstantTerm visit(ConstantTerm term) {
		term.visit();
		return term;
	}

	@Override
	public DefaultPseudonymTerm visit(DefaultPseudonymTerm term) {
		term.getBaseTerm().accept(this);
		term.visit();
		return term;
	}

	@Override
	public FunctionTerm visit(FunctionTerm term) {
		for (ITerm t : term.getArguments()) {
			t.accept(this);
		}
		term.visit();
		return term;
	}

	@Override
	public ITerm visit(MacroTerm term) {
		// there should be no more macro terms at this point
		term.visit();
		return term;
	}

	@Override
	public PseudonymTerm visit(PseudonymTerm term) {
		term.getBaseTerm().accept(this);
		term.getPseudonym().accept(this);
		term.visit();
		return term;
	}

	@Override
	public SetLiteralTerm visit(SetLiteralTerm term) {
		IScope root = term.getScope().findRoot();
		for (ITerm t : term.getTerms()) {
			t.accept(this);
		}
		String name = term.getNameHint();
		IType elemType = term.getElementsType();
		if(elemType == null) { // may happen for empty set literal
			elemType = root.findType(Prelude.MESSAGE);
		}
		IType setType = Prelude.getSetOf(elemType);
		for (ITerm t : term.getTerms()) {
			if (t.inferType() != null) {
				setType = Prelude.getSetOf(t.inferType());
				break;
			}
		}
		VariableSymbol symbol = term.getScope().findRootEntity().getSetSymbol(name, setType, term.getLocation());
		VariableTerm setTerm = symbol.freshTerm(term.getLocation()); //symbol.term(term.getLocation(), term.getScope());
	/*	FunctionTerm setTerm;
		Entity firstEnt = term.getScope().findFirstEntity();
		if (firstEnt != null) {
			setTerm = symbol.term(term.getLocation(), term.getScope(), firstEnt.getIDSymbol().term(term.getLocation(), term.getScope()));
		}
		else {
			setTerm = symbol.term(term.getLocation(), term.getScope(), spec.getDummyConstant(root.findType(Prelude.NAT)).term(term.getLocation(), term.getScope()));
		}*/
		term.setSymbolNameAndTerm(symbol.getName(), setTerm);
		term.visit();
		return term;
	}

	@Override
	public TupleTerm visit(TupleTerm term) {
		for (ITerm t : term.getTerms()) {
			t.accept(this);
		}
		term.visit();
		return term;
	}

	@Override
	public ITerm visit(VariableTerm term) {
		if (term.isMatched()) {
			String dummyName = term.getScope().getFreshNamesGenerator().getFreshNameNumbered(term.getSymbol().getName(), VariableSymbol.class);
			VariableSymbol dummySymbol = term.getScope().addVariable(dummyName, term.getSymbol().getType(), term.getLocation());
			dummySymbol.setReferenceSymbol(term.getSymbol());
			dummySymbol.setMatchedSymbol(true);
			term.setDummySymbol(dummySymbol);
		}
		term.visit();
		return term;
	}

	@Override
	public UnnamedMatchTerm visit(UnnamedMatchTerm term) {
		term.visit();
		return term;
	}

	public NumericTerm visit(NumericTerm term) {
		term.visit();
		return term;
	}
}
