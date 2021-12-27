package de.blablubbabc.insigns;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.lang.Validate;

public final class ClassUtils {

	private static final String CLASS_FILE_EXTENSION = ".class";

	/**
	 * Loads all classes from the given jar file.
	 * 
	 * @param jarFile
	 *            the jar file, not <code>null</code>
	 * @param filter
	 *            only classes whose names are accepted by this filter are loaded
	 * @param logger
	 *            the logger that is used to log warnings when classes cannot be loaded
	 * @return <code>true</code> on (potentially partial) success and <code>false</code> on failure
	 */
	public static boolean loadAllClassesFromJar(File jarFile, Predicate<String> filter, Logger logger) {
		Validate.notNull(jarFile, "jarFile is null");
		if (filter == null) filter = (s) -> true;

		try (ZipInputStream jar = new ZipInputStream(new FileInputStream(jarFile))) {
			for (ZipEntry entry = jar.getNextEntry(); entry != null; entry = jar.getNextEntry()) {
				if (entry.isDirectory()) continue;
				String entryName = entry.getName();
				if (!entryName.endsWith(CLASS_FILE_EXTENSION)) continue;

				// Check the filter:
				String className = entryName.substring(0, entryName.length() - CLASS_FILE_EXTENSION.length()).replace('/', '.');
				if (!filter.test(className)) {
					continue;
				}

				// Try to load the class:
				try {
					Class.forName(className);
				} catch (LinkageError | ClassNotFoundException e) {
					// ClassNotFoundException: Not expected here.
					// LinkageError: If some class dependency could not be found.
					logger.log(Level.WARNING, "Could not load class '" + className + "' from jar file '" + jarFile.getPath() + "'.", e);
					// Continue loading any other remaining classes.
				}
			}
		} catch (IOException e) {
			logger.log(Level.WARNING, "Could not load classes from jar file '" + jarFile.getPath() + "'.", e);
			return false;
		}
		return true;
	}

	private ClassUtils() {
	}
}
