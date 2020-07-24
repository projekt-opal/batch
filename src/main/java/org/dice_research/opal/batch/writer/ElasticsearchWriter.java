package org.dice_research.opal.batch.writer;

import java.io.IOException;

import org.apache.http.HttpHost;
import org.apache.jena.rdf.model.Model;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;

/**
 * Elasticsearch writer.
 * 
 * @see https://www.elastic.co/guide/en/elasticsearch/client/java-rest/7.3/java-rest-high.html
 *
 * @author Adrian Wilke
 */
public class ElasticsearchWriter implements Writer {

	private static final Logger LOGGER = LogManager.getLogger();

	public String hostname;
	public int port = -1;
	public String scheme;
	public String index;
	public int maxModels = -1;

	private BulkRequest bulkRequest;
	private int modelCounter = 0;

	@Override
	public void processModel(Model model, String datasetUri) throws Exception {

		if (hostname == null) {
			throw new RuntimeException("No hostname specified");
		} else if (port == -1) {
			throw new RuntimeException("No port specified");
		} else if (scheme == null) {
			throw new RuntimeException("No scheme specified");
		} else if (index == null) {
			throw new RuntimeException("No index specified");
		} else if (maxModels == -1) {
			throw new RuntimeException("No maxModels specified");
		}

		// Add themes of distributions to dataset. Remove unknown themes.
		new ElasticsearchThemes().cleanThemes(model, model.getResource(datasetUri));

		if (bulkRequest == null) {
			bulkRequest = new BulkRequest();
		}

		ElasticsearchJson json = new ElasticsearchJson();
		json.processModel(model, datasetUri);
		bulkRequest.add(new IndexRequest(index).source(json.getJson(), XContentType.JSON));

		modelCounter++;
		if (modelCounter == maxModels) {
			finish();
		}
	}

	@Override
	public Writer finish() {

		if (bulkRequest != null) {
			try (RestHighLevelClient restHighLevelClient = new RestHighLevelClient(
					RestClient.builder(new HttpHost(hostname, port, scheme)))) {
				BulkResponse bulkResponse = restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
				LOGGER.info(bulkResponse.getItems().length + " items, " + bulkResponse.getTook().toString());
			} catch (IOException e) {
				LOGGER.error(e);
			}
		}

		bulkRequest = null;
		modelCounter = 0;

		return this;
	}

}