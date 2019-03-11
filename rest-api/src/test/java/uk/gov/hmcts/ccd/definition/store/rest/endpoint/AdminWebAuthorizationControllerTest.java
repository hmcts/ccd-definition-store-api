package uk.gov.hmcts.ccd.definition.store.rest.endpoint;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import uk.gov.hmcts.ccd.definition.store.rest.configuration.AdminWebAuthorizationProperties;
import uk.gov.hmcts.ccd.definition.store.rest.endpoint.exceptions.RestEndPointExceptionHandler;
import uk.gov.hmcts.ccd.definition.store.rest.model.AdminWebAuthorization;
import uk.gov.hmcts.ccd.definition.store.rest.model.IdamProperties;
import uk.gov.hmcts.ccd.definition.store.rest.service.IdamProfileClient;

import static org.codehaus.groovy.runtime.InvokerHelper.asList;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AdminWebAuthorizationControllerTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private MockMvc mockMvc;

    @Mock
    private IdamProfileClient idamProfileClient;

    @Mock
    private AdminWebAuthorizationProperties adminWebAuthorizationProperties;

    @BeforeEach
    void setUp() {
        given(idamProfileClient.getLoggedInUserDetails()).willReturn(buildIdamProperties());
        final AdminWebAuthorizationController controller = new AdminWebAuthorizationController(idamProfileClient,
                                                                                               adminWebAuthorizationProperties);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                                 .setControllerAdvice(new RestEndPointExceptionHandler())
                                 .build();
    }

    @DisplayName("Should get no admin web authorization")
    @Test
    void getNoAdminWebAuthorization() throws Exception {
        final MvcResult mvcResult = mockMvc.perform(get("/api/idam/adminweb/authorization")).andExpect(status().isOk()).andReturn();
        assertThat(mvcResult.getResponse().getContentType(), is("application/json;charset=UTF-8"));
        final AdminWebAuthorization
            response =
            MAPPER.readValue(mvcResult.getResponse().getContentAsString(), AdminWebAuthorization.class);
        assertFalse(response.getCanManageUserProfile());
    }

    @DisplayName("Should get admin web authorization")
    @Test
    void getAdminWebAuthorization() throws Exception {

        given(adminWebAuthorizationProperties.getManageUserProfile()).willReturn(asList("fly"));

        final MvcResult mvcResult = mockMvc.perform(get("/api/idam/adminweb/authorization")).andExpect(status().isOk()).andReturn();
        assertThat(mvcResult.getResponse().getContentType(), is("application/json;charset=UTF-8"));
        final AdminWebAuthorization
            response =
            MAPPER.readValue(mvcResult.getResponse().getContentAsString(), AdminWebAuthorization.class);
        assertTrue(response.getCanManageUserProfile());
    }

    private IdamProperties buildIdamProperties() {
        IdamProperties p = new IdamProperties();
        p.setRoles(new String[] {"fly", "dream"});
        return p;
    }
}
