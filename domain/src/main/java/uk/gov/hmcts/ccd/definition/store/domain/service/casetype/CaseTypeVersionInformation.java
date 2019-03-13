package uk.gov.hmcts.ccd.definition.store.domain.service.casetype;

import java.io.Serializable;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class CaseTypeVersionInformation implements Serializable {

    private static final long serialVersionUID = -6848149182886575559L;

    private final Integer version;

    public CaseTypeVersionInformation(final Integer version) {
        this.version = version;
    }

    public Integer getVersion() {
        return version;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

}
