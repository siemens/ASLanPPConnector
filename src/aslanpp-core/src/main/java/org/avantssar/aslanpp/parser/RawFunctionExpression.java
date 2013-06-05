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

package org.avantssar.aslanpp.parser;

import java.util.ArrayList;
import java.util.List;
import org.avantssar.aslanpp.LTLHelper;
import org.avantssar.aslanpp.model.CommunicationTerm;
import org.avantssar.aslanpp.model.ErrorMessages;
import org.avantssar.aslanpp.model.FunctionSymbol;
import org.avantssar.aslanpp.model.IExpression;
import org.avantssar.aslanpp.model.IOwned;
import org.avantssar.aslanpp.model.IScope;
import org.avantssar.aslanpp.model.ITerm;
import org.avantssar.aslanpp.model.LTLExpression;
import org.avantssar.aslanpp.model.MacroSymbol;
import org.avantssar.aslanpp.model.Prelude;
import org.avantssar.commons.ChannelEntry;
import org.avantssar.commons.ErrorGatherer;
import org.avantssar.commons.LocationInfo;

public class RawFunctionExpression extends RawConstVarExpression {

	private final List<IRawExpression> parameters = new ArrayList<IRawExpression>();

	private ITerm cleanTerm;
	private IExpression cleanExpr;
	private CommunicationTerm cleanCommunication;
	private boolean cleanBuilt = false;

	public RawFunctionExpression(IScope scope, LocationInfo location, ErrorGatherer err, String name, List<IRawExpression> parameters) {
		super(scope, location, err, name);
		this.parameters.addAll(parameters);
	}

	public List<IRawExpression> getParameters() {
		return parameters;
	}

