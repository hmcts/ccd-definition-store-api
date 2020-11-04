package uk.gov.hmcts.ccd.definition.store.excel.domain.definition.model;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Object representation of an imported Case Definition workbook sheet. A <code>DefinitionSheet</code> comprises i) the
 * name of the definition data, e.g. Jurisdiction, CaseType; ii) a list of {@link DefinitionDataItem}s, one per each row
 * of data.
 *
 * @author Daniel Lam (A533913)
 */
@Getter
@Setter
public class DefinitionSheet {
    private String name;
    private List<DefinitionDataItem> dataItems;

    /**
     * Constructs a new <code>DefinitionSheet</code>.
     */
    public DefinitionSheet() {
        dataItems = new ArrayList<>();
    }

    /**
     * Adds a <code>DefinitionDataItem</code> to the <code>DefinitionSheet</code>.
     *
     * @param dataItem An item (i.e. a row) of definition data (comprising a number of attributes)
     */
    public void addDataItem(DefinitionDataItem dataItem) {
        dataItems.add(dataItem);
    }
}
