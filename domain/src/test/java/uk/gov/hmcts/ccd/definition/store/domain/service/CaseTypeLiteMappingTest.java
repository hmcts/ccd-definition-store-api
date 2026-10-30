package uk.gov.hmcts.ccd.definition.store.domain.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeLiteEntity;
import uk.gov.hmcts.ccd.definition.store.repository.model.CaseTypeLite;

import static org.assertj.core.api.Assertions.assertThat;

class CaseTypeLiteMappingTest {

    @ParameterizedTest
    @NullSource
    @ValueSource(ints = {1, 42, 99})
    void shouldMapAndSerializeOptionalCaseTypesOrder(Integer order) throws Exception {
        CaseTypeLiteEntity entity = new CaseTypeLiteEntity();
        entity.setReference("TestCase");
        entity.setCaseTypesOrder(order);

        CaseTypeLite response = EntityToResponseDTOMapper.INSTANCE.map(entity);

        assertThat(response.getId()).isEqualTo("TestCase");
        assertThat(response.getCaseTypesOrder()).isEqualTo(order);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode json = mapper.readTree(mapper.writeValueAsString(response));
        assertThat(json.has("case_types_order")).isFalse();
        if (order == null) {
            assertThat(json.has("caseTypesOrder")).isFalse();
        } else {
            assertThat(json.get("caseTypesOrder").isIntegralNumber()).isTrue();
            assertThat(json.get("caseTypesOrder").intValue()).isEqualTo(order);
        }
    }
}
