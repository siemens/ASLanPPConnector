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

import org.avantssar.aslan.MetaInfo;
import org.avantssar.aslan.RewriteRule;
import org.avantssar.aslanpp.model.Entity;
import org.avantssar.aslanpp.model.ITerm;
import org.avantssar.aslanpp.model.SymbolsState;
import org.avantssar.commons.ChannelEntry;
import org.avantssar.commons.LocationInfo;

public class CommunicationEdge extends IntroduceRetractEdge {

	private final ITerm sender;
	private final ITerm receiver;
	private final ITerm payload;
	private final ITerm channel;
	private final ChannelEntry channelType;
	private final ITerm expanded;
	private final boolean send;

	private CommunicationEdge(Entity ownerEntity, INode sourceNode, ITerm sender, ITerm receiver, ITerm payload, ITerm channel, ChannelEntry chType, ITerm fact, boolean send, LocationInfo location,
			ASLanBuilder builder) {
		super(ownerEntity, sourceNode, fact, send, location, builder);
		this.sender = sender;
		this.receiver = receiver;
		this.payload = payload;
		this.channel = channel;
		this.channelType = chType;
		this.expanded = fact;
		this.send = send;
	}

	private CommunicationEdge(CommunicationEdge old, INode sourceNode) {
		super(old, sourceNode);
		this.sender = old.sender;
		this.receiver = old.receiver;
		this.payload = old.payload;
		this.channel = old.channel;
		this.channelType = old.channelType;
		this.expanded = old.expanded;
		this.send = old.send;
	}

	@Override
	protected String getGraphvizPrefix() {
		return "C";
	}

	@Override
	protected IEdge recreate(INode sourceNode) {
		return new CommunicationEdge(this, sourceNode);
	}

	@Override
	protected boolean skipMetaInfo() {
		return true;
	}

	@Override
	protected void doIt(RewriteRule tr, SymbolsState symState, boolean contribute) {
		super.doIt(tr, symState, contribute);
		symState.push();
		MetaInfo mi = startMetaInfo(tr, MetaInfo.COMMUNICATION);
		mi.addParameter(MetaInfo.SENDER, builder.transform(solveReduce(sender, symState, false)).getRepresentation());
		mi.addParameter(MetaInfo.RECEIVER, builder.transform(solveReduce(receiver, symState, !send)).getRepresentation());
		mi.addParameter(MetaInfo.PAYLOAD, builder.transform(solveReduce(payload, symState, !send)).getRepresentation());
		mi.addParameter(MetaInfo.CHANNEL, builder.transform(solveReduce(channel, symState, !send)).getRepresentation());
		ITerm redExp = solveReduce(expanded, symState, !send);
		mi.addParameter(MetaInfo.FACT, builder.transform(redExp).getRepresentation());
		mi.addParameter(MetaInfo.DIRECTION, send ? MetaInfo.SEND : MetaInfo.RECEIVE);
		symState.pop();
		// addMatchesAndAuxiliary(tr, expanded);
	}

}
