package uk.gov.hmcts.ccd.definition.store.excel.parser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionDataItem;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionSheet;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseRoleEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.StateACLEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.StateEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.UserRoleEntity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.ccd.definition.store.excel.parser.AuthorisationCaseTypeParserTest.buildSheetForCaseType;
import static uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName.AUTHORISATION_CASE_STATE;
import static uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName.CASE_TYPE;
import static uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName.STATE;

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

    private CaseRoleEntity caseRoleEntity;

    private EntityToDefinitionDataItemRegistry entityToDefinitionDataItemRegistry;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);
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


        final String caseRole = "[CLAIMANT]";
        caseRoleEntity = new CaseRoleEntity();
        caseRoleEntity.setReference(caseRole);
        caseRoleEntity.setCaseType(caseTypeEntity);
        context.registerCaseRoles(Arrays.asList(caseRoleEntity));
    }

    @Test
    public void shouldParseEntityWithUserRoleFound() {

        final String role = "CaseWorker 1";

        final DefinitionDataItem item1 = new DefinitionDataItem(SheetName.AUTHORISATION_CASE_STATE.getName());
        item1.addAttribute(ColumnName.CASE_TYPE_ID.toString(), CASE_TYPE_UNDER_TEST);
        item1.addAttribute(ColumnName.STATE_ID.toString(), CASE_STATE_UNDER_TEST);
        item1.addAttribute(ColumnName.USER_ROLE.toString(), role);
        item1.addAttribute(ColumnName.CRUD.toString(), " CCCd  ");
        definitionSheet.addDataItem(item1);
        final Collection<StateACLEntity> entities = subject.parseAll(definitionSheets, caseTypeEntity, stateEntity);
        assertThat(entities.size(), is(1));

        final StateACLEntity stateACLEntity = new ArrayList<>(entities).get(0);
        assertThat(stateACLEntity.getCrudAsString(), is("CCCd"));
        assertThat(stateACLEntity.getId(), is(nullValue()));
        assertThat(stateACLEntity.getUserRole(), is(mockUserRoleEntity));
        assertThat(stateACLEntity.getCreate(), is(true));
        assertThat(stateACLEntity.getUpdate(), is(false));
        assertThat(stateACLEntity.getRead(), is(false));
        assertThat(stateACLEntity.getDelete(), is(true));

        assertThat(entityToDefinitionDataItemRegistry.getForEntity(stateACLEntity), is(Optional.of(item1)));
    }

    @Test
    public void shouldParseEntityWithCaseRoleFound() {

        final String caseRole = "[CLAIMANT]";

        final DefinitionDataItem item1 = new DefinitionDataItem(SheetName.AUTHORISATION_CASE_TYPE.getName());
        item1.addAttribute(ColumnName.CASE_TYPE_ID.toString(), CASE_TYPE_UNDER_TEST);
        item1.addAttribute(ColumnName.STATE_ID.toString(), CASE_STATE_UNDER_TEST);
        item1.addAttribute(ColumnName.USER_ROLE.toString(), caseRole);
        item1.addAttribute(ColumnName.CRUD.toString(), " CCCd  ");
        definitionSheet.addDataItem(item1);
        final Collection<StateACLEntity> entities = subject.parseAll(definitionSheets, caseTypeEntity, stateEntity);
        assertThat(entities.size(), is(1));

        final StateACLEntity stateACLEntity = new ArrayList<>(entities).get(0);
        assertThat(stateACLEntity.getCrudAsString(), is("CCCd"));
        assertThat(stateACLEntity.getId(), is(nullValue()));
        assertThat(stateACLEntity.getUserRole(), is(caseRoleEntity));
        assertThat(stateACLEntity.getCreate(), is(true));
        assertThat(stateACLEntity.getUpdate(), is(false));
        assertThat(stateACLEntity.getRead(), is(false));
        assertThat(stateACLEntity.getDelete(), is(true));

        assertThat(entityToDefinitionDataItemRegistry.getForEntity(stateACLEntity), is(Optional.of(item1)));
    }

    @Test
    public void shouldParseEntityWithUserRoleNotFound() {

        final String role = "CaseWorker 2";

        final DefinitionDataItem item1 = new DefinitionDataItem(SheetName.AUTHORISATION_CASE_STATE.getName());
        item1.addAttribute(ColumnName.CASE_TYPE_ID.toString(), CASE_TYPE_UNDER_TEST);
        item1.addAttribute(ColumnName.STATE_ID.toString(), CASE_STATE_UNDER_TEST);
        item1.addAttribute(ColumnName.USER_ROLE.toString(), role);
        item1.addAttribute(ColumnName.CRUD.toString(), " CCCd  ");
        definitionSheet.addDataItem(item1);
        final Collection<StateACLEntity> entities = subject.parseAll(definitionSheets, caseTypeEntity, stateEntity);
        assertThat(entities.size(), is(1));

        final StateACLEntity stateACLEntity = new ArrayList<>(entities).get(0);
        assertThat(stateACLEntity.getCrudAsString(), is("CCCd"));
        assertThat(stateACLEntity.getId(), is(nullValue()));
        assertThat(stateACLEntity.getUserRole(), is(nullValue()));
        assertThat(stateACLEntity.getUserRoleId(), is(role));

        assertThat(entityToDefinitionDataItemRegistry.getForEntity(stateACLEntity), is(Optional.of(item1)));
    }

    @Test
    public void shouldParseEntityWithInvalidCrud() {

        final String role = "CaseWorker 1";

        final DefinitionDataItem item1 = new DefinitionDataItem(SheetName.AUTHORISATION_CASE_STATE.getName());
        item1.addAttribute(ColumnName.CASE_TYPE_ID.toString(), CASE_TYPE_UNDER_TEST);
        item1.addAttribute(ColumnName.STATE_ID.toString(), CASE_STATE_UNDER_TEST);
        item1.addAttribute(ColumnName.USER_ROLE.toString(), role);
        item1.addAttribute(ColumnName.CRUD.toString(), " X y  ");
        definitionSheet.addDataItem(item1);
        final Collection<StateACLEntity> entities = subject.parseAll(definitionSheets, caseTypeEntity, stateEntity);
        assertThat(entities.size(), is(1));

        final StateACLEntity stateACLEntity = new ArrayList<>(entities).get(0);
        assertThat(stateACLEntity.getCrudAsString(), is("X y"));
        assertThat(stateACLEntity.getId(), is(nullValue()));
        assertThat(stateACLEntity.getUserRole(), is(mockUserRoleEntity));

        assertThat(entityToDefinitionDataItemRegistry.getForEntity(stateACLEntity), is(Optional.of(item1)));
    }

    @Test
    public void shouldParseEntityWithInvalidCrudAndUserNotFound() {

        final String role = "CaseWorker 2";

        final DefinitionDataItem item1 = new DefinitionDataItem(SheetName.AUTHORISATION_CASE_STATE.getName());
        item1.addAttribute(ColumnName.CASE_TYPE_ID.toString(), CASE_TYPE_UNDER_TEST);
        item1.addAttribute(ColumnName.STATE_ID.toString(), CASE_STATE_UNDER_TEST);
        item1.addAttribute(ColumnName.USER_ROLE.toString(), role);
        item1.addAttribute(ColumnName.CRUD.toString(), " X y  ");
        definitionSheet.addDataItem(item1);
        final Collection<StateACLEntity> entities = subject.parseAll(definitionSheets, caseTypeEntity, stateEntity);
        assertThat(entities.size(), is(1));

        final StateACLEntity stateACLEntity = new ArrayList<>(entities).get(0);
        assertThat(stateACLEntity.getCrudAsString(), is("X y"));
        assertThat(stateACLEntity.getId(), is(nullValue()));
        assertThat(stateACLEntity.getUserRole(), is(nullValue()));
        assertThat(stateACLEntity.getUserRoleId(), is(role));

        assertThat(entityToDefinitionDataItemRegistry.getForEntity(stateACLEntity), is(Optional.of(item1)));
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
