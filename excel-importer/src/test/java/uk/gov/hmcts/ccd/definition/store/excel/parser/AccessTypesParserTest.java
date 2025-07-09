package uk.gov.hmcts.ccd.definition.store.excel.parser;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationException;
import uk.gov.hmcts.ccd.definition.store.excel.endpoint.exception.MapperException;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionDataItem;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionSheet;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName;
import uk.gov.hmcts.ccd.definition.store.excel.validation.AccessTypesValidator;
import uk.gov.hmcts.ccd.definition.store.repository.entity.AccessTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.JurisdictionEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.RoleToAccessProfilesEntity;

import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName.CASE_TYPE_ID;
import static uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName.ACCESS_TYPE;
import static uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName.ACCESS_TYPE_ROLE;

class AccessTypesParserTest extends ParserTestBase {

    private static final String CASE_TYPE_ID_1 = "TestCaseTypeID_1";
    private static final String CASE_TYPE_ID_2 = "TestCaseTypeID_2";
    private static final String JURISDICTION = "BEFTA_MASTER";
    private static String ROLE_TO_ACCESS_PROFILES_ROLE_NAME = "Role1";
    private static String CASE_GROUP_ID_TEMPLATE = JURISDICTION + ":$ORGID$";
    private AccessTypesParser accessTypesParser;
    private Map<String, DefinitionSheet> definitionSheets;
    private DefinitionSheet definitionSheet;
    private ParseContext parseContext;
    @Mock
    private CaseTypeEntity caseTypeEntity;
    @Mock
    private CaseTypeEntity caseTypeEntity2;
    @Mock
    private JurisdictionEntity jurisdictionEntity;
    private List<RoleToAccessProfilesEntity> roleToAccessProfilesEntities;
    @Mock
    private RoleToAccessProfilesEntity roleToAccessProfilesEntity;

    @BeforeEach
    void setup() {
        parseContext = new ParseContext();
        MockitoAnnotations.openMocks(this);

        //setup case types
        when(caseTypeEntity.getReference()).thenReturn(CASE_TYPE_ID_1);
        parseContext.registerCaseType(caseTypeEntity);
        when(caseTypeEntity2.getReference()).thenReturn(CASE_TYPE_ID_2);
        parseContext.registerCaseType(caseTypeEntity2);

        //setup jurisdiction
        when(jurisdictionEntity.getReference()).thenReturn(JURISDICTION);
        when(caseTypeEntity.getJurisdiction()).thenReturn(jurisdictionEntity);
        when(caseTypeEntity2.getJurisdiction()).thenReturn(jurisdictionEntity);
        parseContext.setJurisdiction(jurisdictionEntity);

        //setup role to access profile
        when(roleToAccessProfilesEntity.getRoleName()).thenReturn(ROLE_TO_ACCESS_PROFILES_ROLE_NAME);
        roleToAccessProfilesEntities = List.of(roleToAccessProfilesEntity);

        accessTypesParser = new AccessTypesParser(new AccessTypesValidator());

        definitionSheets = new HashMap<>();
        definitionSheet = new DefinitionSheet();
        definitionSheets.put(ACCESS_TYPE.getName(), definitionSheet);
    }

