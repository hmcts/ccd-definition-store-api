package uk.gov.hmcts.ccd.definition.store.excel.validation;

import uk.gov.hmcts.ccd.definition.store.excel.parser.ParseContext;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.ColumnName;
import uk.gov.hmcts.ccd.definition.store.excel.util.mapper.SheetName;
import uk.gov.hmcts.ccd.definition.store.repository.entity.SearchCriteriaEntity;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SearchCriteriaValidator {

    private final DotNotationValidator dotNotationValidator;

    @Autowired
    public SearchCriteriaValidator(DotNotationValidator dotNotationValidator) {
        this.dotNotationValidator = dotNotationValidator;
    }

    public void validate(final List<SearchCriteriaEntity> searchCriteriaEntities,
                         final ParseContext parseContext) {

        searchCriteriaEntities.forEach(searchCriteriaEntity -> {
            String caseType = searchCriteriaEntity.getCaseType().getReference();

            validateOtherCaseReference(parseContext, caseType, searchCriteriaEntity);
        });
    }

    private void validateOtherCaseReference(ParseContext parseContext,
                                            String caseType,
                                            SearchCriteriaEntity searchCriteriaEntity) {

        String otherCaseReference = searchCriteriaEntity.getOtherCaseReference();

        if (StringUtils.isNoneBlank(otherCaseReference)) {
            dotNotationValidator.validate(
                parseContext,
                SheetName.SEARCH_CRITERIA,
                ColumnName.OTHER_CASE_REFERENCE,
                caseType,
                otherCaseReference
            );
        }
    }

}
