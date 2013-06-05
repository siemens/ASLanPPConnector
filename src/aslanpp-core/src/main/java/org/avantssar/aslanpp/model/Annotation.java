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
import java.util.List;

import org.avantssar.commons.LocationInfo;

public class Annotation {
	private String name;
	private IOwned goal;
	private List<ITerm> knowers; // for secrecy goals
	private ITerm sender  ; // for channel goals
	private ITerm receiver; // for channel goals
	
	public Annotation(String name) {
		this.name = name;
	}

	public String toString() {
		return name;
	}
	
	private boolean termsWeakEqual(ITerm t1, ITerm t2) {
		if (t1 == null || t2 == null || t1.equals(t2))
			return true;
		if (!(t1 instanceof VariableTerm) || 
			!(t2 instanceof VariableTerm))
			return false;
		VariableTerm v1 = (VariableTerm)t1;
		VariableTerm v2 = (VariableTerm)t2;
		return (!v1.isMatched() && v2.isMatched() &&
				(v1.getSymbol().equals(v2.getSymbol())));
	}
	// used by AbstractTerm.visit()
	public void visit(ITerm term) {
		Entity current = term.getScope().findFirstEntity();
		LocationInfo loc = term.getLocation();
		if (current == null) { // should not happen
			term.getScope().getErrorGatherer().addError(loc, ErrorMessages.ANNOTATED_TERM_NOT_IN_ENTITY, term.getRepresentation());
			return;
		}	
		goal = current.getEntryInHierarchyMultipleTypes(name, SessionSecrecyGoal.class, SessionChannelGoal.class);
		if (goal == null || !(goal.getOwner() instanceof Entity)) {
			current.getErrorGatherer().addWarning(loc, ErrorMessages.MISSING_GOAL_FOR_ANNOTATION, name, current.getOriginalName());
			return;
		}
		if (!(goal instanceof SessionSecrecyGoal) && CommunicationTerm.active == null) {
			current.getErrorGatherer().addWarning(loc, ErrorMessages.CHANNEL_GOAL_ANNOTATION_NOT_IN_PAYLOAD, name);
		}
		Entity session = (Entity)goal.getOwner();
		if (goal instanceof SessionSecrecyGoal) {
			SessionSecrecyGoal secrGoal = (SessionSecrecyGoal) goal;
			secrGoal.used++;
			knowers = new ArrayList<ITerm>();
			for (ITerm t : secrGoal.getAgents()) {
				if (t != null) {
					ITerm mapped = session.mapArgTerm(false, goal.getOriginalName(), t, current);
					if (mapped != null) {// otherwise, error already reported
						knowers.add(mapped);
					}
				}
			}
		}
		else {
			SessionChannelGoal chGoal = (SessionChannelGoal) goal;
			boolean undirectedAuth = chGoal.hasUndirectedAuthentication();
			sender  =  session.mapArgTerm(true, goal.getOriginalName(), chGoal.getSender  (), current);
			receiver = (undirectedAuth ? null :
				       session.mapArgTerm(true, goal.getOriginalName(), chGoal.getReceiver(), current));
			if (undirectedAuth && !(chGoal.getReceiver() instanceof UnnamedMatchTerm)) {
				current.getErrorGatherer().addError(chGoal.getReceiver().getLocation(), ErrorMessages.UNDIRECTED_AUTH_MUST_USE_DUMMY_RECEIVER);
			}
			if (true) { //sender != null && (receiver != null || undirectedAuth)) { // otherwise, error already reported
				ITerm actor = current.getActorSymbol().term();
				ITerm activeSender   = (CommunicationTerm.active == null ? null :
						                CommunicationTerm.active.getSender  ());
				ITerm activeReceiver = (CommunicationTerm.active == null ? null : 
						                CommunicationTerm.active.getReceiver());
				boolean   senderIsActor = actor.equals(  sender);
				boolean receiverIsActor = actor.equals(receiver);
				boolean   activesenderIsActor = (  sender == null && actor.equals(activeSender  ));
				boolean activereceiverIsActor = (receiver == null && actor.equals(activeReceiver));
				if (  senderIsActor || activesenderIsActor)
					chGoal.usedSender++;
				if (receiverIsActor || activereceiverIsActor || undirectedAuth)
					chGoal.usedReceiver++;
				if (!termsWeakEqual(sender, activeSender)) {
					current.getErrorGatherer().addWarning(CommunicationTerm.active.getLocation(), 
							ErrorMessages.CHANNEL_GOAL_MISMATCH, chGoal.getOriginalName(), "sender", sender);
				}
				if (!termsWeakEqual(receiver, activeReceiver)) {
					current.getErrorGatherer().addWarning(CommunicationTerm.active.getLocation(), 
							ErrorMessages.CHANNEL_GOAL_MISMATCH, chGoal.getOriginalName(), "receiver", receiver);
				}
				if (chGoal.hasSecrecy()) {
					if (undirectedAuth) {
						current.getErrorGatherer().addError(chGoal.getReceiver().getLocation(), ErrorMessages.UNDIRECTED_AUTH_NOT_FOR_CHANNEL_SECRECY_GOAL);
					}
					if (! senderIsActor && !  activesenderIsActor && 
						!receiverIsActor&& !activereceiverIsActor) {
						current.getErrorGatherer().addError(loc, ErrorMessages.ACTOR_TERM_ON_NO_SIDE_OF_CHANNEL_GOAL,
								sender != null ? "computed " : "", receiver != null ? "computed " : "");
					}
				}
				if (chGoal.hasAuthentication() || chGoal.hasFreshness()) {
					if (senderIsActor || activesenderIsActor) { // Actor on sender side
						if (receiverIsActor || activereceiverIsActor) {
							current.getErrorGatherer().addError(loc, ErrorMessages.ACTOR_TERM_ON_BOTH_SIDES_OF_CHANNEL_GOAL,
									sender != null ? "computed " : "", receiver != null ? "computed " : "");
						}
					}
					else { // Actor should be on receiver side
						if (undirectedAuth) {
							if (!actor.equals(activeReceiver)) {
								current.getErrorGatherer().addError(loc, ErrorMessages.ACTOR_TERM_ON_NO_SIDE_OF_CHANNEL_GOAL,
										sender != null ? "computed " : "", receiver != null ? "computed " : "");
							}
						}
						else if (!receiverIsActor && !activereceiverIsActor) {
							current.getErrorGatherer().addError(loc, ErrorMessages.ACTOR_TERM_ON_NO_SIDE_OF_CHANNEL_GOAL,
									sender != null ? "computed " : "", receiver != null ? "computed " : "");
						}
					}
				}
				if(  sender == null)   sender = activeSender;
				if(receiver == null) receiver = activeReceiver;
			}
		}
	}
	
