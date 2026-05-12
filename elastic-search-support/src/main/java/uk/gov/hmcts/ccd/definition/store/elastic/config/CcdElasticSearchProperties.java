package uk.gov.hmcts.ccd.definition.store.elastic.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;
import java.util.Map;

@ConfigurationProperties("elasticsearch")
@Getter
@Setter
public class CcdElasticSearchProperties {

    private String host;
    private int port;
    private String scheme;
    private int indexShards;
    private int indexShardsReplicas;
    private String dynamic;
    private String casesIndexType;
    private String casesIndexNameFormat;
    private int casesIndexMappingFieldsLimit;
    private Map<String, String> elasticMappings;
    private Map<String, String> typeMappings;
    private Map<String, String> casePredefinedMappings;
    private List<String> ccdIgnoredTypes;
    private String securityClassificationMapping;
    private boolean enabled;
    private int batchSize;
}
