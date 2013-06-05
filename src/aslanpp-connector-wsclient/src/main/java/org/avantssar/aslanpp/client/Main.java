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

package org.avantssar.aslanpp.client;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import org.avantssar.aslanpp.ASLanPPConnector;
import org.avantssar.aslanpp.TranslatorOutput;
import org.avantssar.aslanpp.impl.ASLanPPConnectorImpl;
import org.avantssar.commons.Error;
import org.avantssar.commons.TranslatorOptions;
import org.avantssar.commons.Utils;
import org.avantssar.commons.TranslatorOptions.HornClausesLevel;
import org.avantssar.commons.TranslatorOptions.OptimizationLevel;
import org.kohsuke.args4j.CmdLineException;

public class Main {

	public static void main(String[] args) {

		ClientSideTranslatorOptions options = new ClientSideTranslatorOptions(Main.class);
		try {
			options.getParser().parseArgument(args);
			options.ckeckAtEnd();
		}
		catch (CmdLineException ex) {
			reportException("Inconsistent options.", ex, System.err);
			options.showShortHelp(System.err);
			return;
		}

		if (options.isShowHelp()) {
			options.showLongHelp(System.out);
			return;
		}

		ASLanPPConnector translator = new ASLanPPConnectorImpl(options.getHost(), options.getPort());

		if (options.isShowVersion()) {
			System.out.println(translator.getFullTitleLine());
			return;
		}

		String fileName = null;
		List<InputStream> inputs = new ArrayList<InputStream>();
		if (options.getIn() != null) {
			for (String f : options.getIn()) {
				if(fileName == null)
					fileName = f;
				try {
					InputStream is = new FileInputStream(f);
					inputs.add(is);
				}
				catch (FileNotFoundException e) {
					reportException("Could not open input file '" + f + "'.", e, System.err);
					return;
				}
			}
		}
		else {
			inputs.add(System.in);
		}

		PrintStream output;
		if (options.getOut() != null) {
			try {
				output = new PrintStream(options.getOut());
			}
			catch (FileNotFoundException e) {
				reportException("Could not open output file '" + options.getOut().getAbsolutePath() + "'.", e, System.err);
				return;
			}
		}
		else {
			output = System.out;
		}

		try {
			TranslatorOutput result = null;
			if (options.getAnalysisResult() != null) {
				InputStream analysisResult;
				if (inputs.size() > 1) {
					reportException("When interpreting an analysis result, only one specification is accepted.", null, System.err);
					return;
				}
				try {
					analysisResult = new FileInputStream(options.getAnalysisResult());
				}
				catch (FileNotFoundException e) {
					reportException("Could not open analysis result file '" + options.getAnalysisResult().getAbsolutePath() + "'.", e, System.err);
					return;
				}
				String aslanSpec = Utils.stream2string(inputs.get(0));
				String anResult = Utils.stream2string(analysisResult);
				result = translator.translateAnalysisResult(aslanSpec, anResult);
			}
			else {
				TranslatorOptions wsOptions = new TranslatorOptions();
				wsOptions.setGoalsAsAttackStates(options.isGoalsAsAttackStates());
				wsOptions.setHornClausesLevel(Enum.valueOf(HornClausesLevel.class, options.getHornClausesLevel().name()));
				wsOptions.setOptimizationLevel(Enum.valueOf(OptimizationLevel.class, options.getOptimizationLevel().name()));
				wsOptions.setOrchestrationClient(options.getOrchestrationClient());
				wsOptions.setPreprocess(options.isPreprocess());
				wsOptions.setPrettyPrint(options.isPrettyPrint());
				wsOptions.setStripOutput(options.isStripOutput());

				String[] aslanppSpecs = new String[inputs.size()];
				for (int i = 0; i < inputs.size(); i++) {
					aslanppSpecs[i] = Utils.stream2string(inputs.get(i));
				}

				result = translator.translate(wsOptions, fileName, aslanppSpecs);
			}
			if (result != null) {
				if (result.getSpecification() != null) {
					output.print(result.getSpecification());
				}

				if (result.getErrors() != null && result.getErrors().size() > 0) {
					for (Error e: result.getErrors()) {
						System.err.println(e);
					}
				}
				if (result.getWarnings() != null && result.getWarnings().size() > 0) {
					for (Error e : result.getWarnings()) {
						System.err.println(e);
					}
				}
			}
		}
		catch (Exception e) {
			reportException("Exception while performing translation.", e, System.err);
		}
	}

	private static void reportException(String message, Throwable e, PrintStream out) {
		out.print("Error: ");
		out.println(message);
		if (e != null) {
			out.println(e.getMessage());
		}
		out.println();
	}

}
