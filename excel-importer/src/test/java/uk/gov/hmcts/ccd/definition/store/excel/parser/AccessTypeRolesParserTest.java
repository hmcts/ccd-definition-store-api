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
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName;
import uk.gov.hmcts.ccd.definition.store.excel.validation.AccessTypeRolesValidator;
import uk.gov.hmcts.ccd.definition.store.repository.entity.AccessTypeRolesEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.JurisdictionEntity;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName.CASE_TYPE_ID;
import static uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName.ACCESS_TYPE_ROLES;

import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

class AccessTypeRolesParserTest extends ParserTestBase {

    private static final String ACCESSTYPEROLES_DESCRIPTION = "Test Desc1";
    private static final String CASE_TYPE_ID_1 = "TestCaseTypeID_1";
    private static final String JURISDICTION = "BEFTA_MASTER";
    private static final String ORG_POLICY_FIELD = "OrganisationPolicyField1";
    private static final String ORG_POLICY_FIELD2 = "PolicyField1";
    private AccessTypeRolesParser accessTypeRolesParser;
    private Map<String, DefinitionSheet> definitionSheets;
    private DefinitionSheet definitionSheet;
    private ParseContext parseContext;
    @Mock
    private CaseTypeEntity caseTypeEntity;
    @Mock
    private JurisdictionEntity jurisdictionEntity;
    @Mock
    private CaseFieldEntity caseFieldEntity;
    @Mock
    private CaseFieldEntity caseFieldEntity2;
    @Mock
    private FieldTypeEntity fieldTypeEntity;
    @Mock
    private FieldTypeEntity fieldTypeEntity2;

    @BeforeEach
    public void setup() {
        parseContext = new ParseContext();
        MockitoAnnotations.initMocks(this);

        //setup case type
        when(caseTypeEntity.getReference()).thenReturn(CASE_TYPE_ID_1);
        parseContext.registerCaseType(caseTypeEntity);

        //setup jurisdiction
        when(jurisdictionEntity.getReference()).thenReturn(JURISDICTION);
        when(caseTypeEntity.getJurisdiction()).thenReturn(jurisdictionEntity);
        parseContext.setJurisdiction(jurisdictionEntity);

        //setup case fields
        when(fieldTypeEntity.getReference()).thenReturn("OrganisationPolicy");
        when(caseFieldEntity.getReference()).thenReturn(ORG_POLICY_FIELD);
        when(caseFieldEntity.getFieldType()).thenReturn(fieldTypeEntity);
        when(fieldTypeEntity2.getReference()).thenReturn("Text");
        when(caseFieldEntity2.getReference()).thenReturn(ORG_POLICY_FIELD2);
        when(caseFieldEntity2.getFieldType()).thenReturn(fieldTypeEntity2);
        when(caseTypeEntity.getCaseFields()).thenReturn(
            new HashSet<>(Arrays.asList(caseFieldEntity, caseFieldEntity2)));
        parseContext.registerCaseType(caseTypeEntity);
        parseContext.registerCaseFieldForCaseType(CASE_TYPE_ID_1, caseFieldEntity);
        parseContext.registerCaseFieldForCaseType(CASE_TYPE_ID_1, caseFieldEntity2);

        accessTypeRolesParser = new AccessTypeRolesParser(new AccessTypeRolesValidator());

        definitionSheets = new HashMap<>();

        definitionSheet = new DefinitionSheet();
        definitionSheets.put(SheetName.ROLE_TO_ACCESS_PROFILES.getName(), definitionSheet);

        definitionSheets.put(ACCESS_TYPE_ROLES.getName(), definitionSheet);
    }

