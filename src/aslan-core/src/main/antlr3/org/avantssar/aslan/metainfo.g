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

grammar metainfo;

@parser::header {
  package org.avantssar.aslan;
  
  import java.util.List;
  import java.util.ArrayList;
  import java.util.Map;
  import java.util.LinkedHashMap;
}

@lexer::header {
  package org.avantssar.aslan;
}

@parser::members {
	public boolean wasAnyError = false;
	public List<String> allErrors = new ArrayList<String>();
	
	public void emitErrorMessage(String msg) {
	 wasAnyError = true;
	 allErrors.add(msg);
	}	
}

@lexer::members {
	public boolean wasAnyError = false;
	public List<String> allErrors = new ArrayList<String>();
	
	public void emitErrorMessage(String msg) {
	 wasAnyError = true;
	 allErrors.add(msg);
	}	
}

metainfo returns [String name, List<String> flags, Map<String, String> parameters]
@init {
  $flags = new ArrayList<String>();
  $parameters = new LinkedHashMap<String, String>();
}
  : (' '|'\t')* '@' VALUE {$name=$VALUE.text;} 
    ('(' 
     p1=parameter 
     { 
       if ($p1.value == null)
         $flags.add($p1.key);
       else
         $parameters.put($p1.key.trim(), $p1.value);
     } 
     (';' p2=parameter
     { 
       if ($p2.value == null)
         $flags.add($p2.key);
       else
         $parameters.put($p2.key.trim(), $p2.value);
     } 
     )* 
     ')'
    )?
    (' '|'\t')* 
    EOF
  ;
  
parameter returns [String key, String value]
  : k=VALUE {$key=$k.text;}
    ('=' v=term {$value=$v.text;})?
  ;
  
terms
  : term (',' term)*
  ;
  
term
  : VALUE ('(' terms ')')?
  ;
  
VALUE : ~('\n' | '\r' | '@' | '=' | '(' | ')' | ',' | ';')+;
WS: ('\n' | '\r')+ {$channel=HIDDEN;} ;
