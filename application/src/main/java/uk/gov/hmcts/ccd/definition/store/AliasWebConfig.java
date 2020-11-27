package uk.gov.hmcts.ccd.definition.store;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class AliasWebConfig implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/status/health").setViewName("forward:/health");
        registry.addViewController("/").setViewName("forward:/health");
    }
}
