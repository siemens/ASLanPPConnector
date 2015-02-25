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

import javax.xml.bind.annotation.XmlType;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.Option;

@XmlType(namespace = "http://aslanpp.avantssar.org/")
public class TranslatorOptions {

	public static final String GOALS_AS_ATTACK_STATES = "-gas";
	public static final String SETS_AS_MESSAGES = "-sam";
 	public static final String GOALS_AS_LTL_FORMULAS = "-ltl";
	public static final String ORCHESTRATION_CLIENT = "-orch";
	public static final String HORN_CLAUSES_LEVEL = "-hc";
	public static final String OPTIMIZATION_LEVEL = "-opt";
	public static final String STRIP_OUTPUT = "-so";
//	public static final String RULE_NAMES_WITHOUT_LINE_NUMBER = "-rnl";

	@XmlType(namespace = "http://aslanpp.avantssar.org/")
	public static enum OptimizationLevel {
		/**
		 * No optimization is performed.
		 */
		NONE,
		/**
		 * Redundant (empty) transitions are removed.
		 */
		NOPS,
		/**
		 * Redundant (empty) transitions are removed, and then consecutive
		 * transitions are lumped together whenever possible.
		 */
		LUMP
	}

	@XmlType(namespace = "http://aslanpp.avantssar.org/")
	public static enum HornClausesLevel {
		/**
		 * All Horn clauses appear in the ASLan output.
		 */
		ALL,
		/**
		 * The Horn clauses that are related to public and invertible functions
		 * do not appear in the ASLan output.
		 */
		NPI,
		/**
		 * No Horn clause appears in the ASLan output.
		 */
		NONE
	}

	@Option(name = SETS_AS_MESSAGES, aliases = "--sets-as-messages", usage = "Generate support for using sets as message. The default is off, assuming support at the backend side.")
	public static boolean setsAsMessages = false;

	@Option(name = GOALS_AS_ATTACK_STATES, aliases = "--goals-as-attack-states", usage = "Request rendering ASLan++ goals as ASLan attack states whenever possible. This is the default. When an ASLan++ goal cannot be rendered as an ASLan attack state (for instance, if it uses non-trivial LTL operators) then it will be rendered as an ASLan LTL goal and a warning will be issued.")
	private boolean goalsAsAttackStates = true;

	@Option(name = GOALS_AS_LTL_FORMULAS, aliases = "--goals-as-ltl-formulas", usage = "Force rendering ASLan++ goals as ASLan LTL goals. Backends typically have trouble with this.")
	private boolean goalsAsLTLFormulas = false;

	@Option(name = ORCHESTRATION_CLIENT, aliases = "--orchestration-client", metaVar = "CLIENT_ENTITY_NAME", usage = "Render the specified entity as an orchestration client. The name of the specified entity is changed to 'OrchestrationClient' and some extra symbols and transitions are added to the resulting ASLan specification.")
	private String orchestrationClient = null;

	@Option(name = HORN_CLAUSES_LEVEL, aliases = "--horn-clauses-level", usage = "Makes it possible to leave out from the generated ASLan output some or all of the Horn clauses. Can be one of: ALL (all Horn clauses are included in the output), NPI (the Horn clauses related to public and invertible functions are left out of the translation) or NONE (all Horn clauses are left out of the translation). The default value is ALL.")
	private HornClausesLevel hornClausesLevel = HornClausesLevel.ALL;

	@Option(name = OPTIMIZATION_LEVEL, aliases = "--optimization-level", usage = "Sets a certain optimization level. Can be one of: LUMP (this is the highest level of optimization, which means that the generated ASLan transitions are lumped together as much as possible), NOPS (this is an intermediate level of optimization, which means that empty transitions and transitions that can never be taken are eliminated) or NONE (this disables any optimization). The default value is LUMP.")
	private OptimizationLevel optimizationLevel = OptimizationLevel.LUMP;

	// @Option(name = "-bang", aliases = "--use-bang-operator", usage =
	// "Use the bang operator '!' in the generated ASLan output (for compressed entities).")
	// private boolean useBangOperator = false;

	@Option(name = STRIP_OUTPUT, aliases = "--strip-output", usage = "If this flag is enabled, then no comments and no line information will be generated in the ASLan specification. This is intended for debugging purposes.")
	private boolean stripOutput = false;

/*	@Option(name = RULE_NAMES_WITHOUT_LINE_NUMBER, aliases = "--rules-no-line-number", usage = "By default, the generated ASLan transition rules contain ASLan++ source line numbers. Use this option to suppress them.")
	private static boolean rulesNoLineNumber = false;
*/
	@Option(name = "-E", aliases = "--preprocess", usage = "Perform only preprocessing of the ASLan++ specification for error checking. Macros are expanded and a detailed representation of the specification is returned.  Hidden symbols, added automatically during the translation, are also shown, so the specification that is returned cannot be used again as a valid ASLan++ specification. This options is intended only for debugging purposes.")
	private boolean preprocess = false;

