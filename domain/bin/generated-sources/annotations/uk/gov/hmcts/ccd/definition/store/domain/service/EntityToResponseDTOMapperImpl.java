package uk.gov.hmcts.ccd.definition.store.domain.service;

import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.repository.entity.AccessProfileEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.AccessTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.AccessTypeRoleEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.BannerEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseRoleEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeLiteEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CategoryEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.ChallengeQuestionTabEntity;
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
import uk.gov.hmcts.ccd.definition.store.repository.entity.ReindexEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.RoleToAccessProfilesEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.SearchAliasFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.SearchCasesResultFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.SearchCriteriaEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.SearchInputCaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.SearchPartyEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.SearchResultCaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.ShellMappingEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.SortOrder;
import uk.gov.hmcts.ccd.definition.store.repository.entity.StateEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.WebhookEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.WorkBasketCaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.WorkBasketInputCaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.model.AccessTypeField;
import uk.gov.hmcts.ccd.definition.store.repository.model.AccessTypeRoleField;
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
import uk.gov.hmcts.ccd.definition.store.repository.model.Category;
import uk.gov.hmcts.ccd.definition.store.repository.model.ChallengeQuestion;
import uk.gov.hmcts.ccd.definition.store.repository.model.EventPostState;
import uk.gov.hmcts.ccd.definition.store.repository.model.FieldType;
import uk.gov.hmcts.ccd.definition.store.repository.model.FixedListItem;
import uk.gov.hmcts.ccd.definition.store.repository.model.Jurisdiction;
import uk.gov.hmcts.ccd.definition.store.repository.model.JurisdictionUiConfig;
import uk.gov.hmcts.ccd.definition.store.repository.model.ReindexTask;
import uk.gov.hmcts.ccd.definition.store.repository.model.RoleAssignment;
import uk.gov.hmcts.ccd.definition.store.repository.model.RoleToAccessProfiles;
import uk.gov.hmcts.ccd.definition.store.repository.model.SearchAliasField;
import uk.gov.hmcts.ccd.definition.store.repository.model.SearchCasesResultField;
import uk.gov.hmcts.ccd.definition.store.repository.model.SearchCriteria;
import uk.gov.hmcts.ccd.definition.store.repository.model.SearchInputField;
import uk.gov.hmcts.ccd.definition.store.repository.model.SearchParty;
import uk.gov.hmcts.ccd.definition.store.repository.model.SearchResultsField;
import uk.gov.hmcts.ccd.definition.store.repository.model.ShellMapping;
import uk.gov.hmcts.ccd.definition.store.repository.model.Version;
import uk.gov.hmcts.ccd.definition.store.repository.model.WorkBasketResultField;
import uk.gov.hmcts.ccd.definition.store.repository.model.WorkbasketInputField;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-10-27T13:27:46+0000",
    comments = "version: 1.6.3, compiler: Eclipse JDT (IDE) 3.46.200.v20261008-1115, environment: Java 25.0.4.1 (Eclipse Adoptium)"
)
@Component
public class EntityToResponseDTOMapperImpl implements EntityToResponseDTOMapper {

    @Override
    public CaseType map(CaseTypeEntity caseTypeEntity) {
        if ( caseTypeEntity == null ) {
            return null;
        }

        CaseType caseType = new CaseType();

        caseType.setVersion( caseTypeEntityToVersion( caseTypeEntity ) );
        caseType.setId( caseTypeEntity.getReference() );
        caseType.setCallbackGetCaseUrl( caseTypeEntityGetCaseWebhookUrl( caseTypeEntity ) );
        List<Integer> timeouts = caseTypeEntityGetCaseWebhookTimeouts( caseTypeEntity );
        List<Integer> list = timeouts;
        if ( list != null ) {
            caseType.setRetriesGetCaseUrl( new ArrayList<Integer>( list ) );
        }
        caseType.setPrintableDocumentsUrl( caseTypeEntityPrintWebhookUrl( caseTypeEntity ) );
        caseType.setDescription( caseTypeEntity.getDescription() );
        caseType.setName( caseTypeEntity.getName() );
        caseType.setJurisdiction( map( caseTypeEntity.getJurisdiction() ) );
        caseType.setEvents( eventEntityListToCaseEventList( caseTypeEntity.getEvents() ) );
        caseType.setStates( stateEntityListToCaseStateList( caseTypeEntity.getStates() ) );
        caseType.setCaseFields( caseFieldEntitySetToCaseFieldList( caseTypeEntity.getCaseFields() ) );
        caseType.setSecurityClassification( caseTypeEntity.getSecurityClassification() );
        caseType.setSearchAliasFields( searchAliasFieldEntityListToSearchAliasFieldList( caseTypeEntity.getSearchAliasFields() ) );
        if ( caseType.getRoleToAccessProfiles() != null ) {
            List<RoleToAccessProfiles> list5 = roleToAccessProfilesEntityListToRoleToAccessProfilesList( caseTypeEntity.getRoleToAccessProfiles() );
            if ( list5 != null ) {
                caseType.getRoleToAccessProfiles().addAll( list5 );
            }
        }
        if ( caseType.getSearchCriterias() != null ) {
            List<SearchCriteria> list6 = searchCriteriaEntityListToSearchCriteriaList( caseTypeEntity.getSearchCriterias() );
            if ( list6 != null ) {
                caseType.getSearchCriterias().addAll( list6 );
            }
        }
        if ( caseType.getSearchParties() != null ) {
            List<SearchParty> list7 = searchPartyEntityListToSearchPartyList( caseTypeEntity.getSearchParties() );
            if ( list7 != null ) {
                caseType.getSearchParties().addAll( list7 );
            }
        }
        if ( caseType.getCategories() != null ) {
            List<Category> list8 = categoryEntityListToCategoryList( caseTypeEntity.getCategories() );
            if ( list8 != null ) {
                caseType.getCategories().addAll( list8 );
            }
        }
        if ( caseType.getAccessTypes() != null ) {
            List<AccessTypeField> list9 = accessTypeEntityListToAccessTypeFieldList( caseTypeEntity.getAccessTypes() );
            if ( list9 != null ) {
                caseType.getAccessTypes().addAll( list9 );
            }
        }
        if ( caseType.getAccessTypeRoles() != null ) {
            List<AccessTypeRoleField> list10 = accessTypeRoleEntityListToAccessTypeRoleFieldList( caseTypeEntity.getAccessTypeRoles() );
            if ( list10 != null ) {
                caseType.getAccessTypeRoles().addAll( list10 );
            }
        }

        caseType.setAcls( uk.gov.hmcts.ccd.definition.store.domain.service.EntityToResponseDTOMapper.AuthorisationToAccessControlListMapper.map(               caseTypeEntity.getCaseTypeACLEntities()           ) );

        return caseType;
    }

    @Override
    public CaseTypeLite map(CaseTypeLiteEntity caseTypeLiteEntity) {
        if ( caseTypeLiteEntity == null ) {
            return null;
        }

        CaseTypeLite caseTypeLite = new CaseTypeLite();

        caseTypeLite.setId( caseTypeLiteEntity.getReference() );
        caseTypeLite.setDescription( caseTypeLiteEntity.getDescription() );
        caseTypeLite.setName( caseTypeLiteEntity.getName() );
        caseTypeLite.setStates( stateEntityListToCaseStateList( caseTypeLiteEntity.getStates() ) );
        caseTypeLite.setEvents( eventLiteEntityListToCaseEventLiteList( caseTypeLiteEntity.getEvents() ) );

        caseTypeLite.setAcls( uk.gov.hmcts.ccd.definition.store.domain.service.EntityToResponseDTOMapper.AuthorisationToAccessControlListMapper.map(               caseTypeLiteEntity.getCaseTypeLiteACLEntities()           ) );

        return caseTypeLite;
    }

