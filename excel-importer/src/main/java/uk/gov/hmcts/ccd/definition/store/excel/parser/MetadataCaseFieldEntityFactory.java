package uk.gov.hmcts.ccd.definition.store.excel.parser;

import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;

public interface MetadataCaseFieldEntityFactory {

    CaseFieldEntity createCaseFieldEntity(ParseContext parseContext, CaseTypeEntity caseType);

}
