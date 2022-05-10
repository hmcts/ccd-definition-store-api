package uk.gov.hmcts.ccd.definition.store.domain.service.translation;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseFieldEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.CaseTypeEntity;
import uk.gov.hmcts.ccd.definition.store.repository.entity.StateEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class TranslationServiceImpl implements TranslationService {

    @Override
    public Map<String, String> getPhrasesToTranslate(List<CaseTypeEntity> caseTypes) {
        HashMap<String, String> phrasesExtracted = new HashMap<>();
        caseTypes.forEach(
            caseTypeEntity -> {
                //CaseEvent

                //CaseEventToFields
                //CaseField
                getCaseFields(caseTypeEntity.getCaseFields(),phrasesExtracted);
                //CaseRoles
                //CaseType
                phrasesExtracted.put(caseTypeEntity.getName(), "");
                phrasesExtracted.put(caseTypeEntity.getDescription(), "");
                // CaseTypeTab
                //ChallengeQuestionTab
                //ComplexTypes
                //EventToComplexTypes
                //FixedLists
                //Jurisdiction
                phrasesExtracted.put(caseTypeEntity.getJurisdiction().getName(), "");
                phrasesExtracted.put(caseTypeEntity.getJurisdiction().getDescription(), "");
                //SearchCaseResultsFields

                //SearchInputFields
                //SearchResultFields
                //State
                getStateFields(caseTypeEntity.getStates(), phrasesExtracted);

                //WorkBasketInputFields
                //WorkBasketResultFields
            }
        );
        return phrasesExtracted;
    }

    private void getCaseFields(Set<CaseFieldEntity> caseFields, HashMap<String, String> phrasesExtracted) {
        caseFields.forEach(
            caseField -> {
                phrasesExtracted.put(caseField.getLabel(), "");
                phrasesExtracted.put(caseField.getHint(), "");
            }
        );
    }

    private void getStateFields(List<StateEntity> states, HashMap<String, String> phrasesExtracted) {
        states.forEach(
            state -> {
                phrasesExtracted.put(state.getName(), "");
                phrasesExtracted.put(state.getDescription(), "");
            }
        );
    }


}
