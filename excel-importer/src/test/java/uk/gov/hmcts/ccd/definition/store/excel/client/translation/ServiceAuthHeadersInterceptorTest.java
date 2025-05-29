package uk.gov.hmcts.ccd.definition.store.excel.client.translation;

import uk.gov.hmcts.ccd.definition.store.repository.SecurityUtils;

import feign.RequestTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.mockito.BDDMockito.given;

class ServiceAuthHeadersInterceptorTest {

    public static final String S2S_TOKEN = "dcdsfda";
    @InjectMocks
    private ServiceAuthHeadersInterceptor interceptor;

    @Mock
    private SecurityUtils securityUtils;
    private RequestTemplate template;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        template = new RequestTemplate();
    }

    @Test
    @DisplayName("System user auth headers should apply")
    void shouldApplyAuthHeaders() {
        given(securityUtils.getS2SToken()).willReturn(S2S_TOKEN);

        interceptor.apply(template);

        assertThat(template.headers().get("ServiceAuthorization"), contains(S2S_TOKEN));
    }
}
