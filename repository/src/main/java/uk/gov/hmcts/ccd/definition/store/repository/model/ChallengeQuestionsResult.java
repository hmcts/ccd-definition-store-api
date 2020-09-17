package uk.gov.hmcts.ccd.definition.store.repository.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public class ChallengeQuestionsResult {

    private List<ChallengeQuestion> questions;
}
