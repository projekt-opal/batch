package misc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFLanguages;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dice_research.opal.batch.TestFiles;
import org.dice_research.opal.test_cases.OpalTestCases;
import org.junit.Assume;
import org.junit.Test;

/**
 * This test extracts DCAT date formats.
 * 
 * Usage: Specify input source (optional: and input graph) in {@link #test()}.
 *
 * @author Adrian Wilke
 */
public class CreateExampleFilesTest {

	private static final boolean SKIP_TEST = true;

	private static final Logger LOGGER = LogManager.getLogger();

	Set<String> datatypeUris = new TreeSet<>();
	Map<String, Long> stringFormats = new HashMap<>();

	@Test
	public void test() throws Exception {

		// Does not test functionality
		Assume.assumeFalse("Development test (Creating test files)", SKIP_TEST);

		printTestCases();

		String setId, testId, extension, graphName;
		Model model;
		Dataset dataset;
		Lang lang;
		File file;
		File testResourcesDirectory = new File("src/test/resources");

		if (Boolean.FALSE) {
			setId = "edp-2019-12-17";
			testId = "med-kodierungshandbuch";
			extension = "nq";
			lang = RDFLanguages.NQUADS;
			graphName = TestFiles.DEFAULT_GRAPH_NAME;
			file = getFile(testResourcesDirectory, setId, testId, extension);
			model = OpalTestCases.getTestCase(setId, testId).getModel();
			dataset = DatasetFactory.create();
			dataset.addNamedModel(graphName, model);
			LOGGER.info("Writing: " + file.getAbsolutePath());
			try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
				RDFDataMgr.write(fileOutputStream, dataset, lang);
			}
		}

		if (Boolean.FALSE) {
			setId = "opal-2019-06-24";
			testId = "mcloud-moers-innenstadt";
			extension = "nq";
			lang = RDFLanguages.NQUADS;
			graphName = TestFiles.DEFAULT_GRAPH_NAME;
			file = getFile(testResourcesDirectory, setId, testId, extension);
			model = OpalTestCases.getTestCase(setId, testId).getModel();
			dataset = DatasetFactory.create();
			dataset.addNamedModel(graphName, model);
			LOGGER.info("Writing: " + file.getAbsolutePath());
			try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
				RDFDataMgr.write(fileOutputStream, dataset, lang);
			}
		}

		if (Boolean.FALSE) {
			setId = "edp-2019-12-17";
			testId = "med-kodierungshandbuch";
			extension = "ttl";
			lang = RDFLanguages.TURTLE;
			graphName = null;
			file = getFile(testResourcesDirectory, setId, testId, extension);
			model = OpalTestCases.getTestCase(setId, testId).getModel();
			LOGGER.info("Writing: " + file.getAbsolutePath());
			try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
				RDFDataMgr.write(fileOutputStream, model, lang);
			}
		}

		if (Boolean.FALSE) {
			setId = "opal-2019-06-24";
			testId = "mcloud-moers-innenstadt";
			extension = "ttl";
			lang = RDFLanguages.TURTLE;
			graphName = null;
			file = getFile(testResourcesDirectory, setId, testId, extension);
			model = OpalTestCases.getTestCase(setId, testId).getModel();
			LOGGER.info("Writing: " + file.getAbsolutePath());
			try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
				RDFDataMgr.write(fileOutputStream, model, lang);
			}
		}

	}

	private File getFile(File directory, String setId, String testId, String extension) {
		File dir = new File(directory, extension);
		dir.mkdirs();
		return new File(dir, setId + "-" + testId + "." + extension);
	}

	private void printTestCases() throws URISyntaxException, IOException {
		StringBuilder stringBuilder = new StringBuilder();
		SortedSet<String> setIds = OpalTestCases.listTestSets();
		stringBuilder.append("Available Set-IDs and Test-IDs:");
		stringBuilder.append(System.lineSeparator());
		for (String setId : setIds) {
			SortedSet<String> testCaseIds = OpalTestCases.listTestCases(setId);
			for (String testId : testCaseIds) {
				stringBuilder.append(setId);
				stringBuilder.append(" ");
				stringBuilder.append(testId);
				stringBuilder.append(System.lineSeparator());
			}
		}
		System.out.println(stringBuilder.toString());
	}
}