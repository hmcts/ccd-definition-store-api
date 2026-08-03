package uk.gov.hmcts.ccd.definition.store.domain.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.ccd.definition.store.domain.service.casetype.CaseTypeServiceImpl;
import uk.gov.hmcts.ccd.definition.store.domain.service.casetype.CaseTypeSnapshotService;
import uk.gov.hmcts.ccd.definition.store.domain.service.casetype.CaseTypeService;
import uk.gov.hmcts.ccd.definition.store.domain.service.legacyvalidation.LegacyCaseTypeValidator;
import uk.gov.hmcts.ccd.definition.store.domain.service.metadata.MetadataFieldService;
import uk.gov.hmcts.ccd.definition.store.domain.showcondition.ShowConditionParser;
import uk.gov.hmcts.ccd.definition.store.domain.validation.complexfield.ComplexFieldEntityDisplayContextParameterValidatorImpl;
import uk.gov.hmcts.ccd.definition.store.domain.validation.complexfield.ComplexFieldEntityDocumentTypeRegularExpressionValidator;
import uk.gov.hmcts.ccd.definition.store.domain.validation.complexfield.ComplexFieldEntityShowConditionValidatorImpl;
import uk.gov.hmcts.ccd.definition.store.domain.validation.displaycontextparameter.DisplayContextParameterValidatorFactory;
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
import uk.gov.hmcts.ccd.definition.store.domain.validation.fieldtype.FieldTypeValidationContextFactory;
import uk.gov.hmcts.ccd.definition.store.domain.validation.fieldtype.FieldTypeValidator;
import uk.gov.hmcts.ccd.definition.store.domain.validation.searchaliasfield.SearchAliasFieldNameValidator;
import uk.gov.hmcts.ccd.definition.store.domain.validation.searchaliasfield.SearchAliasFieldTypeValidator;
import uk.gov.hmcts.ccd.definition.store.domain.validation.searchaliasfield.SearchAliasFieldUnicityValidator;
import uk.gov.hmcts.ccd.definition.store.repository.CaseFieldEntityUtil;
import uk.gov.hmcts.ccd.definition.store.repository.CaseTypeRepository;
import uk.gov.hmcts.ccd.definition.store.repository.FieldTypeRepository;
import uk.gov.hmcts.ccd.definition.store.repository.SearchAliasFieldRepository;
import uk.gov.hmcts.ccd.definition.store.repository.SearchCriteriaRepository;
import uk.gov.hmcts.ccd.definition.store.repository.SearchPartyRepository;
import uk.gov.hmcts.ccd.definition.store.repository.SecurityUtils;
import uk.gov.hmcts.ccd.definition.store.repository.ShellMappingRepository;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.model.DefinitionModelMapper;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ContextConfiguration
class ServicesAutowiringTest {

    @Autowired
    private CaseTypeService caseTypeService;

    @Autowired
    private FieldTypeService fieldTypeService;

