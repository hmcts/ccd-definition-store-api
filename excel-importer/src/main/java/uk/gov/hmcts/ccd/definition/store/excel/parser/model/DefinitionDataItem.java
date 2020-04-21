package uk.gov.hmcts.ccd.definition.store.excel.parser.model;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.tuple.Pair;
import uk.gov.hmcts.ccd.definition.store.excel.endpoint.exception.MapperException;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName;
import uk.gov.hmcts.ccd.definition.store.repository.DisplayContext;
import uk.gov.hmcts.ccd.definition.store.repository.SecurityClassification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.isBlank;

public class DefinitionDataItem {

    private final String sheetName;
    private final List<Pair<String, Object>> attributes;

    private static final String NO_COLUMN = "Couldn't find the column %s in the sheet %s";
    private static final String INVALID_VALUE_COLUMN = "Invalid value '%s' is found in column '%s' in the sheet '%s'";
    private static final String
        INVALID_OR_BLANK_COLUMN =
        "There's a missing value in the column '%s' or invalid value in the sheet '%s'";

    public DefinitionDataItem(String sheetName) {
        this.sheetName = sheetName;
        attributes = new ArrayList<>();
    }

    public void addAttribute(ColumnName columnName, Object value) {
        attributes.add(Pair.of(columnName.toString(), value));
    }

    public void addAttribute(String key, Object value) {
        attributes.add(Pair.of(key, value));
    }

    public Object findAttribute(ColumnName columnName) {
        final String name = columnName.toString();
        final Object
            result =
            attributes.stream()
                      .filter(attribute -> attribute.getKey().equalsIgnoreCase(name))
                      .findFirst()
                      .map(p -> p.getValue())
                      .orElse(null);

        if (ColumnName.isRequired(SheetName.forName(sheetName), columnName)) {
            if (result == null) {
                throw new MapperException(String.format(NO_COLUMN, name, sheetName));
            }
            if (isBlank(result.toString())) {
                throw new MapperException(String.format(INVALID_OR_BLANK_COLUMN, name, sheetName));
            }
        }
        return result;
    }

    public String getString(ColumnName columnName) {
        final Object attribute = findAttribute(columnName);
        if (attribute == null) {
            return null;
        } else {
            return StringUtils.trim(attribute.toString());
        }
    }

    public Integer getInteger(ColumnName columnName) {
        final String attribute = getString(columnName);
        if (attribute == null) {
            return null;
        } else {
            if (NumberUtils.isCreatable(attribute)) {
                return NumberUtils.createDouble(attribute).intValue();
            } else {
                throw new MapperException(String.format(INVALID_VALUE_COLUMN, attribute, columnName, sheetName));
            }
        }
    }

    public BigDecimal getBigDecimal(ColumnName columnName) {
        final String attribute = getString(columnName);
        if (attribute == null) {
            return null;
        } else {
            if (NumberUtils.isCreatable(attribute)) {
                return BigDecimal.valueOf(NumberUtils.createDouble(attribute));
            } else {
                throw new MapperException(String.format(INVALID_VALUE_COLUMN, attribute, columnName, sheetName));
            }
        }
    }

    public Date getDate(ColumnName columnName) {
        final Object attribute = findAttribute(columnName);
        if (attribute == null) {
            return null;
        } else {
            if (attribute instanceof Date) {
                return (Date) attribute;
            } else {
                throw new MapperException(String.format(INVALID_VALUE_COLUMN, attribute, columnName, sheetName));
            }
        }
    }

    public LocalDate getLocalDate(ColumnName columnName) {
        final Date date = getDate(columnName);

        if (null == date) {
            return null;
        }

        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    public Boolean getBoolean(ColumnName columnName) {
        final Object attribute = findAttribute(columnName);
        if (attribute == null) {
            return null;
        } else {
            switch (attribute.toString().toLowerCase()) {
                case "yes":
                    return true;
                case "y":
                    return true;
                case "t":
                    return true;
                case "true":
                    return true;
                case "no":
                    return false;
                case "n":
                    return false;
                case "false":
                    return false;
                case "f":
                    return false;
                default:
                    throw new MapperException(String.format(INVALID_VALUE_COLUMN, attribute, columnName, sheetName));
            }
        }
    }

    public SecurityClassificationColumn getSecurityClassification() {
        final String securityClassificationString = getString(ColumnName.SECURITY_CLASSIFICATION);
        final SecurityClassification securityClassification;
        try {
            securityClassification =
                securityClassificationString != null ?
                    SecurityClassification.valueOf(securityClassificationString.toUpperCase()) :
                    null;
            return new SecurityClassificationColumn(securityClassificationString, securityClassification);
        } catch (IllegalArgumentException e) {
            // SecurityClassification field on returned object will be null
            return new SecurityClassificationColumn(securityClassificationString, null);
        }
    }

    public DisplayContextColumn getDisplayContext() {
        final String displayContextString = getString(ColumnName.DISPLAY_CONTEXT);
        final DisplayContext displayContext;
        try {
            displayContext =
                displayContextString != null ? DisplayContext.valueOf(displayContextString.toUpperCase()) : null;
            return new DisplayContextColumn(displayContextString, displayContext);
        } catch (IllegalArgumentException e) {
            return new DisplayContextColumn(displayContextString, null);
        }
    }

    public String getId() {
        return getString(ColumnName.ID);
    }

    public String getCaseTypeId() {
        return getString(ColumnName.CASE_TYPE_ID);
    }

    public String getSheetName() {
        return sheetName;
    }

    public String getDisplayContextParameter() {
        return getString(ColumnName.DISPLAY_CONTEXT_PARAMETER);
    }

    public String getCaseFieldId() {
        return getString(ColumnName.CASE_FIELD_ID) != null ? getString(ColumnName.CASE_FIELD_ID) :
            new StringBuilder()
                .append(getString(ColumnName.ID))
                .append(".")
                .append(getString(ColumnName.LIST_ELEMENT_CODE))
                .toString();
    }

}
