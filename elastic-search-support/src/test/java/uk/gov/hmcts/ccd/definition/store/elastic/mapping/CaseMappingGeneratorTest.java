package uk.gov.hmcts.ccd.definition.store.elastic.mapping;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import uk.gov.hmcts.ccd.definition.store.elastic.TestUtils;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.SearchAliasFieldEntity;
import uk.gov.hmcts.ccd.definition.store.utils.CaseTypeBuilder;
import uk.gov.hmcts.ccd.definition.store.utils.SearchAliasFieldBuilder;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.definition.store.elastic.hamcresutil.IsEqualJSON.equalToJSONInFile;
import static uk.gov.hmcts.ccd.definition.store.elastic.mapping.CaseMappingGenerator.ALIAS_CASE_FIELD_PATH_PLACE_HOLDER;
import static uk.gov.hmcts.ccd.definition.store.elastic.mapping.MappingGenerator.ALIAS;
import static uk.gov.hmcts.ccd.definition.store.elastic.mapping.MappingGenerator.ALIAS_TEXT_SORT;
import static uk.gov.hmcts.ccd.definition.store.elastic.mapping.MappingGenerator.DEFAULT_TEXT;
import static uk.gov.hmcts.ccd.definition.store.utils.CaseFieldBuilder.newField;
import static uk.gov.hmcts.ccd.definition.store.utils.CaseFieldBuilder.newTextField;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CaseMappingGeneratorTest extends AbstractMapperTest implements TestUtils {

    private static final String SUPPLEMENTARY_DATA_MAPPING =
        "{\"properties\":{\"new_case\":{\"type\":\"flattened\"},"
            + "\"orgs_assigned_users\":{\"type\":\"flattened\"}}}";

    @InjectMocks
    private CaseMappingGenerator mappingGenerator;

    private final CaseTypeBuilder caseType = new CaseTypeBuilder().withJurisdiction("jur").withReference("caseTypeA");

    @BeforeEach
    void setUp() {
        super.setup();
        when(config.getDynamic()).thenReturn("dynamicConfig");
        addMappingGenerator(new StubTypeMappingGenerator(null, "Text", "dataMapping", "dataClassificationMapping"));
        mappingGenerator.inject(stubTypeMappersManager);
    }

    @Test
    void shouldCrateMappingForPredefinedProperties() {
        predefinedMappings.put("testPropA", "valuePropA");
        predefinedMappings.put("testPropB", "valuePropB");
        predefinedMappings.put("supplementary_data", SUPPLEMENTARY_DATA_MAPPING);

        String result = mappingGenerator.generateMapping(caseType.build());

        assertThat(result, equalToJSONInFile(
            readFileFromClasspath("json/case_mapping_generator_predefined_properties.json")));
    }

    @Test
    void shouldMapOrganisationKeyedSupplementaryDataAsFlattenedFields() throws IOException {
        predefinedMappings.put("supplementary_data", SUPPLEMENTARY_DATA_MAPPING);

        JsonNode mapping = new ObjectMapper().readTree(mappingGenerator.generateMapping(caseType.build()));
        JsonNode supplementaryDataProperties = mapping.path("properties").path("supplementary_data").path("properties");
        JsonNode newCase = supplementaryDataProperties.path("new_case");
        JsonNode orgsAssignedUsers = supplementaryDataProperties.path("orgs_assigned_users");

        assertEquals("flattened", newCase.path("type").asText());
        assertEquals("flattened", orgsAssignedUsers.path("type").asText());
        assertFalse(newCase.has("properties"));
        assertFalse(orgsAssignedUsers.has("properties"));
    }

    @Test
    void shouldCreateMappingForDataAndDataClassification() {
        CaseFieldEntity fieldA = newTextField("fieldA").build();
        CaseFieldEntity fieldB = newTextField("fieldB").build();
        CaseFieldEntity fieldC = newField("fieldC", "Label").build();
        caseType.addField(fieldA).addField(fieldB).addField(fieldC);

        String result = mappingGenerator.generateMapping(caseType.build());

        assertThat(result, equalToJSONInFile(readFileFromClasspath("json/case_mapping_generator_data.json")));
    }

    @Test
    void shouldCreateMappingForSearchAlias() {
        elasticMappings.put(ALIAS, "aliasMapping." + ALIAS_CASE_FIELD_PATH_PLACE_HOLDER);
        elasticMappings.put(ALIAS_TEXT_SORT, "aliasTextSortMapping." + ALIAS_CASE_FIELD_PATH_PLACE_HOLDER);
        elasticMappings.put(DEFAULT_TEXT, "textType");
        typeMappings.put("Text", "textType");

        SearchAliasFieldEntity searchAliasField = new SearchAliasFieldBuilder("aliasFieldA")
            .withFieldType("Text")
            .withCaseFieldPath("caseFieldPath")
            .build();

        caseType.addSearchAliasField(searchAliasField);

        String result = mappingGenerator.generateMapping(caseType.build());
        assertThat(result, equalToJSONInFile(readFileFromClasspath("json/case_mapping_generator_search_alias.json")));
    }
}
