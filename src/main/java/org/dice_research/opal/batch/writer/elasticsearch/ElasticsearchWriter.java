package org.dice_research.opal.batch.writer.elasticsearch;

import org.apache.jena.rdf.model.Model;
import org.dice_research.opal.batch.writer.RdfWriter;
import org.elasticsearch.client.RestHighLevelClient;

/**
 * This is a test on integrating the ES writer.
 * 
 * Mappings
 * https://github.com/projekt-opal/opaldata/blob/master/elasticsearch-initialization/mappings.json
 * 
 * https://github.com/projekt-opal/converter/blob/master/elasticsearch-writer/src/main/java/org/diceresearch/elasticsearchwriter/utility/impl/ElasticSearchWriterImpl.java
 * https://github.com/projekt-opal/converter/blob/master/elasticsearch-writer/src/main/java/org/diceresearch/elasticsearchwriter/utility/ModelMapper.java
 * https://github.com/projekt-opal/converter/tree/master/elasticsearch-writer/src/main/java/org/diceresearch/elasticsearchwriter/entity
 *
 * https://www.elastic.co/guide/en/elasticsearch/client/java-rest/current/java-rest-high.html
 *
 * https://github.com/google/gson/blob/master/UserGuide.md#TOC-Object-Examples
 * 
 * @author Adrian Wilke
 */
public class ElasticsearchWriter implements RdfWriter {

	private RestHighLevelClient restHighLevelClient;

	@Override
	public RdfWriter write(Model model) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RdfWriter finish() {
		// TODO Auto-generated method stub
		return null;
	}

}
