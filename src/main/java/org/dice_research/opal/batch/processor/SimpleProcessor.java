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
public class SimpleProcessor extends AbstractProcessor {

	public static SimpleProcessor create(ModelProcessor modelProcessor) {
		return new SimpleProcessor(modelProcessor);
	}

	private ModelProcessor modelProcessor;

	public SimpleProcessor(ModelProcessor modelProcessor) {
		this.modelProcessor = modelProcessor;
	}

	public ModelProcessor getModelProcessor() {
		return modelProcessor;
	}

	public SimpleProcessor setModelProcessor(ModelProcessor modelProcessor) {
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