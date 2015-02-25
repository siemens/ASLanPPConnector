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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;
import org.avantssar.aslan.ASLanSpecificationBuilder;
import org.avantssar.aslan.AttackState;
import org.avantssar.aslan.Constant;
import org.avantssar.aslan.Function;
import org.avantssar.aslan.IASLanSpec;
import org.avantssar.aslan.IRepresentable;
import org.avantssar.aslan.InitialState;
import org.avantssar.aslan.MetaInfo;
import org.avantssar.aslan.Variable;
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
import org.avantssar.aslanpp.model.GenericScope;
import org.avantssar.aslanpp.model.Goal;
import org.avantssar.aslanpp.model.HornClause;
import org.avantssar.aslanpp.model.IExpression;
import org.avantssar.aslanpp.model.IScope;
import org.avantssar.aslanpp.model.IStatement;
import org.avantssar.aslanpp.model.ISymbol;
import org.avantssar.aslanpp.model.ITerm;
import org.avantssar.aslanpp.model.IType;
import org.avantssar.aslanpp.model.ImplicationExpression;
import org.avantssar.aslanpp.model.InequalityExpression;
import org.avantssar.aslanpp.model.IntroduceStatement;
import org.avantssar.aslanpp.model.InvariantGoal;
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
import org.avantssar.aslanpp.model.SymbolsState;
import org.avantssar.aslanpp.model.TupleTerm;
import org.avantssar.aslanpp.model.TupleType;
import org.avantssar.aslanpp.model.UnnamedMatchTerm;
import org.avantssar.aslanpp.model.VariableSymbol;
import org.avantssar.aslanpp.model.VariableTerm;
import org.avantssar.aslanpp.visitors.IASLanPPVisitor;
import org.avantssar.aslanpp.visitors.TypeAssigner;
import org.avantssar.commons.ChannelModel;
import org.avantssar.commons.ErrorGatherer;
import org.avantssar.commons.LocationInfo;
import org.avantssar.commons.TranslatorOptions;
import org.avantssar.commons.ChannelEntry.Type;
import org.avantssar.commons.TranslatorOptions.HornClausesLevel;
import org.avantssar.commons.TranslatorOptions.OptimizationLevel;

public class ASLanBuilder implements IASLanPPVisitor {

	private static class ASLanWrapper<T> {

		public T value;
	}

	private static class ASLanTypeWrapper extends ASLanWrapper<org.avantssar.aslan.IType> {

	}

	private static class ASLanTermWrapper extends ASLanWrapper<org.avantssar.aslan.ITerm> {

	}

	private static final int CONST_MAX_SUCC_NESTING = 9;

	private final String title;
	private final String version;

	private final IASLanSpec aslanSpec;
	private Entity ent;
	private final Map<Entity, INode> firstNodes = new HashMap<Entity, INode>();
	private final Map<Entity, TransitionsRecorder> recorders = new HashMap<Entity, TransitionsRecorder>();
	private final List<INode> independentRules = new ArrayList<INode>();
	private final Stack<INode> nodesStack = new Stack<INode>();
	private final TranslatorOptions options;
	private ChannelModel cm;
	private final Deque<ASLanTypeWrapper> typeWrappers = new ArrayDeque<ASLanTypeWrapper>();
	private final Deque<ASLanTermWrapper> termWrappers = new ArrayDeque<ASLanTermWrapper>();
	private ErrorGatherer err;
	private ASLanPPSpecification spec;

	public ASLanBuilder(TranslatorOptions options, String title, String version) {
		this.title = title;
		this.version = version;

		this.options = options;
		this.aslanSpec = ASLanSpecificationBuilder.instance().createASLanSpecification();
	}

	public IASLanSpec getASLanSpecification() {
		return aslanSpec;
	}

	public void report() {
		System.out.println("We have flow graph for " + firstNodes.size() + " entities");
		for (Entity e : firstNodes.keySet()) {
			System.out.println();
			System.out.println("Flow graph for entity " + e.getOriginalName());
			INode fn = firstNodes.get(e);
			fn.renderGraphviz(System.out);
		}
	}

	public Map<Entity, INode> getFirstNodes() {
		return firstNodes;
	}

	public Map<Entity, TransitionsRecorder> getRecorders() {
		return recorders;
	}

	@Override
	public void visit(ASLanPPSpecification spec) {
		this.spec = spec;
		this.cm = spec.getChannelModel();
		this.err = spec.getErrorGatherer();
		Entity rootEnt = spec.getRootEntity();
		
		// copy verbatim meta-info from aslan++ (as comments)
		for (org.avantssar.aslanpp.model.MetaInfo mi : spec.getMetaInfo()) {
			if (mi.getTag().equals(org.avantssar.aslanpp.model.MetaInfo.VERBATIM)) {
				aslanSpec.addCommentLine(" " + mi.getValue());
			}
		}

		MetaInfo miSpec = aslanSpec.addMetaInfo(MetaInfo.SPECIFICATION);
		miSpec.addFlag(spec.getSpecificationName());
		MetaInfo miChannelModel = aslanSpec.addMetaInfo(MetaInfo.CHANNEL_MODEL);
		miChannelModel.addFlag(spec.getChannelModel().toString());

		// copy modeler meta-info from aslan++
		for (org.avantssar.aslanpp.model.MetaInfo mi : spec.getMetaInfo()) {
			if (mi.getTag().equals(org.avantssar.aslanpp.model.MetaInfo.MODELER)) {
				MetaInfo aslanMI = aslanSpec.addMetaInfo(mi.getTag());
				aslanMI.addFlag(mi.getValue());
			}
		}

		MetaInfo connectorName = aslanSpec.addMetaInfo(MetaInfo.CONNECTOR_NAME);
		connectorName.addFlag(title);
		MetaInfo connectorVersion = aslanSpec.addMetaInfo(MetaInfo.CONNECTOR_VERSION);
		connectorVersion.addFlag(version);
		MetaInfo optionsInfo = aslanSpec.addMetaInfo(MetaInfo.CONNECTOR_OPTIONS);
		StringBuffer opts = new StringBuffer();
		opts.append(TranslatorOptions.OPTIMIZATION_LEVEL).append(" ").append(options.getOptimizationLevel().toString());
		opts.append(" ").append(TranslatorOptions.HORN_CLAUSES_LEVEL).append(" ").append(options.getHornClausesLevel().toString());
		if (options.isGoalsAsAttackStates()) {
			opts.append(" ").append(TranslatorOptions.GOALS_AS_ATTACK_STATES);
		}
		if (options.isStripOutput()) {
			opts.append(" ").append(TranslatorOptions.STRIP_OUTPUT);
		}
		if (options.getOrchestrationClient() != null) {
			opts.append(" ").append(TranslatorOptions.ORCHESTRATION_CLIENT).append(" ").append(options.getOrchestrationClient());
		}
		optionsInfo.addFlag(opts.toString());

		// copy other meta-info from aslan++
		for (org.avantssar.aslanpp.model.MetaInfo mi : spec.getMetaInfo()) {
			if (!mi.getTag().equals(org.avantssar.aslanpp.model.MetaInfo.MODELER) && !mi.getTag().equals(org.avantssar.aslanpp.model.MetaInfo.VERBATIM)) {
				MetaInfo aslanMI = aslanSpec.addMetaInfo(mi.getTag());
				aslanMI.addFlag(mi.getValue());
			}
		}

		visitSpecOrEntity(spec);

		if (rootEnt != null) {
			addIndependentRules(spec, rootEnt);

			Constant cDummyNat = aslanSpec.constant(spec.findType(Prelude.NAT).getDummyName(), IASLanSpec.NAT);

			// initial state
			InitialState init = aslanSpec.initialState("init");
			// true fact
			init.addFact(transform(spec.findConstant(Prelude.TRUE).term()));
			// iknows(0)
			init.addFact(IASLanSpec.IKNOWS.term(aslanSpec.numericTerm(0)));
			// private keys for intruder
			SimpleType tPublicKey = spec.findType(Prelude.PUBLIC_KEY);
			SimpleType tAgent = spec.findType(Prelude.AGENT);
			for (FunctionSymbol f : spec.getEntriesByType(FunctionSymbol.class)) {
				if (f.getType().equals(tPublicKey)) {
					if (f.getArgumentsTypes().size() == 1) {
						if (f.getArgumentsTypes().get(0).equals(tAgent)) {
							Function aslanF = aslanSpec.findFunction(f.getName());
							init.addFact(IASLanSpec.IKNOWS.term(IASLanSpec.INV.term(aslanF.term(IASLanSpec.INTRUDER.term()))));
						}
					}
				}
			}
			// dishonest intruder
			init.addFact(aslanSpec.findFunction(Prelude.DISHONEST).term(IASLanSpec.INTRUDER.term()));
			// state fact for root entity
			StateTerm startState = new StateTerm(rootEnt, true);
			Node n = new Node();
			n.assignIndexes(new Counters(1, 1));
			startState.setStep(n.getStateIndexTerm(rootEnt));
			startState.setActor(spec.findConstant(Prelude.ROOT).term());
			startState.setID(spec.numericTerm(0));
			startState.setRestToDummy(null);
			rootEnt.getStateFunction().accept(this);
			init.addFact(transform(startState));
			// non-private constants
			gatherPublicConstants(spec, init);
			// non-private/non-invertible functions (if HCs are disabled)
			if (options.getHornClausesLevel() != HornClausesLevel.ALL) {
				gatherPublicFunctions(spec, init);
			}
			// isAgent facts
			// init.addFact(aslanSpec.findFunction(Prelude.IS_AGENT).term(IASLanSpec.INTRUDER.term()));
			gatherAgents(rootEnt, init);

			rootEnt.accept(this);

			int idx = 1;
			for (INode in : independentRules) {
				idx = in.assignIndexes(new Counters(1, idx)).edges;
			}
			assignIndexes(rootEnt, idx);
			if (options.getOptimizationLevel() != OptimizationLevel.NONE) {
				optimizeEdges(rootEnt);
				idx = 1;
				for (INode in : independentRules) {
					idx = in.assignIndexes(new Counters(1, idx)).edges;
				}
				assignIndexes(rootEnt, idx);
			}
			for (INode in : independentRules) {
				in.setBigState();
				in.computeBigSmallState(new ArrayList<String>());
			}
			computeBigSmallSteps(rootEnt);
			idx = 1;
			// if (options.getOptimizationLevel() == OptimizationLevel.LUMP) {
			for (INode in : independentRules) {
				in.clearVisited();
				idx = in.gatherTransitionsLumped(aslanSpec, null, new SymbolsState(), new HashSet<Integer>(), idx, in.getNodeIndex(), new TransitionsRecorder());
			}
			gatherTransitions(rootEnt, idx, options.getOptimizationLevel() == OptimizationLevel.LUMP);

			// add meta info to initial state
			MetaInfo startMI = init.addMetaInfo(MetaInfo.NEW_INSTANCE);
			startState.fillMetaInfo(startMI, this);

			// special terms (added by the orchestrator for example)
			for (ITerm t : rootEnt.getForInitialState()) {
				init.addFact(transform(t));
			}
			// child(dummy_nat, 0) in initial state
			Function fncChild = aslanSpec.findFunction(Prelude.CHILD);
			init.addFact(fncChild.term(cDummyNat.term(), aslanSpec.numericTerm(0)));

			// intruder should be generated for orchestration! bug in orchestrator
			if (options.getOrchestrationClient() != null) {
				IASLanSpec.INTRUDER.setPrelude(false);
			}

			aslanSpec.finish();
		}
	}

