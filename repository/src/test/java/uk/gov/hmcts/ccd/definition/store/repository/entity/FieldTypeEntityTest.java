package uk.gov.hmcts.ccd.definition.store.repository.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static java.util.Collections.emptyList;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertAll;
import static uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils.BASE_DOCUMENT;
import static uk.gov.hmcts.ccd.definition.store.repository.FieldTypeUtils.BASE_TEXT;
import static uk.gov.hmcts.ccd.definition.store.utils.FieldTypeBuilder.newType;
import static uk.gov.hmcts.ccd.definition.store.utils.FieldTypeBuilder.textFieldType;

class FieldTypeEntityTest {

    @Nested
    @DisplayName("getChildren tests")
    class FieldTypeGetChildrenTests {

        @Test
        @DisplayName("should return empty list for no base field type")
        public void shouldReturnEmptyListIfTypeHasBaseFieldTypeAsNull() {
            List<ComplexFieldEntity> children = textFieldType().getChildren();

            assertThat(children, is(emptyList()));
        }

        @Test
        @DisplayName("should return empty list if base type is not Complex or Collection")
        public void shouldReturnEmptyListIfTypeIsFixedListType() {
            List<ComplexFieldEntity> children = newType("fixedList")
                .withBaseFieldType(newType("FixedListType")
                    .build())
                .build()
                .getChildren();

            assertThat(children, is(emptyList()));
        }

        @Test
        @DisplayName("should get children of collection type")
        public void shouldGetChildrenOfCollectionType() {
            FieldTypeEntity collectionType = newType("collectionType")
                .addFieldToCollection(newType("complexType")
                    .addFieldToComplex("simpleType1", newType("simpleType1")
                        .build())
                    .addFieldToComplex("simpleType2", newType("simpleType2")
                        .build())
                    .buildComplex())
                .buildCollection();

            List<ComplexFieldEntity> children = collectionType.getChildren();

            assertAll(
                () -> assertThat(children.size(), is(2)),
                () -> assertTrue(children.stream().anyMatch(e -> e.getReference().equals("simpleType1"))),
                () -> assertTrue(children.stream().anyMatch(e -> e.getReference().equals("simpleType2")))
            );
        }

        @Test
        @DisplayName("should return empty list if collection type missing children types")
        public void shouldReturnEmptyListIfCollectionTypeMissingChildrenTypes() {
            List<ComplexFieldEntity> children = newType("Collection")
                .buildCollection()
                .getChildren();

            assertThat(children, is(emptyList()));
        }

        @Test
        @DisplayName("should get children of complex type")
        public void shouldGetChildrenOfComplexType() {
            List<ComplexFieldEntity> children = newType("Complex")
                .addFieldToComplex("caseField1", newType("caseField1").build())
                .addFieldToComplex("caseField2", newType("caseField2").build())
                .buildComplex()
                .getChildren();

            assertThat(children.size(), is(2));
            assertTrue(children.stream().anyMatch(e -> e.getReference().equals("caseField1")));
            assertTrue(children.stream().anyMatch(e -> e.getReference().equals("caseField2")));
        }
    }

    @Nested
    @DisplayName("Document type")
    class DocumentType {

        @Test
        @DisplayName("should return true when field type reference is document type")
        void shouldReturnTrueWhenReferenceIsDocument() {
            FieldTypeEntity fieldTypeEntity = new FieldTypeEntity();
            fieldTypeEntity.setReference(BASE_DOCUMENT);

            assertThat(fieldTypeEntity.isDocumentType(), is(true));
        }

        @Test
        @DisplayName("should return true when base field type reference is document type")
        void shouldReturnTrueWhenBaseReferenceIsDocument() {
            FieldTypeEntity baseFieldTypeEntity = new FieldTypeEntity();
            baseFieldTypeEntity.setReference(BASE_DOCUMENT);
            FieldTypeEntity fieldTypeEntity = new FieldTypeEntity();
            fieldTypeEntity.setBaseFieldType(baseFieldTypeEntity);

            assertThat(fieldTypeEntity.isDocumentType(), is(true));
        }

        @Test
        @DisplayName("should return false when field type or base field type reference is not document type")
        void shouldReturnFalse() {
            FieldTypeEntity baseFieldTypeEntity = new FieldTypeEntity();
            baseFieldTypeEntity.setReference(BASE_TEXT);
            FieldTypeEntity fieldTypeEntity = new FieldTypeEntity();
            fieldTypeEntity.setBaseFieldType(baseFieldTypeEntity);

            assertThat(fieldTypeEntity.isDocumentType(), is(false));
        }
    }

}
