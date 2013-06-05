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

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlType;

@XmlType(namespace = "http://xml.aslan.avantssar.org/")
public class ConverterOutput {

	private String specification;
	private List<String> warnings = new ArrayList<String>();
	private List<String> errors = new ArrayList<String>();

	/**
	 * The resulted ASLan specification. Can be represented in plain text or in
	 * XML, depending on what kind of conversion was done. May be null if errors
	 * occurred during the conversion.
	 */
	public String getSpecification() {
		return specification;
	}

	public void setSpecification(String specification) {
		this.specification = specification;
	}

	/**
	 * A list of warnings raised by the converter. May be null or empty if no
	 * warnings were raised.
	 */
	public List<String> getWarnings() {
		return warnings;
	}

	public void setWarnings(List<String> warnings) {
		this.warnings = warnings;
	}

	/**
	 * A list of errors raised by the converter. May be null or empty if no
	 * errors were raised.
	 */
	public List<String> getErrors() {
		return errors;
	}

	public void setErrors(List<String> errors) {
		this.errors = errors;
	}

}
