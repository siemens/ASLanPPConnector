// Copyright 2010-2013 (c) IeAT, Siemens AG, AVANTSSAR and SPaCIoS consortia.
// Licensed under the Apache License, Version 2.0.

grammar aslan;

@parser::header {
  package org.avantssar.aslan;
			
  import java.util.List;
  import java.util.ArrayList;
  import java.io.PrintWriter;
  import java.io.StringWriter;
  import org.avantssar.commons.*;
}

@lexer::header {
  package org.avantssar.aslan;     
  
  import org.avantssar.commons.*;  
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
                     ASLanErrorMessages.DEFAULT.LEXER_ERROR, getErrorMessage(e, tokenNames));
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
	private ISymbolsProvider[] extraDefaults;
	
	public void setExtraSymbolsProviders(ISymbolsProvider[] provs) {
		this.extraDefaults = provs;
	}

	private ErrorGatherer err;

	public void setErrorGatherer(ErrorGatherer eg) {
		err = eg;
	}
	
	//overrides http://www.antlr.org/api/Java/_base_recognizer_8java-source.html#l00189
    public void displayRecognitionError(String[] tokenNames, RecognitionException e)
    {
		err.addError(new LocationInfo(e),
                     ASLanErrorMessages.DEFAULT.PARSER_ERROR, getErrorMessage(e, tokenNames));
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
	    else if (e instanceof ASLanSyntaxErrorException) {
	    	ASLanSyntaxErrorException asee = (ASLanSyntaxErrorException)e;
	    	msg = asee.getMessage();
	    }
	    if (msg == null)
	    	msg = super.getErrorMessage(e, tokenNames);
	    return msg;
	}
	
	private void gatherComments(Token currentToken, IRepresentable repr) {
		List<String> comments = new ArrayList<String>();
		int i = currentToken.getTokenIndex();
		boolean over = false;
		do {
			i--;
			if (i >= 0) {
				Token tok = input.get(i);
				if (tok.getChannel() == 55) {
					comments.add(0, tok.getText().trim().substring(1));
				}
				else if (tok.getChannel() != 91) {
					over = true;
				}
			}
			else {
				over = true;
			}
		}
		while (!over);
		for (String s : comments) {
			repr.addCommentLine(s);
		}
	}
	
}

aslanSpecification returns [IASLanSpec aslanSpec]
scope {
  IASLanSpec spec;
}
@init {
  $aslanSpecification::spec = ASLanSpecificationBuilder.instance().createASLanSpecification(err, extraDefaults);
}
@after {
  $aslanSpec = $aslanSpecification::spec;
}
  : signatureSection
  	typesSection
  	equationsSection? 
  	initsSection
  	hornClausesSection
  	rulesSection
  	goalsSection
  ;

signatureSection
  :
  SECTION 
  {
    if ($aslanSpecification.size() > 0)
    {
      gatherComments($SECTION, $aslanSpecification::spec);
    }
  }
  'signature' ':' (superType | functionDecl)*
  ;

superType
  :
  pt1=primitiveType '>' pt2=primitiveType 
  {
    if ($pt1.itype != null && $pt2.itype != null)
      $pt2.itype.setSuperType($pt1.itype);
  }
  ;

primitiveType returns [PrimitiveType itype]
  :
  LOWERNAME 
  {
    if ($aslanSpecification.size() > 0) {
      $itype = $aslanSpecification::spec.primitiveType(new LocationInfo($LOWERNAME), $LOWERNAME.text);
    }
  }
  ;

functionDecl
scope {
  Map<LocationInfo, String> funcs;
}
@init {
  $functionDecl::funcs = new HashMap<LocationInfo, String>();
}
  :
  f1=LOWERNAME {$functionDecl::funcs.put(new LocationInfo($f1), $f1.text);} 
  (',' f2=LOWERNAME {$functionDecl::funcs.put(new LocationInfo($f2), $f2.text);})* 
  ':' typeStar '->' type 
  {
    if ($aslanSpecification.size() > 0) {
      for (LocationInfo loc : $functionDecl::funcs.keySet()) {
        String f = $functionDecl::funcs.get(loc);
        Function fnc = $aslanSpecification::spec.function(loc, f, $type.itype, $typeStar.itypes.toArray(new IType[$typeStar.itypes.size()]));
		gatherComments($f1, fnc);
      }
    }
  }
  ;

typeStar returns [List<IType> itypes]
@init {
  $itypes = new ArrayList<IType>();
}
  :
  t1=type { $itypes.add($t1.itype); } ('*' t2=type {$itypes.add($t2.itype);})*
  ;


