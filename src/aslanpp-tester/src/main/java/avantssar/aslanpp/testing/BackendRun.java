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
import java.io.PrintStream;
import avantssar.aslan.backends.Verdict;

public class BackendRun {

	@SuppressWarnings("unused")
	private final String backendName;
	private final File outputDir;
	private File output;
	private File error;
	private final Verdict verdict;

	public BackendRun(String backendName, File outputDir, File output, File error, Verdict verdict) {
		this.backendName = backendName;
		this.outputDir = outputDir;
		this.output = output;
		this.error = error;
		this.verdict = verdict;
	}

	public void transformIntoHTML() {
		output = HTMLHelper.toHTML(output, false);
		error = HTMLHelper.toHTML(error, false);
	}

	public void report(PrintStream out, boolean odd, Verdict expectedVerdict) {
		out.print("<td style='background-color: ");
		if (verdict == expectedVerdict) {
			out.print(TranslationReport.GREEN(odd));
		}
		else {
			out.print(TranslationReport.RED(odd));
		}
		out.print(";'>");
		HTMLHelper.fileOrDash(out, outputDir, output, verdict.toString());
		out.print("&nbsp;&nbsp;&nbsp;");
		if (error != null) {
			HTMLHelper.fileOrDash(out, outputDir, error, "stderr");
		}
		out.print("</td>");
	}

}
