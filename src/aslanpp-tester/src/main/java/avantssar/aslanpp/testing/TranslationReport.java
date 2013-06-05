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
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import avantssar.aslan.backends.IBackendRunner;
import avantssar.aslan.backends.Verdict;

public class TranslationReport {

	public static String RED(boolean odd) {
		return odd ? "#DD3333" : "#FF5555";
	}

	public static String GREEN(boolean odd) {
		return odd ? "#33DD33" : "#55FF55";
	}

	public static String BG(boolean odd) {
		return odd ? "#DDDDDD" : "#FFFFFF";
	}

	private final File outputDir;
	private final List<TranslationInstance> specs = new ArrayList<TranslationInstance>();
	private final List<String> backendNames = new ArrayList<String>();

	public TranslationReport(List<IBackendRunner> backends, File outputDir) {
		this.outputDir = outputDir;
		for (IBackendRunner br : backends) {
			backendNames.add(br.getName());
		}
	}

	public TranslationInstance newInstance(File aslanPPfile, File aslanPPcheck1, File aslanPPcheck1det, File aslanPPcheck2, File aslanPPcheck2det, int errors, File errorsFile, int warnings,
			File warningsFile, File expectedASLanFile, File aslanFile, Verdict expectedVerdict) {
		TranslationInstance ti = new TranslationInstance(outputDir, aslanPPfile, aslanPPcheck1, aslanPPcheck1det, aslanPPcheck2, aslanPPcheck2det, errors, errorsFile, warnings, warningsFile,
				expectedASLanFile, aslanFile, expectedVerdict);
		specs.add(ti);
		return ti;
	}

	private void transformIntoHTML() {
		for (TranslationInstance ti : specs) {
			ti.transformIntoHTML();
		}
	}

	public void report(String htmlFile) {
		transformIntoHTML();

		PrintStream out;
		try {
			out = new PrintStream(htmlFile);
			out.println("<html>");
			out.println("<head>");
			out.println("<style>");
			out.println("<!--");
			out.println("th { font-size: 8pt; }");
			out.println("td { font-size: 8pt; }");
			out.println("a {color: black;}");
			out.println("-->");
			out.println("</style>");
			out.println("</head>");
			out.println("<body>");
			out.println("<table border='1' cellspacing='0' cellpadding='1'>");
			out.print("<tr>");
			out.print("<th>No.</th>");
			out.print("<th>ASLan++</th>");
			out.print("<th>Errors</th>");
			out.print("<th>Warnings</th>");
			out.print("<th>ASLan</th>");
			out.print("<th>Exp. Verdict</th>");
			for (String s : backendNames) {
				out.print("<th>");
				out.print(s);
				out.print("</th>");
			}
			out.println("</tr>");
			boolean odd = true;
			int idx = 1;
			for (TranslationInstance ti : specs) {
				ti.report(out, idx++, backendNames, odd);
				odd = !odd;
			}
			out.println("</table>");
			out.println("</body>");
			out.println("</html>");
			out.close();
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
}
