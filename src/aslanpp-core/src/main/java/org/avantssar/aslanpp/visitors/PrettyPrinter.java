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

import java.io.FileNotFoundException;
import java.io.PrintStream;
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
import org.avantssar.aslanpp.model.IExpression;
import org.avantssar.aslanpp.model.INamed;
import org.avantssar.aslanpp.model.IScope;
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
import org.avantssar.aslanpp.model.ImplicitExplicitSymbol.ImplicitExplicitState;

/**
 * Generates a textual representation of an ASLan++ specification (or some part
 * of it). Can be used either for printing a "normal" representation of the
 * specification, either for showing all internal details, like disambiguated
 * names and virtual scopes.
 * 
 * @author gabi
 */
public class PrettyPrinter implements IASLanPPVisitor {

	private final StringBuffer sb = new StringBuffer();
	private int indent = 0;
	private final boolean showInternals;
	private Entity ent;

	public PrettyPrinter() {
		this(false);
	}

	public PrettyPrinter(boolean showInternals) {
		this.showInternals = showInternals;
	}

	@Override
	public void visit(ASLanPPSpecification spec) {
		sb.append("specification ").append(spec.getSpecificationName());
		endLine();
		sb.append("channel_model ").append(spec.getChannelModel().toString());
		endLine();

		if (showInternals) {
			endLine();
			startLine();
			sb.append("entity Global_scope {");
			endLine();
			indent();
			showDeclarations(spec);
		}
		if (spec.getRootEntity() != null) {
			endLine();
			spec.getRootEntity().accept(this);
		}
		if (showInternals) {
			showGoals(spec); // TODO are there any at this level at all?
			unindent();
			startLine();
			sb.append("}");
			endLine();
		}
	}

	@Override
	public void visit(Entity ent) {
		Entity oldEnt = this.ent;
		this.ent = ent;

		startLine();
		sb.append("entity ");
		putName(ent);
		List<VariableSymbol> parameters = ent.getParameters();
		if (parameters.size() > 0) {
			sb.append("(");
			boolean firstGroup = true;
			for (DeclarationGroup gr : ent.getDeclarationGroups()) {
				int cnt = 0;
				for (ISymbol s : gr) {
					if (parameters.contains(s)) {
						cnt++;
					}
				}
				if (cnt > 0) {
					if (!firstGroup) {
						sb.append(", ");
					}
					boolean first = true;
					IType tt = null;
					for (ISymbol s : gr) {
						if (parameters.contains(s)) {
							if (!first) {
								sb.append(", ");
							}
							else {
								tt = s.getType();
							}
							putName(s);
							first = false;
						}
					}
					sb.append(": ");
					tt.accept(this);
					firstGroup = false;
				}
			}
			sb.append(")");
		}
		sb.append(" {");
		endLine();
		indent();

		if (ent.getImports().size() > 0) {
			endLine();
			startLine();
			sb.append("import");
			endLine();
			indent();
			startLine();
			boolean first = true;
			for (String imp : ent.getImports()) {
				if (!first) {
					sb.append(", ");
				}
				sb.append(imp);
				first = false;
			}
			sb.append(";");
			endLine();
			unindent();
		}

		showDeclarations(ent);

		for (Entity child : ent.getEntriesByType(Entity.class, !showInternals)) {
			endLine();
			child.accept(this);
		}

		if (ent.getBodyStatement() != null) {
			endLine();
			startLine();
			if (ent.isUncompressed()) {
				sb.append("uncompressed").append(" ");
			}
			else if (ent.wereBreakpointsAdded()) {
				sb.append("breakpoints {");
				boolean first = true;
				for (String s : ent.getBreakpoints()) {
					if (!first) {
						sb.append(", ");
					}
					sb.append(s);
					first = false;
				}
				sb.append("}");
				endLine();
			}
			sb.append("body");
			endLine();
			ent.getBodyStatement().accept(this);
		}

		showGoals(ent);

		unindent();
		startLine();
		sb.append("}");
		endLine();

		this.ent = oldEnt;
	}

