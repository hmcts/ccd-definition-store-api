package uk.gov.hmcts.net.ccd.definition.store.rest;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

import java.io.InputStream;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.apache.http.HttpStatus;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import uk.gov.hmcts.ccd.definition.store.repository.model.Jurisdiction;
import uk.gov.hmcts.net.ccd.definition.store.BaseTest;

public class CaseDefinitionControllerEndpointIT extends BaseTest {
    private static final String JURISDICTIONS_URL = "/api/data/jurisdictions";

    @Test
    public void shouldReturnJurisdictions() throws Exception {
        givenUserProfileReturnsSuccess();
        InputStream inputStream = new ClassPathResource(EXCEL_FILE_CCD_DEFINITION, getClass()).getInputStream();
        MockMultipartFile file = new MockMultipartFile("file", inputStream);
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.fileUpload(IMPORT_URL)
            .file(file)
            .header(AUTHORIZATION, "Bearer testUser"))
            .andReturn();
        assertResponseCode(mvcResult, HttpStatus.SC_CREATED);
        final String URL = JURISDICTIONS_URL + "?ids=TEST";
        final MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get(URL))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andReturn();

        List<Jurisdiction> jurisdictions = mapper.readValue(result.getResponse().getContentAsString(),
                TypeFactory.defaultInstance().constructType(new TypeReference<List<Jurisdiction>>(){}));

        assertAll(
            () -> assertThat(jurisdictions, hasSize(1)),
            () -> assertThat(jurisdictions, hasItem(allOf(hasProperty("id", is("TEST")),
                    hasProperty("name", is("Test")),
                    hasProperty("name", is("Test"))
            )))
        );
    }
}
