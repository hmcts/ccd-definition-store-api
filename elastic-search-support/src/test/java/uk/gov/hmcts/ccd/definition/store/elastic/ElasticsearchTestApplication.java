package uk.gov.hmcts.ccd.definition.store.elastic;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.autoconfigure.security.servlet.ManagementWebSecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication(
    scanBasePackages = "uk.gov.hmcts.ccd.definition.store.elastic",
    exclude = {
        OAuth2ClientAutoConfiguration.class,
        ManagementWebSecurityAutoConfiguration.class
    }
)
@EnableConfigurationProperties
public class ElasticsearchTestApplication {
    public static void main(String[] args) {
        SpringApplication.run(ElasticsearchTestApplication.class, args);
    }
}
