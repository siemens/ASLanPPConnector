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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.antlr.runtime.ANTLRInputStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.CommonTreeNodeStream;
import org.avantssar.aslanpp.ASLanPPNewLexer;
import org.avantssar.aslanpp.ASLanPPNewParser;
import org.avantssar.aslanpp.Debug;
import org.avantssar.aslanpp.SymbolsNew;
import org.avantssar.aslanpp.ToASLanNew;
import org.avantssar.aslanpp.visitors.IASLanPPVisitor;
import org.avantssar.aslanpp.visitors.PrettyPrinter;
import org.avantssar.commons.ChannelEntry;
import org.avantssar.commons.ChannelModel;
import org.avantssar.commons.ErrorGatherer;
import org.avantssar.commons.LocationInfo;

public class Entity extends GenericScope implements IEntityOwner {

	public static final String ACTOR_PREFIX = "Actor";
	public static final String SL_PREFIX = "SL";
	public static final String ID_PREFIX = "IID";

	private final VariableSymbol actorSymbol;
	private final VariableSymbol idSymbol;
	private final VariableSymbol stepSymbol;
	private final List<VariableSymbol> stateSymbols = new ArrayList<VariableSymbol>();
	private final List<VariableSymbol> stateParameters = new ArrayList<VariableSymbol>();

	private final List<String> breakpoints = new ArrayList<String>();
	private boolean breakpointsAdded = false;
	private boolean uncompressed;

	private final List<String> imports = new ArrayList<String>();

	// private boolean hasSymbolicInst = false;
	// private VariableSymbol symbolicVar;

	private final ChannelModel cm;

	private FunctionSymbol stateFnc;

	private IStatement bodyStatement;

	private final List<ITerm> forInitialState = new ArrayList<ITerm>();

	private final EntityManager manager;

	protected static Entity fromFile(EntityManager manager, String name, ChannelModel cm, ErrorGatherer err) {
		Debug.logger.trace("Loading from files an entity called '" + name + "'.");
		try {
			File f = manager.findFileInPath(name);
			if (f != null) {
				FileInputStream fi = new FileInputStream(f);
				return fromStream(manager, fi, cm, err);
			}
			else {
				Debug.logger.info("Entity '" + name + "' cannot be located for import. Did you configure ASLANPATH correctly?");
				throw new RuntimeException("Entity '" + name + "' cannot be located for import. Did you configure ASLANPATH correctly?");
			}
		}
		catch (Exception ex) {
			Debug.logger.error("Exception occured while loading entity '" + name + "'.", ex);
			err.addException(ErrorMessages.ERROR_AT_FILE_IMPORT, name, ex.getMessage());
			return null;
		}
	}

	public static Entity fromStream(EntityManager manager, InputStream is, ChannelModel cm, ErrorGatherer err) {
		try {
			Debug.logger.trace("Loading from stream an entity.");
			ANTLRInputStream input = new ANTLRInputStream(is);
			ASLanPPNewLexer lexer = new ASLanPPNewLexer(input);
			lexer.setErrorGatherer(err);
			CommonTokenStream tokens = new CommonTokenStream(lexer);
			ASLanPPNewParser parser = new ASLanPPNewParser(tokens);
			parser.setErrorGatherer(err);
			ASLanPPSpecification dummy = new ASLanPPSpecification(manager, "dummy", cm);
			ASLanPPNewParser.entityDeclaration_return r = parser.entityDeclaration(dummy);
			Entity ent = r.e;
			if (r.getTree() != null) {
				// By this time the types are registered, so we can run the
				// tree
				// grammar that will register the symbols.
				CommonTree ct = (CommonTree) r.getTree();
				CommonTreeNodeStream nodes = new CommonTreeNodeStream(ct);
				SymbolsNew symb = new SymbolsNew(nodes);
				symb.entity(dummy);
				// Now we can run the tree grammar that will load the
				// expressions and types into the in-memory model.
				nodes.reset();
				ToASLanNew ta = new ToASLanNew(nodes);
				ta.entity(dummy);
			}
			Debug.logger.info("Entity called '" + ent.getName() + "' was successfully loaded from stream.");
			return ent;
		}
		catch (Exception ex) {
			Debug.logger.error("Exception occured while loading entity from stream.", ex);
			err.addException(ErrorMessages.ERROR_AT_IMPORT, ex.getMessage());
			return null;
		}
	}

