package uk.gov.hmcts.ccd.definition.store.excel.parser;

import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName.AUTHORISATION_CASE_TYPE;
import static uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName.CASE_TYPE;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionDataItem;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionSheet;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseRoleEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeACLEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.UserRoleEntity;

@RunWith(MockitoJUnitRunner.class)
public class AuthorisationCaseTypeParserTest {

    private static final String CASE_TYPE_UNDER_TEST = "Some Case Type";
    private AuthorisationCaseTypeParser subject;
    private CaseTypeEntity caseType;
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

        entityToDefinitionDataItemRegistry = new EntityToDefinitionDataItemRegistry();
        subject = new AuthorisationCaseTypeParser(context, entityToDefinitionDataItemRegistry);
        caseType = new CaseTypeEntity();
        caseType.setReference(CASE_TYPE_UNDER_TEST);
        definitionSheets.put(AUTHORISATION_CASE_TYPE.getName(), definitionSheet);
        definitionSheets.put(CASE_TYPE.getName(), buildSheetForCaseType());

        final String caseRole = "[CLAIMANT]";
        caseRoleEntity = new CaseRoleEntity();
        caseRoleEntity.setReference(caseRole);
        caseRoleEntity.setCaseType(caseType);
        context.registerCaseRoles(Arrays.asList(caseRoleEntity));
    }

    @Test
    public void shouldParseEntityWithUserRoleFound() {

        final String role = "CaseWorker 1";

        final DefinitionDataItem item1 = new DefinitionDataItem(SheetName.AUTHORISATION_CASE_TYPE.getName());
        item1.addAttribute(ColumnName.CASE_TYPE_ID.toString(), CASE_TYPE_UNDER_TEST);
        item1.addAttribute(ColumnName.USER_ROLE.toString(), role);
        item1.addAttribute(ColumnName.CRUD.toString(), " CCCd  ");
        definitionSheet.addDataItem(item1);
        final Collection<CaseTypeACLEntity> entities = subject.parseAll(definitionSheets, caseType);
        assertThat(entities.size(), is(1));

        final CaseTypeACLEntity caseTypeACLEntity = new ArrayList<>(entities).get(0);
        assertThat(caseTypeACLEntity.getCrudAsString(), is("CCCd"));
        assertThat(caseTypeACLEntity.getId(), is(nullValue()));
        assertThat(caseTypeACLEntity.getUserRole(), is(mockUserRoleEntity));
        assertThat(caseTypeACLEntity.getCreate(), is(true));
        assertThat(caseTypeACLEntity.getUpdate(), is(false));
        assertThat(caseTypeACLEntity.getRead(), is(false));
        assertThat(caseTypeACLEntity.getDelete(), is(true));

        assertThat(entityToDefinitionDataItemRegistry.getForEntity(caseTypeACLEntity), is(Optional.of(item1)));
    }

    @Test
    public void shouldParseEntityWithCaseRoleFound() {

        final String caseRole = "[CLAIMANT]";

        final DefinitionDataItem item1 = new DefinitionDataItem(SheetName.AUTHORISATION_CASE_TYPE.getName());
        item1.addAttribute(ColumnName.CASE_TYPE_ID.toString(), CASE_TYPE_UNDER_TEST);
        item1.addAttribute(ColumnName.USER_ROLE.toString(), caseRole);
        item1.addAttribute(ColumnName.CRUD.toString(), " CCCd  ");
        definitionSheet.addDataItem(item1);
        final Collection<CaseTypeACLEntity> entities = subject.parseAll(definitionSheets, caseType);
        assertThat(entities.size(), is(1));

        final CaseTypeACLEntity caseTypeACLEntity = new ArrayList<>(entities).get(0);
        assertThat(caseTypeACLEntity.getCrudAsString(), is("CCCd"));
        assertThat(caseTypeACLEntity.getId(), is(nullValue()));
        assertThat(caseTypeACLEntity.getUserRole(), is(caseRoleEntity));
        assertThat(caseTypeACLEntity.getCreate(), is(true));
        assertThat(caseTypeACLEntity.getUpdate(), is(false));
        assertThat(caseTypeACLEntity.getRead(), is(false));
        assertThat(caseTypeACLEntity.getDelete(), is(true));

        assertThat(entityToDefinitionDataItemRegistry.getForEntity(caseTypeACLEntity), is(Optional.of(item1)));
    }

    @Test
    public void shouldParseEntityWithUserRoleNotFound() {

        final String role = "CaseWorker 2";

        final DefinitionDataItem item1 = new DefinitionDataItem(SheetName.AUTHORISATION_CASE_TYPE.getName());
        item1.addAttribute(ColumnName.CASE_TYPE_ID.toString(), CASE_TYPE_UNDER_TEST);
        item1.addAttribute(ColumnName.USER_ROLE.toString(), role);
        item1.addAttribute(ColumnName.CRUD.toString(), " CCCd  ");
        definitionSheet.addDataItem(item1);
        final Collection<CaseTypeACLEntity> entities = subject.parseAll(definitionSheets, caseType);
        assertThat(entities.size(), is(1));

        final CaseTypeACLEntity caseTypeACLEntity = new ArrayList<>(entities).get(0);
        assertThat(caseTypeACLEntity.getCrudAsString(), is("CCCd"));
        assertThat(caseTypeACLEntity.getId(), is(nullValue()));
        assertThat(caseTypeACLEntity.getUserRole(), is(nullValue()));

        assertThat(entityToDefinitionDataItemRegistry.getForEntity(caseTypeACLEntity), is(Optional.of(item1)));
    }

    @Test
    public void shouldParseEntityWithInvalidCrud() {

        final String role = "CaseWorker 1";

        final DefinitionDataItem item1 = new DefinitionDataItem(SheetName.AUTHORISATION_CASE_TYPE.getName());
        item1.addAttribute(ColumnName.CASE_TYPE_ID.toString(), CASE_TYPE_UNDER_TEST);
        item1.addAttribute(ColumnName.USER_ROLE.toString(), role);
        item1.addAttribute(ColumnName.CRUD.toString(), " X y  ");
        definitionSheet.addDataItem(item1);
        final Collection<CaseTypeACLEntity> entities = subject.parseAll(definitionSheets, caseType);
        assertThat(entities.size(), is(1));

        final CaseTypeACLEntity caseTypeACLEntity = new ArrayList<>(entities).get(0);
        assertThat(caseTypeACLEntity.getCrudAsString(), is("X y"));
        assertThat(caseTypeACLEntity.getId(), is(nullValue()));
        assertThat(caseTypeACLEntity.getUserRole(), is(mockUserRoleEntity));

        assertThat(entityToDefinitionDataItemRegistry.getForEntity(caseTypeACLEntity), is(Optional.of(item1)));
    }

    @Test
    public void shouldParseEntityWithInvalidCrudAndUserNotFound() {

        final String role = "CaseWorker 2";

        final DefinitionDataItem item1 = new DefinitionDataItem(SheetName.AUTHORISATION_CASE_TYPE.getName());
        item1.addAttribute(ColumnName.CASE_TYPE_ID.toString(), CASE_TYPE_UNDER_TEST);
        item1.addAttribute(ColumnName.USER_ROLE.toString(), role);
        item1.addAttribute(ColumnName.CRUD.toString(), " X y  ");
        definitionSheet.addDataItem(item1);
        final Collection<CaseTypeACLEntity> entities = subject.parseAll(definitionSheets, caseType);
        assertThat(entities.size(), is(1));

        final CaseTypeACLEntity caseTypeACLEntity = new ArrayList<>(entities).get(0);
        assertThat(caseTypeACLEntity.getCrudAsString(), is("X y"));
        assertThat(caseTypeACLEntity.getId(), is(nullValue()));
        assertThat(caseTypeACLEntity.getUserRole(), is(nullValue()));

        assertThat(entityToDefinitionDataItemRegistry.getForEntity(caseTypeACLEntity), is(Optional.of(item1)));
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
