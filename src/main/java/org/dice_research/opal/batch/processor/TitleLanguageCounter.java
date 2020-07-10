package org.dice_research.opal.batch.processor;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.vocabulary.DCTerms;
import org.dice_research.opal.batch.construction.internal.StringCounter;
import org.dice_research.opal.common.interfaces.ModelProcessor;

/**
 * Counts languages of titles.
 * 
 * Does not change model.
 * 
 * @author Adrian Wilke
 */
public class TitleLanguageCounter implements ModelProcessor {

	private StringCounter literalLanguageCounter;
	private StringCounter germanEnglishCounter;

	public static final String DE = "de";
	public static final String EN = "en";
	public static final String DE_EN = "de+en";
	public static final String EMPTY = "[EMPTY]";

	public TitleLanguageCounter(StringCounter literalLanguageCounter, StringCounter germanEnglishCounter) {
		this.literalLanguageCounter = literalLanguageCounter;
		this.germanEnglishCounter = germanEnglishCounter;
	}

	@Override
	public void processModel(Model model, String datasetUri) throws Exception {
		Resource dataset = model.getResource(datasetUri);
		processTriples(dataset, DCTerms.title);
	}

	private void processTriples(Resource s, Property p) {
		StmtIterator stmtIterator = s.listProperties(p);
		boolean hasDe = false;
		boolean hasEn = false;
		while (stmtIterator.hasNext()) {
			RDFNode o = stmtIterator.next().getObject();
			if (o.isLiteral()) {

				String literalLanguage = o.asLiteral().getLanguage();

				// Was empty on reading EDP nt file. Workaround.
				if (literalLanguage.trim().isEmpty()) {
					int index = o.toString().lastIndexOf("\"@");
					if (index != -1) {
						literalLanguage = o.toString().substring(index + 2);
					}
				}

				if (literalLanguage.trim().isEmpty()) {
					literalLanguage = EMPTY;
				}

				// Count exact language
				literalLanguageCounter.increment(literalLanguage);

				// Do not include empty literals
				if (o.asLiteral().getString().trim().isEmpty()) {
					continue;
				}

				// Count de variants
				if (literalLanguage.toLowerCase().startsWith(DE)) {
					hasDe = true;
					germanEnglishCounter.increment(DE);

					// Count en variants
				} else if (literalLanguage.toLowerCase().startsWith(EN)) {
					hasEn = true;
					germanEnglishCounter.increment(EN);
				}
			}

		}

		// Count de and en variants
		if (hasDe && hasEn) {
			germanEnglishCounter.increment(DE_EN);
		}
	}

}