types returns [List<IType> itypes]
@init {
  $itypes = new ArrayList<IType>();
}
  :
  t1=type { $itypes.add($t1.itype); } (',' t2=type {$itypes.add($t2.itype);})*
  ;

type returns [IType itype]
  :
  primitiveType 
  {
    $itype = $primitiveType.itype;
  }
  | LOWERNAME '(' types ')' 
    { 
    if ($aslanSpecification.size() > 0) {
      if ("set".equals($LOWERNAME.text)) {
        if ($types.itypes.size() != 1) {
          err.addException(new LocationInfo($LOWERNAME), ASLanErrorMessages.WRONG_NUMBER_OF_PARAMETERS, "Type", "set", 1, $types.itypes.size()); 
        }
        else {
          $itype = $aslanSpecification::spec.setType(new LocationInfo($LOWERNAME), $types.itypes.get(0));
        }
      }
      else if ("pair".equals($LOWERNAME.text)) {
        if ($types.itypes.size() != 2) {
          err.addException(new LocationInfo($LOWERNAME), ASLanErrorMessages.WRONG_NUMBER_OF_PARAMETERS, "Type", "pair", 2, $types.itypes.size()); 
        }
        else {
          $itype = $aslanSpecification::spec.pairType(new LocationInfo($LOWERNAME), $types.itypes.get(0), $types.itypes.get(1));
        }
      }
      else {
        $itype = $aslanSpecification::spec.compoundType(new LocationInfo($LOWERNAME), $LOWERNAME.text, $types.itypes.toArray(new IType[$types.itypes.size()]));
      }
    }
  }
  ;

typesSection
  :
  SECTION 'types' ':' varConstDecl*
  ;

varConstDecl
scope {
  Map<LocationInfo, String> consts;
  Map<LocationInfo, String> vars;
}
@init {
  $varConstDecl::consts = new HashMap<LocationInfo, String>();
  $varConstDecl::vars = new HashMap<LocationInfo, String>();
}
  :
  (
    c1=LOWERNAME { $varConstDecl::consts.put(new LocationInfo($c1), $c1.text);}
    | v1=UPPERNAME { $varConstDecl::vars.put(new LocationInfo($v1), $v1.text);}
  )
  (
    ','
    (
      c2=LOWERNAME { $varConstDecl::consts.put(new LocationInfo($c2), $c2.text);}
      | v2=UPPERNAME { $varConstDecl::vars.put(new LocationInfo($v2), $v2.text);}
    )
  )*
  ':' type 
  {
    if ($aslanSpecification.size() > 0) {
      for (LocationInfo loc : $varConstDecl::consts.keySet()) {
        String c = $varConstDecl::consts.get(loc);
        if (c != null) {
          Constant cnst = $aslanSpecification::spec.constant(loc, c, $type.itype);
		  gatherComments($c1, cnst);
        }
      }
      for (LocationInfo loc : $varConstDecl::vars.keySet()) {
        String v = $varConstDecl::vars.get(loc);
        if (v != null) {
          Variable var = $aslanSpecification::spec.variable(loc, v, $type.itype);
          gatherComments($v1, var);
        }
      }
    }
  }
  ;

equationsSection
  :
  SECTION 'equations' ':' equation*
  ;

equation
  :
  t1=term e='=' t2=term 
  {
    if ($aslanSpecification.size() > 0) {
      $aslanSpecification::spec.equation(new LocationInfo($e), $t1.iterm, $t2.iterm);
    }
  }
  ;

initsSection
  :
  SECTION 'inits' ':' initialState+
  ;

initialState
scope {
	InitialState init;
}
  : l='initial_state' LOWERNAME
   {
    if ($aslanSpecification.size() > 0) {
      $initialState::init = $aslanSpecification::spec.initialState(new LocationInfo($l), $LOWERNAME.text);
      gatherComments($l, $initialState::init);
    }
   } 
   ':=' facts 
   {
    if ($aslanSpecification.size() > 0) {
      for (ITerm t : $facts.ifacts)
        if (t != null)      
          $initialState::init.addFact(t);
    }
   } 
  ;

hornClausesSection
  :
  SECTION 'hornClauses' ':' hornClause*
  ;

