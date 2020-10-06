// TODO rewrite after RDM-1588 and RDM-1589
//package uk.gov.hmcts.net.ccd.definition.store.e2e;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.exparity.hamcrest.date.DateMatchers;
//import org.junit.Before;
//import org.junit.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.MediaType;
//import org.springframework.jdbc.core.JdbcTemplate;
//import org.springframework.test.annotation.DirtiesContext;
//import org.springframework.test.context.jdbc.Sql;
//import org.springframework.test.jdbc.JdbcTestUtils;
//import org.springframework.web.util.UriTemplate;
//import uk.gov.hmcts.ccd.definition.store.repository.SecurityClassification;
//import uk.gov.hmcts.ccd.definition.store.repository.model.UserRole;
//
//import javax.sql.DataSource;
//import java.time.LocalDate;
//import java.util.Base64;
//import java.util.Date;
//import java.util.HashMap;
//import java.util.Map;
//
//import static io.restassured.RestAssured.given;
//import static org.apache.http.HttpStatus.*;
//import static org.hamcrest.CoreMatchers.notNullValue;
//import static org.hamcrest.CoreMatchers.nullValue;
//import static org.hamcrest.core.Is.is;
//import static org.junit.Assert.assertThat;
//import static org.springframework.http.HttpHeaders.AUTHORIZATION;
//import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;
//import static uk.gov.hmcts.ccd.definition.store.repository.SecurityClassification.PUBLIC;
//import static uk.gov.hmcts.ccd.definition.store.repository.SecurityClassification.RESTRICTED;
//
//public class UserRoleControllerEndPointTest {
//
//    private static final String URL_API_USER_ROLE = "/api/user-role";
//    private static final UriTemplate USER_PROFILE_JURISDICTIONS_GET_URL = new UriTemplate(
//        "http://localhost:{port}" + URL_API_USER_ROLE + "?role={role}");
//    private static final String SERVICE_AUTHORIZATION = "ServiceAuthorization";
//    private static final String ROLE_DEFINED = "@<\"*#$%^\\/";
//    private static final ObjectMapper MAPPER = new ObjectMapper();
//
//    private JdbcTemplate template;
//    private Map<String, Object> uriVariables;
//
//    @Autowired
//    private DataSource db;
//
//    @Before
//    public void setup() {
//        template = new JdbcTemplate(db);
//        uriVariables = new HashMap<>();
//        uriVariables.put("port", Integer.toString(serverPort));
//    }
//
//    @Test
//    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = { "classpath:sql/user_role.sql" })
//    public void shouldGetRole_whenRoleExists() throws Exception {
//
//        uriVariables.put("role", Base64.getEncoder().encode(ROLE_DEFINED.getBytes()));
//
//        final String response = given()
//            .contentType(MediaType.APPLICATION_JSON_VALUE)
//            .header("ServiceAuthorization", serviceTokenGenerator.generate())
//            .header(AUTHORIZATION, generateUserToken())
//            .expect()
//            .statusCode(SC_OK)
//            .when()
//            .get(USER_PROFILE_JURISDICTIONS_GET_URL.expand(uriVariables).toString())
//            .asString();
//
//        final UserRole userRole = MAPPER.readValue(response, UserRole.class);
//        assertThat(userRole.getRole(), is(ROLE_DEFINED));
//        assertThat(userRole.getSecurityClassification(), is(PUBLIC));  // from user_role.sql
//    }
//
//    @Test
//    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = { "classpath:sql/user_role.sql" })
//    public void shouldHaveStatusCodeNotFound_whenRoleDoesNotExist() {
//
//        uriVariables.put("role", Base64.getEncoder().encode("does-not-exist".getBytes()));
//
//        given()
//            .contentType(MediaType.APPLICATION_JSON_VALUE)
//            .header("ServiceAuthorization", serviceTokenGenerator.generate())
//            .header(AUTHORIZATION, generateUserToken())
//            .expect()
//            .statusCode(SC_NOT_FOUND)
//            .when()
//            .get(USER_PROFILE_JURISDICTIONS_GET_URL.expand(uriVariables).toString());
//    }
//
//    @Test
//    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = { "classpath:sql/user_role.sql" })
//    public void shouldHaveStatusCodeBadRequest_whenRoleIsNotBase64Encoded() {
//
//        uriVariables.put("role", ROLE_DEFINED.getBytes());
//
//        given()
//            .contentType(MediaType.APPLICATION_JSON_VALUE)
//            .header("ServiceAuthorization", serviceTokenGenerator.generate())
//            .header(AUTHORIZATION, generateUserToken())
//            .expect()
//            .statusCode(SC_BAD_REQUEST)
//            .when()
//            .get(USER_PROFILE_JURISDICTIONS_GET_URL.expand(uriVariables).toString());
//    }
//
//    @Test
//    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = { "classpath:sql/user_role.sql" })
//    public void shouldCreate_whenRoleDoesNotExist() {
//
//        final String role = "ca/ro@some.where";
//        final UserRole userRole = createUserRole(role,
//            PUBLIC,
//            LocalDate.of(2017, 2, 25).toString(),
//            LocalDate.of(3017, 12, 25).toString());
//
//        assertThat(JdbcTestUtils.countRowsInTableWhere(template, "user_role", "role = '" + role + "'"), is(0));
//
//        given()
//            .contentType(MediaType.APPLICATION_JSON_VALUE)
//            .header(SERVICE_AUTHORIZATION, serviceTokenGenerator.generate())
//            .header(AUTHORIZATION, generateUserToken())
//            .body(userRole)
//            .expect()
//            .statusCode(SC_CREATED)
//            .when()
//            .put(URL_API_USER_ROLE);
//
//        final Map<String, Object> result = template.queryForMap("select * from user_role where role = ?", role);
//        assertThat(result.get("id"), is(notNullValue()));
//        assertThat(result.get("created_at"), is(notNullValue()));
//        assertThat(new Date(((Date)result.get("live_from")).getTime()),
//            DateMatchers.sameDay(LocalDate.parse("2017-02-25")));
//        assertThat(new Date(((Date)result.get("live_to")).getTime()),
//            DateMatchers.sameDay(LocalDate.parse("3017-12-25")));
//        assertThat(result.get("security_classification"), is("PUBLIC"));
//    }
//
//    @Test
//    @Sql(executionPhase = BEFORE_TEST_METHOD, scripts = { "classpath:sql/user_role.sql" })
//    public void shouldUpdate_whenRoleExists() {
//
//        final UserRole userRole = createUserRole(ROLE_DEFINED, RESTRICTED, null, null);
//
//        final Map<String, Object> before = template
//          .queryForMap("select * from user_role where role = ?", ROLE_DEFINED);
//        final Integer roleId = (Integer)before.get("id");
//        assertThat(roleId, is(notNullValue()));
//        assertThat(before.get("security_classification"), is("PUBLIC"));
//
//        given()
//            .contentType(MediaType.APPLICATION_JSON_VALUE)
//            .header(SERVICE_AUTHORIZATION, serviceTokenGenerator.generate())
//            .header(AUTHORIZATION, generateUserToken())
//            .body(userRole)
//            .expect()
//            .statusCode(SC_RESET_CONTENT)
//            .when()
//            .put(URL_API_USER_ROLE);
//
//        final Map<String, Object> result = template
//          .queryForMap("select * from user_role where role = ?", ROLE_DEFINED);
//        assertThat(result.get("id"), is(roleId));
//        assertThat(result.get("created_at"), is(notNullValue()));
//        assertThat(result.get("live_from"), is(nullValue()));
//        assertThat(result.get("live_from"), is(nullValue()));
//        assertThat(result.get("security_classification"), is("RESTRICTED"));
//    }
//
//    private UserRole createUserRole(final String role,
//                                    final SecurityClassification securityClassification,
//                                    final String liveFrom,
//                                    final String liveTo) {
//        final UserRole r = new UserRole();
//        r.setRole(role);
//        r.setSecurityClassification(securityClassification);
//        r.setLiveFrom(liveFrom);
//        r.setLiveTo(liveTo);
//        return r;
//    }
//}
