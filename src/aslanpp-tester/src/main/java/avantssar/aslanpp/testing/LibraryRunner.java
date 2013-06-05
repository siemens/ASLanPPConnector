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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import org.avantssar.aslan.IASLanSpec;
import org.avantssar.aslanpp.ASLanPPConnectorImpl;
import org.avantssar.aslanpp.TranslatorOutput;
import org.avantssar.aslanpp.model.ASLanPPSpecification;
import org.avantssar.aslanpp.model.EntityManager;
import org.avantssar.aslanpp.visitors.PrettyPrinter;
import org.avantssar.commons.ChannelEntry;
import org.avantssar.commons.ChannelModel;
import org.avantssar.commons.TranslatorOptions;
import org.avantssar.commons.ChannelEntry.Type;
import org.avantssar.commons.TranslatorOptions.HornClausesLevel;
import avantssar.aslan.backends.BackendsManager;
import avantssar.aslan.backends.IBackendRunner;
import avantssar.aslan.backends.Verdict;
import avantssar.aslanpp.library.AbstractSpecProvider;
import avantssar.aslanpp.library.HashFunction;

public class LibraryRunner {

	private final File workingDir;
	private final List<Class<?>> models = new ArrayList<Class<?>>();
	private BackendsManager bm;

	public LibraryRunner() {
		this(null);
	}

	public LibraryRunner(String workingDir) {
		this.workingDir = new File(workingDir == null ? System.getProperty("java.io.tmpdir") : workingDir);
		if (!this.workingDir.isDirectory() || !this.workingDir.exists()) {
			throw new IllegalArgumentException("Cannot find working dir for running specifications: " + this.workingDir.getAbsolutePath());
		}

		try {
			bm = BackendsManager.instance();
		}
		catch (IOException e) {
			bm = null;
			e.printStackTrace();
		}
		if (bm == null) {
			throw new RuntimeException("No backend runners!");
		}

		Class<?>[] allClasses = getClasses(AbstractSpecProvider.class.getPackage().getName());
		SortedMap<String, Class<?>> temp = new TreeMap<String, Class<?>>();
		for (Class<?> c : allClasses) {
			if (IChannelModelFlexibleSpecProvider.class.isAssignableFrom(c) && c.isAnnotationPresent(Specification.class)) {
				temp.put(c.getName(), c);
			}
		}
		models.addAll(temp.values());
	}

	public void run() {
		System.out.println("Running in directory " + workingDir.getAbsolutePath());
		System.out.println();
		TranslatorOptions options = new TranslatorOptions();
		options.setStripOutput(true);
		options.setGoalsAsAttackStates(true);
		options.setHornClausesLevel(HornClausesLevel.ALL);

		run(ChannelModel.CCM, options);
		run(ChannelModel.ACM, options);
	}

	private void run(ChannelModel cm, TranslatorOptions options) {
		System.out.println();
		System.out.println("===============" + repeat("=", cm.toString().length()));
		System.out.println("Channel model: " + cm);
		System.out.println("===============" + repeat("=", cm.toString().length()));
		System.out.println();
		System.out.print(String.format("%-48s%10s", "Specification", "Expected"));
		for (IBackendRunner br : bm.getBackendRunners()) {
			System.out.print(String.format("%10s", br.getName()));
		}
		System.out.println();
		System.out.println(repeat("-", 48 + 10 + 10 * bm.getBackendRunners().size()));
		for (Class<?> clz : models) {
			run(cm, options, clz);
		}
	}