    @Test
    public void shouldParse() {
        final DefinitionDataItem item = new DefinitionDataItem(ACCESS_TYPE_ROLES.getName());

        item.addAttribute(ColumnName.LIVE_FROM.toString(), Date.from(LocalDate.of(2023,
            Month.FEBRUARY, 12).atStartOfDay(ZoneId.systemDefault()).toInstant()));
        item.addAttribute(ColumnName.LIVE_TO.toString(), Date.from(LocalDate.of(2080,
            Month.FEBRUARY, 12).atStartOfDay(ZoneId.systemDefault()).toInstant()));
        item.addAttribute(ColumnName.CASE_TYPE_ID.toString(), CASE_TYPE_ID_1);
        item.addAttribute(ColumnName.ACCESS_TYPE_ID.toString(), "access id");
        item.addAttribute(ColumnName.ORGANISATION_PROFILE_ID.toString(), "OrgProfileID");
        item.addAttribute(ColumnName.DESCRIPTION.toString(), ACCESSTYPEROLES_DESCRIPTION);
        item.addAttribute(ColumnName.HINT_TEXT.toString(), "Hint");
        item.addAttribute(ColumnName.DISPLAY_ORDER.toString(), 1.0);
        item.addAttribute(ColumnName.GROUP_ROLE_NAME.toString(), "Group role name");
        item.addAttribute(ColumnName.ORGANISATION_POLICY_FIELD.toString(), ORG_POLICY_FIELD);
        item.addAttribute(ColumnName.CASE_GROUP_ID_TEMPLATE.toString(), JURISDICTION + ":$ORGID$");
        definitionSheet.addDataItem(item);
        definitionSheets.put(ACCESS_TYPE_ROLES.getName(), definitionSheet);

        List<AccessTypeRolesEntity> accessTypeRolesEntities =
            accessTypeRolesParser.parse(definitionSheets, parseContext);

        assertThat(!accessTypeRolesEntities.isEmpty(), is(true));

        for (AccessTypeRolesEntity accessTypeRolesEntity: accessTypeRolesEntities) {
            assertAll(
                () -> assertThat(accessTypeRolesEntity.getLiveFrom(), is(LocalDate.of(2023, Month.FEBRUARY, 12))),
                () -> assertThat(accessTypeRolesEntity.getLiveTo(), is(LocalDate.of(2080, Month.FEBRUARY, 12))),
                () -> assertThat(accessTypeRolesEntity.getCaseTypeId().getReference(), is(CASE_TYPE_ID_1)),
                () -> assertThat(accessTypeRolesEntity.getAccessTypeId(), is("access id")),
                () -> assertThat(accessTypeRolesEntity.getOrganisationProfileId(), is("OrgProfileID")),
                () -> assertThat(accessTypeRolesEntity.getAccessMandatory(), is(false)),
                () -> assertThat(accessTypeRolesEntity.getAccessDefault(), is(false)),
                () -> assertThat(accessTypeRolesEntity.getDisplay(), is(false)),
                () -> assertThat(accessTypeRolesEntity.getDescription(), is(ACCESSTYPEROLES_DESCRIPTION)),
                () -> assertThat(accessTypeRolesEntity.getHint(), is("Hint")),
                () -> assertThat(accessTypeRolesEntity.getDisplayOrder(), is(1)),
                () -> assertThat(accessTypeRolesEntity.getGroupRoleName(), is("Group role name")),
                () -> assertThat(accessTypeRolesEntity.getOrganisationPolicyField(), is(ORG_POLICY_FIELD)),
                () -> assertThat(accessTypeRolesEntity.getGroupAccessEnabled(), is(false)),
                () -> assertThat(accessTypeRolesEntity.getCaseAccessGroupIdTemplate(), is(JURISDICTION + ":$ORGID$"))
            );
        }
    }

    @Test
    public void shouldFailWhenCaseTypeIDIsInvalid() {
        final DefinitionDataItem item = new DefinitionDataItem(ACCESS_TYPE_ROLES.toString());
        item.addAttribute(ColumnName.DESCRIPTION.toString(), ACCESSTYPEROLES_DESCRIPTION);
        item.addAttribute(ColumnName.GROUP_ACCESS_ENABLED.toString(), true);
        item.addAttribute(ColumnName.ACCESS_TYPE_ID.toString(), "Access ID");
        item.addAttribute(CASE_TYPE_ID.toString(), CASE_TYPE_UNDER_TEST);
        definitionSheet.addDataItem(item);

        definitionSheets.put(ACCESS_TYPE_ROLES.getName(), definitionSheet);

        ValidationException exception =
            assertThrows(ValidationException.class, () -> accessTypeRolesParser.parse(definitionSheets, parseContext));
        assertThat(exception.getValidationResult().getValidationErrors().get(0).getDefaultMessage(),
            is("Case Type not found Some Case Type in column 'CaseTypeID' in the sheet 'AccessTypeRoles'"));
    }

