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
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.avantssar.aslanpp.Debug;
import org.avantssar.commons.ChannelEntry;
import org.avantssar.commons.ErrorGatherer;
import org.avantssar.commons.LocationInfo;

public class GenericScope extends AbstractOwned implements IScope {

	// We can't make this a map, because the symbols may have their names
	// changed, and then the association in the map would become obsolete.
	private final Set<IOwned> symbols = new LinkedHashSet<IOwned>();

	private final List<DeclarationGroup> declarationGroups = new ArrayList<DeclarationGroup>();

	public List<ChannelGoal> chGoals = new ArrayList<ChannelGoal>();

	private FreshNameGenerator freshGen;

	public GenericScope(IScope owner, String name) {
		super(owner, name);
		if (owner != null) {
			freshGen = owner.getFreshNamesGenerator();
		}
		else {
			freshGen = new FreshNameGenerator(this);
		}
	}

	public FreshNameGenerator getFreshNamesGenerator() {
		return freshGen;
	}

	public ErrorGatherer getErrorGatherer() {
		if (getOwner() != null) {
			ErrorGatherer fromUp = getOwner().getErrorGatherer();
			if (fromUp != null) {
				return fromUp;
			}
		}
		// this line should never be called, it's just a safe fallback
		return new ErrorGatherer(ErrorMessages.DEFAULT);
	}

	public void purge() {
		symbols.clear();
	}

	public List<IOwned> getAllEntries() {
		List<IOwned> entries = new ArrayList<IOwned>();
		for (IOwned entry : symbols) {
			entries.add(entry);
		}
		return entries;
	}

	public void removeEntry(IOwned entry) {
		symbols.remove(entry);
	}

	public void storeEntry(IOwned symbol) {
		for (IOwned existing : symbols) {
			if (existing == symbol) {
				Debug.logger.warn("Symbol '" + symbol.getName() + "' added more than once to scope '" + symbol.getOwner().getName() + "'.");
				return;
			}
		}
		symbols.add(symbol);
	}

	public void listEntries() {
		List<IScope> scopes = new ArrayList<IScope>();
		scopes.add(this);
		while (scopes.size() > 0) {
			IScope currentScope = scopes.remove(0);
			System.out.println(currentScope.getName() + " --> " + currentScope.getOwner());
			List<IOwned> entries = currentScope.getAllEntries();
			for (IOwned entry : entries) {
				System.out.println("\t" + entry.getName() + " --> " + entry.getClass().getCanonicalName());
				if (entry instanceof IScope) {
					scopes.add((IScope) entry);
				}
			}
		}
	}

	private <T extends IOwned> T checkNameDeep(String name, Class<T> type) {
		List<IScope> scopes = new ArrayList<IScope>();
		scopes.add(this);
		T entry = null;
		while (entry == null && scopes.size() > 0) {
			IScope currentScope = scopes.remove(0);
			for (T e : currentScope.getEntriesByType(type)) {
				if (filterName(e.getName()).equals(filterName(name))) {
					entry = e;
					break;
				}
			}
			// entry = currentScope.getEntry(name, type);
			if (entry == null) {
				for (IOwned childrenScope : currentScope.getAllEntries()) {
					if (childrenScope instanceof IScope) {
						scopes.add((IScope) childrenScope);
					}
				}
			}
		}
		return entry;
	}

	private String filterName(String name) {
		// map the name to ASLan
		return name.replaceAll("[^a-zA-Z0-9_]", "_");
	}

	public GenericScope findRoot() {
		if (getOwner() == null) {
			return this;
		}
		else {
			return ((GenericScope) getOwner()).findRoot();
		}
	}

	public <T extends IOwned> boolean isNameClash(String name, Class<T> type) {
		IOwned entry = findRoot().checkNameDeep(name, type);
		return entry != null;
	}

	@SuppressWarnings("unchecked")
	public <T extends IOwned> T getEntry(String name, Class<T> type) {
		for (IOwned existingSymbol : symbols) {
			if (existingSymbol.getOriginalName().equals(name)) {
				if (type.equals(existingSymbol.getClass())) {
					return (T) existingSymbol;
				}
			}
		}
		return null;
	}

