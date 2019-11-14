package uk.gov.hmcts.ccd.definition.store;

import liquibase.configuration.GlobalConfiguration;
import liquibase.configuration.LiquibaseConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CaseDataAPIApplication {
    public static void main(String[] args) {
        //Setting Liquibase DB Lock property before Spring starts up.
        LiquibaseConfiguration.getInstance()
            .getConfiguration(GlobalConfiguration.class)
            .setUseDbLock(true);
        SpringApplication.run(CaseDataAPIApplication.class, args);
    }
}



