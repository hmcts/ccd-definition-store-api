package uk.gov.hmcts.ccd.definition.store.rest.endpoint;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

class FrameworkErrorFixtureContractTest {
    @Test
    void frameworkValidationMatchesFunctionalFixtureWithoutExtraFields() throws Exception {
        var mvc = standaloneSetup(new Probe()).setControllerAdvice(new ControllerExceptionHandler()).build();
        var result = mvc.perform(post("/validation-probe").contentType("application/json").content("{}"))
            .andExpect(status().isBadRequest()).andReturn();
        var root = Path.of("").toAbsolutePath();
        while (!Files.exists(root.resolve("settings.gradle"))) {
            root = root.getParent();
        }
        var fixture = root.resolve("aat/src/aat/resources/features/F-138 User Role API/S-138.8.td.json");
        var mapper = new ObjectMapper();
        var expected = mapper.readTree(fixture.toFile()).get("expectedResponse");
        assertThat(mapper.readTree(result.getResponse().getContentAsString())).isEqualTo(expected.get("body"));
        assertThat(result.getResponse().getContentType()).startsWith("application/problem+json");
        assertThat(result.getResponse().getContentAsString()).doesNotContain("name", "validation-probe", "java.lang");
    }

    @RestController
    static class Probe {
        @PostMapping("/validation-probe")
        public void validate(@Valid @RequestBody Input input) {
        }
    }

    record Input(@NotBlank String name) {
    }
}
