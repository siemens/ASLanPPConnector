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

grammar ASLanPPNew;

options {
		output=AST;
		ASTLabelType = CommonTree;
}

tokens {
		ANNOTATED;
		ARGS;
		ARROW;
		ASSERT;
		ASSIGNMENT;
		AUTHENTIC;
		BLOCK;
		BODY;
		HEAD;
		CHANNEL;
		CHANNEL_NAMED;
		CHANNEL_GOAL;
		CHANNEL_NO_TYPE;
		CHANNEL_PART_RECEIVE;
		CHANNEL_PART_SEND;
		CHANNEL_TYPE;
		HORN_CLAUSE;
		HORN_CLAUSES;
		COMPRESSED;
		CONCAT;
		CONFIDENTIAL;
		CONJUNCTION;
		CONST_VAR;
		CONSTS;
		CONSTRAINT;
		CONSTRAINTS;
		DECLARATION;
		DISJUNCTION;
		ENTITY;
		ENTITYINST;
		EQUATIONS;
		EQUATION;
		EQUALITY;
		INEQUALITY;
		EXISTS;
		FCALL;
		FUNCTION;
		FORALL;
		FORALL;
		FRESH;
		INVARIANT_GOAL;
		SESSION_CHANNEL_GOAL;
		SESSION_SECRECY_GOAL;
		GOALS;
		IF;
		IMPLICATION;
		IMPORTS;
		INTRODUCE;
		KNOWERS;
		LHS;
		LTL_BINARY;
		LTL_UNARY;
		MACRO;
		MACROS;
		MATCH;
		MATCH_EMPTY;
		NEGATION;
		NEW;
		NOTHING;
		NUMERIC;
		OPERATOR;
		OVER;
		PAREN;
		PAYLOAD;
		PSEUDONYM;
		RECEIVE;
		REGULAR;
		RESILIENT;
		NOT_RESILIENT;
		NOT_FRESH;
		RETRACT;
		RHS;
		SECRECY_GOAL;
		SECURE;
		SELECT;
		SELECT_OPTION;
		SEND;
		SEND_CHANNEL;
		SET;
		ANY;
		SYMBOLS;
		STMT_BLOCK;
		TUPLE;
		VAR;
		VARS;
		WHILE;
		BREAKPOINTS;
		UNCOMPRESSED;
		NONPUBLIC='nonpublic';
		NONINVERTIBLE='noninvertible';
		FLAGS;
		ARGUMENTS;
		OOP_CALL;
		TRANSMISSION_FNC;
		TRANSMISSION_ANN;
		TERM;
		GUARD;
		CHANNEL_GOALS;
}
       
@parser::header {
            package org.avantssar.aslanpp;

			import org.avantssar.commons.*;            
            import org.avantssar.aslanpp.model.*;
                        
            import java.util.Map;
            import java.util.HashMap;
            import java.util.List;
            import java.util.ArrayList;
            import java.util.Set;
            import java.util.HashSet;
        }

@lexer::header {
            package org.avantssar.aslanpp;
 
			import org.avantssar.commons.*;            
            import org.avantssar.aslanpp.model.*;
        }

@lexer::members {
	private ErrorGatherer err;

	public void setErrorGatherer(ErrorGatherer eg) {
		err = eg;
	}

	//overrides http://www.antlr.org/api/Java/_base_recognizer_8java-source.html#l00189
    public void displayRecognitionError(String[] tokenNames, RecognitionException e)
    {
		err.addError(new LocationInfo(e),
                     ErrorMessages.DEFAULT.LEXER_ERROR, getErrorMessage(e, tokenNames));
    }

	//overrides http://www.antlr.org/api/Java/_lexer_8java-source.html#l00264
	public String getErrorMessage(RecognitionException e, String[] tokenNames)
	{
	    String msg;
        if (e instanceof NoViableAltException) { //no viable alternative
            NoViableAltException nvae = (NoViableAltException)e;
            msg = "cannot proceed at character "+getCharErrorDisplay(e.c);
        }
        else if (e instanceof EarlyExitException) { //required (...)+ loop did not match anything
            EarlyExitException eee = (EarlyExitException)e;
            msg = "missing at least one occurrence of repeatable element at character "+getCharErrorDisplay(e.c);
        }
        else {
            msg = super.getErrorMessage(e, tokenNames);
        }
        return msg;
	}
}

