package org.dice_research.opal.batch;

import java.io.File;
import java.io.FilenameFilter;
import java.util.LinkedList;
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

	private List<File> inputs;
	private String inputGraph;

	private File outputDirectory;
	private String outputTitle;
	private Lang outputLanguage;
	private int outputSize;

	private RdfWriter rdfWriter;
	private List<ModelProcessor> modelProcessors;
	private long processedModels = 0;

	/**
	 * Executes batch with given configuration and default processors.
	 */
	public void execute(Cfg cfg) throws Exception {
		execute(cfg, new Processors().createModelProcessors(cfg));
	}

	/**
	 * Executes batch with given configuration and processors.
	 */
	public void execute(Cfg cfg, Processors processors) throws Exception {
		long time = System.currentTimeMillis();

		// Configure components
		this.modelProcessors = processors.getModelProcessors();

		// Configure I/O
		checkInput(cfg);
		checkOutput(cfg);
		setRdfWriter();

		// Process data
		for (File input : inputs) {
			if (input.isFile()) {
				processFile(input);
			} else {
				processDirectory(input);
			}
		}
		rdfWriter.finish();

		LOGGER.info("Run time (secs): " + 1f * (System.currentTimeMillis() - time) / 1000);
		LOGGER.info("Processed models: " + processedModels);
	}

	private void checkInput(Cfg cfg) {
		if (!cfg.has(CfgKeys.IO_INPUT)) {
			throw new RuntimeException("No input set.");
		}
		inputs = new LinkedList<>();
		for (String inputString : cfg.get(CfgKeys.IO_INPUT).split("\\|")) {
			File file = new File(inputString.trim());
			if (!file.canRead()) {
				throw new RuntimeException("Can not read input: " + file.getAbsolutePath());
			}
			inputs.add(file);
		}

		if (cfg.has(CfgKeys.IO_INPUT_GRAPH)) {
			inputGraph = cfg.get(CfgKeys.IO_INPUT_GRAPH);
		}
	}

	private void checkOutput(Cfg cfg) {
		if (!cfg.has(CfgKeys.IO_OUTPUT_DIRECTORY)) {
			throw new RuntimeException("No output directory set.");
		}
		outputDirectory = new File(cfg.get(CfgKeys.IO_OUTPUT_DIRECTORY));
		if (!outputDirectory.exists()) {
			outputDirectory.mkdirs();
		}
		if (!outputDirectory.canWrite()) {
			throw new RuntimeException("Can not write output directory: " + outputDirectory.getAbsolutePath());
		}

		if (!cfg.has(CfgKeys.IO_OUTPUT_TITLE)) {
			throw new RuntimeException("No output title set.");
		}
		outputTitle = cfg.get(CfgKeys.IO_OUTPUT_TITLE);

		if (!cfg.has(CfgKeys.IO_OUTPUT_FORMAT)) {
			throw new RuntimeException("No output format set.");
		}
		outputLanguage = RDFLanguages.fileExtToLang(cfg.get(CfgKeys.IO_OUTPUT_FORMAT));
		if (outputLanguage == null) {
			throw new RuntimeException("Unknown output format: " + cfg.get(CfgKeys.IO_OUTPUT_FORMAT));
		}

		if (!cfg.has(CfgKeys.IO_OUTPUT_SIZE)) {
			throw new RuntimeException("No output size set.");
		}
		outputSize = Integer.parseInt(cfg.get(CfgKeys.IO_OUTPUT_SIZE));
	}

	private void processDirectory(File inputDirectory) {

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
			try {
				processFile(inputFile);
			} catch (Exception e) {
				LOGGER.error("Error in processing " + inputFile.getAbsolutePath(), e);
			}
		}
	}

	private void processFile(File inputFile) throws Exception {
		RdfFileReader rdfFileReader = new RdfFileReader().setFile(inputFile);
		if (inputGraph != null) {
			rdfFileReader.setGraphName(inputGraph);
		}

		while (rdfFileReader.hasNext()) {
			RdfReaderResult result = rdfFileReader.next();
			processModel(result.getModel(), result.getDatasetUri());
			rdfWriter.write(result.getModel());
		}
	}

	private void processModel(Model model, String datasetUri) throws Exception {
		for (ModelProcessor modelProcessor : modelProcessors) {
			modelProcessor.processModel(model, datasetUri);
		}
		processedModels++;
	}

	private Set<String> getFileExtensions() {
		Set<String> fileExtensions = new TreeSet<>();
		for (Lang lang : RDFLanguages.getRegisteredLanguages()) {
			fileExtensions.addAll(lang.getFileExtensions());
		}
		return fileExtensions;
	}

	private RdfWriter setRdfWriter() {
		rdfWriter = null;
		if (outputDirectory != null) {
			RdfFileWriter rdfFileWriter = new RdfFileWriter();
			rdfFileWriter.directory = outputDirectory;
			rdfFileWriter.title = outputTitle;
			rdfFileWriter.lang = outputLanguage;
			rdfFileWriter.maxModels = outputSize;
			rdfWriter = rdfFileWriter;
		} else {
			rdfWriter = new DummyWriter();
		}
		return rdfWriter;
	}
}