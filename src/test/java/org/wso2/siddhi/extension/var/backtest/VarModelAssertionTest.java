//package org.wso2.siddhi.extension.var.backtest;
//
//import org.junit.Test;
//import org.wso2.siddhi.extension.var.models.Asset;
//
//import java.util.HashMap;
//import java.util.Map;
//
///**
// * Created by flash on 7/14/16.
// */
//public class VarModelAssertionTest {
//    Map<String, Asset> portfolio = new HashMap<>();
//    private VarModelAssertion calc = new HistoricalCalculatorAssertion(1, portfolio, 0.95, 250);
//
//    @org.junit.Test
//    public void testCalculateOriginal() throws Exception {
//        this.portfolio.put("MSI", new Asset(200));
//        this.portfolio.put("IBM", new Asset(250));
//        this.portfolio.put("MSFT", new Asset(100));
//        this.portfolio.put("APPL", new Asset(175));
//        this.portfolio.put("EBAY", new Asset(300));
//
//
//    }
//
//    @Test
//    public void testAssertMethodValidity() throws Exception {
//
//    }
//
//    @Test
//    public void testCalculateProbability() throws Exception {
//
//    }
//}