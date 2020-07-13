package org.dice_research.opal.batch.utils;

import org.dice_research.opal.batch.configuration.Cfg;
import org.dice_research.opal.batch.configuration.CfgKeys;

/**
 * Utilities to set configuration values.
 *
 * @author Adrian Wilke
 */
public class CfgUtils {

	public static void disableAddingLabelsFile(Cfg cfg) {
		cfg.set(CfgKeys.ADD_LABELS, Boolean.FALSE.toString());
	}

	public static void disableHashDetection(Cfg cfg) {
		cfg.set(CfgKeys.CATFISH_REPLACE_URIS_CATALOG_BY_FILENAME, Boolean.FALSE.toString());
		cfg.set(CfgKeys.CATFISH_REPLACE_URIS_CATALOG, "");
	}

	public static void disableOpal(Cfg cfg) {
		cfg.set(CfgKeys.RUN_CATFISH, Boolean.FALSE.toString());
		cfg.set(CfgKeys.RUN_LANG, Boolean.FALSE.toString());
		cfg.set(CfgKeys.RUN_GEO, Boolean.FALSE.toString());
		cfg.set(CfgKeys.RUN_CIVET, Boolean.FALSE.toString());
	}

	public static void disableOutputWriting(Cfg cfg) {
		cfg.set(CfgKeys.IO_OUTPUT_WRITE, Boolean.FALSE.toString());
	}

	private Cfg cfg;

	public CfgUtils() {
		this.cfg = new Cfg();
	}

	public CfgUtils(Cfg cfg) {
		this.cfg = cfg;
	}

	public CfgUtils disableAddingLabelsFile() {
		disableAddingLabelsFile(cfg);
		return this;
	}

	public CfgUtils disableDatasetUriRewriting() {
		disableHashDetection(cfg);
		return this;
	}

	public CfgUtils disableOpal() {
		disableOpal(cfg);
		return this;
	}

	public CfgUtils disableOutputWriting() {
		disableOutputWriting(cfg);
		return this;
	}

	public Cfg getCfg() {
		return cfg;
	}
}