    @Test
    void caseTypeServiceHasAllRequiredValidatorsWiredIntoList() {

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

        List<FieldTypeValidator> fieldTypeValidators
            = (List<FieldTypeValidator>) ReflectionTestUtils.getField(fieldTypeService, "validators");

        assertListOfComponentsContainInstances(
            fieldTypeValidators,
            BaseReferenceFieldTypeValidator.class,
            FieldTypeComplexFieldsValidatorImpl.class
        );

        assertListOfComponentsContainInstances(
            (List) ReflectionTestUtils.getField(
                getValidator(fieldTypeValidators, FieldTypeComplexFieldsValidatorImpl.class),
                "complexFieldValidators"
            ),
            ComplexFieldEntityDisplayContextParameterValidatorImpl.class,
            ComplexFieldEntityDocumentTypeRegularExpressionValidator.class,
            ComplexFieldEntityShowConditionValidatorImpl.class
        );

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
    @Import({
        CaseTypeServiceImpl.class,
        FieldTypeServiceImpl.class,
        CaseTypeEntitySecurityClassificationValidatorImpl.class,
        CaseTypeEntityCaseFieldsValidatorImpl.class,
        CaseTypeEntityEventValidatorImpl.class,
        CaseTypeEntityACLValidatorImpl.class,
        CaseTypeEntityCrudValidatorImpl.class,
        CaseTypeEntitySearchAliasFieldsValidatorImpl.class,
        CaseFieldEntitySecurityClassificationValidatorImpl.class,
        CaseFieldEntityComplexFieldsValidatorImpl.class,
        CaseFieldEntityACLValidatorImpl.class,
        CaseFieldEntityCrudValidatorImpl.class,
        ComplexFieldEntitySecurityClassificationValidatorImpl.class,
        EventEntityEventCaseFieldsValidatorImpl.class,
        EventEntitySecurityClassificationValidatorImpl.class,
        EventEntityCrudValidatorImpl.class,
        EventEntityACLValidatorImpl.class,
        EventEntityCreateEventValidator.class,
        EventEntityPostStateValidator.class,
        EventEntityEnablingConditionValidator.class,
        EventCaseFieldLabelCaseFieldValidator.class,
        EventCaseFieldShowConditionValidatorImpl.class,
        EventCaseFieldDisplayContextValidatorImpl.class,
        BaseReferenceFieldTypeValidator.class,
        FieldTypeComplexFieldsValidatorImpl.class,
        ComplexFieldEntityDisplayContextParameterValidatorImpl.class,
        ComplexFieldEntityDocumentTypeRegularExpressionValidator.class,
        ComplexFieldEntityShowConditionValidatorImpl.class
    })
    public static class Config {

        @Bean
        public CaseTypeRepository caseTypeRepository() {
            return mock(CaseTypeRepository.class);
        }

        @Bean
        public EntityToResponseDTOMapper entityToResponseDTOMapper() {
            return mock(EntityToResponseDTOMapper.class);
        }

        @Bean
        public LegacyCaseTypeValidator legacyCaseTypeValidator() {
            return mock(LegacyCaseTypeValidator.class);
        }

        @Bean
        public MetadataFieldService metadataFieldService() {
            return mock(MetadataFieldService.class);
        }

        @Bean
        public CaseTypeSnapshotService caseTypeSnapshotService() {
            return mock(CaseTypeSnapshotService.class);
        }

        @Bean
        public FieldTypeRepository fieldTypeRepository() {
            FieldTypeRepository fieldTypeRepository = mock(FieldTypeRepository.class);
            when(fieldTypeRepository.findPredefinedComplexTypes()).thenReturn(List.of());
            return fieldTypeRepository;
        }

        @Bean
        public CaseFieldEntityUtil caseFieldEntityUtil() {
            return mock(CaseFieldEntityUtil.class);
        }

        @Bean
        public ShowConditionParser showConditionParser() {
            return mock(ShowConditionParser.class);
        }

        @Bean
        public SearchAliasFieldRepository searchAliasFieldRepository() {
            return mock(SearchAliasFieldRepository.class);
        }

        @Bean
        public SearchAliasFieldTypeValidator searchAliasFieldTypeValidator(
            SearchAliasFieldRepository searchAliasFieldRepository
        ) {
            return new SearchAliasFieldTypeValidator(searchAliasFieldRepository);
        }

        @Bean
        public SearchAliasFieldUnicityValidator searchAliasFieldUnicityValidator() {
            return new SearchAliasFieldUnicityValidator();
        }

        @Bean
        public SearchAliasFieldNameValidator searchAliasFieldNameValidator() {
            return new SearchAliasFieldNameValidator();
        }

        @Bean
        public FieldTypeValidationContextFactory fieldTypeValidationContextFactory() {
            return mock(FieldTypeValidationContextFactory.class);

        @Primary
        public ShellMappingRepository shellMappingRepository() {
            return mock(ShellMappingRepository.class);
        }

        @Bean
        @Primary
        public ImportJobRepository importJobRepository() {
            return mock(ImportJobRepository.class);
        }

        @Bean
        public DisplayContextParameterValidatorFactory displayContextParameterValidatorFactory() {
            return mock(DisplayContextParameterValidatorFactory.class);
        }
    }
}