    @Test
    void shouldParse() {
        final DefinitionDataItem item = new DefinitionDataItem(ACCESS_TYPE.getName());
        item.addAttribute(ColumnName.LIVE_FROM.toString(), Date.from(LocalDate.of(2023,
            Month.FEBRUARY, 12).atStartOfDay(ZoneId.systemDefault()).toInstant()));
        item.addAttribute(ColumnName.LIVE_TO.toString(), Date.from(LocalDate.of(2080,
            Month.FEBRUARY, 12).atStartOfDay(ZoneId.systemDefault()).toInstant()));
        item.addAttribute(ColumnName.CASE_TYPE_ID.toString(), CASE_TYPE_ID_1);
        item.addAttribute(ColumnName.ACCESS_TYPE_ID.toString(), "access id");
        item.addAttribute(ColumnName.ORGANISATION_PROFILE_ID.toString(), "OrgProfileID");
        item.addAttribute(ColumnName.DESCRIPTION.toString(), "Test Desc1");
        item.addAttribute(ColumnName.HINT_TEXT.toString(), "Hint");
        item.addAttribute(ColumnName.DISPLAY.toString(), true);
        item.addAttribute(ColumnName.DISPLAY_ORDER.toString(), 1);
        item.addAttribute(ColumnName.GROUP_ROLE_NAME.toString(), ROLE_TO_ACCESS_PROFILES_ROLE_NAME);
        item.addAttribute(ColumnName.CASE_ASSIGNED_ROLE_FIELD.toString(), ROLE_TO_ACCESS_PROFILES_ROLE_NAME);
        item.addAttribute(ColumnName.CASE_GROUP_ID_TEMPLATE.toString(), CASE_GROUP_ID_TEMPLATE);
        item.addAttribute(ColumnName.GROUP_ACCESS_ENABLED.toString(), true);
        definitionSheet.addDataItem(item);
        definitionSheets.put(ACCESS_TYPE.getName(), definitionSheet);

        final DefinitionDataItem item2 = new DefinitionDataItem(ACCESS_TYPE.getName());
        item2.addAttribute(ColumnName.LIVE_FROM.toString(), Date.from(LocalDate.of(2023,
            Month.FEBRUARY, 12).atStartOfDay(ZoneId.systemDefault()).toInstant()));
        item2.addAttribute(ColumnName.LIVE_TO.toString(), Date.from(LocalDate.of(2080,
            Month.FEBRUARY, 12).atStartOfDay(ZoneId.systemDefault()).toInstant()));
        item2.addAttribute(ColumnName.CASE_TYPE_ID.toString(), CASE_TYPE_ID_1);
        item2.addAttribute(ColumnName.ACCESS_TYPE_ID.toString(), "access id2");
        item2.addAttribute(ColumnName.ORGANISATION_PROFILE_ID.toString(), "OrgProfileID2");
        item2.addAttribute(ColumnName.DESCRIPTION.toString(), "Test Desc1");
        item2.addAttribute(ColumnName.HINT_TEXT.toString(), "Hint");
        item2.addAttribute(ColumnName.DISPLAY.toString(), true);
        item2.addAttribute(ColumnName.DISPLAY_ORDER.toString(), 2);
        item2.addAttribute(ColumnName.GROUP_ROLE_NAME.toString(), ROLE_TO_ACCESS_PROFILES_ROLE_NAME);
        item2.addAttribute(ColumnName.CASE_ASSIGNED_ROLE_FIELD.toString(), ROLE_TO_ACCESS_PROFILES_ROLE_NAME);
        item2.addAttribute(ColumnName.CASE_GROUP_ID_TEMPLATE.toString(), CASE_GROUP_ID_TEMPLATE);
        item2.addAttribute(ColumnName.GROUP_ACCESS_ENABLED.toString(), true);
        definitionSheet.addDataItem(item2);
        definitionSheets.put(ACCESS_TYPE.getName(), definitionSheet);

        List<AccessTypeEntity> accessTypesEntities =
            accessTypesParser.parse(definitionSheets, parseContext);

        AccessTypeEntity accessTypeEntity1 = accessTypesEntities.get(0);
        AccessTypeEntity accessTypeEntity2 = accessTypesEntities.get(1);

        assertAll(
            () -> assertEquals(2, accessTypesEntities.size()),

            () -> assertThat(accessTypeEntity1.getLiveFrom(),
                is(LocalDate.of(2023, Month.FEBRUARY, 12))),
            () -> assertThat(accessTypeEntity1.getLiveTo(),
                is(LocalDate.of(2080, Month.FEBRUARY, 12))),
            () -> assertThat(accessTypeEntity1.getCaseType().getReference(), is(CASE_TYPE_ID_1)),
            () -> assertThat(accessTypeEntity1.getAccessTypeId(), is("access id")),
            () -> assertThat(accessTypeEntity1.getOrganisationProfileId(), is("OrgProfileID")),
            () -> assertThat(accessTypeEntity1.getAccessMandatory(), is(false)),
            () -> assertThat(accessTypeEntity1.getAccessDefault(), is(false)),
            () -> assertThat(accessTypeEntity1.getDisplay(), is(true)),
            () -> assertThat(accessTypeEntity1.getDisplayOrder(), is(1)),
            () -> assertThat(accessTypeEntity1.getDescription(), is("Test Desc1")),
            () -> assertThat(accessTypeEntity1.getHint(), is("Hint")),


            () -> assertThat(accessTypeEntity2.getLiveFrom(),
                is(LocalDate.of(2023, Month.FEBRUARY, 12))),
            () -> assertThat(accessTypeEntity2.getLiveTo(),
                is(LocalDate.of(2080, Month.FEBRUARY, 12))),
            () -> assertThat(accessTypeEntity2.getCaseType().getReference(), is(CASE_TYPE_ID_1)),
            () -> assertThat(accessTypeEntity2.getAccessTypeId(), is("access id2")),
            () -> assertThat(accessTypeEntity2.getOrganisationProfileId(), is("OrgProfileID2")),
            () -> assertThat(accessTypeEntity2.getAccessMandatory(), is(false)),
            () -> assertThat(accessTypeEntity2.getAccessDefault(), is(false)),
            () -> assertThat(accessTypeEntity2.getDisplay(), is(true)),
            () -> assertThat(accessTypeEntity2.getDisplayOrder(), is(2)),
            () -> assertThat(accessTypeEntity2.getDescription(), is("Test Desc1")),
            () -> assertThat(accessTypeEntity2.getHint(), is("Hint"))
        );
    }

