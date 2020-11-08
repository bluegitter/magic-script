package org.ssssssss.script;

import java.util.HashSet;
import java.util.Set;

public class MagicPackageLoader {

	private static final Set<String> packages = new HashSet<>();

	static {
		packages.add("java.util.*");
		packages.add("java.lang.*");
	}

	public static void addPackage(String prefix) {
		packages.add(prefix.replace("*", ""));
	}

	public static Class<?> findClass(String simpleName) {
		for (String prefix : packages) {
			try {
				return Class.forName(prefix + simpleName);
			} catch (Exception ignored) {
			}
		}
		return null;
	}
}
