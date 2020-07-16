package org.dice_research.opal.batch.writer;

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
import org.apache.jena.sparql.vocabulary.FOAF;
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
 * Usage: Create one {@link JsonExtractor} instance for each execution. After
 * calling {@link #processModel(Model, String)}, the result is available via
 * {@link #getJson()}.
 * 
 * Based on these Elasticsearch mappings:
 * https://github.com/projekt-opal/opaldata/blob/6a2ecdc4a41c8eb7f168543d732560ece82c1451/elasticsearch-initialization/mappings.json
 * and this project:
 * https://github.com/projekt-opal/converter/tree/master/elasticsearch-writer/src/main/java/org/diceresearch/elasticsearchwriter/
 *
 * @author Adrian Wilke
 */
public class JsonExtractor implements ModelProcessor {

	private static final Logger LOGGER = LogManager.getLogger();

	private static final boolean PARALLEL_STREAMS = true;
	private static final int SPLITERATOR_CHARACTERISTICS = Spliterator.IMMUTABLE | Spliterator.NONNULL;

	private JSONObject jsonObject = new JSONObject();

	/**
	 * Gets the processing result.
	 */
	public String getJson() {
		return jsonObject.toString();
	}

	/**
	 * Gets the processing result.
	 */
	public String getJson(int indentFactor) {
		return jsonObject.toString(indentFactor);
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
		add(dataset, Opal.PROP_ORIGINAL_URI, jsonObject, "originalUrls");
		addLiterals(dataset, DCAT.theme, jsonObject, "themes", new String[] {}, true);
		jsonObject.put("uri", dataset.getURI());
		addLiterals(dataset, DCTerms.title, jsonObject, "title", new String[] { "", "en" }, false);
		addLiterals(dataset, DCTerms.title, jsonObject, "title_de", new String[] { "de" }, false);
		addLiterals(dataset, DCTerms.description, jsonObject, "description", new String[] { "", "en" }, false);
		addLiterals(dataset, DCTerms.description, jsonObject, "description_de", new String[] { "de" }, false);
		add(dataset, DCAT.landingPage, jsonObject, "landingPage");
		addLiterals(dataset, DCTerms.language, jsonObject, "language", new String[] {}, false);
		addLiterals(dataset, DCAT.keyword, jsonObject, "keywords", new String[] { "", "en" }, true);
		addLiterals(dataset, DCAT.keyword, jsonObject, "keywords_de", new String[] { "", "en" }, true);
		add(dataset, DCTerms.issued, jsonObject, "issued");
		add(dataset, DCTerms.modified, jsonObject, "modified");
		// public List<String> hasQualityMeasurements; // TODO
		addPublisher(dataset, DCTerms.publisher, jsonObject, "publisher");
		addPublisher(dataset, DCTerms.creator, jsonObject, "creator");
		add(dataset, DCTerms.spatial, jsonObject, "spatial");
		addLiterals(dataset, DCAT.contactPoint, jsonObject, "contactPoint", new String[] {}, true); // TODO fields
		addLiterals(dataset, DCTerms.license, jsonObject, "license", new String[] {}, true); // TODO name

		JSONArray distributions = new JSONArray();
		getStatementStream(dataset, DCAT.distribution).filter(s -> s.getObject().isURIResource())
				.map(s -> s.getObject().asResource()).forEach(o -> processDistribution(o, distributions));
		jsonObject.put("distributions", distributions);

		add(dataset, DCTerms.accrualPeriodicity, jsonObject, "accrualPeriodicity");
		add(dataset, DCTerms.identifier, jsonObject, "dcatIdentifier");
		// add(dataset, DCTerms.temporal, jsonObject, "temporal"); // TODO

		return jsonObject;
	}

	/**
	 * Adds the given distribution to JSON.
	 */
	private void processDistribution(Resource distribution, JSONArray distributions) {
		JSONObject jsonObject = new JSONObject();
		add(distribution, Opal.PROP_ORIGINAL_URI, jsonObject, "originalURLs"); // TODO test
		jsonObject.put("uri", distribution.getURI());
		add(distribution, DCTerms.title, jsonObject, "title");
		add(distribution, DCTerms.description, jsonObject, "description");
		add(distribution, DCTerms.issued, jsonObject, "issued");
		add(distribution, DCTerms.modified, jsonObject, "modified");
		addLiterals(distribution, DCTerms.license, jsonObject, "license", new String[] {}, true); // TODO name
		add(distribution, DCAT.accessURL, jsonObject, "accessUrl");
		add(distribution, DCAT.downloadURL, jsonObject, "downloadUrl");
		add(distribution, DCTerms.format, jsonObject, "format"); // TODO use cleaned
		add(distribution, DCAT.byteSize, jsonObject, "byteSize");
		add(distribution, DCTerms.rights, jsonObject, "rights");
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
	 * Adds one entry to JSON.
	 * 
	 * If a resource is found, the respective URI is used. For literals, a non-empty
	 * value is used.
	 */
	private void add(Resource resource, Property property, JSONObject jsonObject, String jsonKey) {
		if (resource.hasProperty(property)) {
			RDFNode rdfNode = resource.getProperty(property).getObject();

			if (rdfNode.isURIResource()) {
				jsonObject.put(jsonKey, rdfNode.asResource().getURI());

			} else if (rdfNode.isLiteral()) {
				String value = rdfNode.asLiteral().getValue().toString().trim();
				if (!value.isEmpty()) {
					jsonObject.put(jsonKey, value);
				}

			} else {
				LOGGER.warn("Not a literal or UriResource: " + resource);
			}
		}
	}

	/**
	 * Adds a literal value to JSON.
	 */
	private void addLiterals(Resource resource, Property property, JSONObject jsonObject, String jsonKey,
			String[] languages, boolean multipleValues) {
		StmtIterator stmtIterator = resource.listProperties(property);
		while (stmtIterator.hasNext()) {
			RDFNode rdfNode = stmtIterator.next().getObject();

			if (rdfNode.isLiteral()) {
				String language = rdfNode.asLiteral().getLanguage();
				if (languages.length == 0 || Arrays.asList(languages).contains(language)) {
					String value = rdfNode.asLiteral().getValue().toString().trim();
					if (!value.isEmpty()) {
						if (multipleValues) {
							jsonObject.append(jsonKey, value);
						} else {
							jsonObject.put(jsonKey, value);
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
	 * Adds publisher data.
	 */
	private void addPublisher(Resource resource, Property property, JSONObject jsonObject, String jsonKey) {
		if (resource.hasProperty(property)) {
			RDFNode rdfNode = resource.getProperty(property).getObject();

			if (rdfNode.isURIResource()) {
				JSONObject publisher = new JSONObject();
				publisher.put("uri", rdfNode.asResource().getURI());
				addLiterals(rdfNode.asResource(), FOAF.name, publisher, "name", new String[] {}, false);
				addLiterals(rdfNode.asResource(), FOAF.mbox, publisher, "mbox", new String[] {}, false);
				addLiterals(rdfNode.asResource(), FOAF.homepage, publisher, "homepage", new String[] {}, false);
				jsonObject.put(jsonKey, publisher);

			} else if (rdfNode.isLiteral()) {
				String value = rdfNode.asLiteral().getValue().toString().trim();
				if (!value.isEmpty()) {
					JSONObject publisher = new JSONObject();
					publisher.put("name", value);
					jsonObject.put(jsonKey, publisher);
				}

			} else {
				LOGGER.warn("Not a literal or UriResource: " + resource);
			}
		}
	}

}