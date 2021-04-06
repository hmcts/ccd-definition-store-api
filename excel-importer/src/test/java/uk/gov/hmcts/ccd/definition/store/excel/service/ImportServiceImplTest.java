package uk.gov.hmcts.ccd.definition.store.excel.service;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils.BASE_BASE_LOCATION;
import static uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils.BASE_CASE_HISTORY_VIEWER;
import static uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils.BASE_CASE_PAYMENT_HISTORY_VIEWER;
import static uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils.BASE_COLLECTION;
import static uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils.BASE_COMPLEX;
import static uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils.BASE_DATE;
import static uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils.BASE_DATE_TIME;
import static uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils.BASE_DOCUMENT;
import static uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils.BASE_DYNAMIC_LIST;
import static uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils.BASE_DYNAMIC_MULTI_SELECT_LIST;
import static uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils.BASE_DYNAMIC_RADIO_LIST;
import static uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils.BASE_EMAIL;
import static uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils.BASE_FIXED_LIST;
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
import static uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils.BASE_YES_OR_NO;
import static uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils.PREDEFINED_COMPLEX_CASE_LOCATION;
import static uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils.PREDEFINED_COMPLEX_CHANGE_ORGANISATION_REQUEST;
import static uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils.PREDEFINED_COMPLEX_ORGANISATION;
import static uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils.PREDEFINED_COMPLEX_ORGANISATION_POLICY;
import static uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils.PREDEFINED_COMPLEX_PREVIOUS_ORGANISATION;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.ApplicationEventPublisher;

import com.google.common.collect.Lists;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import uk.gov.hmcts.ccd.definition.store.excel.domain.definition.model.DefinitionFileUploadMetadata;
import uk.gov.hmcts.ccd.definition.store.excel.endpoint.exception.InvalidImportException;
import uk.gov.hmcts.ccd.definition.store.excel.parser.ChallengeQuestionParser;
import uk.gov.hmcts.ccd.definition.store.excel.parser.EntityToDefinitionDataItemRegistry;
import uk.gov.hmcts.ccd.definition.store.excel.parser.MetadataCaseFieldEntityFactory;
import uk.gov.hmcts.ccd.definition.store.excel.parser.ParseContext;
import uk.gov.hmcts.ccd.definition.store.excel.parser.ParserFactory;
import uk.gov.hmcts.ccd.definition.store.excel.parser.SpreadsheetParser;
import uk.gov.hmcts.ccd.definition.store.excel.service.ImportServiceImpl;
import uk.gov.hmcts.ccd.definition.store.excel.validation.HiddenFieldsValidator;
import uk.gov.hmcts.ccd.definition.store.excel.validation.SpreadsheetValidator;
import uk.gov.hmcts.ccd.definition.store.domain.ApplicationParams;
import uk.gov.hmcts.ccd.definition.store.domain.service.FieldTypeService;
import uk.gov.hmcts.ccd.definition.store.domain.service.JurisdictionService;
import uk.gov.hmcts.ccd.definition.store.domain.service.JurisdictionUiConfigService;
import uk.gov.hmcts.ccd.definition.store.domain.service.LayoutService;
import uk.gov.hmcts.ccd.definition.store.domain.service.accessprofiles.RoleToAccessProfileService;
import uk.gov.hmcts.ccd.definition.store.domain.service.banner.BannerService;
import uk.gov.hmcts.ccd.definition.store.domain.service.casetype.CaseTypeService;
import uk.gov.hmcts.ccd.definition.store.domain.service.metadata.MetadataField;
import uk.gov.hmcts.ccd.definition.store.domain.service.question.ChallengeQuestionTabService;
import uk.gov.hmcts.ccd.definition.store.domain.service.workbasket.WorkBasketUserDefaultService;
import uk.gov.hmcts.ccd.definition.store.domain.showcondition.ShowConditionParser;
import uk.gov.hmcts.ccd.definition.store.domain.validation.MissingUserRolesException;
import uk.gov.hmcts.ccd.definition.store.event.DefinitionImportedEvent;
import uk.gov.hmcts.ccd.definition.store.repository.CaseFieldRepository;
import uk.gov.hmcts.ccd.definition.store.repository.UserRoleRepository;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.DataFieldType;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.JurisdictionEntity;
import uk.gov.hmcts.ccd.definition.store.rest.model.IdamProperties;
import uk.gov.hmcts.ccd.definition.store.rest.service.IdamProfileClient;


