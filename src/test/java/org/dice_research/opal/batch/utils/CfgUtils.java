package org.dice_research.opal.batch.utils;

import org.dice_research.opal.batch.configuration.Cfg;
import org.dice_research.opal.batch.configuration.CfgKeys;

/**
 * Utilities to set configuration values.
 *
 * @author Adrian Wilke
 */
public class CfgUtils {

	public static void disableAll(Cfg cfg) {
		disableWriting(cfg);
		disableAddingLabelsFile(cfg);
		disableOpalComponents(cfg);
		disableCatfishOptions(cfg);
		disableCivetOptions(cfg);
	}

	public static void disableWriting(Cfg cfg) {
		cfg.set(CfgKeys.IO_OUTPUT_WRITE, Boolean.FALSE.toString());
		cfg.set(CfgKeys.IO_ELASTICSEARCH_WRITE, Boolean.FALSE.toString());
	}

	public static void disableAddingLabelsFile(Cfg cfg) {
		cfg.set(CfgKeys.ADD_LABELS, Boolean.FALSE.toString());
	}

	public static void disableOpalComponents(Cfg cfg) {
		cfg.set(CfgKeys.RUN_CATFISH, Boolean.FALSE.toString());
		cfg.set(CfgKeys.RUN_LANG, Boolean.FALSE.toString());
		cfg.set(CfgKeys.RUN_GEO, Boolean.FALSE.toString());
		cfg.set(CfgKeys.RUN_CIVET, Boolean.FALSE.toString());
	}

	public static void disableCatfishOptions(Cfg cfg) {
		cfg.set(CfgKeys.CATFISH_EMPTY_BLANK_NODES, Boolean.FALSE.toString());
		cfg.set(CfgKeys.CATFISH_EQUALIZE_DATES, Boolean.FALSE.toString());
		cfg.set(CfgKeys.CATFISH_FORMATS, Boolean.FALSE.toString());
		cfg.set(CfgKeys.CATFISH_LITERALS, Boolean.FALSE.toString());
		cfg.set(CfgKeys.CATFISH_REMOVE_NON_DE__EN_EMPTY_TITLES, Boolean.FALSE.toString());
		cfg.set(CfgKeys.CATFISH_REMOVE_NON_DE_EN, Boolean.FALSE.toString());
		cfg.set(CfgKeys.CATFISH_REPLACE_URIS_CATALOG, Boolean.FALSE.toString());
		cfg.set(CfgKeys.CATFISH_REPLACE_URIS_CATALOG_BY_FILENAME, Boolean.FALSE.toString());
	}

	public static void disableCivetOptions(Cfg cfg) {
		cfg.set(CfgKeys.CIVET_LOG, Boolean.FALSE.toString());
		cfg.set(CfgKeys.CIVET_LONG_RUN, Boolean.FALSE.toString());
		cfg.set(CfgKeys.CIVET_REMOVE_MEASUREMENTS, Boolean.FALSE.toString());
	}

	private Cfg cfg;

	public CfgUtils() {
		this.cfg = new Cfg();
	}

	public CfgUtils(Cfg cfg) {
		this.cfg = cfg;
	}

	public CfgUtils disableAll() {
		disableAll(cfg);
		return this;
	}

	public CfgUtils disableWriting() {
		disableWriting(cfg);
		return this;
	}

	public CfgUtils disableAddingLabelsFile() {
		disableAddingLabelsFile(cfg);
		return this;
	}

	public CfgUtils disableUriRewriting() {
		cfg.set(CfgKeys.CATFISH_REPLACE_URIS_CATALOG_BY_FILENAME, Boolean.FALSE.toString());
		cfg.set(CfgKeys.CATFISH_REPLACE_URIS_CATALOG, "");
		return this;
	}

	public CfgUtils disableOpalComponents() {
		disableOpalComponents(cfg);
		return this;
	}

	public Cfg getCfg() {
		return cfg;
	}
}