    @Test
    void shouldFailWhenCaseTypeIDIsInvalid() {
        final DefinitionDataItem item = new DefinitionDataItem(ACCESS_TYPE.toString());
        item.addAttribute(ColumnName.DESCRIPTION.toString(), "Test Desc1");

        item.addAttribute(ColumnName.ACCESS_TYPE_ID.toString(), "Access ID");
        item.addAttribute(CASE_TYPE_ID.toString(), CASE_TYPE_UNDER_TEST);
        definitionSheet.addDataItem(item);

        definitionSheets.put(ACCESS_TYPE.getName(), definitionSheet);

        ValidationException exception =
            assertThrows(ValidationException.class, () -> accessTypesParser
                .parse(definitionSheets, parseContext));
        assertThat(exception.getValidationResult().getValidationErrors().get(0).toString(),
            is("Case Type not found Some Case Type in column 'CaseTypeID' in the sheet 'AccessType'"));

    }

    @Test
    void shouldFailWhenDateIsInvalid() {
        final DefinitionDataItem item = new DefinitionDataItem(ACCESS_TYPE.getName());
        item.addAttribute(ColumnName.CASE_TYPE_ID.toString(), CASE_TYPE_ID_1);
        item.addAttribute(ColumnName.ACCESS_TYPE_ID.toString(), "access id");
        item.addAttribute(ColumnName.ORGANISATION_PROFILE_ID.toString(), "ProfileID");
        item.addAttribute(ColumnName.LIVE_FROM.toString(), "invalid date");

        definitionSheet.addDataItem(item);
        definitionSheets.put(ACCESS_TYPE.getName(), definitionSheet);

        MapperException exception =
            assertThrows(MapperException.class, () -> accessTypesParser
                .parse(definitionSheets, parseContext));
        assertThat(exception.getMessage(),
            is("Invalid value 'invalid date' is found in column 'LiveFrom' in the sheet 'AccessType'"));

    }

