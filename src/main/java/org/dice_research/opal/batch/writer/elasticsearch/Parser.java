package org.dice_research.opal.batch.writer.elasticsearch;

import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.vocabulary.DCAT;
import org.dice_research.opal.batch.writer.elasticsearch.container.Dataset;
import org.dice_research.opal.batch.writer.elasticsearch.container.Distribution;
import org.dice_research.opal.common.interfaces.ModelProcessor;

public class Parser implements ModelProcessor {

	private static final boolean PARALLEL_STREAMS = true;
	private static final int SPLITERATOR_CHARACTERISTICS = Spliterator.IMMUTABLE | Spliterator.NONNULL;

	private Dataset dataset;

	public Dataset getDataset() {
		return dataset;
	}

	private Stream<Statement> getStatementStream(Resource resource, Property property) {
		return StreamSupport.stream(Spliterators.spliteratorUnknownSize(

				resource.listProperties(property), SPLITERATOR_CHARACTERISTICS), PARALLEL_STREAMS);
	}

	private Dataset processDataset(Resource resource) {
		dataset = new Dataset();

		dataset.distributions = getStatementStream(resource, DCAT.distribution)
				.filter(s -> s.getObject().isURIResource()).map(s -> s.getObject().asResource())
				.map(o -> processDistribution(o)).collect(Collectors.toList());

		return dataset;
	}

	private Distribution processDistribution(Resource resource) {
		Distribution distribution = new Distribution();

		distribution.uri = resource.getURI();

		return distribution;
	}

	@Override
	public void processModel(Model model, String datasetUri) throws Exception {
		processDataset(model.getResource(datasetUri));
	}

}