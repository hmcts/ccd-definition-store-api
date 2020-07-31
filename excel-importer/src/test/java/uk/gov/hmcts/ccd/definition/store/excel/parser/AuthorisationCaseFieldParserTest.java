package uk.gov.hmcts.ccd.definition.store.excel.parser;

import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.ccd.definition.store.excel.parser.AuthorisationCaseTypeParserTest.buildSheetForCaseType;
import static uk.gov.hmcts.ccd.definition.store.excel.parser.ParserTestBase.CASE_FIELD_UNDER_TEST;
import static uk.gov.hmcts.ccd.definition.store.excel.parser.ParserTestBase.CASE_TYPE_UNDER_TEST;
import static uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName.*;
import static uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName.AUTHORISATION_CASE_FIELD;
import static uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName.CASE_FIELD;
import static uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName.CASE_TYPE;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionDataItem;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionSheet;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName;
import uk.gov.hmcts.ccd.definition.store.repository.entity.*;

public class AuthorisationCaseFieldParserTest {

    private AuthorisationCaseFieldParser subject;
    private CaseTypeEntity caseType;
    private CaseFieldEntity caseField;
    private Map<String, DefinitionSheet> definitionSheets = new HashMap<>();
    private final DefinitionSheet definitionSheet = new DefinitionSheet();

    @Mock
    private UserRoleEntity mockUserRoleEntity;

    private CaseRoleEntity caseRoleEntity;