    @Test
    public void shouldFailWhenDateIsInvalid() {
        final DefinitionDataItem item = new DefinitionDataItem(ACCESS_TYPE_ROLES.getName());
        item.addAttribute(ColumnName.CASE_TYPE_ID.toString(), CASE_TYPE_ID_1);
        item.addAttribute(ColumnName.ACCESS_TYPE_ID.toString(), "access id");
        item.addAttribute(ColumnName.ORGANISATION_PROFILE_ID.toString(), "ProfileID");
        item.addAttribute(ColumnName.LIVE_FROM.toString(), "invalid date");

        definitionSheet.addDataItem(item);
        definitionSheets.put(ACCESS_TYPE_ROLES.getName(), definitionSheet);

        MapperException exception =
            assertThrows(MapperException.class, () -> accessTypeRolesParser.parse(definitionSheets, parseContext));
        assertThat(exception.getMessage(),
            is("Invalid value 'invalid date' is found in column 'LiveFrom' in the sheet 'AccessTypeRoles'"));
    }

    @Test
    public void shouldFailIfDisplayIsSetToTrueAndRequiredFieldsAreNotSet() {
        final DefinitionDataItem item = new DefinitionDataItem(ACCESS_TYPE_ROLES.getName());
        item.addAttribute(ColumnName.CASE_TYPE_ID.toString(), CASE_TYPE_ID_1);
        item.addAttribute(ColumnName.ACCESS_TYPE_ID.toString(), "access id");
        item.addAttribute(ColumnName.ORGANISATION_PROFILE_ID.toString(), "ProfileID");
        item.addAttribute(ColumnName.LIVE_FROM.toString(), Date.from(LocalDate.of(2023,
            Month.FEBRUARY, 12).atStartOfDay(ZoneId.systemDefault()).toInstant()));
        item.addAttribute(ColumnName.GROUP_ROLE_NAME.toString(), "Group role name");
        item.addAttribute(ColumnName.CASE_GROUP_ID_TEMPLATE.toString(), JURISDICTION + ":$ORGID$");
        item.addAttribute(ColumnName.ORGANISATION_POLICY_FIELD.toString(), ORG_POLICY_FIELD);
        item.addAttribute(ColumnName.DISPLAY, true);

        definitionSheet.addDataItem(item);
        definitionSheets.put(ACCESS_TYPE_ROLES.getName(), definitionSheet);

        ValidationException exception =
            assertThrows(ValidationException.class, () -> accessTypeRolesParser.parse(definitionSheets, parseContext));

        assertAll(
            () -> assertThat(exception.getValidationResult().getValidationErrors()
                .size() == 4, is(true)),
            () -> assertThat(exception.getValidationResult().getValidationErrors(), allOf(
                    hasItem(matchesValidationErrorWithDefaultMessage(
                        "'AccessMandatory' and 'AccessDefault' must be set to true for 'Display' to be used "
                            + "in the sheet 'AccessTypeRoles'")),
                    hasItem(matchesValidationErrorWithDefaultMessage(
                        "'Description' must be set for 'Display' to be used in the sheet 'AccessTypeRoles'")),
                    hasItem(matchesValidationErrorWithDefaultMessage(
                        "'HintText' must be set for 'Display' to be used in the sheet 'AccessTypeRoles'")),
                    hasItem(matchesValidationErrorWithDefaultMessage(
                        "'DisplayOrder' should not be null or empty for 'Display' to be used in column 'DisplayOrder' "
                            + "in the sheet 'AccessTypeRoles'"))
                )
            )
        );
    }

