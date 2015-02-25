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

package org.avantssar.aslanpp.model;

import java.text.MessageFormat;
import java.util.HashMap;
import org.avantssar.commons.IErrorMessagesProvider;

public class ErrorMessages extends HashMap<String, String> implements IErrorMessagesProvider {

	private static final long serialVersionUID = -2991181197907671111L;

	public static final String ACTOR_TERM_ON_BOTH_SIDES_OF_CHANNEL_GOAL = "actor_term_on_both_sides_of_channel_goal";
	public static final String FRESHNESS_GOAL_WITHOUT_AUTHENTICATION = "freshness_gaol_without_authenticcationl";
	public static final String ACTOR_TERM_ON_NO_SIDE_OF_CHANNEL_GOAL = "actor_term_on_no_side_of_channel_goal";
	public static final String UNDIRECTED_AUTH_NOT_FOR_CHANNEL_SECRECY_GOAL = "undirected_auth_not_for_channel_secrecy_goal";
	public static final String UNDIRECTED_AUTH_MUST_USE_DUMMY_RECEIVER = "undirected_auth_must_use_dummy_receiver";
	public static final String DUMMY_RECEIVER_ONLY_FOR_SENDING = "dummy_receiver_only_for_sending";
	public static final String CANNOT_FIND_CHILD_ENTITY = "cannot_find_child_entity";
	public static final String CHANNEL_GOAL_ONLY_AFTER_TRANSMISSION_OR_GUARD = "channel_goal_only_after_transmission_or_guard";
	public static final String CHANNEL_GOALS_ONE_TRANSMISSION = "channel_goals_one_transmission";
	public static final String CIRCULAR_DEPENDENCY_DURING_IMPORT = "circular_dependency_during_import";
	public static final String DUPLICATE_SYMBOL_IN_SCOPE = "duplicate_symbol_in_scope";
	public static final String REDEFINING_SYMBOL_OF_SCOPE = "redefining symbol of scope";
	public static final String GOAL_CANNOT_BE_RENDERED_AS_ATTACK_STATE = "cannot_render_goal_as_attack_state";
	public static final String GOAL_FORMULA_IS_NOT_GLOBAL = "goal_formual_is_not_global";
	public static final String INTEGER_LITERAL_EXCEEDS_MAX_SUCC_NESTING = "integer_literal_exceeds_max_succ_nesting";
	public static final String SET_LITERAL_IN_GUARD = "set_literal_in_guard";
	public static final String SET_LITERAL_IN_RECEIVE = "set_literal_in_payload";
	public static final String INVALID_SPECIFICATION_NAME = "invalid_specification_name";
	public static final String INVALID_CHANNEL_MODEL_CODE = "invalid_channel_model_code";
	public static final String MATCHES_CAN_ONLY_BE_USED_IN_GUARDS_AND_RECEIVES = "matches_can_only_be_used_in_guards_and_receives";
	public static final String LEXER_ERROR = "lexer_error";
	public static final String PARSER_ERROR = "parser_error";
	public static final String RECEIVES_ONLY_IN_SELECT_GUARDS_AND_TRANSMISSIONS = "receives_only_in_select_guards_and_transmissions";
	public static final String SENDS_ONLY_IN_TRANSMISSIONS = "sends_only_in_transmissions";
	public static final String SET_SYMBOL_NAME_ALREADY_IN_USE = "set_symbol_name_already_in_use";
	public static final String UNDEFINED_CONSTANT_OR_MACRO = "undefined_constant_or_macro";
	public static final String UNDEFINED_FUNCTION_OR_MACRO = "undefined_function_or_macro";
	public static final String UNDEFINED_TYPE = "undefined_type";
	public static final String UNDEFINED_VARIABLE = "undefined_variable";
	public static final String UNDEFINED_CHANNEL = "undefined_channel";
	public static final String VARIABLE_NOT_UNIFORMLY_MATCHES = "variable_not_uniformly_matched";
	public static final String ELEMENT_OF_TYPE_FACT_NOT_ACCEPTED = "element_of_type_fact_not_accepted";
	public static final String LOCAL_VARIABLE_IN_INVARIANT = "local variable in invariant";
	public static final String INHERITED_VARIABLE_IN_INVARIANT = "inherited_variable_in_invariant";
	public static final String INHERITED_VARIABLE_USED = "inherited_variable_used";
	public static final String INVARIANT_ALWAYS_ACTIVE = "invariant_always_active";
	public static final String LUMPING_ASSUMING_SATISFIABLE_GUARD = "lumping_assuming_satisfiable_guard";
	public static final String WRONG_ARITY_FOR_TUPLE_TYPE = "wrong_arity_for_tuple_type";
	public static final String WRONG_NUMBER_OF_PARAMETERS = "wrong_number_or_parameters";
	public static final String WRONG_TYPE_FOR_TERM = "wrong_type_for_term";
	public static final String WRONG_COMPOUND_TYPE_FOR_TERM = "wrong_compound_type_for_term";
	public static final String CANNOT_GROUP_SYMBOLS_DUE_TO_CATEGORIES = "cannot_group_symbols_due_to_categories";
	public static final String CANNOT_GROUP_SYMBOLS_DUE_TO_TYPES = "cannot_group_symbols_due_to_types";
	public static final String CANNOT_GROUP_SYMBOLS_DUE_TO_NON_PUBLIC = "cannot_group_symbols_due_to_non_public";
	public static final String CANNOT_GROUP_SYMBOLS_DUE_TO_NON_INVERTIBLE = "cannot_group_symbols_due_to_non_invertible";
	public static final String ERROR_AT_FILE_IMPORT = "error_at_file_import";
	public static final String ERROR_AT_IMPORT = "error_at_import";
	public static final String IMPLICIT_EXPLICIT_CONFLICT_KNOWN_LOCATION = "implicit_explicit_conflict_known_location";
	public static final String IMPLICIT_EXPLICIT_CONFLICT_UNKNOWN_LOCATION = "implicit_explicit_conflict_unknown_location";
	public static final String SYMBOL_HIDES_ANOTHER_SYMBOL_IN_ANCESTOR_ENTITY = "symbol_hides_another_symbol_in_ancestor_entity";
	public static final String EQUATIONS_ALLOWED_ONLY_IN_ROOT_ENTITY = "equations_allowed_only_in_root_entity";
	public static final String CONSTRAINTS_ALLOWED_ONLY_IN_ROOT_ENTITY = "constraints_allowed_only_in_root_entity";
	public static final String HORN_CLAUSE_ARGUMENT_CANNOT_BE_UNIVERSALLY_QUANTIFIED = "horn_clause_argument_cannot_be_universally_quantified";
	public static final String UNIVERSALLY_QUANTIFIED_VARIABLE_CANNOT_BE_USED_IN_BODY_OF_HORN_CLAUSE = "universally_quantified_variable_cannot_be_used_in_body_of_horn_clause";
	public static final String UNIVERSALLY_QUANTIFIED_VARIABLE_UNUSED_IN_HEAD_OF_HORN_CLAUSE = "universally_quantified_variable_unused_in_head_of_horn_clause";
	public static final String VARIABLE_MUST_APPEAR_IN_HEAD_OR_ARGS_OF_HORN_CLAUSE = "variable_must_be_used_in_head_or_args_of_horn_clause";
	public static final String VARIABLE_MUST_APPEAR_QUANTIFIED_OR_ARG_OF_HORN_CLAUSE = "variable_must_be_quantified_or_in_args_of_horn_clause";
	public static final String PARAMETER_SHOULD_BE_USED_IN_HORN_CLAUSE = "parameter_should_be_used_in_horn_clause";
	public static final String ONLY_FACTS_CAN = "only_facts_can";
	public static final String TRUE_FALSE_CANNOT = "true_false_cannot_be_introduced_or_retracted";
	public static final String CANNOT_APPEAR_IN_HEAD_OF_HORN_CLAUSE = "cannot_appear_in_head_of_horn_clause";
	public static final String TERM_CANNOT_BE_RETRACTED = "term_cannot_be_retracted";
	public static final String DUPLICATE_TYPE = "duplicate_type";
	public static final String WRONG_TAG_IN_METAINFO = "wrong_tag_in_metainfo";
	public static final String CANNOT_CHANGE_TYPE_OF_VARIABLE = "cannot_change_type_of_variable";
	public static final String EQUALITY_EXPECTED_IN_HORN_CLAUSE = "equality_expected_in_horn_clause";
	public static final String EQUALITIES_IN_HORN_CLAUSES_ARE_EXPERIMENTAL = "equalities_in_horn_clauses_are_experimental";
	public static final String UNKNOWN_FUNCTION_USED_IN_COMPOUND_TYPE = "unknown_function_used_in_compound_type";
	public static final String FUNCTION_IN_COMPOUND_TYPE_WRONG_NUMBER_OF_ARGUMENTS = "function_in_compound_type_wrong_number_of_arguments";
	public static final String INVALID_NUMERIC_FORMAT = "invalid_numeric_format";
	public static final String UNDEFINED_BREAKPOINT = "undefined_breakpoint";