    @Override
    public CaseEvent map(EventEntity eventEntity) {
        if ( eventEntity == null ) {
            return null;
        }

        CaseEvent caseEvent = new CaseEvent();

        caseEvent.setId( eventEntity.getReference() );
        caseEvent.setCaseFields( eventCaseFieldEntityListToCaseEventFieldList( eventEntity.getEventCaseFields() ) );
        caseEvent.setCallBackURLAboutToStartEvent( eventEntityWebhookStartUrl( eventEntity ) );
        List<Integer> timeouts = eventEntityWebhookStartTimeouts( eventEntity );
        List<Integer> list1 = timeouts;
        if ( list1 != null ) {
            caseEvent.setRetriesTimeoutAboutToStartEvent( new ArrayList<Integer>( list1 ) );
        }
        caseEvent.setCallBackURLAboutToSubmitEvent( eventEntityWebhookPreSubmitUrl( eventEntity ) );
        List<Integer> timeouts1 = eventEntityWebhookPreSubmitTimeouts( eventEntity );
        List<Integer> list2 = timeouts1;
        if ( list2 != null ) {
            caseEvent.setRetriesTimeoutURLAboutToSubmitEvent( new ArrayList<Integer>( list2 ) );
        }
        caseEvent.setCallBackURLSubmittedEvent( eventEntityWebhookPostSubmitUrl( eventEntity ) );
        List<Integer> timeouts2 = eventEntityWebhookPostSubmitTimeouts( eventEntity );
        List<Integer> list3 = timeouts2;
        if ( list3 != null ) {
            caseEvent.setRetriesTimeoutURLSubmittedEvent( new ArrayList<Integer>( list3 ) );
        }
        caseEvent.setName( eventEntity.getName() );
        caseEvent.setDescription( eventEntity.getDescription() );
        caseEvent.setOrder( eventEntity.getOrder() );
        caseEvent.setPostStates( eventPostStateEntityListToEventPostStateList( eventEntity.getPostStates() ) );
        caseEvent.setSecurityClassification( eventEntity.getSecurityClassification() );
        caseEvent.setShowSummary( eventEntity.getShowSummary() );
        caseEvent.setPublish( eventEntity.getPublish() );
        caseEvent.setTtlIncrement( eventEntity.getTtlIncrement() );
        caseEvent.setShowEventNotes( eventEntity.getShowEventNotes() );
        caseEvent.setEndButtonLabel( eventEntity.getEndButtonLabel() );
        caseEvent.setCanSaveDraft( eventEntity.getCanSaveDraft() );
        caseEvent.setEventEnablingCondition( eventEntity.getEventEnablingCondition() );

        caseEvent.setAcls( uk.gov.hmcts.ccd.definition.store.domain.service.EntityToResponseDTOMapper.AuthorisationToAccessControlListMapper.map(               eventEntity.getEventACLEntities()           ) );
        caseEvent.setPreStates( eventEntity.isCanCreate() ? java.util.Collections.emptyList() : eventEntity.getPreStates().isEmpty() ? java.util.Arrays.asList("*") : eventEntity.getPreStates().stream().map(StateEntity::getReference).collect(java.util.stream.Collectors.toList()) );

        return caseEvent;
    }

    @Override
    public EventPostState map(EventPostStateEntity eventPostStateEntity) {
        if ( eventPostStateEntity == null ) {
            return null;
        }

        EventPostState eventPostState = new EventPostState();

        eventPostState.setEnablingCondition( eventPostStateEntity.getEnablingCondition() );
        eventPostState.setPriority( eventPostStateEntity.getPriority() );
        eventPostState.setPostStateReference( eventPostStateEntity.getPostStateReference() );

        return eventPostState;
    }

    @Override
    public CaseEventLite map(EventLiteEntity eventLiteEntity) {
        if ( eventLiteEntity == null ) {
            return null;
        }

        CaseEventLite caseEventLite = new CaseEventLite();

        caseEventLite.setId( eventLiteEntity.getReference() );
        caseEventLite.setName( eventLiteEntity.getName() );
        caseEventLite.setDescription( eventLiteEntity.getDescription() );

        caseEventLite.setPreStates( eventLiteEntity.isCanCreate() ? java.util.Collections.emptyList() : eventLiteEntity.getPreStates().isEmpty() ? java.util.Arrays.asList("*") : eventLiteEntity.getPreStates().stream().map(StateEntity::getReference).collect(java.util.stream.Collectors.toList()) );
        caseEventLite.setAcls( uk.gov.hmcts.ccd.definition.store.domain.service.EntityToResponseDTOMapper.AuthorisationToAccessControlListMapper.map(               eventLiteEntity.getEventLiteACLs()           ) );

        return caseEventLite;
    }

    @Override
    public Jurisdiction map(JurisdictionEntity jurisdictionEntity) {
        if ( jurisdictionEntity == null ) {
            return null;
        }

        Jurisdiction jurisdiction = new Jurisdiction();

        jurisdiction.setId( jurisdictionEntity.getReference() );
        jurisdiction.setLiveUntil( jurisdictionEntity.getLiveTo() );
        jurisdiction.setName( jurisdictionEntity.getName() );
        jurisdiction.setDescription( jurisdictionEntity.getDescription() );
        jurisdiction.setLiveFrom( jurisdictionEntity.getLiveFrom() );

        return jurisdiction;
    }

    @Override
    public CaseState map(StateEntity stateEntity) {
        if ( stateEntity == null ) {
            return null;
        }

        CaseState caseState = new CaseState();

        caseState.setId( stateEntity.getReference() );
        caseState.setName( stateEntity.getName() );
        caseState.setDescription( stateEntity.getDescription() );
        caseState.setOrder( stateEntity.getOrder() );
        caseState.setTitleDisplay( stateEntity.getTitleDisplay() );

        caseState.setAcls( uk.gov.hmcts.ccd.definition.store.domain.service.EntityToResponseDTOMapper.AuthorisationToAccessControlListMapper.map(               stateEntity.getStateACLEntities()           ) );

        return caseState;
    }

    @Override
    public CaseField map(CaseFieldEntity caseFieldEntity) {
        if ( caseFieldEntity == null ) {
            return null;
        }

        CaseField caseField = new CaseField();

        caseField.setId( caseFieldEntity.getReference() );
        caseField.setCaseTypeId( caseFieldEntityCaseTypeReference( caseFieldEntity ) );
        caseField.setHintText( caseFieldEntity.getHint() );
        if ( caseFieldEntity.getLiveTo() != null ) {
            caseField.setLiveUntil( DateTimeFormatter.ISO_LOCAL_DATE.format( caseFieldEntity.getLiveTo() ) );
        }
        caseField.setLabel( caseFieldEntity.getLabel() );
        caseField.setFieldType( map( caseFieldEntity.getFieldType() ) );
        caseField.setHidden( caseFieldEntity.getHidden() );
        if ( caseFieldEntity.getSecurityClassification() != null ) {
            caseField.setSecurityClassification( caseFieldEntity.getSecurityClassification().name() );
        }
        if ( caseFieldEntity.getLiveFrom() != null ) {
            caseField.setLiveFrom( DateTimeFormatter.ISO_LOCAL_DATE.format( caseFieldEntity.getLiveFrom() ) );
        }
        caseField.setCategoryId( caseFieldEntity.getCategoryId() );

        caseField.setAcls( uk.gov.hmcts.ccd.definition.store.domain.service.EntityToResponseDTOMapper.AuthorisationToAccessControlListMapper.map(               caseFieldEntity.getCaseFieldACLEntities()           ) );
        caseField.setComplexACLs( uk.gov.hmcts.ccd.definition.store.domain.service.EntityToResponseDTOMapper.AuthorisationToAccessControlListMapper.mapComplex(               caseFieldEntity.getComplexFieldACLEntities()           ) );
        caseField.setMetadata( caseFieldEntity.isMetadataField() );

        return caseField;
    }

