package uk.gov.hmcts.ccd.definition.store.repository;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.ccd.definition.store.repository.entity.ComplexFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldTypeListItemEntity;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils.PREDEFINED_COMPLEX_ADDRESS_GLOBAL;
import static uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils.PREDEFINED_COMPLEX_ADDRESS_GLOBAL_UK;
import static uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils.PREDEFINED_COMPLEX_ADDRESS_UK;
import static uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils.PREDEFINED_COMPLEX_ORDER_SUMMARY;
import static uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils.PREDEFINED_COMPLEX_ORGANISATION;
import static uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils.PREDEFINED_COMPLEX_ORGANISATION_POLICY;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {
    SanityCheckApplication.class,
    TestConfiguration.class
})
@TestPropertySource(locations = "classpath:test.properties")
@Transactional
public class FieldTypeRepositoryTest {

    @Autowired
    private FieldTypeRepository fieldTypeRepository;

    private VersionedDefinitionRepositoryDecorator<FieldTypeEntity, Integer> versionedFieldTypeRepository;
    private static FieldTypeEntity textBaseType;

    @Before
    public void setup() {
        versionedFieldTypeRepository = new VersionedDefinitionRepositoryDecorator<>(fieldTypeRepository);
        textBaseType = canRetrieveBaseType();
    }

    @Test
    public void canExtendBaseType() {

        final FieldTypeEntity newType = new FieldTypeEntity();
        newType.setBaseFieldType(textBaseType);
        newType.setReference("ExtendedText");
        newType.setMinimum("21");
        newType.setMaximum("42");
        newType.setRegularExpression(".+");

        final FieldTypeEntity saved = versionedFieldTypeRepository.save(newType);

        final Optional<FieldTypeEntity> optionalFieldTypeEntity = fieldTypeRepository.findById(saved.getId());
        FieldTypeEntity persistedType = optionalFieldTypeEntity.get();
        assertNotNull(persistedType);
        assertThat(persistedType.getReference(), equalTo("ExtendedText"));
        assertThat(persistedType.getVersion(), equalTo(1));
        assertThat(persistedType.getBaseFieldType().getReference(), equalTo("Text"));
        assertThat(persistedType.getMinimum(), equalTo("21"));
        assertThat(persistedType.getMaximum(), equalTo("42"));
        assertThat(persistedType.getRegularExpression(), equalTo(".+"));
    }

    @Test
    public void canCreateFixedListType() {
        final Optional<FieldTypeEntity> fixedListType = fieldTypeRepository.findFirstByReferenceOrderByVersionDesc("FixedList");

        final FieldTypeEntity newType = new FieldTypeEntity();
        newType.setBaseFieldType(fixedListType.get());
        newType.setReference("ExampleList");

        final FieldTypeListItemEntity option1 = new FieldTypeListItemEntity();
        option1.setValue("Option1");
        option1.setLabel("Option 1");
        final FieldTypeListItemEntity option2 = new FieldTypeListItemEntity();
        option2.setValue("Option2");
        option2.setLabel("Option 2");

        newType.addListItems(Arrays.asList(option1, option2));

        final FieldTypeEntity saved = versionedFieldTypeRepository.save(newType);

        final Optional<FieldTypeEntity> optionalFieldTypeEntity = fieldTypeRepository.findById(saved.getId());
        FieldTypeEntity persistedType = optionalFieldTypeEntity.get();
        assertNotNull(persistedType);
        assertThat(persistedType.getReference(), equalTo("ExampleList"));
        assertThat(persistedType.getVersion(), equalTo(1));
        assertThat(persistedType.getBaseFieldType().getReference(), equalTo("FixedList"));
        assertThat(persistedType.getMinimum(), is(nullValue()));
        assertThat(persistedType.getMaximum(), is(nullValue()));
        assertThat(persistedType.getRegularExpression(), is(nullValue()));
        assertThat(persistedType.getListItems(), hasSize(2));

        final FieldTypeListItemEntity item0 = persistedType.getListItems().get(0);
        assertThat(item0.getValue(), equalTo("Option1"));
        assertThat(item0.getLabel(), equalTo("Option 1"));

        final FieldTypeListItemEntity item1 = persistedType.getListItems().get(1);
        assertThat(item1.getValue(), equalTo("Option2"));
        assertThat(item1.getLabel(), equalTo("Option 2"));
    }

    @Test
    public void canCreateCollectionType() {
        final Optional<FieldTypeEntity> collectionType = fieldTypeRepository.findFirstByReferenceOrderByVersionDesc("Collection");

        final FieldTypeEntity newType = new FieldTypeEntity();
        newType.setBaseFieldType(collectionType.get());
        newType.setReference("CollectionOfTexts");
        newType.setMinimum("1");
        newType.setMaximum("3");
        newType.setCollectionFieldType(textBaseType);

        final FieldTypeEntity saved = versionedFieldTypeRepository.save(newType);

        final Optional<FieldTypeEntity> optionalFieldTypeEntity = fieldTypeRepository.findById(saved.getId());
        FieldTypeEntity persistedType = optionalFieldTypeEntity.get();
        assertNotNull(persistedType);
        assertThat(persistedType.getReference(), equalTo("CollectionOfTexts"));
        assertThat(persistedType.getVersion(), equalTo(1));
        assertThat(persistedType.getBaseFieldType().getReference(), equalTo("Collection"));
        assertThat(persistedType.getMinimum(), equalTo("1"));
        assertThat(persistedType.getMaximum(), equalTo("3"));
        assertThat(persistedType.getRegularExpression(), is(nullValue()));
        assertThat(persistedType.getCollectionFieldType().getReference(), equalTo("Text"));
    }

