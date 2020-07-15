package org.dice_research.opal.batch;

import java.io.File;

import org.apache.jena.rdf.model.Model;
import org.dice_research.opal.batch.configuration.Cfg;
import org.dice_research.opal.batch.configuration.CfgKeys;
import org.dice_research.opal.batch.construction.ConstructorManager;
import org.dice_research.opal.batch.construction.IndependentConstructor;
import org.dice_research.opal.batch.utils.CfgUtils;
import org.dice_research.opal.batch.utils.FileUtils;
import org.dice_research.opal.batch.utils.TestFiles;
import org.dice_research.opal.batch.writer.elasticsearch.ElasticsearchWriter;
import org.dice_research.opal.common.interfaces.ModelProcessor;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests {@link ElasticsearchWriter}.
 *
 * @author Adrian Wilke
 */
public class ElasticsearchWriterTest implements ModelProcessor {

	private ElasticsearchWriter writer;

	@Before
	public void setUp() throws Exception {
		writer = new ElasticsearchWriter();
	}

	@After
	public void tearDown() throws Exception {
	}
	
	@Test
	public void test() throws Exception {

		// TODO
//		TestCase testCase = OpalTestCases.getTestCase("edp-2019-12-17", "med-kodierungshandbuch.nt");
//		Model model = ModelFactory.createDefaultModel().add(testCase.getModel());
//		String datasetUri = testCase.getDatasetUri();

		Cfg cfg = new CfgUtils().disableOutputWriting().disableAddingLabelsFile().disableOpal()
				.disableDatasetUriRewriting().getCfg();

		File input = TestFiles.getFile(TestFiles.EDP_2019_12_17_MED_KODIERUNGSHANDBUCH_TTL);
		cfg.set(CfgKeys.IO_INPUT, input.getAbsolutePath());
		File outputDir = FileUtils.createtmpDirectory(getClass());
		cfg.set(CfgKeys.IO_OUTPUT_DIRECTORY, outputDir.getAbsolutePath());

		ConstructorManager constructorManager = new ConstructorManager()
				.addConstructor(new IndependentConstructor(this));

		new Batch().execute(cfg, constructorManager);

		if (Boolean.TRUE) {
			FileUtils.deleteDirectory(outputDir);
		}

	}

	@Override
	public void processModel(Model model, String datasetUri) throws Exception {
		writer.processModel(model, datasetUri);
	}

}