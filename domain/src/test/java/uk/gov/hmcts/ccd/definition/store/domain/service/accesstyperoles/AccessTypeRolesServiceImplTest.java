package uk.gov.hmcts.ccd.definition.store.domain.service.accesstyperoles;

import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.ccd.definition.store.domain.service.EntityToResponseDTOMapper;
import uk.gov.hmcts.ccd.definition.store.repository.AccessTypeRolesRepository;

public class AccessTypeRolesServiceImplTest {

    private AccessTypeRolesServiceImpl classUnderTest;

    @Mock
    private AccessTypeRolesRepository repository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        classUnderTest = new AccessTypeRolesServiceImpl(repository);
    }
}
