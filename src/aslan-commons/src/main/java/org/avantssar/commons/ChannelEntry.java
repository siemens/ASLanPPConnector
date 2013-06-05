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

package org.avantssar.commons;

public class ChannelEntry {
	public enum Type {
		Regular, Secure, Confidential, Authentic
	}

	public static final ChannelEntry regular = 
			new ChannelEntry(Type.Regular, false, false, false, null);
	public static final ChannelEntry authentic = 
			new ChannelEntry(Type.Authentic, false, false, false, null);
	public static final ChannelEntry undirectedAuthentic = 
			new ChannelEntry(Type.Authentic, true, false, false, null);
	public static final ChannelEntry confidential = 
			new ChannelEntry(Type.Confidential, false, false, false, null);
	public static final ChannelEntry secure = 
			new ChannelEntry(Type.Secure, false, false, false, null);
	public static final ChannelEntry resilientRegular = 
			new ChannelEntry(Type.Regular, false, true, false, null);
	public static final ChannelEntry resilientAuthentic = 
			new ChannelEntry(Type.Authentic, false, true, false, null);
	public static final ChannelEntry resilientUndirectedAuthentic = 
			new ChannelEntry(Type.Authentic, true, true, false, null);
	public static final ChannelEntry resilientConfidential = 
			new ChannelEntry(Type.Confidential, false, true, false, null);
	public static final ChannelEntry resilientSecure = 
			new ChannelEntry(Type.Secure, false, true, false, null);
	public static final ChannelEntry freshRegular = 
			new ChannelEntry(Type.Regular, false, false, true, null);
	public static final ChannelEntry freshAuthentic = 
			new ChannelEntry(Type.Authentic, false, false, true, null);
	public static final ChannelEntry freshUndirectedAuthentic = 
			new ChannelEntry(Type.Authentic, true, false, true, null);
	public static final ChannelEntry freshConfidential = 
			new ChannelEntry(Type.Confidential, false, false, true, null);
	public static final ChannelEntry freshSecure = 
			new ChannelEntry(Type.Secure, false, false, true, null);
	public static final ChannelEntry freshResilientRegular = 
			new ChannelEntry(Type.Regular, false, true, true, null);
	public static final ChannelEntry freshResilientAuthentic = 
			new ChannelEntry(Type.Authentic, false, true, true, null);
	public static final ChannelEntry freshResilientUndirectedAuthentic = 
			new ChannelEntry(Type.Authentic, true, true, true, null);
	public static final ChannelEntry freshResilientConfidential = 
			new ChannelEntry(Type.Confidential, false, true, true, null);
	public static final ChannelEntry freshResilientSecure = 
			new ChannelEntry(Type.Secure, false, true, true, null);

	public static final ChannelEntry[] values = { regular, authentic, undirectedAuthentic,
			confidential, secure, resilientRegular, resilientAuthentic, resilientUndirectedAuthentic,
			resilientConfidential, resilientSecure, freshRegular,
			freshAuthentic, freshUndirectedAuthentic, freshConfidential, freshSecure,
			freshResilientRegular, freshResilientAuthentic, freshResilientUndirectedAuthentic,
			freshResilientConfidential, freshResilientSecure };

	public final Type type;
	public final boolean undirected;
	public final boolean resilient;
	public final boolean fresh;
	public final String name;
	public final String arrow;
	public final String nonArrow;

	private ChannelEntry(Type type, boolean undirected, boolean resilient, boolean fresh,
			String name) {
		this.type = type;
		this.undirected = undirected;
		this.resilient = resilient;
		this.fresh = fresh;
		this.name = name;
		this.arrow = buildArrowRepresentation(type, undirected, resilient, fresh);
		this.nonArrow = buildNonArrowRepresentation(type, undirected, resilient, fresh);
	}

	public static ChannelEntry from(Type type, boolean undirected, boolean resilient,
			boolean fresh, String name) {
		if (name != null) {
			return new ChannelEntry(Type.Regular, false, false, false, name);
		} else {
			for (ChannelEntry ce : values) {
				if (ce.name == null && 
					ce.type      == type && 
					ce.undirected == undirected && 
					ce.resilient == resilient && 
					ce.fresh     == fresh) {
					return ce;
				}
			}
			return null;
		}
	}

	public static ChannelEntry getByKey(String key, boolean named) {
		if (!named) {
			for (ChannelEntry ce : values) {
				if (ce.arrow.equals(key) || ce.nonArrow.equals(key)) {
					return ce;
				}
			}
			return null;
		} else {
			return new ChannelEntry(Type.Regular, false, false, false, key);
		}
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof ChannelEntry) {
			ChannelEntry ce = (ChannelEntry) o;
			boolean ok = this.type    == ce.type      && this.undirected == ce.undirected 
					&& this.resilient == ce.resilient && this.fresh      == ce.fresh 
					&& this.arrow     == ce.arrow 	  && this.nonArrow   == ce.nonArrow;
			if (ok) {
				if (this.name != null) {
					ok = ce.name != null && this.name.equals(ce.name);
				} else {
					ok = ce.name == null;
				}
			}
			return ok;
		}
		return false;
	}

	private String buildArrowRepresentation(Type type, 
			boolean undirected, boolean resilient, boolean fresh) {
		String repr;
		if (name != null) {
			repr = "-" + name + "->";
		} else {
			repr = (resilient ? "=>" : "->");
			if (fresh) repr = repr + ">";
			if (undirected) repr = repr + "?";
			if (type == Type.Authentic || type == Type.Secure) repr = "*" + repr;
			if (type == Type.Confidential || type == Type.Secure) repr = repr +  "*";
		}
		return repr;
	}

	private String buildNonArrowRepresentation(Type type, 
			boolean undirected, boolean resilient, boolean fresh) {
		String repr = "";
		if (name != null) {
			repr = name;
		} else {
			if (resilient) {
				repr += "res";
			}
			if (fresh) {
				if (repr.length() > 0) {
					repr += "_";
				}
				repr += "fresh";
			}
			if (type == Type.Regular) {
				repr = "regular";
			}
			else {
				if (repr.length() > 0) {
					repr += "_";
				}
				if (type == Type.Authentic) {
					if (undirected) 
					  repr += "u";
					repr += "auth";
				} else if (type == Type.Confidential) {
					repr += "conf";
				} else if (type == Type.Secure) {
					repr += "sec";
				}
			}
			if (repr.length() > 0) {
				repr += "Ch";
			}
		}
		return repr;
	}

}
