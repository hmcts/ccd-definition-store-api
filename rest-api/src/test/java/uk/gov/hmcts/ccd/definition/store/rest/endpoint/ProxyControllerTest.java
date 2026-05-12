package uk.gov.hmcts.ccd.definition.store.rest.endpoint;

import uk.gov.hmcts.ccd.definition.store.rest.service.ProxyService;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.TEXT_PLAIN;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@ExtendWith(MockitoExtension.class)
class ProxyControllerTest {

    private MockMvc mockMvc;
    private ProxyController controller;

    @Mock
    private ProxyService proxyService;

    @BeforeEach
    void setUp() {
        this.controller = new ProxyController(proxyService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
            .setControllerAdvice(new ControllerExceptionHandler())
            .build();
    }

    @DisplayName("Should post proxy fail")
    @Test
    void shouldFailPostProxyRequest() throws Exception {
        final MvcResult mvcResult = mockMvc.perform(post("/proxy")
            .contentType(TEXT_PLAIN)
            .content(""))
            .andExpect(status().is4xxClientError()).andReturn();
    }

    @DisplayName("Should post proxy request")
    @Test
    void shouldPostProxyRequest() throws Exception {
        val url = "http://testest";
        val expectedResult = "mockedHTML";
        when(proxyService.proxyRequest(anyString())).thenReturn(expectedResult);
        val result = controller.proxyRequest(url);
        assertEquals(expectedResult,result);
    }
}