	public void run(ChannelModel cm, TranslatorOptions options, Class<?> clz) {
		if (IChannelModelFlexibleSpecProvider.class.isAssignableFrom(clz) && clz.isAnnotationPresent(Specification.class)) {
			try {
				Constructor<?> c = clz.getConstructor(new Class<?>[] {});
				IChannelModelFlexibleSpecProvider specProvider = (IChannelModelFlexibleSpecProvider) c.newInstance(new Object[] {});
				specProvider.setChannelModel(cm);
				if (specProvider instanceof IChannelTypeFlexibleSpecProvider) {
					IChannelTypeFlexibleSpecProvider mmSpecProvider = (IChannelTypeFlexibleSpecProvider) specProvider;
					for (Type t : Type.values()) {
						mmSpecProvider.setChannelType(ChannelEntry.from(t, false, false, false, null));
						Verdict expected = specProvider.getExpectedVerdict();
						runSpecification(specProvider, options, expected);
					}
				}
				else {
					Verdict expected = specProvider.getExpectedVerdict();
					runSpecification(specProvider, options, expected);
				}
			}
			catch (Exception e) {
				System.out.println("Exception: " + e.getMessage());
				e.printStackTrace();
			}
		}
	}

	private void runSpecification(IChannelModelFlexibleSpecProvider specProvider, TranslatorOptions options, Verdict expectedVerdict) throws IOException {

		ASLanPPSpecification aslanPPspec = specProvider.getASLanPPSpecification();

		System.out.print(String.format("%-48s%10s", aslanPPspec.getSpecificationName(), expectedVerdict));

		String filePrefix = workingDir.getAbsolutePath() + File.separator + aslanPPspec.getSpecificationName() + "_" + aslanPPspec.getChannelModel().toString();

		try {

			// Write the specification to a first file.
			String firstPPfile = filePrefix + ".a.aslan++";
			aslanPPspec.toFile(firstPPfile);

			// Load it back.
			ASLanPPSpecification aslanPPspecBack = ASLanPPSpecification.fromStream(new EntityManager(), firstPPfile, new FileInputStream(firstPPfile), null);

			// Write it to another file.
			String secondPPfile = filePrefix + ".b.aslan++";
			if (aslanPPspecBack != null) 
				aslanPPspecBack.toFile(secondPPfile);

			// Load it back again.
			ASLanPPSpecification aslanPPspecBackAgain = ASLanPPSpecification.fromStream(new EntityManager(), secondPPfile, new FileInputStream(secondPPfile), null);

			// Make sure they are the same.
			if (aslanPPspecBack != null && aslanPPspecBackAgain != null &&
						aslanPPspecBack.toString().equals(aslanPPspecBackAgain.toString())) {
				// Make sure their detailed representations are the same.
				PrettyPrinter ppOne = new PrettyPrinter(true);
				aslanPPspecBack.finalize(false);
				aslanPPspecBack.accept(ppOne);
				ppOne.toFile(filePrefix + ".ad.aslan++");
				PrettyPrinter ppTwo = new PrettyPrinter(true);
				aslanPPspecBackAgain.finalize(false);
				aslanPPspecBackAgain.accept(ppTwo);
				ppTwo.toFile(filePrefix + ".bd.aslan++");
				if (ppOne.toString().equals(ppTwo.toString())) {
					// Get the expected ASLan specification.
					IASLanSpec expectedSpec = specProvider.getExpectedASLanTranslation(options);
					String expectedSpecAsString = expectedSpec.getStrippedRepresentation();
					File aslanFile = new File(filePrefix + ".e.aslan");
					writeToFile(aslanFile.getAbsolutePath(), expectedSpecAsString);

					// Make the translation. Use the original ASLan++ model,
					// without line info.
					ASLanPPConnectorImpl translator = new ASLanPPConnectorImpl();
					TranslatorOutput result = translator.translate(options, firstPPfile, aslanPPspec.toString());
					String aslanSpecAsString = result.getSpecification();
					writeToFile(filePrefix + ".t.aslan", aslanSpecAsString);

					if (expectedSpecAsString.equals(aslanSpecAsString)) {
						for (IBackendRunner br : bm.getBackendRunners()) {
							List<String> pars = specProvider.getBackendParameters(br.getName());
							String pref = filePrefix + "_" + br.getName().toLowerCase().replaceAll("[^a-z]", "");
							File captureOutput = new File(pref + ".out.txt");
							File captureError = new File(pref + ".err.txt");
							PrintStream fout = new PrintStream(captureOutput);
							PrintStream ferr = new PrintStream(captureError);
							Verdict v = br.analyze(aslanFile, pars, fout, ferr);
							fout.close();
							ferr.close();
							if (v.equals(expectedVerdict)) {
								System.out.print(String.format("%10s", ":-)"));
							}
							else {
								System.out.print(String.format("%10s", v.toString()));
								saveWrong(aslanSpecAsString, br, aslanPPspec.getSpecificationName(), aslanPPspec.getChannelModel());
							}
						}
						System.out.println();
					}
					else {
						System.out.println(String.format("%20s", "Wrong output from translator."));
					}
				}
				else {
					System.out.println(String.format("%20s", "Problem at details with the translator."));
				}
			}
			else {
				System.out.println(String.format("%20s", "Problem with the translator."));
			}
		}
		catch (Exception ex) {
			System.out.println(String.format("%20s", "Exception"));
			PrintWriter pw;
			try {
				pw = new PrintWriter(filePrefix + ".error.txt");
				ex.printStackTrace(pw);
				pw.close();
			}
			catch (FileNotFoundException e1) {}
		}
	}

