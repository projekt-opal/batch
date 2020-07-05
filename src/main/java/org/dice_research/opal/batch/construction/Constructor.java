package org.dice_research.opal.batch.construction;

import java.util.List;

import org.dice_research.opal.batch.configuration.Cfg;
import org.dice_research.opal.common.interfaces.ModelProcessor;

/**
 * Constructors create and configure {@link ModelProcessor}s.
 *
 * @author Adrian Wilke
 */
public interface Constructor {

	/**
	 * Adds a model-processor based on the given configuration.
	 * 
	 * @return if the processor was added.
	 */
	public boolean addModelProcessor(Cfg cfg, List<ModelProcessor> processors);

	/**
	 * Creates a model-processor based on the given configuration.
	 */
	public ModelProcessor createModelProcessor(Cfg cfg);

	/**
	 * For finalizing, e.g. writing final data.
	 */
	public Constructor finish(Cfg cfg);
}
