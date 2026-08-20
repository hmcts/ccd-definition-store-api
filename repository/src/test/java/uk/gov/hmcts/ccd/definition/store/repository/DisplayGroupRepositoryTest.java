package uk.gov.hmcts.ccd.definition.store.repository;

import uk.gov.hmcts.ccd.definition.store.repository.entity.AccessProfileEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.DisplayGroupCaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.DisplayGroupEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.DisplayGroupPurpose;
import uk.gov.hmcts.ccd.definition.store.repository.entity.DisplayGroupType;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.JurisdictionEntity;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.AllOf.allOf;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static uk.gov.hmcts.ccd.definition.store.repository.entity.DisplayGroupPurpose.EDIT;
import static uk.gov.hmcts.ccd.definition.store.repository.entity.DisplayGroupPurpose.VIEW;
import static uk.gov.hmcts.ccd.definition.store.repository.entity.DisplayGroupType.PAGE;
import static uk.gov.hmcts.ccd.definition.store.repository.entity.DisplayGroupType.TAB;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {
    SanityCheckApplication.class,
    TestConfiguration.class
})
@TestPropertySource(locations = "classpath:test.properties")
@Transactional
class DisplayGroupRepositoryTest {

    private static final String CASE_TYPE_REFERENCE = "CaseTypeRef";
    private static final String SHOW_CONDITION = "showCondition";
    private static final String DISPLAY_GROUP_LABEL = "label dg";
    private static final String DISPLAY_GROUP_CHANNEL = "channel dg";
    private static final int DISPLAY_GROUP_ORDER = 4;
    private static final String TAB_VIEW_DISPLAY_GROUP_REFERENCE = "ref dg tab view";
    private static final String SUBFIELD_DISPLAY_GROUP_REFERENCE = "ref dg tab view subfields";
    private static final String DUPLICATE_WHOLE_FIELD_DISPLAY_GROUP_REFERENCE = "ref dg tab view duplicate whole";
    private static final String DUPLICATE_SUBFIELD_DISPLAY_GROUP_REFERENCE = "ref dg tab view duplicate subfield";
    private static final String DUPLICATE_SUBFIELD_CASE_DISPLAY_GROUP_REFERENCE =
        "ref dg tab view duplicate subfield case";
    private static final String PRIMARY_CASE_FIELD_REFERENCE = "cf1";
    private static final String SECONDARY_CASE_FIELD_REFERENCE = "cf2";
    private static final String ADDRESS_ELEMENT_PATH = "Address";
    private static final String LOWER_CASE_ADDRESS_ELEMENT_PATH = "address";
    private static final String POST_CODE_ELEMENT_PATH = "PostCode";

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

    @BeforeEach
    void setup() {
        versionedCaseTypeRepository = new VersionedDefinitionRepositoryDecorator<>(caseTypeRepository);

        caseTypeV1 = versionedCaseTypeRepository.save(caseTypeEntity());
        caseTypeV2 = versionedCaseTypeRepository.save(caseTypeEntity());
        caseTypeV3 = versionedCaseTypeRepository.save(caseTypeEntity());
    }

    @SuppressWarnings("checkstyle:LineLength")
    @Test
    void shouldReturnDisplayGroupEntityForLatestCaseTypeVersion_whenDisplayGroupEntitesExistAcrossMultipleVersionsOfCaseType() {

        createDisplayGroupsForCase(caseTypeV1);
        createDisplayGroupsForCase(caseTypeV2);
        createDisplayGroupsForCase(caseTypeV3);

        final List<DisplayGroupEntity> fetched = displayGroupRepository
            .findTabsByCaseTypeReference(CASE_TYPE_REFERENCE);

        assertThat(fetched, hasSize(1));

        DisplayGroupEntity fetchedDg = fetched.get(0);
        assertThat(fetchedDg, allOf(
            hasProperty("reference", equalTo(TAB_VIEW_DISPLAY_GROUP_REFERENCE)),
            hasProperty("label", equalTo(DISPLAY_GROUP_LABEL)),
            hasProperty("channel", equalTo(DISPLAY_GROUP_CHANNEL)),
            hasProperty("order", equalTo(DISPLAY_GROUP_ORDER)),
            hasProperty("type", equalTo(TAB)),
            hasProperty("purpose", equalTo(VIEW)),
            hasProperty("showCondition", equalTo(SHOW_CONDITION)),
            hasProperty("caseType", is(caseTypeV3))
        ));


        assertThat(fetchedDg.getDisplayGroupCaseFields(), allOf(
            hasItem(allOf(
                hasProperty("order", is(1)),
                hasProperty("columnNumber", nullValue()),
                hasProperty("caseFieldElementPath", nullValue()),
                hasProperty("caseField", hasProperty("reference", is(PRIMARY_CASE_FIELD_REFERENCE)))
            )),
            hasItem(allOf(
                hasProperty("order", is(2)),
                hasProperty("columnNumber", is(2)),
                hasProperty("caseFieldElementPath", nullValue()),
                hasProperty("caseField", hasProperty("reference", is(SECONDARY_CASE_FIELD_REFERENCE)))
            ))
            )
        );
    }

