package uk.gov.hmcts.ccd.definition.store.rest.endpoint;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import uk.gov.hmcts.ccd.definition.store.domain.service.EntityToResponseDTOMapper;
import uk.gov.hmcts.ccd.definition.store.domain.service.EntityToResponseDTOMapperImpl;
import uk.gov.hmcts.ccd.definition.store.domain.service.accessprofiles.RoleToAccessProfilesServiceImpl;
import uk.gov.hmcts.ccd.definition.store.domain.service.accesstyperoles.AccessTypeRolesServiceImpl;
import uk.gov.hmcts.ccd.definition.store.domain.service.casetype.CaseTypeServiceImpl;
import uk.gov.hmcts.ccd.definition.store.domain.service.legacyvalidation.LegacyCaseTypeValidator;
import uk.gov.hmcts.ccd.definition.store.domain.service.metadata.MetadataFieldService;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.domain.validation.casetype.CaseTypeEntityValidator;
import uk.gov.hmcts.ccd.definition.store.repository.AccessTypeRolesRepository;
import uk.gov.hmcts.ccd.definition.store.repository.CaseTypeRepository;
import uk.gov.hmcts.ccd.definition.store.repository.RoleToAccessProfilesRepository;
import uk.gov.hmcts.ccd.definition.store.repository.entity.*;
import uk.gov.hmcts.ccd.definition.store.repository.model.*;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.TEXT_PLAIN;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = {EntityToResponseDTOMapperImpl.class})
@ExtendWith(SpringExtension.class)
public class AccessTypeRolesControllerTest {

    private AccessTypeRolesController controller;
    private AccessTypeRolesServiceImpl accessTypeRolesServiceImpl;

    private CaseTypeServiceImpl caseTypeServiceImpl;

    private RoleToAccessProfilesServiceImpl roleToAccessProfilesServiceImpl;

    @Mock
    private LegacyCaseTypeValidator legacyCaseTypeValidator;

    //@Mock
    private MetadataFieldService metadataFieldService;

    @Captor
    private ArgumentCaptor<Collection<CaseTypeEntity>> captor;

    @Spy
    private  EntityToResponseDTOMapper entityToResponseDTOMapper = new EntityToResponseDTOMapperImpl();

    @MockBean
    private AccessTypeRolesRepository accessTypeRolesRepository;

    @Mock
    private RoleToAccessProfilesRepository roleToAccessProfilesRepository;

    private MockMvc mockMvc;

    private List<String> orgProfileIds = List.of(new String[]{"organisationProfileId_1", "organisationProfileId_2"});

    private AccessTypeRolesEntity accessTypeRolesEntity;
    private AccessTypeRolesField accessTypeRolesField;

    private AccessTypeRolesEntity accessTypeRolesEntity1;
    private AccessTypeRolesField accessTypeRolesField1;

    private AccessTypeRolesEntity accessTypeRolesEntity2;
    private AccessTypeRolesField accessTypeRolesField2;


    private JurisdictionEntity jurisdiction = new JurisdictionEntity();
    private  CaseTypeEntity caseTypeEntity = new CaseTypeEntity();
    private  CaseTypeEntity caseTypeEntity1 = new CaseTypeEntity();

    private CaseTypeEntity caseTypeEntity2 = new CaseTypeEntity();

    private CaseTypeEntity caseTypeEntity3 = new CaseTypeEntity();

    private Collection<CaseTypeEntity> caseTypeEntities = Arrays.asList(caseTypeEntity1, caseTypeEntity2,
        caseTypeEntity3);
    private static final String JURISDICTION_REFERENCE = "TEST";

    private static final String CASE_TYPE_REFERENCE = "get-test";
    private static final String CASE_TYPE_REFERENCE_1 = "get-test1";
    private static final String CASE_TYPE_REFERENCE_2 = "get-test2";
    private static final String CASE_TYPE_REFERENCE_3 = "get-test3";
    private static final int DEFAULT_VERSION = 69;
    @Mock
    private CaseTypeEntityValidator caseTypeEntityValidator1;

