package uk.gov.hmcts.ccd.definition.store.excel.parser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionDataItem;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionSheet;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName;
import uk.gov.hmcts.ccd.definition.store.repository.entity.AccessProfileEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseRoleEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.StateACLEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.StateEntity;

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

class AuthorisationCaseStateParserTest {
    private static final String CASE_TYPE_UNDER_TEST = "Some Case Type";
    private static final String CASE_STATE_UNDER_TEST = "Some Case State";
    private AuthorisationCaseStateParser subject;
    private CaseTypeEntity caseTypeEntity;
    private StateEntity stateEntity;
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
    void setup() {
        MockitoAnnotations.openMocks(this);
        final ParseContext context = new ParseContext();
        given(mockAccessProfileEntity.getReference()).willReturn(TEST_ACCESS_PROFILE_FOUND);
        context.registerAccessProfiles(Arrays.asList(mockAccessProfileEntity));

        entityToDefinitionDataItemRegistry = new EntityToDefinitionDataItemRegistry();
        subject = new AuthorisationCaseStateParser(context, entityToDefinitionDataItemRegistry);
        caseTypeEntity = new CaseTypeEntity();
        caseTypeEntity.setReference(CASE_TYPE_UNDER_TEST);
        stateEntity = new StateEntity();
        stateEntity.setReference(CASE_STATE_UNDER_TEST);
        definitionSheets.put(AUTHORISATION_CASE_STATE.getName(), definitionSheet);
        definitionSheets.put(CASE_TYPE.getName(), buildSheetForCaseType());
        definitionSheets.put(STATE.getName(), buildSheetForCaseState());

        caseRoleEntity = new CaseRoleEntity();
        caseRoleEntity.setReference(TEST_CASE_ROLE_FOUND);
        caseRoleEntity.setCaseType(caseTypeEntity);
        context.registerCaseRoles(Arrays.asList(caseRoleEntity));
    }

    @Test
    void shouldParseEntityWithAccessProfileFound() {
        final DefinitionDataItem item1 = new DefinitionDataItem(SheetName.AUTHORISATION_CASE_STATE.getName());
        item1.addAttribute(ColumnName.CASE_TYPE_ID.toString(), CASE_TYPE_UNDER_TEST);
        item1.addAttribute(ColumnName.STATE_ID.toString(), CASE_STATE_UNDER_TEST);
        item1.addAttribute(ColumnName.ACCESS_PROFILE.toString(), TEST_ACCESS_PROFILE_FOUND);
        item1.addAttribute(ColumnName.CRUD.toString(), " CCCd  ");
        definitionSheet.addDataItem(item1);
        final Collection<StateACLEntity> entities = subject.parseAll(definitionSheets, caseTypeEntity, stateEntity);
        assertThat(entities.size(), is(1));

        final StateACLEntity stateACLEntity = new ArrayList<>(entities).get(0);
        assertThat(stateACLEntity.getCrudAsString(), is("CCCd"));
        assertThat(stateACLEntity.getId(), is(nullValue()));
        assertThat(stateACLEntity.getAccessProfile(), is(mockAccessProfileEntity));
        assertThat(stateACLEntity.getCreate(), is(true));
        assertThat(stateACLEntity.getUpdate(), is(false));
        assertThat(stateACLEntity.getRead(), is(false));
        assertThat(stateACLEntity.getDelete(), is(true));

        assertThat(entityToDefinitionDataItemRegistry.getForEntity(stateACLEntity), is(Optional.of(item1)));
    }

    @Test
    void shouldParseEntityWithCaseRoleFound() {
        final DefinitionDataItem item1 = new DefinitionDataItem(SheetName.AUTHORISATION_CASE_TYPE.getName());
        item1.addAttribute(ColumnName.CASE_TYPE_ID.toString(), CASE_TYPE_UNDER_TEST);
        item1.addAttribute(ColumnName.STATE_ID.toString(), CASE_STATE_UNDER_TEST);
        item1.addAttribute(ColumnName.ACCESS_PROFILE.toString(), TEST_CASE_ROLE_FOUND);
        item1.addAttribute(ColumnName.CRUD.toString(), " CCCd  ");
        definitionSheet.addDataItem(item1);
        final Collection<StateACLEntity> entities = subject.parseAll(definitionSheets, caseTypeEntity, stateEntity);
        assertThat(entities.size(), is(1));

        final StateACLEntity stateACLEntity = new ArrayList<>(entities).get(0);
        assertThat(stateACLEntity.getCrudAsString(), is("CCCd"));
        assertThat(stateACLEntity.getId(), is(nullValue()));
        assertThat(stateACLEntity.getAccessProfile(), is(caseRoleEntity));
        assertThat(stateACLEntity.getCreate(), is(true));
        assertThat(stateACLEntity.getUpdate(), is(false));
        assertThat(stateACLEntity.getRead(), is(false));
        assertThat(stateACLEntity.getDelete(), is(true));

        assertThat(entityToDefinitionDataItemRegistry.getForEntity(stateACLEntity), is(Optional.of(item1)));
    }

