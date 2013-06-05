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

tree grammar ToASLanNew;

options {
	tokenVocab=ASLanPPNew;
	ASTLabelType=CommonTree;
	output=template;
}

@header {
    package org.avantssar.aslanpp;

	import java.util.Map;
	import java.util.LinkedHashMap;
    
	import org.avantssar.commons.*;
    import org.avantssar.aslanpp.model.*;
    import org.avantssar.aslanpp.parser.*;
    import org.avantssar.aslanpp.parser.RawLogicalExpression.LogicalOperator;
}

@members {
	ErrorGatherer err;
	
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
}

entity[IScope scope]
scope { 
	Entity e; 
}
@init {
	err = $scope.getErrorGatherer();
}
	: ^(ENTITY UPPERNAME {$entity::e = $scope.findEntity($UPPERNAME.text);} 
			(^(ARGUMENTS .*))? declaration[$entity::e]* entity[$entity::e]* body[$entity::e]? (^(CONSTRAINTS constraints[$entity::e]+))? (^(GOALS goals[$entity::e]+))?)
	; 
	
declaration[Entity e]
	: ^(SYMBOLS .*)
	| ^(MACROS macro[$e]+)
	| ^(HORN_CLAUSES hornClause[$e]+)
	| ^(EQUATIONS equation[$e]+)
	;

macro[Entity e]
scope {
	MacroSymbol m;
}
	: ^(MACRO LOWERNAME {$macro::m = $e.findMacro($LOWERNAME.text);} (^(ARGS .*))?
		  expression[$macro::m] {$macro::m.setBody($expression.iexpression.getTerm(false, true));} 
	   )
	;
	
hornClause[Entity e]
scope {
	HornClause hc;
}
	: ^(HORN_CLAUSE LOWERNAME {$hornClause::hc = $e.hornClause(new LocationInfo($LOWERNAME.token), $LOWERNAME.text);} 
		 (^(ARGS (a=UPPERNAME {$hornClause::hc.argument($a.text, new LocationInfo($a));})+))? 
		 (^(FORALL (f=UPPERNAME {$hornClause::hc.universallyQuantified($f.text, new LocationInfo($f));})+))? 
		 ^(HEAD lhs=expression[$hornClause::hc] {$hornClause::hc.setHead($lhs.iexpression.getTerm(false, true));}) 
		 ^(BODY ((rhs=expression[$hornClause::hc] 
		          {if ($rhs.iexpression.isComparison())
		          	$hornClause::hc.addEquality($rhs.iexpression.getComparison());
		           else 
		            $hornClause::hc.addBody($rhs.iexpression.getTerm(false, true));
		          }
		        )+)?)
	   ) 
	;
	
equation[Entity e]
scope {
	Equation eq;
}
	: ^(EQUATION {$equation::eq = $e.equation(new LocationInfo($EQUATION));}
			lhs=expression[$equation::eq] {$equation::eq.setLeftTerm($lhs.iexpression.getTerm(false, false));}
			rhs=expression[$equation::eq] {$equation::eq.setRightTerm($rhs.iexpression.getTerm(false, false));}
		)
	;
	
body[Entity e]
	: ^(BODY (^(BREAKPOINTS (LOWERNAME {$e.addBreakpoints($LOWERNAME.text);})+)|UNCOMPRESSED {$e.setUncompressed(true);})? stmt[$e])
		{$e.body($stmt.s);}
	;
	
constraints[Entity e]
scope {
	Constraint c;
}
	: ^(CONSTRAINT (n=LOWERNAME|n=UPPERNAME) {$constraints::c = $e.constraint(new LocationInfo($n), $n.text);} 
			expression[$constraints::c] {$constraints::c.setFormula($expression.iexpression.getFormula());}
	   )
	;