	private void addIndependentRules(ASLanPPSpecification spec, Entity root) {
		// Horn clause for ACM, if needed
		if (spec.getChannelModel() == ChannelModel.ACM) {
			//IType tAgent = spec.findType(Prelude.AGENT);
			VariableSymbol acmOfficialSender = spec.findVariable("ACM_OS");
			VariableSymbol acmRealSender = spec.findVariable("ACM_RS");
			VariableSymbol acmReceiver = spec.findVariable("ACM_Rcv");
			VariableSymbol acmPayload = spec.findVariable("ACM_Msg");
			VariableSymbol acmCh = spec.findVariable("ACM_Ch");
			ITerm acmLeft = spec.findFunction(Prelude.SENT).term(acmRealSender.term(), acmOfficialSender.term(), acmReceiver.term(), acmPayload.term(), acmCh.term());
			ITerm acmRight = spec.findFunction(Prelude.RCVD).term(acmReceiver.term(), acmOfficialSender.term(), acmPayload.term(), acmCh.term());
			FactToFactEdge acm = new FactToFactEdge(spec, "ACM", acmLeft, acmRight, new ArrayList<VariableSymbol>(), this);
			independentRules.add(acm.getSourceNode());
		}
		// Symbolic instances, if needed
		// if (root.getHasSymbolicInst()) {
		// ConstantSymbol cTrue = spec.findConstant(Prelude.TRUE);
		// VariableSymbol vSymA = root.getSymbolicVar();
		// FunctionSymbol fIsAgent = spec.findFunction(Prelude.IS_AGENT);
		// FunctionSymbol fDishonest = spec.findFunction(Prelude.DISHONEST);
		// List<ITerm> left = new ArrayList<ITerm>();
		// List<ITerm> right = new ArrayList<ITerm>();
		// left.add(cTrue.term());
		// right.add(cTrue.term());
		// right.add(fIsAgent.term(vSymA.freshTerm()));
		// FactToFactEdge siHonest = new FactToFactEdge(spec, "symbolic_1",
		// left, right, Arrays.asList(new VariableSymbol[] { vSymA }), this);
		// independentRules.add(siHonest.getSourceNode());
		//
		// List<ITerm> leftD = new ArrayList<ITerm>();
		// leftD.addAll(left);
		// List<ITerm> rightD = new ArrayList<ITerm>();
		// rightD.addAll(right);
		// rightD.add(fDishonest.term(vSymA.freshTerm()));
		// FactToFactEdge siDishonest = new FactToFactEdge(spec, "symbolic_2",
		// leftD, rightD, Arrays.asList(new VariableSymbol[] { vSymA }), this);
		// independentRules.add(siDishonest.getSourceNode());
		// }
	}

	private void gatherAgents(Entity ent, InitialState init) {
		for (ConstantSymbol c : ent.getEntriesByType(ConstantSymbol.class)) {
			if (ent.findType(Prelude.AGENT).isAssignableFrom(c.getType())) {
				c.accept(this);
				org.avantssar.aslan.ITerm ct = aslanSpec.findConstant(c.getName()).term();
				// init.addFact(aslanSpec.findFunction(Prelude.IS_AGENT).term(ct));
				// if no HCs, add here the knowledge of ak and ck and pk
				if (options.getHornClausesLevel() != HornClausesLevel.ALL) {
					if (cm == ChannelModel.CCM) {
						init.addFact(IASLanSpec.IKNOWS.term(aslanSpec.findFunction(Prelude.AUTHENTICATION_KEY).term(ct)));
						init.addFact(IASLanSpec.IKNOWS.term(aslanSpec.findFunction(Prelude.CONFIDENTIALITY_KEY).term(ct)));
					}
					init.addFact(IASLanSpec.IKNOWS.term(aslanSpec.findFunction(Prelude.PK).term(ct)));
				}
			}
		}
		for (Entity child : ent.getEntriesByType(Entity.class)) {
			gatherAgents(child, init);
		}
	}

	private void gatherPublicConstants(IScope ent, InitialState init) {
		for (ConstantSymbol c : ent.getEntriesByType(ConstantSymbol.class)) {
			if (!c.isNonPublic() && (ent.findType(Prelude.MESSAGE).isAssignableFrom(c.getType()))) {
				c.accept(this);
				init.addFact(IASLanSpec.IKNOWS.term(aslanSpec.findConstant(c.getName()).term()));
			}
		}
		for (Entity child : ent.getEntriesByType(Entity.class)) {
			gatherPublicConstants(child, init);
		}
	}

	private void gatherPublicFunctions(IScope ent, InitialState init) {
		for (FunctionSymbol f : ent.getEntriesByType(FunctionSymbol.class)) {
			if (!f.isPartOfPrelude() && ent.findType(Prelude.MESSAGE).isAssignableFrom(f.getType()) && !f.isNonPublic()) {
				f.accept(this);
				init.addFact(IASLanSpec.IKNOWS.term(aslanSpec.findFunction(f.getName()).constantTerm()));
			}
		}
		for (Entity child : ent.getEntriesByType(Entity.class)) {
			gatherPublicFunctions(child, init);
		}
	}

	private int assignIndexes(Entity ent, int stepIndex) {
		int nextStepIndex = stepIndex;
		INode fn = firstNodes.get(ent);
		if (fn != null) {
			fn.clearVisited();
			Counters outcome = fn.assignIndexes(new Counters(1, nextStepIndex));
			nextStepIndex = outcome.edges;
		}
		for (Entity child : ent.getEntriesByType(Entity.class)) {
			nextStepIndex = assignIndexes(child, nextStepIndex);
		}
		return nextStepIndex;
	}

	private void optimizeEdges(Entity ent) {
		INode fn = firstNodes.get(ent);
		if (fn != null) {
			fn.clearVisited();
			fn = fn.optimize(null);
			firstNodes.put(ent, fn);
		}
		for (Entity child : ent.getEntriesByType(Entity.class)) {
			optimizeEdges(child);
		}
	}

	private void computeBigSmallSteps(Entity ent) {
		List<String> compr = ent.getInheritedCompressed();
		INode fn = firstNodes.get(ent);
		if (fn != null) {
			fn.setBigState();
			fn.computeBigSmallState(compr);
		}
		for (Entity child : ent.getEntriesByType(Entity.class)) {
			computeBigSmallSteps(child);
		}
	}

	private int gatherTransitions(Entity ent, int nextStepIndex, boolean lumped) {
		int idx = nextStepIndex;
		INode fn = firstNodes.get(ent);
		if (fn != null) {
			fn.clearVisited();
			TransitionsRecorder rec = new TransitionsRecorder();
			if (!lumped || ent.isUncompressed()) {
				idx = fn.gatherTransitions(aslanSpec, idx);
			}
			else {
				idx = fn.gatherTransitionsLumped(aslanSpec, null, new SymbolsState(), new HashSet<Integer>(), idx, fn.getNodeIndex(), rec);
			}
			recorders.put(ent, rec);
		}
		for (Entity child : ent.getEntriesByType(Entity.class)) {
			idx = gatherTransitions(child, idx, lumped);
		}
		return idx;
	}

