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

package org.avantssar.aslan.of;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import org.antlr.runtime.ANTLRInputStream;
import org.antlr.runtime.CommonTokenStream;
import org.avantssar.aslan.ASLanErrorMessages;
import org.avantssar.aslan.ASLanSpecificationBuilder;
import org.avantssar.aslan.IASLanSpec;
import org.avantssar.commons.ErrorGatherer;

public class AnalysisResult {

	public static AnalysisResult parse(String plainTextSpec)
	{
		return fromPlainText(plainTextSpec, new ErrorGatherer(ASLanErrorMessages.DEFAULT));
	}

	public static AnalysisResult fromPlainText(String plainTextSpec, ErrorGatherer err) {
		StringBuffer sb = new StringBuffer(plainTextSpec);
		if (!plainTextSpec.endsWith("\n")) {
			sb.append("\n");
		}
		// manually extract the COMMENTS section
		// because it cannot be properly parsed by the ANTLR parser,
		// due to rounded brackets and commas
		int commentsIndex = sb.indexOf(ofParser.tokenNames[ofParser.COMMENTS]);
		if (commentsIndex >= 0) {
			int commentsSecondIndex = sb.indexOf(ofParser.tokenNames[ofParser.COMMENTS], commentsIndex + 1);
			if (commentsSecondIndex >= 0) {
				err.addError(OutputFormatErrorMessages.DUPLICATE_COMMENTS_SECTION);
      //return null;
			}
			else {
				int statsIndex = sb.indexOf(ofParser.tokenNames[ofParser.STATISTICS], commentsIndex + 1);
				if (statsIndex >= 0) {
					String comments = sb.subSequence(commentsIndex, statsIndex).toString();
					// replace dangerous characters
					comments = comments.replaceAll("\\(", "<");
					comments = comments.replaceAll("\\)", ">");
					comments = comments.replaceAll("\\[", "<");
					comments = comments.replaceAll("\\]", ">");
					comments = comments.replaceAll("\\{", "<");
					comments = comments.replaceAll("\\}", ">");
					comments = comments.replaceAll(",", ";");
					sb.delete(commentsIndex, statsIndex);
					sb.insert(commentsIndex, comments);
				}
				else {
					err.addError(OutputFormatErrorMessages.STATISTICS_SECTION_MISSING);
        //return null;
				}
			}
		}
		ByteArrayInputStream bais = new ByteArrayInputStream(sb.toString().getBytes());
		try {
			ANTLRInputStream antlrStream = new ANTLRInputStream(bais);
			ofLexer lexer = new ofLexer(antlrStream);
			lexer.setErrorGatherer(err);
			CommonTokenStream tokens = new CommonTokenStream(lexer);
			ofParser parser = new ofParser(tokens);
			parser.setErrorGatherer(err);
			return parser.aslanOF(null);
		}
		catch (Exception e) {
			e.printStackTrace(System.err);
			return null;
		}
	}

	public static void main(String[] args) throws Exception {
		if (args.length != 2) {
			System.out.println("Two parameters expected:");
			System.out.println("1. the file that contains the output from the backend");
			System.out.println("2. the file that contains the ASLan specification");
		}
		else {
			FileInputStream fis = new FileInputStream(args[0]);
			FileInputStream faslan = new FileInputStream(args[1]);
			String content = stream2string(fis);
			String aslanString = stream2string(faslan);
			ErrorGatherer aslanErr = new ErrorGatherer(ASLanErrorMessages.DEFAULT);
			IASLanSpec aslanSpec = ASLanSpecificationBuilder.instance().loadFromPlainText(aslanString, aslanErr);
			ErrorGatherer err = new ErrorGatherer(OutputFormatErrorMessages.DEFAULT);
			AnalysisResult ar = AnalysisResult.fromPlainText(content, err);
			ExecutionScenario exec = new ExecutionScenario(aslanSpec, err);
			ExecutionScenarioBuilder pp = new ExecutionScenarioBuilder(aslanSpec, exec, err);
			ar.accept(pp);
			err.report(System.err);
			/*(void)*/exec.execute();
		}
	}

	protected static String stream2string(InputStream is) throws IOException {
		if (is != null) {
			Writer writer = new StringWriter();
			char[] buffer = new char[1024];
			Reader reader = new BufferedReader(new InputStreamReader(is));
			int n;
			while ((n = reader.read(buffer)) != -1) {
				writer.write(buffer, 0, n);
			}
			is.close();
			return writer.toString();
		}
		else {
			return null;
		}
	}

	private final String filename;
	private String backendName;
	private String backendVersion;
	private String backendDate;
	private Goals goals;
	private boolean inconclusive;
	private Trace trace;
	private final List<String> details = new ArrayList<String>();
	private final List<String> comments = new ArrayList<String>();
	private final List<Statistics> stats = new ArrayList<Statistics>();
	private final List<String> unused = new ArrayList<String>();
	private final List<IGroundTerm> explicit = new ArrayList<IGroundTerm>();
	private final List<IGroundTerm> implicit = new ArrayList<IGroundTerm>();
	private final List<IGroundTerm> finalState = new ArrayList<IGroundTerm>();