hornClause
scope {
	HornClause hc;
	boolean hasVars;
}
@init {
	$hornClause::hasVars = false;
}
  :
  l='hc' LOWERNAME ('(' vars ')' {$hornClause::hasVars=true;})? 
  ':=' f1=term 
  {
    if ($aslanSpecification.size() > 0) 
    {
      $hornClause::hc = $aslanSpecification::spec.hornClause(new LocationInfo($l), $LOWERNAME.text, $f1.iterm);
      gatherComments($l, $hornClause::hc);
      if ($hornClause::hasVars) {
        for (Variable v : $vars.vars)
        	$hornClause::hc.addParameter(v);
      }
    }  
  } 
  (':-' 
   f2=hcBody
    {
      if ($aslanSpecification.size() > 0) 
      {
        $hornClause::hc.addBodyFact($f2.ifact);
      }  
    } 
   (',' f3=hcBody 
    {
      if ($aslanSpecification.size() > 0) 
      {
        $hornClause::hc.addBodyFact($f3.ifact);
      }  
    }
   )*
  )?
  ;

rulesSection
  :
  SECTION 'rules' ':' rule*
  ;

rule
scope {
	RewriteRule r;
}
  :
  s='step' LOWERNAME 
  { 
    if ($aslanSpecification.size() > 0) 
    {
      $rule::r = $aslanSpecification::spec.rule(new LocationInfo($s), $LOWERNAME.text);
      gatherComments($s, $rule::r);
    }
  }
  ('(' vars ')'
  {
    if ($aslanSpecification.size() > 0) 
    {
      for (Variable v : $vars.vars)
        $rule::r.addParameter(v);
    }
  }
  )? 
  ':=' 
  pNFacts 
  {
    if ($aslanSpecification.size() > 0) 
    {
      for (ITerm t : $pNFacts.ifacts)
        if (t != null)
          $rule::r.addLHS(t);
    }
  } 
  (conditions
  {
    if ($aslanSpecification.size() > 0) 
    {
      for (ITerm t : $conditions.iconds)
        if (t != null)
          $rule::r.addLHS(t);
    }
  }
  )?
  (existsVars
  {
    if ($aslanSpecification.size() > 0) 
    {
      for (Variable v : $existsVars.vars)
        $rule::r.addExists(v);
    }
  }
  )? 
  '=>' 
  facts
  {
    if ($aslanSpecification.size() > 0) 
    {
      for (ITerm t : $facts.ifacts)
        if (t != null)
          $rule::r.addRHS(t);        
    }
  } 
  ;

existsVars returns [List<Variable> vars]
  :
  '=[' EXISTS vars ']' {$vars = $vars.vars;}
  ;

goalsSection
  :
  SECTION 'goals' ':' goal*
  ;

goal
  :
  lTLGoal
  | attackState
  ;

lTLGoal
scope {
	Goal g;
	boolean hasVars;
}
@init {
	$lTLGoal::hasVars = false;
}
  :
  gl='goal' LOWERNAME ('(' vars ')' {$lTLGoal::hasVars = true;})? ':=' formula 
  {
    if ($aslanSpecification.size() > 0) {
      $lTLGoal::g = $aslanSpecification::spec.goal(new LocationInfo($gl), $LOWERNAME.text, $formula.iterm);
      if ($lTLGoal::hasVars) {
        for (Variable v : $vars.vars)
      	  $lTLGoal::g.addParameter(v);
      }
      gatherComments($gl, $lTLGoal::g);
    }
  }
  ;

attackState
scope {
  AttackState as;
}
  :
  a='attack_state' LOWERNAME 
  {
    if ($aslanSpecification.size() > 0) {
      $attackState::as = $aslanSpecification::spec.attackState(new LocationInfo($a), $LOWERNAME.text);
      gatherComments($a, $attackState::as);
    }
  }
  ('(' vars ')'
  {
    if ($aslanSpecification.size() > 0) {
	  for (Variable v : $vars.vars)
        $attackState::as.addParameter(v);
    }
  }
  )? 
  ':=' 
  pNFacts 
  {
    if ($aslanSpecification.size() > 0) {
      for (ITerm t : $pNFacts.ifacts)
        if (t != null)
          $attackState::as.addTerm(t);
    }
  }
  (conditions
  {
    if ($aslanSpecification.size() > 0) {
      for (ITerm t : $conditions.iconds)
        if (t != null)
          $attackState::as.addTerm(t);
    }
  }
  )? 
  ;

pNFacts returns [List<ITerm> ifacts]
@init {
	$ifacts = new ArrayList<ITerm>();
}
  :
  f1=pNFact {$ifacts.add($f1.ifact);} (('.')=>'.' f2=pNFact {$ifacts.add($f2.ifact);})*
  ;

