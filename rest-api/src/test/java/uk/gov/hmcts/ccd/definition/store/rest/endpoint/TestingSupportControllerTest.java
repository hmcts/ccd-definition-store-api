package uk.gov.hmcts.ccd.definition.store.rest.endpoint;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.MutationQuery;
import org.hibernate.query.NativeQuery;
import org.hibernate.resource.transaction.spi.TransactionStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;

import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@ExtendWith(MockitoExtension.class)
class TestingSupportControllerTest {
    @Mock
    private SessionFactory sessionFactory;
    @Mock
    private Session session;
    @Mock
    private NativeQuery<Integer> nativeQuery;
    @Mock
    private MutationQuery mutationQuery;
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
        when(session.createNativeQuery(anyString(), eq(Integer.class)))
            .thenReturn(nativeQuery);
        when(nativeQuery.setParameterList(eq("caseTypesWithChangeIds"), anyList()))
            .thenReturn(nativeQuery);
        when(nativeQuery.list())
            .thenReturn(List.of(Integer.parseInt("1"),Integer.parseInt("2")));
        when(session.createNativeMutationQuery(anyString()))
            .thenReturn(mutationQuery);
        when(mutationQuery.setParameterList(eq("caseTypeIds"), anyList()))
            .thenReturn(mutationQuery);
        when(session.getTransaction())
            .thenReturn(transaction);
        mockMvc.perform(delete("/api/testing-support/cleanup-case-type/1")
                .param("caseTypeIds", "Benefit"))
            .andDo(print())
            .andExpect(status().isOk());

        verify(session, times(1))
            .createNativeQuery(anyString(), eq(Integer.class));

