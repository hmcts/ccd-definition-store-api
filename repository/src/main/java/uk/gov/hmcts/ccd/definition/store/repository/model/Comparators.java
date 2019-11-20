package uk.gov.hmcts.ccd.definition.store.repository.model;

import java.util.Comparator;

public class Comparators {

    public static final Comparator<Orderable> NULLS_LAST_ORDER_COMPARATOR = (o1, o2) -> {
        if (o1.getOrder() == null) {
            return 1;
        }
        if (o2.getOrder() == null) {
            return -1;
        }
        return o1.getOrder().compareTo(o2.getOrder());

    };

}
