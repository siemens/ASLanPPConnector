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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

import org.avantssar.aslan.ISymbolsProvider;
import org.avantssar.aslan.SATMCSymbolsProvider;
import org.avantssar.commons.Utils;
import org.avantssar.commons.XMLCommandLineOptions;
import org.kohsuke.args4j.CmdLineException;

public class Main {

	public static void main(String[] args) {

		XMLCommandLineOptions options = new XMLCommandLineOptions(Main.class);
		try {
			options.getParser().parseArgument(args);
			options.ckeckAtEnd();
		} catch (CmdLineException ex) {
			reportException("Inconsistent options.", ex, System.err);
			options.showShortHelp(System.err);
			return;
		}

		if (options.isShowHelp()) {
			options.showLongHelp(System.out);
			return;
		}

		ASLanXMLConverterImpl converter;
		ISymbolsProvider satmcExtraSymbols = null;
		if (options.isSatmc()) {
			satmcExtraSymbols = new SATMCSymbolsProvider();
		}
		if (satmcExtraSymbols != null) {
			converter = new ASLanXMLConverterImpl(satmcExtraSymbols);
		} else {
			converter = new ASLanXMLConverterImpl();
		}

		if (options.isShowVersion()) {
			System.out.println(converter.getFullTitleLine());
			return;
		}

		InputStream input;
		if (options.isReadFromStdin()) {
			input = System.in;
		} else {
			try {
				input = new FileInputStream(options.getIn());
			} catch (FileNotFoundException e) {
				reportException("Could not open input file '"
						+ options.getIn().getAbsolutePath() + "'.", e,
						System.err);
				return;
			}
		}

		String inputSpec = null;
		try {
			inputSpec = Utils.stream2string(input);
		} catch (IOException e) {
			reportException("Could not read from specified input.", e,
					System.err);
		}

		PrintStream output;
		if (options.getOut() != null) {
			try {
				output = new PrintStream(options.getOut());
			} catch (FileNotFoundException e) {
				reportException("Could not open output file '"
						+ options.getOut().getAbsolutePath() + "'.", e,
						System.err);
				return;
			}
		} else {
			output = System.out;
		}

		try {
			ConverterOutput result;
			if (options.isCheck()) {
				result = converter.check(inputSpec);
				report(result, System.out);
			} else {
				if (options.isFromXML()) {
					result = converter.xml2aslan(inputSpec);
				} else {
					result = converter.aslan2xml(inputSpec);
				}
				if (result.getSpecification() != null) {
					output.print(result.getSpecification());
				}
				report(result, System.err);
			}
		} catch (Exception e) {
			reportException("Exception while doing "
					+ (options.isCheck() ? "check" : "conversion") + ".", e,
					System.err);
		}
	}

	private static void report(ConverterOutput co, PrintStream out) {
		for (String e : co.getErrors()) {
			out.println(e);
		}
		for (String w : co.getWarnings()) {
			out.println(w);
		}
	}

	private static void reportException(String message, Throwable e,
			PrintStream out) {
		out.print("Error: ");
		out.println(message);
		out.println(e.getMessage());
		out.println();
	}

}
