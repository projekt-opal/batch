package org.dice_research.opal.batch;

import java.io.File;

import org.dice_research.opal.batch.configuration.Cfg;
import org.dice_research.opal.batch.configuration.CfgKeys;
import org.dice_research.opal.batch.utils.CfgUtils;
import org.dice_research.opal.batch.utils.FileUtils;
import org.dice_research.opal.batch.utils.TestFiles;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests, if different inputs are read by checking if the respective output
 * files are created.
 *
 * @author Adrian Wilke
 */
public class ReadWriteTest {

	@Test
	public void testNquadFile() throws Exception {
		File inputFile = TestFiles.getFile(TestFiles.EDP_2019_12_17_MED_KODIERUNGSHANDBUCH_NQ);
		File outputDir = FileUtils.createtmpDirectory(getClass());

		Cfg cfg = new CfgUtils().disableDatasetUriRewriting().getCfg();
		cfg.set(CfgKeys.IO_INPUT, inputFile.getAbsolutePath());
		cfg.set(CfgKeys.IO_INPUT_GRAPH, TestFiles.DEFAULT_GRAPH_NAME);
		cfg.set(CfgKeys.IO_OUTPUT_DIRECTORY, outputDir.getAbsolutePath());
		cfg.set(CfgKeys.IO_OUTPUT_TITLE, this.getClass().getName());

		new Batch().execute(cfg);

		Assert.assertTrue(outputDir.listFiles().length > 0);

		FileUtils.deleteDirectory(outputDir);
	}

	@Test
	public void testTurtleFile() throws Exception {
		File inputFile = TestFiles.getFile(TestFiles.EDP_2019_12_17_MED_KODIERUNGSHANDBUCH_TTL);
		File outputDir = FileUtils.createtmpDirectory(getClass());

		Cfg cfg = new CfgUtils().disableDatasetUriRewriting().getCfg();
		cfg.set(CfgKeys.IO_INPUT, inputFile.getAbsolutePath());
		cfg.set(CfgKeys.IO_INPUT_GRAPH, TestFiles.DEFAULT_GRAPH_NAME);
		cfg.set(CfgKeys.IO_OUTPUT_DIRECTORY, outputDir.getAbsolutePath());
		cfg.set(CfgKeys.IO_OUTPUT_TITLE, this.getClass().getName());

		new Batch().execute(cfg);

		Assert.assertTrue(outputDir.listFiles().length > 0);

		FileUtils.deleteDirectory(outputDir);
	}

	@Test
	public void testNquadFiles() throws Exception {
		File inputDir = TestFiles.getFile(TestFiles.EDP_2019_12_17_MED_KODIERUNGSHANDBUCH_NQ).getParentFile();
		File outputDir = FileUtils.createtmpDirectory(getClass());

		Cfg cfg = new CfgUtils().disableDatasetUriRewriting().getCfg();
		cfg.set(CfgKeys.IO_INPUT, inputDir.getAbsolutePath());
		cfg.set(CfgKeys.IO_INPUT_GRAPH, TestFiles.DEFAULT_GRAPH_NAME);
		cfg.set(CfgKeys.IO_OUTPUT_DIRECTORY, outputDir.getAbsolutePath());
		cfg.set(CfgKeys.IO_OUTPUT_TITLE, this.getClass().getName());

		new Batch().execute(cfg);

		Assert.assertTrue(outputDir.listFiles().length > 0);

		FileUtils.deleteDirectory(outputDir);
	}

	@Test
	public void testTurtleFiles() throws Exception {
		File inputDir = TestFiles.getFile(TestFiles.EDP_2019_12_17_MED_KODIERUNGSHANDBUCH_TTL).getParentFile();
		File outputDir = FileUtils.createtmpDirectory(getClass());

		Cfg cfg = new CfgUtils().disableDatasetUriRewriting().getCfg();
		cfg.set(CfgKeys.IO_INPUT, inputDir.getAbsolutePath());
		cfg.set(CfgKeys.IO_INPUT_GRAPH, TestFiles.DEFAULT_GRAPH_NAME);
		cfg.set(CfgKeys.IO_OUTPUT_DIRECTORY, outputDir.getAbsolutePath());
		cfg.set(CfgKeys.IO_OUTPUT_TITLE, this.getClass().getName());

		new Batch().execute(cfg);

		Assert.assertTrue(outputDir.listFiles().length > 0);

		FileUtils.deleteDirectory(outputDir);
	}

}