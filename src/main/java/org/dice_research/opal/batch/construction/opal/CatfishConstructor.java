package org.dice_research.opal.batch.construction.opal;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dice_research.opal.batch.configuration.Cfg;
import org.dice_research.opal.batch.configuration.CfgException;
import org.dice_research.opal.batch.configuration.CfgKeys;
import org.dice_research.opal.batch.construction.AbstractConstructor;
import org.dice_research.opal.catfish.Catfish;
import org.dice_research.opal.catfish.config.CleaningConfig;
import org.dice_research.opal.common.constants.Catalogs;
import org.dice_research.opal.common.interfaces.ModelProcessor;

/**
 * Catfish.
 * 
 * Available catalogs are configured in {@link #getCatalogIds()}.
 * 
 * @see https://github.com/projekt-opal/catfish
 *
 * @author Adrian Wilke
 */
public class CatfishConstructor extends AbstractConstructor {

	private static final Logger LOGGER = LogManager.getLogger();

	public static List<String> getCatalogIds() {
		List<String> catalogIds = new LinkedList<>();
		catalogIds.add(Catalogs.ID_EUROPEANDATAPORTAL);
		catalogIds.add(Catalogs.ID_GOVDATA);
		catalogIds.add(Catalogs.ID_MCLOUD);
		catalogIds.add(Catalogs.ID_MDM);
		return catalogIds;
	}

	public static String getCatalogId(File file) {
		String catalogId = null;
		for (String catalog : getCatalogIds()) {
			if (file.getName().contains(catalog)) {
				if (catalogId == null) {
					catalogId = catalog;
				} else {
					throw new CfgException(
							"The input " + file.getName() + " contains multiple entries of " + getCatalogIds());
				}
			}
		}

		if (catalogId == null) {
			throw new RuntimeException(
					"Please ensure the file name " + file.getName() + " contains one entry of " + getCatalogIds());
		}

		return catalogId;
	}

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

				// Removes literals, which are not empty, german or english
				.setRemoveNonDeEnEmptyTitleLiterals(cfg.getBoolean(CfgKeys.CATFISH_REMOVE_NON_DE__EN_EMPTY_TITLES))

				// Removes datasets, which do not have a german and an english title.
				// Additionally, non-german and non-english titles and descriptions are removed.
				// (optional method call, default: false)
				.setRemoveNonDeEnTitleDatasets(cfg.getBoolean(CfgKeys.CATFISH_REMOVE_NON_DE_EN))

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
		
		// Rewrites URIs of datasets and distributions
		// Catalogs are listed at opal.common.constants.Catalogs
		// (optional method call, default: null)
		if (cfg.has(CfgKeys.CATFISH_REPLACE_URIS_CATALOG)) {
			String catalogId = cfg.get(CfgKeys.CATFISH_REPLACE_URIS_CATALOG);
			if (getCatalogIds().contains(catalogId)) {
				cleaningConfig.setCatalogIdToReplaceUris(catalogId);
			} else {
				LOGGER.error("Unknown catalog: '" + catalogId + "'. Will not rewrite URIs.");
			}
		}

		return new Catfish(cleaningConfig);
	}

}