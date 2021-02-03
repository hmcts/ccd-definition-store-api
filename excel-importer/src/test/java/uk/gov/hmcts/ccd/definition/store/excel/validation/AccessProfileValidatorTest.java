package uk.gov.hmcts.ccd.definition.store.excel.validation;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationException;
import uk.gov.hmcts.ccd.definition.store.excel.parser.ParseContext;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.RoleToAccessProfileEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.UserRoleEntity;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AccessProfileValidatorTest {

    private static final String CASE_TYPE_ID_1 = "TestCaseTypeID_1";
    private static final String CASE_TYPE_ID_2 = "TestCaseTypeID_2";

    private AccessProfileValidator validator;
    private ParseContext  parseContext;
    private CaseTypeEntity caseTypeEntity1;
    private CaseTypeEntity caseTypeEntity2;

    @BeforeEach
    void setUp() {
        parseContext = mock(ParseContext.class);
        validator = new AccessProfileValidator();

        caseTypeEntity1 = mock(CaseTypeEntity.class);
        when(caseTypeEntity1.getReference()).thenReturn(CASE_TYPE_ID_1);
        caseTypeEntity2 = mock(CaseTypeEntity.class);
        when(caseTypeEntity2.getReference()).thenReturn(CASE_TYPE_ID_2);
        Set<CaseTypeEntity> caseTypes = Sets.newHashSet(caseTypeEntity1, caseTypeEntity2);
        given(parseContext.getCaseTypes()).willReturn(caseTypes);
        UserRoleEntity userRoleEntity = mock(UserRoleEntity.class);
        when(parseContext.getRole(anyString(), anyString())).thenReturn(Optional.of(userRoleEntity));
    }


    @Test
    void shouldThrowExceptionWhenAccessProfileNotFound() {
        when(parseContext.getRole(anyString(), anyString())).thenReturn(Optional.empty());
        Assertions.assertThrows(ValidationException.class, () -> validator.validate(createEntities(), parseContext));
    }


    @Test
    void shouldNotThrowExceptionWhenAccessProfileFound() {
        UserRoleEntity userRoleEntity = mock(UserRoleEntity.class);
        when(parseContext.getRole(anyString(), anyString())).thenReturn(Optional.of(userRoleEntity));
        validator.validate(createEntities(), parseContext);
    }

    private List<RoleToAccessProfileEntity> createEntities() {
        RoleToAccessProfileEntity entity1 = mock(RoleToAccessProfileEntity.class);
        when(entity1.getAccessProfiles()).thenReturn("caseworker,citizen");
        when(entity1.getCaseType()).thenReturn(caseTypeEntity1);

        RoleToAccessProfileEntity entity2 = mock(RoleToAccessProfileEntity.class);
        when(entity2.getAccessProfiles()).thenReturn("caseworker");
        when(entity2.getCaseType()).thenReturn(caseTypeEntity2);

        return Lists.newArrayList(entity1, entity2);
    }

}