goals[Entity e]
scope {
	InvariantGoal g;
}
	: ^(INVARIANT_GOAL (n=LOWERNAME|n=UPPERNAME) {$goals::g = $e.invariantGoal(new LocationInfo($n), $n.text);} 
			expression[$goals::g] {$goals::g.setFormula($expression.iexpression.getFormula());}
	   )
	| ^(SESSION_CHANNEL_GOAL (n=LOWERNAME|n=UPPERNAME) expression[$e])
	  {
	    RawChannelGoalInfo chgi = $expression.iexpression.getChannelGoal();
	    $e.sessionChannelGoal(new LocationInfo($n), $n.text, chgi.sender.getTerm(false, true), chgi.receiver.getTerm(false, true), chgi.type);
	  }
	| ^(SESSION_SECRECY_GOAL (n=LOWERNAME|n=UPPERNAME) expression[$e])
	  {
	    $e.sessionSecrecyGoal(new LocationInfo($n), $n.text, $expression.iexpression.getTerm(false, true));
	  }
	;
	
vardecl returns [String name]
	: UPPERNAME {$name = $UPPERNAME.text;}
	;

var[IScope scope, boolean relaxed] returns [VariableSymbol sym]
	: v=UPPERNAME
	{
		$sym = $scope.findVariable($v.text);
		if ($sym == null)
		{ 
			if ($relaxed)
				$sym = $scope.addUntypedVariable($v.text, new LocationInfo($v));
			else
       			err.addException(new LocationInfo($v), ErrorMessages.UNDEFINED_VARIABLE, $v.text, $scope.getName());
		}
	} 
	;

	
