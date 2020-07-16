package org.dice_research.opal.batch.writer;

import java.io.IOException;

import org.apache.http.HttpHost;
import org.apache.jena.rdf.model.Model;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;

public class ElasticsearchWriter implements Writer {

	private static final Logger LOGGER = LogManager.getLogger();

	private RestHighLevelClient restHighLevelClient;

	public String hostname;
	public int port = -1;
	public String scheme;
	public String index;

	@Override
	public void processModel(Model model, String datasetUri) throws Exception {

		if (restHighLevelClient == null) {
			if (hostname == null) {
				throw new RuntimeException("No hostname specified");
			} else if (port == -1) {
				throw new RuntimeException("No hostname specified");
			} else if (scheme == null) {
				throw new RuntimeException("No scheme specified");
			} else if (index == null) {
				throw new RuntimeException("No index specified");
			}
			this.restHighLevelClient = new RestHighLevelClient(
					RestClient.builder(new HttpHost(hostname, port, scheme)));
		}

		JsonExtractor jsonExtractor = new JsonExtractor();
		jsonExtractor.processModel(model, datasetUri);

		IndexRequest indexRequest = new IndexRequest(index).source(jsonExtractor.getJson(), XContentType.JSON);
		IndexResponse indexResponse = restHighLevelClient.index(indexRequest, RequestOptions.DEFAULT);

		LOGGER.debug(indexResponse);
	}

	@Override
	public Writer finish() {
		try {
			this.restHighLevelClient.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return this;
	}

}