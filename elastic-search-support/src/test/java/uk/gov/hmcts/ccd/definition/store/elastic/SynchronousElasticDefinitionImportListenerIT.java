package uk.gov.hmcts.ccd.definition.store.elastic;

import org.json.JSONException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import uk.gov.hmcts.ccd.definition.store.event.DefinitionImportedEvent;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldTypeEntity;
import uk.gov.hmcts.ccd.definition.store.utils.CaseFieldBuilder;
import uk.gov.hmcts.ccd.definition.store.utils.CaseTypeBuilder;
import uk.gov.hmcts.ccd.definition.store.utils.FieldTypeBuilder;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.hmcts.ccd.definition.store.elastic.hamcresutil.IsEqualJSON.equalToJSONInFile;
import static uk.gov.hmcts.ccd.definition.store.utils.CaseFieldBuilder.newField;
import static uk.gov.hmcts.ccd.definition.store.utils.CaseFieldBuilder.newTextField;
import static uk.gov.hmcts.ccd.definition.store.utils.FieldTypeBuilder.newType;
import static uk.gov.hmcts.ccd.definition.store.utils.FieldTypeBuilder.textFieldType;

class SynchronousElasticDefinitionImportListenerIT extends ElasticsearchBaseTest {

    private static final String CASE_TYPE_A = "CaseTypeA";
    private static final String CASE_TYPE_B = "CaseTypeB";

    @Value("${elasticsearch.port}")
    private String port;

    @Autowired
    private SynchronousElasticDefinitionImportListener definitionImportListener;

    private final CaseTypeBuilder caseTypeBuilder = new CaseTypeBuilder()
        .withJurisdiction("JUR").withReference(CASE_TYPE_A);

    @BeforeEach
    void setUp() throws IOException {
        deleteElasticsearchIndices(WILDCARD);
    }

    @Test
    @SuppressWarnings("checkstyle:VariableDeclarationUsageDistance")
    void shouldCreateCompleteElasticsearchIndexForSingleCaseType() throws IOException, JSONException {
        CaseFieldEntity baseTypeField = newTextField("TextField").build();
        CaseFieldEntity complexField = newComplexField("ComplexField");
        CaseFieldEntity collectionField = newCollectionFieldOfBaseType(
            "CollectionField", "BaseCollectionType");
        CaseFieldEntity complexCollectionField = newCollectionOfComplexField(
            "ComplexCollectionField", "ComplexCollectionType");

        CaseFieldEntity nonSearchableBaseTypeField = newTextField("NonSearchableTextField")
            .withSearchable(false).build();
        CaseFieldEntity nonSearchableComplexField = newComplexField("NonSearchableComplexField");
        nonSearchableComplexField.getFieldType().getComplexFields().get(0).getFieldType()
            .getComplexFields().get(1).setSearchable(false);
        nonSearchableComplexField.getFieldType().getComplexFields().get(0).getFieldType()
            .getComplexFields().get(2).setSearchable(false);

        CaseFieldEntity nonSearchableCollectionField = newCollectionField(
            "NonSearchableCollectionField", "NonSearchableCollectionType");
        nonSearchableCollectionField.setSearchable(false);

        CaseFieldEntity nonSearchableComplexCollectionField = newCollectionOfComplexField(
            "NonSearchableComplexCollectionField", "NonSearchableComplexCollectionType");
        nonSearchableComplexCollectionField.getCollectionFieldType().getComplexFields().get(0).setSearchable(false);
        nonSearchableComplexCollectionField.getCollectionFieldType().getComplexFields().get(2).setSearchable(false);

        CaseTypeEntity caseTypeEntity = caseTypeBuilder
            .addField(baseTypeField)
            .addField(complexField)
            .addField(collectionField)
            .addField(complexCollectionField)
            .addField(nonSearchableBaseTypeField)
            .addField(nonSearchableComplexField)
            .addField(nonSearchableCollectionField)
            .addField(nonSearchableComplexCollectionField).build();

        DefinitionImportedEvent event = new DefinitionImportedEvent(Collections.singletonList(caseTypeEntity));

        definitionImportListener.onDefinitionImported(event);

        String response = getElasticsearchIndices(CASE_TYPE_A);

        assertThat(response, equalToJSONInFile(
            readFileFromClasspath("integration/single_casetype_index.json"),
            ignoreFieldsComparator(getDynamicIndexResponseFields(CASE_TYPE_A))));
    }

