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

package org.avantssar.aslan.of;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.avantssar.aslan.HornClause;
import org.avantssar.aslan.IASLanSpec;
import org.avantssar.aslan.IParameterized;
import org.avantssar.aslan.ITerm;
import org.avantssar.aslan.RewriteRule;
import org.avantssar.aslan.Variable;
import org.avantssar.commons.ErrorGatherer;

public class ExecutionScenarioBuilder implements IAnalysisResultVisitor {

	private final StringBuffer sb = new StringBuffer();
	private int indent = 0;

	private final ErrorGatherer err;

	private final IASLanSpec aslanSpec;
	private final ExecutionScenario exec;

	public ExecutionScenarioBuilder(IASLanSpec aslanSpec, ExecutionScenario exec, ErrorGatherer err) {
		RewriteRule rule = aslanSpec.findRule("step_001_ACM");
		if (rule != null) { // using this as indication for ACM
		//  rule.addCommentLine("@communication(entity=prelude; iid=0; line=0; sender=ACM_RS; receiver=ACM_Rcv; payload=ACM_Msg; channel=ACM_Ch; fact=sent(ACM_RS,ACM_OS,ACM_Rcv,ACM_Msg,ACM_Ch); direction=send)");
		//  rule.addCommentLine("@communication(entity=prelude; iid=0; line=0; sender=ACM_OS; receiver=ACM_Rcv; payload=ACM_Msg; channel=ACM_Ch; fact=rcvd(ACM_Rcv,ACM_OS,ACM_Msg,ACM_Ch); direction=receive)");

			Variable vA  = aslanSpec.variable("A" , IASLanSpec.AGENT);
			Variable vR  = aslanSpec.variable("R" , IASLanSpec.AGENT);
			Variable vM  = aslanSpec.variable("M" , IASLanSpec.MESSAGE);
			Variable vCh = aslanSpec.variable("Ch", IASLanSpec.CHANNEL);
			ITerm rcvd = IASLanSpec.RCVD.term(IASLanSpec.INTRUDER.term(), vA.term(), vM.term(), vCh.term());

			rule = aslanSpec.rule("fake");
			rule.addCommentLine("@guard(entity=prelude; iid=0; line=0; test=iknows(M))");
			rule.addCommentLine("@guard(entity=prelude; iid=0; line=0; test=iknows(A))");
			rule.addCommentLine("@guard(entity=prelude; iid=0; line=0; test=iknows(R))");
		    rule.addCommentLine("@communication(entity=prelude; iid=0; line=0; sender=A; receiver=R; payload=M; channel=Ch; fact=sent(i, A, R, M, Ch); direction=send)");
			rule.addParameter(vA); rule.addParameter(vR); rule.addParameter(vM); rule.addParameter(vCh);
			ITerm sent = IASLanSpec.SENT.term(IASLanSpec.INTRUDER.term(), vA.term(), vR.term(), vM.term(), vCh.term());
			rule.addLHS(IASLanSpec.IKNOWS.term(vM.term()));
			rule.addLHS(IASLanSpec.IKNOWS.term(vA.term()));
			rule.addLHS(IASLanSpec.IKNOWS.term(vR.term()));
			rule.addRHS(sent); 

			rule = aslanSpec.rule("intercept");
		//  rule.addCommentLine("@communication(entity=prelude; iid=0; line=0; sender=A; receiver=R; payload=M; channel=Ch; fact=sent(A, A, R, M, Ch); direction=send)");
			rule.addCommentLine("@retract(entity=prelude; iid=0; line=0; fact=sent(A, A, R, M, Ch))");
			rule.addCommentLine("@communication(entity=prelude; iid=0; line=0; sender=A; receiver=i; payload=M; channel=Ch; fact=rcvd(i, A, M, Ch); direction=receive)");
			rule.addCommentLine("@introduce(entity=prelude; iid=0; line=0; fact=iknows(M))");
			rule.addParameter(vA); rule.addParameter(vR); rule.addParameter(vM); rule.addParameter(vCh);
			sent = IASLanSpec.SENT.term(vA.term(), vA.term(), vR.term(), vM.term(), vCh.term());
			rule.addLHS(sent);
			                   rule.addRHS(rcvd); rule.addRHS(IASLanSpec.IKNOWS.term(vM.term()));

			rule = aslanSpec.rule("overhear");
		//  rule.addCommentLine("@communication(entity=prelude; iid=0; line=0; sender=A; receiver=R; payload=M; channel=Ch; fact=sent(A, A, R, M, Ch); direction=send)");
			rule.addCommentLine("@communication(entity=prelude; iid=0; line=0; sender=A; receiver=i; payload=M; channel=Ch; fact=rcvd(i, A, M, Ch); direction=receive)");
			rule.addCommentLine("@introduce(entity=prelude; iid=0; line=0; fact=iknows(M))");
			rule.addParameter(vA); rule.addParameter(vR); rule.addParameter(vM); rule.addParameter(vCh);
			sent = IASLanSpec.SENT.term(vA.term(), vA.term(), vR.term(), vM.term(), vCh.term());
			rule.addLHS(sent);
			rule.addRHS(sent); rule.addRHS(rcvd); rule.addRHS(IASLanSpec.IKNOWS.term(vM.term()));
		}
		this.aslanSpec = aslanSpec;
		this.exec = exec;
		this.err = err;
	}

