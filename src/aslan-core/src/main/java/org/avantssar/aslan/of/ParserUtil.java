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

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.antlr.runtime.IntStream;
import org.avantssar.aslan.ASLanSyntaxErrorException;

public class ParserUtil {

	public static final String SATMC = "SATMC";
	public static final String OFMC = "OFMC";
	public static final String CL_ATSE = "CL-ATSE";

	public static final String[] backends = new String[] { SATMC, OFMC, CL_ATSE };

	public static void checkFileName(IntStream input, String word) throws ASLanSyntaxErrorException {

	}

	public static void checkBackend(IntStream input, String word) throws ASLanSyntaxErrorException {
		if (!Arrays.asList(backends).contains(word)) {
			throw new ASLanSyntaxErrorException(input, "Unrecognized backend: \"" + word + "\". Valid options are: " + options(backends) + ".");
		}
	}

	public static void checkNat(IntStream input, String word) throws ASLanSyntaxErrorException {
		try {
			Integer.parseInt(word);
		}
		catch (NumberFormatException e) {
			throw new ASLanSyntaxErrorException(input, "Nat expected and encountered \"" + word + "\".");
		}
	}

	public static void checkFloat(IntStream input, String word) throws ASLanSyntaxErrorException {
		try {
			Double.parseDouble(word);
		}
		catch (NumberFormatException e) {
			throw new ASLanSyntaxErrorException(input, "Float expected and encountered \"" + word + "\".");
		}
	}

	public static void checkName(IntStream input, String word) throws ASLanSyntaxErrorException {
		Pattern p = Pattern.compile("[a-zA-Z_][a-zA-Z0-9_]*");
		Matcher m = p.matcher(word);
		if (!m.matches()) {
			throw new ASLanSyntaxErrorException(input, "Name expected and encountered \"" + word + "\".");
		}
	}

	public static boolean checkNatOrName(IntStream input, String word) throws ASLanSyntaxErrorException {
		try {
			checkNat(input, word);
			return true;
		}
		catch (ASLanSyntaxErrorException e1) {
			try {
				checkName(input, word);
				return false;
			}
			catch (ASLanSyntaxErrorException e2) {
				throw new ASLanSyntaxErrorException(input, "Nat or Name expected and encountered \"" + word + "\".");
			}
		}
	}

	private static String options(String[] opts) {
		StringBuffer sb = new StringBuffer();
		for (String s : opts) {
			if (sb.length() > 0) {
				sb.append(" ");
			}
			sb.append("\"").append(s).append("\"");
		}
		return sb.toString();
	}

}
