package org.dice_research.opal.batch;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.compress.utils.IOUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFLanguages;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dice_research.opal.batch.configuration.Cfg;
import org.dice_research.opal.batch.configuration.CfgException;
import org.dice_research.opal.batch.configuration.CfgKeys;
import org.dice_research.opal.batch.configuration.Filenames;
import org.dice_research.opal.batch.construction.Constructor;
import org.dice_research.opal.batch.construction.ConstructorManager;
import org.dice_research.opal.batch.construction.opal.CatfishConstructor;
import org.dice_research.opal.batch.reader.RdfFileReader;
import org.dice_research.opal.batch.reader.RdfReaderResult;
import org.dice_research.opal.batch.writer.DummyWriter;
import org.dice_research.opal.batch.writer.RdfFileWriter;
import org.dice_research.opal.batch.writer.RdfWriter;
import org.dice_research.opal.catfish.Catfish;
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
		try {
			if (args.length == 0) {
				// Use default.properties
				new Batch().execute(new Cfg());
			} else {
				// Use user-defined configuration
				new Batch().execute(new Cfg(new File(args[0])));
			}
		} catch (CfgException e) {
			// Handle self-defined configuration exceptions
			System.err.println("Error: " + e.getMessage());
			System.exit(1);
		}
	}

	// Parsed from configuration

	private List<File> inputs;
	private String inputGraph;
	private File outputDirectory;
	private String outputTitle;
	private Lang outputLanguage;
	private int outputSize;

	// Internal

	private RdfWriter rdfWriter;
	private List<ModelProcessor> modelProcessors;

	/**
	 * Executes batch with given configuration and default constructors.
	 */
	public void execute(Cfg cfg) throws Exception {
		execute(cfg, ConstructorManager.create());
	}

	/**
	 * Executes batch with given configuration and constructors.
	 */
	public void execute(Cfg cfg, ConstructorManager constructorManager) throws Exception {

		// Configuration
		checkInput(cfg);
		checkOutput(cfg);
		modelProcessors = constructorManager.createModelProcessors(cfg).getModelProcessors();
		setRdfWriter();

		// Read and process data
		for (File input : inputs) {
			if (input.isDirectory()) {
				processDirectory(input);
			} else {
				processFile(input);
			}
		}

		// Finalize
		rdfWriter.finish();
		for (Constructor constructor : constructorManager.getConstructors()) {
			constructor.finish(cfg);
		}

		// Meta
		createAdditionalFiles(cfg);
		LOGGER.info("Finished. Results: " + outputDirectory.getAbsolutePath());
	}

	/**
	 * Checks input values of configuration and sets variables.
	 */
	private void checkInput(Cfg cfg) {
		if (!cfg.has(CfgKeys.IO_INPUT)) {
			throw new CfgException("No input set.");
		}
		inputs = new LinkedList<>();
		for (String inputString : cfg.get(CfgKeys.IO_INPUT).split("\\|")) {
			File file = new File(inputString.trim());
			if (!file.canRead()) {
				throw new CfgException("Can not read input: " + file.getAbsolutePath());
			}
			inputs.add(file);
		}

		if (cfg.has(CfgKeys.IO_INPUT_GRAPH)) {
			inputGraph = cfg.get(CfgKeys.IO_INPUT_GRAPH);
		}

		if (cfg.getBoolean(CfgKeys.RUN_CATFISH) && cfg.getBoolean(CfgKeys.CATFISH_REPLACE_URIS_CATALOG_BY_FILENAME)) {
			String catalogId = CatfishConstructor.getCatalogId(inputs.get(0));
			cfg.set(CfgKeys.CATFISH_REPLACE_URIS_CATALOG, catalogId);
			LOGGER.info("Using catalog '" + catalogId + "' for replacing URIs.");
		}
	}

	/**
	 * Checks output values of configuration and sets variables.
	 */
	private void checkOutput(Cfg cfg) {
		if (!cfg.has(CfgKeys.IO_OUTPUT_DIRECTORY)) {
			throw new CfgException("No output directory set.");
		}
		outputDirectory = new File(cfg.get(CfgKeys.IO_OUTPUT_DIRECTORY));
		if (!outputDirectory.exists()) {
			outputDirectory.mkdirs();
		}
		if (!outputDirectory.canWrite()) {
			throw new CfgException("Can not write output directory: " + outputDirectory.getAbsolutePath());
		}

		if (!cfg.has(CfgKeys.IO_OUTPUT_TITLE)) {
			throw new CfgException("No output title set.");
		}
		outputTitle = cfg.get(CfgKeys.IO_OUTPUT_TITLE);

		if (!cfg.has(CfgKeys.IO_OUTPUT_FORMAT)) {
			throw new CfgException("No output format set.");
		}
		outputLanguage = RDFLanguages.fileExtToLang(cfg.get(CfgKeys.IO_OUTPUT_FORMAT));
		if (outputLanguage == null) {
			throw new CfgException("Unknown output format: " + cfg.get(CfgKeys.IO_OUTPUT_FORMAT));
		}

		if (!cfg.has(CfgKeys.IO_OUTPUT_SIZE)) {
			throw new CfgException("No output size set.");
		}
		outputSize = Integer.parseInt(cfg.get(CfgKeys.IO_OUTPUT_SIZE));
	}

	/**
	 * Processes directory and calls {@link #processFile(File)}.
	 */
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

	/**
	 * Processes file and calls {@link #processModel(Model, String)}.
	 */
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

	/**
	 * Processes model.
	 */
	private void processModel(Model model, String datasetUri) throws Exception {
		for (ModelProcessor modelProcessor : modelProcessors) {
			modelProcessor.processModel(model, datasetUri);

			// Go on with rewritten dataset URI
			if (modelProcessor instanceof Catfish) {
				String newDatasetUri = ((Catfish) modelProcessor).getNewDatasetUri();
				if (newDatasetUri != null) {
					datasetUri = newDatasetUri;
				}
			}
		}
	}

	/**
	 * Gets file extensions of registered RDF languages.
	 */
	private Set<String> getFileExtensions() {
		Set<String> fileExtensions = new TreeSet<>();
		for (Lang lang : RDFLanguages.getRegisteredLanguages()) {
			fileExtensions.addAll(lang.getFileExtensions());
		}
		return fileExtensions;
	}

	/**
	 * Sets the RDF-writer to use.
	 */
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

	/**
	 * Creates an additional file containing labels for themes.
	 */
	private void createAdditionalFiles(Cfg cfg) {
		if (cfg.getBoolean(CfgKeys.ADD_LABELS)) {
			try {
				IOUtils.copy(getClass().getResourceAsStream("opal-themes-labels.ttl"),
						new FileOutputStream(new File(outputDirectory, Filenames.LANGUAGES)));
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}
}