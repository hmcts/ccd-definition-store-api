package uk.gov.hmcts.ccd.definition.store.excel.parser.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

/**
 * Object representation of an imported Case Definition workbook sheet. A <code>DefinitionSheet</code> comprises i) the
 * name of the definition data, e.g. Jurisdiction, CaseType; ii) a list of {@link DefinitionDataItem}s, one per each row
 * of data.
 */

public class DefinitionSheet {


    private String name;
    private final List<DefinitionDataItem> dataItems = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Adds a <code>DefinitionDataItem</code> to the <code>DefinitionSheet</code>.
     *
     * @param dataItem An item (i.e. a row) of definition data (comprising a number of attributes)
     */
    public void addDataItem(DefinitionDataItem dataItem) {
        dataItems.add(dataItem);
    }

    public List<DefinitionDataItem> getDataItems() {
        return dataItems;
    }

    public Map<String, List<DefinitionDataItem>> groupDataItemsById() {
        /**
         * We assume complex types are declared in the spreadsheet after other complex type they depend on.
         * So the grouping must preserve this ordering
         */
        return dataItems.stream().collect(groupingBy(DefinitionDataItem::getId, LinkedHashMap::new, toList()));
    }

    public Map<String, List<DefinitionDataItem>> groupDataItemsByCaseType() {
        return dataItems.stream().collect(groupingBy(DefinitionDataItem::getCaseTypeId));
    }

    public static boolean isDisplayContextParameter(
        String item,
        DisplayContextParameter.DisplayContextParameterValues displayContextParameterValue) {
        return item.startsWith(displayContextParameterValue.toString());
    }
}