	private void showDeclarations(IScope scope) {
		List<SimpleType> types = scope.getEntriesByType(SimpleType.class, !showInternals);
		if (types.size() > 0) {
			endLine();
			startLine();
			sb.append("types");
			endLine();
			indent();
			for (SimpleType t : types) {
				startLine();
				sb.append(t.getName());
				if (t.getSuperType() != null) {
					sb.append(" < ");
					sb.append(t.getSuperType().getName());
				}
				sb.append(";");
				endLine();
			}
			unindent();
		}

		List<ISymbol> symbols = scope.getEntriesByType(ISymbol.class, !showInternals);
		int cnt = 0;
		for (ISymbol s : symbols) {
			if (!skip(s)) {
				cnt++;
			}
		}
		if (cnt > 0) {
			endLine();
			startLine();
			sb.append("symbols");
			endLine();
			indent();
			for (DeclarationGroup gr : scope.getDeclarationGroups()) {
				gr.accept(this);
			}
			unindent();
		}

		if (showInternals) {
			List<IScope> allScopes = scope.getEntriesByType(IScope.class);
			for (IScope vs : allScopes) {
				if (!(vs instanceof Entity)) {
					visitVirtualScope(vs);
				}
			}
		}

		List<MacroSymbol> macros = scope.getEntriesByType(MacroSymbol.class, !showInternals);
		if (macros.size() > 0) {
			endLine();
			startLine();
			sb.append("macros");
			endLine();
			indent();
			for (MacroSymbol macro : macros) {
				macro.accept(this);
			}
			unindent();
		}

		List<HornClause> clauses = scope.getEntriesByType(HornClause.class, !showInternals);
		int hcCount = 0;
		for (HornClause hc : clauses) {
			if (!hc.isPartOfPrelude()) {
				hcCount++;
			}
		}
		if (hcCount > 0) {
			endLine();
			startLine();
			sb.append("clauses");
			endLine();
			indent();
			boolean first = true;
			for (HornClause cl : scope.getEntriesByType(HornClause.class)) {
				if (!cl.isPartOfPrelude()) {
					if (!first) {
						endLine();
					}
					cl.accept(this);
					first = false;
				}
			}
			unindent();
		}

		List<Equation> equations = scope.getEntriesByType(Equation.class, !showInternals);
		if (equations.size() > 0) {
			endLine();
			startLine();
			sb.append("equations");
			endLine();
			indent();
			for (Equation eq : equations) {
				eq.accept(this);
			}
			unindent();
		}
	}

	private void showGoals(IScope scope) {
		List<Goal> goals = scope.getEntriesByType(Goal.class, !showInternals);
		List<SessionChannelGoal> chGoals = scope.getEntriesByType(SessionChannelGoal.class, !showInternals);
		List<SessionSecrecyGoal> secrGoals = scope.getEntriesByType(SessionSecrecyGoal.class, !showInternals);
		if (goals.size() + chGoals.size() + secrGoals.size() > 0) {
			endLine();
			startLine();
			sb.append("goals");
			endLine();
			indent();
			for (Goal g : goals) {
				g.accept(this);
			}
			for (SessionChannelGoal chG : chGoals) {
				chG.accept(this);
			}
			for (SessionSecrecyGoal secrG : secrGoals) {
				secrG.accept(this);
			}
			unindent();
		}
	}

	public void visit(SimpleType type) {
		sb.append(type.getName());
	}

	public void visit(CompoundType type) {
		sb.append(type.getName());
		sb.append("(");
		boolean first = true;
		for (IType t : type.getArgumentTypes()) {
			if (!first) {
				sb.append(", ");
			}
			t.accept(this);
			first = false;
		}
		sb.append(")");
	}

	public void visit(SetType type) {
		type.getBaseType().accept(this);
		sb.append(" ").append("set");
	}

	public void visit(TupleType type) {
		sb.append("(");
		boolean first = true;
		for (IType t : type.getBaseTypes()) {
			if (!first) {
				sb.append(", ");
			}
			t.accept(this);
			first = false;
		}
		sb.append(")");
	}

	public void visitVirtualScope(IScope scope) {
		List<ISymbol> symbols = scope.getEntriesByType(ISymbol.class);
		if (symbols.size() > 0) {
			endLine();
			startLine();
			sb.append("entity ");
			putName(scope);
			sb.append("_scope {");
			endLine();
			indent();
			for (ISymbol s : symbols) {
				startLine();
				s.accept(this);
				sb.append(": ");
				s.getType().accept(this);
				sb.append(";");
				endLine();
			}
			unindent();
			startLine();
			sb.append("}");
			endLine();
		}
	}

