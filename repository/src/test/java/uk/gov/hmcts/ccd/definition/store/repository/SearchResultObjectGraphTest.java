package uk.gov.hmcts.ccd.definition.store.repository;

import uk.gov.hmcts.ccd.definition.store.repository.entity.AccessProfileEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.JurisdictionEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.SearchResultCaseFieldEntity;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.AllOf.allOf;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
    SanityCheckApplication.class,
    TestConfiguration.class
})
@TestPropertySource(locations = "classpath:test.properties")
@Transactional
class SearchResultObjectGraphTest {

    @Autowired
    private CaseTypeRepository caseTypeRepository;

    @Autowired
    private SearchResultCaseFieldRepository searchResultCaseFieldRepository;

    @Autowired
    private TestHelper helper;

    private CaseTypeEntity caseType;

    private VersionedDefinitionRepositoryDecorator<CaseTypeEntity, Integer> versionedCaseTypeRepository;

    @BeforeEach
    void setup() {
        versionedCaseTypeRepository = new VersionedDefinitionRepositoryDecorator<>(caseTypeRepository);

        final JurisdictionEntity jurisdiction = helper.createJurisdiction();
        final FieldTypeEntity fieldType = helper.createType(jurisdiction);

        final CaseTypeEntity c = new CaseTypeEntity();
        c.setReference("id");
        c.setName("ename");
        c.setJurisdiction(jurisdiction);
        c.setSecurityClassification(SecurityClassification.PUBLIC);

        c.addCaseField(helper.buildCaseField("cf1", fieldType, "label cf1", true));
        c.addCaseField(helper.buildCaseField("cf2", fieldType, "label cf2", false));

        caseType = versionedCaseTypeRepository.save(c);
    }

    @Test
    void saveDisplayGroup() {

        final SearchResultCaseFieldEntity f = createSearchResultCaseField(
            caseType, getCaseField(caseType, "cf1"), "label dg", 4);

        searchResultCaseFieldRepository.save(f);

        final List<SearchResultCaseFieldEntity> fetched = searchResultCaseFieldRepository
            .findByCaseTypeId(caseType.getId());

        assertThat(fetched, hasSize(1));

        SearchResultCaseFieldEntity fetchedField = fetched.get(0);
        assertThat(fetchedField, allOf(
            hasProperty("label", equalTo("label dg")),
            hasProperty("order", equalTo(4))
        ));


        assertThat(fetchedField.getCaseField(), hasProperty("reference", is("cf1")));
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
        f.setAccessProfile(createAccessProfile());
        return f;
    }

    private AccessProfileEntity createAccessProfile() {
        final AccessProfileEntity accessProfile = new AccessProfileEntity();
        accessProfile.setReference("access profile ref");
        accessProfile.setName("access profile name");
        accessProfile.setDescription("access profile description");
        accessProfile.setSecurityClassification(SecurityClassification.PUBLIC);
        return accessProfile;
    }

    private CaseFieldEntity getCaseField(final CaseTypeEntity caseType, final String reference) {
        return caseType.getCaseFields()
            .stream()
            .filter(f -> StringUtils.equals(reference, f.getReference()))
            .findFirst()
            .orElseThrow(IllegalArgumentException::new);
    }
}
