package uk.gov.hmcts.ccd.definition.store.elastic.mapping.type;

import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.definition.store.elastic.hamcresutil.IsEqualJSON.equalToJSONInFile;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.hmcts.ccd.definition.store.elastic.TestUtils;
import uk.gov.hmcts.ccd.definition.store.elastic.mapping.AbstractMapperTest;
import uk.gov.hmcts.ccd.definition.store.elastic.mapping.StubComplexTypeMappingGenerator;
import uk.gov.hmcts.ccd.definition.store.elastic.mapping.StubTypeMappingGenerator;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldTypeEntity;
import uk.gov.hmcts.ccd.definition.store.utils.FieldTypeBuilder;

@RunWith(MockitoJUnitRunner.class)
public class CollectionTypeMappingGeneratorTest extends AbstractMapperTest implements TestUtils {

    @InjectMocks
    private CollectionTypeMappingGenerator collectionTypeMapper;

    @Before
    public void setup() {
        super.setup();

        when(config.getSecurityClassificationMapping()).thenReturn("securityClassificationMapping");

        typeMappings.put("Text", "textMapping");
        elasticMappings.put("disabled", "disabledMapping");

        StubTypeMappingGenerator stubTypeMappingGenerator = new StubTypeMappingGenerator("Text", "dataMapping",
                "dataClassificationMapping");
        StubComplexTypeMappingGenerator stubComplexTypeMappingGenerator =
                new StubComplexTypeMappingGenerator("Complex",
                        "complexDataMapping", "complexDataClassificationMapping");
        stubMappingGenerator(stubTypeMappingGenerator);
        stubMappingGenerator(stubComplexTypeMappingGenerator);
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
        FieldTypeEntity collectionFieldType = new FieldTypeBuilder().withReference
                ("reasons-51503ee8-ac6d-4b57-845e-4806332a9820").withCollectionFieldType(FieldTypeBuilder
                .textFieldType()).buildCollection();

        CaseFieldEntity collectionField = new CaseFieldEntity();
        collectionField.setReference("Aliases");
        collectionField.setFieldType(collectionFieldType);
        return collectionField;
    }

    private CaseFieldEntity newCollectionOfComplexField() {
        FieldTypeEntity collectionFieldType = new FieldTypeBuilder().withReference
                ("reasons-51503ee8-ac6d-4b57-845e-4806332a9820").withCollectionFieldType(newComplexType()).buildCollection();

        CaseFieldEntity collectionField = new CaseFieldEntity();
        collectionField.setReference("Aliases");
        collectionField.setFieldType(collectionFieldType);
        return collectionField;
    }

    private FieldTypeEntity newComplexType() {
        FieldTypeBuilder complexType = new FieldTypeBuilder().withReference("Person");
        complexType.addComplexField("forename", FieldTypeBuilder.textFieldType());
        return complexType.buildComplex();
    }
}