package uk.gov.hmcts.ccd.definition.store.elastic.integration;

import java.io.IOException;

import static com.google.common.collect.Lists.newArrayList;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.definition.store.elastic.hamcresutil.IsEqualJSON.equalToJSONInFile;
import static uk.gov.hmcts.ccd.definition.store.utils.CaseFieldBuilder.newField;
import static uk.gov.hmcts.ccd.definition.store.utils.CaseFieldBuilder.newTextField;
import static uk.gov.hmcts.ccd.definition.store.utils.FieldTypeBuilder.newType;
import static uk.gov.hmcts.ccd.definition.store.utils.FieldTypeBuilder.textFieldType;

import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.boot.test.context.SpringBootTestContextBootstrapper;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.BootstrapWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.ccd.definition.store.elastic.ElasticDefinitionImportListener;
import uk.gov.hmcts.ccd.definition.store.elastic.TestUtils;
import uk.gov.hmcts.ccd.definition.store.elastic.client.HighLevelCCDElasticClient;
import uk.gov.hmcts.ccd.definition.store.elastic.config.CcdElasticSearchProperties;
import uk.gov.hmcts.ccd.definition.store.elastic.config.ElasticSearchConfiguration;
import uk.gov.hmcts.ccd.definition.store.elastic.mapping.CaseMappingGenerator;
import uk.gov.hmcts.ccd.definition.store.event.DefinitionImportedEvent;
import uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldTypeEntity;
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
    private CaseMappingGenerator mappingGenerator;

    @MockBean
    private HighLevelCCDElasticClient client;

    @Mock
    private ObjectFactory<HighLevelCCDElasticClient> clientObjectFactory;

    @BeforeEach
    public void setUp() {
        when(clientObjectFactory.getObject()).thenReturn(client);
    }

    @Test
    public void testListeningToDefinitionImportedEvent() throws IOException {
        CaseTypeEntity caseType = createCaseType();

        publisher.publishEvent(new DefinitionImportedEvent(newArrayList(caseType)));

        verify(client).createIndex(anyString(), anyString());
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
}
