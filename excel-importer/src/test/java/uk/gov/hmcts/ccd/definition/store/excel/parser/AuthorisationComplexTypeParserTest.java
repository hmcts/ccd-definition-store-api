package uk.gov.hmcts.ccd.definition.store.excel.parser;

import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.ccd.definition.store.excel.endpoint.exception.MapperException;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionDataItem;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionSheet;
import uk.gov.hmcts.ccd.definition.store.repository.entity.AccessProfileEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseRoleEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.ComplexFieldACLEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.ComplexFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldTypeEntity;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.ccd.definition.store.excel.parser.AuthorisationCaseFieldParserTest.buildSheetForCaseField;
import static uk.gov.hmcts.ccd.definition.store.excel.parser.AuthorisationCaseTypeParserTest.buildSheetForCaseType;
import static uk.gov.hmcts.ccd.definition.store.excel.parser.ParserTestBase.CASE_FIELD_UNDER_TEST;
import static uk.gov.hmcts.ccd.definition.store.excel.parser.ParserTestBase.CASE_TYPE_UNDER_TEST;
import static uk.gov.hmcts.ccd.definition.store.excel.parser.ParserTestBase.COMPLEX_FIELD_UNDER_TEST;
import static uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName.ACCESS_PROFILE;
import static uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName.CASE_FIELD_ID;
import static uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName.CASE_TYPE_ID;
import static uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName.CRUD;
import static uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName.FIELD_TYPE;
import static uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName.ID;
import static uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName.LIST_ELEMENT_CODE;
import static uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName.NAME;
import static uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName.AUTHORISATION_COMPLEX_TYPE;
import static uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName.CASE_FIELD;
import static uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName.CASE_TYPE;
import static uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName.COMPLEX_TYPES;

public class AuthorisationComplexTypeParserTest {

    protected static final String CASE_FIELD_2 = "Some Case Field 2";
    protected static final String COMPLEX_FIELD_2 = "Some Complex Field 2";
    private static final String ELEMENT_CODE_1 = "Element Code 1";
    private static final String ELEMENT_CODE_2 = "Element Code 2";
    private static final String ELEMENT_CODE_3 = "Element Code 3";
    private static final String ELEMENT_CODE_FIELD_TYPE_1 = "Field Type 1";
    private static final String ELEMENT_CODE_FIELD_TYPE_2 = "Field Type 2";
    private AuthorisationComplexTypeParser classUnderTest;
    private CaseTypeEntity caseType;
    private CaseFieldEntity caseField;
    private CaseFieldEntity caseField2;
    private ComplexFieldEntity complexField1;
    private ComplexFieldEntity complexField2;
    private ComplexFieldEntity complexField3;
    private Map<String, DefinitionSheet> definitionSheets = new HashMap<>();
    private final DefinitionSheet definitionSheet = new DefinitionSheet();

    private static final String TEST_ACCESS_PROFILE_FOUND = "CaseWorker 1";
    private static final String TEST_ACCESS_PROFILE_NOT_FOUND = "CaseWorker 2";
    private static final String TEST_CASE_ROLE_FOUND = "[CLAIMANT]";

    @Mock
    private AccessProfileEntity mockAccessProfileEntity;

    private CaseRoleEntity caseRoleEntity;

    private EntityToDefinitionDataItemRegistry entityToDefinitionDataItemRegistry;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        final ParseContext context = new ParseContext();
        given(mockAccessProfileEntity.getReference()).willReturn(TEST_ACCESS_PROFILE_FOUND);
        context.registerAccessProfiles(Arrays.asList(mockAccessProfileEntity));

        entityToDefinitionDataItemRegistry = new EntityToDefinitionDataItemRegistry();
        classUnderTest = new AuthorisationComplexTypeParser(context, entityToDefinitionDataItemRegistry);
        caseType = new CaseTypeEntity();
        caseType.setReference(CASE_TYPE_UNDER_TEST);

        caseField = new CaseFieldEntity();
        caseField.setReference(CASE_FIELD_UNDER_TEST);
        caseType.addCaseField(caseField);

        caseField2 = new CaseFieldEntity();
        caseField2.setReference(CASE_FIELD_2);
        caseType.addCaseField(caseField2);

        complexField1 = new ComplexFieldEntity();
        complexField1.setReference(ELEMENT_CODE_1);

        complexField2 = new ComplexFieldEntity();
        complexField2.setReference(ELEMENT_CODE_2);

