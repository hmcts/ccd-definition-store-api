package uk.gov.hmcts.ccd.definition.store.rest.endpoint;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.util.UriTemplate;
import uk.gov.hmcts.ccd.definition.store.domain.exception.DuplicateUserRoleException;
import uk.gov.hmcts.ccd.definition.store.domain.exception.NotFoundException;
import uk.gov.hmcts.ccd.definition.store.domain.service.UserRoleService;
import uk.gov.hmcts.ccd.definition.store.domain.service.response.ServiceResponse;
import uk.gov.hmcts.ccd.definition.store.repository.SecurityClassification;
import uk.gov.hmcts.ccd.definition.store.repository.model.UserRole;
import uk.gov.hmcts.ccd.definition.store.rest.endpoint.exceptions.RestEndPointExceptionHandler;

import javax.persistence.OptimisticLockException;
import javax.persistence.PersistenceException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.http.HttpStatus.SC_OK;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.ccd.definition.store.domain.service.response.SaveOperationEnum.CREATE;
import static uk.gov.hmcts.ccd.definition.store.domain.service.response.SaveOperationEnum.UPDATE;

class UserRoleControllerTest {

    private static final String URL_API_USER_ROLE = "/api/user-role";
    private static final UriTemplate URL_TEMPLATE = new UriTemplate(URL_API_USER_ROLE + "?role={role}");
    private static final String URL_API_USER_ROLES = "/api/user-roles/role1,role2";
    private static final String URL_API_ALL_USER_ROLES = "/api/user-roles";
    private static final String ROLE_DEFINED = "@<\"*#$%^\\/";
    private static final String ROLE1 = "role1";
    private static final String ROLE2 = "role2";
    private static final MediaType CONTENT_TYPE = new MediaType(
        MediaType.APPLICATION_JSON.getType(),
        MediaType.APPLICATION_JSON.getSubtype(),
        Charset.forName("utf8"));
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private MockMvc mockMvc;
    private Map<String, Object> uriVariables;

    @InjectMocks
    private UserRoleController controller;