@RunWith(MockitoJUnitRunner.class)
public class ImportServiceImplTest {

    public static final String BAD_FILE = "CCD_TestDefinition_V12.xlsx";
    private static final String GOOD_FILE = "CCD_TestDefinition.xlsx";
    private static final String JURISDICTION_NAME = "TEST";
    private static final String TEST_ADDRESS_BOOK_CASE_TYPE = "TestAddressBookCase";
    private static final String TEST_COMPLEX_ADDRESS_BOOK_CASE_TYPE = "TestComplexAddressBookCase";

    private ImportServiceImpl service;

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
    private UserRoleRepository userRoleRepository;

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
    private ChallengeQuestionTabService challengeQuestionTabService;

    @Mock
    private RoleToAccessProfileService roleToAccessProfileService;

    @Mock
    private ApplicationParams applicationParams;

    private FieldTypeEntity fixedTypeBaseType;
    private FieldTypeEntity multiSelectBaseType;
    private FieldTypeEntity complexType;
    private FieldTypeEntity textBaseType;
    private FieldTypeEntity numberBaseType;
    private FieldTypeEntity emailBaseType;
    private FieldTypeEntity yesNoBaseType;
    private FieldTypeEntity dateBaseType;
    private FieldTypeEntity dateTimeBaseType;
    private FieldTypeEntity postCodeBaseType;
    private FieldTypeEntity moneyGBPBaseType;
    private FieldTypeEntity phoneUKBaseType;
    private FieldTypeEntity textAreaBaseType;
    private FieldTypeEntity collectionBaseType;
    private FieldTypeEntity documentBaseType;
    private FieldTypeEntity organisationBaseType;
    private FieldTypeEntity organisationPolicyBaseType;
    private FieldTypeEntity labelBaseType;
    private FieldTypeEntity casePaymentHistoryViewerBaseType;
    private FieldTypeEntity caseHistoryViewerBaseType;
    private FieldTypeEntity fixedListRadioTypeBaseType;
    private FieldTypeEntity dynamicListBaseType;
    private FieldTypeEntity dynamicRadioListBaseType;
    private FieldTypeEntity dynamicMultiSelectListBaseType;
    private FieldTypeEntity changeOrganisationRequest;
    private FieldTypeEntity previousOrganisationBaseType;
    private FieldTypeEntity caseLocationBaseType;
    private FieldTypeEntity regionBaseType;
    private FieldTypeEntity baseLocationBaseType;