    @Mock
    private CaseTypeEntityValidator caseTypeEntityValidator2;
    @Mock
    private CaseTypeRepository caseTypeRepository;

    @BeforeEach
    void setUp() throws IOException {
        initMocks(this);

        //accessTypeRolesRepository = Mockito.mock(AccessTypeRolesRepository.class);
        setUpRolesToAccessProfile();
        setUpCaseTypeData();
        setUpAccessTypeRoleData();

/*        doReturn(orgProfileIds)
            .when(accessTypeRolesRepository)
            .findByOrganisationProfileIds(orgProfileIds);*/
         controller = new AccessTypeRolesController(entityToResponseDTOMapper, accessTypeRolesRepository);

         mockMvc = MockMvcBuilders.standaloneSetup(controller)
            .setControllerAdvice(new ControllerExceptionHandler())
            .build();
    }

    @DisplayName("Should post retrieve-access-types fail")
    @Test
    public void shouldFailPostretrieve_access_types_Request() throws Exception {
        final MvcResult mvcResult = mockMvc.perform(post("/api/retrieve-access-types")
                .contentType(TEXT_PLAIN)
                .content(""))
            .andExpect(status().is4xxClientError()).andReturn();
    }

    @Test
    @DisplayName("Should handle empty accessTypeRoles collection")
    void shouldHandleEmptyCollection() throws Exception {
        OrganisationProfileIds organisationProfileIds = new OrganisationProfileIds();
        ATRJurisdictionResults expected = new ATRJurisdictionResults();

        ObjectMapper objmapper = new ObjectMapper();
        final MvcResult mvcResult = mockMvc.perform(post("/api/retrieve-access-types")
                .contentType(APPLICATION_JSON)
                .content(objmapper.writeValueAsString(organisationProfileIds)))
            .andExpect(status().isOk()).andReturn();

        assertThat(mvcResult.getResponse().getContentAsString().toString(), is("{\"jurisdictions\":[]}"));

        ATRJurisdictionResults jurisdictionResults = controller.retrieveAccessTypeRoles(organisationProfileIds);
        assertEquals(expected.getJurisdictions(), jurisdictionResults.getJurisdictions());
    }

    @DisplayName("should return AccessTypeRolesField List")
    @Test
    void shouldHandleACollection() throws Exception {
        OrganisationProfileIds organisationProfileIds = new OrganisationProfileIds();
        organisationProfileIds.setOrganisationProfileIds(orgProfileIds);
        List<AccessTypeRolesField> expected = new ArrayList<>();

        //AccessTypeRolesEntity source = accessTypeRolesEntity;
        //AccessTypeRolesField target =  entityToResponseDTOMapper.map(source);

        ObjectMapper objmapper = new ObjectMapper();
        String response = objmapper.writeValueAsString(new AccessTypeRolesField());
        String request = objmapper.writeValueAsString(organisationProfileIds);

        final MvcResult mvcResult = mockMvc.perform(post("/api/retrieve-access-types")
                //.contextPath("/api")
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .content(request))
            .andDo(print())
            .andExpect(status().isOk()).andReturn();

        //assertThat(mvcResult.getResponse().getContentAsString(), is("[]"));
        assertAll(
            () -> assertThat(mvcResult.getResponse().getContentAsString(), is(3)),
            //() -> assertThat(mvcResult.get(0).getOrganisationProfileId(), is(orgProfileIds.get(0)))
            () -> assertThat(mvcResult.getResponse().getContentAsString(), containsString(orgProfileIds.get(0)))
        );

        ATRJurisdictionResults jurisdictionResults = controller.retrieveAccessTypeRoles(organisationProfileIds);
        assertEquals(expected, jurisdictionResults );
    }

    private <T> Matcher<T> matchesCaseTypeEntityWithJurisdictionAndVersionAdded(CaseTypeEntity caseTypeEntity1,
                                                                                JurisdictionEntity jurisdiction,
                                                                                int version) {

        return new BaseMatcher<T>() {
            @Override
            public boolean matches(Object o) {
                return o == caseTypeEntity1
                    && ((CaseTypeEntity) o).getJurisdiction().equals(jurisdiction)
                    && ((CaseTypeEntity) o).getVersion().equals(version);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("Expected to be the same CaseTypeEntity instance with the jurisdiction set");
            }
        };

    }