    @Test
    public void shouldFailIfAccessMandatoryIsNotSet() {
        final DefinitionDataItem item = new DefinitionDataItem(ACCESS_TYPE_ROLES.getName());
        item.addAttribute(ColumnName.CASE_TYPE_ID.toString(), CASE_TYPE_ID_1);
        item.addAttribute(ColumnName.ACCESS_TYPE_ID.toString(), "access id");
        item.addAttribute(ColumnName.ORGANISATION_PROFILE_ID.toString(), "ProfileID");
        item.addAttribute(ColumnName.LIVE_FROM.toString(), Date.from(LocalDate.of(2023,
            Month.FEBRUARY, 12).atStartOfDay(ZoneId.systemDefault()).toInstant()));
        item.addAttribute(ColumnName.GROUP_ROLE_NAME.toString(), "Group role name");
        item.addAttribute(ColumnName.CASE_GROUP_ID_TEMPLATE.toString(), JURISDICTION + ":$ORGID$");
        item.addAttribute(ColumnName.ORGANISATION_POLICY_FIELD.toString(), ORG_POLICY_FIELD);
        item.addAttribute(ColumnName.DISPLAY, true);
        item.addAttribute(ColumnName.DESCRIPTION.toString(), ACCESSTYPEROLES_DESCRIPTION);
        item.addAttribute(ColumnName.HINT_TEXT.toString(), "Hint");
        item.addAttribute(ColumnName.DISPLAY_ORDER, 1);
        item.addAttribute(ColumnName.ACCESS_DEFAULT, true);

        definitionSheet.addDataItem(item);
        definitionSheets.put(ACCESS_TYPE_ROLES.getName(), definitionSheet);

        ValidationException exception =
            assertThrows(ValidationException.class, () -> accessTypeRolesParser.parse(definitionSheets, parseContext));

        assertAll(
            () -> assertThat(exception.getValidationResult().getValidationErrors().size() == 1, is(true)),
            () -> assertThat(exception.getValidationResult().getValidationErrors(), allOf(
                    hasItem(matchesValidationErrorWithDefaultMessage(
                        "'AccessMandatory' and 'AccessDefault' must be set to true for 'Display' to be used "
                            + "in the sheet 'AccessTypeRoles'"))
                )
            )
        );
    }

    @Test
    public void shouldFailIfAccessDefaultIsNotSet() {
        final DefinitionDataItem item = new DefinitionDataItem(ACCESS_TYPE_ROLES.getName());
        item.addAttribute(ColumnName.CASE_TYPE_ID.toString(), CASE_TYPE_ID_1);
        item.addAttribute(ColumnName.ACCESS_TYPE_ID.toString(), "access id");
        item.addAttribute(ColumnName.ORGANISATION_PROFILE_ID.toString(), "ProfileID");
        item.addAttribute(ColumnName.LIVE_FROM.toString(), Date.from(LocalDate.of(2023,
            Month.FEBRUARY, 12).atStartOfDay(ZoneId.systemDefault()).toInstant()));
        item.addAttribute(ColumnName.GROUP_ROLE_NAME.toString(), "Group role name");
        item.addAttribute(ColumnName.CASE_GROUP_ID_TEMPLATE.toString(), JURISDICTION + ":$ORGID$");
        item.addAttribute(ColumnName.ORGANISATION_POLICY_FIELD.toString(), ORG_POLICY_FIELD);
        item.addAttribute(ColumnName.DISPLAY, true);
        item.addAttribute(ColumnName.DESCRIPTION.toString(), ACCESSTYPEROLES_DESCRIPTION);
        item.addAttribute(ColumnName.HINT_TEXT.toString(), "Hint");
        item.addAttribute(ColumnName.DISPLAY_ORDER, 1);
        item.addAttribute(ColumnName.ACCESS_MANDATORY, true);

        definitionSheet.addDataItem(item);
        definitionSheets.put(ACCESS_TYPE_ROLES.getName(), definitionSheet);

        ValidationException exception =
            assertThrows(ValidationException.class, () -> accessTypeRolesParser.parse(definitionSheets, parseContext));

        assertAll(
            () -> assertThat(exception.getValidationResult().getValidationErrors().size() == 1, is(true)),
            () -> assertThat(exception.getValidationResult().getValidationErrors(), allOf(
                    hasItem(matchesValidationErrorWithDefaultMessage(
                        "'AccessMandatory' and 'AccessDefault' must be set to true for 'Display' to be used "
                            + "in the sheet 'AccessTypeRoles'"))
                )
            )
        );
    }

