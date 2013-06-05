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

grammar of;

tokens {
	COMMENTS='COMMENTS';
	STATISTICS='STATISTICS';
}

@parser::header {
  package org.avantssar.aslan.of;

  import org.avantssar.commons.*;			
  import org.avantssar.aslan.*;
  import java.util.List;
  import java.util.ArrayList;
  import java.io.PrintWriter;
  import java.io.StringWriter;
}

@lexer::header {
  package org.avantssar.aslan.of;     

  import org.avantssar.commons.*;			
  import org.avantssar.aslan.*;
}

@lexer::members {
	private ErrorGatherer err;

	public ErrorGatherer getErrorGatherer() {
		return err;
	}

	public void setErrorGatherer(ErrorGatherer eg) {
		err = eg;
	}
	
	//overrides http://www.antlr.org/api/Java/_base_recognizer_8java-source.html#l00189
    public void displayRecognitionError(String[] tokenNames, RecognitionException e)
    {
		err.addError(new LocationInfo(e),
                     OutputFormatErrorMessages.DEFAULT.LEXER_ERROR, getErrorMessage(e, tokenNames));
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
	private ErrorGatherer err;

	public void setErrorGatherer(ErrorGatherer eg) {
		err = eg;
	}
	
	public ErrorGatherer getErrorGatherer() {
		return err;
	}

	//overrides http://www.antlr.org/api/Java/_base_recognizer_8java-source.html#l00189
    public void displayRecognitionError(String[] tokenNames, RecognitionException e)
    {
		err.addError(new LocationInfo(e),
                     OutputFormatErrorMessages.DEFAULT.PARSER_ERROR, getErrorMessage(e, tokenNames));
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
	    if (msg == null)
	    	msg = super.getErrorMessage(e, tokenNames);
	    return msg;
	}
}

aslanOF[IASLanSpec aslanSpec] returns [AnalysisResult result]
  : 'INPUT' filename=WORD {ParserUtil.checkFileName(input, $filename.text);}
     {$result = new AnalysisResult($filename.text);}
    'SUMMARY' 
    ('ATTACK_FOUND' goals {$result.setAttack($goals.g);} |
     'NO_ATTACK_FOUND' |
     'INCONCLUSIVE' {$result.setInconclusive(true);})
    ('DETAILS' (dw=WORD {$result.addDetail($dw.text);})+)?
    'BACKEND' backend=WORD 
    {ParserUtil.checkBackend(input, $backend.text);
     $result.setBackendName($backend.text);
    } 
    'VERSION' ver=WORD {$result.setBackendVersion($ver.text);} 
    ('(' date=WORD {$result.setBackendDate($date.text);}')')?
    (COMMENTS (cw=WORD {$result.addComment($cw.text);})+)?
    STATISTICS (stat {$result.addStatistics($stat.l, $stat.v, $stat.u);})+
    ('UNUSED:' '{' ( u1=WORD {ParserUtil.checkName(input, $u1.text); $result.addUnused($u1.text);} (',' u2=WORD {ParserUtil.checkName(input, $u2.text); $result.addUnused($u2.text);})* )? '}')?
    ('TRACE:' trace {$result.setTrace($trace.t);})?
    ('CLOSED_FINAL_STATE:'
      (
    	('EXPLICIT:' '{' ( et1=term {$result.addExplicit($et1.iterm);} (',' et2=term {$result.addExplicit($et2.iterm);})* )? '}'
    	'IMPLICIT:' '{' ( it1=term {$result.addImplicit($it1.iterm);} (',' it2=term {$result.addImplicit($it2.iterm);})* )? '}'
        )      
      |
        (
        '{' ( et1=term {$result.addFinal($et1.iterm);} (',' et2=term {$result.addFinal($et2.iterm);})* )? '}'
        )
      )	
    )?
    EOF
  ;

goals returns [Goals g]
@init {
	$g = new Goals();
}
	: 'GOAL:' term {$g.add($term.iterm);}|
	  'GOALS:' '{' terms {$g.addAll($terms.iterms);} '}'
	;

stat returns [String l, String v, String u]
	: label=WORD value=WORD unit=WORD
	  { $l = $label.text; $v=$value.text; $u=$unit.text; }
	;

trace returns [Trace t]
scope {
	TraceState tst;
}
@init {
	$t = new Trace();
}
	: n1=WORD {ParserUtil.checkNat(input, $n1.text);} c1=clauses 
	  {$trace::tst = new TraceState(Integer.parseInt($n1.text));
	   $trace::tst.setClauses($c1.c);
	   $t.add($trace::tst);} 
	  (r1=rules n2=WORD {ParserUtil.checkNat(input, $n2.text);} c2=clauses
	  {$trace::tst.setRules($r1.r);
	   $trace::tst = new TraceState(Integer.parseInt($n2.text));
	   $trace::tst.setClauses($c2.c);
	   $t.add($trace::tst);
	  }
	  )* 
	;
	
rules returns [Rules r]
@init {
	$r = new Rules();
}
	: 'RULES:' 
	  (ms=macrostep {$r.add($ms.ms);}|
	   '{' ms1=macrostep {$r.add($ms1.ms);} (',' ms2=macrostep {$r.add($ms2.ms);})+ '}'
	   )
	;

macrostep returns [MacroStep ms]
@init {
	$ms = new MacroStep();
}
	: t=term {$ms.add($t.iterm);}
	| '[' t1=term {$ms.add($t1.iterm);} (',' t2=term {$ms.add($t2.iterm);})+ ']'
	;

clauses returns [Clauses c]
@init {
	$c = new Clauses();
}
	: 'CLAUSES:' '{' (terms {$c.addAll($terms.iterms);})? '}'
	;

terms returns [List<IGroundTerm> iterms]
@init {
	$iterms = new ArrayList<IGroundTerm>();
}
	: t1=term {$iterms.add($t1.iterm);} (',' t2=term {$iterms.add($t2.iterm);})*
	;
	
term returns [IGroundTerm iterm]
scope {
	boolean hasParameters;
	boolean isNat;
}
@init {
	$term::hasParameters = false;
}
	: n=WORD {$term::isNat = ParserUtil.checkNatOrName(input,$n.text);} 
	  ('(' {ParserUtil.checkName(input, $n.text); $term::hasParameters=true;} terms ')')?
	  {
	  	if ($term::hasParameters) {
	  	  $iterm = new GroundFunction(new LocationInfo($n), $n.text, $terms.iterms);	
	  	}
	  	else {
	  	  if ($term::isNat) {
	  	  	$iterm = new GroundNumeral(new LocationInfo($n), Integer.parseInt($n.text));
	  	  }
	  	  else {
	  	    $iterm = new GroundConstant(new LocationInfo($n), $n.text);
	  	  }	
	  	}
	  }
	;

WORD : ~(' ' | '\t' | '\n' | '\r' | '(' | ')' | '{' | '}' | '[' | ']' | ',')+;
WS: (' ' | '\t' | '\n' | '\r')+ {$channel=HIDDEN;} ;
LINE_COMMENT : '%' ~('\n'|'\r')* '\r'? '\n' {$channel=55;} ;
