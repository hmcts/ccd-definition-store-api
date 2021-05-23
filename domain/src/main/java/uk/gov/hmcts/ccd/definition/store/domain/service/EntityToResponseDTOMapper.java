package uk.gov.hmcts.ccd.definition.store.domain.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import uk.gov.hmcts.ccd.definition.store.domain.service.casetype.mapper.FieldTypeListItemMapper;
import uk.gov.hmcts.ccd.definition.store.repository.entity.Authorisation;
import uk.gov.hmcts.ccd.definition.store.repository.entity.BannerEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseRoleEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeLiteEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.ChallengeQuestionTabEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.ComplexFieldACLEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.ComplexFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.DisplayGroupCaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.DisplayGroupEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventCaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventComplexTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventLiteEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.EventPostStateEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldTypeListItemEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.JurisdictionEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.JurisdictionUiConfigEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.RoleToAccessProfilesEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.SearchAliasFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.SearchCasesResultFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.SearchInputCaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.SearchResultCaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.StateEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.WorkBasketCaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.WorkBasketInputCaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.model.AccessControlList;
import uk.gov.hmcts.ccd.definition.store.repository.model.Banner;
import uk.gov.hmcts.ccd.definition.store.repository.model.CaseEvent;
import uk.gov.hmcts.ccd.definition.store.repository.model.CaseEventField;
import uk.gov.hmcts.ccd.definition.store.repository.model.CaseEventFieldComplex;
import uk.gov.hmcts.ccd.definition.store.repository.model.CaseEventLite;
import uk.gov.hmcts.ccd.definition.store.repository.model.CaseField;
import uk.gov.hmcts.ccd.definition.store.repository.model.CaseRole;
import uk.gov.hmcts.ccd.definition.store.repository.model.CaseState;
import uk.gov.hmcts.ccd.definition.store.repository.model.CaseType;
import uk.gov.hmcts.ccd.definition.store.repository.model.CaseTypeLite;
import uk.gov.hmcts.ccd.definition.store.repository.model.CaseTypeTab;
import uk.gov.hmcts.ccd.definition.store.repository.model.CaseTypeTabField;
import uk.gov.hmcts.ccd.definition.store.repository.model.ChallengeQuestion;
import uk.gov.hmcts.ccd.definition.store.repository.model.ComplexACL;
import uk.gov.hmcts.ccd.definition.store.repository.model.EventPostState;
import uk.gov.hmcts.ccd.definition.store.repository.model.FieldType;
import uk.gov.hmcts.ccd.definition.store.repository.model.FixedListItem;
import uk.gov.hmcts.ccd.definition.store.repository.model.Jurisdiction;
import uk.gov.hmcts.ccd.definition.store.repository.model.JurisdictionUiConfig;
import uk.gov.hmcts.ccd.definition.store.repository.model.RoleToAccessProfiles;
import uk.gov.hmcts.ccd.definition.store.repository.model.SearchAliasField;
import uk.gov.hmcts.ccd.definition.store.repository.model.SearchCasesResultField;
import uk.gov.hmcts.ccd.definition.store.repository.model.SearchInputField;
import uk.gov.hmcts.ccd.definition.store.repository.model.SearchResultsField;
import uk.gov.hmcts.ccd.definition.store.repository.model.WorkBasketResultField;
import uk.gov.hmcts.ccd.definition.store.repository.model.WorkbasketInputField;

import static uk.gov.hmcts.ccd.definition.store.repository.model.Comparators.NULLS_LAST_ORDER_COMPARATOR;

@SuppressWarnings("checkstyle:LineLength")
@Mapper(componentModel = "spring")
public interface EntityToResponseDTOMapper {
    EntityToResponseDTOMapper INSTANCE = Mappers.getMapper(EntityToResponseDTOMapper.class);

    FieldTypeListItemMapper fieldTypeListItemMapper = new FieldTypeListItemMapper();

