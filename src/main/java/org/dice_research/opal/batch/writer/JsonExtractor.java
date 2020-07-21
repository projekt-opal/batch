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
import org.locationtech.jts.geom.Point;

import io.github.galbiston.geosparql_jena.implementation.GeometryWrapper;
import io.github.galbiston.geosparql_jena.implementation.datatype.WKTDatatype;

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

		// Based on:
		// https://github.com/projekt-opal/converter/blob/master/elasticsearch-writer/src/main/java/org/diceresearch/elasticsearchwriter/entity/DataSet.java

		// --- ES Strings

		jsonObject.put("uri", dataset.getURI());
		addLiterals(dataset, DCTerms.title, jsonObject, "title", new String[] { "", "en" }, false);
		addLiterals(dataset, DCTerms.title, jsonObject, "title_de", new String[] { "de" }, false);
		addLiterals(dataset, DCTerms.description, jsonObject, "description", new String[] { "", "en" }, false);
		addLiterals(dataset, DCTerms.description, jsonObject, "description_de", new String[] { "de" }, false);
		add(dataset, DCAT.landingPage, jsonObject, "landingPage", false);
		addLiterals(dataset, DCTerms.language, jsonObject, "language", new String[] {}, false);
//		add(dataset, DCTerms.issued, jsonObject, "issued", false); // TODO Govdata exception
//		add(dataset, DCTerms.modified, jsonObject, "modified", false); // TODO Govdata exception
		add(dataset, DCTerms.accrualPeriodicity, jsonObject, "accrualPeriodicity", false);
		add(dataset, DCTerms.identifier, jsonObject, "dcatIdentifier", false);

		// --- ES String lists

		add(dataset, Opal.PROP_ORIGINAL_URI, jsonObject, "originalUrls", true);
		addLiterals(dataset, DCAT.keyword, jsonObject, "keywords", new String[] { "", "en" }, true);
		addLiterals(dataset, DCAT.keyword, jsonObject, "keywords_de", new String[] { "", "en" }, true);
		addLiterals(dataset, DCAT.theme, jsonObject, "themes", new String[] {}, true);

		// --- ES objects

		addPublisher(dataset, DCTerms.publisher, jsonObject, "publisher");
		addPublisher(dataset, DCTerms.creator, jsonObject, "creator");

		// TODO (import)
		// Exception in thread "main" ElasticsearchStatusException[Elasticsearch
		// exception [type=mapper_parsing_exception, reason=object mapping for [spatial]
		// tried to parse field [spatial] as object, but found a concrete value]]
		// add(dataset, DCTerms.spatial, jsonObject, "spatial");
		addGeo(dataset, DCTerms.spatial, jsonObject, "spatial");

		addLiterals(dataset, DCAT.contactPoint, jsonObject, "contactPoint", new String[] {}, true); // TODO fields

		// add(dataset, DCTerms.temporal, jsonObject, "temporal"); // TODO

		// --- ES object lists

		addLiterals(dataset, DCTerms.license, jsonObject, "license", new String[] {}, true); // TODO name

		// public List<String> hasQualityMeasurements; // TODO

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

		// Based on:
		// https://github.com/projekt-opal/converter/blob/master/elasticsearch-writer/src/main/java/org/diceresearch/elasticsearchwriter/entity/Distribution.java

		JSONObject jsonObject = new JSONObject();

		// --- ES Strings

		jsonObject.put("uri", distribution.getURI());
		add(distribution, DCTerms.title, jsonObject, "title", false);
		add(distribution, DCTerms.description, jsonObject, "description", false);
		add(distribution, DCTerms.issued, jsonObject, "issued", false);
		add(distribution, DCTerms.modified, jsonObject, "modified", false);

		// TODO (import)
		// Exception in thread "main" ElasticsearchStatusException[Elasticsearch
		// exception [type=mapper_parsing_exception, reason=object mapping for
		// [distributions.license] tried to parse field [null] as object, but found a
		// concrete value]]
		// addLiterals(distribution, DCTerms.license, jsonObject, "license", new
		// String[] {}, true); // TODO name

		add(distribution, DCAT.accessURL, jsonObject, "accessUrl", false);
		add(distribution, DCAT.downloadURL, jsonObject, "downloadUrl", false);
		add(distribution, DCTerms.format, jsonObject, "format", false); // TODO use cleaned

		// --- ES String lists

		add(distribution, Opal.PROP_ORIGINAL_URI, jsonObject, "originalURLs", false); // TODO test
		add(distribution, DCTerms.rights, jsonObject, "rights", true);

		// --- ES long

		add(distribution, DCAT.byteSize, jsonObject, "byteSize", false);

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
	 * Adds entries to JSON.
	 * 
	 * If a resource is found, the respective URI is used. For literals, a non-empty
	 * value is used.
	 */
	private void add(Resource resource, Property property, JSONObject jsonObject, String jsonKey,
			boolean multipleValues) {
		StmtIterator stmtIterator = resource.listProperties(property);
		while (stmtIterator.hasNext()) {
			RDFNode rdfNode = stmtIterator.next().getObject();
			String value = null;

			if (rdfNode.isURIResource()) {
				value = rdfNode.asResource().getURI().trim();

			} else if (rdfNode.isLiteral()) {
				value = rdfNode.asLiteral().getValue().toString().trim();

			} else {
				LOGGER.warn("Not a literal or UriResource: " + resource);
			}

			if (value != null && !value.isEmpty()) {
				if (multipleValues) {
					jsonObject.append(jsonKey, value);
				} else {
					jsonObject.put(jsonKey, value);
					return;
				}
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
				// TOOD
//				LOGGER.warn("Not a literal: " + resource);
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

	/**
	 * TODO: Dev method
	 */
	private void recursivelyAddTriples(Resource resource, StringBuilder sb) {
		StmtIterator stmtIterator = resource.listProperties();
		while (stmtIterator.hasNext()) {
			Statement stmt = stmtIterator.next();
			sb.append(" ");
			sb.append(stmt.toString());
			sb.append(System.lineSeparator());
			if (stmt.getObject().isResource()) {
				recursivelyAddTriples(stmt.getObject().asResource(), sb);
			}
		}
	}

	/**
	 * Adds geo data.
	 * 
	 * TODO
	 */
	private void addGeo(Resource resource, Property property, JSONObject jsonObject, String jsonKey) {
		if (resource.hasProperty(property)) {
			RDFNode rdfNode = resource.getProperty(property).getObject();

			if (rdfNode.isURIResource()) {

			} else if (rdfNode.isLiteral()) {

			} else {

				// https://www.elastic.co/guide/en/elasticsearch/reference/7.3/geo-point.html

				// Check "Geohash"

				// Blank nodes in Govdata sometimes contain geo data

				Resource blankNode = rdfNode.asResource();
				if (blankNode.hasProperty(DCAT.centroid)) {
					RDFNode centroid = blankNode.getProperty(DCAT.centroid).getObject();
					if (centroid.isLiteral()) {

						if (WKTDatatype.checkURI(centroid.asLiteral().getDatatypeURI())) {
							String lex = centroid.asLiteral().getLexicalForm().trim();
							GeometryWrapper geometryWrapper = WKTDatatype.INSTANCE.parse(lex);
							Point point = geometryWrapper.getParsingGeometry().getCentroid();

							JSONObject geometry = new JSONObject();
							geometry.put("geometry", point.getX() + "," + point.getY());
							jsonObject.put(jsonKey, geometry);
						}
					}
				}

				// StringBuilder sb = new StringBuilder();
				// recursivelyAddTriples(rdfNode.asResource(), sb);
				// System.err.println("B: " + rdfNode.asResource().getURI() +
				// System.lineSeparator() + sb.toString());

			}
		}
	}

}