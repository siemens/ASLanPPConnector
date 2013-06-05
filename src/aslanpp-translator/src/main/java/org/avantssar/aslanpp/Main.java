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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

import org.antlr.runtime.RecognitionException;
import org.avantssar.aslanpp.Debug.LogLevel;
import org.avantssar.aslanpp.OfflineConnectorCmdLineOptions;
import org.avantssar.aslanpp.model.EntityManager;
import org.avantssar.commons.Utils;
import org.kohsuke.args4j.CmdLineException;

public class Main {

	public static void main(String[] args) throws IOException, NullPointerException, RecognitionException {

		Debug.initLog(LogLevel.INFO);

		OfflineConnectorCmdLineOptions options = new OfflineConnectorCmdLineOptions(Main.class);
		try {
			options.getParser().parseArgument(args);
			options.ckeckAtEnd();
		}
		catch (CmdLineException ex) {
			reportException("Parsing options failed.", ex);
			options.showShortHelp(System.err);
			return;
		}

		if (options.isShowHelp()) {
			options.showLongHelp(System.out);
			return;
		}

		ASLanPPConnectorImpl translator = new ASLanPPConnectorImpl();

		if (options.isShowVersion()) {
			System.out.println(translator.getFullTitleLine());
			return;
		}

		InputStream input;
		if (options.isReadFromStdin()) {
			input = System.in;
		}
		else {
			String base = options.getIn().getParent();
			if (base != null) {
				System.setProperty(EntityManager.ASLAN_ENVVAR, base);
				// try {
				// EntityManager.loadASLanPath();
				// }
				// catch (IOException e) {
				// reportException("Exception while loading ASLANPATH for directory '" + base + "': ", e);
				// }
			}
			try {
				input = new FileInputStream(options.getIn());
			}
			catch (FileNotFoundException e) {
				reportException("Could not open input file '" + options.getIn().getAbsolutePath() + "'.", e);
				return;
			}
		}

/*		try*/ {
			TranslatorOutput result = null;
			if (options.getAnalysisResult() != null) {
				InputStream analysisResult;
				try {
					analysisResult = new FileInputStream(options.getAnalysisResult());
				}
				catch (FileNotFoundException e) {
					reportException("Could not open analysis result file '" + options.getAnalysisResult().getAbsolutePath() + "'.", e);
					return;
				}
				String aslanSpec = Utils.stream2string(input);
				String anResult = Utils.stream2string(analysisResult);
				result = translator.translateAnalysisResult(aslanSpec, anResult);
			}
			else {
				String aslanppSpec = Utils.stream2string(input);
				result = translator.translateExt(true, options.getOutputGraphvizFiles(), options, options.getIn().getAbsolutePath(), aslanppSpec).toTranslatorOutput();
			}
			if (result != null) {
				result.printWarnErrors();
				if (result.getSpecification() != null && result.getErrors().size() == 0) {
					PrintStream output;
					if (options.getOut() != null) {
						try {
							output = new PrintStream(options.getOut());
						}
						catch (FileNotFoundException e) {
							reportException("Could not open output file '" + options.getOut().getAbsolutePath() + "'.", e);
							return;
						}
					}
					else {
						output = System.out;
					}
					output.print(result.getSpecification());
				}
			}
		}
		/*catch (Exception e) {
			reportException("Exception while performing translation.", e);
		}*/
	}

	protected static void reportError(String message, Throwable e) {
		System.err.print("Error: ");
		System.err.println(message);
		System.err.println(e.getMessage());
		Debug.logger.error(message, e);
	}
	
	private static void reportException(String message, Throwable e) {
		System.err.print("Fatal: ");
		System.err.println(message);
		System.err.println(e.getMessage());
		Debug.logger.fatal(message, e);
	}
}
