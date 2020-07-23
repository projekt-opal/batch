package org.dice_research.opal.batch.writer;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
import org.apache.jena.vocabulary.VCARD4;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dice_research.opal.common.interfaces.ModelProcessor;
import org.dice_research.opal.common.vocabulary.Dqv;
import org.dice_research.opal.common.vocabulary.Opal;
import org.json.JSONArray;
import org.json.JSONObject;
import org.locationtech.jts.geom.Point;

import io.github.galbiston.geosparql_jena.implementation.GeometryWrapper;
import io.github.galbiston.geosparql_jena.implementation.datatype.WKTDatatype;

/**
 * Extracts data from model and creates a JSON object.
 * 
 * Usage: Create one {@link ElasticsearchJson} instance for each execution.
 * After calling {@link #processModel(Model, String)}, the result is available
 * via {@link #getJson()}.
 * 
 * Based on these Elasticsearch mappings:
 * https://github.com/projekt-opal/opaldata/blob/6a2ecdc4a41c8eb7f168543d732560ece82c1451/elasticsearch-initialization/mappings.json
 * and this code:
 * https://github.com/projekt-opal/converter/tree/master/elasticsearch-writer/src/main/java/org/diceresearch/elasticsearchwriter/
 *
 * @author Adrian Wilke
 */
public class ElasticsearchJson implements ModelProcessor {

	private static final Logger LOGGER = LogManager.getLogger();

	private static final boolean PARALLEL_STREAMS = true;
	private static final int SPLITERATOR_CHARACTERISTICS = Spliterator.IMMUTABLE | Spliterator.NONNULL;

	private static final Pattern DATE_PATTERN = Pattern.compile("(^\\d{4}-\\d{2}-\\d{2})(.*)");

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
		addLiterals(dataset, DCTerms.language, jsonObject, "language", null, false);
		addDate(dataset, DCTerms.issued, jsonObject, "issued");
		addDate(dataset, DCTerms.modified, jsonObject, "modified");
		add(dataset, DCAT.landingPage, jsonObject, "landingPage", false);
		add(dataset, DCTerms.accrualPeriodicity, jsonObject, "accrualPeriodicity", false);
		add(dataset, DCTerms.identifier, jsonObject, "dcatIdentifier", false);

		// --- ES String lists

		addLiterals(dataset, DCAT.keyword, jsonObject, "keywords", new String[] { "", "en" }, true);
		addLiterals(dataset, DCAT.keyword, jsonObject, "keywords_de", new String[] { "", "en" }, true);
		addThemes(dataset, DCAT.theme, jsonObject, "themes");
		add(dataset, Opal.PROP_ORIGINAL_URI, jsonObject, "originalUrls", true);

		// --- ES objects

		addPublisher(dataset, DCTerms.publisher, jsonObject, "publisher");
		addPublisher(dataset, DCTerms.creator, jsonObject, "creator");
		addGeo(dataset, DCTerms.spatial, jsonObject, "spatial");
		addContactPoint(dataset, DCAT.contactPoint, jsonObject, "contactPoint");

		// TODO DCTerms.temporal (startDate, endDate) currently not used

		// --- ES object lists

		addLicense(dataset, DCTerms.license, jsonObject, "license");
		addMetrics(dataset, Dqv.HAS_QUALITY_MEASUREMENT, jsonObject, "hasQualityMeasurements");

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
		addDate(distribution, DCTerms.issued, jsonObject, "issued");
		addDate(distribution, DCTerms.modified, jsonObject, "modified");
		add(distribution, DCAT.accessURL, jsonObject, "accessUrl", false);
		add(distribution, DCAT.downloadURL, jsonObject, "downloadUrl", false);
		addFormat(distribution, DCTerms.format, jsonObject, "format");
		addLicense(distribution, DCTerms.license, jsonObject, "license");

		// --- ES String lists

		add(distribution, Opal.PROP_ORIGINAL_URI, jsonObject, "originalURLs", false);
		add(distribution, DCTerms.rights, jsonObject, "rights", true);

		// --- ES long

		addBytes(distribution, DCAT.byteSize, jsonObject, "byteSize");