	protected Entity(EntityManager manager, IEntityOwner owner, String name, ChannelModel cm) {
		super(owner, name);
		if (owner instanceof ASLanPPSpecification) {
			ASLanPPSpecification spec = (ASLanPPSpecification) owner;
			spec.setRootEntity(this);
		}
		this.cm = cm;
		actorSymbol = addStateVariable(ACTOR_PREFIX, owner.findType(Prelude.AGENT));
		idSymbol = addStateVariable(ID_PREFIX, owner.findType(Prelude.NAT));
		stepSymbol = addStateVariable(SL_PREFIX, owner.findType(Prelude.NAT));

		manager.registerEntity(this);
		this.manager = manager;
	}

	public PrettyPrinter toStream(OutputStream out) throws IOException {
		return toStream(out, false);
	}

	public PrettyPrinter toStream(OutputStream out, boolean showInternals) throws IOException {
		PrettyPrinter pp = new PrettyPrinter(showInternals);
		accept(pp);
		out.write(pp.toString().getBytes());
		return pp;
	}

	public PrettyPrinter toFile(String path) throws IOException {
		return toFile(path, false);
	}

	public PrettyPrinter toFile(String path, boolean showInternals) throws IOException {
		FileOutputStream fos = new FileOutputStream(path);
		return toStream(fos, showInternals);
	}

	public void accept(IASLanPPVisitor visitor) {
		visitor.visit(this);
	}

	public void addToInitialState(ITerm t) {
		forInitialState.add(t);
	}

	public List<ITerm> getForInitialState() {
		return forInitialState;
	}

	// public void setHasSymbolicInst(VariableSymbol symbolicVar) {
	// hasSymbolicInst = true;
	// this.symbolicVar = symbolicVar;
	// }
	//
	// public boolean getHasSymbolicInst() {
	// return hasSymbolicInst;
	// }
	//
	// public VariableSymbol getSymbolicVar() {
	// return symbolicVar;
	// }

	@Override
	public boolean participatesForSymbol(ISymbol sym) {
		boolean result = false;
		if (sym.equals(actorSymbol)) {
			result = true;
		}
		else if (sym.equals(idSymbol)) {
			result = true;
		}
		else if (sym.equals(stepSymbol)) {
			result = true;
		}
		else if (stateParameters.contains(sym)) {
			result = true;
		}
		else if (stateSymbols.contains(sym)) {
			result = true;
		}
		return result;
	}

	public void setUncompressed(boolean uncompressed) {
		this.uncompressed = uncompressed;
	}

	public boolean isUncompressed() {
		return uncompressed;
	}

	public boolean wereBreakpointsAdded() {
		return breakpointsAdded;
	}

	public void setBreakpointsAdded(boolean b) {
		breakpointsAdded = b;
	}

	public void addBreakpoints(String... names) {
		for (String s : names) {
			breakpoints.add(s);
		}
		setBreakpointsAdded(true);
	}

	public List<String> getBreakpoints() {
		return breakpoints;
	}

	public List<String> getInheritedCompressed() {
		List<String> compr = getInheritedCompressedEx();
		if (compr.size() == 0) {
			compr = new ArrayList<String>();
			compr.add(Prelude.RECEIVE);
		}
		return compr;
	}

	private List<String> getInheritedCompressedEx() {
		if (breakpoints.size() > 0) {
			return breakpoints;
		}
		else {
			if (getOwner() == null || !(getOwner() instanceof Entity)) {
				return breakpoints;
			}
			else {
				return ((Entity) getOwner()).getInheritedCompressedEx();
			}
		}
	}

	public VariableSymbol getActorSymbol() {
		return actorSymbol;
	}

	public boolean hasActorParameter() {
		return stateParameters.contains(actorSymbol);
	}

	public VariableSymbol getIDSymbol() {
		return idSymbol;
	}

	public VariableSymbol getStepSymbol() {
		return stepSymbol;
	}

