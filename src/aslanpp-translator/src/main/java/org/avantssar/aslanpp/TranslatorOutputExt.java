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

package org.avantssar.aslanpp;

import java.util.ArrayList;
import java.util.List;

import org.avantssar.aslanpp.model.ASLanPPSpecification;
import org.avantssar.commons.Error;
import org.avantssar.commons.ErrorGatherer;

public class TranslatorOutputExt { // an improved variant of TranslatorOutput

	private String result;
	private ASLanPPSpecification spec;
	private List<Error> warnings = new ArrayList<Error>();
	private List<Error> errors   = new ArrayList<Error>();

	/**
	 * The actual result, which may be the pretty-printed ASLan++ specification,
	 * the translation to ASLan, or the result of back-translating the analysis.
	 * May be null if errors occurred or no output was requested.
	 */
	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	/**
	 * The resulting internal representation of the ASLan++ specification.
	 * May be null if errors occurred during the translation from ASLan++.
	 */
	public ASLanPPSpecification getASLanPPSpecification() {
		return spec;
	}

	public void setASLanPPSpecification(ASLanPPSpecification spec) {
		this.spec = spec;
	}

	/**
	 * A list of warnings raised by the translator. May be null or empty if no
	 * warnings were raised.
	 */
	public List<Error> getWarnings() {
		return warnings;
	}

	public void setWarnings(List<Error> warnings) {
		this.warnings = warnings;
	}

	/**
	 * A list of errors raised by the translator. May be null or empty if no
	 * errors were raised.
	 */
	public List<Error> getErrors() {
		return errors;
	}

	public void setErrors(List<Error> errors) {
		this.errors = errors;
	}
	
	public void addWarnErrors(ErrorGatherer err) {
		for (Error e : err) {
			if (e.getSeverity() == Error.Severity.WARNING) {
				warnings.add(e);
			}		
			else {
				errors.add(e);
			}
		}
	}

	public TranslatorOutput toTranslatorOutput() {
		TranslatorOutput res = new TranslatorOutput();
		res.setSpecification(result);
		for (Error e : warnings) {
			res.addWarning(e.toString());
		}
		for (Error e : errors) {
			res.addError(e.toString());
		}
		return res;
	}
	
}