    @Test
    void shouldAllowSameDisplayGroupCaseFieldWithDifferentElementPaths() {
        DisplayGroupEntity displayGroup = createDisplayGroupWithoutFields(
            caseTypeV3, SUBFIELD_DISPLAY_GROUP_REFERENCE, DISPLAY_GROUP_LABEL, DISPLAY_GROUP_CHANNEL,
            DISPLAY_GROUP_ORDER, TAB, VIEW, SHOW_CONDITION);
        addDisplayGroupField(getCaseField(caseTypeV3, PRIMARY_CASE_FIELD_REFERENCE), displayGroup, 1, null,
            ADDRESS_ELEMENT_PATH);
        addDisplayGroupField(getCaseField(caseTypeV3, PRIMARY_CASE_FIELD_REFERENCE), displayGroup, 2, null,
            POST_CODE_ELEMENT_PATH);

        displayGroupRepository.saveAndFlush(displayGroup);

        final List<DisplayGroupEntity> fetched = displayGroupRepository
            .findTabsByCaseTypeReference(CASE_TYPE_REFERENCE);

        DisplayGroupEntity fetchedDg = fetched.stream()
            .filter(dg -> SUBFIELD_DISPLAY_GROUP_REFERENCE.equals(dg.getReference()))
            .findFirst()
            .orElseThrow();

        assertThat(fetchedDg.getDisplayGroupCaseFields(), allOf(
            hasItem(allOf(
                hasProperty("caseFieldElementPath", is(ADDRESS_ELEMENT_PATH)),
                hasProperty("caseField", hasProperty("reference", is(PRIMARY_CASE_FIELD_REFERENCE)))
            )),
            hasItem(allOf(
                hasProperty("caseFieldElementPath", is(POST_CODE_ELEMENT_PATH)),
                hasProperty("caseField", hasProperty("reference", is(PRIMARY_CASE_FIELD_REFERENCE)))
            ))
        ));
    }

    @Test
    void shouldRejectDuplicateWholeDisplayGroupCaseField() {
        DisplayGroupEntity displayGroup = createDisplayGroupWithoutFields(
            caseTypeV3, DUPLICATE_WHOLE_FIELD_DISPLAY_GROUP_REFERENCE, DISPLAY_GROUP_LABEL, DISPLAY_GROUP_CHANNEL,
            DISPLAY_GROUP_ORDER, TAB, VIEW, SHOW_CONDITION);
        addDisplayGroupField(getCaseField(caseTypeV3, PRIMARY_CASE_FIELD_REFERENCE), displayGroup, 1, null);
        addDisplayGroupField(getCaseField(caseTypeV3, PRIMARY_CASE_FIELD_REFERENCE), displayGroup, 2, null);

        assertThrows(DataIntegrityViolationException.class, () -> displayGroupRepository.saveAndFlush(displayGroup));
    }

    @Test
    void shouldRejectDuplicateDisplayGroupCaseFieldElementPath() {
        assertDuplicateDisplayGroupCaseFieldElementPathIsRejected(
            DUPLICATE_SUBFIELD_DISPLAY_GROUP_REFERENCE,
            ADDRESS_ELEMENT_PATH
        );
    }

    @Test
    void shouldRejectDuplicateDisplayGroupCaseFieldElementPathIgnoringCase() {
        assertDuplicateDisplayGroupCaseFieldElementPathIsRejected(
            DUPLICATE_SUBFIELD_CASE_DISPLAY_GROUP_REFERENCE,
            LOWER_CASE_ADDRESS_ELEMENT_PATH
        );
    }

