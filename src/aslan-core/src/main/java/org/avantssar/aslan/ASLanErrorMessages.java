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

import java.text.MessageFormat;
import java.util.HashMap;
import org.avantssar.commons.IErrorMessagesProvider;

public class ASLanErrorMessages extends HashMap<String, String> implements IErrorMessagesProvider {

	private static final long serialVersionUID = 3277877894537266341L;

	public static final String WRONG_TYPE_FOR_TERM = "wrong_type_for_term";
	public static final String UNKNOWN_FUNCTION_USED_IN_COMPOUND_TYPE = "unknown_function_used_in_compound_type";
	public static final String FUNCTION_IN_COMPOUND_TYPE_WRONG_NUMBER_OF_ARGUMENTS = "function_in_compound_type_wrong_number_of_arguments";
	public static final String COMPOUND_TYPE_ARGUMENT_DOES_NOT_MATCH = "compound_type_argument_does_not_match";
	public static final String VARIABLE_OF_UNACCEPTED_TYPE = "variable_of_unaccepted_type";
	public static final String WRONG_NUMBER_OF_PARAMETERS = "wrong_number_or_parameters";
	public static final String UNDEFINED_SYMBOL = "undefined_symbol";
	public static final String INVALID_NUMERIC_CONSTANT = "invalid_numeric_constant";
	public static final String LEXER_ERROR = "lexer_error";
	public static final String PARSER_ERROR = "parser_error";
	public static final String STATE_FUNCTION_TOO_FEW_ARGUMENTS = "state_function_too_few_arguments";
	public static final String STATE_FUNCTION_WRONG_TYPED_ARGUMENTS = "state_function_wrong_typed_arguments";
	public static final String INVALID_TAG = "invalid_tag";
	public static final String INVALID_TAG_FOR = "invalid_tag_for";
	public static final String INVALID_TAG_EXPECTING = "invalid_tag_expecting";
	public static final String TOO_FEW_CHILDREN_UNDER = "too_few_childrent_under";
	public static final String UNKNOWN_VARIABLE_IN_TAG = "unknown_variable_in_tag";
	public static final String DUPLICATE_SYMBOL = "duplicate_symbol";
	public static final String NAME_IS_ALREADY_USED = "name_is_already_used";
	public static final String CONSTRUCT_EXPECTS_ONLY_FACTS = "construct_expects_only_facts";
	public static final String INVALID_NAME = "invalid_name";
	public static final String INVALID_EXPL_IMPL_STATE = "invalid_expl_impl_state";
	public static final String IMPL_EXPL_STATE_CONFLICT = "impl_expl_state_conflict";

	public static final String GENERIC_ERROR = "generic_error";

	public static final ASLanErrorMessages DEFAULT = new ASLanErrorMessages();

	private ASLanErrorMessages() {
		put(WRONG_TYPE_FOR_TERM, "Term with type \"{0}\" should not appear in a place where type \"{1}\" (or a subtype of it) is expected: \"{2}\".");
		put(UNKNOWN_FUNCTION_USED_IN_COMPOUND_TYPE, "Compound type \"{0}\" references an unknown function: \"{1}\".");
		put(FUNCTION_IN_COMPOUND_TYPE_WRONG_NUMBER_OF_ARGUMENTS, "Function referenced in compound type \"{0}\" expects {1} arguments, but the compound type has {2} arguments.");
		put(COMPOUND_TYPE_ARGUMENT_DOES_NOT_MATCH, "Argument {0} of compound type \"{1}\" has type \"{2}\" while the same argument of referenced function has type \"{3}\".");
		put(VARIABLE_OF_UNACCEPTED_TYPE, "Variable \"{0}\" is of type \"{1}\" which is not accepted. Variables cannot be of type \"{2}\" or its subtypes.");
		put(WRONG_NUMBER_OF_PARAMETERS, "{0} \"{1}\" expects {2} parameters and receives {3}.");
		put(UNDEFINED_SYMBOL, "Undefined {0}: \"{1}\".");
		put(INVALID_NUMERIC_CONSTANT, "Invalid numeric constant: \"{0}\".");
		put(LEXER_ERROR, "ASLan Lexer: {0}");
		put(PARSER_ERROR, "ASLan Parser: {0}");
		put(STATE_FUNCTION_TOO_FEW_ARGUMENTS, "State function \"{0}\" should have at least 3 arguments and it has only {1}.");
		put(STATE_FUNCTION_WRONG_TYPED_ARGUMENTS, "The first three arguments of state function \"{0}\" should be of typed " + IASLanSpec.AGENT.getName() + " (or a subtype of it), "
				+ IASLanSpec.NAT.getName() + " and " + IASLanSpec.NAT.getName() + ", but they are \"{1}\", \"{2}\" and \"{3}\".");
		put(INVALID_TAG, "Invalid tag. \"{0}\" is not allowed under \"{1}\".");
		put(INVALID_TAG_FOR, "Invalid tag. \"{0}\" is not accepted as a {1} tag.");
		put(INVALID_TAG_EXPECTING, "Invalid tag. Expecting \"{0}\" and encountered \"{1}\".");
		put(TOO_FEW_CHILDREN_UNDER, "Too few children under \"{0}\" node.");
		put(UNKNOWN_VARIABLE_IN_TAG, "Unknown variable \"{0}\" in \"{1}\" tag.");
		put(DUPLICATE_SYMBOL, "Duplicate {0}: \"{1}\".");
		put(NAME_IS_ALREADY_USED, "Name is already used by {0}: \"{1}\".");
		put(CONSTRUCT_EXPECTS_ONLY_FACTS, "{0} expects only facts. The term \"{1}\" has type \"{2}\".");
		put(INVALID_NAME, "Invalid character(s) in \"{0}\" for {1}.");
		put(INVALID_EXPL_IMPL_STATE, "Cannot set the implicit/explicit state to unknown.");
		put(IMPL_EXPL_STATE_CONFLICT, "Symbol \"{0}\" cannot be used as an {1} symbol, because it was already used as an {2} symbol.");
		put(GENERIC_ERROR, "{0}");
	}

	@Override
	public String fill(String key, Object... values) {
		return MessageFormat.format(get(key), values);
	}

}