    @Mapping(source = "caseTypeEntity.reference", target = "id")
    @Mapping(source = "caseTypeEntity.version", target = "version.number")
    @Mapping(source = "caseTypeEntity.liveFrom", target = "version.liveFrom")
    @Mapping(source = "caseTypeEntity.liveTo", target = "version.liveUntil")
    @Mapping(
        expression = "java("
            + "           uk.gov.hmcts.ccd.definition.store.domain.service.EntityToResponseDTOMapper.AuthorisationToAccessControlListMapper.map("
            + "               caseTypeEntity.getCaseTypeACLEntities()"
            + "           )"
            + "       )",
        target = "acls"
    )
    @Mapping(source = "caseTypeEntity.printWebhook.url", target = "printableDocumentsUrl")
    CaseType map(CaseTypeEntity caseTypeEntity);

    @Mapping(source = "caseTypeLiteEntity.reference", target = "id")
    @Mapping(
        expression = "java("
            + "           uk.gov.hmcts.ccd.definition.store.domain.service.EntityToResponseDTOMapper.AuthorisationToAccessControlListMapper.map("
            + "               caseTypeLiteEntity.getCaseTypeLiteACLEntities()"
            + "           )"
            + "       )",
        target = "acls"
    )
    CaseTypeLite map(CaseTypeLiteEntity caseTypeLiteEntity);

    @Mapping(source = "eventEntity.reference", target = "id")
    @Mapping(source = "eventEntity.eventCaseFields", target = "caseFields")
    @Mapping(source = "eventEntity.webhookStart.url", target = "callBackURLAboutToStartEvent")
    @Mapping(source = "eventEntity.webhookStart.timeouts", target = "retriesTimeoutAboutToStartEvent")
    @Mapping(source = "eventEntity.webhookPreSubmit.url", target = "callBackURLAboutToSubmitEvent")
    @Mapping(source = "eventEntity.webhookPreSubmit.timeouts", target = "retriesTimeoutURLAboutToSubmitEvent")
    @Mapping(source = "eventEntity.webhookPostSubmit.url", target = "callBackURLSubmittedEvent")
    @Mapping(source = "eventEntity.webhookPostSubmit.timeouts", target = "retriesTimeoutURLSubmittedEvent")
    @Mapping(
        expression = "java("
            + "           uk.gov.hmcts.ccd.definition.store.domain.service.EntityToResponseDTOMapper.AuthorisationToAccessControlListMapper.map("
            + "               eventEntity.getEventACLEntities()"
            + "           )"
            + "       )",
        target = "acls"
    )
    @Mapping(
        expression = "java(eventEntity.isCanCreate() ? java.util.Collections.emptyList() "
            + ": eventEntity.getPreStates().isEmpty() ? java.util.Arrays.asList(\"*\") "
            + ": eventEntity.getPreStates().stream().map(StateEntity::getReference).collect("
            + "java.util.stream.Collectors.toList()))",
        target = "preStates"
    )
    CaseEvent map(EventEntity eventEntity);

    EventPostState map(EventPostStateEntity eventPostStateEntity);

    @Mapping(
        expression = "java(eventLiteEntity.isCanCreate() ? java.util.Collections.emptyList() "
            + ": eventLiteEntity.getPreStates().isEmpty() ? java.util.Arrays.asList(\"*\") "
            + ": eventLiteEntity.getPreStates().stream().map(StateEntity::getReference).collect("
            + "java.util.stream.Collectors.toList()))",
        target = "preStates"
    )
    @Mapping(source = "eventLiteEntity.reference", target = "id")
    @Mapping(
        expression = "java("
            + "           uk.gov.hmcts.ccd.definition.store.domain.service.EntityToResponseDTOMapper.AuthorisationToAccessControlListMapper.map("
            + "               eventLiteEntity.getEventLiteACLs()"
            + "           )"
            + "       )",
        target = "acls"
    )
    CaseEventLite map(EventLiteEntity eventLiteEntity);

    @Mapping(source = "jurisdictionEntity.reference", target = "id")
    @Mapping(source = "jurisdictionEntity.liveTo", target = "liveUntil")
    Jurisdiction map(JurisdictionEntity jurisdictionEntity);

