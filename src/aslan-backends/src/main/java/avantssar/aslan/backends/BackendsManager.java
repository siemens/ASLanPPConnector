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

package avantssar.aslan.backends;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;

public class BackendsManager {

	public static class SortedProperties extends Properties {

		private static final long serialVersionUID = 8055870071142987426L;

		@Override
		@SuppressWarnings({"unchecked", "rawtypes"})
		public synchronized Enumeration keys() {
			Enumeration keysEnum = super.keys();
			Vector keyList = new Vector();
			while (keysEnum.hasMoreElements()) {
				keyList.add(keysEnum.nextElement());
			}
			Collections.sort(keyList);
			return keyList.elements();
		}
	}

	private static final String FILE_NAME = "backends.properties";
	private static final String OPTIONS = "options";
	private static final String TIMEOUT = "timeout";
	private static final String[] BACKEND_NAMES = new String[] { CLAtSeBackendRunner.NAME, OFMCBackendRunner.NAME, SATMCBackendRunner.NAME };

	public static BackendsManager instance() throws IOException {
		File f = new File(FILE_NAME);
		if (!f.exists()) {
			Properties p = new SortedProperties();
			for (String name : BACKEND_NAMES) {
				fillBackend(p, name);
			}
			p.store(new PrintStream(f), " Sample settings for running the ASLAn backends.\n" + " In order to run a backend, its executable must be specified.\n"
					+ " The other lines (with timeout and parameters) are optional.\n" + " Program parameters are separated by spaces.\n" + " Timeouts are given in seconds.\n");
			return null;
		}
		else {
			return new BackendsManager(f);
		}
	}

	public static void printFirstTimeMessage(PrintStream out) {
		out.println("It seems you do not have a configuration file for running the ASLAN backends.");
		File f = new File(BackendsManager.FILE_NAME);
		out.println("A sample file was generated for you at " + f.getAbsolutePath() + ".");
		out.println("Please fill it with the correct data and run the program again.");
	}

	private final Properties props = new Properties();
	private final List<IBackendRunner> backends = new ArrayList<IBackendRunner>();

	private BackendsManager(File f) throws IOException {
		props.load(new FileInputStream(f));
		String clatsePath = props.getProperty(toKey(CLAtSeBackendRunner.NAME));
		if (clatsePath != null) {
			try {
				CLAtSeBackendRunner clatse = new CLAtSeBackendRunner(clatsePath);
				completeBackend(clatse);
				backends.add(clatse);
			}
			catch (BackendRunnerInstantiationException e) {
				System.err.println("Error while preparing " + CLAtSeBackendRunner.NAME + " backend runner: " + e.getMessage() + ".");
			}
		}

		String ofmcPath = props.getProperty(toKey(OFMCBackendRunner.NAME));
		if (ofmcPath != null) {
			try {
				OFMCBackendRunner ofmc = new OFMCBackendRunner(ofmcPath);
				completeBackend(ofmc);
				backends.add(ofmc);
			}
			catch (BackendRunnerInstantiationException e) {
				System.err.println("Error while preparing " + OFMCBackendRunner.NAME + " backend runner: " + e.getMessage() + ".");
			}
		}

		String satmcPath = props.getProperty(toKey(SATMCBackendRunner.NAME));
		if (satmcPath != null) {
			try {
				SATMCBackendRunner satmc = new SATMCBackendRunner(satmcPath);
				completeBackend(satmc);
				backends.add(satmc);
			}
			catch (BackendRunnerInstantiationException e) {
				System.err.println("Error while preparing " + SATMCBackendRunner.NAME + " backend runner: " + e.getMessage() + ".");
			}
		}
	}

	public List<IBackendRunner> getBackendRunners() {
		return backends;
	}

	private void completeBackend(IBackendRunner br) {
		String key = toKey(br.getName());
		String timeoutKey = key + "_" + TIMEOUT;
		if (props.containsKey(timeoutKey)) {
			String timeoutValue = props.getProperty(timeoutKey);
			try {
				long t = Long.parseLong(timeoutValue);
				br.setTimeout(t);
			}
			catch (NumberFormatException e) {
				System.err.println("Invalid timeout value '" + timeoutValue + "' for backend runner " + br.getName() + ". Defaulting to " + br.getTimeout() + ".");
			}
		}

		String optsKey = key + "_" + OPTIONS;
		if (props.containsKey(optsKey)) {
			String opts = props.getProperty(optsKey);
			StringTokenizer st = new StringTokenizer(opts);
			while (st.hasMoreTokens()) {
				br.addDefaultParameter(st.nextToken());
			}
		}
	}

	private static void fillBackend(Properties props, String name) {
		String key = toKey(name);
		props.setProperty(key, "/path/to/" + key);
		props.setProperty(key + "_" + OPTIONS, "options for " + name);
		props.setProperty(key + "_" + TIMEOUT, Integer.toString(IBackendRunner.DEFAULT_TIMEOUT_IN_SECCONDS));
	}

	private static String toKey(String s) {
		return s.replaceAll("[^A-Za-z0-9]", "").toLowerCase();
	}
}
