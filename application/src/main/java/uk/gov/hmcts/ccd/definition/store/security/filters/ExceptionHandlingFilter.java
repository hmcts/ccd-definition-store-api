package uk.gov.hmcts.ccd.definition.store.security.filters;

import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.connector.ClientAbortException;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Used to catch the exceptions thrown in the Spring Security filter chain, except the following ServiceAuthFilter
 * owned exceptions. <b>uk.gov.hmcts.reform.authorisation.exceptions.InvalidTokenException</b>
 * <br/><b>uk.gov.hmcts.reform.authorisation.exceptions.ServiceException</b><br/>This filters should always stay
 * at the top of the hierarchy, as it catches exceptions in the Spring Security filters chain
 */
@Slf4j
public class ExceptionHandlingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) {
        try {
            filterChain.doFilter(request, response);
        } catch (Exception exception) {
            log.error("Exception thrown in the security filter chain", exception);

            if (exception instanceof ClientAbortException) {
                response.setStatus(HttpStatus.BAD_GATEWAY.value());
            } else {
                response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            }
        }
    }
}
