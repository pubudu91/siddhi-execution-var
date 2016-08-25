//package org.wso2.siddhi.extension.var.realtime;
//
//import junit.framework.TestCase;
//import org.junit.Assert;
//import org.wso2.siddhi.extension.var.models.Asset;
//
//import java.util.HashMap;
//import java.util.Map;
//
//
///**
// * Created by dilini92 on 7/4/16.
// */
//public class HistoricalVaRCalcForPortfolioTest extends TestCase {
//    private Map<String, Asset> portfolio;
//    private void init(){
//        portfolio = new HashMap<>();
//        Asset apple = new Asset(100);
//        Asset google = new Asset(150);
//        Asset ibm = new Asset(200);
//        Asset facebook = new Asset(100);
//        portfolio.put("Apple", apple);
//        portfolio.put("Google", google);
//        portfolio.put("IBM", ibm);
//        portfolio.put("Facebook", facebook);
//    }
//
//    public void testAddEvent() throws Exception {
//        Object inputData[] = {"Apple", 754.63};
//        init();
//        HistoricalVaRCalculator calculator = new HistoricalVaRCalculator(6, 0.95, portfolio);
//        calculator.addEvent(inputData);
//        int expectedValue = 1;
//        int actualValue = portfolio.get("Apple").getHistoricalValues().size();
//        Assert.assertEquals(expectedValue, actualValue, 0);
//    }
//
//    public void testRemoveEvent() throws Exception {
//        Object inputData[] = {"Apple", 754.63};
//        init();
//        HistoricalVaRCalculator calculator = new HistoricalVaRCalculator(6, 0.95, portfolio);
//        calculator.addEvent(inputData);
//        calculator.addEvent(inputData);
//        int expectedValue = 1;
//        calculator.removeEvent("Apple");
//        int actualValue = portfolio.get("Apple").getHistoricalValues().size();
//        Assert.assertEquals(expectedValue, actualValue, 0);
//    }
//
//    public void testProcessData() throws Exception {
//        init();
//        Asset apple = portfolio.get("Apple");
//        apple.addHistoricalValue(117.290001);
//        apple.addHistoricalValue(115.209999);
//        apple.addHistoricalValue(128.949997);
//        apple.addHistoricalValue(126.690002);
//        apple.addHistoricalValue(115.199997);
//        apple.addHistoricalValue(117.290001);
//
//        Asset google = portfolio.get("Google");
//        google.addHistoricalValue(650.280029);
//        google.addHistoricalValue(738.869995);
//        google.addHistoricalValue(633.729984);
//        google.addHistoricalValue(656.450012);
//        google.addHistoricalValue(539.789978);
//        google.addHistoricalValue(530.392405);
//
//        Asset ibm = portfolio.get("IBM");
//        ibm.addHistoricalValue(137.789993);
//        ibm.addHistoricalValue(133.820007);
//        ibm.addHistoricalValue(166.259995);
//        ibm.addHistoricalValue(165.460007);
//        ibm.addHistoricalValue(158.199997);
//        ibm.addHistoricalValue(159.809998);
//
//        Asset facebook = portfolio.get("Facebook");
//        facebook.addHistoricalValue(87.949997);
//        facebook.addHistoricalValue(104.599998);
//        facebook.addHistoricalValue(93.93);
//        facebook.addHistoricalValue(80.419998);
//        facebook.addHistoricalValue(78.839996);
//        facebook.addHistoricalValue(78.07);
//
//        HistoricalVaRCalculator calculator = new HistoricalVaRCalculator(6, 0.95, portfolio);
//        double var = (Double)calculator.processData();
//        double expected = -6735.0615234178;
//        Assert.assertEquals(expected, var, 0);
//    }
//}