	public IOwned getEntryMultipleTypes(String name, Class<?>... types) {
		for (IOwned existingSymbol : symbols) {
			if (existingSymbol.getOriginalName().equals(name)) {
				for (Class<?> c : types) {
					if (c.equals(existingSymbol.getClass())) {
						return existingSymbol;
					}
				}
			}
		}
		return null;
	}

	public <T extends IOwned> T getEntryInHierarchy(String name, Class<T> type) {
		T here = getEntry(name, type);
		if (here != null) {
			return here;
		}
		else if (getOwner() != null) {
			return getOwner().getEntryInHierarchy(name, type);
		}
		else {
			return null;
		}
	}

	public SimpleType findType(String name) {
		return getEntryInHierarchy(name, SimpleType.class);
	}

	public IType findCompoundType(String name) {
		FunctionSymbol fnc = checkNameDeep(name, FunctionSymbol.class);
		if (fnc != null) {
			return fnc.getType();
		}
		else {
			return null;
		}
	}

	public Entity findFirstEntity() {
		if (this instanceof Entity) {
			return (Entity) this;
		}
		else if (this.getOwner() != null) {
			return this.getOwner().findFirstEntity();
		}
		else {
			return null;
		}
	}

	public Entity findRootEntity() {
		if (getOwner() != null) {
			Entity fromUp = getOwner().findRootEntity();
			if (fromUp != null) {
				return fromUp;
			}
		}
		if (this instanceof Entity) {
			return (Entity) this;
		}
		else {
			return null;
		}
	}

	public Entity findEntity(String name) {
		return getEntryInHierarchy(name, Entity.class);
	}

	public MacroSymbol findMacro(String name) {
		return getEntryInHierarchy(name, MacroSymbol.class);
	}

	public VariableSymbol findVariable(String name) {
		return getEntryInHierarchy(name, VariableSymbol.class);
	}

	public ConstantSymbol findConstant(String name) {
		return getEntryInHierarchy(name, ConstantSymbol.class);
	}

	public FunctionSymbol findFunction(String name) {
		return getEntryInHierarchy(name, FunctionSymbol.class);
	}

	public IOwned getEntryInHierarchyMultipleTypes(String name, Class<?>... types) {
		IOwned here = getEntryMultipleTypes(name, types);
		if (here != null) {
			return here;
		}
		else if (this.getOwner() != null) {
			return this.getOwner().getEntryInHierarchyMultipleTypes(name, types);
		}
		else {
			return null;
		}
	}

	public IOwned findConstantOrMacro(String name) {
		return getEntryInHierarchyMultipleTypes(name, ConstantSymbol.class, MacroSymbol.class);
	}

	public IOwned findFunctionOrMacro(String name) {
		return getEntryInHierarchyMultipleTypes(name, FunctionSymbol.class, MacroSymbol.class);
	}

	protected <T extends IOwned> void checkDuplicate(String name, Class<T> type, String typeString, LocationInfo location) {	
		if (getEntry(name, type) != null) {
			getErrorGatherer().addError(location, ErrorMessages.DUPLICATE_SYMBOL_IN_SCOPE, typeString, name, getName());
			//return null;
		}
		if (location != null && // only check for user-defined symbols
			this.getOwner() != null && type != VariableSymbol.class) {
			T existing = this.getOwner().getEntry(name, type);
			if (existing != null) {
				getErrorGatherer().addWarning(location, ErrorMessages.REDEFINING_SYMBOL_OF_SCOPE, typeString, name, existing.getOwner().getName());
			}
		}
	}

	public VariableSymbol addVariable(String name, IType type) {
		return addVariable(name, type, null);
	}

	public VariableSymbol addVariable(String name, IType type, LocationInfo location) {
		return addVariableEx(name, type, location);
	}

	public VariableSymbol addUntypedVariable(String name) {
		return addUntypedVariable(name, null);
	}

	public VariableSymbol addUntypedVariable(String name, LocationInfo location) {
		return addVariableEx(name, null, location);
	}

	private VariableSymbol addVariableEx(String name, IType type, LocationInfo location) {
		checkDuplicate(name, VariableSymbol.class, "variable", location);
		VariableSymbol sym;
		if (type != null) {
			sym = new VariableSymbol(this, location, name, type);
		}
		else {
			sym = new VariableSymbol(this, location, name);
		}
		group(location, sym);
		return sym;
	}

