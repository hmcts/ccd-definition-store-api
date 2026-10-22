package uk.gov.hmcts.ccd.definition.store.domain;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "decentralised-events")
public class DecentralisedEventsConfiguration {

    private Map<String, List<String>> caseTypes = new HashMap<>();

    public Map<String, List<String>> getCaseTypes() {
        return caseTypes;
    }

    public void setCaseTypes(Map<String, List<String>> caseTypes) {
        this.caseTypes = caseTypes;
    }
}
