package uk.gov.hmcts.ccd.definition.store.domain.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import org.mapstruct.Mapping;
import uk.gov.hmcts.ccd.definition.store.domain.service.casetype.mapper.FieldTypeListItemMapper;
import uk.gov.hmcts.ccd.definition.store.repository.entity.*;
import uk.gov.hmcts.ccd.definition.store.repository.model.*;

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
            + ": eventEntity.getPreStates().stream().map(StateEntity::getReference).collect(java.util.stream.Collectors.toList()))",
        target = "preStates"
    )
    @Mapping(
        expression = "java(eventEntity.getPostState() == null ? \"*\" : eventEntity.getPostState().getReference())",
        target = "postState"
    )
    CaseEvent map(EventEntity eventEntity);

    @Mapping(
        expression = "java(eventLiteEntity.isCanCreate() ? java.util.Collections.emptyList() "
            + ": eventLiteEntity.getPreStates().isEmpty() ? java.util.Arrays.asList(\"*\") "
            + ": eventLiteEntity.getPreStates().stream().map(StateEntity::getReference).collect(java.util.stream.Collectors.toList()))",
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
    SearchInputField map(SearchInputCaseFieldEntity searchInputCaseFieldEntity);

    @Mapping(source = "searchResultCaseFieldEntity.caseField.reference", target = "caseFieldId")
    @Mapping(source = "searchResultCaseFieldEntity.caseFieldElementPath", target = "caseFieldElementPath")
    @Mapping(source = "searchResultCaseFieldEntity.userRole.reference", target = "role")
    @Mapping(expression = "java(searchResultCaseFieldEntity.getCaseField().isMetadataField())", target = "metadata")
    SearchResultsField map(SearchResultCaseFieldEntity searchResultCaseFieldEntity);

    @Mapping(source = "workBasketInputCaseFieldEntity.caseField.reference", target = "caseFieldId")
    @Mapping(source = "workBasketInputCaseFieldEntity.caseFieldElementPath", target = "caseFieldElementPath")
    @Mapping(source = "workBasketInputCaseFieldEntity.userRole.reference", target = "role")
    WorkbasketInputField map(WorkBasketInputCaseFieldEntity workBasketInputCaseFieldEntity);

    @Mapping(source = "workBasketCaseFieldEntity.caseField.reference", target = "caseFieldId")
    @Mapping(source = "workBasketCaseFieldEntity.caseFieldElementPath", target = "caseFieldElementPath")
    @Mapping(source = "workBasketCaseFieldEntity.userRole.reference", target = "role")
    @Mapping(expression = "java(workBasketCaseFieldEntity.getCaseField().isMetadataField())", target = "metadata")
    WorkBasketResultField map(WorkBasketCaseFieldEntity workBasketCaseFieldEntity);

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
                                                                    complexTypeEntity.getShowCondition()))
                .collect(Collectors.toList());
        }
    }

    @Mapping(source = "searchAliasFieldEntity.reference", target = "id")
    @Mapping(source = "searchAliasFieldEntity.caseType.reference", target = "caseTypeId")
    SearchAliasField map(SearchAliasFieldEntity searchAliasFieldEntity);

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
            if (baseFieldTypeEntity == null && FieldTypeEntity.isComplexField(fieldTypeEntity.getReference())) {
                return getCaseFields(fieldTypeEntity.getComplexFields());
            } else if (baseFieldTypeEntity != null &&
                       FieldTypeEntity.isComplexField(baseFieldTypeEntity.getReference())) {
                return getCaseFields(fieldTypeEntity.getComplexFields());
            }
            return new ArrayList<>();
        }

        private static List<CaseField> getCaseFields(List<ComplexFieldEntity> complexFieldEntityList) {
            List<CaseField> caseFields = new ArrayList<>();
            for (ComplexFieldEntity complexFieldEntity : complexFieldEntityList) {
                caseFields.add(EntityToResponseDTOMapper.INSTANCE.map(complexFieldEntity));
            }
            return caseFields;
        }
    }
}
