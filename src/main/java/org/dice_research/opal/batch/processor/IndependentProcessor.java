package org.dice_research.opal.batch.processor;

import java.util.List;

import org.dice_research.opal.batch.configuration.Cfg;
import org.dice_research.opal.common.interfaces.ModelProcessor;

/**
 * Creates a processor which ignores {@link Cfg}.
 * 
 * Usage: Use {@link #create(ModelProcessor)}.
 *
 * @author Adrian Wilke
 */
public class IndependentProcessor extends AbstractProcessor {

	public static IndependentProcessor create(ModelProcessor modelProcessor) {
		return new IndependentProcessor(modelProcessor);
	}

	private ModelProcessor modelProcessor;

	public IndependentProcessor(ModelProcessor modelProcessor) {
		this.modelProcessor = modelProcessor;
	}

	public ModelProcessor getModelProcessor() {
		return modelProcessor;
	}

	public IndependentProcessor setModelProcessor(ModelProcessor modelProcessor) {
		this.modelProcessor = modelProcessor;
		return this;
	}

	@Override
	public boolean addModelProcessor(Cfg cfg, List<ModelProcessor> processors) {
		processors.add(modelProcessor);
		return true;
	}

	@Override
	public ModelProcessor createModelProcessor(Cfg cfg) {
		return modelProcessor;
	}

}