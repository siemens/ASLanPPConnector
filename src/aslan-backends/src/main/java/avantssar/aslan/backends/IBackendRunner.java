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
import java.io.PrintStream;
import java.util.List;

public interface IBackendRunner {

	static int DEFAULT_TIMEOUT_IN_SECCONDS = 15;

	String getName();

	String getVersion();

	String getTitleLine();

	String getFullDescription();

	File getExecutable();

	void addDefaultParameter(String parameter);

	// void addParameter(String parameter);

	// List<String> getParameters();

	// void purgeParameters();

	void setTimeout(long timeout);

	long getTimeout();

	Verdict analyze(File aslanFile, List<String> parameters, PrintStream out, PrintStream err);

	IBackendRunner spawn() throws BackendRunnerInstantiationException;

}
