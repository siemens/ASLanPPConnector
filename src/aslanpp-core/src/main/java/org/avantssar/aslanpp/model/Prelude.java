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

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import org.avantssar.aslanpp.Debug;
import org.avantssar.commons.ChannelModel;

@SuppressWarnings("unused")
public class Prelude {

	public static class PredefinedTypeData {

		public String name;
		public boolean partOfPrelude;
		public String supertype;
		public boolean set;

		public PredefinedTypeData(String name, boolean partOfPrelude) {
			this(name, partOfPrelude, false);
		}

		public PredefinedTypeData(String name, boolean partOfPrelude, boolean set) {
			this(name, partOfPrelude, null);
			this.set = set;
		}

		public PredefinedTypeData(String name, boolean partOfPrelude, String supertype) {
			this.name = name;
			this.partOfPrelude = partOfPrelude;
			this.supertype = supertype;
			this.set = false;
		}

		@Override
		public String toString() {
			StringBuffer sb = new StringBuffer();
			sb.append("[").append(name);
			sb.append(", ").append(partOfPrelude);
			sb.append(", ").append(supertype);
			sb.append(", ").append(set).append("]");
			return sb.toString();
		}
	}

	private static class PredefinedConstantData {

		public String name;
		public PredefinedTypeData type;
		public boolean partOfPrelude;
		public boolean nonPublic;
		public ChannelModel cm;

		public PredefinedConstantData(String name, PredefinedTypeData type, boolean partOfPrelude, boolean nonPublic, ChannelModel cm) {
			this.name = name;
			this.type = type;
			this.partOfPrelude = partOfPrelude;
			this.nonPublic = nonPublic;
			this.cm = cm;
		}

		@Override
		public String toString() {
			StringBuffer sb = new StringBuffer();
			sb.append("[").append(name);
			sb.append(", ").append(type);
			sb.append(", ").append(partOfPrelude);
			sb.append(", ").append(nonPublic);
			sb.append(", ").append(cm).append("]");
			return sb.toString();
		}
	}

	public static class PredefinedFunctionData extends PredefinedConstantData {

		public boolean nonInvertible;
		public PredefinedTypeData[] argTypes;

		public PredefinedFunctionData(String name, PredefinedTypeData type, PredefinedTypeData[] argTypes, boolean partOfPrelude, boolean nonPublic, boolean nonInvertible, ChannelModel cm) {
			super(name, type, partOfPrelude, nonPublic, cm);
			this.nonInvertible = nonInvertible;
			this.argTypes = argTypes;
		}

		@Override
		public String toString() {
			StringBuffer sb = new StringBuffer();
			sb.append(super.toString());
			sb.delete(sb.length() - 1, sb.length());
			sb.append(", ").append(nonInvertible);
			sb.append(", {)");
			for (int i = 0; i < argTypes.length; i++) {
				if (i > 0) {
					sb.append(", ");
				}
				sb.append(argTypes[i]);
			}
			sb.append("}]");
			return sb.toString();
		}
	}

	public static final String FACT = "fact";
	public static final String PROTOCOL_ID = "protocol_id";
	public static final String MESSAGE = "message";
	public static final String TEXT = "text";
	public static final String AGENT = "agent";
	public static final String NAT = "nat";
	public static final String PRIVATE_KEY = "private_key";
	public static final String SYMMETRIC_KEY = "symmetric_key";
	public static final String CHANNEL = "channel";
	public static final String PUBLIC_KEY = "public_key";
	public static final String SLABEL = "slabel";
	public static final String SET = "set";

	private static final PredefinedTypeData TYPE_FACT = new PredefinedTypeData(FACT, true);
	private static final PredefinedTypeData TYPE_PROTOCOL_ID = new PredefinedTypeData(PROTOCOL_ID, true);
	private static final PredefinedTypeData TYPE_MESSAGE = new PredefinedTypeData(MESSAGE, true);
	private static final PredefinedTypeData TYPE_MESSAGE_SET = new PredefinedTypeData(MESSAGE, true, true);
	private static final PredefinedTypeData TYPE_TEXT = new PredefinedTypeData(TEXT, true, MESSAGE);
	private static final PredefinedTypeData TYPE_AGENT = new PredefinedTypeData(AGENT, true, MESSAGE);
	private static final PredefinedTypeData TYPE_AGENT_SET = new PredefinedTypeData(AGENT, true, true);
	private static final PredefinedTypeData TYPE_NAT = new PredefinedTypeData(NAT, true, MESSAGE);
	private static final PredefinedTypeData TYPE_PRIVATE_KEY = new PredefinedTypeData(PRIVATE_KEY, true, MESSAGE);
	private static final PredefinedTypeData TYPE_SYMMETRIC_KEY = new PredefinedTypeData(SYMMETRIC_KEY, true, MESSAGE);
	private static final PredefinedTypeData TYPE_CHANNEL = new PredefinedTypeData(CHANNEL, false);
	private static final PredefinedTypeData TYPE_PUBLIC_KEY = new PredefinedTypeData(PUBLIC_KEY, true, AGENT);
	private static final PredefinedTypeData TYPE_SLABEL = new PredefinedTypeData(SLABEL, true, TEXT);
	private static final Map<IType, SetType> sets = new HashMap<IType, SetType>();

