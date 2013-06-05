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

public interface IASLanPPVisitor {

	void visit(ASLanPPSpecification spec);

	void visit(Entity ent);

	void visit(SimpleType type);

	void visit(CompoundType type);

	void visit(SetType type);

	void visit(TupleType type);

	void visit(DeclarationGroup gr);

	void visit(VariableSymbol var);

	void visit(ConstantSymbol cnst);

	void visit(FunctionSymbol fnc);

	void visit(MacroSymbol macro);

	void visit(HornClause clause);

	void visit(Equation equation);

	void visit(Constraint constraint);

	void visit(Goal goal);

	void visit(SessionChannelGoal chGoal);

	void visit(SessionSecrecyGoal secrGoal);

	void visit(AssignmentStatement stmt);

	void visit(AssertStatement stmt);

	void visit(BlockStatement stmt);

	void visit(FreshStatement stmt);

	void visit(LoopStatement stmt);

	void visit(IntroduceStatement stmt);

	void visit(RetractStatement stmt);

	void visit(SelectStatement stmt);

	void visit(BranchStatement stmt);

	void visit(NewEntityInstanceStatement stmt);

	void visit(SymbolicInstanceStatement stmt);

	void visit(SecrecyGoalStatement stmt);

	void visit(ChannelGoal goal);

	void visit(NegationExpression expr);

	void visit(ConjunctionExpression expr);

	void visit(DisjunctionExpression expr);

	void visit(ExistsExpression expr);

	void visit(ForallExpression expr);

	void visit(ImplicationExpression expr);

	void visit(LTLExpression expr);

	void visit(EqualityExpression expr);

	void visit(InequalityExpression expr);

	void visit(BaseExpression expr);

	ITerm visit(CommunicationTerm term);

	ITerm visit(ConcatTerm term);

	ITerm visit(ConstantTerm term);

	ITerm visit(DefaultPseudonymTerm term);

	ITerm visit(FunctionTerm term);

	ITerm visit(MacroTerm term);

	ITerm visit(PseudonymTerm term);

	ITerm visit(SetLiteralTerm term);

	ITerm visit(TupleTerm term);

	ITerm visit(VariableTerm term);

	ITerm visit(UnnamedMatchTerm term);

	ITerm visit(NumericTerm term);
}
