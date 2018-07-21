package uk.gov.hmcts.ccd.definition.store.repository.model;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.util.Objects;

/**
 * A "lite" version of the {@link uk.gov.hmcts.ccd.definition.store.repository.entity.StateEntity} class that contains selected State fields (reference and id) for
 * display purposes. (Class introduced to break a circular dependency between Jurisdiction (containing a list of
 * states) and states (containing a reference to a parent CaseType), when using the Mapstruct mapper
 * interface.)
 */
public class StateLite {
    private String id = null;
    private String name =null;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
