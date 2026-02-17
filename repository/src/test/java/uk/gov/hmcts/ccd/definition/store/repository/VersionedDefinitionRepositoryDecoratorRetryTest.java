package uk.gov.hmcts.ccd.definition.store.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;

import java.util.List;
import java.util.Optional;

import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VersionedDefinitionRepositoryDecoratorRetryTest {

    @Mock
    private VersionedDefinitionRepository<CaseTypeEntity, Integer> repository;

    private VersionedDefinitionRepositoryDecorator<CaseTypeEntity, Integer> decorator;

    @BeforeEach
    void setUp() {
        decorator = new VersionedDefinitionRepositoryDecorator<>(repository);
    }

    @Nested
    @DisplayName("save() retry on DataIntegrityViolationException")
    class SaveRetryTests {

        @Test
        @DisplayName("Retries save and succeeds when first save throws DataIntegrityViolationException")
        void retriesSaveWhenDataIntegrityViolationExceptionThrown() {
            CaseTypeEntity entity = new CaseTypeEntity();
            entity.setReference("id");
            entity.setName("name");

            when(repository.findLastVersion("id")).thenReturn(Optional.of(0));
            when(repository.save(any(CaseTypeEntity.class)))
                .thenThrow(new DataIntegrityViolationException("unique constraint"))
                .thenReturn(entity);

            CaseTypeEntity result = decorator.save(entity);

            verify(repository, times(2)).findLastVersion("id");
            verify(repository, times(2)).save(any(CaseTypeEntity.class));
            assertThat(result, is(entity));
        }

        @Test
        @DisplayName("Re-assigns version on retry so second save uses updated version")
        void reassignsVersionOnRetry() {
            CaseTypeEntity entity = new CaseTypeEntity();
            entity.setReference("id");
            entity.setName("name");

            when(repository.findLastVersion("id"))
                .thenReturn(Optional.of(0))   // first assignVersion
                .thenReturn(Optional.of(1));  // second assignVersion (after another invocation committed v1)
            when(repository.save(any(CaseTypeEntity.class)))
                .thenThrow(new DataIntegrityViolationException("unique constraint"))
                .thenAnswer(inv -> inv.getArgument(0));

            CaseTypeEntity result = decorator.save(entity);

            verify(repository, times(2)).findLastVersion("id");
            verify(repository, times(2)).save(any(CaseTypeEntity.class));
            // After retry, entity was re-assigned version 2 (lastFromDb 1 + 1)
            assertThat(result.getVersion(), is(2));
        }

        @Test
        @DisplayName("Does not retry when save throws a different exception")
        void doesNotRetryOnOtherException() {
            CaseTypeEntity entity = new CaseTypeEntity();
            entity.setReference("id");
            when(repository.findLastVersion("id")).thenReturn(Optional.of(0));
            when(repository.save(any(CaseTypeEntity.class)))
                .thenThrow(new IllegalArgumentException("other"));

            assertThrows(IllegalArgumentException.class, () -> decorator.save(entity));

            verify(repository, times(1)).findLastVersion("id");
            verify(repository, times(1)).save(any(CaseTypeEntity.class));
        }
    }

    @Nested
    @DisplayName("saveAll() retry on DataIntegrityViolationException")
    class SaveAllRetryTests {

        @Test
        @DisplayName("Retries saveAll and succeeds when first saveAll throws DataIntegrityViolationException")
        void retriesSaveAllWhenDataIntegrityViolationExceptionThrown() {
            CaseTypeEntity entity = new CaseTypeEntity();
            entity.setReference("id");
            entity.setName("name");
            List<CaseTypeEntity> list = singletonList(entity);

            when(repository.findLastVersion("id")).thenReturn(Optional.of(0));
            when(repository.saveAll(any(List.class)))
                .thenThrow(new DataIntegrityViolationException("unique constraint"))
                .thenReturn(list);

            List<CaseTypeEntity> result = decorator.saveAll(list);

            verify(repository, times(2)).findLastVersion("id");
            verify(repository, times(2)).saveAll(any(List.class));
            assertThat(result.size(), is(1));
            assertThat(result.get(0), is(entity));
        }

        @Test
        @DisplayName("Re-assigns versions for batch on retry")
        void reassignsVersionsForBatchOnRetry() {
            CaseTypeEntity entity = new CaseTypeEntity();
            entity.setReference("id");
            entity.setName("name");
            List<CaseTypeEntity> list = singletonList(entity);

            when(repository.findLastVersion("id"))
                .thenReturn(Optional.of(0))
                .thenReturn(Optional.of(1));
            when(repository.saveAll(any(List.class)))
                .thenThrow(new DataIntegrityViolationException("unique constraint"))
                .thenAnswer(inv -> inv.getArgument(0));

            List<CaseTypeEntity> result = decorator.saveAll(list);

            verify(repository, times(2)).findLastVersion("id");
            verify(repository, times(2)).saveAll(any(List.class));
            // After retry, entity was re-assigned version 2 (lastFromDb 1 + 1)
            assertThat(result.get(0).getVersion(), is(2));
        }

        @Test
        @DisplayName("Does not retry saveAll when a different exception is thrown")
        void doesNotRetrySaveAllOnOtherException() {
            CaseTypeEntity entity = new CaseTypeEntity();
            entity.setReference("id");
            List<CaseTypeEntity> list = singletonList(entity);

            when(repository.findLastVersion("id")).thenReturn(Optional.of(0));
            when(repository.saveAll(any(List.class)))
                .thenThrow(new IllegalStateException("other"));

            assertThrows(IllegalStateException.class, () -> decorator.saveAll(list));

            verify(repository, times(1)).findLastVersion("id");
            verify(repository, times(1)).saveAll(any(List.class));
        }
    }
}