	@Override
	public void visit(Entity ent) {
		Entity oldEnt = this.ent;
		this.ent = ent;
		visitSpecOrEntity(ent);

		for (Entity e : ent.getEntriesByType(Entity.class)) {
			e.accept(this);
		}

		if (ent.getBodyStatement() != null) {
			// this.ent = ent;
			INode firstNode = new Node();
			firstNodes.put(ent, firstNode);
			nodesStack.push(firstNode);
			ent.getBodyStatement().accept(this);
			nodesStack.pop();

			firstNode.assignIndexes(new Counters(1, 1));
			firstNode.clearVisited();
		}

		this.ent = oldEnt;
	}

	private void visitSpecOrEntity(IScope scope) {
		for (SimpleType t : scope.getEntriesByType(SimpleType.class)) {
			if (t.getSuperType() != null) {
				org.avantssar.aslan.PrimitiveType sup = aslanSpec.primitiveType(t.getSuperType().getName());
				aslanSpec.primitiveType(t.getName()).setSuperType(sup);
			}
			else {
				aslanSpec.primitiveType(t.getName());
			}
		}

		for (ISymbol s : scope.getEntriesByType(ISymbol.class)) {
			s.accept(this);
		}

		for (IScope vs : scope.getEntriesByType(IScope.class)) {
			if (!(vs instanceof Entity)) {
				for (ISymbol s : vs.getEntriesByType(ISymbol.class)) {
					s.accept(this);
				}
			}
		}

		for (HornClause cl : scope.getEntriesByType(HornClause.class)) {
			cl.accept(this);
		}

		for (Equation eq : scope.getEntriesByType(Equation.class)) {
			eq.accept(this);
		}

		for (Constraint c : scope.getEntriesByType(Constraint.class)) {
			c.accept(this);
		}
		for (Goal g : scope.getEntriesByType(Goal.class)) {
			g.accept(this);
		}
		for (SessionChannelGoal chGoal : scope.getEntriesByType(SessionChannelGoal.class)) {
			chGoal.accept(this);
		}
		for (SessionSecrecyGoal secrGoal : scope.getEntriesByType(SessionSecrecyGoal.class)) {
			secrGoal.accept(this);
		}
	}

	@Override
	public void visit(SimpleType type) {
		ASLanTypeWrapper tw = currentType();
		tw.value = aslanSpec.primitiveType(type.getName());
	}

	@Override
	public void visit(CompoundType type) {
		if (type.getName() == CompoundType.CONCAT) { // message concatenation has simple type message
		//	TupleType t = new TupleType(type.getArgumentTypes());
			SimpleType t = spec.findType(Prelude.MESSAGE);
			visit(t);
			return;
		}
		ASLanTypeWrapper tw = currentType();
		List<org.avantssar.aslan.IType> argTypes = new ArrayList<org.avantssar.aslan.IType>();
		for (IType t : type.getArgumentTypes()) {
			wrapType();
			t.accept(this);
			ASLanTypeWrapper twInner = unwrapType();
			argTypes.add(twInner.value);
		}
		tw.value = aslanSpec.compoundType(type.getName(), argTypes.toArray(new org.avantssar.aslan.IType[argTypes.size()]));
	}

	@Override
	public void visit(SetType type) {
		ASLanTypeWrapper tw = currentType();
		wrapType();
		type.getBaseType().accept(this);
		ASLanTypeWrapper twInner = unwrapType();
		tw.value = aslanSpec.setType(twInner.value);
	}

	@Override
	public void visit(TupleType type) {
		ASLanTypeWrapper tw = currentType();
		List<org.avantssar.aslan.IType> argTypes = new ArrayList<org.avantssar.aslan.IType>();
		for (IType t : type.getBaseTypes()) {
			wrapType();
			t.accept(this);
			ASLanTypeWrapper twInner = unwrapType();
			argTypes.add(twInner.value);
		}
		tw.value = argTypes.get(argTypes.size() - 1);
		for (int i = argTypes.size() - 2; i >= 0; i--) {
			tw.value = aslanSpec.pairType(argTypes.get(i), tw.value);
		}
	}

	@Override
	public void visit(DeclarationGroup gr) {}

	@Override
	public void visit(VariableSymbol var) {
		// checkType(var.getType());
		Variable v = aslanSpec.variable(var.getName(), transform(var.getType()));
		if (v.getCommentLines().size() == 0) {
			if (var.wasDisambiguated() || var.getReferenceSymbol() != null) {
				MetaInfo mi = v.addMetaInfo(MetaInfo.ORIGINAL_NAME);
				if (var.wasDisambiguated()) {
					mi.addParameter(MetaInfo.NAME, var.getOriginalName());
				}
				else if (var.getReferenceSymbol() != null) {
					mi.addParameter(MetaInfo.NAME, var.getReferenceSymbol().getOriginalName());
				}
				if (var.isFreshSymbol()) {
					mi.addParameter(MetaInfo.FRESH, Boolean.TRUE.toString());
				}
				if (var.isMatchedSymbol()) {
					mi.addParameter(MetaInfo.MATCH, Boolean.TRUE.toString());
				}
			}
		}
	}

	// private void checkType(IType t) {
	// if (aslanSpec.findConstant(t.getDummyName()) == null) {
	// org.avantssar.aslan.IType dummyType = transform(t);
	// aslanSpec.constant(t.getDummyName(), dummyType);
	// }
	// }

	@Override
	public void visit(ConstantSymbol cnst) {
		// checkType(cnst.getType());
		if (!cnst.isPartOfPrelude()) {
			Constant c = aslanSpec.constant(cnst.getName(), transform(cnst.getType()));
			if (c.getCommentLines().size() == 0) {
				if (cnst.wasDisambiguated() || cnst.getReferenceSymbol() != null) {
					MetaInfo mi = c.addMetaInfo(MetaInfo.ORIGINAL_NAME);
					if (cnst.wasDisambiguated()) {
						mi.addParameter(MetaInfo.NAME, cnst.getOriginalName());
					}
					else if (cnst.getReferenceSymbol() != null) {
						mi.addParameter(MetaInfo.NAME, cnst.getReferenceSymbol().getOriginalName());
					}
				}
			}
		}
	}

	@Override
	public void visit(FunctionSymbol fnc) {
		// checkType(fnc.getType());
		List<org.avantssar.aslan.IType> argTypes = new ArrayList<org.avantssar.aslan.IType>();
		for (IType t : fnc.getArgumentsTypes()) {
			// checkType(t);
			argTypes.add(transform(t));
		}
		org.avantssar.aslan.Function aslanFnc = aslanSpec.findFunction(fnc.getName());
		if (fnc.getName().equals(Prelude.CONTAINS)) {
			aslanFnc = aslanSpec.function(fnc.getName(), transform(fnc.getType()), argTypes.get(1), argTypes.get(0));
		}
		else {
			aslanFnc = aslanSpec.function(fnc.getName(), transform(fnc.getType()), argTypes.toArray(new org.avantssar.aslan.IType[argTypes.size()]));
		}
		if (fnc.isPartOfPrelude()) {
			aslanFnc.setPrelude(true);
		}
		if (aslanFnc.getCommentLines().size() == 0 && fnc.wasDisambiguated()) {
			MetaInfo mi = aslanFnc.addMetaInfo(MetaInfo.ORIGINAL_NAME);
			mi.addParameter(MetaInfo.NAME, fnc.getOriginalName());
		}
	}

	@Override
	public void visit(MacroSymbol macro) {}

	@Override
	public void visit(HornClause clause) {
		if (options.getHornClausesLevel() != HornClausesLevel.NONE) {
			org.avantssar.aslan.HornClause hc = aslanSpec.hornClause(clause.getName(), transform(clause.getHead()));
			/* not needed any more due to new syntactic constraints:
			for (org.avantssar.aslan.ITerm t : jointStateContext(clause.getBody(), clause.getEqualities()))
				hc.addBodyFact(t);
			*/
			for (ITerm t : clause.getBody())
				hc.addBodyFact(transform(t));
			for (IExpression e : clause.getEqualities())
				hc.addBodyFact(transform(e));

			if (!options.isStripOutput() && clause.getLocation() != null) {
				MetaInfo locInfo = hc.addMetaInfo(MetaInfo.HORN_CLAUSE);
				locInfo.addParameter(MetaInfo.NAME, clause.getOriginalName());
				locInfo.addParameter(MetaInfo.LINE, Integer.toString(clause.getLocation().line));
				locInfo.addParameter(MetaInfo.COL , Integer.toString(clause.getLocation().col ));
				for (VariableSymbol v : clause.getArguments()) {
					locInfo.addParameter(v.getOriginalName(), v.getName());
				}
			}
		}
	}

  private List<org.avantssar.aslan.ITerm> getStateContext(IExpression e) {
    List<ITerm> ts = new ArrayList<ITerm>();
    //ts.add(ent.getIDSymbol().term()); // "IID", force state_fact for current entity; ### TODO: re-add when INVARIANT_ALWAYS_ACTIVE can be dropped.
    List<IExpression> es = new ArrayList<IExpression>();
    es.add(e);
    List<org.avantssar.aslan.ITerm> terms = jointStateContext(ts, es);
    return terms;
  }