	public List<VariableSymbol> getStateSymbols() {
		return stateSymbols;
	}

	public VariableSymbol addParameter(String varName, IType type) {
		return addParameter(varName, type, null);
	}

	public VariableSymbol addParameter(String varName, IType type, LocationInfo location) {
		VariableSymbol sym = null;
		if (varName.equals(Entity.ACTOR_PREFIX)) {
			actorSymbol.changeType(type);
			sym = actorSymbol;
			group(location, sym);
		}
		else {
			sym = addVariable(varName, type, location);
			stateSymbols.add(sym);
		}
		stateParameters.add(sym);
		return sym;
	}

	public VariableSymbol addStateVariable(String name, IType type) {
		return addStateVariable(name, type, null);
	}

	public VariableSymbol addStateVariable(String name, IType type, LocationInfo location) {
		VariableSymbol sym = addVariable(name, type, location);
		if (sym != null) {
			stateSymbols.add(sym);
		}
		return sym;
	}

	public void addImports(String... names) {
		for (String s : names) {
			imports.add(s);
		}
		solveImports(new ArrayDeque<String>());
	}

	public List<String> getImports() {
		return imports;
	}

	private void solveImports(Deque<String> importStack) {
		if (importStack.contains(getOriginalName())) {
			StringBuffer sb = new StringBuffer();
			Iterator<String> iter = importStack.descendingIterator();
			boolean first = true;
			while (iter.hasNext()) {
				if (!first) {
					sb.append(" -> ");
				}
				sb.append(iter.next());
				first = false;
			}
			sb.append(" -> ");
			sb.append(getOriginalName());
			getErrorGatherer().addException(ErrorMessages.CIRCULAR_DEPENDENCY_DURING_IMPORT, sb.toString());
			return;
		}
		importStack.push(getOriginalName());
		List<String> alreadyImported = new ArrayList<String>();
		for (int i = 0; i < imports.size(); i++) {
			String entityName = imports.get(i);
			Entity ent = manager.getEntity(entityName, cm);
			ent.solveImports(importStack);
			if (!alreadyImported.contains(ent.getName())) {
				mergeSectionsFrom(ent);
				alreadyImported.add(ent.getName());
			}
		}
	}

	private void mergeSectionsFrom(Entity ent) {
		Debug.logger.debug("Merging sections from " + ent.getName() + " into " + getName() + ".");

		for (IOwned entry : ent.getAllEntries()) {
			entry.transferTo(this);
		}
	}

	public <T extends IStatement> T body(T stmt) {
		bodyStatement = stmt;
		return stmt;
	}

	public IStatement getBodyStatement() {
		return bodyStatement;
	}

	public BlockStatement block() {
		return block(null);
	}

	public BlockStatement block(LocationInfo location) {
		return new BlockStatement(location);
	}

	public AssignmentStatement assign(ITerm var, ITerm term) {
		return assign(null, var, term);
	}

	public AssignmentStatement assign(LocationInfo location, ITerm var, ITerm term) {
		if (var instanceof VariableTerm) {
			return new AssignmentStatement(location, (VariableTerm) var, term);
		}
		else {
			getErrorGatherer().addException(location, ErrorMessages.VARIABLE_EXPECTED_IN_ASSIGNMENT_OR_FRESH, "assignment", var.getRepresentation());
			return null;
		}
	}

	public FreshStatement fresh(ITerm var) {
		return fresh(null, var);
	}

	public FreshStatement fresh(LocationInfo location, ITerm var) {
		if (var instanceof VariableTerm) {
			return new FreshStatement(location, (VariableTerm) var);
		}
		else {
			getErrorGatherer().addException(location, ErrorMessages.VARIABLE_EXPECTED_IN_ASSIGNMENT_OR_FRESH, "fresh", var.getRepresentation());
			return null;
		}
	}

	public LoopStatement loop(IExpression guard) {
		return loop(null, guard);
	}

	public LoopStatement loop(LocationInfo location, IExpression guard) {
		return new LoopStatement(location, guard);
	}

	public SelectStatement select() {
		return select(null);
	}

	public SelectStatement select(LocationInfo location) {
		return new SelectStatement(location);
	}

