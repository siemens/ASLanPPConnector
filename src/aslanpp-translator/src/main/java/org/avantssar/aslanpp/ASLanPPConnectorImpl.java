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

package org.avantssar.aslanpp;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.jar.Attributes.Name;
import javax.jws.WebService;

import org.antlr.runtime.RecognitionException;
import org.avantssar.aslan.ASLanErrorMessages;
import org.avantssar.aslan.ASLanSpecificationBuilder;
import org.avantssar.aslan.IASLanSpec;
import org.avantssar.aslan.of.AnalysisResult;
import org.avantssar.aslan.of.ExecutionScenario;
import org.avantssar.aslan.of.ExecutionScenarioBuilder;
import org.avantssar.aslan.of.OutputFormatErrorMessages;
import org.avantssar.aslanpp.ITranslatorHook; // TODO not really needed
import org.avantssar.aslanpp.flow.ASLanBuilder;
import org.avantssar.aslanpp.flow.Orchestrator;
import org.avantssar.aslanpp.model.ASLanPPSpecification;
import org.avantssar.aslanpp.model.Entity;
import org.avantssar.aslanpp.model.EntityManager;
import org.avantssar.aslanpp.model.ErrorMessages;
import org.avantssar.aslanpp.visitors.PrettyPrinter;
import org.avantssar.commons.ASLanPPException;
import org.avantssar.commons.ChannelModel;
import org.avantssar.commons.Error;
import org.avantssar.commons.ErrorGatherer;
import org.avantssar.commons.TranslatorOptions;
import org.avantssar.commons.TranslatorOptionsException;
import org.avantssar.commons.Utils;
import org.avantssar.commons.TranslatorOptions.HornClausesLevel;
import org.avantssar.commons.Utils.InputType;

@WebService(endpointInterface = "org.avantssar.aslanpp.ASLanPPConnector")
public class ASLanPPConnectorImpl implements ASLanPPConnector {

	private String title;
	private String version;
	private String vendor;
	private String titleLine;

	private final List<ITranslatorHook> hooks = new ArrayList<ITranslatorHook>(); // TODO not really needed

