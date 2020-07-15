package org.dice_research.opal.batch.writer.elasticsearch;

import org.apache.jena.rdf.model.Model;
import org.dice_research.opal.batch.writer.RdfWriter;

/**
 * This is a test on integrating the ES writer.
 * 
 * TODO
 * https://www.elastic.co/guide/en/elasticsearch/client/java-rest/current/java-rest-high.html
 *
 * @author Adrian Wilke
 */
public class ElasticsearchWriter implements RdfWriter {

//	private RestHighLevelClient restHighLevelClient;
private JsonExtractor jsonExtractor = new JsonExtractor();

	@Override
	public void processModel(Model model, String datasetUri) throws Exception {
		System.out.println("ElasticsearchWriter");
		jsonExtractor.processModel(model, datasetUri);

		model.write(System.out, "TTL");
		System.out.println(jsonExtractor.getJsonObject().toString(2));
	}

	@Override
	public RdfWriter finish() {
		return this;
	}

}