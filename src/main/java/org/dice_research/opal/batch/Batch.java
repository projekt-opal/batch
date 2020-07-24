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
import org.dice_research.opal.batch.writer.ElasticsearchWriter;
import org.dice_research.opal.batch.writer.OutputInfo;
import org.dice_research.opal.batch.writer.RdfFileBufferWriter;
import org.dice_research.opal.batch.writer.Writer;
import org.dice_research.opal.catfish.Catfish;
import org.dice_research.opal.catfish.checker.DistributionAccessChecker;
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

	private Writer rdfWriter;
	private Writer elasticsearchWriter;
	private List<ModelProcessor> modelProcessors;
	private OutputInfo outputInfo = new OutputInfo();

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
		setWriters(cfg);

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
		elasticsearchWriter.finish();
		cfg.set(CfgKeys.INTERNAL_WRITTEN_MODELS, Long.valueOf(outputInfo.writtenModels).toString());
		cfg.set(CfgKeys.INTERNAL_WRITTEN_TRIPLES, Long.valueOf(outputInfo.writtenTriples).toString());
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

		if (cfg.has(CfgKeys.IO_ELASTICSEARCH_WRITE) && cfg.getBoolean(CfgKeys.IO_ELASTICSEARCH_WRITE)) {
			if (!cfg.has(CfgKeys.IO_ELASTICSEARCH_HOSTNAME)) {
				throw new CfgException("Elasticsearch hostname not set");
			} else if (!cfg.has(CfgKeys.IO_ELASTICSEARCH_INDEX)) {
				throw new CfgException("Elasticsearch index not set");
			} else if (!cfg.has(CfgKeys.IO_ELASTICSEARCH_PORT)) {
				throw new CfgException("Elasticsearch port not set");
			} else if (!cfg.has(CfgKeys.IO_ELASTICSEARCH_SCHEME)) {
				throw new CfgException("Elasticsearch scheme not set");
			}
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

		// Configure reader
		RdfFileReader rdfFileReader = new RdfFileReader().setFile(inputFile);
		if (inputGraph != null) {
			rdfFileReader.setGraphName(inputGraph);
		}

		// Read, process, write
		while (rdfFileReader.hasNext()) {
			RdfReaderResult result = rdfFileReader.next();
			String datasetUri = result.getDatasetUri();

			// Check if dataset should be processed
			if (!new DistributionAccessChecker().checkModel(result.getModel(), datasetUri)) {
				continue;
			}

			datasetUri = processModel(result.getModel(), result.getDatasetUri());

			// Check if dataset should be written
			if (!new DistributionAccessChecker().checkModel(result.getModel(), datasetUri)) {
				continue;
			}

			rdfWriter.processModel(result.getModel(), result.getDatasetUri());
			elasticsearchWriter.processModel(result.getModel(), result.getDatasetUri());
			outputInfo.writtenModels++;
			outputInfo.writtenTriples += result.getModel().size();
			if (outputInfo.writtenModels % 10000 == 0) {
				LOGGER.info("Processed datasets: " + outputInfo.writtenModels);
			}
		}
	}

	/**
	 * Processes model.
	 */
	private String processModel(Model model, String datasetUri) throws Exception {

		String currentDatasetUri = datasetUri;

		for (ModelProcessor modelProcessor : modelProcessors) {
			modelProcessor.processModel(model, currentDatasetUri);

			if (modelProcessor instanceof Catfish) {
				if (((Catfish) modelProcessor).getNewDatasetUri() != null) {
					currentDatasetUri = ((Catfish) modelProcessor).getNewDatasetUri();
				}
			}
		}

		return currentDatasetUri;
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
	private void setWriters(Cfg cfg) {
		rdfWriter = null;
		if (cfg.getBoolean(CfgKeys.IO_OUTPUT_WRITE)) {

			// RdfFileWriter currently replaced by RdfFileBufferWriter
			// RdfFileWriter rdfFileWriter = new RdfFileWriter();

			RdfFileBufferWriter rdfFileWriter = new RdfFileBufferWriter();

			rdfFileWriter.directory = outputDirectory;
			rdfFileWriter.title = outputTitle;
			rdfFileWriter.lang = outputLanguage;
			rdfFileWriter.maxModels = outputSize;
			rdfWriter = rdfFileWriter;
		} else {
			rdfWriter = new DummyWriter();
		}

		if (cfg.has(CfgKeys.IO_ELASTICSEARCH_WRITE) && cfg.getBoolean(CfgKeys.IO_ELASTICSEARCH_WRITE)) {
			ElasticsearchWriter writer = new ElasticsearchWriter();
			writer.hostname = cfg.get(CfgKeys.IO_ELASTICSEARCH_HOSTNAME);
			writer.port = cfg.getInt(CfgKeys.IO_ELASTICSEARCH_PORT);
			writer.scheme = cfg.get(CfgKeys.IO_ELASTICSEARCH_SCHEME);
			writer.index = cfg.get(CfgKeys.IO_ELASTICSEARCH_INDEX);
			writer.maxModels = outputSize;
			elasticsearchWriter = writer;
		} else {
			elasticsearchWriter = new DummyWriter();
		}
	}

	/**
	 * Creates an additional file containing labels for themes.
	 */
	private void createAdditionalFiles(Cfg cfg) {
		if (cfg.getBoolean(CfgKeys.ADD_LABELS)) {
			try {
				IOUtils.copy(getClass().getResourceAsStream("opal-themes-labels.ttl"),
						new FileOutputStream(new File(outputDirectory, Filenames.LABELS_THEMES)));
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}
}