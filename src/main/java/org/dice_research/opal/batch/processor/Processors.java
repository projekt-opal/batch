package org.dice_research.opal.batch.processor;

import java.util.LinkedList;
import java.util.List;

import org.dice_research.opal.batch.configuration.Cfg;
import org.dice_research.opal.common.interfaces.ModelProcessor;

/**
 * Handles all processors.
 *
 * @author Adrian Wilke
 */
public abstract class Processors {

	/**
	 * Creates a list of new model processor objects.
	 */
	public static List<ModelProcessor> createModelProcessors(Cfg cfg) {
		List<ModelProcessor> modelProcessors = new LinkedList<>();
		new CatfishProcessor().addModelProcessor(cfg, modelProcessors);
		new LanguageDetectionProcessor().addModelProcessor(cfg, modelProcessors);
		new GeoDataProcessor().addModelProcessor(cfg, modelProcessors);
		new CivetProcessor().addModelProcessor(cfg, modelProcessors);
		return modelProcessors;
	}

}