package uk.gov.hmcts.ccd.definition.store;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@ComponentScan({ "uk.gov.hmcts.ccd" })
@SuppressWarnings("HideUtilityClassConstructor")
@EnableFeignClients
@EnableAsync
public class CaseDataAPIApplication {
    public static void main(String[] args) {
        SpringApplication.run(CaseDataAPIApplication.class, args);
    }
}