	public static final String TRUE = "true";
	public static final String FALSE = "false";
	public static final String INTRUDER = "i";
	public static final String ROOT = "root";
	// public static final String CH_AUTHENTIC = "authCh";
	// public static final String CH_CONFIDENTIAL = "confCh";
	// public static final String CH_SECURE = "secCh";
	// public static final String CH_RESILIENT = "resCh";
	// public static final String CH_RESILIENT_AUTHENTIC = "res_authCh";
	// public static final String CH_RESILIENT_CONFIDENTIAL = "res_confCh";
	// public static final String CH_RESILIENT_SECURE = "res_secCh";
	public static final String CH_TAG_SECURE = "stag";
	public static final String CH_TAG_AUTHENTIC = "atag";
	public static final String CH_TAG_CONFIDENTIAL = "ctag";
	public static final String ACM_CH_REGULAR = "regular";
	public static final String ACM_CH_AUTHENTIC = "auth";
	public static final String ACM_CH_CONFIDENTIAL = "conf";
	public static final String ACM_CH_SECURE = "sec";
	public static final String ACM_CH_RESILIENT_REGULAR = "res";
	public static final String ACM_CH_RESILIENT_AUTHENTIC = "res_auth";
	public static final String ACM_CH_RESILIENT_CONFIDENTIAL = "res_conf";
	public static final String ACM_CH_RESILIENT_SECURE = "res_sec";

	public static final String SECRET = "secret";
	public static final String HEAR = "hear";
	public static final String WHISPER = "whisper";
	public static final String REQUEST = "request";
	public static final String WITNESS = "witness";
	public static final String DESCENDANT = "descendant";
	public static final String CHILD = "child";
	public static final String DISHONEST = "dishonest";
	// public static final String IS_AGENT = "isAgent";
	public static final String CONFIDENTIALITY_KEY = "ck";
	public static final String AUTHENTICATION_KEY = "ak";
	public static final String PK = "pk";
	public static final String SIGN = "sign";
	public static final String PAIR = "pair";
	public static final String SCRYPT = "scrypt";
	public static final String CRYPT = "crypt";
	public static final String INV = "inv";
	public static final String IKNOWS = "iknows";
	public static final String REMOVE = "remove";
	public static final String ADD = "add";
	public static final String CONTAINS = "contains";
	public static final String SENT = "sent";
	public static final String RCVD = "rcvd";
	public static final String ACM_CHANNEL = "ch";
	public static final String RECEIVE = "receive";
	public static final String SEND = "send";
	public static final String DEFAULT_PSEUDONYM = "defaultPseudonym";
	public static final String HASH = "hash";
	public static final String SUCC = "succ";
	public static final String LEQ = "leq";
	// ACM channel related facts
	public static final String ACM_CONFIDENTIAL_TO = "confidential_to";
	public static final String ACM_WEAKLY_CONFIDENTIAL = "weakly_confidential";
	public static final String ACM_AUTHENTIC_ON = "authentic_on";
	public static final String ACM_WEAKLY_AUTHENTIC = "weakly_authentic";
	public static final String ACM_RESILIENT = "resilient";
	public static final String ACM_LINK = "link";
	public static final String ACM_BILATERAL  =  "bilateral_conf_auth";
    public static final String ACM_UNILATERAL = "unilateral_conf_auth";

