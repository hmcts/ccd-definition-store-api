package uk.gov.hmcts.ccd.definition.store.security.idam;

import uk.gov.hmcts.reform.authorisation.exceptions.InvalidTokenException;
import uk.gov.hmcts.reform.authorisation.exceptions.ServiceException;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import feign.FeignException;
import feign.Request;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class IdamRepositoryTest {

    private static final String TEST_USER_TOKEN = "Test";

    @Mock
    private IdamClient idamClient;

    @InjectMocks
    private IdamRepository idamRepository;

    private AutoCloseable autoCloseable;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Get user info if token is passed")
    void getUserInfo() {
        UserInfo userInfo = mock(UserInfo.class);
        when(idamClient.getUserInfo(anyString())).thenReturn(userInfo);
        UserInfo returnedUserInfo = idamRepository.getUserInfo(TEST_USER_TOKEN);
        assertNotNull(returnedUserInfo);
    }

    @Test
    @DisplayName("Throw InvalidTokenException for Feign exception with status code at client error lower boundary")
    void invalidTokenExceptionLowerBoundary() {
        checkInvalidTokenException(400, "invalidTokenExceptionLowerBoundary");
    }

    @Test
    @DisplayName("Throw InvalidTokenException for Feign exception with status code at client error upper boundary")
    void invalidTokenExceptionUpperBoundary() {
        // In reality the highest series 4xx code is 429 (Too many requests).  For upper boundary testing of
        // IdamRepository.isClientError() this test uses 499 which doesn't correspond to an existing series 4xx code.
        checkInvalidTokenException(499, "invalidTokenExceptionUpperBoundary");
    }

    @Test
    @DisplayName("Throw ServiceException for Feign exception with status code below client error lower boundary")
    void serviceExceptionBelowLowerBoundary() {
        // In reality the highest series 3xx code is 308 (Permanent Redirect).  For lower boundary testing of
        // IdamRepository.isClientError() this test uses 399 which doesn't correspond to an existing series 3xx code.
        checkServiceException(399, "serviceExceptionLowerRange");
    }

    @Test
    @DisplayName("Throw ServiceException for Feign exception with status code above client error upper boundary")
    void serviceExceptionAboveUpperBoundary() {
        checkServiceException(500, "serviceExceptionUpperRange");
    }

    @Test
    @DisplayName("Error message is logged when exception is caught")
    void errorMessageLogged() {
        String exceptionMessage = "errorMessageLogged";

        FeignException.FeignClientException feignClientException
            = new FeignException.FeignClientException(403, exceptionMessage, createRequestForFeignException(),
                new byte[0], null);

        when(idamClient.getUserInfo(anyString())).thenThrow(feignClientException);

        // Get handle to IdamRepository class logger
        Logger logger = (Logger) LoggerFactory.getLogger(IdamRepository.class);
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);

        assertThrows(InvalidTokenException.class, () -> idamRepository.getUserInfo(TEST_USER_TOKEN));

        // Confirm that the last entry in the logger contains the
        // expected message and has been logged at the expected level
        List<ILoggingEvent> loggerList = listAppender.list;
        ILoggingEvent lastLogEntry = loggerList.get(loggerList.size() - 1);
        assertEquals(Level.ERROR, lastLogEntry.getLevel());
        // Need to use getFormattedMessage() as exception message is logged using a parameterised string
        assertEquals("FeignException: retrieve user info: " + exceptionMessage,
            lastLogEntry.getFormattedMessage());

        logger.detachAndStopAllAppenders();
    }

    private Request createRequestForFeignException() {
        return Request.create(Request.HttpMethod.GET, "dummyUrl", Map.of(), new byte[0], Charset.defaultCharset(),
            null);
    }

    private void checkInvalidTokenException(int status, String message) {
        FeignException.FeignClientException feignClientException
            = new FeignException.FeignClientException(status, message, createRequestForFeignException(), new byte[0],
            null);

        when(idamClient.getUserInfo(anyString())).thenThrow(feignClientException);

        InvalidTokenException thrownException =
            assertThrows(InvalidTokenException.class, () -> idamRepository.getUserInfo(TEST_USER_TOKEN));
        assertEquals(message, thrownException.getMessage());
    }

    private void checkServiceException(int status, String message) {
        FeignException.FeignServerException feignServerException
            = new FeignException.FeignServerException(status, message, createRequestForFeignException(), new byte[0],
            null);

        when(idamClient.getUserInfo(anyString())).thenThrow(feignServerException);

        ServiceException thrownException =
            assertThrows(ServiceException.class, () -> idamRepository.getUserInfo(TEST_USER_TOKEN));
        assertEquals(message, thrownException.getMessage());
    }
}
