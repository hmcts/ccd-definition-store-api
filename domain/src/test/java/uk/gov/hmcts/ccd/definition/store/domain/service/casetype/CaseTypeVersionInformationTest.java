package uk.gov.hmcts.ccd.definition.store.domain.service.casetype;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.text.MatchesPattern.matchesPattern;
import static org.hamcrest.MatcherAssert.assertThat;

class CaseTypeVersionInformationTest {

    @Test
    void testJson() throws JsonProcessingException {
        final CaseTypeVersionInformation version = new CaseTypeVersionInformation(678);
        final ObjectMapper objectMapper = new ObjectMapper();
        assertThat(objectMapper.writeValueAsString(version), is("{\"version\":678}"));
    }

    @Test
    void testGetterAndToString() {
        final CaseTypeVersionInformation version = new CaseTypeVersionInformation(679);
        assertThat(version.getVersion(), is(679));
        assertThat(version.toString(),
            matchesPattern("^uk.gov.hmcts.ccd.definition.store.domain.service.casetype"
                + ".CaseTypeVersionInformation@\\S+\\[version=679\\]$"));
    }

}
