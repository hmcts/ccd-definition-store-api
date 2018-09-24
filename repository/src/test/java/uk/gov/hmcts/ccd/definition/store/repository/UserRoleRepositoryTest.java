package uk.gov.hmcts.ccd.definition.store.repository;

import javax.persistence.EntityManager;
import javax.sql.DataSource;
import java.util.Optional;

import static com.github.npathai.hamcrestopt.OptionalMatchers.isEmpty;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.ccd.definition.store.repository.entity.UserRoleEntity;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = {
    SanityCheckApplication.class,
    TestConfiguration.class
})
@TestPropertySource(locations = "classpath:test.properties")
@Transactional
public class UserRoleRepositoryTest {

    private static final Logger LOG = LoggerFactory.getLogger(UserRoleRepositoryTest.class);

    @Autowired
    private UserRoleRepository repository;

    @Autowired
    private DataSource ds;

    private JdbcTemplate jdbcTemplate;

    @Autowired
    private EntityManager entityManager;

    @Before
    public void setup() {
        jdbcTemplate = new JdbcTemplate(ds);
        jdbcTemplate.update("insert into user_role (role, security_classification) values (?, 'PUBLIC')",
            "xyz = '3'");
    }

    @Test
    public void shouldFindRole() {
        final UserRoleEntity role = repository.findTopByReference("xyz = '3'").get();
        assertThat(role.getId(), is(notNullValue()));
        assertThat(role.getCreatedAt(), is(notNullValue()));
//        assertThat(role.getLiveTo(), is(nullValue()));
//        assertThat(role.getLiveFrom(), is(nullValue()));
        assertThat(role.getReference(), is("xyz = '3'"));
        assertThat(role.getSecurityClassification(), is(SecurityClassification.PUBLIC));
    }

    @Test
    public void shouldFindNoEntity() {
        final Optional<UserRoleEntity> role = repository.findTopByReference("xyz = 3");
        assertThat(role, isEmpty());
    }

    @Test
    public void shouldCreateRole() {
        final String role = "xyz = '3";
        final UserRoleEntity entity = new UserRoleEntity();
        entity.setReference(role);
        entity.setSecurityClassification(SecurityClassification.RESTRICTED);
        repository.save(entity);

        entityManager.flush();
        entityManager.clear();

        final UserRoleEntity afterSave = repository.findTopByReference(role).get();
        assertThat(afterSave.getReference(), is(role));
        assertThat(afterSave.getSecurityClassification(), is(SecurityClassification.RESTRICTED));
//        assertThat(afterSave.getJpaOptimisticLock(), is(notNullValue()));
    }

    @Test(expected = DataIntegrityViolationException.class)
    public void shouldFail_whenCreateDuplicateRoles() {
        final UserRoleEntity entity = new UserRoleEntity();
        entity.setReference("xyz = '3'");
        entity.setSecurityClassification(SecurityClassification.RESTRICTED);
        repository.save(entity);
    }

//    @Test
//    public void shouldUpdateLock_whenUpdate() {
//        final UserRoleEntity role = repository.findTopByReference("xyz = '3'").get();
//        final long versionBeforeSave = role.getJpaOptimisticLock();
//        role.setSecurityClassification(SecurityClassification.RESTRICTED);
//        repository.save(role);
//
//        entityManager.flush();
//        entityManager.clear();
//
//        final UserRoleEntity afterSave = repository.findTopByReference("xyz = '3'").get();
//        assertThat(afterSave.getJpaOptimisticLock(), greaterThan(versionBeforeSave));
//        assertThat(afterSave.getSecurityClassification(), is(SecurityClassification.RESTRICTED));
//    }
//
//    @Test(expected = ObjectOptimisticLockingFailureException.class)
//    public void shouldFail_whenCreatedObjectIsUpdatedAgain() {
//        final String role = "xyz = expectUpdateFail";
//        final UserRoleEntity entity = new UserRoleEntity();
//        entity.setRole(role);
//        entity.setSecurityClassification(SecurityClassification.RESTRICTED);
//
//        assertThat("Before save", entity.getJpaOptimisticLock(), is(nullValue()));
//        assertThat("Before save", entity.getId(), is(nullValue()));
//
//        repository.save(entity);
//
//        assertThat("After save", entity.getJpaOptimisticLock(), is(0L));
//        assertThat("After save", entity.getJpaOptimisticLock(), is(notNullValue()));
//
//        entityManager.flush();
//        entityManager.clear();
//
//        final UserRoleEntity fetched = repository.findTopByReference(role).get();
//        assertThat(fetched.getRole(), is(role));
//        assertThat(fetched.getSecurityClassification(), is(SecurityClassification.RESTRICTED));
//        assertThat(fetched.getJpaOptimisticLock(), is(0L));
//
//        fetched.setSecurityClassification(SecurityClassification.PUBLIC);
//        repository.save(fetched);
//
//        entityManager.flush();
//        entityManager.clear();
//
//        assertThat("After 2nd save", entity.getJpaOptimisticLock(), is(0L));
//        assertThat("After 2nd save", fetched.getJpaOptimisticLock(), is(1L));
//
//        entity.setSecurityClassification(SecurityClassification.PRIVATE);
//
//        try {
//            repository.save(entity);
//        } catch (ObjectOptimisticLockingFailureException ex) {
//            LOG.info("Exception expected {}", ex.getMessage(), ex);
//            throw ex;
//        }
//
//        fail("Did not expect entity can be saved again");
//    }
//
//    @Test(expected = ObjectOptimisticLockingFailureException.class)
//    public void shouldFail_whenFectedObjectIsUpdatedAfterAnotherFind() {
//        final String role = "xyz = expectUpdateFail";
//        final UserRoleEntity entity = new UserRoleEntity();
//        entity.setRole(role);
//        entity.setSecurityClassification(SecurityClassification.RESTRICTED);
//
//        repository.save(entity);
//
//        entityManager.flush();
//        entityManager.clear();
//
//        final UserRoleEntity fetched = repository.findTopByReference(role).get();
//
//        assertThat(fetched.getJpaOptimisticLock(), is(0L));
//
//        fetched.setSecurityClassification(SecurityClassification.PUBLIC);
//        repository.save(fetched);
//
//        entityManager.flush();
//        entityManager.clear();
//
//        assertThat("After save", entity.getJpaOptimisticLock(), is(0L));
//        assertThat("After save", fetched.getJpaOptimisticLock(), is(1L));
//
//        fetched.setSecurityClassification(SecurityClassification.PRIVATE);
//
//        repository.save(fetched);
//
//        entityManager.flush();
//        entityManager.clear();
//
//        assertThat("After 2nd save", entity.getJpaOptimisticLock(), is(0L));
//        assertThat("After 2nd save", fetched.getJpaOptimisticLock(), is(1L));
//
//        final UserRoleEntity fetchedAgain = repository.findTopByReference(role).get();
//        assertThat("After repository find", fetchedAgain.getJpaOptimisticLock(), is(2L));
//        assertThat("After repository find", fetched.getJpaOptimisticLock(), is(1L));
//        assertThat("After repository find", entity.getJpaOptimisticLock(), is(0L));
//
//        fetched.setSecurityClassification(SecurityClassification.PUBLIC);
//
//        try {
//            repository.save(fetched);
//        } catch (ObjectOptimisticLockingFailureException ex) {
//            LOG.info("Exception expected {}", ex.getMessage(), ex);
//            throw ex;
//        }
//
//        fail("Did not expect entity can be saved again");
//    }
}