pNFact returns [ITerm ifact]
  :
  f1=fact {$ifact = $f1.ifact;}
  | NOT '(' f2=fact ')' 
  {
    if ($f2.ifact != null)
      $ifact = $f2.ifact.negate();
  }
  ;

facts returns [List<ITerm> ifacts]
@init {
	$ifacts = new ArrayList<ITerm>();
}
  :
  f1=fact {$ifacts.add($f1.ifact);} ('.' f2=fact {$ifacts.add($f2.ifact);})*
  ;

fact returns [ITerm ifact]
  :
  term {$ifact = $term.iterm;}
  // make sure it is positive
  ;

conditions returns [List<ITerm> iconds]
@init {
  $iconds = new ArrayList<ITerm>();
}
  :
  ('&' condition {$iconds.add($condition.icond);})+
  ;

condition returns [ITerm icond] 
  :
	atomicCondition {$icond = $atomicCondition.icond;}
	| NOT '(' atomicCondition ')' 
	{
	  if ($atomicCondition.icond != null)
	    $icond = $atomicCondition.icond.negate(new LocationInfo($NOT));
	}
  ;
  
atomicCondition returns [ITerm icond]
  :
    EQUAL '(' t1=term ',' t2=term ')'
  {
    if ($aslanSpecification.size() > 0) {
      $icond = IASLanSpec.EQUAL.term(new LocationInfo($EQUAL), $t1.iterm, $t2.iterm);
    } 
  }
  | leq='leq' '(' t1 = term ',' t2 = term ')'
  {
    if ($aslanSpecification.size() > 0) {
      $icond = IASLanSpec.LEQ.term(new LocationInfo($leq), $t1.iterm, $t2.iterm);
    } 
  }
  ;

hcBody returns [ITerm ifact]
  : pNFact { $ifact = $pNFact.ifact; }
  | condition { $ifact = $condition.icond; } 
  ;

terms returns [List<ITerm> iterms]
@init {
  $iterms = new ArrayList<ITerm>();
}
  :
  t1=term {$iterms.add($t1.iterm);} (',' t2=term {$iterms.add($t2.iterm);})*
  ;

term returns [ITerm iterm]
  :
  variable { $iterm = $variable.var;  }
  | function {$iterm = $function.fnc;} 
  | constant {$iterm = $constant.cnst;}
  | numericConstant {$iterm = $numericConstant.num;}
  ;

variable returns [VariableTerm var]
  : UPPERNAME
  {
    if ($aslanSpecification.size() > 0) {
      Variable v = $aslanSpecification::spec.findVariable($UPPERNAME.text); 
      if (v == null) {
        err.addException(new LocationInfo($UPPERNAME), ASLanErrorMessages.UNDEFINED_SYMBOL, "variable", $UPPERNAME.text); 
      }
      $var = v.term(new LocationInfo($UPPERNAME)); 
    }
  }
  ;

function returns [FunctionTerm fnc]
  : n=LOWERNAME '(' terms ')'
  {
    if ($aslanSpecification.size() > 0) {
      Function f = $aslanSpecification::spec.findFunction($n.text); 
      if (f == null) {
        err.addException(new LocationInfo($n), ASLanErrorMessages.UNDEFINED_SYMBOL, "function", $n.text); 
      }
      $fnc = f.term(new LocationInfo($n), $terms.iterms.toArray(new ITerm[$terms.iterms.size()])); 
    }
  }
  ;

constant returns [ITerm cnst]
  : LOWERNAME 
  {
    if ($aslanSpecification.size() > 0) {
      Constant c = $aslanSpecification::spec.findConstant($LOWERNAME.text); 
      if (c == null) {
        Function f = $aslanSpecification::spec.findFunction($LOWERNAME.text); 
        if (f == null) {
          err.addException(new LocationInfo($LOWERNAME), ASLanErrorMessages.UNDEFINED_SYMBOL, "constant/function", $LOWERNAME.text); 
        }
        $cnst = f.constantTerm(new LocationInfo($LOWERNAME));
      }
      else {
        $cnst = c.term(new LocationInfo($LOWERNAME));
      }
    }
  }
  ;

