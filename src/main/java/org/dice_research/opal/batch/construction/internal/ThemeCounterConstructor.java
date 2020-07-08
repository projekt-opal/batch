package org.dice_research.opal.batch.construction.internal;

import org.apache.jena.vocabulary.DCAT;
import org.dice_research.opal.batch.configuration.CfgKeys;
import org.dice_research.opal.batch.configuration.Filenames;

/**
 * Counts DCAT themes.
 *
 * @author Adrian Wilke
 */
public class ThemeCounterConstructor extends AbstractCounterConstructor {

	@Override
	public String getCfgKey() {
		return CfgKeys.RUN_THEME_COUNTER;
	}

	@Override
	public String getProperty() {
		return DCAT.theme.getURI();
	}

	@Override
	public String getFilename() {
		return Filenames.THEMES;
	}

}