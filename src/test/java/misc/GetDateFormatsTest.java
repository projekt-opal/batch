package misc;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.vocabulary.DCAT;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.RDF;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dice_research.opal.batch.Batch;
import org.dice_research.opal.batch.configuration.Cfg;
import org.dice_research.opal.batch.configuration.CfgKeys;
import org.dice_research.opal.batch.processor.Processors;
import org.dice_research.opal.common.interfaces.ModelProcessor;
import org.junit.Assume;
import org.junit.Test;

/**
 * This test extracts DCAT date formats.
 * 
 * Usage: Specify input source (optional: and input graph) in {@link #test()}.
 *
 * @author Adrian Wilke
 */
public class GetDateFormatsTest implements ModelProcessor {

	private static final Logger LOGGER = LogManager.getLogger();

	Set<String> datatypeUris = new TreeSet<>();
	Map<String, Long> stringFormats = new HashMap<>();

	@Test
	public void test() throws Exception {

		// Does not test functionality
		Assume.assumeTrue("Development test", false);

		String input = null, inputGraph = null;

		if (Boolean.TRUE) {
			// https://hobbitdata.informatik.uni-leipzig.de/OPAL/OpalGraph/2019-06-24/
			input = "/home/adi/DICE/Data/OpalGraph/2019-06-24/subset";
			inputGraph = null;
		}

		// TODO: Check why no results are created
		if (Boolean.FALSE) {
			// https://hobbitdata.informatik.uni-leipzig.de/OPAL/OpalGraph/2020-02-19/
			input = "/home/adi/DICE/Data/OpalGraph/2020-02-19/opal_2020-02-19_15-05-05.nq";
			inputGraph = "http://projekt-opal.de";
		}

		extract(new File(input), inputGraph);
	}

	private void extract(File input, String inputGraph) throws Exception {

		Assume.assumeTrue(input.canRead());

		Cfg cfg = new Cfg();
		cfg.set(CfgKeys.IO_INPUT, input.getAbsolutePath());

		if (inputGraph != null) {
			cfg.set(CfgKeys.IO_INPUT_GRAPH, inputGraph);
		}

		File outputDirectory = new File(System.getProperty("java.io.tmpdir"), this.getClass().getName());
		cfg.set(CfgKeys.IO_OUTPUT, outputDirectory.getAbsolutePath());

		Processors processors = new Processors();
		processors.addModelProcessor(this);

		new Batch().execute(cfg, processors);

		for (File file : outputDirectory.listFiles()) {
			file.delete();
		}
		outputDirectory.delete();

		for (String datatypeUri : datatypeUris) {
			System.out.println(datatypeUri);
		}

		System.out.println();
		for (Entry<String, Long> entry : sortByValue(stringFormats).entrySet()) {
			System.out.println(entry.getKey() + " " + entry.getValue());
		}
	}

	@Override
	public void processModel(Model model, String datasetUri) throws Exception {
		ResIterator resIterator;
		Resource resource;

		// Datasets

		resIterator = model.listResourcesWithProperty(RDF.type, DCAT.Dataset);
		while (resIterator.hasNext()) {
			resource = resIterator.next();
			processTriples(model, resource, DCTerms.modified, datasetUri);
			processTriples(model, resource, DCTerms.issued, datasetUri);
		}

		// Distributions

		resIterator = model.listResourcesWithProperty(RDF.type, DCAT.Distribution);
		while (resIterator.hasNext()) {
			resource = resIterator.next();
			processTriples(model, resource, DCTerms.modified, datasetUri);
			processTriples(model, resource, DCTerms.issued, datasetUri);
		}
	}

	private void processTriples(Model model, Resource s, Property p, String datasetUri) {
		StmtIterator stmtIterator = s.listProperties(p);
		while (stmtIterator.hasNext()) {
			RDFNode rdfNode = stmtIterator.next().getObject();
			if (rdfNode.isLiteral()) {
				String datatypeUri = rdfNode.asLiteral().getDatatypeURI();
				if (datatypeUri.equals("http://www.w3.org/2001/XMLSchema#string")) {
					getStringFormat(rdfNode.asLiteral().getString());
				}
				datatypeUris.add(datatypeUri);
			} else {
				LOGGER.warn(rdfNode.toString() + " " + datasetUri);
			}
		}
	}

	private void getStringFormat(String string) {
		StringBuilder stringBuilder = new StringBuilder();
		for (char c : string.toCharArray()) {
			int i = (int) c;
			// 0 9
			if (i >= 48 && i <= 57) {
				stringBuilder.append('N');
			}
			// A Z
			else if (i >= 65 && i <= 90) {
				stringBuilder.append('C');
			}
			// a z
			else if (i >= 97 && i <= 122) {
				stringBuilder.append('C');
			}

			else if (c == 'ä' || c == 'ö' || c == 'ü' || c == 'ß') {
				stringBuilder.append('C');
			}

			else if (c == 'Ä' || c == 'Ö' || c == 'Ü') {
				stringBuilder.append('C');
			}

			else {
				stringBuilder.append(c);
			}
		}
		String format = stringBuilder.toString();
		if (!stringFormats.containsKey(format)) {
			stringFormats.put(format, 0l);
		}
		stringFormats.put(format, stringFormats.get(format) + 1);
	}

	/**
	 * @see https://dzone.com/articles/how-to-sort-a-map-by-value-in-java-8
	 */
	private static Map<String, Long> sortByValue(final Map<String, Long> wordCounts) {
		return wordCounts.entrySet().stream().sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
	}
}