expression[IScope e] returns [IRawExpression iexpression]
scope { 
	LinkedHashMap<String, LocationInfo> allSymbols;
	List<IRawExpression> allExpressions;
	boolean isExists;
	boolean hasRight;
}
@init { 
	$expression::allSymbols = new LinkedHashMap<String, LocationInfo>();
	$expression::allExpressions = new ArrayList<IRawExpression>(); 
}
	: ^(IMPLICATION left=expression[$e] right=expression[$e])
	  {
	    // switch associativity if needed
	    if ($left.iexpression instanceof RawLogicalExpression)
	    {
	      RawLogicalExpression rleft = (RawLogicalExpression)$left.iexpression;
	      $iexpression = new RawLogicalExpression($e, rleft.getLocation(), err, rleft.getLeft(),
	                             new RawLogicalExpression($e, new LocationInfo($IMPLICATION), err, rleft.getRight(), $right.iexpression, LogicalOperator.Implication),
	                             LogicalOperator.Implication);
	    }
	    else
	    { 
	      $iexpression = new RawLogicalExpression($e, new LocationInfo($IMPLICATION), err, $left.iexpression, $right.iexpression, LogicalOperator.Implication);
	    } 
	  } 
	| ^(CONJUNCTION left=expression[$e] right=expression[$e])
	  { $iexpression = new RawLogicalExpression($e, new LocationInfo($CONJUNCTION), err, $left.iexpression, $right.iexpression, LogicalOperator.Conjunction); } 
	| ^(DISJUNCTION left=expression[$e] right=expression[$e]) 
	  { $iexpression = new RawLogicalExpression($e, new LocationInfo($DISJUNCTION), err, $left.iexpression, $right.iexpression, LogicalOperator.Disjunction); }
	| ^(NEGATION ex=expression[$e])
	  { $iexpression = new RawNegatedExpression($e, new LocationInfo($NEGATION), err, $ex.iexpression); } 
	| ^(PAREN ex=expression[$e])
	  { $iexpression = new RawParenthesisExpression($e, new LocationInfo($PAREN), err, $ex.iexpression); } 
	| ^((q=FORALL {$expression::isExists=false;} | q=EXISTS {$expression::isExists=true;}) 
		    ^(VARS (v=vardecl { $expression::allSymbols.put($v.name, new LocationInfo((CommonTree)$v.start)); })+) 
		    ex=expression[$e]) 
	  { $iexpression = new RawQuantifiedExpression($e, new LocationInfo($q), err, !$expression::isExists, $expression::allSymbols, $ex.iexpression); }
	| ^(EQUALITY left=expression[$e] right=expression[$e])
	  { $iexpression = new RawComparisonExpression($e, new LocationInfo($EQUALITY), err, $left.iexpression, $right.iexpression, true); } 
	| ^(INEQUALITY left=expression[$e] right=expression[$e])
	  { $iexpression = new RawComparisonExpression($e, new LocationInfo($INEQUALITY), err, $left.iexpression, $right.iexpression, false); } 
	| ^(TUPLE (ex=expression[$e] { $expression::allExpressions.add($ex.iexpression); })+ )
	  { $iexpression = new RawConcatenatedExpression($e, new LocationInfo($TUPLE), err, $expression::allExpressions, true); } 
	| ^(CONCAT (ex=expression[$e] { $expression::allExpressions.add($ex.iexpression); })+ )
	  { $iexpression = new RawConcatenatedExpression($e, new LocationInfo($CONCAT), err, $expression::allExpressions, false); } 
	| ^(PSEUDONYM left=expression[$e] (right=expression[$e] { $expression::hasRight = true; })?) 
	  {
	  	if ($expression::hasRight)
	  		$iexpression = new RawPseudonymExpression($e, new LocationInfo($PSEUDONYM), err, $left.iexpression, $right.iexpression);
	  	else
	  		$iexpression = new RawPseudonymExpression($e, new LocationInfo($PSEUDONYM), err, $left.iexpression);
	  }
	| ^(SET (ex=expression[$e] { $expression::allExpressions.add($ex.iexpression); })*)
	  { $iexpression = new RawSetExpression($e, new LocationInfo($SET), err, $expression::allExpressions); } 
	| ^(MATCH UPPERNAME) 
	  { $iexpression = new RawMatchedExpression($e, new LocationInfo($MATCH), err, $UPPERNAME.text); } 
	| MATCH 
	  { $iexpression = new RawMatchedExpression($e, new LocationInfo($MATCH), err); } 
	| ^(NUMERIC NUMBER)
	  { $iexpression = new RawNumericExpression($e, new LocationInfo($NUMERIC), err, $NUMBER.text); }
	| ^(FCALL (n=LOWERNAME|n=UPPERNAME|n=LTL_SPECIAL_OP) ^(ARGS (ex=expression[$e] { $expression::allExpressions.add($ex.iexpression); })+))
	  { $iexpression = new RawFunctionExpression($e, new LocationInfo($FCALL), err, $n.text, $expression::allExpressions); }
	| ^(CONST_VAR (n=LOWERNAME|n=UPPERNAME|n=LTL_SPECIAL_OP))
      { $iexpression = new RawConstVarExpression($e, new LocationInfo($CONST_VAR), err, $n.text); }
    | ^(OOP_CALL left=expression[$e] right=expression[$e])
      { $iexpression = new RawOOPCallExpression($e, new LocationInfo($OOP_CALL), err, $left.iexpression, $right.iexpression); }
    | ^(CHANNEL left=expression[$e] tp=. right=expression[$e])
      { $iexpression = new RawChannelExpression($e, new LocationInfo($CHANNEL), err, $left.iexpression, $right.iexpression, $tp.getText(), false); }
    | ^(CHANNEL_NAMED left=expression[$e] tp=. right=expression[$e])
      { $iexpression = new RawChannelExpression($e, new LocationInfo($CHANNEL_NAMED), err, $left.iexpression, $right.iexpression, $tp.getText(), true); }
    | ^(TRANSMISSION_ANN left=expression[$e] right=expression[$e])
      { $iexpression = new RawAnnotatedTransmissionExpression($e, new LocationInfo($TRANSMISSION_ANN), err, $left.iexpression, $right.iexpression); }
    | ^(TRANSMISSION_FNC left=expression[$e] (n=LOWERNAME|n=UPPERNAME))
      { $iexpression = new RawFunctionTransmissionExpression($e, new LocationInfo($TRANSMISSION_FNC), err, $left.iexpression, $n.text); }
    | ^(ANNOTATED (n=LOWERNAME|n=UPPERNAME) ex=expression[$e])
      { $iexpression = new RawAnnotatedExpression($e, new LocationInfo($ANNOTATED), err, $n.text, $ex.iexpression); }
	;

