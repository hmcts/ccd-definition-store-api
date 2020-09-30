package uk.gov.hmcts.ccd.definition.store.domain.validation.displaycontextparameter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.definition.store.domain.displaycontextparameter.DisplayContextParameterType;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

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
        return Optional.ofNullable(
            validatorCache.get(displayContextParameterType)).orElseThrow(NoSuchElementException::new);
    }
}
