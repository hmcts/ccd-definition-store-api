package uk.gov.hmcts.ccd.definition.store.rest.endpoint;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import uk.gov.hmcts.ccd.definition.store.domain.exception.NotFoundException;
import uk.gov.hmcts.ccd.definition.store.domain.service.CaseRoleService;
import uk.gov.hmcts.ccd.definition.store.domain.service.JurisdictionService;
import uk.gov.hmcts.ccd.definition.store.domain.service.casetype.CaseTypeService;
import uk.gov.hmcts.ccd.definition.store.domain.service.casetype.CaseTypeVersionInformation;
import uk.gov.hmcts.ccd.definition.store.repository.model.CaseType;
import uk.gov.hmcts.ccd.definition.store.repository.model.Jurisdiction;
import uk.gov.hmcts.ccd.definition.store.repository.model.Version;
import uk.gov.hmcts.ccd.definition.store.rest.endpoint.exceptions.RestEndPointExceptionHandler;

import java.util.List;
import java.util.Optional;

import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsCollectionContaining.hasItems;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class CaseDefinitionControllerTest {

    private CaseTypeService caseTypeService = mock(CaseTypeService.class);
    private JurisdictionService jurisdictionService = mock(JurisdictionService.class);
    private CaseRoleService caseRoleService = mock(CaseRoleService.class);
    private CaseDefinitionController subject;
    private MockMvc mockMvc;

    @BeforeEach
    public void createSubject() {
        subject = new CaseDefinitionController(caseTypeService, jurisdictionService, caseRoleService);
        mockMvc = MockMvcBuilders.standaloneSetup(subject)
            .setControllerAdvice(new RestEndPointExceptionHandler())
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

    @Nested
    @DisplayName("Test the dataJurisdictionsJurisdictionIdCaseTypeGet method")
    class DataJurisdictionsJurisdictionIdCaseTypeGetTests {

        @Test
        @DisplayName("Should call the CaseTypeService with the jurisdictionId when jurisdictionId is not null")
        public void shouldCallCaseTypeService_whenJurisdictionIdIsNotNull() {
            subject.dataJurisdictionsJurisdictionIdCaseTypeGet("SAMPLE-ID");
            verify(caseTypeService, times(1)).findByJurisdictionId(eq("SAMPLE-ID"));
        }

        @Test
        @DisplayName("Should call the CaseTypeService with null when jurisdictionId is null")
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
    @DisplayName("Test the dataCaseworkerIdAndJurisdictionIdCaseTypeGet method")
    class FindByCaseTypeIdTests {

        @Test
        @DisplayName("Should return the CaseType when the CaseType exists")
        public void shouldReturnCaseType_whenCaseTypeExists() {
            String caseTypeId = "CaseTypeId";
            CaseType caseType = new CaseType();

            when(caseTypeService.findByCaseTypeId(caseTypeId)).thenReturn(Optional.of(caseType));

            assertEquals(caseType, subject.dataCaseworkerIdAndJurisdictionIdCaseTypeGet(null, null, caseTypeId));
            verify(caseTypeService).findByCaseTypeId(caseTypeId);
        }

        @Test
        @DisplayName("Should throw a NotFoundException when the CaseType does not exist")
        public void shouldThrowNotFoundException_whenCaseTypeDoesNotExist() {
            String caseTypeId = "CaseTypeId";

            when(caseTypeService.findByCaseTypeId(caseTypeId)).thenReturn(Optional.empty());

            NotFoundException notFoundException = assertThrows(NotFoundException.class,
                () -> subject.dataCaseworkerIdAndJurisdictionIdCaseTypeGet(null, null, caseTypeId)
            );

            assertEquals(caseTypeId, notFoundException.getMessage());
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
            assertEquals(itemName, result.getId());
            verify(caseTypeService).findByCaseTypeId(itemName);
        }

        @Test
        @DisplayName("Should throw a NotFoundException when the CaseType does not exist")
        public void shouldThrowNotFoundException_whenCaseTypeDoesNotExist() {
            String caseTypeId = "CaseTypeId";

            when(caseTypeService.findByCaseTypeId(caseTypeId)).thenReturn(Optional.empty());

            NotFoundException notFoundException = assertThrows(NotFoundException.class,
                () -> subject.dataCaseTypeIdGet(caseTypeId));

            assertEquals(caseTypeId, notFoundException.getMessage());
        }
    }

    @Nested
    @DisplayName("Test retrieval of jurisdiction definition")
    class JurisdictionTests {

        @Test
        public void shouldCallJurisdictionGetAllWithListOfIds() {
            subject.findJurisdictions(Optional.of(newArrayList("J1", "J2")));
            ArgumentCaptor<List> argument = ArgumentCaptor.forClass(List.class);
            verify(jurisdictionService, times(1)).getAll((List<String>) argument.capture());
            List<String> value = argument.getValue();
            assertThat(value, hasItems("J1", "J2"));
        }

        @Test
        public void shouldCallJurisdictionGetAllWhenNoIds() {
            subject.findJurisdictions(Optional.empty());
            verify(jurisdictionService, times(0)).getAll(anyList());
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
            assertThat(mvcResult.getResponse().getContentAsString(), is("{\"version\":9087}"));
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
