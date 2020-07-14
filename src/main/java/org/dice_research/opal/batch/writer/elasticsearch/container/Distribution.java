package org.dice_research.opal.batch.writer.elasticsearch.container;

import java.util.LinkedList;
import java.util.List;

public class Distribution {

	private String uri;
	private List<String> originalUrls = new LinkedList<>();
	private String title;
	private String description;
	private String issued;
	private String modified;
	private String license; // TODO
	private String accessUrl;
	private String downloadUrl;
	private String format;
	private long byteSize;
	private List<String> rights = new LinkedList<>();
}
