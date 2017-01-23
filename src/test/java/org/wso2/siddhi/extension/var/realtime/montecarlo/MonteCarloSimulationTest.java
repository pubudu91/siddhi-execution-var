package org.wso2.siddhi.extension.var.realtime.montecarlo;

import junit.framework.Assert;
import org.junit.Test;

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
//        Map<String, Double> parameters = new HashMap<>();
//        MonteCarloSimulation mc = this.init();
//        double randomVal = this.init().getRandomZVal();
//
//        parameters.put("distributionMean", 12.345);
//        parameters.put("standardDeviation", 1.34);
//        parameters.put("timeSlice", 0.01);
//        parameters.put("currentStockValue", 259.67);
//        parameters.put("randomValue", randomVal);
//
//        double drift = (parameters.get("distributionMean") - (parameters.get("standardDeviation") *
//                parameters.get("standardDeviation")) / 2) * parameters.get("timeSlice");
//        double stochasticOffset = parameters.get("standardDeviation") * randomVal *
//                Math.sqrt(parameters.get("timeSlice"));
//        double bmo = parameters.get("currentStockValue") * Math.exp(drift + stochasticOffset);
//
//        Assert.assertEquals(bmo, mc.getBrownianMotionOutput(parameters));

    }

    @Test
    public void testSimulation() throws Exception {
        int umb=75000;
        long begin = System.currentTimeMillis();
        double[] temp1 = {2, 3, 4};
        double[] temp = new MonteCarloNativeSimulation().simulation(0.001216, 0.01960, 0.1, 234.5, umb, 100);
//        double[] temp = new MonteCarloSimulation(umb).parallelSimulation(umb, 100, temp1, 0.1, 234.5);
        long end = System.currentTimeMillis();
        System.out.println((double) (end - begin) / 1000);
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

    @Test
    public void testParallelSimulation() throws Exception {
//        long begin = System.currentTimeMillis();
//        double[] temp = new MonteCarloSimulation(20000).parallelSimulation(20000, 100, 0.001216, 0.1, 234.5);
//        long end = System.currentTimeMillis();
//        System.out.println((double) (end - begin) / 1000);
//        System.out.println(temp[0]);
    }
}