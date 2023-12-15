package uk.gov.hmcts.ccd.definition.store.domain.service.accesstyperoles;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.ccd.definition.store.domain.service.EntityToResponseDTOMapper;
import uk.gov.hmcts.ccd.definition.store.repository.AccessTypeRolesRepository;
import uk.gov.hmcts.ccd.definition.store.repository.entity.AccessTypeRolesEntity;


import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.mock;

public class AccessTypeRolesServiceImplTest {

    private AccessTypeRolesServiceImpl classUnderTest;

    @Mock
    private AccessTypeRolesRepository repository;

    @Mock
    private EntityToResponseDTOMapper dtoMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        classUnderTest = new AccessTypeRolesServiceImpl(repository, dtoMapper);
    }

    @Test
    @DisplayName(
        "Should save the passed entities")
    void shouldSaveEntity() {
        AccessTypeRolesEntity accessTypeRolesEntity = mock(AccessTypeRolesEntity.class);
        List<AccessTypeRolesEntity> entitiesToSave = new ArrayList<>();
        entitiesToSave.add(accessTypeRolesEntity);
        classUnderTest.saveAll(entitiesToSave);
        verify(repository, times(1)).saveAll(eq(entitiesToSave));
    }

}
