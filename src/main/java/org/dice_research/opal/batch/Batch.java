package org.dice_research.opal.batch;

import java.io.File;
import java.util.List;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.Lang;
import org.dice_research.opal.batch.configuration.Cfg;
import org.dice_research.opal.batch.configuration.CfgKeys;
import org.dice_research.opal.batch.processor.Processors;
import org.dice_research.opal.batch.reader.RdfFileReader;
import org.dice_research.opal.batch.reader.RdfReader;
import org.dice_research.opal.batch.reader.RdfReaderResult;
import org.dice_research.opal.batch.writer.RdfFileWriter;
import org.dice_research.opal.batch.writer.RdfWriter;
import org.dice_research.opal.common.interfaces.ModelProcessor;

/**
 * Batch processing of OPAL components.
 *
 * @author Adrian Wilke
 */
public class Batch {

	/**
	 * Main entry point.
	 */
	public static void main(String[] args) throws Exception {
		long time = System.currentTimeMillis();

		// Set default or custom configuration
		Cfg cfg = null;
		if (args.length == 0) {
			cfg = new Cfg();
		} else {
			cfg = new Cfg(new File(args[0]));
		}

		// Configure I/O
		File inputFile = new File(cfg.get(CfgKeys.IO_INPUT_FILE));
		File outputFile = new File(cfg.get(CfgKeys.IO_OUTPUT_FILE));
		String inputGraph = cfg.get(CfgKeys.IO_INPUT_GRAPH);

		// Configure components
		Batch batch = new Batch();
		batch.modelProcessors = Processors.createModelProcessors(cfg);

		// Process data
		if (inputGraph != null && !inputGraph.isEmpty()) {
			batch.processNquadsFile(inputFile, inputGraph, outputFile, Lang.TURTLE);
		} else {
			batch.processFile(inputFile, outputFile, Lang.TURTLE);
		}

		System.out.println(1f * (System.currentTimeMillis() - time) / 1000);
	}

	private List<ModelProcessor> modelProcessors;

	private void processFile(File inputFile, File outputFile, Lang outputLang) throws Exception {
		RdfReader rdfReader = new RdfFileReader().setFile(inputFile);
		RdfWriter rdfWriter = new RdfFileWriter().setFile(outputFile).setLang(outputLang);
		while (rdfReader.hasNext()) {
			RdfReaderResult result = rdfReader.next();
			processModel(result.getModel(), result.getDatasetUri());
			rdfWriter.write(result.getModel());
		}
		rdfWriter.finish();
	}

	private void processNquadsFile(File inputFile, String inputGraphName, File outputFile, Lang outputLang)
			throws Exception {
		RdfReader rdfReader = new RdfFileReader().setFile(inputFile).setGraphName(inputGraphName);
		RdfWriter rdfWriter = new RdfFileWriter().setFile(outputFile).setLang(outputLang);
		while (rdfReader.hasNext()) {
			RdfReaderResult result = rdfReader.next();
			processModel(result.getModel(), result.getDatasetUri());
			rdfWriter.write(result.getModel());
		}
		rdfWriter.finish();
	}

	private void processModel(Model model, String datasetUri) throws Exception {
		for (ModelProcessor modelProcessor : modelProcessors) {
			modelProcessor.processModel(model, datasetUri);
		}
	}
}