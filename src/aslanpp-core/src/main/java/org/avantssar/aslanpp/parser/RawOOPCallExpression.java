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

import org.avantssar.aslanpp.model.CommunicationTerm;
import org.avantssar.aslanpp.model.ErrorMessages;
import org.avantssar.aslanpp.model.FunctionSymbol;
import org.avantssar.aslanpp.model.IExpression;
import org.avantssar.aslanpp.model.IOwned;
import org.avantssar.aslanpp.model.IScope;
import org.avantssar.aslanpp.model.ITerm;
import org.avantssar.aslanpp.model.MacroSymbol;
import org.avantssar.aslanpp.model.Prelude;
import org.avantssar.commons.ChannelEntry;
import org.avantssar.commons.ErrorGatherer;
import org.avantssar.commons.LocationInfo;

public class RawOOPCallExpression extends AbstractRawExpression {

	private final IRawExpression caller;
	private final IRawExpression what;

	private ITerm cleanTerm;
	private CommunicationTerm cleanCommunication;
	private boolean cleanBuilt;

	public RawOOPCallExpression(IScope scope, LocationInfo location, ErrorGatherer err, IRawExpression caller, IRawExpression what) {
		super(scope, location, err);
		this.caller = caller;
		this.what = what;
	}

	public IRawExpression getCaller() {
		return caller;
	}

	public IRawExpression getWhat() {
		return what;
	}

	@Override
	public void accept(IRawExpressionVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	public IExpression getFormula() {
		buildClean(true);
		if (cleanTerm != null) {
			return cleanTerm.expression();
		}
		else {
			getErrorGatherer().addException(getLocation(), ErrorMessages.ITEM_NOT_ALLOWED_IN_THIS_PLACE, "Function call", getRepresentation(), "formula");
			return null;
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
			// should never get here
			getErrorGatherer().addException(getLocation(), ErrorMessages.ITEM_NOT_ALLOWED_IN_THIS_PLACE, "Function", getRepresentation(), "guard");
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
			// should never get here
			getErrorGatherer().addException(getLocation(), ErrorMessages.ITEM_NOT_ALLOWED_IN_THIS_PLACE, "Function", getRepresentation(), "term");
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
			getErrorGatherer().addException(getLocation(), ErrorMessages.ITEM_NOT_ALLOWED_IN_THIS_PLACE, "Function", getRepresentation(), "transmission");
			return null;
		}
	}

	@Override
	public IExpression getComparison() {
		getErrorGatherer().addException(getLocation(), ErrorMessages.ITEM_NOT_ALLOWED_IN_THIS_PLACE, "Function call", getRepresentation(), "(in)equality");
		return null;
	}

	@Override
	public boolean isChannelGoal() {
		return true;
	}

	@Override
	public RawChannelGoalInfo getChannelGoal() {
		return new RawChannelGoalInfo(caller, what, ChannelEntry.regular.arrow);
	}

	private void buildClean(boolean strictVarCheck) {
		if (!cleanBuilt) {
			if (getWhat() instanceof RawConstVarExpression) {
				String whName = ((RawConstVarExpression) getWhat()).getName();
				if (whName.equals(Prelude.SEND) || whName.equals(Prelude.RECEIVE)) {
					if (getWhat() instanceof RawFunctionExpression) {
						RawFunctionExpression rFnc = (RawFunctionExpression) getWhat();
						if (rFnc.getParameters().size() == 2) {
							ITerm sender = getCaller().getTerm(false, true);
							ITerm receiver = rFnc.getParameters().get(0).getTerm(false, true);
							// if receiver switch the roles
							if (whName.equals(Prelude.RECEIVE)) {
								ITerm aux = sender;
								sender = receiver;
								receiver = aux;
							}
							ITerm payload = rFnc.getParameters().get(1).getTerm(false, true);
							cleanCommunication = getScope().communication(getLocation(), sender, receiver, payload, null, ChannelEntry.regular, whName.equals(Prelude.RECEIVE), true, true);
						}
						else {
							getErrorGatherer().addException(getLocation(), ErrorMessages.WRONG_NUMBER_OF_PARAMETERS, "Transmission", whName, 3, 1);
						}
					}
					else {
						getErrorGatherer().addException(getLocation(), ErrorMessages.WRONG_NUMBER_OF_PARAMETERS, "Transmission", whName, 3, 1);
					}
				}
				else {
					String name = null;
					ITerm[] args = null;
					if (getWhat() instanceof RawFunctionExpression) {
						RawFunctionExpression rFnc = (RawFunctionExpression) getWhat();
						if (Character.isLowerCase(rFnc.getName().charAt(0))) {
							name = rFnc.getName();
							args = new ITerm[rFnc.getParameters().size() + 1];
							args[0] = getCaller().getTerm(false, strictVarCheck);
							for (int i = 0; i < rFnc.getParameters().size(); i++) {
								args[i + 1] = rFnc.getParameters().get(i).getTerm(false, strictVarCheck);
							}
						}
						else {
							getErrorGatherer().addException(getLocation(), ErrorMessages.INVALID_NAME_FOR_ITEM, "function/macro", rFnc.getName());
						}
					}
					else if (getWhat() instanceof RawConstVarExpression) {
						RawConstVarExpression rCnst = (RawConstVarExpression) getWhat();
						if (Character.isLowerCase(rCnst.getName().charAt(0))) {
							name = rCnst.getName();
							args = new ITerm[1];
							args[0] = getCaller().getTerm(false, strictVarCheck);
						}
						else {
							getErrorGatherer().addException(getLocation(), ErrorMessages.INVALID_NAME_FOR_ITEM, "function/macro", rCnst.getName());
						}
					}
					IOwned sym = getScope().findFunctionOrMacro(name);
					if (sym instanceof FunctionSymbol) {
						cleanTerm = ((FunctionSymbol) sym).term(getLocation(), getScope(), args);
					}
					else if (sym instanceof MacroSymbol) {
						cleanTerm = ((MacroSymbol) sym).term(getLocation(), getScope(), args);
					}
					else {
						getErrorGatherer().addException(getLocation(), ErrorMessages.UNDEFINED_FUNCTION_OR_MACRO, name, getScope().getOriginalName());
					}
				}
			}
			else {
				getErrorGatherer().addException(getLocation(), ErrorMessages.DIFFERENT_ITEM_EXPECTED, "function/macro", getWhat().getRepresentation());
			}
			cleanBuilt = true;
		}
	}
}
