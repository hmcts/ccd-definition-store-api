package uk.gov.hmcts.ccd.definition.store.excel.service;

import uk.gov.hmcts.ccd.definition.store.domain.ApplicationParams;
import uk.gov.hmcts.ccd.definition.store.domain.service.FieldTypeService;
import uk.gov.hmcts.ccd.definition.store.domain.service.JurisdictionService;
import uk.gov.hmcts.ccd.definition.store.domain.service.JurisdictionUiConfigService;
import uk.gov.hmcts.ccd.definition.store.domain.service.LayoutService;
import uk.gov.hmcts.ccd.definition.store.domain.service.accessprofiles.RoleToAccessProfileService;
import uk.gov.hmcts.ccd.definition.store.domain.service.accesstyperoles.AccessTypeRolesService;
import uk.gov.hmcts.ccd.definition.store.domain.service.accesstypes.AccessTypesService;
import uk.gov.hmcts.ccd.definition.store.domain.service.banner.BannerService;
import uk.gov.hmcts.ccd.definition.store.domain.service.casetype.CaseTypeService;
import uk.gov.hmcts.ccd.definition.store.domain.service.category.CategoryTabService;
import uk.gov.hmcts.ccd.definition.store.domain.service.metadata.MetadataField;
import uk.gov.hmcts.ccd.definition.store.domain.service.question.ChallengeQuestionTabService;
import uk.gov.hmcts.ccd.definition.store.domain.service.searchcriteria.SearchCriteriaService;
import uk.gov.hmcts.ccd.definition.store.domain.service.searchparty.SearchPartyService;
import uk.gov.hmcts.ccd.definition.store.domain.service.workbasket.WorkBasketUserDefaultService;
import uk.gov.hmcts.ccd.definition.store.domain.showcondition.ShowConditionParser;
import uk.gov.hmcts.ccd.definition.store.domain.validation.MissingAccessProfilesException;
import uk.gov.hmcts.ccd.definition.store.event.DefinitionImportedEvent;
import uk.gov.hmcts.ccd.definition.store.excel.domain.definition.model.DefinitionFileUploadMetadata;
import uk.gov.hmcts.ccd.definition.store.excel.endpoint.exception.InvalidImportException;
import uk.gov.hmcts.ccd.definition.store.excel.endpoint.exception.MapperException;
import uk.gov.hmcts.ccd.definition.store.excel.parser.AccessTypeRolesParser;
import uk.gov.hmcts.ccd.definition.store.excel.parser.AccessTypesParser;
import uk.gov.hmcts.ccd.definition.store.excel.parser.CategoryParser;
import uk.gov.hmcts.ccd.definition.store.excel.parser.ChallengeQuestionParser;
import uk.gov.hmcts.ccd.definition.store.excel.parser.EntityToDefinitionDataItemRegistry;
import uk.gov.hmcts.ccd.definition.store.excel.parser.MetadataCaseFieldEntityFactory;
import uk.gov.hmcts.ccd.definition.store.excel.parser.ParseContext;
import uk.gov.hmcts.ccd.definition.store.excel.parser.ParserFactory;
import uk.gov.hmcts.ccd.definition.store.excel.parser.SpreadsheetParser;
import uk.gov.hmcts.ccd.definition.store.excel.validation.CategoryIdValidator;
import uk.gov.hmcts.ccd.definition.store.excel.validation.HiddenFieldsValidator;
import uk.gov.hmcts.ccd.definition.store.excel.validation.SearchCriteriaValidator;
import uk.gov.hmcts.ccd.definition.store.excel.validation.SearchPartyValidator;
import uk.gov.hmcts.ccd.definition.store.excel.validation.SpreadsheetValidator;
import uk.gov.hmcts.ccd.definition.store.repository.AccessProfileRepository;
import uk.gov.hmcts.ccd.definition.store.repository.AccessTypeRolesRepository;
import uk.gov.hmcts.ccd.definition.store.repository.AccessTypesRepository;
import uk.gov.hmcts.ccd.definition.store.repository.CaseFieldRepository;
import uk.gov.hmcts.ccd.definition.store.repository.entity.AccessProfileEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.DataFieldType;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.JurisdictionEntity;
import uk.gov.hmcts.ccd.definition.store.rest.model.IdamProperties;
import uk.gov.hmcts.ccd.definition.store.rest.service.IdamProfileClient;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import com.google.common.collect.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils.BASE_BASE_LOCATION;
import static uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils.BASE_CASE_HISTORY_VIEWER;
import static uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils.BASE_CASE_PAYMENT_HISTORY_VIEWER;
import static uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils.BASE_COLLECTION;
import static uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils.BASE_COMPLEX;
import static uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils.BASE_COMPONENT_LAUNCHER;
import static uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils.BASE_DATE;
import static uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils.BASE_DATE_TIME;
import static uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils.BASE_DOCUMENT;
import static uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils.BASE_DYNAMIC_LIST;
import static uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils.BASE_DYNAMIC_MULTI_SELECT_LIST;
import static uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils.BASE_DYNAMIC_RADIO_LIST;
import static uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils.BASE_EMAIL;
import static uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils.BASE_FIXED_LIST;
import static uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils.BASE_FLAG_LAUNCHER;
import static uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils.BASE_LABEL;
import static uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils.BASE_MONEY_GBP;
import static uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils.BASE_MULTI_SELECT_LIST;
import static uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils.BASE_NUMBER;
import static uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils.BASE_PHONE_UK;
import static uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils.BASE_POST_CODE;
import static uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils.BASE_RADIO_FIXED_LIST;
import static uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils.BASE_REGION;
import static uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils.BASE_TEXT;
import static uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils.BASE_TEXT_AREA;
import static uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils.BASE_WAYS_TO_PAY;
import static uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils.BASE_YES_OR_NO;
import static uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils.PREDEFINED_COMPLEX_ADDRESS_GLOBAL;
import static uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils.PREDEFINED_COMPLEX_ADDRESS_GLOBAL_UK;
import static uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils.PREDEFINED_COMPLEX_ADDRESS_UK;
import static uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils.PREDEFINED_COMPLEX_CASELINK;
import static uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils.PREDEFINED_COMPLEX_CASE_ACCESS_GROUP;
import static uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils.PREDEFINED_COMPLEX_CASE_ACCESS_GROUPS;
import static uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils.PREDEFINED_COMPLEX_CASE_LOCATION;
import static uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils.PREDEFINED_COMPLEX_CASE_MESSAGE;
import static uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils.PREDEFINED_COMPLEX_CASE_QUERIES_COLLECTION;
import static uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils.PREDEFINED_COMPLEX_CHANGE_ORGANISATION_REQUEST;
import static uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils.PREDEFINED_COMPLEX_FLAGS;
import static uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils.PREDEFINED_COMPLEX_JUDICIAL_USER;
import static uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils.PREDEFINED_COMPLEX_LINK_REASON;
import static uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils.PREDEFINED_COMPLEX_ORDER_SUMMARY;
import static uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils.PREDEFINED_COMPLEX_ORGANISATION;
import static uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils.PREDEFINED_COMPLEX_ORGANISATION_POLICY;
import static uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils.PREDEFINED_COMPLEX_PREVIOUS_ORGANISATION;
import static uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils.PREDEFINED_COMPLEX_SEARCH_CRITERIA;
import static uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils.PREDEFINED_COMPLEX_SEARCH_PARTY;
import static uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils.PREDEFINED_COMPLEX_TTL;


