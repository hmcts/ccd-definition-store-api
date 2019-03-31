package uk.gov.hmcts.ccd.definition.store.database;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Order(0)
public class MasterDBConnectionInterceptor {

    @Pointcut(value="execution(public * *(..))")
    public void anyPublicMethod() { }

    @Around("@annotation(routeToMasterDB)")
    public Object proceed(ProceedingJoinPoint pjp, RouteToMasterDB routeToMasterDB) throws Throwable {
        try {
            RoutingDataSource.setMasterRoute();
            Object result = pjp.proceed();
            return result;
        } finally {
            RoutingDataSource.clearMasterRoute();
        }
    }
}
