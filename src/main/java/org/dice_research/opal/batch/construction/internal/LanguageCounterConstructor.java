package org.dice_research.opal.batch.construction.internal;

import java.io.File;

import org.apache.jena.vocabulary.DCTerms;
import org.dice_research.opal.batch.configuration.Cfg;
import org.dice_research.opal.batch.configuration.CfgKeys;
import org.dice_research.opal.batch.configuration.Filenames;

/**
 * Counts DCAT themes.
 *
 * @author Adrian Wilke
 */
public class LanguageCounterConstructor extends AbstractCounterConstructor {

	@Override
	public String getCfgKey() {
		return CfgKeys.STATISTICS_LANGUAGES_COUNTER;
	}

	@Override
	public String getProperty() {
		return DCTerms.language.getURI();
	}

	@Override
	public File getFile(Cfg cfg) {
		return Filenames.getFile(cfg, Filenames.LANGUAGES);
	}

}