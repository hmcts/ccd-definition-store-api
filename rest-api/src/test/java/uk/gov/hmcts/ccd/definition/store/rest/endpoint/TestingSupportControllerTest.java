package uk.gov.hmcts.ccd.definition.store.rest.endpoint;

import static java.util.Collections.emptyList;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

import java.util.List;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.NativeQuery;
import org.hibernate.type.IntegerType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;

@ExtendWith(MockitoExtension.class)
class TestingSupportControllerTest {
    @Mock
    private SessionFactory sessionFactory;
    @Mock
    private Session session;
    @Mock
    private NativeQuery nativeQuery;
    @Mock
    private Transaction transaction;

    @InjectMocks
    private TestingSupportController controller;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = standaloneSetup(controller)
            .setControllerAdvice(new ControllerExceptionHandler())
            .build();
        when(sessionFactory.openSession())
            .thenReturn(session);
    }

    @Test
    @DisplayName("Should execute delete queries")
    void shouldDeleteRecords() throws Exception {
        when(session.createNativeQuery(anyString()))
            .thenReturn(nativeQuery);
        when(nativeQuery.setParameterList(eq("caseTypesWithChangeIds"), anyList()))
            .thenReturn(nativeQuery);
        when(nativeQuery.setParameterList(eq("caseTypeIds"), anyList(), isA(IntegerType.class)))
            .thenReturn(nativeQuery);
        when(nativeQuery.list())
            .thenReturn(List.of("1","2"));
        when(session.getTransaction())
            .thenReturn(transaction);
        mockMvc.perform(delete("/api/testing-support/cleanup-case-type/1")
                .param("caseTypeIds", "Benefit"))
            .andDo(print())
            .andExpect(status().isOk());

        verify(session, times(29))
            .createNativeQuery(anyString());
    }

    @Test
    @DisplayName("Should return case type not found")
    void shouldReturnNotFound() throws Exception {
        when(session.createNativeQuery(anyString()))
            .thenReturn(nativeQuery);
        when(nativeQuery.setParameterList(eq("caseTypesWithChangeIds"), anyList()))
            .thenReturn(nativeQuery);
        when(nativeQuery.list())
            .thenReturn(emptyList());
        when(session.getTransaction())
            .thenReturn(transaction);
        mockMvc.perform(delete("/api/testing-support/cleanup-case-type/1")
                .param("caseTypeIds", "NoFound"))
            .andDo(print())
            .andExpect(status().isNotFound())
            .andExpect(content().json("{\"message\":\"Object Not Found for:Unable to find case type\"}"));

        verify(session, times(1))
            .createNativeQuery(anyString());
    }
}
