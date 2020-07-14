package org.dice_research.opal.batch.writer.elasticsearch;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.DCAT;
import org.dice_research.opal.batch.writer.elasticsearch.container.Dataset;
import org.dice_research.opal.batch.writer.elasticsearch.container.Distribution;
import org.dice_research.opal.common.interfaces.ModelProcessor;

public class Mapper implements ModelProcessor {

	@Override
	public void processModel(Model model, String datasetUri) throws Exception {
		Resource dataset = model.getResource(datasetUri);
		processDataset(model, dataset);
	}

	private Dataset processDataset(Model model, Resource resource) {
		Dataset dataset = new Dataset();
		for (Resource distribution : ModelUtils.getUriResources(model, resource, DCAT.distribution)) {
			processDistribution(model, distribution);
		}
		return dataset;
	}

	private Distribution processDistribution(Model model, Resource resource) {
		Distribution distribution = new Distribution();

		return distribution;
	}

}