	public BranchStatement branch(IExpression guard) {
		return branch(null, guard);
	}

	public BranchStatement branch(LocationInfo location, IExpression guard) {
		return new BranchStatement(location, guard);
	}

	public IntroduceStatement introduce(ITerm term) {
		return introduce(null, term);
	}

	public IntroduceStatement introduce(LocationInfo location, ITerm term) {
		return new IntroduceStatement(location, term);
	}

	public RetractStatement retract(ITerm term) {
		return retract(null, term);
	}

	public RetractStatement retract(LocationInfo location, ITerm term) {
		return new RetractStatement(location, term);
	}

	public SecrecyGoalStatement secrecyGoal(String name, ITerm payload, ITerm... knowers) {
		return secrecyGoal(null, name, payload, knowers);
	}

	public SecrecyGoalStatement secrecyGoal(LocationInfo location, String name, ITerm payload, ITerm... knowers) {
		return new SecrecyGoalStatement(location, this, name, payload, knowers);
	}

	public Equation equation() {
		return equation(null);
	}

	public Equation equation(LocationInfo location) {
		return new Equation(this, location);
	}

	private void checkExistingGoal(LocationInfo location, String name) {
		IOwned existing = getEntryMultipleTypes(name, Goal.class, InvariantGoal.class, SessionChannelGoal.class, SessionSecrecyGoal.class);
		if (existing != null) {
			getErrorGatherer().addError(location, ErrorMessages.DUPLICATE_SYMBOL_IN_SCOPE, "goal", name, getName());
		}
		if (this.getOwner() != null) {
			existing = this.getOwner().getEntryMultipleTypes(name, Goal.class, InvariantGoal.class, SessionChannelGoal.class, SessionSecrecyGoal.class);
			if (existing != null) {
				getErrorGatherer().addWarning(location, ErrorMessages.REDEFINING_SYMBOL_OF_SCOPE, "goal", name, existing.getOwner().getName());
			}
		}
	}

	public Goal goal(String name) {
		return goal(null, name);
	}

	public Goal goal(LocationInfo location, String name) {
		/* checkExistingGoal(location, name); TODO maybe re-add after improving PostProcessor.handleSecrecy */
		return new Goal(location, this, name);
	}

	public Constraint constraint(LocationInfo location, String name) {
		checkDuplicate(name, Constraint.class, "constraint", location);
		return new Constraint(location, this, name);
	}

	public InvariantGoal invariantGoal(LocationInfo location, String name) {
		checkExistingGoal(location, name);
		return new InvariantGoal(location, this, name);
	}

	public SessionChannelGoal sessionChannelGoal(String name, ITerm sender, ITerm receiver, String chType) {
		return sessionChannelGoal(null, name, sender, receiver, chType);
	}

	public SessionChannelGoal sessionChannelGoal(LocationInfo location, String name, ITerm sender, ITerm receiver, String chType) {
		checkExistingGoal(location, name);
		ChannelEntry type = ChannelEntry.getByKey(chType, false);
		if (type == null) {
			getErrorGatherer().addException(location, ErrorMessages.INVALID_CHANNEL_ARROW, chType);
			return null;
		}
		else {
			return new SessionChannelGoal(location, this, name, sender, receiver, type);
		}
	}

	public SessionSecrecyGoal sessionSecrecyGoal(String name, ITerm agents) {
		return sessionSecrecyGoal(null, name, agents);
	}

	public SessionSecrecyGoal sessionSecrecyGoal(LocationInfo location, String name, ITerm agents) {
		checkExistingGoal(location, name);
		if (agents instanceof SetLiteralTerm) {
			SetLiteralTerm sa = (SetLiteralTerm) agents;
			return new SessionSecrecyGoal(location, this, name, sa.getTerms());
		}
		else {
			getErrorGatherer().addException(location, ErrorMessages.INVALID_SECRECY_GOAL, agents.getRepresentation());
			return null;
		}
	}

