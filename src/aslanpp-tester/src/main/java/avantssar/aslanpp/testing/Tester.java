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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.avantssar.aslanpp.ASLanPPConnectorImpl;
import org.avantssar.aslanpp.Debug;
import org.avantssar.aslanpp.TranslatorOutput;
import org.avantssar.aslanpp.Debug.LogLevel;
import org.avantssar.aslanpp.model.EntityManager;
import org.avantssar.commons.TranslatorOptions;
import org.avantssar.commons.TranslatorOptions.HornClausesLevel;
import org.kohsuke.args4j.CmdLineException;
import avantssar.aslan.backends.BackendRunnerInstantiationException;
import avantssar.aslan.backends.BackendsManager;
import avantssar.aslan.backends.IBackendRunner;

public class Tester {

	private static BackendsManager bm = null;
	private static long finalTimeout = 60;
	private static ExecutorService pool;

	public static void main(String[] args) {

		Debug.initLog(LogLevel.INFO);

		TesterCommandLineOptions options = new TesterCommandLineOptions();
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

		ASLanPPConnectorImpl translator = new ASLanPPConnectorImpl();

		if (options.isShowVersion()) {
			System.out.println(translator.getFullTitleLine());
			return;
		}

		ISpecificationBundleProvider sbp;
		File realInDir;
		if (options.isLibrary()) {
			if (options.getHornClausesLevel() != HornClausesLevel.ALL) {
				System.out.println("When checking the internal library we output all Horn clauses.");
				options.setHornClausesLevel(HornClausesLevel.ALL);
			}
			if (!options.isStripOutput()) {
				System.out.println("When checking the internal library, the ouput is stripped of comments and line information.");
				options.setStripOutput(true);
			}
			File modelsDir = new File(FilenameUtils.concat(options.getOut().getAbsolutePath(), "_models"));
			try {
				FileUtils.forceMkdir(modelsDir);
			}
			catch (IOException e1) {
				System.out.println("Failed to create models folder: " + e1.getMessage());
				Debug.logger.error("Failed to create models folder.", e1);
			}
			realInDir = modelsDir;
			sbp = new LibrarySpecificationsProvider(modelsDir.getAbsolutePath());
		}
		else {
			realInDir = options.getIn();
			sbp = new DiskSpecificationsProvider(options.getIn().getAbsolutePath());
		}

		System.setProperty(EntityManager.ASLAN_ENVVAR, sbp.getASLanPath());
		// try {
		// EntityManager.loadASLanPath();
		// }
		// catch (IOException e) {
		// System.out.println("Exception while reloading ASLANPATH: " +
		// e.getMessage());
		// Debug.logger.error("Exception while loading ASLANPATH.", e);
		// }

		try {
			bm = BackendsManager.instance();
			if (bm != null) {
				for (IBackendRunner br : bm.getBackendRunners()) {
					System.out.println(br.getFullDescription());
					if (br.getTimeout() > finalTimeout) {
						finalTimeout = br.getTimeout();
					}
				}
			}
		}
		catch (IOException e) {
			System.out.println("Failed to load backends: " + e);
		}

		int threadsCount = 50;
		if (options.getThreads() > 0) {
			threadsCount = options.getThreads();
		}
		System.out.println("Will launch " + threadsCount + " threads in parallel (+ will show that a thread starts, - that a thread ends).");
		TranslationReport rep = new TranslationReport(bm != null ? bm.getBackendRunners() : new ArrayList<IBackendRunner>(), options.getOut());
		long startTime = System.currentTimeMillis();
		int specsCount = 0;
		pool = Executors.newFixedThreadPool(threadsCount);
		for (ITestTask task : sbp) {
			doTest(rep, task, realInDir, options.getOut(), translator, options, System.err);
			specsCount++;
		}
		pool.shutdown();
		String reportFile = FilenameUtils.concat(options.getOut().getAbsolutePath(), "index.html");
		try {
			while (!pool.awaitTermination(finalTimeout, TimeUnit.SECONDS)) {}
		}
		catch (InterruptedException e) {
			Debug.logger.error("Interrupted while waiting for pool termination.", e);
			System.out.println("Interrupted while waiting for pool termination: " + e.getMessage());
			System.out.println("The report may be incomplete.");
		}
		long endTime = System.currentTimeMillis();
		long duration = (endTime - startTime) / 1000;
		System.out.println();
		System.out.println(specsCount + " specifications checked in " + duration + " seconds.");
		rep.report(reportFile);
		System.out.println("You can find an overview report at '" + reportFile + "'.");
	}

