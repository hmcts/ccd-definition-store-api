package uk.gov.hmcts.ccd.definition.store.excel.parser;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.JurisdictionEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.StateEntity;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import static com.github.npathai.hamcrestopt.OptionalMatchers.isEmpty;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

public class ParseContextTest {

    private ParseContext parseContext;

    @Before
    public void setup() {
        parseContext = new ParseContext();
    }

    @Test(expected = SpreadsheetParsingException.class)
    public void shouldFail_whenRegisterCaseFieldTypeAgainForCaseType() {

        final CaseFieldEntity caseField = mock(CaseFieldEntity.class);
        given(caseField.getReference()).willReturn("case field id");

        parseContext.registerCaseFieldForCaseType("caseTypeId", caseField);

        try {
            parseContext.registerCaseFieldForCaseType("caseTypeId", caseField);
        } catch (SpreadsheetParsingException ex) {
            Assertions.assertThat(ex).hasMessage("Case field already registered for ID: case field id");
            throw ex;
        }
    }

    @Test
    public void shouldRegisterAndGetCaseFieldForCaseType() {

        final CaseFieldEntity caseField = mock(CaseFieldEntity.class);
        given(caseField.getReference()).willReturn("case field id");

        parseContext.registerCaseFieldForCaseType("caseTypeId", caseField);

        final CaseFieldEntity retrieved = parseContext.getCaseFieldForCaseType("caseTypeId", "case field id");
        assertThat(retrieved, is(caseField));
    }

    @Test
    public void shouldRegisterAndGetCaseFieldType() {

        final FieldTypeEntity fieldType = mock(FieldTypeEntity.class);
        parseContext.registerCaseFieldType("caseTypeId", "fieldId", fieldType);

        final FieldTypeEntity retrieved = parseContext.getCaseFieldType("caseTypeId", "fieldId");

        assertThat(retrieved, is(fieldType));
    }

    @Test(expected = SpreadsheetParsingException.class)
    public void shouldFail_whenRegisterCaseFieldTypeAgain() {

        final FieldTypeEntity fieldType = mock(FieldTypeEntity.class);
        parseContext.registerCaseFieldType("caseTypeId", "fieldId", fieldType);

        try {
            parseContext.registerCaseFieldType("caseTypeId", "fieldId", fieldType);
        } catch (SpreadsheetParsingException ex) {
            Assertions.assertThat(ex).hasMessage("Type already registered for field: fieldId");
            throw ex;
        }
    }

    @Test
    public void shouldRegisterAndGetStateForCaseType() {

        final StateEntity state = mock(StateEntity.class);
        given(state.getReference()).willReturn("some state id");
        parseContext.registerStateForCaseType("case type id", state);

        final StateEntity retrieved = parseContext.getStateForCaseType("case type id", "some state id");

        assertThat(retrieved, is(state));

    }

    @Test(expected = SpreadsheetParsingException.class)
    public void shouldFail_whenRegisterStateForCaseTypeAgain() {

        final StateEntity state = mock(StateEntity.class);
        given(state.getReference()).willReturn("some state id");
        parseContext.registerStateForCaseType("case type id", state);

        try {
            parseContext.registerStateForCaseType("case type id", state);
        } catch (SpreadsheetParsingException ex) {
            Assertions.assertThat(ex).hasMessage("State already registered for ID: some state id");
            throw ex;
        }
    }

    @Test
    public void shouldSetAndGetJurisdiction() {

        final JurisdictionEntity j = mock(JurisdictionEntity.class);
        parseContext.setJurisdiction(j);

        assertThat(parseContext.getJurisdiction(), is(j));
    }

    @Test
    public void shouldRegisterAndGetCaseType() {
        final CaseTypeEntity t1 = mock(CaseTypeEntity.class);
        final CaseTypeEntity t2 = mock(CaseTypeEntity.class);

        parseContext.registerCaseType(t1);
        parseContext.registerCaseType(t2);
        parseContext.registerCaseType(t1);

        final Set<CaseTypeEntity> caseTypes = parseContext.getCaseTypes();
        assertThat(caseTypes.size(), is(2));
        assertThat(caseTypes, containsInAnyOrder(t1, t2));
    }

    @Test(expected = SpreadsheetParsingException.class)
    public void shouldFail_whenCaseTypeNotRegistered() {
        try {
            parseContext.getCaseFieldType("ngitb", "case field");
        } catch (SpreadsheetParsingException ex) {
            Assertions.assertThat(ex).hasMessage("No types registered for case type: ngitb");
            throw ex;
        }
    }

