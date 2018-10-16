package uk.gov.hmcts.ccd.definition.store.elastic.mapping.type;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.definition.store.elastic.hamcresutil.IsEqualJSON.equalToJSONInFile;
import static uk.gov.hmcts.ccd.definition.store.utils.FieldTypeBuilder.newType;
import static uk.gov.hmcts.ccd.definition.store.utils.FieldTypeBuilder.textFieldType;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.ccd.definition.store.elastic.TestUtils;
import uk.gov.hmcts.ccd.definition.store.elastic.mapping.AbstractMapperTest;
import uk.gov.hmcts.ccd.definition.store.elastic.mapping.StubComplexTypeMappingGenerator;
import uk.gov.hmcts.ccd.definition.store.elastic.mapping.StubTypeMappingGenerator;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldTypeEntity;
import uk.gov.hmcts.ccd.definition.store.utils.FieldTypeBuilder;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class CollectionTypeMappingGeneratorTest extends AbstractMapperTest implements TestUtils {

    @InjectMocks
    private CollectionTypeMappingGenerator collectionTypeMapper;

    @BeforeEach
    public void setUp() {
        super.setup();

        when(config.getSecurityClassificationMapping()).thenReturn("securityClassificationMapping");

        typeMappings.put("Text", "textMapping");
        elasticMappings.put("disabled", "disabledMapping");

        StubTypeMappingGenerator stubTypeMappingGenerator = new StubTypeMappingGenerator("Text", "dataMapping",
                "dataClassificationMapping");
        StubComplexTypeMappingGenerator stubComplexTypeMappingGenerator =
                new StubComplexTypeMappingGenerator("Complex",
                        "complexDataMapping", "complexDataClassificationMapping");
        addMappingGenerator(stubTypeMappingGenerator);
        addMappingGenerator(stubComplexTypeMappingGenerator);
        collectionTypeMapper.inject(stubTypeMappersManager);
        stubTypeMappingGenerator.inject(stubTypeMappersManager);
        stubComplexTypeMappingGenerator.inject(stubTypeMappersManager);
    }

    @Test
    public void shouldGenerateMappingForCollectionType() {
        CaseFieldEntity collectionField = newCollectionField();

        String result = collectionTypeMapper.dataMapping(collectionField);

        assertThat(result, equalToJSONInFile(
                readFileFromClasspath("json/type_mapping_collection_type.json")));
    }

    @Test
    public void shouldGenerateClassificationMappingForCollectionType() {
        CaseFieldEntity collectionField = newCollectionField();

        String result = collectionTypeMapper.dataClassificationMapping(collectionField);

        assertThat(result, equalToJSONInFile(
                readFileFromClasspath("json/type_mapping_classification_collection_type.json")));

    }

    @Test
    public void shouldGenerateMappingForCollectionOfComplexType() {
        CaseFieldEntity collectionField = newCollectionOfComplexField();

        String result = collectionTypeMapper.dataMapping(collectionField);

        assertThat(result, equalToJSONInFile(
                readFileFromClasspath("json/type_mapping_collection_complex_type.json")));
    }

    @Test
    public void shouldGenerateClassificationMappingForCollectionOfComplexType() {
        CaseFieldEntity collectionField = newCollectionOfComplexField();

        String result = collectionTypeMapper.dataClassificationMapping(collectionField);

        assertThat(result, equalToJSONInFile(
                readFileFromClasspath("json/type_mapping_classification_collection_complex_type.json")));

    }

    private CaseFieldEntity newCollectionField() {
        FieldTypeEntity collectionFieldType = newType("reasons-51503ee8-ac6d-4b57-845e-4806332a9820")
                .withCollectionFieldType(textFieldType()).buildCollection();

        CaseFieldEntity collectionField = new CaseFieldEntity();
        collectionField.setReference("Aliases");
        collectionField.setFieldType(collectionFieldType);
        return collectionField;
    }

    private CaseFieldEntity newCollectionOfComplexField() {
        FieldTypeEntity collectionFieldType = newType("reasons-51503ee8-ac6d-4b57-845e-4806332a9820")
                .withCollectionFieldType(newComplexType()).buildCollection();

        CaseFieldEntity collectionField = new CaseFieldEntity();
        collectionField.setReference("Aliases");
        collectionField.setFieldType(collectionFieldType);
        return collectionField;
    }

    private FieldTypeEntity newComplexType() {
        FieldTypeBuilder complexType = newType("Person");
        complexType.addComplexField("forename", textFieldType());
        return complexType.buildComplex();
    }
}
