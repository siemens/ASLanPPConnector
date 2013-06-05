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

package org.avantssar.aslan.of;

import java.io.ByteArrayInputStream;
import org.antlr.runtime.ANTLRInputStream;
import org.antlr.runtime.CommonTokenStream;
import org.avantssar.commons.ErrorGatherer;

public class GroundTermBuilder {

	public static IGroundTerm fromString(String term) {
		ByteArrayInputStream bais = new ByteArrayInputStream(term.getBytes());
		try {
			ANTLRInputStream antlrStream = new ANTLRInputStream(bais);
			ErrorGatherer err = new ErrorGatherer(OutputFormatErrorMessages.DEFAULT);
			ofLexer lexer = new ofLexer(antlrStream);
			lexer.setErrorGatherer(err);
			CommonTokenStream tokens = new CommonTokenStream(lexer);
			ofParser parser = new ofParser(tokens);
			parser.setErrorGatherer(err);
			IGroundTerm t = parser.term();
			if (err.getErrors().size() == 0) {
				return t;
			}
			else {
				return null;
			}
		}
		catch (Exception e) {
			// silently ignore any errors for now
			return null;
		}
	}

	public static void main(String[] args) {
		IGroundTerm t = fromString("a(b)");
		System.out.println(t.getRepresentation(null));
	}

}
