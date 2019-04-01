package uk.gov.hmcts.ccd.definition.store.database;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *  Use to route DB requests of the method this annotation is applied to to the master DB when read replicas datasource is enabled
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RouteToMasterDB { }
