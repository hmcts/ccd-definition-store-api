package uk.gov.hmcts.ccd.definition.store.excel.parser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import uk.gov.hmcts.ccd.definition.store.domain.service.metadata.MetadataField;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;

import java.util.Collection;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

class MetadataCaseFieldParserTest {

    private MetadataCaseFieldParser metadataCaseFieldParser;

    @Mock
    private ParseContext parseContext;

    @Mock
    private Map<MetadataField, MetadataCaseFieldEntityFactory> registry;

    @BeforeEach
    void setUp() {
        initMocks(this);
        metadataCaseFieldParser = new MetadataCaseFieldParser(parseContext, registry);
    }

    @Test
    @DisplayName("should return collection of case fields")
    void shouldReturnCollectionOfCaseFields() {
        CaseTypeEntity caseTypeEntity = new CaseTypeEntity();
        CaseFieldEntity caseField = new CaseFieldEntity();
        MetadataCaseFieldEntityFactory factory = mock(MetadataCaseFieldEntityFactory.class);

        when(registry.get(MetadataField.STATE)).thenReturn(factory);
        when(factory.createCaseFieldEntity(parseContext, caseTypeEntity)).thenReturn(caseField);

        Collection<CaseFieldEntity> caseFields = metadataCaseFieldParser.parseAll(caseTypeEntity);

        assertAll(
            () -> assertThat(caseFields, hasSize(1)),
            () -> assertThat(caseFields.iterator().next(), equalTo(caseField)),
            () -> verify(registry).get(MetadataField.STATE),
            () -> verify(factory).createCaseFieldEntity(parseContext, caseTypeEntity),
            () -> verify(parseContext).registerMetadataFields(anyListOf(CaseFieldEntity.class))
        );

    }
}
