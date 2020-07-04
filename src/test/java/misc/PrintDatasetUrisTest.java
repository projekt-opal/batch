package misc;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.NodeIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.DCAT;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dice_research.opal.batch.Batch;
import org.dice_research.opal.batch.TestFiles;
import org.dice_research.opal.batch.configuration.Cfg;
import org.dice_research.opal.batch.configuration.CfgKeys;
import org.dice_research.opal.batch.processor.Processors;
import org.dice_research.opal.common.interfaces.ModelProcessor;
import org.junit.Assume;
import org.junit.Test;

/**
 * This test is an example for using the OPAL batch component to work with RDF
 * files using DCAT.
 * 
 * The {@link #test()} method sets the input and output data.
 * 
 * The {@link #processModel(Model, String)} method will be called for every DCAT
 * dataset model.
 * 
 * @author Adrian Wilke
 */
public class PrintDatasetUrisTest implements ModelProcessor {

	private static final boolean SKIP_TEST = true;

	private static final Logger LOGGER = LogManager.getLogger();

	@Test
	public void test() throws Exception {

		// This test will not be executed, if SKIP_TEST is true.
		// To execute, set the value of SKIP_TEST to false.
		Assume.assumeFalse("Example test", SKIP_TEST);

		// The following variables ensure to have some working data for the example.
		// You can also use other inputs and outputs.
		File inputFile = TestFiles.getFile(TestFiles.EDP_2019_12_17_MED_KODIERUNGSHANDBUCH_NQ);
		String graphName = TestFiles.DEFAULT_GRAPH_NAME;
		File outputFile = File.createTempFile(PrintDatasetUrisTest.class.getName(), ".ttl");

		// If you want to have a look into the generated data, remove the following
		// line.
		outputFile.deleteOnExit();

		// Instead of providing a properties file, we create a configuration manually.
		// The graph name is only required, as we read a N-Quads file.
		Cfg cfg = new Cfg();
		cfg.set(CfgKeys.IO_INPUT, inputFile.getAbsolutePath());
		cfg.set(CfgKeys.IO_INPUT_GRAPH, graphName);
		// TODO
//		cfg.set(CfgKeys.IO_OUTPUT, outputFile.getAbsolutePath());

		// Info strings for the execution.
		LOGGER.info("Input data:  " + cfg.get(CfgKeys.IO_INPUT));
		LOGGER.info("Input graph: " + cfg.get(CfgKeys.IO_INPUT_GRAPH));
		// TODO
//		LOGGER.info("Output data: " + cfg.get(CfgKeys.IO_OUTPUT));

		// We will only use this class as a processor.
		Processors processors = new Processors();
		processors.addModelProcessor(this);

		// Read, process, write.
		new Batch().execute(cfg, processors);
	}

	@Override
	public void processModel(Model model, String datasetUri) throws Exception {
		// This method is called for a dataset identified with the datasetUri.
		// All related data is contained in the model.
		// All changes of the model would be written to the output file.

		Resource dataset = model.getResource(datasetUri);

		List<String> distributions = new LinkedList<>();
		NodeIterator nodeIterator = model.listObjectsOfProperty(dataset, DCAT.distribution);
		while (nodeIterator.hasNext()) {
			distributions.add(nodeIterator.next().toString());
		}

		LOGGER.info(datasetUri + " " + model.size() + " " + distributions);
	}
}