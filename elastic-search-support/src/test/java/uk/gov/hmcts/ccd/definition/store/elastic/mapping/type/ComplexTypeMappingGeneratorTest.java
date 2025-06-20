package uk.gov.hmcts.ccd.definition.store.elastic.mapping.type;

import uk.gov.hmcts.ccd.definition.store.elastic.TestUtils;
import uk.gov.hmcts.ccd.definition.store.elastic.mapping.AbstractMapperTest;
import uk.gov.hmcts.ccd.definition.store.elastic.mapping.StubTypeMappingGenerator;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.ComplexFieldEntity;
import uk.gov.hmcts.ccd.definition.store.utils.CaseFieldBuilder;
import uk.gov.hmcts.ccd.definition.store.utils.FieldTypeBuilder;

import java.util.Iterator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.definition.store.elastic.hamcresutil.IsEqualJSON.equalToJSONInFile;
import static uk.gov.hmcts.ccd.definition.store.utils.CaseFieldBuilder.newField;
import static uk.gov.hmcts.ccd.definition.store.utils.FieldTypeBuilder.labelFieldType;
import static uk.gov.hmcts.ccd.definition.store.utils.FieldTypeBuilder.newType;
import static uk.gov.hmcts.ccd.definition.store.utils.FieldTypeBuilder.textFieldType;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ComplexTypeMappingGeneratorTest extends AbstractMapperTest implements TestUtils {

    @InjectMocks
    private ComplexTypeMappingGenerator complexTypeMapper;

    @BeforeEach
    void setUp() {
        super.setup();

        when(config.getSecurityClassificationMapping()).thenReturn("securityClassificationMapping");
        addMappingGenerator(new StubTypeMappingGenerator(null, "Text", "dataMapping", "dataClassificationMapping"));
        addMappingGenerator(new StubTypeMappingGenerator(null, "Complex", "dataMappingForComplexType",
            "dataClassificationMappingForComplexType"));
        complexTypeMapper.inject(stubTypeMappersManager);
    }

    @Test
    void shouldGenerateMappingForAllComplexTypeFields() {
        CaseFieldEntity complexField = newComplexField();

        String result = complexTypeMapper.doDataMapping(complexField);

        assertThat(result, equalToJSONInFile(
            readFileFromClasspath("json/type_mapping_complex_type.json")));
    }

    @Test
    void shouldGenerateClassificationMappingForAllComplexTypeFields() {
        CaseFieldEntity complexField = newComplexField();

        String result = complexTypeMapper.doDataClassificationMapping(complexField);

        assertThat(result, equalToJSONInFile(
            readFileFromClasspath("json/type_mapping_classification_complex_type.json")));
    }

    @Test
    void shouldGenerateDataMappingForNonSearchableComplexType() {
        CaseFieldEntity complexField = newComplexField();
        complexField.setSearchable(false);

        String result = complexTypeMapper.doDataMapping(complexField);

        assertThat(result, is(disabledMapping));
    }

    @Test
    void shouldGenerateDataMappingForComplexTypeWithNonSearchableNestedField() {
        CaseFieldEntity complexField = newComplexField();
        Iterator<ComplexFieldEntity> iterator = complexField.getFieldType().getComplexFields().iterator();
        for (int i = 0; iterator.hasNext(); i++) {
            ComplexFieldEntity item = iterator.next();
            if (i == 1) {
                item.setSearchable(false);
                break;
            }
        }
        String result = complexTypeMapper.doDataMapping(complexField);

        assertThat(result, equalToJSONInFile(
            readFileFromClasspath("json/type_mapping_complex_type_disabled.json")));
    }


    @Test
    void shouldGenerateDataClassificationMappingForNonSearchableComplexType() {
        CaseFieldEntity complexField = newComplexField();
        complexField.setSearchable(false);

        String result = complexTypeMapper.doDataClassificationMapping(complexField);

        assertThat(result, is(disabledMapping));
    }

    private CaseFieldEntity newComplexField() {
        CaseFieldBuilder complexField = newField("complexField", "SomeComplexType1");
        complexField.addFieldToComplex("field1", textFieldType());
        complexField.addFieldToComplex("field2", textFieldType());
        complexField.addFieldToComplex("field3", labelFieldType());

        FieldTypeBuilder complexType = newType("SomeComplexType2");
        complexField.addFieldToComplex("nestedComplexField", complexType.buildComplex());

        return complexField.buildComplex();
    }
}
