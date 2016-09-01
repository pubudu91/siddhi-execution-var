package org.wso2.siddhi.extension.var.batchmode;

import org.junit.Test;
import org.wso2.siddhi.extension.var.models.Asset;
import org.wso2.siddhi.extension.var.models.Portfolio;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by flash on 7/1/16.
 */
public class MonteCarloVarCalculatorTestCase {

    @Test
    public void testProcessData() throws Exception {
        int limit = 250, calculationsPerDay = 100, numberOfTrials = 20;
        double historicValues_1[] = {
                280.260481,
                100.340176,
        };
        double set_2[] = {
                339.920594,
                348.190596,

        };
        double ci = 0.95, timeSlice = 0.01;
        Map<String, Asset> assets = new HashMap<>();
        Asset asset_1 = new Asset("APPL");
        Asset asset_2 = new Asset("GOOG");

        for (int i = 0; i < historicValues_1.length; i++) {
            asset_1.addHistoricalValue(historicValues_1[i]);
        }

        assets.put("GOOGL", asset_1);
        for (int i = 0; i < set_2.length; i++) {
            asset_2.addHistoricalValue(set_2[i]);
        }
        assets.put("APPL", asset_2);

        Map<String, Integer> assetSet = new HashMap<>();
        assetSet.put("APPL", 100);
        assetSet.put("GOOG", 130);
        Portfolio portfolio = new Portfolio(1, assetSet);

        Map<String, Asset> assetList = new HashMap<>();
        assetList.put("APPL", asset_1);
        assetList.put("GOOG", asset_2);

        MonteCarloVarCalculator calc = new MonteCarloVarCalculator(limit, ci, numberOfTrials, calculationsPerDay, timeSlice);
        calc.assetList = assetList;
        System.out.println(calc.processData(portfolio));
    }
}