    @Test
    public void shouldFailIfDisplayOrderIsInvalid() {
        final DefinitionDataItem item = new DefinitionDataItem(ACCESS_TYPE_ROLES.getName());
        item.addAttribute(ColumnName.CASE_TYPE_ID.toString(), CASE_TYPE_ID_1);
        item.addAttribute(ColumnName.ACCESS_TYPE_ID.toString(), "access id");
        item.addAttribute(ColumnName.ORGANISATION_PROFILE_ID.toString(), "ProfileID");
        item.addAttribute(ColumnName.LIVE_FROM.toString(), Date.from(LocalDate.of(2023,
            Month.FEBRUARY, 12).atStartOfDay(ZoneId.systemDefault()).toInstant()));
        item.addAttribute(ColumnName.GROUP_ROLE_NAME.toString(), "Group role name");
        item.addAttribute(ColumnName.CASE_GROUP_ID_TEMPLATE.toString(), JURISDICTION + ":$ORGID$");
        item.addAttribute(ColumnName.ORGANISATION_POLICY_FIELD.toString(), ORG_POLICY_FIELD);
        item.addAttribute(ColumnName.DISPLAY, true);
        item.addAttribute(ColumnName.DESCRIPTION.toString(), ACCESSTYPEROLES_DESCRIPTION);
        item.addAttribute(ColumnName.HINT_TEXT.toString(), "Hint");
        item.addAttribute(ColumnName.DISPLAY_ORDER, 0);
        item.addAttribute(ColumnName.ACCESS_DEFAULT, true);
        item.addAttribute(ColumnName.ACCESS_MANDATORY, true);

        definitionSheet.addDataItem(item);
        definitionSheets.put(ACCESS_TYPE_ROLES.getName(), definitionSheet);

        ValidationException exception =
            assertThrows(ValidationException.class, () -> accessTypeRolesParser.parse(definitionSheets, parseContext));

        assertAll(
            () -> assertThat(exception.getValidationResult().getValidationErrors().size() == 1, is(true)),
            () -> assertThat(exception.getValidationResult().getValidationErrors(), allOf(
                    hasItem(matchesValidationErrorWithDefaultMessage(
                        "'DisplayOrder' must be greater than 0 in column 'DisplayOrder' "
                            + "in the sheet 'AccessTypeRoles'"))
                )
            )
        );
    }

    @Test
    public void shouldFailIfDisplayOrderIsNotUnique() {
        final DefinitionDataItem item = new DefinitionDataItem(ACCESS_TYPE_ROLES.getName());
        item.addAttribute(ColumnName.CASE_TYPE_ID.toString(), CASE_TYPE_ID_1);
        item.addAttribute(ColumnName.ACCESS_TYPE_ID.toString(), "access id");
        item.addAttribute(ColumnName.ORGANISATION_PROFILE_ID.toString(), "ProfileID");
        item.addAttribute(ColumnName.LIVE_FROM.toString(), Date.from(LocalDate.of(2023,
            Month.FEBRUARY, 12).atStartOfDay(ZoneId.systemDefault()).toInstant()));
        item.addAttribute(ColumnName.GROUP_ROLE_NAME.toString(), "Group role name");
        item.addAttribute(ColumnName.CASE_GROUP_ID_TEMPLATE.toString(), JURISDICTION + ":$ORGID$");
        item.addAttribute(ColumnName.ORGANISATION_POLICY_FIELD.toString(), ORG_POLICY_FIELD);
        item.addAttribute(ColumnName.DISPLAY, true);
        item.addAttribute(ColumnName.DESCRIPTION.toString(), ACCESSTYPEROLES_DESCRIPTION);
        item.addAttribute(ColumnName.HINT_TEXT.toString(), "Hint");
        item.addAttribute(ColumnName.DISPLAY_ORDER, 1);
        item.addAttribute(ColumnName.ACCESS_DEFAULT, true);
        item.addAttribute(ColumnName.ACCESS_MANDATORY, true);
        definitionSheet.addDataItem(item);

        final DefinitionDataItem item2 = new DefinitionDataItem(ACCESS_TYPE_ROLES.getName());
        item2.addAttribute(ColumnName.CASE_TYPE_ID.toString(), CASE_TYPE_ID_1);
        item2.addAttribute(ColumnName.ACCESS_TYPE_ID.toString(), "access id2");
        item2.addAttribute(ColumnName.ORGANISATION_PROFILE_ID.toString(), "ProfileID2");
        item2.addAttribute(ColumnName.LIVE_FROM.toString(), Date.from(LocalDate.of(2023,
            Month.FEBRUARY, 12).atStartOfDay(ZoneId.systemDefault()).toInstant()));
        item2.addAttribute(ColumnName.GROUP_ROLE_NAME.toString(), "Group role name");
        item2.addAttribute(ColumnName.CASE_GROUP_ID_TEMPLATE.toString(), JURISDICTION + ":$ORGID$");
        item2.addAttribute(ColumnName.ORGANISATION_POLICY_FIELD.toString(), ORG_POLICY_FIELD);
        item2.addAttribute(ColumnName.DISPLAY, true);
        item2.addAttribute(ColumnName.DESCRIPTION.toString(), ACCESSTYPEROLES_DESCRIPTION);
        item2.addAttribute(ColumnName.HINT_TEXT.toString(), "Hint");
        item2.addAttribute(ColumnName.DISPLAY_ORDER, 1);
        item2.addAttribute(ColumnName.ACCESS_DEFAULT, true);
        item2.addAttribute(ColumnName.ACCESS_MANDATORY, true);
        definitionSheet.addDataItem(item2);

        definitionSheets.put(ACCESS_TYPE_ROLES.getName(), definitionSheet);

        ValidationException exception =
            assertThrows(ValidationException.class, () -> accessTypeRolesParser.parse(definitionSheets, parseContext));

        assertAll(
            () -> assertThat(exception.getValidationResult().getValidationErrors().size() == 2, is(true)),
            () -> assertThat(exception.getValidationResult().getValidationErrors(), allOf(
                    hasItem(matchesValidationErrorWithDefaultMessage(
                        "'DisplayOrder' must be unique across all Case Types for a given Jurisdiction "
                            + "in the sheet 'AccessTypeRoles'"))
                )
            )
        );
    }