    @Mapping(expression = "java("
        + "           uk.gov.hmcts.ccd.definition.store.domain.service.EntityToResponseDTOMapper.AuthorisationToAccessControlListMapper.map("
        + "               stateEntity.getStateACLEntities()"
        + "           )"
        + "       )",
        target = "acls")
    @Mapping(source = "stateEntity.reference", target = "id")
    CaseState map(StateEntity stateEntity);

    @Mapping(source = "caseFieldEntity.reference", target = "id")
    @Mapping(source = "caseFieldEntity.caseType.reference", target = "caseTypeId")
    @Mapping(source = "caseFieldEntity.hint", target = "hintText")
    @Mapping(source = "caseFieldEntity.liveTo", target = "liveUntil")
    @Mapping(expression = "java("
        + "           uk.gov.hmcts.ccd.definition.store.domain.service.EntityToResponseDTOMapper.AuthorisationToAccessControlListMapper.map("
        + "               caseFieldEntity.getCaseFieldACLEntities()"
        + "           )"
        + "       )",
        target = "acls")
    @Mapping(expression = "java("
        + "           uk.gov.hmcts.ccd.definition.store.domain.service.EntityToResponseDTOMapper.AuthorisationToAccessControlListMapper.mapComplex("
        + "               caseFieldEntity.getComplexFieldACLEntities()"
        + "           )"
        + "       )",
        target = "complexACLs")
    @Mapping(expression = "java(caseFieldEntity.isMetadataField())", target = "metadata")
    CaseField map(CaseFieldEntity caseFieldEntity);

    @Mapping(source = "fieldTypeEntity.reference", target = "id")
    @Mapping(
        expression = "java("
            + "           uk.gov.hmcts.ccd.definition.store.domain.service.EntityToResponseDTOMapper.FixedListMapper.map("
            + "               fieldTypeEntity"
            + "           )"
            + "       )",
        target = "fixedListItems"
    )
    @Mapping(
        expression = "java("
            + "           uk.gov.hmcts.ccd.definition.store.domain.service.EntityToResponseDTOMapper.ComplexFieldsMapper.map("
            + "               fieldTypeEntity))",
        target = "complexFields"
    )
    @Mapping(source = "fieldTypeEntity.minimum", target = "min")
    @Mapping(source = "fieldTypeEntity.maximum", target = "max")
    @Mapping(expression = "java(fieldTypeEntity.getBaseFieldType() == null"
        + " ? fieldTypeEntity.getReference() : fieldTypeEntity.getBaseFieldType().getReference())",
        target = "type")
    FieldType map(FieldTypeEntity fieldTypeEntity);

    @Mapping(source = "caseRoleEntity.reference", target = "id")
    CaseRole map(CaseRoleEntity caseRoleEntity);

    @Mapping(source = "fieldTypeListItemEntity.value", target = "code")
    FixedListItem map(FieldTypeListItemEntity fieldTypeListItemEntity);

    @Mapping(source = "eventCaseFieldEntity.caseField.reference", target = "caseFieldId")
    @Mapping(expression = "java("
        + "       uk.gov.hmcts.ccd.definition.store.domain.service.EntityToResponseDTOMapper.EventComplexTypeEntityToCaseEventFieldComplexListMapper.map("
        + "           eventCaseFieldEntity.getEventComplexTypes()"
        + "       )"
        + "   )",
        target = "caseEventFieldComplex"
    )
    CaseEventField map(EventCaseFieldEntity eventCaseFieldEntity);

    List<CaseEventFieldComplex> map(List<EventComplexTypeEntity> eventComplexTypeEntities);

    CaseEventFieldComplex map(EventComplexTypeEntity eventComplexTypeEntity);

    @Mapping(source = "complexFieldEntity.reference", target = "id")
    @Mapping(source = "complexFieldEntity.hint", target = "hintText")
    CaseField map(ComplexFieldEntity complexFieldEntity);

