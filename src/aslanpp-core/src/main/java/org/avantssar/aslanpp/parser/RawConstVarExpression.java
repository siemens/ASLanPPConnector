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
import org.avantssar.aslanpp.model.ConstantSymbol;
import org.avantssar.aslanpp.model.ConstantTerm;
import org.avantssar.aslanpp.model.ErrorMessages;
import org.avantssar.aslanpp.model.IExpression;
import org.avantssar.aslanpp.model.IOwned;
import org.avantssar.aslanpp.model.IScope;
import org.avantssar.aslanpp.model.ITerm;
import org.avantssar.aslanpp.model.MacroSymbol;
import org.avantssar.aslanpp.model.VariableSymbol;
import org.avantssar.commons.ErrorGatherer;
import org.avantssar.commons.LocationInfo;

public class RawConstVarExpression extends AbstractRawExpression {

	private final String name;

	private ITerm clean;

	public RawConstVarExpression(IScope scope, LocationInfo location, ErrorGatherer err, String name) {
		super(scope, location, err);
		this.name = name;
	}

	public String getName() {
		return name;
	}

	@Override
	public void accept(IRawExpressionVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	public IExpression getFormula() {
		ITerm t = getClean(true);
		if (t instanceof ConstantTerm) {
			return t.expression();
		}
		else {
			getErrorGatherer().addException(getLocation(), ErrorMessages.ITEM_NOT_ALLOWED_IN_THIS_PLACE, "Variable", getRepresentation(), "formula");
			return null;
		}
	}

	@Override
	public IExpression getGuard(boolean allowReceive) {
		ITerm t = getClean(true);
		if (t instanceof ConstantTerm) {
			return t.expression();
		}
		else {
			getErrorGatherer().addException(getLocation(), ErrorMessages.ITEM_NOT_ALLOWED_IN_THIS_PLACE, "Variable", getRepresentation(), "guard");
			return null;
		}
	}

	@Override
	public ITerm getTerm(boolean allowTransmission, boolean strictVarCheck) {
		return getClean(strictVarCheck);
	}

	@Override
	public CommunicationTerm getTransmission() {
		getErrorGatherer().addException(getLocation(), ErrorMessages.ITEM_NOT_ALLOWED_IN_THIS_PLACE, "Constant/variable", getRepresentation(), "transmission");
		return null;
	}

	@Override
	public IExpression getComparison() {
		getErrorGatherer().addException(getLocation(), ErrorMessages.ITEM_NOT_ALLOWED_IN_THIS_PLACE, "Constant/variable", getRepresentation(), "(in)equality");
		return null;
	}

	@Override
	public RawChannelGoalInfo getChannelGoal() {
		getErrorGatherer().addException(getLocation(), ErrorMessages.ITEM_NOT_ALLOWED_IN_THIS_PLACE, "Constant/variable", getRepresentation(), "channel goal");
		return null;
	}

	private ITerm getClean(boolean strictVarCheck) {
		if (clean == null) {
			if (Character.isUpperCase(name.charAt(0))) {
				VariableSymbol var = getScope().findVariable(name);
				if (var == null) {
					if (strictVarCheck) {
						getErrorGatherer().addException(getLocation(), ErrorMessages.UNDEFINED_VARIABLE, name, getScope().getOriginalName());
					}
					else {
						var = getScope().addUntypedVariable(name, getLocation());
						clean = var.term(getLocation(), getScope());
					}
				}
				else {
					clean = var.term(getLocation(), getScope());
				}
			}
			else if (Character.isLowerCase(name.charAt(0))) {
				IOwned sym = getScope().findConstantOrMacro(name);
				if (sym instanceof ConstantSymbol) {
					clean = ((ConstantSymbol) sym).term(getLocation(), getScope());
				}
				else if (sym instanceof MacroSymbol) {
					clean = ((MacroSymbol) sym).term(getLocation(), getScope());
				}
				else {
					getErrorGatherer().addException(getLocation(), ErrorMessages.UNDEFINED_CONSTANT_OR_MACRO, name, getScope().getOriginalName());
				}
			}
			else {
				getErrorGatherer().addException(getLocation(), ErrorMessages.INVALID_NAME_FOR_ITEM, "constant/macro", getName());
			}
		}
		return clean;
	}

}
