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

package org.avantssar.aslanpp.model;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.avantssar.aslanpp.Debug;
import org.avantssar.commons.ChannelModel;
import org.avantssar.commons.ErrorGatherer;

public class EntityManager {

	public static final String ASLAN_EXTENSION = ".aslan++";
	public static final String ASLAN_ENVVAR = "ASLANPATH";

	// private static EntityManager theInstance = new EntityManager();

	private static List<String> aslanPathEntries = null;

	// public static EntityManager getInstance() {
	// return theInstance;
	// }

	private final Map<String, Entity> entitiesByName = new HashMap<String, Entity>();

	// public EntityManager() {
	// // private for singleton
	// }

	public void purge() {
		entitiesByName.clear();
	}

	protected void registerEntity(Entity ent) {
		if (!entitiesByName.containsKey(ent.getName())) {
			entitiesByName.put(ent.getOriginalName(), ent);
		}
	}

	public Entity getEntity(String name, ChannelModel cm) {
		if (entitiesByName.containsKey(name)) {
			return entitiesByName.get(name);
		}
		else {
			// Don't register loaded entities here. They register themselves
			// while being parsed. Otherwise we can't handle circular imports.
			ErrorGatherer err = new ErrorGatherer(ErrorMessages.DEFAULT);
			return Entity.fromFile(this, name, cm, err);
		}
	}

	protected File findFileInPath(String n) throws IOException {
		String name = n + ASLAN_EXTENSION;
		Debug.logger.trace("Searching for file '" + name + "' in path.");
		if (aslanPathEntries == null) {
			loadASLanPath();
		}

		for (int i = 0; i < aslanPathEntries.size(); i++) {
			String pathEntry = aslanPathEntries.get(i);
			File f = new File(pathEntry);
			if (f.exists() && f.isDirectory()) {
				File[] files = f.listFiles();
				for (int j = 0; j < files.length; j++) {
					if (files[j].getName().equals(name)) {
						Debug.logger.trace("File '" + name + "' found in directory '" + f.getCanonicalPath() + "'.");
						return files[j];
					}
				}
			}
		}

		Debug.logger.warn("File '" + name + "' not found in path.");
		return null;
	}

	public void loadASLanPath() throws IOException {
		Debug.logger.trace("Loading " + ASLAN_ENVVAR + " environment variable.");
		aslanPathEntries = new ArrayList<String>();
		String aslanPath = System.getProperty(ASLAN_ENVVAR);
		File currentDir = new File(".");
		boolean currentDirAdded = false;
		if (aslanPath != null) {
			Debug.logger.trace(ASLAN_ENVVAR + " environment variable is set to '" + aslanPath + "'.");
			String[] entries = aslanPath.split(File.pathSeparator);
			Debug.logger.trace(ASLAN_ENVVAR + " environment variable contains " + entries.length + " entries:");
			for (int i = 0; i < entries.length; i++) {
				Debug.logger.trace("Entry " + i + ": " + entries[i]);
				File f = new File(entries[i]);
				if (f.exists() && f.isDirectory()) {
					Debug.logger.trace("Keeping this entry: '" + f.getCanonicalPath() + "'.");
					aslanPathEntries.add(f.getCanonicalPath());
					if (currentDir.getCanonicalPath().equals(f.getCanonicalPath())) {
						currentDirAdded = true;
					}
				}
			}
		}
		else {
			Debug.logger.info("ASLANPATH environment variable is not set.");
		}
		if (!currentDirAdded) {
			aslanPathEntries.add(currentDir.getCanonicalPath());
			Debug.logger.trace("Automatically added entry for current directory: '" + currentDir.getCanonicalPath() + "'.");
		}
	}
}
