package uk.gov.hmcts.ccd.definition.store;

import static org.hamcrest.Matchers.hasProperty;

public class CustomHamcrestMatchers {

    public static <T> org.hamcrest.Matcher<java.lang.Iterable<? super T>> hasItemWithProperty(String property, org.hamcrest.Matcher<? super T> itemMatcher) {
        return org.hamcrest.core.IsCollectionContaining.hasItem(hasProperty(property, itemMatcher));
    }
}
