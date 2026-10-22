package uk.gov.hmcts.ccd.definition.store;

import jakarta.servlet.RequestDispatcher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.http.HttpStatus;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.boot.autoconfigure.web.servlet.error.BasicErrorController;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ErrorPageConfigurationTest {

    @ParameterizedTest
    @CsvSource({"400,/import,/import", "404,/private/case-123,/", "500,/private/case-123,/"})
    void fallbackOmitsDiagnosticsEvenWhenRequested(int status, String uri, String safePath) throws Exception {

        ErrorProperties properties = new Binder(new MapConfigurationPropertySource(
            PropertiesLoaderUtils.loadProperties(new ClassPathResource("application.properties"))))
            .bind("server.error", Bindable.of(ErrorProperties.class)).get();
        final BasicErrorController controller = new BasicErrorController(
            new ErrorPageConfiguration().safeErrorAttributes(), properties);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute(RequestDispatcher.ERROR_STATUS_CODE, status);
        request.setAttribute(RequestDispatcher.ERROR_REQUEST_URI, uri);
        request.setAttribute(RequestDispatcher.ERROR_EXCEPTION, new RuntimeException("internal diagnostic"));
        request.setAttribute(RequestDispatcher.ERROR_MESSAGE, "internal diagnostic");
        request.setParameter("trace", "true");
        request.setParameter("message", "true");
        request.setParameter("errors", "true");
        assertThat(controller.error(request).getBody())
            .containsEntry("message", HttpStatus.valueOf(status).getReasonPhrase())
            .containsEntry("path", safePath)
            .doesNotContainKeys("exception", "trace", "errors");
        assertThat(controller.error(request).getBody().toString())
            .doesNotContain("internal diagnostic", "case-123");
        assertThat(controller.error(request).getStatusCode().value()).isEqualTo(status);
    }

    @Test
    void browserPageDoesNotRenderTheErrorModel() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        response.setStatus(500);
        new ErrorPageConfiguration().errorView().render(Map.of("message", "internal diagnostic",
            "trace", "java.lang.SecretException", "path", "http://internal-service/private"),
            new MockHttpServletRequest(), response);
        assertThat(response.getStatus()).isEqualTo(500);
        assertThat(response.getContentAsString()).contains("Request failed")
            .doesNotContain("internal diagnostic", "SecretException", "internal-service", "Whitelabel");
    }
}
