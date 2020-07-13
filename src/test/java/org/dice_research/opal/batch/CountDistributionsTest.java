package org.dice_research.opal.batch;

import java.io.File;
import java.util.Set;
import java.util.TreeSet;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.vocabulary.DCAT;
import org.dice_research.opal.batch.configuration.Cfg;
import org.dice_research.opal.batch.configuration.CfgKeys;
import org.dice_research.opal.batch.construction.ConstructorManager;
import org.dice_research.opal.batch.construction.IndependentConstructor;
import org.dice_research.opal.batch.utils.CfgUtils;
import org.dice_research.opal.batch.utils.FileUtils;
import org.dice_research.opal.batch.utils.TestFiles;
import org.dice_research.opal.common.interfaces.ModelProcessor;
import org.junit.Assert;
import org.junit.Test;

/**
 * Simple test to demonstrate how to process data using the
 * {@link ModelProcessor} interface and the {@link Batch} component.
 *
 * Edit {@link #processModel(Model, String)} to specify your desired data
 * handling.
 * 
 * If you want to process specific data, edit {@link #input}.
 * 
 * If you want to check the processed data, set {@link #write}.
 *
 * @author Adrian Wilke
 */
public class CountDistributionsTest implements ModelProcessor {

	// Specify the file or directory to read
	File input = TestFiles.getFile(TestFiles.EDP_2019_12_17_MED_KODIERUNGSHANDBUCH_NQ);

	String graphName = TestFiles.DEFAULT_GRAPH_NAME;

	// Specify if the processed data will be written to files.
	boolean write = false;

	/**
	 * Will be called for every dataset.
	 */
	@Override
	public void processModel(Model model, String datasetUri) throws Exception {
		Set<String> distributions = new TreeSet<>();

		StmtIterator stmtIterator = model.getResource(datasetUri).listProperties(DCAT.distribution);
		while (stmtIterator.hasNext()) {
			distributions.add(stmtIterator.next().getObject().toString());
		}

		Assert.assertEquals("Dataset contains 6 distributiuons", 6, distributions.size());
	}

	@Test
	public void test() throws Exception {

		// Create configuration which does not rewrite dataset URIs and does not create
		// a file containing labels
		Cfg cfg = new CfgUtils().disableDatasetUriRewriting().disableAddingLabelsFile().getCfg();

		// Disable writing of RDF data
		if (!write) {
			CfgUtils.disableOutputWriting(cfg);
		}

		// Set required input and output
		cfg.set(CfgKeys.IO_INPUT, input.getAbsolutePath());
		cfg.set(CfgKeys.IO_INPUT_GRAPH, graphName);
		File outputDir = FileUtils.createtmpDirectory(getClass());
		cfg.set(CfgKeys.IO_OUTPUT_DIRECTORY, outputDir.getAbsolutePath());

		// While processing data use only this test
		ConstructorManager constructorManager = new ConstructorManager()
				.addConstructor(new IndependentConstructor(this));

		// Process data
		new Batch().execute(cfg, constructorManager);

		// Delete output directory
		if (!write) {
			FileUtils.deleteDirectory(outputDir);
		}
	}

}