package uk.gov.hmcts.ccd.definition.store.excel.parser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.ccd.definition.store.excel.endpoint.exception.MapperException;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionDataItem;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionSheet;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldACLEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseRoleEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.UserRoleEntity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.ccd.definition.store.excel.parser.AuthorisationCaseTypeParserTest.buildSheetForCaseType;
import static uk.gov.hmcts.ccd.definition.store.excel.parser.ParserTestBase.CASE_FIELD_UNDER_TEST;
import static uk.gov.hmcts.ccd.definition.store.excel.parser.ParserTestBase.CASE_TYPE_UNDER_TEST;
import static uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName.CASE_FIELD_ID;
import static uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName.CASE_TYPE_ID;
import static uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName.CRUD;
import static uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName.ID;
import static uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName.NAME;
import static uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName.USER_ROLE;
import static uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName.AUTHORISATION_CASE_FIELD;
import static uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName.CASE_FIELD;
import static uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName.CASE_TYPE;

public class AuthorisationCaseFieldParserTest {

    private static final String CASE_TYPE_INVALID = "Invalid Case Type";

    private static final String CASE_WORKER_ROLE_1 = "CaseWorker 1";
    private static final String CASE_WORKER_ROLE_2 = "CaseWorker 2";
    private static final String CASE_CRUD_1 = " CCCd  ";
    private static final String CASE_CRUD_2 = " X y  ";

    private AuthorisationCaseFieldParser subject;
    private CaseTypeEntity caseType;
    private CaseFieldEntity caseField;
    private Map<String, DefinitionSheet> definitionSheets;
    private DefinitionSheet definitionSheet;

    @Mock
    private UserRoleEntity mockUserRoleEntity;

    private CaseRoleEntity caseRoleEntity;

