package uk.gov.hmcts.ccd.definition.store.excel.parser;

import lombok.val;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationException;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionDataItem;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionSheet;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName;
import uk.gov.hmcts.ccd.definition.store.excel.validation.AccessTypeRolesValidator;
import uk.gov.hmcts.ccd.definition.store.repository.entity.AccessTypeRolesEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;

import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName.CASE_TYPE_ID;
import static uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName.ACCESS_TYPE_ROLES;

import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneId;
import java.util.Date;
import java.util.Map;
import java.util.HashMap;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
class AccessTypeRolesParserTest extends ParserTestBase {

    private static final String ACCESSTYPEROLES_DESCRIPTION = "Test Desc1";
    private static final String CASE_TYPE_ID_1 = "TestCaseTypeID_1";
    private static final String CASE_TYPE_ID_2 = "TestCaseTypeID_2";

    private ParseContext context;

    private AccessTypeRolesParser accessTypeRolesParser;

    private Map<String, DefinitionSheet> definitionSheets;
    private DefinitionSheet definitionSheet;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);
        parseContext = buildParseContext();

        accessTypeRolesParser = new AccessTypeRolesParser(new AccessTypeRolesValidator());

        definitionSheets = new HashMap<>();
        definitionSheet = new DefinitionSheet();

        definitionSheets.put(SheetName.ROLE_TO_ACCESS_PROFILES.getName(), definitionSheet);
        final CaseTypeEntity caseTypeEntity1 = mock(CaseTypeEntity.class);
        when(caseTypeEntity1.getReference()).thenReturn(CASE_TYPE_ID_1);
        final CaseTypeEntity caseTypeEntity2 = mock(CaseTypeEntity.class);
        when(caseTypeEntity2.getReference()).thenReturn(CASE_TYPE_ID_2);

        definitionSheets.put(ACCESS_TYPE_ROLES.getName(), definitionSheet);
    }

    @Test
    public void shouldParse() {
        final DefinitionDataItem item = new DefinitionDataItem(ACCESS_TYPE_ROLES.getName());

        item.addAttribute(ColumnName.CASE_TYPE_ID.toString(), CASE_TYPE_ID_1);
        item.addAttribute(ColumnName.ORGANISATION_PROFILE_ID.toString(), "ProfileID");
        item.addAttribute(ColumnName.DESCRIPTION.toString(), ACCESSTYPEROLES_DESCRIPTION);
        item.addAttribute(ColumnName.HINT_TEXT.toString(), "Hint");
        item.addAttribute(ColumnName.ORGANISATION_ROLE_NAME.toString(), "Name");
        item.addAttribute(ColumnName.DISPLAY_ORDER.toString(), 1.0);
        item.addAttribute(ColumnName.ORGANISATION_POLICY_FIELD.toString(), "Policy Field Name");
        item.addAttribute(ColumnName.CASE_GROUP_ID_TEMPLATE.toString(), "Case Group ID Template");
        item.addAttribute(ColumnName.GROUP_ROLE_NAME.toString(), "Group role name");
        item.addAttribute(ColumnName.ACCESS_TYPE_ID.toString(), "access id");
        item.addAttribute(ColumnName.LIVE_FROM.toString(), Date.from(LocalDate.of(2023,
            Month.FEBRUARY, 12).atStartOfDay(ZoneId.systemDefault()).toInstant()));

        definitionSheet.addDataItem(item);
        definitionSheets.put(ACCESS_TYPE_ROLES.getName(), definitionSheet);
        List<AccessTypeRolesEntity> accessTypeRolesEntities = accessTypeRolesParser.parse(definitionSheets,
            parseContext);
        assertThat(accessTypeRolesEntities.size() > 0, is(true));
        for (AccessTypeRolesEntity accessTypeRolesEntity: accessTypeRolesEntities) {
            assertAll(
                //Mandatory
                () -> assertThat(accessTypeRolesEntity.getCaseTypeId().getReference(), is(CASE_TYPE_ID_1)),
                () -> assertThat(accessTypeRolesEntity.getAccessTypeId(), is("access id")),
                () -> assertThat(accessTypeRolesEntity.getOrganisationProfileId(), is("ProfileID")),
                () -> assertThat(accessTypeRolesEntity.getDescription(), is(ACCESSTYPEROLES_DESCRIPTION)),
                () -> assertThat(accessTypeRolesEntity.getHint(), is("Hint")),
                () -> assertThat(accessTypeRolesEntity.getDisplayOrder(), is(1)),
                () -> assertThat(accessTypeRolesEntity.getOrganisationalRoleName(), is("Name")),
                () -> assertThat(accessTypeRolesEntity.getGroupRoleName(), is("Group role name")),
                () -> assertThat(accessTypeRolesEntity.getOrganisationPolicyField(), is("Policy Field Name")),
                () -> assertThat(accessTypeRolesEntity.getCaseAccessGroupIdTemplate(),
                    is("Case Group ID Template")),
                () -> assertThat(accessTypeRolesEntity.getLiveFrom(), is(LocalDate.of(2023, Month.FEBRUARY,
                    12))),

                //OPTIONAL
                () -> assertThat(accessTypeRolesEntity.getAccessMandatory(), is(nullValue())),
                () -> assertThat(accessTypeRolesEntity.getAccessDefault(), is(nullValue())),
                () -> assertThat(accessTypeRolesEntity.getDisplay(), is(nullValue())),
                () -> assertThat(accessTypeRolesEntity.getAccessDefault(), is(nullValue())),
                () -> assertThat(accessTypeRolesEntity.getGroupAccessEnabled(), is(nullValue())),
                () -> assertThat(accessTypeRolesEntity.getGroupAccessEnabled(), is(nullValue()))
            );
        }
    }

    @Test
    public void shouldReturnCaseTypeIDNotFound() {
        definitionSheet.addDataItem(buildDefinitionDataItem(ACCESSTYPEROLES_DESCRIPTION));
        try {
            List<AccessTypeRolesEntity> accessTypeRolesEntities = accessTypeRolesParser.parse(definitionSheets,
                parseContext);
            assertThat(accessTypeRolesEntities.size() > 0, is(true));
            for (AccessTypeRolesEntity accessTypeRolesEntity : accessTypeRolesEntities) {
                assertAll(
                    () -> assertThat(accessTypeRolesEntity.getDescription(), is(ACCESSTYPEROLES_DESCRIPTION)),
                    () -> assertThat(accessTypeRolesEntity.getAccessDefault(), is(true)),
                    () -> assertThat(accessTypeRolesEntity.getGroupAccessEnabled(), is(true))
                );
            }
        } catch (ValidationException e) {
            assertThat(e.getValidationResult().getValidationErrors().get(0).toString(),
                is("Case Type not found Some Case Type in column 'CaseTypeID' in the sheet "
                    + "'RoleToAccessProfiles'"));
        }
    }

    @Test
    public void shouldReturnEmptyOptionalWhenAccessTypeRolesSheetHasNoItems() {
        List<AccessTypeRolesEntity> accessTypeRolesEntities = accessTypeRolesParser.parse(definitionSheets, context);

        assertAll(
            () -> assertThat(accessTypeRolesEntities.size() > 0, is(false))
        );
    }

    private DefinitionDataItem buildDefinitionDataItem(String description) {
        final DefinitionDataItem item = new DefinitionDataItem(ACCESS_TYPE_ROLES.toString());
        item.addAttribute(ColumnName.DESCRIPTION.toString(), description);
        item.addAttribute(ColumnName.GROUP_ACCESS_ENABLED.toString(), true);
        item.addAttribute(ColumnName.ACCESS_TYPE_ID.toString(), "Access ID");
        item.addAttribute(CASE_TYPE_ID.toString(), CASE_TYPE_UNDER_TEST);

        return item;
    }

    @Test
    @DisplayName("AccessTypeRolesTabParser - should fail when mandatory AccessTypeID missing")
    void shouldFail_whenMandatoryAccessTypeIDNotGiven() {

        final DefinitionDataItem item = new DefinitionDataItem(ACCESS_TYPE_ROLES.getName());

        item.addAttribute(ColumnName.CASE_TYPE_ID.toString(), CASE_TYPE_ID_1);
        item.addAttribute(ColumnName.ORGANISATION_PROFILE_ID.toString(), "ProfileID");
        item.addAttribute(ColumnName.DESCRIPTION.toString(), "AccessTypeRoles Description");
        item.addAttribute(ColumnName.HINT_TEXT.toString(), "hint");
        item.addAttribute(ColumnName.ORGANISATION_ROLE_NAME.toString(), "Name");
        item.addAttribute(ColumnName.DISPLAY_ORDER.toString(), 1.0);
        item.addAttribute(ColumnName.ORGANISATION_POLICY_FIELD.toString(), "Policy Field Name");
        item.addAttribute(ColumnName.CASE_GROUP_ID_TEMPLATE.toString(), "Case Group ID Template");
        item.addAttribute(ColumnName.LIVE_FROM.toString(), new Date());

        definitionSheet.addDataItem(item);
        definitionSheets.put(ACCESS_TYPE_ROLES.getName(), definitionSheet);

        try {
            accessTypeRolesParser.parse(definitionSheets, parseContext);
        } catch (ValidationException e) {
            assertThat(e.getValidationResult().getValidationErrors().get(0).toString(),
                is("Access Type ID should not be null or empty in column 'AccessTypeID' "
                    + "in the sheet 'AccessTypeRoles'"));
        }

    }

    @Test
    @DisplayName("AccessTypeRolesTabParser - should fail when mandatory OrganisationProfileID missing")
    void shouldFail_whenMandatoryOrganisationProfileIDNotGiven() {

        final DefinitionDataItem item = new DefinitionDataItem(ACCESS_TYPE_ROLES.getName());

        item.addAttribute(ColumnName.CASE_TYPE_ID.toString(), CASE_TYPE_ID_1);
        item.addAttribute(ColumnName.ACCESS_TYPE_ID.toString(), "AccessTypeID");
        item.addAttribute(ColumnName.DESCRIPTION.toString(), "AccessTypeRoles Description");
        item.addAttribute(ColumnName.HINT_TEXT.toString(), "hint");
        item.addAttribute(ColumnName.ORGANISATION_ROLE_NAME.toString(), "Name");
        item.addAttribute(ColumnName.DISPLAY_ORDER.toString(), 1.0);
        item.addAttribute(ColumnName.ORGANISATION_POLICY_FIELD.toString(), "Policy Field Name");
        item.addAttribute(ColumnName.CASE_GROUP_ID_TEMPLATE.toString(), "Case Group ID Template");
        item.addAttribute(ColumnName.LIVE_FROM.toString(), new Date());

        definitionSheet.addDataItem(item);
        definitionSheets.put(ACCESS_TYPE_ROLES.getName(), definitionSheet);

        try {
            accessTypeRolesParser.parse(definitionSheets, parseContext);
        } catch (ValidationException e) {
            assertThat(e.getValidationResult().getValidationErrors().get(0).toString(),
                is("Organisation Profile ID should not be null or empty in column 'OrganisationProfileID' "
                    + "in the sheet 'AccessTypeRoles'"));
        }

    }

    @Test
    @DisplayName("AccessTypeRolesTabParser - should fail when mandatory LiveFrom missing")
    void shouldFail_whenMandatoryLiveFromNotGiven() {

        final DefinitionDataItem item = new DefinitionDataItem(ACCESS_TYPE_ROLES.getName());

        item.addAttribute(ColumnName.CASE_TYPE_ID.toString(), CASE_TYPE_ID_1);
        item.addAttribute(ColumnName.ACCESS_TYPE_ID.toString(), "AccessTypeID");
        item.addAttribute(ColumnName.DESCRIPTION.toString(), "AccessTypeRoles Description");
        item.addAttribute(ColumnName.HINT_TEXT.toString(), "hint");
        item.addAttribute(ColumnName.ORGANISATION_ROLE_NAME.toString(), "Name");
        item.addAttribute(ColumnName.DISPLAY_ORDER.toString(), 1.0);
        item.addAttribute(ColumnName.ORGANISATION_POLICY_FIELD.toString(), "Policy Field Name");

        definitionSheet.addDataItem(item);
        definitionSheets.put(ACCESS_TYPE_ROLES.getName(), definitionSheet);

        try {
            accessTypeRolesParser.parse(definitionSheets, parseContext);
        } catch (ValidationException e) {
            assertThat(e.getValidationResult().getValidationErrors().get(0).toString(),
                is("Live From should not be null or empty in column 'LiveFrom' in the sheet 'AccessTypeRoles'"));
        }

    }

    protected ParseContext buildParseContext() {
        val parseContext = new ParseContext();
        val caseTypeEntity1 = new CaseTypeEntity();
        val caseTypeEntity = new CaseTypeEntity();
        caseTypeEntity.setReference(CASE_TYPE_ID_1);
        caseTypeEntity1.setReference(CASE_TYPE_ID_2);
        parseContext.registerCaseType(caseTypeEntity);
        parseContext.registerCaseType(caseTypeEntity1);
        return parseContext;
    }

}