  private List<org.avantssar.aslan.ITerm> jointStateContext(List<ITerm> ts, List<IExpression> es) {
		Set<Entity> addedEntities = new TreeSet<Entity>();
		List<org.avantssar.aslan.ITerm> terms = new ArrayList<org.avantssar.aslan.ITerm>();
		for (ITerm t : ts) {
			ExpressionContext ctx = new ExpressionContext();
			t.buildContext(ctx, false);
			extendByStateContext(ctx, t.toString(), t.getLocation(), terms, addedEntities);
		}
		for (IExpression e : es) {
			ExpressionContext ctx = new ExpressionContext();
			e.buildContext(ctx, false);
			extendByStateContext(ctx, e.toString(), e.getLocation(), terms, addedEntities);
		}
		return terms;
  }

	private void extendByStateContext(ExpressionContext ctx, String termString, LocationInfo location, List<org.avantssar.aslan.ITerm> terms, Set<Entity> addedEntities) {
		List<ITerm> auxiliaryTerms = ctx.getAuxiliaryTerms();
		if (auxiliaryTerms != null) {
			for (ITerm term : auxiliaryTerms) {
				terms.add(transform(term));
			}
		}
		for (VariableSymbol var : ctx.getVariables()) {
			IScope scope = ctx.getOwner(var);
			if (scope instanceof Entity) {
				Entity e = (Entity) scope;
				if (!addedEntities.contains(e)) {
					addedEntities.add(e);
					if (e.equals(ent)) {
						err.addWarning(location, ErrorMessages.LOCAL_VARIABLE_IN_INVARIANT, var.getOriginalName(), termString);
					}						
					else {
						err.addWarning(location, ErrorMessages.INHERITED_VARIABLE_IN_INVARIANT, var.getOriginalName(), e.getOriginalName(), termString);
						/* Note that the child chain is produced even if attack state does
						 * not refer to local variables but only to inherited variables. */
						for (org.avantssar.aslan.ITerm t : e.childChain(aslanSpec, location, ent)) {
							terms.add(t);
						}
					}						

					org.avantssar.aslan.Function stateFnc = aslanSpec.findFunction(e.getStateFunction().getName());
					List<org.avantssar.aslan.VariableTerm> args = new ArrayList<org.avantssar.aslan.VariableTerm>();
					for (VariableSymbol p : e.getStateSymbols()) {
						org.avantssar.aslan.Variable v = aslanSpec.findVariable(p.getName());
						args.add(v.term());
					}
					org.avantssar.aslan.FunctionTerm stateTerm = stateFnc.term(args.toArray(new org.avantssar.aslan.ITerm[args.size()]));

					terms.add(stateTerm);
				}
			}
		}
	}

	@Override
	public void visit(Equation equation) {
		org.avantssar.aslan.ITerm left = transform(equation.getLeftTerm());
		org.avantssar.aslan.ITerm right = transform(equation.getRightTerm());
		aslanSpec.equation(left, right);
	}

	@Override
	public void visit(Constraint constraint) {
		String asName = constraint.getName();
		asName = asName.substring(0, 1).toLowerCase() + asName.substring(1);
		IExpression expandedFormula = constraint.getFormula();
		org.avantssar.aslan.ITerm f = transform(expandedFormula);
		/*org.avantssar.aslan.Constraint cc = */aslanSpec.constraint(asName, f);
		//	fillGoalMetaInfo(cc, constraint, expandedFormula);
	}

	@Override
	public void visit(Goal goal) {
		String asName = goal.getName();
		asName = asName.substring(0, 1).toLowerCase() + asName.substring(1);
		IExpression expandedFormula = goal.getFormula();

		boolean handled = false;
		expandedFormula = expandedFormula.pushDownNegations().dropOuterForall();
		IExpression subFormula = expandedFormula.getGloballySubterm();
		if (options.isGoalsAsAttackStates()) {
			if(subFormula != null) {
				expandedFormula = subFormula.reduceForAttackState();
				if (expandedFormula.canHandleDeMorgan()) {
					IExpression adjustedGoal = expandedFormula.negate().toDNF();
					List<IExpression> conjs = adjustedGoal.getConjunctions();
					for (IExpression conj : conjs) {
						String name;
						if (conjs.size() == 1) {
							name = asName;
						}
						else {
							name = goal.getFreshNamesGenerator().getFreshNameNumbered(asName, Goal.class);
						}
						AttackState as = aslanSpec.attackState(name);
						int count = 0;
						if (goal instanceof InvariantGoal) {
							List<org.avantssar.aslan.ITerm> terms = getStateContext(conj);
							if (terms.isEmpty() && goal.getOwner() != goal.getOwner().findRootEntity()) { // do not warn for goals defined in root entity (Environment)
								String partInfo = (conjs.size() > 1 ? "The \""+conj+"\" (sub-)term of attack state for " : "");
								err.addWarning(conj.getLocation(), ErrorMessages.INVARIANT_ALWAYS_ACTIVE, partInfo, goal.getOriginalName(), ent.getOriginalName());
							}
							for (org.avantssar.aslan.ITerm t : terms) {
								as.addTerm(t);
								count++;
							}
						}
						List<IExpression> es = conj.getAtomicExpressions(true);
						if (count + es.size() <= 1)
							// avoiding singleton negated attack state term, which CL-AtSe and SATMC cannot handle
							as.addTerm(aslanSpec.findConstant(Prelude.TRUE).term()); // "true"
						for (IExpression e : es) {
							as.addTerm(transform(e));
						}
						fillGoalMetaInfo(as, goal, conj);
					}	
					handled = true;
				}
			}
			else {
				err.addWarning(goal.getLocation(), ErrorMessages.GOAL_CANNOT_BE_RENDERED_AS_ATTACK_STATE, goal.getOriginalName());
			}
		}
		if (!handled) {
			if(subFormula == null) {
				err.addWarning(goal.getLocation(), ErrorMessages.GOAL_FORMULA_IS_NOT_GLOBAL, goal.getOriginalName(), goal.getFormula());
			}
			org.avantssar.aslan.ITerm f = transform(expandedFormula);
			if (goal instanceof InvariantGoal) {
				for(org.avantssar.aslan.ITerm t : getStateContext(expandedFormula)) {
					f = IASLanSpec.IMPLIES.term(t,f); //OR IASLanSpec.NOT.term(t)
				}
				// we do not need "forall vars" ... for free variables here, as they will be implicit
			}
			if(options.isGoalsAsAttackStates() && subFormula != null) {
				f = IASLanSpec.LTL_GLOBALLY.term(f); // re-add the dropped outer '[]'
			}
			org.avantssar.aslan.Goal gg = aslanSpec.goal(asName, f);
			fillGoalMetaInfo(gg, goal, expandedFormula);
		}
	}

	private void fillGoalMetaInfo(IRepresentable as, Goal goal, IExpression part) {
		if (!options.isStripOutput() && goal.getLocation() != null) {
			MetaInfo locInfo = as.addMetaInfo(MetaInfo.GOAL);
			locInfo.addParameter(MetaInfo.NAME, goal.getOriginalName());
			locInfo.addParameter(MetaInfo.LINE, Integer.toString(goal.getLocation().line));
			ExpressionContext fullCtx = new ExpressionContext();
			goal.getFormula().buildContext(fullCtx, false);
			ExpressionContext partCtx = new ExpressionContext();
			part.buildContext(partCtx, false);
			for (VariableSymbol fullV : fullCtx.getVariables()) {
				locInfo.addParameter(fullV.getOriginalName(), fullV.getName());
			}
		}
	}

	@Override
	public void visit(SessionChannelGoal chGoal) {
	// do nothing here, it was transformed into a regular goal during post
	// processing
	}

	@Override
	public void visit(SessionSecrecyGoal secrGoal) {
	// do nothing here, it was transformed into a regular goal during post
	// processing
	}

	@Override
	public void visit(AssignmentStatement stmt) {
		if (ent == null || nodesStack.empty()) {
			return;
		}

		IEdge edge = new AssignmentEdge(ent, nodesStack.peek(), stmt.getSymbolTerm(), stmt.getTerm(), false, stmt.getLocation(), this);
		nodesStack.push(edge.getTargetNode());
	}

	@Override
	public void visit(AssertStatement stmt) {
		if (ent == null || nodesStack.empty()) {
			return;
		}

		ArrayList<ITerm> argTerms = new ArrayList<ITerm>();
		argTerms.addAll(stmt.getFirstTerms());
		argTerms.add(ent.getStepSymbol().term());

		FunctionTerm f = stmt.getCheckFunction().term(argTerms.toArray(new ITerm[argTerms.size()]));
		IntroduceRetractEdge introduceEdge = new IntroduceRetractEdge(ent, nodesStack.peek(), f, true, stmt.getLocation(), this);
		IntroduceRetractEdge retractEdge = new IntroduceRetractEdge(ent, introduceEdge.getTargetNode(), f, false, stmt.getLocation(), this);
		retractEdge.makeStandalone();
		nodesStack.push(retractEdge.getTargetNode());
		
		// TODO DvO: why does this code replace the SL node? 
		argTerms.set(argTerms.size() - 1, new NodeTerm(ent, introduceEdge.getTargetNode()));
		f.setArguments(argTerms);
	}

	@Override
	public void visit(BlockStatement stmt) {
		if (ent == null || nodesStack.empty()) {
			return;
		}

		for (IStatement s : stmt.getStatements()) {
			s.accept(this);
		}
	}

