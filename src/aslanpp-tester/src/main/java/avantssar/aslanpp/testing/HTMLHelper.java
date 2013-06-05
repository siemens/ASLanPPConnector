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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import org.apache.commons.io.FilenameUtils;
import org.avantssar.aslanpp.Debug;

public class HTMLHelper {

	public static String removePrefix(File root, File f) {
		String fNorm = FilenameUtils.normalize(f.getAbsolutePath());
		String relativePath = fNorm;
		if (root.isDirectory()) {
			String rootNorm = FilenameUtils.normalizeNoEndSeparator(root.getAbsolutePath()) + File.separator;
			if (relativePath.startsWith(rootNorm)) {
				relativePath = relativePath.substring(rootNorm.length());
			}
		}
		return relativePath;
	}

	public static void fileOrDash(PrintStream out, File root, File aslanPPfile, String prefix) {
		if (aslanPPfile != null) {
			out.print("<a href='");
			out.print(removePrefix(root, aslanPPfile));
			out.print("'>");
			if (prefix != null) {
				out.print(prefix);
			}
			else {
				out.print(FilenameUtils.getBaseName(aslanPPfile.getName()));
			}
			out.print("</a>");
		}
		else {
			if (prefix != null) {
				out.print(prefix);
			}
			else {
				out.print("-");
			}
		}
	}

	public static File toHTML(File textFile, boolean lineNumbers) {
		if (textFile != null) {
			File htmlFile = new File(textFile.getAbsolutePath() + ".html");
			try {
				BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(textFile)));
				PrintWriter writer = new PrintWriter(htmlFile);
				String line;
				writer.println("<html>");
				writer.println("<body>");
				writer.println("<pre>");
				int lineCount = 1;
				while ((line = reader.readLine()) != null) {
					if (lineNumbers) {
						line = String.format("%4d:   %s", lineCount++, line);
					}
					writer.println(line);
				}
				writer.println("</pre>");
				writer.println("</body>");
				writer.println("</html>");
				reader.close();
				writer.close();
				return htmlFile;
			}
			catch (IOException ex) {
				System.out.println("Failed to convert to HTML file '" + textFile.getAbsolutePath() + "': " + ex.getMessage());
				Debug.logger.error(ex);
				return null;
			}
		}
		else {
			return null;
		}
	}
}
