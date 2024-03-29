package uk.gov.hmcts.ccd.definition.store;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.jwt.JwtIssuerValidator;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationFilter;
import uk.gov.hmcts.ccd.definition.store.elastic.endpoint.ElasticsearchIndexController;
import uk.gov.hmcts.ccd.definition.store.excel.endpoint.ImportController;
import uk.gov.hmcts.ccd.definition.store.security.JwtGrantedAuthoritiesConverter;
import uk.gov.hmcts.ccd.definition.store.security.filters.ExceptionHandlingFilter;
import uk.gov.hmcts.reform.authorisation.filters.ServiceAuthFilter;

import javax.inject.Inject;

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration
    extends WebSecurityConfigurerAdapter {

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

    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers("/swagger-resources/**",
            "/swagger-ui/**",
            "/webjars/**",
            "/v2/**",
            "/health",
            "/health/liveness",
            "/health/readiness",
            "/",
            "/loggers/**",
            "/api/testing-support/**");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .addFilterBefore(exceptionHandlingFilter, BearerTokenAuthenticationFilter.class)
            .addFilterBefore(serviceAuthFilter, BearerTokenAuthenticationFilter.class)
            .sessionManagement().sessionCreationPolicy(STATELESS).and()
            .csrf().disable()
            .formLogin().disable()
            .logout().disable()
            .authorizeRequests()
            .antMatchers(ImportController.URI_IMPORT, ElasticsearchIndexController.ELASTIC_INDEX_URI)
            .hasAuthority("ccd-import")
            .anyRequest()
            .authenticated()
            .and()
            .oauth2ResourceServer()
            .jwt()
            .jwtAuthenticationConverter(jwtAuthenticationConverter)
            .and()
            .and()
            .oauth2Client();
    }

    @Bean
    JwtDecoder jwtDecoder() {
        NimbusJwtDecoder jwtDecoder = (NimbusJwtDecoder) JwtDecoders.fromOidcIssuerLocation(issuerUri);

        // We are using issuerOverride instead of issuerUri as SIDAM has the wrong issuer at the moment
        OAuth2TokenValidator<Jwt> withTimestamp = new JwtTimestampValidator();
        OAuth2TokenValidator<Jwt> withIssuer = new JwtIssuerValidator(issuerOverride);
        // FIXME : enable `withIssuer` once idam migration is done RDM-8094
        // OAuth2TokenValidator<Jwt> validator = new DelegatingOAuth2TokenValidator<>(withTimestamp, withIssuer);
        OAuth2TokenValidator<Jwt> validator = new DelegatingOAuth2TokenValidator<>(withTimestamp);

        jwtDecoder.setJwtValidator(validator);
        return jwtDecoder;
    }
}
