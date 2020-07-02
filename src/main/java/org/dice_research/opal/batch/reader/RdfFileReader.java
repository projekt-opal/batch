package org.dice_research.opal.batch.reader;

import java.io.File;

import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.vocabulary.DCAT;
import org.apache.jena.vocabulary.RDF;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Simple RDF file reader.
 * 
 * Usage: Specify the file to read using {@link #setFile(File)}.
 * 
 * Optional: Specify a graph for a N-Quads file using
 * {@link #setGraphName(String)}.
 * 
 * Use {@link #hasNext()} and {@link #next()} to get DCAT Dataset graphs.
 *
 * @author Adrian Wilke
 */
public class RdfFileReader implements RdfReader {

	private static final Logger LOGGER = LogManager.getLogger();

	private File file = null;
	private String graphName = null;
	private ResIterator datasetIterator = null;

	public RdfFileReader setFile(File file) {
		this.file = file;
		return this;
	}

	public RdfFileReader setGraphName(String graphName) {
		this.graphName = graphName;
		return this;
	}

	public File getFile() {
		return file;
	}

	public String getGraphName() {
		return graphName;
	}

	@Override
	public boolean hasNext() {
		if (datasetIterator == null) {
			initialize();
		}

		return datasetIterator.hasNext();
	}

	@Override
	public RdfReaderResult next() {
		if (datasetIterator == null) {
			initialize();
		}

		Model model = ModelFactory.createDefaultModel();
		Resource dataset = datasetIterator.next();
		addStatements(model, dataset);
		return new RdfReaderResult().setModel(model).setDatasetUri(dataset.getURI());
	}

	private void initialize() {
		if (file == null) {
			throw new RuntimeException("No file specified");
		} else if (!file.canRead()) {
			throw new RuntimeException("Can not read: " + file.getAbsolutePath());
		}

		if (graphName == null) {
			datasetIterator = loadFile(file).listResourcesWithProperty(RDF.type, DCAT.Dataset);
		} else {
			datasetIterator = loadNquadsGraph(file, graphName).listResourcesWithProperty(RDF.type, DCAT.Dataset);
		}
	}

	private void addStatements(Model model, Resource resource) {
		StmtIterator stmtIterator = resource.listProperties();
		while (stmtIterator.hasNext()) {
			Statement statement = stmtIterator.next();
			model.add(statement);
			if (statement.getObject().isResource()) {
				addStatements(model, statement.getObject().asResource());
			}
		}
	}

	private static Model loadFile(File file) {
		LOGGER.info("Reading: " + file.getAbsolutePath() + " " + file.length() / 1000000 + " MB");
		Model model = RDFDataMgr.loadModel(file.toURI().toString());
		LOGGER.info("Read model, size: " + model.size());
		return model;
	}

	private static Model loadNquadsGraph(File nquadsFile, String graphName) {
		LOGGER.info("Reading: " + nquadsFile.getAbsolutePath() + " " + nquadsFile.length() / 1000000 + " MB");
		Dataset dataset = RDFDataMgr.loadDataset(nquadsFile.toURI().toString(), Lang.NQUADS);
		Model model = dataset.getNamedModel(graphName);
		LOGGER.info("Got graph '" + graphName + "', size: " + model.size());
		return model;
	}
}