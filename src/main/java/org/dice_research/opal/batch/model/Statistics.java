package org.dice_research.opal.batch.model;

import org.apache.jena.rdf.model.Model;
import org.dice_research.opal.common.interfaces.ModelProcessor;

public class Statistics implements ModelProcessor {

	public long models = 0;
	public long triples = 0;

	@Override
	public void processModel(Model model, String datasetUri) throws Exception {
		models++;
		triples += model.size();
	}
}