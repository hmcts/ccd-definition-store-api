package uk.gov.hmcts.ccd.definition.store.repository;

import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.ccd.definition.store.repository.entity.*;

import java.util.List;

import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.AllOf.allOf;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {
    SanityCheckApplication.class,
    TestConfiguration.class
})
@TestPropertySource(locations = "classpath:test.properties")
@Transactional
public class GenericLayoutRepositoryTest {

    private static final String CASE_ID = "CaseId";

    @Autowired
    private CaseTypeRepository caseTypeRepository;

    @Autowired
    private GenericLayoutRepository genericLayoutRepository;

    @Autowired
    private TestHelper helper;

    private CaseTypeEntity caseTypeV1;
    private CaseTypeEntity caseTypeV2;
    private CaseTypeEntity caseTypeV3;

    private VersionedDefinitionRepositoryDecorator<CaseTypeEntity, Integer> versionedCaseTypeRepository;

    @Before
    public void setup() {
        versionedCaseTypeRepository = new VersionedDefinitionRepositoryDecorator<>(caseTypeRepository);
        caseTypeV1 = versionedCaseTypeRepository.save(caseTypeEntity());
        caseTypeV2 = versionedCaseTypeRepository.save(caseTypeEntity());
        caseTypeV3 = versionedCaseTypeRepository.save(caseTypeEntity());
    }

    @Test
    public void shouldReturnWorkbasketLayoutsForLatestCaseType_whenWorkbasketLayoutsExistForThreeCaseVersions() {

        genericLayoutRepository.save(createWorkBasketCaseField(caseTypeV1, getCaseField(caseTypeV1, "cf1"), "label dg", 4));
        genericLayoutRepository.save(createWorkBasketCaseField(caseTypeV2, getCaseField(caseTypeV2, "cf1"), "label dg", 4));
        genericLayoutRepository.save(createWorkBasketCaseField(caseTypeV3, getCaseField(caseTypeV3, "cf1"), "label dg", 4));

        final List<WorkBasketCaseFieldEntity> fetched = genericLayoutRepository.findWorkbasketByCaseTypeReference(caseTypeV3.getReference());

        assertThat(fetched, hasSize(1));

        WorkBasketCaseFieldEntity fetchedField = fetched.get(0);
        assertThat(fetchedField, allOf(
            hasProperty("label", equalTo("label dg")),
            hasProperty("order", equalTo(4))
        ));

        assertThat(fetchedField.getCaseField(), hasProperty("reference", is("cf1")));
        assertThat(fetchedField.getCaseType(), is(caseTypeV3));
    }

    private WorkBasketCaseFieldEntity createWorkBasketCaseField(final CaseTypeEntity caseType,
                                                                final CaseFieldEntity caseFieldEntity,
                                                                final String label,
                                                                final int order) {
        final WorkBasketCaseFieldEntity f = new WorkBasketCaseFieldEntity();
        f.setCaseType(caseType);
        f.setCaseField(caseFieldEntity);
        f.setLabel(label);
        f.setOrder(order);
        return f;
    }

    @Test
    public void shouldReturnSearchCasesResultLayoutsForLatestCaseType_whenSearchCasesResultLayoutsExistForThreeCaseVersions() {

        genericLayoutRepository.save(createSearchCasesResultField(caseTypeV1, getCaseField(caseTypeV1, "cf1"), "label dg", 4));
        genericLayoutRepository.save(createSearchCasesResultField(caseTypeV2, getCaseField(caseTypeV2, "cf1"), "label dg", 4));
        genericLayoutRepository.save(createSearchCasesResultField(caseTypeV3, getCaseField(caseTypeV3, "cf1"), "label dg", 4));

        final List<SearchCasesResultFieldEntity> fetched = genericLayoutRepository.findSearchCasesResultsByCaseTypeReference(caseTypeV3.getReference());

        assertThat(fetched, hasSize(1));

        SearchCasesResultFieldEntity fetchedField = fetched.get(0);
        assertThat(fetchedField, allOf(
            hasProperty("label", equalTo("label dg")),
            hasProperty("order", equalTo(4))
        ));

        assertThat(fetchedField.getCaseField(), hasProperty("reference", is("cf1")));
        assertThat(fetchedField.getCaseType(), is(caseTypeV3));
    }

    private SearchCasesResultFieldEntity createSearchCasesResultField(final CaseTypeEntity caseType,
                                                                final CaseFieldEntity caseFieldEntity,
                                                                final String label,
                                                                final int order) {
        final SearchCasesResultFieldEntity f = new SearchCasesResultFieldEntity();
        f.setCaseType(caseType);
        f.setCaseField(caseFieldEntity);
        f.setLabel(label);
        f.setOrder(order);
        return f;
    }

