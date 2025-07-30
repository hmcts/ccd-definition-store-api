package uk.gov.hmcts.ccd.definition.store;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfiguration {

    @Bean
    public OpenAPI springShopOpenAPI() {
        return new OpenAPI()
                .info(new Info().title("CaseDataAPI Application")
                .description("CaseDataAPIApplication"))
                .license("")
                .licenseUrl("")
                .version("1.0.1")
                .contact(new Contact("CCD",
                    "https://tools.hmcts.net/confluence/display/RCCD/Reform%3A+Core+Case+Data+Home",
                    "corecasedatateam@hmcts.net"))
                .termsOfServiceUrl("");
    }

}