@parser::members {
	enum ExpressionContext {TermOnly, ChannelOnly, TermOnlyNoChannel, GuardOnly, GuardOnlyNoReceive, ExpressionNoChannel, Expression};

	private ErrorGatherer err;

    public void setErrorGatherer(ErrorGatherer eg) {
        err = eg;
    }
	
	//overrides http://www.antlr.org/api/Java/_base_recognizer_8java-source.html#l00189
    public void displayRecognitionError(String[] tokenNames, RecognitionException e)
    {
		err.addError(new LocationInfo(e),
                     ErrorMessages.DEFAULT.PARSER_ERROR, getErrorMessage(e, tokenNames));
    }

	//overrides http://www.antlr.org/api/Java/_base_recognizer_8java-source.html#l00219
	public String getErrorMessage(RecognitionException e, String[] tokenNames)
	{
	    String msg = null;
        if (e instanceof NoViableAltException) { //no viable alternative
            msg = "cannot proceed at input "+getTokenErrorDisplay(e.token);
        }
        else if (e instanceof EarlyExitException) { //required (...)+ loop did not match anything
            msg = "missing at least one occurrence of repeatable element at input "+getTokenErrorDisplay(e.token);
        }
	    else if (e instanceof ASLanPPSyntaxErrorException) {
	    	ASLanPPSyntaxErrorException asee = (ASLanPPSyntaxErrorException)e;
	    	msg = asee.getMessage();
	    }
	    if (msg == null)
	    	msg = super.getErrorMessage(e, tokenNames);
	    return msg;
	}
	
	private void gatherComments(Token currentToken, ICommentsHolder ch, int channelCode) {
		List<String> comments = new ArrayList<String>();
		List<LocationInfo> locations = new ArrayList<LocationInfo>();
		int i = currentToken.getTokenIndex();
		boolean over = false;
		do {
			i--;
			if (i >= 0) {
				Token tok = input.get(i);
				if (tok.getChannel() == channelCode) {
					String line = tok.getText().trim();
					comments.add(0, line);
					locations.add(0, new LocationInfo(tok));
				}
			}
			else {
				over = true;
			}
		}
		while (!over);
		for (i = 0; i < comments.size(); i++) {
			String comment = comments.get(i);
			LocationInfo location = locations.get(i);
			ch.addCommentLine(comment, location);
		}
	}
}

program[EntityManager manager] returns [ASLanPPSpecification spec]
scope {
   ChannelModel cm;
}
	: s='specification' sn=name
	    'channel_model' cn=name
	  { $spec = new ASLanPPSpecification($manager, new LocationInfo($sn.tree), $sn.text, new LocationInfo($cn.tree), $cn.text);
        gatherComments($s, $spec, 62); } 
	  entityDeclaration[$spec] EOF 
	  -> entityDeclaration
	;
        
entityDeclaration[IEntityOwner parent] returns [Entity e]
	: 'entity' UPPERNAME 
	{ $e = $parent.entity($UPPERNAME.text); }
	argumentsDeclarations? '{' imports[$e]? (d+=declarations[$e])* (ed+=entityDeclaration[$e])* body? constraints? goalDeclarations? '}' 
		-> ^(ENTITY UPPERNAME argumentsDeclarations? ($d+)? ($ed+)? body? constraints? goalDeclarations?);

argumentsDeclarations 
	: '(' as+=argumentsDeclaration (',' as+=argumentsDeclaration)* ')' -> ^(ARGUMENTS $as+)
	;
argumentsDeclaration
	: v+=var (',' v+=var)* ':' type -> ^(DECLARATION type ^(VARS $v+)) 
	;

imports[Entity e] 
	: 'import' vars ';'
      {
         $e.addImports($vars.names.toArray(new String[$vars.names.size()]));
      }   
    ;

declarations[Entity e]
	: typeDeclarations[$e]!
	| symbolDeclarations
	| macroDeclarations
	| hornClauseDeclarations
	| equationDeclarations
	;

typeDeclarations[Entity e] 
	: 'types' (typeDeclaration[$e] ';')+
	;
typeDeclaration[Entity e]
	: a=LOWERNAME '<' b=LOWERNAME 
	{if ($e.findType($b.text)==null) $e.type(new LocationInfo($b), $b.text);
	$e.type(new LocationInfo($a), $a.text, $e.findType($b.text)); }
	| a=LOWERNAME {$e.type(new LocationInfo($a), $a.text);}
	;

symbolDeclarations 
	: 'symbols' (sd+=symbolDeclaration)+ 
	  -> ^(SYMBOLS $sd+)
	;
