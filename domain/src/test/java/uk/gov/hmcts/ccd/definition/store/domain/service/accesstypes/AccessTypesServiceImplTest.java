package uk.gov.hmcts.ccd.definition.store.domain.service.accesstypes;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.ccd.definition.store.repository.AccessTypesRepository;
import uk.gov.hmcts.ccd.definition.store.repository.entity.AccessTypeEntity;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class AccessTypesServiceImplTest {

    private AccessTypesServiceImpl classUnderTest;

    @Mock
    private AccessTypesRepository repository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        classUnderTest = new AccessTypesServiceImpl(repository);
    }

    @Test
    @DisplayName(
        "Should save the passed entities")
    void shouldSaveEntity() {
        AccessTypeEntity accessTypeRoleEntity = mock(AccessTypeEntity.class);
        List<AccessTypeEntity> entitiesToSave = new ArrayList<>();
        entitiesToSave.add(accessTypeRoleEntity);
        classUnderTest.saveAll(entitiesToSave);
        verify(repository, times(1)).saveAll(eq(entitiesToSave));
    }
}
