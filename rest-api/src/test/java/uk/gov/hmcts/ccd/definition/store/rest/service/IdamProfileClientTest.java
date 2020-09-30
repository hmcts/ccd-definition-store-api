package uk.gov.hmcts.ccd.definition.store.rest.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.ccd.definition.store.repository.SecurityUtils;
import uk.gov.hmcts.ccd.definition.store.rest.model.IdamProperties;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

class IdamProfileClientTest {

    @Mock
    private SecurityUtils securityUtils;

    @InjectMocks
    private IdamProfileClient profileClient;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void getLoggedInUserDetails() {
        UserInfo userInfo = mock(UserInfo.class);
        Mockito.when(securityUtils.getUserInfo()).thenReturn(userInfo);
        IdamProperties idamProperties = profileClient.getLoggedInUserDetails();
        assertNotNull(idamProperties);
    }

}
