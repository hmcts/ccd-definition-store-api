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
        jdbcTemplate.update("delete from event_user_role");
        jdbcTemplate.update("delete from case_type_user_role");
        jdbcTemplate.update("delete from case_field_user_role");
        jdbcTemplate.update("delete from state_user_role");
        jdbcTemplate.update("delete from user_role");
        jdbcTemplate.update("insert into user_role(role, security_classification) values ('CaseWorker1', 'PUBLIC')");
        jdbcTemplate.update("insert into user_role(role, security_classification) values ('CaseWorker2', 'PRIVATE')");
        jdbcTemplate.update("insert into user_role(role, security_classification) values ('CaseWorker3', 'RESTRICTED')");

        final List<Map<String, Object>> list = jdbcTemplate.queryForList("select * from user_role");
        return list
            .stream()
            .collect(Collectors.toMap(l -> (String)l.get("role"), l -> (Integer)l.get("id")));
    }

}
