package uk.gov.hmcts.net.ccd.definition.store.excel;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.jdbc.core.JdbcTemplate;

public class UserRoleSetup {

    public final JdbcTemplate jdbcTemplate;

    public UserRoleSetup(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Map<String, Integer> addUserRoleTestData() {
        jdbcTemplate.update("delete from event_acl");
        jdbcTemplate.update("delete from case_type_acl");
        jdbcTemplate.update("delete from case_field_acl");
        jdbcTemplate.update("delete from state_acl");
        jdbcTemplate.update("delete from role");
        jdbcTemplate.update("insert into role(reference, name, security_classification, dtype) "
            + "values ('CaseWorker1', 'CaseWorker1', 'PUBLIC', 'USERROLE')");
        jdbcTemplate.update("insert into role(reference, name, security_classification, dtype) "
            + "values ('CaseWorker2', 'CaseWorker2', 'PRIVATE', 'USERROLE')");
        jdbcTemplate.update("insert into role(reference, name, security_classification, dtype) "
            + "values ('CaseWorker3', 'CaseWorker3', 'RESTRICTED', 'USERROLE')");

        final List<Map<String, Object>> list = jdbcTemplate.queryForList("select * from user_role");
        return list
            .stream()
            .collect(Collectors.toMap(l -> (String) l.get("role"), l -> (Integer) l.get("id")));
    }

}
