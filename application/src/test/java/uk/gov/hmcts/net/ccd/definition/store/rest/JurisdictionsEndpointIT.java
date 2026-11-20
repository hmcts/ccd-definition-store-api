package uk.gov.hmcts.net.ccd.definition.store.rest;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.ccd.definition.store.repository.CaseTypeRepository;
import uk.gov.hmcts.ccd.definition.store.repository.JurisdictionRepository;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.JurisdictionEntity;
import uk.gov.hmcts.net.ccd.definition.store.BaseTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.ccd.definition.store.repository.SecurityClassification.PUBLIC;

@Transactional
class JurisdictionsEndpointIT extends BaseTest {

    private static final String PRIMARY = "ORDER_PRIMARY";
    private static final String SECONDARY = "ORDER_SECONDARY";

    @Autowired
    private JurisdictionRepository jurisdictionRepository;

    @Autowired
    private CaseTypeRepository caseTypeRepository;

    @Autowired
    private EntityManager entityManager;

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void shouldReturnLatestVersionsWithoutSpecifyingOrderWhenFilterIsAbsent(boolean filtered) throws Exception {
        JurisdictionEntity primary = jurisdiction(PRIMARY);
        caseType(primary, "Late", 1, 1);
        caseType(primary, "Late", 2, 99);
        caseType(primary, "Middle", 1, 42);
        caseType(primary, "Zulu", 1, 1);
        JurisdictionEntity secondary = jurisdiction(SECONDARY);
        caseType(secondary, "SecondaryLater", 1, 30);
        caseType(secondary, "SecondaryEarlier", 1, 2);

        JsonNode response = requestJurisdictions(filtered);

        assertThat(response).hasSize(2);
        JsonNode primaryTypes = findJurisdiction(response, PRIMARY).get("case_types");
        assertThat(primaryTypes).extracting(item ->
            tuple(item.get("id").asText(), item.get("caseTypesOrder").intValue()))
            .containsExactlyInAnyOrder(tuple("Late", 99), tuple("Middle", 42), tuple("Zulu", 1));
        assertThat(primaryTypes).allSatisfy(item -> {
            assertThat(item.get("caseTypesOrder").isIntegralNumber()).isTrue();
            assertThat(item.has("case_types_order")).isFalse();
            assertThat(item.get("name").asText()).isEqualTo(item.get("id").asText() + " name");
            assertThat(item.get("description").asText()).isEqualTo("Case description");
        });
        assertThat(findJurisdiction(response, SECONDARY).get("case_types"))
            .extracting(item -> item.get("id").asText())
            .containsExactlyInAnyOrder("SecondaryEarlier", "SecondaryLater");
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void shouldIncludeNumberedAndUnnumberedCaseTypesWithoutFilter(boolean filtered) throws Exception {
        JurisdictionEntity primary = jurisdiction(PRIMARY);
        caseType(primary, "AlphaWithoutOrder", 1, null);
        caseType(primary, "ZuluNumbered", 1, 42);
        jurisdiction(SECONDARY);

        JsonNode response = requestJurisdictions(filtered);
        JsonNode types = findJurisdiction(response, PRIMARY).get("case_types");

        assertThat(types).extracting(item -> item.get("id").asText())
            .containsExactlyInAnyOrder("AlphaWithoutOrder", "ZuluNumbered");
        assertThat(types).allSatisfy(item -> {
            if ("AlphaWithoutOrder".equals(item.get("id").asText())) {
                assertThat(item.has("caseTypesOrder")).isFalse();
            } else {
                assertThat(item.get("caseTypesOrder").intValue()).isEqualTo(42);
            }
        });
        assertThat(findJurisdiction(response, SECONDARY).get("case_types")).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void shouldKeepLegacyCaseTypesAndFieldsWithoutRequiringOrder(boolean filtered) throws Exception {
        JurisdictionEntity primary = jurisdiction(PRIMARY);
        caseType(primary, "ZLegacy", 1, null);
        caseType(primary, "ALegacy", 1, null);
        jurisdiction(SECONDARY);

        JsonNode response = requestJurisdictions(filtered);
        JsonNode jurisdiction = findJurisdiction(response, PRIMARY);

        assertThat(jurisdiction.get("name").asText()).isEqualTo(PRIMARY + " name");
        assertThat(jurisdiction.get("description").asText()).isEqualTo("Jurisdiction description");
        assertThat(jurisdiction.get("case_types")).extracting(item -> item.get("id").asText())
            .containsExactlyInAnyOrder("ALegacy", "ZLegacy");
        assertThat(jurisdiction.get("case_types")).allSatisfy(item -> {
            assertThat(item.has("caseTypesOrder")).isFalse();
            assertThat(item.get("name").asText()).isEqualTo(item.get("id").asText() + " name");
            assertThat(item.get("description").asText()).isEqualTo("Case description");
            assertThat(item.get("events")).isEmpty();
            assertThat(item.get("states")).isEmpty();
            assertThat(item.get("acls")).isEmpty();
        });
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void shouldIncludeAllCaseTypesWhenOrderValuesAreDuplicated(boolean filtered) throws Exception {
        JurisdictionEntity primary = jurisdiction(PRIMARY);
        caseType(primary, "FirstTie", 1, 42);
        caseType(primary, "Earlier", 1, 7);
        caseType(primary, "SecondTie", 1, 42);
        jurisdiction(SECONDARY);

        JsonNode types = findJurisdiction(requestJurisdictions(filtered), PRIMARY).get("case_types");

        assertThat(types).extracting(item -> item.get("caseTypesOrder").intValue())
            .containsExactlyInAnyOrder(7, 42, 42);
        assertThat(types).extracting(item -> item.get("id").asText())
            .containsExactlyInAnyOrder("Earlier", "FirstTie", "SecondTie");
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void shouldSelectOnlyCaseTypesWithTheRequestedOrderAndSortById(boolean filtered) throws Exception {
        JurisdictionEntity primary = jurisdiction(PRIMARY);
        caseType(primary, "ZuluMatch", 1, 42).setName("Alpha name");
        caseType(primary, "AlphaMatch", 1, 42).setName("Zulu name");
        caseType(primary, "OtherOrder", 1, 7);
        caseType(primary, "WithoutOrder", 1, null);
        caseType(primary, "ChangedOrder", 1, 42);
        caseType(primary, "ChangedOrder", 2, 7);
        JurisdictionEntity secondary = jurisdiction(SECONDARY);
        caseType(secondary, "SecondaryMatch", 1, 42);
        caseType(secondary, "SecondaryOther", 1, 1);

        JsonNode response = requestJurisdictions(filtered, 42);

        JsonNode primaryTypes = findJurisdiction(response, PRIMARY).get("case_types");
        assertThat(primaryTypes).extracting(item -> item.get("id").asText())
            .containsExactly("AlphaMatch", "ZuluMatch");
        assertThat(primaryTypes).allSatisfy(item ->
            assertThat(item.get("caseTypesOrder").intValue()).isEqualTo(42));
        assertThat(findJurisdiction(response, SECONDARY).get("case_types"))
            .extracting(item -> item.get("id").asText()).containsExactly("SecondaryMatch");
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void shouldAcceptOneAsTheMinimumExplicitFilter(boolean filtered) throws Exception {
        JurisdictionEntity primary = jurisdiction(PRIMARY);
        caseType(primary, "One", 1, 1);
        caseType(primary, "Other", 1, 42);
        caseType(primary, "WithoutOrder", 1, null);
        jurisdiction(SECONDARY);

        JsonNode response = requestJurisdictions(filtered, 1);

        assertThat(findJurisdiction(response, PRIMARY).get("case_types"))
            .extracting(item -> item.get("id").asText()).containsExactly("One");
        assertThat(findJurisdiction(response, SECONDARY).get("case_types")).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void shouldKeepJurisdictionsWithNoMatchingCaseTypes(boolean filtered) throws Exception {
        JurisdictionEntity primary = jurisdiction(PRIMARY);
        caseType(primary, "Other", 1, 7);
        caseType(primary, "WithoutOrder", 1, null);
        jurisdiction(SECONDARY);

        JsonNode response = requestJurisdictions(filtered, 99);

        assertThat(response).hasSize(2);
        assertThat(findJurisdiction(response, PRIMARY).get("case_types")).isEmpty();
        assertThat(findJurisdiction(response, SECONDARY).get("case_types")).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1})
    void shouldRejectNonPositiveOrderFilter(int order) throws Exception {
        String body = mockMvc.perform(get("/api/data/jurisdictions").param("caseTypeOrder", Integer.toString(order))
            .header(AUTHORIZATION, "Bearer testUser"))
            .andExpect(status().isBadRequest())
            .andReturn().getResponse().getContentAsString();

        assertThat(body).contains("caseTypeOrder must be greater than or equal to 1");
    }

    @Test
    void shouldRejectNonNumericOrderFilter() throws Exception {
        mockMvc.perform(get("/api/data/jurisdictions").param("caseTypeOrder", "invalid")
            .header(AUTHORIZATION, "Bearer testUser"))
            .andExpect(status().isBadRequest());
    }

    private JsonNode requestJurisdictions(boolean filtered) throws Exception {
        return requestJurisdictions(filtered, null);
    }

    private JsonNode requestJurisdictions(boolean filtered, Integer order) throws Exception {
        entityManager.flush();
        entityManager.clear();
        MockHttpServletRequestBuilder request = get("/api/data/jurisdictions")
            .header(AUTHORIZATION, "Bearer testUser");
        if (filtered) {
            request.param("ids", PRIMARY + "," + SECONDARY);
        }
        if (order != null) {
            request.param("caseTypeOrder", order.toString());
        }
        String body = mockMvc.perform(request).andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();
        return mapper.readTree(body);
    }

    private JsonNode findJurisdiction(JsonNode response, String reference) {
        for (JsonNode jurisdiction : response) {
            if (reference.equals(jurisdiction.get("id").asText())) {
                return jurisdiction;
            }
        }
        throw new AssertionError("Missing jurisdiction " + reference);
    }

    private JurisdictionEntity jurisdiction(String reference) {
        JurisdictionEntity entity = new JurisdictionEntity();
        entity.setReference(reference);
        entity.setName(reference + " name");
        entity.setDescription("Jurisdiction description");
        entity.setVersion(1);
        return jurisdictionRepository.save(entity);
    }

    private CaseTypeEntity caseType(JurisdictionEntity jurisdiction, String reference, int version, Integer order) {
        CaseTypeEntity entity = new CaseTypeEntity();
        entity.setJurisdiction(jurisdiction);
        entity.setReference(reference);
        entity.setVersion(version);
        entity.setName(reference + " name");
        entity.setDescription("Case description");
        entity.setSecurityClassification(PUBLIC);
        entity.setCaseTypesOrder(order);
        return caseTypeRepository.save(entity);
    }
}
