package uk.gov.hmcts.ccd.definition.store.excel.validation;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.excel.parser.ParseContext;
import uk.gov.hmcts.ccd.definition.store.repository.entity.SearchCriteriaEntity;

import java.util.List;

@Component
public class SearchCriteriaValidator {

    public void validate(final List<SearchCriteriaEntity> searchCriteriaEntities,
                         final ParseContext parseContext) {

    }

}