	@Override
	public void visit(FreshStatement stmt) {
		if (ent == null || nodesStack.empty()) {
			return;
		}

		IEdge edge = new AssignmentEdge(ent, nodesStack.peek(), stmt.getSymbolTerm(), stmt.getFreshSymbol().freshTerm(stmt.getLocation()), true, stmt.getLocation(), this);
		nodesStack.push(edge.getTargetNode());
	}

	@Override
	public void visit(LoopStatement stmt) {
		if (ent == null || nodesStack.empty()) {
			return;
		}

		INode sourceNode = nodesStack.peek();
		addGuards(sourceNode, stmt.getGuard(), stmt.getLocation(), true, true, null);
		if (stmt.getBody() != null) {
			stmt.getBody().accept(this);
		}
		new EmptyEdge(ent, nodesStack.peek(), sourceNode, stmt.getLocation(), this);
		addGuards(sourceNode, stmt.getGuard(), stmt.getLocation(), false, false, null);
	}

	private void addGuards(INode sourceNode, IExpression guard, LocationInfo location, 
			Boolean positive, Boolean check, SymbolicInstanceStatement stmt) {
		List<IExpression> conjunctions;
		INode targetNode = null;
		if (guard != null) {
			if (!positive) 
				guard = new NegationExpression(guard);
			guard = guard.toDNF();
			conjunctions = guard.getConjunctions();
			for (IExpression disjunct : conjunctions) {
				List<IExpression> conjuncts = disjunct.getAtomicExpressions(true);
				for (IExpression conjunct : conjuncts) {
					if (!(conjunct instanceof NegationExpression)) { // positive occurrence
						Map<VariableSymbol, Boolean> matches = conjunct.getMatchedVariables();
						for (IExpression disjunct2 : conjunctions) { // check inconsistently matched variable assignment
							if (disjunct2 != disjunct) {
								Set<VariableSymbol> positiveMatchedVars = new HashSet<VariableSymbol>();
								List<IExpression> conjuncts2 = disjunct2.getAtomicExpressions(true);
								for (IExpression conjunct2 : conjuncts2) {
									if (!(conjunct2 instanceof NegationExpression)) { // positive occurrence
										positiveMatchedVars.addAll(conjunct2.getMatchedVariables().keySet());
									}
								}
								for (VariableSymbol var : matches.keySet()) {
									if (!positiveMatchedVars.contains(var)) {
										ent.getErrorGatherer().addWarning(disjunct2.getLocation(), 
												ErrorMessages.VARIABLE_NOT_IN_OTHER_DISJUNCT, positive ? "" : "negation of ", guard.getOriginalRepresentation(), 
														var.getOriginalName(), disjunct2.getOriginalRepresentation());
									}
								}
							}
						}
						if (check) { // check quantification problem
							for (IExpression conjunct2 : conjuncts) {
								if (conjunct2 != conjunct) {
									Map<VariableSymbol, Boolean> matches2 = conjunct2.getMatchedVariables();
									for (VariableSymbol var : matches.keySet()) {
										if (matches2.keySet().contains(var)) {
											ent.getErrorGatherer().addException(conjunct2.getLocation(), 
													ErrorMessages.VARIABLE_APPEARS_IN_MORE_THAN_ONE_CONJUNCT, guard.getOriginalRepresentation(), 
													var.getOriginalName(), conjunct.getOriginalRepresentation(), conjunct2.getOriginalRepresentation());
										}
									}
								}
							}
						}
					}
				}
			}
		}
		else {
			conjunctions = new ArrayList<IExpression>();
			conjunctions.add(null);
		}
		for (IExpression g : conjunctions) {
			AbstractEdge edge;
			if (g != null)
				transform(g);
			if (targetNode == null) {
				edge = (stmt == null ? 
						new GuardedEdge(ent, sourceNode, g, location, this)	:
						new SymbolicInstanceEdge(ent, sourceNode, stmt.getEntity(), stmt.getParameters(), 
								stmt.getUniversallyQuantified(), g, stmt.getNewIDSymbol(), 
								stmt.getDummyValues(), stmt.getLocation(), this));
				targetNode = edge.getTargetNode();
			}
			else {
				edge = (stmt == null ? 
						new GuardedEdge(ent, sourceNode, targetNode, g, location, this) :
							new SymbolicInstanceEdge(ent, sourceNode, targetNode, stmt.getEntity(), stmt.getParameters(), 
		            		 	stmt.getUniversallyQuantified(), g, stmt.getNewIDSymbol(), 
		            		 	stmt.getDummyValues(), stmt.getLocation(), this));
			}
			if (guard != null) visitChannelGoals(guard.getChannelGoals(), edge);
		}
		nodesStack.push(targetNode);
	}
	
	private void visitChannelGoals(List<ChannelGoal> goals, IEdge edge) {
		for (ChannelGoal g : goals) {
			visit(g, edge);
		}
	}

	private void addIntroduceEdge(IntroduceStatement stmt, ITerm term) {
		IntroduceRetractEdge edge = 
				new IntroduceRetractEdge(ent, nodesStack.peek(), term, true, stmt.getLocation(), this);
		visitChannelGoals(stmt.getChannelGoals(), edge);
		nodesStack.push(edge.getTargetNode());
	}
	@Override
	public void visit(IntroduceStatement stmt) {
		if (ent == null || nodesStack.empty()) {
			return;
		}

		boolean doIntroduce = true;
		boolean makeStandalone = false;
		ITerm term = stmt.getTerm();
		if (term instanceof CommunicationTerm) {
			CommunicationTerm ct = (CommunicationTerm) term;
			if (ct.isReceive()) {
				doIntroduce = false;
				makeStandalone = true;
			}
			transform(term);
			// term = ct.getProcessedTerm();
			// term = ct;
		}
		// special handling for add/remove for sets
		else if (term instanceof FunctionTerm) {
			FunctionTerm ft = (FunctionTerm) term;
			if (ft.getSymbol().isSetFunction()) {
				if (ft.getSymbol().getOriginalName().equals(Prelude.REMOVE)) {
					doIntroduce = false;
				}
			}
		}
		IntroduceRetractEdge edge;
		// for ACM: expand introduction of bilateral_conf_auth and unilateral_conf_auth
		if (term instanceof FunctionTerm) {
			FunctionTerm ft = (FunctionTerm) term;
			String name = ft.getSymbol().getOriginalName();
			if (name.equals(Prelude.ACM_BILATERAL) || name.equals(Prelude.ACM_UNILATERAL)) {
				IScope root = ent.findRoot();
				List<ITerm> args = ft.getArguments();
				ITerm ch1 = args.get(0); 
				ITerm ch2 = args.get(1); 
				ITerm b;
				FunctionSymbol fnConf = root.findFunction(Prelude.ACM_CONFIDENTIAL_TO);
				FunctionSymbol fnAuth = root.findFunction(Prelude.ACM_AUTHENTIC_ON);
				if (name.equals(Prelude.ACM_BILATERAL)) {
					ITerm a  = args.get(2);
					b  = args.get(3); 
					addIntroduceEdge(stmt, fnAuth.term(ch1,a));
					addIntroduceEdge(stmt, fnConf.term(ch2,a));
				} else {
					FunctionSymbol fnWeakConf = root.findFunction(Prelude.ACM_WEAKLY_CONFIDENTIAL);
					FunctionSymbol fnWeakAuth = root.findFunction(Prelude.ACM_WEAKLY_AUTHENTIC);
					FunctionSymbol fnLink     = root.findFunction(Prelude.ACM_LINK);
					b  = args.get(2); 
					addIntroduceEdge(stmt, fnWeakAuth.term(ch1));
					addIntroduceEdge(stmt, fnWeakConf.term(ch2));
					addIntroduceEdge(stmt, fnLink.term(ch1, ch2));
				}
				addIntroduceEdge(stmt, fnConf.term(ch1,b));
				term = fnAuth.term(ch2,b);
			}
		}
		// if (stmt.getTerm() instanceof CommunicationTerm) {
		// CommunicationTerm ct = (CommunicationTerm) stmt.getTerm();
		// edge = new CommunicationEdge(ent, nodesStack.peek(), ct.getSender(),
		// ct.getReceiver(), ct.getPayload(), ct.getChannelType(), term,
		// doIntroduce, stmt.getLocation(), this);
		// }
		// else {
		edge = new IntroduceRetractEdge(ent, nodesStack.peek(), term, doIntroduce, stmt.getLocation(), this);
		// }
		if (makeStandalone) {
			edge.makeStandalone();
		}
		visitChannelGoals(stmt.getChannelGoals(), edge);
		nodesStack.push(edge.getTargetNode());
	}

	@Override
	public void visit(RetractStatement stmt) {
		if (ent == null || nodesStack.empty()) {
			return;
		}

		IntroduceRetractEdge edge = new IntroduceRetractEdge(ent, nodesStack.peek(), stmt.getTerm(), false, stmt.getLocation(), this);
		nodesStack.push(edge.getTargetNode());
	}

	@Override
	public void visit(SelectStatement stmt) {
		if (ent == null || nodesStack.empty()) {
			return;
		}

		INode sourceNode = nodesStack.peek();
		INode targetNode = null;
		for (IExpression e : stmt.getChoices().keySet()) {
			IStatement s = stmt.getChoices().get(e);
			addGuards(sourceNode, e, e.getLocation(), true, false, null);
			s.accept(this);
			if (targetNode == null) {
				EmptyEdge outOfFirstChoice = new EmptyEdge(ent, nodesStack.peek(), e.getLocation(), this);
				targetNode = outOfFirstChoice.getTargetNode();
			}
			else {
				new EmptyEdge(ent, nodesStack.peek(), targetNode, e.getLocation(), this);
			}
		}
		nodesStack.push(targetNode);
	}

