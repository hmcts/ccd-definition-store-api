package uk.gov.hmcts.ccd.definition.store.excel.common;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.fail;

public class TestLoggerUtils {

    private TestLoggerUtils() {
        // Hide Utility Class Constructor : Utility classes should not have a public or default constructor
        // (squid:S1118)
    }

    public static ListAppender<ILoggingEvent> setupLogger() {
        Logger logger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);

        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        listAppender.start();

        logger.addAppender(listAppender);
        return listAppender;
    }

    public static void teardownLogger() {
        Logger logger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        logger.detachAndStopAllAppenders();
    }

    public static void assertLogged(final ListAppender<ILoggingEvent> listAppender, final String message) {
        if (listAppender.list.stream()
            .noneMatch(e -> e.getFormattedMessage().equals(message))) {
            fail("No logging event matches: " + message);
        }
    }

}