    private EntityToDefinitionDataItemRegistry entityToDefinitionDataItemRegistry;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);
        final ParseContext context = new ParseContext();
        final String role = "CaseWorker 1";
        given(mockUserRoleEntity.getReference()).willReturn(role);
        context.registerUserRoles(Arrays.asList(mockUserRoleEntity));

        entityToDefinitionDataItemRegistry =  new EntityToDefinitionDataItemRegistry();
        subject = new AuthorisationCaseFieldParser(context, entityToDefinitionDataItemRegistry);
        caseType = new CaseTypeEntity();
        caseType.setReference(CASE_TYPE_UNDER_TEST);
        caseField = new CaseFieldEntity();
        caseField.setReference(CASE_FIELD_UNDER_TEST);
        definitionSheets.put(AUTHORISATION_CASE_FIELD.getName(), definitionSheet);
        definitionSheets.put(CASE_TYPE.getName(), buildSheetForCaseType());
        definitionSheets.put(CASE_FIELD.getName(), buildSheetForCaseField());

        final String caseRole = "[CLAIMANT]";
        caseRoleEntity = new CaseRoleEntity();
        caseRoleEntity.setReference(caseRole);
        caseRoleEntity.setCaseType(caseType);
        context.registerCaseRoles(Arrays.asList(caseRoleEntity));
    }

    @Test
    public void shouldParseEntityWithUserRoleFound() {
        final String role = "CaseWorker 1";

        final DefinitionDataItem item1 = new DefinitionDataItem(SheetName.AUTHORISATION_CASE_FIELD.getName());
        item1.addAttribute(CASE_TYPE_ID.toString(), CASE_TYPE_UNDER_TEST);
        item1.addAttribute(ColumnName.USER_ROLE.toString(), role);
        item1.addAttribute(CRUD.toString(), " CCCd  ");
        item1.addAttribute(CASE_FIELD_ID.toString(), CASE_FIELD_UNDER_TEST);

        definitionSheet.addDataItem(item1);
        subject.parseAndSetACLEntities(definitionSheets, caseType, Collections.singleton(caseField));
        Collection<CaseFieldACLEntity> entities = caseField.getCaseFieldACLEntities();
        assertThat(entities.size(), is(1));

        final CaseFieldACLEntity caseFieldACLEntity = new ArrayList<>(entities).get(0);
        assertThat(caseFieldACLEntity.getCrudAsString(), is("CCCd"));
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
        item1.addAttribute(CRUD.toString(), " CCCd  ");
        item1.addAttribute(CASE_FIELD_ID.toString(), CASE_FIELD_UNDER_TEST);

        definitionSheet.addDataItem(item1);
        subject.parseAndSetACLEntities(definitionSheets, caseType, Collections.singleton(caseField));
        final Collection<CaseFieldACLEntity> entities = caseField.getCaseFieldACLEntities();
        assertThat(entities.size(), is(1));

        final CaseFieldACLEntity caseFieldACLEntity = new ArrayList<>(entities).get(0);
        assertThat(caseFieldACLEntity.getCrudAsString(), is("CCCd"));
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
        final String role = "CaseWorker 2";

        final DefinitionDataItem item1 = new DefinitionDataItem(SheetName.AUTHORISATION_CASE_FIELD.getName());
        item1.addAttribute(CASE_TYPE_ID.toString(), CASE_TYPE_UNDER_TEST);
        item1.addAttribute(ColumnName.USER_ROLE.toString(), role);
        item1.addAttribute(CRUD.toString(), " CCCd  ");
        item1.addAttribute(CASE_FIELD_ID.toString(), CASE_FIELD_UNDER_TEST);

        definitionSheet.addDataItem(item1);
        subject.parseAndSetACLEntities(definitionSheets, caseType, Collections.singleton(caseField));
        Collection<CaseFieldACLEntity> entities = caseField.getCaseFieldACLEntities();
        assertThat(entities.size(), is(1));

        final CaseFieldACLEntity caseFieldACLEntity = new ArrayList<>(entities).get(0);
        assertThat(caseFieldACLEntity.getCrudAsString(), is("CCCd"));
        assertThat(caseFieldACLEntity.getId(), is(nullValue()));
        assertThat(caseFieldACLEntity.getUserRole(), is(nullValue()));

        assertThat(entityToDefinitionDataItemRegistry.getForEntity(caseFieldACLEntity), is(Optional.of(item1)));
    }

    @Test
    public void shouldParseEntityWithInvalidCrud() {
        final String role = "CaseWorker 1";

        final DefinitionDataItem item1 = new DefinitionDataItem(SheetName.AUTHORISATION_CASE_FIELD.getName());
        item1.addAttribute(CASE_TYPE_ID.toString(), CASE_TYPE_UNDER_TEST);
        item1.addAttribute(ColumnName.USER_ROLE.toString(), role);
        item1.addAttribute(CRUD.toString(), " X y  ");
        item1.addAttribute(CASE_FIELD_ID.toString(), CASE_FIELD_UNDER_TEST);

        definitionSheet.addDataItem(item1);
        subject.parseAndSetACLEntities(definitionSheets, caseType, Collections.singleton(caseField));
        Collection<CaseFieldACLEntity> entities = caseField.getCaseFieldACLEntities();
        assertThat(entities.size(), is(1));

        final CaseFieldACLEntity caseFieldACLEntity = new ArrayList<>(entities).get(0);
        assertThat(caseFieldACLEntity.getCrudAsString(), is("X y"));
        assertThat(caseFieldACLEntity.getId(), is(nullValue()));
        assertThat(caseFieldACLEntity.getUserRole(), is(mockUserRoleEntity));

        assertThat(entityToDefinitionDataItemRegistry.getForEntity(caseFieldACLEntity), is(Optional.of(item1)));
    }

    @Test
    public void shouldParseEntityWithInvalidCrudAndUserNotFound() {
        final String role = "CaseWorker 2";

        final DefinitionDataItem item1 = new DefinitionDataItem(SheetName.AUTHORISATION_CASE_FIELD.getName());
        item1.addAttribute(CASE_TYPE_ID.toString(), CASE_TYPE_UNDER_TEST);
        item1.addAttribute(USER_ROLE.toString(), role);
        item1.addAttribute(CRUD.toString(), " X y  ");
        item1.addAttribute(CASE_FIELD_ID.toString(), CASE_FIELD_UNDER_TEST);

        definitionSheet.addDataItem(item1);
        subject.parseAndSetACLEntities(definitionSheets, caseType, Collections.singleton(caseField));
        final Collection<CaseFieldACLEntity> entities = caseField.getCaseFieldACLEntities();
        assertThat(entities.size(), is(1));

        final CaseFieldACLEntity caseFieldACLEntity = new ArrayList<>(entities).get(0);
        assertThat(caseFieldACLEntity.getCrudAsString(), is("X y"));
        assertThat(caseFieldACLEntity.getId(), is(nullValue()));
        assertThat(caseFieldACLEntity.getUserRole(), is(nullValue()));

        assertThat(entityToDefinitionDataItemRegistry.getForEntity(caseFieldACLEntity), is(Optional.of(item1)));
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
