package uk.gov.hmcts.net.ccd.definition.store;

import uk.gov.hmcts.ccd.definition.store.CaseDataAPIApplication;
import uk.gov.hmcts.ccd.definition.store.JacksonUtils;
import uk.gov.hmcts.ccd.definition.store.domain.ApplicationParams;
import uk.gov.hmcts.ccd.definition.store.domain.service.workbasket.WorkBasketUserDefaultService;
import uk.gov.hmcts.ccd.definition.store.excel.azurestorage.AzureStorageConfiguration;
import uk.gov.hmcts.ccd.definition.store.excel.azurestorage.service.AzureBlobStorageClient;
import uk.gov.hmcts.ccd.definition.store.excel.azurestorage.service.FileStorageService;
import uk.gov.hmcts.ccd.definition.store.repository.SecurityUtils;
import uk.gov.hmcts.net.ccd.definition.store.domain.model.DisplayItemsData;
import uk.gov.hmcts.net.ccd.definition.store.excel.UserRoleSetup;
import uk.gov.hmcts.net.ccd.definition.store.wiremock.config.WireMockTestConfiguration;

import javax.sql.DataSource;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
    CaseDataAPIApplication.class,
    TestConfiguration.class,
    TestIdamConfiguration.class,
    WireMockTestConfiguration.class
})
@TestPropertySource(locations = "classpath:test.properties")
@AutoConfigureWireMock(port = 0)
public abstract class BaseTest {

    public static final String EXCEL_FILE_CCD_DEFINITION = "/CCD_TestDefinition_V46_RDM-6719.xlsx";

    public static final String IMPORT_URL = "/import";
    @Inject
    protected DataSource db;

    @Inject
    protected WebApplicationContext wac;

    @Inject
    private ApplicationParams applicationParams;

    @Inject
    private WorkBasketUserDefaultService workBasketUserDefaultService;

    @Value("${wiremock.server.port}")
    protected Integer wiremockPort;

    @MockitoBean
    protected FileStorageService fileStorageService;

    // Mock the AzureBlobStorageClient component, to prevent it being initialised (which requires connection to Azure
    // Storage) during application startup when testing
    @MockitoBean
    private AzureBlobStorageClient storageClient;

    // Mock the AzureStorageConfiguration component, to prevent it being initialised (which requires connection to Azure
    // Storage) during application startup when testing
    @MockitoBean
    private AzureStorageConfiguration azureStorageConfiguration;

    protected MockMvc mockMvc;
    protected JdbcTemplate jdbcTemplate;
    protected Map<String, Integer> userRoleIds;

    protected static final ObjectMapper mapper = new ObjectMapper();

    @BeforeAll
    public static void init() {
        mapper.setSerializationInclusion(JsonInclude.Include.NON_DEFAULT);
    }

    @Inject
    protected SecurityUtils securityUtils;

    @Mock
    protected Authentication authentication;
    @Mock
    protected SecurityContext securityContext;

    @BeforeEach
    void setUp() {
        // reset wiremock counters
        WireMock.resetAllRequests();

        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
        jdbcTemplate = new JdbcTemplate(db);
        ReflectionTestUtils.setField(applicationParams, "userProfileHost", "http://localhost:" + wiremockPort);
        ReflectionTestUtils.setField(workBasketUserDefaultService, "securityUtils", securityUtils);

        doReturn(authentication).when(securityContext).getAuthentication();
        SecurityContextHolder.setContext(securityContext);
        setSecurityAuthorities(authentication);

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
                JacksonUtils.getHashMapTypeReference()
            ));
        } catch (IOException e) {
            fail("Incorrect JSON structure: " + resultSet.getString("display_object"));
        }
        displayItemsData.setDisplayItemVersion(resultSet.getInt("display_item_version"));
        displayItemsData.setType(resultSet.getString("type"));

        return displayItemsData;
    }

    protected void assertResponseCode(MvcResult mvcResult, int httpResponseCode) throws UnsupportedEncodingException {
        MockHttpServletResponse response = mvcResult.getResponse();
        assertEquals(
            httpResponseCode,
            response.getStatus(),
            "Expected [" + httpResponseCode + "] but was [" + response.getStatus() + "]"
                + " Body was [\n" + response.getContentAsString() + "\n]"
        );
    }

    protected void setSecurityAuthorities(Authentication authenticationMock, String... authorities) {

        Jwt jwt = Jwt.withTokenValue("Bearer a jwt token")
            .claim("aClaim", "aClaim")
            .header("aHeader", "aHeader")
            .build();
        when(authenticationMock.getPrincipal()).thenReturn(jwt);

        Collection<? extends GrantedAuthority> authorityCollection = Stream.of(authorities)
            .map(a -> new SimpleGrantedAuthority(a))
            .collect(Collectors.toCollection(ArrayList::new));

        when(authenticationMock.getAuthorities()).thenAnswer(invocationOnMock -> authorityCollection);

    }
}
