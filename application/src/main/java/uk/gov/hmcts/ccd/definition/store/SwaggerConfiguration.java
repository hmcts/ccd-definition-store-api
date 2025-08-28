package uk.gov.hmcts.ccd.definition.store;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfiguration {

    @Bean
    public OpenAPI springShopOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Core Case Data - Definition store API")
                .version("1.0.1")
                .description("Create, modify, retrieve and search definitions")
                .termsOfService("https://tools.hmcts.net/confluence/display/RCCD/Reform%3A+Core+Case+Data+Home")
                .contact(new Contact()
                        .name("CCD")
                        .url("https://tools.hmcts.net/confluence/display/RCCD/Reform%3A+Core+Case+Data+Home")
                        .email("corecasedatateam@hmcts.net"))
                .license(new License()
                        .name("HMCTS License")
                        .url("https://www.apache.org/licenses/LICENSE-2.0.txt"))
            );
    }
}
