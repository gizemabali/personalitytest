package personalitytest;

import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.RestClients;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.stereotype.Component;

import personalitytest.config.ApiConfig;

/**
 * This class is used to configure and set up elastic client.
 * 
 * @author gizemabali
 *
 */
@Component(Config.CONFIG)
@Configuration
@EnableElasticsearchRepositories(basePackages = Config.PERSONALITYTEST)
public class Config {

	private static final String ELASTIC_HOST = ApiConfig.getElasticHostName() + ":" + ApiConfig.getElasticPort();
	static final String CONFIG = "config";
	static final String PERSONALITYTEST = "personalitytest";

	/**
	 * This bean is used to create a elastic rest client {@link org.elasticsearch.client.RestHighLevelClient}
	 * 
	 * @return a rest client
	 */
	@Bean
	public RestHighLevelClient client() {
		ClientConfiguration clientConfiguration = ClientConfiguration.builder().connectedTo(ELASTIC_HOST).build();
		return RestClients.create(clientConfiguration).rest();
	}

	@Bean
	public ElasticsearchOperations elasticsearchTemplate() {
		ElasticsearchRestTemplate elasticsearchRestTemplate = new ElasticsearchRestTemplate(client());
		return elasticsearchRestTemplate;
	}

}
