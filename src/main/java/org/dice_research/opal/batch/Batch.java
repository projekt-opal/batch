package org.dice_research.opal.batch;

import java.io.File;
import java.io.FilenameFilter;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFLanguages;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dice_research.opal.batch.configuration.Cfg;
import org.dice_research.opal.batch.configuration.CfgKeys;
import org.dice_research.opal.batch.processor.Processors;
import org.dice_research.opal.batch.reader.RdfFileReader;
import org.dice_research.opal.batch.reader.RdfReaderResult;
import org.dice_research.opal.batch.writer.DummyWriter;
import org.dice_research.opal.batch.writer.RdfFileWriter;
import org.dice_research.opal.batch.writer.RdfWriter;
import org.dice_research.opal.common.interfaces.ModelProcessor;

/**
 * Batch processing of OPAL components.
 * 
 * Usage: Specify a properties configuration file and pass it as argument.
 *
 * @author Adrian Wilke
 */
public class Batch {

	private static final Logger LOGGER = LogManager.getLogger();

	/**
	 * Main entry point.
	 */
	public static void main(String[] args) throws Exception {

		// Set default or custom configuration
		if (args.length == 0) {
			new Batch().execute(new Cfg());
		} else {
			new Batch().execute(new Cfg(new File(args[0])));
		}
	}

	private List<ModelProcessor> modelProcessors;
	private long couterProcessedModels = 0;

	public void execute(Cfg cfg) throws Exception {
		execute(cfg, new Processors().createModelProcessors(cfg));
	}

	public void execute(Cfg cfg, Processors processors) throws Exception {
		long time = System.currentTimeMillis();

		// Configure components
		this.modelProcessors = processors.getModelProcessors();

		// Configure I/O
		File inputFile = checkInput(cfg);
		File outputFile = null;
		if (!cfg.get(CfgKeys.IO_OUTPUT).trim().isEmpty()) {
			new File(cfg.get(CfgKeys.IO_OUTPUT));
		}
		String inputGraph = cfg.get(CfgKeys.IO_INPUT_GRAPH);

		// Process data
		if (inputFile.isFile()) {
			processFile(inputFile, inputGraph, outputFile, Lang.TURTLE);
		} else {
			processDirectory(inputFile, inputGraph, outputFile, Lang.TURTLE);
		}

		LOGGER.info("Run time (secs): " + 1f * (System.currentTimeMillis() - time) / 1000);
		LOGGER.info("Processed models: " + couterProcessedModels);
	}

	private File checkInput(Cfg cfg) {
		if (!cfg.has(CfgKeys.IO_INPUT)) {
			throw new RuntimeException("No input set.");
		}

		File file = new File(cfg.get(CfgKeys.IO_INPUT));
		if (!file.canRead()) {
			throw new RuntimeException("Can not read input: " + file.getAbsolutePath());
		}

		return file;
	}

	private void processDirectory(File inputDirectory, String inputGraphName, File outputDirectory, Lang outputLang) {
		if (!inputDirectory.canRead()) {
			throw new RuntimeException("Can not read: " + inputDirectory.getAbsolutePath());
		}

		// Get files
		Set<String> fileExtensions = getFileExtensions();
		File[] inputFiles = inputDirectory.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				String ext = name.substring(name.lastIndexOf(".") + 1);
				if (fileExtensions.contains(ext)) {
					return true;
				} else {
					LOGGER.info("Skipping unknown file extension " + name);
					return false;
				}
			}
		});

		// Process
		LOGGER.info("Processing " + inputFiles.length + " files in " + inputDirectory.getAbsolutePath());
		for (File inputFile : inputFiles) {
			File outputFile = null;
			if (outputDirectory != null) {
				new File(outputDirectory, inputFile.getName());
			}
			try {
				processFile(inputFile, inputGraphName, outputFile, outputLang);
			} catch (Exception e) {
				LOGGER.error("Error in processing " + inputFile.getAbsolutePath(), e);
			}
		}
	}

	private void processFile(File inputFile, String inputGraphName, File outputFile, Lang outputLang) throws Exception {

		RdfFileReader rdfFileReader = new RdfFileReader().setFile(inputFile);
		if (inputGraphName != null && !inputGraphName.isEmpty()) {
			rdfFileReader.setGraphName(inputGraphName);
		}

		RdfWriter rdfWriter = null;
		if (outputFile != null) {
			rdfWriter = new RdfFileWriter().setFile(outputFile).setLang(outputLang);
		} else {
			rdfWriter = new DummyWriter();
		}

		while (rdfFileReader.hasNext()) {
			RdfReaderResult result = rdfFileReader.next();
			processModel(result.getModel(), result.getDatasetUri());
			rdfWriter.write(result.getModel());
		}
		rdfWriter.finish();
	}

	private void processModel(Model model, String datasetUri) throws Exception {
		for (ModelProcessor modelProcessor : modelProcessors) {
			modelProcessor.processModel(model, datasetUri);
		}
		couterProcessedModels++;
	}

	private Set<String> getFileExtensions() {
		Set<String> fileExtensions = new TreeSet<>();
		for (Lang lang : RDFLanguages.getRegisteredLanguages()) {
			fileExtensions.addAll(lang.getFileExtensions());
		}
		return fileExtensions;
	}
}