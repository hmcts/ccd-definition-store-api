package uk.gov.hmcts.ccd.definition.store.repository;

import javax.persistence.EntityManager;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static uk.gov.hmcts.ccd.definition.store.CustomHamcrestMatchers.hasItemWithProperty;

import org.apache.commons.lang3.StringUtils;
import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.ccd.definition.store.repository.entity.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {
    SanityCheckApplication.class,
    TestConfiguration.class
})
@TestPropertySource(locations = "classpath:test.properties")
@Transactional
/**
 * Tests that the object graph on Case Type entity is correctly saved and fetched.
 */
public class CaseTypeObjectGraphTest {

    public static final String CASE_FIELD_REFERENCE = "ref cf";
    public static final String CASE_FIELD_LABEL = "lab";
    public static final LocalDate TODAY = LocalDate.of(2017, 7, 7);
    public static final LocalDate TOMORROW = LocalDate.of(2017, 8, 19);

    @Autowired
    private CaseTypeRepository caseTypeRepository;

    @Autowired
    private TestHelper helper;

    @Autowired
    private EntityManager entityManager;

    private JurisdictionEntity jurisdiction;
    private FieldTypeEntity fieldType;

    private VersionedDefinitionRepositoryDecorator<CaseTypeEntity, Integer> versionedCaseTypeRepository;
    private UserRoleEntity userRole1;
    private UserRoleEntity userRole2;
    private UserRoleEntity userRole3;

    @Before
    public void setup() {
        versionedCaseTypeRepository = new VersionedDefinitionRepositoryDecorator<>(caseTypeRepository);

        jurisdiction = helper.createJurisdiction();
        fieldType = helper.createType(jurisdiction);
        userRole1 = helper.createUserRole("user role 1", "user role 1", SecurityClassification.PUBLIC);
        userRole2 = helper.createUserRole("user role 2", "user role 2", SecurityClassification.PRIVATE);
        userRole3 = helper.createUserRole("user role 3", "user role 3", SecurityClassification.RESTRICTED);
    }

