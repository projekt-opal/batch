package org.dice_research.opal.batch.writer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.vocabulary.FOAF;
import org.apache.jena.vocabulary.DCAT;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dice_research.opal.common.vocabulary.Opal;

public class RdfFileBufferWriter implements Writer {

	private static final Logger LOGGER = LogManager.getLogger();

	public File directory;
	public Lang lang;
	public String title;
	public int maxModels;

	File file;
	Model exportModel = null;
	private int modelCounter = 0;
	private int fileCounter = 1;

	@Override
	public void processModel(Model model, String datasetUri) throws Exception {

		// TODO ignores datasetUri

		if (exportModel == null) {
			file = new File(directory, title + "-" + fileCounter + "." + lang.getFileExtensions().get(0));
			if (file.exists()) {
				file.delete();
			}
			exportModel = ModelFactory.createDefaultModel();

			exportModel.setNsPrefix("rdf", RDF.getURI());
			exportModel.setNsPrefix("rdfs", RDFS.getURI());
			exportModel.setNsPrefix("dct", DCTerms.getURI());
			exportModel.setNsPrefix("dcat", DCAT.getURI());
			exportModel.setNsPrefix("foaf", FOAF.getURI());
			exportModel.setNsPrefix("dqv", "http://www.w3.org/ns/dqv#");
			exportModel.setNsPrefix("opal", Opal.NS_OPAL);
			exportModel.setNsPrefix("dataset", Opal.NS_OPAL_DATASETS);
			exportModel.setNsPrefix("distribution", Opal.NS_OPAL_DISTRIBUTIONS);
			exportModel.setNsPrefix("metric", Opal.NS_OPAL_METRICS);

		}

		exportModel.add(model);
		modelCounter++;

		if (modelCounter == maxModels) {
			finish();
		}
	}

	@Override
	public Writer finish() {

		if (exportModel != null) {
			try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
				RDFDataMgr.write(fileOutputStream, exportModel, lang);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}

			LOGGER.info("Wrote: " + file.getAbsolutePath() + " (" + modelCounter + " datasets)");
		}

		file = null;
		exportModel = null;
		modelCounter = 0;
		fileCounter++;

		return this;
	}

}