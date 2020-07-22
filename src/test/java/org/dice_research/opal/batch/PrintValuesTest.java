package org.dice_research.opal.batch;

import java.io.File;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.vocabulary.DCAT;
import org.apache.jena.vocabulary.DCTerms;
import org.dice_research.opal.batch.configuration.Cfg;
import org.dice_research.opal.batch.configuration.CfgKeys;
import org.dice_research.opal.batch.construction.ConstructorManager;
import org.dice_research.opal.batch.construction.IndependentConstructor;
import org.dice_research.opal.batch.utils.CfgUtils;
import org.dice_research.opal.batch.utils.FileUtils;
import org.dice_research.opal.common.interfaces.ModelProcessor;
import org.dice_research.opal.common.vocabulary.Dqv;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * For development: Prints RDF contents.
 * 
 * To run, specify an environment variable with key {@link #INPUT_RDF_FILE}.
 *
 * @author Adrian Wilke
 */
public class PrintValuesTest implements ModelProcessor {

	public static final String INPUT_RDF_FILE = "inputRdfFile";

	private File input;

	@Before
	public void setUp() throws Exception {
		String inputRdfFile = System.getenv(INPUT_RDF_FILE);
		Assume.assumeNotNull(inputRdfFile);
		this.input = new File(inputRdfFile);
	}

	@Override
	public void processModel(Model model, String datasetUri) throws Exception {
		Property property;

		Resource dataset = model.getResource(datasetUri);

		property = DCTerms.title;
		property = DCTerms.issued;

		if (Boolean.FALSE)
			System.out.println(collectNodes(dataset, property));

		if (Boolean.FALSE)
			for (RDFNode rdfNode : collectNodes(dataset, Dqv.HAS_QUALITY_MEASUREMENT)) {
				System.out.println(recursivelyAddTriples(rdfNode.asResource(), null, null));
			}

		if (Boolean.FALSE)
			for (Resource distribution : collectResources(dataset, DCAT.distribution)) {
				System.out.println(distribution);
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

		// Delete output directory
		FileUtils.deleteDirectory(outputDir);
	}

}