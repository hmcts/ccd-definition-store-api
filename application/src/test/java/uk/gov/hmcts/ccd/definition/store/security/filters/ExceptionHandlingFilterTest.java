package uk.gov.hmcts.ccd.definition.store.security.filters;

import java.io.IOException;
import java.util.List;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.catalina.connector.ClientAbortException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class ExceptionHandlingFilterTest {

    private MockHttpServletRequest request;

    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    private ExceptionHandlingFilter filter;

    private Logger logger;
    private ListAppender<ILoggingEvent> listAppender;

    private static final String EXCEPTION_MESSAGE = "Exception thrown in the security filter chain";
    private static final String EXCEPTION_LOGGER_TYPE =
        "uk.gov.hmcts.ccd.definition.store.security.filters.ExceptionHandlingFilter";

    @BeforeEach
    public void setUp() {
        filter = new ExceptionHandlingFilter();
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();

        logger = (Logger) LoggerFactory.getLogger(ExceptionHandlingFilter.class);
        // create and start a ListAppender
        listAppender = new ListAppender<>();
        listAppender.start();

        // add the appender to the logger
        logger.addAppender(listAppender);
    }

    @AfterEach
    public void tearDown() {
        listAppender.stop();
        logger.detachAppender(listAppender);
    }

    @Test
    public void shouldReturn502ResponseWhenClientAbortExceptionThrown() throws ServletException, IOException {
        Mockito.doThrow(ClientAbortException.class)
            .when(filterChain)
            .doFilter(Mockito.eq(request), Mockito.eq(response));
        filter.doFilterInternal(request, response, filterChain);

        assertEquals(502, response.getStatus());
        assertLogging();
    }

    @Test
    public void shouldReturn500ResponseWhenAnyExceptionExceptClientAbortExceptionThrown()
        throws ServletException, IOException {
        Mockito.doThrow(NullPointerException.class)
            .when(filterChain)
            .doFilter(Mockito.eq(request), Mockito.eq(response));
        filter.doFilterInternal(request, response, filterChain);

        assertEquals(500, response.getStatus());
        assertLogging();
    }

    private void assertLogging() {
        List<ILoggingEvent> logsList = listAppender.list;
        ILoggingEvent lastLogEntry = logsList.get(logsList.size() - 1);

        assertEquals(Level.ERROR, lastLogEntry.getLevel());
        assertEquals(EXCEPTION_LOGGER_TYPE, lastLogEntry.getLoggerName());
        assertEquals(EXCEPTION_MESSAGE, lastLogEntry.getMessage());
    }
}
