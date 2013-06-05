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

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.avantssar.commons.Error.Severity;

public class ErrorGatherer extends ArrayList<Error> {

	private static final long serialVersionUID = 7441429350973303438L;

	private final IErrorMessagesProvider provider;

	public ErrorGatherer(IErrorMessagesProvider provider) {
		this.provider = provider;
	}

	public Error addWarning(String key, Object... values) {
		return addWarning(null, key, values);
	}

	public Error addWarning(LocationInfo location, String key, Object... values) {
		return add(location, Severity.WARNING, key, values);
	}

	public Error addError(String key, Object... values) {
		return addError(null, key, values);
	}

	public Error addError(LocationInfo location, String key, Object... values) {
		return add(location, Severity.ERROR, key, values);
	}

	public Error addException(String key, Object... values) {
		return addException(null, key, values);
	}

	public Error addException(LocationInfo location, String key,
			Object... values) {
		Error err = add(location, Severity.FATAL, key, values);
		throw new ASLanPPException(this, err.toString());
	}

	private Error add(LocationInfo location, Severity severity, String key,
			Object... values) {
		Error err = new Error(location, severity, provider, key, values);
		if (!contains(err)) {
			add(err);
		}
		return err;
	}

	public List<String> getWarnings() {
		List<String> ws = new ArrayList<String>();
		for (Error e : this) {
			if (e.getSeverity() == Severity.WARNING) {
				ws.add(e.toString());
			}
		}
		return ws;
	}

	public List<String> getErrors() {
		List<String> ws = new ArrayList<String>();
		for (Error e : this) {
			if (e.getSeverity() == Severity.ERROR) {
				ws.add(e.toString());
			}
		}
		return ws;
	}

	public void report(PrintStream out) {
		for (Error e : this) {
			out.println(e);
		}
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		if (size() == 0) {
			sb.append("No errors/warnings.");
		} else {
			for (Error e : this) {
				sb.append(e.toString()).append("\n");
			}
		}
		return sb.toString();
	}
}
