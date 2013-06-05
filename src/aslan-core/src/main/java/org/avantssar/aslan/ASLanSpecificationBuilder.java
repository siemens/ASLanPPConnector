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

package org.avantssar.aslan;

import java.io.ByteArrayInputStream;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.antlr.runtime.ANTLRInputStream;
import org.antlr.runtime.CommonTokenStream;
import org.avantssar.commons.Error;
import org.avantssar.commons.ErrorGatherer;
import org.w3c.dom.Document;

public class ASLanSpecificationBuilder implements IASLanSpecificationBuilder {

	private static ASLanSpecificationBuilder theInstance = null;

	public static ASLanSpecificationBuilder instance() {
		if (theInstance == null) {
			theInstance = new ASLanSpecificationBuilder();
		}
		return theInstance;
	}

	private ASLanSpecificationBuilder() {
	// private for singleton
	}

	@Override
	public IASLanSpec createASLanSpecification(ISymbolsProvider... extraDefaults) {
		return new ASLanSpecification(new ErrorGatherer(ASLanErrorMessages.DEFAULT), extraDefaults);
	}

	protected IASLanSpec createASLanSpecification(ErrorGatherer err, ISymbolsProvider... extraDefaults) {
		return new ASLanSpecification(err, extraDefaults);
	}

	/**
	 * Parse an ASLan model
	 * 
	 * @param errors
	 *            list where any parsing errors will be added to
	 * @param spec
	 *            the whole ASLan model to be parsed
	 * @return an IASLanSpec instance representing the model given.
	 */
	public static IASLanSpec parse(List<Error> errors, String spec)
	{
		ErrorGatherer err = new ErrorGatherer(ASLanErrorMessages.DEFAULT);
		IASLanSpec result = ASLanSpecificationBuilder.instance()
				.loadFromPlainText(spec, err, new SATMCSymbolsProvider());
		errors.addAll(err);
		return result;
	}

	@Override
	public IASLanSpec loadFromPlainText(String plainTextSpec, ErrorGatherer err, ISymbolsProvider... extraDefaults) {
		ByteArrayInputStream bais = new ByteArrayInputStream(plainTextSpec.getBytes());
		try {
			ANTLRInputStream antlrStream = new ANTLRInputStream(bais);
			aslanLexer lexer = new aslanLexer(antlrStream);
			lexer.setErrorGatherer(err);
			CommonTokenStream tokens = new CommonTokenStream(lexer);
			aslanParser parser = new aslanParser(tokens);
			parser.setErrorGatherer(err);
			parser.setExtraSymbolsProviders(extraDefaults);
			IASLanSpec spec = parser.aslanSpecification();
			spec.finish();
			return spec;
		}
		catch (Exception e) {
			err.addException(ASLanErrorMessages.GENERIC_ERROR, e.getMessage());
			return null;
		}
	}

	public IASLanSpec loadFromXML(String xmlSpec, ErrorGatherer err, ISymbolsProvider... extraDefaults) {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder loader = factory.newDocumentBuilder();

			ByteArrayInputStream bais = new ByteArrayInputStream(xmlSpec.getBytes());
			Document document = loader.parse(bais);
			IASLanSpec spec = ASLanSpecificationBuilder.instance().createASLanSpecification(extraDefaults);
			ASLanXMLLoader parser = new ASLanXMLLoader(err);
			parser.loadFromXML(spec, document);
			spec.finish();
			return spec;
		}
		catch (Exception e) {
			err.addException(ASLanErrorMessages.GENERIC_ERROR, e.getMessage());
			return null;
		}
	}

	protected static JAXBContext prepareContext() throws JAXBException {
		return JAXBContext.newInstance(ASLanSpecification.class, SetType.class, PairType.class, CompoundType.class, FunctionTerm.class, VariableTerm.class, ConstantTerm.class, NumericTerm.class,
				NegatedTerm.class, FunctionConstantTerm.class);
	}
}
