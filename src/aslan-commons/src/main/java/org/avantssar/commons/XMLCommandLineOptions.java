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

package org.avantssar.commons;

import java.io.File;
import java.io.PrintStream;
import java.net.URL;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

public class XMLCommandLineOptions {

	private final String jarFile;
	private final CmdLineParser parser;

	public XMLCommandLineOptions(Class<?> clz) {
		parser = new CmdLineParser(this);
		jarFile = loadJarName(clz, getDefaultJarName());
	}

	public CmdLineParser getParser() {
		return parser;
	}

	protected String getDefaultJarName() {
		return "aslan-xml.jar";
	}

	@Option(name = "-h", aliases = "--help", usage = "Show help.")
	private boolean showHelp;

	@Option(name = "-v", aliases = "--version", usage = "Display version information.")
	private boolean showVersion;

	@Option(name = "-x", aliases = "--from-xml", usage = "Convert from XML into plain text. By default the conversion is from plain text to XML.")
	private boolean fromXML;

	@Option(name = "-c", aliases = "--check", usage = "Perform only verification of the input. No conversion is done. Syntax is checked and some type checks are also done. Can be used only on plaintext specifications.")
	private boolean check;

	@Option(name = "--acm", usage = "Add to the prelude specific functions related to the ACM channel model.")
	private boolean satmc;

	@Option(name = "-o", aliases = "--output", metaVar = "OUTPUT_FILE", usage = "Send output to the specified file instead of stdout.")
	private File out;

	private boolean readFromStdin;

	private File in;

	public boolean isShowHelp() {
		return showHelp;
	}

	public void setShowHelp(boolean showHelp) {
		this.showHelp = showHelp;
	}

	public File getOut() {
		return out;
	}

	public void setOut(File out) {
		this.out = out;
	}

	public boolean isReadFromStdin() {
		return readFromStdin;
	}

	@Option(name = "-", usage = "Read input from stdin instead of from a file.")
	public void setReadFromStdin(boolean readFromStdin) throws CmdLineException {
		if (in != null) {
			throw new CmdLineException(
					parser,
					"Don't use the - option (for reading from stdin) when you also specify an input file.");
		}
		this.readFromStdin = readFromStdin;
	}

	public File getIn() {
		return in;
	}

	@Argument(metaVar = "ASLAN_INPUT_FILE", usage = "Input file with the ASLan specification that should be converted or verified.")
	public void setIn(File in) throws CmdLineException {
		if (readFromStdin) {
			throw new CmdLineException(parser,
					"Don't specify an input file when using the - option for reading from stdin.");
		}
		this.in = in;
	}

	public boolean isShowVersion() {
		return showVersion;
	}

	public void setShowVersion(boolean showVersion) {
		this.showVersion = showVersion;
	}

	public boolean isFromXML() {
		return fromXML;
	}

	public void setFromXML(boolean fromXML) {
		this.fromXML = fromXML;
	}

	public boolean isCheck() {
		return check;
	}

	public void setCheck(boolean check) {
		this.check = check;
	}

	public boolean isSatmc() {
		return satmc;
	}

	public void setSatmc(boolean satmc) {
		this.satmc = satmc;
	}

	public void ckeckAtEnd() throws CmdLineException {
		if (showHelp || showVersion) {
			return;
		}
		if (in == null && !isReadFromStdin()) {
			throw new CmdLineException(parser,
					"Specify an input file or use the - option for reading from stdin.");
		}
		if (in != null) {
			if (!in.isFile() || !in.exists()) {
				throw new CmdLineException(parser, "Cannot find input file.");
			}
		}
		if (isCheck() && isFromXML()) {
			throw new CmdLineException(parser,
					"Check can be done on plaintext specifications only.");
		}
	}

	public void showShortHelp(PrintStream out) {
		showSingleLineUsage(out);
		out
				.println("Run the program with the -h or --help option for details about available options.");
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
		out.print(jarFile);
		parser.printSingleLineUsage(out);
		out.println();
		out.println();
	}

	private String loadJarName(Class<?> clz, String defaultJarName) {
		String pathToOurSpecificResource = clz.getCanonicalName();
		pathToOurSpecificResource = "/"
				+ pathToOurSpecificResource.replace('.', '/') + ".class";
		URL u = clz.getResource(pathToOurSpecificResource);
		if (u.getFile().indexOf("!") > -1) {
			String[] parts = u.getFile().split("!");
			File f = new File(parts[0].substring(parts[0].indexOf(":") + 1));
			return f.getName();
		}
		return defaultJarName;
	}

}