    @Mapping(source = "displayGroupEntity.reference", target = "id")
    @Mapping(source = "displayGroupEntity.showCondition", target = "showCondition")
    @Mapping(source = "displayGroupEntity.displayGroupCaseFields", target = "tabFields")
    @Mapping(source = "displayGroupEntity.userRole.reference", target = "role")
    CaseTypeTab map(DisplayGroupEntity displayGroupEntity);

    @Mapping(source = "displayGroupCaseFieldEntity.showCondition", target = "showCondition")
    CaseTypeTabField map(DisplayGroupCaseFieldEntity displayGroupCaseFieldEntity);

    @Mapping(source = "searchInputCaseFieldEntity.caseField.reference", target = "caseFieldId")
    @Mapping(source = "searchInputCaseFieldEntity.userRole.reference", target = "role")
    @Mapping(source = "searchInputCaseFieldEntity.caseFieldElementPath", target = "caseFieldElementPath")
    @Mapping(source = "searchInputCaseFieldEntity.showCondition", target = "showCondition")
    SearchInputField map(SearchInputCaseFieldEntity searchInputCaseFieldEntity);

    @Mapping(source = "searchResultCaseFieldEntity.caseField.reference", target = "caseFieldId")
    @Mapping(source = "searchResultCaseFieldEntity.caseFieldElementPath", target = "caseFieldElementPath")
    @Mapping(source = "searchResultCaseFieldEntity.userRole.reference", target = "role")
    @Mapping(expression = "java(searchResultCaseFieldEntity.getCaseField().isMetadataField())", target = "metadata")
    SearchResultsField map(SearchResultCaseFieldEntity searchResultCaseFieldEntity);

    @Mapping(source = "workBasketInputCaseFieldEntity.caseField.reference", target = "caseFieldId")
    @Mapping(source = "workBasketInputCaseFieldEntity.caseFieldElementPath", target = "caseFieldElementPath")
    @Mapping(source = "workBasketInputCaseFieldEntity.userRole.reference", target = "role")
    @Mapping(source = "workBasketInputCaseFieldEntity.showCondition", target = "showCondition")
    WorkbasketInputField map(WorkBasketInputCaseFieldEntity workBasketInputCaseFieldEntity);

    @Mapping(source = "workBasketCaseFieldEntity.caseField.reference", target = "caseFieldId")
    @Mapping(source = "workBasketCaseFieldEntity.caseFieldElementPath", target = "caseFieldElementPath")
    @Mapping(source = "workBasketCaseFieldEntity.userRole.reference", target = "role")
    @Mapping(expression = "java(workBasketCaseFieldEntity.getCaseField().isMetadataField())", target = "metadata")
    WorkBasketResultField map(WorkBasketCaseFieldEntity workBasketCaseFieldEntity);

    @Mapping(source = "searchCasesResultFieldEntity.caseField.reference", target = "caseFieldId")
    @Mapping(source = "searchCasesResultFieldEntity.caseFieldElementPath", target = "caseFieldElementPath")
    @Mapping(source = "searchCasesResultFieldEntity.userRole.reference", target = "role")
    @Mapping(expression = "java(searchCasesResultFieldEntity.getCaseField().isMetadataField())", target = "metadata")
    SearchCasesResultField map(SearchCasesResultFieldEntity searchCasesResultFieldEntity);

    class EventComplexTypeEntityToCaseEventFieldComplexListMapper {

        private EventComplexTypeEntityToCaseEventFieldComplexListMapper() {
            // Default constructor
        }

        static List<CaseEventFieldComplex> map(List<? extends EventComplexTypeEntity> eventComplexTypeEntity) {
            return eventComplexTypeEntity.stream()
                .map(complexTypeEntity -> new CaseEventFieldComplex(complexTypeEntity.getReference(),
                                                                    complexTypeEntity.getHint(),
                                                                    complexTypeEntity.getLabel(),
                                                                    complexTypeEntity.getOrder(),
                                                                    complexTypeEntity.getDisplayContext(),
                                                                    complexTypeEntity.getShowCondition(),
                                                                    complexTypeEntity.getDefaultValue(),
                                                                    complexTypeEntity.getPublish(),
                                                                    complexTypeEntity.getPublishAs()
                ))
                .collect(Collectors.toList());
        }
    }

