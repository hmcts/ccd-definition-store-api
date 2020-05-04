package uk.gov.hmcts.ccd.definition.store.excel.parser;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.excel.util.ReferenceUtils;
import uk.gov.hmcts.ccd.definition.store.repository.FieldTypeRepository;
import uk.gov.hmcts.ccd.definition.store.repository.VersionedDefinitionRepositoryDecorator;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.DataFieldType;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldTypeListItemEntity;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import static uk.gov.hmcts.ccd.definition.store.domain.service.metadata.MetadataField.STATE;
import static uk.gov.hmcts.ccd.definition.store.excel.parser.StateMetadataCaseFieldEntityFactory.QUALIFIER;
import static uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils.BASE_FIXED_LIST;

@Component
@Qualifier(QUALIFIER)
public class StateMetadataCaseFieldEntityFactory implements MetadataCaseFieldEntityFactory {

    public static final String QUALIFIER = "StateMetadataCaseFieldEntityFactory";

    private final VersionedDefinitionRepositoryDecorator<FieldTypeEntity, Integer> versionedRepository;

    @Autowired
    public StateMetadataCaseFieldEntityFactory(FieldTypeRepository repository) {
        this.versionedRepository = new VersionedDefinitionRepositoryDecorator<>(repository);
    }

    @Override
    public CaseFieldEntity createCaseFieldEntity(ParseContext parseContext, CaseTypeEntity caseType) {
        return createCaseFieldEntity(createFixedListType(parseContext, caseType), caseType);
    }

    private CaseFieldEntity createCaseFieldEntity(FieldTypeEntity fixedListType, CaseTypeEntity caseType) {
        final CaseFieldEntity caseField = new CaseFieldEntity();

        caseField.setReference(STATE.getReference());
        caseField.setFieldType(fixedListType);
        caseField.setSecurityClassification(caseType.getSecurityClassification());
        caseField.setLabel(STATE.getLabel());
        caseField.setHidden(false);
        caseField.setLiveFrom(LocalDate.now());
        caseField.setDataFieldType(DataFieldType.METADATA);

        return caseField;
    }

    private FieldTypeEntity createFixedListType(ParseContext parseContext, CaseTypeEntity caseType) {
        // Get base field type from context
        FieldTypeEntity fixedListBaseType = parseContext.getBaseType(BASE_FIXED_LIST)
            .orElseThrow(() -> new SpreadsheetParsingException("No base type found for fixed list"));

        // Create fixed list type
        FieldTypeEntity fixedListType = new FieldTypeEntity();
        fixedListType.setJurisdiction(caseType.getJurisdiction());
        fixedListType.setBaseFieldType(fixedListBaseType);
        fixedListType.setReference(ReferenceUtils.listReference(BASE_FIXED_LIST, caseType.getReference() + STATE.getReference()));
        fixedListType.setJurisdiction(parseContext.getJurisdiction());
        fixedListType.addListItems(createStateListItems(caseType));

        return versionedRepository.save(fixedListType);
    }

    private List<FieldTypeListItemEntity> createStateListItems(CaseTypeEntity caseType) {
        return caseType.getStates().stream().map(state -> {
            FieldTypeListItemEntity listItem = new FieldTypeListItemEntity();
            listItem.setValue(state.getReference());
            listItem.setLabel(state.getName());
            return listItem;
        }).collect(Collectors.toList());
    }
}
