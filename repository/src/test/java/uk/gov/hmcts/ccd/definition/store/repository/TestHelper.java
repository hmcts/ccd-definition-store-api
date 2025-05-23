package uk.gov.hmcts.ccd.definition.store.repository;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.repository.entity.AccessProfileEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeLiteEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.DefinitionEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.DefinitionStatus;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventCaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.JurisdictionEntity;

import static uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeLiteEntity.toCaseTypeLiteEntity;

@Component
public class TestHelper {

    private final VersionedDefinitionRepositoryDecorator<JurisdictionEntity, Integer> versionedJurisdictionRepository;
    private final VersionedDefinitionRepositoryDecorator<FieldTypeEntity, Integer> versionedTypeRepository;
    private final VersionedDefinitionRepositoryDecorator<CaseTypeEntity, Integer> versionedCaseTypeRepository;
    private final AccessProfileRepository accessProfileRepository;

    @Autowired
    public TestHelper(JurisdictionRepository jurisdictionRepository,
                      FieldTypeRepository fieldTypeRepository,
                      AccessProfileRepository accessProfileRepository,
                      CaseTypeRepository caseTypeRepository) {
        versionedJurisdictionRepository = new VersionedDefinitionRepositoryDecorator<>(jurisdictionRepository);
        versionedCaseTypeRepository = new VersionedDefinitionRepositoryDecorator<>(caseTypeRepository);
        versionedTypeRepository = new VersionedDefinitionRepositoryDecorator<>(fieldTypeRepository);
        this.accessProfileRepository = accessProfileRepository;
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

    public CaseTypeLiteEntity createCaseTypeLiteEntity(String reference, String name) {
        CaseTypeEntity caseType = createCaseType(reference, name);
        return toCaseTypeLiteEntity(caseType);
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

    public AccessProfileEntity createAccessProfile(final String reference,
                                                   final String name,
                                                   final SecurityClassification sc) {
        final AccessProfileEntity entity = new AccessProfileEntity();
        entity.setReference(reference);
        entity.setName(name);
        entity.setSecurityClassification(sc);
        return accessProfileRepository.save(entity);
    }

    public EventCaseFieldEntity createEventCaseField(CaseFieldEntity caseField,
                                                     DisplayContext displayContext,
                                                     String showCondition,
                                                     Boolean ssco,
                                                     String defaultValue,
                                                     Boolean nullifyByDefault) {
        EventCaseFieldEntity ecf = new EventCaseFieldEntity();
        ecf.setCaseField(caseField);
        ecf.setDisplayContext(displayContext);
        ecf.setShowCondition(showCondition);
        ecf.setShowSummaryChangeOption(ssco);
        ecf.setPublish(false);
        ecf.setDefaultValue(defaultValue);
        ecf.setNullifyByDefault(nullifyByDefault);
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
}
