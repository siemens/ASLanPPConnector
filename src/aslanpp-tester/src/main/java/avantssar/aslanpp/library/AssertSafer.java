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

import org.avantssar.aslanpp.model.ConstantSymbol;
import org.avantssar.commons.ChannelModel;
import org.avantssar.commons.ChannelEntry.Type;
import avantssar.aslan.backends.Verdict;
import avantssar.aslanpp.testing.Specification;

@Specification
public class AssertSafer extends AssertUnsafe {

	public AssertSafer() {
		super();
	}

	@Override
	public Verdict getExpectedVerdict() {
		if (cm == ChannelModel.CCM) {
			if (channelType.type == Type.Authentic || channelType.type == Type.Secure) {
				return Verdict.NoAttack;
			}
			else {
				return Verdict.Attack;
			}
		}
		else {
			return Verdict.NoAttack;
		}
	}

	@Override
	protected ConstantSymbol getPPtoken() {
		return cToken1;
	}

}
