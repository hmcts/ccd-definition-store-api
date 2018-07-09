package uk.gov.hmcts.ccd.definition.store.elastic.config;

import java.util.List;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("elasticsearch")
public class CcdElasticSearchProperties {

    private String host;
    private int port;
    private String scheme;
    private int indexShards;
    private int indexShardsReplicas;
    private String casesIndexType;
    private String casesIndexNameFormat;
    private Map<String, String> elasticMappings;
    private Map<String, String> typeMappings;
    private Map<String, String> casePredefinedMappings;
    private List<String> ccdIgnoredTypes;
    private String securityClassificationMapping;

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

    public String getCasesIndexType() {
        return casesIndexType;
    }

    public void setCasesIndexType(String casesIndexType) {
        this.casesIndexType = casesIndexType;
    }

    public String getCasesIndexNameFormat() {
        return casesIndexNameFormat;
    }

    public void setCasesIndexNameFormat(String casesIndexNameFormat) {
        this.casesIndexNameFormat = casesIndexNameFormat;
    }

    public Map<String, String> getTypeMappings() {
        return typeMappings;
    }

    public void setTypeMappings(Map<String, String> typeMappings) {
        this.typeMappings = typeMappings;
    }

    public Map<String, String> getCasePredefinedMappings() {
        return casePredefinedMappings;
    }

    public void setCasePredefinedMappings(Map<String, String> casePredefinedMappings) {
        this.casePredefinedMappings = casePredefinedMappings;
    }

    public Map<String, String> getElasticMappings() {
        return elasticMappings;
    }

    public void setElasticMappings(Map<String, String> elasticMappings) {
        this.elasticMappings = elasticMappings;
    }

    public List<String> getCcdIgnoredTypes() {
        return ccdIgnoredTypes;
    }

    public void setCcdIgnoredTypes(List<String> ccdIgnoredTypes) {
        this.ccdIgnoredTypes = ccdIgnoredTypes;
    }

    public int getIndexShards() {
        return indexShards;
    }

    public void setIndexShards(int indexShards) {
        this.indexShards = indexShards;
    }

    public int getIndexShardsReplicas() {
        return indexShardsReplicas;
    }

    public void setIndexShardsReplicas(int indexShardsReplicas) {
        this.indexShardsReplicas = indexShardsReplicas;
    }

    public String getScheme() {
        return scheme;
    }

    public void setScheme(String scheme) {
        this.scheme = scheme;
    }

    public String getSecurityClassificationMapping() {
        return securityClassificationMapping;
    }

    public void setSecurityClassificationMapping(String securityClassificationMapping) {
        this.securityClassificationMapping = securityClassificationMapping;
    }
}