symbolDeclaration
	: symbolDeclarationEnum ':' type ';'
	   -> ^(DECLARATION type symbolDeclarationEnum)
	; 
symbolDeclarationEnum 
	: v+=var (',' v+=var)*  -> ^(VARS $v+)
	  | NONPUBLIC* c+=constant (',' c+=constant)* -> ^(CONSTS ^(FLAGS NONPUBLIC)? $c+)
	  | (NONPUBLIC| NONINVERTIBLE)* LOWERNAME '(' t+=type (',' t+=type)* ')' -> ^(FUNCTION ^(FLAGS NONPUBLIC? NONINVERTIBLE?)? LOWERNAME $t+)  
	;
	
macroDeclarations 
	: 'macros' (md+=macroDeclaration)+ 
	  -> ^(MACROS $md+) 
	;
macroDeclaration
	: (vs+=UPPERNAME '->')? LOWERNAME ('(' vs+=UPPERNAME (',' vs+=UPPERNAME)* ')')? '=' expression ';'
		-> ^(MACRO LOWERNAME ^(ARGS $vs+)? expression)
    ;
	
hornClauseDeclarations 
	: 'clauses' (hc+=hornClauseDeclaration)+  
	     -> ^(HORN_CLAUSES $hc+) 
	;
hornClauseDeclaration
	: LOWERNAME ('(' args+=UPPERNAME (',' args+=UPPERNAME)* ')')? ':' 
	    (('forall')=>'forall' (forall+=UPPERNAME)+ '.')? lhs=regular_or_oop
	    (':-' rhs+=equality ('&' rhs+=equality)*)? ';'
	    -> ^(HORN_CLAUSE LOWERNAME ^(ARGS $args+)? ^(FORALL $forall+)? ^(HEAD $lhs) ^(BODY ($rhs+)?)) 
	;
	
equationDeclarations 
	: 'equations' (ed+=equationDeclaration)+
	    -> ^(EQUATIONS $ed+) 
	; 
equationDeclaration 
	: lhs=regular_or_oop '=' rhs=regular_or_oop ';'
	    -> ^(EQUATION $lhs $rhs)
	;
	
body 
	: breakpointsDeclaration? 'body' stmt 
		-> ^(BODY breakpointsDeclaration? stmt)
	;
breakpointsDeclaration 
	: 'breakpoints' '{' c+=constant (',' c+=constant)* '}' -> ^(BREAKPOINTS $c+) 
	| 'uncompressed' -> ^(UNCOMPRESSED)
	;

constraints 
	: 'constraints' (c+=constraint)+ 
	   -> ^(CONSTRAINTS $c+) 
	;
constraint 
	: name ':' expression ';' 
	   -> ^(CONSTRAINT name expression) 
	;

goalDeclarations 
	: 'goals' (g+=goalDeclaration)+ 
	   -> ^(GOALS $g+) 
	;
goalDeclaration 
	: name ':' expression ';' 
	   -> ^(INVARIANT_GOAL name expression) 
	| name ':(_)' expression ';'
	   ->{$expression.wasChannelGoal}? ^(SESSION_CHANNEL_GOAL name expression)
	   ->^(SESSION_SECRECY_GOAL name expression)
	;



	
type : (nonSetPart->nonSetPart) ('set' -> ^(SET $type))? ;

nonSetPart
	: t+=concatPart ('.' t+=concatPart)*
		-> {$t.size() > 1}? ^(CONCAT $t+)
		-> {(CommonTree)$t.get(0)}
	;

concatPart 
	: t+=tuplePart ('*' t+=tuplePart)*
			-> {$t.size() > 1}? ^(TUPLE $t+)
			-> {(CommonTree)$t.get(0)}
	;

tuplePart 
	: (prefixType -> prefixType)
	  ('->' LOWERNAME ('(' types ')')? -> ^(FCALL LOWERNAME ^(ARGS $tuplePart types?)))*
	;
	
prefixType
	: (LOWERNAME->LOWERNAME) ('(' types ')' -> ^(FCALL $prefixType ^(ARGS types)))? 
	| '(' types ')' -> {$types.foundTypes.size() > 1}? ^(TUPLE types)
	                -> types
	;
	
types returns [List foundTypes]
@init { $foundTypes = new ArrayList(); }
	: t=type {$foundTypes.add($t.tree);} (','! t=type {$foundTypes.add($t.tree);})*;

