package uk.gov.hmcts.ccd.definition.store.domain.validation.displaycontextparameter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.domain.datetime.DateTimeFormatParser;
import uk.gov.hmcts.ccd.definition.store.domain.datetime.InvalidDateTimeFormatException;
import uk.gov.hmcts.ccd.definition.store.domain.displaycontextparameter.DisplayContextParameterType;

import java.util.regex.Pattern;

import static uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils.BASE_DATE;

@Component
public class ArgumentValidatorImpl implements DisplayContextParameterValidator {

    private DateTimeFormatParser dateTimeFormatParser;

    @Autowired
    public ArgumentValidatorImpl(DateTimeFormatParser dateTimeFormatParser) {
        this.dateTimeFormatParser = dateTimeFormatParser;
    }

    @Override
    public DisplayContextParameterType getType() {
        return DisplayContextParameterType.ARGUMENT;
    }

    @Override
    public void validate(final String parameterValue, String fieldType){
    }
}
