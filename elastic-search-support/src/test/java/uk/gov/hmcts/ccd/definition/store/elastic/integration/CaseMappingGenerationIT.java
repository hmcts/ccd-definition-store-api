package uk.gov.hmcts.ccd.definition.store.elastic.integration;

import static com.google.common.collect.Lists.newArrayList;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.ccd.definition.store.elastic.hamcresutil.IsEqualJSON.equalToJSONInFile;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTestContextBootstrapper;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.BootstrapWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.ccd.definition.store.elastic.ElasticDefinitionImportListener;
import uk.gov.hmcts.ccd.definition.store.elastic.SynchronousElasticDefinitionImportListener;
import uk.gov.hmcts.ccd.definition.store.elastic.TestUtils;
import uk.gov.hmcts.ccd.definition.store.elastic.client.HighLevelCCDElasticClient;
import uk.gov.hmcts.ccd.definition.store.elastic.config.CcdElasticSearchProperties;
import uk.gov.hmcts.ccd.definition.store.elastic.config.ElasticSearchConfiguration;
import uk.gov.hmcts.ccd.definition.store.elastic.mapping.CaseMappingGenerator;
import uk.gov.hmcts.ccd.definition.store.event.DefinitionImportedEvent;
import uk.gov.hmcts.ccd.definition.store.repository.SanityCheckApplication;
import uk.gov.hmcts.ccd.definition.store.repository.TestConfiguration;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.model.CaseType;
import uk.gov.hmcts.ccd.definition.store.utils.CaseFieldBuilder;
import uk.gov.hmcts.ccd.definition.store.utils.CaseTypeBuilder;
import uk.gov.hmcts.ccd.definition.store.utils.FieldTypeBuilder;

@RunWith(SpringRunner.class)
@BootstrapWith(SpringBootTestContextBootstrapper.class)
@ContextConfiguration(classes = ElasticSearchConfiguration.class, initializers = ConfigFileApplicationContextInitializer.class)
public class CaseMappingGenerationIT implements TestUtils {

    @Autowired
    private ElasticDefinitionImportListener listener;

    @Autowired
    private ApplicationEventPublisher publisher;

    @Autowired
    private CcdElasticSearchProperties config;

    @Autowired
    CaseMappingGenerator mappingGenerator;

    @MockBean
    private HighLevelCCDElasticClient client;

    @Test
    public void testListeningToDefinitionImportedEvent() throws IOException {
        CaseTypeEntity caseType = createCaseType();

        publisher.publishEvent(new DefinitionImportedEvent(newArrayList(caseType)));

        verify(client).createIndex(anyString());
        verify(client).upsertMapping(anyString(), anyString());
    }

    @Test
    public void testMappingGeneration() {
        CaseTypeEntity caseType = createCaseType();

        String mapping = mappingGenerator.generateMapping(caseType);

        assertThat(mapping, equalToJSONInFile(
                readFileFromClasspath("integration/case_type_mapping.json")));
    }

    private CaseTypeEntity createCaseType() {
        CaseTypeBuilder caseTypeBuilder = new CaseTypeBuilder().withJurisdiction("jur").withReference("caseTypeA");
        CaseFieldEntity baseTypeField = new CaseFieldBuilder().withReference("forename").withFieldTypeReference("Text").buildBaseType();
        CaseFieldEntity complexOfComplex = newComplexFieldOfComplex();
        CaseFieldEntity complexOfCollection = newComplexFieldOfCollection();
        CaseFieldEntity collectionOfBaseType = newCollectionFieldOfBaseType();

        return caseTypeBuilder.withField(baseTypeField)
                .withField(complexOfComplex)
                .withField(complexOfCollection)
                .withField(collectionOfBaseType).build();
    }

    private CaseFieldEntity newComplexFieldOfComplex() {
        CaseFieldBuilder executorBuilder = new CaseFieldBuilder().withReference("executor");
        executorBuilder.withFieldTypeReference("Executor");

        FieldTypeBuilder execPersonComplex = new FieldTypeBuilder().withReference("Person");
        execPersonComplex.addComplexField("forename", FieldTypeBuilder.textFieldType());
        execPersonComplex.addComplexField("dob", FieldTypeBuilder.baseFieldType("Date"));
        FieldTypeEntity execPersonComplexFieldType = execPersonComplex.buildComplex();
        executorBuilder.withComplexField("executorPerson", execPersonComplexFieldType);

        return executorBuilder.buildComplexType();
    }

    private CaseFieldEntity newComplexFieldOfCollection() {
        CaseFieldBuilder complexField = new CaseFieldBuilder().withReference("appealReasons");
        complexField.withFieldTypeReference("appealReasons");

        FieldTypeEntity collectionFieldType = new FieldTypeBuilder().withReference
                ("reasons-51503ee8-ac6d-4b57-845e-4806332a9820").withCollectionFieldType(FieldTypeBuilder
                .textFieldType()).buildCollection();

        complexField.withComplexField("reasons", collectionFieldType);
        return complexField.buildComplexType();
    }

    private CaseFieldEntity newCollectionFieldOfBaseType() {
        FieldTypeEntity collectionFieldType = new FieldTypeBuilder().withReference
                ("reasons-51503ee8-ac6d-4b57-845e-4806332a9820").withCollectionFieldType(FieldTypeBuilder
                .textFieldType()).buildCollection();

        CaseFieldEntity collectionField = new CaseFieldEntity();
        collectionField.setReference("Aliases");
        collectionField.setFieldType(collectionFieldType);
        return collectionField;
    }
}