var : UPPERNAME;
constant : LOWERNAME;

vars returns [List<String> names]
@init { $names = new ArrayList<String>(); }
    : v=var { $names.add($v.text); } (','! v=var { $names.add($v.text); })*
    ;

varsplain returns [List<String> names]
@init { $names = new ArrayList<String>(); }
    : (v=var { $names.add($v.text); })+
    ;

constants returns [List<String> names]
@init { $names = new ArrayList<String>(); }
    : c=constant { $names.add($c.text); } (','! c=constant { $names.add($c.text); })*
    ;

expression_with_eof
	: expression EOF
	;

expression_test : expression;
	
expression returns [boolean wasChannelGoal]
	: (e1=disjunction {$wasChannelGoal = $e1.wasChannelGoal;} -> $e1)
	  (('=>')=>s='=>' e2=expression {$wasChannelGoal = true;} -> ^(IMPLICATION[$s, "IMPLICATION"] $e1 $e2))?
	;
	
disjunction returns [boolean wasChannelGoal]	
	: (e1=conjunction {$wasChannelGoal = $e1.wasChannelGoal;} -> $e1)
	  (('|')=>s='|' e2=disjunction {$wasChannelGoal = false;} -> ^(DISJUNCTION[$s, "DISJUNCTION"] $e1 $e2))?
	;
	
conjunction returns [boolean wasChannelGoal]
	: (e1=atomic {$wasChannelGoal = $e1.wasChannelGoal;} -> $e1) 
	  (('&')=>s='&' e2=conjunction {$wasChannelGoal = false;} -> ^(CONJUNCTION[$s, "CONJUNCTION"] $e1 $e2))?
	;
	
atomic returns [boolean wasChannelGoal]
	: equality {$wasChannelGoal = $equality.wasChannelGoal;} -> equality
	| s='forall' (v+=var)+ '.' ex=expression {$wasChannelGoal = false;} -> ^(FORALL[$s, "FORALL"] ^(VARS $v+) $ex)
	| s='exists' (v+=var)+ '.' ex=expression {$wasChannelGoal = false;} -> ^(EXISTS[$s, "EXISTS"] ^(VARS $v+) $ex) 
	| s='!' eq=atomic {$wasChannelGoal = false;} -> ^(NEGATION[$s, "NEGATION"] $eq)
	;
	
equality returns [boolean wasChannelGoal]
	: (transmission {$wasChannelGoal = $transmission.wasChannelGoal;} -> transmission)
	  (('=')=>s='='  t2=regular_or_oop {$wasChannelGoal = false;} -> ^(EQUALITY[$s, "EQUALITY"] $equality $t2)
	  |('!=')=>s='!='  t2=regular_or_oop {$wasChannelGoal = false;} -> ^(INEQUALITY[$s, "INEQUALITY"] $equality $t2))?
	;
	

transmission returns [boolean wasChannelGoal]
	: (other_channel {$wasChannelGoal = $other_channel.wasChannelGoal;} -> other_channel) 
	  (('over')=>'over' name {$wasChannelGoal = false;} -> ^(TRANSMISSION_FNC other_channel name) 
	  | (':')=>':' payload=regular_or_oop {$wasChannelGoal = false;} -> ^(TRANSMISSION_ANN other_channel $payload)
	  )? 
	;

