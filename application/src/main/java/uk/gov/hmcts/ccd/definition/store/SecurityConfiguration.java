package uk.gov.hmcts.ccd.definition.store;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;

import uk.gov.hmcts.ccd.definition.store.elastic.endpoint.ElasticsearchIndexController;
import uk.gov.hmcts.ccd.definition.store.elastic.endpoint.ReindexTaskController;
import uk.gov.hmcts.ccd.definition.store.excel.endpoint.ImportController;
import uk.gov.hmcts.ccd.definition.store.security.JwtGrantedAuthoritiesConverter;
import uk.gov.hmcts.ccd.definition.store.security.filters.ExceptionHandlingFilter;
import uk.gov.hmcts.reform.authorisation.filters.ServiceAuthFilter;

import jakarta.inject.Inject;

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    @Value("${spring.security.oauth2.client.provider.oidc.issuer-uri}")
    private String issuerUri;

    @Value("${oidc.issuer}")
    private String issuerOverride;

    private final ServiceAuthFilter serviceAuthFilter;
    private final JwtAuthenticationConverter jwtAuthenticationConverter;
    private final ExceptionHandlingFilter exceptionHandlingFilter;

    @Inject
    public SecurityConfiguration(final ServiceAuthFilter serviceAuthFilter,
                                 final JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter) {
        this.serviceAuthFilter = serviceAuthFilter;
        this.exceptionHandlingFilter = new ExceptionHandlingFilter();
        jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter);
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return web -> web.ignoring().requestMatchers("/swagger-resources/**",
            "/swagger-ui/**",
            "/webjars/**",
            "/v3/api-docs",
            "/v3/api-docs/**",
            "/v2/**",
            "/health",
            "/health/liveness",
            "/health/readiness",
            "/",
            "/loggers/**",
            "/api/testing-support/**");
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .addFilterBefore(exceptionHandlingFilter, BearerTokenAuthenticationFilter.class)
            .addFilterBefore(serviceAuthFilter, BearerTokenAuthenticationFilter.class)
            .sessionManagement(s -> s.sessionCreationPolicy(STATELESS))
            .csrf(csrf -> csrf.disable()) // NOSONAR - CSRF is disabled as per security requirements
            .formLogin(fl -> fl.disable())
            .logout(lg -> lg.disable())
            .authorizeHttpRequests(ar -> 
                ar.requestMatchers(ImportController.URI_IMPORT, ElasticsearchIndexController.ELASTIC_INDEX_URI,
                        ReindexTaskController.REINDEX_TASKS_URI)
                .hasAuthority("ccd-import")
                .anyRequest()
                .authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(
                jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter)
            ))
            .oauth2Client(Customizer.withDefaults());
        return http.build();
    }

    @Bean
    JwtDecoder jwtDecoder() {
        NimbusJwtDecoder jwtDecoder = (NimbusJwtDecoder) JwtDecoders.fromOidcIssuerLocation(issuerUri);

        // We are using issuerOverride instead of issuerUri as SIDAM has the wrong issuer at the moment
        OAuth2TokenValidator<Jwt> withTimestamp = new JwtTimestampValidator();
        // OAuth2TokenValidator<Jwt> withIssuer = new JwtIssuerValidator(issuerOverride);
        // FIXME : enable `withIssuer` once idam migration is done RDM-8094
        // OAuth2TokenValidator<Jwt> validator = new DelegatingOAuth2TokenValidator<>(withTimestamp, withIssuer);
        OAuth2TokenValidator<Jwt> validator = new DelegatingOAuth2TokenValidator<>(withTimestamp);

        jwtDecoder.setJwtValidator(validator);
        return jwtDecoder;
    }
}