    @Test
    void shouldFailIfDisplayIsSetToTrueAndRequiredFieldsAreNotSet() {
        final DefinitionDataItem item = new DefinitionDataItem(ACCESS_TYPE.getName());
        item.addAttribute(ColumnName.CASE_TYPE_ID.toString(), CASE_TYPE_ID_1);
        item.addAttribute(ColumnName.ACCESS_TYPE_ID.toString(), "access id");
        item.addAttribute(ColumnName.ORGANISATION_PROFILE_ID.toString(), "ProfileID");
        item.addAttribute(ColumnName.LIVE_FROM.toString(), Date.from(LocalDate.of(2023,
            Month.FEBRUARY, 12).atStartOfDay(ZoneId.systemDefault()).toInstant()));
        item.addAttribute(ColumnName.GROUP_ROLE_NAME.toString(), ROLE_TO_ACCESS_PROFILES_ROLE_NAME);
        item.addAttribute(ColumnName.CASE_GROUP_ID_TEMPLATE.toString(), CASE_GROUP_ID_TEMPLATE);
        item.addAttribute(ColumnName.CASE_ASSIGNED_ROLE_FIELD.toString(), ROLE_TO_ACCESS_PROFILES_ROLE_NAME);
        item.addAttribute(ColumnName.DISPLAY, true);
        item.addAttribute(ColumnName.GROUP_ACCESS_ENABLED.toString(), true);

        definitionSheet.addDataItem(item);
        definitionSheets.put(ACCESS_TYPE.getName(), definitionSheet);

        ValidationException exception =
            assertThrows(ValidationException.class, () -> accessTypesParser
                .parse(definitionSheets, parseContext));

        assertAll(
            () -> assertThat(exception.getValidationResult().getValidationErrors()
                .size() == 3, is(true)),
            () -> assertThat(exception.getValidationResult().getValidationErrors(), allOf(
                    hasItem(matchesValidationErrorWithDefaultMessage(
                        "'Description' must be set for 'Display' to be used in the sheet 'AccessType'")),
                    hasItem(matchesValidationErrorWithDefaultMessage(
                        "'HintText' must be set for 'Display' to be used in the sheet 'AccessType'")),
                    hasItem(matchesValidationErrorWithDefaultMessage(
                        "'DisplayOrder' should not be null or empty for 'Display' to be used in column 'DisplayOrder' "
                            + "in the sheet 'AccessType'"))
                )
            )
        );
    }

    @Test
    void shouldFailIfDisplayOrderIsInvalid() {
        final DefinitionDataItem item = new DefinitionDataItem(ACCESS_TYPE.getName());
        item.addAttribute(ColumnName.CASE_TYPE_ID.toString(), CASE_TYPE_ID_1);
        item.addAttribute(ColumnName.ACCESS_TYPE_ID.toString(), "access id");
        item.addAttribute(ColumnName.ORGANISATION_PROFILE_ID.toString(), "ProfileID");
        item.addAttribute(ColumnName.LIVE_FROM.toString(), Date.from(LocalDate.of(2023,
            Month.FEBRUARY, 12).atStartOfDay(ZoneId.systemDefault()).toInstant()));
        item.addAttribute(ColumnName.GROUP_ROLE_NAME.toString(), ROLE_TO_ACCESS_PROFILES_ROLE_NAME);
        item.addAttribute(ColumnName.CASE_GROUP_ID_TEMPLATE.toString(), CASE_GROUP_ID_TEMPLATE);
        item.addAttribute(ColumnName.CASE_ASSIGNED_ROLE_FIELD.toString(), ROLE_TO_ACCESS_PROFILES_ROLE_NAME);
        item.addAttribute(ColumnName.DISPLAY, true);
        item.addAttribute(ColumnName.DESCRIPTION.toString(), "Test Desc1");
        item.addAttribute(ColumnName.HINT_TEXT.toString(), "Hint");
        item.addAttribute(ColumnName.DISPLAY_ORDER, 0);
        item.addAttribute(ColumnName.ACCESS_DEFAULT, true);
        item.addAttribute(ColumnName.ACCESS_MANDATORY, true);
        item.addAttribute(ColumnName.GROUP_ACCESS_ENABLED.toString(), true);

        definitionSheet.addDataItem(item);
        definitionSheets.put(ACCESS_TYPE.getName(), definitionSheet);

        ValidationException exception =
            assertThrows(ValidationException.class, () -> accessTypesParser
                .parse(definitionSheets, parseContext));

        assertAll(
            () -> assertThat(exception.getValidationResult().getValidationErrors().size() == 1, is(true)),
            () -> assertEquals("'DisplayOrder' must be greater than 0 in column 'DisplayOrder' "
                    + "in the sheet 'AccessType'",
                exception.getValidationResult().getValidationErrors().get(0).getDefaultMessage())
        );
    }