	private static final PredefinedConstantData CONST_INTRUDER = new PredefinedConstantData(INTRUDER, TYPE_AGENT, true, false, null);
	private static final PredefinedConstantData CONST_ROOT = new PredefinedConstantData(ROOT, TYPE_AGENT, false, false, null);
	private static final PredefinedConstantData CONST_TRUE = new PredefinedConstantData(TRUE, TYPE_FACT, false, false, null);
	private static final PredefinedConstantData CONST_FALSE = new PredefinedConstantData(FALSE, TYPE_FACT, false, false, null);
	// private static final PredefinedConstantData CONST_CH_AUTHENTIC = new
	// PredefinedConstantData(CH_AUTHENTIC, TYPE_MESSAGE, true, true);
	// private static final PredefinedConstantData CONST_CH_CONFIDENTIAL = new
	// PredefinedConstantData(CH_CONFIDENTIAL, TYPE_MESSAGE, true, true);
	// private static final PredefinedConstantData CONST_CH_SECURE = new
	// PredefinedConstantData(CH_SECURE, TYPE_MESSAGE, true, true);
	// private static final PredefinedConstantData CONST_CH_RESILIENT = new
	// PredefinedConstantData(CH_RESILIENT, TYPE_MESSAGE, true, true);
	// private static final PredefinedConstantData CONST_CH_RESILIENT_AUTHENTIC
	// = new PredefinedConstantData(CH_RESILIENT_AUTHENTIC, TYPE_MESSAGE, true,
	// true);
	// private static final PredefinedConstantData
	// CONST_CH_RESILIENT_CONFIDENTIAL = new
	// PredefinedConstantData(CH_RESILIENT_CONFIDENTIAL, TYPE_MESSAGE, true,
	// true);
	// private static final PredefinedConstantData CONST_CH_RESILIENT_SECURE =
	// new PredefinedConstantData(CH_RESILIENT_SECURE, TYPE_MESSAGE, true,
	// true);
	private static final PredefinedConstantData CONST_CH_TAG_SECURE = new PredefinedConstantData(CH_TAG_SECURE, TYPE_SLABEL, false, false, ChannelModel.CCM);
	private static final PredefinedConstantData CONST_CH_TAG_AUTHENTIC = new PredefinedConstantData(CH_TAG_AUTHENTIC, TYPE_SLABEL, false, false, ChannelModel.CCM);
	private static final PredefinedConstantData CONST_CH_TAG_CONFIDENTIAL = new PredefinedConstantData(CH_TAG_CONFIDENTIAL, TYPE_SLABEL, false, false, ChannelModel.CCM);
	private static final PredefinedConstantData CONST_ACM_CH_REGULAR = new PredefinedConstantData(ACM_CH_REGULAR, TYPE_SLABEL, false, false, ChannelModel.ACM);
	private static final PredefinedConstantData CONST_ACM_CH_AUTHENTIC = new PredefinedConstantData(ACM_CH_AUTHENTIC, TYPE_SLABEL, false, false, ChannelModel.ACM);
	private static final PredefinedConstantData CONST_ACM_CH_CONFIDENTIAL = new PredefinedConstantData(ACM_CH_CONFIDENTIAL, TYPE_SLABEL, false, false, ChannelModel.ACM);
	private static final PredefinedConstantData CONST_ACM_CH_SECURE = new PredefinedConstantData(ACM_CH_SECURE, TYPE_SLABEL, false, false, ChannelModel.ACM);
	private static final PredefinedConstantData CONST_ACM_CH_RESILIENT_REGULAR = new PredefinedConstantData(ACM_CH_RESILIENT_REGULAR, TYPE_SLABEL, false, false, ChannelModel.ACM);
	private static final PredefinedConstantData CONST_ACM_CH_RESILIENT_AUTHENTIC = new PredefinedConstantData(ACM_CH_RESILIENT_AUTHENTIC, TYPE_SLABEL, false, false, ChannelModel.ACM);
	private static final PredefinedConstantData CONST_ACM_CH_RESILIENT_CONFIDENTIAL = new PredefinedConstantData(ACM_CH_RESILIENT_CONFIDENTIAL, TYPE_SLABEL, false, false, ChannelModel.ACM);
	private static final PredefinedConstantData CONST_ACM_CH_RESILIENT_SECURE = new PredefinedConstantData(ACM_CH_RESILIENT_SECURE, TYPE_SLABEL, false, false, ChannelModel.ACM);

