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

package org.avantssar.aslan.of;

import java.text.MessageFormat;
import java.util.HashMap;
import org.avantssar.commons.IErrorMessagesProvider;

public class OutputFormatErrorMessages extends HashMap<String, String> implements IErrorMessagesProvider {

	private static final long serialVersionUID = 2604899590452521073L;

	public static final OutputFormatErrorMessages DEFAULT = new OutputFormatErrorMessages();

	public static final String UNKNOWN_ITEM_REFERENCE = "unknown_item_reference";
	public static final String INVALID_TERM_ENCODING = "invalid_term_encoding";
	public static final String ENTITY_NAME_MISSING = "entity_name_missing";
	public static final String WRONG_NUMBER_OF_PARAMETERS = "wrong_number_of_parameters";
	public static final String INVALID_NUMBER = "invalid_line_number";
	public static final String UNRECOGNIZED_METAINFO = "unrecognized_metainfo";
	public static final String MISSING_AT = "missing_at";
	public static final String CANNOT_FIND_ENTITY_INSTANCE = "cannot_find_entity_instance";
	public static final String INVALID_CHANNEL_TYPE = "invalid_channel_type";
	public static final String MULTIPLE_METAINFO = "multiple_meta_info";
	public static final String RULES_AND_CLAUSES_DO_NOT_MATCH = "rules_and_clauses_do_not_match";
	public static final String SET_DOES_NOT_CONTAIN_ELEMENT = "set_does_not_contain_element";
	public static final String LEXER_ERROR = "lexer_error";
	public static final String PARSER_ERROR = "parser_error";
	public static final String DUPLICATE_COMMENTS_SECTION = "duplicate_comments_section";
	public static final String DETAILS_SECTION_MISSING = "details_section_missing";
	public static final String STATISTICS_SECTION_MISSING = "statistics_section_missing";

	private OutputFormatErrorMessages() {
		put(UNKNOWN_ITEM_REFERENCE, "{0} \"{1}\" cannot be found in the ASLan specification.");
		put(INVALID_TERM_ENCODING, "Term is not properly encoded in metainfo: {0}.");
		put(ENTITY_NAME_MISSING, "Entity name is missing in metainfo.");
		put(WRONG_NUMBER_OF_PARAMETERS, "{0} \"{1}\" expects {2} parameters and receives {3}.");
		put(INVALID_NUMBER, "Invalid integer number: {0}.");
		put(UNRECOGNIZED_METAINFO, "Unrecognized metainfo: {0}.");
		put(MISSING_AT, "{0} is missing at {1} metadata.");
		put(CANNOT_FIND_ENTITY_INSTANCE, "Cannot find entity \"{0}\" with IID \"{1}\".");
		put(INVALID_CHANNEL_TYPE, "Invalid channel type: {0}.");
		put(MULTIPLE_METAINFO, "Metainfo {0} appears more than once. Only the first occurence is considered.");
		put(RULES_AND_CLAUSES_DO_NOT_MATCH, "Number of rule sets is {0} and does not match the number of clauses sets {1}.");
		put(SET_DOES_NOT_CONTAIN_ELEMENT, "On deletion of element \"{1}\" from set \"{0}\", cannot confirm that the element was in the set.");
		put(LEXER_ERROR, "OF Lexer: {0}");
		put(PARSER_ERROR, "OF Parser: {0}");
		put(DUPLICATE_COMMENTS_SECTION, "The COMMENTS section appears twice.");
		put(DETAILS_SECTION_MISSING, "The DETAILS section is missing.");
		put(STATISTICS_SECTION_MISSING, "The STATISTICS section is missing.");
	}

	public String fill(String key, Object... values) {
		return MessageFormat.format(get(key), values);
	}

}
