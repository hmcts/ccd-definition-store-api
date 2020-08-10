package uk.gov.hmcts.ccd.definition.store.elastic;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.HttpWaitStrategy;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.utility.Base58;

import java.time.Duration;

import static java.net.HttpURLConnection.HTTP_OK;
import static java.net.HttpURLConnection.HTTP_UNAUTHORIZED;

public class CcdElasticsearchContainer extends ElasticsearchContainer {

    private static CcdElasticsearchContainer container;

    private CcdElasticsearchContainer() {
        super("docker.elastic.co/elasticsearch/elasticsearch:6.4.2");
    }

    public static GenericContainer<ElasticsearchContainer> getInstance() {
        if (container == null) {
            container = new CcdElasticsearchContainer();
            container.start();
        }
        return container;
    }

    @Override
    public void start() {
        super.start();
    }
}
