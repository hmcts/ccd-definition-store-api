package uk.gov.hmcts.ccd.definition.store;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import uk.gov.hmcts.reform.auth.checker.core.RequestAuthorizer;
import uk.gov.hmcts.reform.auth.checker.core.service.Service;
import uk.gov.hmcts.reform.auth.checker.core.user.User;
import uk.gov.hmcts.reform.auth.checker.spring.serviceanduser.AuthCheckerServiceAndUserFilter;

import javax.inject.Inject;

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration
    extends WebSecurityConfigurerAdapter {

    private final AuthCheckerServiceAndUserFilter filter;

    @Inject
    public SecurityConfiguration(RequestAuthorizer<Service> serviceRequestAuthorizer,
                                 RequestAuthorizer<User> userRequestAuthorizer,
                                 AuthenticationManager authenticationManager) {
        this.filter = new AuthCheckerServiceAndUserFilter(serviceRequestAuthorizer, userRequestAuthorizer);
        filter.setAuthenticationManager(authenticationManager);
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers("/swagger-ui.html",
                                   "/webjars/springfox-swagger-ui/**",
                                   "/swagger-resources/**",
                                   "/v2/**",
                                   "/status/**");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        final ProviderManager authenticationManager = (ProviderManager) authenticationManager();
        authenticationManager.setEraseCredentialsAfterAuthentication(false);
        filter.setAuthenticationManager(authenticationManager());

        http
            .addFilter(filter)
            .sessionManagement().sessionCreationPolicy(STATELESS).and()
            .csrf().disable()
            .formLogin().disable()
            .logout().disable()
            .authorizeRequests()
            .anyRequest().authenticated();
    }
}
