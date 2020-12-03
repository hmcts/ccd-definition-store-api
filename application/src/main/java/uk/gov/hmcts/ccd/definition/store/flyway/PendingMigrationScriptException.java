package uk.gov.hmcts.ccd.definition.store.flyway;

public class PendingMigrationScriptException extends RuntimeException {

    public PendingMigrationScriptException(String script) {
        super("Found migration not yet applied " + script);
    }
}