        FieldTypeEntity complex = new FieldTypeEntity();
        complex.setReference("Complex");

        FieldTypeEntity fieldType1 = new FieldTypeEntity();
        fieldType1.setBaseFieldType(complex);
        fieldType1.addComplexFields(Arrays.asList(complexField1, complexField2));
        caseField.setFieldType(fieldType1);

        complexField3 = new ComplexFieldEntity();
        complexField3.setReference(ELEMENT_CODE_3);
        FieldTypeEntity fieldType2 = new FieldTypeEntity();
        fieldType2.setBaseFieldType(complex);
        fieldType2.addComplexFields(Arrays.asList(complexField3));
        caseField2.setFieldType(fieldType2);

        definitionSheets.put(AUTHORISATION_COMPLEX_TYPE.getName(), definitionSheet);
        definitionSheets.put(CASE_TYPE.getName(), buildSheetForCaseType());
        definitionSheets.put(CASE_FIELD.getName(), buildSheetForCaseField());
        definitionSheets.put(COMPLEX_TYPES.getName(), buildSheetForComplexTypes());

        caseRoleEntity = new CaseRoleEntity();
        caseRoleEntity.setReference(TEST_CASE_ROLE_FOUND);
        caseRoleEntity.setCaseType(caseType);
        context.registerCaseRoles(Arrays.asList(caseRoleEntity));
    }

    @Test
    @DisplayName("should fail when no tab found")
    public void shouldNotParseWhenAuthorisationComplexTypeTabNotFound() {
        definitionSheets.remove(AUTHORISATION_COMPLEX_TYPE.getName());
        MapperException thrown = assertThrows(MapperException.class,
            () -> classUnderTest.parseAll(definitionSheets, caseType));
        assertThat(thrown.getMessage(), is("No AuthorisationComplexType tab found in configuration"));
    }

    @Test
    @DisplayName("should fail when caseField is not found")
    public void shouldNotParseWhenCaseFieldNotFound() {
        definitionSheets.remove(CASE_FIELD.getName());

        final DefinitionSheet sheet = new DefinitionSheet();
        sheet.setName(CASE_FIELD.getName());
        final DefinitionDataItem item = new DefinitionDataItem(CASE_FIELD.getName());
        item.addAttribute(CASE_TYPE_ID, CASE_TYPE_UNDER_TEST);
        item.addAttribute(ID, CASE_FIELD_UNDER_TEST + "_NOT");
        item.addAttribute(NAME, CASE_FIELD_UNDER_TEST + "_NOT");
        sheet.addDataItem(item);
        definitionSheets.put(CASE_FIELD.getName(), sheet);

        final DefinitionDataItem item1 = new DefinitionDataItem(AUTHORISATION_COMPLEX_TYPE.getName());
        item1.addAttribute(CASE_TYPE_ID.toString(), CASE_TYPE_UNDER_TEST);
        item1.addAttribute(CASE_FIELD_ID.toString(), CASE_FIELD_UNDER_TEST);
        item1.addAttribute(LIST_ELEMENT_CODE.toString(), ELEMENT_CODE_1);
        item1.addAttribute(ACCESS_PROFILE.toString(), TEST_ACCESS_PROFILE_FOUND);
        item1.addAttribute(CRUD.toString(), " CCCd  ");
        definitionSheet.addDataItem(item1);

        MapperException thrown = assertThrows(MapperException.class,
            () -> classUnderTest.parseAll(definitionSheets, caseType));
        assertThat(thrown.getMessage(), is("Unknown CaseField 'Some Case Field' for CaseType "
            + "'Some Case Type' in worksheet 'AuthorisationComplexType'"));
    }

    @Test
    @DisplayName("should parse when access profile found")
    public void shouldParseEntityWithAccessProfileFound() {
        final DefinitionDataItem item = new DefinitionDataItem(CASE_FIELD.getName());
        item.addAttribute(CASE_TYPE_ID, CASE_TYPE_UNDER_TEST);
        item.addAttribute(ID, CASE_FIELD_2);
        item.addAttribute(NAME, CASE_FIELD_2);
        definitionSheets.get(CASE_FIELD.getName()).addDataItem(item);

        final DefinitionDataItem item1 = new DefinitionDataItem(AUTHORISATION_COMPLEX_TYPE.getName());
        item1.addAttribute(CASE_TYPE_ID.toString(), CASE_TYPE_UNDER_TEST);
        item1.addAttribute(CASE_FIELD_ID.toString(), CASE_FIELD_UNDER_TEST);
        item1.addAttribute(LIST_ELEMENT_CODE.toString(), ELEMENT_CODE_1);
        item1.addAttribute(ACCESS_PROFILE.toString(), TEST_ACCESS_PROFILE_FOUND);
        item1.addAttribute(CRUD.toString(), " CCCd  ");
        definitionSheet.addDataItem(item1);
        final DefinitionDataItem item2 = new DefinitionDataItem(AUTHORISATION_COMPLEX_TYPE.getName());
        item2.addAttribute(CASE_TYPE_ID.toString(), CASE_TYPE_UNDER_TEST);
        item2.addAttribute(CASE_FIELD_ID.toString(), CASE_FIELD_UNDER_TEST);
        item2.addAttribute(LIST_ELEMENT_CODE.toString(), ELEMENT_CODE_2);
        item2.addAttribute(ACCESS_PROFILE.toString(), TEST_ACCESS_PROFILE_FOUND);
        item2.addAttribute(CRUD.toString(), " RRRRd  ");
        definitionSheet.addDataItem(item2);

        final DefinitionDataItem item3 = new DefinitionDataItem(AUTHORISATION_COMPLEX_TYPE.getName());
        item3.addAttribute(CASE_TYPE_ID.toString(), CASE_TYPE_UNDER_TEST);
        item3.addAttribute(CASE_FIELD_ID.toString(), CASE_FIELD_2);
        item3.addAttribute(LIST_ELEMENT_CODE.toString(), ELEMENT_CODE_3);
        item3.addAttribute(ACCESS_PROFILE.toString(), TEST_ACCESS_PROFILE_FOUND);
        item3.addAttribute(CRUD.toString(), " CUud  ");
        definitionSheet.addDataItem(item3);

        classUnderTest.parseAll(definitionSheets, caseType);

        final CaseFieldEntity caseFieldEntity = caseType.findCaseField(CASE_FIELD_UNDER_TEST)
            .orElseThrow(() -> new RuntimeException());
        final List<ComplexFieldACLEntity> entities = caseFieldEntity.getComplexFieldACLEntities();
        assertAll(
            () -> assertThat(entities.size(), is(2)),
            () -> assertThat(entities.get(0).getCrudAsString(), is("CCCd")),
            () -> assertThat(entities.get(0).getId(), is(nullValue())),
            () -> assertThat(entities.get(0).getAccessProfile(), is(mockAccessProfileEntity)),
            () -> assertThat(entities.get(0).getCreate(), is(true)),
            () -> assertThat(entities.get(0).getUpdate(), is(false)),
            () -> assertThat(entities.get(0).getRead(), is(false)),
            () -> assertThat(entities.get(0).getDelete(), is(true)),
            () -> assertThat(entityToDefinitionDataItemRegistry.getForEntity(entities.get(0)), is(Optional.of(item1))),
            () -> assertThat(entities.get(1).getCrudAsString(), is("RRRRd")),
            () -> assertThat(entities.get(1).getId(), is(nullValue())),
            () -> assertThat(entities.get(1).getAccessProfile(), is(mockAccessProfileEntity)),
            () -> assertThat(entities.get(1).getCreate(), is(false)),
            () -> assertThat(entities.get(1).getUpdate(), is(false)),
            () -> assertThat(entities.get(1).getRead(), is(true)),
            () -> assertThat(entities.get(1).getDelete(), is(true)),
            () -> assertThat(entityToDefinitionDataItemRegistry.getForEntity(entities.get(1)), is(Optional.of(item2)))
        );

        final CaseFieldEntity caseFieldEntity2 = caseType.findCaseField(CASE_FIELD_2)
            .orElseThrow(() -> new RuntimeException());
        final List<ComplexFieldACLEntity> entities2 = caseFieldEntity2.getComplexFieldACLEntities();
        assertAll(
            () -> assertThat(entities2.size(), is(1)),
            () -> assertThat(entities2.get(0).getCrudAsString(), is("CUud")),
            () -> assertThat(entities2.get(0).getId(), is(nullValue())),
            () -> assertThat(entities2.get(0).getAccessProfile(), is(mockAccessProfileEntity)),
            () -> assertThat(entities2.get(0).getCreate(), is(true)),
            () -> assertThat(entities2.get(0).getUpdate(), is(true)),
            () -> assertThat(entities2.get(0).getRead(), is(false)),
            () -> assertThat(entities2.get(0).getDelete(), is(true)),
            () -> assertThat(entityToDefinitionDataItemRegistry.getForEntity(entities2.get(0)), is(Optional.of(item3)))
        );
    }

    @Test
    @DisplayName("should parse when case role found")
    public void shouldParseEntityWithCaseRoleFound() {
        final DefinitionDataItem item1 = new DefinitionDataItem(AUTHORISATION_COMPLEX_TYPE.getName());
        item1.addAttribute(CASE_TYPE_ID.toString(), CASE_TYPE_UNDER_TEST);
        item1.addAttribute(CASE_FIELD_ID.toString(), CASE_FIELD_UNDER_TEST);
        item1.addAttribute(LIST_ELEMENT_CODE.toString(), ELEMENT_CODE_1);
        item1.addAttribute(ACCESS_PROFILE.toString(), TEST_CASE_ROLE_FOUND);
        item1.addAttribute(CRUD.toString(), " CCCd  ");
        definitionSheet.addDataItem(item1);

        classUnderTest.parseAll(definitionSheets, caseType);

        final CaseFieldEntity caseFieldEntity = caseType.findCaseField(CASE_FIELD_UNDER_TEST)
            .orElseThrow(() -> new RuntimeException());
        final ComplexFieldACLEntity complexFieldACLEntity = caseFieldEntity.getComplexFieldACLEntities().get(0);

        assertAll(
            () -> assertThat(complexFieldACLEntity.getCrudAsString(), is("CCCd")),
            () -> assertThat(complexFieldACLEntity.getId(), is(nullValue())),
            () -> assertThat(complexFieldACLEntity.getAccessProfile(), is(caseRoleEntity)),
            () -> assertThat(complexFieldACLEntity.getCreate(), is(true)),
            () -> assertThat(complexFieldACLEntity.getUpdate(), is(false)),
            () -> assertThat(complexFieldACLEntity.getRead(), is(false)),
            () -> assertThat(complexFieldACLEntity.getDelete(), is(true)),
            () -> assertThat(
                entityToDefinitionDataItemRegistry.getForEntity(complexFieldACLEntity), is(Optional.of(item1)))
        );
    }

    @Test
    @DisplayName("should parse when user access profile not found")
    public void shouldParseEntityWithAccessProfileNotFound() {
        final DefinitionDataItem item1 = new DefinitionDataItem(AUTHORISATION_COMPLEX_TYPE.getName());
        item1.addAttribute(CASE_TYPE_ID.toString(), CASE_TYPE_UNDER_TEST);
        item1.addAttribute(CASE_FIELD_ID.toString(), CASE_FIELD_UNDER_TEST);
        item1.addAttribute(LIST_ELEMENT_CODE.toString(), ELEMENT_CODE_1);
        item1.addAttribute(ACCESS_PROFILE.toString(), TEST_ACCESS_PROFILE_NOT_FOUND);
        item1.addAttribute(CRUD.toString(), " CCCd  ");
        definitionSheet.addDataItem(item1);

        classUnderTest.parseAll(definitionSheets, caseType);

        final CaseFieldEntity caseFieldEntity = caseType.findCaseField(CASE_FIELD_UNDER_TEST)
            .orElseThrow(() -> new RuntimeException());
        final ComplexFieldACLEntity complexFieldACLEntity = caseFieldEntity.getComplexFieldACLEntities().get(0);
        ;
        assertAll(
            () -> assertThat(complexFieldACLEntity.getCrudAsString(), is("CCCd")),
            () -> assertThat(complexFieldACLEntity.getId(), is(nullValue())),
            () -> assertThat(
                entityToDefinitionDataItemRegistry.getForEntity(complexFieldACLEntity), is(Optional.of(item1)))
        );
    }

    @Test
    @DisplayName("should parse when CRUD is invalid")
    public void shouldParseEntityWithInvalidCrud() {
        final DefinitionDataItem item1 = new DefinitionDataItem(AUTHORISATION_COMPLEX_TYPE.getName());
        item1.addAttribute(CASE_TYPE_ID.toString(), CASE_TYPE_UNDER_TEST);
        item1.addAttribute(CASE_FIELD_ID.toString(), CASE_FIELD_UNDER_TEST);
        item1.addAttribute(LIST_ELEMENT_CODE.toString(), ELEMENT_CODE_1);
        item1.addAttribute(ACCESS_PROFILE.toString(), TEST_ACCESS_PROFILE_FOUND);
        item1.addAttribute(CRUD.toString(), " X y  ");
        definitionSheet.addDataItem(item1);

        classUnderTest.parseAll(definitionSheets, caseType);

        final CaseFieldEntity caseFieldEntity = caseType.findCaseField(CASE_FIELD_UNDER_TEST)
            .orElseThrow(() -> new RuntimeException());
        final ComplexFieldACLEntity complexFieldACLEntity = caseFieldEntity.getComplexFieldACLEntities().get(0);
        ;
        assertAll(
            () -> assertThat(complexFieldACLEntity.getCrudAsString(), is("X y")),
            () -> assertThat(complexFieldACLEntity.getId(), is(nullValue())),
            () -> assertThat(complexFieldACLEntity.getAccessProfile(), is(mockAccessProfileEntity)),
            () -> assertThat(
                entityToDefinitionDataItemRegistry.getForEntity(complexFieldACLEntity), is(Optional.of(item1)))
        );
    }

    @Test
    @DisplayName("should parse when access profile not found and CRUD is invalid")
    public void shouldParseEntityWithInvalidCrudAndAccessProfileNotFound() {
        final DefinitionDataItem item1 = new DefinitionDataItem(AUTHORISATION_COMPLEX_TYPE.getName());
        item1.addAttribute(CASE_TYPE_ID.toString(), CASE_TYPE_UNDER_TEST);
        item1.addAttribute(CASE_FIELD_ID.toString(), CASE_FIELD_UNDER_TEST);
        item1.addAttribute(LIST_ELEMENT_CODE.toString(), ELEMENT_CODE_1);
        item1.addAttribute(ACCESS_PROFILE.toString(), TEST_ACCESS_PROFILE_NOT_FOUND);
        item1.addAttribute(CRUD.toString(), " X y  ");
        definitionSheet.addDataItem(item1);

        classUnderTest.parseAll(definitionSheets, caseType);

        final CaseFieldEntity caseFieldEntity = caseType.findCaseField(CASE_FIELD_UNDER_TEST)
            .orElseThrow(() -> new RuntimeException());
        final ComplexFieldACLEntity complexFieldACLEntity = caseFieldEntity.getComplexFieldACLEntities().get(0);
        ;
        assertAll(
            () -> assertThat(complexFieldACLEntity.getCrudAsString(), is("X y")),
            () -> assertThat(complexFieldACLEntity.getId(), is(nullValue())),
            () -> assertThat(complexFieldACLEntity.getAccessProfile(), is(nullValue())),
            () -> assertThat(
                entityToDefinitionDataItemRegistry.getForEntity(complexFieldACLEntity), is(Optional.of(item1)))
        );
    }

    static DefinitionSheet buildSheetForComplexTypes() {
        final DefinitionSheet sheet = new DefinitionSheet();
        sheet.setName(COMPLEX_TYPES.getName());
        final DefinitionDataItem item = new DefinitionDataItem(COMPLEX_TYPES.getName());
        item.addAttribute(ID, COMPLEX_FIELD_UNDER_TEST);
        item.addAttribute(LIST_ELEMENT_CODE, ELEMENT_CODE_1);
        item.addAttribute(FIELD_TYPE, ELEMENT_CODE_FIELD_TYPE_1);
        sheet.addDataItem(item);
        final DefinitionDataItem item2 = new DefinitionDataItem(COMPLEX_TYPES.getName());
        item2.addAttribute(ID, COMPLEX_FIELD_UNDER_TEST);
        item2.addAttribute(LIST_ELEMENT_CODE, ELEMENT_CODE_2);
        item2.addAttribute(FIELD_TYPE, ELEMENT_CODE_FIELD_TYPE_2);
        sheet.addDataItem(item2);
        final DefinitionDataItem item3 = new DefinitionDataItem(COMPLEX_TYPES.getName());
        item3.addAttribute(ID, COMPLEX_FIELD_2);
        item3.addAttribute(LIST_ELEMENT_CODE, ELEMENT_CODE_1);
        item3.addAttribute(FIELD_TYPE, ELEMENT_CODE_FIELD_TYPE_1);
        sheet.addDataItem(item3);
        final DefinitionDataItem item4 = new DefinitionDataItem(COMPLEX_TYPES.getName());
        item4.addAttribute(ID, COMPLEX_FIELD_2);
        item4.addAttribute(LIST_ELEMENT_CODE, ELEMENT_CODE_2);
        item4.addAttribute(FIELD_TYPE, ELEMENT_CODE_FIELD_TYPE_2);
        sheet.addDataItem(item4);

        return sheet;
    }
}
