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
import uk.gov.hmcts.ccd.definition.store.repository.entity.RoleToAccessProfilesEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.UserRoleEntity;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RoleToAccessProfilesValidatorTest {

    private static final String CASE_TYPE_ID_1 = "TestCaseTypeID_1";
    private static final String CASE_TYPE_ID_2 = "TestCaseTypeID_2";

    private RoleToAccessProfilesValidator validator;
    private ParseContext  parseContext;
    private CaseTypeEntity caseTypeEntity1;
    private CaseTypeEntity caseTypeEntity2;

    @BeforeEach
    void setUp() {
        parseContext = mock(ParseContext.class);
        validator = new RoleToAccessProfilesValidator();

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

    @Test
    void shouldThrowExceptionOnDuplicateRoleNameAndCaseType() {
        UserRoleEntity userRoleEntity = mock(UserRoleEntity.class);
        when(parseContext.getRole(anyString(), anyString())).thenReturn(Optional.of(userRoleEntity));
        Assertions.assertThrows(ValidationException.class, () -> validator
            .validate(createDuplicateRoleNameAndCaseTypeEntities(), parseContext));
    }

    @Test
    void shouldThrowExceptionOnEmptyRoleName() {
        UserRoleEntity userRoleEntity = mock(UserRoleEntity.class);
        when(parseContext.getRole(anyString(), anyString())).thenReturn(Optional.of(userRoleEntity));
        Assertions.assertThrows(ValidationException.class, () -> validator
            .validate(createEntityWithEmptyRoleName(), parseContext));
    }

    private List<RoleToAccessProfilesEntity> createEntities() {
        RoleToAccessProfilesEntity entity1 = mock(RoleToAccessProfilesEntity.class);
        when(entity1.getAccessProfiles()).thenReturn("caseworker,citizen");
        when(entity1.getCaseType()).thenReturn(caseTypeEntity1);
        when(entity1.getRoleName()).thenReturn("Role1");

        RoleToAccessProfilesEntity entity2 = mock(RoleToAccessProfilesEntity.class);
        when(entity2.getAccessProfiles()).thenReturn("caseworker");
        when(entity2.getCaseType()).thenReturn(caseTypeEntity2);
        when(entity2.getRoleName()).thenReturn("Role2");

        return Lists.newArrayList(entity1, entity2);
    }

    private List<RoleToAccessProfilesEntity> createDuplicateRoleNameAndCaseTypeEntities() {
        RoleToAccessProfilesEntity entity1 = mock(RoleToAccessProfilesEntity.class);
        when(entity1.getAccessProfiles()).thenReturn("caseworker,citizen");
        when(entity1.getCaseType()).thenReturn(caseTypeEntity1);
        when(entity1.getRoleName()).thenReturn("Role1");

        RoleToAccessProfilesEntity entity2 = mock(RoleToAccessProfilesEntity.class);
        when(entity2.getAccessProfiles()).thenReturn("caseworker");
        when(entity2.getCaseType()).thenReturn(caseTypeEntity1);
        when(entity2.getRoleName()).thenReturn("Role1");

        return Lists.newArrayList(entity1, entity2);
    }

    private List<RoleToAccessProfilesEntity> createEntityWithEmptyRoleName() {
        RoleToAccessProfilesEntity entity1 = mock(RoleToAccessProfilesEntity.class);
        when(entity1.getAccessProfiles()).thenReturn("caseworker,citizen");
        when(entity1.getCaseType()).thenReturn(caseTypeEntity1);
        when(entity1.getRoleName()).thenReturn("");

        return Lists.newArrayList(entity1);
    }

}