	public void visit(DeclarationGroup gr) {
		int cnt = 0;
		for (ISymbol sym : gr) {
			if (!skip(sym)) {
				cnt++;
			}
		}
		if (cnt > 0) {
			startLine();
			boolean first = true;
			IType t = null;
			for (ISymbol sym : gr) {
				if (!skip(sym)) {
					if (!first) {
						sb.append(", ");
					}
					else {
						t = sym.getType();
						if (sym instanceof ConstantSymbol) {
							ConstantSymbol csym = (ConstantSymbol) sym;
							if (showInternals) {
								if (csym.getState() != ImplicitExplicitState.unknown) {
									sb.append(csym.getState().toString()).append(" ");
								}
							}
							if (csym.isNonPublic()) {
								sb.append("nonpublic").append(" ");
							}
						}
						else if (sym instanceof FunctionSymbol) {
							FunctionSymbol fsym = (FunctionSymbol) sym;
							if (showInternals) {
								if (fsym.getState() != ImplicitExplicitState.unknown) {
									sb.append(fsym.getState().toString()).append(" ");
								}
							}
							if (fsym.isNonPublic()) {
								sb.append("nonpublic").append(" ");
							}
							if (fsym.isNonInvertible()) {
								sb.append("noninvertible").append(" ");
							}
						}
					}
					sym.accept(this);
					first = false;
				}
			}
			if (t != null) {
				sb.append(": ");
				t.accept(this);
				sb.append(";");
			}
			endLine();
		}
	}

	private boolean skip(ISymbol sym) {
		boolean skip = false;
		if (ent != null) {
			if (sym instanceof VariableSymbol) {
				if (ent.getParameters().contains(sym)) {
					skip = true;
				}
				if (!showInternals) {
					if (!ent.getStateSymbols().contains(sym)) {
						skip = true;
					}
					if (ent.getActorSymbol().equals(sym)) {
						skip = true;
					}
					else if (ent.getIDSymbol().equals(sym)) {
						skip = true;
					}
					else if (ent.getStepSymbol().equals(sym)) {
						skip = true;
					}
				}
			}
			if (!showInternals) {
				if (sym instanceof ConstantSymbol) {
					ConstantSymbol cs = (ConstantSymbol) sym;
					if (cs.isPartOfPrelude()) {
						skip = true;
					}
				}
				else if (sym instanceof FunctionSymbol) {
					FunctionSymbol fs = (FunctionSymbol) sym;
					if (fs.isPartOfPrelude() || fs.isInternalUse()) {
						skip = true;
					}
				}
			}
		}
		return skip;
	}

	@Override
	public void visit(VariableSymbol var) {
		putName(var);
	}

	@Override
	public void visit(ConstantSymbol cnst) {
		putName(cnst);
	}

	@Override
	public void visit(FunctionSymbol fnc) {
		putName(fnc);
		sb.append("(");
		boolean first = true;
		for (IType t : fnc.getArgumentsTypes()) {
			if (!first) {
				sb.append(", ");
			}
			t.accept(this);
			first = false;
		}
		sb.append(")");
	}

	@Override
	public void visit(MacroSymbol macro) {
		startLine();
		putName(macro);
		if (macro.getArguments().size() > 0) {
			sb.append("(");
			boolean first = true;
			for (VariableSymbol arg : macro.getArguments()) {
				if (!first) {
					sb.append(", ");
				}
				putName(arg);
				first = false;
			}
			sb.append(")");
		}
		if (macro.getBody() != null) {
			sb.append(" = ");
			macro.getBody().accept(this);
		}
		sb.append(";");
		endLine();
	}

