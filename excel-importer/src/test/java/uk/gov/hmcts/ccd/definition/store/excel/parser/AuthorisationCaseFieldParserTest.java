package uk.gov.hmcts.ccd.definition.store.excel.parser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionDataItem;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionSheet;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName;
import uk.gov.hmcts.ccd.definition.store.repository.entity.AccessProfileEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldACLEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseRoleEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.BDDMockito.given;
import static uk.gov.hmcts.ccd.definition.store.excel.parser.AuthorisationCaseTypeParserTest.buildSheetForCaseType;
import static uk.gov.hmcts.ccd.definition.store.excel.parser.ParserTestBase.CASE_FIELD_UNDER_TEST;
import static uk.gov.hmcts.ccd.definition.store.excel.parser.ParserTestBase.CASE_TYPE_UNDER_TEST;
import static uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName.ACCESS_PROFILE;
import static uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName.CASE_FIELD_ID;
import static uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName.CASE_TYPE_ID;
import static uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName.CRUD;
import static uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName.ID;
import static uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName.NAME;
import static uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName.AUTHORISATION_CASE_FIELD;
import static uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName.CASE_FIELD;
import static uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName.CASE_TYPE;

public class AuthorisationCaseFieldParserTest {



    private AuthorisationCaseFieldParser subject;
    private CaseTypeEntity caseType;
    private CaseFieldEntity caseField;
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
        MockitoAnnotations.initMocks(this);
        final ParseContext context = new ParseContext();
        given(mockAccessProfileEntity.getReference()).willReturn(TEST_ACCESS_PROFILE_FOUND);
        context.registerAccessProfiles(Arrays.asList(mockAccessProfileEntity));

        entityToDefinitionDataItemRegistry = new EntityToDefinitionDataItemRegistry();
        subject = new AuthorisationCaseFieldParser(context, entityToDefinitionDataItemRegistry);
        caseType = new CaseTypeEntity();
        caseType.setReference(CASE_TYPE_UNDER_TEST);
        caseField = new CaseFieldEntity();
        caseField.setReference(CASE_FIELD_UNDER_TEST);
        definitionSheets.put(AUTHORISATION_CASE_FIELD.getName(), definitionSheet);
        definitionSheets.put(CASE_TYPE.getName(), buildSheetForCaseType());
        definitionSheets.put(CASE_FIELD.getName(), buildSheetForCaseField());