    @Test
    public void saveCaseType() {
        final WebhookEntity printWebhook = createWebHook("http://print");

        final CaseTypeEntity caseType = new CaseTypeEntity();
        caseType.setReference("id");
        caseType.setName("ename");
        caseType.setJurisdiction(jurisdiction);
        caseType.setPrintWebhook(printWebhook);
        caseType.setSecurityClassification(SecurityClassification.PUBLIC);

        CaseFieldEntity cf = helper.buildCaseField(CASE_FIELD_REFERENCE, fieldType, CASE_FIELD_LABEL, false);
        cf.setLiveFrom(LocalDate.of(2017, 8, 31));
        cf.setSecurityClassification(SecurityClassification.RESTRICTED);

        cf.addCaseFieldACL(createCaseFieldUserRoleEntity(userRole1, true, false, false, true));
        cf.addCaseFieldACL(createCaseFieldUserRoleEntity(userRole2, true, true, false, true));

        caseType.addCaseField(cf);

        final EventEntity e1 = createEvent("eid", "ename", 0, SecurityClassification.PRIVATE);
        e1.addEventCaseField(helper.createEventCaseField(cf, DisplayContext.READONLY, "showCond", true));
        final EventEntity e2 = createEvent("eid2", "ename2", 1, SecurityClassification.RESTRICTED);
        final StateEntity s1 = createState("stateId", "stateName", "desc", 3, TODAY, TOMORROW);
        final StateEntity s2 = createState("stateId2", "stateName2", "desc2", 3, TODAY, TOMORROW);

        final StateACLEntity stateACLEntity1 = createStateUserRoleEntity(userRole1, false, false, true, true);
        final StateACLEntity stateACLEntity2 = createStateUserRoleEntity(userRole2, false, true, true, false);
        s1.addStateACL(stateACLEntity1);
        s1.addStateACL(stateACLEntity2);

        caseType.addEvent(e1);
        caseType.addEvent(e2);
        caseType.addState(s1);
        caseType.addState(s2);

        final CaseTypeACLEntity cture1 = createCaseTypeUserRoleEntity(userRole1, false, false, true, true);
        final CaseTypeACLEntity cture2 = createCaseTypeUserRoleEntity(userRole2, false, true, true, false);
        final CaseTypeACLEntity cture3 = createCaseTypeUserRoleEntity(userRole3, true, false, true, true);
        caseType.addCaseTypeACL(cture1);
        caseType.addCaseTypeACL(cture2);
        caseType.addCaseTypeACL(cture3);

        e1.setPostState(s1);

        WebhookEntity h1 = createWebHook("url1", 3, 5, 6, 7, 8);
        WebhookEntity h2 = createWebHook("url2", 3, 50, 6, 20);
        WebhookEntity h3 = createWebHook("url3", 23, 5, 6);
        e1.setWebhookStart(h1);
        e1.setWebhookPreSubmit(h2);
        e1.setWebhookPostSubmit(h3);

        e1.addPreState(s1);
        e1.addPreState(s2);

        e1.setSecurityClassification(SecurityClassification.PRIVATE);

        e1.addEventACL(createEventUserRoleEntity(userRole1, true, false, false, true));
        e1.addEventACL(createEventUserRoleEntity(userRole2, false, false, false, true));
        e2.addEventACL(createEventUserRoleEntity(userRole1, true, true, false, true));
        e2.addEventACL(createEventUserRoleEntity(userRole3, false, true, false, true));

        final CaseTypeEntity saved = versionedCaseTypeRepository.save(caseType);

        // Clear down the entity manager to ensure we are actually reading from the DB
        entityManager.flush();
        entityManager.clear();

        final Optional<CaseTypeEntity> found = caseTypeRepository.findById(saved.getId());
        final CaseTypeEntity fetched = found.get();
        assertThat(fetched, is(notNullValue()));
        assertThat(fetched.getCreatedAt(), is(notNullValue()));
        assertThat(fetched.getCreatedAt().isBefore(LocalDateTime.now()), is(true));
        assertThat(fetched.getPrintWebhook(), hasProperty("url", is("http://print")));
        assertThat(fetched.getJurisdiction(), hasProperty("reference", is("jurisdiction")));
        assertThat(fetched.getSecurityClassification(), is(SecurityClassification.PUBLIC));
        assertThat(fetched.getEvents(), hasItemWithProperty("preStates", contains(
                hasProperty("reference", is("stateId")),
                hasProperty("reference", is("stateId2"))
        )));

        final List<EventEntity> fetchedEvents = fetched.getEvents();
        assertTrue(fetchedEvents.stream().anyMatch(x -> x.getWebhookStart() != null
            &&  x.getWebhookStart().getTimeouts().equals(Lists.newArrayList(3, 5, 6, 7, 8))));
        assertTrue(fetchedEvents.stream().anyMatch(x -> x.getWebhookPreSubmit() != null
            && x.getWebhookPreSubmit().getTimeouts().equals(Lists.newArrayList(3, 50, 6, 20))));
        assertTrue(fetchedEvents.stream().anyMatch(x -> x.getWebhookPostSubmit() != null
            && x.getWebhookPostSubmit().getTimeouts().equals(Lists.newArrayList(23, 5, 6))));
        assertThat(fetchedEvents, hasItem(hasProperty("securityClassification", equalTo(SecurityClassification.PRIVATE))));
        assertThat(fetchedEvents.get(1).getEventCaseFields(), hasSize(1));
        EventCaseFieldEntity eventCaseFieldEntity = fetchedEvents.get(1).getEventCaseFields().get(0);
        assertThat(eventCaseFieldEntity.getCaseField().getReference(), equalTo(cf.getReference()));
        assertThat(eventCaseFieldEntity.getEvent().getReference(), equalTo(e1.getReference()));
        assertThat(eventCaseFieldEntity.getDisplayContext(), equalTo(DisplayContext.READONLY));
        assertThat(eventCaseFieldEntity.getDisplayContext(), equalTo(DisplayContext.READONLY));
        assertThat(eventCaseFieldEntity.getShowCondition(), equalTo("showCond"));
        assertThat(eventCaseFieldEntity.getShowSummaryChangeOption(), equalTo(true));

        // Check states
        assertThat(fetched.getStates(), hasSize(2));
        final StateEntity fetchedState1 = fetched.getStates().get(0);
        assertThat(fetchedState1.getReference(), equalTo("stateId"));
        assertThat(fetchedState1.getLiveFrom(), equalTo(TODAY));
        assertThat(fetchedState1.getLiveTo(), equalTo(TOMORROW));
        assertStateUserRoleEntity(fetchedState1, stateACLEntity1, fetchedState1.getStateACLEntities().get(0));
        assertStateUserRoleEntity(fetchedState1, stateACLEntity2, fetchedState1.getStateACLEntities().get(1));

        // Check case field
        assertThat(fetched.getCaseFields(), hasSize(1));
        final CaseFieldEntity caseField = fetched.getCaseFields().get(0);
        assertThat(caseField.getReference(), equalTo(CASE_FIELD_REFERENCE));
        assertThat(caseField.getLabel(), equalTo(CASE_FIELD_LABEL));
        assertThat(caseField.getFieldType().getReference(), is(fieldType.getReference()));
        assertThat(caseField.getLiveFrom(), equalTo(LocalDate.of(2017, 8, 31)));
        assertThat(caseField.getSecurityClassification(), equalTo(SecurityClassification.RESTRICTED));

        // Check security classifications can be updated
        fetched.setSecurityClassification(SecurityClassification.RESTRICTED);
        fetched.getCaseFields().get(0).setSecurityClassification(SecurityClassification.PRIVATE);
        fetched.getEvents().get(0).setSecurityClassification(SecurityClassification.PUBLIC);

        // Check authorisation case types
        assertThat(fetched.getCaseTypeACLEntities(), hasSize(3));
        assertCaseTypeUserRoleEntity(fetched, cture1, fetched.getCaseTypeACLEntities().get(0));
        assertCaseTypeUserRoleEntity(fetched, cture2, fetched.getCaseTypeACLEntities().get(1));
        assertCaseTypeUserRoleEntity(fetched, cture3, fetched.getCaseTypeACLEntities().get(2));


        // Check authorisation case fields
        assertThat(caseField.getCaseFieldACLEntities().size(), equalTo(2));
        assertThat(caseField.getCaseFieldACLEntities().get(0).getUserRole().getReference(), equalTo("user role 1"));
        assertThat(caseField.getCaseFieldACLEntities().get(1).getUserRole().getReference(), equalTo("user role 2"));
        assertThat(caseField.getCaseFieldACLEntities().get(0).getCreate(), equalTo(true));
        assertThat(caseField.getCaseFieldACLEntities().get(0).getRead(), equalTo(false));
        assertThat(caseField.getCaseFieldACLEntities().get(0).getUpdate(), equalTo(false));
        assertThat(caseField.getCaseFieldACLEntities().get(0).getDelete(), equalTo(true));
        assertThat(caseField.getCaseFieldACLEntities().get(1).getCreate(), equalTo(true));
        assertThat(caseField.getCaseFieldACLEntities().get(1).getRead(), equalTo(true));
        assertThat(caseField.getCaseFieldACLEntities().get(1).getUpdate(), equalTo(false));
        assertThat(caseField.getCaseFieldACLEntities().get(1).getDelete(), equalTo(true));

        // Check authorisation case events
        final EventEntity eventEntity1 = findEventEntity(fetchedEvents, e1);
        final EventEntity eventEntity2 = findEventEntity(fetchedEvents, e2);

        final EventACLEntity eventACLEntity1 = findFetchedEventUserRoleEntity(eventEntity1, "user role 1");
        final EventACLEntity eventACLEntity2 = findFetchedEventUserRoleEntity(eventEntity1, "user role 2");
        final EventACLEntity eventACLEntity3 = findFetchedEventUserRoleEntity(eventEntity2, "user role 1");
        final EventACLEntity eventACLEntity4 = findFetchedEventUserRoleEntity(eventEntity2, "user role 3");

        assertEventUserRoleEntity(eventACLEntity1, true, false, false, true);
        assertEventUserRoleEntity(eventACLEntity2, false, false, false, true);
        assertEventUserRoleEntity(eventACLEntity3, true, true, false, true);
        assertEventUserRoleEntity(eventACLEntity4, false, true, false, true);

        versionedCaseTypeRepository.save(fetched);

        Optional<CaseTypeEntity> optionalfetched = versionedCaseTypeRepository.findById(fetched.getId());

        assertNotNull(optionalfetched.get());
        CaseTypeEntity fetchedAltered = optionalfetched.get();
        assertThat(fetchedAltered.getSecurityClassification(), equalTo(SecurityClassification.RESTRICTED));
        assertThat(fetchedAltered.getCaseFields().get(0).getSecurityClassification(), equalTo(SecurityClassification.PRIVATE));
        assertThat(fetchedAltered.getEvents().get(0).getSecurityClassification(), equalTo(SecurityClassification.PUBLIC));

    }

