package uk.gov.hmcts.ccd.definition.store.elastic;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("elasticsearch")
public class CcdElasticSearchProperties {

    private String host;
    private int port;
    private String indexCasesType;
    private String indexCasesNameFormat;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getIndexCasesType() {
        return indexCasesType;
    }

    public void setIndexCasesType(String indexCasesType) {
        this.indexCasesType = indexCasesType;
    }

    public String getIndexCasesNameFormat() {
        return indexCasesNameFormat;
    }

    public void setIndexCasesNameFormat(String indexCasesNameFormat) {
        this.indexCasesNameFormat = indexCasesNameFormat;
    }
}