    @Override
    public FieldType map(FieldTypeEntity fieldTypeEntity) {
        if ( fieldTypeEntity == null ) {
            return null;
        }

        FieldType fieldType = new FieldType();

        fieldType.setId( fieldTypeEntity.getReference() );
        fieldType.setMin( fieldTypeEntity.getMinimum() );
        fieldType.setMax( fieldTypeEntity.getMaximum() );
        fieldType.setRegularExpression( fieldTypeEntity.getRegularExpression() );
        fieldType.setCollectionFieldType( map( fieldTypeEntity.getCollectionFieldType() ) );

        fieldType.setFixedListItems( uk.gov.hmcts.ccd.definition.store.domain.service.EntityToResponseDTOMapper.FixedListMapper.map(               fieldTypeEntity           ) );
        fieldType.setComplexFields( uk.gov.hmcts.ccd.definition.store.domain.service.EntityToResponseDTOMapper.ComplexFieldsMapper.map(               fieldTypeEntity) );
        fieldType.setType( fieldTypeEntity.getBaseFieldType() == null ? fieldTypeEntity.getReference() : fieldTypeEntity.getBaseFieldType().getReference() );

        return fieldType;
    }

    @Override
    public CaseRole map(CaseRoleEntity caseRoleEntity) {
        if ( caseRoleEntity == null ) {
            return null;
        }

        CaseRole caseRole = new CaseRole();

        caseRole.setId( caseRoleEntity.getReference() );
        caseRole.setName( caseRoleEntity.getName() );
        caseRole.setDescription( caseRoleEntity.getDescription() );

        return caseRole;
    }

    @Override
    public FixedListItem map(FieldTypeListItemEntity fieldTypeListItemEntity) {
        if ( fieldTypeListItemEntity == null ) {
            return null;
        }

        FixedListItem fixedListItem = new FixedListItem();

        fixedListItem.setCode( fieldTypeListItemEntity.getValue() );
        fixedListItem.setLabel( fieldTypeListItemEntity.getLabel() );
        fixedListItem.setOrder( fieldTypeListItemEntity.getOrder() );

        return fixedListItem;
    }

    @Override
    public CaseEventField map(EventCaseFieldEntity eventCaseFieldEntity) {
        if ( eventCaseFieldEntity == null ) {
            return null;
        }

        CaseEventField caseEventField = new CaseEventField();

        caseEventField.setCaseFieldId( eventCaseFieldEntityCaseFieldReference( eventCaseFieldEntity ) );
        if ( eventCaseFieldEntity.getDisplayContext() != null ) {
            caseEventField.setDisplayContext( eventCaseFieldEntity.getDisplayContext().name() );
        }
        caseEventField.setShowCondition( eventCaseFieldEntity.getShowCondition() );
        caseEventField.setShowSummaryChangeOption( eventCaseFieldEntity.getShowSummaryChangeOption() );
        caseEventField.setShowSummaryContentOption( eventCaseFieldEntity.getShowSummaryContentOption() );
        caseEventField.setLabel( eventCaseFieldEntity.getLabel() );
        caseEventField.setHintText( eventCaseFieldEntity.getHintText() );
        caseEventField.setDisplayContextParameter( eventCaseFieldEntity.getDisplayContextParameter() );
        caseEventField.setRetainHiddenValue( eventCaseFieldEntity.getRetainHiddenValue() );
        caseEventField.setPublish( eventCaseFieldEntity.getPublish() );
        caseEventField.setDefaultValue( eventCaseFieldEntity.getDefaultValue() );
        caseEventField.setPublishAs( eventCaseFieldEntity.getPublishAs() );
        caseEventField.setNullifyByDefault( eventCaseFieldEntity.getNullifyByDefault() );

        caseEventField.setCaseEventFieldComplex( uk.gov.hmcts.ccd.definition.store.domain.service.EntityToResponseDTOMapper.EventComplexTypeEntityToCaseEventFieldComplexListMapper.map(           eventCaseFieldEntity.getEventComplexTypes()       ) );

        return caseEventField;
    }

    @Override
    public List<CaseEventFieldComplex> map(List<EventComplexTypeEntity> eventComplexTypeEntities) {
        if ( eventComplexTypeEntities == null ) {
            return null;
        }

        List<CaseEventFieldComplex> list = new ArrayList<CaseEventFieldComplex>( eventComplexTypeEntities.size() );
        for ( EventComplexTypeEntity eventComplexTypeEntity : eventComplexTypeEntities ) {
            list.add( map( eventComplexTypeEntity ) );
        }

        return list;
    }

    @Override
    public CaseEventFieldComplex map(EventComplexTypeEntity eventComplexTypeEntity) {
        if ( eventComplexTypeEntity == null ) {
            return null;
        }

        CaseEventFieldComplex caseEventFieldComplex = new CaseEventFieldComplex();

        caseEventFieldComplex.setReference( eventComplexTypeEntity.getReference() );
        caseEventFieldComplex.setHint( eventComplexTypeEntity.getHint() );
        caseEventFieldComplex.setLabel( eventComplexTypeEntity.getLabel() );
        caseEventFieldComplex.setOrder( eventComplexTypeEntity.getOrder() );
        caseEventFieldComplex.setDisplayContext( eventComplexTypeEntity.getDisplayContext() );
        caseEventFieldComplex.setShowCondition( eventComplexTypeEntity.getShowCondition() );
        caseEventFieldComplex.setPublish( eventComplexTypeEntity.getPublish() );
        caseEventFieldComplex.setPublishAs( eventComplexTypeEntity.getPublishAs() );

        return caseEventFieldComplex;
    }

    @Override
    public CaseField map(ComplexFieldEntity complexFieldEntity) {
        if ( complexFieldEntity == null ) {
            return null;
        }

        CaseField caseField = new CaseField();

        caseField.setId( complexFieldEntity.getReference() );
        caseField.setHintText( complexFieldEntity.getHint() );
        caseField.setLabel( complexFieldEntity.getLabel() );
        caseField.setFieldType( map( complexFieldEntity.getFieldType() ) );
        caseField.setHidden( complexFieldEntity.getHidden() );
        if ( complexFieldEntity.getSecurityClassification() != null ) {
            caseField.setSecurityClassification( complexFieldEntity.getSecurityClassification().name() );
        }
        caseField.setShowCondition( complexFieldEntity.getShowCondition() );
        caseField.setOrder( complexFieldEntity.getOrder() );
        caseField.setDisplayContextParameter( complexFieldEntity.getDisplayContextParameter() );
        caseField.setRetainHiddenValue( complexFieldEntity.getRetainHiddenValue() );
        caseField.setCategoryId( complexFieldEntity.getCategoryId() );

        return caseField;
    }

    @Override
    public CaseTypeTab map(DisplayGroupEntity displayGroupEntity) {
        if ( displayGroupEntity == null ) {
            return null;
        }

        CaseTypeTab caseTypeTab = new CaseTypeTab();

        caseTypeTab.setId( displayGroupEntity.getReference() );
        if ( displayGroupEntity.hasShowCondition() ) {
            caseTypeTab.setShowCondition( displayGroupEntity.getShowCondition() );
        }
        caseTypeTab.setTabFields( displayGroupCaseFieldEntitySetToCaseTypeTabFieldList( displayGroupEntity.getDisplayGroupCaseFields() ) );
        caseTypeTab.setRole( displayGroupEntityAccessProfileReference( displayGroupEntity ) );
        caseTypeTab.setDefaultFocus( displayGroupEntity.getDefaultFocus() );
        caseTypeTab.setLabel( displayGroupEntity.getLabel() );
        caseTypeTab.setOrder( displayGroupEntity.getOrder() );

        return caseTypeTab;
    }

