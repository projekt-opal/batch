package org.dice_research.opal.batch.configuration;

/**
 * Keys used in configuration property files.
 *
 * @author Adrian Wilke
 */
public abstract class CfgKeys {

	public static final String INFO = "info";

	public static final String IO_INPUT = "io.input";
	public static final String IO_INPUT_GRAPH = "io.inputGraph";

	public static final String IO_OUTPUT_DIRECTORY = "io.outputDirectory";
	public static final String IO_OUTPUT_TITLE = "io.outputTitle";
	public static final String IO_OUTPUT_FORMAT = "io.outputFormat";
	public static final String IO_OUTPUT_SIZE = "io.outputSize";
	public static final String IO_OUTPUT_WRITE = "io.outputWrite";

	public static final String IO_ELASTICSEARCH_WRITE = "io.elasticsearch.write";
	public static final String IO_ELASTICSEARCH_HOSTNAME = "io.elasticsearch.hostname";
	public static final String IO_ELASTICSEARCH_PORT = "io.elasticsearch.port";
	public static final String IO_ELASTICSEARCH_SCHEME = "io.elasticsearch.scheme";
	public static final String IO_ELASTICSEARCH_INDEX = "io.elasticsearch.index";

	public static final String RUN_CATFISH = "run.catfish";
	public static final String RUN_LANG = "run.languageDetection";
	public static final String RUN_GEO = "run.geoData";
	public static final String RUN_CIVET = "run.civet";

	public static final String CATFISH_EMPTY_BLANK_NODES = "catfish.cleanEmptyBlankNodes";
	public static final String CATFISH_REMOVE_NON_DE__EN_EMPTY_TITLES = "catfish.removeNonDeEnEmptyTitles";
	public static final String CATFISH_REMOVE_NON_DE_EN = "catfish.removeNonDeEn";
	public static final String CATFISH_LITERALS = "catfish.cleanLiterals";
	public static final String CATFISH_FORMATS = "catfish.cleanFormats";
	public static final String CATFISH_EQUALIZE_DATES = "catfish.equalizeDateFormats";
	public static final String CATFISH_REPLACE_URIS_CATALOG = "catfish.replaceUrisCatalog";
	public static final String CATFISH_REPLACE_URIS_CATALOG_BY_FILENAME = "catfish.replaceUrisCatalogByFilename";

	public static final String CIVET_LONG_RUN = "civet.includeLongRunning";
	public static final String CIVET_LOG = "civet.logIfNotComputed";
	public static final String CIVET_REMOVE_MEASUREMENTS = "civet.removeMeasurements";

	public static final String ADD_LABELS = "add.labels";

	public static final String STATISTICS_DATE_COUNTER = "statistics.dates";
	public static final String STATISTICS_LANGUAGES_COUNTER = "statistics.languages";
	public static final String STATISTICS_THEME_COUNTER = "statistics.themes";
	public static final String STATISTICS_TITLE_LANGUAGES_COUNTER = "statistics.titleLanguages";

	public static final String INTERNAL_WRITTEN_MODELS = "internal.writtenModels";
	public static final String INTERNAL_WRITTEN_TRIPLES = "internal.writtenTriples";
}