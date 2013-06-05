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

import java.util.List;
import org.avantssar.commons.ChannelEntry;
import org.avantssar.commons.ErrorGatherer;
import org.avantssar.commons.LocationInfo;

public interface IScope extends IOwned {

	List<IOwned> getAllEntries();

	<T extends IOwned> List<T> getEntriesByType(Class<T> cls);

	<T extends IOwned> List<T> getEntriesByType(Class<T> cls, boolean skipTransfered);

	void removeEntry(IOwned entry);

	void storeEntry(IOwned symbol);

	<T extends IOwned> boolean isNameClash(String name, Class<T> type);

	<T extends IOwned> T getEntry(String name, Class<T> type);

	IOwned getEntryMultipleTypes(String name, Class<?>... types);

	<T extends IOwned> T getEntryInHierarchy(String name, Class<T> type);

	IOwned getEntryInHierarchyMultipleTypes(String name, Class<?>... types);

	Entity findFirstEntity();

	IScope findRoot();

	Entity findRootEntity();

	Entity findEntity(String name);

	MacroSymbol findMacro(String name);

	VariableSymbol findVariable(String name);

	ConstantSymbol findConstant(String name);

	FunctionSymbol findFunction(String name);

	IOwned findConstantOrMacro(String name);

	IOwned findFunctionOrMacro(String name);

	List<DeclarationGroup> getDeclarationGroups();

	VariableSymbol addVariable(String name, IType type);

	VariableSymbol addVariable(String name, IType type, LocationInfo location);

	VariableSymbol addUntypedVariable(String name);

	VariableSymbol addUntypedVariable(String name, LocationInfo location);

	ConstantSymbol constants(IType type, String name);

	ConstantSymbol constants(LocationInfo location, IType type, String name);

	FunctionSymbol addFunction(String name, IType retType, IType... argTypes);

	FunctionSymbol addFunction(String name, IType retType, List<IType> argTypes, LocationInfo location);

	NumericTerm numericTerm(int value);

	NumericTerm numericTerm(int value, LocationInfo location);

	MacroSymbol addMacro(LocationInfo location, String name);

	HornClause hornClause(LocationInfo location, String name);

	HornClause hornClause(String name);

	UnnamedMatchTerm unnamedMatch();

	UnnamedMatchTerm unnamedMatch(LocationInfo location);

//	CommunicationTerm communication(ITerm sender, ITerm receiver, ITerm payload, ITerm channel, ChannelEntry chType, boolean receive, boolean renderAsFunction, boolean renderOOPStyle);

	CommunicationTerm communication(LocationInfo location, ITerm sender, ITerm receiver, ITerm payload, ITerm channel, ChannelEntry chType, boolean receive, boolean renderAsFunction, boolean renderOOPStyle);

	ChannelGoal chGoal(String name, CommunicationTerm term);

	ChannelGoal chGoal(LocationInfo location, String name, CommunicationTerm term);

	ChannelGoal chGoal(String name, ITerm sender, ITerm receiver, ITerm payload, ChannelEntry type);

	ChannelGoal chGoal(LocationInfo location, String name, ITerm sender, ITerm receiver, ITerm payload, ChannelEntry type);

	boolean participatesForSymbol(ISymbol symbol);

	public void purge();

	ConstantSymbol getDummyConstant(IType type);

	SimpleType findType(String name);

	IType findCompoundType(String name);

	SimpleType type(String name);

	SimpleType type(LocationInfo location, String name);

	SimpleType type(String name, SimpleType superType);

	SimpleType type(LocationInfo location, String name, SimpleType superType);

	void group(ISymbol... symbols);

	FreshNameGenerator getFreshNamesGenerator();

	ErrorGatherer getErrorGatherer();

}
