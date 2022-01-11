package uk.gov.hmcts.ccd.definition.store.domain.validation.displaycontextparameter;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.domain.displaycontextparameter.DisplayContextParameterType;


@Component
public class ArgumentValidatorImpl implements DisplayContextParameterValidator {

    @Override
    public DisplayContextParameterType getType() {
        return DisplayContextParameterType.ARGUMENT;
    }

    @Override
    public void validate(final String parameterValue, String fieldType){
    }
}
