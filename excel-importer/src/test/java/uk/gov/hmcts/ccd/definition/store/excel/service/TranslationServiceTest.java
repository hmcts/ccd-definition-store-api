package uk.gov.hmcts.ccd.definition.store.excel.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import uk.gov.hmcts.ccd.definition.store.domain.exception.BadRequestException;
import uk.gov.hmcts.ccd.definition.store.excel.client.translation.DictionaryRequest;
import uk.gov.hmcts.ccd.definition.store.excel.client.translation.TranslationServiceApiClient;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionSheet;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.ccd.definition.store.excel.service.TranslationHelper.buildSheetForCaseEvent;
import static uk.gov.hmcts.ccd.definition.store.excel.service.TranslationHelper.buildSheetForCaseType;
import static uk.gov.hmcts.ccd.definition.store.excel.service.TranslationHelper.buildSheetForGenerics;
import static uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName.CASE_EVENT;
import static uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName.CASE_TYPE;
import static uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName.WORK_BASKET_RESULT_FIELDS;

class TranslationServiceTest {

    @InjectMocks
    private TranslationServiceImpl translationService;

    @Mock
    private TranslationServiceApiClient translationServiceApiClient;

    private Map<String, DefinitionSheet> definitionSheets;
    private DefinitionSheet definitionSheet;
    private ListAppender<ILoggingEvent> filterLoggerCapture;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);

        Logger filterLogger = (Logger) LoggerFactory.getLogger(TranslationServiceImpl.class);
        filterLogger.setLevel(Level.DEBUG);
        filterLoggerCapture = new ListAppender<>();
        filterLoggerCapture.start();
        filterLogger.addAppender(filterLoggerCapture);

        definitionSheets = new HashMap<>();
        definitionSheet = new DefinitionSheet();
        definitionSheets.put(WORK_BASKET_RESULT_FIELDS.getName(), buildSheetForGenerics(WORK_BASKET_RESULT_FIELDS));
        definitionSheets.put(CASE_TYPE.getName(), buildSheetForCaseType());
        definitionSheets.put(CASE_EVENT.getName(), buildSheetForCaseEvent());
    }

    @Test
    void processDefinitionSheets_TranslationReturn200() {
        translationService.processDefinitionSheets(definitionSheets);
        verify(translationServiceApiClient, times(1)).uploadToDictionary(any());
        List<ILoggingEvent> loggingEvents = filterLoggerCapture.list;
        assertAll(
            () -> assertEquals(3, loggingEvents.size()),
            () -> assertEquals("calling Translation service.. start",
                loggingEvents.get(0).getFormattedMessage()),
            () -> assertEquals(Level.INFO, loggingEvents.get(0).getLevel()),
            () -> assertEquals(Level.WARN, loggingEvents.get(1).getLevel()),
            () -> assertEquals("calling Translation service.. end",
                loggingEvents.get(2).getFormattedMessage()),
            () -> assertEquals(Level.INFO, loggingEvents.get(2).getLevel())
        );
    }

    @Test
    void processDefinitionSheets_TranslationReturn4XX() {
        BadRequestException exception = new BadRequestException("Invalid request");
        doThrow(exception).when(translationServiceApiClient).uploadToDictionary(any(DictionaryRequest.class));
        translationService.processDefinitionSheets(definitionSheets);
        verify(translationServiceApiClient, times(1)).uploadToDictionary(any());
        List<ILoggingEvent> loggingEvents = filterLoggerCapture.list;
        assertAll(
            () -> assertEquals(3, loggingEvents.size()),
            () -> assertEquals("calling Translation service.. start",
                loggingEvents.get(0).getFormattedMessage()),
            () -> assertEquals(Level.INFO, loggingEvents.get(0).getLevel()),
            () -> assertEquals(Level.WARN, loggingEvents.get(1).getLevel()),
            () -> assertEquals("Errors calling Translation service will not fail the definition import",
                loggingEvents.get(1).getFormattedMessage()),

            () -> assertEquals("Error while calling Translation service",
                loggingEvents.get(2).getFormattedMessage()),
            () -> assertEquals(Level.ERROR, loggingEvents.get(2).getLevel())
        );
    }

}


