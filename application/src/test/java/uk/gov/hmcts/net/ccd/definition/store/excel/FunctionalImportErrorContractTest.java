package uk.gov.hmcts.net.ccd.definition.store.excel;

import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.net.ccd.definition.store.BaseTest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;

/** Reuses the functional fixtures and their real workbooks to guard the public import contract. */
@TestPropertySource(properties = {"ccd.authorised.services=ccd_data", "enable-case-group-access-filtering=true"})
// Import jobs use REQUIRES_NEW and survive the test transaction's rollback.
@Sql(statements = "DELETE FROM import_jobs", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD,
    config = @SqlConfig(transactionMode = SqlConfig.TransactionMode.ISOLATED))
class FunctionalImportErrorContractTest extends BaseTest {

    @BeforeEach
    void seedFunctionalAccessProfiles() {
        for (String role : List.of("citizen", "caseworker-approver", "caseworker-befta_master-junior",
            "caseworker-befta_master", "caseworker-caa", "caseworker-befta_master-solicitor",
            "caseworker-befta_master-manager", "caseworker-befta_jurisdiction_1",
            "ft_accessprofile_1", "ft_accessprofile_3", "GS_profile", "next-hearing-date-admin")) {
            jdbcTemplate.update("insert into role(reference, name, security_classification, dtype) "
                + "values (?, ?, 'PUBLIC', 'USERROLE')", role, role);
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("functionalErrors")
    @Transactional
    void retainsFunctionalValidationContract(String id, String resource, int status, String expectedBody)
        throws Exception {
        try (var input = new ClassPathResource(resource).getInputStream()) {
            MvcResult result = mockMvc.perform(multipart(IMPORT_URL)
                .file(new MockMultipartFile("file", input))
                .header(AUTHORIZATION, "Bearer testUser")).andReturn();
            String actual = result.getResponse().getContentAsString();
            assertEquals(status, result.getResponse().getStatus(), id + ": " + actual);
            JSONObject expected = new JSONObject(expectedBody);
            if (expected.has("__plainTextValue__")) {
                assertEquals(expected.getString("__plainTextValue__"),
                    actual.replace("\r", "").replace("\n", ""), id);
            } else {
                JSONObject body = new JSONObject(actual);
                assertFalse(body.getString("timestamp").isBlank());
                expected.put("timestamp", body.getString("timestamp"));
                JSONAssert.assertEquals(expected, body, JSONCompareMode.STRICT);
            }
        }
    }

    static Stream<Arguments> functionalErrors() throws IOException {
        Path root = Path.of("").toAbsolutePath();
        while (root != null && !Files.exists(root.resolve("settings.gradle"))) {
            root = root.getParent();
        }
        if (root == null) {
            throw new IllegalStateException("Repository root not found for functional fixtures");
        }
        List<Arguments> cases = new ArrayList<>();
        for (String folder : List.of("F-092 Conditional Event Post State", "F-093",
            "F-095 Publish Flag in Case Event", "F-097 RoleToAccessProfiles",
            "F-098 rename of UserRole to AccessProfile", "F-099 SearchParty (GlobalSearch)", "F-104 Categories",
            "F-105 TTL Increment in Case Event", "F-106 New CategoryID Column", "F-109 AccessTypeRoles",
            "F-111 AccessTypes", "F-127 ShellMapping")) {
            try (var files = Files.list(root.resolve("aat/src/aat/resources/features").resolve(folder))) {
                for (Path fixture : files.filter(path -> path.getFileName().toString().startsWith("S-"))
                    .filter(path -> path.toString().endsWith(".json")).sorted().toList()) {
                    JSONObject data = new JSONObject(Files.readString(fixture));
                    JSONObject response = data.optJSONObject("expectedResponse");
                    if (response == null) {
                        continue; // Successful fixtures can inherit their response from a shared base.
                    }
                    int status = response.optInt("responseCode");
                    if (status == 400 || status == 422) {
                        String resource = data.getJSONObject("request").getJSONObject("body")
                            .getJSONArray("arrayInMap").getJSONObject(0).getString("filePath");
                        cases.add(Arguments.of(data.getString("_guid_"), resource, status,
                            response.getJSONObject("body").toString()));
                    }
                }
            }
        }
        return cases.stream();
    }
}
