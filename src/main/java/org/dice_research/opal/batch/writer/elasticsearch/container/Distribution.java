package org.dice_research.opal.batch.writer.elasticsearch.container;

import java.util.LinkedList;
import java.util.List;

public class Distribution {

	public String uri;
	public List<String> originalUrls = new LinkedList<>();
	public String title;
	public String description;
	public String issued;
	public String modified;
	public String license; // TODO
	public String accessUrl;
	public String downloadUrl;
	public String format;
	public long byteSize;
	public List<String> rights = new LinkedList<>();

	public void appendString(StringBuilder stringBuilder) {
		stringBuilder.append(uri);
		stringBuilder.append(System.lineSeparator());
	}
}