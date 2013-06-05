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

package org.avantssar;

import junit.framework.TestCase;
import org.avantssar.aslan.ASLanErrorMessages;
import org.avantssar.aslan.ASLanSpecificationBuilder;
import org.avantssar.aslan.IASLanSpec;
import org.avantssar.commons.ErrorGatherer;

public abstract class GenericModelTest extends TestCase {

	public void testModel() {
		IASLanSpec spec = getASLanSpec();

		String ptOne = spec.toPlainText();
		IASLanSpec spec2 = ASLanSpecificationBuilder.instance().loadFromPlainText(ptOne, new ErrorGatherer(ASLanErrorMessages.DEFAULT));
		String ptTwo = spec2.toPlainText();
		assertEquals(ptOne, ptTwo);

		String xmlOne = spec2.toXML();
		IASLanSpec spec3 = ASLanSpecificationBuilder.instance().loadFromXML(xmlOne, new ErrorGatherer(ASLanErrorMessages.DEFAULT));
		String xmlTwo = spec3.toXML();
		assertEquals(xmlOne, xmlTwo);

		IASLanSpec spec4 = ASLanSpecificationBuilder.instance().loadFromXML(xmlTwo, new ErrorGatherer(ASLanErrorMessages.DEFAULT));
		String ptThree = spec4.toPlainText();
		assertEquals(ptTwo, ptThree);
	}

	protected abstract IASLanSpec getASLanSpec();

	public static void main(String[] args) {
		GenericModelTest provider = new HornClauseClosureModelTest();
		IASLanSpec spec = provider.getASLanSpec();
		System.out.println(spec.toPlainText());
	}
}
