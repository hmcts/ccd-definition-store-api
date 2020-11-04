package uk.gov.hmcts.ccd.definition.store.repository;

import org.hamcrest.core.Is;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.ComplexFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.FieldTypeListItemEntity;

import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static uk.gov.hmcts.ccd.definition.store.repository.CaseFieldEntityUtil.parseParentCodes;

public class CaseFieldEntityUtilTest {

    private CaseFieldEntityUtil caseFieldEntityUtil = new CaseFieldEntityUtil();

    @Test
    public void createsValidPossibilities() {

        List<CaseFieldEntity> fieldTypeEntity = asList(caseFieldEntity("field1"),
            caseFieldEntity("field2", exampleFieldTypeEntityWithComplexFields()),
            caseFieldEntity("field3"));

        List<String> result = caseFieldEntityUtil.buildDottedComplexFieldPossibilities(fieldTypeEntity);

        assertThat(result.size(), is(6));
        assertTrue(result.contains("field1"));
        assertTrue(result.contains("field2.NamePrefix"));
        assertTrue(result.contains("field2.FirstName"));
        assertTrue(result.contains("field2.MiddleName"));
        assertTrue(result.contains("field2.LastNameWithSomeCplxFields.LastName"));
        assertTrue(result.contains("field3"));
    }

    @Test
    public void createsValidPossibilitiesForComplexFieldInACollection() {

        List<CaseFieldEntity> fieldTypeEntity = asList(caseFieldEntity("field1"),
            caseFieldEntity("field2", exampleCollectionFieldTypeEntityWithComplexFields()),
            caseFieldEntity("field3"));

        List<String> result = caseFieldEntityUtil.buildDottedComplexFieldPossibilities(fieldTypeEntity);

        assertThat(result.size(), is(6));
        assertTrue(result.contains("field1"));
        assertTrue(result.contains("field2.NamePrefix"));
        assertTrue(result.contains("field2.FirstName"));
        assertTrue(result.contains("field2.MiddleName"));
        assertTrue(result.contains("field2.LastNameWithSomeCplxFields.LastName"));
        assertTrue(result.contains("field3"));
    }

    @Test
    public void testparseParentCodes() {
        String listElementCodes0 = ".awesome";
        final List<String> parentCodes0 = parseParentCodes(listElementCodes0);
        assertThat(parentCodes0, empty());

        String listElementCodes1 = "some";
        final List<String> parentCodes1 = parseParentCodes(listElementCodes1);
        assertThat(parentCodes1, empty());

        String listElementCodes2 = "some.thing";
        final List<String> parentCodes2 = parseParentCodes(listElementCodes2);
        assertThat(parentCodes2, not(empty()));
        assertThat(parentCodes2.size(), Is.is(1));
        assertThat(parentCodes2.get(0), Is.is("some"));

        String listElementCodes3 = "some.thing.coming";
        final List<String> parentCodes3 = parseParentCodes(listElementCodes3);
        assertThat(parentCodes3, not(empty()));
        assertThat(parentCodes3.size(), Is.is(2));
        assertThat(parentCodes3.get(0), Is.is("some.thing"));
        assertThat(parentCodes3.get(1), Is.is("some"));

        String listElementCodes4 = "some.thing.coming.this.way";
        final List<String> parentCodes4 = parseParentCodes(listElementCodes4);
        assertThat(parentCodes4, not(empty()));
        assertThat(parentCodes4.size(), Is.is(4));
        assertThat(parentCodes4.get(0), Is.is("some.thing.coming.this"));
        assertThat(parentCodes4.get(1), Is.is("some.thing.coming"));
        assertThat(parentCodes4.get(2), Is.is("some.thing"));
        assertThat(parentCodes4.get(3), Is.is("some"));
    }


    private static FieldTypeEntity exampleCollectionFieldTypeEntityWithComplexFields() {
        return collectionFieldTypeEntity("field2-9602da3a-137a-43c2-9c0c-676ca00a5443",
            exampleFieldTypeEntityWithComplexFields());
    }

    private static FieldTypeEntity exampleFieldTypeEntityWithComplexFields() {
        return fieldTypeEntity("FullName",
            asList(
                complexFieldEntity(
                    "NamePrefix",
                    fixedListFieldTypeEntity(
                        "FixedList-PreFix",
                        asList(
                            fieldTypeListItemEntity("Mr.", "Mr."),
                            fieldTypeListItemEntity("Mrs.", "Mrs.")))),
                complexFieldEntity("FirstName", fieldTypeEntity("Text", emptyList())),
                complexFieldEntity("MiddleName", fieldTypeEntity("Text", emptyList())),
                complexFieldEntity("LastNameWithSomeCplxFields", fieldTypeEntity("FullName1",
                    asList(
                        complexFieldEntity("LastName", fieldTypeEntity("Text", emptyList()))
                    )))
            ));
    }

    private static CaseFieldEntity caseFieldEntity(String reference, FieldTypeEntity fieldTypeEntity) {
        CaseFieldEntity caseFieldEntity = new CaseFieldEntity();
        caseFieldEntity.setReference(reference);
        caseFieldEntity.setFieldType(fieldTypeEntity);
        return caseFieldEntity;
    }

    private static CaseFieldEntity caseFieldEntity(String caseFieldReference) {
        CaseFieldEntity caseFieldEntity = new CaseFieldEntity();
        caseFieldEntity.setReference(caseFieldReference);
        caseFieldEntity.setFieldType(fieldTypeEntity("TEXT", emptyList()));
        return caseFieldEntity;
    }

    private static FieldTypeEntity fieldTypeEntity(String reference,
                                                   List<ComplexFieldEntity> complexFieldEntityList) {
        FieldTypeEntity fieldTypeEntity = new FieldTypeEntity();
        fieldTypeEntity.setReference(reference);
        fieldTypeEntity.addComplexFields(complexFieldEntityList);
        return fieldTypeEntity;
    }

    private static FieldTypeEntity collectionFieldTypeEntity(String reference, FieldTypeEntity collectionFieldType) {
        FieldTypeEntity fieldTypeEntity = new FieldTypeEntity();
        fieldTypeEntity.setReference(reference);
        fieldTypeEntity.setCollectionFieldType(collectionFieldType);
        return fieldTypeEntity;
    }

    private static ComplexFieldEntity complexFieldEntity(String reerence, FieldTypeEntity fieldTypeEntity) {
        ComplexFieldEntity complexFieldEntity = new ComplexFieldEntity();
        complexFieldEntity.setReference(reerence);
        complexFieldEntity.setFieldType(fieldTypeEntity);
        return complexFieldEntity;
    }

    private static FieldTypeEntity fixedListFieldTypeEntity(String reference,
                                                            List<FieldTypeListItemEntity> listItemEntities) {
        FieldTypeEntity fieldTypeEntity = new FieldTypeEntity();
        fieldTypeEntity.setReference(reference);
        fieldTypeEntity.addListItems(listItemEntities);
        return fieldTypeEntity;
    }

    private static FieldTypeListItemEntity fieldTypeListItemEntity(String label, String value) {
        FieldTypeListItemEntity fieldTypeListItemEntity = new FieldTypeListItemEntity();
        fieldTypeListItemEntity.setLabel(label);
        fieldTypeListItemEntity.setValue(value);
        return fieldTypeListItemEntity;
    }
}
