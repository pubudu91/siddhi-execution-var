package org.wso2.siddhi.extension.var.batchmode;

import junit.framework.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by flash on 9/1/16.
 */
public class MonteCarloSimulationTest {

    public MonteCarloSimulation init() {
        return new MonteCarloSimulation();
    }


    @Test
    public void testGetRandomZVal() throws Exception {
        Assert.assertNotNull(this.init().getRandomZVal());
    }

    @Test
    public void testGetBrownianMotionOutput() throws Exception {
        Map<String, Double> parameters = new HashMap<>();
        MonteCarloSimulation mc = this.init();
        double randomVal = this.init().getRandomZVal();

        parameters.put("distributionMean", 12.345);
        parameters.put("standardDeviation", 1.34);
        parameters.put("timeSlice", 0.01);
        parameters.put("currentStockValue", 259.67);
        parameters.put("randomValue", randomVal);

        double drift = (parameters.get("distributionMean") - (parameters.get("standardDeviation") *
                parameters.get("standardDeviation")) / 2) * parameters.get("timeSlice");
        double stochasticOffset = parameters.get("standardDeviation") * randomVal *
                Math.sqrt(parameters.get("timeSlice"));
        double bmo = parameters.get("currentStockValue") * Math.exp(drift + stochasticOffset);

        Assert.assertEquals(bmo, mc.getBrownianMotionOutput(parameters));

    }

    @Test
    public void testSimulation() throws Exception {

    }

    @Test
    public void testGetStat() throws Exception {

    }

    @Test
    public void testGetDistribution() throws Exception {

    }

    @Test
    public void testGetMeanReturnAndStandardDeviation() throws Exception {

    }
}