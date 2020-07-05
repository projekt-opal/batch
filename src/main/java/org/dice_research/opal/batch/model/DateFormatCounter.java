package org.dice_research.opal.batch.model;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.vocabulary.DCAT;
import org.apache.jena.vocabulary.DCTerms;
import org.dice_research.opal.batch.construction.internal.StringCounter;
import org.dice_research.opal.common.interfaces.ModelProcessor;

/**
 * Counts types and values of date literals.
 * 
 * Does not change model.
 * 
 * Configuration: Use public variables.
 *
 * @author Adrian Wilke
 */
public class DateFormatCounter implements ModelProcessor {

	// Configuration

	public boolean includeDataset = true;
	public boolean includeDistributions = true;

	public boolean countResources = true;
	public boolean countBlankNodes = true;

	public String blankNodeKey = "[BLANK]";
	public String resourceKey = "[RESOURCE]";

	public Character number = '0';
	public Character letter = 'l';
	public Character unknown = '?';

	// Internal

	private StringCounter stringFormatCounter;
	private StringCounter datatypeCounter;

	public DateFormatCounter(StringCounter stringFormatCounter, StringCounter datatypeCounter) {
		this.stringFormatCounter = stringFormatCounter;
		this.datatypeCounter = datatypeCounter;
	}

	@Override
	public void processModel(Model model, String datasetUri) throws Exception {

		Resource dataset = model.getResource(datasetUri);

		if (includeDataset) {
			processTriples(dataset, DCTerms.issued);
			processTriples(dataset, DCTerms.modified);
		}

		if (includeDistributions) {
			StmtIterator stmtIterator = dataset.listProperties(DCAT.distribution);
			while (stmtIterator.hasNext()) {
				RDFNode object = stmtIterator.next().getObject();
				if (object.isResource()) {
					processTriples(object.asResource(), DCTerms.issued);
					processTriples(object.asResource(), DCTerms.modified);
				}
			}
		}
	}

	private void processTriples(Resource s, Property p) {
		StmtIterator stmtIterator = s.listProperties(p);
		while (stmtIterator.hasNext()) {
			RDFNode o = stmtIterator.next().getObject();

			if (o.isLiteral()) {
				String datatypeUri = o.asLiteral().getDatatypeURI();
				datatypeCounter.increment(datatypeUri);

				if (datatypeUri.equals("http://www.w3.org/2001/XMLSchema#string")) {
					String string = o.asLiteral().getString();
					int index = string.lastIndexOf("\"^^");
					if (index != -1) {
						string = string.substring(1, index);
					}
					stringFormatCounter.increment(getStringFormat(string));
				}

			} else if (countResources && o.isURIResource()) {
				datatypeCounter.increment(resourceKey);

			} else if (countBlankNodes && o.isAnon()) {
				datatypeCounter.increment(blankNodeKey);
			}
		}
	}

	private String getStringFormat(String string) {
		StringBuilder stringBuilder = new StringBuilder();
		for (char c : string.toCharArray()) {
			int i = (int) c;

			// 0 9
			if (i >= 48 && i <= 57) {
				stringBuilder.append(number);
			}
			// A Z
			else if (i >= 65 && i <= 90) {
				stringBuilder.append(letter);
			}
			// a z
			else if (i >= 97 && i <= 122) {
				stringBuilder.append(letter);
			}

			else if (c == 'ä' || c == 'ö' || c == 'ü' || c == 'ß') {
				stringBuilder.append(letter);
			}

			else if (c == 'Ä' || c == 'Ö' || c == 'Ü') {
				stringBuilder.append(letter);
			}

			else {
				stringBuilder.append(c);
			}
		}
		return stringBuilder.toString();
	}
}