	private static final PredefinedFunctionData FNC_SECRET = new PredefinedFunctionData(SECRET, TYPE_FACT, new PredefinedTypeData[] { TYPE_MESSAGE, TYPE_PROTOCOL_ID, TYPE_AGENT_SET }, true, true,
			false, null);
//	private static final PredefinedFunctionData FNC_HEAR = new PredefinedFunctionData(HEAR, TYPE_FACT, new PredefinedTypeData[] { TYPE_AGENT, TYPE_MESSAGE }, true, true, false, null);
//	private static final PredefinedFunctionData FNC_WHISPER = new PredefinedFunctionData(WHISPER, TYPE_FACT, new PredefinedTypeData[] { TYPE_AGENT, TYPE_MESSAGE }, true, true, false, null);
	private static final PredefinedFunctionData FNC_REQUEST = new PredefinedFunctionData(REQUEST, TYPE_FACT, new PredefinedTypeData[] { TYPE_AGENT, TYPE_AGENT, TYPE_PROTOCOL_ID, TYPE_MESSAGE,
			TYPE_NAT }, true, true, false, null);
	private static final PredefinedFunctionData FNC_WITNESS = new PredefinedFunctionData(WITNESS, TYPE_FACT, new PredefinedTypeData[] { TYPE_AGENT, TYPE_AGENT, TYPE_PROTOCOL_ID, TYPE_MESSAGE }, true,
			true, false, null);
//	private static final PredefinedFunctionData FNC_DESCENDANT = new PredefinedFunctionData(DESCENDANT, TYPE_FACT, new PredefinedTypeData[] { TYPE_NAT, TYPE_NAT }, false, true, false, null);
	private static final PredefinedFunctionData FNC_CHILD = new PredefinedFunctionData(CHILD, TYPE_FACT, new PredefinedTypeData[] { TYPE_NAT, TYPE_NAT }, false, true, false, null);
	public static final PredefinedFunctionData FNC_DISHONEST = new PredefinedFunctionData(DISHONEST, TYPE_FACT, new PredefinedTypeData[] { TYPE_AGENT }, false, true, false, null);
	public static final PredefinedFunctionData FNC_DEFAULT_PSEUDONYM = new PredefinedFunctionData(DEFAULT_PSEUDONYM, TYPE_PUBLIC_KEY, new PredefinedTypeData[] { TYPE_AGENT, TYPE_NAT }, false, true,
			true, ChannelModel.CCM);
	// private static final PredefinedFunctionData FNC_IS_AGENT = new
	// PredefinedFunctionData(IS_AGENT, TYPE_FACT, new PredefinedTypeData[] {
	// TYPE_AGENT }, false, false, false, null);
	private static final PredefinedFunctionData FNC_CONFIDENTIALITY_KEY = new PredefinedFunctionData(CONFIDENTIALITY_KEY, TYPE_PUBLIC_KEY, new PredefinedTypeData[] { TYPE_AGENT }, false, false, true,
			ChannelModel.CCM);
	private static final PredefinedFunctionData FNC_AUTHENTICATION_KEY = new PredefinedFunctionData(AUTHENTICATION_KEY, TYPE_PUBLIC_KEY, new PredefinedTypeData[] { TYPE_AGENT }, false, false, true,
			ChannelModel.CCM);
	private static final PredefinedFunctionData FNC_PUBLIC_KEY = new PredefinedFunctionData(PK, TYPE_PUBLIC_KEY, new PredefinedTypeData[] { TYPE_AGENT }, false, false, true, null);
	private static final PredefinedFunctionData FNC_SIGN = new PredefinedFunctionData(SIGN, TYPE_MESSAGE, new PredefinedTypeData[] { TYPE_PRIVATE_KEY, TYPE_MESSAGE }, true, false, true, null);
	private static final PredefinedFunctionData FNC_PAIR = new PredefinedFunctionData(PAIR, TYPE_MESSAGE, new PredefinedTypeData[] { TYPE_MESSAGE, TYPE_MESSAGE }, true, false, false, null);
	private static final PredefinedFunctionData FNC_SCRYPT = new PredefinedFunctionData(SCRYPT, TYPE_MESSAGE, new PredefinedTypeData[] { TYPE_SYMMETRIC_KEY, TYPE_MESSAGE }, true, false, true, null);
	private static final PredefinedFunctionData FNC_CRYPT = new PredefinedFunctionData(CRYPT, TYPE_MESSAGE, new PredefinedTypeData[] { TYPE_PUBLIC_KEY, TYPE_MESSAGE }, true, false, true, null);
	private static final PredefinedFunctionData FNC_INV = new PredefinedFunctionData(INV, TYPE_PRIVATE_KEY, new PredefinedTypeData[] { TYPE_PUBLIC_KEY }, true, true, true, null);
	private static final PredefinedFunctionData FNC_IKNOWS = new PredefinedFunctionData(IKNOWS, TYPE_FACT, new PredefinedTypeData[] { TYPE_MESSAGE }, true, true, true, null);
	private static final PredefinedFunctionData FNC_REMOVE = new PredefinedFunctionData(REMOVE, TYPE_FACT, new PredefinedTypeData[] { TYPE_MESSAGE_SET, TYPE_MESSAGE }, true, true, true, null);
	private static final PredefinedFunctionData FNC_ADD = new PredefinedFunctionData(ADD, TYPE_FACT, new PredefinedTypeData[] { TYPE_MESSAGE_SET, TYPE_MESSAGE }, true, true, true, null);
	private static final PredefinedFunctionData FNC_CONTAINS = new PredefinedFunctionData(CONTAINS, TYPE_FACT, new PredefinedTypeData[] { TYPE_MESSAGE_SET, TYPE_MESSAGE }, true, false, false, null);
	private static final PredefinedFunctionData FNC_SENT = new PredefinedFunctionData(SENT, TYPE_FACT, new PredefinedTypeData[] { TYPE_AGENT, TYPE_AGENT, TYPE_AGENT, TYPE_MESSAGE, TYPE_CHANNEL },
			false, true, true, ChannelModel.ACM);
	private static final PredefinedFunctionData FNC_RCVD = new PredefinedFunctionData(RCVD, TYPE_FACT, new PredefinedTypeData[] { TYPE_AGENT, TYPE_AGENT, TYPE_MESSAGE, TYPE_CHANNEL }, false, true,
			true, ChannelModel.ACM);
//	private static final PredefinedFunctionData FNC_HASH = new PredefinedFunctionData(HASH, TYPE_MESSAGE, new PredefinedTypeData[] { TYPE_MESSAGE }, false, false, true, null);
	private static final PredefinedFunctionData FNC_SUCC = new PredefinedFunctionData(SUCC, TYPE_NAT, new PredefinedTypeData[] { TYPE_NAT }, false, false, false, null);
	private static final PredefinedFunctionData FNC_LEQ = new PredefinedFunctionData(LEQ, TYPE_FACT, new PredefinedTypeData[] { TYPE_MESSAGE, TYPE_MESSAGE }, true, false, true, null);

