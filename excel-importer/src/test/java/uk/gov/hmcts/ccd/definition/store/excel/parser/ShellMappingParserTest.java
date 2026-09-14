package uk.gov.hmcts.ccd.definition.store.excel.parser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationException;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionDataItem;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionSheet;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.ShellMappingEntity;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("ShellMapping Parser Test")
class ShellMappingParserTest extends ParserTestBase {

    private static final String SHELL_CASE_TYPE_ID = "ShellCaseType";
    private static final String ORIGINATING_CASE_TYPE_ID = "OriginatingCaseType";
    private static final String SHELL_CASE_FIELD_NAME = "shellField";
    private static final String ORIGINATING_CASE_FIELD_NAME = "origField";
    private static final LocalDate LIVE_FROM = LocalDate.of(2024, 1, 1);
    private static final LocalDate LIVE_TO = LocalDate.of(2024, 12, 31);

    private ShellMappingParser underTest;
    private ParseContext parseContext;
    private CaseTypeEntity shellCaseTypeEntity;
    private CaseTypeEntity originatingCaseTypeEntity;
    private CaseFieldEntity shellCaseFieldEntity;
    private CaseFieldEntity originatingCaseFieldEntity;

    @BeforeEach
    void setUp() {
        init();
        underTest = new ShellMappingParser();
        parseContext = new ParseContext();

        // Create shell case type with field
        shellCaseTypeEntity = new CaseTypeEntity();
        shellCaseTypeEntity.setReference(SHELL_CASE_TYPE_ID);
        shellCaseFieldEntity = new CaseFieldEntity();
        shellCaseFieldEntity.setReference(SHELL_CASE_FIELD_NAME);
        shellCaseTypeEntity.addCaseField(shellCaseFieldEntity);

        // Create originating case type with field
        originatingCaseTypeEntity = new CaseTypeEntity();
        originatingCaseTypeEntity.setReference(ORIGINATING_CASE_TYPE_ID);
        originatingCaseFieldEntity = new CaseFieldEntity();
        originatingCaseFieldEntity.setReference(ORIGINATING_CASE_FIELD_NAME);
        originatingCaseTypeEntity.addCaseField(originatingCaseFieldEntity);

        // Register case types in parse context
        parseContext.registerCaseType(shellCaseTypeEntity);
        parseContext.registerCaseType(originatingCaseTypeEntity);

        definitionSheets = new HashMap<>();
        definitionSheet = new DefinitionSheet();
        definitionSheet.setName(SheetName.SHELL_MAPPING.getName());
        definitionSheets.put(SheetName.SHELL_MAPPING.getName(), definitionSheet);
    }

    @Test
    @DisplayName("Should parse valid ShellMapping entity successfully")
    void shouldParseValidShellMappingEntity() {
        // Given
        DefinitionDataItem dataItem = buildDefinitionDataItem(
            SHELL_CASE_TYPE_ID,
            SHELL_CASE_FIELD_NAME,
            ORIGINATING_CASE_TYPE_ID,
            ORIGINATING_CASE_FIELD_NAME,
            LIVE_FROM,
            LIVE_TO
        );
        definitionSheet.addDataItem(dataItem);

        // When
        List<ShellMappingEntity> result = underTest.parse(definitionSheets, parseContext);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        ShellMappingEntity entity = result.get(0);
        assertAll(
            () -> assertThat(entity.getShellCaseTypeId(), is(notNullValue())),
            () -> assertThat(entity.getShellCaseTypeId().getReference(), is(SHELL_CASE_TYPE_ID)),
            () -> assertThat(entity.getShellCaseFieldName(), is(notNullValue())),
            () -> assertThat(entity.getShellCaseFieldName().getReference(), is(SHELL_CASE_FIELD_NAME)),
            () -> assertThat(entity.getOriginatingCaseTypeId(), is(notNullValue())),
            () -> assertThat(entity.getOriginatingCaseTypeId().getReference(), is(ORIGINATING_CASE_TYPE_ID)),
            () -> assertThat(entity.getOriginatingCaseFieldName(), is(notNullValue())),
            () -> assertThat(entity.getOriginatingCaseFieldName().getReference(), is(ORIGINATING_CASE_FIELD_NAME)),
            () -> assertThat(entity.getLiveFrom(), is(LIVE_FROM)),
            () -> assertThat(entity.getLiveTo(), is(LIVE_TO))
        );
    }

