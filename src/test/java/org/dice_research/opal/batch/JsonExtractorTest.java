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
import org.dice_research.opal.batch.writer.ElasticsearchJson;
import org.dice_research.opal.common.interfaces.ModelProcessor;
import org.dice_research.opal.test_cases.OpalTestCases;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests {@link ElasticsearchJson}.
 *
 * @author Adrian Wilke
 */
public class JsonExtractorTest implements ModelProcessor {

	private ElasticsearchJson jsonExtractor;

	@Test
	public void testKodierungshandbuch() throws Exception {
		jsonExtractor = new ElasticsearchJson();

		Cfg cfg = new CfgUtils().disableWriting().disableAddingLabelsFile().disableOpalComponents()
				.disableUriRewriting().getCfg();

		File input = TestFiles.getFile(TestFiles.EDP_2019_12_17_MED_KODIERUNGSHANDBUCH_TTL);
		cfg.set(CfgKeys.IO_INPUT, input.getAbsolutePath());

		File outputDir = FileUtils.createtmpDirectory(getClass());
		cfg.set(CfgKeys.IO_OUTPUT_DIRECTORY, outputDir.getAbsolutePath());

		ConstructorManager constructorManager = new ConstructorManager()
				.addConstructor(new IndependentConstructor(this));

		new Batch().execute(cfg, constructorManager);

		Assert.assertTrue(jsonExtractor.getJson().length() > 3000);

		if (Boolean.FALSE) {
			System.out.println(jsonExtractor.getJson(2));
		}

		FileUtils.deleteDirectory(outputDir);
	}

	@Test
	public void testZinkbelastung() throws Exception {
		jsonExtractor = new ElasticsearchJson();

		Cfg cfg = new CfgUtils().disableWriting().disableAddingLabelsFile().disableOpalComponents()
				.disableUriRewriting().getCfg();

		Model model = OpalTestCases.getTestCase("edp-2020-06-06", "zinkbelastung").getModel();
		File input = FileUtils.createTmpModelFile(model, getClass(), true);
		cfg.set(CfgKeys.IO_INPUT, input.getAbsolutePath());

		File outputDir = FileUtils.createtmpDirectory(getClass());
		cfg.set(CfgKeys.IO_OUTPUT_DIRECTORY, outputDir.getAbsolutePath());

		ConstructorManager constructorManager = new ConstructorManager()
				.addConstructor(new IndependentConstructor(this));

		new Batch().execute(cfg, constructorManager);

		String json = jsonExtractor.getJson();
		Assert.assertTrue(json.length() > 3000);

		// License URL
		Assert.assertTrue(json.contains("http://dcat-ap.de/def/licenses/other-closed"));

		if (Boolean.FALSE) {
			model.write(System.out, "TTL");
			System.out.println("---");
			System.out.println(jsonExtractor.getJson(2));
		}

		FileUtils.deleteDirectory(outputDir);
	}

	@Test
	public void testKriminalstatistik() throws Exception {
		jsonExtractor = new ElasticsearchJson();

		Cfg cfg = new CfgUtils().disableWriting().disableAddingLabelsFile().disableOpalComponents()
				.disableUriRewriting().getCfg();

		Model model = OpalTestCases.getTestCase("edp-2020-06-06", "kriminalstatistik").getModel();
		File input = FileUtils.createTmpModelFile(model, getClass(), true);
		cfg.set(CfgKeys.IO_INPUT, input.getAbsolutePath());

		File outputDir = FileUtils.createtmpDirectory(getClass());
		cfg.set(CfgKeys.IO_OUTPUT_DIRECTORY, outputDir.getAbsolutePath());

		ConstructorManager constructorManager = new ConstructorManager()
				.addConstructor(new IndependentConstructor(this));

		new Batch().execute(cfg, constructorManager);

		Assert.assertTrue(jsonExtractor.getJson().length() > 3000);

		if (Boolean.FALSE) {
			model.write(System.out, "TTL");
			System.out.println(jsonExtractor.getJson(2));
		}

		FileUtils.deleteDirectory(outputDir);
	}

	@Override
	public void processModel(Model model, String datasetUri) throws Exception {
		jsonExtractor.processModel(model, datasetUri);
	}

}