    @Test
    void shouldFailIfDisplayOrderIsNotUnique() {
        final DefinitionDataItem item = new DefinitionDataItem(ACCESS_TYPE.getName());
        item.addAttribute(ColumnName.CASE_TYPE_ID.toString(), CASE_TYPE_ID_1);
        item.addAttribute(ColumnName.ACCESS_TYPE_ID.toString(), "access id");
        item.addAttribute(ColumnName.ORGANISATION_PROFILE_ID.toString(), "ProfileID");
        item.addAttribute(ColumnName.LIVE_FROM.toString(), Date.from(LocalDate.of(2023,
            Month.FEBRUARY, 12).atStartOfDay(ZoneId.systemDefault()).toInstant()));
        item.addAttribute(ColumnName.GROUP_ROLE_NAME.toString(), ROLE_TO_ACCESS_PROFILES_ROLE_NAME);
        item.addAttribute(ColumnName.CASE_GROUP_ID_TEMPLATE.toString(), CASE_GROUP_ID_TEMPLATE);
        item.addAttribute(ColumnName.CASE_ASSIGNED_ROLE_FIELD.toString(), ROLE_TO_ACCESS_PROFILES_ROLE_NAME);
        item.addAttribute(ColumnName.DISPLAY, true);
        item.addAttribute(ColumnName.DESCRIPTION.toString(), "Test Desc1");
        item.addAttribute(ColumnName.HINT_TEXT.toString(), "Hint");
        item.addAttribute(ColumnName.DISPLAY_ORDER, 1);
        item.addAttribute(ColumnName.ACCESS_DEFAULT, true);
        item.addAttribute(ColumnName.ACCESS_MANDATORY, true);
        item.addAttribute(ColumnName.GROUP_ACCESS_ENABLED.toString(), true);
        definitionSheet.addDataItem(item);

        final DefinitionDataItem item2 = new DefinitionDataItem(ACCESS_TYPE.getName());
        item2.addAttribute(ColumnName.CASE_TYPE_ID.toString(), CASE_TYPE_ID_1);
        item2.addAttribute(ColumnName.ACCESS_TYPE_ID.toString(), "access id2");
        item2.addAttribute(ColumnName.ORGANISATION_PROFILE_ID.toString(), "ProfileID2");
        item2.addAttribute(ColumnName.LIVE_FROM.toString(), Date.from(LocalDate.of(2023,
            Month.FEBRUARY, 12).atStartOfDay(ZoneId.systemDefault()).toInstant()));
        item2.addAttribute(ColumnName.GROUP_ROLE_NAME.toString(), ROLE_TO_ACCESS_PROFILES_ROLE_NAME);
        item2.addAttribute(ColumnName.CASE_GROUP_ID_TEMPLATE.toString(), CASE_GROUP_ID_TEMPLATE);
        item2.addAttribute(ColumnName.CASE_ASSIGNED_ROLE_FIELD.toString(), ROLE_TO_ACCESS_PROFILES_ROLE_NAME);
        item2.addAttribute(ColumnName.DISPLAY, true);
        item2.addAttribute(ColumnName.DESCRIPTION.toString(), "Test Desc1");
        item2.addAttribute(ColumnName.HINT_TEXT.toString(), "Hint");
        item2.addAttribute(ColumnName.DISPLAY_ORDER, 1);
        item2.addAttribute(ColumnName.ACCESS_DEFAULT, true);
        item2.addAttribute(ColumnName.ACCESS_MANDATORY, true);
        item2.addAttribute(ColumnName.GROUP_ACCESS_ENABLED.toString(), true);
        definitionSheet.addDataItem(item2);

        definitionSheets.put(ACCESS_TYPE.getName(), definitionSheet);

        ValidationException exception =
            assertThrows(ValidationException.class, () -> accessTypesParser
                .parse(definitionSheets, parseContext));

        assertAll(
            () -> assertThat(exception.getValidationResult().getValidationErrors().size() == 1, is(true)),
            () -> assertEquals("'DisplayOrder' must be unique across all Case Types for a given Jurisdiction "
                    + "in the sheet 'AccessType'",
                exception.getValidationResult().getValidationErrors().get(0).getDefaultMessage())
        );
    }

    @Test
    void shouldReturnEmptyWhenAccessTypeRolesSheetHasNoItems() {
        List<AccessTypeEntity> accessTypeEntities =
            accessTypesParser
                .parse(definitionSheets, parseContext);

        assertThat(!accessTypeEntities.isEmpty(), is(false));
    }

