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

package org.avantssar.aslan.xml.client;

import org.avantssar.commons.XMLCommandLineOptions;
import org.kohsuke.args4j.Option;

public class ClientXMLCommandLineOptions extends XMLCommandLineOptions {

	public ClientXMLCommandLineOptions(Class<?> clz) {
		super(clz);
	}

	@Override
	protected String getDefaultJarName() {
		return "aslan-xml-wsclient.jar";
	}

	@Option(name = "-s", aliases = "--host", usage = "Server where the web service is hosted. Defaults to avantssar.ieat.ro.")
	private String host = "avantssar.ieat.ro";

	@Option(name = "-p", aliases = "--port", usage = "Port number where the web service is running. Defaults to 80.")
	private int port = 80;

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

}
