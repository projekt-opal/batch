package org.dice_research.opal.batch.reader;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFLanguages;
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
		addStatements(model, dataset, new HashSet<String>());
		return new RdfReaderResult().setModel(model).setDatasetUri(dataset.getURI());
	}

	private void initialize() {
		if (file == null) {
			throw new RuntimeException("No file specified");
		} else if (!file.canRead()) {
			throw new RuntimeException("Can not read: " + file.getAbsolutePath());
		}

		if (RDFLanguages.isQuads(RDFLanguages.filenameToLang(file.getName())) && graphName != null) {
			datasetIterator = loadNquadsGraph(file, graphName).listResourcesWithProperty(RDF.type, DCAT.Dataset);
		} else {
			datasetIterator = loadFile(file).listResourcesWithProperty(RDF.type, DCAT.Dataset);
		}
	}

	private void addStatements(Model model, Resource resource, Set<String> processed) {

		if (resource.isURIResource()) {
			processed.add(resource.getURI());
		} else if (resource.isAnon()) {
			processed.add(resource.toString());
		}

		StmtIterator stmtIterator = resource.listProperties();
		while (stmtIterator.hasNext()) {

			Statement statement = stmtIterator.next();
			model.add(statement);

			if (statement.getObject().isURIResource()
					&& !processed.contains(statement.getObject().asResource().getURI())) {
				addStatements(model, statement.getObject().asResource(), processed);
			} else if (statement.getObject().isAnon() && !processed.contains(statement.getObject().toString())) {
				addStatements(model, statement.getObject().asResource(), processed);
			}
		}
	}

	private static Model loadFile(File file) {
		LOGGER.debug("Reading: " + file.getAbsolutePath() + " " + file.length() / 1000000 + " MB");
		Model model = RDFDataMgr.loadModel(file.toURI().toString());
		LOGGER.debug("Read model, size: " + model.size());
		return model;
	}

	private static Model loadNquadsGraph(File nquadsFile, String graphName) {
		LOGGER.debug("Reading: " + nquadsFile.getAbsolutePath() + " " + nquadsFile.length() / 1000000 + " MB");
		Dataset dataset = RDFDataMgr.loadDataset(nquadsFile.toURI().toString(), Lang.NQUADS);
		Model model = dataset.getNamedModel(graphName);
		LOGGER.debug("Got graph '" + graphName + "', size: " + model.size());
		return model;
	}
}
