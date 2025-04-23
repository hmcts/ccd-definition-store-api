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
import uk.gov.hmcts.ccd.definition.store.repository.entity.AccessProfileEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.RoleToAccessProfilesEntity;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RoleToAccessProfilesValidatorTest {

    private static final String CASE_TYPE_ID_1 = "TestCaseTypeID_1";
    private static final String CASE_TYPE_ID_2 = "TestCaseTypeID_2";
    private static final String STANDARD_CLAIM = "Standard/Claim";
    private static final String STANDARD_LEGAL = "Standard/Legal";
    private static final String ACCESS_CASEWORKER_CITIZEN = "caseworker,citizen";
    private static final String ACCESS_CASEWORKER = "caseworker";
    private static final String ROLE1 = "Role1";

    private RoleToAccessProfilesValidator validator;
    private ParseContext  parseContext;
    private CaseTypeEntity caseTypeEntity1;
    private CaseTypeEntity caseTypeEntity2;

    @BeforeEach
    public void setUp() {
        parseContext = mock(ParseContext.class);
        validator = new RoleToAccessProfilesValidator();

        caseTypeEntity1 = mock(CaseTypeEntity.class);
        when(caseTypeEntity1.getReference()).thenReturn(CASE_TYPE_ID_1);
        caseTypeEntity2 = mock(CaseTypeEntity.class);
        when(caseTypeEntity2.getReference()).thenReturn(CASE_TYPE_ID_2);
        Set<CaseTypeEntity> caseTypes = Sets.newHashSet(caseTypeEntity1, caseTypeEntity2);
        given(parseContext.getCaseTypes()).willReturn(caseTypes);
        AccessProfileEntity accessProfileEntity = mock(AccessProfileEntity.class);
        when(parseContext.getAccessProfile(anyString(), anyString())).thenReturn(Optional.of(accessProfileEntity));
    }


    @Test
    void shouldThrowExceptionWhenAccessProfileNotFound() {
        when(parseContext.getAccessProfile(anyString(), anyString())).thenReturn(Optional.empty());
        Assertions.assertThrows(ValidationException.class, () -> validator.validate(createEntities(), parseContext));
    }

    @Test
    void shouldNotThrowExceptionWhenAccessProfileFound() {
        AccessProfileEntity accessProfileEntity = mock(AccessProfileEntity.class);
        when(parseContext.getAccessProfile(anyString(), anyString())).thenReturn(Optional.of(accessProfileEntity));
        validator.validate(createEntities(), parseContext);
    }

    @Test
    void shouldThrowExceptionOnDuplicateRoleNameAndCaseType() {
        AccessProfileEntity accessProfileEntity = mock(AccessProfileEntity.class);
        when(parseContext.getAccessProfile(anyString(), anyString())).thenReturn(Optional.of(accessProfileEntity));
        Assertions.assertThrows(ValidationException.class, () -> validator
            .validate(createDuplicateRoleNameAndCaseAccessCategoryCaseTypeEntities(), parseContext));
    }

    @Test
    void shouldThrowExceptionOnEmptyRoleName() {
        AccessProfileEntity accessProfileEntity = mock(AccessProfileEntity.class);
        when(parseContext.getAccessProfile(anyString(), anyString())).thenReturn(Optional.of(accessProfileEntity));
        Assertions.assertThrows(ValidationException.class, () -> validator
            .validate(createEntityWithEmptyRoleName(), parseContext));
    }

    @Test
    void shouldThrowExceptionOnEmptyCaseAccessCategoriesWhenRoleNameAndCaseTypeDuplicated() {
        AccessProfileEntity accessProfileEntity = mock(AccessProfileEntity.class);
        when(parseContext.getAccessProfile(anyString(), anyString())).thenReturn(Optional.of(accessProfileEntity));
        Assertions.assertThrows(ValidationException.class, () -> validator
            .validate(createDuplicateEntitiesWithEmptyCaseAccessCategories(), parseContext));
    }

    @Test
    void shouldNotThrowExceptionWhenOnlyCaseAccessCategoriesAreUnique() {
        AccessProfileEntity accessProfileEntity = mock(AccessProfileEntity.class);
        when(parseContext.getAccessProfile(anyString(), anyString())).thenReturn(Optional.of(accessProfileEntity));
        validator.validate(createEntityWithOnlyDifferentCaseAccessCategories(), parseContext);
    }


    private List<RoleToAccessProfilesEntity> createEntities() {
        RoleToAccessProfilesEntity entity1 = mock(RoleToAccessProfilesEntity.class);
        when(entity1.getAccessProfiles()).thenReturn(ACCESS_CASEWORKER_CITIZEN);
        when(entity1.getCaseType()).thenReturn(caseTypeEntity1);
        when(entity1.getRoleName()).thenReturn(ROLE1);
        when(entity1.getCaseAccessCategories()).thenReturn(STANDARD_CLAIM);

        RoleToAccessProfilesEntity entity2 = mock(RoleToAccessProfilesEntity.class);
        when(entity2.getAccessProfiles()).thenReturn(ACCESS_CASEWORKER);
        when(entity2.getCaseType()).thenReturn(caseTypeEntity2);
        when(entity2.getRoleName()).thenReturn("Role2");
        when(entity2.getCaseAccessCategories()).thenReturn(STANDARD_CLAIM);

        return Lists.newArrayList(entity1, entity2);
    }

    private List<RoleToAccessProfilesEntity> createDuplicateRoleNameAndCaseAccessCategoryCaseTypeEntities() {
        RoleToAccessProfilesEntity entity1 = mock(RoleToAccessProfilesEntity.class);
        when(entity1.getAccessProfiles()).thenReturn(ACCESS_CASEWORKER_CITIZEN);
        when(entity1.getCaseType()).thenReturn(caseTypeEntity1);
        when(entity1.getRoleName()).thenReturn(ROLE1);
        when(entity1.getCaseAccessCategories()).thenReturn(STANDARD_CLAIM);

        return Lists.newArrayList(entity1, entity1);
    }

    private List<RoleToAccessProfilesEntity> createEntityWithEmptyRoleName() {
        RoleToAccessProfilesEntity entity1 = mock(RoleToAccessProfilesEntity.class);
        when(entity1.getAccessProfiles()).thenReturn(ACCESS_CASEWORKER_CITIZEN);
        when(entity1.getCaseType()).thenReturn(caseTypeEntity1);
        when(entity1.getRoleName()).thenReturn("");

        return Lists.newArrayList(entity1);
    }

    private List<RoleToAccessProfilesEntity> createDuplicateEntitiesWithEmptyCaseAccessCategories() {
        RoleToAccessProfilesEntity entity1 = mock(RoleToAccessProfilesEntity.class);
        when(entity1.getAccessProfiles()).thenReturn(ACCESS_CASEWORKER_CITIZEN);
        when(entity1.getCaseType()).thenReturn(caseTypeEntity1);
        when(entity1.getRoleName()).thenReturn(ROLE1);
        when(entity1.getCaseAccessCategories()).thenReturn("");

        return Lists.newArrayList(entity1, entity1);
    }

    private List<RoleToAccessProfilesEntity> createEntityWithOnlyDifferentCaseAccessCategories() {
        RoleToAccessProfilesEntity entity1 = mock(RoleToAccessProfilesEntity.class);
        when(entity1.getAccessProfiles()).thenReturn(ACCESS_CASEWORKER_CITIZEN);
        when(entity1.getCaseType()).thenReturn(caseTypeEntity1);
        when(entity1.getRoleName()).thenReturn(ROLE1);
        when(entity1.getCaseAccessCategories()).thenReturn(STANDARD_CLAIM);

        RoleToAccessProfilesEntity entity2 = mock(RoleToAccessProfilesEntity.class);
        when(entity2.getAccessProfiles()).thenReturn(ACCESS_CASEWORKER_CITIZEN);
        when(entity2.getCaseType()).thenReturn(caseTypeEntity1);
        when(entity2.getRoleName()).thenReturn(ROLE1);
        when(entity2.getCaseAccessCategories()).thenReturn(STANDARD_LEGAL);

        return Lists.newArrayList(entity1, entity2);
    }

}
