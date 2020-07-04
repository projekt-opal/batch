package org.dice_research.opal.batch.processor.opal;

import java.util.List;

import org.dice_research.opal.batch.configuration.Cfg;
import org.dice_research.opal.batch.configuration.CfgKeys;
import org.dice_research.opal.batch.processor.AbstractProcessor;
import org.dice_research.opal.civet.Civet;
import org.dice_research.opal.common.interfaces.ModelProcessor;

/**
 * Civet.
 * 
 * @see https://github.com/projekt-opal/civet
 *
 * @author Adrian Wilke
 */
public class CivetProcessor extends AbstractProcessor {

	public boolean addModelProcessor(Cfg cfg, List<ModelProcessor> processors) {
		return super.addModelProcessor(cfg, processors, CfgKeys.RUN_CIVET);
	}

	public Civet createModelProcessor(Cfg cfg) {
		return new Civet()

				// If long running metrics should be included.
				// (optional method call, default: false)
				.setIncludeLongRunning(cfg.getBoolean(CfgKeys.CIVET_LONG_RUN))

				// If it should be logged, if a measurement could not be computed
				// (optional method call, default: true)
				.setLogIfNotComputed(cfg.getBoolean(CfgKeys.CIVET_LOG))

				// If existing measurements should be removed
				// (optional method call, default: true)
				.setRemoveMeasurements(cfg.getBoolean(CfgKeys.CIVET_REMOVE_MEASUREMENTS));
	}
}