        caseRoleEntity = new CaseRoleEntity();
        caseRoleEntity.setReference(TEST_CASE_ROLE_FOUND);
        caseRoleEntity.setCaseType(caseType);
        context.registerCaseRoles(Arrays.asList(caseRoleEntity));
    }

    @Test
    public void shouldParseEntityWithAccessProfileFound() {
        final DefinitionDataItem item1 = new DefinitionDataItem(SheetName.AUTHORISATION_CASE_FIELD.getName());
        item1.addAttribute(CASE_TYPE_ID.toString(), CASE_TYPE_UNDER_TEST);
        item1.addAttribute(ACCESS_PROFILE.toString(), TEST_ACCESS_PROFILE_FOUND);
        item1.addAttribute(CRUD.toString(), " CCCd  ");
        item1.addAttribute(CASE_FIELD_ID.toString(), CASE_FIELD_UNDER_TEST);

        definitionSheet.addDataItem(item1);
        subject.parseAndSetACLEntities(definitionSheets, caseType, Collections.singleton(caseField));
        Collection<CaseFieldACLEntity> entities = caseField.getCaseFieldACLEntities();
        assertThat(entities.size(), is(1));

        final CaseFieldACLEntity caseFieldACLEntity = new ArrayList<>(entities).get(0);
        assertThat(caseFieldACLEntity.getCrudAsString(), is("CCCd"));
        assertThat(caseFieldACLEntity.getId(), is(nullValue()));
        assertThat(caseFieldACLEntity.getAccessProfile(), is(mockAccessProfileEntity));
        assertThat(caseFieldACLEntity.getCreate(), is(true));
        assertThat(caseFieldACLEntity.getUpdate(), is(false));
        assertThat(caseFieldACLEntity.getRead(), is(false));
        assertThat(caseFieldACLEntity.getDelete(), is(true));

        assertThat(entityToDefinitionDataItemRegistry.getForEntity(caseFieldACLEntity), is(Optional.of(item1)));
    }

    @Test
    public void shouldParseEntityWithCaseRoleFound() {
        final DefinitionDataItem item1 = new DefinitionDataItem(SheetName.AUTHORISATION_CASE_FIELD.getName());
        item1.addAttribute(CASE_TYPE_ID.toString(), CASE_TYPE_UNDER_TEST);
        item1.addAttribute(ACCESS_PROFILE.toString(), TEST_CASE_ROLE_FOUND);
        item1.addAttribute(CRUD.toString(), " CCCd  ");
        item1.addAttribute(CASE_FIELD_ID.toString(), CASE_FIELD_UNDER_TEST);

        definitionSheet.addDataItem(item1);
        subject.parseAndSetACLEntities(definitionSheets, caseType, Collections.singleton(caseField));
        final Collection<CaseFieldACLEntity> entities = caseField.getCaseFieldACLEntities();
        assertThat(entities.size(), is(1));

        final CaseFieldACLEntity caseFieldACLEntity = new ArrayList<>(entities).get(0);
        assertThat(caseFieldACLEntity.getCrudAsString(), is("CCCd"));
        assertThat(caseFieldACLEntity.getId(), is(nullValue()));
        assertThat(caseFieldACLEntity.getAccessProfile(), is(caseRoleEntity));
        assertThat(caseFieldACLEntity.getCreate(), is(true));
        assertThat(caseFieldACLEntity.getUpdate(), is(false));
        assertThat(caseFieldACLEntity.getRead(), is(false));
        assertThat(caseFieldACLEntity.getDelete(), is(true));

        assertThat(entityToDefinitionDataItemRegistry.getForEntity(caseFieldACLEntity), is(Optional.of(item1)));
    }

    @Test
    public void shouldParseEntityWithAccessProfileNotFound() {
        final DefinitionDataItem item1 = new DefinitionDataItem(SheetName.AUTHORISATION_CASE_FIELD.getName());
        item1.addAttribute(CASE_TYPE_ID.toString(), CASE_TYPE_UNDER_TEST);
        item1.addAttribute(ACCESS_PROFILE.toString(), TEST_ACCESS_PROFILE_NOT_FOUND);
        item1.addAttribute(CRUD.toString(), " CCCd  ");
        item1.addAttribute(CASE_FIELD_ID.toString(), CASE_FIELD_UNDER_TEST);

        definitionSheet.addDataItem(item1);
        subject.parseAndSetACLEntities(definitionSheets, caseType, Collections.singleton(caseField));
        Collection<CaseFieldACLEntity> entities = caseField.getCaseFieldACLEntities();
        assertThat(entities.size(), is(1));

        final CaseFieldACLEntity caseFieldACLEntity = new ArrayList<>(entities).get(0);
        assertThat(caseFieldACLEntity.getCrudAsString(), is("CCCd"));
        assertThat(caseFieldACLEntity.getId(), is(nullValue()));
        assertThat(caseFieldACLEntity.getAccessProfile(), is(nullValue()));

        assertThat(entityToDefinitionDataItemRegistry.getForEntity(caseFieldACLEntity), is(Optional.of(item1)));
    }

    @Test
    public void shouldParseEntityWithInvalidCrud() {
        final DefinitionDataItem item1 = new DefinitionDataItem(SheetName.AUTHORISATION_CASE_FIELD.getName());
        item1.addAttribute(CASE_TYPE_ID.toString(), CASE_TYPE_UNDER_TEST);
        item1.addAttribute(ACCESS_PROFILE.toString(), TEST_ACCESS_PROFILE_FOUND);
        item1.addAttribute(CRUD.toString(), " X y  ");
        item1.addAttribute(CASE_FIELD_ID.toString(), CASE_FIELD_UNDER_TEST);

        definitionSheet.addDataItem(item1);
        subject.parseAndSetACLEntities(definitionSheets, caseType, Collections.singleton(caseField));
        Collection<CaseFieldACLEntity> entities = caseField.getCaseFieldACLEntities();
        assertThat(entities.size(), is(1));

        final CaseFieldACLEntity caseFieldACLEntity = new ArrayList<>(entities).get(0);
        assertThat(caseFieldACLEntity.getCrudAsString(), is("X y"));
        assertThat(caseFieldACLEntity.getId(), is(nullValue()));
        assertThat(caseFieldACLEntity.getAccessProfile(), is(mockAccessProfileEntity));

        assertThat(entityToDefinitionDataItemRegistry.getForEntity(caseFieldACLEntity), is(Optional.of(item1)));
    }

    @Test
    public void shouldParseEntityWithInvalidCrudAndAccessProfileNotFound() {
        final DefinitionDataItem item1 = new DefinitionDataItem(SheetName.AUTHORISATION_CASE_FIELD.getName());
        item1.addAttribute(CASE_TYPE_ID.toString(), CASE_TYPE_UNDER_TEST);
        item1.addAttribute(ACCESS_PROFILE.toString(), TEST_ACCESS_PROFILE_NOT_FOUND);
        item1.addAttribute(CRUD.toString(), " X y  ");
        item1.addAttribute(CASE_FIELD_ID.toString(), CASE_FIELD_UNDER_TEST);

        definitionSheet.addDataItem(item1);
        subject.parseAndSetACLEntities(definitionSheets, caseType, Collections.singleton(caseField));
        final Collection<CaseFieldACLEntity> entities = caseField.getCaseFieldACLEntities();
        assertThat(entities.size(), is(1));

        final CaseFieldACLEntity caseFieldACLEntity = new ArrayList<>(entities).get(0);
        assertThat(caseFieldACLEntity.getCrudAsString(), is("X y"));
        assertThat(caseFieldACLEntity.getId(), is(nullValue()));
        assertThat(caseFieldACLEntity.getAccessProfile(), is(nullValue()));

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
