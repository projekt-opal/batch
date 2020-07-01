package org.dice_research.opal.batch.processor;

import org.apache.jena.rdf.model.Model;
import org.dice_research.opal.metadata.GeoData;

/**
 * Metadata refinement: Geo data.
 * 
 * @see https://github.com/projekt-opal/metadata-refinement
 *
 * @author Adrian Wilke
 */
public class GeoDataProcessor {

	private GeoData geoData = null;

	private void initialize() throws Exception {
		geoData = new GeoData();
	}

	public void process(Model model, String datasetUri) throws Exception {
		if (geoData == null) {
			initialize();
		}

		geoData.processModel(model, datasetUri);
	}

}