    private EntityToDefinitionDataItemRegistry entityToDefinitionDataItemRegistry;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);
        final ParseContext context = new ParseContext();
        given(mockUserRoleEntity.getReference()).willReturn(CASE_WORKER_ROLE_1);
        context.registerUserRoles(Collections.singletonList(mockUserRoleEntity));

        entityToDefinitionDataItemRegistry = new EntityToDefinitionDataItemRegistry();
        subject = new AuthorisationCaseFieldParser(context, entityToDefinitionDataItemRegistry);
        caseType = new CaseTypeEntity();
        caseType.setReference(CASE_TYPE_UNDER_TEST);
        caseField = new CaseFieldEntity();
        caseField.setReference(CASE_FIELD_UNDER_TEST);

        definitionSheets = new HashMap<>();
        definitionSheet = new DefinitionSheet();
        definitionSheets.put(AUTHORISATION_CASE_FIELD.getName(), definitionSheet);
        definitionSheets.put(CASE_TYPE.getName(), buildSheetForCaseType());
        definitionSheets.put(CASE_FIELD.getName(), buildSheetForCaseField());

        final String caseRole = "[CLAIMANT]";
        caseRoleEntity = new CaseRoleEntity();
        caseRoleEntity.setReference(caseRole);
        caseRoleEntity.setCaseType(caseType);
        context.registerCaseRoles(Collections.singletonList(caseRoleEntity));
    }

    @Test
    public void shouldParseEntityWithUserRoleFound() {
        final DefinitionDataItem item1 = new DefinitionDataItem(SheetName.AUTHORISATION_CASE_FIELD.getName());
        item1.addAttribute(CASE_TYPE_ID.toString(), CASE_TYPE_UNDER_TEST);
        item1.addAttribute(ColumnName.USER_ROLE.toString(), CASE_WORKER_ROLE_1);
        item1.addAttribute(CRUD.toString(), CASE_CRUD_1);
        item1.addAttribute(CASE_FIELD_ID.toString(), CASE_FIELD_UNDER_TEST);

        definitionSheet.addDataItem(item1);
        subject.parseAndSetACLEntities(definitionSheets, caseType, Collections.singleton(caseField));
        Collection<CaseFieldACLEntity> entities = caseField.getCaseFieldACLEntities();
        assertThat(entities.size(), is(1));

        final CaseFieldACLEntity caseFieldACLEntity = new ArrayList<>(entities).get(0);
        assertThat(caseFieldACLEntity.getCrudAsString(), is(CASE_CRUD_1.trim()));
        assertThat(caseFieldACLEntity.getId(), is(nullValue()));
        assertThat(caseFieldACLEntity.getUserRole(), is(mockUserRoleEntity));
        assertThat(caseFieldACLEntity.getCreate(), is(true));
        assertThat(caseFieldACLEntity.getUpdate(), is(false));
        assertThat(caseFieldACLEntity.getRead(), is(false));
        assertThat(caseFieldACLEntity.getDelete(), is(true));

        assertThat(entityToDefinitionDataItemRegistry.getForEntity(caseFieldACLEntity), is(Optional.of(item1)));
    }

    @Test
    public void shouldParseEntityWithCaseRoleFound() {
        final String caseRole = "[CLAIMANT]";

        final DefinitionDataItem item1 = new DefinitionDataItem(SheetName.AUTHORISATION_CASE_FIELD.getName());
        item1.addAttribute(CASE_TYPE_ID.toString(), CASE_TYPE_UNDER_TEST);
        item1.addAttribute(ColumnName.USER_ROLE.toString(), caseRole);
        item1.addAttribute(CRUD.toString(), CASE_CRUD_1);
        item1.addAttribute(CASE_FIELD_ID.toString(), CASE_FIELD_UNDER_TEST);

        definitionSheet.addDataItem(item1);
        subject.parseAndSetACLEntities(definitionSheets, caseType, Collections.singleton(caseField));
        final Collection<CaseFieldACLEntity> entities = caseField.getCaseFieldACLEntities();
        assertThat(entities.size(), is(1));

        final CaseFieldACLEntity caseFieldACLEntity = new ArrayList<>(entities).get(0);
        assertThat(caseFieldACLEntity.getCrudAsString(), is(CASE_CRUD_1.trim()));
        assertThat(caseFieldACLEntity.getId(), is(nullValue()));
        assertThat(caseFieldACLEntity.getUserRole(), is(caseRoleEntity));
        assertThat(caseFieldACLEntity.getCreate(), is(true));
        assertThat(caseFieldACLEntity.getUpdate(), is(false));
        assertThat(caseFieldACLEntity.getRead(), is(false));
        assertThat(caseFieldACLEntity.getDelete(), is(true));

        assertThat(entityToDefinitionDataItemRegistry.getForEntity(caseFieldACLEntity), is(Optional.of(item1)));
    }

    @Test
    public void shouldParseEntityWithUserRoleNotFound() {
        final DefinitionDataItem item1 = new DefinitionDataItem(SheetName.AUTHORISATION_CASE_FIELD.getName());
        item1.addAttribute(CASE_TYPE_ID.toString(), CASE_TYPE_UNDER_TEST);
        item1.addAttribute(ColumnName.USER_ROLE.toString(), CASE_WORKER_ROLE_2);
        item1.addAttribute(CRUD.toString(), CASE_CRUD_1);
        item1.addAttribute(CASE_FIELD_ID.toString(), CASE_FIELD_UNDER_TEST);

        definitionSheet.addDataItem(item1);
        subject.parseAndSetACLEntities(definitionSheets, caseType, Collections.singleton(caseField));
        Collection<CaseFieldACLEntity> entities = caseField.getCaseFieldACLEntities();
        assertThat(entities.size(), is(1));

        final CaseFieldACLEntity caseFieldACLEntity = new ArrayList<>(entities).get(0);
        assertThat(caseFieldACLEntity.getCrudAsString(), is(CASE_CRUD_1.trim()));
        assertThat(caseFieldACLEntity.getId(), is(nullValue()));
        assertThat(caseFieldACLEntity.getUserRole(), is(nullValue()));

        assertThat(entityToDefinitionDataItemRegistry.getForEntity(caseFieldACLEntity), is(Optional.of(item1)));
    }

    @Test
    public void shouldParseEntityWithInvalidCrud() {
        final DefinitionDataItem item1 = new DefinitionDataItem(SheetName.AUTHORISATION_CASE_FIELD.getName());
        item1.addAttribute(CASE_TYPE_ID.toString(), CASE_TYPE_UNDER_TEST);
        item1.addAttribute(ColumnName.USER_ROLE.toString(), CASE_WORKER_ROLE_1);
        item1.addAttribute(CRUD.toString(), CASE_CRUD_2);
        item1.addAttribute(CASE_FIELD_ID.toString(), CASE_FIELD_UNDER_TEST);

        definitionSheet.addDataItem(item1);
        subject.parseAndSetACLEntities(definitionSheets, caseType, Collections.singleton(caseField));
        Collection<CaseFieldACLEntity> entities = caseField.getCaseFieldACLEntities();
        assertThat(entities.size(), is(1));

        final CaseFieldACLEntity caseFieldACLEntity = new ArrayList<>(entities).get(0);
        assertThat(caseFieldACLEntity.getCrudAsString(), is(CASE_CRUD_2.trim()));
        assertThat(caseFieldACLEntity.getId(), is(nullValue()));
        assertThat(caseFieldACLEntity.getUserRole(), is(mockUserRoleEntity));

        assertThat(entityToDefinitionDataItemRegistry.getForEntity(caseFieldACLEntity), is(Optional.of(item1)));
    }

    @Test
    public void shouldParseEntityWithInvalidCrudAndUserNotFound() {
        final DefinitionDataItem item1 = new DefinitionDataItem(SheetName.AUTHORISATION_CASE_FIELD.getName());
        item1.addAttribute(CASE_TYPE_ID.toString(), CASE_TYPE_UNDER_TEST);
        item1.addAttribute(USER_ROLE.toString(), CASE_WORKER_ROLE_2);
        item1.addAttribute(CRUD.toString(), CASE_CRUD_2);
        item1.addAttribute(CASE_FIELD_ID.toString(), CASE_FIELD_UNDER_TEST);

        definitionSheet.addDataItem(item1);
        subject.parseAndSetACLEntities(definitionSheets, caseType, Collections.singleton(caseField));
        final Collection<CaseFieldACLEntity> entities = caseField.getCaseFieldACLEntities();
        assertThat(entities.size(), is(1));

        final CaseFieldACLEntity caseFieldACLEntity = new ArrayList<>(entities).get(0);
        assertThat(caseFieldACLEntity.getCrudAsString(), is(CASE_CRUD_2.trim()));
        assertThat(caseFieldACLEntity.getId(), is(nullValue()));
        assertThat(caseFieldACLEntity.getUserRole(), is(nullValue()));

        assertThat(entityToDefinitionDataItemRegistry.getForEntity(caseFieldACLEntity), is(Optional.of(item1)));
    }

    @Test
    public void shouldParseEntityWithInvalidCaseField() {
        CaseFieldEntity caseField1 = new CaseFieldEntity();
        caseField1.setReference("Invalid case field");

        final DefinitionDataItem item1 = new DefinitionDataItem(SheetName.AUTHORISATION_CASE_FIELD.getName());
        item1.addAttribute(CASE_TYPE_ID.toString(), CASE_TYPE_UNDER_TEST);
        item1.addAttribute(ColumnName.USER_ROLE.toString(), CASE_WORKER_ROLE_1);
        item1.addAttribute(CRUD.toString(), CASE_CRUD_1);
        item1.addAttribute(CASE_FIELD_ID.toString(), CASE_FIELD_UNDER_TEST);
        definitionSheet.addDataItem(item1);

        subject.parseAndSetACLEntities(definitionSheets, caseType, Collections.singleton(caseField1));
        Collection<CaseFieldACLEntity> entities = caseField.getCaseFieldACLEntities();
        assertThat(entities.size(), is(0));
    }

    @Test
    void shouldParseEntityWithInvalidCaseType() {
        final DefinitionDataItem item1 = new DefinitionDataItem(SheetName.AUTHORISATION_CASE_FIELD.getName());
        item1.addAttribute(CASE_TYPE_ID.toString(), CASE_TYPE_INVALID);
        item1.addAttribute(ColumnName.USER_ROLE.toString(), CASE_WORKER_ROLE_1);
        item1.addAttribute(CRUD.toString(), CASE_CRUD_1);
        item1.addAttribute(CASE_FIELD_ID.toString(), CASE_FIELD_UNDER_TEST);
        definitionSheet.addDataItem(item1);

        final MapperException mapperException = assertThrows(MapperException.class,
            () -> subject.parseAndSetACLEntities(definitionSheets, caseType, Collections.singleton(caseField)));
        assertEquals(String.format("Unknown Case Type '%s' in worksheet '%s'",
            CASE_TYPE_INVALID, SheetName.AUTHORISATION_CASE_FIELD.getName()), mapperException.getMessage());
    }

    @Test
    void shouldParseEntityWithNoDefinitions() {
        final MapperException mapperException = assertThrows(MapperException.class,
            () -> subject.parseAndSetACLEntities(null, caseType, Collections.singleton(caseField)));
        assertEquals("A definition must contain a sheet", mapperException.getMessage());
    }

    @Test
    void shouldParseEntityWithEmptyDefinitions() {
        Map<String, DefinitionSheet> definitionSheetMap = new HashMap<>();

        final MapperException mapperException = assertThrows(MapperException.class,
            () -> subject.parseAndSetACLEntities(definitionSheetMap, caseType, Collections.singleton(caseField)));
        assertEquals("A definition must contain a sheet", mapperException.getMessage());
    }

    @Test
    void shouldParseEntityWithNoDefinitionSheet() {
        Map<String, DefinitionSheet> definitionSheetMap = new HashMap<>();
        definitionSheetMap.put(CASE_TYPE.getName(), buildSheetForCaseType());
        definitionSheetMap.put(CASE_FIELD.getName(), buildSheetForCaseField());

        final MapperException mapperException = assertThrows(MapperException.class,
            () -> subject.parseAndSetACLEntities(definitionSheetMap, caseType, Collections.singleton(caseField)));
        assertEquals(String.format("A definition must contain a '%s' sheet",
            SheetName.AUTHORISATION_CASE_FIELD.getName()), mapperException.getMessage());
    }

    @Test
    void shouldParseEntityWithNoDataItems() {
        subject.parseAndSetACLEntities(definitionSheets, caseType, Collections.singleton(caseField));
        final Collection<CaseFieldACLEntity> entities = caseField.getCaseFieldACLEntities();

        assertThat(entities.size(), is(0));
    }

    static DefinitionSheet buildSheetForCaseField() {
        final DefinitionSheet sheet = new DefinitionSheet();
        sheet.setName(CASE_FIELD.getName());
        final DefinitionDataItem item = new DefinitionDataItem(CASE_FIELD.getName());
        item.addAttribute(CASE_TYPE_ID, CASE_TYPE_UNDER_TEST);
        item.addAttribute(ID, CASE_FIELD_UNDER_TEST);
        item.addAttribute(NAME, CASE_FIELD_UNDER_TEST);
        sheet.addDataItem(item);
        return sheet;
    }
}
