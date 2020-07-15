package org.dice_research.opal.batch.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFLanguages;

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

	public static File createTmpModelFile(Model model, Class<?> classObject, boolean deleteOnExit) {
		try {
			File file = File.createTempFile(classObject.getPackageName() + "." + classObject.getName() + ".", ".ttl");
			if (deleteOnExit) {
				file.deleteOnExit();
			}
			RDFDataMgr.write(new FileOutputStream(file), model, RDFLanguages.TURTLE);
			return file;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}