	@Option(name = "-pp", aliases = "--pretty-print", usage = "Pretty-print the ASLan++ specification. Comments are lost during pretty-printing. This option is meant mainly for syntax checking specifications and for getting a compact view of large specifications.")
	private boolean prettyPrint = false;

	// Needed for generating web service stuff.
	public TranslatorOptions() {
	}

	/**
	 * Unless LTL formulas are forced,
	 * render ASLan++ goals as ASLan attack states whenever possible.
	 * When an ASLan++ goal cannot be rendered as an ASLan attack state
	 * (for example if it uses non-trivial LTL operators) then
	 * it will be rendered as an ASLan goal and a warning will be issued.
	 */
	public boolean isGoalsAsAttackStates() {
		return goalsAsAttackStates && !goalsAsLTLFormulas;
	}

	public void setGoalsAsAttackStates(boolean goalsAsAttackStates) {
		this.goalsAsAttackStates = goalsAsAttackStates;
	}

	public void setGoalsLTLFormula(boolean goalsAsLTLFormulas) {
		this.goalsAsLTLFormulas = goalsAsLTLFormulas;
	}

	/**
	 * If specified it should refer to the name of an entity from the ASLan++
	 * specification. The respective entity will be renamed to
	 * <i>OrchestrationClient</b> and the ASLan specification will be generated
	 * in such a way that it can be fed into the orchestrator.
	 */
	public String getOrchestrationClient() {
		return orchestrationClient;
	}

	public void setOrchestrationClient(String orchestrationClient) {
		this.orchestrationClient = orchestrationClient;
	}

	/**
	 * Makes it possible to leave out from the generated ASLan output some or
	 * all of the Horn clauses. Can be one of: ALL (all Horn clauses are
	 * included in the output), NPI (the Horn clauses related to public and
	 * invertible functions are left out of the translation) or NONE (all Horn
	 * clauses are left out of the translation). The default value is ALL.
	 */
	public HornClausesLevel getHornClausesLevel() {
		return hornClausesLevel;
	}

	public void setHornClausesLevel(HornClausesLevel hornClausesLevel) {
		this.hornClausesLevel = hornClausesLevel;
	}

	/**
	 * Sets a certain optimization level. Can be one of: LUMP (this is the
	 * highest level of optimization, which means that the generated ASLan
	 * transitions are lumped together as much as possible), NOPS (this is an
	 * intermediate level of optimization, which means that empty transitions
	 * and transitions that can never be taken are eliminated) or NONE (this
	 * disables any optimization). The default value is LUMP.
	 */
	public OptimizationLevel getOptimizationLevel() {
		return optimizationLevel;
	}

	public void setOptimizationLevel(OptimizationLevel optimizationLevel)
			throws CmdLineException {
		this.optimizationLevel = optimizationLevel;
	}

	/**
	 * Use the bang '!' operator in the generated ASLan specification. This is
	 * an alternative way of compressing the output, without physically lumping
	 * the transitions together.
	 */
	// public boolean isUseBangOperator() {
	// return useBangOperator;
	// }
	//
	// public void setUseBangOperator(boolean useBangOperator) throws
	// CmdLineException {
	// this.useBangOperator = useBangOperator;
	// }

	/**
	 * If this flag is enabled, then no comments and no line information will be
	 * generated in the ASLan specification. This is intended for debugging
	 * purposes.
	 */
	public boolean isStripOutput() {
		return stripOutput;
	}

	public void setStripOutput(boolean stripOutput) {
		this.stripOutput = stripOutput;
	}

/*	public static boolean rulesNoLineNumber() {
		return rulesNoLineNumber;
	}
*/
	/**
	 * Perform only preprocessing of the ASLan++ specification. Macros are
	 * expanded and a detailed representation of the specification is returned.
	 * Hidden symbols, added automatically during the translation, are also
	 * shown, so the specification that is returned cannot be used again as a
	 * valid ASLan++ specification. This options is intended only for debugging
	 * purposes.
	 */
	public boolean isPreprocess() {
		return preprocess;
	}

	public void setPreprocess(boolean preprocess) {
		this.preprocess = preprocess;
	}

	/**
	 * Pretty-print the ASLan++ specification. Comments are lost during
	 * pretty-printing. This option is meant mainly for syntax checking
	 * specifications and for getting a compact view of large specifications.
	 */
	public boolean isPrettyPrint() {
		return prettyPrint;
	}

	public void setPrettyPrint(boolean prettyPrint) {
		this.prettyPrint = prettyPrint;
	}

	public void validate() throws TranslatorOptionsException {
		// if (isUseBangOperator() && getOptimizationLevel() ==
		// OptimizationLevel.LUMP) {
		// throw new
		// TranslatorOptionsException("The bang '!' operator cannot be used when the optimization level is set to "
		// + OptimizationLevel.LUMP + ".");
		// }
		if (getOrchestrationClient() != null && !isGoalsAsAttackStates()) {
			throw new TranslatorOptionsException(
					"The orchestration client feature can be used only if goals are rendered as attack states.");
		}
	}

}
