package org.dice_research.opal.batch.processor;

import java.util.LinkedList;
import java.util.List;

import org.dice_research.opal.batch.configuration.Cfg;
import org.dice_research.opal.batch.processor.opal.CatfishProcessor;
import org.dice_research.opal.batch.processor.opal.CivetProcessor;
import org.dice_research.opal.batch.processor.opal.GeoDataProcessor;
import org.dice_research.opal.batch.processor.opal.LanguageDetectionProcessor;
import org.dice_research.opal.common.interfaces.ModelProcessor;

/**
 * Handles processors.
 *
 * @author Adrian Wilke
 */
public class Processors {

	private List<ModelProcessor> modelProcessors = new LinkedList<>();

	/**
	 * Creates a list of new model processor objects.
	 */
	public Processors createModelProcessors(Cfg cfg) {
		new CatfishProcessor().addModelProcessor(cfg, modelProcessors);
		new LanguageDetectionProcessor().addModelProcessor(cfg, modelProcessors);
		new GeoDataProcessor().addModelProcessor(cfg, modelProcessors);
		new CivetProcessor().addModelProcessor(cfg, modelProcessors);
		return this;
	}

	public Processors addModelProcessor(ModelProcessor modelProcessor) {
		modelProcessors.add(modelProcessor);
		return this;
	}

	public List<ModelProcessor> getModelProcessors() {
		return modelProcessors;
	}

}