	private void handleSecrecy(Entity session, Entity child, List<ITerm> knowers, 
		FunctionSymbol setFunction, String protName, ExpressionContext ctx, ITerm term) {
		LocationInfo loc = term.getLocation();
		Entity rootEnt = session.findRootEntity();
		FunctionTerm secrecyTerm = rootEnt.secrecyTerm(session, 
				session.getIDSymbol().term(loc, child),
				child, knowers, term/* = payload*/, setFunction, protName, loc);
		// System.out.println("secrecy term: " + secrecyTerm.getRepresentation());

		ctx.addSessionGoalTerm(secrecyTerm);
		for(ITerm t : session.childChain(loc, child)) {
			ctx.addSessionGoalTerm(t);
		}
	}

	// for new-style (i.e., session) secrecy and channel goals
	public void buildGoalContext(ExpressionContext ctx, ITerm term) {
		Entity current = term.getScope().findFirstEntity();
		LocationInfo loc = term.getLocation();
		if (current == null || goal == null || !(goal.getOwner() instanceof Entity)) { // maybe due to disabled goal, which is warned for.
			if (goal != null) // should not get here!
				current.getErrorGatherer().addError(loc, ErrorMessages.INTERNAL_ERROR_ANNOTATION_LOST, this.name, term.getRepresentation());
			return;
		}
		Entity session = (Entity)goal.getOwner();
		IScope root = session.findRoot();
		if (goal instanceof SessionSecrecyGoal) {
			SessionSecrecyGoal secrGoal = (SessionSecrecyGoal) goal;
			handleSecrecy(session, current, knowers, secrGoal.getSetFunction(), secrGoal.getSecrecyProtocolName(), ctx, term);
		}
		else {
			SessionChannelGoal chGoal = (SessionChannelGoal) goal;
			boolean undirectedAuth = chGoal.hasUndirectedAuthentication();
			ITerm actor = current.getActorSymbol().term();
			boolean   senderIsActor = actor.equals(sender);
			boolean receiverIsActor = actor.equals(receiver);
			{
				if (sender != null && (receiver != null || undirectedAuth)) {
					if (chGoal.hasSecrecy()) {
						List<ITerm> knowers = new ArrayList<ITerm>();
						knowers.add(sender);
						knowers.add(receiver);
						handleSecrecy(session, current, knowers, chGoal.getSetFunction(), chGoal.getSecrecyProtocolName(), ctx, term);
						// retract secrecy on receive (i.e. add intruder to set of knowers):
						if (receiverIsActor) {
							ITerm retractSecr = root.findFunction(Prelude.ADD).term(chGoal.getSetFunction().term(session.getIDSymbol().term()),
									root.findConstant(Prelude.INTRUDER).term());
							ctx.addSessionGoalTerm(retractSecr);
						}
					}
					if (chGoal.hasAuthentication() || chGoal.hasFreshness()) {
						List<ITerm> toAdd = new ArrayList<ITerm>();
						ConstantSymbol cAuthProt = null;
						if (chGoal.hasAuthentication()) {
							cAuthProt = session.findRootEntity().findConstant(chGoal.getAuthenticationProtocolName());
						}
						ConstantSymbol cFreshProt = null;
						if (chGoal.hasFreshness()) {
							cFreshProt = session.findRootEntity().findConstant(chGoal.getFreshnessProtocolName());
						}
						if (senderIsActor) { // sender side
							if (chGoal.hasAuthentication()) {
								toAdd.add(root.findFunction(Prelude.WITNESS).term(actor, 
										chGoal.hasUndirectedAuthentication() ? root.findConstant(Prelude.INTRUDER).term()
												                             : receiver,
										cAuthProt.term(), term));
							}
/*							if (chGoal.hasFreshness()) {
								toAdd.add(root.findFunction(Prelude.WITNESS).term(mappedSender.term(), <ceiver.term(), cFreshProt.term(), this));
							}*/
						}
						else { // receiver side
							if (chGoal.hasAuthentication()) {
								toAdd.add(root.findFunction(Prelude.REQUEST).term(actor,
										sender, cAuthProt.term(), term,
										current.getIDSymbol().term()));
							}
							if (chGoal.hasFreshness()) {
								toAdd.add(root.findFunction(Prelude.REQUEST).term(actor,
										sender, cFreshProt.term(), term,
										current.getIDSymbol().term()));
							}
						}
						for (ITerm t : toAdd) {
							ctx.addSessionGoalTerm(t);
						}
					}
				}
			}
		}
	}
}
