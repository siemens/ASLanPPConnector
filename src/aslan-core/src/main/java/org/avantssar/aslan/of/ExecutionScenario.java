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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import org.avantssar.aslan.HornClause;
import org.avantssar.aslan.IASLanSpec;
import org.avantssar.aslan.ICommentEntry;
import org.avantssar.aslan.IParameterized;
import org.avantssar.aslan.IRepresentable;
import org.avantssar.aslan.InitialState;
import org.avantssar.aslan.MetaInfo;
import org.avantssar.aslan.Variable;
import org.avantssar.commons.ErrorGatherer;
import org.avantssar.commons.LocationInfo;

public class ExecutionScenario implements ISetProvider {

	private final IASLanSpec aslanSpec;
	private final ErrorGatherer err;

	private final List<EntityState> entities = new ArrayList<EntityState>();

	private final List<ExecutionEventsTraceTransition> transitions = new ArrayList<ExecutionEventsTraceTransition>();
	private final List<ClausesFiredBatch> clauses = new ArrayList<ClausesFiredBatch>();

	private final List<IGroundTerm> facts = new ArrayList<IGroundTerm>();
	private final List<CommunicationEvent> communication = new ArrayList<CommunicationEvent>();
	private final Map<IGroundTerm, Set<IGroundTerm>> sets = new HashMap<IGroundTerm, Set<IGroundTerm>>();

	private GoalsBatch goals;
	private boolean inconclusive;

	public ExecutionScenario(IASLanSpec aslanSpec, ErrorGatherer err) {
		this.aslanSpec = aslanSpec;
		this.err = err;

		// Initial state.
		ExecutionEventsTraceTransition initTrans = new ExecutionEventsTraceTransition();
		ExecutionEventsMacroStep initMS = new ExecutionEventsMacroStep();
		initTrans.add(initMS);
		for (InitialState is : aslanSpec.getInitialStates()) {
			ExecutionEventsBatch initBatch = parseMetaInfo(is, new TreeMap<Variable, IGroundTerm>(), err);
			initMS.add(initBatch);
		}
		addTransition(initTrans);
	}

	public EntityState findEntity(String name, IGroundTerm iid) {
		for (EntityState es : entities) {
			if (es.answersTo(name, iid)) {
				return es;
			}
		}
		return null;
	}

	public void addEntity(EntityState entity, int line, StringBuffer sb, boolean dontPrint) {
		entities.add(entity);
		if (!dontPrint) {
			sb.append("n  " + entity.getRepresentationNice(this, aslanSpec) + "  % on line " + line).append("\n");
		}
	}

	public void addTransition(ExecutionEventsTraceTransition trans) {
		transitions.add(trans);
	}

	public void addClauses(ClausesFiredBatch clb) {
		clauses.add(clb);
	}

	public void setViolatedGoals(GoalsBatch goals) {
		this.goals = goals;
	}

	public void setInconclusive() {
		inconclusive = true;
	}

	public void addFact(IGroundTerm fact, int line, StringBuffer sb, boolean dontPrint) {
		facts.add(fact);
		if (!dontPrint) {
			sb.append("+  " + fact.getRepresentationNice(this, aslanSpec) + "  % on line " + line).append("\n");
		}
		updateSet(fact, true);

	}

	public void removeFact(IGroundTerm fact, int line, StringBuffer sb, boolean dontPrint) {
		facts.remove(fact);
		if (!dontPrint) {
			sb.append("-  " + fact.getRepresentationNice(this, aslanSpec) + "  % on line " + line).append("\n");
		}
		updateSet(fact, false);
	}

	public Set<IGroundTerm> getSet(IGroundTerm set) {
		return sets.get(set);
	}

	private void updateSet(IGroundTerm fact, boolean add) {
		if (fact instanceof GroundFunction) {
			GroundFunction fnc = (GroundFunction) fact;
			if (fnc.getName().equals(IASLanSpec.CONTAINS.getName())) {
				IGroundTerm item = fnc.getParameters().get(0);
				IGroundTerm set = fnc.getParameters().get(1);
				if (add) {
					Set<IGroundTerm> current = sets.get(set);
					if (current == null) {
						current = new TreeSet<IGroundTerm>();
						sets.put(set, current);
					}
					current.add(item);
				}
				else {
					Set<IGroundTerm> current = sets.get(set);
					if (current != null) {
						if (current.contains(item)) {
							current.remove(item);
							if (current.size() == 0) {
								sets.remove(set);
							}
						}
						else {
							err.addError(set.getLocation(), OutputFormatErrorMessages.SET_DOES_NOT_CONTAIN_ITEM, set.getRepresentation(aslanSpec), item.getRepresentation(aslanSpec));
						}
					}
					else {
						err.addError(set.getLocation(), OutputFormatErrorMessages.SET_SHOULD_NOT_BE_EMPTY, set.getRepresentation(aslanSpec));
					}
				}
			}
		}
	}

