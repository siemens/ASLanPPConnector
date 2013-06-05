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

package org.avantssar.aslan.xml;

import javax.jws.WebService;

@WebService
public interface ASLanXMLConverter {

	/**
	 * Retrieves the title of the converter program.
	 * 
	 * @return A String containing the title of the converter program.
	 */
	String getTitle();

	/**
	 * Retrieves the version of the converter program.
	 * 
	 * @return A String containing the version of the converter program.
	 */
	String getVersion();

	/**
	 * Returns the vendor of the converter program.
	 * 
	 * @return A String containing the vendor of the converter program.
	 */
	String getVendor();

	/**
	 * Returns a line containing the vendor, the program title and the version.
	 * 
	 * @return A String that holds the vendor, the program title and the
	 *         version, separated by spaces.
	 */
	String getFullTitleLine();

	/**
	 * Converts an ASLan plain text specification into XML representation.
	 * 
	 * @param aslan
	 *            The plain text ASLan specification.
	 * @return The XML representation of the ASLan specification, together with
	 *         possible errors and warnings that were generated during
	 *         conversion.
	 */
	ConverterOutput aslan2xml(String aslan);

	/**
	 * Converts an ASLan XML specification into plain text.
	 * 
	 * @param xml
	 *            The XML representation of the ASLan specification.
	 * @return The plain text representation of the ASLan specification,
	 *         together with possible errors and warnings that were generated
	 *         during conversion.
	 */
	ConverterOutput xml2aslan(String xml);

	/**
	 * Performs syntax and type checking on an ASLan plain text specification.
	 * 
	 * @param aslan
	 *            The plain text ASLan specification
	 * @return A list of possible errors and warnings that were generated while
	 *         checking the specification, together with a pretty-printed
	 *         version of the specification.
	 */
	ConverterOutput check(String aslan);

}
