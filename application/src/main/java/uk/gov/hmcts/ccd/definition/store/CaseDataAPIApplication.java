package uk.gov.hmcts.ccd.definition.store;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@SuppressWarnings("HideUtilityClassConstructor")
@EnableFeignClients
@EnableAsync
public class CaseDataAPIApplication {

    private static final Logger LOG = LoggerFactory.getLogger(CaseDataAPIApplication.class);

    public static void main(String[] args) {
        try {
            SpringApplication.run(CaseDataAPIApplication.class, args);
        }
        catch (Exception e) {
            LOG.info("JCDEBUG: Exception in CaseDataAPIApplication.main: {} , {}", e.getMessage(), e.toString());
        }
    }
}



