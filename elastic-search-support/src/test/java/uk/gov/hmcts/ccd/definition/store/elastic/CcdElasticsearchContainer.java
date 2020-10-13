package uk.gov.hmcts.ccd.definition.store.elastic;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.elasticsearch.ElasticsearchContainer;

public class CcdElasticsearchContainer extends ElasticsearchContainer {

    private static final String VERSION = "6.4.2";
    private static CcdElasticsearchContainer container;

    private CcdElasticsearchContainer() {
        super("docker.elastic.co/elasticsearch/elasticsearch:" + VERSION);
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
