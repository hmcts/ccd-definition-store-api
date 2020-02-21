package uk.gov.hmcts.ccd.definition.store.domain.validation.displaycontextparameter;

import uk.gov.hmcts.ccd.definition.store.domain.displaycontextparameter.DisplayContextParameterType;

public interface DisplayContextParameterValidator {

    public DisplayContextParameterType getType();

    public void validate(String parameterValue) throws Exception;
}