other_channel returns [boolean wasChannelGoal]
	: (s=regular_or_oop {$wasChannelGoal = $s.wasChannelGoal;} -> $s) 
	  (('*->'  )=>c='*->'   r=regular_or_oop {$wasChannelGoal = true;} -> ^(CHANNEL $s $c $r)
	 | ('*->?' )=>c='*->?'  r=regular_or_oop {$wasChannelGoal = true;} -> ^(CHANNEL $s $c $r)
	 | ( '->*' )=>c= '->*'  r=regular_or_oop {$wasChannelGoal = true;} -> ^(CHANNEL $s $c $r)
	 | ('*->*' )=>c='*->*'  r=regular_or_oop {$wasChannelGoal = true;} -> ^(CHANNEL $s $c $r)
	 | ('*=>'  )=>c='*=>'   r=regular_or_oop {$wasChannelGoal = true;} -> ^(CHANNEL $s $c $r)
	 | ('*=>?' )=>c='*=>?'  r=regular_or_oop {$wasChannelGoal = true;} -> ^(CHANNEL $s $c $r)
	 | ( '=>*' )=>c= '=>*'  r=regular_or_oop {$wasChannelGoal = true;} -> ^(CHANNEL $s $c $r)
	 | ('*=>*' )=>c='*=>*'  r=regular_or_oop {$wasChannelGoal = true;} -> ^(CHANNEL $s $c $r)
	 | ( '->>' )=>c= '->>'  r=regular_or_oop {$wasChannelGoal = true;} -> ^(CHANNEL $s $c $r)
	 | ('*->>' )=>c='*->>'  r=regular_or_oop {$wasChannelGoal = true;} -> ^(CHANNEL $s $c $r)
	 | ('*->>?')=>c='*->>?' r=regular_or_oop {$wasChannelGoal = true;} -> ^(CHANNEL $s $c $r)
	 | ( '->>*')=>c= '->>*' r=regular_or_oop {$wasChannelGoal = true;} -> ^(CHANNEL $s $c $r)
	 | ('*->>*')=>c='*->>*' r=regular_or_oop {$wasChannelGoal = true;} -> ^(CHANNEL $s $c $r)
	 | ('*=>>' )=>c='*=>>'  r=regular_or_oop {$wasChannelGoal = true;} -> ^(CHANNEL $s $c $r)
	 | ('*=>>?')=>c='*=>>?' r=regular_or_oop {$wasChannelGoal = true;} -> ^(CHANNEL $s $c $r)
	 | ( '=>>*')=>c= '=>>*' r=regular_or_oop {$wasChannelGoal = true;} -> ^(CHANNEL $s $c $r)
	 | ('*=>>*')=>c='*=>>*' r=regular_or_oop {$wasChannelGoal = true;} -> ^(CHANNEL $s $c $r)
	 | ( '=>>' )=>c= '=>>'  r=regular_or_oop {$wasChannelGoal = true;} -> ^(CHANNEL $s $c $r)
	 | ( '-' (UPPERNAME|LOWERNAME) '->' )=>'-' (c=UPPERNAME|c=LOWERNAME) '->' r=regular_or_oop {$wasChannelGoal = true;} -> ^(CHANNEL_NAMED $s $c $r) 
	  )?
	;

regular_or_oop returns [boolean wasChannelGoal]
	: (t1=concat {$wasChannelGoal = $t1.wasChannelGoal;} -> $t1)
	  (('->')=>s='->' t2=concat {$wasChannelGoal = true;} -> ^(OOP_CALL $regular_or_oop $t2))*
	;
	
concat returns [boolean wasChannelGoal]
@init {
	$wasChannelGoal = false;
}
	: (base -> base) 
	  (('.')=>
	    (('.')=>s='.' ts+=base)+ -> ^(CONCAT[$s, "CONCAT"] $concat $ts+)
	  )?
	;
	
base returns [boolean wasChannelGoal]
@init {
	$wasChannelGoal = false;
}
	: ((n=LOWERNAME|n=UPPERNAME|n=LTL_SPECIAL_OP) -> ^(CONST_VAR[$n, "CONST_VAR"] $n)) 
	  ('(' ts+=expression (',' ts+=expression)* ')' -> ^(FCALL[$n, "FCALL"] $n ^(ARGS $ts+)))?
	| NUMBER -> ^(NUMERIC[$NUMBER, "NUMERIC"] NUMBER)
	| sb='[' t1=expression (']_[' t2=expression)? ']' -> ^(PSEUDONYM[$sb, "PSEUDONYM"] $t1 $t2?)
	| q='?' UPPERNAME? -> ^(MATCH[$q, "MATCH"] UPPERNAME?)
	| cb='{' '}' -> ^(SET[$cb, "SET"])
	| cb='{' es+=expression 
	  ( (',' es+=expression)* '}' -> ^(SET[$cb, "SET"] $es+)
	  | '}_' key=base -> ^(FCALL LOWERNAME[$cb, "crypt"] ^(ARGS $key $es+))
	  | '}_inv' key=base -> ^(FCALL LOWERNAME[$cb, "sign"] ^(ARGS ^(FCALL LOWERNAME[$cb, "inv"] ^(ARGS $key)) $es+))
	  )
	| cb='{|' payload=expression '|}_' key=base -> ^(FCALL LOWERNAME[$cb, "scrypt"] ^(ARGS $key $payload))
	| name ':(' expression ')' -> ^(ANNOTATED name expression)
	| rb='(' (t1=expression -> ^(PAREN $t1)) ((',' ts+=expression)+  -> ^(TUPLE[$rb, "TUPLE"] $t1 $ts+))? ')'
	;
		