    @Test
    public void shouldReturnEmptyOptionalWhenAccessTypeRolesSheetHasNoItems() {
        List<AccessTypeRolesEntity> accessTypeRolesEntities =
            accessTypeRolesParser.parse(definitionSheets, parseContext);

        assertAll(
            () -> assertThat(!accessTypeRolesEntities.isEmpty(), is(false))
        );
    }

    @Test
    void shouldFailWhenRequiredFieldsAreNotProvided() {

        final DefinitionDataItem item = new DefinitionDataItem(ACCESS_TYPE_ROLES.getName());
        item.addAttribute(ColumnName.CASE_TYPE_ID.toString(), CASE_TYPE_ID_1);
        item.addAttribute(ColumnName.GROUP_ROLE_NAME.toString(), "Group role name");
        item.addAttribute(ColumnName.CASE_GROUP_ID_TEMPLATE.toString(), JURISDICTION + ":$ORGID$");
        item.addAttribute(ColumnName.ORGANISATION_POLICY_FIELD.toString(), ORG_POLICY_FIELD);
        definitionSheet.addDataItem(item);
        definitionSheets.put(ACCESS_TYPE_ROLES.getName(), definitionSheet);

        ValidationException exception =
            assertThrows(ValidationException.class, () -> accessTypeRolesParser.parse(definitionSheets, parseContext));

        assertAll(
            () -> assertThat(exception.getValidationResult().getValidationErrors().size() == 2, is(true)),
            () -> assertThat(exception.getValidationResult().getValidationErrors(), allOf(
                    hasItem(matchesValidationErrorWithDefaultMessage(
                        "Access Type ID should not be null or empty in column 'AccessTypeID' "
                            + "in the sheet 'AccessTypeRoles'")),
                    hasItem(matchesValidationErrorWithDefaultMessage(
                        "Organisation Profile ID should not be null or empty in column 'OrganisationProfileID' "
                            + "in the sheet 'AccessTypeRoles'"))
                )
            )
        );
    }