    private CaseTypeEntity createCaseType(String caseTypeId, String jurisdictionPostfix) {

        CaseTypeEntity caseTypeEntity = new CaseTypeEntity();
        JurisdictionEntity jurisdiction = new JurisdictionEntity();
        jurisdiction.setName("test-" + jurisdictionPostfix);

        Version version = new Version();

        caseTypeEntity.setVersion(version.getNumber());
        caseTypeEntity.setJurisdiction(jurisdiction);
        caseTypeEntity.setName(caseTypeId);
        return caseTypeEntity;
    }

    private AccessTypeRolesEntity createAccessTypeRolesEntity(String caseTypeId, String organisationProfileId) {
        AccessTypeRolesEntity accessTypeRolesEntity = new AccessTypeRolesEntity();
        AccessTypeRolesField accessTypeRolesField = new AccessTypeRolesField();

        //String caseTypeId = "get-test";
        CaseTypeEntity caseTypeReturned = createCaseType(caseTypeId, caseTypeId);

        //accessTypeRolesField = createAccessTypeRolesField(caseTypeId );
        accessTypeRolesEntity.setCaseTypeId(caseTypeReturned);
        accessTypeRolesEntity.setAccessTypeId("default");
        accessTypeRolesEntity.setAccessMandatory(true);
        accessTypeRolesEntity.setAccessDefault(true);
        accessTypeRolesEntity.setGroupAccessEnabled(true);
        accessTypeRolesEntity.setDisplay(true);
        accessTypeRolesEntity.setOrganisationProfileId(organisationProfileId);
        accessTypeRolesEntity.setDescription("ACCESS CASES");
        accessTypeRolesEntity.setOrganisationalRoleName("civil-solicitor");
        accessTypeRolesEntity.setLiveFrom(LocalDate.now());
        accessTypeRolesEntity.setHint("Hint:User can work with all civil cases without needing to be assigned to each case");
        accessTypeRolesEntity.setDescription("Description:User can work with all civil cases without needing to be assigned to each case");
        accessTypeRolesEntity.setDisplayOrder(1);
        accessTypeRolesEntity.setGroupRoleName("[APPLICANTSOLICITORONE]");
        accessTypeRolesEntity.setOrganisationPolicyField("applicant1OrganisationPolicy");
        accessTypeRolesEntity.setCaseAccessGroupIdTemplate("CIVIL:all:CIVIL:AS1:$ORGID$");

        accessTypeRolesField = entityToResponseDTOMapper.map(accessTypeRolesEntity);
        accessTypeRolesField.setIdOfCaseType(caseTypeReturned.getId());

        CaseTypeEntity casetypeEntityResult = accessTypeRolesField.getCaseTypeId();
//Commented as null Helen        assertEquals(caseTypeReturned, casetypeEntityResult.getId());

        return accessTypeRolesEntity;
    }

