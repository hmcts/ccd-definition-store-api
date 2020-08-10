package uk.gov.hmcts.ccd.definition.store.elastic;

import org.json.JSONException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import uk.gov.hmcts.ccd.definition.store.event.DefinitionImportedEvent;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.utils.CaseTypeBuilder;

import java.io.IOException;
import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.hmcts.ccd.definition.store.elastic.hamcresutil.IsEqualJSON.equalToJSONInFile;
import static uk.gov.hmcts.ccd.definition.store.utils.CaseFieldBuilder.newTextField;

class SynchronousElasticDefinitionImportListenerIT extends ElasticsearchBaseTest {

    private static final String CASE_TYPE_NAME = "CaseTypeA";

    @Value("${elasticsearch.port}")
    private String port;

    @Autowired
    private SynchronousElasticDefinitionImportListener definitionImportListener;

    private final CaseTypeBuilder caseType = new CaseTypeBuilder().withJurisdiction("JUR").withReference(CASE_TYPE_NAME);

    @BeforeEach
    void setUp() {
        // TODO: Clear any existing indexes
    }

    @Test
    void shouldCreateCompleteElasticsearchIndexForCaseType() throws IOException, JSONException {
        CaseTypeEntity caseTypeEntity = caseType
            .addField(newTextField("TestReference").build())
            .build();
        // TODO: Add more types
        // TODO: Add non-searchable
        DefinitionImportedEvent event = new DefinitionImportedEvent(Collections.singletonList(caseTypeEntity));

        definitionImportListener.onDefinitionImported(event);

        String response = getElasticsearchIndexes(CASE_TYPE_NAME);

        assertThat(response, equalToJSONInFile(
            readFileFromClasspath("json/single_casetype_index.json"),
            ignoreFieldsComparator(getDynamicIndexResponseFields(CASE_TYPE_NAME))));
    }

    private String[] getDynamicIndexResponseFields(String indexName) {
        String lowerCaseIndex = indexName.toLowerCase();
        // Return paths of fields which may have a different value each time an index is created
        return new String[] {
            lowerCaseIndex + "_cases-000001.settings.index.creation_date",
            lowerCaseIndex + "_cases-000001.settings.index.uuid",
            lowerCaseIndex + "_cases-000001.settings.index.version.created"
        };
    }
}