@ExtendWith(MockitoExtension.class)
public class ImportServiceImplTest {

    public static final String BAD_FILE = "CCD_TestDefinition_V12.xlsx";
    private static final String GOOD_FILE = "CCD_TestDefinition.xlsx";
    private static final String GOOD_FILE_MISSING_ACCESS_TYPES_ROLES_TAB
        = "CCD_TestDefinitionMissingAccessTypes&AccessTypeRolesTab.xlsx";
    private static final String JURISDICTION_NAME = "TEST";
    private static final String TEST_ADDRESS_BOOK_CASE_TYPE = "TestAddressBookCase";
    private static final String TEST_COMPLEX_ADDRESS_BOOK_CASE_TYPE = "TestComplexAddressBookCase";
    private static final String ACCESS_PROFILE_1 = "AccessProfile1";

    private ImportServiceImpl service;

    private final Executor executor = Executors.newSingleThreadExecutor();

    @Mock
    private FieldTypeService fieldTypeService;

    @Mock
    private SpreadsheetValidator spreadsheetValidator;

    @Mock
    private HiddenFieldsValidator hiddenFieldsValidator;

    @Mock
    private JurisdictionService jurisdictionService;

    @Mock
    private CaseTypeService caseTypeService;

    @Mock
    private LayoutService layoutService;

