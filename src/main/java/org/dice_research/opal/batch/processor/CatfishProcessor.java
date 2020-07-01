package org.dice_research.opal.batch.processor;

import org.apache.jena.rdf.model.Model;
import org.dice_research.opal.catfish.Catfish;
import org.dice_research.opal.catfish.config.CleaningConfig;

/**
 * Catfish.
 * 
 * Usage: Use public variables for configuration.
 * 
 * @see https://github.com/projekt-opal/catfish
 *
 * @author Adrian Wilke
 */
public class CatfishProcessor {

	private Catfish catfish = null;

	public boolean cleanEmptyBlankNodes = true;
	public boolean cleanFormats = true;
	public boolean cleanLiterals = true;
	public boolean equalizeDateFormats = false;

	private void initialize() {

		CleaningConfig cleaningConfig = new CleaningConfig()

				// Remove blank nodes, which are not subject of triples
				// (optional method call, default: true)
				.setCleanEmptyBlankNodes(cleanEmptyBlankNodes)

				// Remove triples with literals as object, which contain no value or unreadable.
				// And also extract Language Tag and DataType if it is mistakenly inside the
				// string
				// (optional method call, default: true)
				.setCleanLiterals(cleanLiterals)

				// Check dct:format and dcat:mediaType for values and create new triples.
				// (optional method call, default: true)
				.setCleanFormats(cleanFormats)

				// Rewrites date formats
				// (optional method call, default: false)
				.setEqualizeDateFormats(equalizeDateFormats);

		catfish = new Catfish(cleaningConfig);

	}

	public void process(Model model, String datasetUri) throws Exception {
		if (catfish == null) {
			initialize();
		}

		catfish.processModel(model, datasetUri);
	}

}