    @Mock
    private UserRoleService userRoleService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
            .setControllerAdvice(new RestEndPointExceptionHandler())
            .build();
        uriVariables = new HashMap<>();
    }

    @Nested
    @DisplayName("Get Role Tests")
    class GetRoleTests {
        @Test
        @DisplayName("Should successfully return role")
        void successWhenRoleExists() throws Exception {

            uriVariables.put("role", Base64.getEncoder().encode(ROLE_DEFINED.getBytes()));
            final UserRole mockUserRole = buildUserRole(ROLE_DEFINED, -6);

            when(userRoleService.getRole(ROLE_DEFINED)).thenReturn(mockUserRole);

            final MvcResult mvcResult = mockMvc.perform(
                get(URL_TEMPLATE.expand(uriVariables)))
                .andExpect(status().isOk())
                .andReturn();

            final UserRole userRole = MAPPER.readValue(mvcResult.getResponse().getContentAsString(), UserRole.class);
            assertAll(
                () -> assertThat(userRole.getId(), is(-6)),
                () -> assertThat(userRole.getRole(), is(ROLE_DEFINED)),
                () -> assertThat(userRole.getSecurityClassification(), is(mockUserRole.getSecurityClassification()))
            );
        }

        @Test
        @DisplayName("Should have not found status when role does not exist")
        void shouldHaveStatusNotFound_whenRoleDoesNotExist() throws Throwable {
            uriVariables.put("role", Base64.getEncoder().encode(ROLE_DEFINED.getBytes()));

            when(userRoleService.getRole(ROLE_DEFINED))
                .thenThrow(new NotFoundException("Role '" + ROLE_DEFINED + "' is not found"));

            mockMvc.perform(
                get(URL_TEMPLATE.expand(uriVariables)))
                .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("Get Roles Tests")
    class GetRolesTests {
        @Test
        void shouldGetRoles_whenRolesExists() throws Exception {
            final UserRole mockUserRole = buildUserRole(ROLE1, 1);
            final UserRole mockUserRole2 = buildUserRole(ROLE2, 2);
            List<UserRole> roles = Arrays.asList(mockUserRole, mockUserRole2);
            List<String> roleNames = Arrays.asList("role1", "role2");
            when(userRoleService.getRoles(roleNames)).thenReturn(roles);

            final MvcResult mvcResult = mockMvc.perform(
                get(URL_API_USER_ROLES))
                .andExpect(status().isOk())
                .andReturn();

            final List<UserRole> userRoles = MAPPER.readValue(mvcResult.getResponse().getContentAsString(),
                TypeFactory.defaultInstance().constructType(new TypeReference<List<UserRole>>() {
                }));

            assertAll(
                () -> assertThat(mvcResult.getResponse().getStatus(), is(SC_OK)),
                () -> assertThat(userRoles.size(), is(2)),
                () -> assertThat(userRoles.get(0).getId(), is(1)),
                () -> assertThat(userRoles.get(0).getRole(), is(ROLE1)),
                () -> assertThat(
                    userRoles.get(0).getSecurityClassification(), is(mockUserRole.getSecurityClassification())),
                () -> assertThat(userRoles.get(1).getId(), is(2)),
                () -> assertThat(userRoles.get(1).getRole(), is(ROLE2)),
                () -> assertThat(
                    userRoles.get(1).getSecurityClassification(), is(mockUserRole2.getSecurityClassification()))
            );
        }

        @Test
        void shouldGetEmptyRoles_whenNoRolesExists() throws Exception {
            List<String> roleNames = Arrays.asList("role1", "role2");
            when(userRoleService.getRoles(roleNames)).thenReturn(Collections.EMPTY_LIST);

            final MvcResult mvcResult = mockMvc.perform(
                get(URL_API_USER_ROLES))
                .andExpect(status().isOk())
                .andReturn();

            final List<UserRole> userRoles = MAPPER.readValue(mvcResult.getResponse().getContentAsString(),
                TypeFactory.defaultInstance().constructType(new TypeReference<List<UserRole>>() {
                }));

            assertAll(
                () -> assertThat(mvcResult.getResponse().getStatus(), is(SC_OK)),
                () -> assertThat(userRoles.size(), is(0))
            );
        }

        @Test
        void shouldGetAllRoles() throws Exception {
            final UserRole mockUserRole = buildUserRole(ROLE1, 1);
            final UserRole mockUserRole2 = buildUserRole(ROLE2, 2);
            List<UserRole> roles = Arrays.asList(mockUserRole, mockUserRole2);
            when(userRoleService.getRoles()).thenReturn(roles);

            final MvcResult mvcResult = mockMvc.perform(
                get(URL_API_ALL_USER_ROLES))
                .andExpect(status().isOk())
                .andReturn();

            final List<UserRole> userRoles = MAPPER.readValue(mvcResult.getResponse().getContentAsString(),
                TypeFactory.defaultInstance().constructType(new TypeReference<List<UserRole>>() {
                }));

            assertAll(
                () -> assertThat(mvcResult.getResponse().getStatus(), is(SC_OK)),
                () -> assertThat(userRoles.size(), is(2)),
                () -> assertThat(userRoles.get(0).getId(), is(1)),
                () -> assertThat(userRoles.get(0).getRole(), is(ROLE1)),
                () -> assertThat(
                    userRoles.get(0).getSecurityClassification(), is(mockUserRole.getSecurityClassification())),
                () -> assertThat(userRoles.get(1).getId(), is(2)),
                () -> assertThat(userRoles.get(1).getRole(), is(ROLE2)),
                () -> assertThat(
                    userRoles.get(1).getSecurityClassification(), is(mockUserRole2.getSecurityClassification()))
            );
        }
    }

    @Nested
    @DisplayName("Put Role Tests")
    class PutRoleTests {

        @Test
        @DisplayName("Should have conflict status when role is updated but object optimistic lock exception is thrown")
        void shouldHaveStatusConflict_whenUserRoleIsUpdatedThrowObjectOptimisticLockingFailureException()
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
        @DisplayName("Should have conflict status when role is updated but optimistic lock exception is thrown")
        void shouldHaveStatusConflict_whenUserRoleIsUpdatedThrowOptimisticLockException() throws Throwable {
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
        @DisplayName("Should have server error status when role is updated but persistence exception is thrown")
        void shouldHaveStatus500_whenUserRoleIsUpdatedWithPersistenceException() throws Throwable {
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
        @DisplayName("Should have reset content status when role is updated successfully")
        void shouldHaveStatusResetContent_whenPutSuccessfully() throws Exception {
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
    }


    @Nested
    @DisplayName("Add a Role Tests")
    class AddRoleTests {

        @Test
        @DisplayName("Should have bad request status when role is passed but duplicate user role exception is thrown")
        void shouldHaveStatusConflict_whenUserRoleIsUpdatedThrowObjectOptimisticLockingFailureException()
            throws Throwable {

            final UserRole argument = buildUserRole(ROLE_DEFINED);
            when(userRoleService.createRole(isA(UserRole.class)))
                .thenThrow(new DuplicateUserRoleException("User role already exists"));

            mockMvc.perform(
                post(URL_API_USER_ROLE)
                    .contentType(CONTENT_TYPE)
                    .content(MAPPER.writeValueAsBytes(argument)))
                .andExpect(status().isBadRequest())
            ;
        }

        @Test
        @DisplayName("Should have reset content status when role is created successfully")
        void shouldHaveStatusResetContent_whenPutSuccessfully() throws Exception {
            final UserRole argument = buildUserRole(ROLE_DEFINED);
            final UserRole mockUserRole = buildUserRole(ROLE_DEFINED, -7);
            when(userRoleService.createRole(isA(UserRole.class)))
                .thenReturn(new ServiceResponse<>(mockUserRole, CREATE));

            mockMvc.perform(
                post(URL_API_USER_ROLE)
                    .contentType(CONTENT_TYPE)
                    .content(MAPPER.writeValueAsBytes(argument)))
                .andExpect(status().isCreated())
            ;
        }
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