	public void trackCommunication(CommunicationEvent comm, StringBuffer sb, boolean dontPrint) {
		if (!dontPrint) {
			sb.append(comm.abbrev() + "  " + comm.describe(this, aslanSpec) + comm.comment()).append("\n");
		}
		// if (!communication.contains(comm)) {
		communication.add(comm);
		// }
	}

	public String execute() {
		StringBuffer sb = new StringBuffer();
		if (goals == null) {
			if (inconclusive) {
			// sb.append("INCONCLUSIVE\n");
			}
			// else {
			// sb.append("NO ATTACK FOUND\n");
			// }
		}
		else {
			// sb.append("ATTACK FOUND\n");
			sb.append("\n");
			if (transitions.size() > 1 || clauses.size() > 0) {
				if (transitions.size() != clauses.size()) {
					err.addException(OutputFormatErrorMessages.RULES_AND_CLAUSES_DO_NOT_MATCH, transitions.size(), clauses.size());
				}
				for (int i = 0; i < transitions.size(); i++) {
					if (i == 0) {
						// sb.append("INITIAL STATE:\n");
					}
					else {
						sb.append("STATEMENTS:\n");
					}
					ExecutionEventsTraceTransition trans = transitions.get(i);
					for (ExecutionEventsMacroStep ms : trans) {
						if (ms.size() > 1) {
							sb.append("[\n");
						}
						boolean firstBatch = true;
						for (ExecutionEventsBatch batch : ms) {
							if (!firstBatch) {
								sb.append("\n");
							}
							for (IExecutionEvent ev : batch) {
								if (ev != null) // more robustness on wrong e.g. OFMC output
									ev.execute(this, aslanSpec, err, sb, i == 0);
							}
							firstBatch = false;
						}
						if (ms.size() > 1) {
							sb.append("]\n");
						}
					}
					if (i > 0) {
						sb.append("\n");
					}

					sb.append("STATES:\n");
					for (EntityState ent : entities) {
						sb.append("   " + ent.getRepresentationNice(this, aslanSpec)).append("\n");
					}
					sb.append("\n");

					sb.append("CLAUSES:\n");
					ClausesFiredBatch cl = clauses.get(i);
					for (ClauseFiredEvent clev : cl) {
						if (clev != null) // more robustness on wrong e.g. OFMC output
							clev.execute(this, aslanSpec, sb);
					}
					sb.append("\n");
				}
			}
			printViolated(sb);
		}
		return sb.toString();
	}

  public void printViolated(StringBuffer sb) {
    sb.append("VIOLATED:\n");
		for (GoalViolatedEvent glev : goals) {
      if (glev != null) // more robustness on wrong e.g. OFMC goal output
				glev.execute(this, aslanSpec, sb);
		}
  }

	public String listCommunication() {
		if (communication.size() == 0)
			return "";
		StringBuffer sb = new StringBuffer();
		sb.append("\n");
		sb.append("MESSAGES:\n");
		for (CommunicationEvent comm : communication) {
			String sender   =  comm.getSender  ().getRepresentationNice(this, aslanSpec);
			String receiver =  comm.getReceiver().getRepresentationNice(this, aslanSpec);
			String channel  = (comm.getChannel () != null ?
					           comm.getChannel ().getRepresentationNice(this, aslanSpec) : null);
			String payload  =  comm.getPayload ().getRepresentationNice(this, aslanSpec);
			// non-actor side has < >
			if (comm.isSend()) {
				sender   = " " + sender   + " ";
				receiver = "<" + receiver + ">";
			}
			else {
				sender   = "<" + sender   + ">";
				receiver = " " + receiver + " ";
			}
			sb.append(String.format("%-12s %-5s %-12s : %s", sender, 
					(channel != null ? "-"+channel+"->" : 
						(comm.getChannelType().arrow.charAt(0) == '-' ? " " : "")
						+comm.getChannelType().arrow), 
					receiver, payload)).append("\n");
		}
		return sb.toString();
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		for (ExecutionEventsTraceTransition trans : transitions) {
			sb.append(trans.toString());
		}
		return sb.toString();
	}