stmts[Entity e, BlockStatement s]
	:
	(stmt[$e] {$s.add($stmt.s);})*
	;
	
stmt[Entity e] returns [IStatement s]
	: assignment[$e] {$s = $assignment.s;}
	| fresh[$e] {$s = $fresh.s;}
	| newEntityInst[$e] {$s = $newEntityInst.s;}
	| symbEntityInst[$e] {$s = $symbEntityInst.s;}
	| ifstmt[$e] {$s = $ifstmt.s;}
	| whilestmt[$e] {$s = $whilestmt.s;}
	| selectstmt[$e] {$s = $selectstmt.s;}
	| introducestmt[$e] {$s = $introducestmt.s;}
	| retractstmt[$e] {$s = $retractstmt.s;}
	| assertstmt[$e] {$s = $assertstmt.s;}
	| secrecygoal[$e] {$s = $secrecygoal.s;}
	| ^(BLOCK {$s = $e.block();} stmts[$e, (BlockStatement)$s])
	;
	
assignment[Entity e] returns [AssignmentStatement s]
	: ^(ASSIGNMENT ^(VAR v=expression[$e]) ^(TERM a=expression[$e]))
	  { $s = $e.assign(new LocationInfo($ASSIGNMENT), $v.iexpression.getTerm(false, true), $a.iexpression.getTerm(false, true)); }
	;
	
fresh[Entity e] returns [FreshStatement s]
	: ^(FRESH ^(VAR v=expression[$e]))
	  { $s = $e.fresh(new LocationInfo($FRESH), $v.iexpression.getTerm(false, true)); } 
	;
	
newEntityInst[Entity e] returns [NewEntityInstanceStatement s] 
scope { 
	List<ITerm> terms;
	Entity newEnt;
}
@init {
	$newEntityInst::terms = new ArrayList<ITerm>();
}
	: ^(NEW 
	    ^(ENTITYINST vardecl
	    {
		$newEntityInst::newEnt = $e.getEntry($vardecl.name, Entity.class);
		if ($newEntityInst::newEnt == null)
			err.addException(new LocationInfo((CommonTree)$vardecl.start), ErrorMessages.CANNOT_FIND_CHILD_ENTITY, $vardecl.name, $e.getOriginalName());
	    } 
	    	(^(ARGS (a=expression[$e] {$newEntityInst::terms.add($a.iexpression.getTerm(false, true));})+))?
	    )
	  )
	  { $s = $e.newInstance(new LocationInfo($NEW), $newEntityInst::newEnt, $newEntityInst::terms.toArray(new ITerm[$newEntityInst::terms.size()])); }
	; 	
	
symbEntityInst[Entity e] returns [SymbolicInstanceStatement s]
scope {
	List<ITerm> terms;
	List<String> symbols;
	Entity newEnt;
}
@init {
	$symbEntityInst::terms = new ArrayList<ITerm>();
	$symbEntityInst::symbols = new ArrayList<String>();
}
	: ^(ANY 
		(^(VARS (symname=vardecl { $symbEntityInst::symbols.add($symname.name); } )+ 
		  )
		)? 
		^(ENTITYINST entname=vardecl 
		  {
			$symbEntityInst::newEnt = $e.getEntry($entname.name, Entity.class);
			if ($symbEntityInst::newEnt == null)
				err.addException(new LocationInfo((CommonTree)$entname.start), ErrorMessages.CANNOT_FIND_CHILD_ENTITY, $entname.name, $e.getOriginalName());
	  	$s = $e.symbolicInstance(new LocationInfo($ANY), $symbEntityInst::newEnt);
	  	for (String s : $symbEntityInst::symbols) $s.any(s);
		  }
			(^(ARGS (a=expression[$s] {$symbEntityInst::terms.add($a.iexpression.getTerm(false, true));})+))?
			{$s.setArgs($symbEntityInst::terms.toArray(new ITerm[$symbEntityInst::terms.size()]));}
		) 
		^(GUARD (ex=expression[$s] { $s.setGuard($ex.iexpression.getGuard(true)); })?)
	   )
	;
	
