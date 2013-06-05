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

import java.util.ArrayList;
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
import org.avantssar.aslanpp.model.ExistsExpression;
import org.avantssar.aslanpp.model.ForallExpression;
import org.avantssar.aslanpp.model.FreshStatement;
import org.avantssar.aslanpp.model.FunctionSymbol;
import org.avantssar.aslanpp.model.FunctionTerm;
import org.avantssar.aslanpp.model.Goal;
import org.avantssar.aslanpp.model.HornClause;
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
import org.avantssar.aslanpp.visitors.IASLanPPVisitor;
import org.avantssar.commons.ChannelModel;
import org.avantssar.commons.Term;

public class Orchestrator implements IASLanPPVisitor {

	public static final String ORCHESTRATION_GOAL = "OrchestrationGoal";
	private static final String ORCHESTRATION_CLIENT = "OrchestrationClient";
	public static final String START_ORCHESTRATION = "start_orchestration";
	private static final String END_ORCHESTRATION = "end_orchestration";
	private static final String GOAL_AGENT = "goal_agent";
	private static final String ORCHESTRATION_FINAL_STATE = "orchestrationFinalState";

	private final String orchestrationClient;

	public Orchestrator(String orchestrationClient) {
		this.orchestrationClient = orchestrationClient;
	}

	@Override
	public void visit(ASLanPPSpecification spec) {
		if (spec.getRootEntity() != null) {
			// Add constants for start/end orchestration.
			ConstantSymbol orchStart = spec.constants(spec.findType(Prelude.MESSAGE), START_ORCHESTRATION);
			orchStart.setNonPublic(true);
			ConstantSymbol orchEnd = spec.constants(spec.findType(Prelude.MESSAGE), END_ORCHESTRATION);
			orchEnd.setNonPublic(true);
			// goal agent
			ConstantSymbol goalAgent = spec.constants(spec.findType(Prelude.AGENT), GOAL_AGENT);

			// build up the types for the state_OrchestrationGoal predicate
			// and also the values sent to it.
			List<IType> ogTypes = new ArrayList<IType>();
			List<ITerm> ogTerms = new ArrayList<ITerm>();
			// goal agent
			ogTypes.add(spec.findType(Prelude.AGENT));
			ogTerms.add(goalAgent.term());
			// start_orchestration
			ogTypes.add(spec.findType(Prelude.MESSAGE));
			ogTerms.add(orchStart.term());
			// for CCM atag, ctag and stag
			if (spec.getChannelModel() == ChannelModel.CCM) {
				ogTypes.add(spec.findConstant(Prelude.CH_TAG_AUTHENTIC).getType());
				ogTerms.add(spec.findConstant(Prelude.CH_TAG_AUTHENTIC).term());
				ogTypes.add(spec.findConstant(Prelude.CH_TAG_CONFIDENTIAL).getType());
				ogTerms.add(spec.findConstant(Prelude.CH_TAG_CONFIDENTIAL).term());
				ogTypes.add(spec.findConstant(Prelude.CH_TAG_SECURE).getType());
				ogTerms.add(spec.findConstant(Prelude.CH_TAG_SECURE).term());
			}
			// all agents
			List<ISymbol> agents = new ArrayList<ISymbol>();
			gatherAgents(spec.getRootEntity(), agents);
			for (ISymbol sym : agents) {
				if (sym instanceof ConstantSymbol) {
					ConstantSymbol ag = (ConstantSymbol) sym;
					// skip protocol ids
					if (!ag.getType().equals(spec.findType(Prelude.PROTOCOL_ID))) {
						if (spec.findType(Prelude.AGENT).isAssignableFrom(ag.getType())) {
							ogTypes.add(ag.getType());
							ogTerms.add(ag.term());
							ITerm ak = spec.findFunction(Prelude.AUTHENTICATION_KEY).term(ag.term());
							ogTypes.add(ak.inferType());
							ogTerms.add(ak);
							ITerm ck = spec.findFunction(Prelude.CONFIDENTIALITY_KEY).term(ag.term());
							ogTypes.add(ck.inferType());
							ogTerms.add(ck);
							ITerm pk = spec.findFunction(Prelude.PK).term(ag.term());
							ogTypes.add(pk.inferType());
							ogTerms.add(pk);
							// ITerm invAK =
							// spec.findFunction(Prelude.INV).term(ak);
							// ogTypes.add(invAK.inferType());
							// ogTerms.add(invAK);
							// ITerm invCK =
							// spec.findFunction(Prelude.INV).term(ck);
							// ogTypes.add(invCK.inferType());
							// ogTerms.add(invCK);
							// ITerm invPK =
							// spec.findFunction(Prelude.INV).term(pk);
							// ogTypes.add(invPK.inferType());
							// ogTerms.add(invPK);
						}
						else if (!ag.isNonPublic()) {
							ogTypes.add(ag.getType());
							ogTerms.add(ag.term());
						}
					}
				}
			}
			// intruder
			ConstantSymbol intr = spec.findConstant(Prelude.INTRUDER);
			// ogTypes.add(intr.getType());
			// ogTerms.add(intr.term());
			ITerm ak = spec.findFunction(Prelude.AUTHENTICATION_KEY).term(intr.term());
			ogTypes.add(ak.inferType());
			ogTerms.add(ak);
			ITerm ck = spec.findFunction(Prelude.CONFIDENTIALITY_KEY).term(intr.term());
			ogTypes.add(ck.inferType());
			ogTerms.add(ck);
			ITerm pk = spec.findFunction(Prelude.PK).term(intr.term());
			ogTypes.add(pk.inferType());
			ogTerms.add(pk);
			ITerm invAK = spec.findFunction(Prelude.INV).term(ak);
			ogTypes.add(invAK.inferType());
			ogTerms.add(invAK);
			ITerm invCK = spec.findFunction(Prelude.INV).term(ck);
			ogTypes.add(invCK.inferType());
			ogTerms.add(invCK);
			ITerm invPK = spec.findFunction(Prelude.INV).term(pk);
			ogTypes.add(invPK.inferType());
			ogTerms.add(invPK);

			// Add dummy function to emulate orchestration goal.
			FunctionSymbol orchGoal = spec.addFunction(Term.STATE_PREFIX + "_" + ORCHESTRATION_GOAL, spec.findType(Prelude.FACT), ogTypes.toArray(new IType[ogTypes.size()]));

			// Add term for initial state, to start the orchestration goal
			FunctionTerm startOrchestrationGoal = orchGoal.term(ogTerms.toArray(new ITerm[ogTerms.size()]));
			spec.getRootEntity().addToInitialState(startOrchestrationGoal);

			// Visit the entities, to locate the orchestration client and to
			// remove all goals.
			spec.getRootEntity().accept(this);

			// Add goal for end orchestration
			Goal orchFinalState = spec.getRootEntity().goal(ORCHESTRATION_FINAL_STATE);
			orchFinalState.setFormula(new LTLExpression(LTLExpression.GLOBALLY, spec.findFunction(Prelude.IKNOWS).term(orchEnd.term()).expression().not()));
		}
	}

