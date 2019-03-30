package uk.gov.hmcts.ccd.definition.store;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

public class RoutingDataSource extends AbstractRoutingDataSource {

    private static final ThreadLocal<Route> ctx = new ThreadLocal() {
        protected Route initialValue() {
            return Route.REPLICA;
        }
    };

    public enum Route {
        MASTER, REPLICA
    }

    public static void clearMasterRoute() {
        ctx.remove();
        ctx.set(Route.REPLICA);
    }

    public static void setMasterRoute() {
        ctx.set(Route.MASTER);
    }

    @Override
    protected Object determineCurrentLookupKey() {
        Route route = ctx.get();
        System.out.println("route: " + route);
        return route;
    }
}
