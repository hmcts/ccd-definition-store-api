package uk.gov.hmcts.ccd.definition.store.excel.domain.definition.model;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.tuple.Pair;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName;

import java.util.ArrayList;
import java.util.List;

/**
 * Object representation of a row of Case Definition data from an imported workbook sheet.
 * A <code>DefinitionDataItem</code> uses a list of {@link Pair}s to represent a row of data, which comprises the
 * various attributes of a data item.
 *
 * @author Daniel Lam (A533913)
 */
@Getter
@Setter
public class DefinitionDataItem {
    private List<Pair<String, Object>> attributes;

    /**
     * Constructs an empty <code>DefinitionDataItem</code>.
     */
    public DefinitionDataItem() {
        attributes = new ArrayList<>();
    }

    /**
     * Adds an attribute to the <code>DefinitionDataItem</code>.
     *
     * @param key   The attribute key; expected to be a column header from a sheet of definition data, e.g. CaseType.
     * @param value The attribute value, e.g. ProbateGrant
     */
    public void addAttribute(String key, Object value) {
        attributes.add(Pair.of(key, value));
    }

    public void addAttribute(ColumnName key, Object value) {
        attributes.add(Pair.of(key.toString(), value));
    }

    /**
     * Find the attribute with the given name withing the item.
     *
     * @param name - name of attribute to find
     * @return attribute with the matching name (or null if it wasn't found)
     */
    public Object findAttribute(String name) {
        for (Pair<String, Object> attribute : attributes) {
            if (attribute.getKey().equals(name)) {
                return attribute.getValue();
            }
        }
        return null;
    }
}
