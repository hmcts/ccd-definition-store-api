package uk.gov.hmcts.ccd.definition.store.repository;

import org.hibernate.dialect.PostgreSQL94Dialect;

import java.sql.Types;

public class NewPostgreSQL94Dialect extends PostgreSQL94Dialect {

    public NewPostgreSQL94Dialect() {
        registerColumnType(Types.JAVA_OBJECT, "jsonb");
        registerColumnType(Types.NULL, "null");  // need for gradle task `liquibaseDiffChangelog`
    }

}
