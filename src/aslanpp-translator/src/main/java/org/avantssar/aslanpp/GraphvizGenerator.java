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

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.avantssar.aslan.IASLanSpec;
import org.avantssar.aslanpp.flow.ASLanBuilder;
import org.avantssar.aslanpp.flow.INode;
import org.avantssar.aslanpp.flow.TransitionsRecorder;
import org.avantssar.aslanpp.flow.TransitionsRecorder.TransitionIndexes;
import org.avantssar.aslanpp.model.ASLanPPSpecification;
import org.avantssar.aslanpp.model.Entity;

public class GraphvizGenerator 
  implements ITranslatorHook // TODO not really needed
{

	private static final String LUMPED_COLOR = "blue";
	private final File outputDir;

	public GraphvizGenerator(File outputDir) throws IOException {
		this.outputDir = outputDir;
		FileUtils.forceMkdir(outputDir);
	}

	@Override
	public void translationOver(ASLanPPSpecification aslanPPspec, IASLanSpec aslanSpec, ASLanBuilder builder) {
		for (Entity e : builder.getFirstNodes().keySet()) {
			TransitionsRecorder rec = builder.getRecorders().get(e);
			INode firstNode = builder.getFirstNodes().get(e);
			String gvFile = FilenameUtils.concat(outputDir.getAbsolutePath(), e.getOriginalName() + ".dot");
			try {
				PrintStream out = new PrintStream(gvFile);
				out.println("digraph " + e.getOriginalName() + " {");
				firstNode.clearVisited();
				firstNode.renderGraphviz(out);
				for (TransitionIndexes tri : rec.getRecordedTransitions()) {
					out.println(tri.start + " -> " + tri.end + " [label=\"step_" + tri.index + "\", color=" + LUMPED_COLOR + "];");
				}
				out.println("}");
				out.close();
				DotUtil.runDot(new File(gvFile));
			}
			catch (IOException ex) {
				System.err.println("Failed to render Graphviz file '" + gvFile + "' for entity " + e.getOriginalName() + ": " + ex.getMessage());
				Debug.logger.error("Failed to render Graphiv file '" + gvFile + " for entity " + e.getOriginalName() + ".", ex);
			}
		}
	}
}
