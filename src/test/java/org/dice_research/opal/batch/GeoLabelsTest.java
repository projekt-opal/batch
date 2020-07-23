package org.dice_research.opal.batch;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.RDFS;
import org.dice_research.opal.batch.configuration.Cfg;
import org.dice_research.opal.batch.configuration.CfgKeys;
import org.dice_research.opal.batch.construction.ConstructorManager;
import org.dice_research.opal.batch.construction.IndependentConstructor;
import org.dice_research.opal.batch.utils.CfgUtils;
import org.dice_research.opal.batch.utils.FileUtils;
import org.dice_research.opal.common.interfaces.ModelProcessor;
import org.dice_research.opal.metadata.GeoData;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * Extracts geo labels.
 * 
 * To run, specify an environment variable with key {@link #INPUT}.
 *
 * @author Adrian Wilke
 */
public class GeoLabelsTest implements ModelProcessor {

	public static final String INPUT = "input";

	private File input;
	private Map<String, String> urisToLabels = new HashMap<>();
	private Map<String, Set<String>> datasetUrisToSpatialUris = new HashMap<>();

	@Before
	public void setUp() throws Exception {
		String inputValue = System.getenv(INPUT);
		Assume.assumeNotNull(inputValue);
		this.input = new File(inputValue);
	}

	@Override
	public void processModel(Model model, String datasetUri) throws Exception {
		Resource dataset = model.getResource(datasetUri);

		// Extract places
		GeoData geoGeoData = new GeoData();
		Model newModel = ModelFactory.createDefaultModel().add(model);
		geoGeoData.processModel(newModel, datasetUri);

		// If map is not empty, something was found
		if (!geoGeoData.getUrisToLabels().isEmpty()) {

			// Collect URIs and labels
			urisToLabels.putAll(geoGeoData.getUrisToLabels());

			// If spatial resource URI is known: Collect.
			Set<String> uris = geoGeoData.getUrisToLabels().keySet();
			StmtIterator stmtIterator = newModel.listStatements(dataset, DCTerms.spatial, (RDFNode) null);
			while (stmtIterator.hasNext()) {
				RDFNode rdfNode = stmtIterator.next().getObject();
				if (rdfNode.isURIResource() && uris.contains(rdfNode.asResource().getURI())) {
					if (!datasetUrisToSpatialUris.containsKey(datasetUri)) {
						datasetUrisToSpatialUris.put(datasetUri, new HashSet<>());
					}
					datasetUrisToSpatialUris.get(datasetUri).add(rdfNode.asResource().getURI());
				}
			}
		}
	}

	public StringBuilder recursivelyAddTriples(Resource resource, StringBuilder sb, Set<Resource> processedResources) {
		if (sb == null) {
			sb = new StringBuilder();
		}
		if (processedResources == null) {
			processedResources = new HashSet<>();
		}
		if (processedResources.contains(resource)) {
			return sb;
		} else {
			processedResources.add(resource);
		}

		StmtIterator stmtIterator = resource.listProperties();
		while (stmtIterator.hasNext()) {
			Statement stmt = stmtIterator.next();
			sb.append(" ");
			sb.append(stmt.toString());
			sb.append(System.lineSeparator());
			if (stmt.getObject().isResource()) {
				recursivelyAddTriples(stmt.getObject().asResource(), sb, processedResources);
			}
		}
		return sb;
	}

	public List<Resource> collectResources(Resource resource, Property property) {
		List<Resource> list = new LinkedList<>();
		StmtIterator stmtIterator = resource.listProperties(property);
		while (stmtIterator.hasNext()) {
			Statement statement = stmtIterator.next();
			if (statement.getObject().isResource()) {
				list.add(statement.getObject().asResource());
			}
		}
		return list;
	}

	public List<RDFNode> collectNodes(Resource resource, Property property) {
		List<RDFNode> list = new LinkedList<>();
		StmtIterator stmtIterator = resource.listProperties(property);
		while (stmtIterator.hasNext()) {
			list.add(stmtIterator.next().getObject());
		}
		return list;
	}

	@Test
	public void test() throws Exception {

		// Disable everything
		Cfg cfg = new CfgUtils().disableAll().getCfg();

		// Set input
		cfg.set(CfgKeys.IO_INPUT, input.getAbsolutePath());

		// Set temporary output directory
		File outputDir = FileUtils.createtmpDirectory(getClass());
		cfg.set(CfgKeys.IO_OUTPUT_DIRECTORY, outputDir.getAbsolutePath());

		// Run
		ConstructorManager constructorManager = new ConstructorManager()
				.addConstructor(new IndependentConstructor(this));
		new Batch().execute(cfg, constructorManager);

		Model model = ModelFactory.createDefaultModel();
		model.setNsPrefix("lau", "http://projekt-opal.de/launuts/lau/DE/");
		model.setNsPrefix("nuts", "http://data.europa.eu/nuts/code/");
		model.setNsPrefix("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
		for (Entry<String, String> uriToLabel : urisToLabels.entrySet()) {
			model.add(ResourceFactory.createResource(uriToLabel.getKey()), RDFS.label,
					ResourceFactory.createLangLiteral(uriToLabel.getValue(), "de"));
		}
		try (FileOutputStream fos = new FileOutputStream(new File(outputDir, "labels-geo.ttl"))) {
			RDFDataMgr.write(fos, model, Lang.TURTLE);
		}

		model = ModelFactory.createDefaultModel();
		model.setNsPrefix("lau", "http://projekt-opal.de/launuts/lau/DE/");
		model.setNsPrefix("nuts", "http://data.europa.eu/nuts/code/");
		model.setNsPrefix("dataset", "http://projekt-opal.de/dataset/");
		model.setNsPrefix("dct", "http://purl.org/dc/terms/");
		for (Entry<String, Set<String>> datasetUriToSpatialUri : datasetUrisToSpatialUris.entrySet()) {
			Resource dataset = ResourceFactory.createResource(datasetUriToSpatialUri.getKey());
			for (String spatialUri : datasetUriToSpatialUri.getValue()) {
				model.add(dataset, DCTerms.spatial, ResourceFactory.createResource(spatialUri));
			}
		}
		try (FileOutputStream fos = new FileOutputStream(new File(outputDir, "geo.ttl"))) {
			RDFDataMgr.write(fos, model, Lang.TURTLE);
		}

		System.out.println("Wrote: " + outputDir.getAbsolutePath());
	}

}