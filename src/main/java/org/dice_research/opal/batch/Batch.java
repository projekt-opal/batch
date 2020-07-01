package org.dice_research.opal.batch;

import java.io.File;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.Lang;
import org.dice_research.opal.batch.processor.CatfishProcessor;
import org.dice_research.opal.batch.processor.CivetProcessor;
import org.dice_research.opal.batch.processor.GeoDataProcessor;
import org.dice_research.opal.batch.processor.LanguageDetectionProcessor;
import org.dice_research.opal.batch.reader.RdfFileReader;
import org.dice_research.opal.batch.reader.RdfReader;
import org.dice_research.opal.batch.reader.RdfReaderResult;
import org.dice_research.opal.batch.writer.RdfFileWriter;
import org.dice_research.opal.batch.writer.RdfWriter;

public class Batch {

	public CatfishProcessor catfishProcessor = new CatfishProcessor();
	public LanguageDetectionProcessor languageDetectionProcessor = new LanguageDetectionProcessor();
	public GeoDataProcessor geoDataProcessor = new GeoDataProcessor();
	public CivetProcessor civetProcessor = new CivetProcessor();

	private void process(Model model, String datasetUri) throws Exception {
		catfishProcessor.process(model, datasetUri);
		languageDetectionProcessor.process(model, datasetUri);
		geoDataProcessor.process(model, datasetUri);
		civetProcessor.process(model, datasetUri);
	}

	private void processFile(File inputFile, File outputFile, Lang outputLang) throws Exception {
		RdfReader rdfReader = new RdfFileReader().setFile(inputFile);
		RdfWriter rdfWriter = new RdfFileWriter().setFile(outputFile).setLang(outputLang);
		while (rdfReader.hasNext()) {
			RdfReaderResult result = rdfReader.next();
			process(result.getModel(), result.getDatasetUri());
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
			process(result.getModel(), result.getDatasetUri());
			rdfWriter.write(result.getModel());
		}
		rdfWriter.finish();
	}

	// TODO Replace by tests
	public static void main(String[] args) throws Exception {
		new Batch().tmp();
	}

	// TODO Replace by tests
	private void tmp() throws Exception {
		File inputFile = new File("/home/adi/DICE/Data/OpalGraph/2019-06-24/opal-graph/model1.ttl");
		File outputFile = new File("/tmp/opalbatch.ttl");
		processFile(inputFile, outputFile, Lang.TURTLE);
		if (Boolean.FALSE) {
			processNquadsFile(inputFile, "", outputFile, Lang.TURTLE);
		}
	}

}