	// for ACM
	private static final PredefinedFunctionData FNC_ACM_CHANNEL = new PredefinedFunctionData(ACM_CHANNEL, TYPE_CHANNEL, new PredefinedTypeData[] { TYPE_AGENT, TYPE_AGENT, TYPE_SLABEL }, false, true, true, ChannelModel.ACM);
	private static final PredefinedFunctionData FNC_ACM_CONFIDENTIAL_TO = new PredefinedFunctionData(ACM_CONFIDENTIAL_TO, TYPE_FACT, new PredefinedTypeData[] { TYPE_CHANNEL, TYPE_AGENT }, false, true, true, ChannelModel.ACM);
	private static final PredefinedFunctionData FNC_ACM_WEAKLY_CONFIDENTIAL = new PredefinedFunctionData(ACM_WEAKLY_CONFIDENTIAL, TYPE_FACT, new PredefinedTypeData[] { TYPE_CHANNEL }, false, true, true, ChannelModel.ACM);
	private static final PredefinedFunctionData FNC_ACM_AUTHENTIC_ON = new PredefinedFunctionData(ACM_AUTHENTIC_ON, TYPE_FACT, new PredefinedTypeData[] { TYPE_CHANNEL, TYPE_AGENT }, false, true, true, ChannelModel.ACM);
	private static final PredefinedFunctionData FNC_ACM_WEAKLY_AUTHENTIC = new PredefinedFunctionData(ACM_WEAKLY_AUTHENTIC, TYPE_FACT, new PredefinedTypeData[] { TYPE_CHANNEL }, false, true, true, ChannelModel.ACM);
	private static final PredefinedFunctionData FNC_ACM_RESILIENT = new PredefinedFunctionData(ACM_RESILIENT, TYPE_FACT, new PredefinedTypeData[] { TYPE_CHANNEL }, false, true, true, ChannelModel.ACM);
	private static final PredefinedFunctionData FNC_ACM_LINK = new PredefinedFunctionData(ACM_LINK, TYPE_FACT, new PredefinedTypeData[] { TYPE_CHANNEL, TYPE_CHANNEL }, false, true, true, ChannelModel.ACM);
	private static final PredefinedFunctionData FNC_ACM_BILATERAL  = new PredefinedFunctionData(ACM_BILATERAL , TYPE_FACT, new PredefinedTypeData[] { TYPE_CHANNEL, TYPE_CHANNEL, TYPE_AGENT, TYPE_AGENT }, false, true, true, ChannelModel.ACM);
	private static final PredefinedFunctionData FNC_ACM_UNILATERAL = new PredefinedFunctionData(ACM_UNILATERAL, TYPE_FACT, new PredefinedTypeData[] { TYPE_CHANNEL, TYPE_CHANNEL, TYPE_AGENT             }, false, true, true, ChannelModel.ACM);