    @Override
    public CaseTypeTabField map(DisplayGroupCaseFieldEntity displayGroupCaseFieldEntity) {
        if ( displayGroupCaseFieldEntity == null ) {
            return null;
        }

        CaseTypeTabField caseTypeTabField = new CaseTypeTabField();

        if ( displayGroupCaseFieldEntity.hasShowCondition() ) {
            caseTypeTabField.setShowCondition( displayGroupCaseFieldEntity.getShowCondition() );
        }
        caseTypeTabField.setCaseField( map( displayGroupCaseFieldEntity.getCaseField() ) );
        caseTypeTabField.setOrder( displayGroupCaseFieldEntity.getOrder() );
        caseTypeTabField.setDisplayContextParameter( displayGroupCaseFieldEntity.getDisplayContextParameter() );

        return caseTypeTabField;
    }

    @Override
    public SearchInputField map(SearchInputCaseFieldEntity searchInputCaseFieldEntity) {
        if ( searchInputCaseFieldEntity == null ) {
            return null;
        }

        SearchInputField searchInputField = new SearchInputField();

        searchInputField.setCaseFieldId( searchInputCaseFieldEntityCaseFieldReference( searchInputCaseFieldEntity ) );
        searchInputField.setCaseFieldElementPath( searchInputCaseFieldEntity.getCaseFieldElementPath() );
        searchInputField.setRole( searchInputCaseFieldEntityAccessProfileReference( searchInputCaseFieldEntity ) );
        searchInputField.setShowCondition( searchInputCaseFieldEntity.getShowCondition() );
        searchInputField.setLabel( searchInputCaseFieldEntity.getLabel() );
        searchInputField.setOrder( searchInputCaseFieldEntity.getOrder() );
        searchInputField.setDisplayContextParameter( searchInputCaseFieldEntity.getDisplayContextParameter() );

        return searchInputField;
    }

    @Override
    public SearchResultsField map(SearchResultCaseFieldEntity searchResultCaseFieldEntity) {
        if ( searchResultCaseFieldEntity == null ) {
            return null;
        }

        SearchResultsField searchResultsField = new SearchResultsField();

        searchResultsField.setCaseFieldId( searchResultCaseFieldEntityCaseFieldReference( searchResultCaseFieldEntity ) );
        searchResultsField.setCaseFieldElementPath( searchResultCaseFieldEntity.getCaseFieldElementPath() );
        searchResultsField.setRole( searchResultCaseFieldEntityAccessProfileReference( searchResultCaseFieldEntity ) );
        searchResultsField.setCaseTypeId( searchResultCaseFieldEntityCaseTypeReference( searchResultCaseFieldEntity ) );
        searchResultsField.setLabel( searchResultCaseFieldEntity.getLabel() );
        searchResultsField.setOrder( searchResultCaseFieldEntity.getOrder() );
        searchResultsField.setSortOrder( sortOrderToSortOrder( searchResultCaseFieldEntity.getSortOrder() ) );
        searchResultsField.setDisplayContextParameter( searchResultCaseFieldEntity.getDisplayContextParameter() );

        searchResultsField.setMetadata( searchResultCaseFieldEntity.getCaseField().isMetadataField() );

        return searchResultsField;
    }

    @Override
    public WorkbasketInputField map(WorkBasketInputCaseFieldEntity workBasketInputCaseFieldEntity) {
        if ( workBasketInputCaseFieldEntity == null ) {
            return null;
        }

        WorkbasketInputField workbasketInputField = new WorkbasketInputField();

        workbasketInputField.setCaseFieldId( workBasketInputCaseFieldEntityCaseFieldReference( workBasketInputCaseFieldEntity ) );
        workbasketInputField.setCaseFieldElementPath( workBasketInputCaseFieldEntity.getCaseFieldElementPath() );
        workbasketInputField.setRole( workBasketInputCaseFieldEntityAccessProfileReference( workBasketInputCaseFieldEntity ) );
        workbasketInputField.setShowCondition( workBasketInputCaseFieldEntity.getShowCondition() );
        workbasketInputField.setLabel( workBasketInputCaseFieldEntity.getLabel() );
        workbasketInputField.setOrder( workBasketInputCaseFieldEntity.getOrder() );
        workbasketInputField.setDisplayContextParameter( workBasketInputCaseFieldEntity.getDisplayContextParameter() );

        return workbasketInputField;
    }

    @Override
    public WorkBasketResultField map(WorkBasketCaseFieldEntity workBasketCaseFieldEntity) {
        if ( workBasketCaseFieldEntity == null ) {
            return null;
        }

        WorkBasketResultField workBasketResultField = new WorkBasketResultField();

        workBasketResultField.setCaseFieldId( workBasketCaseFieldEntityCaseFieldReference( workBasketCaseFieldEntity ) );
        workBasketResultField.setCaseFieldElementPath( workBasketCaseFieldEntity.getCaseFieldElementPath() );
        workBasketResultField.setRole( workBasketCaseFieldEntityAccessProfileReference( workBasketCaseFieldEntity ) );
        workBasketResultField.setCaseTypeId( workBasketCaseFieldEntityCaseTypeReference( workBasketCaseFieldEntity ) );
        workBasketResultField.setLabel( workBasketCaseFieldEntity.getLabel() );
        workBasketResultField.setOrder( workBasketCaseFieldEntity.getOrder() );
        workBasketResultField.setSortOrder( sortOrderToSortOrder( workBasketCaseFieldEntity.getSortOrder() ) );
        workBasketResultField.setDisplayContextParameter( workBasketCaseFieldEntity.getDisplayContextParameter() );

        workBasketResultField.setMetadata( workBasketCaseFieldEntity.getCaseField().isMetadataField() );

        return workBasketResultField;
    }

    @Override
    public SearchCasesResultField map(SearchCasesResultFieldEntity searchCasesResultFieldEntity) {
        if ( searchCasesResultFieldEntity == null ) {
            return null;
        }

        SearchCasesResultField searchCasesResultField = new SearchCasesResultField();

        searchCasesResultField.setCaseFieldId( searchCasesResultFieldEntityCaseFieldReference( searchCasesResultFieldEntity ) );
        searchCasesResultField.setCaseFieldElementPath( searchCasesResultFieldEntity.getCaseFieldElementPath() );
        searchCasesResultField.setRole( searchCasesResultFieldEntityAccessProfileReference( searchCasesResultFieldEntity ) );
        searchCasesResultField.setCaseTypeId( searchCasesResultFieldEntityCaseTypeReference( searchCasesResultFieldEntity ) );
        searchCasesResultField.setLabel( searchCasesResultFieldEntity.getLabel() );
        searchCasesResultField.setOrder( searchCasesResultFieldEntity.getOrder() );
        searchCasesResultField.setUseCase( searchCasesResultFieldEntity.getUseCase() );
        searchCasesResultField.setSortOrder( sortOrderToSortOrder( searchCasesResultFieldEntity.getSortOrder() ) );
        searchCasesResultField.setDisplayContextParameter( searchCasesResultFieldEntity.getDisplayContextParameter() );

        searchCasesResultField.setMetadata( searchCasesResultFieldEntity.getCaseField().isMetadataField() );

        return searchCasesResultField;
    }

    @Override
    public SearchAliasField map(SearchAliasFieldEntity searchAliasFieldEntity) {
        if ( searchAliasFieldEntity == null ) {
            return null;
        }

        SearchAliasField searchAliasField = new SearchAliasField();

        searchAliasField.setId( searchAliasFieldEntity.getReference() );
        searchAliasField.setCaseTypeId( searchAliasFieldEntityCaseTypeReference( searchAliasFieldEntity ) );
        searchAliasField.setCaseFieldPath( searchAliasFieldEntity.getCaseFieldPath() );
        searchAliasField.setFieldType( map( searchAliasFieldEntity.getFieldType() ) );

        return searchAliasField;
    }

    @Override
    public Banner map(BannerEntity bannerEntity) {
        if ( bannerEntity == null ) {
            return null;
        }

        Banner banner = new Banner();

        if ( bannerEntity.getId() != null ) {
            banner.setId( String.valueOf( bannerEntity.getId() ) );
        }
        banner.setBannerEnabled( bannerEntity.getBannerEnabled() );
        banner.setBannerDescription( bannerEntity.getBannerDescription() );
        banner.setBannerUrl( bannerEntity.getBannerUrl() );
        banner.setBannerUrlText( bannerEntity.getBannerUrlText() );

        return banner;
    }

