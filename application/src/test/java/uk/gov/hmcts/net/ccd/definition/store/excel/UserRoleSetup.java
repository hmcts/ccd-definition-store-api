package uk.gov.hmcts.net.ccd.definition.store.excel;

import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class UserRoleSetup {

    public final JdbcTemplate jdbcTemplate;

    public UserRoleSetup(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Map<String, Integer> addUserRoleTestData() {
        jdbcTemplate.update("delete from event_acl");
        jdbcTemplate.update("delete from case_type_acl");
        jdbcTemplate.update("delete from case_field_acl");
        jdbcTemplate.update("delete from complex_field_acl");
        jdbcTemplate.update("delete from state_acl");
        jdbcTemplate.update("update display_group set role_id = null where role_id is not null");
        jdbcTemplate.update("update search_input_case_field set role_id = null where role_id is not null");
        jdbcTemplate.update("update search_result_case_field set role_id = null where role_id is not null");
        jdbcTemplate.update("update workbasket_input_case_field set role_id = null where role_id is not null");
        jdbcTemplate.update("update workbasket_case_field set role_id = null where role_id is not null");
        jdbcTemplate.update("delete from role");
        jdbcTemplate.update("insert into role(reference, name, security_classification, dtype) "
            + "values ('CaseWorker1', 'CaseWorker1', 'PUBLIC', 'USERROLE')");
        jdbcTemplate.update("insert into role(reference, name, security_classification, dtype) "
            + "values ('CaseWorker2', 'CaseWorker2', 'PRIVATE', 'USERROLE')");
        jdbcTemplate.update("insert into role(reference, name, security_classification, dtype) "
            + "values ('CaseWorker3', 'CaseWorker3', 'RESTRICTED', 'USERROLE')");

        final List<Map<String, Object>> list = jdbcTemplate.queryForList("select * from role");
        return list
            .stream()
            .collect(Collectors.toMap(l -> (String) l.get("reference"), l -> (Integer) l.get("id")));
    }

}
