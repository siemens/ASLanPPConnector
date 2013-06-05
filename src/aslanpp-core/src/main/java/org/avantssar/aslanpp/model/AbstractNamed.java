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

/**
 * Convenience class that provides a basic implementation for the INamed
 * interface.
 * 
 * @author gabi
 */
public abstract class AbstractNamed implements INamed {

	private final String name;
	private String pseudonym;

	public AbstractNamed(String name) {
		if (name == null) {
			throw new IllegalArgumentException("Cannot instantiate named item with null name.");
		}
		this.name = escape(name);
	}

	public String getName() {
		return pseudonym != null ? pseudonym : name;
	}

	public String getOriginalName() {
		return name;
	}

	public void setPseudonym(String pseudonym) {
		this.pseudonym = escape(pseudonym);
	}

	public boolean hasPseudonym() {
		return pseudonym != null;
	}

	/**
	 * The comparison is done based on the public name, not on the original
	 * name.
	 */
	public int compareTo(INamed o) {
		return getName().compareTo(o.getName());
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof INamed) {
			return compareTo((INamed) o) == 0;
		}
		else {
			return false;
		}
	}

	/**
	 * Replaces any illegal characters with underscores. Legal characters are
	 * letters, digits, the underscore sign and the apostrophe.
	 * 
	 * @param text
	 *            The original text which may contain illegal characters.
	 * @return A version of the original text with all illegal characters
	 *         replaced with underscores.
	 */
	private String escape(String text) {
		return text.replaceAll("[^a-zA-Z0-9_']", "_");
	}
}
