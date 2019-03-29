package uk.gov.hmcts.ccd.definition.store;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

public class RoutingDataSource extends AbstractRoutingDataSource {
    private static final ThreadLocal<Route> ctx = new ThreadLocal<>();

    public enum Route {
        MASTER, REPLICA
    }

    public static void clearMasterRoute() {
        ctx.remove();
    }

    public static void setMasterRoute() {
        ctx.set(Route.MASTER);
    }

    @Override
    protected Object determineCurrentLookupKey() {
        return ctx.get();
    }
}
