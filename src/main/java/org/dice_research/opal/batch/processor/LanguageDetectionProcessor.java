package org.dice_research.opal.batch.processor;

import org.apache.jena.rdf.model.Model;
import org.dice_research.opal.metadata.LanguageDetection;

/**
 * Metadata refinement: Language detection.
 * 
 * @see https://github.com/projekt-opal/metadata-refinement
 *
 * @author Adrian Wilke
 */
public class LanguageDetectionProcessor {

	private LanguageDetection languageDetection = null;

	private void initialize() throws Exception {
		languageDetection = new LanguageDetection();
		languageDetection.initialize();
	}

	public void process(Model model, String datasetUri) throws Exception {
		if (languageDetection == null) {
			initialize();
		}

		languageDetection.processModel(model, datasetUri);
	}

}