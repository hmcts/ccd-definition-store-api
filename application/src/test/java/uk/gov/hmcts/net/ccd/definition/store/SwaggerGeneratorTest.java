package uk.gov.hmcts.net.ccd.definition.store;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Test;
import org.springframework.test.web.servlet.ResultActions;

import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

public class SwaggerGeneratorTest extends BaseTest {

    @Test
    @SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
    public void generateDocs() throws Exception {
            ResultActions perform = mockMvc.perform(get("/v2/api-docs"));
            byte[] specs = perform.andExpect(status().isOk()).andReturn().getResponse().getContentAsByteArray();

            try (OutputStream outputStream = Files.newOutputStream(Paths.get("/tmp/swagger-specs.json"))) {
                outputStream.write(specs);
        }
    }
}