    @Test(expected = SpreadsheetParsingException.class)
    public void shouldFail_whenCaseFieldNotRegistered() {
        try {
            final FieldTypeEntity f = mock(FieldTypeEntity.class);
            parseContext.registerCaseFieldType("ngitb", "case_field", f);
            parseContext.getCaseFieldType("ngitb", "case field");
        } catch (SpreadsheetParsingException ex) {
            Assertions.assertThat(ex).hasMessage("No types registered for case field ID: ngitb/case field");
            throw ex;
        }
    }

    @Test(expected = SpreadsheetParsingException.class)
    public void shouldFail_whenCaseTypeNotRegisteredForState() {
        try {
            parseContext.getStateForCaseType("ngitb", "state");
        } catch (SpreadsheetParsingException ex) {
            Assertions.assertThat(ex).hasMessage("No states registered for case type: ngitb");
            throw ex;
        }
    }

    @Test(expected = SpreadsheetParsingException.class)
    public void shouldFail_whenStateNotRegistered() {
        try {
            final StateEntity s = mock(StateEntity.class);
            given(s.getReference()).willReturn("State");

            parseContext.registerStateForCaseType("ngitb", s);
            parseContext.getStateForCaseType("ngitb", "state");

        } catch (SpreadsheetParsingException ex) {
            Assertions.assertThat(ex).hasMessage("No state registered for state ID: ngitb/state");
            throw ex;
        }
    }

    @Test(expected = SpreadsheetParsingException.class)
    public void shouldFail_whenCaseTypeNotRegisteredFormCaseField() {
        try {
            parseContext.getCaseFieldForCaseType("ngitb", "state");
        } catch (SpreadsheetParsingException ex) {
            Assertions.assertThat(ex).hasMessage("No case fields registered for case type: ngitb");
            throw ex;
        }
    }

    @Test(expected = SpreadsheetParsingException.class)
    public void shouldFail_whenCaseFieldNotRegisteredForCaseType() {
        try {

            final CaseFieldEntity o = mock(CaseFieldEntity.class);
            given(o.getReference()).willReturn("CF");

            parseContext.registerCaseFieldForCaseType("ngitb", o);
            parseContext.getCaseFieldForCaseType("ngitb", "cf");

        } catch (SpreadsheetParsingException ex) {
            Assertions.assertThat(ex).hasMessage("Unknown field cf for case type ngitb");
            throw ex;
        }
    }

    @Test
    public void shouldGetEmpty_whenGetBaseTypeNotExist() {
        final Optional<FieldTypeEntity> type = parseContext.getBaseType("type");
        assertThat(type, isEmpty());
    }

    @Test
    public void shouldAddAndGetBaseType() {

        final FieldTypeEntity t = mock(FieldTypeEntity.class);
        given(t.getReference()).willReturn("ngitb");

        parseContext.addBaseTypes(Arrays.asList(t));
        final Optional<FieldTypeEntity> type = parseContext.getBaseType("ngitb");
        assertThat(type.get(), is(t));
    }

    @Test
    public void shouldAddToAllTypesAndGet() {

        final FieldTypeEntity t = mock(FieldTypeEntity.class);
        given(t.getReference()).willReturn("ngitb");

        parseContext.addToAllTypes(Arrays.asList(t));
        final Optional<FieldTypeEntity> type = parseContext.getType("ngitb");
        assertThat(type.get(), is(t));
    }

    @Test
    public void shouldGetEmpty_whenGetTypeNotExist() {

        final Optional<FieldTypeEntity> type = parseContext.getType("ngitb");
        assertThat(type, isEmpty());
    }

    @Test
    public void shouldReturnMetadataField_whenGetCaseFieldForCaseTypeIsMetadataField() {
        String metadataFieldName = "METADATA";
        CaseFieldEntity caseField = new CaseFieldEntity();
        caseField.setReference("TEST");
        parseContext.registerCaseFieldForCaseType("caseTypeId", caseField);

        CaseFieldEntity metadataField = new CaseFieldEntity();
        metadataField.setReference(metadataFieldName);
        parseContext.registerMetadataFields(Collections.singletonList(metadataField));

        CaseFieldEntity response = parseContext.getCaseFieldForCaseType("caseTypeId",
                                                                        String.join(metadataFieldName, "[", "]"));

        assertThat(response, is(metadataField));
    }
}