    @Test
    void shouldParseEntityWithAccessProfileNotFound() {
        final DefinitionDataItem item1 = new DefinitionDataItem(SheetName.AUTHORISATION_CASE_STATE.getName());
        item1.addAttribute(ColumnName.CASE_TYPE_ID.toString(), CASE_TYPE_UNDER_TEST);
        item1.addAttribute(ColumnName.STATE_ID.toString(), CASE_STATE_UNDER_TEST);
        item1.addAttribute(ColumnName.ACCESS_PROFILE.toString(), TEST_ACCESS_PROFILE_NOT_FOUND);
        item1.addAttribute(ColumnName.CRUD.toString(), " CCCd  ");
        definitionSheet.addDataItem(item1);
        final Collection<StateACLEntity> entities = subject.parseAll(definitionSheets, caseTypeEntity, stateEntity);
        assertThat(entities.size(), is(1));

        final StateACLEntity stateACLEntity = new ArrayList<>(entities).get(0);
        assertThat(stateACLEntity.getCrudAsString(), is("CCCd"));
        assertThat(stateACLEntity.getId(), is(nullValue()));
        assertThat(stateACLEntity.getAccessProfile(), is(nullValue()));
        assertThat(stateACLEntity.getAccessProfileId(), is(TEST_ACCESS_PROFILE_NOT_FOUND));

        assertThat(entityToDefinitionDataItemRegistry.getForEntity(stateACLEntity), is(Optional.of(item1)));
    }

    @Test
    void shouldParseEntityWithInvalidCrud() {
        final DefinitionDataItem item1 = new DefinitionDataItem(SheetName.AUTHORISATION_CASE_STATE.getName());
        item1.addAttribute(ColumnName.CASE_TYPE_ID.toString(), CASE_TYPE_UNDER_TEST);
        item1.addAttribute(ColumnName.STATE_ID.toString(), CASE_STATE_UNDER_TEST);
        item1.addAttribute(ColumnName.ACCESS_PROFILE.toString(), TEST_ACCESS_PROFILE_FOUND);
        item1.addAttribute(ColumnName.CRUD.toString(), " X y  ");
        definitionSheet.addDataItem(item1);
        final Collection<StateACLEntity> entities = subject.parseAll(definitionSheets, caseTypeEntity, stateEntity);
        assertThat(entities.size(), is(1));

        final StateACLEntity stateACLEntity = new ArrayList<>(entities).get(0);
        assertThat(stateACLEntity.getCrudAsString(), is("X y"));
        assertThat(stateACLEntity.getId(), is(nullValue()));
        assertThat(stateACLEntity.getAccessProfile(), is(mockAccessProfileEntity));

        assertThat(entityToDefinitionDataItemRegistry.getForEntity(stateACLEntity), is(Optional.of(item1)));
    }

    @Test
    void shouldParseEntityWithInvalidCrudAndAccessProfileNotFound() {
        final DefinitionDataItem item1 = new DefinitionDataItem(SheetName.AUTHORISATION_CASE_STATE.getName());
        item1.addAttribute(ColumnName.CASE_TYPE_ID.toString(), CASE_TYPE_UNDER_TEST);
        item1.addAttribute(ColumnName.STATE_ID.toString(), CASE_STATE_UNDER_TEST);
        item1.addAttribute(ColumnName.ACCESS_PROFILE.toString(), TEST_ACCESS_PROFILE_NOT_FOUND);
        item1.addAttribute(ColumnName.CRUD.toString(), " X y  ");
        definitionSheet.addDataItem(item1);
        final Collection<StateACLEntity> entities = subject.parseAll(definitionSheets, caseTypeEntity, stateEntity);
        assertThat(entities.size(), is(1));

        final StateACLEntity stateACLEntity = new ArrayList<>(entities).get(0);
        assertThat(stateACLEntity.getCrudAsString(), is("X y"));
        assertThat(stateACLEntity.getId(), is(nullValue()));
        assertThat(stateACLEntity.getAccessProfile(), is(nullValue()));
        assertThat(stateACLEntity.getAccessProfileId(), is(TEST_ACCESS_PROFILE_NOT_FOUND));

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
