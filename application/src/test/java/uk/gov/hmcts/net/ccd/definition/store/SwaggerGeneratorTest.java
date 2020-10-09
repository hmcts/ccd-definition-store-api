package uk.gov.hmcts.net.ccd.definition.store;

import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.test.web.servlet.ResultActions;

import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class SwaggerGeneratorTest extends BaseTest {

    @DisplayName("Generate swagger documentation for all APIs")
    @Test
    public void generateDocs() throws Exception {
        ResultActions perform = mockMvc.perform(get("/v2/api-docs"));
        byte[] specs = perform
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsByteArray();
        try (OutputStream outputStream = Files.newOutputStream(Paths.get("/tmp/ccd-definition-store-api.json"))) {
            outputStream.write(specs);
        }
    }
}
