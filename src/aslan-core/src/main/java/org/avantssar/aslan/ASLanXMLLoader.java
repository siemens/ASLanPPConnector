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

import java.util.ArrayList;
import java.util.List;
import org.avantssar.commons.ErrorGatherer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ASLanXMLLoader {

	private final ErrorGatherer err;

	public ASLanXMLLoader(ErrorGatherer err) {
		this.err = err;
	}

	public void loadFromXML(IASLanSpec spec, Document doc) {
		parseSpec(doc.getDocumentElement(), spec);
	}

	private void parseSpec(Element el, IASLanSpec spec) {
		expect(el, ASLanXMLSerializer.ASLAN);

		parseComments(el, spec);

		Element signature = getFirstChildByName(el, ASLanXMLSerializer.SIGNATURE);
		if (signature != null) {
			NodeList all = signature.getChildNodes();
			for (int i = 0; i < all.getLength(); i++) {
				if (all.item(i) instanceof Element) {
					Element child = (Element) all.item(i);
					if (child.getNodeName().equals(ASLanXMLSerializer.TYPE)) {
						parseSuperType(child, spec);
					}
					else if (child.getNodeName().equals(ASLanXMLSerializer.FUNCTION)) {
						parseFunction(child, spec);
					}
					else {
						err.addException(ASLanErrorMessages.INVALID_TAG, child.getNodeName(), ASLanXMLSerializer.SIGNATURE);
					}
				}
			}
		}

		Element types = getFirstChildByName(el, ASLanXMLSerializer.TYPES);
		if (types != null) {
			NodeList all = types.getChildNodes();
			for (int i = 0; i < all.getLength(); i++) {
				if (all.item(i) instanceof Element) {
					Element child = (Element) all.item(i);
					if (child.getNodeName().equals(ASLanXMLSerializer.VARIABLE)) {
						parseVariable(child, spec);
					}
					else if (child.getNodeName().equals(ASLanXMLSerializer.CONSTANT)) {
						parseConstant(child, spec);
					}
					else {
						err.addException(ASLanErrorMessages.INVALID_TAG, child.getNodeName(), ASLanXMLSerializer.TYPES);
					}
				}
			}
		}

		Element equations = getFirstChildByName(el, ASLanXMLSerializer.EQUATIONS);
		if (equations != null) {
			NodeList all = equations.getChildNodes();
			for (int i = 0; i < all.getLength(); i++) {
				if (all.item(i) instanceof Element) {
					Element child = (Element) all.item(i);
					if (child.getNodeName().equals(ASLanXMLSerializer.EQUATION)) {
						parseEquation(child, spec);
					}
					else {
						err.addException(ASLanErrorMessages.INVALID_TAG, child.getNodeName(), ASLanXMLSerializer.EQUATIONS);
					}
				}
			}
		}

		Element inits = getFirstChildByName(el, ASLanXMLSerializer.INITS);
		if (inits != null) {
			NodeList all = inits.getChildNodes();
			for (int i = 0; i < all.getLength(); i++) {
				if (all.item(i) instanceof Element) {
					Element child = (Element) all.item(i);
					if (child.getNodeName().equals(ASLanXMLSerializer.INITIAL_STATE)) {
						parseInitialState(child, spec);
					}
					else {
						err.addException(ASLanErrorMessages.INVALID_TAG, child.getNodeName(), ASLanXMLSerializer.INITS);
					}
				}
			}
		}

		Element hornClauses = getFirstChildByName(el, ASLanXMLSerializer.HORN_CLAUSES);
		if (hornClauses != null) {
			NodeList all = hornClauses.getChildNodes();
			for (int i = 0; i < all.getLength(); i++) {
				if (all.item(i) instanceof Element) {
					Element child = (Element) all.item(i);
					if (child.getNodeName().equals(ASLanXMLSerializer.HORN_CLAUSE)) {
						parseHornClause(child, spec);
					}
					else {
						err.addException(ASLanErrorMessages.INVALID_TAG, child.getNodeName(), ASLanXMLSerializer.HORN_CLAUSES);
					}
				}
			}
		}

		Element rules = getFirstChildByName(el, ASLanXMLSerializer.RULES);
		if (rules != null) {
			NodeList all = rules.getChildNodes();
			for (int i = 0; i < all.getLength(); i++) {
				if (all.item(i) instanceof Element) {
					Element child = (Element) all.item(i);
					if (child.getNodeName().equals(ASLanXMLSerializer.STEP)) {
						parseRule(child, spec);
					}
					else {
						err.addException(ASLanErrorMessages.INVALID_TAG, child.getNodeName(), ASLanXMLSerializer.RULES);
					}
				}
			}
		}

		Element goals = getFirstChildByName(el, ASLanXMLSerializer.GOALS);
		if (goals != null) {
			NodeList all = goals.getChildNodes();
			for (int i = 0; i < all.getLength(); i++) {
				if (all.item(i) instanceof Element) {
					Element child = (Element) all.item(i);
					if (child.getNodeName().equals(ASLanXMLSerializer.ATTACK_STATE)) {
						parseAttackState(child, spec);
					}
					else if (child.getNodeName().equals(ASLanXMLSerializer.GOAL)) {
						parseGoal(child, spec);
					}
					else {
						err.addException(ASLanErrorMessages.INVALID_TAG, child.getNodeName(), ASLanXMLSerializer.GOALS);
					}
				}
			}
		}
	}

	private void parseSuperType(Element el, IASLanSpec spec) {
		expect(el, ASLanXMLSerializer.TYPE);
		String typeName = el.getAttribute(ASLanXMLSerializer.NAME);
		String superTypeName = el.getAttribute(ASLanXMLSerializer.SUPER_TYPE);
		PrimitiveType type = spec.primitiveType(typeName);
		PrimitiveType superType = spec.primitiveType(superTypeName);
		type.setSuperType(superType);
	}

	private Function parseFunction(Element el, IASLanSpec spec) {
		expect(el, ASLanXMLSerializer.FUNCTION);
		String name = el.getAttribute(ASLanXMLSerializer.NAME);
		Element returnTypeEl = getFirstChildByName(el, ASLanXMLSerializer.RETURN_TYPE);
		IType returnType = parseType(getFirstChild(returnTypeEl), spec);
		Element parameterTypesEl = getFirstChildByName(el, ASLanXMLSerializer.PARAMETER_TYPES);
		List<IType> argTypes = new ArrayList<IType>();
		NodeList all = parameterTypesEl.getChildNodes();
		for (int i = 0; i < all.getLength(); i++) {
			if (all.item(i) instanceof Element) {
				argTypes.add(parseType((Element) all.item(i), spec));
			}
		}
		Function fnc = spec.function(name, returnType, argTypes.toArray(new IType[argTypes.size()]));
		parseComments(el, fnc);
		return fnc;
	}

	private Variable parseVariable(Element el, IASLanSpec spec) {
		expect(el, ASLanXMLSerializer.VARIABLE);
		String name = el.getAttribute(ASLanXMLSerializer.NAME);
		IType type = parseType(getFirstNonCommentChild(el), spec);
		Variable var = spec.variable(name, type);
		parseComments(el, var);
		return var;
	}

	private Constant parseConstant(Element el, IASLanSpec spec) {
		expect(el, ASLanXMLSerializer.CONSTANT);
		String name = el.getAttribute(ASLanXMLSerializer.NAME);
		IType type = parseType(getFirstNonCommentChild(el), spec);
		Constant cnst = spec.constant(name, type);
		parseComments(el, cnst);
		return cnst;
	}

	private Element getFirstNonCommentChild(Element el) {
		NodeList all = el.getChildNodes();
		for (int i = 0; i < all.getLength(); i++) {
			Node child = all.item(i);
			if (child instanceof Element) {
				if (!child.getNodeName().equals(ASLanXMLSerializer.COMMENTS)) {
					return (Element) child;
				}
			}
		}
		return null;
	}

	private IType parseType(Element el, IASLanSpec spec) {
		if (el.getNodeName().equals(ASLanXMLSerializer.TYPE)) {
			String typeName = el.getAttribute(ASLanXMLSerializer.NAME);
			return spec.primitiveType(typeName);
		}
		else if (el.getNodeName().equals(ASLanXMLSerializer.PAIR_TYPE)) {
			Element left = getFirstChildByName(el, ASLanXMLSerializer.LEFT);
			Element right = getFirstChildByName(el, ASLanXMLSerializer.RIGHT);
			IType leftType = parseType(getFirstChild(left), spec);
			IType rightType = parseType(getFirstChild(right), spec);
			return spec.pairType(leftType, rightType);
		}
		else if (el.getNodeName().equals(ASLanXMLSerializer.SET_TYPE)) {
			IType baseType = parseType(getFirstChild(el), spec);
			return spec.setType(baseType);
		}
		else if (el.getNodeName().equals(ASLanXMLSerializer.COMPOUND_TYPE)) {
			String typeName = el.getAttribute(ASLanXMLSerializer.NAME);
			List<IType> argTypes = new ArrayList<IType>();
			NodeList all = el.getChildNodes();
			for (int i = 0; i < all.getLength(); i++) {
				if (all.item(i) instanceof Element) {
					argTypes.add(parseType((Element) all.item(i), spec));
				}
			}
			return spec.compoundType(typeName, argTypes.toArray(new IType[argTypes.size()]));
		}
		else {
			err.addException(ASLanErrorMessages.INVALID_TAG_FOR, el.getNodeName(), "type");
			return null;
		}
	}

	private HornClause parseHornClause(Element el, IASLanSpec spec) {
		expect(el, ASLanXMLSerializer.HORN_CLAUSE);
		String name = el.getAttribute(ASLanXMLSerializer.NAME);
		Element headEl = getFirstChildByName(el, ASLanXMLSerializer.HEAD);
		Element bodyEl = getFirstChildByName(el, ASLanXMLSerializer.BODY);
		ITerm head = parseTerm(getFirstChild(headEl), spec);
		HornClause hc = spec.hornClause(name, head);
		parseComments(el, hc);
		Element parsEl = getFirstChildByName(el, ASLanXMLSerializer.PARAMETERS);
		loadParameters(hc, parsEl, spec);
		if (bodyEl != null) {
			NodeList all = bodyEl.getChildNodes();
			for (int i = 0; i < all.getLength(); i++) {
				if (all.item(i) instanceof Element) {
					hc.addBodyFact(parseTerm((Element) all.item(i), spec));
				}
			}
		}
		return hc;
	}

	private void loadParameters(IParameterized owner, Element parsEl, IASLanSpec spec) {
		if (parsEl != null) {
			NodeList all = parsEl.getChildNodes();
			int cnt = 0;
			for (int i = 0; i < all.getLength(); i++) {
				if (all.item(i) instanceof Element) {
					Element currEl = (Element) all.item(i);
					expect(currEl, ASLanXMLSerializer.VARIABLE);
					cnt++;
				}
			}
			List<String> names = new ArrayList<String>(cnt);
			for (int i = 0; i < cnt; i++) {
				names.add("");
			}
			for (int i = 0; i < all.getLength(); i++) {
				if (all.item(i) instanceof Element) {
					Element currEl = (Element) all.item(i);
					String name = currEl.getAttribute(ASLanXMLSerializer.NAME);
					String idx = currEl.getAttribute(ASLanXMLSerializer.POSITION);
					try {
						int pos = Integer.parseInt(idx);
						names.set(pos - 1, name);
					}
					catch (NumberFormatException ex) {
						err.addException(ASLanErrorMessages.INVALID_NUMERIC_CONSTANT, idx);
					}
				}
			}
			for (String s : names) {
				Variable v = spec.findVariable(s);
				owner.addParameter(v);
			}
		}
	}

	private Equation parseEquation(Element el, IASLanSpec spec) {
		expect(el, ASLanXMLSerializer.EQUATION);
		ITerm left = null, right = null;
		Element leftEl = getFirstChildByName(el, ASLanXMLSerializer.LEFT);
		if (leftEl != null) {
			Element leftChild = getFirstChild(leftEl);
			left = parseTerm(leftChild, spec);
		}
		Element rightEl = getFirstChildByName(el, ASLanXMLSerializer.RIGHT);
		if (rightEl != null) {
			Element rightChild = getFirstChild(rightEl);
			right = parseTerm(rightChild, spec);
		}
		if (left != null && right != null) {
			Equation eq = spec.equation(left, right);
			return eq;
		}
		else {
			err.addException(ASLanErrorMessages.TOO_FEW_CHILDREN_UNDER, ASLanXMLSerializer.EQUATION);
			return null;
		}
	}

	private InitialState parseInitialState(Element el, IASLanSpec spec) {
		expect(el, ASLanXMLSerializer.INITIAL_STATE);
		String name = el.getAttribute(ASLanXMLSerializer.NAME);
		InitialState is = spec.initialState(name);
		parseComments(el, is);
		Element factsEl = getFirstChildByName(el, ASLanXMLSerializer.FACTS);
		if (factsEl != null) {
			NodeList all = factsEl.getChildNodes();
			for (int i = 0; i < all.getLength(); i++) {
				if (all.item(i) instanceof Element) {
					if (!((Element) all.item(i)).getNodeName().equals(ASLanXMLSerializer.COMMENTS)) {
						is.addFact(parseTerm((Element) all.item(i), spec));
					}
				}
			}
		}
		return is;
	}

	private RewriteRule parseRule(Element el, IASLanSpec spec) {
		expect(el, ASLanXMLSerializer.STEP);
		String name = el.getAttribute(ASLanXMLSerializer.NAME);
		RewriteRule rr = spec.rule(name);
		parseComments(el, rr);
		Element parsEl = getFirstChildByName(el, ASLanXMLSerializer.PARAMETERS);
		loadParameters(rr, parsEl, spec);
		Element lhs = getFirstChildByName(el, ASLanXMLSerializer.LHS);
		if (lhs != null) {
			Element factsEl = getFirstChildByName(lhs, ASLanXMLSerializer.FACTS);
			if (factsEl != null) {
				NodeList all = factsEl.getChildNodes();
				for (int i = 0; i < all.getLength(); i++) {
					if (all.item(i) instanceof Element) {
						rr.addLHS(parseTerm((Element) all.item(i), spec));
					}
				}
			}
			Element conditionsEl = getFirstChildByName(lhs, ASLanXMLSerializer.CONDITIONS);
			if (conditionsEl != null) {
				NodeList all = conditionsEl.getChildNodes();
				for (int i = 0; i < all.getLength(); i++) {
					if (all.item(i) instanceof Element) {
						rr.addLHS(parseTerm((Element) all.item(i), spec));
					}
				}
			}
		}

		Element exists = getFirstChildByName(el, ASLanXMLSerializer.EXISTS);
		if (exists != null) {
			NodeList all = exists.getChildNodes();
			for (int i = 0; i < all.getLength(); i++) {
				if (all.item(i) instanceof Element) {
					String vname = ((Element) all.item(i)).getAttribute(ASLanXMLSerializer.NAME);
					Variable v = spec.findVariable(vname);
					if (v != null) {
						rr.addExists(v);
					}
					else {
						err.addException(ASLanErrorMessages.UNKNOWN_VARIABLE_IN_TAG, vname, ASLanXMLSerializer.EXISTS);
					}
				}
			}
		}

		Element rhs = getFirstChildByName(el, ASLanXMLSerializer.RHS);
		if (rhs != null) {
			Element factsEl = getFirstChildByName(rhs, ASLanXMLSerializer.FACTS);
			if (factsEl != null) {
				NodeList all = factsEl.getChildNodes();
				for (int i = 0; i < all.getLength(); i++) {
					if (all.item(i) instanceof Element) {
						rr.addRHS(parseTerm((Element) all.item(i), spec));
					}
				}
			}
		}
		return rr;
	}

	private AttackState parseAttackState(Element el, IASLanSpec spec) {
		expect(el, ASLanXMLSerializer.ATTACK_STATE);
		String name = el.getAttribute(ASLanXMLSerializer.NAME);
		AttackState as = spec.attackState(name);
		parseComments(el, as);
		Element parsEl = getFirstChildByName(el, ASLanXMLSerializer.PARAMETERS);
		loadParameters(as, parsEl, spec);
		Element factsEl = getFirstChildByName(el, ASLanXMLSerializer.FACTS);
		if (factsEl != null) {
			NodeList all = factsEl.getChildNodes();
			for (int i = 0; i < all.getLength(); i++) {
				if (all.item(i) instanceof Element) {
					as.addTerm(parseTerm((Element) all.item(i), spec));
				}
			}
		}
		Element conditionsEl = getFirstChildByName(el, ASLanXMLSerializer.CONDITIONS);
		if (conditionsEl != null) {
			NodeList all = conditionsEl.getChildNodes();
			for (int i = 0; i < all.getLength(); i++) {
				if (all.item(i) instanceof Element) {
					as.addTerm(parseTerm((Element) all.item(i), spec));
				}
			}
		}
		return as;
	}

	private Goal parseGoal(Element el, IASLanSpec spec) {
		expect(el, ASLanXMLSerializer.GOAL);
		String name = el.getAttribute(ASLanXMLSerializer.NAME);
		Element formulaEl = getFirstChildByName(el, ASLanXMLSerializer.FORMULA);
		ITerm formula = null;
		if (formulaEl != null) {
			formula = parseTerm(getFirstChild(formulaEl), spec);
		}
		Goal g = spec.goal(name, formula);
		parseComments(el, g);
		Element parsEl = getFirstChildByName(el, ASLanXMLSerializer.PARAMETERS);
		loadParameters(g, parsEl, spec);
		return g;
	}

	private ITerm parseTerm(Element el, IASLanSpec spec) {
		if (el.getNodeName().equals(ASLanXMLSerializer.VARIABLE)) {
			String name = el.getAttribute(ASLanXMLSerializer.NAME);
			return spec.findVariable(name).term();
		}
		else if (el.getNodeName().equals(ASLanXMLSerializer.CONSTANT)) {
			String name = el.getAttribute(ASLanXMLSerializer.NAME);
			return spec.findConstant(name).term();
		}
		else if (el.getNodeName().equals(ASLanXMLSerializer.FUNCTION) || el.getNodeName().equals(ASLanXMLSerializer.LTL)) {
			String name = el.getAttribute(ASLanXMLSerializer.NAME);
			List<ITerm> pars = new ArrayList<ITerm>();
			Element parametersEl = getFirstChildByName(el, ASLanXMLSerializer.PARAMETERS);
			NodeList all = parametersEl.getChildNodes();
			for (int i = 0; i < all.getLength(); i++) {
				if (all.item(i) instanceof Element) {
					pars.add(parseTerm((Element) all.item(i), spec));
				}
			}
			return spec.findFunction(name).term(pars.toArray(new ITerm[pars.size()]));
		}
		else if (el.getNodeName().equals(ASLanXMLSerializer.FUNCTION_CONSTANT)) {
			String name = el.getAttribute(ASLanXMLSerializer.NAME);
			return spec.findFunction(name).constantTerm();
		}
		else if (el.getNodeName().equals(ASLanXMLSerializer.NUMBER)) {
			String valueStr = el.getAttribute(ASLanXMLSerializer.VALUE);
			int value = Integer.parseInt(valueStr);
			return spec.numericTerm(value);
		}
		else if (el.getNodeName().equals(ASLanXMLSerializer.NOT)) {
			ITerm base = parseTerm(getFirstChild(el), spec);
			return base.negate();
		}
		else if (el.getNodeName().equals(ASLanXMLSerializer.EXISTS)) {
			String name = el.getAttribute(ASLanXMLSerializer.VARIABLE);
			Variable v = spec.findVariable(name);
			ITerm base = parseTerm(getFirstChild(el), spec);
			return base.exists(v);
		}
		else if (el.getNodeName().equals(ASLanXMLSerializer.FORALL)) {
			String name = el.getAttribute(ASLanXMLSerializer.VARIABLE);
			Variable v = spec.findVariable(name);
			ITerm base = parseTerm(getFirstChild(el), spec);
			return base.forall(v);
		}
		else {
			err.addException(ASLanErrorMessages.INVALID_TAG_FOR, el.getNodeName(), "term");
			return null;
		}
	}

	private void parseComments(Element el, IRepresentable repr) {
		Element commentsRoot = getFirstChildByName(el, ASLanXMLSerializer.COMMENTS);
		if (commentsRoot != null) {
			NodeList all = commentsRoot.getChildNodes();
			for (int i = 0; i < all.getLength(); i++) {
				Node child = all.item(i);
				if (child instanceof Element) {
					Element chel = (Element) child;
					if (child.getNodeName().equals(ASLanXMLSerializer.COMMENT_LINE)) {
						String cm = child.getTextContent();
						repr.addCommentLine(cm);
					}
					else if (child.getNodeName().equals(ASLanXMLSerializer.METAINFO)) {
						String name = chel.getAttribute(ASLanXMLSerializer.NAME);
						MetaInfo mi = repr.addMetaInfo(name);
						NodeList allfp = chel.getChildNodes();
						for (int j = 0; j < allfp.getLength(); j++) {
							Node fp = allfp.item(j);
							if (fp instanceof Element) {
								if (fp.getNodeName().equals(ASLanXMLSerializer.FLAG)) {
									String flag = fp.getTextContent();
									mi.addFlag(flag);
								}
								else if (fp.getNodeName().equals(ASLanXMLSerializer.PARAMETER)) {
									Element pKey = getFirstChildByName((Element) fp, ASLanXMLSerializer.NAME);
									Element pValue = getFirstChildByName((Element) fp, ASLanXMLSerializer.VALUE);
									String key = pKey.getTextContent();
									String value = pValue.getTextContent();
									mi.addParameter(key, value);
								}
							}
						}
					}
				}
			}
		}
	}

	private Element getFirstChildByName(Element el, String childTag) {
		NodeList all = el.getChildNodes();
		for (int i = 0; i < all.getLength(); i++) {
			Node child = all.item(i);
			if (child instanceof Element) {
				if (child.getNodeName().equals(childTag)) {
					return (Element) child;
				}
			}
		}
		return null;
	}

	private Element getFirstChild(Element el) {
		NodeList all = el.getChildNodes();
		for (int i = 0; i < all.getLength(); i++) {
			Node child = all.item(i);
			if (child instanceof Element) {
				return (Element) child;
			}
		}
		return null;
	}

	private void expect(Element el, String tag) {
		if (!tag.equals(el.getNodeName())) {
			err.addException(ASLanErrorMessages.INVALID_TAG_EXPECTING, tag, el.getNodeName());
		}
	}
}
