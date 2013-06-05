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
import org.apache.commons.io.FilenameUtils;
import avantssar.aslan.backends.IBackendRunner;
import avantssar.aslan.backends.Verdict;

public class BackendTask implements Runnable {

	private static Integer nextIndex = 1;

	private final IBackendRunner runner;
	private final List<String> parameters = new ArrayList<String>();
	private final File aslanFile;
	private final TranslationInstance ti;

	public BackendTask(IBackendRunner runner, List<String> parameters, File aslanFile, TranslationInstance ti) {
		this.runner = runner;
		if (parameters != null) {
			this.parameters.addAll(parameters);
		}
		this.aslanFile = aslanFile;
		this.ti = ti;
	}

	public void run() {
		//int idx = 0;
		synchronized (nextIndex) {
			//idx = nextIndex;
			nextIndex++;
		}
		System.out.print("+");
		String runnerPrefix = FilenameUtils.removeExtension(aslanFile.getAbsolutePath()) + "." + runner.getName().replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
		File outputFile = new File(runnerPrefix + ".output.txt");
		File errorsFile = new File(runnerPrefix + ".errors.txt");
		try {
			PrintStream captureOutput = new PrintStream(outputFile);
			PrintStream captureErrors = new PrintStream(errorsFile);
			// runner.purgeParameters();
			// for (String p : parameters) {
			// runner.addParameter(p);
			// }
			Verdict v = runner.analyze(aslanFile, parameters, captureOutput, captureErrors);
			captureOutput.close();
			captureErrors.close();
			outputFile = Tester.deleteIfZero(outputFile);
			errorsFile = Tester.deleteIfZero(errorsFile);
			ti.putVerdict(runner, v, outputFile, errorsFile);
		}
		catch (FileNotFoundException e) {
			ti.putVerdict(runner, Verdict.Error, null, null);
		}
		System.out.print("-");
	}

}