    private void assertEventUserRoleEntity(final EventACLEntity entity,
                                           final boolean canCreate,
                                           final boolean canRead,
                                           final boolean canUpdate,
                                           final boolean canDelete) {
        final String reasonPrefix = String.format("Case type '%s, Event '%s', User Role '%s' ",
                entity.getEvent().getCaseType().getReference(), entity.getEvent().getReference(), entity.getUserRole().getReference());
        assertThat(reasonPrefix + "can create", entity.getCreate(), is(canCreate));
        assertThat(reasonPrefix + "can read", entity.getRead(), is(canRead));
        assertThat(reasonPrefix + "can update", entity.getUpdate(), is(canUpdate));
        assertThat(reasonPrefix + "can delete", entity.getDelete(), is(canDelete));
    }

    private EventACLEntity findFetchedEventUserRoleEntity(final EventEntity eventEntity, final String userRole) {
        // @formatter:off
        return eventEntity.getEventACLEntities()
            .stream()
            .filter(r -> StringUtils.equals(userRole, r.getUserRole().getReference()))
            .findFirst()
            .get();
        // @formatter:on
    }

    private EventEntity findEventEntity(final List<EventEntity> fetchedEvents, final EventEntity eventEntity) {
        // @formatter:off
        return fetchedEvents
            .stream()
            .filter(e -> StringUtils.equals(e.getReference(), eventEntity.getReference()))
            .findFirst()
            .get();
        // @formatter:on
    }