	@Override
	public void visit(HornClause clause) {
		startLine();
		putName(clause);
		if (clause.getArguments().size() > 0) {
			sb.append("(");
			boolean first = true;
			for (VariableSymbol v : clause.getArguments()) {
				if (!first) {
					sb.append(", ");
				}
				putName(v);
				first = false;
			}
			sb.append(")");
		}
		sb.append(": ");
		endLine();
		indent();
		startLine();
		if (clause.getUniversallyQuantified().size() > 0) {
			sb.append("forall");
			for (VariableSymbol v : clause.getUniversallyQuantified()) {
				sb.append(" ");
				putName(v);
			}
			sb.append(". ");
		}
		clause.getHead().accept(this);
		if (clause.getBody().size() > 0 || clause.getEqualities().size() > 0) {
			sb.append(" :-");
			endLine();
			boolean first = true;
			indent();
			for (ITerm t : clause.getBody()) {
				if (!first) {
					sb.append(" &");
					endLine();
				}
				startLine();
				t.accept(this);
				first = false;
			}
			for (IExpression e : clause.getEqualities()) {
				if (!first) {
					sb.append(" &");
					endLine();
				}
				startLine();
				e.accept(this);
				first = false;
			}
			unindent();
		}
		sb.append(";");
		endLine();
		unindent();
	}

	public void visit(Equation equation) {
		startLine();
		if (equation.getLeftTerm() != null) {
			equation.getLeftTerm().accept(this);
		}
		else {
			sb.append("?");
		}
		sb.append(" = ");
		if (equation.getRightTerm() != null) {
			equation.getRightTerm().accept(this);
		}
		else {
			sb.append("?");
		}
		sb.append(";");
		endLine();
	}

	@Override
	public void visit(Constraint constraint) {
		startLine();
		putName(constraint);
		sb.append(": ");
		constraint.getFormula().accept(this);
		sb.append(";");
		endLine();
	}

	@Override
	public void visit(Goal goal) {
		startLine();
		putName(goal);
		sb.append(": ");
		goal.getFormula().accept(this);
		sb.append(";");
		endLine();
	}

	@Override
	public void visit(SessionChannelGoal chGoal) {
		startLine();
		putName(chGoal);
		sb.append(":(_) ");
		chGoal.getSender().accept(this);
		sb.append(" ").append(chGoal.getType().arrow).append(" ");
		chGoal.getReceiver().accept(this);
		sb.append(";");
		endLine();
	}

	@Override
	public void visit(SessionSecrecyGoal secrGoal) {
		startLine();
		putName(secrGoal);
		sb.append(" :(_) {");
		boolean first = true;
		for (ITerm t : secrGoal.getAgents()) {
			if (!first) {
				sb.append(", ");
			}
			t.accept(this);
			first = false;
		}
		sb.append("};");
		endLine();
	}

	@Override
	public void visit(AssignmentStatement stmt) {
		startLine();
		stmt.getSymbolTerm().accept(this);
		sb.append(" := ");
		stmt.getTerm().accept(this);
		sb.append(";");
		endLine();
	}

	@Override
	public void visit(AssertStatement stmt) {
		startLine();
		sb.append("assert ");
		putName(stmt);
		sb.append(": ");
		stmt.getGuard().accept(this);
		sb.append(";");
		endLine();
	}

	@Override
	public void visit(BlockStatement stmt) {
		startLine();
		sb.append("{");
		endLine();
		indent();
		for (IStatement s : stmt.getStatements()) {
			s.accept(this);
		}
		unindent();
		startLine();
		sb.append("}");
		endLine();
	}

	@Override
	public void visit(FreshStatement stmt) {
		startLine();
		stmt.getSymbolTerm().accept(this);
		sb.append(" := fresh();");
		endLine();
	}

	@SuppressWarnings("unused")
	@Override
	public void visit(LoopStatement stmt) {
		startLine();
		sb.append("while (");
		stmt.getGuard().accept(this);
		sb.append(")");
		if (stmt != null) {
			endLine();
			if (!(stmt.getBody() instanceof BlockStatement)) {
				indent();
			}
			stmt.getBody().accept(this);
			if (!(stmt.getBody() instanceof BlockStatement)) {
				unindent();
			}
		}
		else {
			sb.append(" {}");
			endLine();
		}
	}

	@Override
	public void visit(IntroduceStatement stmt) {
		startLine();
		stmt.getTerm().accept(this);
		sb.append(";");
		endLine();
		if (stmt.getChannelGoals().size() > 0) {
			for (ChannelGoal cg : stmt.getChannelGoals()) {
				startLine();
				cg.accept(this);
				endLine();
			}
		}
	}

