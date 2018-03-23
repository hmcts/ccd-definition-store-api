package uk.gov.hmcts.ccd.definition.store.repository;

import java.util.Arrays;

public enum SecurityClassification {

    PUBLIC {
        @Override
        public boolean isMoreRestrictiveThan(SecurityClassification securityClassification) {
            return false;
        }
    },
    PRIVATE {
        @Override
        public boolean isMoreRestrictiveThan(SecurityClassification securityClassification) {
            return Arrays.asList(SecurityClassification.PUBLIC)
                .contains(securityClassification);
        }
    },
    RESTRICTED {
        @Override
        public boolean isMoreRestrictiveThan(SecurityClassification securityClassification) {
            return Arrays.asList(SecurityClassification.PUBLIC, SecurityClassification.PRIVATE)
                .contains(securityClassification);
        }
    };

    public abstract boolean isMoreRestrictiveThan(SecurityClassification securityClassification);
}
