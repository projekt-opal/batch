package org.dice_research.opal.batch.construction;

import java.util.List;

import org.dice_research.opal.batch.configuration.Cfg;
import org.dice_research.opal.common.interfaces.ModelProcessor;

/**
 * Creates a constructor which ignores {@link Cfg}.
 * 
 * Usage: Use {@link #create(ModelProcessor)}.
 *
 * @author Adrian Wilke
 */
public class IndependentConstructor extends AbstractConstructor {

	public static IndependentConstructor create(ModelProcessor modelProcessor) {
		return new IndependentConstructor(modelProcessor);
	}

	private ModelProcessor modelProcessor;

	public IndependentConstructor(ModelProcessor modelProcessor) {
		this.modelProcessor = modelProcessor;
	}

	public ModelProcessor getModelProcessor() {
		return modelProcessor;
	}

	public IndependentConstructor setModelProcessor(ModelProcessor modelProcessor) {
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