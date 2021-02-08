package uk.gov.hmcts.ccd.definition.store.repository;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeACLEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.DefinitionEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.DefinitionStatus;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventCaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.JurisdictionEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.UserRoleEntity;

import static uk.gov.hmcts.ccd.definition.store.repository.SecurityClassification.PUBLIC;

@Component
public class TestHelper {

    private final VersionedDefinitionRepositoryDecorator<JurisdictionEntity, Integer> versionedJurisdictionRepository;
    private final VersionedDefinitionRepositoryDecorator<FieldTypeEntity, Integer> versionedTypeRepository;
    private final VersionedDefinitionRepositoryDecorator<CaseTypeEntity, Integer> versionedCaseTypeRepository;
    private final UserRoleRepository userRoleRepository;

    @Autowired
    public TestHelper(JurisdictionRepository jurisdictionRepository,
                      FieldTypeRepository fieldTypeRepository,
                      UserRoleRepository userRoleRepository,
                      CaseTypeRepository caseTypeRepository) {
        versionedJurisdictionRepository = new VersionedDefinitionRepositoryDecorator<>(jurisdictionRepository);
        versionedCaseTypeRepository = new VersionedDefinitionRepositoryDecorator<>(caseTypeRepository);
        versionedTypeRepository = new VersionedDefinitionRepositoryDecorator<>(fieldTypeRepository);
        this.userRoleRepository = userRoleRepository;
    }

    public JurisdictionEntity createJurisdiction() {
        return createJurisdiction("jurisdiction", "name", "desc");
    }

    public JurisdictionEntity createJurisdiction(String reference, String name, String desc) {
        final JurisdictionEntity jurisdiction = new JurisdictionEntity();
        jurisdiction.setReference(reference);
        jurisdiction.setName(name);
        jurisdiction.setDescription(desc);
        return versionedJurisdictionRepository.save(jurisdiction);
    }

    public CaseTypeEntity createCaseType(String reference, String name) {
        JurisdictionEntity jurisdictionEntity = createJurisdiction();
        final CaseTypeEntity caseType = new CaseTypeEntity();
        caseType.setReference(reference);
        caseType.setName(name);
        caseType.setVersion(1);
        caseType.setDescription(name);
        caseType.setJurisdiction(jurisdictionEntity);
        caseType.setSecurityClassification(SecurityClassification.PUBLIC);
        return this.versionedCaseTypeRepository.save(caseType);
    }

    public FieldTypeEntity createType(JurisdictionEntity jurisdiction) {
        final FieldTypeEntity fieldType = new FieldTypeEntity();
        fieldType.setReference("SomeType");
        fieldType.setMinimum("2");
        fieldType.setMaximum("4");
        fieldType.setJurisdiction(jurisdiction);
        return versionedTypeRepository.save(fieldType);
    }

    public CaseFieldEntity buildCaseField(final String reference,
                                          final FieldTypeEntity fieldType,
                                          final String label,
                                          final Boolean hidden) {
        final CaseFieldEntity caseFieldEntity = new CaseFieldEntity();
        caseFieldEntity.setReference(reference);
        caseFieldEntity.setFieldType(fieldType);
        caseFieldEntity.setLabel(label);
        caseFieldEntity.setHidden(hidden);
        caseFieldEntity.setSecurityClassification(SecurityClassification.PUBLIC);
        return caseFieldEntity;
    }

    public UserRoleEntity createUserRole(final String reference,
                                         final String name,
                                         final SecurityClassification sc) {
        final UserRoleEntity entity = new UserRoleEntity();
        entity.setReference(reference);
        entity.setName(name);
        entity.setSecurityClassification(sc);
        return userRoleRepository.save(entity);
    }

    public EventCaseFieldEntity createEventCaseField(CaseFieldEntity caseField,
                                                     DisplayContext displayContext,
                                                     String showCondition,
                                                     Boolean ssco) {
        EventCaseFieldEntity ecf = new EventCaseFieldEntity();
        ecf.setCaseField(caseField);
        ecf.setDisplayContext(displayContext);
        ecf.setShowCondition(showCondition);
        ecf.setShowSummaryChangeOption(ssco);
        ecf.setPublish(false);
        return ecf;
    }

    public DefinitionEntity buildDefinition(final JurisdictionEntity jurisdiction,
                                            final String description,
                                            final DefinitionStatus status) throws IOException {
        final DefinitionEntity definitionEntity = new DefinitionEntity();
        definitionEntity.setJurisdiction(jurisdiction);
        definitionEntity.setDescription(description);
        final ObjectMapper mapper = new ObjectMapper();
        final JsonNode data = mapper.readTree("{\"FieldX\": \"ValueX\", \"FieldZ\": []}");
        definitionEntity.setData(data);
        definitionEntity.setAuthor("lrmgc2gp7g@example.com");
        definitionEntity.setDeleted(false);
        definitionEntity.setStatus(status);
        return definitionEntity;
    }

