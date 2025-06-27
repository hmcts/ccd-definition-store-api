package uk.gov.hmcts.ccd.definition.store;

import org.springframework.boot.actuate.autoconfigure.endpoint.web.CorsEndpointProperties;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties;
import org.springframework.boot.actuate.autoconfigure.web.server.ManagementPortType;
import org.springframework.boot.actuate.endpoint.ExposableEndpoint;
import org.springframework.boot.actuate.endpoint.web.EndpointMediaTypes;
import org.springframework.boot.actuate.endpoint.web.ExposableWebEndpoint;
import org.springframework.boot.actuate.endpoint.web.WebEndpointsSupplier;
import org.springframework.boot.actuate.endpoint.web.EndpointMapping;
import org.springframework.boot.actuate.endpoint.web.EndpointLinksResolver;
import org.springframework.boot.actuate.endpoint.web.annotation.ControllerEndpointsSupplier;
import org.springframework.boot.actuate.endpoint.web.annotation.ServletEndpointsSupplier;
import org.springframework.boot.actuate.endpoint.web.servlet.WebMvcEndpointHandlerMapping;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;

import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.ParameterBuilder;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.RequestParameterBuilder;

import springfox.documentation.schema.ModelRef;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.service.RequestParameter;
import springfox.documentation.service.Parameter;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import uk.gov.hmcts.ccd.definition.store.excel.endpoint.ImportController;
import uk.gov.hmcts.ccd.definition.store.rest.endpoint.CaseDefinitionController;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Collection;
import java.util.List;

@Configuration
public class SwaggerConfiguration {

    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2) // or OPENAPI_3
            .select()
            .apis(RequestHandlerSelectors.basePackage(CaseDataAPIApplication.class.getPackage().getName()))
            //.apis(RequestHandlerSelectors.basePackage("uk.gov.hmcts"))
            .paths(PathSelectors.any())
            .build()
            .globalOperationParameters(Arrays.asList(
                new ParameterBuilder()
                    .name("Authorization")
                    .description("JWT Bearer token")
                    .modelRef(new ModelRef("string"))
                    .parameterType("header")
                    .required(true)
                    .build(),
                new ParameterBuilder()
                    .name("ServiceAuthorization")
                    .description("S2S token")
                    .modelRef(new ModelRef("string"))
                    .parameterType("header")
                    .required(true)
                    .build()
            ));
    }

    private ApiInfo apiV1Info() {
        return new ApiInfoBuilder()
            .title("Core Case Data - Definition store API")
            .description("Create, modify, retrieve and search definitions")
            .license("")
            .licenseUrl("")
            .version("1.0.1")
            .contact(new Contact("CCD",
                "https://tools.hmcts.net/confluence/display/RCCD/Reform%3A+Core+Case+Data+Home",
                "corecasedatateam@hmcts.net"))
            .termsOfServiceUrl("")
            .build();
    }

    @Bean
    public Docket apiV1External() {
        return getNewDocketForPackageOf(ImportController.class, "v1_external", apiV1Info());
    }

    @Bean
    public Docket apiV1Internal() {
        return getNewDocketForPackageOf(CaseDefinitionController.class, "v1_internal", apiV1Info());
    }

    private Docket getNewDocketForPackageOf(Class<?> klazz, String groupName, ApiInfo apiInfo) {
        return new Docket(DocumentationType.SWAGGER_2)
            .groupName(groupName)
            .select()
            .apis(RequestHandlerSelectors.basePackage(klazz.getPackage().getName()))
            .paths(PathSelectors.any())
            .build().useDefaultResponseMessages(false)
            .apiInfo(apiInfo)
            .globalRequestParameters(Arrays.asList(headerAuthorization(), headerServiceAuthorization()));
    }

    private RequestParameter headerAuthorization() {
        return new RequestParameterBuilder()
            .name("Authorization")
            .description("Keyword `Bearer` followed by a valid IDAM user token")
            .in("header")
            .accepts(Collections.singleton(MediaType.APPLICATION_JSON))
            .required(true)
            .build();
    }

    private RequestParameter headerServiceAuthorization() {
        return new RequestParameterBuilder()
            .name("ServiceAuthorization")
            .description("Valid Service-to-Service JWT token for a whitelisted micro-service")
            .in("header")
            .accepts(Collections.singleton(MediaType.APPLICATION_JSON))
            .required(true)
            .build();
    }

    //CCD-3509 CVE-2021-22044 required to fix null pointers in integration tests,
    //conflict in Springfox after Springboot 2.6.10
    @Bean
    public WebMvcEndpointHandlerMapping webEndpointServletHandlerMapping(WebEndpointsSupplier webEndpointsSupplier,
        ServletEndpointsSupplier servletEndpointsSupplier, ControllerEndpointsSupplier controllerEndpointsSupplier,
        EndpointMediaTypes endpointMediaTypes, CorsEndpointProperties corsProperties,
        WebEndpointProperties webEndpointProperties, Environment environment) {

        List<ExposableEndpoint<?>> allEndpoints = new ArrayList<>();
        Collection<ExposableWebEndpoint> webEndpoints = webEndpointsSupplier.getEndpoints();
        allEndpoints.addAll(webEndpoints);
        allEndpoints.addAll(servletEndpointsSupplier.getEndpoints());
        allEndpoints.addAll(controllerEndpointsSupplier.getEndpoints());
        String basePath = webEndpointProperties.getBasePath();
        EndpointMapping endpointMapping = new EndpointMapping(basePath);
        boolean shouldRegisterLinksMapping = this.shouldRegisterLinksMapping(webEndpointProperties, environment,
            basePath);
        return new WebMvcEndpointHandlerMapping(endpointMapping, webEndpoints, endpointMediaTypes,
            corsProperties.toCorsConfiguration(),
            new EndpointLinksResolver(allEndpoints, basePath),
            shouldRegisterLinksMapping, null);
    }

    private boolean shouldRegisterLinksMapping(WebEndpointProperties webEndpointProperties, Environment environment,
                                               String basePath) {
        return webEndpointProperties.getDiscovery().isEnabled() && (StringUtils.hasText(basePath)
            || ManagementPortType.get(environment).equals(ManagementPortType.DIFFERENT));
    }

}
