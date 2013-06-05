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

import java.util.Map;
import org.avantssar.aslan.IASLanSpec;
import org.avantssar.aslan.MetaInfo;
import org.avantssar.aslan.Variable;
import org.avantssar.commons.ChannelEntry;
import org.avantssar.commons.ErrorGatherer;
import org.avantssar.commons.LocationInfo;

public class CommunicationEvent extends AbstractExecutionEvent {

	private static final String LABEL = "communication";

	private final IGroundTerm sender;
	private final IGroundTerm receiver;
	private final IGroundTerm payload;
	private final IGroundTerm channel;
	private final ChannelEntry channelType;
	private final boolean send;
	private final IGroundTerm expandedFact;
	private final boolean guard;

	protected CommunicationEvent(String entity, IGroundTerm iid, int lineNumber, IGroundTerm sender, IGroundTerm receiver, IGroundTerm payload, IGroundTerm channel, ChannelEntry channelType, boolean send,
			IGroundTerm expandedFact, boolean guard) {
		super(entity, iid, lineNumber);
		this.sender = sender;
		this.receiver = receiver;
		this.payload = payload;
		this.channel = channel;
		this.channelType = channelType;
		this.send = send;
		this.expandedFact = expandedFact;
		this.guard = guard;
	}

	public IGroundTerm getSender() {
		return sender;
	}

	public IGroundTerm getReceiver() {
		return receiver;
	}

	public IGroundTerm getPayload() {
		return payload;
	}

	public IGroundTerm getChannel() {
		return channel;
	}

	public ChannelEntry getChannelType() {
		return channelType;
	}

	public boolean isSend() {
		return send;
	}

	public IGroundTerm getExpandedFact() {
		return expandedFact;
	}

	public boolean isGuard() {
		return guard;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof CommunicationEvent) {
			CommunicationEvent ce = (CommunicationEvent) o;
			// send and guard are ignored
			return this.sender.equals(ce.sender) && this.receiver.equals(ce.receiver) && this.payload.equals(ce.payload)  && this.channel.equals(ce.channel) && this.channelType.equals(ce.channelType)
					&& this.expandedFact.equals(ce.expandedFact);
		}
		return false;
	}

	@Override
	public void execute(ExecutionScenario exec, IASLanSpec aslanSpec, ErrorGatherer err, StringBuffer sb, boolean dontPrint) {
		// System.out.println("executing " + toString());
		exec.trackCommunication(this, sb, dontPrint);
	}

	public String describe(ISetProvider setProvider, IASLanSpec aslanSpec) {
		return sender.getRepresentationNice(setProvider, aslanSpec) + " " + /*TODO*/channelType.arrow + " " + receiver.getRepresentationNice(setProvider, aslanSpec) + " : "
				+ payload.getRepresentationNice(setProvider, aslanSpec);
	}

	public String comment() {
		return "  % " + (guard ? "from guard " : "") + "on line " + getLineNumber();
	}

	public String abbrev() {
		if (guard) {
			return "g";
		}
		else if (send) {
			return "s";
		}
		else {
			return "r";
		}
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("[").append(LABEL).append("; ");
		super.fillBasic(sb);
		sb.append("sender: ").append(sender.getRepresentation(null)).append("; ");
		sb.append("receiver: ").append(receiver.getRepresentation(null)).append("; ");
		sb.append("payload: ").append(payload.getRepresentation(null)).append("; ");
		sb.append("channel: ").append(channel == null ? channelType :
									  payload.getRepresentation(null)).append("; ");
		sb.append("direction: ").append(send ? MetaInfo.SEND : MetaInfo.RECEIVE).append("; ");
		sb.append("fact: ").append(expandedFact.getRepresentation(null)).append("; ");
		sb.append("guard: ").append(guard).append("; ");
		sb.append("]");
		return sb.toString();
	}

	public static CommunicationEvent fromMetaInfo(MetaInfo mi, ErrorGatherer err, Map<Variable, IGroundTerm> assigned) {
		LocationInfo loc = mi.getLocation(err);
		Bundle b = extractBasicFromMetaInfo(mi, err, assigned, new String[] { MetaInfo.COMMUNICATION, MetaInfo.COMMUNICATION_GUARD }, LABEL);
		IGroundTerm sender   = str2termEx(mi, MetaInfo.SENDER  , err, assigned, "Sender"  , LABEL);
		IGroundTerm receiver = str2termEx(mi, MetaInfo.RECEIVER, err, assigned, "Receiver", LABEL);
		IGroundTerm payload  = str2termEx(mi, MetaInfo.PAYLOAD , err, assigned, "Payload" , LABEL);
		IGroundTerm channel  = null;
							 //str2termEx(mi, MetaInfo.CHANNEL , err, assigned, "Channel" , LABEL); 
		String chType = mi.getParameters().get(MetaInfo.CHANNEL);
		ChannelEntry /*ch;
		if (chType == null) {
			ch = ChannelEntry.regular;
		}
		else {*/
		ch = ChannelEntry.getByKey(chType, false);
		if (ch == null) {
			channel = str2term(chType, err, assigned);
			ch = ChannelEntry.getByKey(chType, true);
			if (ch == null) {
				err.addException(loc, OutputFormatErrorMessages.INVALID_CHANNEL_TYPE, chType);
			}
		}
		String dir = mi.getParameters().get(MetaInfo.DIRECTION);
		if (dir == null) {
			err.addException(loc, OutputFormatErrorMessages.MISSING_AT, "Direction", LABEL);
		}
		IGroundTerm expandedFact = str2termEx(mi, MetaInfo.FACT, err, assigned, "Expanded fact", LABEL);
		return new CommunicationEvent(b.entity, b.iid, b.lineNumber, sender, receiver, payload, channel, ch, dir.equals(MetaInfo.SEND), expandedFact, mi.getName().equals(MetaInfo.COMMUNICATION_GUARD));
	}

}
