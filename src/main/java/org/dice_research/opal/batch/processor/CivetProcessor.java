package org.dice_research.opal.batch.processor;

import org.apache.jena.rdf.model.Model;
import org.dice_research.opal.civet.Civet;

/**
 * Civet.
 * 
 * Usage: Use public variables for configuration.
 * 
 * @see https://github.com/projekt-opal/civet
 *
 * @author Adrian Wilke
 */
public class CivetProcessor {

	private Civet civet = null;

	public boolean includeLongRunning = false;
	public boolean logIfNotComputed = true;
	public boolean removeMeasurements = true;

	private void initialize() {
		civet = new Civet()

				// If long running metrics should be included.
				// (optional method call, default: false)
				.setIncludeLongRunning(includeLongRunning)

				// If it should be logged, if a measurement could not be computed
				// (optional method call, default: true)
				.setLogIfNotComputed(logIfNotComputed)

				// If existing measurements should be removed
				// (optional method call, default: true)
				.setRemoveMeasurements(removeMeasurements);
	}

	public void process(Model model, String datasetUri) throws Exception {
		if (civet == null) {
			initialize();
		}

		civet.processModel(model, datasetUri);
	}

}