	@Override
	public void visit(BranchStatement stmt) {
		if (ent == null || nodesStack.empty()) {
			return;
		}

		INode sourceNode = nodesStack.peek();
		addGuards(sourceNode, stmt.getGuard(), stmt.getLocation(), true, true, null);
		if (stmt.getTrueBranch() != null) {
			stmt.getTrueBranch().accept(this);
		}
		EmptyEdge outOfTrue = new EmptyEdge(ent, nodesStack.peek(), stmt.getLocation(), this);
		addGuards(sourceNode, stmt.getGuard(), stmt.getLocation(), false, false, null);
		if (stmt.getFalseBranch() != null) {
			stmt.getFalseBranch().accept(this);
		}
		new EmptyEdge(ent, nodesStack.peek(), outOfTrue.getTargetNode(), stmt.getLocation(), this);
		nodesStack.push(outOfTrue.getTargetNode());
	}

	@Override
	public void visit(NewEntityInstanceStatement stmt) {
		if (ent == null || nodesStack.empty()) {
			return;
		}

		INode sourceNode = nodesStack.peek();
		IEdge edge = new NewInstanceEdge(ent, sourceNode, stmt.getEntity(), stmt.getParameters(), stmt.getNewIDSymbol(), stmt.getDummyValues(), stmt.getLocation(), this);
		nodesStack.push(edge.getTargetNode());
	}

	@Override
	public void visit(SymbolicInstanceStatement stmt) {
		if (ent == null || nodesStack.empty()) {
			return;
		}
    addGuards(nodesStack.peek(), stmt.getGuard(), stmt.getLocation(), true, false, stmt);
	}

	@Override
	public void visit(SecrecyGoalStatement stmt) {
		handleSecrecy(stmt.getSecrecyGoalName(), stmt.getOwner().findFirstEntity(), stmt.getAgents(), 
				stmt.getPayload(), stmt.getSetSymbol(), stmt.getSecrecyProtocolName(), null, stmt.getLocation());
	}

	@Override
	public void visit(ChannelGoal goal) {}

	// for old-style inline channel goal
	public void visit(ChannelGoal goal, IEdge edge) {
	  //Entity ent = goal.getOwner().findFirstEntity();
		GenericScope root = ent.findRoot();
		/* check that both parts of ChannelGoal are present and agree on the goal type
		   assuming that inline channel goal names are globally unique */
		boolean foundSender = false;
		boolean foundReceiver = false;
		for (ChannelGoal g : root.chGoals) {
			if (g.getOriginalName().equals(goal.getOriginalName())) {
				foundSender   |= g.getSender  ().holdsActor(); 
				foundReceiver |= g.getReceiver().holdsActor();
				if (g.getType() != goal.getType())
					ent.getErrorGatherer().addError(goal.getLocation(), ErrorMessages.INLINE_CHANNEL_GOAL_TYPE_MISMATCH, g.getLocation());
			}
		}
		if (!foundSender)
			ent.getErrorGatherer().addError(goal.getLocation(), ErrorMessages.INLINE_CHANNEL_GOAL_MISSING_SENDER);
		if (!foundReceiver)
			ent.getErrorGatherer().addError(goal.getLocation(), ErrorMessages.INLINE_CHANNEL_GOAL_MISSING_RECEIVER);
		if (goal.getSender() instanceof UnnamedMatchTerm) {
			ent.getErrorGatherer().addError(goal.getSender().getLocation(), ErrorMessages.MATCHES_CAN_ONLY_BE_USED_IN_GUARDS_AND_RECEIVES, goal.getSender());
			return;
		}
		if (goal.hasUndirectedAuthentication() && goal.hasSecrecy()) {
			ent.getErrorGatherer().addError(goal.getReceiver().getLocation(), ErrorMessages.MATCHES_CAN_ONLY_BE_USED_IN_GUARDS_AND_RECEIVES, goal.getReceiver());
			return;
		}
		if (goal.hasSecrecy()) {
			List<ITerm> knowers = new ArrayList<ITerm>();
			knowers.add(goal.getSender());
			knowers.add(goal.getReceiver());
			FunctionTerm secrecyTerm = handleSecrecy(goal.getOriginalName(), goal.getOwner().findFirstEntity(), knowers, 
					goal.getPayload(), goal.getSetSymbol(), goal.getSecrecyProtocolName(), edge, goal.getLocation());
			Entity session = ent.getOwner().findFirstEntity();
			if (session == null) {
				ent.getErrorGatherer().addError(goal.getLocation(), ErrorMessages.SECRECY_GOAL_IN_UNNESTED_ENTITY);
			}
			else {
				if (goal.getReceiver().holdsActor()) {
					ITerm retractSecr = ent.findRoot().findFunction(Prelude.ADD).term(goal.getSetSymbol().term(session.getIDSymbol().term()), session.findConstant(Prelude.INTRUDER).term());
					secrecyTerm.addSessionGoalTerm(retractSecr);
					// if (edge instanceof IntroduceRetractEdge) {
					// ((IntroduceRetractEdge) edge).addFact(retractSecr, true);
					// }
					// else if (edge instanceof GuardedEdge) {
					// ((GuardedEdge) edge).addFact(retractSecr);
					// }
					// else {
					// err.addError(goal.getLocation(),
					// ErrorMessages.CHANNEL_GOAL_ONLY_AFTER_TRANSMISSION_OR_GUARD);
					// }
				}
			}
		}
		if (goal.hasAuthentication() || goal.hasFreshness()) {
			List<ITerm> toAdd = new ArrayList<ITerm>();
			ConstantSymbol cAuthProt = null;
			if (goal.hasAuthentication()) {
				cAuthProt = goal.getOwner().findConstant(goal.getAuthenticationProtocolName());
			}
			ConstantSymbol cFreshProt = null;
			if (goal.hasFreshness()) {
				cFreshProt = goal.getOwner().findConstant(goal.getFreshnessProtocolName());
			}
			if (goal.getSender().holdsActor()) { // sender side
				if (goal.getReceiver().holdsActor()) {
					err.addError(goal.getLocation(), ErrorMessages.ACTOR_TERM_ON_BOTH_SIDES_OF_CHANNEL_GOAL);
				}
				else {
					if (goal.hasAuthentication()) {
						toAdd.add(root.findFunction(Prelude.WITNESS).term(goal.getSender(), 
									goal.hasUndirectedAuthentication() ? 
										root.findConstant(Prelude.INTRUDER).term() : goal.getReceiver(), 
								  cAuthProt.term(), goal.getPayload()));
					}
/*					if (goal.hasFreshness()) {
						toAdd.add(root.findFunction(Prelude.WITNESS).term(goal.getSender(), goal.getReceiver(), cFreshProt.term(), goal.getPayload()));
					}*/
				}
			}
			else { // receiver side
				if (goal.getReceiver().holdsActor()) {
					if (goal.hasAuthentication()) {
						toAdd.add(root.findFunction(Prelude.REQUEST).term(goal.getReceiver(), goal.getSender(), cAuthProt.term(), goal.getPayload(), ent.getIDSymbol().term()));
					}
					if (goal.hasFreshness()) {
						toAdd.add(root.findFunction(Prelude.REQUEST).term(goal.getReceiver(), goal.getSender(), cFreshProt.term(), goal.getPayload(), ent.getIDSymbol().term()));
					}
				}
				else {
					err.addError(goal.getLocation(), ErrorMessages.ACTOR_TERM_ON_NO_SIDE_OF_CHANNEL_GOAL);
				}

			}
			if (toAdd.size() > 0) {
				if (edge instanceof IntroduceRetractEdge) {
					for (ITerm tt : toAdd) {
						((IntroduceRetractEdge) edge).addFact(tt, true);
					}
				}
				else if (edge instanceof GuardedEdge) {
					for (ITerm tt : toAdd) {
						((GuardedEdge) edge).addFact(tt);
					}
				}
				else {
					err.addError(goal.getLocation(), ErrorMessages.CHANNEL_GOAL_ONLY_AFTER_TRANSMISSION_OR_GUARD);
				}
			}
		}
	}

