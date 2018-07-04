package uk.gov.hmcts.ccd.definition.store.domain.service.metadata;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.ccd.definition.store.repository.model.CaseState;
import uk.gov.hmcts.ccd.definition.store.repository.model.CaseType;
import uk.gov.hmcts.ccd.definition.store.repository.model.FixedListItem;

import java.util.List;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

class StateMetadataFixedListItemFactoryTest {

    private final StateMetadataFixedListItemFactory factory = new StateMetadataFixedListItemFactory();

    @Test
    @DisplayName("Should return fixed list for state")
    void shouldReturnFixedListForState() {
        CaseType caseType = new CaseType();
        CaseState caseState1 = new CaseState();
        caseState1.setName("STATE 1");
        caseState1.setId("STATE1");
        CaseState caseState2 = new CaseState();
        caseState2.setName("STATE 2");
        caseState2.setId("STATE2");
        caseType.setStates(asList(caseState1, caseState2));

        List<FixedListItem> stateFixedList = factory.createFixedListItems(caseType);

        assertThat(stateFixedList, hasSize(2));
        assertThat(stateFixedList.get(0).getCode(), is("STATE1"));
        assertThat(stateFixedList.get(0).getLabel(), is("STATE 1"));
        assertThat(stateFixedList.get(1).getCode(), is("STATE2"));
        assertThat(stateFixedList.get(1).getLabel(), is("STATE 2"));
    }
}