		distributions.put(jsonObject);
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

			} else if (rdfNode.isAnon()) {

			} else if (rdfNode.isLiteral()) {
				value = rdfNode.asLiteral().getValue().toString().trim();

			} else if (rdfNode.isStmtResource()) {
				LOGGER.warn("RDF* triple term");
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
	 * Adds literal values to JSON.
	 */
	private void addLiterals(Resource resource, Property property, JSONObject jsonObject, String jsonKey,
			String[] languages, boolean multipleValues) {
		StmtIterator stmtIterator = resource.listProperties(property);
		while (stmtIterator.hasNext()) {
			RDFNode rdfNode = stmtIterator.next().getObject();
			if (rdfNode.isLiteral()) {
				String language = rdfNode.asLiteral().getLanguage();
				if (languages == null || languages.length == 0 || Arrays.asList(languages).contains(language)) {
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
			}
		}
	}

	private void addContactPoint(Resource resource, Property property, JSONObject jsonObject, String jsonKey) {
		if (resource.hasProperty(property)) {
			RDFNode rdfNode = resource.getProperty(property).getObject();

			if (rdfNode.isResource()) {
				Resource contact = rdfNode.asResource();
				JSONObject jo = new JSONObject();
				addLiterals(contact, VCARD4.hasEmail, jo, "email", new String[] {}, false);
				addLiterals(contact, VCARD4.fn, jo, "name", new String[] {}, false);

				if (contact.hasProperty(VCARD4.hasAddress)) {
					RDFNode hasAddress = contact.getProperty(VCARD4.hasAddress).getObject();
					if (hasAddress.isResource() && hasAddress.asResource().hasProperty(VCARD4.street_address)) {
						jo.put("adress",
								hasAddress.asResource().getProperty(VCARD4.street_address).getObject().toString());
					}
				}

				if (contact.hasProperty(VCARD4.hasTelephone)) {
					RDFNode hasTel = contact.getProperty(VCARD4.hasTelephone).getObject();
					if (hasTel.isResource() && hasTel.asResource().hasProperty(VCARD4.hasValue)) {
						jo.put("phone", hasTel.asResource().getProperty(VCARD4.hasValue).getObject().toString());
					}
				}

				jsonObject.put(jsonKey, jo);
			}
		}
	}

	private void addMetrics(Resource resource, Property property, JSONObject jsonObject, String jsonKey) {
		Map<String, String> measurements = new HashMap<>();
		StmtIterator stmtIterator = resource.listProperties(property);
		while (stmtIterator.hasNext()) {
			Statement stmt = stmtIterator.next();
			RDFNode rdfNode = stmt.getObject();
			if (rdfNode.isResource()) {
				Resource measurement = rdfNode.asResource();
				String metric = null;
				String value = null;
				if (measurement.hasProperty(Dqv.IS_MEASUREMENT_OF)) {
					metric = measurement.getProperty(Dqv.IS_MEASUREMENT_OF).getObject().toString();
				}
				if (measurement.hasProperty(Dqv.HAS_VALUE)) {
					value = measurement.getProperty(Dqv.HAS_VALUE).getObject().asLiteral().getLexicalForm();
				}
				if (metric != null && value != null) {
					measurements.put(metric, value);
				}
			}
		}
		if (!measurements.isEmpty()) {
			JSONArray ja = new JSONArray();
			for (Entry<String, String> entry : measurements.entrySet()) {
				JSONObject jo = new JSONObject();
				jo.put("isMeasurementOf", entry.getKey());
				jo.put("value", entry.getValue());
				ja.put(jo);
			}
			jsonObject.put(jsonKey, ja);
		}
	}

	/**
	 * Adds date, if it starts with YYYY-MM-DD.
	 */
	private void addDate(Resource resource, Property property, JSONObject jsonObject, String jsonKey) {
		if (resource.hasProperty(property)) {
			RDFNode rdfNode = resource.getProperty(property).getObject();
			if (rdfNode.isLiteral()) {
				String value = rdfNode.asLiteral().getValue().toString().trim();
				Matcher matcher = DATE_PATTERN.matcher(value);
				if (matcher.find()) {
					jsonObject.put(jsonKey, matcher.group(1));
				}
			}
		}
	}

	private void addFormat(Resource resource, Property property, JSONObject jsonObject, String jsonKey) {
		StmtIterator stmtIterator = resource.listProperties(property);
		while (stmtIterator.hasNext()) {
			RDFNode rdfNode = stmtIterator.next().getObject();
			if (rdfNode.isURIResource()) {
				Resource format = rdfNode.asResource();
				if (format.getURI().startsWith(Opal.NS_OPAL_FORMAT)) {
					jsonObject.put(jsonKey, format.getURI().substring(Opal.NS_OPAL_FORMAT.length()));
				}
			}
		}
	}

	private void addLicense(Resource resource, Property property, JSONObject jsonObject, String jsonKey) {
		JSONObject jo = new JSONObject();
		add(resource, property, jsonObject, "name", true);
		jsonObject.put(jsonKey, jo);
		// TODO DCTerms.license / license.name currently not used
	}

	private void addBytes(Resource resource, Property property, JSONObject jsonObject, String jsonKey) {
		if (resource.hasProperty(property)) {
			RDFNode rdfNode = resource.getProperty(property).getObject();
			if (rdfNode.isLiteral()) {
				try {
					jsonObject.put(jsonKey, Long.parseLong(rdfNode.asLiteral().getLexicalForm()));
				} catch (NumberFormatException e) {
				}
			}
		}
	}

	/**
	 * Adds geo data.
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
			}
		}
	}

	private void addThemes(Resource resource, Property property, JSONObject jsonObject, String jsonKey) {
		StmtIterator stmtIterator = resource.listProperties(property);
		while (stmtIterator.hasNext()) {
			RDFNode rdfNode = stmtIterator.next().getObject();

			if (rdfNode.isURIResource()) {
				jsonObject.append(jsonKey, rdfNode.asResource().getURI());

			} else if (rdfNode.isLiteral()) {
				jsonObject.append(jsonKey, rdfNode.asLiteral().getLexicalForm());
			}
		}
	}

	/**
	 * Gets a stream of triples for the resource-property-combination.
	 */
	private Stream<Statement> getStatementStream(Resource resource, Property property) {
		return StreamSupport.stream(
				Spliterators.spliteratorUnknownSize(resource.listProperties(property), SPLITERATOR_CHARACTERISTICS),
				PARALLEL_STREAMS);
	}

}