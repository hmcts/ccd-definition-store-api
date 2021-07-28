package uk.gov.hmcts.ccd.definition.store.repository.model;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class RoleAssignmentTest {


    @Test
    public void shouldGetEntity_whenToEntity() {

        var roleAssignment = new RoleAssignment();
        var name = "name";
        var id = "id";
        roleAssignment.setId(id);
        roleAssignment.setName(name);
        assertThat(roleAssignment.getId(), is(id));
        assertThat(roleAssignment.getName(), is(name));

    }

}