    @Test
    @DisplayName("Should parse multiple ShellMapping entities successfully")
    void shouldParseMultipleShellMappingEntities() {
        // Given
        String shellCaseType2 = "ShellCaseType2";
        String shellField2 = "shellField2";

        CaseTypeEntity shellCaseType2Entity = new CaseTypeEntity();
        shellCaseType2Entity.setReference(shellCaseType2);
        CaseFieldEntity shellField2Entity = new CaseFieldEntity();
        shellField2Entity.setReference(shellField2);
        shellCaseType2Entity.addCaseField(shellField2Entity);

        String origCaseType2 = "OriginatingCaseType2";
        String origField2 = "origField2";
        CaseTypeEntity origCaseType2Entity = new CaseTypeEntity();
        origCaseType2Entity.setReference(origCaseType2);
        CaseFieldEntity origField2Entity = new CaseFieldEntity();
        origField2Entity.setReference(origField2);
        origCaseType2Entity.addCaseField(origField2Entity);

        parseContext.registerCaseType(shellCaseType2Entity);
        parseContext.registerCaseType(origCaseType2Entity);

        DefinitionDataItem dataItem1 = buildDefinitionDataItem(
            SHELL_CASE_TYPE_ID,
            SHELL_CASE_FIELD_NAME,
            ORIGINATING_CASE_TYPE_ID,
            ORIGINATING_CASE_FIELD_NAME,
            LIVE_FROM,
            LIVE_TO
        );
        DefinitionDataItem dataItem2 = buildDefinitionDataItem(
            shellCaseType2,
            shellField2,
            origCaseType2,
            origField2,
            LIVE_FROM,
            LIVE_TO
        );
        definitionSheet.addDataItem(dataItem1);
        definitionSheet.addDataItem(dataItem2);

        // When
        List<ShellMappingEntity> result = underTest.parse(definitionSheets, parseContext);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertThat(result.get(0).getShellCaseTypeId().getReference(), is(SHELL_CASE_TYPE_ID));
        assertThat(result.get(1).getShellCaseTypeId().getReference(), is(shellCaseType2));
    }

    @Test
    @DisplayName("Should parse ShellMapping entity with null live dates")
    void shouldParseShellMappingEntityWithNullLiveDates() {
        // Given
        DefinitionDataItem dataItem = buildDefinitionDataItem(
            SHELL_CASE_TYPE_ID,
            SHELL_CASE_FIELD_NAME,
            ORIGINATING_CASE_TYPE_ID,
            ORIGINATING_CASE_FIELD_NAME,
            null,
            null
        );
        definitionSheet.addDataItem(dataItem);

        // When
        List<ShellMappingEntity> result = underTest.parse(definitionSheets, parseContext);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        ShellMappingEntity entity = result.get(0);
        assertThat(entity.getLiveFrom(), is(nullValue()));
        assertThat(entity.getLiveTo(), is(nullValue()));
    }

    @Test
    @DisplayName("Should throw ValidationException when shell case type not found")
    void shouldThrowValidationExceptionWhenShellCaseTypeNotFound() {
        // Given
        DefinitionDataItem dataItem = buildDefinitionDataItem(
            "NonExistentShellCaseType",
            SHELL_CASE_FIELD_NAME,
            ORIGINATING_CASE_TYPE_ID,
            ORIGINATING_CASE_FIELD_NAME,
            LIVE_FROM,
            LIVE_TO
        );
        definitionSheet.addDataItem(dataItem);

        // When/Then
        ValidationException exception = assertThrows(ValidationException.class,
            () -> underTest.parse(definitionSheets, parseContext));
        assertNotNull(exception);
    }