    @Override
    public JurisdictionUiConfig map(JurisdictionUiConfigEntity jurisdictionUiConfigEntity) {
        if ( jurisdictionUiConfigEntity == null ) {
            return null;
        }

        JurisdictionUiConfig jurisdictionUiConfig = new JurisdictionUiConfig();

        jurisdictionUiConfig.setId( jurisdictionUiConfigEntityJurisdictionReference( jurisdictionUiConfigEntity ) );
        jurisdictionUiConfig.setName( jurisdictionUiConfigEntityJurisdictionName( jurisdictionUiConfigEntity ) );
        jurisdictionUiConfig.setShuttered( jurisdictionUiConfigEntity.getShuttered() );

        return jurisdictionUiConfig;
    }

    @Override
    public ChallengeQuestion map(ChallengeQuestionTabEntity challengeQuestionEntity) {
        if ( challengeQuestionEntity == null ) {
            return null;
        }

        ChallengeQuestion challengeQuestion = new ChallengeQuestion();

        challengeQuestion.setCaseTypeId( challengeQuestionEntityCaseTypeReference( challengeQuestionEntity ) );
        challengeQuestion.setOrder( challengeQuestionEntity.getOrder() );
        challengeQuestion.setQuestionText( challengeQuestionEntity.getQuestionText() );
        challengeQuestion.setAnswerFieldType( map( challengeQuestionEntity.getAnswerFieldType() ) );
        challengeQuestion.setDisplayContextParameter( challengeQuestionEntity.getDisplayContextParameter() );
        challengeQuestion.setChallengeQuestionId( challengeQuestionEntity.getChallengeQuestionId() );
        challengeQuestion.setAnswerField( challengeQuestionEntity.getAnswerField() );
        challengeQuestion.setQuestionId( challengeQuestionEntity.getQuestionId() );
        if ( challengeQuestionEntity.isIgnoreNullFields() != null ) {
            challengeQuestion.setIgnoreNullFields( challengeQuestionEntity.isIgnoreNullFields() );
        }

        return challengeQuestion;
    }

    @Override
    public RoleToAccessProfiles map(RoleToAccessProfilesEntity roleToAccessProfilesEntity) {
        if ( roleToAccessProfilesEntity == null ) {
            return null;
        }

        RoleToAccessProfiles roleToAccessProfiles = new RoleToAccessProfiles();

        roleToAccessProfiles.setCaseTypeId( roleToAccessProfilesEntityCaseTypeReference( roleToAccessProfilesEntity ) );
        roleToAccessProfiles.setAuthorisations( roleToAccessProfilesEntity.getAuthorisation() );
        roleToAccessProfiles.setDisabled( roleToAccessProfilesEntity.getDisabled() );
        roleToAccessProfiles.setReadOnly( roleToAccessProfilesEntity.getReadOnly() );
        roleToAccessProfiles.setAccessProfiles( roleToAccessProfilesEntity.getAccessProfiles() );
        roleToAccessProfiles.setLiveFrom( roleToAccessProfilesEntity.getLiveFrom() );
        roleToAccessProfiles.setLiveTo( roleToAccessProfilesEntity.getLiveTo() );
        roleToAccessProfiles.setRoleName( roleToAccessProfilesEntity.getRoleName() );
        roleToAccessProfiles.setCaseAccessCategories( roleToAccessProfilesEntity.getCaseAccessCategories() );

        return roleToAccessProfiles;
    }

    @Override
    public SearchCriteria map(SearchCriteriaEntity searchCriteriaEntity) {
        if ( searchCriteriaEntity == null ) {
            return null;
        }

        SearchCriteria searchCriteria = new SearchCriteria();

        searchCriteria.setCaseTypeId( searchCriteriaEntityCaseTypeReference( searchCriteriaEntity ) );
        searchCriteria.setLiveFrom( searchCriteriaEntity.getLiveFrom() );
        searchCriteria.setLiveTo( searchCriteriaEntity.getLiveTo() );
        searchCriteria.setOtherCaseReference( searchCriteriaEntity.getOtherCaseReference() );

        return searchCriteria;
    }

    @Override
    public SearchParty map(SearchPartyEntity searchPartyEntity) {
        if ( searchPartyEntity == null ) {
            return null;
        }

        SearchParty searchParty = new SearchParty();

        searchParty.setCaseTypeId( searchPartyEntityCaseTypeReference( searchPartyEntity ) );
        searchParty.setSearchPartyDob( searchPartyEntity.getSearchPartyDob() );
        searchParty.setSearchPartyDod( searchPartyEntity.getSearchPartyDod() );
        searchParty.setSearchPartyPostCode( searchPartyEntity.getSearchPartyPostCode() );
        searchParty.setSearchPartyAddressLine1( searchPartyEntity.getSearchPartyAddressLine1() );
        searchParty.setSearchPartyEmailAddress( searchPartyEntity.getSearchPartyEmailAddress() );
        searchParty.setLiveFrom( searchPartyEntity.getLiveFrom() );
        searchParty.setLiveTo( searchPartyEntity.getLiveTo() );
        searchParty.setSearchPartyName( searchPartyEntity.getSearchPartyName() );
        searchParty.setSearchPartyCollectionFieldName( searchPartyEntity.getSearchPartyCollectionFieldName() );

        return searchParty;
    }

    @Override
    public Category map(CategoryEntity categoryEntity) {
        if ( categoryEntity == null ) {
            return null;
        }

        Category category = new Category();

        category.setCaseTypeId( categoryEntityCaseTypeReference( categoryEntity ) );
        category.setCategoryId( categoryEntity.getCategoryId() );
        category.setCategoryLabel( categoryEntity.getCategoryLabel() );
        category.setParentCategoryId( categoryEntity.getParentCategoryId() );
        category.setLiveFrom( categoryEntity.getLiveFrom() );
        category.setLiveTo( categoryEntity.getLiveTo() );
        category.setDisplayOrder( categoryEntity.getDisplayOrder() );

        return category;
    }

    @Override
    public AccessTypeRoleField map(AccessTypeRoleEntity accessTypeRoleEntity) {
        if ( accessTypeRoleEntity == null ) {
            return null;
        }

        AccessTypeRoleField accessTypeRoleField = new AccessTypeRoleField();

        accessTypeRoleField.setCaseTypeId( accessTypeRoleEntityCaseTypeReference( accessTypeRoleEntity ) );
        accessTypeRoleField.setId( accessTypeRoleEntity.getId() );
        accessTypeRoleField.setLiveFrom( accessTypeRoleEntity.getLiveFrom() );
        accessTypeRoleField.setLiveTo( accessTypeRoleEntity.getLiveTo() );
        accessTypeRoleField.setAccessTypeId( accessTypeRoleEntity.getAccessTypeId() );
        accessTypeRoleField.setOrganisationProfileId( accessTypeRoleEntity.getOrganisationProfileId() );
        accessTypeRoleField.setOrganisationalRoleName( accessTypeRoleEntity.getOrganisationalRoleName() );
        accessTypeRoleField.setGroupRoleName( accessTypeRoleEntity.getGroupRoleName() );
        accessTypeRoleField.setCaseAssignedRoleField( accessTypeRoleEntity.getCaseAssignedRoleField() );
        accessTypeRoleField.setGroupAccessEnabled( accessTypeRoleEntity.getGroupAccessEnabled() );
        accessTypeRoleField.setCaseAccessGroupIdTemplate( accessTypeRoleEntity.getCaseAccessGroupIdTemplate() );

        return accessTypeRoleField;
    }

