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

public class UpperNameValidator extends GenericValidator {

	@Override
	public String getDescription() {
		return "variable name (should start with upper-case letter, then any sequence of letters, digits, or '_')";
	}

	@Override
	protected String getPattern() {
		return "[A-Z][a-zA-Z0-9_]*";
	}

}
