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

package org.avantssar.aslanpp;

import org.antlr.runtime.tree.Tree;
import org.avantssar.aslanpp.model.IType;
import org.avantssar.aslanpp.visitors.PrettyPrinter;

public class Util {

	@SuppressWarnings("unused")
	private static final String DOT_PROGRAM = "dot";

	public static String lowerFirst(String s) {
		return s.substring(0, 1).toLowerCase() + s.substring(1);
	}

	public static String upperFirst(String s) {
		return s.substring(0, 1).toUpperCase() + s.substring(1);
	}

	public static Tree cloneTree(Tree root) {
		if (root == null) {
			return null;
		}

		Tree result = root.dupNode();
		for (int i = 0; i < root.getChildCount(); i++) {
			Tree child = root.getChild(i);
			result.addChild(cloneTree(child));
		}
		return result;
	}

	public static Tree insertSubject(Tree fcall, Tree subject) {
		Tree result = fcall.dupNode();
		for (int i = 0; i < fcall.getChildCount(); i++) {
			Tree child = fcall.getChild(i);
			if ("ARGS".equals(child.getText())) {
				Tree extendedArgs = child.dupNode();
				extendedArgs.addChild(cloneTree(subject));
				for (int j = 0; j < child.getChildCount(); j++) {
					extendedArgs.addChild(cloneTree(child.getChild(j)));
				}
				result.addChild(extendedArgs);
			}
			else {
				result.addChild(cloneTree(child));
			}
		}
		return result;
	}

	// public static CommonTree createVariableNode(String name) {
	// TreeAdaptor adaptor = new CommonTreeAdaptor();
	// TreeWizard wizard = new TreeWizard(adaptor, ASLanPPParser.tokenNames);
	// CommonTree result = (CommonTree) wizard.create("(VAR UPPERNAME[" + name +
	// "])");
	// // System.out.println("variable node ->" + name + "<-: " +
	// // result.toStringTree());
	// return result;
	// }

	// public static String convertStreamToString(InputStream is) {
	// if (is != null) {
	// StringBuilder sb = new StringBuilder();
	// String line;
	// try {
	// BufferedReader reader = new BufferedReader(new InputStreamReader(is));
	// while ((line = reader.readLine()) != null) {
	// sb.append(line).append("\n");
	// }
	// }
	// catch (IOException ex) {
	// ex.printStackTrace();
	// }
	// return sb.toString();
	// }
	// else {
	// return "";
	// }
	// }

	public static String represent(IType v) {
		PrettyPrinter pp = new PrettyPrinter();
		v.accept(pp);
		return pp.toString();
	}

}
