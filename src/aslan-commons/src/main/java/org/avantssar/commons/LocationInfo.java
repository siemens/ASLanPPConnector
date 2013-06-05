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

import org.antlr.runtime.ClassicToken;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.Token;
import org.antlr.runtime.tree.Tree;

public class LocationInfo {

	public static LocationInfo NOWHERE = new LocationInfo(-1, -1, -1);

	public int line;
	public int col;
	public int next; // column of next token/tree

	public LocationInfo(int line, int col, int next) {
		this.line = line;
		this.col = col;
		this.next = next;
	}

	public LocationInfo(Tree tree) {
		this(tree.getLine(), tree.getCharPositionInLine(),
				tree.getChild(1)!=null ? tree.getChild(1).getCharPositionInLine()
				    /* TODO improve */ : tree.getCharPositionInLine()+0*tree.toString().length());
	}

	public LocationInfo(Token token) {
		this(token.getLine(), token.getCharPositionInLine(),
			 token.getCharPositionInLine()+token.getText().length());
	}

	private static Token fakeTokenForLocation(RecognitionException e) {
		ClassicToken t = new ClassicToken(Token.INVALID_TOKEN_TYPE);
		t.setLine(e.line);
		t.setCharPositionInLine(e.charPositionInLine);
		t.setText(""); // dummy text determining next col; TODO improve
		return t;
	}
	public LocationInfo(RecognitionException e) {
		this(e.token != null ? e.token : fakeTokenForLocation(e));
	}
	
	 @Override
	public boolean equals(Object other) {
		if (other instanceof LocationInfo) {
			LocationInfo oloc = (LocationInfo) other;
			return this.line == oloc.line && this.col == oloc.col;
			 	                       // && this.next == oloc.next;
		}
		return false;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		if (line >= 0) {
			sb.append("line ").append(line);
			if (col >= 0) {
				sb.append(", column ").append(col);
			//	sb.append(", next ").append(next);
			}
		}
		else {
			sb.append("unknown location");
		}
		return sb.toString();
	}
}
