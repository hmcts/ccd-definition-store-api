package uk.gov.hmcts.ccd.definition.store.excel.parser;

import com.google.common.collect.Lists;

import java.util.List;

/**
 * Contains the results of a parse stage.
 * Parsed elements that were introduced or altered since the last import of the jurisdiction or case type are recorded as `new`.
 *
 * @param <T> Type of the result, e.g. {@link uk.gov.hmcts.ccd.definition.store.repository.entity.FieldTypeEntity}
 */
public class ParseResult<T> {
    private final List<T> allResults = Lists.newArrayList();

    /**
     * A sorted collection must be used here as some results may need to be persisted in a specific order to satisfy inter-dependencies.
     */
    private final List<T> newResults = Lists.newArrayList();

    /**
     * Register a result entry.
     * This entry is marked as `new` and can be retrieved with {@link #getAllResults()} and {@link #getNewResults()}.
     *
     * @param result Result to be registered and marked as `new`.
     */
    public void addNew(T result) {
        newResults.add(result);
        allResults.add(result);
    }

    /**
     * Register a result entry.
     * This entry is NOT marked as `new` and can only be retrieved with {@link #getAllResults()}.
     *
     * @param result - result
     */
    public void addExisting(T result) {
        allResults.add(result);
    }

    /**
     * Retrieve all results irrespective of whether they are "new" or not.
     * @return all registered result entries, marked as `new` or not.
     */
    public List<T> getAllResults() {
        return allResults;
    }

    /**
     * Return all results marked as "new".
     * @return all registered result entries marked as `new`.
     */
    public List<T> getNewResults() {
        return newResults;
    }

    public ParseResult<T> add(ParseResult.Entry<T> item) {
        if (item.isExisting()) {
            addExisting(item.getValue());
        } else {
            addNew(item.getValue());
        }
        return this;
    }

    /**
     * Merge a given parse result into the current parse result. Entries marked as `new` on each side are kept as `new`.
     *
     * @param result result entries to be merged with the current parse result
     * @return the current parse result
     */
    public ParseResult<T> add(ParseResult<T> result) {
        this.newResults.addAll(result.newResults);
        this.allResults.addAll(result.allResults);
        return this;
    }

    static class Entry<T> {

        public static <U> Entry<U> createNew(U value) {
            return new Entry<>(value, Boolean.FALSE);
        }

        public static <U> Entry<U> createExisting(U value) {
            return new Entry<>(value, Boolean.TRUE);
        }

        private final T value;
        private final Boolean existing;

        private Entry(T value, Boolean existing) {
            this.value = value;
            this.existing = existing;
        }

        public T getValue() {
            return value;
        }

        public Boolean isExisting() {
            return existing;
        }
    }
}
