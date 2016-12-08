//package org.wso2.siddhi.extension.var.realtime;
//
//import junit.framework.TestCase;
//import org.junit.Assert;
//import org.junit.Test;
//import org.wso2.siddhi.extension.var.models.Asset;
//import org.wso2.siddhi.extension.var.models.Portfolio;
//
//import java.util.HashMap;
//import java.util.Map;
//
//
///**
// * Created by dilini92 on 7/4/16.
// */
//public class HistoricalVaRCalcForPortfolioTest extends TestCase {
//    private Map<String, Integer> portfolioList;
//    private Map<String, Asset> assetList;
//    private Portfolio portfolio;
//
//    /**
//     * Initialize the test objects
//     */
//    private void init(){
//        portfolioList = new HashMap<>();
//        assetList = new HashMap<>();
//
//        Asset apple = new Asset(RealtimeVaRTestConstants.APPLE);
//        Asset google = new Asset(RealtimeVaRTestConstants.GOOGLE);
//        Asset ibm = new Asset(RealtimeVaRTestConstants.IBM);
//        Asset facebook = new Asset(RealtimeVaRTestConstants.FACEBOOK);
//
//        assetList.put(RealtimeVaRTestConstants.APPLE, apple);
//        assetList.put(RealtimeVaRTestConstants.GOOGLE, google);
//        assetList.put(RealtimeVaRTestConstants.IBM, ibm);
//        assetList.put(RealtimeVaRTestConstants.FACEBOOK, facebook);
//
//        portfolioList.put(RealtimeVaRTestConstants.APPLE, 100);
//        portfolioList.put(RealtimeVaRTestConstants.GOOGLE, 150);
//        portfolioList.put(RealtimeVaRTestConstants.IBM, 75);
//        portfolioList.put(RealtimeVaRTestConstants.FACEBOOK, 120);
//
//        portfolio = new Portfolio(1, portfolioList);
//    }
//
//    /**
//     *
//     * @throws Exception
//     */
//    @Test
//    public void testAddEvent() throws Exception {
//        Object inputData[] = {RealtimeVaRTestConstants.APPLE, 754.63};
//        init();
//        HistoricalVaRCalculator calculator = new HistoricalVaRCalculator(6, 0.95);
//        calculator.setAssetList(assetList);
//        calculator.addEvent(inputData);
//        int expectedValue = 1;
////        int actualValue = assetList.get(RealtimeVaRTestConstants.APPLE).getHistoricalValues().size();
////        Assert.assertEquals(expectedValue, actualValue, 0);
//    }
//
//    /**
//     *
//     * @throws Exception
//     */
//    @Test
//    public void testRemoveEvent() throws Exception {
//        Object inputData[] = {RealtimeVaRTestConstants.APPLE, 754.63};
//        init();
//        HistoricalVaRCalculator calculator = new HistoricalVaRCalculator(6, 0.95);
//        calculator.setAssetList(assetList);
//        calculator.addEvent(inputData);
//        calculator.addEvent(inputData);
//        int expectedValue = 1;
//        calculator.removeEvent(RealtimeVaRTestConstants.APPLE);
//        int actualValue = assetList.get(RealtimeVaRTestConstants.APPLE).getHistoricalValues().size();
//        Assert.assertEquals(expectedValue, actualValue, 0);
//    }
//
//    /**
//     *
//     * @throws Exception
//     */
//    @Test
//    public void testProcessData() throws Exception {
//        init();
//        Asset apple = assetList.get(RealtimeVaRTestConstants.APPLE);
//        apple.addHistoricalValue(117.290001);
//        apple.addHistoricalValue(115.209999);
//        apple.addHistoricalValue(128.949997);
//        apple.addHistoricalValue(126.690002);
//        apple.addHistoricalValue(115.199997);
//        apple.addHistoricalValue(117.290001);
//
//        Asset google = assetList.get(RealtimeVaRTestConstants.GOOGLE);
//        google.addHistoricalValue(650.280029);
//        google.addHistoricalValue(738.869995);
//        google.addHistoricalValue(633.729984);
//        google.addHistoricalValue(656.450012);
//        google.addHistoricalValue(539.789978);
//        google.addHistoricalValue(530.392405);
//
//        Asset ibm = assetList.get(RealtimeVaRTestConstants.IBM);
//        ibm.addHistoricalValue(137.789993);
//        ibm.addHistoricalValue(133.820007);
//        ibm.addHistoricalValue(166.259995);
//        ibm.addHistoricalValue(165.460007);
//        ibm.addHistoricalValue(158.199997);
//        ibm.addHistoricalValue(159.809998);
//
//        Asset facebook = assetList.get(RealtimeVaRTestConstants.FACEBOOK);
//        facebook.addHistoricalValue(87.949997);
//        facebook.addHistoricalValue(104.599998);
//        facebook.addHistoricalValue(93.93);
//        facebook.addHistoricalValue(80.419998);
//        facebook.addHistoricalValue(78.839996);
//        facebook.addHistoricalValue(78.07);
//
//        HistoricalVaRCalculator calculator = new HistoricalVaRCalculator(6, 0.95);
//        calculator.setAssetList(assetList);
//        System.out.println(calculator.processData(portfolio));
//    }
//}