	public static final String ITEM_NOT_ALLOWED_IN_THIS_PLACE = "item_not_allowed_in_this_place";
	public static final String INCOMPLETE_TRANSMISSION_TERM = "incomplete_transmission_term";
	public static final String INVALID_NAME_FOR_ITEM = "invalid_name_for_item";
	public static final String DIFFERENT_ITEM_EXPECTED = "different_item_expected";
	public static final String CHANNEL_MUST_NOT_BE_FRESH_OR_RESILIENT = "channel_must_not_be_fresh_or_resilient";
	public static final String WRONG_NUMBER_OF_TRANSMISSION_PARAMETERS = "wrong_number_of_transmission_parameters";
	public static final String VARIABLE_EXPECTED_IN_ASSIGNMENT_OR_FRESH = "variable_expected_in_assignment_or_fresh";
	public static final String INVALID_CHANNEL_ARROW = "invalid_channel_arrow";
	public static final String INVALID_SECRECY_GOAL = "invalid_secrecy_goal";
	public static final String INTERNAL_ERROR_ANNOTATION_LOST = "internal_error_annotation_lost"; 
	public static final String DUPLICATE_ANNOTATION = "duplicate_annotation"; 
	public static final String ANNOTATED_TERM_NOT_IN_ENTITY = "annotated_term_not_in_entity";
	public static final String CHANNEL_GOAL_ANNOTATION_NOT_IN_PAYLOAD = "annotation_not_in_payload";
	public static final String MISSING_GOAL_FOR_ANNOTATION = "missing_goal_for_annotation";
	public static final String SESSION_GOAL_TERM_USED_MORE_THAN_ONCE = "session_goal_term_used_more_than_once";
	public static final String SESSION_GOAL_TERM_USED_INCONSISTENTLY = "session_goal_term_used_inconsistently";
	public static final String SESSION_GOAL_TERM_NOT_USED_EVERY_INST = "session_goal_term_not_used_in_every_instance";
	public static final String CHANNEL_GOAL_UNUSED_SENDER = "channel_goal_unused_sender";
	public static final String CHANNEL_GOAL_UNUSED_RECEIVER = "channel_goal_unused_receiver";
	public static final String CHANNEL_GOAL_MISMATCH = "channel_goal_mismatch";
	public static final String SECRECY_GOAL_UNUSED = "secrecy_goal_unused";
	public static final String SECRECY_GOAL_NUM_USED = "secrecy_goal_num_used";
	public static final String SESSION_GOAL_TERM_NOT_FOUND = "session_goal_term_not_found";
	public static final String SECRECY_GOAL_IN_UNNESTED_ENTITY = "secrecy_goal_in_unnested_entity";
	public static final String INLINE_SECRECY_GOAL_ASSUMPTION = "inline_secrecy_goal_assumption";
	public static final String INLINE_SECRECY_GOALS_DEPRECATED = "inline_secrecy_goals_deprecated";
	public static final String INLINE_CHANNEL_GOALS_DEPRECATED = "inline_channel_goals_deprecated";
	public static final String INLINE_CHANNEL_GOAL_TYPE_MISMATCH = "inline_channel_goal_type_mismatch";
	public static final String INLINE_CHANNEL_GOAL_MISSING_SENDER = "inline_channel_goal_missing_sender";
	public static final String INLINE_CHANNEL_GOAL_MISSING_RECEIVER = "inline_channel_goal_missing_receiver";

