package org.example.simulator.statistics;

import org.example.network.Router;
public class RouterStats extends Stats{

    private final Router router;

    public RouterStats(Router router) {
        this.router = router;
    }

    @Override
    protected void additionalCalculations() {
        //no additional calculations
    }

    @Override
    protected String fileName() {
        return router.toString();
    }
}
