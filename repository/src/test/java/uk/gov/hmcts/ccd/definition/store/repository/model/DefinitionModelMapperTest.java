package uk.gov.hmcts.ccd.definition.store.repository.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.ccd.definition.store.JacksonUtils;
import uk.gov.hmcts.ccd.definition.store.repository.entity.DefinitionEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.DefinitionStatus;
import uk.gov.hmcts.ccd.definition.store.repository.entity.JurisdictionEntity;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.when;

class DefinitionModelMapperTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Mock
    private DefinitionEntity definitionEntity;

    @Mock
    private JurisdictionEntity jurisdictionEntity;

    private Definition definition;

    private Definition definitionWithNoData;

    @InjectMocks
    private DefinitionModelMapper classUnderTest;

    @BeforeEach
    void setUp() throws IOException {
        MockitoAnnotations.initMocks(this);
        setupMockJurisdictionEntity();
        setupMockDefinitionEntity();
        definition = createDefinition();
        definitionWithNoData = createDefinition();
        definitionWithNoData.setData(null);
    }

    @Test
    @DisplayName("Should map model to entity")
    void shouldReturnPopulatedEntity() {
        final DefinitionEntity entity = classUnderTest.toEntity(definition);
        assertAll(
            () -> assertThat(entity.getId(), is(nullValue())),
            // The Jurisdiction is expected not to be mapped deliberately; it will always be an existing
            // entity, so it
            // should be retrieved and set on the DefinitionEntity, post mapping
            () -> assertThat(entity.getJurisdiction(), is(nullValue())),
            () -> assertThat(entity.getCaseTypes(), is(definition.getCaseTypes())),
            () -> assertThat(entity.getDescription(), is(definition.getDescription())),
            () -> assertThat(entity.getVersion(), is(definition.getVersion())),
            () -> assertThat(entity.getStatus(), is(definition.getStatus())),
            () -> assertThat(entity.getData(), is(JacksonUtils.convertValueJsonNode(definition.getData()))),
            () -> assertThat(entity.getAuthor(), is(definition.getAuthor())),
            () -> assertThat(entity.getCreatedAt(), is(nullValue())),
            () -> assertThat(entity.getLastModified(), is(definition.getLastModified())),
            () -> assertThat(entity.isDeleted(), is(definition.isDeleted())));
    }

    @Test
    @DisplayName("Should map model to entity even when there is no data")
    void shouldReturnPopulatedEntityEvenWithNoData() throws Exception {
        final DefinitionEntity entity = classUnderTest.toEntity(definitionWithNoData);
        assertAll(
            () -> assertThat(entity.getId(), is(nullValue())),
            () -> assertThat(entity.getJurisdiction(), is(nullValue())),
            () -> assertThat(entity.getCaseTypes(), is(definition.getCaseTypes())),
            () -> assertThat(entity.getDescription(), is(definition.getDescription())),
            () -> assertThat(entity.getVersion(), is(definition.getVersion())),
            () -> assertThat(entity.getStatus(), is(definition.getStatus())),
            () -> assertThat(mapper.writeValueAsString(entity.getData()), is("{}")),
            () -> assertThat(entity.getAuthor(), is(definition.getAuthor())),
            () -> assertThat(entity.getCreatedAt(), is(nullValue())),
            () -> assertThat(entity.getLastModified(), is(definition.getLastModified())),
            () -> assertThat(entity.isDeleted(), is(definition.isDeleted())));
    }

    @Test
    @DisplayName("Should copy model to an existent entity")
    void shouldCopyModelToExistentEntity() {
        final DefinitionEntity entity = new DefinitionEntity();
        classUnderTest.toEntity(definition, entity);
        assertAll(
            () -> assertThat(entity.getId(), is(nullValue())),
            () -> assertThat(entity.getJurisdiction(), is(nullValue())),
            () -> assertThat(entity.getCaseTypes(), is(definition.getCaseTypes())),
            () -> assertThat(entity.getDescription(), is(definition.getDescription())),
            () -> assertThat(entity.getVersion(), is(nullValue())),
            () -> assertThat(entity.getStatus(), is(definition.getStatus())),
            () -> assertThat(entity.getData(), is(JacksonUtils.convertValueJsonNode(definition.getData()))),
            () -> assertThat(entity.getAuthor(), is(definition.getAuthor())),
            () -> assertThat(entity.getCreatedAt(), is(nullValue())),
            () -> assertThat(entity.getLastModified(), is(definition.getLastModified())),
            () -> assertThat(entity.isDeleted(), is(definition.isDeleted())));
    }

    @Test
    @DisplayName("Should copy model to an existent entity, with no data")
    void shouldCopyModelToExistentEntityWithNoData() throws Exception {
        final DefinitionEntity entity = new DefinitionEntity();
        classUnderTest.toEntity(definitionWithNoData, entity);
        assertAll(
            () -> assertThat(entity.getId(), is(nullValue())),
            () -> assertThat(entity.getJurisdiction(), is(nullValue())),
            () -> assertThat(entity.getCaseTypes(), is(definition.getCaseTypes())),
            () -> assertThat(entity.getDescription(), is(definition.getDescription())),
            () -> assertThat(entity.getVersion(), is(nullValue())),
            () -> assertThat(entity.getStatus(), is(definition.getStatus())),
            () -> assertThat(mapper.writeValueAsString(entity.getData()), is("{}")),
            () -> assertThat(entity.getAuthor(), is(definition.getAuthor())),
            () -> assertThat(entity.getCreatedAt(), is(nullValue())),
            () -> assertThat(entity.getLastModified(), is(definition.getLastModified())),
            () -> assertThat(entity.isDeleted(), is(definition.isDeleted())));
    }

    @Test
    @DisplayName("Should map entity to model")
    void shouldReturnPopulatedModel() {
        final Definition model = classUnderTest.toModel(definitionEntity);
        assertThat(model.getJurisdiction().getId(), is(definitionEntity.getJurisdiction().getReference()));
        assertThat(model.getJurisdiction().getName(), is(definitionEntity.getJurisdiction().getName()));
        assertThat(model.getJurisdiction().getDescription(), is(definitionEntity.getJurisdiction().getDescription()));
        assertThat(model.getJurisdiction().getLiveFrom(), is(definitionEntity.getJurisdiction().getLiveFrom()));
        assertThat(model.getJurisdiction().getLiveUntil(), is(definitionEntity.getJurisdiction().getLiveTo()));
        assertThat(model.getCaseTypes(), is(definitionEntity.getCaseTypes()));
        assertThat(model.getDescription(), is(definitionEntity.getDescription()));
        assertThat(model.getVersion(), is(definitionEntity.getVersion()));
        assertThat(model.getStatus(), is(definitionEntity.getStatus()));
        Map<String, JsonNode> data = JacksonUtils.convertValue(definitionEntity.getData());
        assertThat(model.getData(), is(data));
        assertThat(model.getAuthor(), is(definitionEntity.getAuthor()));
        assertThat(model.getCreatedAt(), is(definitionEntity.getCreatedAt()));
        assertThat(model.getLastModified(), is(definitionEntity.getLastModified()));
        assertThat(model.isDeleted(), is(definitionEntity.isDeleted()));
    }

    private void setupMockDefinitionEntity() throws IOException {
        when(definitionEntity.getId()).thenReturn(-1);
        when(definitionEntity.getJurisdiction()).thenReturn(jurisdictionEntity);
        when(definitionEntity.getCaseTypes()).thenReturn("TestCaseType,AnotherTestCaseType");
        when(definitionEntity.getDescription()).thenReturn("Test description");
        when(definitionEntity.getVersion()).thenReturn(1);
        when(definitionEntity.getStatus()).thenReturn(DefinitionStatus.DRAFT);
        JsonNode node = mapper.readTree("{\"Field1\": \"Value1\", \"Field2\": []}");
        when(definitionEntity.getData()).thenReturn(node);
        when(definitionEntity.getAuthor()).thenReturn("ccd@hmcts");
        when(definitionEntity.getCreatedAt()).thenReturn(LocalDateTime.of(2018, 10, 11, 13, 12, 30));
        when(definitionEntity.getLastModified()).thenReturn(LocalDateTime.of(2018, 10, 11, 13, 14, 20));
        when(definitionEntity.isDeleted()).thenReturn(false);
    }

    private Definition createDefinition() throws IOException {
        final Definition definition = new Definition();
        definition.setCaseTypes("CaseType1,CaseType2");
        definition.setDescription("Description");
        definition.setVersion(2);
        definition.setStatus(DefinitionStatus.PUBLISHED);
        JsonNode node = mapper.readTree("{\"Field1\": \"Value1\", \"Field2\": []}");
        Map<String, JsonNode> data = new HashMap<>();
        data.put("Data", node);
        definition.setData(data);
        definition.setAuthor("ccd2@hmcts");
        definition.setCreatedAt(LocalDateTime.of(2018, 10, 11, 13, 50, 50));
        definition.setLastModified(LocalDateTime.of(2018, 10, 11, 13, 52, 40));
        definition.setDeleted(false);
        return definition;
    }

    private void setupMockJurisdictionEntity() {
        when(jurisdictionEntity.getId()).thenReturn(-1);
        when(jurisdictionEntity.getCreatedAt()).thenReturn(LocalDateTime.of(2018, 10, 16, 15, 10, 0));
        when(jurisdictionEntity.getReference()).thenReturn("TEST2");
        when(jurisdictionEntity.getVersion()).thenReturn(2);
        when(jurisdictionEntity.getLiveFrom()).thenReturn(Date.from(LocalDate.of(2018, 10, 16).atStartOfDay()
            .atZone(ZoneId.systemDefault()).toInstant()));
        when(jurisdictionEntity.getLiveTo()).thenReturn(Date.from(LocalDate.of(2028, 10, 16).atStartOfDay()
            .atZone(ZoneId.systemDefault()).toInstant()));
        when(jurisdictionEntity.getName()).thenReturn("Test 2 Jurisdiction");
        when(jurisdictionEntity.getDescription()).thenReturn("Second jurisdiction used for testing");
    }
}
