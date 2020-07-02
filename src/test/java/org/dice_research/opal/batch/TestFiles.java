package org.dice_research.opal.batch;

import java.io.File;
import java.util.Arrays;
import java.util.TreeSet;

/**
 * List of test resources.
 * 
 * Use {@link #getFile(String)} and constant to get a File.
 * 
 * Use {@link #main(String[])} to generate Java code.
 *
 * @author Adrian Wilke
 */
public abstract class TestFiles {

	public static final String EDP_2019_12_17_MED_KODIERUNGSHANDBUCH_NQ = "edp-2019-12-17-med-kodierungshandbuch.nq";
	public static final String EDP_2019_12_17_MED_KODIERUNGSHANDBUCH_TTL = "edp-2019-12-17-med-kodierungshandbuch.ttl";
	public static final String OPAL_2019_06_24_MCLOUD_MOERS_INNENSTADT_NQ = "opal-2019-06-24-mcloud-moers-innenstadt.nq";
	public static final String OPAL_2019_06_24_MCLOUD_MOERS_INNENSTADT_TTL = "opal-2019-06-24-mcloud-moers-innenstadt.ttl";

	public static final String DEFAULT_GRAPH_NAME = "http://projekt-opal.de";

	public static final File TEST_RESOURCES_DIRECTORY = new File("src/test/resources");

	public static File getFile(String filename) {
		File dir = new File(TEST_RESOURCES_DIRECTORY, filename.substring(filename.lastIndexOf('.') + 1));
		File file = new File(dir, filename);
		if (!file.canRead()) {
			throw new RuntimeException("Can not read file: " + file.getAbsolutePath());
		}
		return file;
	}

	public static TreeSet<File> getFiles() {
		return new TreeSet<File>(Arrays.asList(TEST_RESOURCES_DIRECTORY.listFiles()));
	}

	public static void main(String[] args) {
		TestFiles.printJava();
	}

	private static void printJava() {
		StringBuilder stringBuilder = new StringBuilder();
		for (File file : getFiles()) {
			if (file.isFile()) {
				stringBuilder.append("public static final String ");
				stringBuilder.append(file.getName().replace(".", "_").replace("-", "_").toUpperCase());
				stringBuilder.append(" = \"");
				stringBuilder.append(file.getName());
				stringBuilder.append("\";");
				stringBuilder.append(System.lineSeparator());
			}
		}
		System.out.println(stringBuilder);
	}

}