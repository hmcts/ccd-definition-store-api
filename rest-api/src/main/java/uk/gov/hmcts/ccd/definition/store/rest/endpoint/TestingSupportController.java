package uk.gov.hmcts.ccd.definition.store.rest.endpoint;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import uk.gov.hmcts.ccd.definition.store.domain.exception.NotFoundException;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


@RestController
@Api(value = "/api/testing-support")
@RequestMapping(value = "/api/testing-support")
@Slf4j
public class TestingSupportController {

    private final SessionFactory sessionFactory;

    @Autowired
    public TestingSupportController(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @DeleteMapping(value = "/cleanup-case-type/{changeId}")
    @ApiOperation(value = "Delete a list of Case Type Schemas", notes = "Blank body response.\n")
    @ApiResponses(value = {
        @ApiResponse(code = 204, message = "Success"),
        @ApiResponse(code = 404, message = "Unable to find case type"),
        @ApiResponse(code = 500, message = "Unexpected error")
    })
    @ConditionalOnExpression("${testing-support-endpoints.enabled:false}")
    public void dataCaseTypeIdDelete(
        @ApiParam(value = "Change ID", required = true) @PathVariable("changeId") BigInteger changeId,
        @ApiParam(value = "Case Type ID", required = true) @RequestParam("caseTypeIds") String caseTypeIds) {

        log.info("Invoked for changeId {} and caseTypeIds {} ", changeId, caseTypeIds);

        var caseIdList = Arrays.stream(caseTypeIds.split(",")).toList();
        var caseTypesWithChangeIds = caseIdList.stream().map(caseTypeId -> caseTypeId + "-" + changeId).toList();

        Session session = sessionFactory.openSession();
        session.beginTransaction();

        var ids = getCaseTypeIdsByReferences(session, caseTypesWithChangeIds);
        if (ids.isEmpty()) {
            throw new NotFoundException("Unable to find case type");
        }

        var sql = getDeleteSql();

        sql.forEach(sqlStatement -> executeSql(session, sqlStatement, ids));

        session.close();

        log.info("Deleted records for changeId {} and caseTypeIds {} ", changeId, caseTypeIds);
    }



    private List<Integer> getCaseTypeIdsByReferences(Session session, List<String> caseTypesWithChangeIds) {
        var ids = session.createNativeQuery(
                "SELECT id FROM case_type WHERE reference IN ( :caseTypesWithChangeIds );",
                Integer.class)
            .setParameterList("caseTypesWithChangeIds", caseTypesWithChangeIds)
            .list();
        session.getTransaction().commit();
        List<Integer> intIds = new ArrayList<>();
        for (Object s : ids) {
            intIds.add(Integer.valueOf(s.toString()));
        }
        return intIds;
    }



    @DeleteMapping(value = "/cleanup-case-type/{caseTypeId}")
    @ApiOperation(value = "Delete a list of Case Type Schemas", notes = "Blank body response.\n")
    @ApiResponses(value = {
        @ApiResponse(code = 204, message = "Success"),
        @ApiResponse(code = 404, message = "Unable to find case type"),
        @ApiResponse(code = 500, message = "Unexpected error")
    })
    @ConditionalOnExpression("${testing-support-endpoints.enabled:false}")
    public void dataCaseTypeIdDeleteOnlyWithCaseTypeIds(

        @ApiParam(value = "Case Type ID", required = true) @RequestParam("caseTypeIds") String caseTypeIds) {

        log.info("Invoked for caseTypeIds {} ", caseTypeIds);

        var caseIdList = Arrays.stream(caseTypeIds.split(",")).toList();
        var caseTypesWithChangeIds = caseIdList.stream().map(caseTypeId -> caseTypeId).toList();

        Session session = sessionFactory.openSession();
        session.beginTransaction();

        var ids = getCaseTypeIdsByReferencesForCaseTypeIds(session, caseTypesWithChangeIds);
        if (ids.isEmpty()) {
            throw new NotFoundException("Unable to find case type");
        }

        var sql = getDeleteSql();

        sql.forEach(sqlStatement -> executeSql(session, sqlStatement, ids));

        session.close();

        log.info("Deleted records for caseTypeIds {} ", caseTypeIds);
    }



    private List<Integer> getCaseTypeIdsByReferencesForCaseTypeIds(Session session, List<String> caseTypesWithCaseTypeIds) {
        var ids = session.createNativeQuery(
                "SELECT id FROM case_type WHERE reference IN ( :caseTypesWithCaseTypeIds );",
                Integer.class)
            .setParameterList("caseTypesWithChangeIds", caseTypesWithCaseTypeIds)
            .list();
        session.getTransaction().commit();
        List<Integer> intIds = new ArrayList<>();
        for (Object s : ids) {
            intIds.add(Integer.valueOf(s.toString()));
        }
        return intIds;
    }



        private ArrayList<String> getDeleteSql () {
            return new ArrayList<>(
                Arrays.asList(
                    """
                        DELETE FROM event_case_field_complex_type
                        WHERE  event_case_field_id IN (SELECT id
                                                       FROM   event_case_field
                                                       WHERE  event_id IN (SELECT id
                                                                           FROM   event
                                                                           WHERE
                                                              case_type_id IN ( :caseTypeIds ) )
                                                             )
                        """,
                    """
                        DELETE FROM event_case_field
                        WHERE  event_id IN (SELECT id
                                            FROM   event
                                            WHERE  case_type_id IN ( :caseTypeIds ) )
                        """,
                    """
                        DELETE FROM challenge_question
                        WHERE  case_type_id IN ( :caseTypeIds )
                        """,
                    """
                        DELETE FROM display_group_case_field
                        WHERE  display_group_id IN (SELECT id
                            FROM   display_group
                            WHERE  case_type_id IN ( :caseTypeIds ) )
                        """,
                    """
                        DELETE FROM case_field_acl
                        WHERE  case_field_id IN (SELECT id
                            FROM   case_field
                            WHERE  case_type_id IN ( :caseTypeIds ) )
                        """,
                    """
                        DELETE FROM workbasket_case_field
                        WHERE  case_type_id IN ( :caseTypeIds )
                        """,
                    """
                        DELETE FROM workbasket_input_case_field
                        WHERE  case_type_id IN ( :caseTypeIds )
                        """,
                    """
                        DELETE FROM search_alias_field
                        WHERE  case_type_id IN ( :caseTypeIds )
                        """,
                    """
                        DELETE FROM search_result_case_field
                        WHERE  case_type_id IN ( :caseTypeIds )
                        """,
                    """
                        DELETE FROM search_input_case_field
                        WHERE  case_type_id IN ( :caseTypeIds )
                        """,
                    """
                        DELETE FROM search_cases_result_fields
                        WHERE  case_type_id IN ( :caseTypeIds )
                        """,
                    """
                        DELETE FROM complex_field_acl
                        WHERE  case_field_id IN (SELECT id
                            FROM   case_field
                            WHERE  case_type_id IN ( :caseTypeIds ) )
                        """,
                    """
                        DELETE FROM case_field
                        WHERE  case_type_id IN ( :caseTypeIds )
                        """,
                    """
                        DELETE FROM display_group
                        WHERE  case_type_id IN ( :caseTypeIds )
                        """,
                    """
                        DELETE FROM event_webhook
                        WHERE  event_id IN (SELECT id
                            FROM   event
                            WHERE  case_type_id IN ( :caseTypeIds ) )
                        """,
                    """
                        DELETE FROM event_pre_state
                        WHERE  event_id IN (SELECT id
                            FROM   event
                            WHERE  case_type_id IN ( :caseTypeIds ) )
                        """,
                    """
                        DELETE FROM event_acl
                        WHERE  event_id IN (SELECT id
                            FROM   event
                            WHERE  case_type_id IN ( :caseTypeIds ) )
                        """,
                    """
                        DELETE FROM event_post_state
                        WHERE  case_event_id IN (SELECT id
                            FROM   event
                            WHERE  case_type_id IN ( :caseTypeIds ) )
                        """,
                    """
                        DELETE FROM state_acl
                        WHERE  state_id IN (SELECT id
                            FROM   state
                            WHERE  case_type_id IN ( :caseTypeIds ) )
                        """,
                    """
                        DELETE FROM state
                        WHERE  case_type_id IN ( :caseTypeIds )
                        """,
                    """
                        DELETE FROM case_type_acl
                        WHERE  case_type_id IN ( :caseTypeIds )
                        """,
                    """
                        DELETE FROM ROLE
                        WHERE  case_type_id IN ( :caseTypeIds )
                        """,
                    """
                        DELETE FROM role_to_access_profiles
                        WHERE  case_type_id IN ( :caseTypeIds )
                        """,
                    """
                        DELETE FROM search_criteria
                        WHERE  case_type_id IN ( :caseTypeIds )
                        """,
                    """
                        DELETE FROM search_party
                        WHERE  case_type_id IN ( :caseTypeIds )
                        """,
                    """
                        DELETE FROM category
                        WHERE  case_type_id IN ( :caseTypeIds )
                        """,
                    """
                        DELETE FROM event
                        WHERE  case_type_id IN ( :caseTypeIds )
                        """,
                    """
                        DELETE FROM case_type
                        WHERE  id IN ( :caseTypeIds )
                        """
                )
            );
        }

        private void executeSql (Session session, String sql, List < Integer > ids){
            session.beginTransaction();
            session.createNativeMutationQuery(sql)
                .setParameterList("caseTypeIds", ids)
                .executeUpdate();
            session.getTransaction().commit();
        }
    }