	public static void doTest(TranslationReport rep, ITestTask task, File sourceBaseDir, File targetBaseDir, ASLanPPConnectorImpl translator, TesterCommandLineOptions options, PrintStream err) {
		String relativePath = FilenameUtils.normalize(task.getASLanPP().getAbsolutePath()).substring(FilenameUtils.normalizeNoEndSeparator(sourceBaseDir.getAbsolutePath()).length() + 1);
		File aslanPPinput = new File(FilenameUtils.concat(FilenameUtils.normalize(targetBaseDir.getAbsolutePath()), relativePath));

		try {
			FileUtils.copyFile(task.getASLanPP(), aslanPPinput, true);
			File aslanPPcheck1 = new File(FilenameUtils.removeExtension(aslanPPinput.getAbsolutePath()) + ".check1.aslan++");
			File aslanPPcheck1det = new File(FilenameUtils.removeExtension(aslanPPinput.getAbsolutePath()) + ".check1.detailed.aslan++");
			File aslanPPcheck1err = new File(FilenameUtils.removeExtension(aslanPPinput.getAbsolutePath()) + ".check1.errors.aslan++");
			File aslanPPcheck1warn = new File(FilenameUtils.removeExtension(aslanPPinput.getAbsolutePath()) + ".check1.warnings.aslan++");
			File aslanPPcheck2 = null;
			File aslanPPcheck2det = null;
			File aslanPPcheck2err = null;
			File aslanPPcheck2warn = null;
			File aslanFile = new File(FilenameUtils.removeExtension(aslanPPinput.getAbsolutePath()) + ".aslan");
			File errorsFile = new File(FilenameUtils.removeExtension(aslanPPinput.getAbsolutePath()) + ".errors.txt");
			File warningsFile = new File(FilenameUtils.removeExtension(aslanPPinput.getAbsolutePath()) + ".warnings.txt");

			// Load the file and write it back to another file.
			// Then again load it back and write it to another file.
			// The files should have the same content.
			String fileName = aslanPPinput.getAbsolutePath();
			String aslanppSpec = FileUtils.readFileToString(task.getASLanPP());
			TranslatorOptions optPP = new TranslatorOptions();
			optPP.setPrettyPrint(true);
			TranslatorOptions optPPdet = new TranslatorOptions();
			optPPdet.setPreprocess(true);
			// EntityManager.getInstance().purge();
			TranslatorOutput firstLoad = translator.translate(optPP, fileName, aslanppSpec);
			FileUtils.writeStringToFile(aslanPPcheck1, firstLoad.getSpecification());
			FileUtils.writeLines(aslanPPcheck1err, firstLoad.getErrors());
			aslanPPcheck1err = deleteIfZero(aslanPPcheck1err);
			FileUtils.writeLines(aslanPPcheck1warn, firstLoad.getWarnings());
			aslanPPcheck1warn = deleteIfZero(aslanPPcheck1warn);
			// EntityManager.getInstance().purge();
			TranslatorOutput firstLoadDet = translator.translate(optPPdet, fileName, aslanppSpec);
			// TODO should use translate_main, since result will be empty
			FileUtils.writeStringToFile(aslanPPcheck1det, firstLoadDet.getSpecification());
			if (firstLoad.getSpecification() != null) {
				aslanPPcheck2 = new File(FilenameUtils.removeExtension(aslanPPinput.getAbsolutePath()) + ".check2.aslan++");
				aslanPPcheck2det = new File(FilenameUtils.removeExtension(aslanPPinput.getAbsolutePath()) + ".check2.detailed.aslan++");
				aslanPPcheck2err = new File(FilenameUtils.removeExtension(aslanPPinput.getAbsolutePath()) + ".check2.errors.aslan++");
				aslanPPcheck2warn = new File(FilenameUtils.removeExtension(aslanPPinput.getAbsolutePath()) + ".check2.warnings.aslan++");
				// EntityManager.getInstance().purge();
				TranslatorOutput secondLoad = translator.translate(optPP, null, firstLoad.getSpecification());
				FileUtils.writeStringToFile(aslanPPcheck2, secondLoad.getSpecification());
				FileUtils.writeLines(aslanPPcheck2err, secondLoad.getErrors());
				aslanPPcheck2err = deleteIfZero(aslanPPcheck2err);
				FileUtils.writeLines(aslanPPcheck2warn, secondLoad.getWarnings());
				aslanPPcheck2warn = deleteIfZero(aslanPPcheck2warn);
				// EntityManager.getInstance().purge();
				TranslatorOutput secondLoadDet = translator.translate(optPPdet, null, firstLoad.getSpecification());
				FileUtils.writeStringToFile(aslanPPcheck2det, secondLoadDet.getSpecification());
			}

			// EntityManager.getInstance().purge();
			TranslatorOutput result = translator.translate(options, fileName, aslanppSpec);
			FileUtils.writeStringToFile(aslanFile, result.getSpecification());
			FileUtils.writeLines(errorsFile, result.getErrors());
			FileUtils.writeLines(warningsFile, result.getWarnings());

			File expASLan = null;
			if (task.getExpectedASLan() != null) {
				expASLan = new File(FilenameUtils.removeExtension(aslanPPinput.getAbsolutePath()) + ".expected.aslan");
				FileUtils.copyFile(task.getExpectedASLan(), expASLan, true);
			}

			TranslationInstance ti = rep.newInstance(aslanPPinput, deleteIfZero(aslanPPcheck1), deleteIfZero(aslanPPcheck1det), deleteIfZero(aslanPPcheck2), deleteIfZero(aslanPPcheck2det), result
					.getErrors().size(), deleteIfZero(errorsFile), result.getWarnings().size(), deleteIfZero(warningsFile), deleteIfZero(expASLan), deleteIfZero(aslanFile), task.getExpectedVerdict());
			if (ti.hasTranslation()) {
				if (bm != null && bm.getBackendRunners().size() > 0) {
					for (final IBackendRunner runner : bm.getBackendRunners()) {
						try {
							List<String> pars = task.getBackendParameters(runner.getName());
							pool.execute(new BackendTask(runner.spawn(), pars, aslanFile, ti));
						}
						catch (BackendRunnerInstantiationException bex) {
							Debug.logger.error("Failed to spawn backend " + runner.getName() + ".", bex);
						}
					}
				}
			}
		}
		catch (Exception e) {
			System.out.println("Exception while testing file '" + task.getASLanPP().getAbsolutePath() + "': " + e.getMessage());
			Debug.logger.error("Failed to test file '" + aslanPPinput + "'.", e);
		}
	}

	public static File deleteIfZero(File f) {
		if (f != null && f.length() == 0) {
			if (f.delete()) {
				return null;
			}
		}
		return f;
	}

	private static void reportException(String message, Throwable e, PrintStream out) {
		out.print("Error: ");
		out.println(message);
		out.println(e.getMessage());
		out.println();
		Debug.logger.fatal(message, e);
	}
}
