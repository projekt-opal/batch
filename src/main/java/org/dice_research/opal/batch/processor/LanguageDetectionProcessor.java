package org.dice_research.opal.batch.processor;

import java.util.List;

import org.dice_research.opal.batch.configuration.Cfg;
import org.dice_research.opal.batch.configuration.CfgKeys;
import org.dice_research.opal.common.interfaces.ModelProcessor;
import org.dice_research.opal.metadata.LanguageDetection;

/**
 * Metadata refinement: Language detection.
 * 
 * @see https://github.com/projekt-opal/metadata-refinement
 *
 * @author Adrian Wilke
 */
public class LanguageDetectionProcessor extends AbstractProcessor {

	@Override
	public boolean addModelProcessor(Cfg cfg, List<ModelProcessor> processors) {
		return super.addModelProcessor(cfg, processors, CfgKeys.RUN_LANG);
	}

	@Override
	public ModelProcessor createModelProcessor(Cfg cfg) {
		LanguageDetection languageDetection = new LanguageDetection();
		try {
			languageDetection.initialize();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return languageDetection;
	}

}