package org.dice_research.opal.batch;

import java.io.File;
import java.nio.file.Files;

import org.dice_research.opal.batch.configuration.Cfg;
import org.dice_research.opal.batch.configuration.CfgKeys;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests, if output files are created.
 *
 * @author Adrian Wilke
 */
public class ReadWriteTest {

	@Test
	public void testNquadFile() throws Exception {
		File inputFile = TestFiles.getFile(TestFiles.EDP_2019_12_17_MED_KODIERUNGSHANDBUCH_NQ);

		File outputFile = File.createTempFile(ReadWriteTest.class.getName(), ".txt");
		outputFile.deleteOnExit();

		Cfg cfg = new Cfg();
		cfg.set(CfgKeys.IO_INPUT, inputFile.getAbsolutePath());
		cfg.set(CfgKeys.IO_INPUT_GRAPH, TestFiles.DEFAULT_GRAPH_NAME);
		cfg.set(CfgKeys.IO_OUTPUT, outputFile.getAbsolutePath());

		new Batch().execute(cfg);

		Assert.assertTrue(outputFile.length() > 0);
	}

	@Test
	public void testTurtleFile() throws Exception {
		File inputFile = TestFiles.getFile(TestFiles.EDP_2019_12_17_MED_KODIERUNGSHANDBUCH_TTL);

		File outputFile = File.createTempFile(ReadWriteTest.class.getName(), ".txt");
		outputFile.deleteOnExit();

		Cfg cfg = new Cfg();
		cfg.set(CfgKeys.IO_INPUT, inputFile.getAbsolutePath());
		cfg.set(CfgKeys.IO_OUTPUT, outputFile.getAbsolutePath());

		new Batch().execute(cfg);

		Assert.assertTrue(outputFile.length() > 0);
		Assert.assertTrue(outputFile.length() > inputFile.length());
	}

	@Test
	public void testNquadFiles() throws Exception {
		File inputDir = TestFiles.getFile(TestFiles.EDP_2019_12_17_MED_KODIERUNGSHANDBUCH_NQ).getParentFile();

		File outputDir = Files.createTempDirectory(this.getClass().getName()).toFile();

		Cfg cfg = new Cfg();
		cfg.set(CfgKeys.IO_INPUT, inputDir.getAbsolutePath());
		cfg.set(CfgKeys.IO_INPUT_GRAPH, TestFiles.DEFAULT_GRAPH_NAME);
		cfg.set(CfgKeys.IO_OUTPUT, outputDir.getAbsolutePath());

		new Batch().execute(cfg);

		Assert.assertEquals(inputDir.listFiles().length, outputDir.listFiles().length);

		for (File outputFile : outputDir.listFiles()) {
			outputFile.delete();
		}
		outputDir.delete();
	}

	@Test
	public void testTurtleFiles() throws Exception {
		File inputDir = TestFiles.getFile(TestFiles.EDP_2019_12_17_MED_KODIERUNGSHANDBUCH_TTL).getParentFile();

		File outputDir = Files.createTempDirectory(this.getClass().getName()).toFile();

		Cfg cfg = new Cfg();
		cfg.set(CfgKeys.IO_INPUT, inputDir.getAbsolutePath());
		cfg.set(CfgKeys.IO_OUTPUT, outputDir.getAbsolutePath());

		new Batch().execute(cfg);

		Assert.assertEquals(inputDir.listFiles().length, outputDir.listFiles().length);

		for (File outputFile : outputDir.listFiles()) {
			outputFile.delete();
		}
		outputDir.delete();
	}
}