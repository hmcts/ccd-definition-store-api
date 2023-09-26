package uk.gov.hmcts.ccd.definition.store.rest.endpoint;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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


@RestController
@Api(value = "/api/testing-support")
@RequestMapping(value = "/api/testing-support")
public class TestingSupportController {

    private final SessionFactory sessionFactory;

    @Autowired
    public TestingSupportController(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    private static final Logger LOG = LoggerFactory.getLogger(TestingSupportController.class);

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

        var caseIdList = Arrays.stream(caseTypeIds.split(",")).toList();
        var caseTypesWithChangeIds = caseIdList.stream().map(caseTypeId -> caseTypeId + "-" + changeId).toList();

        Session session = sessionFactory.openSession();
        session.beginTransaction();

        var ids = session.createNativeQuery("SELECT id FROM case_type WHERE reference IN ( :caseTypesWithChangeIds );")
            .setParameterList("caseTypesWithChangeIds", caseTypesWithChangeIds)
            .list();
        session.getTransaction().commit();
        if (ids.size() != caseIdList.size()) {
            throw new NotFoundException("Unable to find case type");
        }

        ArrayList<String> sql = new ArrayList<String>(
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

        for (String sqlStatement : sql) {
            session.beginTransaction();
            session.createNativeQuery(sqlStatement)
                .setParameterList("caseTypeIds", ids, org.hibernate.type.IntegerType.INSTANCE)
                .executeUpdate();
            session.getTransaction().commit();
        }

        session.close();
    }
}