    @Test
    @DisplayName("Should throw ValidationException when shell case field not found")
    void shouldThrowValidationExceptionWhenShellCaseFieldNotFound() {
        // Given
        DefinitionDataItem dataItem = buildDefinitionDataItem(
            SHELL_CASE_TYPE_ID,
            "NonExistentShellField",
            ORIGINATING_CASE_TYPE_ID,
            ORIGINATING_CASE_FIELD_NAME,
            LIVE_FROM,
            LIVE_TO
        );
        definitionSheet.addDataItem(dataItem);

        // When/Then
        ValidationException exception = assertThrows(ValidationException.class,
            () -> underTest.parse(definitionSheets, parseContext));
        assertNotNull(exception);
    }

    @Test
    @DisplayName("Should throw ValidationException when originating case type not found")
    void shouldThrowValidationExceptionWhenOriginatingCaseTypeNotFound() {
        // Given
        DefinitionDataItem dataItem = buildDefinitionDataItem(
            SHELL_CASE_TYPE_ID,
            SHELL_CASE_FIELD_NAME,
            "NonExistentOriginatingCaseType",
            ORIGINATING_CASE_FIELD_NAME,
            LIVE_FROM,
            LIVE_TO
        );
        definitionSheet.addDataItem(dataItem);

        // When/Then
        ValidationException exception = assertThrows(ValidationException.class,
            () -> underTest.parse(definitionSheets, parseContext));
        assertNotNull(exception);
    }

    @Test
    @DisplayName("Should throw ValidationException when shell case type and originating case type are same")
    void shouldThrowValidationExceptionWhenShellCaseTypeAndOriginatingCaseTypeAreSame() {
        // Given - use same case type for both shell and originating
        DefinitionDataItem dataItem = buildDefinitionDataItem(
            SHELL_CASE_TYPE_ID,
            SHELL_CASE_FIELD_NAME,
            SHELL_CASE_TYPE_ID, // Same as shell case type
            ORIGINATING_CASE_FIELD_NAME,
            LIVE_FROM,
            LIVE_TO
        );
        definitionSheet.addDataItem(dataItem);

        // When/Then
        ValidationException exception = assertThrows(ValidationException.class,
            () -> underTest.parse(definitionSheets, parseContext));
        assertNotNull(exception);
    }

    @Test
    @DisplayName("Should throw ValidationException when shell case type and originating "
        + "case type are same (case insensitive)")
    void shouldThrowValidationExceptionWhenShellCaseTypeAndOriginatingCaseTypeAreSameCaseInsensitive() {
        // Given - use same case type with different case for shell and originating
        DefinitionDataItem dataItem = buildDefinitionDataItem(
            SHELL_CASE_TYPE_ID,
            SHELL_CASE_FIELD_NAME,
            SHELL_CASE_TYPE_ID.toLowerCase(), // Same as shell case type but different case
            ORIGINATING_CASE_FIELD_NAME,
            LIVE_FROM,
            LIVE_TO
        );
        definitionSheet.addDataItem(dataItem);

        // When/Then
        ValidationException exception = assertThrows(ValidationException.class,
            () -> underTest.parse(definitionSheets, parseContext));
        assertNotNull(exception);
    }

    @Test
    @DisplayName("Should throw ValidationException when originating case field not found")
    void shouldThrowValidationExceptionWhenOriginatingCaseFieldNotFound() {
        // Given
        DefinitionDataItem dataItem = buildDefinitionDataItem(
            SHELL_CASE_TYPE_ID,
            SHELL_CASE_FIELD_NAME,
            ORIGINATING_CASE_TYPE_ID,
            "NonExistentOrigField",
            LIVE_FROM,
            LIVE_TO
        );
        definitionSheet.addDataItem(dataItem);

        // When/Then
        ValidationException exception = assertThrows(ValidationException.class,
            () -> underTest.parse(definitionSheets, parseContext));
        assertNotNull(exception);
    }

