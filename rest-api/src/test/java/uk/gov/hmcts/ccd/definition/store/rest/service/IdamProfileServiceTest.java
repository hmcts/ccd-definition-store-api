package uk.gov.hmcts.ccd.definition.store.rest.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import uk.gov.hmcts.ccd.definition.store.repository.SecurityUtils;
import uk.gov.hmcts.ccd.definition.store.rest.model.IdamProperties;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class IdamProfileServiceTest {

    private static final String USER_ID = "123";

    private IdamProfileClient client;

    @Mock
    private SecurityUtils securityUtils;

    @Captor
    private ArgumentCaptor<String> idamUserProfileURLCaptor;

    @Captor
    private ArgumentCaptor<HttpMethod> httpMethodCaptor;

    @Captor
    private ArgumentCaptor<HttpEntity> requestEntityCaptor;

    @Captor
    private ArgumentCaptor<Class<IdamProperties>> idamPropertiesClassCaptor;

    @BeforeEach
    void setUp() {
        client = new IdamProfileClient(securityUtils);
        UserInfo userInfo = UserInfo.builder()
            .uid(USER_ID)
            .sub("emailId@a.com")
            .roles(new ArrayList<>())
            .build();
        doReturn(userInfo).when(securityUtils).getUserInfo();
    }

    @DisplayName("Should get logged in user details")
    @Test
    public void shouldGetLoggedInUserDetails() {
        final IdamProperties expectedIdamProperties = client.getLoggedInUserDetails();
        assertEquals(USER_ID, expectedIdamProperties.getId());
        assertEquals("emailId@a.com", expectedIdamProperties.getEmail());
    }

}
