package uk.gov.hmcts.ccd.definition.store.domain.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.definition.store.repository.CaseTypeLiteRepository;
import uk.gov.hmcts.ccd.definition.store.repository.JurisdictionRepository;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeLiteEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.JurisdictionEntity;
import uk.gov.hmcts.ccd.definition.store.repository.model.CaseTypeLite;
import uk.gov.hmcts.ccd.definition.store.repository.model.Jurisdiction;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JurisdictionServiceImplTest {

    @Mock
    private JurisdictionRepository repository;

    @Mock
    private CaseTypeLiteRepository caseTypeRepository;

    private JurisdictionServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new JurisdictionServiceImpl(repository, caseTypeRepository, EntityToResponseDTOMapper.INSTANCE);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void shouldPreserveIncomingOrderRegardlessOfNamesAndOrderValues(boolean filtered) {
        List<CaseTypeLite> result = getCaseTypes(filtered, List.of(
            caseType("Zulu", 1), caseType("beta", 42), caseType("alpha", 99)));

        assertThat(result).extracting(CaseTypeLite::getId).containsExactly("Zulu", "beta", "alpha");
        assertThat(result).extracting(CaseTypeLite::getCaseTypesOrder).containsExactly(1, 42, 99);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void shouldPreserveIncomingOrderForNumberedAndUnnumberedCaseTypes(boolean filtered) {
        List<CaseTypeLiteEntity> incoming = List.of(
            caseType("ZUnnumbered", null), caseType("Later", 42),
            caseType("AUnnumbered", null), caseType("Earlier", 7));

        List<CaseTypeLite> result = getCaseTypes(filtered, incoming);

        assertThat(result).extracting(CaseTypeLite::getId)
            .containsExactly("ZUnnumbered", "Later", "AUnnumbered", "Earlier");
        assertThat(incoming).extracting(CaseTypeLiteEntity::getReference)
            .containsExactly("ZUnnumbered", "Later", "AUnnumbered", "Earlier");
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void shouldPreserveIncomingOrderForDuplicateValues(boolean filtered) {
        List<CaseTypeLite> result = getCaseTypes(filtered, List.of(
            caseType("ZFirstTie", 42), caseType("Earlier", 7), caseType("ASecondTie", 42)));

        assertThat(result).extracting(CaseTypeLite::getId).containsExactly("ZFirstTie", "Earlier", "ASecondTie");
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void shouldPreserveIncomingOrderWhenAllOrdersAreMissing(boolean filtered) {
        List<CaseTypeLite> result = getCaseTypes(filtered,
            List.of(caseType("ZFirst", null), caseType("ASecond", null)));

        assertThat(result).extracting(CaseTypeLite::getId).containsExactly("ZFirst", "ASecond");
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void shouldNotSortByDisplayNameOrId(boolean filtered) {
        CaseTypeLiteEntity first = caseType("ZId", 99);
        first.setName("Alpha");
        CaseTypeLiteEntity second = caseType("AId", 1);
        second.setName("Zulu");

        assertThat(getCaseTypes(filtered, List.of(second, first))).extracting(CaseTypeLite::getId)
            .containsExactly("AId", "ZId");
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void shouldReturnEmptyCaseTypes(boolean filtered) {
        assertThat(getCaseTypes(filtered, List.of())).isEmpty();
    }

    private List<CaseTypeLite> getCaseTypes(boolean filtered, List<CaseTypeLiteEntity> caseTypes) {
        JurisdictionEntity jurisdiction = new JurisdictionEntity();
        jurisdiction.setReference("TEST");
        when(caseTypeRepository.findByJurisdictionId("TEST")).thenReturn(caseTypes);
        List<Jurisdiction> result;
        if (filtered) {
            when(repository.findAllLatestVersionByReference(List.of("test"))).thenReturn(List.of(jurisdiction));
            result = service.getAll(List.of("TEST"));
        } else {
            when(repository.findAllLatestVersion()).thenReturn(List.of(jurisdiction));
            result = service.getAll();
        }
        assertThat(result).singleElement().satisfies(item -> assertThat(item.getId()).isEqualTo("TEST"));
        return result.getFirst().getCaseTypes();
    }

    private CaseTypeLiteEntity caseType(String reference, Integer order) {
        CaseTypeLiteEntity entity = new CaseTypeLiteEntity();
        entity.setReference(reference);
        entity.setName(reference);
        entity.setCaseTypesOrder(order);
        return entity;
    }
}