    @Before
    public void setup() {
        Map<MetadataField, MetadataCaseFieldEntityFactory> registry = new HashMap<>();
        registry.put(MetadataField.STATE, metadataCaseFieldEntityFactory);

        final ParserFactory parserFactory = new ParserFactory(new ShowConditionParser(),
            new EntityToDefinitionDataItemRegistry(), registry, spreadsheetValidator, hiddenFieldsValidator,
            challengeQuestionParser, applicationParams);

        final SpreadsheetParser spreadsheetParser = new SpreadsheetParser(spreadsheetValidator);

        service = new ImportServiceImpl(spreadsheetValidator,
            spreadsheetParser,
            parserFactory,
            fieldTypeService,
            jurisdictionService,
            caseTypeService,
            layoutService,
            userRoleRepository,
            workBasketUserDefaultService,
            caseFieldRepository,
            applicationEventPublisher,
            idamProfileClient,
            bannerService,
            jurisdictionUiConfigService,
            challengeQuestionTabService,
            roleToAccessProfileService);

        fixedTypeBaseType = buildBaseType(BASE_FIXED_LIST);
        dynamicListBaseType = buildBaseType(BASE_DYNAMIC_LIST);
        multiSelectBaseType = buildBaseType(BASE_MULTI_SELECT_LIST);
        complexType = buildBaseType(BASE_COMPLEX);
        textBaseType = buildBaseType(BASE_TEXT);
        numberBaseType = buildBaseType(BASE_NUMBER);
        emailBaseType = buildBaseType(BASE_EMAIL);
        yesNoBaseType = buildBaseType(BASE_YES_OR_NO);
        dateBaseType = buildBaseType(BASE_DATE);
        dateTimeBaseType = buildBaseType(BASE_DATE_TIME);
        postCodeBaseType = buildBaseType(BASE_POST_CODE);
        moneyGBPBaseType = buildBaseType(BASE_MONEY_GBP);
        phoneUKBaseType = buildBaseType(BASE_PHONE_UK);
        textAreaBaseType = buildBaseType(BASE_TEXT_AREA);
        collectionBaseType = buildBaseType(BASE_COLLECTION);
        documentBaseType = buildBaseType(BASE_DOCUMENT);
        organisationBaseType = buildBaseType(PREDEFINED_COMPLEX_ORGANISATION);
        organisationPolicyBaseType = buildBaseType(PREDEFINED_COMPLEX_ORGANISATION_POLICY);
        labelBaseType = buildBaseType(BASE_LABEL);
        casePaymentHistoryViewerBaseType = buildBaseType(BASE_CASE_PAYMENT_HISTORY_VIEWER);
        caseHistoryViewerBaseType = buildBaseType(BASE_CASE_HISTORY_VIEWER);
        fixedListRadioTypeBaseType = buildBaseType(BASE_RADIO_FIXED_LIST);
        changeOrganisationRequest = buildBaseType(PREDEFINED_COMPLEX_CHANGE_ORGANISATION_REQUEST);
        previousOrganisationBaseType = buildBaseType(PREDEFINED_COMPLEX_PREVIOUS_ORGANISATION);
        caseLocationBaseType = buildBaseType(PREDEFINED_COMPLEX_CASE_LOCATION);
        regionBaseType = buildBaseType(BASE_REGION);
        baseLocationBaseType = buildBaseType(BASE_BASE_LOCATION);
        dynamicRadioListBaseType = buildBaseType(BASE_DYNAMIC_RADIO_LIST);
        dynamicMultiSelectListBaseType = buildBaseType(BASE_DYNAMIC_MULTI_SELECT_LIST);


        given(jurisdiction.getReference()).willReturn(JURISDICTION_NAME);

        final IdamProperties idamProperties = new IdamProperties();
        idamProperties.setId("445");
        idamProperties.setEmail("user@hmcts.net");

        doReturn(idamProperties).when(idamProfileClient).getLoggedInUserDetails();
    }

    @Test(expected = InvalidImportException.class)
    public void shouldNotImportDefinition() throws Exception {

        given(jurisdictionService.get(JURISDICTION_NAME)).willReturn(Optional.of(jurisdiction));
        given(fieldTypeService.getBaseTypes()).willReturn(Arrays.asList(fixedTypeBaseType,
            multiSelectBaseType,
            complexType));
        given(fieldTypeService.getTypesByJurisdiction(JURISDICTION_NAME)).willReturn(Lists.newArrayList());

        final InputStream inputStream = getClass().getClassLoader().getResourceAsStream(BAD_FILE);

        service.importFormDefinitions(inputStream);
    }

