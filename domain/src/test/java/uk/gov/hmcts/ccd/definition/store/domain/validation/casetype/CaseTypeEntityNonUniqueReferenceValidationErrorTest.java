package uk.gov.hmcts.ccd.definition.store.domain.validation.casetype;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationErrorMessageCreator;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.model.CaseType;
import uk.gov.hmcts.ccd.definition.store.repository.model.Jurisdiction;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CaseTypeEntityNonUniqueReferenceValidationErrorTest {

    private static final String OVERRIDDEN_ERROR_MESSAGE = "The overridden error message";

    @Mock
    private ValidationErrorMessageCreator mockValidationErrorMessageCreator;

    private CaseTypeEntityNonUniqueReferenceValidationError classUnderTest;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        when(mockValidationErrorMessageCreator.createErrorMessage(any(CaseTypeEntityNonUniqueReferenceValidationError.class)))
            .thenReturn(OVERRIDDEN_ERROR_MESSAGE);
        classUnderTest = new CaseTypeEntityNonUniqueReferenceValidationError(caseTypeEntityWithReference("Charley says Dont talk to strangers"),
                                                                             caseTypeWithIdAndWithJurisdictionWithId("Charley says Dont talk to strangers",
                                                                                                                     "Charley's Jurisdiction"));
    }

    @Test
    public void testDefaultMessage() {
        assertEquals("Case Type with name 'Charley says Dont talk to strangers' already exists for 'Charley's Jurisdiction' jurisdiction. Case types must be unique across all existing jurisdictions.",
                     classUnderTest.getDefaultMessage());
    }

    @Test
    public void testCreateMessage() {
        assertEquals(OVERRIDDEN_ERROR_MESSAGE, classUnderTest.createMessage(mockValidationErrorMessageCreator));
        verify(mockValidationErrorMessageCreator).createErrorMessage(classUnderTest);
    }

    private CaseTypeEntity caseTypeEntityWithReference(String name) {
        CaseTypeEntity caseTypeEntity = new CaseTypeEntity();
        caseTypeEntity.setName(name);
        return caseTypeEntity;
    }

    private CaseType caseTypeWithIdAndWithJurisdictionWithId(String name, String jurisdictionName) {
        CaseType caseType = new CaseType();
        caseType.setName(name);
        Jurisdiction jurisdictionEntity = new Jurisdiction();
        jurisdictionEntity.setName(jurisdictionName);
        caseType.setJurisdiction(jurisdictionEntity);
        return caseType;
    }

}
