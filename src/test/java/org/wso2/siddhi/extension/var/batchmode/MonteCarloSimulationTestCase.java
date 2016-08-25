//package org.wso2.siddhi.extension.var.batchmode;
//
//import org.junit.Assert;
//import org.junit.Before;
//import org.junit.Test;
//
//import java.util.HashMap;
//import java.util.Map;
//
///**
// * Created by flash on 7/1/16.
// */
//public class MonteCarloSimulationTestCase {
//    private MonteCarloSimulation testInstance;
//
//    @Before
//    public void init() {
//        testInstance = new MonteCarloSimulation();
//    }
//
//    @Test
//    public void testGetRandomZVal() throws Exception {
//        double zValue = testInstance.getRandomZVal();
//        Assert.assertNotNull(zValue);
//        Assert.assertTrue(zValue > -4 && zValue < 4);
//    }
//
//    @Test
//    public void testGetBrownianMotionOutput() throws Exception {
//        Map<String, Double> parameters = new HashMap<>();
//        parameters.put("distributionMean", 0.2);
//        parameters.put("standardDeviation", 0.4);
//        parameters.put("timeSlice", 0.01);
//        parameters.put("currentStockValue", 20.0);
//
//        double expectedStockValue = testInstance.getBrownianMotionOutput(parameters);
//        double calculatedStockValue = 20 * Math.exp((0.12) * 0.01 + testInstance.getRandomZValue() * 0.4 * Math.sqrt(0.01));
////        System.out.println(expectedStockValue);
//        Assert.assertEquals(expectedStockValue, calculatedStockValue, 0);
//    }
//
//    @Test
//    public void testSimulation() throws Exception {
//        double historical[] = {32.3, 34.2, 33.4, 35.0, 34.230, 33.0, 31.4};
//        double terminalValues[] = testInstance.simulation(3, 200, historical, 0.01, 30.0);
//
////        System.out.println(new DescriptiveStatistics(historical).getStandardDeviation());
//        Assert.assertEquals(3, terminalValues.length);
//
//    }
//
//    @Test
//    public void testGetMeanReturnAndStandardDeviation() throws Exception {
//        double historical[] = {32.3, 34.2, 33.4, 35.0, 34.230, 33.0, 31.4};
//        Map<String, Double> parameters = testInstance.getMeanReturnAndStandardDeviation(historical);
////        System.out.println("Mean Return" + parameters.get("meanReturn"));
////        System.out.println("Std Deviation Return" + parameters.get("meanStandardDeviation"));
//
//
//    }
//
//}