    @Mock
    private JurisdictionEntity jurisdiction;

    @Mock
    private AccessProfileRepository accessProfileRepository;

    @Mock
    private AccessTypesRepository accessTypesRepository;

    @Mock
    private AccessTypeRolesRepository accessTypeRolesRepository;

    @Mock
    private WorkBasketUserDefaultService workBasketUserDefaultService;

    @Mock
    private CaseFieldRepository caseFieldRepository;

    @Mock
    private MetadataCaseFieldEntityFactory metadataCaseFieldEntityFactory;

    @Mock
    private IdamProfileClient idamProfileClient;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @Captor
    private ArgumentCaptor<DefinitionImportedEvent> eventCaptor;

    @Mock
    private BannerService bannerService;

    @Mock
    private JurisdictionUiConfigService jurisdictionUiConfigService;

    @Mock
    private ChallengeQuestionParser challengeQuestionParser;

    @Mock
    private CategoryParser categoryParser;

    @Mock
    private AccessTypesParser accessTypesParser;

    @Mock
    private AccessTypeRolesParser accessTypeRolesParser;

    @Mock
    private ChallengeQuestionTabService challengeQuestionTabService;

    @Mock
    private RoleToAccessProfileService roleToAccessProfileService;

    @Mock
    private SearchCriteriaService searchCriteriaService;

    @Mock
    private SearchCriteriaValidator searchCriteriaValidator;

    @Mock
    private CategoryIdValidator categoryIdValidator;

    @Mock
    private SearchPartyService searchPartyService;

    @Mock
    private CategoryTabService categoryTabService;

    @Mock
    private SearchPartyValidator searchPartyValidator;

    @Mock
    private ApplicationParams applicationParams;

    @Mock
    private TranslationService translationService;

    @Mock
    private AccessTypesService accessTypesService;

    @Mock
    private AccessTypeRolesService accessTypeRolesService;

    @BeforeEach
    void setup() {
        Map<MetadataField, MetadataCaseFieldEntityFactory> registry = new HashMap<>();
        registry.put(MetadataField.STATE, metadataCaseFieldEntityFactory);

        final ParserFactory parserFactory = new ParserFactory(new ShowConditionParser(),
            new EntityToDefinitionDataItemRegistry(), registry, spreadsheetValidator, hiddenFieldsValidator,
            challengeQuestionParser, categoryParser, accessTypesParser, accessTypeRolesParser, searchPartyValidator,
            searchCriteriaValidator, categoryIdValidator, applicationParams, executor);

        final SpreadsheetParser spreadsheetParser = new SpreadsheetParser(spreadsheetValidator);

        service = new ImportServiceImpl(spreadsheetValidator,
            spreadsheetParser,
            parserFactory,
            fieldTypeService,
            jurisdictionService,
            caseTypeService,
            layoutService,
            accessProfileRepository,
            accessTypesRepository,
            accessTypeRolesRepository,
            workBasketUserDefaultService,
            caseFieldRepository,
            applicationEventPublisher,
            idamProfileClient,
            bannerService,
            jurisdictionUiConfigService,
            challengeQuestionTabService,
            roleToAccessProfileService,
            searchCriteriaService,
            searchPartyService, categoryTabService,
            translationService,
            accessTypesService,
            accessTypeRolesService,
            applicationParams);

        lenient().doReturn(JURISDICTION_NAME).when(jurisdiction).getReference();

        final IdamProperties idamProperties = new IdamProperties();
        idamProperties.setId("445");
        idamProperties.setEmail("user@hmcts.net");

        lenient().doReturn(idamProperties).when(idamProfileClient).getLoggedInUserDetails();
        lenient().when(applicationParams.isWelshTranslationEnabled()).thenReturn(true);
        lenient().when(applicationParams.isCaseGroupAccessFilteringEnabled()).thenReturn(true);

        AccessProfileEntity accessProfileEntity = new AccessProfileEntity();
        accessProfileEntity.setReference(ACCESS_PROFILE_1);
        lenient().doReturn(Arrays.asList(accessProfileEntity)).when(accessProfileRepository).findAll();
    }

