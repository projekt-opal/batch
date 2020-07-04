package org.dice_research.opal.batch.processor.opal;

import java.util.List;

import org.dice_research.opal.batch.configuration.Cfg;
import org.dice_research.opal.batch.configuration.CfgKeys;
import org.dice_research.opal.batch.processor.AbstractProcessor;
import org.dice_research.opal.catfish.Catfish;
import org.dice_research.opal.catfish.config.CleaningConfig;
import org.dice_research.opal.common.interfaces.ModelProcessor;

/**
 * Catfish.
 * 
 * @see https://github.com/projekt-opal/catfish
 *
 * @author Adrian Wilke
 */
public class CatfishProcessor extends AbstractProcessor {

	@Override
	public boolean addModelProcessor(Cfg cfg, List<ModelProcessor> processors) {
		return super.addModelProcessor(cfg, processors, CfgKeys.RUN_CATFISH);
	}

	@Override
	public ModelProcessor createModelProcessor(Cfg cfg) {
		CleaningConfig cleaningConfig = new CleaningConfig()

				// Remove blank nodes, which are not subject of triples
				// (optional method call, default: true)
				.setCleanEmptyBlankNodes(cfg.getBoolean(CfgKeys.CATFISH_EMPTY_BLANK_NODES))

				// Remove triples with literals as object, which contain no value or unreadable.
				// And also extract Language Tag and DataType if it is mistakenly inside the
				// string
				// (optional method call, default: true)
				.setCleanLiterals(cfg.getBoolean(CfgKeys.CATFISH_LITERALS))

				// Check dct:format and dcat:mediaType for values and create new triples.
				// (optional method call, default: true)
				.setCleanFormats(cfg.getBoolean(CfgKeys.CATFISH_FORMATS))

				// Rewrites date formats
				// (optional method call, default: false)
				.setEqualizeDateFormats(cfg.getBoolean(CfgKeys.CATFISH_EQUALIZE_DATES));

		return new Catfish(cleaningConfig);
	}

}