package org.dice_research.opal.batch.writer.elasticsearch;

import java.util.Arrays;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.vocabulary.DCAT;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dice_research.opal.common.interfaces.ModelProcessor;
import org.dice_research.opal.common.vocabulary.Opal;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Extracts data from model and creates a JSON object.
 * 
 * Usage: Create one {@link JsonExtractor} instance for each execution. After calling
 * {@link #processModel(Model, String)}, the result is available via
 * {@link #getJsonObject()}.
 * 
 * Based on these Elasticsearch mappings:
 * https://github.com/projekt-opal/opaldata/blob/6a2ecdc4a41c8eb7f168543d732560ece82c1451/elasticsearch-initialization/mappings.json
 *
 * @author Adrian Wilke
 */
public class JsonExtractor implements ModelProcessor {

	// TODO
//	 * https://github.com/projekt-opal/converter/blob/master/elasticsearch-writer/src/main/java/org/diceresearch/elasticsearchwriter/utility/impl/ElasticSearchWriterImpl.java
//	 * https://github.com/projekt-opal/converter/blob/master/elasticsearch-writer/src/main/java/org/diceresearch/elasticsearchwriter/utility/ModelMapper.java
//	 * https://github.com/projekt-opal/converter/tree/master/elasticsearch-writer/src/main/java/org/diceresearch/elasticsearchwriter/entity

	private static final Logger LOGGER = LogManager.getLogger();

	private static final boolean PARALLEL_STREAMS = true;
	private static final int SPLITERATOR_CHARACTERISTICS = Spliterator.IMMUTABLE | Spliterator.NONNULL;

	private JSONObject jsonObject = new JSONObject();

	/**
	 * Gets the processing result.
	 */
	public JSONObject getJsonObject() {
		return jsonObject;
	}

	/**
	 * Extracts dataset data and creates a JSON object.
	 */
	@Override
	public void processModel(Model model, String datasetUri) throws Exception {
		processDataset(model.getResource(datasetUri));
	}

	/**
	 * Adds the given dataset to JSON.
	 */
	private JSONObject processDataset(Resource dataset) {

		jsonObject.put("uri", dataset.getURI());
		appendToJson(dataset, Opal.PROP_ORIGINAL_URI, jsonObject, "originalUrls");
		appendToJson(dataset, DCTerms.title, jsonObject, "title");
		appendLiteralsToJson(dataset, DCTerms.title, jsonObject, "title", new String[] { "", "en" }, false);
		appendLiteralsToJson(dataset, DCTerms.title, jsonObject, "title_de", new String[] { "de" }, false);
		appendLiteralsToJson(dataset, DCTerms.description, jsonObject, "description", new String[] { "", "en" }, false);
		appendLiteralsToJson(dataset, DCTerms.description, jsonObject, "description_de", new String[] { "de" }, false);
		appendToJson(dataset, DCAT.landingPage, jsonObject, "landingPage");
		appendLiteralsToJson(dataset, DCTerms.language, jsonObject, "language", null, true);
//		appendToJson(dataset, DCTerms.language, jsonObject, "language");
		appendLiteralsToJson(dataset, DCAT.keyword, jsonObject, "keywords", new String[] { "", "en" }, true);
		appendLiteralsToJson(dataset, DCAT.keyword, jsonObject, "keywords_de", new String[] { "", "en" }, true);
		appendToJson(dataset, DCTerms.issued, jsonObject, "issued");
		appendToJson(dataset, DCTerms.modified, jsonObject, "modified");
//		public List<String> licenses; // TODO
//		public List<String> themes;// TODO
//		public List<String> hasQualityMeasurements; // TODO
//		appendToJson(dataset, null, jsonObject, "publisher");
//		appendToJson(dataset, null, jsonObject, "creator");
		appendToJson(dataset, DCTerms.spatial, jsonObject, "spatial");
//		appendToJson(dataset, null, jsonObject, "contactPoint");
//		public List<Distribution> distributions; // TODO
		appendToJson(dataset, DCTerms.accrualPeriodicity, jsonObject, "accrualPeriodicity");
		appendToJson(dataset, DCTerms.identifier, jsonObject, "dcatIdentifier");
//		appendToJson(dataset, null, jsonObject, "temporal");

		JSONArray distributions = new JSONArray();
		getStatementStream(dataset, DCAT.distribution).filter(s -> s.getObject().isURIResource())
				.map(s -> s.getObject().asResource()).forEach(o -> processDistribution(o, distributions));
		jsonObject.put("distributions", distributions);

		return jsonObject;
	}

	/**
	 * Adds the given distribution to JSON.
	 */
	private void processDistribution(Resource distribution, JSONArray distributions) {

		JSONObject jsonObject = new JSONObject();
		jsonObject.put("dataSetUri", distribution.getURI());

		// TODO not in test data
		appendToJson(distribution, Opal.PROP_ORIGINAL_URI, jsonObject, "originalUrl");

		appendToJson(distribution, DCTerms.title, jsonObject, "title");
		appendToJson(distribution, DCTerms.description, jsonObject, "description");
		appendToJson(distribution, DCTerms.issued, jsonObject, "issued");
		appendToJson(distribution, DCTerms.modified, jsonObject, "modified");

		// TODO DCTerms.license

		appendToJson(distribution, DCAT.accessURL, jsonObject, "accessUrl");
		appendToJson(distribution, DCAT.downloadURL, jsonObject, "downloadUrl");

		// TODO use cleaned
		appendToJson(distribution, DCTerms.format, jsonObject, "format");

		appendToJson(distribution, DCAT.byteSize, jsonObject, "byteSize");

		distributions.put(jsonObject);

	}

	/**
	 * Gets a stream of triples for the resource-property-combination.
	 */
	private Stream<Statement> getStatementStream(Resource resource, Property property) {
		return StreamSupport.stream(
				Spliterators.spliteratorUnknownSize(resource.listProperties(property), SPLITERATOR_CHARACTERISTICS),
				PARALLEL_STREAMS);
	}

	/**
	 * Appends a literal value to JSON.
	 * 
	 * @param languages      can be an array of languages or null to match any
	 *                       language
	 * @param multipleValues true, if multiple values can be added. False, if only
	 *                       one value should be added.
	 */
	private void appendLiteralsToJson(Resource resource, Property property, JSONObject jsonObject, String jsonKey,
			String[] languages, boolean multipleValues) {
		StmtIterator stmtIterator = resource.listProperties(property);
		while (stmtIterator.hasNext()) {
			RDFNode rdfNode = stmtIterator.next().getObject();

			if (rdfNode.isLiteral()) {
				String language = rdfNode.asLiteral().getLanguage();
				if (languages == null || Arrays.asList(languages).contains(language)) {
					String value = rdfNode.asLiteral().getString().trim();
					if (!value.isEmpty()) {
						jsonObject.append(jsonKey, value);
						if (!multipleValues) {
							return;
						}
					}
				}

			} else {
				LOGGER.warn("Not a literal: " + resource);
			}
		}
	}

	/**
	 * Appends one entry to JSON.
	 * 
	 * If a resource is found, the respective URI is used. For literals, a non-empty
	 * value is used.
	 */
	private void appendToJson(Resource resource, Property property, JSONObject jsonObject, String jsonKey) {
		if (resource.hasProperty(property)) {
			RDFNode rdfNode = resource.getProperty(property).getObject();

			if (rdfNode.isURIResource()) {
				jsonObject.append(jsonKey, rdfNode.asResource().getURI());

			} else if (rdfNode.isLiteral()) {
				String value = rdfNode.asLiteral().getString().trim();
				if (!value.isEmpty()) {
					jsonObject.append(jsonKey, value);
				}

			} else {
				LOGGER.warn("Not a literal or UriResource: " + resource);
			}
		}
	}

}