	public ASLanPPConnectorImpl() {
		loadTitleVersionAndVendor();
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

	public void addHook(ITranslatorHook hook) { // TODO not really needed
		hooks.add(hook);
	}

	@Override
	public TranslatorOutput translate(TranslatorOptions options, String fileName, String... aslanppSpecs) 
			throws NullPointerException, IOException, RecognitionException {
		return translateExt(false, null, options, fileName, aslanppSpecs).toTranslatorOutput();
	}

	public TranslatorOutputExt translateExt(boolean main, File graphvizDir,
			TranslatorOptions options, String fileName, String... aslanppSpecs) 
			throws NullPointerException, IOException, RecognitionException {
		// Handle Graphviz, if needed.
		if (graphvizDir != null) {
			try {
				GraphvizGenerator gg = new GraphvizGenerator(graphvizDir);
				addHook(gg);
			}
			catch (IOException e) {
				Main.reportError("Failed to initialize Graphviz generator with output directory '" + graphvizDir.getAbsolutePath()
						+ "'. No Graphiviz files will be generated.", e);
			}
		}
		TranslatorOutputExt result = new TranslatorOutputExt();
		ErrorGatherer err = new ErrorGatherer(ErrorMessages.DEFAULT);
		try {
			EntityManager manager = new EntityManager();
			try {
				manager.loadASLanPath();
			}
			catch (IOException e) {
				Debug.logger.error("IO Exception while loading ASLANPATH.", e);
			}

			InputStream specIS = null;
			List<InputStream> modulesIS = new ArrayList<InputStream>();

			for (String s : aslanppSpecs) {
				InputStream is = new ByteArrayInputStream(s.getBytes());
				InputType it = Utils.getInputType(is);
				is.reset();
				if (it == InputType.Specification) {
					if (specIS == null) {
						specIS = is;
					}
					// if there are multiple specification, just report error
					// and return
					else {
						err.addException(ErrorMessages.MULTIPLE_SPECIFICATIONS_TO_TRANSLATE/*, [])aslanppSpecs*/);
					}
				}
				else if (it == InputType.Module) {
					modulesIS.add(is);
				}
				// invalid input is rejected
				else {
					err.addException(ErrorMessages.INVALID_INPUT_TO_TRANSLATE);
				}
			}
			if (specIS == null) {
				err.addException(ErrorMessages.NO_SPECIFICATION_TO_TRANSLATE);
			}
			else {
				if (modulesIS.size() > 0) {
					ChannelModel cm = Utils.getChannelModel(specIS);
					specIS.reset();
					if (cm != null) {
						for (InputStream is : modulesIS) {
							Entity.fromStream(manager, is, cm, err);
						}
						result.addWarnErrors(err);
					}
					else {
						err.addError(ErrorMessages.INVALID_CHANNEL_MODEL);
					}
				}
			}

			// Check if the options are consistent.
			options.validate();

			// Load the spec from the file and parse (and check) it.
			ASLanPPSpecification spec = ASLanPPSpecification.fromStream(manager, fileName, specIS, err);
			result.setASLanPPSpecification(spec);

			if (spec != null) {
				err = spec.getErrorGatherer();

				// If only pretty printing, stop here.
				if (options.isPrettyPrint()) {
					PrettyPrinter pp = new PrettyPrinter();
					spec.accept(pp);
					result.setResult(pp.toString());
				}
				else {
					spec.finalize(options.getHornClausesLevel() != HornClausesLevel.ALL);
					
					// If preprocessing only, then stop here.
					if (options.isPreprocess() && main) {
						PrettyPrinter pp = new PrettyPrinter(true);
						spec.accept(pp);
						result.setResult(pp.toString());
					}
					else if (!options.isPreprocess()) {
						// If orchestrating, do it now.
						if (options.getOrchestrationClient() != null) {
							Orchestrator orch = new Orchestrator(options.getOrchestrationClient());
							spec.accept(orch);
						}
						
						// Build the ASLan model.
						ASLanBuilder builder = new ASLanBuilder(options, getTitle(), getVersion());
						try {
						  spec.accept(builder);
						}
						catch (ASLanPPException ae) {
							err.addAll(ae.getErrorGatherer());
							throw ae;
						}
						finally {
							err.addAll(builder.getASLanSpecification().getErrorGatherer()); //copy the errors from validation phase
						}
						IASLanSpec aslanSpec = builder.getASLanSpecification();
						
						// Notify registered hooks.
						for (ITranslatorHook h : hooks) {
							h.translationOver(spec, aslanSpec, builder);
						}
						
						// Grab the representation of the ASLan model,
						// stripped if needed.
						org.avantssar.aslan.PrettyPrinter pp = new org.avantssar.aslan.PrettyPrinter(options.isStripOutput());
						aslanSpec.accept(pp);
						result.setResult(pp.toString());
					}
				}
			}
		}
		catch (TranslatorOptionsException toe) {
			err.addError("Conflicting translator options: " + toe.getMessage());
			Debug.logger.fatal("Translator options exception.", toe);
		}
		catch (ASLanPPException ae) {
			//err.addAll(ae.getErrorGatherer()); //copy the errors from parsing phase into the new instance.
			Debug.logger.fatal(ae.getMessage());
		}
/*		catch (Throwable e) {
			result.getErrors().add("Transformation failed: " + e.toString());
			Debug.logger.error("Transformation failed.", e);
		}*/
		result.addWarnErrors(err);
		if (result.getErrors().size() > 0) {
			result.setResult(null);
		}
        Debug.logger.info("Translation reported in total " + result.getErrors  ().size() + " errors.");
        Debug.logger.info("Translation reported in total " + result.getWarnings().size() + " warnings.");
        return result;
	}

	@Override
	public TranslatorOutput translateAnalysisResult(String aslanSpec, String analysisResult) {
		TranslatorOutput result = new TranslatorOutput();
		ErrorGatherer err = new ErrorGatherer(OutputFormatErrorMessages.DEFAULT);
		try {
			ErrorGatherer aslanErr = new ErrorGatherer(ASLanErrorMessages.DEFAULT);
			IASLanSpec aslanModel = ASLanSpecificationBuilder.instance().loadFromPlainText(aslanSpec, aslanErr);
			AnalysisResult ar = AnalysisResult.fromPlainText(analysisResult, err);
			if (ar != null) {
				ExecutionScenario exec = new ExecutionScenario(aslanModel, err);
				ExecutionScenarioBuilder pp = new ExecutionScenarioBuilder(aslanModel, exec, err);
				ar.accept(pp);
				StringBuffer trResult = new StringBuffer();
				ar.printHeader(exec, trResult, err);
				trResult.append(exec.execute());
				ar.printFooter(exec, aslanModel, trResult);
				trResult.append(exec.listCommunication());
				result.setSpecification(trResult.toString());
			}
		}
		catch (ASLanPPException ae) {
//			err.addError(ae.getError().toString());
//			err.addAll(ae.getErrorGatherer());
			Debug.logger.error("ASLanPP exception.", ae);
		}
/*		catch (Throwable e) {
			err.addError("Transformation failed: " + e.getMessage());
			Debug.logger.error("Transformation failed.", e);
		}*/
		for (Error e : err) {
			if (e.getSeverity() == Error.Severity.WARNING) {
				result.addWarning(e.toString());
			}		
			else {
				result.addError(e.toString());
			}
		}
		return result;
	}

	private void loadTitleVersionAndVendor() {
		// Try to load the info directly through the API.
		title = getClass().getPackage().getImplementationTitle();
		version = getClass().getPackage().getImplementationVersion();
		vendor = getClass().getPackage().getImplementationVendor();

		// If failed, try from web manifest.
		if (title == null) {
			try {
				URL u = Main.class.getResource("/" + getClass().getCanonicalName().replace('.', '/') + ".class");
				if (u.getFile().contains("WEB-INF")) {
					int index = u.getFile().indexOf("WEB-INF");
					String path = u.getFile().substring(0, index) + "META-INF/MANIFEST.MF";
					Manifest mf = new Manifest(new FileInputStream(path));
					Attributes attr = mf.getMainAttributes();
					title = attr.getValue(Name.IMPLEMENTATION_TITLE);
					version = attr.getValue(Name.IMPLEMENTATION_VERSION);
					if (version == null) {
						version = attr.getValue(new Name("Bundle-Version"));
					}
					vendor = attr.getValue(Name.IMPLEMENTATION_VENDOR);
				}
			}
			catch (IOException e) {
				Debug.logger.error("Exception while loading program title, version and Jar name.", e);
			}
		}

		// If still failed, put in some default values.
		if (title == null) {
			title = "AVANTSSAR ASLan++ Connector";
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

	public static void main(String[] args) throws NullPointerException, RecognitionException {
		String[] specs = new String[] { "/home/david/proj/avantssar/trunk/shared/case-studies/misc/testsuite/t.aslan++" };
		ASLanPPConnectorImpl conn = new ASLanPPConnectorImpl();
		TranslatorOptions opts = new TranslatorOptions();
		opts.setGoalsAsAttackStates(true);
	//	opts.setPreprocess(true);
		try {
			String[] contents = new String[specs.length];
			for (int i = 0; i < specs.length; i++) {
				contents[i] = Utils.stream2string(new FileInputStream(specs[i]));
			}
			TranslatorOutput result = conn.translateExt(true, null, opts, specs[0], contents).toTranslatorOutput();
			result.printWarnErrors();
			if (result.getSpecification() != null) {
				System.out.println(result.getSpecification());
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

}
