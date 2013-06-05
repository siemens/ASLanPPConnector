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
import java.lang.reflect.Constructor;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import org.avantssar.commons.ChannelEntry;
import org.avantssar.commons.ChannelModel;
import org.avantssar.commons.ChannelEntry.Type;
import avantssar.aslanpp.library.AbstractSpecProvider;

public class LibrarySpecificationsProvider extends ArrayList<ITestTask> implements ISpecificationBundleProvider {

	private static final long serialVersionUID = -8462043346380779035L;

	private final File outputDir;

	public LibrarySpecificationsProvider(String outputDirStr) {
		this.outputDir = new File(outputDirStr);
		if (!this.outputDir.exists() || !this.outputDir.isDirectory()) {
			throw new IllegalArgumentException("Path '" + outputDir + "' does not exist or does not point to a directory.");
		}

		Class<?>[] allClasses = getClasses(AbstractSpecProvider.class.getPackage().getName());
		SortedMap<String, Class<?>> temp = new TreeMap<String, Class<?>>();
		for (Class<?> c : allClasses) {
			if (ISpecProvider.class.isAssignableFrom(c) && c.isAnnotationPresent(Specification.class)) {
				// if
				// (c.getSimpleName().startsWith("SessionSecrecyGoalInEnvironment"))
				// {
				temp.put(c.getName(), c);
				// }
			}
		}
		ChannelModel[] cms = new ChannelModel[] { ChannelModel.CCM, ChannelModel.ACM };
		for (Class<?> clz : temp.values()) {
			try {
				Constructor<?> c = clz.getConstructor(new Class<?>[] {});
				if (IChannelTypeFlexibleSpecProvider.class.isAssignableFrom(clz)) {
					for (ChannelModel cm : cms) {
						for (Type t : Type.values()) {
							IChannelTypeFlexibleSpecProvider specProvider = (IChannelTypeFlexibleSpecProvider) c.newInstance(new Object[] {});
							specProvider.setChannelModel(cm);
							specProvider.setChannelType(ChannelEntry.from(t, false, false, false, null));
							try {
								add(new LibraryTestTask(specProvider, outputDir));
							}
							catch (IOException e) {
								System.out.println("Cannot create library task for " + clz.getCanonicalName() + " with channel model " + cm.toString() + " and channel type " + t.toString());
							}
						}
					}
				}
				else if (IChannelModelFlexibleSpecProvider.class.isAssignableFrom(clz)) {
					for (ChannelModel cm : cms) {
						IChannelModelFlexibleSpecProvider specProvider = (IChannelModelFlexibleSpecProvider) c.newInstance(new Object[] {});
						specProvider.setChannelModel(cm);
						try {
							add(new LibraryTestTask(specProvider, outputDir));
						}
						catch (IOException e) {
							System.out.println("Cannot create library task for " + clz.getCanonicalName() + " with channel model " + cm.toString() + ".");
						}
					}
				}
				else if (ISpecProvider.class.isAssignableFrom(clz)) {
					ISpecProvider specProvider = (ISpecProvider) c.newInstance(new Object[] {});
					try {
						add(new LibraryTestTask(specProvider, outputDir));
					}
					catch (IOException e) {
						System.out.println("Cannot create library task for " + clz.getCanonicalName() + ".");
					}
				}
			}
			catch (Exception e) {
				e.printStackTrace();
				System.out.println("Failed to run constructor of class " + clz.getCanonicalName() + ": " + e.getMessage());
			}
		}
	}

	@Override
	public String getASLanPath() {
		return outputDir.getAbsolutePath();
	}

	// Taken from http://snippets.dzone.com/posts/show/4831
	private static Class<?>[] getClasses(String packageName) {
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		assert classLoader != null;
		String path = packageName.replace('.', '/');
		Enumeration<URL> resources;
		ArrayList<Class<?>> classes = new ArrayList<Class<?>>();
		try {
			resources = classLoader.getResources(path);
			while (resources.hasMoreElements()) {
				URL resource = resources.nextElement();
				if ("jar".equals(resource.getProtocol())) {
					JarURLConnection juc = (JarURLConnection) resource.openConnection();
					if (juc != null) {
						JarFile jf = juc.getJarFile();
						if (jf != null) {
							Enumeration<JarEntry> entries = jf.entries();
							while (entries.hasMoreElements()) {
								JarEntry entry = entries.nextElement();
								if (!entry.isDirectory() && entry.getName().startsWith(juc.getEntryName())) {
									String clsName = entry.getName().replaceAll("/", ".");
									try {
										classes.add(Class.forName(clsName.substring(0, clsName.length() - 6)));
									}
									catch (ClassNotFoundException e) {
										System.out.println("Failed to load class from file '" + entry.getName() + "'.");
									}
								}
							}
						}
					}
				}
				else {
					classes.addAll(findClasses(new File(resource.getFile()), packageName));
				}
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		return classes.toArray(new Class[classes.size()]);
	}

	private static List<Class<?>> findClasses(File directory, String packageName) {
		List<Class<?>> classes = new ArrayList<Class<?>>();
		if (!directory.exists()) {
			return classes;
		}
		File[] files = directory.listFiles();
		for (File file : files) {
			if (file.isDirectory()) {
				assert !file.getName().contains(".");
				classes.addAll(findClasses(file, packageName + "." + file.getName()));
			}
			else if (file.getName().endsWith(".class")) {
				try {
					classes.add(Class.forName(packageName + '.' + file.getName().substring(0, file.getName().length() - 6)));
				}
				catch (ClassNotFoundException e) {
					System.out.println("Failed to load class from file '" + file.getAbsolutePath() + "'.");
				}
			}
		}
		return classes;
	}

	public static void main(String[] args) {
		Class<?>[] clss = getClasses(AbstractSpecProvider.class.getPackage().getName());
		System.out.println("Got " + clss.length + " classes");
		for (Class<?> c : clss) {
			System.out.println(c.getCanonicalName());
		}
	}
}
