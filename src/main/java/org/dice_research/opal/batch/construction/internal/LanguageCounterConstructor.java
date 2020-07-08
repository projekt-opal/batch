package org.dice_research.opal.batch.construction.internal;

import org.apache.jena.vocabulary.DCTerms;
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
		return CfgKeys.RUN_LANGUAGES_COUNTER;
	}

	@Override
	public String getProperty() {
		return DCTerms.language.getURI();
	}

	@Override
	public String getFilename() {
		return Filenames.LANGUAGES;
	}

}