stmt_with_eof 
    : stmt EOF
    ;

stmt
scope { boolean hasExists; }
@init { $stmt::hasExists = false; }
	: (annotated_var ':=')=>vr=annotated_var w=':=' 
		('fresh()' -> ^(FRESH[$w, "FRESH"] ^(VAR $vr)) 
		 | a=expression -> ^(ASSIGNMENT[$w, "ASSIGNMENT"] ^(VAR $vr) ^(TERM $a))) ';'
	| ('{')=>w='{' 
		('}' -> ^(BLOCK[$w, "BLOCK"])
		| (s+=stmt)+ '}' -> ^(BLOCK[$w, "BLOCK"] $s+))
	| w='new' entityInst ';' -> ^(NEW[$w, "NEW"] entityInst)
	| w='any' (varsplain '.')? entityInst (('where')=>'where' expression)? ';' 
	    -> ^(ANY[$w, "ANY"] ^(VARS varsplain)? entityInst ^(GUARD expression?))
	| w='retract' expression ';' 
	    -> ^(RETRACT[$w, "RETRACT"] ^(TERM expression))
	| w='if' '(' expression ')' sa=stmt (('else')=>'else' sb=stmt)? 
	    -> ^(IF[$w, "IF"] ^(GUARD expression) $sa $sb?)
	| w='while' '(' expression ')' stmt 
	    -> ^(WHILE[$w, "WHILE"] ^(GUARD expression) stmt)
	| w='select' '{' selectOption+ '}' 
	    -> ^(SELECT[$w, "SELECT"] selectOption+)
	| (w='assert' name ':' (('exists')=>'exists' (v+=var)+ '.' {$stmt::hasExists = true;})? expression ';' 
			-> {$stmt::hasExists}? ^(ASSERT[$w, "ASSERT"] name ^(GUARD ^(EXISTS ^(VARS $v+) expression)))
			-> ^(ASSERT[$w, "ASSERT"] name ^(GUARD expression))
	  )
	| w='secrecy_goal' name ':' 
	    ks+=regular_or_oop (',' ks+=regular_or_oop)* ':' 
	    payload=regular_or_oop ';' 
	    -> ^(SECRECY_GOAL[$w, "SECRECY_GOAL"] name ^(KNOWERS $ks+) ^(PAYLOAD $payload))
	| ex=expression ';' 
	  (('channel_goal')=> cg+=channelgoal 
	  )*   
	    -> ^(INTRODUCE ^(TERM $ex) ^(CHANNEL_GOALS $cg*))
	;

annotated_var
	: UPPERNAME -> ^(CONST_VAR[$UPPERNAME, "CONST_VAR"] UPPERNAME)
	| n=name ann=':(' av=annotated_var ')' -> ^(ANNOTATED[$ann, "ANNOTATED"] $n $av) 
	;
	
channelgoal 
	: w='channel_goal' name ':' expression ';' 
	    -> ^(CHANNEL_GOAL[$w, "CHANNEL_GOAL"] name expression)
	;
	
entityInst 
    : var ('(' es+=expression (',' es+=expression)* ')')? 
        -> ^(ENTITYINST var ^(ARGS $es+)?);
        
selectOption 
    : w='on' '(' expression ')' ':' cg+=channelgoal* stmt 
        -> ^(SELECT_OPTION[$w, "SELECT_OPTION"] ^(GUARD expression) ^(CHANNEL_GOALS $cg*) stmt) ;

name : UPPERNAME | LOWERNAME;

LTL_SPECIAL_OP : '<>' | '<->' | '[]' | '[-]';
UPPERNAME  : UPPERLETTER ALPHANUM*;
LOWERNAME  : LOWERLETTER ALPHANUM*;	
NUMBER     : '0' | NON_ZERO_DIGIT DIGIT*;
fragment UPPERLETTER : 'A'..'Z' ;
fragment LOWERLETTER : 'a'..'z' ;
fragment DIGIT : '0' | NON_ZERO_DIGIT ;
fragment NON_ZERO_DIGIT : '1'..'9';
fragment ALPHANUM : UPPERLETTER | LOWERLETTER | DIGIT | '_' | '\'' ;
WS: (' ' | '\t' | '\n' |'\r' )+ {$channel=HIDDEN;} ;
LINE_COMMENT : '%' ~('\n'|'\r')* ('\r'? '\n'|EOF) {$channel=62;} ;
