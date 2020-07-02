package org.dice_research.opal.batch.configuration;

/**
 * Keys used in configuration property files.
 *
 * @author Adrian Wilke
 */
public abstract class CfgKeys {

	public static final String IO_INPUT = "io.input";
	public static final String IO_OUTPUT = "io.output";
	public static final String IO_INPUT_GRAPH = "io.inputGraph";

	public static final String RUN_CATFISH = "catfish";
	public static final String RUN_LANG = "metadata.LanguageDetection";
	public static final String RUN_GEO = "metadata.GeoData";
	public static final String RUN_CIVET = "civet";

	public static final String CATFISH_EMPTY_BLANK_NODES = "catfish.cleanEmptyBlankNodes";
	public static final String CATFISH_LITERALS = "catfish.cleanLiterals";
	public static final String CATFISH_FORMATS = "catfish.cleanFormats";
	public static final String CATFISH_EQUALIZE_DATES = "catfish.equalizeDateFormats";

	public static final String CIVET_LONG_RUN = "civet.includeLongRunning";
	public static final String CIVET_LOG = "civet.logIfNotComputed";
	public static final String CIVET_REMOVE_MEASUREMENTS = "civet.removeMeasurements";

}