numericConstant returns [ITerm num]
  :
  NUMERAL 
  {
    if ($aslanSpecification.size() > 0) {
      try {
      	int n = Integer.parseInt($NUMERAL.text);
        $num = $aslanSpecification::spec.numericTerm(new LocationInfo($NUMERAL), n);   	
      }
      catch (NumberFormatException e) {
        err.addException(new LocationInfo($NUMERAL), ASLanErrorMessages.INVALID_NUMERIC_CONSTANT, $NUMERAL.text); 
      }
    }
  }
  ;

// LTL included here. Also 'and' 'or' and 'implies'  
formula returns [ITerm iterm]
scope {
  List<ITerm> terms;
}
@init {
  $formula::terms = new ArrayList<ITerm>();
}
  :
  fact {$iterm = $fact.ifact;}
  | EQUAL '(' t1=term ',' t2=term ')'
  {
    if ($aslanSpecification.size() > 0) {
      $iterm = IASLanSpec.EQUAL.term(new LocationInfo($EQUAL), $t1.iterm, $t2.iterm);
    } 
  }
  | NOT '(' f=formula ')' 
  {
    if ($f.iterm != null)
      $iterm = $f.iterm.negate(new LocationInfo($NOT));
  }
  | and='and' '(' f1=formula ',' f2=formula ')'
  {
    if ($aslanSpecification.size() > 0) {
      $iterm = IASLanSpec.AND.term(new LocationInfo($and), $f1.iterm, $f2.iterm);
    } 
  }
  | or='or' '(' f1=formula ',' f2=formula ')'
  {
    if ($aslanSpecification.size() > 0) {
      $iterm = IASLanSpec.OR.term(new LocationInfo($or), $f1.iterm, $f2.iterm);
    } 
  }
  | impl='implies' '(' f1=formula ',' f2=formula ')'
  {
    if ($aslanSpecification.size() > 0) {
      $iterm = IASLanSpec.IMPLIES.term(new LocationInfo($impl), $f1.iterm, $f2.iterm);
    } 
  }
  | fa='forall' v=variable '.' f=formula 
  {
    if ($f.iterm != null)
      $iterm = $f.iterm.forall(new LocationInfo($fa), $v.var.getSymbol());
  }
  | EXISTS v=variable '.' f=formula 
  {
    if ($f.iterm != null)
      $iterm = $f.iterm.exists(new LocationInfo($EXISTS), $v.var.getSymbol());
  }
  | n=UPPERNAME '(' f1=formula {$formula::terms.add($f1.iterm);} (',' f2=formula {$formula::terms.add($f2.iterm);})* ')'
  {
    if ($aslanSpecification.size() > 0) {
      Function fnc = $aslanSpecification::spec.findFunction($n.text); 
      if (fnc == null) {
        err.addException(new LocationInfo($n), ASLanErrorMessages.UNDEFINED_SYMBOL, "LTL operator", $n.text); 
      }
      $iterm = fnc.term(new LocationInfo($UPPERNAME), $formula::terms.toArray(new ITerm[$formula::terms.size()])); 
    }
  }
  ;


vars returns [List<Variable> vars]
@init {
  $vars = new ArrayList<Variable>(); 
}
  :
  v1=variable 
  {if ($v1.var != null) $vars.add($v1.var.getSymbol());} 
  (',' v2=variable 
    {if ($v2.var != null) $vars.add($v2.var.getSymbol());}
  )*
  ;

SECTION : 'section';
EXISTS  : 'exists';
NOT : 'not';
EQUAL : 'equal';

/*  

// neXt | Yesterday | Finally | Once | Globally | Historically
LTLOp1    : 'X' | 'Y' | 'F' | 'O' | 'G' | 'H'  
    ; 
// Until | Release | Since
LTLOp2    : 'U' | 'R' | 'S'
    ;     
*/

UPPERNAME  : UPPERLETTER ALPHANUM*;
LOWERNAME  : LOWERLETTER ALPHANUM*; 
NUMERAL    : ZERO_DIGIT | NON_ZERO_DIGIT DIGIT*;
fragment UPPERLETTER : 'A'..'Z' ;
fragment LOWERLETTER : 'a'..'z' ;
fragment ZERO_DIGIT  : '0' ;
fragment NON_ZERO_DIGIT : '1'..'9' ;
fragment DIGIT      : ZERO_DIGIT | NON_ZERO_DIGIT ;
fragment ALPHANUM : UPPERLETTER | LOWERLETTER | DIGIT | '_' ;
WS: (' ' | '\t' | '\n' | '\r')+ {$channel=91;} ;
LINE_COMMENT : '%' ~('\n'|'\r')* '\r'? '\n' {$channel=55;} ;
