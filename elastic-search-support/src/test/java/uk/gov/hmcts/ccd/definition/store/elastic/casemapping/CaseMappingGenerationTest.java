package uk.gov.hmcts.ccd.definition.store.elastic.casemapping;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectFactory;
import uk.gov.hmcts.ccd.definition.store.elastic.ElasticDefinitionImportListener;
import uk.gov.hmcts.ccd.definition.store.elastic.client.HighLevelCCDElasticClient;
import uk.gov.hmcts.ccd.definition.store.elastic.config.CcdElasticSearchProperties;
import uk.gov.hmcts.ccd.definition.store.elastic.exception.handler.ElasticsearchErrorHandler;
import uk.gov.hmcts.ccd.definition.store.elastic.mapping.CaseMappingGenerator;
import uk.gov.hmcts.ccd.definition.store.elastic.service.ReindexService;
import uk.gov.hmcts.ccd.definition.store.event.DefinitionImportedEvent;
import uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldTypeEntity;
import uk.gov.hmcts.ccd.definition.store.utils.CaseFieldBuilder;
import uk.gov.hmcts.ccd.definition.store.utils.CaseTypeBuilder;
import uk.gov.hmcts.ccd.definition.store.utils.FieldTypeBuilder;

import java.io.IOException;

import static com.google.common.collect.Lists.newArrayList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.definition.store.utils.CaseFieldBuilder.newField;
import static uk.gov.hmcts.ccd.definition.store.utils.CaseFieldBuilder.newTextField;
import static uk.gov.hmcts.ccd.definition.store.utils.FieldTypeBuilder.newType;
import static uk.gov.hmcts.ccd.definition.store.utils.FieldTypeBuilder.textFieldType;

/**
 * Unit test for definition-import listener behaviour (createIndex / upsertMapping).
 * Does not use Spring context, so it cannot affect other ITs (e.g. SynchronousElasticDefinitionImportListenerIT).
 */
@ExtendWith(MockitoExtension.class)
class CaseMappingGenerationTest {

    @Mock
    private CcdElasticSearchProperties config;

    @Mock
    private CaseMappingGenerator mappingGenerator;

    @Mock
    private ObjectFactory<HighLevelCCDElasticClient> clientFactory;

    @Mock
    private HighLevelCCDElasticClient client;

    @Mock
    private ElasticsearchErrorHandler elasticsearchErrorHandler;

    @Mock
    private ReindexService reindexService;

    private ElasticDefinitionImportListener listener;

    @BeforeEach
    void setUp() throws IOException {
        listener = new TestDefinitionImportListener(
            config, mappingGenerator, clientFactory, elasticsearchErrorHandler, reindexService);
        when(config.getCasesIndexNameFormat()).thenReturn("%s_cases");
        when(clientFactory.getObject()).thenReturn(client);
        doReturn(false).when(client).aliasExists(anyString());
        when(mappingGenerator.generateMapping(any(CaseTypeEntity.class))).thenReturn("{}");
    }

    @Test
    void testListeningToDefinitionImportedEvent() throws IOException {
        CaseTypeEntity caseType = createCaseType();

        listener.onDefinitionImported(new DefinitionImportedEvent(newArrayList(caseType)));

        verify(client).createIndex(anyString(), anyString());
        verify(client).upsertMapping(anyString(), anyString());
    }

    @Test
    void testListeningToDefinitionImportedEventWithDynamicLists() throws IOException {
        CaseTypeEntity caseType = createCaseTypeWithDynamicLists();

        listener.onDefinitionImported(new DefinitionImportedEvent(newArrayList(caseType)));

        verify(client).createIndex(anyString(), anyString());
        verify(client).upsertMapping(anyString(), anyString());
    }

    private CaseTypeEntity createCaseType() {
        CaseTypeBuilder caseTypeBuilder = new CaseTypeBuilder().withJurisdiction("jur").withReference("caseTypeA");
        CaseFieldEntity baseTypeField = newTextField("forename").build();
        CaseFieldEntity complexOfComplex = newComplexFieldOfComplex();
        CaseFieldEntity complexOfCollection = newComplexFieldOfCollection();
        CaseFieldEntity collectionOfBaseType = newCollectionFieldOfBaseType();
        CaseFieldEntity dynamicField = newField("dynamicList", FieldTypeUtils.BASE_DYNAMIC_LIST).build();

        return caseTypeBuilder.addField(baseTypeField)
            .addField(complexOfComplex)
            .addField(complexOfCollection)
            .addField(collectionOfBaseType)
            .addField(dynamicField).build();
    }

    private CaseTypeEntity createCaseTypeWithDynamicLists() {
        CaseTypeEntity caseTypeEntity = createCaseType();
        CaseFieldEntity dynamicRadioField = newField("dynamicRadioList",
            FieldTypeUtils.BASE_DYNAMIC_RADIO_LIST).build();
        CaseFieldEntity dynamicMultiSelectField = newField("dynamicMultiSelectList",
            FieldTypeUtils.BASE_DYNAMIC_MULTI_SELECT_LIST).build();

        caseTypeEntity.addCaseField(dynamicRadioField);
        caseTypeEntity.addCaseField(dynamicMultiSelectField);
        return caseTypeEntity;
    }

    private CaseFieldEntity newComplexFieldOfComplex() {
        CaseFieldBuilder complexOfComplex = newField("executor", "Executor");

        FieldTypeBuilder complexType = newType("Person");
        complexType.addFieldToComplex("forename", textFieldType());
        complexType.addFieldToComplex("dob", newType("Date").build());
        FieldTypeEntity execPersonComplexFieldType = complexType.buildComplex();
        complexOfComplex.addFieldToComplex("executorPerson", execPersonComplexFieldType);

        return complexOfComplex.buildComplex();
    }

    private CaseFieldEntity newComplexFieldOfCollection() {
        CaseFieldBuilder complexField = newField("appealReasons", "appealReasons");

        FieldTypeEntity collectionFieldType = newType("reasons-51503ee8-ac6d-4b57-845e-4806332a9820")
            .addFieldToCollection(textFieldType()).buildCollection();

        complexField.addFieldToComplex("reasons", collectionFieldType);
        return complexField.buildComplex();
    }

    private CaseFieldEntity newCollectionFieldOfBaseType() {
        FieldTypeEntity collectionFieldType = newType("reasons-51503ee8-ac6d-4b57-845e-4806332a9820")
            .addFieldToCollection(textFieldType()).buildCollection();

        CaseFieldEntity collectionField = new CaseFieldEntity();
        collectionField.setReference("Aliases");
        collectionField.setFieldType(collectionFieldType);
        return collectionField;
    }

    private static final class TestDefinitionImportListener extends ElasticDefinitionImportListener {

        TestDefinitionImportListener(CcdElasticSearchProperties config,
                                     CaseMappingGenerator mappingGenerator,
                                     ObjectFactory<HighLevelCCDElasticClient> clientFactory,
                                     ElasticsearchErrorHandler elasticsearchErrorHandler,
                                     ReindexService reindexService) {
            super(config, mappingGenerator, clientFactory, elasticsearchErrorHandler, reindexService);
        }

        @Override
        public void onDefinitionImported(DefinitionImportedEvent event) {
            super.initialiseElasticSearch(event);
        }
    }
}