	@Override
	public void visit(RetractStatement stmt) {
		startLine();
		sb.append("retract ");
		stmt.getTerm().accept(this);
		sb.append(";");
		endLine();
	}

	@Override
	public void visit(SelectStatement stmt) {
		startLine();
		sb.append("select");
		endLine();
		startLine();
		sb.append("{");
		endLine();
		indent();
		boolean first = true;
		for (IExpression g : stmt.getChoices().keySet()) {
			if (!first) {
				endLine();
			}
			startLine();
			sb.append("on (");
			g.accept(this);
			sb.append(") :");
			endLine();
			if (g.getChannelGoals().size() > 0) {
				for (ChannelGoal cg : g.getChannelGoals()) {
					startLine();
					cg.accept(this);
					endLine();
				}
			}
			IStatement s = stmt.getChoices().get(g);
			if (!(s instanceof BlockStatement)) {
				indent();
			}
			if (s == null) {
				System.out.println("what??");
				sb.append("?");
			}
			if (s != null) {
				s.accept(this);
			}
			if (!(s instanceof BlockStatement)) {
				unindent();
			}
			first = false;
		}
		unindent();
		startLine();
		sb.append("}");
		endLine();
	}

	@Override
	public void visit(BranchStatement stmt) {
		startLine();
		sb.append("if (");
		stmt.getGuard().accept(this);
		sb.append(")");
		if (stmt.getTrueBranch() != null) {
			endLine();
			if (!(stmt.getTrueBranch() instanceof BlockStatement)) {
				indent();
			}
			stmt.getTrueBranch().accept(this);
			if (!(stmt.getTrueBranch() instanceof BlockStatement)) {
				unindent();
			}
		}
		else {
			sb.append(" {}");
			endLine();
		}
		if (stmt.getFalseBranch() != null) {
			startLine();
			sb.append("else");
			endLine();
			if (!(stmt.getFalseBranch() instanceof BlockStatement)) {
				indent();
			}
			stmt.getFalseBranch().accept(this);
			if (!(stmt.getFalseBranch() instanceof BlockStatement)) {
				unindent();
			}
		}
	}

	@Override
	public void visit(NewEntityInstanceStatement stmt) {
		startLine();
		sb.append("new ");
		putName(stmt.getEntity());
		if (stmt.getParameters().size() > 0) {
			sb.append("(");
			boolean first = true;
			for (ITerm a : stmt.getParameters()) {
				if (!first) {
					sb.append(", ");
				}
				a.accept(this);
				first = false;
			}
			sb.append(")");
		}
		sb.append(";");
		endLine();
	}

	@Override
	public void visit(SymbolicInstanceStatement stmt) {
		startLine();
		if (stmt.getUniversallyQuantified().size() > 0) {
			sb.append("any");
			for (VariableSymbol v : stmt.getUniversallyQuantified()) {
				sb.append(" ");
				putName(v);
			}
			sb.append(". ");
		}
		putName(stmt.getEntity());
		if (stmt.getParameters().size() > 0) {
			sb.append("(");
			boolean first = true;
			for (ITerm a : stmt.getParameters()) {
				if (!first) {
					sb.append(", ");
				}
				a.accept(this);
				first = false;
			}
			sb.append(")");
		}
		if (stmt.getGuard() != null) {
			sb.append(" where ");
			stmt.getGuard().accept(this);
		}
		sb.append(";");
		endLine();
	}

	public void visit(SecrecyGoalStatement stmt) {
		startLine();
		sb.append("secrecy_goal").append(" ");
		sb.append(stmt.getName());
		sb.append(": ");
		boolean first = true;
		for (ITerm t : stmt.getAgents()) {
			if (!first) {
				sb.append(", ");
			}
			t.accept(this);
			first = false;
		}
		sb.append(": ");
		stmt.getPayload().accept(this);
		sb.append(";");
		endLine();
	}

	public void visit(ChannelGoal goal) {
		sb.append("channel_goal").append(" ");
		putName(goal);
		sb.append(": ");
		goal.getSender().accept(this);
		sb.append(" ");
		sb.append(goal.getType().arrow);
		sb.append(" ");
		goal.getReceiver().accept(this);
		sb.append(" : ");
		goal.getPayload().accept(this);
		sb.append(";");
	}