	public ErrorGatherer getErrorGatherer() {
		return err;
	}

	@Override
	public void visit(AnalysisResult ar) {
		startLine();
		sb.append(ar.getFilename());
		endLine();
		startLine();
		sb.append("backend");
		sb.append(" ").append(ar.getBackendName());
		sb.append(" ").append(ar.getBackendVersion());
		if (ar.getBackendDate() != null) {
			sb.append("(").append(ar.getBackendDate()).append(")");
		}
		endLine();
		if (ar.getAttack() != null) {
			startLine();
			sb.append("attack");
			endLine();
			indent();
			exec.setViolatedGoals(ar.getAttack().accept(this));
			unindent();
		}
		else {
			if (ar.isInconclusive()) {
				startLine();
				sb.append("inconclusive");
				endLine();
				exec.setInconclusive();
			}
			else {
				startLine();
				sb.append("no attack");
				endLine();
			}
		}
		if (ar.getTrace() != null) {
			startLine();
			sb.append("trace");
			endLine();
			indent();
			ar.getTrace().accept(this);
			unindent();
		}
	}

	@Override
	public GoalsBatch visit(Goals goals) {
		GoalsBatch gb = new GoalsBatch();
		for (IGroundTerm t : goals) {
			gb.add(visitGoal(t));
		}
		return gb;
	}

	private GoalViolatedEvent visitGoal(IGroundTerm term) {
		String name = null;
		List<IGroundTerm> parameters = new ArrayList<IGroundTerm>();
		if (term instanceof GroundFunction) {
			GroundFunction f = (GroundFunction) term;
			name = f.getName();
			parameters.addAll(f.getParameters());
		}
		else if (term instanceof GroundConstant) {
			GroundConstant c = (GroundConstant) term;
			name = c.getName();
		}
		if (name != null) {
			IParameterized as = aslanSpec.findAttackState(name);
			if (as == null) {
				as = aslanSpec.findGoal(name);
			}
			if (as != null) {
				startLine();
				sb.append("[ok] ");
				term.accept(this);
				endLine();
				Map<Variable, IGroundTerm> assigned = mapParameters(as, parameters, "Goal/Attack state");
				return ExecutionScenario.parseGoalMetaInfo(as, assigned, err);
			}
			else {
				err.addError(term.getLocation(), OutputFormatErrorMessages.UNKNOWN_ITEM_REFERENCE, "Goal/Attack state", name);
				return null;
			}
		}
		else {
			throw new RuntimeException("Function/Constant expected as goal: " + term.getLocation());
		}
	}

	@Override
	public void visit(Trace trace) {
		for (TraceState ts : trace) {
			ts.accept(this);
		}
	}

	@Override
	public void visit(TraceState traceState) {
		startLine();
		sb.append(traceState.getIndex());
		endLine();
		if (traceState.getRules() != null) {
			startLine();
			sb.append("rules");
			endLine();
			indent();
			exec.addTransition(traceState.getRules().accept(this));
			unindent();
		}
		if (traceState.getClauses() != null) {
			startLine();
			sb.append("clauses");
			endLine();
			indent();
			exec.addClauses(traceState.getClauses().accept(this));
			unindent();
		}
	}

	@Override
	public ExecutionEventsTraceTransition visit(Rules rules) {
		ExecutionEventsTraceTransition trans = new ExecutionEventsTraceTransition();
		for (MacroStep step : rules) {
			trans.add(step.accept(this));
		}
		return trans;
	}

