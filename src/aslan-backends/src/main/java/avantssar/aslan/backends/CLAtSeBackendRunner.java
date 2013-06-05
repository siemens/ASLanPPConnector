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

public class CLAtSeBackendRunner extends AbstractBackendRunner {

    public static final String NAME = "CL-AtSe";

    public CLAtSeBackendRunner(String binaryFile) throws BackendRunnerInstantiationException {
        super(binaryFile, "Version ([0-9a-zA-Z\\.\\-]+)");
    }

    public String getName() {
        return NAME;
    }

    public CLAtSeBackendRunner spawn() throws BackendRunnerInstantiationException {
        CLAtSeBackendRunner fresh = new CLAtSeBackendRunner(getBinaryFile().getAbsolutePath());
        for (String parameter : getDefaultParameters()) {
            fresh.addDefaultParameter(parameter);
        }
        fresh.setTimeout(getTimeout());
        return fresh;
    }

}
