package uk.gov.hmcts.ccd.definition.store.domain.validation.displaycontextparameter;

import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.definition.store.domain.displaycontextparameter.DisplayContextParameterType;

@Service
public class DisplayContextParameterValidatorFactory {

    private static final Map<DisplayContextParameterType, DisplayContextParameterValidator> validatorCache =
        new EnumMap<>(DisplayContextParameterType.class);

    @Autowired
    public DisplayContextParameterValidatorFactory(List<DisplayContextParameterValidator> validators) {
        for (DisplayContextParameterValidator validator : validators) {
            validatorCache.put(validator.getType(), validator);
        }
    }

    public DisplayContextParameterValidator getValidator(DisplayContextParameterType displayContextParameterType) {
        return Optional.ofNullable(validatorCache.get(displayContextParameterType)).orElseThrow(NoSuchElementException::new);
    }
}
