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

package avantssar.aslanpp.testing;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.apache.commons.io.FileUtils;
import org.avantssar.aslanpp.Debug;
import avantssar.aslan.backends.IBackendRunner;
import avantssar.aslan.backends.Verdict;

public class TranslationInstance {

	private final File outputDir;
	private File aslanPPfile;
	private File aslanPPcheck1;
	private File aslanPPcheck1det;
	private File aslanPPcheck2;
	private File aslanPPcheck2det;
	private File errorsFile;
	private File warningsFile;
	private File aslanFile;
	private File expectedASLanFile;
	private final Verdict expectedVerdict;
	private final Map<String, BackendRun> verdicts = new TreeMap<String, BackendRun>();
	private int errors = 0, warnings = 0;

	protected TranslationInstance(File outputDir, File aslanPPfile, File aslanPPcheck1, File aslanPPcheck1det, File aslanPPcheck2, File aslanPPcheck2det, int errors, File errorsFile, int warnings,
			File warningsFile, File expectedASLanFile, File aslanFile, Verdict expectedVerdict) {
		this.outputDir = outputDir;
		this.aslanPPfile = aslanPPfile;
		this.aslanPPcheck1 = aslanPPcheck1;
		this.aslanPPcheck1det = aslanPPcheck1det;
		this.aslanPPcheck2 = aslanPPcheck2;
		this.aslanPPcheck2det = aslanPPcheck2det;
		this.errors = errors;
		this.errorsFile = errorsFile;
		this.warnings = warnings;
		this.warningsFile = warningsFile;
		this.expectedASLanFile = expectedASLanFile;
		this.aslanFile = aslanFile;
		this.expectedVerdict = expectedVerdict;
	}

	public boolean hasTranslation() {
		return aslanFile != null;
	}

	public void putVerdict(IBackendRunner br, Verdict v, File output, File error) {
		synchronized (verdicts) {
			verdicts.put(br.getName(), new BackendRun(br.getName(), outputDir, output, error, v));
		}
	}

	public void transformIntoHTML() {
		for (BackendRun br : verdicts.values()) {
			br.transformIntoHTML();
		}
		aslanPPfile = HTMLHelper.toHTML(aslanPPfile, true);
		aslanPPcheck1 = HTMLHelper.toHTML(aslanPPcheck1, true);
		aslanPPcheck1det = HTMLHelper.toHTML(aslanPPcheck1det, true);
		aslanPPcheck2 = HTMLHelper.toHTML(aslanPPcheck2, true);
		aslanPPcheck2det = HTMLHelper.toHTML(aslanPPcheck2det, true);
		errorsFile = HTMLHelper.toHTML(errorsFile, false);
		warningsFile = HTMLHelper.toHTML(warningsFile, false);
		expectedASLanFile = HTMLHelper.toHTML(expectedASLanFile, true);
		aslanFile = HTMLHelper.toHTML(aslanFile, true);
	}

	public void report(PrintStream out, int index, List<String> backendNames, boolean odd) {
		out.print("<tr style='background-color: ");
		out.print(TranslationReport.BG(odd));
		out.println(";'>");
		out.println("<td style='text-align: right;'>" + index + "</td>");

		out.print("<td");
		boolean different = false;
		boolean differentDet = false;
		if (aslanPPcheck1 != null || aslanPPcheck2 != null) {
			try {
				if (aslanPPcheck1 != null && aslanPPcheck2 != null) {
					different = !FileUtils.contentEquals(aslanPPcheck1, aslanPPcheck2);
				}
				else {
					different = true;
				}
				if (aslanPPcheck1det != null && aslanPPcheck2det != null) {
					differentDet = !FileUtils.contentEquals(aslanPPcheck1det, aslanPPcheck2det);
				}
				else {
					differentDet = aslanPPcheck1det != null || aslanPPcheck2det != null;
				}
				out.print(" style='background-color: ");
				if (different || differentDet) {
					out.print(TranslationReport.RED(odd));
				}
				else {
					out.print(TranslationReport.GREEN(odd));
				}
				out.print(";'");
			}
			catch (IOException e) {
				Debug.logger.error("Failed to compare files '" + aslanPPcheck1.getAbsolutePath() + "' and '" + aslanPPcheck2.getAbsolutePath() + "'.", e);
			}
		}
		out.print(">");
		HTMLHelper.fileOrDash(out, outputDir, aslanPPfile, null);
		// if (different) {
		// out.print(" ");
		// HTMLHelper.fileOrDash(out, aslanPPcheck1, "pp1");
		// out.print(" ");
		// HTMLHelper.fileOrDash(out, aslanPPcheck2, "pp2");
		// }
		// if (differentDet) {
		// out.print(" ");
		// HTMLHelper.fileOrDash(out, aslanPPcheck1det, "pp1d");
		// out.print(" ");
		// HTMLHelper.fileOrDash(out, aslanPPcheck2det, "pp2d");
		// }
		out.println("</td>");

		out.print("<td style='background-color: ");
		if (errors > 0) {
			out.print(TranslationReport.RED(odd));
		}
		else {
			out.print(TranslationReport.GREEN(odd));
		}
		out.print(";'>");
		HTMLHelper.fileOrDash(out, outputDir, errorsFile, errors > 0 ? Integer.toString(errors) : "-");
		out.println("</td>");
		out.print("<td style='background-color: ");
		if (warnings > 0) {
			out.print(TranslationReport.RED(odd));
		}
		else {
			out.print(TranslationReport.GREEN(odd));
		}
		out.print(";'>");
		HTMLHelper.fileOrDash(out, outputDir, warningsFile, warnings > 0 ? Integer.toString(warnings) : "-");
		out.println("</td>");
		filesCompared(out, aslanFile, expectedASLanFile, "expected", odd);
		out.println("<td>" + expectedVerdict.toString() + "</td>");
		for (String s : backendNames) {
			BackendRun br = verdicts.get(s);
			if (br != null) {
				br.report(out, odd, expectedVerdict);
			}
			else {
				out.print("<td>-</td>");
			}
		}
		out.println("</tr>");
	}

	private void filesCompared(PrintStream out, File f, File ref, String label, boolean odd) {
		out.print("<td");
		boolean different = false;
		if (f != null && ref != null) {
			try {
				different = !FileUtils.contentEquals(f, ref);
				out.print(" style='background-color: ");
				if (different) {
					out.print(TranslationReport.RED(odd));
				}
				else {
					out.print(TranslationReport.GREEN(odd));
				}
				out.print(";'");
			}
			catch (IOException e) {
				Debug.logger.error("Failed to compare files '" + f.getAbsolutePath() + "' and '" + ref.getAbsolutePath() + "'.", e);
			}
		}
		out.print(">");
		HTMLHelper.fileOrDash(out, outputDir, f, null);
		if (ref != null && different) {
			out.print(" ");
			HTMLHelper.fileOrDash(out, outputDir, ref, label);
		}
		out.println("</td>");
	}
}
