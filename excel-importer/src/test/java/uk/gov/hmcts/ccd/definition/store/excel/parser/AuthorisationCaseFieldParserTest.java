package uk.gov.hmcts.ccd.definition.store.excel.parser;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionDataItem;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionSheet;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName;
import uk.gov.hmcts.ccd.definition.store.repository.entity.*;

import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.ccd.definition.store.excel.parser.AuthorisationCaseTypeParserTest.buildSheetForCaseType;
import static uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName.*;

@RunWith(MockitoJUnitRunner.class)
public class AuthorisationCaseFieldParserTest {

    private static final String CASE_TYPE_UNDER_TEST = "Some Case Type";
    private static final String CASE_FIELD_UNDER_TEST = "Some Case Field";
    private AuthorisationCaseFieldParser subject;
    private CaseTypeEntity caseType;
    private CaseFieldEntity caseField;
    private Map<String, DefinitionSheet> definitionSheets = new HashMap<>();
    private final DefinitionSheet definitionSheet = new DefinitionSheet();

    @Mock
    private UserRoleEntity mockUserRoleEntity;

    private CaseRoleEntity caseRoleEntity;

    private EntityToDefinitionDataItemRegistry entityToDefinitionDataItemRegistry;

    @Before
    public void setup() {
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
    public void shouldParseEntity_withUserRoleFound() {

        final String role = "CaseWorker 1";

        final DefinitionDataItem item1 = new DefinitionDataItem(SheetName.AUTHORISATION_CASE_FIELD.getName());
        item1.addAttribute(ColumnName.CASE_TYPE_ID.toString(), CASE_TYPE_UNDER_TEST);
        item1.addAttribute(ColumnName.USER_ROLE.toString(), role);
        item1.addAttribute(ColumnName.CRUD.toString(), " CCCd  ");
        item1.addAttribute(ColumnName.CASE_FIELD_ID.toString(), CASE_FIELD_UNDER_TEST);

        definitionSheet.addDataItem(item1);
        final Collection<CaseFieldACLEntity> entities = subject.parseAll(definitionSheets, caseType, caseField);
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
    public void shouldParseEntity_withCaseRoleFound() {

        final String caseRole = "[CLAIMANT]";

        final DefinitionDataItem item1 = new DefinitionDataItem(SheetName.AUTHORISATION_CASE_FIELD.getName());
        item1.addAttribute(ColumnName.CASE_TYPE_ID.toString(), CASE_TYPE_UNDER_TEST);
        item1.addAttribute(ColumnName.USER_ROLE.toString(), caseRole);
        item1.addAttribute(ColumnName.CRUD.toString(), " CCCd  ");
        item1.addAttribute(ColumnName.CASE_FIELD_ID.toString(), CASE_FIELD_UNDER_TEST);

        definitionSheet.addDataItem(item1);
        final Collection<CaseFieldACLEntity> entities = subject.parseAll(definitionSheets, caseType, caseField);
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
    public void shouldParseEntity_withUserRoleNotFound() {

        final String role = "CaseWorker 2";

        final DefinitionDataItem item1 = new DefinitionDataItem(SheetName.AUTHORISATION_CASE_FIELD.getName());
        item1.addAttribute(ColumnName.CASE_TYPE_ID.toString(), CASE_TYPE_UNDER_TEST);
        item1.addAttribute(ColumnName.USER_ROLE.toString(), role);
        item1.addAttribute(ColumnName.CRUD.toString(), " CCCd  ");
        item1.addAttribute(ColumnName.CASE_FIELD_ID.toString(), CASE_FIELD_UNDER_TEST);

        definitionSheet.addDataItem(item1);
        final Collection<CaseFieldACLEntity> entities = subject.parseAll(definitionSheets, caseType, caseField);
        assertThat(entities.size(), is(1));

        final CaseFieldACLEntity caseFieldACLEntity = new ArrayList<>(entities).get(0);
        assertThat(caseFieldACLEntity.getCrudAsString(), is("CCCd"));
        assertThat(caseFieldACLEntity.getId(), is(nullValue()));
        assertThat(caseFieldACLEntity.getUserRole(), is(nullValue()));

        assertThat(entityToDefinitionDataItemRegistry.getForEntity(caseFieldACLEntity), is(Optional.of(item1)));
    }

    @Test
    public void shouldParseEntity_withInvalidCrud() {

        final String role = "CaseWorker 1";

        final DefinitionDataItem item1 = new DefinitionDataItem(SheetName.AUTHORISATION_CASE_FIELD.getName());
        item1.addAttribute(ColumnName.CASE_TYPE_ID.toString(), CASE_TYPE_UNDER_TEST);
        item1.addAttribute(ColumnName.USER_ROLE.toString(), role);
        item1.addAttribute(ColumnName.CRUD.toString(), " X y  ");
        item1.addAttribute(ColumnName.CASE_FIELD_ID.toString(), CASE_FIELD_UNDER_TEST);

        definitionSheet.addDataItem(item1);
        final Collection<CaseFieldACLEntity> entities = subject.parseAll(definitionSheets, caseType, caseField);
        assertThat(entities.size(), is(1));

        final CaseFieldACLEntity caseFieldACLEntity = new ArrayList<>(entities).get(0);
        assertThat(caseFieldACLEntity.getCrudAsString(), is("X y"));
        assertThat(caseFieldACLEntity.getId(), is(nullValue()));
        assertThat(caseFieldACLEntity.getUserRole(), is(mockUserRoleEntity));

        assertThat(entityToDefinitionDataItemRegistry.getForEntity(caseFieldACLEntity), is(Optional.of(item1)));
    }

    @Test
    public void shouldParseEntity_withInvalidCrudAndUserNotFound() {

        final String role = "CaseWorker 2";

        final DefinitionDataItem item1 = new DefinitionDataItem(SheetName.AUTHORISATION_CASE_FIELD.getName());
        item1.addAttribute(ColumnName.CASE_TYPE_ID.toString(), CASE_TYPE_UNDER_TEST);
        item1.addAttribute(ColumnName.USER_ROLE.toString(), role);
        item1.addAttribute(ColumnName.CRUD.toString(), " X y  ");
        item1.addAttribute(ColumnName.CASE_FIELD_ID.toString(), CASE_FIELD_UNDER_TEST);

        definitionSheet.addDataItem(item1);
        final Collection<CaseFieldACLEntity> entities = subject.parseAll(definitionSheets, caseType, caseField);
        assertThat(entities.size(), is(1));

        final CaseFieldACLEntity caseFieldACLEntity = new ArrayList<>(entities).get(0);
        assertThat(caseFieldACLEntity.getCrudAsString(), is("X y"));
        assertThat(caseFieldACLEntity.getId(), is(nullValue()));
        assertThat(caseFieldACLEntity.getUserRole(), is(nullValue()));

        assertThat(entityToDefinitionDataItemRegistry.getForEntity(caseFieldACLEntity), is(Optional.of(item1)));
    }

    private DefinitionSheet buildSheetForCaseField() {
        final DefinitionSheet sheet = new DefinitionSheet();
        sheet.setName(CASE_FIELD.getName());
        final DefinitionDataItem item = new DefinitionDataItem(CASE_FIELD.getName());
        item.addAttribute(ColumnName.CASE_TYPE_ID, CASE_TYPE_UNDER_TEST);
        item.addAttribute(ColumnName.ID, CASE_FIELD_UNDER_TEST);
        item.addAttribute(ColumnName.NAME, CASE_FIELD_UNDER_TEST);
        sheet.addDataItem(item);
        return sheet;
    }
}
