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

package org.avantssar.aslan.xml;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.jar.Attributes.Name;

import javax.jws.WebService;

import org.avantssar.aslan.ASLanErrorMessages;
import org.avantssar.aslan.ASLanSpecificationBuilder;
import org.avantssar.aslan.IASLanSpec;
import org.avantssar.aslan.IASLanSpecificationBuilder;
import org.avantssar.aslan.ISymbolsProvider;
import org.avantssar.commons.ErrorGatherer;
import org.avantssar.commons.Utils;

@WebService(endpointInterface = "org.avantssar.aslan.xml.ASLanXMLConverter")
public class ASLanXMLConverterImpl implements ASLanXMLConverter {

	private String title;
	private String version;
	private String vendor;
	private String titleLine;

	private final IASLanSpecificationBuilder builder;
	private final ISymbolsProvider[] extraDefaults;

	public ASLanXMLConverterImpl(ISymbolsProvider... extraDefaults) {
		loadTitleVersionAndVendor();
		builder = ASLanSpecificationBuilder.instance();
		this.extraDefaults = extraDefaults;
	}

	public String getTitle() {
		return title;
	}

	public String getVersion() {
		return version;
	}

	public String getVendor() {
		return vendor;
	}

	public String getFullTitleLine() {
		return titleLine;
	}

	@Override
	public ConverterOutput aslan2xml(String aslan) {
		ErrorGatherer err = new ErrorGatherer(ASLanErrorMessages.DEFAULT);
		IASLanSpec spec = builder.loadFromPlainText(aslan, err, extraDefaults);
		ConverterOutput co = new ConverterOutput();
		if (spec != null) {
			co.setSpecification(spec.toXML());
			transfer(spec.getErrorGatherer(), co);
		} else {
			transfer(err, co);
		}
		return co;
	}

	@Override
	public ConverterOutput xml2aslan(String xml) {
		ErrorGatherer err = new ErrorGatherer(ASLanErrorMessages.DEFAULT);
		IASLanSpec spec = builder.loadFromXML(xml, err, extraDefaults);
		ConverterOutput co = new ConverterOutput();
		if (spec != null) {
			co.setSpecification(spec.toPlainText());
			transfer(spec.getErrorGatherer(), co);
		} else {
			transfer(err, co);
		}
		return co;
	}

	@Override
	public ConverterOutput check(String aslan) {
		ErrorGatherer err = new ErrorGatherer(ASLanErrorMessages.DEFAULT);
		IASLanSpec spec = builder.loadFromPlainText(aslan, err, extraDefaults);
		ConverterOutput co = new ConverterOutput();
		if (spec != null) {
			co.setSpecification(spec.toPlainText());
			transfer(spec.getErrorGatherer(), co);
		} else {
			transfer(err, co);
		}
		return co;
	}

	private void transfer(ErrorGatherer err, ConverterOutput co) {
		co.setErrors(err.getErrors());
		co.setWarnings(err.getWarnings());
	}

	private void loadTitleVersionAndVendor() {
		// Try to load the info directly through the API.
		title = getClass().getPackage().getImplementationTitle();
		version = getClass().getPackage().getImplementationVersion();
		vendor = getClass().getPackage().getImplementationVendor();

		// If failed, try from web manifest.
		if (title == null) {
			try {
				URL u = Main.class.getResource("/"
						+ getClass().getCanonicalName().replace('.', '/')
						+ ".class");
				if (u.getFile().contains("WEB-INF")) {
					int index = u.getFile().indexOf("WEB-INF");
					String path = u.getFile().substring(0, index)
							+ "META-INF/MANIFEST.MF";
					Manifest mf = new Manifest(new FileInputStream(path));
					Attributes attr = mf.getMainAttributes();
					title = attr.getValue(Name.IMPLEMENTATION_TITLE);
					version = attr.getValue(Name.IMPLEMENTATION_VERSION);
					vendor = attr.getValue(Name.IMPLEMENTATION_VENDOR);
				}
			} catch (IOException e) {
				// TODO: report error somehow, take Web services into account
			}
		}

		// If still failed, put in some default values.
		if (title == null) {
			title = "ASLan to XML Converter";
		}
		if (version == null) {
			version = "Unknown";
		}
		if (vendor == null) {
			vendor = "AVANTSSAR";
		}

		// Build the title line.
		titleLine = vendor + " " + title + " " + version;
	}

	public static void main(String[] args) throws Exception {
		ASLanXMLConverterImpl converter = new ASLanXMLConverterImpl();
		InputStream input;
		input = new FileInputStream("/tmp/a.aslan");
		String inputSpec = null;
		inputSpec = Utils.stream2string(input);
		ConverterOutput outputSpec;
		outputSpec = converter.aslan2xml(inputSpec);
		outputSpec = converter.xml2aslan(outputSpec.getSpecification());
		System.out.print(outputSpec.getSpecification());
	}
}