    @Test
    void shouldFailWhenAccessTypeIDNotProvided() {

        final DefinitionDataItem item = new DefinitionDataItem(ACCESS_TYPE_ROLE.getName());
        item.addAttribute(ColumnName.CASE_TYPE_ID.toString(), CASE_TYPE_ID_1);
        item.addAttribute(ColumnName.GROUP_ROLE_NAME.toString(), ROLE_TO_ACCESS_PROFILES_ROLE_NAME);
        item.addAttribute(ColumnName.CASE_GROUP_ID_TEMPLATE.toString(), CASE_GROUP_ID_TEMPLATE);
        item.addAttribute(ColumnName.CASE_ASSIGNED_ROLE_FIELD.toString(), ROLE_TO_ACCESS_PROFILES_ROLE_NAME);
        item.addAttribute(ColumnName.GROUP_ACCESS_ENABLED.toString(), true);
        definitionSheet.addDataItem(item);
        definitionSheets.put(ACCESS_TYPE_ROLE.getName(), definitionSheet);

        ValidationException exception =
            assertThrows(ValidationException.class, () -> accessTypesParser
                .parse(definitionSheets, parseContext));
        assertAll(
            () -> assertThat(exception.getValidationResult().getValidationErrors().size() == 1, is(true)),
            () -> assertEquals(exception.getValidationResult().getValidationErrors().get(0).getDefaultMessage(),
                "Access Type ID should not be null or empty in column 'AccessTypeID' "
                    + "in the sheet 'AccessType'")
        );
    }

    @Test
    void shouldFailWhenOrgProfileIDNotProvided() {

        final DefinitionDataItem item = new DefinitionDataItem(ACCESS_TYPE_ROLE.getName());
        item.addAttribute(ColumnName.CASE_TYPE_ID.toString(), CASE_TYPE_ID_1);
        item.addAttribute(ColumnName.ACCESS_TYPE_ID.toString(), "access id");
        item.addAttribute(ColumnName.GROUP_ROLE_NAME.toString(), ROLE_TO_ACCESS_PROFILES_ROLE_NAME);
        item.addAttribute(ColumnName.CASE_GROUP_ID_TEMPLATE.toString(), CASE_GROUP_ID_TEMPLATE);
        item.addAttribute(ColumnName.CASE_ASSIGNED_ROLE_FIELD.toString(), ROLE_TO_ACCESS_PROFILES_ROLE_NAME);
        item.addAttribute(ColumnName.GROUP_ACCESS_ENABLED.toString(), true);
        definitionSheet.addDataItem(item);
        definitionSheets.put(ACCESS_TYPE_ROLE.getName(), definitionSheet);

        ValidationException exception =
            assertThrows(ValidationException.class, () -> accessTypesParser
                .parse(definitionSheets, parseContext));

        assertAll(
            () -> assertThat(exception.getValidationResult().getValidationErrors().size() == 1, is(true)),
            () -> assertEquals(exception.getValidationResult().getValidationErrors().get(0).getDefaultMessage(),
                "Organisation Profile ID should not be null or empty in column 'OrganisationProfileID' "
                    + "in the sheet 'AccessType'")
        );
    }

    @Test
    void shouldFailWhenJurisdictionIsNotUnique() {
        final DefinitionDataItem item = new DefinitionDataItem(ACCESS_TYPE_ROLE.getName());
        item.addAttribute(ColumnName.CASE_TYPE_ID.toString(), CASE_TYPE_ID_1);
        item.addAttribute(ColumnName.ACCESS_TYPE_ID.toString(), "access id");
        item.addAttribute(ColumnName.ORGANISATION_PROFILE_ID.toString(), "ProfileID");
        item.addAttribute(ColumnName.GROUP_ROLE_NAME.toString(), ROLE_TO_ACCESS_PROFILES_ROLE_NAME);
        item.addAttribute(ColumnName.CASE_GROUP_ID_TEMPLATE.toString(), CASE_GROUP_ID_TEMPLATE);
        item.addAttribute(ColumnName.CASE_ASSIGNED_ROLE_FIELD.toString(), ROLE_TO_ACCESS_PROFILES_ROLE_NAME);
        item.addAttribute(ColumnName.GROUP_ACCESS_ENABLED.toString(), true);
        definitionSheet.addDataItem(item);

        final DefinitionDataItem item2 = new DefinitionDataItem(ACCESS_TYPE_ROLE.getName());
        item2.addAttribute(ColumnName.CASE_TYPE_ID.toString(), CASE_TYPE_ID_1);
        item2.addAttribute(ColumnName.ACCESS_TYPE_ID.toString(), "access id");
        item2.addAttribute(ColumnName.ORGANISATION_PROFILE_ID.toString(), "ProfileID");
        item2.addAttribute(ColumnName.GROUP_ROLE_NAME.toString(), ROLE_TO_ACCESS_PROFILES_ROLE_NAME);
        item2.addAttribute(ColumnName.CASE_GROUP_ID_TEMPLATE.toString(), CASE_GROUP_ID_TEMPLATE);
        item2.addAttribute(ColumnName.CASE_ASSIGNED_ROLE_FIELD.toString(), ROLE_TO_ACCESS_PROFILES_ROLE_NAME);
        item2.addAttribute(ColumnName.GROUP_ACCESS_ENABLED.toString(), true);
        definitionSheet.addDataItem(item2);

        definitionSheets.put(ACCESS_TYPE.getName(), definitionSheet);

        ValidationException exception =
            assertThrows(ValidationException.class, () -> accessTypesParser
                .parse(definitionSheets, parseContext));

        assertAll(
            () -> assertThat(exception.getValidationResult().getValidationErrors().size() == 1, is(true)),
            () -> assertEquals("'AccessTypeID' in combination with the 'CaseTypeID' and "
                    + "'OrganisationProfileID', must be unique within the Jurisdiction.  Therefore, if a service "
                    + "requires the same Access Type and Organisation Profile to apply for several Case Types in "
                    + "the same Jurisdiction, the configuration needs to be repeated for each required case type. "
                    + "in the sheet 'AccessType'",
                exception.getValidationResult().getValidationErrors().get(0).getDefaultMessage())
        );
    }

