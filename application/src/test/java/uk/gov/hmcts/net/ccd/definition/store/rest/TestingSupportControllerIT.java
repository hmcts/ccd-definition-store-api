package uk.gov.hmcts.net.ccd.definition.store.rest;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import uk.gov.hmcts.net.ccd.definition.store.BaseTest;

import java.io.InputStream;
import java.math.BigInteger;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

public class TestingSupportControllerIT extends BaseTest {

    private static final String CLEANUP_CASE_TYPE_URL = "/api/testing-support/cleanup-case-type/%s?caseTypeIds=%s";

    private static final String CASE_TYPE_URL = "/api/data/case-type/%s";
    private static final Logger LOG = LoggerFactory.getLogger(TestingSupportControllerIT.class);

    @Test
    public void shouldReturnCaseType() throws Exception {
        try (final InputStream inputStream =
                 new ClassPathResource("/CCD_TestDefinition_TestingSupportData.xlsx", getClass()).getInputStream()) {
            MockMultipartFile file = new MockMultipartFile("file", inputStream);
            MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.multipart(IMPORT_URL)
                    .file(file)
                    .header(AUTHORIZATION, "Bearer testUser"))
                .andReturn();
            assertResponseCode(mvcResult, HttpStatus.SC_CREATED);

            var deleteResult = mockMvc.perform(MockMvcRequestBuilders.delete(
                String.format(CLEANUP_CASE_TYPE_URL, new BigInteger("123"), "TestAddressBookCase")))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
            assertResponseCode(deleteResult, HttpStatus.SC_OK);

            mockMvc.perform(MockMvcRequestBuilders.get(String.format(CASE_TYPE_URL, "TestAddressBookCase-123")))
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andReturn();

            mockMvc.perform(MockMvcRequestBuilders.get(String.format(CASE_TYPE_URL, "TestComplexAddressBookCase")))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(jsonPath("$.id").value("TestComplexAddressBookCase"))
                .andReturn();
        }
    }
}
