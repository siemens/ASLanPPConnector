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

package avantssar.aslanpp.testing;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.avantssar.aslanpp.Debug;

public class DiskSpecificationsProvider extends ArrayList<ITestTask> implements ISpecificationBundleProvider {

	private static final long serialVersionUID = 7936526134207024251L;
	private final String aslanPath;

	public DiskSpecificationsProvider(String baseDirectory) {
		File base = new File(baseDirectory);
		if (!base.exists() || !base.isDirectory()) {
			throw new IllegalArgumentException("Path '" + baseDirectory + "' cannot be accessed or does not point to a directory.");
		}
		IOFileFilter filter = FileFilterUtils.suffixFileFilter(".aslan++");
		IOFileFilter dirFilter = FileFilterUtils.makeCVSAware(FileFilterUtils.makeSVNAware(null));
		Collection<File> specs = FileUtils.listFiles(base, filter, dirFilter);
		List<String> forSort = new ArrayList<String>();
		SortedSet<String> forASLanPath = new TreeSet<String>();
		for (File f : specs) {
			if (isSpec(f)) {
				forSort.add(f.getAbsolutePath());
				forASLanPath.add(f.getParent());
			}
		}
		Collections.sort(forSort);
		for (String s : forSort) {
			add(new DiskTestTask(new File(s)));
		}
		String temp = "";
		for (String s : forASLanPath) {
			if (temp.length() > 0) {
				temp += File.pathSeparator;
			}
			temp += s;
		}
		aslanPath = temp;
	}

	public String getASLanPath() {
		return aslanPath;
	}

	private boolean isSpec(File f) {
		boolean isSpecification = false;
		try {
			LineIterator li = FileUtils.lineIterator(f);
			while (li.hasNext()) {
				String line = li.nextLine();
				if (line.trim().length() > 0) {
					if (line.trim().startsWith("%")) {
						continue;
					}
					else {
						if (line.trim().startsWith("specification")) {
							isSpecification = true;
						}
						break;
					}
				}
			}
		}
		catch (IOException e) {
			Debug.logger.error("Failed to decide if specification or not: " + f.getAbsolutePath(), e);
		}
		return isSpecification;
	}

}
