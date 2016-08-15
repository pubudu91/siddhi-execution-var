package org.wso2.siddhi.extension.var.backtest;

import org.wso2.siddhi.extension.var.models.Asset;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by flash on 7/14/16.
 */
public class HistoricalCalculatorAssertionTestCase {

    @org.junit.Test
    public void testCalculateVar() throws Exception {
        Map<String, Asset> portfolio = new HashMap<>();
        portfolio.put("MSI", new Asset(200));
        portfolio.put("IBM", new Asset(250));
        portfolio.put("MSFT", new Asset(100));
        portfolio.put("APPL", new Asset(175));
        portfolio.put("EBAY", new Asset(300));
        try {
            System.out.println(new HistoricalCalculatorAssertion(250, portfolio, 0.95, 250).AssertMethodValidity(1));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



}