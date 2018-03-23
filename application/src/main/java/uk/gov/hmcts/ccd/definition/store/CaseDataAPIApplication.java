package uk.gov.hmcts.ccd.definition.store;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("uk.gov.hmcts.ccd.definition.store")
public class CaseDataAPIApplication {
    public static void main(String[] args) {
        SpringApplication.run(CaseDataAPIApplication.class, args);
    }
}