	public static final String ROOT_ENTITY_NO_PARAMETERS = "root_entity_no_parameters";

	public static final String MULTIPLE_SPECIFICATIONS_TO_TRANSLATE = "multiple_specification_to_translate";
	public static final String NO_SPECIFICATION_TO_TRANSLATE = "no_specification_to_translate";
	public static final String INVALID_INPUT_TO_TRANSLATE = "invalid_input_to_translate";
	public static final String INVALID_CHANNEL_MODEL = "invalid_channel_model";

	public static final String VARIABLE_APPEARS_IN_MORE_THAN_ONE_CONJUNCT = "variable_appears_in_more_than_one_conjunct";
	public static final String VARIABLE_NOT_IN_OTHER_DISJUNCT = "variable_not_in_other_disjunct";
	public static final String ACTOR_ASSIGNED = "actor_assigned";

	public static final ErrorMessages DEFAULT = new ErrorMessages();

	private ErrorMessages() {
		put(WRONG_NUMBER_OF_PARAMETERS, "{0} \"{1}\" expects {2} parameters and receives {3}.");
		put(WRONG_TYPE_FOR_TERM, "Term \"{2}\" has type \"{0}\", but the expected type is \"{1}\" (or a subtype).");
		put(WRONG_COMPOUND_TYPE_FOR_TERM, "Term \"{0}\" does not match compound type \"{1}\".");
		put(WRONG_ARITY_FOR_TUPLE_TYPE, "Tuple term \"{2}\" has arity {0}, but the expected arity is {1}.");
		put(DUPLICATE_SYMBOL_IN_SCOPE, "Duplicate {0} \"{1}\" in scope \"{2}\".");
		put(REDEFINING_SYMBOL_OF_SCOPE, "Redefinition of {0} hiding \"{1}\" of scope \"{2}\".");
		put(SENDS_ONLY_IN_TRANSMISSIONS, "Sends can only be used in transmission statements: \"{0}\".");
		put(RECEIVES_ONLY_IN_SELECT_GUARDS_AND_TRANSMISSIONS, "Receives can only be used in select guards and send/receive statements: \"{0}\".");
		put(MATCHES_CAN_ONLY_BE_USED_IN_GUARDS_AND_RECEIVES, "Variable patterns using ''?'' may only be used in guards, retract, and receive statements: \"{0}\".");
		put(CHANNEL_GOALS_ONE_TRANSMISSION, "Channel goals can be used when there is exactly one transmission: \"{0}\".");
		put(VARIABLE_NOT_UNIFORMLY_MATCHES, "Variable name \"{0}\" must not appear with and without preceding ''?'' within the same guard: \"{1}\".");
		put(INHERITED_VARIABLE_USED, "Variable \"{0}\" inherited from entity \"{1}\" is used in entity \"{2}\". So far, OFMC cannot handle this.");
		put(LOCAL_VARIABLE_IN_INVARIANT, "Local variable \"{0}\" is used in goal (sub-)term \"{1}\". So far, CL-AtSe with the \"--lvl 2\" option and OFMC cannot handle this.");
		put(INHERITED_VARIABLE_IN_INVARIANT, "Variable \"{0}\" inherited from entity \"{1}\" is used in goal (sub-)term \"{2}\". So far, CL-AtSe with the \"--lvl 2\" option and OFMC cannot handle this.");
		put(INVARIANT_ALWAYS_ACTIVE, "{0}Goal \"{1}\" is always active, even when no instance of entity \"{2}\" exists."); // because CL-AtSe with the \"--lvl 2\" option and OFMC cannot handle references to entity instances in goals
		put(LUMPING_ASSUMING_SATISFIABLE_GUARD, "Lumping symbolic session instantation under the assumption that the guard \"{0}\" is satisfiable");
		put(GOAL_CANNOT_BE_RENDERED_AS_ATTACK_STATE, "Cannot render goal \"{0}\" as an ASLan attack state and will keep the LTL formula instead.");
		put(GOAL_FORMULA_IS_NOT_GLOBAL, "Goal \"{0}\" is not recognized as an invariant. Maybe what you meant was \"[]({1})\" ?");
		put(INTEGER_LITERAL_EXCEEDS_MAX_SUCC_NESTING, "Value of integer literal \"{0}\" is greater than {1}. It will be treated as uninterpreted nonpublic constant.");
		put(SET_LITERAL_IN_GUARD, "Set literal \"{0}\" cannot appear in guard, sorry.");
		put(SET_LITERAL_IN_RECEIVE, "Set literal \"{0}\" cannot appear in payload of receive statement, sorry.");
		put(ACTOR_TERM_ON_BOTH_SIDES_OF_CHANNEL_GOAL, "Actor is both {0}sender and {1}receiver of channel goal.");
		put(ACTOR_TERM_ON_NO_SIDE_OF_CHANNEL_GOAL, "Actor is missing as {0}sender or {1}receiver of channel goal.");
		put(FRESHNESS_GOAL_WITHOUT_AUTHENTICATION, "Freshness goal can be used only with authentication.");
		put(UNDIRECTED_AUTH_NOT_FOR_CHANNEL_SECRECY_GOAL, "Undirected authentication not possible for channel goal with secrecy");
		put(UNDIRECTED_AUTH_MUST_USE_DUMMY_RECEIVER, "Undirected authentication goal must use \"?\" receiver");
		put(DUMMY_RECEIVER_ONLY_FOR_SENDING, "The \"?\" receiver is only allowed if the sender is \"Actor\"");
		put(CHANNEL_GOAL_ONLY_AFTER_TRANSMISSION_OR_GUARD, "Channel goal can be used only after transmission statement of after guard.");
		put(LEXER_ERROR, "Lexer: {0}");
		put(PARSER_ERROR, "Parser: {0}");
		put(SET_SYMBOL_NAME_ALREADY_IN_USE, "Symbol \"{0}\" needed for set is already used for another function which has a different signature. Name \"{1}\" was picked instead.");
		put(CIRCULAR_DEPENDENCY_DURING_IMPORT, "Circular dependency during import: {0}.");
		put(ELEMENT_OF_TYPE_FACT_NOT_ACCEPTED, "Element \"{0}\" is of type \"{1}\", but type \"" + Prelude.FACT + "\" is not allowed here.");
		put(UNDEFINED_TYPE, "Type \"{0}\" is not defined.");
		put(UNDEFINED_VARIABLE, "Variable \"{0}\" is not defined in the scope of \"{1}\".");
		put(UNDEFINED_FUNCTION_OR_MACRO, "Function or macro \"{0}\" is not defined in the scope of \"{1}\".");
		put(UNDEFINED_CONSTANT_OR_MACRO, "Constant or macro \"{0}\" is not defined in the scope of \"{1}\".");
		put(UNDEFINED_CHANNEL, "Channel \"{0}\" is not defined in the scope of \"{1}\".");
		put(CANNOT_FIND_CHILD_ENTITY, "Entity \"{0}\" is not a direct sub-entity of \"{1}\".");
		put(INVALID_SPECIFICATION_NAME, "Specification name \"{0}\" does not match file name \"{1}\".");
		put(INVALID_CHANNEL_MODEL_CODE, "Invalid channel model option \"{0}\". Valid options are {1}. Assuming \"{2}\".");
		put(CANNOT_GROUP_SYMBOLS_DUE_TO_CATEGORIES, "Cannot group declarations for symbols that have different categories.");
		put(CANNOT_GROUP_SYMBOLS_DUE_TO_NON_INVERTIBLE, "Cannot group declarations for functions that have different ''noninvertible'' attributes.");
		put(CANNOT_GROUP_SYMBOLS_DUE_TO_NON_PUBLIC, "Cannot group declarations for constants that have different ''nonpublic'' attributes.");
		put(CANNOT_GROUP_SYMBOLS_DUE_TO_TYPES, "Cannot group declarations for symbols that have different types.");
		put(ERROR_AT_FILE_IMPORT, "Error at import of entity \"{0}\": {1}.");
		put(ERROR_AT_IMPORT, "Error at import: {0}.");
		put(IMPLICIT_EXPLICIT_CONFLICT_KNOWN_LOCATION, "Symbol \"{0}\" cannot be used as {1} fact, because it was previously used as {2} fact at {3}.");
		put(IMPLICIT_EXPLICIT_CONFLICT_UNKNOWN_LOCATION, "Symbol \"{0}\" cannot be used as {1}, because it was previously used as {2}.");
		put(SYMBOL_HIDES_ANOTHER_SYMBOL_IN_ANCESTOR_ENTITY, "{0} \"{1}\" declared in entity \"{2}\" hides another {3} with the same name defined in outer entity \"{4}\".");
		put(EQUATIONS_ALLOWED_ONLY_IN_ROOT_ENTITY, "Equations are allowed only in the root entity.");
		put(CONSTRAINTS_ALLOWED_ONLY_IN_ROOT_ENTITY, "Constraints are allowed only in the root entity.");
		put(HORN_CLAUSE_ARGUMENT_CANNOT_BE_UNIVERSALLY_QUANTIFIED,
				"Variable \"{0}\" of clause \"{1}\" appears both in the argument list and universally quantified.");
		put(UNIVERSALLY_QUANTIFIED_VARIABLE_CANNOT_BE_USED_IN_BODY_OF_HORN_CLAUSE, "Universally quantified variable \"{0}\" cannot be used in the body of clause \"{1}\".");
		put(UNIVERSALLY_QUANTIFIED_VARIABLE_UNUSED_IN_HEAD_OF_HORN_CLAUSE, "Universally quantified variable \"{0}\" unused in the head of clause \"{1}\".");
		put(VARIABLE_MUST_APPEAR_IN_HEAD_OR_ARGS_OF_HORN_CLAUSE, "Variable \"{0}\" in the body of clause \"{1}\" must appear in its head or argument list.");
		put(VARIABLE_MUST_APPEAR_QUANTIFIED_OR_ARG_OF_HORN_CLAUSE, "Variable \"{0}\" in the head of clause \"{1}\" must appear in argument list or quantified.");
		put(PARAMETER_SHOULD_BE_USED_IN_HORN_CLAUSE, "Parameter \"{0}\" is not used in clause \"{1}\".");
		put(ONLY_FACTS_CAN, "Term \"{1}\" cannot {0} because its type is \"{2}\", while type \"" + Prelude.FACT + "\" is expected.");
		put(TRUE_FALSE_CANNOT, "The constant symbols \"" + Prelude.TRUE + "\" and \"" + Prelude.FALSE + "\" cannot {0}.");
		put(CANNOT_APPEAR_IN_HEAD_OF_HORN_CLAUSE, "Fact symbol \"{0}\" cannot appear in the head of a clause");
		put(TERM_CANNOT_BE_RETRACTED, "The term \"{0}\" cannot be retracted.");
		put(DUPLICATE_TYPE, "Type \"{0}\" was already defined in this or another entity.");
		put(WRONG_TAG_IN_METAINFO, "Wrong tag \"@{0}\" in metainfo comment. Recognized tags are: {1}.");
		put(CANNOT_CHANGE_TYPE_OF_VARIABLE, "Variable \"{0}\" has type \"{1}\". Cannot change its type to \"{2}\".");
		put(EQUALITY_EXPECTED_IN_HORN_CLAUSE, "Equality expected in clause, but found another kind of expression: \"{0}\".");
		put(EQUALITIES_IN_HORN_CLAUSES_ARE_EXPERIMENTAL, "(In-)equalities in clauses are an experimental feature, which is so far only supported by CL-AtSe.");
		put(UNKNOWN_FUNCTION_USED_IN_COMPOUND_TYPE, "Compound type \"{0}\" references an unknown function.");
		put(FUNCTION_IN_COMPOUND_TYPE_WRONG_NUMBER_OF_ARGUMENTS, "Function referenced in compound type \"{0}\" expects {1} arguments, but the compound type has {2} arguments.");
		put(INVALID_NUMERIC_FORMAT, "Invalid numeric format: \"{0}\".");
		put(UNDEFINED_BREAKPOINT, "Breakpoint \"{0}\" refers to an undefined symbol in entity \"{1}\".");

		put(ITEM_NOT_ALLOWED_IN_THIS_PLACE, "{0} is not allowed in this place: {1}. Expected: {2}.");
		put(INCOMPLETE_TRANSMISSION_TERM, "Incomplete transmission term: {0}.");
		put(INVALID_NAME_FOR_ITEM, "Invalid name \"{1}\" for {0}.");
		put(DIFFERENT_ITEM_EXPECTED, "Expecting {0} and encountered: {1}.");
		put(CHANNEL_MUST_NOT_BE_FRESH_OR_RESILIENT, "Assumed channel properties \"{0}\" must not include freshness or resilience.");
		put(WRONG_NUMBER_OF_TRANSMISSION_PARAMETERS, "Transmission expected 2 or 3 parameters (the Actor may be omitted) and receives {0}: {1}.");
		put(VARIABLE_EXPECTED_IN_ASSIGNMENT_OR_FRESH, "Variable expected in {0}: \"{1}\".");
		put(INVALID_CHANNEL_ARROW, "Invalid channel type: \"{0}\".");
		put(INVALID_SECRECY_GOAL, "Invalid secrecy goal: \"{0}\".");
		put(INTERNAL_ERROR_ANNOTATION_LOST, "Internal error: lost annotation \"{0}\" for term \"{1}\".");
		put(DUPLICATE_ANNOTATION, "Duplicate annotation: \"{0}\"");
		put(ANNOTATED_TERM_NOT_IN_ENTITY, "Annotated term does not belong to an entity: \"{0}\".");
		put(CHANNEL_GOAL_ANNOTATION_NOT_IN_PAYLOAD, "Annotation \"{0}\" for channel goal should be in transmission payload context.");
		put(MISSING_GOAL_FOR_ANNOTATION, "For annotation \"{0}\" in entity \"{1}\" there is no corresponding goal.");
		put(SESSION_GOAL_TERM_USED_MORE_THAN_ONCE, "Term \"{0}\" in {1} goal \"{2}\" is used at more than one argument position of instantiation of child entity \"{3}\". This parameter will be ignored.");
		put(SESSION_GOAL_TERM_USED_INCONSISTENTLY, "Term \"{0}\" in {1} goal \"{2}\" is used inconsistently for multiple instantiations of entity \"{3}\". This instance will be ignored.");
		put(SESSION_GOAL_TERM_NOT_USED_EVERY_INST, "Term \"{0}\" in {1} goal \"{2}\" of \"{4}\" not used (at same argument position) in every instance of sub-entity \"{3}\". Actually using parameter \"{4}\".");
		put(CHANNEL_GOAL_UNUSED_SENDER  , "Could not detect sender side use of channel goal \"{0}\".");
		put(CHANNEL_GOAL_UNUSED_RECEIVER, "Could not detect receiver side use of channel goal \"{0}\".");
		put(CHANNEL_GOAL_MISMATCH  , "For channel goal \"{0}\", the {1} side of transmission does not match the computed argument \"{2}\".");
		put(SECRECY_GOAL_UNUSED         , "Could not find use of secrecy goal \"{0}\".");
		put(SECRECY_GOAL_NUM_USED       , "Secrecy goal \"{0}\" should be used at least {1} times, but only {2} use(s) found.");
		put(SESSION_GOAL_TERM_NOT_FOUND, "Term \"{0}\" in {1} goal \"{2}\" of \"{4}\" not found as argument of sub-entity \"{3}\".");
		put(SECRECY_GOAL_IN_UNNESTED_ENTITY, "Secrecy goal can be used only in nested entity (e.g. entities inside a session).");
		put(INLINE_SECRECY_GOAL_ASSUMPTION, "For deprecated inline secrecy goal \"{0}\", assuming that all related goals are declared in direct sub-entities of \"{1}\", that is, in siblings of the \"{2}\" entity. Please check.");
		put(INLINE_SECRECY_GOALS_DEPRECATED, "Inline secrecy goals are deprecated. Better switch to session level secrecy goals.");
		put(INLINE_CHANNEL_GOALS_DEPRECATED, "Inline channel goals are deprecated. Better switch to session level channel goals.");
		put(INLINE_CHANNEL_GOAL_TYPE_MISMATCH, "Type of inline channel goal at {0} is inconsistent with this channel goal.");
		put(INLINE_CHANNEL_GOAL_MISSING_SENDER, "No related inline channel goal found with Actor in sender position.");
		put(INLINE_CHANNEL_GOAL_MISSING_RECEIVER, "No relatecd inline channel goal found with Actor in receiver position.");

		put(ROOT_ENTITY_NO_PARAMETERS, "The root entity \"{0}\" cannot have parameters.");

		put(MULTIPLE_SPECIFICATIONS_TO_TRANSLATE,
				"More than one ASLan++ specifications were provided for translation. Please provide only one specification, with possibly additional imported modules.");
		put(NO_SPECIFICATION_TO_TRANSLATE, "No specification was provided for translation. Please check that you are not trying to translate only imported modules.");
		put(INVALID_INPUT_TO_TRANSLATE, "Invalid input provided for translation. Please provide an ASLan++ specification.");
		put(INVALID_CHANNEL_MODEL, "Invalid channel model in specification.");

		put(VARIABLE_APPEARS_IN_MORE_THAN_ONE_CONJUNCT, "In the DNF of the guard \"{0}\", variable \"{1}\" appears in more than one conjunct: \"{2}\" and \"{3}\" within the same disjunct. If this guard <G> is in a trailing ''if'' statement without an ''else'', you may replace the ''if(<G>) <S>'' by ''select '{ on(<G>): <S>'}''");
		put(VARIABLE_NOT_IN_OTHER_DISJUNCT, "In the DNF of the {0}guard \"{1}\", variable \"{2}\" is assigned in some situations but not in disjunct \"{3}\"");
		put(ACTOR_ASSIGNED, "The \"Actor\" parameter may not be assigned to.");
	}

	public String fill(String key, Object... values) {
		String pattern = get(key);
		if (pattern == null)
			return ("internal bug: no pattern found for error key '"+key+"");
		else
			return MessageFormat.format(pattern, values);
	}
}
