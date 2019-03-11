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
import uk.gov.hmcts.ccd.definition.store.rest.endpoint.exceptions.RestEndPointExceptionHandler;
import uk.gov.hmcts.ccd.definition.store.rest.model.IdamProperties;
import uk.gov.hmcts.ccd.definition.store.rest.service.IdamProfileClient;

import java.util.List;

import static java.util.Arrays.asList;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@ExtendWith(MockitoExtension.class)
class IdamProfileControllerTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private MockMvc mockMvc;

    @Mock
    private IdamProfileClient idamProfileClient;

    private IdamProperties idamProperties;

    @BeforeEach
    void setUp() {
        idamProperties = buildIdamProperties();
        given(idamProfileClient.getLoggedInUserDetails()).willReturn(idamProperties);
        final IdamProfileController controller = new IdamProfileController(idamProfileClient);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                                 .setControllerAdvice(new RestEndPointExceptionHandler())
                                 .build();
    }

    @DisplayName("Should get idam profile")
    @Test
    public void shouldGetIdamProfile() throws Exception {
        final MvcResult mvcResult = mockMvc.perform(get("/api/idam/profile")).andExpect(status().isOk()).andReturn();
        assertThat(mvcResult.getResponse().getContentType(), is("application/json;charset=UTF-8"));
        final IdamProperties
            response =
            MAPPER.readValue(mvcResult.getResponse().getContentAsString(), IdamProperties.class);
        assertThat(MAPPER.writeValueAsString(response), is(MAPPER.writeValueAsString(idamProperties)));
    }

    private IdamProperties buildIdamProperties() {
        final IdamProperties properties = new IdamProperties();
        properties.setId(randomAlphanumeric(20));
        properties.setEmail(randomAlphabetic(19) + "@example.com");
        properties.setForename(randomAlphabetic(15));
        properties.setSurname(randomAlphabetic(27));
        final List<String> roles = asList(randomAlphanumeric(10), randomAlphanumeric(12));
        properties.setRoles(roles.toArray(new String[roles.size()]));
        return properties;
    }

}
