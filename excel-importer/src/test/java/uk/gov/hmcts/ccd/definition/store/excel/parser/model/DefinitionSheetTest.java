package uk.gov.hmcts.ccd.definition.store.excel.parser.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.aMapWithSize;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.iterableWithSize;

@DisplayName("DefinitionSheet")
class DefinitionSheetTest {

    private DefinitionSheet sheet;

    @BeforeEach
    void setUp() {
        sheet = new DefinitionSheet();
        sheet.setName(SheetName.FIXED_LISTS.getName());
    }

    @Nested
    @DisplayName("groupDataItemsByCaseTypeAndId")
    class GroupDataItemsByCaseTypeAndId {

        @Test
        @DisplayName("returns empty map when no data items")
        void returnsEmptyMapWhenNoDataItems() {
            Map<String, List<DefinitionDataItem>> result = sheet.groupDataItemsByCaseTypeAndId();

            assertThat(result.isEmpty(), is(true));
        }

        @Test
        @DisplayName("groups by id only when caseTypeId is blank")
        void groupsByIdOnlyWhenCaseTypeIdBlank() {
            DefinitionDataItem item1 = dataItem("list1", null, "code1", "Label1", 1);
            DefinitionDataItem item2 = dataItem("list1", null, "code2", "Label2", 2);
            sheet.addDataItem(item1);
            sheet.addDataItem(item2);

            Map<String, List<DefinitionDataItem>> result = sheet.groupDataItemsByCaseTypeAndId();

            assertThat(result, aMapWithSize(1));
            assertThat(result, hasKey("list1"));
            assertThat(result.get("list1"), iterableWithSize(2));
        }

        @Test
        @DisplayName("groups by id and caseTypeId when caseTypeId is set")
        void groupsByIdAndCaseTypeIdWhenSet() {
            DefinitionDataItem item1 = dataItem("list1", "CaseTypeA", "code1", "Label1", 1);
            DefinitionDataItem item2 = dataItem("list1", "CaseTypeA", "code2", "Label2", 2);
            sheet.addDataItem(item1);
            sheet.addDataItem(item2);

            Map<String, List<DefinitionDataItem>> result = sheet.groupDataItemsByCaseTypeAndId();

            assertThat(result, aMapWithSize(1));
            assertThat(result, hasKey("list1-CaseTypeA"));
            assertThat(result.get("list1-CaseTypeA"), iterableWithSize(2));
        }

        @Test
        @DisplayName("creates separate groups for same id different caseTypeId")
        void separateGroupsForSameIdDifferentCaseTypeId() {
            DefinitionDataItem itemA1 = dataItem("mylist", "CaseTypeA", "c1", "L1", 1);
            DefinitionDataItem itemB1 = dataItem("mylist", "CaseTypeB", "c1", "L1", 1);
            sheet.addDataItem(itemA1);
            sheet.addDataItem(itemB1);

            Map<String, List<DefinitionDataItem>> result = sheet.groupDataItemsByCaseTypeAndId();

            assertThat(result, aMapWithSize(2));
            assertThat(result, hasKey("mylist-CaseTypeA"));
            assertThat(result, hasKey("mylist-CaseTypeB"));
            assertThat(result.get("mylist-CaseTypeA"), iterableWithSize(1));
            assertThat(result.get("mylist-CaseTypeB"), iterableWithSize(1));
        }

        @Test
        @DisplayName("uses id only when caseTypeId is blank string")
        void usesIdOnlyWhenCaseTypeIdBlankString() {
            DefinitionDataItem item = dataItem("list1", "", "code1", "L1", 1);
            sheet.addDataItem(item);

            Map<String, List<DefinitionDataItem>> result = sheet.groupDataItemsByCaseTypeAndId();

            assertThat(result, hasKey("list1"));
            assertThat(result.get("list1"), iterableWithSize(1));
        }

        @Test
        @DisplayName("preserves insertion order of keys (LinkedHashMap)")
        void preservesInsertionOrder() {
            sheet.addDataItem(dataItem("first", null, "c1", "L1", 1));
            sheet.addDataItem(dataItem("second", "CT", "c1", "L1", 1));
            sheet.addDataItem(dataItem("third", null, "c1", "L1", 1));

            Map<String, List<DefinitionDataItem>> result = sheet.groupDataItemsByCaseTypeAndId();

            List<String> keys = List.copyOf(result.keySet());
            assertThat(keys, contains("first", "second-CT", "third"));
        }
    }

    private static DefinitionDataItem dataItem(String id, String caseTypeId,
                                               String listCode, String listLabel, int order) {
        DefinitionDataItem item = new DefinitionDataItem(SheetName.FIXED_LISTS.getName());
        item.addAttribute(ColumnName.ID.toString(), id);
        if (caseTypeId != null) {
            item.addAttribute(ColumnName.CASE_TYPE_ID.toString(), caseTypeId);
        }
        item.addAttribute(ColumnName.LIST_ELEMENT_CODE.toString(), listCode);
        item.addAttribute(ColumnName.LIST_ELEMENT.toString(), listLabel);
        item.addAttribute(ColumnName.DISPLAY_ORDER.toString(), order);
        return item;
    }
}