    private void assertCaseTypeUserRoleEntity(final CaseTypeEntity caseType,
                                              final CaseTypeACLEntity expected,
                                              final CaseTypeACLEntity actual) {
        assertThat(expected.getCaseType().getReference(), is(caseType.getReference()));
        assertThat(expected.getUserRole().getReference(), is(actual.getUserRole().getReference()));
        assertThat(expected.getUserRole().getSecurityClassification(), is(actual.getUserRole().getSecurityClassification()));
    }

    private void assertStateUserRoleEntity(final StateEntity stateEntity,
                                              final StateACLEntity expected,
                                              final StateACLEntity actual) {
        assertThat(expected.getStateEntity().getReference(), is(stateEntity.getReference()));
        assertThat(expected.getUserRole().getReference(), is(actual.getUserRole().getReference()));
        assertThat(expected.getUserRole().getSecurityClassification(), is(actual.getUserRole().getSecurityClassification()));
    }

    private void setAuthorisationData(final Authorisation entity,
                                      final UserRoleEntity userRole,
                                      final Boolean canCreate,
                                      final Boolean canRead,
                                      final Boolean canUpdate,
                                      final Boolean canDelete) {
        entity.setUserRole(userRole);
        entity.setCreate(canCreate);
        entity.setRead(canRead);
        entity.setUpdate(canUpdate);
        entity.setDelete(canDelete);
    }

    private CaseTypeACLEntity createCaseTypeUserRoleEntity(final UserRoleEntity userRole,
                                                           final Boolean canCreate,
                                                           final Boolean canRead,
                                                           final Boolean canUpdate,
                                                           final Boolean canDelete) {
        final CaseTypeACLEntity entity = new CaseTypeACLEntity();
        setAuthorisationData(entity, userRole, canCreate, canRead, canUpdate, canDelete);
        return entity;
    }

    private CaseFieldACLEntity createCaseFieldUserRoleEntity(final UserRoleEntity userRole,
                                                             final Boolean canCreate,
                                                             final Boolean canRead,
                                                             final Boolean canUpdate,
                                                             final Boolean canDelete) {
        final CaseFieldACLEntity entity = new CaseFieldACLEntity();
        setAuthorisationData(entity, userRole, canCreate, canRead, canUpdate, canDelete);
        return entity;
    }

    private EventACLEntity createEventUserRoleEntity(final UserRoleEntity role,
                                                     final Boolean canCreate,
                                                     final Boolean canRead,
                                                     final Boolean canUpdate,
                                                     final Boolean canDelete) {
        EventACLEntity e = new EventACLEntity();
        e.setUserRole(role);
        e.setCreate(canCreate);
        e.setRead(canRead);
        e.setUpdate(canUpdate);
        e.setDelete(canDelete);
        return e;
    }

    private StateACLEntity createStateUserRoleEntity(final UserRoleEntity userRole,
                                                     final Boolean canCreate,
                                                     final Boolean canRead,
                                                     final Boolean canUpdate,
                                                     final Boolean canDelete) {
        final StateACLEntity entity = new StateACLEntity();
        setAuthorisationData(entity, userRole, canCreate, canRead, canUpdate, canDelete);
        return entity;
    }

    private WebhookEntity createWebHook(final String url, final Integer... timeouts) {
        final WebhookEntity webhook = new WebhookEntity();
        webhook.setUrl(url);
        webhook.setTimeouts(Lists.newArrayList(timeouts));
        return webhook;
    }

    private EventEntity createEvent(final String reference, final String name, final Integer order, final SecurityClassification sc) {
        final EventEntity event = new EventEntity();
        event.setReference(reference);
        event.setName(name);
        event.setOrder(order);
        event.setSecurityClassification(sc);
        return event;
    }

    private StateEntity createState(final String id,
                                    final String name,
                                    final String description,
                                    final Integer order,
                                    final LocalDate liveFrom,
                                    final LocalDate liveTo) {
        final StateEntity state = new StateEntity();
        state.setReference(id);
        state.setName(name);
        state.setDescription(description);
        state.setOrder(order);
        state.setLiveFrom(liveFrom);
        state.setLiveTo(liveTo);
        return state;
    }
}