	protected AnalysisResult(String filename) {
		this.filename = filename;
	}

	public void accept(IAnalysisResultVisitor visitor) {
		visitor.visit(this);
	}

	public String getBackendName() {
		return backendName;
	}

	public void setBackendName(String backendName) {
		this.backendName = backendName;
	}

	public String getBackendVersion() {
		return backendVersion;
	}

	public void setBackendVersion(String backendVersion) {
		this.backendVersion = backendVersion;
	}

	public String getBackendDate() {
		return backendDate;
	}

	public void setBackendDate(String backendDate) {
		this.backendDate = backendDate;
	}

	public void addStatistics(String label, String value, String unit) {
		this.stats.add(new Statistics(label, value, unit));
	}

	public void addUnused(String name) {
		this.unused.add(name);
	}

	public void addDetail(String s) {
		this.details.add(s);
	}

	public void addComment(String comment) {
		this.comments.add(comment);
	}

	public void addExplicit(IGroundTerm t) {
		this.explicit.add(t);
	}

	public void addImplicit(IGroundTerm t) {
		this.implicit.add(t);
	}

	public void addFinal(IGroundTerm t) {
		this.finalState.add(t);
	}

	public Goals getAttack() {
		return goals;
	}

	public void setAttack(Goals goals) {
		this.goals = goals;
	}

	public void setInconclusive(boolean inconclusive) {
		this.inconclusive = inconclusive;
	}

	public boolean isInconclusive() {
		return inconclusive;
	}

	public Trace getTrace() {
		return trace;
	}

	public void setTrace(Trace trace) {
		this.trace = trace;
	}

	public String getFilename() {
		return filename;
	}

	public void printHeader(ExecutionScenario exec, StringBuffer sb, ErrorGatherer err) {
		sb.append("INPUT:").append("\n\t").append(filename).append("\n");
		sb.append("\n");
		sb.append("SUMMARY:").append("\n").append("\t");
		if (getAttack() != null) {
			sb.append("ATTACK_FOUND\n\n");
			exec.printViolated(sb); // also here in the header for the reader's convenience
		}
		else if (isInconclusive()) {
			sb.append("INCONCLUSIVE\n");
		}
		else {
			sb.append("NO_ATTACK_FOUND\n");
		}
		if (details.size() > 0) {
			sb.append("\n");
			sb.append("DETAILS:").append("\n");
			for (String s : details) {
				sb.append("\t");
				sb.append(s).append("\n");
			}
		}
		else {
			err.addError(OutputFormatErrorMessages.DETAILS_SECTION_MISSING);
    //return null;
		}
		if (backendName != null) {
			sb.append("\n");
			sb.append("BACKEND:").append("\n");
			sb.append("\t").append(backendName).append(" ").append(backendVersion);
			if (backendDate != null) {
				sb.append("(").append(backendDate).append(")");
			}
			sb.append("\n");
		}
		if (comments.size() > 0) {
			sb.append("\n");
			sb.append("COMMENTS:");
			boolean first = true;
			String prev = null;
			for (String s : comments) {
				if (first || (prev != null && prev.endsWith(".")) || (s.equals(s.toUpperCase()) && Character.isLetter(s.charAt(0)))) {
					sb.append("\n").append("\t");
				}
				sb.append(s).append(" ");
				first = false;
				prev = s;
			}
			sb.append("\n");
		}
		if (stats.size() > 0) {
			sb.append("\n");
			sb.append("STATISTICS:").append("\n");
			for (Statistics ss : stats) {
				sb.append("\t").append(ss.getLabel()).append(" ").append(ss.getValue()).append(" ").append(ss.getUnit()).append("\n");
			}
		}
	}

	public void printFooter(ISetProvider setProvider, IASLanSpec aslanSpec, StringBuffer sb) {
		if (unused.size() > 0) {
			sb.append("\n");
			sb.append("UNUSED:").append("\n");
			for (String s : unused) {
				sb.append("\t").append(s).append("\n");
			}
		}
		if (explicit.size() + implicit.size() > 0) {
			if (explicit.size() > 0) {
				sb.append("\n");
				sb.append("EXPLICIT:").append("\n");
				for (IGroundTerm t : explicit) {
					sb.append("\t");
					sb.append(t.getRepresentationNice(setProvider, aslanSpec));
					sb.append("\n");
				}
			}
			if (implicit.size() > 0) {
				sb.append("\n");
				sb.append("IMPLICIT:").append("\n");
				for (IGroundTerm t : implicit) {
					sb.append("\t");
					sb.append(t.getRepresentationNice(setProvider, aslanSpec));
					sb.append("\n");
				}
			}
		}
		else {
			if (finalState.size() > 0) {
				sb.append("\n");
				sb.append("CLOSED_FINAL_STATE:").append("\n");
				for (IGroundTerm t : finalState) {
					sb.append("\t");
					sb.append(t.getRepresentationNice(setProvider, aslanSpec));
					sb.append("\n");
				}
			}
		}
	}

}
