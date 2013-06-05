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

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.antlr.runtime.TokenStream;
import org.antlr.runtime.tree.CommonTree;

public class LTLHelper {

	private static LTLHelper theInstance = null;

	public static LTLHelper getInstance() {
		if (theInstance == null) {
			theInstance = new LTLHelper();
		}
		return theInstance;
	}

	public Set<String> unaryLTLOps;
	public Set<String> binaryLTLOps;

	private LTLHelper() {
		unaryLTLOps = new HashSet<String>();
		unaryLTLOps.add("X"); // next
		unaryLTLOps.add("Y"); // yesterday
		unaryLTLOps.add("<>"); // finally
		unaryLTLOps.add("<->"); // once
		unaryLTLOps.add("[]"); // globally
		unaryLTLOps.add("[-]"); // historically

		binaryLTLOps = new HashSet<String>();
		binaryLTLOps.add("U"); // until
		binaryLTLOps.add("R"); // release
		binaryLTLOps.add("S"); // since
	}

	public void check(String operator, List<CommonTree> args, TokenStream input) throws ASLanPPSyntaxErrorException {
		if (!unaryLTLOps.contains(operator) && !binaryLTLOps.contains(operator)) {
			throw new ASLanPPSyntaxErrorException(input, "Invalid LTL operator '" + operator + "'.");
		}
		if (unaryLTLOps.contains(operator) && (args.size() != 1)) {
			throw new ASLanPPSyntaxErrorException(input, "Invalid number of arguments for unary LTL operator '" + operator + "'.");
		}
		if (binaryLTLOps.contains(operator) && (args.size() != 2)) {
			throw new ASLanPPSyntaxErrorException(input, "Invalid number of arguments for binary LTL operator '" + operator + "'.");
		}
	}

	public boolean isUnary(String operator) {
		return unaryLTLOps.contains(operator);
	}

	public boolean isBinary(String operator) {
		return binaryLTLOps.contains(operator);
	}

}