	private void instantiationArguments(String goalType, String goalName, 
			Boolean[] usedInEveryInst, ITerm[] instArgs, ITerm[] mapped, ITerm target, IStatement stmt) {
		if (stmt != null) {
			Entity subEnt = null;
			List<ITerm> args = new ArrayList<ITerm>(); 
			if (stmt instanceof BlockStatement) {
				BlockStatement blk = (BlockStatement) stmt;
				for (IStatement ss : blk.getStatements()) {
					//PrettyPrinter pp = new PrettyPrinter();
					//ss.accept(pp);
					// System.out.print("checking statement: " + pp.toString());
					instantiationArguments(goalType, goalName, usedInEveryInst, instArgs, mapped, target, ss);
				}
			}
			else if (stmt instanceof SelectStatement) {
				SelectStatement s = (SelectStatement) stmt;
				Iterator<IStatement> i = s.getChoices().values().iterator();
				while (i.hasNext()) {
					instantiationArguments(goalType, goalName, usedInEveryInst, instArgs, mapped, target, i.next());
				}
			}
			else if (stmt instanceof BranchStatement) {
				BranchStatement s = (BranchStatement) stmt;
				instantiationArguments(goalType, goalName, usedInEveryInst, instArgs, mapped, target, s.getTrueBranch());
				instantiationArguments(goalType, goalName, usedInEveryInst, instArgs, mapped, target, s.getFalseBranch());
			}
			else if (stmt instanceof LoopStatement) {
				LoopStatement s = (LoopStatement) stmt;
				instantiationArguments(goalType, goalName, usedInEveryInst, instArgs, mapped, target, s.getBody());
			}
			else if (stmt instanceof NewEntityInstanceStatement) {
				NewEntityInstanceStatement neStmt = (NewEntityInstanceStatement) stmt;
				subEnt = neStmt.getEntity();
				args = neStmt.getParameters();
			}
			else if (stmt instanceof SymbolicInstanceStatement) {
				SymbolicInstanceStatement siStmt = (SymbolicInstanceStatement) stmt;
				subEnt = siStmt.getEntity();
				args = siStmt.getParameters();
			}
			if(subEnt != null) {
				boolean foundInSubEntArgs = false;
				for (int i = 0; i < args.size(); i++) {
					ITerm arg = args.get(i);
					if (arg != null && subEnt.equals(this)) {
						if (target != null && arg.equals(target)) {
							if (foundInSubEntArgs) {
								getErrorGatherer().addWarning(arg.getLocation(), 
										ErrorMessages.SESSION_GOAL_TERM_USED_MORE_THAN_ONCE, 
										arg.getRepresentation(), goalType, goalName, this.getOriginalName());
							}
							foundInSubEntArgs = true;
							if (instArgs[i] != null && !instArgs[i].equals(arg)) { // TODO can this happen at all? Only when target changes.
								getErrorGatherer().addWarning(instArgs[i].getLocation(), ErrorMessages.SESSION_GOAL_TERM_USED_INCONSISTENTLY, 
										instArgs[i].getRepresentation(), goalType, goalName, this.getOriginalName());									
							}
							if (instArgs[i] == null)
								instArgs[i] = arg;
							if (mapped[0] == null) // main output is the very first use of target
								mapped[0] = this.getParameters().get(i).term();
						}
						else {
							usedInEveryInst[i] = false;
						}
					}
					else if (arg != null && !subEnt.equals(this) && 
							target != null &&  arg.equals(target)) {
						foundInSubEntArgs = true;
						instantiationArguments(goalType, goalName, usedInEveryInst, instArgs, mapped, 
								subEnt.getParameters().get(i).term(), subEnt.bodyStatement);
					}
				}
				if (!foundInSubEntArgs) {
					instantiationArguments(goalType, goalName, usedInEveryInst, instArgs, mapped, null, subEnt.bodyStatement);
				}
			}
		}
	}
	