	private FunctionTerm handleSecrecy(String goalName, Entity child, List<ITerm> knowers, ITerm payload, 
			FunctionSymbol setFunction, String protName, IEdge edge, LocationInfo location) {
		Entity rootEnt = child.findRootEntity();
		IScope root = rootEnt.findRoot();
		//knowers = knowers.toArray(new ITerm[knowers.size()]);

		IScope session = child.getOwner().findFirstEntity();
		ITerm sessionIDterm = null;
		if (session == null) {
			// ent.getErrorGatherer().addException(location,
			// ErrorMessages.SECRECY_GOAL_IN_UNNESTED_ENTITY);
			session = spec;
			ConstantSymbol dummyNat = session.getDummyConstant(session.findType(Prelude.NAT));
			sessionIDterm = dummyNat.term(location, child);
		}
		else {
			sessionIDterm = ((Entity) session).getIDSymbol().term(location, child);
		}

		//TODO maybe generalize to other ancestor entity? Now just giving a warning.
		err.addWarning(location, ErrorMessages.INLINE_SECRECY_GOAL_ASSUMPTION, goalName, session.getOriginalName(), child.getOriginalName());
		
		FunctionTerm secrecyTerm = rootEnt.secrecyTerm(session, sessionIDterm, child, knowers, payload, setFunction, protName, location);

		FunctionSymbol fncChild = root.findFunction(Prelude.CHILD);
		ITerm childTerm = fncChild.term(location, child, sessionIDterm, child.getIDSymbol().term(location, child));
		secrecyTerm.addSessionGoalTerm(childTerm);

		if (edge != null) {
			if (edge instanceof IntroduceRetractEdge) {
				IntroduceRetractEdge irEdge = (IntroduceRetractEdge) edge;
				irEdge.addFact(secrecyTerm, true);
				// irEdge.addFact(childTerm, true);
				// irEdge.addFact(childTerm, false);
			}
			else if (edge instanceof GuardedEdge) {
				GuardedEdge gEdge = (GuardedEdge) edge;
				gEdge.addFact(secrecyTerm);
				// gEdge.addFact(childTerm);
			}
			else {
				err.addError(location, ErrorMessages.CHANNEL_GOAL_ONLY_AFTER_TRANSMISSION_OR_GUARD);
			}
		}
		else {
			IntroduceRetractEdge newEdge = new IntroduceRetractEdge(child, nodesStack.peek(), secrecyTerm, true, location, this);
			// newEdge.addFact(childTerm, true);
			nodesStack.push(newEdge.getTargetNode());
		}

		return secrecyTerm;
	}

	@Override
	public void visit(NegationExpression expr) {
		ASLanTermWrapper tw = currentTerm();
		tw.value = transform(expr.getBaseExpression()).negate();
	}

	@Override
	public void visit(ConjunctionExpression expr) {
		ASLanTermWrapper tw = currentTerm();
		tw.value = IASLanSpec.AND.term(transform(expr.getLeftExpression()), transform(expr.getRightExpression()));
	}

	@Override
	public void visit(DisjunctionExpression expr) {
		ASLanTermWrapper tw = currentTerm();
		tw.value = IASLanSpec.OR.term(transform(expr.getLeftExpression()), transform(expr.getRightExpression()));
	}

	@Override
	public void visit(ExistsExpression expr) {
		ASLanTermWrapper tw = currentTerm();
		tw.value = transform(expr.getBaseExpression());
		for (VariableSymbol v : expr.getSymbols()) {
			tw.value = tw.value.exists(aslanSpec.findVariable(v.getName()));
		}
	}

	@Override
	public void visit(ForallExpression expr) {
		ASLanTermWrapper tw = currentTerm();
		tw.value = transform(expr.getBaseExpression());
		for (VariableSymbol v : expr.getSymbols()) {
			tw.value = tw.value.forall(aslanSpec.findVariable(v.getName()));
		}
	}

	@Override
	public void visit(ImplicationExpression expr) {
		ASLanTermWrapper tw = currentTerm();
		tw.value = transform(expr.unroll());
	}

	@Override
	public void visit(LTLExpression expr) {
		List<org.avantssar.aslan.ITerm> pars = new ArrayList<org.avantssar.aslan.ITerm>();
		for (IExpression e : expr.getChildExpressions()) {
			pars.add(transform(e));
		}
		ASLanTermWrapper tw = currentTerm();
		org.avantssar.aslan.Function fnc = aslanSpec.findFunction(LTLExpression.convertOp(expr.getOperator()));
		tw.value = fnc.term(pars.toArray(new org.avantssar.aslan.ITerm[pars.size()]));
	}

	@Override
	public void visit(EqualityExpression expr) {
		ASLanTermWrapper tw = currentTerm();
		wrapTerm();
		ITerm newLeftTerm = expr.getLeftTerm().accept(this);
		ASLanTermWrapper twInnerLeft = unwrapTerm();
		wrapTerm();
		ITerm newRightTerm = expr.getRightTerm().accept(this);
		ASLanTermWrapper twInnerRight = unwrapTerm();
		tw.value = IASLanSpec.EQUAL.term(twInnerLeft.value, twInnerRight.value);
		expr.setLeftTerm(newLeftTerm);
		expr.setRightTerm(newRightTerm);
	}

	@Override
	public void visit(InequalityExpression expr) {
		ASLanTermWrapper tw = currentTerm();
		wrapTerm();
		ITerm newLeftTerm = expr.getLeftTerm().accept(this);
		ASLanTermWrapper twInnerLeft = unwrapTerm();
		wrapTerm();
		ITerm newRightTerm = expr.getRightTerm().accept(this);
		ASLanTermWrapper twInnerRight = unwrapTerm();
		tw.value = IASLanSpec.EQUAL.term(twInnerLeft.value, twInnerRight.value).negate();
		expr.setLeftTerm(newLeftTerm);
		expr.setRightTerm(newRightTerm);
	}

	@Override
	public void visit(BaseExpression expr) {
		ASLanTermWrapper tw = currentTerm();
		wrapTerm();
		ITerm newTerm = expr.getBaseTerm().accept(this);
		ASLanTermWrapper twInner = unwrapTerm();
		tw.value = twInner.value;
		expr.setBaseTerm(newTerm);
	}

	@Override
	public ITerm visit(CommunicationTerm term) {
		ASLanTermWrapper tw = currentTerm();

		ITerm result = null;
		FunctionSymbol fncIknows = term.getScope().findFunction(Prelude.IKNOWS);
		if (cm.equals(ChannelModel.CCM)) {
			// Start with the payload.
			result = term.getPayload();

			// If authentic or secure, add the receiver.
			if ((term.getType() == Type.Authentic && !term.isUndirected()) ||  
				 term.getType() == Type.Secure) {
				result = ConcatTerm.concat(term.getLocation(), term.getScope(), term.getReceiver(), result);
			}
			// If authentic, confidential or secure, add the proper tag.
			if (term.getType() == Type.Authentic) {
				ConstantSymbol cAtag = term.getScope().findConstant(Prelude.CH_TAG_AUTHENTIC);
				result = ConcatTerm.concat(term.getLocation(), term.getScope(), cAtag.term(), result);
			}
			else if (term.getType() == Type.Confidential) {
				ConstantSymbol cCtag = term.getScope().findConstant(Prelude.CH_TAG_CONFIDENTIAL);
				result = ConcatTerm.concat(term.getLocation(), term.getScope(), cCtag.term(), result);
			}
			else if (term.getType() == Type.Secure) {
				ConstantSymbol cStag = term.getScope().findConstant(Prelude.CH_TAG_SECURE);
				result = ConcatTerm.concat(term.getLocation(), term.getScope(), cStag.term(), result);
			}
			FunctionSymbol fncConf = null;
			boolean pseudoSender = false;
			ITerm senderNym = null;
			if (term.getSender() instanceof PseudonymTerm) {
				pseudoSender = true;
				senderNym = ((PseudonymTerm) term.getSender()).getPseudonym();
			}
			else if (term.getSender() instanceof DefaultPseudonymTerm) {
				pseudoSender = true;
				senderNym = term.getSender();
			}
			if (pseudoSender) {
				fncConf = term.getScope().findFunction(Prelude.PK);
			}
			else {
				fncConf = term.getScope().findFunction(Prelude.CONFIDENTIALITY_KEY);
			}
			// If authentic or secure sign what we have so far.
			if (term.getType() == Type.Authentic || term.getType() == Type.Secure) {
				FunctionSymbol fncSign = term.getScope().findFunction(Prelude.SIGN);
				FunctionSymbol fncInv = term.getScope().findFunction(Prelude.INV);
				ITerm publicKey;
				if (pseudoSender) {
					publicKey = senderNym;
				}
				else {
					FunctionSymbol fncAK = term.getScope().findFunction(Prelude.AUTHENTICATION_KEY);
					publicKey = fncAK.term(term.getSender());
				}
				result = fncSign.term(fncInv.term(publicKey), result);
			}
			// If confidential or secure crypt what we have so far.
			if (term.getType() == Type.Confidential || term.getType() == Type.Secure) {
				FunctionSymbol fncCrypt = term.getScope().findFunction(Prelude.CRYPT);
				ITerm publicKey;
				if (term.getReceiver() instanceof PseudonymTerm) {
					publicKey = ((PseudonymTerm) term.getReceiver()).getPseudonym();
				}
				else if (term.getReceiver() instanceof DefaultPseudonymTerm) {
					publicKey = term.getReceiver();
				}
				// if the receiver is already PK, don't apply pk(...) on it
				// (filter out AGENT, which is subtype of PK)
				else if (!term.getReceiver().inferType().isAssignableFrom(term.getScope().findType(Prelude.PUBLIC_KEY))
						|| term.getReceiver().inferType().isAssignableFrom(term.getScope().findType(Prelude.AGENT))) {
					publicKey = fncConf.term(term.getReceiver());
				}
				else {
					publicKey = term.getReceiver();
				}
				result = fncCrypt.term(publicKey, result);
			}
			// If pseudonymous sender, also send the nym.
			if (pseudoSender && (term.getType() == Type.Authentic || term.getType() == Type.Secure)) {
				result = ConcatTerm.concat(term.getLocation(), term.getScope(), senderNym, result);
			}
			// Send it to the network.
			result = fncIknows.term(result);
		}
		else if (cm.equals(ChannelModel.ACM)) {
			ITerm ch = term.getChannel();
			if (ch == null) {
			/*TODO check if I correctly replaced the following:
			String channelName = term.getChannelType().name;
			if (channelName != null) {
				if (Character.isLowerCase(channelName.charAt(0))) {
					ConstantSymbol channelSymbol = term.getScope().findConstant(channelName);
					ch = channelSymbol.term();
				}
				else {
					VariableSymbol channelSymbol = term.getScope().findVariable(channelName);
					ch = channelSymbol.term();
				}
			}
			else {*/
				String chTag;
				if (!term.isResilient()) {
					if (term.getType() == Type.Authentic) {
						chTag = Prelude.ACM_CH_AUTHENTIC;
					}
					else if (term.getType() == Type.Confidential) {
						chTag = Prelude.ACM_CH_CONFIDENTIAL;
					}
					else if (term.getType() == Type.Secure) {
						chTag = Prelude.ACM_CH_SECURE;
					}
					else {
						chTag = Prelude.ACM_CH_REGULAR;
					}
				}
				else {
					if (term.getType() == Type.Authentic) {
						chTag = Prelude.ACM_CH_RESILIENT_AUTHENTIC;
					}
					else if (term.getType() == Type.Confidential) {
						chTag = Prelude.ACM_CH_RESILIENT_CONFIDENTIAL;
					}
					else if (term.getType() == Type.Secure) {
						chTag = Prelude.ACM_CH_RESILIENT_SECURE;
					}
					else {
						chTag = Prelude.ACM_CH_RESILIENT_REGULAR;
					}
				}
				FunctionSymbol fncChannel = term.getScope().findFunction(Prelude.ACM_CHANNEL);
				ITerm chTagTerm = term.getScope().findConstant(chTag).term();
				ch = fncChannel.term(term.getSender(), term.getReceiver(), chTagTerm);
			}

			if (!term.isReceive()) {
				FunctionSymbol fncSent = term.getScope().findFunction(Prelude.SENT);
				result = fncSent.term(term.getRealSender(), term.getSender(), term.getReceiver(), term.getPayload(), ch);
			}
			else {
				FunctionSymbol fncRcvd = term.getScope().findFunction(Prelude.RCVD);
				result = fncRcvd.term(term.getReceiver(), term.getSender(), term.getPayload(), ch);
			}

		}
		else {
			result = fncIknows.term(term.getPayload());
		}

		if (term.isReceive()) {
			result.setDiscardOnRHS(true);
			result.setExpandedReceive(true);
		}
		term.setProcessedTerm(result);

		// check types for the newly built term.
		TypeAssigner ta = new TypeAssigner(err);
		result.accept(ta);

		tw.value = transform(result);
		return term;
	}

