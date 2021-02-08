package uk.gov.hmcts.ccd.definition.store.repository.accessprofile;


import java.util.List;
import java.util.Set;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.repository.UserRoleRepository;

@Component
public class GetCaseTypeRolesRepository {

    private static final String GET_CASE_TYPE_ROLE_IDS = "select role_id from case_field_acl WHERE case_field_id "
        + "IN (select id from case_field where case_type_id = :caseType) "
        + "UNION "
        + "select role_id from case_type_acl WHERE case_type_id = :caseType "
        + "UNION "
        + "select role_id from complex_field_acl WHERE case_field_id IN (select id from case_field where case_type_id = :caseType) "
        + "UNION "
        + "select role_id from event_acl WHERE event_id in (select id from event where case_type_id = :caseType) "
        + "UNION "
        + "select role_id from state_acl WHERE state_id in (select id from state where case_type_id = :caseType)";

    private final UserRoleRepository userRoleRepository;

    @PersistenceContext
    private EntityManager em;

    @Autowired
    public GetCaseTypeRolesRepository(UserRoleRepository userRoleRepository) {
        this.userRoleRepository = userRoleRepository;
    }

    public Set<String> findCaseTypeRoles(Integer caseTypeId) {
        List<Integer> roleIds = findCaseTypeRoleIds(caseTypeId);
        return userRoleRepository.findAllReferenceById(roleIds);
    }

    private List<Integer> findCaseTypeRoleIds(Integer caseTypeId) {
        Query query = em.createNativeQuery(GET_CASE_TYPE_ROLE_IDS);
        query.setParameter("caseType", caseTypeId);
        return query.getResultList();
    }
}
