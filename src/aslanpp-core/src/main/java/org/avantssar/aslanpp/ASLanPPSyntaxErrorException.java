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

import org.antlr.runtime.IntStream;
import org.antlr.runtime.RecognitionException;

public class ASLanPPSyntaxErrorException extends RecognitionException {

	private static final long serialVersionUID = -6188794441182917547L;

	private String message;

	public ASLanPPSyntaxErrorException(IntStream input, String message) {
		super(input);
		this.message = message;
	}

	public String getMessage() {
		return message;
	}

}