    @Override
    public AccessTypeField map(AccessTypeEntity accessTypeEntity) {
        if ( accessTypeEntity == null ) {
            return null;
        }

        AccessTypeField accessTypeField = new AccessTypeField();

        accessTypeField.setCaseTypeId( accessTypeEntityCaseTypeReference( accessTypeEntity ) );
        accessTypeField.setJurisdictionId( accessTypeEntityCaseTypeJurisdictionReference( accessTypeEntity ) );
        accessTypeField.setJurisdictionName( accessTypeEntityCaseTypeJurisdictionName( accessTypeEntity ) );
        accessTypeField.setId( accessTypeEntity.getId() );
        accessTypeField.setLiveFrom( accessTypeEntity.getLiveFrom() );
        accessTypeField.setLiveTo( accessTypeEntity.getLiveTo() );
        accessTypeField.setAccessTypeId( accessTypeEntity.getAccessTypeId() );
        accessTypeField.setOrganisationProfileId( accessTypeEntity.getOrganisationProfileId() );
        accessTypeField.setAccessMandatory( accessTypeEntity.getAccessMandatory() );
        accessTypeField.setAccessDefault( accessTypeEntity.getAccessDefault() );
        accessTypeField.setDisplay( accessTypeEntity.getDisplay() );
        accessTypeField.setDescription( accessTypeEntity.getDescription() );
        accessTypeField.setHint( accessTypeEntity.getHint() );
        accessTypeField.setDisplayOrder( accessTypeEntity.getDisplayOrder() );

        return accessTypeField;
    }

    @Override
    public ShellMapping map(ShellMappingEntity shellMappingEntity) {
        if ( shellMappingEntity == null ) {
            return null;
        }

        ShellMapping shellMapping = new ShellMapping();

        shellMapping.setShellCaseTypeId( shellMappingEntityShellCaseTypeIdReference( shellMappingEntity ) );
        shellMapping.setShellCaseFieldName( shellMappingEntityShellCaseFieldNameReference( shellMappingEntity ) );
        shellMapping.setOriginatingCaseTypeId( shellMappingEntityOriginatingCaseTypeIdReference( shellMappingEntity ) );
        shellMapping.setOriginatingCaseFieldName( shellMappingEntityOriginatingCaseFieldNameReference( shellMappingEntity ) );

        return shellMapping;
    }

    @Override
    public ReindexTask map(ReindexEntity reindexEntity) {
        if ( reindexEntity == null ) {
            return null;
        }

        ReindexTask reindexTask = new ReindexTask();

        if ( reindexEntity.getDeleteOldIndex() != null ) {
            reindexTask.setDeleteOldIndex( reindexEntity.getDeleteOldIndex() );
        }
        reindexTask.setCaseType( reindexEntity.getCaseType() );
        reindexTask.setJurisdiction( reindexEntity.getJurisdiction() );
        reindexTask.setIndexName( reindexEntity.getIndexName() );
        reindexTask.setStartTime( reindexEntity.getStartTime() );
        reindexTask.setEndTime( reindexEntity.getEndTime() );
        reindexTask.setStatus( reindexEntity.getStatus() );
        reindexTask.setExceptionMessage( reindexEntity.getExceptionMessage() );
        reindexTask.setReindexResponse( reindexEntity.getReindexResponse() );
        reindexTask.setWhoImported( reindexEntity.getWhoImported() );

        return reindexTask;
    }

    @Override
    public RoleAssignment roleToAccessProfilesEntityToRoleAssignment(RoleToAccessProfilesEntity roleToAccessProfilesEntity) {
        if ( roleToAccessProfilesEntity == null ) {
            return null;
        }

        RoleAssignment roleAssignment = new RoleAssignment();

        roleAssignment.setId( roleToAccessProfilesEntityCaseTypeReference( roleToAccessProfilesEntity ) );
        roleAssignment.setName( roleToAccessProfilesEntity.getRoleName() );

        return roleAssignment;
    }

    protected Version caseTypeEntityToVersion(CaseTypeEntity caseTypeEntity) {
        if ( caseTypeEntity == null ) {
            return null;
        }

        Version version = new Version();

        version.setNumber( caseTypeEntity.getVersion() );
        if ( caseTypeEntity.getLiveFrom() != null ) {
            version.setLiveFrom( Date.from( caseTypeEntity.getLiveFrom().atStartOfDay( ZoneOffset.UTC ).toInstant() ) );
        }
        if ( caseTypeEntity.getLiveTo() != null ) {
            version.setLiveUntil( Date.from( caseTypeEntity.getLiveTo().atStartOfDay( ZoneOffset.UTC ).toInstant() ) );
        }

        return version;
    }

    private String caseTypeEntityGetCaseWebhookUrl(CaseTypeEntity caseTypeEntity) {
        WebhookEntity getCaseWebhook = caseTypeEntity.getGetCaseWebhook();
        if ( getCaseWebhook == null ) {
            return null;
        }
        return getCaseWebhook.getUrl();
    }

    private List<Integer> caseTypeEntityGetCaseWebhookTimeouts(CaseTypeEntity caseTypeEntity) {
        WebhookEntity getCaseWebhook = caseTypeEntity.getGetCaseWebhook();
        if ( getCaseWebhook == null ) {
            return null;
        }
        return getCaseWebhook.getTimeouts();
    }

    private String caseTypeEntityPrintWebhookUrl(CaseTypeEntity caseTypeEntity) {
        WebhookEntity printWebhook = caseTypeEntity.getPrintWebhook();
        if ( printWebhook == null ) {
            return null;
        }
        return printWebhook.getUrl();
    }

    protected List<CaseEvent> eventEntityListToCaseEventList(List<EventEntity> list) {
        if ( list == null ) {
            return null;
        }

        List<CaseEvent> list1 = new ArrayList<CaseEvent>( list.size() );
        for ( EventEntity eventEntity : list ) {
            list1.add( map( eventEntity ) );
        }

        return list1;
    }

    protected List<CaseState> stateEntityListToCaseStateList(List<StateEntity> list) {
        if ( list == null ) {
            return null;
        }

        List<CaseState> list1 = new ArrayList<CaseState>( list.size() );
        for ( StateEntity stateEntity : list ) {
            list1.add( map( stateEntity ) );
        }

        return list1;
    }

    protected List<CaseField> caseFieldEntitySetToCaseFieldList(Set<CaseFieldEntity> set) {
        if ( set == null ) {
            return null;
        }

        List<CaseField> list = new ArrayList<CaseField>( set.size() );
        for ( CaseFieldEntity caseFieldEntity : set ) {
            list.add( map( caseFieldEntity ) );
        }

        return list;
    }

    protected List<SearchAliasField> searchAliasFieldEntityListToSearchAliasFieldList(List<SearchAliasFieldEntity> list) {
        if ( list == null ) {
            return null;
        }

        List<SearchAliasField> list1 = new ArrayList<SearchAliasField>( list.size() );
        for ( SearchAliasFieldEntity searchAliasFieldEntity : list ) {
            list1.add( map( searchAliasFieldEntity ) );
        }

        return list1;
    }

    protected List<RoleToAccessProfiles> roleToAccessProfilesEntityListToRoleToAccessProfilesList(List<RoleToAccessProfilesEntity> list) {
        if ( list == null ) {
            return null;
        }

        List<RoleToAccessProfiles> list1 = new ArrayList<RoleToAccessProfiles>( list.size() );
        for ( RoleToAccessProfilesEntity roleToAccessProfilesEntity : list ) {
            list1.add( map( roleToAccessProfilesEntity ) );
        }

        return list1;
    }

    protected List<SearchCriteria> searchCriteriaEntityListToSearchCriteriaList(List<SearchCriteriaEntity> list) {
        if ( list == null ) {
            return null;
        }

        List<SearchCriteria> list1 = new ArrayList<SearchCriteria>( list.size() );
        for ( SearchCriteriaEntity searchCriteriaEntity : list ) {
            list1.add( map( searchCriteriaEntity ) );
        }

        return list1;
    }

