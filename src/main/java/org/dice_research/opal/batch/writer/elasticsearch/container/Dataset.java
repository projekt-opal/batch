package org.dice_research.opal.batch.writer.elasticsearch.container;

import java.util.List;

/**
 * Based on
 * https://github.com/projekt-opal/converter/blob/f466e2b033bd7bbb8cfa9a34fd5f23eb189145a6/elasticsearch-writer/src/main/java/org/diceresearch/elasticsearchwriter/entity/DataSet.java#L8
 *
 * @author Adrian Wilke
 */
public class Dataset {

	public String uri;
	public List<String> originalUrls;
	public String title;
	public String title_de;
	public String description;
	public String description_de;
	public String landingPage;
	public String language;
	public List<String> keywords;
	public List<String> keywords_de;
	public String issued;
	public String modified;
	public List<String> licenses; // TODO
	public List<String> themes;
	public List<String> hasQualityMeasurements; // TODO
	public String publisher; // TODO
	public String creator; // TODO
	public String spatial; // TODO
	public String contactPoint; // TODO
	public List<Distribution> distributions; // TODO
	public String accrualPeriodicity;
	public String dcatIdentifier;
	public String temporal; // TODO

	@Override
	public String toString() {
		return appendString(new StringBuilder()).toString();
	}

	public StringBuilder appendString(StringBuilder stringBuilder) {
		for (Distribution distribution : distributions) {
			distribution.appendString(stringBuilder);
			stringBuilder.append(System.lineSeparator());
		}
		return stringBuilder;
	}
}
