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

import java.util.Map;

public class OFMCBackendRunner extends AbstractBackendRunner {

	public static final String NAME = "OFMC";

	public OFMCBackendRunner(String binaryFile) throws BackendRunnerInstantiationException {
		super(binaryFile, "Version ([0-9a-zA-Z\\.\\-]+)");
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	protected void customizeProcessBuilder(Map<String, String> env) {
		env.put("OFMC_PATH", getExecutable().getParent());
	}

	@Override
	public OFMCBackendRunner spawn() throws BackendRunnerInstantiationException {
		OFMCBackendRunner fresh = new OFMCBackendRunner(getBinaryFile().getAbsolutePath());
        for (String parameter : getDefaultParameters()) {
            fresh.addDefaultParameter(parameter);
        }
		fresh.setTimeout(getTimeout());
		return fresh;
	}

}
