package uk.gov.hmcts.ccd.definition.store.rest.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

class IDAMPropertiesTest {

    ObjectMapper M = new ObjectMapper();
    
    @Test
    void x() throws JsonProcessingException {
        final IdamProperties p = new IdamProperties();
        p.setEmail("hello@example.com");
        p.setRoles(new String[]{"role1", "role2"});
        System.out.println(M.writeValueAsString(p));
    }

}