	/*
	 * Given an "target" term, finds out under which argument the term is passed to a "child" entity.
	 */
	protected ITerm mapArgTerm(boolean channel_goal, String goalName, ITerm target, Entity child) {
		// System.out.println("mapping symbol: " + target.getName() +
		// " from entity " + getOriginalName() + " to entity " +
		// child.getOriginalName());
		if(child.equals(this)) {
			return target; // trivial case: in same entity
		}
		if(channel_goal && target instanceof UnnamedMatchTerm) { // '?' allowed for channel goal
			return null;
		}
		Boolean[] usedInEveryInst = new Boolean[child.getParametersCount()];
		ITerm  [] instArgs        = new ITerm  [child.getParametersCount()];
		ITerm  [] mapped          = new ITerm  [1]; // output parameter of instantiationArguments
		for (int i = 0; i < child.getParametersCount(); i++) {
			usedInEveryInst[i] = true;
			instArgs[i] = null; // used to construct union for all instantiations of child entity
		}
		String goalType = channel_goal ? "channel" : "secrecy"; 
		child.instantiationArguments(goalType, goalName, usedInEveryInst, instArgs, mapped, target, bodyStatement);
		
		if (mapped[0] == null) {
			getErrorGatherer().addError(
					target.getLocation(), ErrorMessages.SESSION_GOAL_TERM_NOT_FOUND,
					target, goalType, goalName, child.getOriginalName(), getOriginalName());
		}
		else {
			for (int i = 0; i < child.getParametersCount(); i++) {
				if (instArgs[i] != null) {
					if (!usedInEveryInst[i]) {
						getErrorGatherer().addWarning(target.getLocation(), 
								ErrorMessages.SESSION_GOAL_TERM_NOT_USED_EVERY_INST,
								target, goalType, goalName, child.getOriginalName(), getOriginalName(), mapped[0]);
					}
				}
			}
		}
		return mapped[0];
	}

	public void buildDummyValues(Map<VariableSymbol, ConstantSymbol> dummyValues) {
		GenericScope root = findRoot();
		for (VariableSymbol sym : getStateSymbols()) {
			if (!getParameters().contains(sym)) {
				if (!sym.equals(getStepSymbol()) && !sym.equals(getIDSymbol())) {
					boolean unique = sym.equals(getActorSymbol());
					String freshDummyName = !unique ? sym.getType().getDummyName() :
					  getFreshNamesGenerator().getFreshNameNumbered(sym.getType().getDummyName(), ConstantSymbol.class);
					ConstantSymbol freshDummy = root.getEntry(freshDummyName, ConstantSymbol.class);
					if (unique || freshDummy == null) {
						freshDummy = (unique ? this : root).constants(sym.getType(), freshDummyName);
						freshDummy.setNonPublic(true);
					}
					dummyValues.put(sym, freshDummy);
				}
			}
		}
	}

	public NewEntityInstanceStatement newInstance(Entity ent, ITerm... args) {
		return newInstance(null, ent, args);
	}

	public NewEntityInstanceStatement newInstance(LocationInfo location, Entity ent, ITerm... args) {
		return new NewEntityInstanceStatement(location, ent, args);
	}

	public Entity entity(String name) {
		return new Entity(manager, this, name, cm);
	}

	public IntroduceStatement comm(ITerm sender, ITerm receiver, ITerm payload, ITerm channel, ChannelEntry chType, boolean receive, boolean renderAsFunction, boolean renderOOPStyle) {
		return comm(null, sender, receiver, payload, channel, chType, receive, renderAsFunction, renderOOPStyle);
	}

	public IntroduceStatement comm(LocationInfo location, ITerm sender, ITerm receiver, ITerm payload, ITerm channel, ChannelEntry chType, boolean receive, boolean renderAsFunction, boolean renderOOPStyle) {
		CommunicationTerm ct = communication(location, sender, receiver, payload, channel, chType, receive, renderAsFunction, renderOOPStyle);
		return new IntroduceStatement(location, ct);
	}

	public SymbolicInstanceStatement symbolicInstance(Entity ent) {
		return symbolicInstance(null, ent);
	}

	public SymbolicInstanceStatement symbolicInstance(LocationInfo location, Entity ent) {
		return new SymbolicInstanceStatement(location, this, ent);
	}

	public AssertStatement assertion(String name) {
		return assertion(null, name);
	}

	public AssertStatement assertion(LocationInfo location, String name) {
		return new AssertStatement(location, this, name);
	}

	public List<VariableSymbol> getParameters() {
		return stateParameters;
	}

	public int getParametersCount() {
		return stateParameters.size();
	}