	@Override
	public ITerm visit(ConcatTerm term) {
		ASLanTermWrapper tw = currentTerm();
		tw.value = makePairs(term.getTerms());
		return term;
	}

	@Override
	public ITerm visit(ConstantTerm term) {
		ASLanTermWrapper tw = currentTerm();
		tw.value = aslanSpec.findConstant(term.getSymbol().getName()).term();
		return term;
	}

	@Override
	public ITerm visit(DefaultPseudonymTerm term) {
		ASLanTermWrapper tw = currentTerm();
		tw.value = aslanSpec.findFunction(Prelude.DEFAULT_PSEUDONYM).term(transform(term.getBaseTerm()), transform(term.getScope().findFirstEntity().getIDSymbol().term()));
		return term;
	}

	@Override
	public ITerm visit(FunctionTerm term) {
		org.avantssar.aslan.ITerm[] parameters = transformTermsCollection(term.getArguments());
		ASLanTermWrapper tw = currentTerm();
		FunctionSymbol fnc = term.getSymbol();
		if (fnc.isSetFunction()) {
			tw.value = IASLanSpec.CONTAINS.term(parameters[1], parameters[0]);
		}
		else {
			tw.value = aslanSpec.findFunction(fnc.getName()).term(parameters);
		}
		return term;
	}

	@Override
	public ITerm visit(MacroTerm term) {
		// there should be no macro term left by now
		return term;
	}

	@Override
	public ITerm visit(PseudonymTerm term) {
		ASLanTermWrapper tw = currentTerm();
		tw.value = transform(term.getPseudonym());
		return term;
	}

	@Override
	public ITerm visit(SetLiteralTerm term) {
		ASLanTermWrapper tw = currentTerm();
		// tw.value =
		// aslanSpec.findFunction(term.getSymbol().getName()).term(aslanSpec.findVariable(term.getOwner().findFirstEntity().getIDSymbol().getName()).term());
		tw.value = transform(term.getSetTerm());
		return term;
	}

	@Override
	public ITerm visit(TupleTerm term) {
		ASLanTermWrapper tw = currentTerm();
		tw.value = makePairs(term.getTerms());
		return term;
	}

	@Override
	public ITerm visit(VariableTerm term) {
		ASLanTermWrapper tw = currentTerm();
		if (term.isMatched()) {
			org.avantssar.aslan.Variable var = aslanSpec.findVariable(term.getDummySymbol().getName());
			tw.value = var.term();
		}
		else {
			org.avantssar.aslan.Variable var = aslanSpec.findVariable(term.getSymbol().getName());
			tw.value = var.term(term.getLocation());
		}
		return term;
	}

	@Override
	public ITerm visit(UnnamedMatchTerm term) {
		ASLanTermWrapper tw = currentTerm();
		org.avantssar.aslan.Variable var = aslanSpec.findVariable(term.getDummySymbol().getName());
		tw.value = var.term();
		return term;
	}

	public NumericTerm visit(NumericTerm term) {
		ASLanTermWrapper tw = currentTerm();
		int v = term.getValue();
		if(v > CONST_MAX_SUCC_NESTING) {
			tw.value = aslanSpec.numericTerm(v);
			err.addWarning(term.getLocation(), ErrorMessages.INTEGER_LITERAL_EXCEEDS_MAX_SUCC_NESTING, v, CONST_MAX_SUCC_NESTING);
		}
		else {
			tw.value = aslanSpec.numericTerm(0);
			while(v-- > 0)
				tw.value = aslanSpec.findFunction(Prelude.SUCC).term(tw.value);
		}
		return term;
	}

	public StateTerm visit(StateTerm term) {
		for (ITerm t : term.getParameters()) {
			if (t instanceof ConstantTerm) {
				ConstantSymbol csym = ((ConstantTerm) t).getSymbol();
				if (csym.getName().equals(csym.getType().getDummyName())) {
					if (aslanSpec.findConstant(csym.getType().getDummyName()) == null) {
						org.avantssar.aslan.IType dummyType = transform(csym.getType());
						aslanSpec.constant(csym.getType().getDummyName(), dummyType);
					}
				}
			}
		}
		org.avantssar.aslan.ITerm[] parameters = transformTermsCollection(term.getParameters());
		ASLanTermWrapper tw = currentTerm();
		tw.value = aslanSpec.findFunction(term.getEntity().getStateFunction().getName()).term(parameters);
		return term;
	}

	public NodeTerm visit(NodeTerm term) {
		ASLanTermWrapper tw = currentTerm();
		tw.value = aslanSpec.numericTerm(term.getNode().getNodeIndex());
		return term;
	}

	public org.avantssar.aslan.ITerm transform(IExpression expr) {
		wrapTerm();
		expr.accept(this);
		ASLanTermWrapper twInner = unwrapTerm();
		return twInner.value;
	}

	public org.avantssar.aslan.ITerm makePairs(List<ITerm> terms) {
		org.avantssar.aslan.ITerm[] aslanTerms = transformTermsCollection(terms);
		org.avantssar.aslan.ITerm result = aslanTerms[aslanTerms.length - 1];
		for (int i = aslanTerms.length - 2; i >= 0; i--) {
			result = IASLanSpec.PAIR.term(aslanTerms[i], result);
		}
		return result;
	}

	private org.avantssar.aslan.ITerm[] transformTermsCollection(List<ITerm> terms) {
		List<org.avantssar.aslan.ITerm> aslanTerms = new ArrayList<org.avantssar.aslan.ITerm>();
		for (ITerm t : terms) {
			aslanTerms.add(transform(t));
		}
		return aslanTerms.toArray(new org.avantssar.aslan.ITerm[aslanTerms.size()]);
	}

	public org.avantssar.aslan.ITerm transform(ITerm term) {
		wrapTerm();
		term.accept(this);
		ASLanTermWrapper twInner = unwrapTerm();
		return twInner.value;
	}

	public org.avantssar.aslan.IType transform(IType type) {
		wrapType();
		type.accept(this);
		ASLanTypeWrapper twInner = unwrapType();
		return twInner.value;
	}

	private ASLanTypeWrapper wrapType() {
		ASLanTypeWrapper tw = new ASLanTypeWrapper();
		typeWrappers.push(tw);
		return tw;
	}

	private ASLanTypeWrapper unwrapType() {
		return typeWrappers.pop();
	}

	private ASLanTypeWrapper currentType() {
		return typeWrappers.peek();
	}

	private ASLanTermWrapper wrapTerm() {
		ASLanTermWrapper tw = new ASLanTermWrapper();
		termWrappers.push(tw);
		return tw;
	}

	private ASLanTermWrapper unwrapTerm() {
		return termWrappers.pop();
	}

	private ASLanTermWrapper currentTerm() {
		return termWrappers.peek();
	}

}
