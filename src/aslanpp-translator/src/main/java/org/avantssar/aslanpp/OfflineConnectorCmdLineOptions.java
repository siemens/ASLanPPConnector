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

package org.avantssar.aslanpp;

import java.io.File;
import org.avantssar.commons.CmdLineTranslatorOptions;
import org.avantssar.commons.TranslatorOptionsException;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.Option;

public class OfflineConnectorCmdLineOptions extends CmdLineTranslatorOptions {

	private boolean readFromStdin;

	private File in;

	@Option(name = "-gv", aliases = "--graphviz", metaVar = "DIR", usage = "Output Graphviz .dot files with the transition rules of each entity. The black edges and dashed nodes correspond to micro-transitions before they are lumped. The files are written to the specified directory. If the 'dot' program is available in the PATH then also output .png files with the transitions rules. Otherwise you can convert the .dot files into images manually. See http://www.graphviz.org/ for details.")
	private File outputGraphvizFiles;

	public boolean isReadFromStdin() {
		return readFromStdin;
	}

	@Option(name = "-", usage = "Read input from stdin instead of from a file.")
	public void setReadFromStdin(boolean readFromStdin) throws CmdLineException {
		if (in != null) {
			throw new CmdLineException(getParser(), "Don't use the - option (for reading from stdin) when you also specify an input file.");
		}
		this.readFromStdin = readFromStdin;
	}

	public File getIn() {
		return in;
	}

	@Argument(metaVar = "INPUT_FILE", usage = "Input file with ASLan++ specification that should be translated to ASLan, or with the ASLan specification for which the analysis result should be translated to ASLan++.")
	public void setIn(File in) throws CmdLineException {
		if (readFromStdin) {
			throw new CmdLineException(getParser(), "Don't specify an input file when using the - option for reading from stdin.");
		}
		this.in = in;
	}

	public OfflineConnectorCmdLineOptions(Class<?> clz) {
		super(clz);
	}

	public File getOutputGraphvizFiles() {
		return outputGraphvizFiles;
	}

	public void setOutputGraphvizFiles(File outputGraphvizFiles) {
		this.outputGraphvizFiles = outputGraphvizFiles;
	}

	public void ckeckAtEnd() throws CmdLineException {
		try {
			super.validate();
		}
		catch (TranslatorOptionsException e) {
			throw new CmdLineException(getParser(), e.getMessage());
		}
		if (isShowHelp() || isShowVersion()) {
			return;
		}
		if (getIn() == null && !isReadFromStdin()) {
			throw new CmdLineException(getParser(), "Specify an input file or use the - option for reading from stdin.");
		}
		if (getIn() != null) {
			if (getIn().isDirectory()) {
				if (getOut() == null) {
					throw new CmdLineException(getParser(), "When translating from an input directory, the output must be specified (and be a directory).");
				}
				if (!getOut().exists() || !getOut().isDirectory()) {
					throw new CmdLineException(getParser(), "When translating from an input directory, the output must be an existing directory.");
				}
			}
		}
		if (getOut() != null) {
			if (getOut().isDirectory()) {
				if (getIn() == null) {
					throw new CmdLineException(getParser(), "When translating into an output directory, the input must be specified (and be a directory).");
				}
				if (!getIn().exists() || !getIn().isDirectory()) {
					throw new CmdLineException(getParser(), "When translating into an output directory, the input must be an existing directory.");
				}
			}
		}
		if (getAnalysisResult() != null && getIn() == null) {
			throw new CmdLineException(getParser(), "When translating an analysis result, input cannot be read from stdin.");
		}
	}

}
