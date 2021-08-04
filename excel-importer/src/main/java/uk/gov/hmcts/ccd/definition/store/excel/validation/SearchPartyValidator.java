package uk.gov.hmcts.ccd.definition.store.excel.validation;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.excel.parser.ParseContext;
import uk.gov.hmcts.ccd.definition.store.repository.entity.SearchPartyEntity;

import java.util.List;

@Component
public class SearchPartyValidator {

    public void validate(final List<SearchPartyEntity> searchPartyEntities,
                         final ParseContext parseContext) {

    }

}
