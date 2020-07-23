package org.dice_research.opal.batch.writer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.NodeIterator;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.SimpleSelector;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.impl.StatementImpl;
import org.apache.jena.vocabulary.DCAT;

/**
 * Add themes of distributions to dataset. Removes unknown themes.
 * 
 * @see https://github.com/projekt-opal/converter/blob/12dfe8f6e03ccbcb53c2bc835f00eebce798a6d3/elasticsearch-writer/src/main/java/org/diceresearch/elasticsearchwriter/service/ThemeCleaner.java
 *
 * @author mnafshin
 */
public class ElasticsearchThemes {

	private final String[] themes = new String[] {

			"http://publications.europa.eu/resource/authority/data-theme/AGRI",
			"http://publications.europa.eu/resource/authority/data-theme/EDUC",
			"http://publications.europa.eu/resource/authority/data-theme/ENVI",
			"http://publications.europa.eu/resource/authority/data-theme/ENER",
			"http://publications.europa.eu/resource/authority/data-theme/TRAN",
			"http://publications.europa.eu/resource/authority/data-theme/TECH",
			"http://publications.europa.eu/resource/authority/data-theme/ECON",
			"http://publications.europa.eu/resource/authority/data-theme/SOCI",
			"http://publications.europa.eu/resource/authority/data-theme/HEAL",
			"http://publications.europa.eu/resource/authority/data-theme/GOVE",
			"http://publications.europa.eu/resource/authority/data-theme/REGI",
			"http://publications.europa.eu/resource/authority/data-theme/JUST",
			"http://publications.europa.eu/resource/authority/data-theme/INTR",
			"http://publications.europa.eu/resource/authority/data-theme/OP_DATPRO" };

	public void cleanThemes(Model model, Resource dataset) {
		List<Statement> toBeAdded = new ArrayList<>();
		NodeIterator nodeIterator = model.listObjectsOfProperty(dataset, DCAT.theme);
		while (nodeIterator.hasNext()) {
			RDFNode rdfNode = nodeIterator.nextNode();
			if (Arrays.stream(themes).noneMatch(s -> s.equals(rdfNode.toString()))) {
				Set<Statement> triples = cleanThemesRecursively(model, dataset, rdfNode);
				if (triples != null)
					toBeAdded.addAll(triples);
			}
		}

		model.add(toBeAdded);
	}

	/**
	 * Removes unknown themes. Collects known themes.
	 */
	private Set<Statement> cleanThemesRecursively(Model model, Resource dataset, RDFNode node) {
		if (!node.isResource())
			return null;
		Set<Statement> toBeAdded = new HashSet<>();

		// Remove theme from dataset
		model.remove(dataset, DCAT.theme, node);

		// Go through triples of current node
		List<Statement> statements = model.listStatements(new SimpleSelector(node.asResource(), null, (RDFNode) null))
				.toList();
		statements.forEach(statement -> {
			RDFNode object = statement.getObject();
			if (Arrays.stream(themes).anyMatch(s -> s.equals(object.toString())))
				// If theme is known: Add triple
				toBeAdded.add(new StatementImpl(dataset, DCAT.theme, object));
			else {
				// Go on recursively
				if (model.contains(statement)) { // to prevent infinite loop if there is a loop in the graph
					model.remove(statement.getSubject(), statement.getPredicate(), object);
					cleanThemesRecursively(model, dataset, object);
				}
			}
		});
		return toBeAdded;
	}
}