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
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeUserRoleEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.UserRoleEntity;

import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName.AUTHORISATION_CASE_TYPE;
import static uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName.CASE_TYPE;

@RunWith(MockitoJUnitRunner.class)
public class AuthorisationCaseTypeParserTest {

    private static final String CASE_TYPE_UNDER_TEST = "Some Case Type";
    private AuthorisationCaseTypeParser subject;
    private CaseTypeEntity caseType;
    private Map<String, DefinitionSheet> definitionSheets = new HashMap<>();
    private final DefinitionSheet definitionSheet = new DefinitionSheet();

    @Mock
    private UserRoleEntity mockUserRoleEntity;

    private EntityToDefinitionDataItemRegistry entityToDefinitionDataItemRegistry;

    @Before
    public void setup() {
        final ParseContext context = new ParseContext();
        final String role = "CaseWorker 1";
        given(mockUserRoleEntity.getReference()).willReturn(role);
        context.registerUserRoles(Arrays.asList(mockUserRoleEntity));

        entityToDefinitionDataItemRegistry = new EntityToDefinitionDataItemRegistry();
        subject = new AuthorisationCaseTypeParser(context, entityToDefinitionDataItemRegistry);
        caseType = new CaseTypeEntity();
        caseType.setReference(CASE_TYPE_UNDER_TEST);
        definitionSheets.put(AUTHORISATION_CASE_TYPE.getName(), definitionSheet);
        definitionSheets.put(CASE_TYPE.getName(), buildSheetForCaseType());
    }

    @Test
    public void shouldParseEntity_withUserRoleFound() {

        final String role = "CaseWorker 1";

        final DefinitionDataItem item1 = new DefinitionDataItem(SheetName.AUTHORISATION_CASE_TYPE.getName());
        item1.addAttribute(ColumnName.CASE_TYPE_ID.toString(), CASE_TYPE_UNDER_TEST);
        item1.addAttribute(ColumnName.USER_ROLE.toString(), role);
        item1.addAttribute(ColumnName.CRUD.toString(), " CCCd  ");
        definitionSheet.addDataItem(item1);
        final Collection<CaseTypeUserRoleEntity> entities = subject.parseAll(definitionSheets, caseType);
        assertThat(entities.size(), is(1));

        final CaseTypeUserRoleEntity caseTypeUserRoleEntity = new ArrayList<>(entities).get(0);
        assertThat(caseTypeUserRoleEntity.getCrudAsString(), is("CCCd"));
        assertThat(caseTypeUserRoleEntity.getId(), is(nullValue()));
        assertThat(caseTypeUserRoleEntity.getUserRole(), is(mockUserRoleEntity));
        assertThat(caseTypeUserRoleEntity.getCreate(), is(true));
        assertThat(caseTypeUserRoleEntity.getUpdate(), is(false));
        assertThat(caseTypeUserRoleEntity.getRead(), is(false));
        assertThat(caseTypeUserRoleEntity.getDelete(), is(true));

        assertThat(entityToDefinitionDataItemRegistry.getForEntity(caseTypeUserRoleEntity), is(Optional.of(item1)));
    }

    @Test
    public void shouldParseEntity_withUserRoleNotFound() {

        final String role = "CaseWorker 2";

        final DefinitionDataItem item1 = new DefinitionDataItem(SheetName.AUTHORISATION_CASE_TYPE.getName());
        item1.addAttribute(ColumnName.CASE_TYPE_ID.toString(), CASE_TYPE_UNDER_TEST);
        item1.addAttribute(ColumnName.USER_ROLE.toString(), role);
        item1.addAttribute(ColumnName.CRUD.toString(), " CCCd  ");
        definitionSheet.addDataItem(item1);
        final Collection<CaseTypeUserRoleEntity> entities = subject.parseAll(definitionSheets, caseType);
        assertThat(entities.size(), is(1));

        final CaseTypeUserRoleEntity caseTypeUserRoleEntity = new ArrayList<>(entities).get(0);
        assertThat(caseTypeUserRoleEntity.getCrudAsString(), is("CCCd"));
        assertThat(caseTypeUserRoleEntity.getId(), is(nullValue()));
        assertThat(caseTypeUserRoleEntity.getUserRole(), is(nullValue()));

        assertThat(entityToDefinitionDataItemRegistry.getForEntity(caseTypeUserRoleEntity), is(Optional.of(item1)));
    }

    @Test
    public void shouldParseEntity_withInvalidCrud() {

        final String role = "CaseWorker 1";

        final DefinitionDataItem item1 = new DefinitionDataItem(SheetName.AUTHORISATION_CASE_TYPE.getName());
        item1.addAttribute(ColumnName.CASE_TYPE_ID.toString(), CASE_TYPE_UNDER_TEST);
        item1.addAttribute(ColumnName.USER_ROLE.toString(), role);
        item1.addAttribute(ColumnName.CRUD.toString(), " X y  ");
        definitionSheet.addDataItem(item1);
        final Collection<CaseTypeUserRoleEntity> entities = subject.parseAll(definitionSheets, caseType);
        assertThat(entities.size(), is(1));

        final CaseTypeUserRoleEntity caseTypeUserRoleEntity = new ArrayList<>(entities).get(0);
        assertThat(caseTypeUserRoleEntity.getCrudAsString(), is("X y"));
        assertThat(caseTypeUserRoleEntity.getId(), is(nullValue()));
        assertThat(caseTypeUserRoleEntity.getUserRole(), is(mockUserRoleEntity));

        assertThat(entityToDefinitionDataItemRegistry.getForEntity(caseTypeUserRoleEntity), is(Optional.of(item1)));
    }

    @Test
    public void shouldParseEntity_withInvalidCrudAndUserNotFound() {

        final String role = "CaseWorker 2";

        final DefinitionDataItem item1 = new DefinitionDataItem(SheetName.AUTHORISATION_CASE_TYPE.getName());
        item1.addAttribute(ColumnName.CASE_TYPE_ID.toString(), CASE_TYPE_UNDER_TEST);
        item1.addAttribute(ColumnName.USER_ROLE.toString(), role);
        item1.addAttribute(ColumnName.CRUD.toString(), " X y  ");
        definitionSheet.addDataItem(item1);
        final Collection<CaseTypeUserRoleEntity> entities = subject.parseAll(definitionSheets, caseType);
        assertThat(entities.size(), is(1));

        final CaseTypeUserRoleEntity caseTypeUserRoleEntity = new ArrayList<>(entities).get(0);
        assertThat(caseTypeUserRoleEntity.getCrudAsString(), is("X y"));
        assertThat(caseTypeUserRoleEntity.getId(), is(nullValue()));
        assertThat(caseTypeUserRoleEntity.getUserRole(), is(nullValue()));

        assertThat(entityToDefinitionDataItemRegistry.getForEntity(caseTypeUserRoleEntity), is(Optional.of(item1)));
    }

    static DefinitionSheet buildSheetForCaseType() {
        final DefinitionSheet sheet = new DefinitionSheet();
        sheet.setName(SheetName.CASE_TYPE.getName());
        final DefinitionDataItem item = new DefinitionDataItem(SheetName.CASE_TYPE.getName());
        item.addAttribute(ColumnName.ID, CASE_TYPE_UNDER_TEST);
        item.addAttribute(ColumnName.NAME, CASE_TYPE_UNDER_TEST);
        sheet.addDataItem(item);
        return sheet;
    }
}
