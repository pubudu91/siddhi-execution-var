package org.wso2.siddhi.extension.var.batchmode;

import org.junit.Test;
import org.wso2.siddhi.extension.var.models.Asset;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Created by flash on 7/1/16.
 */
public class MonteCarloVarCalculatorTestCase {

    @Test
    public void testAddEvent() throws Exception {

    }

    @Test
    public void testRemoveEvent() throws Exception {

    }

    @Test
    public void testProcessData() throws Exception {
        int limit=2500,calculationsPerDay=200,numberOfTrials=2000;
        double ci=0.95,timeSlice=0.01;
        Map<String, Asset> assets=new HashMap<>();
        Asset asset_1=new Asset(200);
        Asset asset_2=new Asset(250);
        Asset asset_3=new Asset(150);
        for (int i = 0; i < 2500; i++) {
            asset_1.addHistoricalValue(new Random().nextInt(4)+20+new Random().nextDouble());
        }
        assets.put("GOOGL", asset_1);
        for (int i = 0; i < 2500; i++) {
            asset_2.addHistoricalValue(new Random().nextInt(8)+30+new Random().nextDouble());
        }
        assets.put("APPL", asset_2);
        for (int i = 0; i < 2500; i++) {
            asset_3.addHistoricalValue(new Random().nextInt(6)+20+new Random().nextDouble());
        }
        assets.put("APPL", asset_3);
        MonteCarloVarCalculator calc = new MonteCarloVarCalculator(limit,ci,assets,numberOfTrials,calculationsPerDay,timeSlice);
        System.out.println(calc.processData());
    }
}