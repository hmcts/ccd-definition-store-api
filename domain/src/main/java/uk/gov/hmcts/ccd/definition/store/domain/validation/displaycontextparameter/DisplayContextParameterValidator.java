package uk.gov.hmcts.ccd.definition.store.domain.validation.displaycontextparameter;

import uk.gov.hmcts.ccd.definition.store.domain.displaycontextparameter.DisplayContextParameterType;

public interface DisplayContextParameterValidator {

    DisplayContextParameterType getType();

    void validate(String parameterValue, String fieldType) throws Exception;
}
