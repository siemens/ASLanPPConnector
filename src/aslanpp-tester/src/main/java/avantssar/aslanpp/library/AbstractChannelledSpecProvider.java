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

package avantssar.aslanpp.library;

import org.avantssar.aslan.IASLanSpec;
import org.avantssar.commons.ChannelEntry;
import org.avantssar.commons.ChannelModel;
import org.avantssar.commons.ChannelEntry.Type;
import avantssar.aslanpp.testing.IChannelTypeFlexibleSpecProvider;

public abstract class AbstractChannelledSpecProvider extends AbstractSpecProvider implements IChannelTypeFlexibleSpecProvider {

	protected ChannelEntry channelType = ChannelEntry.regular;

	@Override
	public void setChannelType(ChannelEntry channelType) {
		this.channelType = channelType;
	}

	public ChannelEntry getChannelType() {
		return channelType;
	}

	private org.avantssar.aslan.ITerm receiveCCM(org.avantssar.aslan.ITerm receiver, org.avantssar.aslan.ITerm sender, org.avantssar.aslan.ITerm pseudo, org.avantssar.aslan.ITerm payload) {
		return sendCCM(sender, pseudo, receiver, payload);
	}

	private org.avantssar.aslan.ITerm sendCCM(org.avantssar.aslan.ITerm sender, org.avantssar.aslan.ITerm pseudo, org.avantssar.aslan.ITerm receiver, org.avantssar.aslan.ITerm payload) {
		if (pseudo.getRepresentation().equals(sender.getRepresentation())) {
			if (channelType.type == Type.Confidential) {
				return IASLanSpec.IKNOWS.term(IASLanSpec.CRYPT.term(fCK.term(receiver), IASLanSpec.PAIR.term(ccmConf.term(), payload)));
			}
			else if (channelType.type == Type.Secure) {
				return IASLanSpec.IKNOWS.term(IASLanSpec.CRYPT.term(fCK.term(receiver), IASLanSpec.SIGN.term(IASLanSpec.INV.term(fAK.term(pseudo)), IASLanSpec.PAIR.term(ccmSec.term(), IASLanSpec.PAIR
						.term(receiver, payload)))));
			}
			else if (channelType.type == Type.Authentic) {
				return IASLanSpec.IKNOWS.term(IASLanSpec.SIGN.term(IASLanSpec.INV.term(fAK.term(pseudo)), IASLanSpec.PAIR.term(ccmAuth.term(), IASLanSpec.PAIR.term(receiver, payload))));
			}
			else {
				return IASLanSpec.IKNOWS.term(payload);
			}
		}
		else {
			if (channelType.type == Type.Confidential) {
				return IASLanSpec.IKNOWS.term(IASLanSpec.CRYPT.term(fPK.term(receiver), IASLanSpec.PAIR.term(ccmConf.term(), payload)));
			}
			else if (channelType.type == Type.Secure) {
				return IASLanSpec.IKNOWS.term(IASLanSpec.PAIR.term(pseudo, IASLanSpec.CRYPT.term(fPK.term(receiver), IASLanSpec.SIGN.term(IASLanSpec.INV.term(pseudo), IASLanSpec.PAIR.term(ccmSec
						.term(), IASLanSpec.PAIR.term(receiver, payload))))));
			}
			else if (channelType.type == Type.Authentic) {
				return IASLanSpec.IKNOWS.term(IASLanSpec.PAIR.term(pseudo, IASLanSpec.SIGN.term(IASLanSpec.INV.term(pseudo), IASLanSpec.PAIR.term(ccmAuth.term(), IASLanSpec.PAIR.term(receiver,
						payload)))));
			}
			else {
				return IASLanSpec.IKNOWS.term(payload);
			}
		}
	}

	protected org.avantssar.aslan.ITerm doSend(ChannelModel cm, org.avantssar.aslan.ITerm sender, org.avantssar.aslan.ITerm pseudo, org.avantssar.aslan.ITerm receiver,
			org.avantssar.aslan.ITerm payload) {
		return doSend(cm, sender, pseudo, receiver, payload, null);
	}

	protected org.avantssar.aslan.ITerm doSend(ChannelModel cm, org.avantssar.aslan.ITerm sender, org.avantssar.aslan.ITerm pseudo, org.avantssar.aslan.ITerm receiver,
			org.avantssar.aslan.ITerm payload, org.avantssar.aslan.ITerm channel) {
		if (cm == ChannelModel.ACM) {
			return sendACM(sender, pseudo, receiver, payload, channel);
		}
		else if (cm == ChannelModel.CCM) {
			return sendCCM(sender, pseudo, receiver, payload);
		}
		else {
			return null;
		}
	}

	protected org.avantssar.aslan.ITerm doReceive(ChannelModel cm, org.avantssar.aslan.ITerm receiver, org.avantssar.aslan.ITerm sender, org.avantssar.aslan.ITerm pseudo,
			org.avantssar.aslan.ITerm payload) {
		return doReceive(cm, receiver, sender, pseudo, payload, null);
	}

	protected org.avantssar.aslan.ITerm doReceive(ChannelModel cm, org.avantssar.aslan.ITerm receiver, org.avantssar.aslan.ITerm sender, org.avantssar.aslan.ITerm pseudo,
			org.avantssar.aslan.ITerm payload, org.avantssar.aslan.ITerm channel) {
		if (cm == ChannelModel.ACM) {
			return receiveACM(receiver, pseudo, payload, channel);
		}
		else if (cm == ChannelModel.CCM) {
			return receiveCCM(receiver, sender, pseudo, payload);
		}
		else {
			return null;
		}

	}

	private org.avantssar.aslan.ITerm sendACM(org.avantssar.aslan.ITerm sender, org.avantssar.aslan.ITerm pseudo, org.avantssar.aslan.ITerm receiver, org.avantssar.aslan.ITerm payload,
			org.avantssar.aslan.ITerm channel) {
		org.avantssar.aslan.ITerm finalChannel;
		if (channel != null) {
			finalChannel = channel;
		}
		else {
			org.avantssar.aslan.Constant chtype = acmChannelType();
			finalChannel = acmChFnc.term(pseudo, receiver, chtype.term());
		}
		return acmSent.term(sender, pseudo, receiver, payload, finalChannel);
	}

	private org.avantssar.aslan.ITerm receiveACM(org.avantssar.aslan.ITerm receiver, org.avantssar.aslan.ITerm pseudo, org.avantssar.aslan.ITerm payload, org.avantssar.aslan.ITerm channel) {
		org.avantssar.aslan.ITerm finalChannel;
		if (channel != null) {
			finalChannel = channel;
		}
		else {
			org.avantssar.aslan.Constant chtype = acmChannelType();
			finalChannel = acmChFnc.term(pseudo, receiver, chtype.term());
		}
		return acmRcvd.term(receiver, pseudo, payload, finalChannel);
	}

	private org.avantssar.aslan.Constant acmChannelType() {
		if (channelType.type == Type.Authentic) {
			return cACM_auth;
		}
		else if (channelType.type == Type.Secure) {
			return cACM_sec;
		}
		else if (channelType.type == Type.Confidential) {
			return cACM_conf;
		}
		else {
			return cACM_regular;
		}
	}
}
