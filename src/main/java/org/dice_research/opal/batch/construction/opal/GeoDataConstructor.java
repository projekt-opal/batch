package org.dice_research.opal.batch.construction.opal;

import java.util.List;

import org.dice_research.opal.batch.configuration.Cfg;
import org.dice_research.opal.batch.configuration.CfgKeys;
import org.dice_research.opal.batch.construction.AbstractConstructor;
import org.dice_research.opal.common.interfaces.ModelProcessor;
import org.dice_research.opal.metadata.GeoData;

/**
 * Metadata refinement: Geo data.
 * 
 * @see https://github.com/projekt-opal/metadata-refinement
 *
 * @author Adrian Wilke
 */
public class GeoDataConstructor extends AbstractConstructor {

	@Override
	public boolean addModelProcessor(Cfg cfg, List<ModelProcessor> processors) {
		return super.addModelProcessor(cfg, processors, CfgKeys.RUN_GEO);
	}

	@Override
	public ModelProcessor createModelProcessor(Cfg cfg) {
		return new GeoData();
	}

}