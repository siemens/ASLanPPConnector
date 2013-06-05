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
import java.net.URL;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

public class CommandLineOptions {

	private static String JAR_FILE = "aslan-backends.jar";

	private static CmdLineParser parser;

	public CommandLineOptions() {
		parser = new CmdLineParser(this);
		loadJarName();
	}

	public CmdLineParser getParser() {
		return parser;
	}

	@Option(name = "-h", aliases = "--help", usage = "Show help.")
	private boolean showHelp;

	@Argument(metaVar = "ASLAN_INPUT_FILE", usage = "Input file with ASLan specification that should be sent to the backends.")
	private File in;

	public boolean isShowHelp() {
		return showHelp;
	}

	public void setShowHelp(boolean showHelp) {
		this.showHelp = showHelp;
	}

	public File getIn() {
		return in;
	}

	public void setIn(File in) {
		this.in = in;
	}

	public void ckeckAtEnd() throws CmdLineException {
		if (!isShowHelp()) {
			if (in == null) {
				throw new CmdLineException(parser, "Please specify an input file that should be sent to the backends.");
			}
			if (!in.exists() || !in.isFile()) {
				throw new CmdLineException(parser, "Cannot find input file: " + in.getAbsolutePath() + ".");
			}
		}
	}

	public void showShortHelp(PrintStream out) {
		showSingleLineUsage(out);
		out.println("Run the program with the -h or --help option for details about available options.");
	}

	public void showLongHelp(PrintStream out) {
		showSingleLineUsage(out);
		out.println("Available options:");
		out.println();
		parser.printUsage(out);
		out.println();
		out.println();
	}

	public void showSingleLineUsage(PrintStream out) {
		out.print("Usage: java -jar ");
		out.print(JAR_FILE);
		parser.printSingleLineUsage(out);
		out.println();
		out.println();
	}

	private void loadJarName() {
		String pathToOurSpecificResource = Backends.class.getCanonicalName();
		pathToOurSpecificResource = "/" + pathToOurSpecificResource.replace('.', '/') + ".class";
		URL u = Backends.class.getResource(pathToOurSpecificResource);
		if (u.getFile().indexOf("!") > -1) {
			String[] parts = u.getFile().split("!");
			File f = new File(parts[0].substring(parts[0].indexOf(":") + 1));
			JAR_FILE = f.getName();
		}
	}

}
