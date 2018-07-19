package uk.gov.hmcts.ccd.definition.store.domain.service;

import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.definition.store.repository.SecurityClassification;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.JurisdictionEntity;
import uk.gov.hmcts.ccd.definition.store.repository.model.CaseTypeLite;
import uk.gov.hmcts.ccd.definition.store.repository.model.Jurisdiction;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Date;

import static org.hamcrest.Matchers.hasItems;
import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

class CaseTypeEntityToCaseTypeLiteDTOMapperTest {

    private static final String LIVE_FROM = "2017-02-02";

    private static final String LIVE_TO = "2018-03-03";

    private CaseTypeEntityToCaseTypeLiteDTOMapper classUnderTest = new CaseTypeEntityToCaseTypeLiteDTOMapperImpl();

    private CaseTypeEntityToCaseTypeLiteDTOMapper spyOnClassUnderTest;

    @BeforeEach
    public void setupSpy() throws Exception {
        spyOnClassUnderTest = spy(classUnderTest);
    }

    @Nested
    @DisplayName("Should return a CaseTypeLite whose fields match those in the CaseTypeEntity")
    public class MapCaseTypeEntitySubsetTests {

        @Test
        public void testMapSubsetCaseTypeEntity() throws Exception {
            CaseTypeEntity caseTypeEntity = caseTypeEntity();

            CaseTypeLite caseTypeLite = classUnderTest.map(caseTypeEntity);

            // Assertions
            assertEquals(caseTypeLite.getId(), caseTypeEntity.getReference());
            assertEquals(caseTypeLite.getDescription(), caseTypeEntity.getDescription());
            assertEquals(caseTypeLite.getName(), caseTypeEntity.getName());
        }

        @Test
        public void testMapEmptyCaseTypeEntity() throws Exception {
            CaseTypeEntity caseTypeEntity = new CaseTypeEntity();

            CaseTypeLite caseTypeLite = classUnderTest.map(caseTypeEntity);

            // Assertions
            assertNull(caseTypeLite.getId());
            assertNull(caseTypeLite.getDescription());
            assertNull(caseTypeLite.getName());
        }

        private CaseTypeEntity caseTypeEntity() {
            CaseTypeEntity caseTypeEntity = new CaseTypeEntity();
            caseTypeEntity.setVersion(100);
            caseTypeEntity.setLiveFrom(LocalDate.parse(LIVE_FROM));
            caseTypeEntity.setLiveTo(LocalDate.parse(LIVE_TO));
            caseTypeEntity.setReference("Reference");
            caseTypeEntity.setName("Name");
            caseTypeEntity.setSecurityClassification(SecurityClassification.PUBLIC);
            return caseTypeEntity;
        }
    }

    @Nested
    @DisplayName("Should return a Jurisdiction which matches the JurisdictionEntity")
    public class MapJurisdictionEntityTests {

        @Test
        public void testMapJurisdictionEntity() throws Exception {

            JurisdictionEntity jurisdictionEntity = new JurisdictionEntity();
            jurisdictionEntity.setDescription("Jurisdiction Description");
            jurisdictionEntity.setName("Jurisdiction Name");
            jurisdictionEntity.setReference("Jurisdiction Reference");
            jurisdictionEntity.setLiveFrom(new Date());
            jurisdictionEntity.setLiveTo(new Date());

            CaseTypeEntity caseTypeEntity1 = new CaseTypeEntity();
            CaseTypeEntity caseTypeEntity2 = new CaseTypeEntity();
            CaseTypeLite caseType1 = new CaseTypeLite();
            CaseTypeLite caseType2 = new CaseTypeLite();
            when(spyOnClassUnderTest.map(caseTypeEntity1)).thenReturn(caseType1);
            when(spyOnClassUnderTest.map(caseTypeEntity2)).thenReturn(caseType2);
            jurisdictionEntity.addCaseTypes(Arrays.asList(caseTypeEntity1, caseTypeEntity2));

            // Call the 'spied on' implementation
            Jurisdiction jurisdiction = spyOnClassUnderTest.map(jurisdictionEntity);

            Assert.assertEquals(jurisdictionEntity.getDescription(), jurisdiction.getDescription());
            Assert.assertEquals(jurisdictionEntity.getName(), jurisdiction.getName());
            Assert.assertEquals(jurisdictionEntity.getReference(), jurisdiction.getId());
            Assert.assertEquals(jurisdictionEntity.getLiveFrom(), jurisdiction.getLiveFrom());
            Assert.assertEquals(jurisdictionEntity.getLiveTo(), jurisdiction.getLiveUntil());
            Assert.assertEquals(2, jurisdiction.getCaseTypes().size());
            assertThat(jurisdiction.getCaseTypes(), hasItems(caseType1, caseType2));
        }

        @Test
        public void testMapEmptyJurisdictionEntity() throws Exception {

            JurisdictionEntity jurisdictionEntity = new JurisdictionEntity();

            Jurisdiction jurisdiction = classUnderTest.map(jurisdictionEntity);

            Assert.assertNull(jurisdiction.getDescription());
            Assert.assertNull(jurisdiction.getName());
            Assert.assertNull(jurisdiction.getId());
            Assert.assertNull(jurisdiction.getLiveFrom());
            Assert.assertNull(jurisdiction.getLiveUntil());
            Assert.assertEquals(0, jurisdiction.getCaseTypes().size());
        }
    }
}
