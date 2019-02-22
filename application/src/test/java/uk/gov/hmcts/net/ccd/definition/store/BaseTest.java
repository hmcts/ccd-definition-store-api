package uk.gov.hmcts.net.ccd.definition.store;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.ccd.definition.store.CaseDataAPIApplication;
import uk.gov.hmcts.ccd.definition.store.domain.ApplicationParams;
import uk.gov.hmcts.ccd.definition.store.domain.service.workbasket.WorkBasketUserDefaultService;
import uk.gov.hmcts.ccd.definition.store.excel.azurestorage.AzureStorageConfiguration;
import uk.gov.hmcts.ccd.definition.store.excel.azurestorage.service.AzureBlobStorageClient;
import uk.gov.hmcts.ccd.definition.store.excel.azurestorage.service.FileStorageService;
import uk.gov.hmcts.ccd.definition.store.excel.service.ImportServiceImpl;
import uk.gov.hmcts.ccd.definition.store.repository.SecurityUtils;
import uk.gov.hmcts.ccd.definition.store.rest.model.IdamProperties;
import uk.gov.hmcts.net.ccd.definition.store.domain.model.DisplayItemsData;
import uk.gov.hmcts.net.ccd.definition.store.excel.UserRoleSetup;

import javax.inject.Inject;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {
    CaseDataAPIApplication.class,
    TestConfiguration.class
})
@TestPropertySource(locations = "classpath:test.properties")
public abstract class BaseTest {
    public static final String EXCEL_FILE_CCD_DEFINITION = "/CCD_TestDefinition_V39_RDM-3317.xlsx";
    public static final String IMPORT_URL = "/import";
    @Inject
    protected DataSource db;

    @Inject
    protected WebApplicationContext wac;

    @Inject
    private ApplicationParams applicationParams;

    @Inject
    private WorkBasketUserDefaultService workBasketUserDefaultService;

    @Autowired
    private ImportServiceImpl importService;

    @MockBean
    protected FileStorageService fileStorageService;

    // Mock the AzureBlobStorageClient component, to prevent it being initialised (which requires connection to Azure
    // Storage) during application startup when testing
    @MockBean
    private AzureBlobStorageClient storageClient;

    // Mock the AzureStorageConfiguration component, to prevent it being initialised (which requires connection to Azure
    // Storage) during application startup when testing
    @MockBean
    private AzureStorageConfiguration azureStorageConfiguration;

    protected MockMvc mockMvc;
    protected JdbcTemplate jdbcTemplate;
    protected Map<String, Integer> userRoleIds;

    protected static final ObjectMapper mapper = new ObjectMapper();

    @Rule  // The @Rule 'wireMockRule' must be public
    public WireMockRule wireMockRule = new WireMockRule(WireMockConfiguration.wireMockConfig().dynamicPort());

    @BeforeClass
    public static void init() {
        mapper.setSerializationInclusion(JsonInclude.Include.NON_DEFAULT);
    }

    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
        jdbcTemplate = new JdbcTemplate(db);
        final Integer port = wireMockRule.port();
        ReflectionTestUtils.setField(applicationParams, "userProfileHost", "http://localhost:" + port);
        final SecurityUtils securityUtils = mock(SecurityUtils.class);
        when(securityUtils.authorizationHeaders()).thenReturn(new HttpHeaders());
        ReflectionTestUtils.setField(workBasketUserDefaultService, "securityUtils", securityUtils);

        final IdamProperties idamProperties = new IdamProperties();
        idamProperties.setId("445");
        idamProperties.setEmail("user@hmcts.net");

        // Override getUserDetails to avoid calling IdAM with invalid authorization
        doReturn(idamProperties).when(importService).getUserDetails();

        userRoleIds = new UserRoleSetup(jdbcTemplate).addUserRoleTestData();

        // Enable Definition file upload to Azure (mocked)
        when(azureStorageConfiguration.isAzureUploadEnabled()).thenReturn(true);
    }

    protected DisplayItemsData mapDisplayItemsData(ResultSet resultSet, Integer i) throws SQLException {
        final DisplayItemsData displayItemsData = new DisplayItemsData();
        displayItemsData.setCaseTypeId(resultSet.getString("case_type_id"));
        displayItemsData.setVersion(resultSet.getDate("version"));
        displayItemsData.setReference(resultSet.getInt("reference"));
        try {
            displayItemsData.setDisplayObject(mapper.convertValue(
                mapper.readTree(resultSet.getString("display_object")),
                new TypeReference<Map<String, JsonNode>>() {
                }
            ));
        } catch (IOException e) {
            Assert.fail("Incorrect JSON structure: " + resultSet.getString("display_object"));
        }
        displayItemsData.setDisplayItemVersion(resultSet.getInt("display_item_version"));
        displayItemsData.setType(resultSet.getString("type"));

        return displayItemsData;
    }

    protected void givenUserProfileReturnsSuccess() {
        WireMock.givenThat(WireMock.put(urlEqualTo("/user-profile/users"))
            .willReturn(WireMock.aResponse()
                .withStatus(201)
                .withHeader("Content-Type", "text/plain")
                .withBody("Hello world!")));
    }

    protected void assertResponseCode(MvcResult mvcResult, int httpResponseCode) throws UnsupportedEncodingException {
        MockHttpServletResponse response = mvcResult.getResponse();
        assertEquals("Expected [" + httpResponseCode + "] but was [" + response.getStatus() + "]" +
                " Body was [\n" + response.getContentAsString() + "\n]",
            httpResponseCode,
            mvcResult.getResponse().getStatus()
        );
    }
}
