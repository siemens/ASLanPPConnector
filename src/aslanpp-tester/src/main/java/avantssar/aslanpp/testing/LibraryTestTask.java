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
import java.io.IOException;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.avantssar.aslan.IASLanSpec;
import org.avantssar.aslanpp.model.ASLanPPSpecification;
import org.avantssar.commons.TranslatorOptions;
import avantssar.aslan.backends.Verdict;

public class LibraryTestTask implements ITestTask {

	private final ISpecProvider specProvider;
	private final File aslanPP;
	private final File aslan;

	public LibraryTestTask(ISpecProvider specProvider, File modelsDir) throws IOException {
		this.specProvider = specProvider;
		ASLanPPSpecification spec = specProvider.getASLanPPSpecification();
		StringBuffer sb = new StringBuffer();
		sb.append(spec.getSpecificationName());
		if (specProvider instanceof IChannelModelFlexibleSpecProvider) {
			IChannelModelFlexibleSpecProvider cmFlex = (IChannelModelFlexibleSpecProvider) specProvider;
			sb.append("_");
			sb.append(cmFlex.getChannelModel().toString());
			if (specProvider instanceof IChannelTypeFlexibleSpecProvider) {
				IChannelTypeFlexibleSpecProvider ctFlex = (IChannelTypeFlexibleSpecProvider) specProvider;
				sb.append("_");
				sb.append(ctFlex.getChannelType().type.toString());
			}
		}
        String baseFileName = sb.toString();
		sb.append(".aslan++");
        spec.setSpecificationName(baseFileName);
		aslanPP = new File(FilenameUtils.concat(modelsDir.getAbsolutePath(), sb.toString()));
		aslan = new File(FilenameUtils.removeExtension(aslanPP.getAbsolutePath()) + ".aslan");
		FileUtils.writeStringToFile(aslanPP, spec.toString());
		TranslatorOptions options = new TranslatorOptions();
		IASLanSpec aslanSpec = specProvider.getExpectedASLanTranslation(options);
		aslanSpec.getHornClauses();
		if (aslanSpec != null) {
			FileUtils.writeStringToFile(aslan, aslanSpec.getStrippedRepresentation());
		}
	}

	@Override
	public File getASLanPP() {
		return aslanPP;
	}

	@Override
	public List<String> getBackendParameters(String backendName) {
		List<String> pars = specProvider.getBackendParameters(backendName);
		return pars;
	}

	@Override
	public File getExpectedASLan() {
		return aslan;
	}

	@Override
	public Verdict getExpectedVerdict() {
		return specProvider.getExpectedVerdict();
	}

}
