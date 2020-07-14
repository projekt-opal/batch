package org.dice_research.opal.batch.writer.elasticsearch.container;

import java.util.LinkedList;
import java.util.List;

/**
 * Based on
 * https://github.com/projekt-opal/converter/blob/f466e2b033bd7bbb8cfa9a34fd5f23eb189145a6/elasticsearch-writer/src/main/java/org/diceresearch/elasticsearchwriter/entity/DataSet.java#L8
 *
 * @author Adrian Wilke
 */
public class Dataset {

	public String uri;
	public List<String> originalUrls = new LinkedList<>();
	public String title;
	public String title_de;
	public String description;
	public String description_de;
	public String landingPage;
	public String language;
	public List<String> keywords = new LinkedList<>();
	public List<String> keywords_de = new LinkedList<>();
	public String issued;
	public String modified;
	public List<String> licenses = new LinkedList<>(); // TODO
	public List<String> themes = new LinkedList<>();
	public List<String> hasQualityMeasurements = new LinkedList<>(); // TODO
	public String publisher; // TODO
	public String creator; // TODO
	public String spatial; // TODO
	public String contactPoint; // TODO
	public List<Distribution> distributions = new LinkedList<>(); // TODO
	public String accrualPeriodicity;
	public String dcatIdentifier;
	public String temporal; // TODO
}
