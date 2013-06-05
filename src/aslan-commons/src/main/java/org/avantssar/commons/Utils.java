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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.StringTokenizer;

public class Utils {

	public static enum InputType {
		Specification, Module, Unknown
	};

	public static InputType getInputType(InputStream is) throws IOException {
		if (is != null) {
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(is));
			String line;
			InputType result = InputType.Unknown;
			while ((line = reader.readLine()) != null) {
				// we stop on first non-comment line
				if (line.trim().length() > 0 && !line.trim().startsWith("%")) {
					if (line.toLowerCase().trim().startsWith("specification")) {
						result = InputType.Specification;
					} else if (line.toLowerCase().trim().startsWith("entity")) {
						result = InputType.Module;
					}
					break;
				}
			}
			return result;
		} else {
			return InputType.Unknown;
		}
	}

	public static ChannelModel getChannelModel(InputStream is)
			throws IOException {
		if (is != null) {
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(is));
			String line;
			StringBuffer noComments = new StringBuffer();
			while ((line = reader.readLine()) != null) {
				if (line.trim().length() > 0 && !line.trim().startsWith("%")) {
					noComments.append(line).append("\n");
				}
			}
			StringTokenizer st = new StringTokenizer(noComments.toString());
			if (st.hasMoreTokens()) {
				String specKW = st.nextToken();
				if (specKW.equals("specification")) {
					if (st.hasMoreTokens()) {
//						String specName = st.nextToken();
						if (st.hasMoreTokens()) {
							String cmKW = st.nextToken();
							if (cmKW.equals("channel_model")) {
								if (st.hasMoreTokens()) {
									String cmName = st.nextToken();
									try {
										return ChannelModel.valueOf(cmName);
									} catch (Exception e) {
										return null;
									}
								}
							}
						}
					}
				}
			}
			return null;
		} else {
			return null;
		}
	}

	public static String stream2string(InputStream is) throws IOException {
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
		} else {
			return null;
		}
	}

}
