package uk.gov.hmcts.ccd.definition.store.repository;

import jakarta.persistence.EntityManager;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.ccd.definition.store.repository.entity.GenericLayoutEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.InputCaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.SearchCasesResultFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.SearchInputCaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.SearchResultCaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.SortOrder;
import uk.gov.hmcts.ccd.definition.store.repository.entity.WorkBasketCaseFieldEntity;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

@Repository
public class GenericLayoutRepositoryImpl {

    private static final String SEARCH_INPUT_INSERT = """
        INSERT INTO search_input_case_field
        (live_from, live_to, case_type_id, case_field_id, label, display_order,
         case_field_element_path, role_id, show_condition, display_context_parameter)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;
    private static final String WORKBASKET_INPUT_INSERT = """
        INSERT INTO workbasket_input_case_field
        (live_from, live_to, case_type_id, case_field_id, label, display_order,
         case_field_element_path, role_id, show_condition, display_context_parameter)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;
    private static final String SEARCH_RESULT_INSERT = """
        INSERT INTO search_result_case_field
        (live_from, live_to, case_type_id, case_field_id, label, display_order,
         case_field_element_path, role_id, sort_order_direction, sort_order_priority,
         display_context_parameter)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;
    private static final String WORKBASKET_RESULT_INSERT = """
        INSERT INTO workbasket_case_field
        (live_from, live_to, case_type_id, case_field_id, label, display_order,
         case_field_element_path, role_id, sort_order_direction, sort_order_priority,
         display_context_parameter)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;
    private static final String SEARCH_CASES_RESULT_INSERT = """
        INSERT INTO search_cases_result_fields
        (live_from, live_to, case_type_id, case_field_id, label, display_order,
         case_field_element_path, role_id, sort_order_direction, sort_order_priority,
         display_context_parameter, use_case)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

    private final JdbcTemplate jdbcTemplate;
    private final EntityManager entityManager;

    public GenericLayoutRepositoryImpl(JdbcTemplate jdbcTemplate, EntityManager entityManager) {
        this.jdbcTemplate = jdbcTemplate;
        this.entityManager = entityManager;
    }

    public void insertAll(List<GenericLayoutEntity> layouts) {
        if (layouts.isEmpty()) {
            return;
        }

        entityManager.flush();
        GenericLayoutEntity first = layouts.getFirst();
        if (first instanceof SearchInputCaseFieldEntity) {
            batchInsert(SEARCH_INPUT_INSERT, layouts, this::setInputValues);
        } else if (first instanceof InputCaseFieldEntity) {
            batchInsert(WORKBASKET_INPUT_INSERT, layouts, this::setInputValues);
        } else if (first instanceof SearchCasesResultFieldEntity) {
            batchInsert(SEARCH_CASES_RESULT_INSERT, layouts, this::setSearchCasesResultValues);
        } else if (first instanceof SearchResultCaseFieldEntity) {
            batchInsert(SEARCH_RESULT_INSERT, layouts, this::setResultValues);
        } else {
            batchInsert(WORKBASKET_RESULT_INSERT, layouts, this::setResultValues);
        }
    }

    private void batchInsert(String sql, List<GenericLayoutEntity> layouts, LayoutStatementSetter setter) {
        jdbcTemplate.batchUpdate(sql, layouts, 100, setter::setValues);
    }

    private void setInputValues(PreparedStatement statement, GenericLayoutEntity layout) throws SQLException {
        setCommonValues(statement, layout);
        statement.setString(9, ((InputCaseFieldEntity) layout).getShowCondition());
        statement.setString(10, layout.getDisplayContextParameter());
    }

    private void setResultValues(PreparedStatement statement, GenericLayoutEntity layout) throws SQLException {
        setCommonValues(statement, layout);
        SortOrder sortOrder = resultSortOrder(layout);
        statement.setString(9, sortOrder == null ? null : sortOrder.getDirection());
        if (sortOrder == null || sortOrder.getPriority() == null) {
            statement.setObject(10, null);
        } else {
            statement.setInt(10, sortOrder.getPriority());
        }
        statement.setString(11, layout.getDisplayContextParameter());
    }

    private void setSearchCasesResultValues(PreparedStatement statement, GenericLayoutEntity layout)
        throws SQLException {
        setResultValues(statement, layout);
        statement.setString(12, ((SearchCasesResultFieldEntity) layout).getUseCase());
    }

    private void setCommonValues(PreparedStatement statement, GenericLayoutEntity layout) throws SQLException {
        statement.setDate(1, layout.getLiveFrom() == null ? null : Date.valueOf(layout.getLiveFrom()));
        statement.setDate(2, layout.getLiveTo() == null ? null : Date.valueOf(layout.getLiveTo()));
        statement.setInt(3, layout.getCaseType().getId());
        statement.setInt(4, layout.getCaseField().getId());
        statement.setString(5, layout.getLabel());
        statement.setObject(6, layout.getOrder());
        statement.setString(7, layout.getCaseFieldElementPath());
        statement.setObject(8, layout.getAccessProfile() == null ? null : layout.getAccessProfile().getId());
    }

    private SortOrder resultSortOrder(GenericLayoutEntity layout) {
        if (layout instanceof SearchResultCaseFieldEntity searchResult) {
            return searchResult.getSortOrder();
        }
        if (layout instanceof SearchCasesResultFieldEntity searchCasesResult) {
            return searchCasesResult.getSortOrder();
        }
        return ((WorkBasketCaseFieldEntity) layout).getSortOrder();
    }

    @FunctionalInterface
    private interface LayoutStatementSetter {
        void setValues(PreparedStatement statement, GenericLayoutEntity layout) throws SQLException;
    }
}