    @Test
    public void shouldFailWhenAccessTypeIdIsNotUnique() {
        final DefinitionDataItem item = new DefinitionDataItem(ACCESS_TYPE_ROLES.getName());
        item.addAttribute(ColumnName.CASE_TYPE_ID.toString(), CASE_TYPE_ID_1);
        item.addAttribute(ColumnName.ACCESS_TYPE_ID.toString(), "access id");
        item.addAttribute(ColumnName.ORGANISATION_PROFILE_ID.toString(), "ProfileID");
        item.addAttribute(ColumnName.GROUP_ROLE_NAME.toString(), "Group role name");
        item.addAttribute(ColumnName.CASE_GROUP_ID_TEMPLATE.toString(), JURISDICTION + ":$ORGID$");
        item.addAttribute(ColumnName.ORGANISATION_POLICY_FIELD.toString(), ORG_POLICY_FIELD);
        definitionSheet.addDataItem(item);

        final DefinitionDataItem item2 = new DefinitionDataItem(ACCESS_TYPE_ROLES.getName());
        item2.addAttribute(ColumnName.CASE_TYPE_ID.toString(), CASE_TYPE_ID_1);
        item2.addAttribute(ColumnName.ACCESS_TYPE_ID.toString(), "access id");
        item2.addAttribute(ColumnName.ORGANISATION_PROFILE_ID.toString(), "ProfileID");
        item2.addAttribute(ColumnName.GROUP_ROLE_NAME.toString(), "Group role name");
        item2.addAttribute(ColumnName.CASE_GROUP_ID_TEMPLATE.toString(), JURISDICTION + ":$ORGID$");
        item2.addAttribute(ColumnName.ORGANISATION_POLICY_FIELD.toString(), ORG_POLICY_FIELD);
        definitionSheet.addDataItem(item);

        definitionSheets.put(ACCESS_TYPE_ROLES.getName(), definitionSheet);

        ValidationException exception =
            assertThrows(ValidationException.class, () -> accessTypeRolesParser.parse(definitionSheets, parseContext));

        assertAll(
            () -> assertThat(exception.getValidationResult().getValidationErrors().size() == 2, is(true)),
            () -> assertThat(exception.getValidationResult().getValidationErrors(), allOf(
                    hasItem(matchesValidationErrorWithDefaultMessage(
                        "'AccessTypeID' must be unique within the Jurisdiction in the sheet 'AccessTypeRoles'")))
            )
        );

    }

    @Test
    public void shouldFailIfCaseAccessGroupIDTemplateIsInvalid() {
        final DefinitionDataItem item = new DefinitionDataItem(ACCESS_TYPE_ROLES.getName());
        item.addAttribute(ColumnName.CASE_TYPE_ID.toString(), CASE_TYPE_ID_1);
        item.addAttribute(ColumnName.ACCESS_TYPE_ID.toString(), "access id");
        item.addAttribute(ColumnName.ORGANISATION_PROFILE_ID.toString(), "ProfileID");
        item.addAttribute(ColumnName.GROUP_ROLE_NAME.toString(), "Group role name");
        item.addAttribute(ColumnName.CASE_GROUP_ID_TEMPLATE.toString(), "INVALID:$INVALID$");
        item.addAttribute(ColumnName.ORGANISATION_POLICY_FIELD.toString(), ORG_POLICY_FIELD);

        definitionSheet.addDataItem(item);
        definitionSheets.put(ACCESS_TYPE_ROLES.getName(), definitionSheet);

        ValidationException exception =
            assertThrows(ValidationException.class, () -> accessTypeRolesParser.parse(definitionSheets, parseContext));

        assertAll(
            () -> assertThat(exception.getValidationResult().getValidationErrors().size() == 2, is(true)),
            () -> assertThat(exception.getValidationResult().getValidationErrors(), allOf(
                    hasItem(matchesValidationErrorWithDefaultMessage(
                        "'INVALID:$INVALID$' must start with Service Name in column 'CaseAccessGroupIDTemplate' "
                            + "in the sheet 'AccessTypeRoles'")),
                    hasItem(matchesValidationErrorWithDefaultMessage(
                        "'INVALID:$INVALID$' must end with $ORGID$ column 'CaseAccessGroupIDTemplate' "
                            + "in the sheet 'AccessTypeRoles'"))
                )
            )
        );
    }

