package uk.gov.hmcts.ccd.definition.store.rest.endpoint;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import uk.gov.hmcts.ccd.definition.store.domain.exception.BadRequestException;
import uk.gov.hmcts.ccd.definition.store.domain.exception.NotFoundException;
import uk.gov.hmcts.ccd.definition.store.domain.service.CaseRoleService;
import uk.gov.hmcts.ccd.definition.store.domain.service.JurisdictionService;
import uk.gov.hmcts.ccd.definition.store.domain.service.accessprofiles.RoleToAccessProfileService;
import uk.gov.hmcts.ccd.definition.store.domain.service.casetype.CaseTypeService;
import uk.gov.hmcts.ccd.definition.store.domain.service.casetype.CaseTypeVersionInformation;
import uk.gov.hmcts.ccd.definition.store.repository.model.CaseType;
import uk.gov.hmcts.ccd.definition.store.repository.model.CaseTypeLite;
import uk.gov.hmcts.ccd.definition.store.repository.model.Jurisdiction;
import uk.gov.hmcts.ccd.definition.store.repository.model.Version;

import java.util.List;
import java.util.Optional;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CaseDefinitionControllerTest {

    private final CaseTypeService caseTypeService = mock(CaseTypeService.class);
    private final JurisdictionService jurisdictionService = mock(JurisdictionService.class);
    private final CaseRoleService caseRoleService = mock(CaseRoleService.class);
    private final RoleToAccessProfileService roleToAccessProfilesService = mock(RoleToAccessProfileService.class);
    private CaseDefinitionController subject;
    private MockMvc mockMvc;

    @BeforeEach
    void createSubject() {
        subject = new CaseDefinitionController(caseTypeService, jurisdictionService, caseRoleService,
            roleToAccessProfilesService);
        mockMvc = MockMvcBuilders.standaloneSetup(subject)
            .setControllerAdvice(new ControllerExceptionHandler())
            .build();
    }

    private CaseType createCaseType(String caseTypeId, String jurisdictionPostfix) {
        CaseType caseType = new CaseType();
        Jurisdiction jurisdiction = new Jurisdiction();
        jurisdiction.setId("test-" + jurisdictionPostfix);
        caseType.setJurisdiction(jurisdiction);
        caseType.setId(caseTypeId);
        Version version = new Version();
        caseType.setVersion(version);
        return caseType;
    }

    @Deprecated
    @Disabled
    @Nested
    @SuppressWarnings("removal")
    @DisplayName("Test the dataJurisdictionsJurisdictionIdCaseTypeGet method")
    class DataJurisdictionsJurisdictionIdCaseTypeGetTests {

        @Test
        @DisplayName("Should call the CaseTypeService with the jurisdictionId when jurisdictionId is not null")
        @SuppressWarnings({"deprecation", "removal"})
        public void shouldCallCaseTypeService_whenJurisdictionIdIsNotNull() {
            subject.dataJurisdictionsJurisdictionIdCaseTypeGet("SAMPLE-ID");
            verify(caseTypeService, times(1)).findByJurisdictionId(eq("SAMPLE-ID"));
        }

        @Test
        @DisplayName("Should call the CaseTypeService with null when jurisdictionId is null")
        @SuppressWarnings({"deprecation", "removal"})
        public void shouldCallCaseTypeService_whenJurisdictionIdIsNull() {
            subject.dataJurisdictionsJurisdictionIdCaseTypeGet(null);
            verify(caseTypeService, times(1)).findByJurisdictionId(null);
        }
    }

    @Nested
    @DisplayName("Test the getCaseRoles method")
    class GetCaseRoleTests {

        @Test
        @DisplayName("Should call the caseRoleService by correct parameter")
        void shouldCallCaseRoleService() {
            subject.getCaseRoles(null, null, "someCaseTypeId");
            verify(caseRoleService, times(1)).findByCaseTypeId(eq("someCaseTypeId"));
        }

        @Test
        @DisplayName("Should call the caseRoleService with null when parameter is null")
        void shouldCallCaseRoleServiceWhenParameterIsNull() {
            subject.getCaseRoles(null, null, null);
            verify(caseRoleService, times(1)).findByCaseTypeId(null);
        }
    }

    @Nested
    @DisplayName("Test the getRoleToAccessProfiles   method")
    class GetCaseRoleForRATests {

        @Test
        @DisplayName("Should call the roleToAccessProfilesService by correct parameter")
        void shouldCallCaseRoleService() {
            subject.getRoleToAccessProfiles(null, null, "someCaseTypeId");
            verify(roleToAccessProfilesService, times(1)).findRoleAssignmentsByCaseTypeId(eq("someCaseTypeId"));
        }

        @Test
        @DisplayName("Should call the roleToAccessProfilesService with null when parameter is null")
        void shouldCallCaseRoleServiceWhenParameterIsNull() {
            subject.getRoleToAccessProfiles(null, null, null);
            verify(roleToAccessProfilesService, times(1)).findRoleAssignmentsByCaseTypeId(null);
        }
    }

    @Nested
    @DisplayName("Test the dataCaseworkerIdAndJurisdictionIdCaseTypeGet method")
    class FindByCaseTypeIdTests {

        @Test
        @DisplayName("Should return the CaseType when the CaseType exists")
        public void shouldReturnCaseType_whenCaseTypeExists() {
            String caseTypeId = "CaseTypeId";
            CaseType caseType = new CaseType();

            when(caseTypeService.findByCaseTypeId(caseTypeId)).thenReturn(Optional.of(caseType));

            assertThat(subject.dataCaseworkerIdAndJurisdictionIdCaseTypeGet(null, null, caseTypeId))
                .isEqualTo(caseType);
            verify(caseTypeService).findByCaseTypeId(caseTypeId);
        }

        @Test
        @DisplayName("Should throw a NotFoundException when the CaseType does not exist")
        public void shouldThrowNotFoundException_whenCaseTypeDoesNotExist() {
            String caseTypeId = "CaseTypeId";

            when(caseTypeService.findByCaseTypeId(caseTypeId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> subject.dataCaseworkerIdAndJurisdictionIdCaseTypeGet(null, null, caseTypeId))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(caseTypeId);
        }
    }

    @Nested
    @DisplayName("Test the dataCaseTypeIdGet method")
    class DataCaseTypeIdGetTests {

        @Test
        @DisplayName("Should return the CaseType when the CaseType exists")
        public void shouldReturnCaseType_whenCaseTypeExistsForId() {
            String itemName = "get-test";
            CaseType caseTypeReturned = createCaseType(itemName, itemName);
            when(caseTypeService.findByCaseTypeId(any())).thenReturn(Optional.of(caseTypeReturned));
            CaseType result = subject.dataCaseTypeIdGet(itemName);
            assertThat(result.getId()).isEqualTo(itemName);
            verify(caseTypeService).findByCaseTypeId(itemName);
        }

        @Test
        @DisplayName("Should throw a NotFoundException when the CaseType does not exist")
        public void shouldThrowNotFoundException_whenCaseTypeDoesNotExist() {
            String caseTypeId = "CaseTypeId";

            when(caseTypeService.findByCaseTypeId(caseTypeId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> subject.dataCaseTypeIdGet(caseTypeId))
                .isInstanceOf(NotFoundException.class)
                .hasMessage(caseTypeId);
        }
    }

    @Nested
    @DisplayName("Test retrieval of jurisdiction definition")
    class JurisdictionTests {

        @ParameterizedTest
        @ValueSource(ints = {0, -1, Integer.MIN_VALUE})
        void shouldRejectOrderBelowOneBeforeRetrievingJurisdictions(int order) {
            Optional<List<String>> jurisdictionIds = Optional.empty();
            Optional<Integer> caseTypeOrder = Optional.of(order);

            assertThatThrownBy(() -> subject.findJurisdictions(jurisdictionIds, caseTypeOrder))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("caseTypeOrder must be greater than or equal to 1");
            verifyNoInteractions(jurisdictionService);
        }

        @ParameterizedTest
        @ValueSource(booleans = {false, true})
        void shouldFilterAndSortByIdOnlyWhenOrderIsPresent(boolean filterPresent) {
            CaseTypeLite zulu = caseTypeLite("AId", "Zulu", 42);
            CaseTypeLite alpha = caseTypeLite("ZId", "alpha", 42);
            CaseTypeLite other = caseTypeLite("Other", "Beta", 7);
            Jurisdiction jurisdiction = new Jurisdiction();
            jurisdiction.setCaseTypes(List.of(alpha, other, zulu));
            when(jurisdictionService.getAll()).thenReturn(List.of(jurisdiction));

            List<Jurisdiction> result = subject.findJurisdictions(Optional.empty(),
                filterPresent ? Optional.of(42) : Optional.empty());

            assertThat(result).singleElement().satisfies(item -> {
                if (filterPresent) {
                    assertThat(item.getCaseTypes()).extracting(CaseTypeLite::getId).containsExactly("AId", "ZId");
                } else {
                    assertThat(item.getCaseTypes()).extracting(CaseTypeLite::getId)
                        .containsExactly("ZId", "Other", "AId");
                }
            });
        }

        private CaseTypeLite caseTypeLite(String id, String name, Integer order) {
            CaseTypeLite caseType = new CaseTypeLite();
            caseType.setId(id);
            caseType.setName(name);
            caseType.setCaseTypesOrder(order);
            return caseType;
        }

        @Test
        void shouldCallJurisdictionGetAllWithListOfIds() {
            subject.findJurisdictions(Optional.of(newArrayList("J1", "J2")), Optional.empty());
            ArgumentCaptor<List> argument = ArgumentCaptor.forClass(List.class);
            verify(jurisdictionService, times(1)).getAll((List<String>) argument.capture());
            List<String> value = argument.getValue();
            assertThat(value).contains("J1", "J2");
        }

        @Test
        void shouldCallJurisdictionGetAllWhenNoIds() {
            subject.findJurisdictions(Optional.empty(), Optional.empty());
            verify(jurisdictionService, never()).getAll(anyList());
            verify(jurisdictionService, times(1)).getAll();
        }
    }

    @Nested
    @DisplayName("Case type version information")
    class VersionInfo {

        private static final String CASE_TYPE_REFERENCE = "ThreeSevenEightNine";

        @Test
        @DisplayName("Green path")
        void greenPath() throws Exception {
            when(caseTypeService
                .findVersionInfoByCaseTypeId(CASE_TYPE_REFERENCE))
                .thenReturn(Optional.of(new CaseTypeVersionInformation(9087)));
            final MvcResult
                mvcResult =
                mockMvc.perform(get("/api/data/case-type/" + CASE_TYPE_REFERENCE + "/version"))
                    .andExpect(status().isOk())
                    .andReturn();
            assertThat(mvcResult.getResponse().getContentAsString()).isEqualTo("{\"version\":9087}");
        }

        @Test
        @DisplayName("Not found")
        void notFound() throws Exception {
            when(caseTypeService.findVersionInfoByCaseTypeId(CASE_TYPE_REFERENCE)).thenReturn(Optional.empty());
            mockMvc.perform(get("/api/data/case-type/" + CASE_TYPE_REFERENCE + "/version"))
                .andExpect(status().isNotFound());
        }
    }

}
