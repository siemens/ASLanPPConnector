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

package org.avantssar.aslan;

import org.avantssar.commons.ErrorGatherer;
import org.avantssar.commons.LocationInfo;

public abstract class AbstractNamed extends AbstractRepresentable implements INamed {

	private final String name;

	protected AbstractNamed(LocationInfo location, ErrorGatherer err, String name, INameValidator validator) {
		super(location, err);
		this.name = escape(name);
		if (!validator.isNameCorrect(this.name)) {
			getErrorGatherer().addException(location, ASLanErrorMessages.INVALID_NAME, name, validator.getDescription());
		}
	}

	public String getName() {
		return name;
	}

	/**
	 * Replaces any illegal characters with underscores. Legal characters are
	 * letters, digits and the underscore sign.
	 * 
	 * @param text
	 *            The original text which may contain illegal characters.
	 * @return A version of the original text with all illegal characters
	 *         replaced with underscores.
	 */
	public static String escape(String text) {
		return text.replaceAll("[^a-zA-Z0-9_]", "_");
	}
}
