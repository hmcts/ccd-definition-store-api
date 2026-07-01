package uk.gov.hmcts.ccd.definition.store.elastic;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

class VersionedCaseIndexHelperTest {

    private static final String BASE = "ft_caseaccessgroups_cases";

    @Test
    void shouldReturnFirstVersionIndexName() {
        assertThat(VersionedCaseIndexHelper.firstVersionIndexName(BASE),
            is(equalTo("ft_caseaccessgroups_cases-000001")));
    }

    @Test
    void shouldFindLatestVersionedIndexByNumericSuffix() {
        Optional<String> latest = VersionedCaseIndexHelper.findLatestVersionedIndex(
            List.of(
                BASE + "-000001",
                BASE + "-000010",
                BASE + "-000002",
                "other_index"
            ),
            BASE
        );

        assertThat(latest, is(equalTo(Optional.of(BASE + "-000010"))));
    }

    @Test
    void shouldReturnEmptyWhenNoMatchingIndices() {
        Optional<String> latest = VersionedCaseIndexHelper.findLatestVersionedIndex(
            List.of("unrelated-000001"),
            BASE
        );

        assertThat(latest.isEmpty(), is(true));
    }
}
