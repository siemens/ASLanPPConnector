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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class AbstractBackendRunner implements IBackendRunner {

	private final File binaryFile;
	private String version;
	private final List<String> defaultParameters = new ArrayList<String>();
	private long timeout = DEFAULT_TIMEOUT_IN_SECCONDS;

	protected AbstractBackendRunner(String binaryFile, String versionRegex) throws BackendRunnerInstantiationException {
		try {
			this.binaryFile = new File(binaryFile).getCanonicalFile();
		}
		catch (IOException e) {
			throw new BackendRunnerInstantiationException("Problem with binary '" + binaryFile + "' for backend runner for " + getName() + ".", e);
		}
		if (!this.binaryFile.exists() || !this.binaryFile.isFile()) {
			throw new BackendRunnerInstantiationException("Cannot find binary file '" + binaryFile + "' for backend " + getName() + ", or it is not a regular file.");
		}
		try {
			ProcessBuilder pb = getProcessBuilder(Arrays.asList(new String[] { "--version" }));
			Process p = pb.start();
			p.waitFor();
			String output = collectStream(p.getInputStream());
			Pattern pattern = Pattern.compile(versionRegex);
			Matcher matcher = pattern.matcher(output);
			if (matcher.find()) {
				version = matcher.group(1);
			}
			else {
				version = "Unknown version";
			}
		}
		catch (Exception e) {
			throw new BackendRunnerInstantiationException("Failed to obtain version from backend " + getName() + ".", e);
		}
	}

	public String getVersion() {
		return version;
	}

	public String getTitleLine() {
		return getName() + " " + version;
	}

	public String getFullDescription() {
		StringBuffer sb = new StringBuffer();
		sb.append(getTitleLine()).append("\n");
		sb.append(binaryFile.getAbsolutePath());
		for (String s : defaultParameters) {
			sb.append(" ").append(s);
		}
		sb.append("\n");
		sb.append("Timeout: ").append(timeout).append(" s").append("\n");
		return sb.toString();
	}

	public File getExecutable() {
		return binaryFile;
	}

	public final void addDefaultParameter(String flag) {
		defaultParameters.add(flag);
	}
    
    public final List<String> getDefaultParameters() {
        return Collections.unmodifiableList(defaultParameters);
    }

	// public List<String> getParameters() {
	// // don't return the original collection
	// List<String> copy = new ArrayList<String>();
	// copy.addAll(parameters);
	// return copy;
	// }
	//
	// public void purgeParameters() {
	// parameters.clear();
	// }

	public long getTimeout() {
		return timeout;
	}

	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}

	public synchronized Verdict analyze(File aslanFile, List<String> parameters, PrintStream out, PrintStream err) {
		List<String> cmd = getCommand(aslanFile, parameters);
		logCommand(cmd);
		ProcessBuilder pb = getProcessBuilder(cmd);
		Verdict v = null;
		try {
			Process p = pb.start();
			try {
				long now = System.currentTimeMillis();
				long finish = now + 1000L * timeout;
				while (isAlive(p) && (System.currentTimeMillis() < finish)) {
					Thread.sleep(10);
				}
				if (isAlive(p)) {
					throw new InterruptedException("Program stopped after " + timeout + " seconds.");
				}
				int result = p.exitValue();

				String errFromBackend = collectStream(p.getErrorStream());
				String outputFromBackend = collectStream(p.getInputStream());
				if (result == 0) {
					v = giveVerdict(outputFromBackend);
				}
				else {
					err.println("Program returned error code " + result + ".\n");
					v = Verdict.Error;
				}
				out.print(outputFromBackend);
				err.print(errFromBackend);
			}
			catch (InterruptedException ie) {
				p.destroy();
				out.println(ie.getMessage());
				v = Verdict.Timeout;
			}
		}
		catch (Exception e) {
			e.printStackTrace(err);
			v = Verdict.Error;
		}
		return v;
	}

	private void logCommand(List<String> command) {
	// StringBuffer sb = new StringBuffer();
	// sb.append("[" + getName() + ":");
	// for (String s : command) {
	// sb.append(" ").append(s);
	// }
	// sb.append("]");
	// System.out.println(sb.toString());
	}

	protected ProcessBuilder getProcessBuilder(List<String> parameters) {
		ProcessBuilder pb = new ProcessBuilder();

		pb.directory(binaryFile.getParentFile());

		pb.environment().clear();
		// pb.environment().put("PATH", binaryFile.getParent());
		customizeProcessBuilder(pb.environment());

		List<String> copy = new ArrayList<String>();
		copy.add(binaryFile.getAbsolutePath());
		copy.addAll(parameters);
		pb.command(copy);

		return pb;
	}

	protected void customizeProcessBuilder(Map<String, String> env) {}

	protected List<String> getCommand(File aslanFile, List<String> parameters) {
		List<String> cmd = new ArrayList<String>();
		if (flagsGoLast()) {
			cmd.add(aslanFile.getAbsolutePath());
		}
		cmd.addAll(defaultParameters);
		cmd.addAll(parameters);
		if (!flagsGoLast()) {
			cmd.add(aslanFile.getAbsolutePath());
		}
		return cmd;
	}

	protected boolean flagsGoLast() {
		return false;
	}

	protected Verdict giveVerdict(String output) {
		int idx = output.indexOf("SUMMARY");
		if (idx >= 0) {
			output = output.substring(idx);
		}
		StringTokenizer tok = new StringTokenizer(output);
		if (tok.hasMoreTokens()) {
			String first = tok.nextToken();
			if (first.equals("SUMMARY") && tok.hasMoreTokens()) {
				String second = tok.nextToken();
				if (second.equals("UNSAFE") || second.equals("ATTACK_FOUND")) {
					return Verdict.Attack;
				}
				else if (second.equals("SAFE") || second.equals("NO_ATTACK_FOUND")) {
					return Verdict.NoAttack;
				}
			}
		}
		return Verdict.Other;
	}

	private String collectStream(InputStream str) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(str));
		String line;
		StringBuffer sb = new StringBuffer();
		while ((line = br.readLine()) != null) {
			sb.append(line).append("\n");
		}
		return sb.toString();
	}

	private static boolean isAlive(Process p) {
		try {
			p.exitValue();
			return false;
		}
		catch (IllegalThreadStateException e) {
			return true;
		}
	}

	protected File getBinaryFile() {
		return binaryFile;
	}

}
