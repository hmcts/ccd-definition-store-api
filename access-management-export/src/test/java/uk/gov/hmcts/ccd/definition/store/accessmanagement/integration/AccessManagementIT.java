package uk.gov.hmcts.ccd.definition.store.accessmanagement.integration;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.boot.test.context.SpringBootTestContextBootstrapper;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.BootstrapWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.ccd.definition.store.accessmanagement.AccessManagementDefinitionImportListener;
import uk.gov.hmcts.ccd.definition.store.accessmanagement.config.AccessManagementExportConfiguration;
import uk.gov.hmcts.ccd.definition.store.event.DefinitionImportedEvent;
import uk.gov.hmcts.ccd.definition.store.event.RoleImportedEvent;
import uk.gov.hmcts.ccd.definition.store.repository.SecurityClassification;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.UserRoleEntity;
import uk.gov.hmcts.ccd.definition.store.utils.CaseFieldBuilder;
import uk.gov.hmcts.ccd.definition.store.utils.CaseTypeBuilder;
import uk.gov.hmcts.ccd.definition.store.utils.FieldTypeBuilder;
import uk.gov.hmcts.reform.amlib.DefaultRoleSetupImportService;
import uk.gov.hmcts.reform.amlib.enums.AccessType;
import uk.gov.hmcts.reform.amlib.enums.RoleType;

import static com.google.common.collect.Lists.newArrayList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.ccd.definition.store.utils.CaseFieldBuilder.newField;
import static uk.gov.hmcts.ccd.definition.store.utils.CaseFieldBuilder.newTextField;
import static uk.gov.hmcts.ccd.definition.store.utils.FieldTypeBuilder.newType;
import static uk.gov.hmcts.ccd.definition.store.utils.FieldTypeBuilder.textFieldType;
import static uk.gov.hmcts.reform.amlib.enums.SecurityClassification.valueOf;

@RunWith(SpringRunner.class)
@BootstrapWith(SpringBootTestContextBootstrapper.class)
@ContextConfiguration(classes = AccessManagementExportConfiguration.class, initializers = ConfigFileApplicationContextInitializer.class)
public class AccessManagementIT {

    @Autowired
    private AccessManagementDefinitionImportListener listener;

    @Autowired
    private ApplicationEventPublisher publisher;

    @Autowired
    private AccessManagementExportConfiguration config;

    @MockBean
    private DefaultRoleSetupImportService defaultRoleSetupImportService;

    @Test
    public void testListeningToDefinitionImportedEvent() {
        CaseTypeEntity caseType = createCaseType();

        publisher.publishEvent(new DefinitionImportedEvent(newArrayList(caseType)));

        verify(defaultRoleSetupImportService).addService(anyString(), anyString());
        verify(defaultRoleSetupImportService).addResourceDefinition(anyString(), anyString(), anyString());

    }

    @Test
    public void testListeningToRoleImportedEvent() {
        UserRoleEntity role = createRole();

        publisher.publishEvent(new RoleImportedEvent(role));
        
        verify(defaultRoleSetupImportService).addRole(role.getName(), RoleType.RESOURCE,
            valueOf(role.getSecurityClassification().name()), AccessType.ROLE_BASED);
    }

    private UserRoleEntity createRole() {
        UserRoleEntity role = new UserRoleEntity();
        role.setName("name");
        role.setSecurityClassification(SecurityClassification.valueOf("PUBLIC"));
        return role;
    }

    private CaseTypeEntity createCaseType() {
        CaseTypeBuilder caseTypeBuilder = new CaseTypeBuilder().withJurisdiction("jur")
            .withReference("caseTypeA").withJurisdictionDescription("caseTypeA description");
        CaseFieldEntity baseTypeField = newTextField("forename").build();
        CaseFieldEntity complexOfComplex = newComplexFieldOfComplex();
        CaseFieldEntity complexOfCollection = newComplexFieldOfCollection();
        CaseFieldEntity collectionOfBaseType = newCollectionFieldOfBaseType();

        return caseTypeBuilder.addField(baseTypeField)
            .addField(complexOfComplex)
            .addField(complexOfCollection)
            .addField(collectionOfBaseType).build();
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
