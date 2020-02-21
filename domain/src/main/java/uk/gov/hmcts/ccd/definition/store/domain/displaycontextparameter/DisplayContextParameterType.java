package uk.gov.hmcts.ccd.definition.store.domain.displaycontextparameter;

import com.google.common.base.Strings;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum DisplayContextParameterType {
    DATETIMEENTRY,
    DATETIMEDISPLAY,
    TABLE,
    LIST;

    private static final Pattern PATTERN = Pattern.compile("#(.+)\\((.+)\\)");
    private static final int TYPE_GROUP = 1;
    private static final int VALUE_GROUP = 2;

    private static final String ERROR_MESSAGE_INVALID_PARAMETER = "Invalid display context parameter '%s'";

    public static DisplayContextParameterType getParameterTypeFor(String displayContextParameter) throws IllegalArgumentException {
        Matcher m = PATTERN.matcher(displayContextParameter);
        if (m.matches()) {
            return valueOf(m.group(TYPE_GROUP));
        }
        throw new IllegalArgumentException(String.format(ERROR_MESSAGE_INVALID_PARAMETER, displayContextParameter));
    }

    public static String getParameterValueFor(String displayContextParameter) throws IllegalArgumentException {
        Matcher m = PATTERN.matcher(displayContextParameter);
        if (m.matches() && !Strings.isNullOrEmpty(m.group(VALUE_GROUP))) {
            return m.group(VALUE_GROUP);
        }
        throw new IllegalArgumentException(String.format(ERROR_MESSAGE_INVALID_PARAMETER, displayContextParameter));
    }
}
