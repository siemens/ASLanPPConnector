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

package avantssar.aslanpp.library;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import org.avantssar.aslan.ASLanSpecificationBuilder;
import org.avantssar.aslan.Constant;
import org.avantssar.aslan.Function;
import org.avantssar.aslan.HornClause;
import org.avantssar.aslan.IASLanSpec;
import org.avantssar.aslan.InitialState;
import org.avantssar.aslan.RewriteRule;
import org.avantssar.aslan.Variable;
import org.avantssar.aslanpp.model.ASLanPPSpecification;
import org.avantssar.aslanpp.model.ConstantSymbol;
import org.avantssar.aslanpp.model.Entity;
import org.avantssar.aslanpp.model.EntityManager;
import org.avantssar.aslanpp.model.FunctionSymbol;
import org.avantssar.aslanpp.model.Prelude;
import org.avantssar.aslanpp.model.SimpleType;
import org.avantssar.commons.ChannelModel;
import avantssar.aslan.backends.Verdict;
import avantssar.aslanpp.testing.BackendParameters;
import avantssar.aslanpp.testing.IChannelModelFlexibleSpecProvider;
import avantssar.aslanpp.testing.Specification;

@SuppressWarnings("unused")
public abstract class AbstractSpecProvider implements IChannelModelFlexibleSpecProvider {

	protected ChannelModel cm = ChannelModel.CCM;

	protected SimpleType tppAgent;
	protected SimpleType tppMessage;
	protected SimpleType tppFact;
	protected SimpleType tppText;
	protected FunctionSymbol fppPair;
	ConstantSymbol cppTrue;
	protected Entity env;

//	protected org.avantssar.aslan.Function fDescendant;
	protected org.avantssar.aslan.Function fChild;
	protected org.avantssar.aslan.Function fDishonest;
	protected Function fPK;
	// protected Function fIsAgent;
	// protected org.avantssar.aslan.Constant cDummyAgent;
	protected org.avantssar.aslan.Constant cDummyNat;
	protected org.avantssar.aslan.Constant cRoot;
	org.avantssar.aslan.Function acmSent;
	org.avantssar.aslan.Function acmRcvd;
	org.avantssar.aslan.Function acmChFnc;
	org.avantssar.aslan.Constant cACM_auth;
	org.avantssar.aslan.Constant cACM_conf;
	org.avantssar.aslan.Constant cACM_sec;
	org.avantssar.aslan.Constant cACM_regular;
	org.avantssar.aslan.Constant cACM_res_auth;
	org.avantssar.aslan.Constant cACM_res_conf;
	org.avantssar.aslan.Constant cACM_res_sec;
	org.avantssar.aslan.Constant cACM_res_regular;

	org.avantssar.aslan.Function fAK;
	org.avantssar.aslan.Function fCK;
	org.avantssar.aslan.Function fHash;
	org.avantssar.aslan.Constant ccmAuth;
	org.avantssar.aslan.Constant ccmConf;
	org.avantssar.aslan.Constant ccmSec;
	org.avantssar.aslan.Function fSucc;

	protected int stepIndex = 1;

	public String getSpecificationName() {
		return getClass().getSimpleName();
	}

	public void setChannelModel(ChannelModel cm) {
		this.cm = cm;
	}

	public ChannelModel getChannelModel() {
		return cm;
	}

	public Verdict getExpectedVerdict() {
		Specification spec = this.getClass().getAnnotation(Specification.class);
		return spec.expectedVerdict();
	}

	public List<String> getBackendParameters(String backendName) {
		List<String> pars = new ArrayList<String>();
		for (Annotation a : getClass().getAnnotations()) {
			if (a.annotationType().equals(BackendParameters.class)) {
				BackendParameters bp = (BackendParameters) a;
				if (bp.backend().equals(backendName)) {
					for (String s : bp.parameters()) {
						pars.add(s);
					}
				}
			}
		}
		return pars;
	}

