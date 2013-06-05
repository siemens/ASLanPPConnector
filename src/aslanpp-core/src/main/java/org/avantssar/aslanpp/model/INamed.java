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
 * Generic interface for entities that have names. An original name is assigned
 * upon construction and cannot be changed afterwards. However it is possible to
 * assign a pseudonym that will be used instead of the original name.
 * 
 * @author gabi
 */
public interface INamed extends Comparable<INamed> {

	/**
	 * Gets the public name. If a pseudonym was set, then the pseudonym is
	 * returned. Otherwise the original name is returned.
	 * 
	 * @return A String that holds the public name.
	 */
	String getName();

	/**
	 * Gets the original name. Ignores any pseudonym that might have been set.
	 * 
	 * @return A String that holds the original name.
	 */
	String getOriginalName();

	/**
	 * Sets a pseudonym that should be used instead of the original name.
	 * 
	 * @param pseudonym
	 *            A String that holds the pseudonym to be used.
	 */
	void setPseudonym(String pseudonym);

	/**
	 * Checks if there was any pseudonym set.
	 * 
	 * @return true if a pseudonym was set, false otherwise.
	 */
	boolean hasPseudonym();

}
