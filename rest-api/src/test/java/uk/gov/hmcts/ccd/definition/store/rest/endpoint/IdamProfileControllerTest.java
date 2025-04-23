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
import uk.gov.hmcts.ccd.definition.store.rest.model.IdamProperties;
import uk.gov.hmcts.ccd.definition.store.rest.service.IdamProfileClient;

import java.util.List;

import static java.util.Arrays.asList;
import static org.apache.commons.lang3.RandomStringUtils.secure;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.MatcherAssert.assertThat;
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
    public void setUp() {
        idamProperties = buildIdamProperties();
        given(idamProfileClient.getLoggedInUserDetails()).willReturn(idamProperties);
        final IdamProfileController controller = new IdamProfileController(idamProfileClient);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
            .setControllerAdvice(new ControllerExceptionHandler())
            .build();
    }

    @DisplayName("Should get idam profile")
    @Test
    public void shouldGetIdamProfile() throws Exception {
        final MvcResult mvcResult = mockMvc.perform(get("/api/idam/profile")).andExpect(status().isOk()).andReturn();
        assertThat(mvcResult.getResponse().getContentType(), is("application/json"));
        final IdamProperties
            response =
            MAPPER.readValue(mvcResult.getResponse().getContentAsString(), IdamProperties.class);
        assertThat(MAPPER.writeValueAsString(response), is(MAPPER.writeValueAsString(idamProperties)));
    }

    private IdamProperties buildIdamProperties() {
        final IdamProperties properties = new IdamProperties();
        properties.setId(secure().nextAlphanumeric(20));
        properties.setEmail(secure().nextAlphanumeric(19) + "@example.com");
        properties.setForename(secure().nextAlphabetic(15));
        properties.setSurname(secure().nextAlphabetic(27));
        final List<String> roles = asList(secure().nextAlphanumeric(10), secure().nextAlphanumeric(12));
        properties.setRoles(roles.toArray(new String[roles.size()]));
        return properties;
    }

}