    @Test(expected = MissingUserRolesException.class)
    public void importDefinitionThrowsMissingUserRole() throws Exception {

        given(jurisdictionService.get(JURISDICTION_NAME)).willReturn(Optional.of(jurisdiction));
        given(fieldTypeService.getBaseTypes()).willReturn(Arrays.asList(fixedTypeBaseType,
            multiSelectBaseType,
            complexType,
            textBaseType,
            numberBaseType,
            emailBaseType,
            yesNoBaseType,
            dateBaseType,
            dateTimeBaseType,
            postCodeBaseType,
            moneyGBPBaseType,
            phoneUKBaseType,
            textAreaBaseType,
            collectionBaseType,
            documentBaseType,
            organisationBaseType,
            organisationPolicyBaseType,
            labelBaseType,
            casePaymentHistoryViewerBaseType,
            caseHistoryViewerBaseType,
            fixedListRadioTypeBaseType,
            dynamicListBaseType,
            changeOrganisationRequest,
            previousOrganisationBaseType,
            caseLocationBaseType,
            regionBaseType,
            baseLocationBaseType,
            dynamicRadioListBaseType,
            dynamicMultiSelectListBaseType));
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
        doThrow(MissingUserRolesException.class)
            .when(caseTypeService).createAll(any(JurisdictionEntity.class), any(Collection.class), any(Set.class));
        final InputStream inputStream = getClass().getClassLoader().getResourceAsStream(GOOD_FILE);

        service.importFormDefinitions(inputStream);
    }

    @Test
    public void shouldImportDefinition() throws Exception {

        given(jurisdictionService.get(JURISDICTION_NAME)).willReturn(Optional.of(jurisdiction));
        given(fieldTypeService.getBaseTypes()).willReturn(Arrays.asList(fixedTypeBaseType,
            multiSelectBaseType,
            complexType,
            textBaseType,
            numberBaseType,
            emailBaseType,
            yesNoBaseType,
            dateBaseType,
            dateTimeBaseType,
            postCodeBaseType,
            moneyGBPBaseType,
            phoneUKBaseType,
            textAreaBaseType,
            collectionBaseType,
            documentBaseType,
            organisationBaseType,
            organisationPolicyBaseType,
            labelBaseType,
            casePaymentHistoryViewerBaseType,
            caseHistoryViewerBaseType,
            fixedListRadioTypeBaseType,
            dynamicListBaseType,
            changeOrganisationRequest,
            previousOrganisationBaseType,
            dynamicRadioListBaseType,
            dynamicMultiSelectListBaseType));
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
        assertThat(eventCaptor.getValue().getContent().size(), equalTo(2));
    }

    @Test
    public void shouldReturnImportWarnings() {
        Map<MetadataField, MetadataCaseFieldEntityFactory> registry = new HashMap<>();
        registry.put(MetadataField.STATE, metadataCaseFieldEntityFactory);

        final ParserFactory parserFactory = new ParserFactory(new ShowConditionParser(),
            new EntityToDefinitionDataItemRegistry(), registry, spreadsheetValidator,
            hiddenFieldsValidator,challengeQuestionParser, applicationParams);

        final SpreadsheetParser spreadsheetParser = mock(SpreadsheetParser.class);

        service = new ImportServiceImpl(spreadsheetValidator,
            spreadsheetParser,
            parserFactory,
            fieldTypeService,
            jurisdictionService,
            caseTypeService,
            layoutService,
            userRoleRepository,
            workBasketUserDefaultService,
            caseFieldRepository,
            applicationEventPublisher,
            idamProfileClient,
            bannerService,
            jurisdictionUiConfigService,
            challengeQuestionTabService,
            roleToAccessProfileService);

        final List<String> importWarnings = Arrays.asList("Warning1", "Warning2");

        given(spreadsheetParser.getImportWarnings()).willReturn(importWarnings);

        final List<String> warnings = service.getImportWarnings();
        assertThat(warnings.size(), equalTo(2));
        assertThat(importWarnings, containsInAnyOrder("Warning1", "Warning2"));
        verify(spreadsheetParser).getImportWarnings();
    }

    private FieldTypeEntity buildBaseType(final String reference) {
        FieldTypeEntity fieldTypeEntity = new FieldTypeEntity();
        fieldTypeEntity.setReference(reference);
        return fieldTypeEntity;
    }
}