    @Test
    void shouldPassWhenCaseTypeIsUnique() {
        final DefinitionDataItem item = new DefinitionDataItem(ACCESS_TYPE_ROLE.getName());
        item.addAttribute(ColumnName.CASE_TYPE_ID.toString(), CASE_TYPE_ID_1);
        item.addAttribute(ColumnName.ACCESS_TYPE_ID.toString(), "access id");
        item.addAttribute(ColumnName.ORGANISATION_PROFILE_ID.toString(), "ProfileID");
        item.addAttribute(ColumnName.GROUP_ROLE_NAME.toString(), ROLE_TO_ACCESS_PROFILES_ROLE_NAME);
        item.addAttribute(ColumnName.CASE_GROUP_ID_TEMPLATE.toString(), CASE_GROUP_ID_TEMPLATE);
        item.addAttribute(ColumnName.CASE_ASSIGNED_ROLE_FIELD.toString(), ROLE_TO_ACCESS_PROFILES_ROLE_NAME);
        item.addAttribute(ColumnName.GROUP_ACCESS_ENABLED.toString(), true);
        definitionSheet.addDataItem(item);

        final DefinitionDataItem item2 = new DefinitionDataItem(ACCESS_TYPE_ROLE.getName());
        item2.addAttribute(ColumnName.CASE_TYPE_ID.toString(), CASE_TYPE_ID_2);
        item2.addAttribute(ColumnName.ACCESS_TYPE_ID.toString(), "access id");
        item2.addAttribute(ColumnName.ORGANISATION_PROFILE_ID.toString(), "ProfileID");
        item2.addAttribute(ColumnName.GROUP_ROLE_NAME.toString(), ROLE_TO_ACCESS_PROFILES_ROLE_NAME);
        item2.addAttribute(ColumnName.CASE_GROUP_ID_TEMPLATE.toString(), CASE_GROUP_ID_TEMPLATE);
        item2.addAttribute(ColumnName.CASE_ASSIGNED_ROLE_FIELD.toString(), ROLE_TO_ACCESS_PROFILES_ROLE_NAME);
        item2.addAttribute(ColumnName.GROUP_ACCESS_ENABLED.toString(), true);
        definitionSheet.addDataItem(item2);

        definitionSheets.put(ACCESS_TYPE.getName(), definitionSheet);

        accessTypesParser.parse(definitionSheets, parseContext);
    }

    private <T> Matcher<T> matchesValidationErrorWithDefaultMessage(String defaultMessage) {
        return new BaseMatcher<T>() {
            @Override
            public boolean matches(Object o) {
                return o instanceof ValidationError
                    && ((ValidationError) o).getDefaultMessage().equals(defaultMessage);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("a ValidationError with defaultMessage " + defaultMessage);
            }
        };
    }
}
