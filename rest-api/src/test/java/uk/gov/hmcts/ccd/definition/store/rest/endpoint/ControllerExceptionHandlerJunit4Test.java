package uk.gov.hmcts.ccd.definition.store.rest.endpoint;

import org.hamcrest.collection.IsMapContaining;
import org.junit.Before;
import org.junit.Test;
import uk.gov.hmcts.ccd.definition.store.domain.exception.NotFoundException;
import uk.gov.hmcts.ccd.definition.store.domain.service.legacyvalidation.CaseTypeValidationException;
import uk.gov.hmcts.ccd.definition.store.domain.service.legacyvalidation.rules.CaseTypeValidationResult;
import uk.gov.hmcts.ccd.definition.store.rest.endpoint.exceptions.DuplicateFoundException;

import java.util.Map;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * I am here to resolve a sonar issue.
 */
public class ControllerExceptionHandlerJunit4Test {

    private ControllerExceptionHandler handler;

    @Before
    public void setup() {
        handler = new ControllerExceptionHandler();
    }

    @Test
    public void objectNotFound() {
        final NotFoundException e = new NotFoundException("message");
        final Map<String, String> map = handler.objectNotFound(e);
        assertThat(map.size(), is(1));
        assertThat(map, IsMapContaining.hasEntry("message", "Object Not Found for:message"));
    }

    @Test
    public void objectFound() {
        final DuplicateFoundException e = new DuplicateFoundException("message");
        final Map<String, String> map = handler.objectFound(e);
        assertThat(map.size(), is(1));
        assertThat(map, IsMapContaining.hasEntry("message", "Object already exists for:message"));
    }

    @Test
    public void caseTypeValidationEmptyMessage() {
        final CaseTypeValidationResult r = new CaseTypeValidationResult();
        final CaseTypeValidationException e = new CaseTypeValidationException(r);
        final String s = handler.caseTypeValidation(e);
        assertThat(s, is(""));
    }
}
