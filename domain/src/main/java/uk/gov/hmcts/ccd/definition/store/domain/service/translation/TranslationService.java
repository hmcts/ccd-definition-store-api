package uk.gov.hmcts.ccd.definition.store.domain.service.translation;

import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;

import java.util.List;
import java.util.Map;

public interface TranslationService {

    Map<String, String> getPhrasesToTranslate(List<CaseTypeEntity> caseTypes);
}