	private Prelude() {}

	public static void registerDefaultTypesAndSymbols(IScope scope, ChannelModel cm) {
		Field[] allFields = Prelude.class.getDeclaredFields();
		// first the types
		for (Field f : allFields) {
			if (Modifier.isStatic(f.getModifiers())) {
				try {
					if (PredefinedTypeData.class.equals(f.getType())) {
						PredefinedTypeData pdt = (PredefinedTypeData) f.get(null);
						getType(pdt, scope);
					}
				}
				catch (NullPointerException e) {
					Debug.logger.error("Failed to register default field '" + f.getName() + "'.", e);
					throw e;
				}
				catch (IllegalAccessException e) {
					Debug.logger.error("Failed to register default field '" + f.getName() + "'.", e);
					throw new NullPointerException(); // TODO this is a hack; maybe throw other exception
				}
			}
		}
		// then constants and functions
		for (Field f : allFields) {
			if (Modifier.isStatic(f.getModifiers())) {
				if (PredefinedFunctionData.class.equals(f.getType())) {
					try {
						PredefinedFunctionData fnc = (PredefinedFunctionData) f.get(null);
						if (fnc.cm == null || fnc.cm == cm) {
							IType[] at = new IType[fnc.argTypes.length];
							for (int i = 0; i < fnc.argTypes.length; i++) {
								at[i] = getType(fnc.argTypes[i], scope);
							}
							FunctionSymbol fs = scope.addFunction(fnc.name, getType(fnc.type, scope), at);
							fs.setNonInvertible(fnc.nonInvertible);
							fs.setNonPublic(fnc.nonPublic);
							if (fnc.partOfPrelude) {
								fs.setPartOfPrelude(true);
							}
						}
					}
					catch (Exception e) {
						Debug.logger.error("Failed to register default function symbol '" + f.getName() + "'.", e);
					}
				}
				else if (PredefinedConstantData.class.equals(f.getType())) {
					try {
						PredefinedConstantData cnst = (PredefinedConstantData) f.get(null);
						if (cnst.cm == null || cnst.cm == cm) {
							ConstantSymbol cs = scope.constants(getType(cnst.type, scope), cnst.name);
							cs.setNonPublic(cnst.nonPublic);
							if (cnst.partOfPrelude) {
								cs.setPartOfPrelude(true);
							}
						}
					}
					catch (Exception e) {
						Debug.logger.error("Failed to register default constant symbol '" + f.getName() + "'.", e);
					}

				}
			}
		}
	}

	private static IType getType(PredefinedTypeData pdt, IScope scope) {
		IType res = null;
		if (!pdt.set) {
			SimpleType sup = null;
			if (pdt.supertype != null) {
				sup = scope.findType(pdt.supertype);
				if (sup == null) {
					sup = scope.type(pdt.supertype);
				}
			}
			SimpleType tt = scope.findType(pdt.name);
			if (tt != null) {
				if (sup != null) {
					tt.setSuperType(sup);
				}
				res = tt;
			}
			else {
				if (sup == null) {
					res = scope.type(pdt.name);
				}
				else {
					res = scope.type(pdt.name, sup);
				}
			}
		}
		else {
			PredefinedTypeData newPDT = new PredefinedTypeData(pdt.name, pdt.partOfPrelude, null);
			res = getSetOf(getType(newPDT, scope));
		}
		return res;
	}

	public static SetType getSetOf(IType type) {
		SetType setType;
		if (!sets.containsKey(type)) {
			setType = new SetType(type);
			sets.put(type, setType);
		}
		else {
			setType = sets.get(type);
		}
		return setType;
	}

	private static final Map<String, IType> treeTypesCache = new HashMap<String, IType>();

}