ifstmt[Entity e] returns [BranchStatement s] 
	: ^(IF ^(GUARD expression[$e]) { $s = $e.branch(new LocationInfo($IF), $expression.iexpression.getGuard(false)); }
		a=stmt[$e] { $s.branchTrue($a.s); }
		(b=stmt[$e] { $s.branchFalse($b.s); })?
	   )
	;	
	
whilestmt[Entity e] returns [LoopStatement s]
	: ^(WHILE ^(GUARD expression[$e]) { $s = $e.loop(new LocationInfo($WHILE), $expression.iexpression.getGuard(false)); } 
			stmt[$e] { $s.body($stmt.s); } 
	  )
	;	
	
selectstmt[Entity e] returns [SelectStatement s]
	: ^(SELECT  { $s = $e.select(new LocationInfo($SELECT)); }
			selectOption[$e, $s]+
		) 
	;	
	
selectOption[Entity e, SelectStatement s]
scope {
	IExpression grd;
}
	:  ^(SELECT_OPTION ^(GUARD expression[$e] {$selectOption::grd = $expression.iexpression.getGuard(true);})
		    ^(CHANNEL_GOALS channelgoal[$e, $selectOption::grd]*) 
		    stmt[$e])
	  {
	    $s.choice($selectOption::grd, $stmt.s);
	  } 
	;
	
introducestmt[Entity e] returns [IntroduceStatement s]
	: ^(INTRODUCE ^(TERM expression[$e]) 
	  { $s = $e.introduce(new LocationInfo($INTRODUCE), $expression.iexpression.getTerm(true, true)); }
	    ^(CHANNEL_GOALS channelgoal[$e, $s]*))
 	;
 	
channelgoal[Entity e, IChannelGoalHolder holder]
	: ^(CHANNEL_GOAL (name=LOWERNAME|name=UPPERNAME) expression[$e])
		{ ChannelGoal cg = $e.chGoal(new LocationInfo($CHANNEL_GOAL), $name.text, $expression.iexpression.getTransmission());
		  $holder.attachChannelGoal(cg);
		}
	;
	 	
retractstmt[Entity e] returns [RetractStatement s] 
	: ^(RETRACT ^(TERM expression[$e])) 
	  { $s = $e.retract(new LocationInfo($RETRACT), $expression.iexpression.getTerm(false, true)); }	
	;
	
assertstmt[Entity e] returns [AssertStatement s]
	: ^(ASSERT (name=LOWERNAME|name=UPPERNAME)
		    { $s = $e.assertion(new LocationInfo($ASSERT), $name.text); }
			^(GUARD expression[$s]) { $s.setGuard($expression.iexpression.getFormula()); }
		)
	;
	
secrecygoal[Entity e] returns [SecrecyGoalStatement s]
scope {
	List<ITerm> knowers;
}
@init {
	$secrecygoal::knowers = new ArrayList<ITerm>();
}
	: ^(SECRECY_GOAL (name=LOWERNAME|name=UPPERNAME) 
			^(KNOWERS (k=expression[$e] {$secrecygoal::knowers.add($k.iexpression.getTerm(false, true));})+)
			^(PAYLOAD payload=expression[$e])
		)
		{ $s = $e.secrecyGoal(new LocationInfo($SECRECY_GOAL), $name.text, $payload.iexpression.getTerm(false, true), $secrecygoal::knowers.toArray(new ITerm[$secrecygoal::knowers.size()])); }
	;