    void setUpCaseTypeData(){
        List<AccessTypeRolesField> accessTypeRolesFields;

        caseTypeEntity.setReference(CASE_TYPE_REFERENCE);
        caseTypeEntity1.setReference(CASE_TYPE_REFERENCE_1);
        caseTypeEntity2.setReference(CASE_TYPE_REFERENCE_2);
        caseTypeEntity3.setReference(CASE_TYPE_REFERENCE_3);
        jurisdiction.setReference(JURISDICTION_REFERENCE);

        caseTypeEntities = Arrays.asList(caseTypeEntity1, caseTypeEntity2,
            caseTypeEntity3);

        when(caseTypeRepository.findLastVersion(any())).thenReturn(Optional.of(DEFAULT_VERSION));
        when(caseTypeEntityValidator1.validate(any())).thenReturn(new ValidationResult());
        when(caseTypeEntityValidator2.validate(any())).thenReturn(new ValidationResult());
        when(caseTypeRepository.findFirstByReferenceIgnoreCaseOrderByCreatedAtDescIdDesc(CASE_TYPE_REFERENCE_1))
            .thenReturn(Optional.of(caseTypeEntity1));

        CaseType caseType = new CaseType();
        caseType.setId(CASE_TYPE_REFERENCE_1);
        when(entityToResponseDTOMapper.map(same(caseTypeEntity1))).thenReturn(caseType);
        when(caseTypeRepository.findFirstByReferenceIgnoreCaseOrderByCreatedAtDescIdDesc(CASE_TYPE_REFERENCE_2))
            .thenReturn(Optional.empty());
        when(caseTypeRepository.findFirstByReferenceIgnoreCaseOrderByCreatedAtDescIdDesc(CASE_TYPE_REFERENCE_3))
            .thenReturn(Optional.empty());

        accessTypeRolesFields = caseType.getAccessTypeRoles();

        caseTypeServiceImpl = new CaseTypeServiceImpl(
            caseTypeRepository,
            entityToResponseDTOMapper,
            legacyCaseTypeValidator,
            Arrays.asList(caseTypeEntityValidator1, caseTypeEntityValidator2),
            metadataFieldService);
        caseTypeServiceImpl.createAll(jurisdiction, caseTypeEntities, new HashSet<>());
        assertComponentsCalled(true, null);

    }

    private <T> Matcher<T> matchesCaseTypeEntityWithJurisdictionAdded(CaseTypeEntity caseTypeEntity1,
                                                                      JurisdictionEntity jurisdiction) {

        return new BaseMatcher<T>() {
            @Override
            public boolean matches(Object o) {
                return o == caseTypeEntity1
                    && ((CaseTypeEntity) o).getJurisdiction().equals(jurisdiction);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("Expected to be the same CaseTypeEntity instance with the jurisdiction set");
            }
        };

    }

    private void assertComponentsCalled(boolean shouldSave, CaseTypeEntity caseTypeWithLegacyValidationException) {

        InOrder inOrder = Mockito.inOrder(
            legacyCaseTypeValidator,
            caseTypeEntityValidator1, caseTypeEntityValidator2,
            caseTypeRepository
        );

        for (CaseTypeEntity caseTypeEntity : caseTypeEntities) {
            inOrder.verify(legacyCaseTypeValidator).validateCaseType(
                argThat(matchesCaseTypeEntityWithJurisdictionAdded(caseTypeEntity, jurisdiction)));
            if (caseTypeWithLegacyValidationException != null
                && caseTypeWithLegacyValidationException == caseTypeEntity) {
                return;
            }
            inOrder.verify(caseTypeEntityValidator1).validate(
                argThat(matchesCaseTypeEntityWithJurisdictionAdded(caseTypeEntity, jurisdiction)));
            inOrder.verify(caseTypeEntityValidator2).validate(
                argThat(matchesCaseTypeEntityWithJurisdictionAdded(caseTypeEntity, jurisdiction)));
            inOrder.verify(caseTypeRepository).findFirstByReferenceIgnoreCaseOrderByCreatedAtDescIdDesc(
                caseTypeEntity.getReference());
            inOrder.verify(caseTypeRepository).caseTypeExistsInAnyJurisdiction(
                caseTypeEntity.getReference(), jurisdiction.getReference());
        }

        if (shouldSave) {
            inOrder.verify(caseTypeRepository).saveAll(captor.capture());
            Collection<CaseTypeEntity> savedCaseTypeEntities = captor.getValue();
            assertEquals(caseTypeEntities.size(), savedCaseTypeEntities.size());

            for (CaseTypeEntity caseTypeEntity : caseTypeEntities) {
                assertThat(savedCaseTypeEntities, hasItem(
                    matchesCaseTypeEntityWithJurisdictionAndVersionAdded(caseTypeEntity, jurisdiction,
                        DEFAULT_VERSION + 1)));
            }

        }

        inOrder.verifyNoMoreInteractions();

    }