    protected List<SearchParty> searchPartyEntityListToSearchPartyList(List<SearchPartyEntity> list) {
        if ( list == null ) {
            return null;
        }

        List<SearchParty> list1 = new ArrayList<SearchParty>( list.size() );
        for ( SearchPartyEntity searchPartyEntity : list ) {
            list1.add( map( searchPartyEntity ) );
        }

        return list1;
    }

    protected List<Category> categoryEntityListToCategoryList(List<CategoryEntity> list) {
        if ( list == null ) {
            return null;
        }

        List<Category> list1 = new ArrayList<Category>( list.size() );
        for ( CategoryEntity categoryEntity : list ) {
            list1.add( map( categoryEntity ) );
        }

        return list1;
    }

    protected List<AccessTypeField> accessTypeEntityListToAccessTypeFieldList(List<AccessTypeEntity> list) {
        if ( list == null ) {
            return null;
        }

        List<AccessTypeField> list1 = new ArrayList<AccessTypeField>( list.size() );
        for ( AccessTypeEntity accessTypeEntity : list ) {
            list1.add( map( accessTypeEntity ) );
        }

        return list1;
    }

    protected List<AccessTypeRoleField> accessTypeRoleEntityListToAccessTypeRoleFieldList(List<AccessTypeRoleEntity> list) {
        if ( list == null ) {
            return null;
        }

        List<AccessTypeRoleField> list1 = new ArrayList<AccessTypeRoleField>( list.size() );
        for ( AccessTypeRoleEntity accessTypeRoleEntity : list ) {
            list1.add( map( accessTypeRoleEntity ) );
        }

        return list1;
    }

    protected List<CaseEventLite> eventLiteEntityListToCaseEventLiteList(List<EventLiteEntity> list) {
        if ( list == null ) {
            return null;
        }

        List<CaseEventLite> list1 = new ArrayList<CaseEventLite>( list.size() );
        for ( EventLiteEntity eventLiteEntity : list ) {
            list1.add( map( eventLiteEntity ) );
        }

        return list1;
    }

    protected List<CaseEventField> eventCaseFieldEntityListToCaseEventFieldList(List<EventCaseFieldEntity> list) {
        if ( list == null ) {
            return null;
        }

        List<CaseEventField> list1 = new ArrayList<CaseEventField>( list.size() );
        for ( EventCaseFieldEntity eventCaseFieldEntity : list ) {
            list1.add( map( eventCaseFieldEntity ) );
        }

        return list1;
    }

    private String eventEntityWebhookStartUrl(EventEntity eventEntity) {
        WebhookEntity webhookStart = eventEntity.getWebhookStart();
        if ( webhookStart == null ) {
            return null;
        }
        return webhookStart.getUrl();
    }

    private List<Integer> eventEntityWebhookStartTimeouts(EventEntity eventEntity) {
        WebhookEntity webhookStart = eventEntity.getWebhookStart();
        if ( webhookStart == null ) {
            return null;
        }
        return webhookStart.getTimeouts();
    }

    private String eventEntityWebhookPreSubmitUrl(EventEntity eventEntity) {
        WebhookEntity webhookPreSubmit = eventEntity.getWebhookPreSubmit();
        if ( webhookPreSubmit == null ) {
            return null;
        }
        return webhookPreSubmit.getUrl();
    }

    private List<Integer> eventEntityWebhookPreSubmitTimeouts(EventEntity eventEntity) {
        WebhookEntity webhookPreSubmit = eventEntity.getWebhookPreSubmit();
        if ( webhookPreSubmit == null ) {
            return null;
        }
        return webhookPreSubmit.getTimeouts();
    }

    private String eventEntityWebhookPostSubmitUrl(EventEntity eventEntity) {
        WebhookEntity webhookPostSubmit = eventEntity.getWebhookPostSubmit();
        if ( webhookPostSubmit == null ) {
            return null;
        }
        return webhookPostSubmit.getUrl();
    }

    private List<Integer> eventEntityWebhookPostSubmitTimeouts(EventEntity eventEntity) {
        WebhookEntity webhookPostSubmit = eventEntity.getWebhookPostSubmit();
        if ( webhookPostSubmit == null ) {
            return null;
        }
        return webhookPostSubmit.getTimeouts();
    }

    protected List<EventPostState> eventPostStateEntityListToEventPostStateList(List<EventPostStateEntity> list) {
        if ( list == null ) {
            return null;
        }

        List<EventPostState> list1 = new ArrayList<EventPostState>( list.size() );
        for ( EventPostStateEntity eventPostStateEntity : list ) {
            list1.add( map( eventPostStateEntity ) );
        }

        return list1;
    }

    private String caseFieldEntityCaseTypeReference(CaseFieldEntity caseFieldEntity) {
        CaseTypeEntity caseType = caseFieldEntity.getCaseType();
        if ( caseType == null ) {
            return null;
        }
        return caseType.getReference();
    }

    private String eventCaseFieldEntityCaseFieldReference(EventCaseFieldEntity eventCaseFieldEntity) {
        CaseFieldEntity caseField = eventCaseFieldEntity.getCaseField();
        if ( caseField == null ) {
            return null;
        }
        return caseField.getReference();
    }

    protected List<CaseTypeTabField> displayGroupCaseFieldEntitySetToCaseTypeTabFieldList(Set<DisplayGroupCaseFieldEntity> set) {
        if ( set == null ) {
            return null;
        }

        List<CaseTypeTabField> list = new ArrayList<CaseTypeTabField>( set.size() );
        for ( DisplayGroupCaseFieldEntity displayGroupCaseFieldEntity : set ) {
            list.add( map( displayGroupCaseFieldEntity ) );
        }

        return list;
    }

    private String displayGroupEntityAccessProfileReference(DisplayGroupEntity displayGroupEntity) {
        AccessProfileEntity accessProfile = displayGroupEntity.getAccessProfile();
        if ( accessProfile == null ) {
            return null;
        }
        return accessProfile.getReference();
    }

    private String searchInputCaseFieldEntityCaseFieldReference(SearchInputCaseFieldEntity searchInputCaseFieldEntity) {
        CaseFieldEntity caseField = searchInputCaseFieldEntity.getCaseField();
        if ( caseField == null ) {
            return null;
        }
        return caseField.getReference();
    }

    private String searchInputCaseFieldEntityAccessProfileReference(SearchInputCaseFieldEntity searchInputCaseFieldEntity) {
        AccessProfileEntity accessProfile = searchInputCaseFieldEntity.getAccessProfile();
        if ( accessProfile == null ) {
            return null;
        }
        return accessProfile.getReference();
    }

    private String searchResultCaseFieldEntityCaseFieldReference(SearchResultCaseFieldEntity searchResultCaseFieldEntity) {
        CaseFieldEntity caseField = searchResultCaseFieldEntity.getCaseField();
        if ( caseField == null ) {
            return null;
        }
        return caseField.getReference();
    }

    private String searchResultCaseFieldEntityAccessProfileReference(SearchResultCaseFieldEntity searchResultCaseFieldEntity) {
        AccessProfileEntity accessProfile = searchResultCaseFieldEntity.getAccessProfile();
        if ( accessProfile == null ) {
            return null;
        }
        return accessProfile.getReference();
    }

    private String searchResultCaseFieldEntityCaseTypeReference(SearchResultCaseFieldEntity searchResultCaseFieldEntity) {
        CaseTypeEntity caseType = searchResultCaseFieldEntity.getCaseType();
        if ( caseType == null ) {
            return null;
        }
        return caseType.getReference();
    }

    protected uk.gov.hmcts.ccd.definition.store.repository.model.SortOrder sortOrderToSortOrder(SortOrder sortOrder) {
        if ( sortOrder == null ) {
            return null;
        }

        uk.gov.hmcts.ccd.definition.store.repository.model.SortOrder sortOrder1 = new uk.gov.hmcts.ccd.definition.store.repository.model.SortOrder();

        sortOrder1.setDirection( sortOrder.getDirection() );
        sortOrder1.setPriority( sortOrder.getPriority() );

        return sortOrder1;
    }

