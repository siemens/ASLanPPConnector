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

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.FilenameUtils;
import org.avantssar.aslanpp.model.ASLanPPSpecification;
import org.avantssar.aslanpp.model.EntityManager;
import org.avantssar.aslanpp.model.MetaInfo;
import avantssar.aslan.backends.CLAtSeBackendRunner;
import avantssar.aslan.backends.OFMCBackendRunner;
import avantssar.aslan.backends.SATMCBackendRunner;
import avantssar.aslan.backends.Verdict;

public class DiskTestTask implements ITestTask {

	private final File aslanPP;

	private final Map<String, String> parameters = new HashMap<String, String>();

	public DiskTestTask(File aslanPP) {
		this.aslanPP = aslanPP;

		try {
			EntityManager manager = new EntityManager();
			InputStream is = new FileInputStream(aslanPP);
			ASLanPPSpecification spec = ASLanPPSpecification.fromStream(manager, aslanPP.getAbsolutePath(), is, null);
			if (spec != null) {
				for (MetaInfo mi : spec.getMetaInfo()) {
					String backendName = tag2backend(mi.getTag());
					if (backendName != null) {
						parameters.put(backendName, mi.getValue());
					}
				}
			}
		}
		catch (Exception e) {
			// do nothing
		}
	}

	@Override
	public File getASLanPP() {
		return aslanPP;
	}

	@Override
	public List<String> getBackendParameters(String backendName) {
		if (parameters.containsKey(backendName)) {
			String value = parameters.get(backendName);
			String[] parts = value.split(" ");
			return Arrays.asList(parts);
		}
		else {
			return null;
		}
	}

	@Override
	public File getExpectedASLan() {
		return null;
	}

	@Override
	public Verdict getExpectedVerdict() {
		if (FilenameUtils.getBaseName(aslanPP.getAbsolutePath()).endsWith("_Safe")) {
			return Verdict.NoAttack;
		}
		else {
			return Verdict.Attack;
		}
	}

	private String tag2backend(String tag) {
		if (MetaInfo.SATMC.equals(tag)) {
			return SATMCBackendRunner.NAME;
		}
		else if (MetaInfo.OFMC.equals(tag)) {
			return OFMCBackendRunner.NAME;
		}
		else if (MetaInfo.CLATSE.equals(tag)) {
			return CLAtSeBackendRunner.NAME;
		}
		else {
			return null;
		}
	}
}