    public DefinitionEntity buildDefinition(final JurisdictionEntity jurisdiction,
                                            final String description) throws IOException {
        final DefinitionEntity definitionEntity = new DefinitionEntity();
        definitionEntity.setJurisdiction(jurisdiction);
        definitionEntity.setDescription(description);
        final ObjectMapper mapper = new ObjectMapper();
        final JsonNode data = mapper.readTree("{\"Field1\": \"Value1\", \"Field2\": []}");
        definitionEntity.setData(data);
        definitionEntity.setAuthor("ccd@hmcts");
        definitionEntity.setDeleted(false);
        return definitionEntity;
    }

    public CaseTypeEntity createCaseTypeEntity(String reference,
                                               String name,
                                               Integer version,
                                               JurisdictionEntity jurisdiction) {
        final CaseTypeEntity caseType = new CaseTypeEntity();
        caseType.setReference(reference);
        caseType.setName(name);
        caseType.setVersion(version);
        caseType.setJurisdiction(jurisdiction);
        caseType.setSecurityClassification(PUBLIC);
        return caseType;
    }

    public CaseTypeEntity createCaseTypeEntityWithCaseTypeACL(String reference,
                                                               String name,
                                                               Integer version,
                                                               JurisdictionEntity jurisdiction,
                                                               Collection<CaseTypeACLEntity> caseTypeACLList) {
        final CaseTypeEntity caseType = new CaseTypeEntity();
        caseType.setReference(reference);
        caseType.setName(name);
        caseType.setVersion(version);
        caseType.setJurisdiction(jurisdiction);
        caseType.setSecurityClassification(PUBLIC);
        caseType.addCaseTypeACLEntities(caseTypeACLList);
        return caseType;
    }

    public Collection<CaseTypeACLEntity> createCaseTypeACL() {
        CaseTypeACLEntity caseTypeACLWithCreateOnly = caseTypeACLWithUserRoleEntity(
            "role-with-create-only", true, false, false, false,
            "User Role 1", "User Role 1", SecurityClassification.RESTRICTED);
        CaseTypeACLEntity caseTypeACLWithReadOnly = caseTypeACLWithUserRoleEntity(
            "role-with-read-only", false, true, false, false,
            "User Role 2", "User Role 2", SecurityClassification.PRIVATE);
        CaseTypeACLEntity caseTypeACLWithUpdateOnly = caseTypeACLWithUserRoleEntity(
            "role-with-update-only", false, false, true, false,
            "User Role 3", "User Role 3", SecurityClassification.RESTRICTED);
        CaseTypeACLEntity caseTypeACLWithDeleteOnly = caseTypeACLWithUserRoleEntity(
            "role-with-delete-only", false, false, false, true,
            "User Role 4", "User Role 4", SecurityClassification.PUBLIC);
        return (Arrays.asList(
            caseTypeACLWithCreateOnly, caseTypeACLWithReadOnly, caseTypeACLWithUpdateOnly, caseTypeACLWithDeleteOnly));
    }

    public Collection<CaseTypeACLEntity> createCaseTypeACLWithFullAccess() {
        CaseTypeACLEntity caseTypeACLWithFullAccess = caseTypeACLWithUserRoleEntity(
            "role-with-full-access", true, true, true, true,
            "User Role Full Access", "User Role Full Access", SecurityClassification.PUBLIC);
        return (Collections.singletonList(caseTypeACLWithFullAccess));
    }

    public CaseTypeACLEntity caseTypeACLWithUserRoleEntity(String reference,
                                                            Boolean create,
                                                            Boolean read,
                                                            Boolean update,
                                                            Boolean delete,
                                                            String userRoleReference,
                                                            String userRoleName,
                                                            SecurityClassification sc) {
        CaseTypeACLEntity caseTypeACLEntity = new CaseTypeACLEntity();
        caseTypeACLEntity.setUserRole(createUserRoleEntity(userRoleReference, userRoleName, sc));
        caseTypeACLEntity.setCreate(create);
        caseTypeACLEntity.setRead(read);
        caseTypeACLEntity.setUpdate(update);
        caseTypeACLEntity.setDelete(delete);
        return caseTypeACLEntity;
    }

    private UserRoleEntity createUserRoleEntity(String reference, String userRoleName, SecurityClassification sc) {
        return createUserRole(reference, userRoleName, sc);
    }
}
