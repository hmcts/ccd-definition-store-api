package uk.gov.hmcts.ccd.definition.store.domain.service;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.ccd.definition.store.AppInsights;
import uk.gov.hmcts.ccd.definition.store.domain.service.casetype.CaseTypeService;
import uk.gov.hmcts.ccd.definition.store.domain.service.metadata.MetadataFieldService;
import uk.gov.hmcts.ccd.definition.store.domain.validation.casefield.CaseFieldEntityACLValidatorImpl;
import uk.gov.hmcts.ccd.definition.store.domain.validation.casefield.CaseFieldEntityComplexFieldsValidatorImpl;
import uk.gov.hmcts.ccd.definition.store.domain.validation.casefield.CaseFieldEntityCrudValidatorImpl;
import uk.gov.hmcts.ccd.definition.store.domain.validation.casefield.CaseFieldEntitySecurityClassificationValidatorImpl;
import uk.gov.hmcts.ccd.definition.store.domain.validation.casetype.CaseTypeEntityACLValidatorImpl;
import uk.gov.hmcts.ccd.definition.store.domain.validation.casetype.CaseTypeEntityCaseFieldsValidatorImpl;
import uk.gov.hmcts.ccd.definition.store.domain.validation.casetype.CaseTypeEntityCrudValidatorImpl;
import uk.gov.hmcts.ccd.definition.store.domain.validation.casetype.CaseTypeEntityEventValidatorImpl;
import uk.gov.hmcts.ccd.definition.store.domain.validation.casetype.CaseTypeEntitySearchAliasFieldsValidatorImpl;
import uk.gov.hmcts.ccd.definition.store.domain.validation.casetype.CaseTypeEntitySecurityClassificationValidatorImpl;
import uk.gov.hmcts.ccd.definition.store.domain.validation.casetype.CaseTypeEntityValidator;
import uk.gov.hmcts.ccd.definition.store.domain.validation.complexfield.ComplexFieldEntitySecurityClassificationValidatorImpl;
import uk.gov.hmcts.ccd.definition.store.domain.validation.event.EventEntityACLValidatorImpl;
import uk.gov.hmcts.ccd.definition.store.domain.validation.event.EventEntityCreateEventValidator;
import uk.gov.hmcts.ccd.definition.store.domain.validation.event.EventEntityCrudValidatorImpl;
import uk.gov.hmcts.ccd.definition.store.domain.validation.event.EventEntityEnablingConditionValidator;
import uk.gov.hmcts.ccd.definition.store.domain.validation.event.EventEntityEventCaseFieldsValidatorImpl;
import uk.gov.hmcts.ccd.definition.store.domain.validation.event.EventEntityPostStateValidator;
import uk.gov.hmcts.ccd.definition.store.domain.validation.event.EventEntitySecurityClassificationValidatorImpl;
import uk.gov.hmcts.ccd.definition.store.domain.validation.eventcasefield.EventCaseFieldDisplayContextValidatorImpl;
import uk.gov.hmcts.ccd.definition.store.domain.validation.eventcasefield.EventCaseFieldLabelCaseFieldValidator;
import uk.gov.hmcts.ccd.definition.store.domain.validation.eventcasefield.EventCaseFieldShowConditionValidatorImpl;
import uk.gov.hmcts.ccd.definition.store.domain.validation.fieldtype.BaseReferenceFieldTypeValidator;
import uk.gov.hmcts.ccd.definition.store.domain.validation.fieldtype.FieldTypeComplexFieldsValidatorImpl;
import uk.gov.hmcts.ccd.definition.store.domain.validation.fieldtype.FieldTypeValidator;
import uk.gov.hmcts.ccd.definition.store.domain.validation.searchaliasfield.SearchAliasFieldNameValidator;
import uk.gov.hmcts.ccd.definition.store.domain.validation.searchaliasfield.SearchAliasFieldTypeValidator;
import uk.gov.hmcts.ccd.definition.store.domain.validation.searchaliasfield.SearchAliasFieldUnicityValidator;
import uk.gov.hmcts.ccd.definition.store.repository.AccessProfileRepository;
import uk.gov.hmcts.ccd.definition.store.repository.AccessTypeRolesRepository;
import uk.gov.hmcts.ccd.definition.store.repository.AccessTypesRepository;
import uk.gov.hmcts.ccd.definition.store.repository.BannerRepository;
import uk.gov.hmcts.ccd.definition.store.repository.CaseFieldEntityUtil;
import uk.gov.hmcts.ccd.definition.store.repository.CaseFieldRepository;
import uk.gov.hmcts.ccd.definition.store.repository.CaseRoleRepository;
import uk.gov.hmcts.ccd.definition.store.repository.CaseTypeLiteRepository;
import uk.gov.hmcts.ccd.definition.store.repository.CaseTypeRepository;
import uk.gov.hmcts.ccd.definition.store.repository.CategoryTabRepository;
import uk.gov.hmcts.ccd.definition.store.repository.ChallengeQuestionTabRepository;
import uk.gov.hmcts.ccd.definition.store.repository.DisplayGroupRepository;
import uk.gov.hmcts.ccd.definition.store.repository.DraftDefinitionRepositoryDecorator;
import uk.gov.hmcts.ccd.definition.store.repository.EventRepository;
import uk.gov.hmcts.ccd.definition.store.repository.FieldTypeRepository;
import uk.gov.hmcts.ccd.definition.store.repository.GenericLayoutRepository;
import uk.gov.hmcts.ccd.definition.store.repository.JurisdictionRepository;
import uk.gov.hmcts.ccd.definition.store.repository.JurisdictionUiConfigRepository;
import uk.gov.hmcts.ccd.definition.store.repository.RoleToAccessProfilesRepository;
import uk.gov.hmcts.ccd.definition.store.repository.SearchAliasFieldRepository;
import uk.gov.hmcts.ccd.definition.store.repository.SearchCriteriaRepository;
import uk.gov.hmcts.ccd.definition.store.repository.SearchPartyRepository;
import uk.gov.hmcts.ccd.definition.store.repository.SecurityUtils;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.model.DefinitionModelMapper;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ContextConfiguration
class ServicesAutowiringTest implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Test
    void caseTypeServiceHasAllRequiredValidatorsWiredIntoList() {

        CaseTypeService caseTypeService
            = applicationContext.getAutowireCapableBeanFactory().getBean(CaseTypeService.class);

        List<CaseTypeEntityValidator> caseTypeEntityValidators
            = (List<CaseTypeEntityValidator>) ReflectionTestUtils.getField(caseTypeService, "caseTypeEntityValidators");

        assertListOfComponentsContainInstances(
            caseTypeEntityValidators,
            CaseTypeEntitySecurityClassificationValidatorImpl.class,
            CaseTypeEntityCaseFieldsValidatorImpl.class,
            CaseTypeEntityEventValidatorImpl.class,
            CaseTypeEntityACLValidatorImpl.class,
            CaseTypeEntityCrudValidatorImpl.class,
            CaseTypeEntitySearchAliasFieldsValidatorImpl.class
        );

        // Check the CaseTypeEntityCaseFieldsValidatorImpl class has the required validator(s) wired in
        assertListOfComponentsContainInstances(
            (List) ReflectionTestUtils.getField(
                getValidator(caseTypeEntityValidators, CaseTypeEntityCaseFieldsValidatorImpl.class),
                "caseFieldEntityValidators"
            ),
            CaseFieldEntitySecurityClassificationValidatorImpl.class,
            CaseFieldEntityComplexFieldsValidatorImpl.class,
            CaseFieldEntityACLValidatorImpl.class,
            CaseFieldEntityCrudValidatorImpl.class
        );

        // Check the CaseFieldEntityComplexFieldsValidatorImpl class has the required validator(s) wired in
        assertListOfComponentsContainInstances(
            (List) ReflectionTestUtils.getField(
                getValidator(
                    (List) ReflectionTestUtils.getField(
                        getValidator(
                            caseTypeEntityValidators,
                            CaseTypeEntityCaseFieldsValidatorImpl.class
                        ),
                        "caseFieldEntityValidators"
                    ),
                    CaseFieldEntityComplexFieldsValidatorImpl.class
                ),
                "complexFieldEntityValidators"
            ),
            ComplexFieldEntitySecurityClassificationValidatorImpl.class
        );

        // Check the CaseTypeEntityEventValidatorImpl class has the required validator(s) wired in
        assertListOfComponentsContainInstances(
            (List) ReflectionTestUtils.getField(
                getValidator(caseTypeEntityValidators, CaseTypeEntityEventValidatorImpl.class),
                "eventEntityValidators"
            ),
            EventEntityEventCaseFieldsValidatorImpl.class,
            EventEntitySecurityClassificationValidatorImpl.class,
            EventEntityCrudValidatorImpl.class,
            EventEntityACLValidatorImpl.class,
            EventEntityCreateEventValidator.class,
            EventEntityPostStateValidator.class,
            EventEntityEnablingConditionValidator.class
        );

        // Check the EventEntityEventCaseFieldsValidatorImpl class has the required validator(s) wired in
        assertListOfComponentsContainInstances(
            (List) ReflectionTestUtils.getField(
                getValidator(
                    (List) ReflectionTestUtils.getField(
                        getValidator(
                            caseTypeEntityValidators,
                            CaseTypeEntityEventValidatorImpl.class
                        ),
                        "eventEntityValidators"
                    ),
                    EventEntityEventCaseFieldsValidatorImpl.class
                ),
                "eventCaseFieldValidators"
            ),
            EventCaseFieldLabelCaseFieldValidator.class,
            EventCaseFieldShowConditionValidatorImpl.class,
            EventCaseFieldDisplayContextValidatorImpl.class
        );

        // Check the CaseTypeEntitySearchAliasFieldsValidatorImpl class has the required validator(s) wired in
        assertListOfComponentsContainInstances(
            (List) ReflectionTestUtils.getField(
                getValidator(caseTypeEntityValidators, CaseTypeEntitySearchAliasFieldsValidatorImpl.class),
                "searchAliasFieldValidators"
            ),
            SearchAliasFieldTypeValidator.class,
            SearchAliasFieldUnicityValidator.class,
            SearchAliasFieldNameValidator.class
        );

    }

    @Test
    void fieldTypeServiceHasAllRequiredValidatorsWiredIntoList() {

        FieldTypeService fieldTypeService
            = applicationContext.getAutowireCapableBeanFactory().getBean(FieldTypeService.class);

        List<FieldTypeValidator> fieldTypeValidators
            = (List<FieldTypeValidator>) ReflectionTestUtils.getField(fieldTypeService, "validators");

        assertListOfComponentsContainInstances(
            fieldTypeValidators,
            BaseReferenceFieldTypeValidator.class,
            FieldTypeComplexFieldsValidatorImpl.class
        );

    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    private <T> T getValidator(List objects, Class<T> validatorClass) {
        for (Object o : objects) {
            if (validatorClass.isInstance(o)) {
                return validatorClass.cast(o);
            }
        }
        throw new RuntimeException("No validator found for class " + validatorClass);
    }

    private void assertListOfComponentsContainInstances(Collection components, Class... componentClasses) {

        for (Class validatorClass : componentClasses) {
            assertTrue(
                containsInstance(components, validatorClass),
                String.format("Instance of %s not found", validatorClass.getCanonicalName())
            );
        }

    }

    private boolean containsInstance(Collection collection, Class clazz) {
        return collection == null ? false : collection.stream()
            .anyMatch(item -> clazz.isInstance(item));
    }

    @Configuration
    @ComponentScan(basePackages = {"uk.gov.hmcts.ccd.definition.store.domain"}
    )
    public static class Config {

        @Bean
        @Primary
        public AppInsights appInsights() {
            return mock(AppInsights.class);
        }

        @Bean
        @Primary
        public CaseTypeRepository caseTypeRepository() {
            return mock(CaseTypeRepository.class);
        }

        @Bean
        @Primary
        public ChallengeQuestionTabRepository challengeQuestionTabRepository() {
            return mock(ChallengeQuestionTabRepository.class);
        }

        @Bean
        @Primary
        public CaseTypeLiteRepository caseTypeLiteRepository() {
            return mock(CaseTypeLiteRepository.class);
        }

        @Bean
        @Primary
        public CategoryTabRepository categoryTabRepository() {
            return mock(CategoryTabRepository.class);
        }


        @Bean
        @Primary
        public BannerRepository bannerRepository() {
            return mock(BannerRepository.class);
        }

        @Bean
        @Primary
        public JurisdictionUiConfigRepository jurisdictionUiConfigRepository() {
            return mock(JurisdictionUiConfigRepository.class);
        }

        @Bean
        @Primary
        public FieldTypeRepository fieldTypeRepository() {
            FieldTypeRepository fieldTypeRepository = mock(FieldTypeRepository.class);
            FieldTypeEntity textBaseType = new FieldTypeEntity();
            textBaseType.setReference("Text");
            when(fieldTypeRepository.findBaseType(eq("Text"))).thenReturn(
                Optional.of(textBaseType)
            );
            return fieldTypeRepository;
        }

        @Bean
        @Primary
        public JurisdictionRepository jurisdictionRepository() {
            return mock(JurisdictionRepository.class);
        }

        @Bean
        @Primary
        public GenericLayoutRepository genericLayoutRepository() {
            return mock(GenericLayoutRepository.class);
        }

        @Bean
        @Primary
        public DisplayGroupRepository displayGroupRepository() {
            return mock(DisplayGroupRepository.class);
        }

        @Bean
        @Primary
        public AccessProfileRepository accessProfileRepository() {
            return mock(AccessProfileRepository.class);
        }

        @Bean
        @Primary
        public AccessTypeRolesRepository accessTypeRolesRepository() {
            return mock(AccessTypeRolesRepository.class);
        }

        @Bean
        @Primary
        public AccessTypesRepository accessTypeRepository() {
            return mock(AccessTypesRepository.class);
        }

        @Bean
        @Primary
        public EventRepository eventRepository() {
            return mock(EventRepository.class);
        }

        @Bean
        @Primary
        public SecurityUtils securityUtils() {
            return mock(SecurityUtils.class);
        }

        @Bean
        @Primary
        public CaseFieldRepository caseFieldRepository() {
            return mock(CaseFieldRepository.class);
        }

        @Bean
        @Primary
        public MetadataFieldService metadataFieldService() {
            return mock(MetadataFieldService.class);
        }

        @Bean
        @Primary
        public CaseRoleRepository caseRoleRepository() {
            return mock(CaseRoleRepository.class);
        }

        @Bean
        @Primary
        public DraftDefinitionRepositoryDecorator draftDefinitionRepositoryDecorator() {
            return mock(DraftDefinitionRepositoryDecorator.class);
        }

        @Bean
        @Primary
        public CaseFieldEntityUtil caseFieldEntityUtil() {
            return mock(CaseFieldEntityUtil.class);
        }

        @Bean
        @Primary
        public DefinitionModelMapper definitionModelMapper() {
            return mock(DefinitionModelMapper.class);
        }

        @Bean
        @Primary
        public SearchAliasFieldRepository searchAliasFieldRepository() {
            return mock(SearchAliasFieldRepository.class);
        }

        @Bean
        @Primary
        public RoleToAccessProfilesRepository roleToAccessProfileRepository() {
            return mock(RoleToAccessProfilesRepository.class);
        }

        @Bean
        @Primary
        public SearchCriteriaRepository getSearchCriteriaRepository() {
            return mock(SearchCriteriaRepository.class);
        }

        @Bean
        @Primary
        public SearchPartyRepository searchPartyRepository() {
            return mock(SearchPartyRepository.class);
        }
    }
}