    private void createDisplayGroupsForCase(CaseTypeEntity caseType) {
        displayGroupRepository.save(createDisplayGroup(
            caseType, "ref dg page edit", DISPLAY_GROUP_LABEL, DISPLAY_GROUP_CHANNEL, DISPLAY_GROUP_ORDER, PAGE, EDIT,
            SHOW_CONDITION));
        displayGroupRepository.save(createDisplayGroup(
            caseType, "ref dg page view", DISPLAY_GROUP_LABEL, DISPLAY_GROUP_CHANNEL, DISPLAY_GROUP_ORDER, PAGE, VIEW,
            SHOW_CONDITION));
        displayGroupRepository.save(createDisplayGroup(
            caseType, "ref dg tab edit", DISPLAY_GROUP_LABEL, DISPLAY_GROUP_CHANNEL, DISPLAY_GROUP_ORDER, TAB, EDIT,
            SHOW_CONDITION));
        displayGroupRepository.save(createDisplayGroup(
            caseType, TAB_VIEW_DISPLAY_GROUP_REFERENCE, DISPLAY_GROUP_LABEL, DISPLAY_GROUP_CHANNEL, DISPLAY_GROUP_ORDER,
            TAB, VIEW, SHOW_CONDITION));
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

    private void addDisplayGroupField(final CaseFieldEntity cf,
                                      final DisplayGroupEntity dg,
                                      final int order,
                                      Integer column,
                                      String caseFieldElementPath) {
        final DisplayGroupCaseFieldEntity dgf = new DisplayGroupCaseFieldEntity();
        dgf.setCaseField(cf);
        dgf.setOrder(order);
        dgf.setColumnNumber(column);
        dgf.setCaseFieldElementPath(caseFieldElementPath);
        dg.addDisplayGroupCaseField(dgf);
    }

    private void assertDuplicateDisplayGroupCaseFieldElementPathIsRejected(String displayGroupReference,
                                                                          String duplicateElementPath) {
        DisplayGroupEntity displayGroup = createDisplayGroupWithoutFields(
            caseTypeV3, displayGroupReference, DISPLAY_GROUP_LABEL, DISPLAY_GROUP_CHANNEL, DISPLAY_GROUP_ORDER, TAB,
            VIEW, SHOW_CONDITION);
        addDisplayGroupField(getCaseField(caseTypeV3, PRIMARY_CASE_FIELD_REFERENCE), displayGroup, 1, null,
            ADDRESS_ELEMENT_PATH);
        addDisplayGroupField(getCaseField(caseTypeV3, PRIMARY_CASE_FIELD_REFERENCE), displayGroup, 2, null,
            duplicateElementPath);

        assertThrows(DataIntegrityViolationException.class, () -> displayGroupRepository.saveAndFlush(displayGroup));
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
        dg.setAccessProfile(createAccessProfile());
        addDisplayGroupField(getCaseField(caseType, PRIMARY_CASE_FIELD_REFERENCE), dg, 1, null);
        addDisplayGroupField(getCaseField(caseType, SECONDARY_CASE_FIELD_REFERENCE), dg, 2, 2);
        return dg;
    }

    private DisplayGroupEntity createDisplayGroupWithoutFields(final CaseTypeEntity caseType,
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
        dg.setAccessProfile(createAccessProfile());
        return dg;
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

    private CaseTypeEntity caseTypeEntity() {

        final JurisdictionEntity jurisdiction = helper.createJurisdiction();
        final FieldTypeEntity fieldType = helper.createType(jurisdiction);

        CaseTypeEntity c = new CaseTypeEntity();
        c.setReference(CASE_TYPE_REFERENCE);
        c.setName("ename");
        c.setJurisdiction(jurisdiction);
        c.setSecurityClassification(SecurityClassification.PUBLIC);

        c.addCaseField(helper.buildCaseField(PRIMARY_CASE_FIELD_REFERENCE, fieldType, "label cf1", true));
        c.addCaseField(helper.buildCaseField(SECONDARY_CASE_FIELD_REFERENCE, fieldType, "label cf2", false));
        return c;
    }
}