    void setUpAccessTypeRoleData(){

        String caseTypeId = CASE_TYPE_REFERENCE;//"get-test";
        accessTypeRolesEntity = createAccessTypeRolesEntity(caseTypeId, "SOLICITOR_ORG");
        List<AccessTypeRolesEntity> entityList = new ArrayList<>();
        entityList.add(accessTypeRolesEntity);

        caseTypeId = CASE_TYPE_REFERENCE_1;//"get-test1";
        accessTypeRolesEntity1 = createAccessTypeRolesEntity(caseTypeId, "organisationProfileId_1");
        entityList.add(accessTypeRolesEntity1);

        caseTypeId = CASE_TYPE_REFERENCE_2;//"get-test2";
        accessTypeRolesEntity2 = createAccessTypeRolesEntity(caseTypeId, "organisationProfileId_2");
        entityList.add(accessTypeRolesEntity2);

        accessTypeRolesServiceImpl = new AccessTypeRolesServiceImpl(accessTypeRolesRepository,
            entityToResponseDTOMapper);

        List<AccessTypeRolesEntity> entities = Arrays.asList(accessTypeRolesEntity, accessTypeRolesEntity1,
            accessTypeRolesEntity2);
        when(accessTypeRolesRepository.saveAll(entities)).thenReturn(entities);
        accessTypeRolesServiceImpl.saveAll(entities);
        verify(accessTypeRolesRepository).saveAll(entities);
        verify(accessTypeRolesRepository, times(1)).saveAll(eq(entityList));

        doReturn(accessTypeRolesField).when(entityToResponseDTOMapper).map(accessTypeRolesEntity);
        doReturn(accessTypeRolesField1).when(entityToResponseDTOMapper).map(accessTypeRolesEntity1);
        doReturn(accessTypeRolesField2).when(entityToResponseDTOMapper).map(accessTypeRolesEntity2);

        List<AccessTypeRolesEntity> result = accessTypeRolesRepository.findByOrganisationProfileIds(orgProfileIds);
        when(accessTypeRolesRepository.findByOrganisationProfileIds(orgProfileIds)).thenReturn(result);
    }


    private void setUpRolesToAccessProfile(){
        //Roles to access profiles
        roleToAccessProfilesServiceImpl = new RoleToAccessProfilesServiceImpl(roleToAccessProfilesRepository, entityToResponseDTOMapper, caseTypeRepository);

        List<RoleToAccessProfilesEntity> roleToAccessProfileEntities = Lists.newArrayList();
        roleToAccessProfileEntities.add(createRoleToAccessProfile(CASE_TYPE_REFERENCE_1, "TestRole1", "judge"));
        roleToAccessProfileEntities.add(createRoleToAccessProfile(CASE_TYPE_REFERENCE_2, "TestRole2", "solicitor"));
        doReturn(roleToAccessProfileEntities).when(roleToAccessProfilesRepository).findByCaseTypeReferenceIn(anyList());
        List<String> references = Lists.newArrayList(CASE_TYPE_REFERENCE_1, CASE_TYPE_REFERENCE_2, CASE_TYPE_REFERENCE_3);
        List<RoleToAccessProfiles> valuesReturned = roleToAccessProfilesServiceImpl.findByCaseTypeReferences(references);
        Assert.assertEquals(2, valuesReturned.size());

        roleToAccessProfilesServiceImpl.saveAll(roleToAccessProfileEntities);
        verify(roleToAccessProfilesRepository, times(1)).saveAll(eq(roleToAccessProfileEntities));

    }
    private RoleToAccessProfilesEntity createRoleToAccessProfile(String caseType, String roleName, String accessProfiles) {
        RoleToAccessProfilesEntity roleToAccessProfilesEntity = new RoleToAccessProfilesEntity();
        roleToAccessProfilesEntity.setCaseType(createCaseTypeEntity(caseType));
        roleToAccessProfilesEntity.setRoleName(roleName);
        roleToAccessProfilesEntity.setAccessProfiles(accessProfiles);
        roleToAccessProfilesEntity.setDisabled(false);
        roleToAccessProfilesEntity.setReadOnly(false);
        roleToAccessProfilesEntity.setAuthorisation("");
        return roleToAccessProfilesEntity;
    }

    private CaseTypeEntity createCaseTypeEntity(String caseType) {
        CaseTypeEntity entity = new CaseTypeEntity();
        entity.setReference(caseType);
        return entity;
    }
}