	@Override
	public void accept(IRawExpressionVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	public IExpression getFormula() {
		buildClean(true);
		if (cleanExpr != null) {
			return cleanExpr;
		}
		else {
			return cleanTerm.expression();
		}
	}

	@Override
	public IExpression getGuard(boolean allowReceive) {
		buildClean(true);
		if (cleanTerm != null) {
			return cleanTerm.expression();
		}
		else if (cleanCommunication != null) {
			if (allowReceive) {
				if (cleanCommunication.isReceive()) {
					return cleanCommunication.expression();
				}
				else {
					getErrorGatherer().addException(getLocation(), ErrorMessages.ITEM_NOT_ALLOWED_IN_THIS_PLACE, "Transmission", getRepresentation(), "guard without receive");
					return null;
				}
			}
			else {
				getErrorGatherer().addException(getLocation(), ErrorMessages.ITEM_NOT_ALLOWED_IN_THIS_PLACE, "Transmission", getRepresentation(), "guard without receive");
				return null;
			}
		}
		else {
			getErrorGatherer().addException(getLocation(), ErrorMessages.ITEM_NOT_ALLOWED_IN_THIS_PLACE, "LTL formula", getRepresentation(), "guard");
			return null;
		}
	}

	@Override
	public ITerm getTerm(boolean allowTransmission, boolean strictVarCheck) {
		buildClean(strictVarCheck);
		if (cleanTerm != null) {
			return cleanTerm;
		}
		else if (cleanCommunication != null) {
			if (allowTransmission) {
				return cleanCommunication;
			}
			else {
				getErrorGatherer().addException(getLocation(), ErrorMessages.ITEM_NOT_ALLOWED_IN_THIS_PLACE, "Transmission", getRepresentation(), "term");
				return null;
			}
		}
		else {
			getErrorGatherer().addException(getLocation(), ErrorMessages.ITEM_NOT_ALLOWED_IN_THIS_PLACE, "LTL formula", getRepresentation(), "term");
			return null;
		}
	}

	@Override
	public CommunicationTerm getTransmission() {
		buildClean(true);
		if (cleanCommunication != null) {
			return cleanCommunication;
		}
		else {
			getErrorGatherer().addException(getLocation(), ErrorMessages.ITEM_NOT_ALLOWED_IN_THIS_PLACE, "Function/LTL formula", getRepresentation(), "transmission");
			return null;
		}
	}

	@Override
	public IExpression getComparison() {
		getErrorGatherer().addException(getLocation(), ErrorMessages.ITEM_NOT_ALLOWED_IN_THIS_PLACE, "Function", getRepresentation(), "(in)equality");
		return null;
	}

	private void buildClean(boolean strictVarCheck) {
		if (!cleanBuilt) {
			if (Character.isLowerCase(getName().charAt(0))) {
				// if send or receive, then it becomes communication channel
				if (getName().equals(Prelude.SEND) || getName().equals(Prelude.RECEIVE)) {
					if (parameters.size() != 2 && parameters.size() != 3) {
						getErrorGatherer().addException(getLocation(), ErrorMessages.WRONG_NUMBER_OF_TRANSMISSION_PARAMETERS, parameters.size(), getRepresentation());
					}
					else {
						int i = 0;
						ITerm sender;
						if (parameters.size() == 2) {
							RawConstVarExpression fakeActor = new RawConstVarExpression(getScope(), getLocation(), getErrorGatherer(), "Actor");
							sender = fakeActor.getTerm(false, true);
						}
						else {
							sender = parameters.get(i++).getTerm(false, true);
						}
						ITerm receiver = parameters.get(i++).getTerm(false, true);
						// if receiver switch the roles
						if (getName().equals(Prelude.RECEIVE)) {
							ITerm aux = sender;
							sender = receiver;
							receiver = aux;
						}
						ITerm payload = parameters.get(i++).getTerm(false, true);
						CommunicationTerm ct = getScope().communication(getLocation(), sender, receiver, payload, null, ChannelEntry.regular, getName().equals(Prelude.RECEIVE), true, false);
						cleanCommunication = ct;
						cleanBuilt = true;
					}
				}
				else {
					ITerm[] args = new ITerm[parameters.size()];
					for (int i = 0; i < parameters.size(); i++) {
						args[i] = parameters.get(i).getTerm(false, strictVarCheck);
					}
					IOwned sym = getScope().findFunctionOrMacro(getName());
					if (sym instanceof FunctionSymbol) {
						cleanTerm = ((FunctionSymbol) sym).term(getLocation(), getScope(), args);
					}
					else if (sym instanceof MacroSymbol) {
						cleanTerm = ((MacroSymbol) sym).term(getLocation(), getScope(), args);
					}
					else {
						getErrorGatherer().addException(getLocation(), ErrorMessages.UNDEFINED_FUNCTION_OR_MACRO, getName(), getScope().getOriginalName());
					}
				}
			}
			else if (LTLHelper.getInstance().isUnary(getName()) || LTLHelper.getInstance().isBinary(getName())) {
				if (LTLHelper.getInstance().isUnary(getName())) {
					if (parameters.size() != 1) {
						getErrorGatherer().addException(getLocation(), ErrorMessages.WRONG_NUMBER_OF_PARAMETERS, "LTL operator", getName(), 1, parameters.size());
					}
					cleanExpr = new LTLExpression(getName(), parameters.get(0).getFormula());
				}
				else if (LTLHelper.getInstance().isBinary(getName())) {
					if (parameters.size() != 2) {
						getErrorGatherer().addException(getLocation(), ErrorMessages.WRONG_NUMBER_OF_PARAMETERS, "LTL operator", getName(), 2, parameters.size());
					}
					cleanExpr = new LTLExpression(getName(), parameters.get(0).getFormula(), parameters.get(1).getFormula());
				}
			}
			else {
				getErrorGatherer().addException(getLocation(), ErrorMessages.INVALID_NAME_FOR_ITEM, "function/macro (or missing \"new\" keyword or missing \':\' in case a goal label was intended)", getName());
			}
			cleanBuilt = true;
		}
	}
}