    private String workBasketInputCaseFieldEntityCaseFieldReference(WorkBasketInputCaseFieldEntity workBasketInputCaseFieldEntity) {
        CaseFieldEntity caseField = workBasketInputCaseFieldEntity.getCaseField();
        if ( caseField == null ) {
            return null;
        }
        return caseField.getReference();
    }

    private String workBasketInputCaseFieldEntityAccessProfileReference(WorkBasketInputCaseFieldEntity workBasketInputCaseFieldEntity) {
        AccessProfileEntity accessProfile = workBasketInputCaseFieldEntity.getAccessProfile();
        if ( accessProfile == null ) {
            return null;
        }
        return accessProfile.getReference();
    }

    private String workBasketCaseFieldEntityCaseFieldReference(WorkBasketCaseFieldEntity workBasketCaseFieldEntity) {
        CaseFieldEntity caseField = workBasketCaseFieldEntity.getCaseField();
        if ( caseField == null ) {
            return null;
        }
        return caseField.getReference();
    }

    private String workBasketCaseFieldEntityAccessProfileReference(WorkBasketCaseFieldEntity workBasketCaseFieldEntity) {
        AccessProfileEntity accessProfile = workBasketCaseFieldEntity.getAccessProfile();
        if ( accessProfile == null ) {
            return null;
        }
        return accessProfile.getReference();
    }

    private String workBasketCaseFieldEntityCaseTypeReference(WorkBasketCaseFieldEntity workBasketCaseFieldEntity) {
        CaseTypeEntity caseType = workBasketCaseFieldEntity.getCaseType();
        if ( caseType == null ) {
            return null;
        }
        return caseType.getReference();
    }

    private String searchCasesResultFieldEntityCaseFieldReference(SearchCasesResultFieldEntity searchCasesResultFieldEntity) {
        CaseFieldEntity caseField = searchCasesResultFieldEntity.getCaseField();
        if ( caseField == null ) {
            return null;
        }
        return caseField.getReference();
    }

    private String searchCasesResultFieldEntityAccessProfileReference(SearchCasesResultFieldEntity searchCasesResultFieldEntity) {
        AccessProfileEntity accessProfile = searchCasesResultFieldEntity.getAccessProfile();
        if ( accessProfile == null ) {
            return null;
        }
        return accessProfile.getReference();
    }

    private String searchCasesResultFieldEntityCaseTypeReference(SearchCasesResultFieldEntity searchCasesResultFieldEntity) {
        CaseTypeEntity caseType = searchCasesResultFieldEntity.getCaseType();
        if ( caseType == null ) {
            return null;
        }
        return caseType.getReference();
    }

    private String searchAliasFieldEntityCaseTypeReference(SearchAliasFieldEntity searchAliasFieldEntity) {
        CaseTypeEntity caseType = searchAliasFieldEntity.getCaseType();
        if ( caseType == null ) {
            return null;
        }
        return caseType.getReference();
    }

    private String jurisdictionUiConfigEntityJurisdictionReference(JurisdictionUiConfigEntity jurisdictionUiConfigEntity) {
        JurisdictionEntity jurisdiction = jurisdictionUiConfigEntity.getJurisdiction();
        if ( jurisdiction == null ) {
            return null;
        }
        return jurisdiction.getReference();
    }

    private String jurisdictionUiConfigEntityJurisdictionName(JurisdictionUiConfigEntity jurisdictionUiConfigEntity) {
        JurisdictionEntity jurisdiction = jurisdictionUiConfigEntity.getJurisdiction();
        if ( jurisdiction == null ) {
            return null;
        }
        return jurisdiction.getName();
    }

    private String challengeQuestionEntityCaseTypeReference(ChallengeQuestionTabEntity challengeQuestionTabEntity) {
        CaseTypeLiteEntity caseType = challengeQuestionTabEntity.getCaseType();
        if ( caseType == null ) {
            return null;
        }
        return caseType.getReference();
    }

    private String roleToAccessProfilesEntityCaseTypeReference(RoleToAccessProfilesEntity roleToAccessProfilesEntity) {
        CaseTypeEntity caseType = roleToAccessProfilesEntity.getCaseType();
        if ( caseType == null ) {
            return null;
        }
        return caseType.getReference();
    }

    private String searchCriteriaEntityCaseTypeReference(SearchCriteriaEntity searchCriteriaEntity) {
        CaseTypeEntity caseType = searchCriteriaEntity.getCaseType();
        if ( caseType == null ) {
            return null;
        }
        return caseType.getReference();
    }

    private String searchPartyEntityCaseTypeReference(SearchPartyEntity searchPartyEntity) {
        CaseTypeEntity caseType = searchPartyEntity.getCaseType();
        if ( caseType == null ) {
            return null;
        }
        return caseType.getReference();
    }

    private String categoryEntityCaseTypeReference(CategoryEntity categoryEntity) {
        CaseTypeLiteEntity caseType = categoryEntity.getCaseType();
        if ( caseType == null ) {
            return null;
        }
        return caseType.getReference();
    }

    private String accessTypeRoleEntityCaseTypeReference(AccessTypeRoleEntity accessTypeRoleEntity) {
        CaseTypeLiteEntity caseType = accessTypeRoleEntity.getCaseType();
        if ( caseType == null ) {
            return null;
        }
        return caseType.getReference();
    }

    private String accessTypeEntityCaseTypeReference(AccessTypeEntity accessTypeEntity) {
        CaseTypeLiteEntity caseType = accessTypeEntity.getCaseType();
        if ( caseType == null ) {
            return null;
        }
        return caseType.getReference();
    }

    private String accessTypeEntityCaseTypeJurisdictionReference(AccessTypeEntity accessTypeEntity) {
        CaseTypeLiteEntity caseType = accessTypeEntity.getCaseType();
        if ( caseType == null ) {
            return null;
        }
        JurisdictionEntity jurisdiction = caseType.getJurisdiction();
        if ( jurisdiction == null ) {
            return null;
        }
        return jurisdiction.getReference();
    }

    private String accessTypeEntityCaseTypeJurisdictionName(AccessTypeEntity accessTypeEntity) {
        CaseTypeLiteEntity caseType = accessTypeEntity.getCaseType();
        if ( caseType == null ) {
            return null;
        }
        JurisdictionEntity jurisdiction = caseType.getJurisdiction();
        if ( jurisdiction == null ) {
            return null;
        }
        return jurisdiction.getName();
    }

    private String shellMappingEntityShellCaseTypeIdReference(ShellMappingEntity shellMappingEntity) {
        CaseTypeLiteEntity shellCaseTypeId = shellMappingEntity.getShellCaseTypeId();
        if ( shellCaseTypeId == null ) {
            return null;
        }
        return shellCaseTypeId.getReference();
    }

    private String shellMappingEntityShellCaseFieldNameReference(ShellMappingEntity shellMappingEntity) {
        CaseFieldEntity shellCaseFieldName = shellMappingEntity.getShellCaseFieldName();
        if ( shellCaseFieldName == null ) {
            return null;
        }
        return shellCaseFieldName.getReference();
    }

    private String shellMappingEntityOriginatingCaseTypeIdReference(ShellMappingEntity shellMappingEntity) {
        CaseTypeLiteEntity originatingCaseTypeId = shellMappingEntity.getOriginatingCaseTypeId();
        if ( originatingCaseTypeId == null ) {
            return null;
        }
        return originatingCaseTypeId.getReference();
    }

    private String shellMappingEntityOriginatingCaseFieldNameReference(ShellMappingEntity shellMappingEntity) {
        CaseFieldEntity originatingCaseFieldName = shellMappingEntity.getOriginatingCaseFieldName();
        if ( originatingCaseFieldName == null ) {
            return null;
        }
        return originatingCaseFieldName.getReference();
    }
}
