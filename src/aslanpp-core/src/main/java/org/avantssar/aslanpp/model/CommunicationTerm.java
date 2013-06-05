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

import org.avantssar.aslanpp.visitors.IASLanPPVisitor;
import org.avantssar.commons.ChannelEntry;
import org.avantssar.commons.LocationInfo;
import org.avantssar.commons.ChannelEntry.Type;

public class CommunicationTerm extends AbstractTerm {

	public static class Statistics {

		public int sends;
		public int receives;

		public void add(Statistics s) {
			sends += s.sends;
			receives += s.receives;
		}
	};

	public enum Direction {
		Send, Receive;
	};

	private ITerm realSource;
	private final ITerm source;
	private final ITerm target;
	public static CommunicationTerm active;
	private final ITerm payload;
	// private final boolean resilient;
	// private final boolean fresh;
	// private final Type type;
	private final ITerm channel;
	private final ChannelEntry chType;
	private boolean sending;
	private final boolean receiveHint;
	private ITerm processedTerm;
	private final boolean renderAsFunction;
	private final boolean renderOOPStyle;

	public CommunicationTerm(LocationInfo location, IScope scope, ITerm source, ITerm target, ITerm payload, ITerm channel, ChannelEntry chType, boolean receiveHint, boolean renderAsFunction, boolean renderOOPStyle) {
		super(location, scope, false);
		this.source = source;
		this.target = target;
		this.payload = payload;
		this.channel = channel;
		this.chType = chType;
		this.receiveHint = receiveHint;
		this.renderAsFunction = renderAsFunction;
		this.renderOOPStyle = renderOOPStyle;

		if (target.holdsActor()) {
			if (source.holdsActor()) {
				sending = !receiveHint;
			}
			else {
				sending = false;
			}
		}
		else {
			if (source.holdsActor()) {
				sending = true;
			}
			else {
				sending = !receiveHint;
			}
		}
		setDiscardOnRHS(!sending);

		realSource = null;
		if (source instanceof PseudonymTerm) {
			PseudonymTerm pseudoSource = (PseudonymTerm) source;
			realSource = pseudoSource.getOriginalAgent();
		}
		else if (source instanceof DefaultPseudonymTerm) {
			DefaultPseudonymTerm pseudoSource = (DefaultPseudonymTerm) source;
			realSource = pseudoSource.getBaseTerm();
		}
		else {
			realSource = source;
		}

	}

	public ITerm getRealSender() {
		return realSource;
	}

	public ITerm getSender() {
		return source;
	}

	public ITerm getReceiver() {
		return target;
	}

	public ITerm getPayload() {
		return payload;
	}

	public Type getType() {
		return chType.type;
	}

	public boolean isReceive() {
		return !sending;
	}

	public boolean isRenderAsFunction() {
		return renderAsFunction;
	}

	public boolean isRenderOOPStyle() {
		return renderOOPStyle;
	}

	public ITerm getChannel() {
		return channel;
	}

	public ChannelEntry getChannelType() {
		return chType;
	}

	public boolean isUndirected() {
		return chType.undirected;
	}

	public boolean isResilient() {
		return chType.resilient;
	}

	public boolean getReceiveHint() {
		return receiveHint;
	}

	// @Override
	// public boolean discardOnRHS() {
	// return !sending;
	// }

	public IType inferType() {
		return getScope().findType(Prelude.FACT);
	}

	@Override
	public void buildContext(ExpressionContext ctx, boolean isInNegatedCondition) {
		// super.buildContext(ctx, isInNegatedCondition);
		// source.buildContext(ctx, isInNegatedCondition);
		// target.buildContext(ctx, isInNegatedCondition);
		// payload.buildContext(ctx, isInNegatedCondition);
		
		processedTerm.buildContext(ctx, isInNegatedCondition);

		if (sending) {
			if (ctx.isBreakpoint(Prelude.SEND)) {
				ctx.setBreakpoint();
			}
		}
		else {
			if (ctx.isBreakpoint(Prelude.RECEIVE)) {
				ctx.setBreakpoint();
			}
		}

		Statistics s = new Statistics();
		if (sending) {
			s.sends++;
		}
		else {
			s.receives++;
		}
		ctx.updateCommunicationStatistics(s);
	}

	@Override
	public void useContext(ExpressionContext ctx, SymbolsState symState) {
		source.useContext(ctx, symState);
		target.useContext(ctx, symState);
		payload.useContext(ctx, symState);
	}

	public ITerm reduce(SymbolsState symState) {
		CommunicationTerm newCT = new CommunicationTerm(getLocation(), getScope(), source.reduce(symState), target.reduce(symState), payload.reduce(symState), 
					channel == null ? null : channel.reduce(symState), chType, receiveHint, renderAsFunction,
				renderOOPStyle);
		newCT.processedTerm = this.processedTerm;
		return newCT;
	}

	@Override
	public ITerm accept(IASLanPPVisitor visitor) {
		return visitor.visit(this);
	}

	public ITerm getProcessedTerm() {
		return processedTerm;
	}

	public void setProcessedTerm(ITerm processedTerm) {
		this.processedTerm = processedTerm;
	}
}