        verify(session, times(28))
            .createNativeMutationQuery(anyString());
    }

    @Test
    @DisplayName("Should execute delete queries Only With CaseType Ids")
    void shouldDeleteRecordsOnlyWithCaseTypeIds() throws Exception {
        when(session.createNativeQuery(anyString(), eq(Integer.class)))
            .thenReturn(nativeQuery);
        when(nativeQuery.setParameterList(eq("caseTypesWithChangeIds"), anyList()))
            .thenReturn(nativeQuery);
        when(nativeQuery.list())
            .thenReturn(List.of(Integer.parseInt("1"),Integer.parseInt("2")));
        when(session.createNativeMutationQuery(anyString()))
            .thenReturn(mutationQuery);
        when(mutationQuery.setParameterList(eq("caseTypeIds"), anyList()))
            .thenReturn(mutationQuery);
        when(session.getTransaction())
            .thenReturn(transaction);
        mockMvc.perform(delete("/api/testing-support/cleanup-case-type/id/1"))
            .andDo(print())
            .andExpect(status().isOk());

        verify(session, times(1))
            .createNativeQuery(anyString(), eq(Integer.class));

        verify(session, times(28))
            .createNativeMutationQuery(anyString());
    }

    @Test
    @DisplayName("Should return case type not found")
    void shouldReturnNotFound() throws Exception {
        when(session.createNativeQuery(anyString(), eq(Integer.class)))
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
            .createNativeQuery(anyString(), eq(Integer.class));

        verify(session, never())
            .createNativeMutationQuery(anyString());

    }

    @Test
    @DisplayName("Should delete a user role")
    void shouldDeleteUserRole() throws Exception {
        setUpUserRoleDeletion();
        when(mutationQuery.executeUpdate())
            .thenReturn(1);

        mockMvc.perform(delete("/api/testing-support/cleanup-user-role")
                .param("role", "functional-role"))
            .andExpect(status().isOk());

        verify(mutationQuery).executeUpdate();
        verify(transaction).commit();
        verify(transaction, never()).rollback();
        verify(session).close();
    }

    @Test
    @DisplayName("Should return not found when user role does not exist")
    void shouldReturnUserRoleNotFound() throws Exception {
        setUpUserRoleDeletion();
        when(mutationQuery.executeUpdate())
            .thenReturn(0);

        mockMvc.perform(delete("/api/testing-support/cleanup-user-role")
                .param("role", "missing-role"))
            .andExpect(status().isNotFound())
            .andExpect(content().json("{\"message\":\"Object Not Found for:Unable to find user role\"}"));

        verify(transaction).commit();
        verify(transaction, never()).rollback();
        verify(session).close();
    }

    @Test
    @DisplayName("Should roll back and close the session when creating the user role deletion query fails")
    void shouldCleanUpWhenUserRoleDeletionQueryFails() {
        RuntimeException failure = new RuntimeException("Unable to create deletion query");
        when(session.beginTransaction()).thenReturn(transaction);
        when(transaction.getStatus()).thenReturn(TransactionStatus.ACTIVE);
        when(session.createNativeMutationQuery(anyString())).thenThrow(failure);

        RuntimeException thrown = assertThrows(RuntimeException.class,
            () -> controller.cleanupUserRole("functional-role"));

        assertSame(failure, thrown);
        verify(transaction).rollback();
        verify(transaction, never()).commit();
        verify(session).close();
    }

    @Test
    @DisplayName("Should roll back and close the session when deleting a user role fails")
    void shouldCleanUpWhenUserRoleDeletionFails() {
        setUpUserRoleDeletion();
        RuntimeException failure = new RuntimeException("Role is referenced by a case ACL");
        when(transaction.getStatus()).thenReturn(TransactionStatus.ACTIVE);
        when(mutationQuery.executeUpdate()).thenThrow(failure);

        RuntimeException thrown = assertThrows(RuntimeException.class,
            () -> controller.cleanupUserRole("functional-role"));

        assertSame(failure, thrown);
        verify(transaction).rollback();
        verify(transaction, never()).commit();
        verify(session).close();
    }

    @Test
    @DisplayName("Should roll back and close the session when committing a user role deletion fails")
    void shouldCleanUpWhenUserRoleDeletionCommitFails() {
        setUpUserRoleDeletion();
        RuntimeException failure = new RuntimeException("Unable to commit deletion");
        when(mutationQuery.executeUpdate()).thenReturn(1);
        when(transaction.getStatus()).thenReturn(TransactionStatus.FAILED_COMMIT);
        doThrow(failure).when(transaction).commit();

        RuntimeException thrown = assertThrows(RuntimeException.class,
            () -> controller.cleanupUserRole("functional-role"));

        assertSame(failure, thrown);
        verify(transaction).rollback();
        verify(session).close();
    }

    @Test
    @DisplayName("Should close the session without repeating a rollback after a failed commit")
    void shouldNotRepeatRollbackWhenFailedCommitAlreadyRolledBack() {
        setUpUserRoleDeletion();
        RuntimeException failure = new RuntimeException("Deletion commit was rolled back");
        when(mutationQuery.executeUpdate()).thenReturn(1);
        when(transaction.getStatus()).thenReturn(TransactionStatus.ROLLED_BACK);
        doThrow(failure).when(transaction).commit();

        RuntimeException thrown = assertThrows(RuntimeException.class,
            () -> controller.cleanupUserRole("functional-role"));

        assertSame(failure, thrown);
        verify(transaction, never()).rollback();
        verify(session).close();
    }

    @Test
    @DisplayName("Should close the session when starting the user role deletion transaction fails")
    void shouldCloseSessionWhenUserRoleDeletionTransactionCannotStart() {
        RuntimeException failure = new RuntimeException("Unable to start transaction");
        when(session.beginTransaction()).thenThrow(failure);

        RuntimeException thrown = assertThrows(RuntimeException.class,
            () -> controller.cleanupUserRole("functional-role"));

        assertSame(failure, thrown);
        verify(session).close();
        verify(session, never()).createNativeMutationQuery(anyString());
        verifyNoInteractions(transaction);
    }

    @Test
    @DisplayName("Should preserve the deletion failure and close the session when rollback also fails")
    void shouldPreserveUserRoleDeletionFailureWhenRollbackFails() {
        setUpUserRoleDeletion();
        RuntimeException deletionFailure = new RuntimeException("Role is referenced by a case ACL");
        RuntimeException rollbackFailure = new RuntimeException("Unable to roll back deletion");
        when(transaction.getStatus()).thenReturn(TransactionStatus.ACTIVE);
        when(mutationQuery.executeUpdate()).thenThrow(deletionFailure);
        doThrow(rollbackFailure).when(transaction).rollback();

        RuntimeException thrown = assertThrows(RuntimeException.class,
            () -> controller.cleanupUserRole("functional-role"));

        assertSame(deletionFailure, thrown);
        assertArrayEquals(new Throwable[]{rollbackFailure}, thrown.getSuppressed());
        verify(transaction).rollback();
        verify(transaction, never()).commit();
        verify(session).close();
    }

    private void setUpUserRoleDeletion() {
        when(session.beginTransaction()).thenReturn(transaction);
        when(session.createNativeMutationQuery(anyString())).thenReturn(mutationQuery);
        when(mutationQuery.setParameter(eq("role"), anyString())).thenReturn(mutationQuery);
    }
}