    @Test
    void shouldNotImportDefinition() throws Exception {

        given(jurisdictionService.get(JURISDICTION_NAME)).willReturn(Optional.of(jurisdiction));
        given(fieldTypeService.getBaseTypes()).willReturn(Arrays.asList(buildBaseType(BASE_FIXED_LIST),
            buildBaseType(BASE_MULTI_SELECT_LIST),
            buildBaseType(BASE_COMPLEX)));
        given(fieldTypeService.getTypesByJurisdiction(JURISDICTION_NAME)).willReturn(Lists.newArrayList());

        final InputStream inputStream = getClass().getClassLoader().getResourceAsStream(BAD_FILE);
        assertThrows(InvalidImportException.class, () -> service.importFormDefinitions(inputStream));
    }

    @Test
    void importDefinitionThrowsMissingAccessProfiles() throws Exception {

        given(jurisdictionService.get(JURISDICTION_NAME)).willReturn(Optional.of(jurisdiction));

        given(fieldTypeService.getBaseTypes()).willReturn(getBaseTypesList());
        given(fieldTypeService.getPredefinedComplexTypes()).willReturn(getPredefinedComplexBaseTypesList());

        given(fieldTypeService.getTypesByJurisdiction(JURISDICTION_NAME)).willReturn(Lists.newArrayList());
        CaseFieldEntity caseRef = new CaseFieldEntity();
        caseRef.setReference("[CASE_REFERENCE]");
        given(caseFieldRepository.findByDataFieldTypeAndCaseTypeNull(DataFieldType.METADATA))
            .willReturn(Collections.singletonList(caseRef));
        CaseFieldEntity state = new CaseFieldEntity();
        state.setReference("[STATE]");
        state.setDataFieldType(DataFieldType.METADATA);
        given(metadataCaseFieldEntityFactory.createCaseFieldEntity(any(ParseContext.class), any(CaseTypeEntity.class)))
            .willReturn(state);
        doThrow(MissingAccessProfilesException.class)
            .when(caseTypeService).createAll(any(JurisdictionEntity.class), any(Collection.class), any(Set.class));
        final InputStream inputStream = getClass().getClassLoader().getResourceAsStream(GOOD_FILE);

        assertThrows(MissingAccessProfilesException.class, () -> service.importFormDefinitions(inputStream));
    }

    @Test
    void shouldImportDefinitionWhenMissingAccessTypeAndAccessTypeRoleTab() throws Exception {

        given(jurisdictionService.get(JURISDICTION_NAME)).willReturn(Optional.of(jurisdiction));

        given(fieldTypeService.getBaseTypes()).willReturn(getBaseTypesList());
        given(fieldTypeService.getPredefinedComplexTypes()).willReturn(getPredefinedComplexBaseTypesList());

        given(fieldTypeService.getTypesByJurisdiction(JURISDICTION_NAME)).willReturn(Lists.newArrayList());
        CaseFieldEntity caseRef = new CaseFieldEntity();
        caseRef.setReference("[CASE_REFERENCE]");
        given(caseFieldRepository.findByDataFieldTypeAndCaseTypeNull(DataFieldType.METADATA))
            .willReturn(Collections.singletonList(caseRef));
        CaseFieldEntity state = new CaseFieldEntity();
        state.setReference("[STATE]");
        state.setDataFieldType(DataFieldType.METADATA);
        given(metadataCaseFieldEntityFactory.createCaseFieldEntity(any(ParseContext.class), any(CaseTypeEntity.class)))
            .willReturn(state);

        final InputStream inputStream = getClass().getClassLoader()
            .getResourceAsStream(GOOD_FILE_MISSING_ACCESS_TYPES_ROLES_TAB);

        final DefinitionFileUploadMetadata metadata = service.importFormDefinitions(inputStream);
        assertEquals(JURISDICTION_NAME, metadata.getJurisdiction());
        assertEquals(2, metadata.getCaseTypes().size());
        assertEquals(TEST_ADDRESS_BOOK_CASE_TYPE, metadata.getCaseTypes().get(0));
        assertEquals(TEST_COMPLEX_ADDRESS_BOOK_CASE_TYPE, metadata.getCaseTypes().get(1));
        assertEquals("user@hmcts.net", metadata.getUserId());
        assertEquals(
            TEST_ADDRESS_BOOK_CASE_TYPE + "," + TEST_COMPLEX_ADDRESS_BOOK_CASE_TYPE,
            metadata.getCaseTypesAsString());

        verify(caseFieldRepository).findByDataFieldTypeAndCaseTypeNull(DataFieldType.METADATA);
        verify(applicationEventPublisher).publishEvent(eventCaptor.capture());
        verify(translationService).processDefinitionSheets(anyMap());
        verify(categoryIdValidator).validate(any(ParseContext.class));
        assertThat(eventCaptor.getValue().getContent().size(), equalTo(2));
    }