    @Test
    void shouldCreateElasticsearchIndexForAllCaseTypes() throws IOException, JSONException {
        CaseFieldEntity baseTypeField1 = newTextField("TextField1").build();
        CaseFieldEntity baseTypeField2 = newTextField("TextField2").build();

        CaseTypeEntity caseTypeEntity1 = caseTypeBuilder
            .addField(baseTypeField1)
            .build();
        CaseTypeEntity caseTypeEntity2 = new CaseTypeBuilder()
            .withJurisdiction("JUR")
            .withReference(CASE_TYPE_B)
            .addField(baseTypeField2)
            .build();

        DefinitionImportedEvent event = new DefinitionImportedEvent(Arrays.asList(caseTypeEntity1, caseTypeEntity2));

        definitionImportListener.onDefinitionImported(event);

        String response = getElasticsearchIndices(CASE_TYPE_A, CASE_TYPE_B);

        assertThat(response, equalToJSONInFile(
            readFileFromClasspath("integration/multi_casetypes_indices.json"),
            ignoreFieldsComparator(getDynamicIndexResponseFields(CASE_TYPE_A, CASE_TYPE_B))));
    }

    private String[] getDynamicIndexResponseFields(String... indexNames) {
        return Arrays.stream(indexNames)
            .map(String::toLowerCase)
            .map(index -> new String[] {
                // Paths of fields which may have a different value each time an index is created
                index + "_cases-000001.settings.index.creation_date",
                index + "_cases-000001.settings.index.uuid",
                index + "_cases-000001.settings.index.version.created"})
            .flatMap(Arrays::stream)
            .toArray(String[]::new);
    }

    private CaseFieldEntity newComplexField(String topLevelReference) {
        FieldTypeBuilder complexType = newType("Person");
        complexType.addFieldToComplex("Forename", textFieldType());
        complexType.addFieldToComplex("Surname", textFieldType());
        complexType.addFieldToComplex("Dob", newType("Date").build());
        FieldTypeEntity execPersonComplexFieldType = complexType.buildComplex();

        CaseFieldBuilder complexOfComplex = newField(topLevelReference, topLevelReference);
        complexOfComplex.addFieldToComplex(topLevelReference + "Person", execPersonComplexFieldType);

        return complexOfComplex.buildComplex();
    }

    private CaseFieldEntity newCollectionField(String topLevelReference, String collectionReference) {
        CaseFieldBuilder complexField = newField(topLevelReference, topLevelReference);

        FieldTypeEntity collectionFieldType = newType(collectionReference + "-51503ee8-ac6d-4b57-845e-4806332a9820")
            .addFieldToCollection(textFieldType()).buildCollection();

        complexField.addFieldToComplex(collectionReference, collectionFieldType);
        return complexField.buildComplex();
    }

    private CaseFieldEntity newCollectionFieldOfBaseType(String fieldReference, String collectionTypeReference) {
        FieldTypeEntity collectionFieldType = newType(collectionTypeReference + "-51503ee8-ac6d-4b57-845e-4806332a9820")
            .addFieldToCollection(textFieldType()).buildCollection();

        CaseFieldEntity collectionField = new CaseFieldEntity();
        collectionField.setReference(fieldReference);
        collectionField.setFieldType(collectionFieldType);
        return collectionField;
    }

    private CaseFieldEntity newCollectionOfComplexField(String fieldReference, String collectionTypeReference) {
        FieldTypeBuilder complexType = newType("Person");
        complexType.addFieldToComplex("Forename", textFieldType());
        complexType.addFieldToComplex("Surname", textFieldType());
        complexType.addFieldToComplex("Dob", newType("Date").build());
        FieldTypeEntity personComplexFieldType = complexType.buildComplex();

        FieldTypeEntity collectionFieldType = newType(collectionTypeReference + "-51503ee8-ac6d-4b57-845e-4806332a9820")
            .addFieldToCollection(personComplexFieldType).buildCollection();

        CaseFieldEntity collectionField = new CaseFieldEntity();
        collectionField.setReference(fieldReference);
        collectionField.setFieldType(collectionFieldType);

        return collectionField;
    }
}
