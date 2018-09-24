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
import uk.gov.hmcts.ccd.definition.store.repository.entity.StateEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.StateUserRoleEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.UserRoleEntity;

import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.ccd.definition.store.excel.parser.AuthorisationCaseTypeParserTest.buildSheetForCaseType;
import static uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName.*;

@RunWith(MockitoJUnitRunner.class)
public class AuthorisationCaseStateParserTest {
    private static final String CASE_TYPE_UNDER_TEST = "Some Case Type";
    private static final String CASE_STATE_UNDER_TEST = "Some Case State";
    private AuthorisationCaseStateParser subject;
    private CaseTypeEntity caseTypeEntity;
    private StateEntity stateEntity;
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
        subject = new AuthorisationCaseStateParser(context, entityToDefinitionDataItemRegistry);
        caseTypeEntity = new CaseTypeEntity();
        caseTypeEntity.setReference(CASE_TYPE_UNDER_TEST);
        stateEntity = new StateEntity();
        stateEntity.setReference(CASE_STATE_UNDER_TEST);
        definitionSheets.put(AUTHORISATION_CASE_STATE.getName(), definitionSheet);
        definitionSheets.put(CASE_TYPE.getName(), buildSheetForCaseType());
        definitionSheets.put(STATE.getName(), buildSheetForCaseState());
    }

    @Test
    public void shouldParseEntity_withUserRoleFound() {

        final String role = "CaseWorker 1";

        final DefinitionDataItem item1 = new DefinitionDataItem(SheetName.AUTHORISATION_CASE_STATE.getName());
        item1.addAttribute(ColumnName.CASE_TYPE_ID.toString(), CASE_TYPE_UNDER_TEST);
        item1.addAttribute(ColumnName.STATE_ID.toString(), CASE_STATE_UNDER_TEST);
        item1.addAttribute(ColumnName.USER_ROLE.toString(), role);
        item1.addAttribute(ColumnName.CRUD.toString(), " CCCd  ");
        definitionSheet.addDataItem(item1);
        final Collection<StateUserRoleEntity> entities = subject.parseAll(definitionSheets, caseTypeEntity, stateEntity);
        assertThat(entities.size(), is(1));

        final StateUserRoleEntity stateUserRoleEntity = new ArrayList<>(entities).get(0);
        assertThat(stateUserRoleEntity.getCrudAsString(), is("CCCd"));
        assertThat(stateUserRoleEntity.getId(), is(nullValue()));
        assertThat(stateUserRoleEntity.getUserRole(), is(mockUserRoleEntity));
        assertThat(stateUserRoleEntity.getCreate(), is(true));
        assertThat(stateUserRoleEntity.getUpdate(), is(false));
        assertThat(stateUserRoleEntity.getRead(), is(false));
        assertThat(stateUserRoleEntity.getDelete(), is(true));

        assertThat(entityToDefinitionDataItemRegistry.getForEntity(stateUserRoleEntity), is(Optional.of(item1)));
    }

    @Test
    public void shouldParseEntity_withUserRoleNotFound() {

        final String role = "CaseWorker 2";

        final DefinitionDataItem item1 = new DefinitionDataItem(SheetName.AUTHORISATION_CASE_STATE.getName());
        item1.addAttribute(ColumnName.CASE_TYPE_ID.toString(), CASE_TYPE_UNDER_TEST);
        item1.addAttribute(ColumnName.STATE_ID.toString(), CASE_STATE_UNDER_TEST);
        item1.addAttribute(ColumnName.USER_ROLE.toString(), role);
        item1.addAttribute(ColumnName.CRUD.toString(), " CCCd  ");
        definitionSheet.addDataItem(item1);
        final Collection<StateUserRoleEntity> entities = subject.parseAll(definitionSheets, caseTypeEntity, stateEntity);
        assertThat(entities.size(), is(1));

        final StateUserRoleEntity stateUserRoleEntity = new ArrayList<>(entities).get(0);
        assertThat(stateUserRoleEntity.getCrudAsString(), is("CCCd"));
        assertThat(stateUserRoleEntity.getId(), is(nullValue()));
        assertThat(stateUserRoleEntity.getUserRole(), is(nullValue()));

        assertThat(entityToDefinitionDataItemRegistry.getForEntity(stateUserRoleEntity), is(Optional.of(item1)));
    }

    @Test
    public void shouldParseEntity_withInvalidCrud() {

        final String role = "CaseWorker 1";

        final DefinitionDataItem item1 = new DefinitionDataItem(SheetName.AUTHORISATION_CASE_STATE.getName());
        item1.addAttribute(ColumnName.CASE_TYPE_ID.toString(), CASE_TYPE_UNDER_TEST);
        item1.addAttribute(ColumnName.STATE_ID.toString(), CASE_STATE_UNDER_TEST);
        item1.addAttribute(ColumnName.USER_ROLE.toString(), role);
        item1.addAttribute(ColumnName.CRUD.toString(), " X y  ");
        definitionSheet.addDataItem(item1);
        final Collection<StateUserRoleEntity> entities = subject.parseAll(definitionSheets, caseTypeEntity, stateEntity);
        assertThat(entities.size(), is(1));

        final StateUserRoleEntity stateUserRoleEntity = new ArrayList<>(entities).get(0);
        assertThat(stateUserRoleEntity.getCrudAsString(), is("X y"));
        assertThat(stateUserRoleEntity.getId(), is(nullValue()));
        assertThat(stateUserRoleEntity.getUserRole(), is(mockUserRoleEntity));

        assertThat(entityToDefinitionDataItemRegistry.getForEntity(stateUserRoleEntity), is(Optional.of(item1)));
    }

    @Test
    public void shouldParseEntity_withInvalidCrudAndUserNotFound() {

        final String role = "CaseWorker 2";

        final DefinitionDataItem item1 = new DefinitionDataItem(SheetName.AUTHORISATION_CASE_STATE.getName());
        item1.addAttribute(ColumnName.CASE_TYPE_ID.toString(), CASE_TYPE_UNDER_TEST);
        item1.addAttribute(ColumnName.STATE_ID.toString(), CASE_STATE_UNDER_TEST);
        item1.addAttribute(ColumnName.USER_ROLE.toString(), role);
        item1.addAttribute(ColumnName.CRUD.toString(), " X y  ");
        definitionSheet.addDataItem(item1);
        final Collection<StateUserRoleEntity> entities = subject.parseAll(definitionSheets, caseTypeEntity, stateEntity);
        assertThat(entities.size(), is(1));

        final StateUserRoleEntity stateUserRoleEntityeUserRoleEntity = new ArrayList<>(entities).get(0);
        assertThat(stateUserRoleEntityeUserRoleEntity.getCrudAsString(), is("X y"));
        assertThat(stateUserRoleEntityeUserRoleEntity.getId(), is(nullValue()));
        assertThat(stateUserRoleEntityeUserRoleEntity.getUserRole(), is(nullValue()));

        assertThat(entityToDefinitionDataItemRegistry.getForEntity(stateUserRoleEntityeUserRoleEntity), is(Optional.of(item1)));
    }

    private DefinitionSheet buildSheetForCaseState() {
        final DefinitionSheet sheet = new DefinitionSheet();
        sheet.setName(STATE.getName());
        final DefinitionDataItem item = new DefinitionDataItem(STATE.getName());
        item.addAttribute(ColumnName.CASE_TYPE_ID, CASE_TYPE_UNDER_TEST);
        item.addAttribute(ColumnName.ID, CASE_STATE_UNDER_TEST);
        item.addAttribute(ColumnName.NAME, CASE_STATE_UNDER_TEST);
        sheet.addDataItem(item);
        return sheet;
    }
}