    @Test
    public void shouldSearchInputLayoutsForLatestCaseType_whenSearchInputLayoutsExistForThreeCaseVersions() {

        genericLayoutRepository.save(createSearchInputCaseField(caseTypeV1, getCaseField(caseTypeV1, "cf2"), "qwerty", 6));
        genericLayoutRepository.save(createSearchInputCaseField(caseTypeV2, getCaseField(caseTypeV2, "cf2"), "qwerty", 6));
        genericLayoutRepository.save(createSearchInputCaseField(caseTypeV3, getCaseField(caseTypeV3, "cf2"), "qwerty", 6));

        final List<SearchInputCaseFieldEntity> fetched = genericLayoutRepository.findSearchInputsByCaseTypeReference(caseTypeV3.getReference());

        assertThat(fetched, hasSize(1));

        SearchInputCaseFieldEntity fetchedField = fetched.get(0);
        assertThat(fetchedField, allOf(
            hasProperty("label", equalTo("qwerty")),
            hasProperty("order", equalTo(6))
        ));


        assertThat(fetchedField.getCaseField(), hasProperty("reference", is("cf2")));
    }

    private SearchInputCaseFieldEntity createSearchInputCaseField(final CaseTypeEntity caseType,
                                                                  final CaseFieldEntity caseFieldEntity,
                                                                  final String label,
                                                                  final int order) {
        final SearchInputCaseFieldEntity f = new SearchInputCaseFieldEntity();
        f.setCaseType(caseType);
        f.setCaseField(caseFieldEntity);
        f.setLabel(label);
        f.setOrder(order);
        return f;
    }

    @Test
    public void shouldSearchResultLayoutsForLatestCaseType_whenSearchResultLayoutsExistForThreeCaseVersions() {

        genericLayoutRepository.save(createSearchResultCaseField(caseTypeV1, getCaseField(caseTypeV1, "cf3"), "v vhjf vh", 12));
        genericLayoutRepository.save(createSearchResultCaseField(caseTypeV2, getCaseField(caseTypeV2, "cf3"), "v vhjf vh", 12));
        genericLayoutRepository.save(createSearchResultCaseField(caseTypeV3, getCaseField(caseTypeV3, "cf3"), "v vhjf vh", 12));

        final List<SearchResultCaseFieldEntity> fetched = genericLayoutRepository.findSearchResultsByCaseTypeReference(caseTypeV3.getReference());

        assertThat(fetched, hasSize(1));

        SearchResultCaseFieldEntity fetchedField = fetched.get(0);
        assertThat(fetchedField, allOf(
            hasProperty("label", equalTo("v vhjf vh")),
            hasProperty("order", equalTo(12))
        ));


        assertThat(fetchedField.getCaseField(), hasProperty("reference", is("cf3")));
    }

    private SearchResultCaseFieldEntity createSearchResultCaseField(final CaseTypeEntity caseType,
                                                                    final CaseFieldEntity caseFieldEntity,
                                                                    final String label,
                                                                    final int order) {
        final SearchResultCaseFieldEntity f = new SearchResultCaseFieldEntity();
        f.setCaseType(caseType);
        f.setCaseField(caseFieldEntity);
        f.setLabel(label);
        f.setOrder(order);
        return f;
    }

    private CaseFieldEntity getCaseField(final CaseTypeEntity caseType, final String reference) {
        return caseType.getCaseFields()
            .stream()
            .filter(f -> StringUtils.equals(reference, f.getReference()))
            .findFirst()
            .orElseThrow(IllegalArgumentException::new);
    }

    private CaseTypeEntity caseTypeEntity() {
        final JurisdictionEntity jurisdiction = helper.createJurisdiction();
        final FieldTypeEntity fieldType = helper.createType(jurisdiction);

        CaseTypeEntity caseTypeEntity = new CaseTypeEntity();
        caseTypeEntity.setReference(CASE_ID);
        caseTypeEntity.setName("ename");
        caseTypeEntity.setJurisdiction(jurisdiction);
        caseTypeEntity.setSecurityClassification(SecurityClassification.PUBLIC);

        caseTypeEntity.addCaseField(helper.buildCaseField("cf1", fieldType, "label cf1", true));
        caseTypeEntity.addCaseField(helper.buildCaseField("cf2", fieldType, "label cf2", false));
        caseTypeEntity.addCaseField(helper.buildCaseField("cf3", fieldType, "label cf3", false));

        return caseTypeEntity;
    }
}