    @Test
    public void shouldFailIfCaseAccessGroupIDTemplateOrOrgFieldIsMissing() {
        final DefinitionDataItem item = new DefinitionDataItem(ACCESS_TYPE_ROLES.getName());
        item.addAttribute(ColumnName.CASE_TYPE_ID.toString(), CASE_TYPE_ID_1);
        item.addAttribute(ColumnName.ACCESS_TYPE_ID.toString(), "access id");
        item.addAttribute(ColumnName.ORGANISATION_PROFILE_ID.toString(), "ProfileID");
        item.addAttribute(ColumnName.GROUP_ROLE_NAME.toString(), "Group role name");

        definitionSheet.addDataItem(item);
        definitionSheets.put(ACCESS_TYPE_ROLES.getName(), definitionSheet);

        ValidationException exception =
            assertThrows(ValidationException.class, () -> accessTypeRolesParser.parse(definitionSheets, parseContext));

        assertAll(
            () -> assertThat(exception.getValidationResult().getValidationErrors().size() == 2, is(true)),
            () -> assertThat(exception.getValidationResult().getValidationErrors(), allOf(
                    hasItem(matchesValidationErrorWithDefaultMessage(
                        "'CaseAccessGroupIDTemplate' must be set if 'GroupRoleName' is not null "
                            + "in the sheet 'AccessTypeRoles'")),
                    hasItem((matchesValidationErrorWithDefaultMessage(
                        "'OrganisationPolicyField' must be set if 'GroupRoleName' is not null "
                            + "in the sheet 'AccessTypeRoles'"
                    )))
                )
            )
        );
    }

    @Test
    public void shouldFailIfARoleNameIsNotProvided() {
        final DefinitionDataItem item = new DefinitionDataItem(ACCESS_TYPE_ROLES.getName());
        item.addAttribute(ColumnName.CASE_TYPE_ID.toString(), CASE_TYPE_ID_1);
        item.addAttribute(ColumnName.ACCESS_TYPE_ID.toString(), "access id");
        item.addAttribute(ColumnName.ORGANISATION_PROFILE_ID.toString(), "ProfileID");

        definitionSheet.addDataItem(item);
        definitionSheets.put(ACCESS_TYPE_ROLES.getName(), definitionSheet);

        ValidationException exception =
            assertThrows(ValidationException.class, () -> accessTypeRolesParser.parse(definitionSheets, parseContext));

        assertAll(
            () -> assertThat(exception.getValidationResult().getValidationErrors().size() == 1, is(true)),
            () -> assertThat(exception.getValidationResult().getValidationErrors(), allOf(
                    hasItem(matchesValidationErrorWithDefaultMessage(
                        "Either 'OrganisationalRoleName' or 'GroupRoleName' must be set "
                            + "in the sheet 'AccessTypeRoles'"))
                )
            )
        );
    }

    @Test
    public void shouldFailIfInvalidOrgPolicyFieldIsProvided() {
        final DefinitionDataItem item = new DefinitionDataItem(ACCESS_TYPE_ROLES.getName());
        item.addAttribute(ColumnName.CASE_TYPE_ID.toString(), CASE_TYPE_ID_1);
        item.addAttribute(ColumnName.ACCESS_TYPE_ID.toString(), "access id");
        item.addAttribute(ColumnName.ORGANISATION_PROFILE_ID.toString(), "ProfileID");
        item.addAttribute(ColumnName.GROUP_ROLE_NAME.toString(), "Group role name");
        item.addAttribute(ColumnName.CASE_GROUP_ID_TEMPLATE.toString(), JURISDICTION + ":$ORGID$");
        item.addAttribute(ColumnName.ORGANISATION_POLICY_FIELD.toString(), ORG_POLICY_FIELD2);

        definitionSheet.addDataItem(item);
        definitionSheets.put(ACCESS_TYPE_ROLES.getName(), definitionSheet);

        ValidationException exception =
            assertThrows(ValidationException.class, () -> accessTypeRolesParser.parse(definitionSheets, parseContext));

        assertAll(
            () -> assertThat(exception.getValidationResult().getValidationErrors().size() == 1, is(true)),
            () -> assertThat(exception.getValidationResult().getValidationErrors(), allOf(
                    hasItem(matchesValidationErrorWithDefaultMessage(
                        "'PolicyField1' is not a field with the type OrganisationPolicy "
                            + "in column 'OrganisationPolicyField' in the sheet 'AccessTypeRoles'"))
                )
            )
        );
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
