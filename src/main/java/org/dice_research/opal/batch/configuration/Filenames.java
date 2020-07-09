package org.dice_research.opal.batch.configuration;

import java.io.File;

public abstract class Filenames {

	public static final String INFO = "info.txt";

	public static final String LANGUAGES = "languages.csv";
	public static final String THEMES = "themes.csv";

	public static final String DATESTRINGFORMATS = "date-stringformats.csv";
	public static final String DATEFORMATS = "date-formats.csv";
	public static final String DATETYPES = "date-types.csv";

	public static final String LABELS_THEMES = "labels-themes.ttl";

	private static final String SEPARATOR = "-";

	/**
	 * Gets a file in the output directory.
	 * 
	 * If the configuration contains a title, it is used as a prefix.
	 */
	public static File getFile(Cfg cfg, String filename) {
		if (cfg.has(CfgKeys.IO_OUTPUT_TITLE)) {
			return new File(cfg.get(CfgKeys.IO_OUTPUT_DIRECTORY),
					cfg.get(CfgKeys.IO_OUTPUT_TITLE) + SEPARATOR + filename);
		} else {
			return new File(cfg.get(CfgKeys.IO_OUTPUT_DIRECTORY), filename);
		}
	}
}