	public ASLanPPSpecification startASLanPPSpec() {
		EntityManager manager = new EntityManager();
		ASLanPPSpecification spec = new ASLanPPSpecification(manager, getSpecificationName(), cm);

		tppAgent = spec.findType(Prelude.AGENT);
		tppMessage = spec.findType(Prelude.MESSAGE);
		tppFact = spec.findType(Prelude.FACT);
		tppText = spec.findType(Prelude.TEXT);

		fppPair = spec.findFunction(Prelude.PAIR);

		cppTrue = spec.findConstant(Prelude.TRUE);

		env = spec.entity("Environment");

		return spec;
	}

	protected String getStateFunctionName(String entityName) {
		return "state_" + entityName;
	}

	protected boolean hasSymbolicInstances() {
		return false;
	}

	protected IASLanSpec startASLanSpec() {
		stepIndex = 1;
		IASLanSpec spec = ASLanSpecificationBuilder.instance().createASLanSpecification();
		spec.function("defaultPseudonym", IASLanSpec.PUBLIC_KEY, IASLanSpec.AGENT, IASLanSpec.NAT);
//		fDescendant = spec.function("descendant", IASLanSpec.FACT, IASLanSpec.NAT, IASLanSpec.NAT);
		fChild = spec.function("child", IASLanSpec.FACT, IASLanSpec.NAT, IASLanSpec.NAT);
		fDishonest = spec.function("dishonest", IASLanSpec.FACT, IASLanSpec.AGENT);
		fSucc = spec.function("succ", IASLanSpec.NAT, IASLanSpec.NAT);
		// fIsAgent = spec.function("isAgent", IASLanSpec.FACT,
		// IASLanSpec.AGENT);
		fPK = spec.findFunction("pk");
		fHash = spec.findFunction("hash");
		// spec.function("isAgent", IASLanSpec.FACT, IASLanSpec.AGENT);
		//Constant cTrue = spec.constant("true", IASLanSpec.FACT);
		spec.constant("false", IASLanSpec.FACT);
		org.avantssar.aslan.PrimitiveType tChannel = spec.primitiveType("channel");
		// tChannel.setSuperType(IASLanSpec.MESSAGE);
		org.avantssar.aslan.PrimitiveType tSLabel = spec.primitiveType("slabel");
		tSLabel.setSuperType(IASLanSpec.TEXT);
		if (cm == ChannelModel.CCM) {
			fAK = spec.function("ak", IASLanSpec.PUBLIC_KEY, IASLanSpec.AGENT);
			fCK = spec.function("ck", IASLanSpec.PUBLIC_KEY, IASLanSpec.AGENT);
			ccmAuth = spec.constant("atag", tSLabel);
			ccmConf = spec.constant("ctag", tSLabel);
			ccmSec = spec.constant("stag", tSLabel);

			org.avantssar.aslan.Variable vX2 = spec.variable("Ak_arg_1", IASLanSpec.AGENT);
			org.avantssar.aslan.HornClause hcAK = spec.hornClause(getPublicName("ak"), IASLanSpec.IKNOWS.term(fAK.term(vX2.term())));
			hcAK.addBodyFact(IASLanSpec.IKNOWS.term(vX2.term()));
			org.avantssar.aslan.Variable vX1 = spec.variable("Ck_arg_1", IASLanSpec.AGENT);
			org.avantssar.aslan.HornClause hcCK = spec.hornClause(getPublicName("ck"), IASLanSpec.IKNOWS.term(fCK.term(vX1.term())));
			hcCK.addBodyFact(IASLanSpec.IKNOWS.term(vX1.term()));
		}
		else if (cm == ChannelModel.ACM) {
			acmChFnc = spec.function("ch", tChannel, IASLanSpec.AGENT, IASLanSpec.AGENT, tSLabel);
			acmRcvd = spec.function("rcvd", IASLanSpec.FACT, IASLanSpec.AGENT, IASLanSpec.AGENT, IASLanSpec.MESSAGE, tChannel);
			acmSent = spec.function("sent", IASLanSpec.FACT, IASLanSpec.AGENT, IASLanSpec.AGENT, IASLanSpec.AGENT, IASLanSpec.MESSAGE, tChannel);
			cACM_auth = spec.constant("auth", tSLabel);
			cACM_conf = spec.constant("conf", tSLabel);
			cACM_regular = spec.constant("regular", tSLabel);
			cACM_sec = spec.constant("sec", tSLabel);
			cACM_res_regular = spec.constant("res", tSLabel);
			cACM_res_auth = spec.constant("res_auth", tSLabel);
			cACM_res_conf = spec.constant("res_conf", tSLabel);
			cACM_res_sec = spec.constant("res_sec", tSLabel);
			org.avantssar.aslan.Variable acmRS = spec.variable("ACM_RS", IASLanSpec.AGENT);
			org.avantssar.aslan.Variable acmOS = spec.variable("ACM_OS", IASLanSpec.AGENT);
			org.avantssar.aslan.Variable acmRcv = spec.variable("ACM_Rcv", IASLanSpec.AGENT);
			org.avantssar.aslan.Variable acmMsg = spec.variable("ACM_Msg", IASLanSpec.MESSAGE);
			org.avantssar.aslan.Variable acmCh = spec.variable("ACM_Ch", IASLanSpec.CHANNEL);
			RewriteRule acm = spec.rule(getNextStepName("ACM"));
			acm.addLHS(acmSent.term(acmRS.term(), acmOS.term(), acmRcv.term(), acmMsg.term(), acmCh.term()));
			acm.addRHS(acmRcvd.term(acmRcv.term(), acmOS.term(), acmMsg.term(), acmCh.term()));
		}

		// if (hasSymbolicInstances()) {
		// RewriteRule sym1 = spec.rule(getNextStepName("symbolic_1"));
		// Variable vSymA = spec.variable("Sym_A", IASLanSpec.AGENT);
		// sym1.addLHS(cTrue.term());
		// sym1.addRHS(cTrue.term());
		// sym1.addRHS(IASLanSpec.IKNOWS.term(vSymA.term()));
		// sym1.addExists(vSymA);
		//
		// RewriteRule sym2 = spec.rule(getNextStepName("symbolic_2"));
		// sym2.addLHS(cTrue.term());
		// sym2.addRHS(cTrue.term());
		// sym2.addRHS(IASLanSpec.IKNOWS.term(vSymA.term()));
		// sym2.addRHS(fDishonest.term(vSymA.term()));
		// sym2.addExists(vSymA);
		// }

/* unused
		// for descendant
		Variable vA1 = spec.variable("Descendant_arg_1", IASLanSpec.NAT);
		Variable vA2 = spec.variable("Descendant_arg_2", IASLanSpec.NAT);
		Variable vA3 = spec.variable("Descendant_arg_3", IASLanSpec.NAT);
		HornClause hcDesc = spec.hornClause("descendant_closure", fDescendant.term(vA1.term(), vA3.term()));
		hcDesc.addBodyFact(fDescendant.term(vA1.term(), vA2.term()));
		hcDesc.addBodyFact(fDescendant.term(vA2.term(), vA3.term()));
		HornClause hcDescDirect = spec.hornClause("descendant_direct", fDescendant.term(vA1.term(), vA2.term()));
		hcDescDirect.addBodyFact(fChild.term(vA1.term(), vA2.term()));
*/
		// for pk
		org.avantssar.aslan.Variable vXPK = spec.variable("Pk_arg_1", IASLanSpec.AGENT);
		org.avantssar.aslan.HornClause hcPK = spec.hornClause(getPublicName("pk"), IASLanSpec.IKNOWS.term(fPK.term(vXPK.term())));
		hcPK.addBodyFact(IASLanSpec.IKNOWS.term(vXPK.term()));
		// for hash
		org.avantssar.aslan.Variable vXHash = spec.variable("Hash_arg_1", IASLanSpec.MESSAGE);
		org.avantssar.aslan.HornClause hcHash = spec.hornClause(getPublicName("hash"), IASLanSpec.IKNOWS.term(fHash.term(vXHash.term())));
		hcHash.addBodyFact(IASLanSpec.IKNOWS.term(vXHash.term()));
		// for succ
		org.avantssar.aslan.Variable vXSucc = spec.variable("Succ_arg_1", IASLanSpec.NAT);
		org.avantssar.aslan.HornClause hcSuccPublic = spec.hornClause(getPublicName("succ"), IASLanSpec.IKNOWS.term(fSucc.term(vXSucc.term())));
		hcSuccPublic.addBodyFact(IASLanSpec.IKNOWS.term(vXSucc.term()));
		org.avantssar.aslan.HornClause hcSuccInv = spec.hornClause(getInvName("succ", 1), IASLanSpec.IKNOWS.term(vXSucc.term()));
		hcSuccInv.addBodyFact(IASLanSpec.IKNOWS.term(fSucc.term(vXSucc.term())));

		cRoot = spec.constant("root", IASLanSpec.AGENT);
		// cDummyAgent = spec.constant("dummy_agent", IASLanSpec.AGENT);
		cDummyNat = spec.constant("dummy_nat", IASLanSpec.NAT);

		return spec;
	}