	@Override
	public ExecutionEventsMacroStep visit(MacroStep macroStep) {
		ExecutionEventsMacroStep ms = new ExecutionEventsMacroStep();
		for (IGroundTerm t : macroStep) {
			ms.add(visitRule(t));
		}
		return ms;
	}

	private Map<Variable, IGroundTerm> mapParameters(IParameterized rr, List<IGroundTerm> fpars, String label) {
		Map<Variable, IGroundTerm> pars = new TreeMap<Variable, IGroundTerm>();
		if (rr.getParameters().size() != fpars.size()) {
			err.addError(OutputFormatErrorMessages.WRONG_NUMBER_OF_PARAMETERS, label, rr.getName(), rr.getParameters().size(), fpars.size());
		}
		// TODO weird code below - why ExplicitParameters vs. Parameters?
		List<Variable> varsInOrder = new ArrayList<Variable>();
		for (Variable v : rr.getExplicitParameters()) {
			if (rr.getParameters().contains(v)) {
				varsInOrder.add(v);
			}
		}
		for (Variable v : rr.getParameters()) {
			if (!varsInOrder.contains(v)) {
				varsInOrder.add(v);
			}
		}

		for (int i = 0; i < rr.getParameters().size() 
             && i < fpars.size()  // more robustness on wrong e.g. OFMC output
           ; i++) {
			Variable v = varsInOrder.get(i);
			IGroundTerm t = fpars.get(i);
			pars.put(v, t);
		}
		return pars;
	}

	private ExecutionEventsBatch visitRule(IGroundTerm term) {
		if (term instanceof GroundFunction) {
			GroundFunction f = (GroundFunction) term;
			RewriteRule rule = aslanSpec.findRule(f.getName());
			if (rule != null) {
				startLine();
				sb.append("[ok] ");
				term.accept(this);
				endLine();
				Map<Variable, IGroundTerm> assigned = mapParameters(rule, f.getParameters(), "Rewrite rule");
				return ExecutionScenario.parseMetaInfo(rule, assigned, err);
			}
			else {
				err.addError(term.getLocation(), OutputFormatErrorMessages.UNKNOWN_ITEM_REFERENCE, "Rewrite rule", f.getName());
				return new ExecutionEventsBatch();
			}
		}
		else {
			throw new RuntimeException("Function expected as rule: " + term.getLocation());
		}
	}

	@Override
	public ClausesFiredBatch visit(Clauses clauses) {
		ClausesFiredBatch batch = new ClausesFiredBatch();
		for (IGroundTerm t : clauses) {
			batch.add(visitClause(t));
		}
		return batch;
	}

	private ClauseFiredEvent visitClause(IGroundTerm term) {
		String name = null;
		List<IGroundTerm> parameters = new ArrayList<IGroundTerm>();
		if (term instanceof GroundFunction) {
			GroundFunction f = (GroundFunction) term;
			name = f.getName();
			parameters.addAll(f.getParameters());
		}
		else if (term instanceof GroundConstant) {
			GroundConstant c = (GroundConstant) term;
			name = c.getName();
		}
		if (name != null) {
			HornClause clause = aslanSpec.findHornClause(name);
			if (clause != null) {
				startLine();
				sb.append("[ok] ");
				term.accept(this);
				endLine();
				Map<Variable, IGroundTerm> assigned = mapParameters(clause, parameters, "Horn clause");
				return ExecutionScenario.parseClauseMetaInfo(clause, assigned, err);
			}
			else {
				err.addError(term.getLocation(), OutputFormatErrorMessages.UNKNOWN_ITEM_REFERENCE, "Horn clause", name);
				return null;
			}
		}
		else {
			throw new RuntimeException(term == null ? "Ground term expected" : "Function expected as rule: " + term.getLocation());
		}
	}

	@Override
	public void visit(GroundConstant constant) {
		sb.append(constant.getName());
	}

	@Override
	public void visit(GroundNumeral numeral) {
		sb.append(numeral.getValue());
	}

	@Override
	public void visit(GroundFunction function) {
		sb.append(function.getName());
		sb.append("(");
		boolean first = true;
		for (IGroundTerm p : function.getParameters()) {
			if (!first) {
				sb.append(",");
			}
			if (p != null) {
				p.accept(this);
			}
			else {
				sb.append("?");
			}
			first = false;
		}
		sb.append(")");
	}

	@Override
	public String toString() {
		return sb.toString();
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
