package uk.gov.hmcts.ccd.definition.store.excel.parser;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationError;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationException;
import uk.gov.hmcts.ccd.definition.store.domain.validation.ValidationResult;
import uk.gov.hmcts.ccd.definition.store.excel.endpoint.exception.MapperException;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionDataItem;
import uk.gov.hmcts.ccd.definition.store.excel.parser.model.DefinitionSheet;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.NoCConfigEntity;

import static java.util.Optional.ofNullable;

@Slf4j
public class NoCConfigParser {

    private ParseContext parseContext;


    public NoCConfigParser(ParseContext parseContext) {
        this.parseContext = parseContext;
    }

    public Map<String, List<NoCConfigEntity>> parse(Map<String, DefinitionSheet> definitionSheets) {
        Map<String, List<DefinitionDataItem>> nocConfigsCaseType = ofNullable(definitionSheets
            .get(SheetName.NOC_CONFIG.getName()))
            .map(DefinitionSheet::groupDataItemsByCaseType)
            .orElse(Collections.emptyMap());

        checkInvalidCaseTypeIds(nocConfigsCaseType);
        Map<String, List<NoCConfigEntity>> caseTypeNoCConfigEntities = new HashMap<>();
        ValidationResult validationResult = new ValidationResult();
        parseContext.getCaseTypes()
            .stream()
            .forEach(caseType -> {
                log.debug("Parsing noc config for case type {}...", caseType.getReference());
                caseTypeNoCConfigEntities.put(caseType.getReference(),
                    ofNullable(nocConfigsCaseType.get(caseType.getReference()))
                        .map(dataItems -> dataItems
                            .stream()
                            .map(dataItem -> parseNoCConfigItems(dataItem, caseType, validationResult))
                            .collect(Collectors.toList()))
                        .orElse(Collections.emptyList()));
            });
        if (!validationResult.isValid()) {
            throw new ValidationException(validationResult);
        }

        return caseTypeNoCConfigEntities;
    }

    private void checkInvalidCaseTypeIds(Map<String, List<DefinitionDataItem>> nocConfigsCaseType) {
        Set<String> caseTypesNotDefined = ofNullable(nocConfigsCaseType.keySet())
            .orElse(Collections.emptySet())
            .stream()
            .filter(key -> parseContext
                .getCaseTypes()
                .stream()
                .noneMatch(caseTypeEntity -> caseTypeEntity.getReference().equalsIgnoreCase(key)))
            .collect(Collectors.toSet());

        if (!caseTypesNotDefined.isEmpty()) {
            throw new MapperException(String.format("Unknown Case Type(s) '%s' in worksheet '%s'",
                caseTypesNotDefined.stream().sorted().collect(Collectors.joining(",")),
                SheetName.NOC_CONFIG));
        }
    }

    private NoCConfigEntity parseNoCConfigItems(DefinitionDataItem dataItem,
                                                CaseTypeEntity caseType,
                                                ValidationResult validationResult) {
        NoCConfigEntity noCConfigEntity = new NoCConfigEntity();
        noCConfigEntity.setReasonsRequired(parseColumnValue(dataItem,
            ColumnName.REASON_REQUIRED,
            validationResult));
        noCConfigEntity.setNocActionInterpretationRequired(parseColumnValue(dataItem,
            ColumnName.NOC_ACTION_INTERPRETATION_REQUIRED,
            validationResult));
        noCConfigEntity.setCaseType(caseType);
        return noCConfigEntity;
    }

    private boolean parseColumnValue(DefinitionDataItem dataItem,
                                     ColumnName columnName,
                                     ValidationResult validationResult) {
        try {
            return dataItem.getBoolean(columnName);
        } catch (MapperException me) {
            validationResult.addError(new ValidationError(me.getMessage()) {
                @Override
                public String toString() {
                    return getDefaultMessage();
                }
            });
        }
        return false;
    }
}
