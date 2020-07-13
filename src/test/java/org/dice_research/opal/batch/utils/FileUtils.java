package org.dice_research.opal.batch.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * Methods to handle temporary directories.
 *
 * @author Adrian Wilke
 */
public abstract class FileUtils {

	public static File createtmpDirectory(String directoryName) {
		try {
			return Files.createTempDirectory(directoryName).toFile();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static File createtmpDirectory(Class<?> classObject) {
		try {
			return Files.createTempDirectory(classObject.getPackageName() + "." + classObject.getName() + ".").toFile();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static void deleteDirectory(File directory) {
		for (File file : directory.listFiles()) {
			file.delete();
		}
		directory.delete();
	}
}