	@Override
	public void visit(NegationExpression expr) {
		sb.append("!");
		expr.getBaseExpression().accept(this);
	}

	@Override
	public void visit(ConjunctionExpression expr) {
		sb.append("(");
		expr.getLeftExpression().accept(this);
		sb.append(" & ");
		expr.getRightExpression().accept(this);
		sb.append(")");
	}

	@Override
	public void visit(DisjunctionExpression expr) {
		sb.append("(");
		expr.getLeftExpression().accept(this);
		sb.append(" | ");
		expr.getRightExpression().accept(this);
		sb.append(")");
	}

	@Override
	public void visit(ExistsExpression expr) {
		sb.append("(");
		if (expr.getSymbols().size() > 0) {
			sb.append("exists");
			for (ISymbol sym : expr.getSymbols()) {
				sb.append(" ");
				putName(sym);
			}
			sb.append(". ");
		}
		expr.getBaseExpression().accept(this);
		sb.append(")");
	}

	@Override
	public void visit(ForallExpression expr) {
		sb.append("(");
		if (expr.getSymbols().size() > 0) {
			sb.append("forall");
			for (ISymbol sym : expr.getSymbols()) {
				sb.append(" ");
				putName(sym);
			}
			sb.append(". ");
		}
		expr.getBaseExpression().accept(this);
		sb.append(")");
	}

	@Override
	public void visit(ImplicationExpression expr) {
		sb.append("(");
		expr.getLeftExpression().accept(this);
		sb.append(" => ");
		expr.getRightExpression().accept(this);
		sb.append(")");
	}

	@Override
	public void visit(LTLExpression expr) {
		sb.append(LTLExpression.convertOpBack(expr.getOperator()));
		sb.append("(");
		boolean first = true;
		for (IExpression e : expr.getChildExpressions()) {
			if (!first) {
				sb.append(",");
			}
			e.accept(this);
			first = false;
		}
		sb.append(")");
	}

	@Override
	public void visit(EqualityExpression expr) {
		sb.append("(");
		expr.getLeftTerm().accept(this);
		sb.append(" = ");
		expr.getRightTerm().accept(this);
		sb.append(")");
	}

	@Override
	public void visit(InequalityExpression expr) {
		sb.append("(");
		expr.getLeftTerm().accept(this);
		sb.append(" != ");
		expr.getRightTerm().accept(this);
		sb.append(")");
	}

	@Override
	public void visit(BaseExpression expr) {
		expr.getBaseTerm().accept(this);
	}

	@Override
	public CommunicationTerm visit(CommunicationTerm term) {
		if (term.isRenderAsFunction()) {
			if (term.isRenderOOPStyle()) {
				if (term.isReceive()) {
					term.getReceiver().accept(this);
				}
				else {
					term.getSender().accept(this);
				}
				sb.append("->");
			}
			sb.append(term.isReceive() ? Prelude.RECEIVE : Prelude.SEND);
			sb.append("(");
			if (!term.isRenderOOPStyle()) {
				if (term.isReceive()) {
					term.getReceiver().accept(this);
				}
				else {
					term.getSender().accept(this);
				}
				sb.append(", ");
			}
			if (term.isReceive()) {
				term.getSender().accept(this);
			}
			else {
				term.getReceiver().accept(this);
			}
			sb.append(", ");
			term.getPayload().accept(this);
			sb.append(")");
			String tag = term.getChannelType().nonArrow;
			if (tag.trim().length() > 0) {
				sb.append(" over ");
				if (term.getChannel() == null) {
					sb.append(tag);
				}
				else {
					term.getChannel().accept(this);
				}
			}
		}
		else {
			term.getSender().accept(this);
			sb.append(" ");
			if (term.getChannel() == null) {
				sb.append(term.getChannelType().arrow);
			}
			else {
				sb.append("-");
				term.getChannel().accept(this);
				sb.append("->");
			}
			sb.append(" ");
			term.getReceiver().accept(this);
			sb.append(" : ");
			term.getPayload().accept(this);
		}
		return term;
	}

	private void startAnnotations(ITerm term) {
		for (int i = term.getAnnotations().size() - 1; i >= 0; i--) {
			sb.append(term.getAnnotations().get(i).toString()).append(":(");
		}
	}

