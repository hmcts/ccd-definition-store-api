package uk.gov.hmcts.ccd.definition.store.database;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

public class RoutingDataSource extends AbstractRoutingDataSource {

    public enum Route {
        MASTER, REPLICA
    }

    private static final ThreadLocal<Route> ctx = ThreadLocal.withInitial(() -> Route.REPLICA);

    public static void clearMasterRoute() {
        ctx.remove();
        ctx.set(Route.REPLICA);
    }

    public static void setMasterRoute() {
        ctx.set(Route.MASTER);
    }

    @Override
    protected Object determineCurrentLookupKey() {
        return ctx.get();
    }
}