    @Test
    public void canCreateComplexType() {
        final Optional<FieldTypeEntity> complexType = fieldTypeRepository.findFirstByReferenceOrderByVersionDesc("Complex");

        final FieldTypeEntity newType = new FieldTypeEntity();
        newType.setBaseFieldType(complexType.get());
        newType.setReference("Address");

        final ComplexFieldEntity field1 = new ComplexFieldEntity();
        field1.setReference("Line1");
        field1.setLabel("Line 1");
        field1.setSecurityClassification(SecurityClassification.PUBLIC);
        field1.setFieldType(textBaseType);
        final ComplexFieldEntity field2 = new ComplexFieldEntity();
        field2.setReference("Line2");
        field2.setLabel("Line 2");
        field2.setSecurityClassification(SecurityClassification.PRIVATE);
        field2.setFieldType(textBaseType);
        field2.setShowCondition("aShowCondition");

        newType.addComplexFields(Arrays.asList(field1, field2));

        final FieldTypeEntity saved = versionedFieldTypeRepository.save(newType);

        final Optional<FieldTypeEntity> optionalFieldTypeEntity = fieldTypeRepository.findById(saved.getId());
        FieldTypeEntity persistedType = optionalFieldTypeEntity.get();
        assertNotNull(persistedType);
        assertThat(persistedType.getReference(), equalTo("Address"));
        assertThat(persistedType.getVersion(), equalTo(1));
        assertThat(persistedType.getBaseFieldType().getReference(), equalTo("Complex"));
        assertThat(persistedType.getMinimum(), is(nullValue()));
        assertThat(persistedType.getMaximum(), is(nullValue()));
        assertThat(persistedType.getRegularExpression(), is(nullValue()));
        assertThat(persistedType.getComplexFields(), hasSize(2));

        final ComplexFieldEntity persistedField1 = persistedType.getComplexFields().get(0);
        assertThat(persistedField1.getReference(), equalTo("Line1"));
        assertThat(persistedField1.getLabel(), equalTo("Line 1"));
        assertThat(persistedField1.getSecurityClassification(), equalTo(SecurityClassification.PUBLIC));
        assertThat(persistedField1.getFieldType().getReference(), equalTo("Text"));

        final ComplexFieldEntity persistedField2 = persistedType.getComplexFields().get(1);
        assertThat(persistedField2.getReference(), equalTo("Line2"));
        assertThat(persistedField2.getLabel(), equalTo("Line 2"));
        assertThat(persistedField2.getSecurityClassification(), equalTo(SecurityClassification.PRIVATE));
        assertThat(persistedField2.getFieldType().getReference(), equalTo("Text"));
        assertThat(persistedField2.getShowCondition(), equalTo("aShowCondition"));
    }

    @Test
    public void returnEmptyOptional_whenTypeDoesNotExistForReference() {
        assertFalse(fieldTypeRepository.findBaseType("NonExistantFieldType").isPresent());
    }

    @Test
    public void returnEmptyOptional_whenNonBaseTypeExistsForReference() {
        FieldTypeEntity fieldTypeEntity = new FieldTypeEntity();
        fieldTypeEntity.setReference("NonBaseType");
        fieldTypeEntity.setBaseFieldType(textBaseType);

        versionedFieldTypeRepository.save(fieldTypeEntity);

        assertFalse(fieldTypeRepository.findBaseType("NonBaseType").isPresent());
    }

    @Test
    public void returnListOfPreDefinedComplexTypes() {

        FieldTypeEntity notPredefined = new FieldTypeEntity();
        notPredefined.setReference("NotPredefinedComplexType");
        versionedFieldTypeRepository.save(notPredefined);

        List<FieldTypeEntity> predefinedComplexTypes = fieldTypeRepository.findPredefinedComplexTypes();

        assertEquals(7, predefinedComplexTypes.size());

        assertThat(predefinedComplexTypes, hasItems(
                fieldTypeWithReference(PREDEFINED_COMPLEX_ADDRESS_GLOBAL),
                fieldTypeWithReference(PREDEFINED_COMPLEX_ADDRESS_UK),
                fieldTypeWithReference(PREDEFINED_COMPLEX_ADDRESS_GLOBAL_UK),
                fieldTypeWithReference(PREDEFINED_COMPLEX_ORDER_SUMMARY),
                fieldTypeWithReference(PREDEFINED_COMPLEX_ORGANISATION),
                fieldTypeWithReference(PREDEFINED_COMPLEX_ORGANISATION_POLICY)
            )
        );
    }

    private Matcher fieldTypeWithReference(String reference) {
        return new BaseMatcher() {
            @Override
            public boolean matches(Object o) {
                return o instanceof FieldTypeEntity
                    && ((FieldTypeEntity) o).getReference().equals(reference);
            }

            @Override
            public void describeTo(Description description) {

            }
        };
    }

    private FieldTypeEntity canRetrieveBaseType() {
        final Optional<FieldTypeEntity> textType = fieldTypeRepository.findFirstByReferenceOrderByVersionDesc("Text");

        assertThat(textType.isPresent(), is(Boolean.TRUE));

        final FieldTypeEntity typeEntity = textType.get();
        assertThat(typeEntity.getReference(), equalTo("Text"));
        assertThat(typeEntity.getVersion(), equalTo(1));
        assertThat(typeEntity.getBaseFieldType(), is(nullValue()));
        assertThat(typeEntity.getCollectionFieldType(), is(nullValue()));
        assertThat(typeEntity.getComplexFields(), is(empty()));

        return typeEntity;
    }
}