    @Test
    @DisplayName("Should return empty list when sheet has no data items")
    void shouldReturnEmptyListWhenSheetHasNoDataItems() {
        // Given - no data items added

        // When
        List<ShellMappingEntity> result = underTest.parse(definitionSheets, parseContext);

        // Then
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    @DisplayName("Should handle case type with multiple fields correctly")
    void shouldHandleCaseTypeWithMultipleFieldsCorrectly() {
        // Given - add additional field to shell case type
        CaseFieldEntity additionalField = new CaseFieldEntity();
        additionalField.setReference("additionalField");
        shellCaseTypeEntity.addCaseField(additionalField);

        DefinitionDataItem dataItem = buildDefinitionDataItem(
            SHELL_CASE_TYPE_ID,
            SHELL_CASE_FIELD_NAME,
            ORIGINATING_CASE_TYPE_ID,
            ORIGINATING_CASE_FIELD_NAME,
            LIVE_FROM,
            LIVE_TO
        );
        definitionSheet.addDataItem(dataItem);

        // When
        List<ShellMappingEntity> result = underTest.parse(definitionSheets, parseContext);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertThat(result.get(0).getShellCaseFieldName().getReference(), is(SHELL_CASE_FIELD_NAME));
    }

    @Test
    @DisplayName("Should throw ValidationException when duplicate ShellCaseTypeID "
        + "and ShellCaseFieldName combination found")
    void shouldThrowValidationExceptionWhenDuplicateShellCaseTypeAndFieldFound() {
        // Given - add two data items with same ShellCaseTypeID and ShellCaseFieldName
        DefinitionDataItem dataItem1 = buildDefinitionDataItem(
            SHELL_CASE_TYPE_ID,
            SHELL_CASE_FIELD_NAME,
            ORIGINATING_CASE_TYPE_ID,
            ORIGINATING_CASE_FIELD_NAME,
            LIVE_FROM,
            LIVE_TO
        );
        DefinitionDataItem dataItem2 = buildDefinitionDataItem(
            SHELL_CASE_TYPE_ID,
            SHELL_CASE_FIELD_NAME,
            ORIGINATING_CASE_TYPE_ID,
            ORIGINATING_CASE_FIELD_NAME,
            LIVE_FROM,
            LIVE_TO
        );
        definitionSheet.addDataItem(dataItem1);
        definitionSheet.addDataItem(dataItem2);

        // When/Then
        ValidationException exception = assertThrows(ValidationException.class,
            () -> underTest.parse(definitionSheets, parseContext));
        assertNotNull(exception);
    }

    private DefinitionDataItem buildDefinitionDataItem(String shellCaseTypeId,
                                                        String shellCaseFieldName,
                                                        String originatingCaseTypeId,
                                                        String originatingCaseFieldName,
                                                        LocalDate liveFrom,
                                                        LocalDate liveTo) {
        DefinitionDataItem item = new DefinitionDataItem(SheetName.SHELL_MAPPING.toString());
        item.addAttribute(ColumnName.SHELL_CASE_TYPE_ID, shellCaseTypeId);
        item.addAttribute(ColumnName.SHELL_CASE_FIELD_NAME, shellCaseFieldName);
        item.addAttribute(ColumnName.ORIGINATING_CASE_TYPE_ID, originatingCaseTypeId);
        item.addAttribute(ColumnName.ORIGINATING_CASE_FIELD_NAME, originatingCaseFieldName);
        if (liveFrom != null) {
            Date liveFromDate = Date.from(liveFrom.atStartOfDay(ZoneId.systemDefault()).toInstant());
            item.addAttribute(ColumnName.LIVE_FROM, liveFromDate);
        }
        if (liveTo != null) {
            Date liveToDate = Date.from(liveTo.atStartOfDay(ZoneId.systemDefault()).toInstant());
            item.addAttribute(ColumnName.LIVE_TO, liveToDate);
        }
        return item;
    }
}
