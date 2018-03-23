package uk.gov.hmcts.ccd.definition.store.repository.model;

import java.util.List;

public interface HasAcls {

    void setAcls(List<AccessControlList> acls);

}
