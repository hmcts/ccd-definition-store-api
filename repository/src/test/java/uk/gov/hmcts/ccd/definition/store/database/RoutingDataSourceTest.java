package uk.gov.hmcts.ccd.definition.store.database;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.jupiter.api.Test;

class RoutingDataSourceTest {

    @Test
    public void testInitialValueIsReplicaRoute() {
        RoutingDataSource routingDataSource = new RoutingDataSource();
        assertThat(routingDataSource.determineCurrentLookupKey(), equalTo(RoutingDataSource.Route.REPLICA));
    }

    @Test
    public void testSetMasterRoute() {
        RoutingDataSource routingDataSource = new RoutingDataSource();
        routingDataSource.setMasterRoute();
        assertThat(routingDataSource.determineCurrentLookupKey(), equalTo(RoutingDataSource.Route.MASTER));
    }

    @Test
    public void testClearMasterRoute() {
        RoutingDataSource routingDataSource = new RoutingDataSource();
        routingDataSource.setMasterRoute();
        routingDataSource.clearMasterRoute();
        assertThat(routingDataSource.determineCurrentLookupKey(), equalTo(RoutingDataSource.Route.REPLICA));
    }
}
