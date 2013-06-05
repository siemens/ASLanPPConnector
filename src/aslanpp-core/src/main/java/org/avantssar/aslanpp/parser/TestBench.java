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

package org.avantssar.aslanpp.parser;

import java.io.FileInputStream;
import org.antlr.runtime.ANTLRInputStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.CommonTreeNodeStream;
import org.avantssar.aslanpp.ASLanPPNewLexer;
import org.avantssar.aslanpp.ASLanPPNewParser;
import org.avantssar.aslanpp.SymbolsNew;
import org.avantssar.aslanpp.ToASLanNew;
import org.avantssar.aslanpp.model.ASLanPPSpecification;
import org.avantssar.aslanpp.model.EntityManager;
import org.avantssar.aslanpp.model.ErrorMessages;
import org.avantssar.commons.Error;
import org.avantssar.commons.ErrorGatherer;
import org.avantssar.commons.Error.Severity;

public class TestBench {

	public static void main(String args[]) throws Exception {
		String filename = "/tmp/a.aslan++";

		ANTLRInputStream antStream = new ANTLRInputStream(new FileInputStream(filename));
		ErrorGatherer err = new ErrorGatherer(ErrorMessages.DEFAULT);
		ASLanPPNewLexer lexer = new ASLanPPNewLexer(antStream);
		lexer.setErrorGatherer(err);
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		ASLanPPNewParser parser = new ASLanPPNewParser(tokens);
		parser.setErrorGatherer(err);
		ASLanPPNewParser.program_return r = null;
		r = parser.program(new EntityManager());
		ASLanPPSpecification spec = r.spec;
		if (spec != null) {
			// move the errors from parsing phase into the new instance.
			spec.getErrorGatherer().addAll(err);
			if (parser.getNumberOfSyntaxErrors() > 0) {
				StringBuffer sb = new StringBuffer();
				for (Error e : spec.getErrorGatherer()) {
					sb.append(e.toString().substring(Severity.ERROR.toString().length() + 1).trim()).append("\n");
				}
				err.addException(ErrorMessages.PARSER_ERROR, sb.toString());
			}
			else {
				if (r.getTree() != null) {
					// By this time the types are registered, so we can run the
					// tree
					// grammar that will register the symbols.
					CommonTree ct = (CommonTree) r.getTree();
					// System.out.println(ct.toStringTree());
					CommonTreeNodeStream nodes = new CommonTreeNodeStream(ct);
					SymbolsNew symb = new SymbolsNew(nodes);
					symb.entity(spec);
					// Now we can run the tree grammar that will load the
					// expressions and types into the in-memory model.
					nodes.reset();
					ToASLanNew ta = new ToASLanNew(nodes);
					ta.entity(spec);

					System.out.println(spec.toString());
				}
			}
		}
		else {
			if (parser.getNumberOfSyntaxErrors() > 0) {
				StringBuffer sb = new StringBuffer();
				for (Error e : err) {
					sb.append(e.toString().substring(Severity.ERROR.toString().length() + 1).trim()).append("\n");
				}
				err.addException(ErrorMessages.PARSER_ERROR, sb.toString());
			}
		}
	}

}
