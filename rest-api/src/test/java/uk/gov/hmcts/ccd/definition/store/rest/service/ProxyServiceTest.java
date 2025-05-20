package uk.gov.hmcts.ccd.definition.store.rest.service;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import java.io.IOException;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ProxyServiceTest {

    @InjectMocks
    private ProxyService proxyService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void failProxyRequest() {
        val exception = assertThrows(IOException.class, () -> proxyService.proxyRequest(""));
        assertThat(exception.getCause().getMessage(), is("Target host is not specified"));
    }
}