    @Test
    void shouldImportDefinition2() throws Exception {

        given(jurisdictionService.get(JURISDICTION_NAME)).willReturn(Optional.of(jurisdiction));

        given(fieldTypeService.getBaseTypes()).willReturn(getBaseTypesList());
        given(fieldTypeService.getPredefinedComplexTypes()).willReturn(getPredefinedComplexBaseTypesList());

        given(fieldTypeService.getTypesByJurisdiction(JURISDICTION_NAME)).willReturn(Lists.newArrayList());
        CaseFieldEntity caseRef = new CaseFieldEntity();
        caseRef.setReference("[CASE_REFERENCE]");
        given(caseFieldRepository.findByDataFieldTypeAndCaseTypeNull(DataFieldType.METADATA))
            .willReturn(Collections.singletonList(caseRef));
        CaseFieldEntity state = new CaseFieldEntity();
        state.setReference("[STATE]");
        state.setDataFieldType(DataFieldType.METADATA);
        given(metadataCaseFieldEntityFactory.createCaseFieldEntity(any(ParseContext.class), any(CaseTypeEntity.class)))
            .willReturn(state);

        final InputStream inputStream = getClass().getClassLoader().getResourceAsStream(GOOD_FILE);

        final DefinitionFileUploadMetadata metadata = service.importFormDefinitions(inputStream);
        assertEquals(JURISDICTION_NAME, metadata.getJurisdiction());
        assertEquals(2, metadata.getCaseTypes().size());
        assertEquals(TEST_ADDRESS_BOOK_CASE_TYPE, metadata.getCaseTypes().get(0));
        assertEquals(TEST_COMPLEX_ADDRESS_BOOK_CASE_TYPE, metadata.getCaseTypes().get(1));
        assertEquals("user@hmcts.net", metadata.getUserId());
        assertEquals(
            TEST_ADDRESS_BOOK_CASE_TYPE + "," + TEST_COMPLEX_ADDRESS_BOOK_CASE_TYPE,
            metadata.getCaseTypesAsString());

        verify(caseFieldRepository).findByDataFieldTypeAndCaseTypeNull(DataFieldType.METADATA);
        verify(applicationEventPublisher).publishEvent(eventCaptor.capture());
        verify(translationService).processDefinitionSheets(anyMap());
        verify(categoryIdValidator).validate(any(ParseContext.class));
        assertThat(eventCaptor.getValue().getContent().size(), equalTo(2));
    }

    @Test
    void shouldVerifyAccessWhenWelshTranslationDisabled() throws Exception {
        when(applicationParams.isWelshTranslationEnabled()).thenReturn(false);
        given(jurisdictionService.get(JURISDICTION_NAME)).willReturn(Optional.of(jurisdiction));
        given(fieldTypeService.getBaseTypes()).willReturn(getBaseTypesList());
        given(fieldTypeService.getPredefinedComplexTypes()).willReturn(getPredefinedComplexBaseTypesList());
        given(fieldTypeService.getTypesByJurisdiction(JURISDICTION_NAME)).willReturn(Lists.newArrayList());
        CaseFieldEntity caseRef = new CaseFieldEntity();
        caseRef.setReference("[CASE_REFERENCE]");
        given(caseFieldRepository.findByDataFieldTypeAndCaseTypeNull(DataFieldType.METADATA))
            .willReturn(Collections.singletonList(caseRef));
        CaseFieldEntity state = new CaseFieldEntity();
        state.setReference("[STATE]");
        state.setDataFieldType(DataFieldType.METADATA);
        given(metadataCaseFieldEntityFactory.createCaseFieldEntity(any(ParseContext.class), any(CaseTypeEntity.class)))
            .willReturn(state);
        final InputStream inputStream = getClass().getClassLoader().getResourceAsStream(GOOD_FILE);

        final DefinitionFileUploadMetadata metadata = service.importFormDefinitions(inputStream);
        assertEquals(JURISDICTION_NAME, metadata.getJurisdiction());
        assertEquals(2, metadata.getCaseTypes().size());
        assertEquals(TEST_ADDRESS_BOOK_CASE_TYPE, metadata.getCaseTypes().get(0));
        assertEquals(TEST_COMPLEX_ADDRESS_BOOK_CASE_TYPE, metadata.getCaseTypes().get(1));
        assertEquals("user@hmcts.net", metadata.getUserId());
        assertEquals(
            TEST_ADDRESS_BOOK_CASE_TYPE + "," + TEST_COMPLEX_ADDRESS_BOOK_CASE_TYPE,
            metadata.getCaseTypesAsString());

        verify(caseFieldRepository).findByDataFieldTypeAndCaseTypeNull(DataFieldType.METADATA);
        verify(applicationEventPublisher).publishEvent(eventCaptor.capture());
        verifyNoMoreInteractions(translationService);
        assertThat(eventCaptor.getValue().getContent().size(), equalTo(2));
    }