    @Mapping(source = "searchAliasFieldEntity.reference", target = "id")
    @Mapping(source = "searchAliasFieldEntity.caseType.reference", target = "caseTypeId")
    SearchAliasField map(SearchAliasFieldEntity searchAliasFieldEntity);

    Banner map(BannerEntity bannerEntity);

    @Mapping(source = "jurisdictionUiConfigEntity.jurisdiction.reference", target = "id")
    @Mapping(source = "jurisdictionUiConfigEntity.jurisdiction.name", target = "name")
    JurisdictionUiConfig map(JurisdictionUiConfigEntity jurisdictionUiConfigEntity);

    @Mapping(source = "caseType.reference", target = "caseTypeId")
    ChallengeQuestion map(ChallengeQuestionTabEntity challengeQuestionEntity);

    @Mapping(source = "caseType.reference", target = "caseTypeId")
    RoleToAccessProfiles map(RoleToAccessProfilesEntity roleToAccessProfilesEntity);

    // Would be conventional to use a Default method like
    // default AccessControlList map(Authorisation authorisation)
    // but this does not play nicely with Mockito v1
    class AuthorisationToAccessControlListMapper {

        private AuthorisationToAccessControlListMapper() {
            // Default constructor
        }

        static List<AccessControlList> map(List<? extends Authorisation> authorisation) {
            return authorisation.stream()
                .map(auth -> new AccessControlList(auth.getUserRole().getReference(),
                    auth.getCreate(),
                    auth.getRead(),
                    auth.getUpdate(),
                    auth.getDelete()))
                .collect(Collectors.toList());
        }

        static List<ComplexACL> mapComplex(List<ComplexFieldACLEntity> complexFieldACLEntities) {
            return complexFieldACLEntities.stream()
                .map(el -> new ComplexACL(el.getUserRole().getReference(),
                    el.getCreate(),
                    el.getRead(),
                    el.getUpdate(),
                    el.getDelete(),
                    el.getListElementCode()))
                .collect(Collectors.toList());
        }
    }

    class FixedListMapper {
        private FixedListMapper() {
            // Default constructor
        }

        static List<FixedListItem> map(FieldTypeEntity fieldTypeEntity) {
            FieldTypeEntity baseFieldTypeEntity = fieldTypeEntity.getBaseFieldType();
            if (baseFieldTypeEntity == null && FieldTypeEntity.isFixedList(fieldTypeEntity.getReference())) {
                return fieldTypeListItemMapper.entityToModel(fieldTypeEntity.getListItems());
            } else if (baseFieldTypeEntity != null && FieldTypeEntity.isFixedList(baseFieldTypeEntity.getReference())) {
                return fieldTypeListItemMapper.entityToModel(fieldTypeEntity.getListItems());
            }
            return new ArrayList<>();
        }
    }

    class ComplexFieldsMapper {
        private ComplexFieldsMapper() {
            // Default constructor
        }

        static List<CaseField> map(FieldTypeEntity fieldTypeEntity) {
            FieldTypeEntity baseFieldTypeEntity = fieldTypeEntity.getBaseFieldType();
            if (baseFieldTypeEntity == null && isComplexField(fieldTypeEntity.getReference())) {
                return getCaseFields(fieldTypeEntity.getComplexFields());
            } else if (baseFieldTypeEntity != null && isComplexField(baseFieldTypeEntity.getReference())) {
                return getCaseFields(fieldTypeEntity.getComplexFields());
            }
            return new ArrayList<>();
        }

        private static List<CaseField> getCaseFields(List<ComplexFieldEntity> complexFieldEntityList) {
            return complexFieldEntityList.stream()
                .map(complexFieldEntity -> EntityToResponseDTOMapper.INSTANCE.map(complexFieldEntity))
                .sorted(NULLS_LAST_ORDER_COMPARATOR)
                .collect(Collectors.toList());
        }

        private static boolean isComplexField(String reference) {
            return "Complex".equalsIgnoreCase(reference);
        }
    }
}
