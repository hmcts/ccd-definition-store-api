package uk.gov.hmcts.ccd.definition.store.repository.entity;

import java.util.UUID;

public class IdGenerator {

    private IdGenerator() {
    }

    public static String createId() {
        UUID uuid = java.util.UUID.randomUUID();
        return uuid.toString();
    }
}