	public FunctionSymbol getStateFunction() {
		return stateFnc;
	}

	public void setStateFunction(FunctionSymbol stateFnc) {
		this.stateFnc = stateFnc;
	}

	public boolean hasAncestor(Entity ancestor) {
		if (equals(ancestor)) {
			return true;
		}
		else {
			if (getOwner() == null || !(getOwner() instanceof Entity)) {
				return false;
			}
			else {
				return ((Entity) getOwner()).hasAncestor(ancestor);
			}
		}
	}

	public FunctionSymbol getSetFunction(String name, IType setType, LocationInfo location) {
		if (name == null) {
			name = getFreshNamesGenerator().getFreshNameNumbered("set", ISymbol.class);
		}
		if (setType == null) {
			setType = Prelude.getSetOf(findType(Prelude.MESSAGE));
		}
		Entity root = findRootEntity();
		List<IType> argTypes = new ArrayList<IType>();
		argTypes.add(findType(Prelude.NAT));
		FunctionSymbol symbol = root.findFunction(name);
		if (symbol != null) {
			if (!symbol.respectsSignature(setType, argTypes)) {
				String newName = getFreshNamesGenerator().getFreshName(name, FunctionSymbol.class);
				getErrorGatherer().addWarning(location, ErrorMessages.SET_SYMBOL_NAME_ALREADY_IN_USE, name, newName);
				name = newName;
			}
		}
		else {
			symbol = root.addFunction(name, setType, argTypes, location);
			symbol.setNonPublic(true);
			symbol.setNonInvertible(true);
			symbol.setInternalUse(true);
		}
		return symbol;
	}

	public VariableSymbol getSetSymbol(String name, IType setType, LocationInfo location) {
		if (name == null) {
			name = getFreshNamesGenerator().getFreshNameNumbered("Set", ISymbol.class);
		}
		if (setType == null) {
			setType = Prelude.getSetOf(findType(Prelude.MESSAGE));
		}
		// TODO: ugly to have these globalized set names; is this unavoidable because of their type declarations?
		GenericScope root = findRoot();
		VariableSymbol symbol = root.findVariable(name);
		if (symbol == null) {
			symbol = root.addVariable(name, setType, location);
		}
		return symbol;
	}

	public FunctionTerm secrecyTerm(IScope session, ITerm sessionIDterm, Entity child, 
			List<ITerm> knowers, ITerm payload, FunctionSymbol setSymbol, String protName, LocationInfo location) {
		IScope root = findRoot();
		ConstantSymbol cProt = findConstant(protName);
		if (cProt == null) {
			cProt = constants(root.findType(Prelude.PROTOCOL_ID), protName);
		}
		SetLiteralTerm knowersSet = new SetLiteralTerm(location, session, knowers, setSymbol.getOriginalName());
		knowersSet.setSymbolNameAndTerm(setSymbol.getName(), setSymbol.term(sessionIDterm));
		return root.findFunction(Prelude.SECRET).term(location, child, payload, cProt.term(location, child), knowersSet);
	}

	public List<ITerm> childChain(LocationInfo location, Entity child) { // this = ancestor
		ArrayList<ITerm> result = new ArrayList<ITerm>();
		while (!child.equals(this)) {
			Entity parent = child.getOwner().findFirstEntity();
			result.add(findRoot().findFunction(Prelude.CHILD).term(location, this,
					parent.getIDSymbol().term(location, this), 
					child .getIDSymbol().term(location, this)));
			child = parent;
		}
		return result;
	}
	
	public List<org.avantssar.aslan.ITerm> childChain(org.avantssar.aslan.IASLanSpec spec, LocationInfo location, Entity child) { // this = ancestor
		ArrayList<org.avantssar.aslan.ITerm> result = new ArrayList<org.avantssar.aslan.ITerm>();
		while (!child.equals(this)) {
			Entity parent = child.getOwner().findFirstEntity();
			result.add(spec.findFunction(Prelude.CHILD).term(
					spec.findVariable(parent.getIDSymbol().getName()).term(), 
					spec.findVariable(child .getIDSymbol().getName()).term()));
			child = parent;
		}
		return result;
	}
}
