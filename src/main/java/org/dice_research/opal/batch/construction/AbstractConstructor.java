package org.dice_research.opal.batch.construction;

import java.util.List;

import org.dice_research.opal.batch.configuration.Cfg;
import org.dice_research.opal.common.interfaces.ModelProcessor;

/**
 * Abstract constructor.
 *
 * @author Adrian Wilke
 */
public abstract class AbstractConstructor implements Constructor {

	/**
	 * Adds a new model processor instance, if is set in the given configuration.
	 * 
	 * @return if the processor was added.
	 */
	public boolean addModelProcessor(Cfg cfg, List<ModelProcessor> processors, String key) {
		if (cfg.getBoolean(key)) {
			processors.add(createModelProcessor(cfg));
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Adds a new model processor instance, if is set in the given configuration.
	 * 
	 * @return if the processor was added.
	 */
	public abstract boolean addModelProcessor(Cfg cfg, List<ModelProcessor> processors);

	/**
	 * Creates a model processor instance based on the given configuration.
	 */
	public abstract ModelProcessor createModelProcessor(Cfg cfg);

	/**
	 * Default implementation: Only return instance.
	 */
	@Override
	public Constructor finish(Cfg cfg) {
		return this;
	}
}