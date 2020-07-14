package org.dice_research.opal.batch.writer.elasticsearch;

import java.util.LinkedList;
import java.util.List;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.StmtIterator;

public abstract class ModelUtils {

	public static List<RDFNode> getRdfNodes(Model model, Resource subject, Property predicate) {
		List<RDFNode> rdfNodes = new LinkedList<>();
		StmtIterator stmtIterator = model.listStatements(subject, predicate, (RDFNode) null);
		while (stmtIterator.hasNext()) {
			rdfNodes.add(stmtIterator.next().getObject());
		}
		return rdfNodes;
	}

	public static List<Resource> getUriResources(Model model, Resource subject, Property predicate) {
		List<Resource> rdfNodes = new LinkedList<>();
		StmtIterator stmtIterator = model.listStatements(subject, predicate, (RDFNode) null);
		while (stmtIterator.hasNext()) {
			RDFNode rdfNode = stmtIterator.next().getObject();
			if (rdfNode.isURIResource()) {
				rdfNodes.add(rdfNode.asResource());
			}
		}
		return rdfNodes;
	}

}