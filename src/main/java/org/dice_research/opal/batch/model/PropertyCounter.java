package org.dice_research.opal.batch.model;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.vocabulary.DCAT;
import org.dice_research.opal.batch.processor.internal.StringCounter;
import org.dice_research.opal.common.interfaces.ModelProcessor;

/**
 * Counts values of properties.
 * 
 * Does not change model.
 * 
 * Configuration: Use public variables.
 *
 * @author Adrian Wilke
 */
public class PropertyCounter implements ModelProcessor {

	// Configuration

	public boolean includeDataset = true;
	public boolean includeDistributions = true;

	public boolean countResources = true;
	public boolean countLiterals = true;
	public boolean countBlankNodes = true;

	public String blankNodeKey = "[BLANK]";

	// Internal

	private StringCounter counter;
	private Property property;

	public PropertyCounter(StringCounter counter, String propertyUri) {
		this.counter = counter;
		this.property = ResourceFactory.createProperty(propertyUri);
	}

	public PropertyCounter(StringCounter counter, Property property) {
		this.counter = counter;
		this.property = property;
	}

	@Override
	public void processModel(Model model, String datasetUri) throws Exception {
		
		Resource dataset = model.getResource(datasetUri);

		if (includeDataset) {
			count(dataset, property);
		}

		if (includeDistributions) {
			StmtIterator stmtIterator = dataset.listProperties(DCAT.distribution);
			while (stmtIterator.hasNext()) {
				RDFNode object = stmtIterator.next().getObject();
				if (object.isResource()) {
					count(object.asResource(), property);
				}
			}
		}
	}

	private void count(Resource subject, Property property) {
		StmtIterator stmtIterator = subject.listProperties(property);
		while (stmtIterator.hasNext()) {
			RDFNode object = stmtIterator.next().getObject();
			if (object.isURIResource() && countResources) {
				counter.increment(object.asResource().getURI());
			} else if (object.isLiteral() && countLiterals) {
				counter.increment(object.asLiteral().getString());
			} else if (object.isAnon() && countBlankNodes) {
				counter.increment(blankNodeKey);
			}
		}
	}

}