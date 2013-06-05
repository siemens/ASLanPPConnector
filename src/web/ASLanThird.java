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

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Scanner;
import org.avantssar.aslan.ASLanErrorMessages;
import org.avantssar.aslan.ASLanSpecificationBuilder;
import org.avantssar.aslan.IASLanSpec;
import org.avantssar.commons.ErrorGatherer;

public class ASLanThird {

	public static void main(String[] args) throws Exception {
		// read the ASLan specification from the XML file into a String
		StringBuffer sb = new StringBuffer();
		String newline = System.getProperty("line.separator");
		Scanner sc = new Scanner(new FileInputStream("simple.xml"));
		while (sc.hasNextLine()) {
			sb.append(sc.nextLine()).append(newline);
		}
		sc.close();

		// load the specification from the XML String
		IASLanSpec spec = ASLanSpecificationBuilder.instance().loadFromXML(sb.toString(), new ErrorGatherer(ASLanErrorMessages.DEFAULT));

		// write the specification into another file in XML format
		PrintStream out = new PrintStream(new FileOutputStream("simple2.aslan"));
		out.print(spec.toPlainText());
		out.close();
	}
}