	public ConstantSymbol constants(IType type, String name) {
		return constants(null, type, name);
	}

	public ConstantSymbol constants(LocationInfo location, IType type, String name) {
		return addConstant(location, type, name);
	}

	private ConstantSymbol addConstant(LocationInfo location, IType type, String name) {
		checkDuplicate(name, ConstantSymbol.class, "constant", location);
		ConstantSymbol sym = new ConstantSymbol(this, location, name, type);
		group(location, sym);
		return sym;
	}

	public FunctionSymbol addFunction(String name, IType retType, IType... argTypes) {
		return addFunction(name, retType, argTypes, null);
	}

	public FunctionSymbol addFunction(String name, IType retType, IType[] argTypes, LocationInfo location) {
		return addFunction(name, retType, Arrays.asList(argTypes), location);
	}

	public FunctionSymbol addFunction(String name, IType retType, List<IType> argTypes) {
		return addFunction(name, retType, argTypes, null);
	}

	public FunctionSymbol addFunction(String name, IType retType, List<IType> argTypes, LocationInfo location) {
		checkDuplicate(name, FunctionSymbol.class, "function", location);
		IType[] at = new IType[argTypes.size()];
		FunctionSymbol sym = new FunctionSymbol(this, location, name, retType, argTypes.toArray(at));
		group(location, sym);
		return sym;
	}

	public NumericTerm numericTerm(int value) {
		return numericTerm(value, null);
	}

	public NumericTerm numericTerm(int value, LocationInfo location) {
		return new NumericTerm(location, this, value);
	}

	public MacroSymbol addMacro(LocationInfo location, String name) {
		checkDuplicate(name, MacroSymbol.class, "macro", location);
		return new MacroSymbol(location, this, name);
	}

	public HornClause hornClause(String name) {
		return hornClause(null, name);
	}

	public HornClause hornClause(LocationInfo location, String name) {
		checkDuplicate(name, HornClause.class, "clause", location);
		return new HornClause(this, location, name);
	}

	public boolean participatesForSymbol(ISymbol symbol) {
		return false;
	}

	@Override
	public String toString() {
		return getName() + " [" + getOriginalName() + "]";
	}

	@Override
	public ConstantSymbol getDummyConstant(IType type) {
		if (getOwner() != null) {
			return getOwner().getDummyConstant(type);
		}
		else {
			ConstantSymbol dummyConstant = findConstant(type.getDummyName());
			if (dummyConstant == null) {
				dummyConstant = constants(type, type.getDummyName());
				dummyConstant.setNonPublic(true);
			}
			return dummyConstant;
		}
	}

	@Override
	public UnnamedMatchTerm unnamedMatch() {
		return unnamedMatch(null);
	}

	@Override
	public UnnamedMatchTerm unnamedMatch(LocationInfo location) {
		return new UnnamedMatchTerm(location, this);
	}