    @Test
    void shouldReturnImportWarnings() {
        Map<MetadataField, MetadataCaseFieldEntityFactory> registry = new HashMap<>();
        registry.put(MetadataField.STATE, metadataCaseFieldEntityFactory);

        final ParserFactory parserFactory = new ParserFactory(new ShowConditionParser(),
            new EntityToDefinitionDataItemRegistry(), registry, spreadsheetValidator,
            hiddenFieldsValidator,challengeQuestionParser,
            categoryParser, accessTypesParser, accessTypeRolesParser, searchPartyValidator, searchCriteriaValidator,
            categoryIdValidator, applicationParams, executor);

        final SpreadsheetParser spreadsheetParser = mock(SpreadsheetParser.class);

        service = new ImportServiceImpl(spreadsheetValidator,
            spreadsheetParser,
            parserFactory,
            fieldTypeService,
            jurisdictionService,
            caseTypeService,
            layoutService,
            accessProfileRepository,
            accessTypesRepository,
            accessTypeRolesRepository,
            workBasketUserDefaultService,
            caseFieldRepository,
            applicationEventPublisher,
            idamProfileClient,
            bannerService,
            jurisdictionUiConfigService,
            challengeQuestionTabService,
            roleToAccessProfileService,
            searchCriteriaService,
            searchPartyService, categoryTabService,
            translationService, accessTypesService,
            accessTypeRolesService, applicationParams);


        final List<String> importWarnings = Arrays.asList("Warning1", "Warning2");

        given(spreadsheetParser.getImportWarnings()).willReturn(importWarnings);

        final List<String> warnings = service.getImportWarnings();
        assertThat(warnings.size(), equalTo(2));
        assertThat(importWarnings, containsInAnyOrder("Warning1", "Warning2"));
        verify(spreadsheetParser).getImportWarnings();
    }

    @Test
    void shouldThrowMapperException() {
        given(hiddenFieldsValidator.parseComplexTypesHiddenFields(any(), any())).willThrow(MapperException.class);
        given(jurisdictionService.get(JURISDICTION_NAME)).willReturn(Optional.of(jurisdiction));
        given(fieldTypeService.getBaseTypes()).willReturn(Arrays.asList(
            buildBaseType(BASE_FIXED_LIST),
            buildBaseType(BASE_MULTI_SELECT_LIST),
            buildBaseType(BASE_COMPLEX),
            buildBaseType(BASE_TEXT),
            buildBaseType(BASE_NUMBER),
            buildBaseType(BASE_EMAIL),
            buildBaseType(BASE_YES_OR_NO),
            buildBaseType(BASE_DATE),
            buildBaseType(BASE_DATE_TIME),
            buildBaseType(BASE_POST_CODE),
            buildBaseType(BASE_MONEY_GBP),
            buildBaseType(BASE_PHONE_UK),
            buildBaseType(BASE_TEXT_AREA),
            buildBaseType(BASE_COLLECTION),
            buildBaseType(BASE_DOCUMENT),
            buildBaseType(BASE_LABEL),
            buildBaseType(BASE_CASE_PAYMENT_HISTORY_VIEWER),
            buildBaseType(BASE_CASE_HISTORY_VIEWER),
            buildBaseType(BASE_RADIO_FIXED_LIST),
            buildBaseType(BASE_DYNAMIC_LIST),
            buildBaseType(BASE_DYNAMIC_RADIO_LIST),
            buildBaseType(BASE_DYNAMIC_MULTI_SELECT_LIST)));
        given(fieldTypeService.getTypesByJurisdiction(JURISDICTION_NAME)).willReturn(Lists.newArrayList());
        CaseFieldEntity caseRef = new CaseFieldEntity();
        caseRef.setReference("[CASE_REFERENCE]");
        CaseFieldEntity state = new CaseFieldEntity();
        state.setReference("[STATE]");
        state.setDataFieldType(DataFieldType.METADATA);
        final InputStream inputStream = getClass().getClassLoader().getResourceAsStream(GOOD_FILE);

        assertThrows(MapperException.class, () -> service.importFormDefinitions(inputStream));
    }