	private void gatherAgents(Entity ent, List<ISymbol> agents) {
		for (ConstantSymbol c : ent.getEntriesByType(ConstantSymbol.class)) {
			// if (ent.findType(Prelude.AGENT).isAssignableFrom(c.getType())) {
			agents.add(c);
			// }
		}
		for (FunctionSymbol f : ent.getEntriesByType(FunctionSymbol.class)) {
			agents.add(f);
		}
		for (Entity child : ent.getEntriesByType(Entity.class)) {
			gatherAgents(child, agents);
		}
	}

	@Override
	public void visit(Entity ent) {
		if (ent.getOriginalName().equals(orchestrationClient)) {
			ent.removeEntry(ent.getStateFunction());
			ent.setPseudonym(ORCHESTRATION_CLIENT);
			// re-generate state function
			List<IType> argTypes = new ArrayList<IType>();
			argTypes.add(ent.getActorSymbol().getType());
			argTypes.add(ent.getIDSymbol().getType());
			argTypes.add(ent.getStepSymbol().getType());
			for (VariableSymbol var : ent.getStateSymbols()) {
				if (!skip(ent, var)) {
					argTypes.add(var.getType());
				}
			}
			FunctionSymbol fstate = ent.addFunction(Term.STATE_PREFIX + "_" + ent.getName(), ent.findType(Prelude.FACT), argTypes.toArray(new IType[argTypes.size()]));
			fstate.setNonInvertible(true);
			fstate.setNonPublic(true);
			ent.setStateFunction(fstate);

			BlockStatement bd = null;
			if (ent.getBodyStatement() == null) {
				bd = ent.body(ent.block());
			}
			else if (ent.getBodyStatement() instanceof BlockStatement) {
				bd = (BlockStatement) ent.getBodyStatement();
			}
			else {
				bd = ent.block();
				bd.add(ent.getBodyStatement());
			}
			RetractStatement first = ent.retract(ent.findFunction(Prelude.IKNOWS).term(ent.findConstant(START_ORCHESTRATION).term()));
			IntroduceStatement last = ent.introduce(ent.findFunction(Prelude.IKNOWS).term(ent.findConstant(END_ORCHESTRATION).term()));
			bd.getStatements().add(0, first);
			bd.getStatements().add(last);
		}

		for (Entity child : ent.getEntriesByType(Entity.class)) {
			child.accept(this);
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
	public void visit(NegationExpression expr) {}

	@Override
	public void visit(ConjunctionExpression expr) {}

	@Override
	public void visit(DisjunctionExpression expr) {}

	@Override
	public void visit(ExistsExpression expr) {}

	@Override
	public void visit(ForallExpression expr) {}

	@Override
	public void visit(ImplicationExpression expr) {}

	@Override
	public void visit(LTLExpression expr) {}

	@Override
	public void visit(EqualityExpression expr) {}

	@Override
	public void visit(InequalityExpression expr) {}

	@Override
	public void visit(BaseExpression expr) {}

	@Override
	public ITerm visit(CommunicationTerm term) {
		return term;
	}

	@Override
	public ITerm visit(ConcatTerm term) {
		return term;
	}

	@Override
	public ITerm visit(ConstantTerm term) {
		return term;
	}

	@Override
	public ITerm visit(DefaultPseudonymTerm term) {
		return term;
	}

	@Override
	public ITerm visit(FunctionTerm term) {
		return term;
	}

	@Override
	public ITerm visit(MacroTerm term) {
		return term;
	}

	@Override
	public ITerm visit(PseudonymTerm term) {
		return term;
	}

	@Override
	public ITerm visit(SetLiteralTerm term) {
		return term;
	}

	@Override
	public ITerm visit(TupleTerm term) {
		return term;
	}

	@Override
	public ITerm visit(VariableTerm term) {
		return term;
	}

	@Override
	public ITerm visit(UnnamedMatchTerm term) {
		return term;
	}

	public NumericTerm visit(NumericTerm term) {
		return term;
	}

}
