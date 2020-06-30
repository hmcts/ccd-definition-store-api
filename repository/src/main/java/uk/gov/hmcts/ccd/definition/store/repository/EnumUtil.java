package uk.gov.hmcts.ccd.definition.store.repository;

public class EnumUtil {

    private EnumUtil() {
        // Hide Utility Class Constructor : Utility classes should not have a public or default constructor (squid:S1118)
    }

    /**
     * A common method for all enums since they can't have another base class.
     * @param <T> Enum type
     * @param c enum type. All enums must be all caps.
     * @param enumConstant case insensitive representation of Enum
     * @return corresponding enum, or null
     * @link https://stackoverflow.com/questions/604424/lookup-enum-by-string-value
     */
    public static <T extends Enum<T>> T getEnumFromString(Class<T> c, String enumConstant) {
        if (c == null) {
            throw new IllegalArgumentException("Class argument cannot be null");
        }
        if (enumConstant == null) {
            throw new IllegalArgumentException("Enum constant argument cannot be null");
        }
        return Enum.valueOf(c, enumConstant.toUpperCase());
    }
}