	public <T extends IOwned> List<T> getEntriesByType(Class<T> cls) {
		return getEntriesByType(cls, false);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends IOwned> List<T> getEntriesByType(Class<T> cls, boolean skipTransfered) {
		List<T> result = new ArrayList<T>();
		for (IOwned entry : symbols) {
			if (cls.isAssignableFrom(entry.getClass())) {
				if (!entry.wasTransfered() || !skipTransfered) {
					result.add((T) entry);
				}
			}
		}
		return result;
	}

	@Override
	public SimpleType type(String name) {
		return type(null, name);
	}

	public SimpleType type(LocationInfo location, String name) {
		return new SimpleType(location, this, name, false);
	}

	@Override
	public SimpleType type(String name, SimpleType superType) {
		return type(null, name, superType);
	}

	public SimpleType type(LocationInfo location, String name, SimpleType superType) {
		return new SimpleType(location, this, name, false, superType);
	}

	@Override
	public void group(ISymbol... symbols) {
		group(LocationInfo.NOWHERE, symbols);
	}
	public void group(LocationInfo location, ISymbol... symbols) {
		if (symbols.length == 0) {
			return;
		}
		// check category, types, non public and non invertible to be the same
		Class<?> cat = symbols[0].getClass();
		IType refType = symbols[0].getType();
		boolean refNonPublic = false;
		boolean refNonInvertible = false;
		if (cat.equals(ConstantSymbol.class)) {
			ConstantSymbol cs = (ConstantSymbol) symbols[0];
			refNonPublic = cs.isNonPublic();
		}
		else if (cat.equals(FunctionSymbol.class)) {
			FunctionSymbol fs = (FunctionSymbol) symbols[0];
			refNonPublic = fs.isNonPublic();
			refNonInvertible = fs.isNonInvertible();
		}
		for (int i = 1; i < symbols.length; i++) {
			if (!cat.equals(symbols[i].getClass())) {
				getErrorGatherer().addException(location, ErrorMessages.CANNOT_GROUP_SYMBOLS_DUE_TO_CATEGORIES);
			}
			if (!refType.equals(symbols[i].getType())) {
				getErrorGatherer().addException(location, ErrorMessages.CANNOT_GROUP_SYMBOLS_DUE_TO_TYPES);
			}
			if (symbols[i].getClass().equals(ConstantSymbol.class)) {
				ConstantSymbol cs = (ConstantSymbol) symbols[i];
				if (refNonPublic != cs.isNonPublic()) {
					getErrorGatherer().addError(location, ErrorMessages.CANNOT_GROUP_SYMBOLS_DUE_TO_NON_PUBLIC);
				}
			}
			else if (symbols[i].getClass().equals(FunctionSymbol.class)) {
				FunctionSymbol fs = (FunctionSymbol) symbols[i];
				if (refNonPublic != fs.isNonPublic()) {
					getErrorGatherer().addError(location, ErrorMessages.CANNOT_GROUP_SYMBOLS_DUE_TO_NON_PUBLIC);
				}
				if (refNonInvertible != fs.isNonInvertible()) {
					getErrorGatherer().addError(location, ErrorMessages.CANNOT_GROUP_SYMBOLS_DUE_TO_NON_INVERTIBLE);
				}
			}
		}
		// remove from any other groups
		for (ISymbol sym : symbols) {
			for (DeclarationGroup gr : declarationGroups) {
				if (gr.contains(sym)) {
					gr.remove(sym);
				}
			}
		}
		// remove empty groups
		List<DeclarationGroup> toRemove = new ArrayList<DeclarationGroup>();
		for (DeclarationGroup gr : declarationGroups) {
			if (gr.size() == 0) {
				toRemove.add(gr);
			}
		}
		declarationGroups.removeAll(toRemove);
		// create new group
		DeclarationGroup newGroup = new DeclarationGroup();
		for (ISymbol sym : symbols) {
			newGroup.add(sym);
		}
		declarationGroups.add(newGroup);
	}

	public List<DeclarationGroup> getDeclarationGroups() {
		return declarationGroups;
	}

	public CommunicationTerm communication(ITerm sender, ITerm receiver, ITerm payload, ITerm channel, ChannelEntry chType, boolean receive, boolean renderAsFunction, boolean renderOOPStyle) {
		return communication(null, sender, receiver, payload, channel, chType, receive, renderAsFunction, renderOOPStyle);
	}

	public CommunicationTerm communication(LocationInfo location, ITerm sender, ITerm receiver, ITerm payload, ITerm channel, ChannelEntry chType, boolean receive, boolean renderAsFunction, boolean renderOOPStyle) {
		return new CommunicationTerm(location, this, sender, receiver, payload, channel, chType, receive, renderAsFunction, renderOOPStyle);
	}

	public ChannelGoal chGoal(String name, CommunicationTerm term) {
		return chGoal(null, name, term);
	}

	public ChannelGoal chGoal(LocationInfo location, String name, CommunicationTerm term) {
		return chGoal(location, name, term.getSender(), term.getReceiver(), term.getPayload(), term.getChannelType());
	}

	public ChannelGoal chGoal(String name, ITerm sender, ITerm receiver, ITerm payload, ChannelEntry type) {
		return chGoal(null, name, sender, receiver, payload, type);
	}

	public ChannelGoal chGoal(LocationInfo location, String name, ITerm sender, ITerm receiver, ITerm payload, ChannelEntry type) {
		ChannelGoal g = new ChannelGoal(this, location, name, sender, receiver, payload, type);
		findRoot().chGoals.add(g); // used in ASLanBuilder to check that both parts of ChannelGoal are present and agree on the goal type
		return g;
	}

}