	public static ExecutionEventsBatch parseMetaInfo(IRepresentable repr, Map<Variable, IGroundTerm> assigned, ErrorGatherer err) {
		ExecutionEventsBatch batch = new ExecutionEventsBatch();
		for (ICommentEntry comm : repr.getCommentLines()) {
			if (comm instanceof MetaInfo) {
				IExecutionEvent step = null;
				MetaInfo mi = (MetaInfo) comm;
				LocationInfo loc = mi.getLocation(err);
				if (mi.getName().equals(MetaInfo.NEW_INSTANCE)) {
					step = NewEntityInstanceEvent.fromMetaInfo(mi, err, assigned);
				}
				else if (mi.getName().equals(MetaInfo.ASSIGNMENT) || mi.getName().equals(MetaInfo.FRESH) || mi.getName().equals(MetaInfo.MATCH) || mi.getName().equals(MetaInfo.STEP_LABEL)) {
					step = AssignmentOrFreshEvent.fromMetaInfo(mi, err, assigned);
				}
				else if (mi.getName().equals(MetaInfo.INTRODUCE) || mi.getName().equals(MetaInfo.RETRACT)) {
					step = IntroducedOrRetractFactEvent.fromMetaInfo(mi, err, assigned);
				}
				else if (mi.getName().equals(MetaInfo.GUARD)) {
					step = GuardPassedEvent.fromMetaInfo(mi, err, assigned);
				}
				else if (mi.getName().equals(MetaInfo.COMMUNICATION) || mi.getName().equals(MetaInfo.COMMUNICATION_GUARD)) {
					step = CommunicationEvent.fromMetaInfo(mi, err, assigned);
				}
				if (step != null) {
					batch.add(step);
				}
				else {
					err.addWarning(loc, OutputFormatErrorMessages.UNRECOGNIZED_METAINFO, mi.getName());
				}
			}
		}
		return batch;
	}

	public static ClauseFiredEvent parseClauseMetaInfo(HornClause clause, Map<Variable, IGroundTerm> assigned, ErrorGatherer err) {
		ClauseFiredEvent event = null;
		for (ICommentEntry comm : clause.getCommentLines()) {
			if (comm instanceof MetaInfo) {
				MetaInfo mi = (MetaInfo) comm;
				LocationInfo loc = mi.getLocation(err);
				if (mi.getName().equals(MetaInfo.HORN_CLAUSE)) {
					if (event != null) {
						err.addError(loc, OutputFormatErrorMessages.MULTIPLE_METAINFO, mi.getName());
					}
					else {
						event = ClauseFiredEvent.fromMetaInfo(clause, mi, err, assigned);
					}
				}
				else {
					err.addWarning(loc, OutputFormatErrorMessages.UNRECOGNIZED_METAINFO, mi.getName());
				}
			}
		}
		if (event == null) {
			event = new ClauseFiredEvent(null, clause.getName(), 0);
			for (Variable v : clause.getParameters()) {
				IGroundTerm t = GroundTermBuilder.fromString(v.getName());
				if (t == null) {
					err.addException(v.getLocation(), OutputFormatErrorMessages.INVALID_TERM_ENCODING, v.getName());
				}
				t = t.reduce(assigned);
				event.put(v.getName(), t);
			}
		}
		return event;
	}

	public static GoalViolatedEvent parseGoalMetaInfo(IParameterized clause, Map<Variable, IGroundTerm> assigned, ErrorGatherer err) {
		GoalViolatedEvent event = null;
		for (ICommentEntry comm : clause.getCommentLines()) {
			if (comm instanceof MetaInfo) {
				MetaInfo mi = (MetaInfo) comm;
				LocationInfo loc = mi.getLocation(err);
				if (mi.getName().equals(MetaInfo.GOAL)) {
					if (event != null) {
						err.addError(loc, OutputFormatErrorMessages.MULTIPLE_METAINFO, mi.getName());
					}
					else {
						event = GoalViolatedEvent.fromMetaInfo(clause, mi, err, assigned);
					}
				}
				else {
					err.addWarning(loc, OutputFormatErrorMessages.UNRECOGNIZED_METAINFO, mi.getName());
				}
			}
		}
		return event;
	}
}
