package uk.gov.hmcts.ccd.definition.store.excel.service;

import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.hmcts.ccd.definition.store.domain.service.FieldTypeService;
import uk.gov.hmcts.ccd.definition.store.domain.service.JurisdictionService;
import uk.gov.hmcts.ccd.definition.store.domain.service.LayoutService;
import uk.gov.hmcts.ccd.definition.store.domain.service.casetype.CaseTypeService;
import uk.gov.hmcts.ccd.definition.store.domain.service.workbasket.WorkBasketUserDefaultService;
import uk.gov.hmcts.ccd.definition.store.domain.showcondition.ShowConditionParser;
import uk.gov.hmcts.ccd.definition.store.domain.validation.fieldtype.FieldTypeValidationContextFactory;
import uk.gov.hmcts.ccd.definition.store.excel.parser.EntityToDefinitionDataItemRegistry;
import uk.gov.hmcts.ccd.definition.store.excel.parser.ParserFactory;
import uk.gov.hmcts.ccd.definition.store.excel.parser.SpreadsheetParser;
import uk.gov.hmcts.ccd.definition.store.excel.parser.SpreadsheetParsingException;
import uk.gov.hmcts.ccd.definition.store.excel.validation.SpreadsheetValidator;
import uk.gov.hmcts.ccd.definition.store.repository.UserRoleRepository;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.JurisdictionEntity;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Optional;

import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils.*;

@RunWith(MockitoJUnitRunner.class)
public class ImportServiceImplTest {

    public static final String BAD_FILE = "CCD_TestDefinition_V12.xlsx";
    private static final String GOOD_FILE = "CCD_TestDefinition.xlsx";
    private static final String JURISDICTION_NAME = "TEST";

    private ImportServiceImpl service;

    private SpreadsheetParser spreadsheetParser;

    private ParserFactory parserFactory;

    @Mock
    private FieldTypeService fieldTypeService;

    private FieldTypeValidationContextFactory fieldTypevalidationContextFactory;

    @Mock
    private SpreadsheetValidator spreadsheetValidator;

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
    private FieldTypeEntity labelBaseType;
    private FieldTypeEntity casePaymentHistoryViewerBaseType;

    @Before
    public void setup() {

        parserFactory = new ParserFactory(new ShowConditionParser(), new EntityToDefinitionDataItemRegistry());

        spreadsheetParser = new SpreadsheetParser(spreadsheetValidator);

        service = new ImportServiceImpl(spreadsheetValidator,
                                        spreadsheetParser,
                                        parserFactory,
                                        fieldTypeService,
                                        jurisdictionService,
                                        caseTypeService,
                                        layoutService,
                                        userRoleRepository,
                                        workBasketUserDefaultService);

        fixedTypeBaseType = buildBaseType(BASE_FIXED_LIST);
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
        labelBaseType = buildBaseType(BASE_LABEL);
        casePaymentHistoryViewerBaseType = buildBaseType(BASE_CASE_PAYMENT_HISTORY_VIEWER);

        given(jurisdiction.getReference()).willReturn(JURISDICTION_NAME);
    }

    @Test(expected = SpreadsheetParsingException.class)
    public void shouldNotImportDefinition() throws Exception {

        given(jurisdictionService.get(JURISDICTION_NAME)).willReturn(Optional.of(jurisdiction));
        given(fieldTypeService.getBaseTypes()).willReturn(Arrays.asList(fixedTypeBaseType,
                                                                        multiSelectBaseType,
                                                                        complexType));
        given(fieldTypeService.getTypesByJurisdiction(JURISDICTION_NAME)).willReturn(Lists.newArrayList());

        final InputStream inputStream = ClassLoader.getSystemResourceAsStream(BAD_FILE);

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
                                                                        labelBaseType,
                                                                        casePaymentHistoryViewerBaseType));
        given(fieldTypeService.getTypesByJurisdiction(JURISDICTION_NAME)).willReturn(Lists.newArrayList());

        final InputStream inputStream = ClassLoader.getSystemResourceAsStream(GOOD_FILE);

        service.importFormDefinitions(inputStream);
    }

    private FieldTypeEntity buildBaseType(final String reference) {
        FieldTypeEntity fieldTypeEntity = new FieldTypeEntity();
        fieldTypeEntity.setReference(reference);
        return fieldTypeEntity;
    }

}