	private void endAnnotations(ITerm term) {
		for (int i = term.getAnnotations().size() - 1; i >= 0; i--) {
			sb.append(")");
		}
	}

	@Override
	public ConcatTerm visit(ConcatTerm term) {
		startAnnotations(term);
		boolean first = true;
		for (ITerm t : term.getTerms()) {
			if (!first) {
				sb.append(".");
			}
			t.accept(this);
			first = false;
		}
		endAnnotations(term);
		return term;
	}

	@Override
	public ConstantTerm visit(ConstantTerm term) {
		startAnnotations(term);
		putName(term.getSymbol());
		endAnnotations(term);
		return term;
	}

	@Override
	public DefaultPseudonymTerm visit(DefaultPseudonymTerm term) {
		sb.append("[");
		term.getBaseTerm().accept(this);
		sb.append("]");
		return term;
	}

	@Override
	public FunctionTerm visit(FunctionTerm term) {
		startAnnotations(term);
		boolean isOOP = false;// term.isOOPStyle();
		if (isOOP) {
			term.getArguments().get(0).accept(this);
			sb.append("->");
		}
		putName(term.getSymbol());
		sb.append("(");
		boolean first = true;
		boolean skipped = false;
		for (ITerm t : term.getArguments()) {
			if (first && isOOP && !skipped) {
				skipped = true;
				continue;
			}
			if (!first) {
				sb.append(",");
			}
			t.accept(this);
			first = false;
		}
		sb.append(")");
		endAnnotations(term);
		return term;
	}

	@Override
	public MacroTerm visit(MacroTerm term) {
		startAnnotations(term);
		putName(term.getMacro());
		if (term.getArguments().size() > 0) {
			sb.append("(");
			boolean first = true;
			for (ITerm t : term.getArguments()) {
				if (!first) {
					sb.append(", ");
				}
				t.accept(this);
				first = false;
			}
			sb.append(")");
		}
		endAnnotations(term);
		return term;
	}

	@Override
	public PseudonymTerm visit(PseudonymTerm term) {
		sb.append("[");
		term.getBaseTerm().accept(this);
		sb.append("]_[");
		term.getPseudonym().accept(this);
		sb.append("]");
		return term;
	}

	@Override
	public SetLiteralTerm visit(SetLiteralTerm term) {
		sb.append("{");
		boolean first = true;
		for (ITerm t : term.getTerms()) {
			if (!first) {
				sb.append(", ");
			}
			t.accept(this);
			first = false;
		}
		sb.append("}");
		return term;
	}

	@Override
	public TupleTerm visit(TupleTerm term) {
		startAnnotations(term);
		sb.append("(");
		boolean first = true;
		for (ITerm t : term.getTerms()) {
			if (!first) {
				sb.append(",");
			}
			t.accept(this);
			first = false;
		}
		sb.append(")");
		endAnnotations(term);
		return term;
	}

	@Override
	public VariableTerm visit(VariableTerm term) {
		startAnnotations(term);
		if (term.isMatched()) {
			sb.append("?");
		}
		putName(term.getSymbol());
		endAnnotations(term);
		return term;
	}

	@Override
	public UnnamedMatchTerm visit(UnnamedMatchTerm term) {
		startAnnotations(term);
		sb.append("?");
		if (showInternals && term.getDummySymbol() != null) {
			putName(term.getDummySymbol());
		}
		endAnnotations(term);
		return term;
	}

	public NumericTerm visit(NumericTerm term) {
		startAnnotations(term);
		sb.append(term.getValue());
		endAnnotations(term);
		return term;
	}

	@Override
	public String toString() {
		return sb.toString();
	}

	public void toFile(String fileName) {
		try {
			PrintStream out = new PrintStream(fileName);
			out.print(sb.toString());
			out.close();
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	private void putName(INamed n) {
		if (showInternals) {
			sb.append(n.getName());
		}
		else {
			sb.append(n.getOriginalName());
		}
	}

	private void indent() {
		indent++;
	}

	private void unindent() {
		indent--;
	}

	private void startLine() {
		for (int i = 0; i < indent; i++) {
			sb.append("\t");
		}
	}

	private void endLine() {
		sb.append("\n");
	}

}
