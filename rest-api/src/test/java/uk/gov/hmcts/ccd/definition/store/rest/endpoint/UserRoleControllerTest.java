package uk.gov.hmcts.ccd.definition.store.rest.endpoint;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.util.UriTemplate;
import uk.gov.hmcts.ccd.definition.store.domain.exception.NotFoundException;
import uk.gov.hmcts.ccd.definition.store.domain.service.UserRoleService;
import uk.gov.hmcts.ccd.definition.store.domain.service.response.ServiceResponse;
import uk.gov.hmcts.ccd.definition.store.repository.SecurityClassification;
import uk.gov.hmcts.ccd.definition.store.repository.model.UserRole;
import uk.gov.hmcts.ccd.definition.store.rest.endpoint.exceptions.RestEndPointExceptionHandler;

import javax.persistence.OptimisticLockException;
import javax.persistence.PersistenceException;
import java.nio.charset.Charset;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.ccd.definition.store.domain.service.response.SaveOperationEnum.UPDATE;

public class UserRoleControllerTest {

    private static final String URL_API_USER_ROLE = "/api/user-role";

    private static final UriTemplate URL_TEMPLAE = new UriTemplate(URL_API_USER_ROLE + "?role={role}");

    private static final String ROLE_DEFINED = "@<\"*#$%^\\/";

    private static final MediaType CONTENT_TYPE = new MediaType(MediaType.APPLICATION_JSON.getType(),
        MediaType.APPLICATION_JSON.getSubtype(), Charset.forName("utf8"));

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private MockMvc mockMvc;

    private Map<String, Object> uriVariables;

    @InjectMocks
    private UserRoleController controller;

    @Mock
    private UserRoleService userRoleService;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
            .setControllerAdvice(new RestEndPointExceptionHandler())
            .build();
        uriVariables = new HashMap<>();
    }

    @Test
    public void shouldGetRole_whenRoleExists() throws Exception {

        uriVariables.put("role", Base64.getEncoder().encode(ROLE_DEFINED.getBytes()));
        final UserRole mockUserRole = buildUserRole(ROLE_DEFINED, -6);

        when(userRoleService.getRole(ROLE_DEFINED)).thenReturn(mockUserRole);

        final MvcResult mvcResult = mockMvc.perform(
            get(URL_TEMPLAE.expand(uriVariables)))
            .andExpect(status().isOk())
            .andReturn();

        final UserRole userRole = MAPPER.readValue(mvcResult.getResponse().getContentAsString(), UserRole.class);
        assertThat(userRole.getId(), is(-6));
        assertThat(userRole.getRole(), is(ROLE_DEFINED));
        assertThat(userRole.getSecurityClassification(), is(mockUserRole.getSecurityClassification()));
    }

    @Test
    public void shouldHaveStatusNotFound_whenRoleDoesNotExist() throws Throwable {

        uriVariables.put("role", Base64.getEncoder().encode(ROLE_DEFINED.getBytes()));
        final UserRole mockUserRole = buildUserRole(ROLE_DEFINED);

        when(userRoleService.getRole(ROLE_DEFINED))
            .thenThrow(new NotFoundException("Role '" + ROLE_DEFINED + "' is not found"));

        mockMvc.perform(
            get(URL_TEMPLAE.expand(uriVariables)))
            .andExpect(status().isNotFound());
    }

    @Test
    public void shouldHaveStatusConflict_whenUserRoleIsUpdatedThrowOptimsticLockException() throws Throwable {

        final UserRole argument = buildUserRole(ROLE_DEFINED);
        when(userRoleService.saveRole(isA(UserRole.class)))
            .thenThrow(new OptimisticLockException("Object updated already"));

        mockMvc.perform(
            put(URL_API_USER_ROLE)
                .contentType(CONTENT_TYPE)
                .content(MAPPER.writeValueAsBytes(argument)))
            .andExpect(status().isConflict())
        ;
    }

    @Test
    public void shouldHaveStatusConflict_whenUserRoleIsUpdatedThrowObjectOptimisticLockingFailureException()
        throws Throwable {

        final UserRole argument = buildUserRole(ROLE_DEFINED);
        when(userRoleService.saveRole(isA(UserRole.class)))
            .thenThrow(new ObjectOptimisticLockingFailureException("Object updated already", 1));

        mockMvc.perform(
            put(URL_API_USER_ROLE)
                .contentType(CONTENT_TYPE)
                .content(MAPPER.writeValueAsBytes(argument)))
            .andExpect(status().isConflict())
        ;
    }

    @Test
    public void shouldHaveStatus500_whenUserRoleIsUpdatedWithPersistenceException() throws Throwable {

        final UserRole argument = buildUserRole(ROLE_DEFINED);
        when(userRoleService.saveRole(isA(UserRole.class)))
            .thenThrow(new PersistenceException("something funny in the database"));

        mockMvc.perform(
            put(URL_API_USER_ROLE)
                .contentType(CONTENT_TYPE)
                .content(MAPPER.writeValueAsBytes(argument)))
            .andExpect(status().isInternalServerError())
        ;
    }

    @Test
    public void shouldHaveStatusResetContent_whenPutSuccessfully() throws Exception {

        final UserRole argument = buildUserRole(ROLE_DEFINED);
        final UserRole mockUserRole = buildUserRole(ROLE_DEFINED, -7);
        when(userRoleService.saveRole(isA(UserRole.class))).thenReturn(new ServiceResponse<>(mockUserRole, UPDATE));

        mockMvc.perform(
            put(URL_API_USER_ROLE)
                .contentType(CONTENT_TYPE)
                .content(MAPPER.writeValueAsBytes(argument)))
            .andExpect(status().isResetContent())
        ;
    }

    private UserRole buildUserRole(final String role) {
        return buildUserRole(role, null);
    }

    private UserRole buildUserRole(final String role, Integer id) {
        final UserRole r = new UserRole();
        r.setId(id);
        r.setSecurityClassification(SecurityClassification.PUBLIC);
        r.setRole(role);
        return r;
    }

}
