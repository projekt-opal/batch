package org.dice_research.opal.batch.writer;

import org.apache.jena.rdf.model.Model;

public class DummyWriter implements Writer {

	@Override
	public Writer finish() {
		return this;
	}

	@Override
	public void processModel(Model model, String datasetUri) throws Exception {
	}

}
