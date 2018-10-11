package uk.gov.hmcts.ccd.definition.store.elastic.mapping.type;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.ccd.definition.store.elastic.TestUtils;
import uk.gov.hmcts.ccd.definition.store.elastic.mapping.AbstractMapperTest;
import uk.gov.hmcts.ccd.definition.store.elastic.mapping.StubTypeMappingGenerator;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.utils.CaseFieldBuilder;
import uk.gov.hmcts.ccd.definition.store.utils.FieldTypeBuilder;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.definition.store.elastic.hamcresutil.IsEqualJSON.equalToJSONInFile;
import static uk.gov.hmcts.ccd.definition.store.utils.CaseFieldBuilder.newField;
import static uk.gov.hmcts.ccd.definition.store.utils.FieldTypeBuilder.labelFieldType;
import static uk.gov.hmcts.ccd.definition.store.utils.FieldTypeBuilder.newType;
import static uk.gov.hmcts.ccd.definition.store.utils.FieldTypeBuilder.textFieldType;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ComplexTypeMappingGeneratorTest extends AbstractMapperTest implements TestUtils {

    @InjectMocks
    private ComplexTypeMappingGenerator complexTypeMapper;

    @BeforeEach
    public void setUp() {
        super.setup();

        when(config.getSecurityClassificationMapping()).thenReturn("securityClassificationMapping");
        addMappingGenerator(new StubTypeMappingGenerator("Text", "dataMapping","dataClassificationMapping"));
        addMappingGenerator(new StubTypeMappingGenerator("Complex", "dataMappingForComplexType",
                "dataClassificationMappingForComplexType"));
        complexTypeMapper.inject(stubTypeMappersManager);
    }

    @Test
    public void shouldGenerateMappingForAllComplexTypeFields() {
        CaseFieldEntity complexField = newComplexField();

        String result = complexTypeMapper.dataMapping(complexField);

        assertThat(result, equalToJSONInFile(
                readFileFromClasspath("json/type_mapping_complex_type.json")));
    }

    @Test
    public void shouldGenerateClassificationMappingForAllComplexTypeFields() {
        CaseFieldEntity complexField = newComplexField();

        String result = complexTypeMapper.dataClassificationMapping(complexField);

        assertThat(result, equalToJSONInFile(
                readFileFromClasspath("json/type_mapping_classification_complex_type.json")));
    }

    private CaseFieldEntity newComplexField() {
        CaseFieldBuilder complexField = newField("complexField", "SomeComplexType1");
        complexField.withComplexField("field1", textFieldType());
        complexField.withComplexField("field2", textFieldType());
        complexField.withComplexField("field3", labelFieldType());

        FieldTypeBuilder complexType = newType("SomeComplexType2");
        complexField.withComplexField("nestedComplexField", complexType.buildComplex());

        return complexField.buildComplex();
    }
}