	private void saveWrong(String aslanSpecAsString, IBackendRunner br, String specName, ChannelModel cm) {
		String filePrefix = workingDir.getAbsolutePath() + File.separator + "problematic" + File.separator + br.getName();
		File dir = new File(filePrefix);
		dir.mkdirs();
		if (dir.exists() && dir.isDirectory()) {
			String fileName = filePrefix + File.separator + specName + "_" + cm.toString() + ".aslan";
			writeToFile(fileName, aslanSpecAsString);
		}
	}

	private static void writeToFile(String file, String content) {
		PrintWriter pw;
		try {
			pw = new PrintWriter(file);
			pw.print(content);
			pw.close();
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	private String repeat(String s, int n) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < n; i++) {
			sb.append(s);
		}
		return sb.toString();
	}

	// Taken from http://snippets.dzone.com/posts/show/4831
	@SuppressWarnings("rawtypes")
	private static Class[] getClasses(String packageName) {
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		assert classLoader != null;
		String path = packageName.replace('.', '/');
		Enumeration<URL> resources;
		ArrayList<Class> classes = new ArrayList<Class>();
		try {
			resources = classLoader.getResources(path);
			List<File> dirs = new ArrayList<File>();
			while (resources.hasMoreElements()) {
				URL resource = resources.nextElement();
				dirs.add(new File(resource.getFile()));
			}
			for (File directory : dirs) {
				classes.addAll(findClasses(directory, packageName));
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		return classes.toArray(new Class[classes.size()]);
	}

	@SuppressWarnings("rawtypes")
	private static List<Class> findClasses(File directory, String packageName) {
		List<Class> classes = new ArrayList<Class>();
		if (!directory.exists()) {
			return classes;
		}
		File[] files = directory.listFiles();
		for (File file : files) {
			if (file.isDirectory()) {
				assert !file.getName().contains(".");
				classes.addAll(findClasses(file, packageName + "." + file.getName()));
			}
			else if (file.getName().endsWith(".class")) {
				try {
					classes.add(Class.forName(packageName + '.' + file.getName().substring(0, file.getName().length() - 6)));
				}
				catch (ClassNotFoundException e) {
					System.out.println("Failed to load class from file '" + file.getAbsolutePath() + "'.");
				}
			}
		}
		return classes;
	}

	public static void main(String[] args) {
		LibraryRunner lr = new LibraryRunner();
		// lr.run();
		TranslatorOptions options = new TranslatorOptions();
		options.setStripOutput(true);
		options.setGoalsAsAttackStates(true);
		options.setHornClausesLevel(HornClausesLevel.ALL);
		lr.run(ChannelModel.CCM, options, HashFunction.class);
		// lr.run(ChannelModel.ACM, options, SymbolicInstances.class);
	}
}