	private String getPublicName(String base) {
		return "public_" + base;
	}

	private String getInvName(String base, int idx) {
		return "inv_" + base + "_" + idx;
	}

	// private String xname(String pref, int x) {
	// StringBuffer sb = new StringBuffer();
	// sb.append(pref.substring(0, 1).toUpperCase());
	// sb.append(pref.substring(1));
	// sb.append("_arg_");
	// sb.append(x);
	// return sb.toString();
	// }

	protected String getNextStepName(String name) {
		return "step_" + String.format("%03d", (stepIndex++)) + "_" + name;
	}

	protected void prefillInitialState(IASLanSpec spec, InitialState init) {
		init.addFact(IASLanSpec.IKNOWS.term(spec.numericTerm(0)));
		init.addFact(fDishonest.term(IASLanSpec.INTRUDER.term()));
		init.addFact(spec.findConstant("true").term());
		// init.addFact(fIsAgent.term(IASLanSpec.INTRUDER.term()));
		init.addFact(IASLanSpec.IKNOWS.term(IASLanSpec.INTRUDER.term()));
		init.addFact(IASLanSpec.IKNOWS.term(IASLanSpec.INV.term(IASLanSpec.PK.term(IASLanSpec.INTRUDER.term()))));
		init.addFact(IASLanSpec.IKNOWS.term(cRoot.term()));
		init.addFact(fChild.term(cDummyNat.term(), spec.numericTerm(0)));
		if (cm == ChannelModel.CCM) {
			init.addFact(IASLanSpec.IKNOWS.term(ccmAuth.term()));
			init.addFact(IASLanSpec.IKNOWS.term(ccmConf.term()));
			init.addFact(IASLanSpec.IKNOWS.term(ccmSec.term()));
			init.addFact(IASLanSpec.IKNOWS.term(IASLanSpec.INV.term(fAK.term(IASLanSpec.INTRUDER.term()))));
			init.addFact(IASLanSpec.IKNOWS.term(IASLanSpec.INV.term(fCK.term(IASLanSpec.INTRUDER.term()))));
		}
		else if (cm == ChannelModel.ACM) {
			init.addFact(IASLanSpec.IKNOWS.term(cACM_auth.term()));
			init.addFact(IASLanSpec.IKNOWS.term(cACM_conf.term()));
			init.addFact(IASLanSpec.IKNOWS.term(cACM_regular.term()));
			init.addFact(IASLanSpec.IKNOWS.term(cACM_sec.term()));
			init.addFact(IASLanSpec.IKNOWS.term(cACM_res_auth.term()));
			init.addFact(IASLanSpec.IKNOWS.term(cACM_res_conf.term()));
			init.addFact(IASLanSpec.IKNOWS.term(cACM_res_regular.term()));
			init.addFact(IASLanSpec.IKNOWS.term(cACM_res_sec.term()));
		}
	}
}
