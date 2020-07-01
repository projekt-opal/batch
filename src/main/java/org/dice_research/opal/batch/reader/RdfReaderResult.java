package org.dice_research.opal.batch.reader;

import org.apache.jena.rdf.model.Model;

/**
 * Result container for {@link RdfReader}.
 *
 * @author Adrian Wilke
 */
public class RdfReaderResult {

	private String datasetUri;
	private Model model;

	public String getDatasetUri() {
		return datasetUri;
	}

	public Model getModel() {
		return model;
	}

	public RdfReaderResult setDatasetUri(String datasetUri) {
		this.datasetUri = datasetUri;
		return this;
	}

	public RdfReaderResult setModel(Model model) {
		this.model = model;
		return this;
	}
}