    private FieldTypeEntity buildBaseType(final String reference) {
        FieldTypeEntity fieldTypeEntity = new FieldTypeEntity();
        fieldTypeEntity.setReference(reference);
        return fieldTypeEntity;
    }

    private List<FieldTypeEntity> getBaseTypesList() {
        return Arrays.asList(
            buildBaseType(BASE_FIXED_LIST),
            buildBaseType(BASE_MULTI_SELECT_LIST),
            buildBaseType(BASE_COMPLEX),
            buildBaseType(BASE_TEXT),
            buildBaseType(BASE_NUMBER),
            buildBaseType(BASE_EMAIL),
            buildBaseType(BASE_YES_OR_NO),
            buildBaseType(BASE_DATE),
            buildBaseType(BASE_DATE_TIME),
            buildBaseType(BASE_POST_CODE),
            buildBaseType(BASE_MONEY_GBP),
            buildBaseType(BASE_PHONE_UK),
            buildBaseType(BASE_TEXT_AREA),
            buildBaseType(BASE_COLLECTION),
            buildBaseType(BASE_DOCUMENT),
            buildBaseType(BASE_LABEL),
            buildBaseType(BASE_CASE_PAYMENT_HISTORY_VIEWER),
            buildBaseType(BASE_CASE_HISTORY_VIEWER),
            buildBaseType(BASE_RADIO_FIXED_LIST),
            buildBaseType(BASE_DYNAMIC_LIST),
            buildBaseType(BASE_DYNAMIC_RADIO_LIST),
            buildBaseType(BASE_DYNAMIC_MULTI_SELECT_LIST),
            buildBaseType(BASE_WAYS_TO_PAY),
            buildBaseType(BASE_REGION),
            buildBaseType(BASE_BASE_LOCATION),
            buildBaseType(BASE_FLAG_LAUNCHER),
            buildBaseType(BASE_COMPONENT_LAUNCHER));
    }

    private List<FieldTypeEntity> getPredefinedComplexBaseTypesList() {
        return Arrays.asList(
            buildBaseType(PREDEFINED_COMPLEX_LINK_REASON),
            buildBaseType(PREDEFINED_COMPLEX_CASELINK),
            buildBaseType(PREDEFINED_COMPLEX_SEARCH_PARTY),
            buildBaseType(PREDEFINED_COMPLEX_SEARCH_CRITERIA),
            buildBaseType(PREDEFINED_COMPLEX_FLAGS),
            buildBaseType(PREDEFINED_COMPLEX_JUDICIAL_USER),
            buildBaseType(PREDEFINED_COMPLEX_CHANGE_ORGANISATION_REQUEST),
            buildBaseType(PREDEFINED_COMPLEX_PREVIOUS_ORGANISATION),
            buildBaseType(PREDEFINED_COMPLEX_ORGANISATION),
            buildBaseType(PREDEFINED_COMPLEX_ORGANISATION_POLICY),
            buildBaseType(PREDEFINED_COMPLEX_ADDRESS_GLOBAL),
            buildBaseType(PREDEFINED_COMPLEX_ADDRESS_GLOBAL_UK),
            buildBaseType(PREDEFINED_COMPLEX_ADDRESS_UK),
            buildBaseType(PREDEFINED_COMPLEX_ORDER_SUMMARY),
            buildBaseType(PREDEFINED_COMPLEX_CASE_LOCATION),
            buildBaseType(PREDEFINED_COMPLEX_TTL),
            buildBaseType(PREDEFINED_COMPLEX_CASE_QUERIES_COLLECTION),
            buildBaseType(PREDEFINED_COMPLEX_CASE_MESSAGE),
            buildBaseType(PREDEFINED_COMPLEX_CASE_ACCESS_GROUP),
            buildBaseType(PREDEFINED_COMPLEX_CASE_ACCESS_GROUPS));
    }
}
