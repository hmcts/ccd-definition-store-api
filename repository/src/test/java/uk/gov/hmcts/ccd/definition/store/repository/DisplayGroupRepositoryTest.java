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
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.DisplayGroupCaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.DisplayGroupEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.DisplayGroupPurpose;
import uk.gov.hmcts.ccd.definition.store.repository.entity.DisplayGroupType;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.JurisdictionEntity;

import java.util.List;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.AllOf.allOf;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static uk.gov.hmcts.ccd.definition.store.repository.entity.DisplayGroupPurpose.EDIT;
import static uk.gov.hmcts.ccd.definition.store.repository.entity.DisplayGroupPurpose.VIEW;
import static uk.gov.hmcts.ccd.definition.store.repository.entity.DisplayGroupType.PAGE;
import static uk.gov.hmcts.ccd.definition.store.repository.entity.DisplayGroupType.TAB;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {
    SanityCheckApplication.class,
    TestConfiguration.class
})
@TestPropertySource(locations = "classpath:test.properties")
@Transactional
public class DisplayGroupRepositoryTest {

    private static final String CASE_TYPE_REFERENCE = "CaseTypeRef";
    private static final String SHOW_CONDITION = "showCondition";

    @Autowired
    private CaseTypeRepository caseTypeRepository;

    @Autowired
    private DisplayGroupRepository displayGroupRepository;

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

    @SuppressWarnings("checkstyle:LineLength")
    @Test
    public void shouldReturnDisplayGroupEntityForLatestCaseTypeVersion_whenDisplayGroupEntitesExistAcrossMultipleVersionsOfCaseType() {

        createDisplayGroupsForCase(caseTypeV1);
        createDisplayGroupsForCase(caseTypeV2);
        createDisplayGroupsForCase(caseTypeV3);

        final List<DisplayGroupEntity> fetched = displayGroupRepository
            .findTabsByCaseTypeReference(CASE_TYPE_REFERENCE);

        assertThat(fetched, hasSize(1));

        DisplayGroupEntity fetchedDg = fetched.get(0);
        assertThat(fetchedDg, allOf(
            hasProperty("reference", equalTo("ref dg tab view")),
            hasProperty("label", equalTo("label dg")),
            hasProperty("channel", equalTo("channel dg")),
            hasProperty("order", equalTo(4)),
            hasProperty("type", equalTo(TAB)),
            hasProperty("purpose", equalTo(VIEW)),
            hasProperty("showCondition", equalTo(SHOW_CONDITION)),
            hasProperty("caseType", is(caseTypeV3))
        ));


        assertThat(fetchedDg.getDisplayGroupCaseFields(), allOf(
            hasItem(allOf(
                hasProperty("order", is(1)),
                hasProperty("columnNumber", nullValue()),
                hasProperty("caseField", hasProperty("reference", is("cf1")))
            )),
            hasItem(allOf(
                hasProperty("order", is(2)),
                hasProperty("columnNumber", is(2)),
                hasProperty("caseField", hasProperty("reference", is("cf2")))
            ))
            )
        );
    }

    private void createDisplayGroupsForCase(CaseTypeEntity caseType) {
        displayGroupRepository.save(createDisplayGroup(
            caseType, "ref dg page edit", "label dg", "channel dg", 4, PAGE, EDIT, SHOW_CONDITION));
        displayGroupRepository.save(createDisplayGroup(
            caseType, "ref dg page view", "label dg", "channel dg", 4, PAGE, VIEW, SHOW_CONDITION));
        displayGroupRepository.save(createDisplayGroup(
            caseType, "ref dg tab edit", "label dg", "channel dg", 4, TAB, EDIT, SHOW_CONDITION));
        displayGroupRepository.save(createDisplayGroup(
            caseType, "ref dg tab view", "label dg", "channel dg", 4, TAB, VIEW, SHOW_CONDITION));
    }

    private void addDisplayGroupField(final CaseFieldEntity cf,
                                      final DisplayGroupEntity dg,
                                      final int order,
                                      Integer column) {
        final DisplayGroupCaseFieldEntity dgf = new DisplayGroupCaseFieldEntity();
        dgf.setCaseField(cf);
        dgf.setOrder(order);
        dgf.setColumnNumber(column);
        dg.addDisplayGroupCaseField(dgf);
    }

    private DisplayGroupEntity createDisplayGroup(final CaseTypeEntity caseType,
                                                  final String reference,
                                                  final String label,
                                                  final String channel,
                                                  final int order,
                                                  final DisplayGroupType type,
                                                  final DisplayGroupPurpose purpose,
                                                  final String showCondition) {
        final DisplayGroupEntity dg = new DisplayGroupEntity();
        dg.setCaseType(caseType);
        dg.setReference(reference);
        dg.setLabel(label);
        dg.setChannel(channel);
        dg.setOrder(order);
        dg.setType(type);
        dg.setPurpose(purpose);
        dg.setShowCondition(showCondition);
        addDisplayGroupField(getCaseField(caseType, "cf1"), dg, 1, null);
        addDisplayGroupField(getCaseField(caseType, "cf2"), dg, 2, 2);
        return dg;
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

        CaseTypeEntity c = new CaseTypeEntity();
        c.setReference(CASE_TYPE_REFERENCE);
        c.setName("ename");
        c.setJurisdiction(jurisdiction);
        c.setSecurityClassification(SecurityClassification.PUBLIC);

        c.addCaseField(helper.buildCaseField("cf1", fieldType, "label cf1", true));
        c.addCaseField(helper.buildCaseField("cf2", fieldType, "label cf2", false));
        return c;
    }
}
