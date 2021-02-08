package uk.gov.hmcts.ccd.definition.store.excel.endpoint;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import uk.gov.hmcts.ccd.definition.store.excel.service.RoleToAccessProfileMappingService;

import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class RoleToAccessProfileControllerTest {

    private static final String URL = "/access-profile/mapping";

    @Mock
    private RoleToAccessProfileMappingService roleToAccessProfileMappingService;

    @InjectMocks
    RoleToAccessProfileController roleToAccessProfileController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(roleToAccessProfileController).build();
    }

    @DisplayName("Create Role to access profiles mapping")
    @Test
    void shouldCreateRoleToAccessProfileMapping() throws Exception {
        when(roleToAccessProfileMappingService.createAccessProfileMapping(anySet()))
            .thenReturn("Role to access profile mapping completed successfully");

        mockMvc.perform(put(URL).param("ctid", "CaseType_1")).andExpect(status().isOk());
    }

}
