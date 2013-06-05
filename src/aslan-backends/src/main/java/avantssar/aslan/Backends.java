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

package avantssar.aslan;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import org.kohsuke.args4j.CmdLineException;
import avantssar.aslan.backends.BackendsManager;
import avantssar.aslan.backends.IBackendRunner;
import avantssar.aslan.backends.Verdict;

public class Backends {

	public static void main(String[] args) {
		CommandLineOptions options = new CommandLineOptions();
		try {
			options.getParser().parseArgument(args);
			options.ckeckAtEnd();
		}
		catch (CmdLineException ex) {
			System.err.println("Error: " + ex.getMessage());
			System.err.println();
			options.showShortHelp(System.err);
			return;
		}

		if (options.isShowHelp()) {
			options.showLongHelp(System.out);
			return;
		}

		BackendsManager bm = null;
		try {
			bm = BackendsManager.instance();
			if (bm == null) {
				BackendsManager.printFirstTimeMessage(System.out);
			}
			else {
				System.out.println("Found " + bm.getBackendRunners().size() + " backends:");
				System.out.println();
				for (IBackendRunner br : bm.getBackendRunners()) {
					System.out.println(br.getFullDescription());
				}

				File canon = options.getIn().getCanonicalFile();
				String fileNameNoExt = canon.getName();
				if (fileNameNoExt.indexOf(".") > 0) {
					fileNameNoExt = fileNameNoExt.substring(0, fileNameNoExt.lastIndexOf("."));
				}
				for (IBackendRunner br : bm.getBackendRunners()) {
					System.out.print(br.getName() + " says... ");
					String base = canon.getParent() + File.separator + fileNameNoExt + "." + suffix(br.getName());
					File outputFile = new File(base + ".out.txt");
					File errFile = new File(base + ".err.txt");
					PrintStream output = new PrintStream(outputFile);
					PrintStream err = new PrintStream(errFile);
					Verdict v = br.analyze(canon, new ArrayList<String>(), output, err);
					output.close();
					err.close();
					System.out.println(v.toString());
				}
			}
		}
		catch (Exception e) {
			System.err.println("Exception: " + e.getMessage());
		}
	}

	private static String suffix(String s) {
		return s.replaceAll("[^A-Za-z0-9]", "").toLowerCase();
	}
}
