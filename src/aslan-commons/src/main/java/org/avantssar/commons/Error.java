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

public class Error implements Comparable<Error> {

	public static enum Severity {
		FATAL, ERROR, WARNING
	};

	private final LocationInfo location;
	private final Severity severity;
	private final String key;
	private final String message;

	protected Error() { this(null, null, null); }
	
	protected Error(Severity severity, IErrorMessagesProvider provider,
			String key, Object... values) {
		this(null, severity, provider, key, values);
	}

	protected Error(LocationInfo location, Severity severity,
			IErrorMessagesProvider provider, String key, Object... values) {
		this.location = location;
		this.severity = severity;
		this.key = key;
		this.message = provider.fill(key, values);
	}

	public String getCode() {
		return key;
	}

	public Severity getSeverity() {
		return severity;
	}

	public String getMessage() {
		return message;
	}

	public int getLine() {
		return location != null ? location.line : LocationInfo.NOWHERE.line;
	}

	public int getColumn() {
		return location != null ? location.col : LocationInfo.NOWHERE.col;
	}

	public int getNextColumn() {
		return location != null ? location.next : LocationInfo.NOWHERE.next;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(severity.toString());
		if (location != null && !location.equals(LocationInfo.NOWHERE)) {
			sb.append(" at ").append(location.toString());
		}
		sb.append(": ");
		sb.append(message);
		return sb.toString();
	}

	@Override
	public int compareTo(Error o) {
		return this.toString().compareTo(o.toString());
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof Error) {
			Error oe = (Error) o;
			return this.compareTo(oe) == 0;
		}
		return false;
	}
}
