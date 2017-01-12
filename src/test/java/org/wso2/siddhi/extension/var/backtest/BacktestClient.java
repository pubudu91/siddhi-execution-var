package org.wso2.siddhi.extension.var.backtest;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.json.JSONObject;
import org.wso2.siddhi.extension.var.models.Asset;
import org.wso2.siddhi.extension.var.models.Portfolio;
import org.wso2.siddhi.extension.var.realtime.HistoricalVaRCalculator;
import org.wso2.siddhi.extension.var.realtime.ParametricVaRCalculator;
import org.wso2.siddhi.extension.var.realtime.VaRPortfolioCalc;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

/**
 * Created by dilip on 09/01/17.
 */
public class BacktestClient {

    private static Scanner scan;
    private static final int BATCH_SIZE = 251;
    private static final int NUMBER_OF_ASSETS = 25;
    private static final double VAR_CI = 0.95;
    private static final int SAMPLE_SIZE = 20;
    private static final int VAR_PER_SAMPLE = 500;
    private static final String PORTFOLIO_KEY = "Portfolio 1";
    private static ArrayList<Double> calculatedVarList = new ArrayList();
    private static ArrayList<Double> actualVarList = new ArrayList();
    private static Double previousPortfolioValue = null;

    public static void main(String[] args) throws FileNotFoundException {
        new BacktestClient().runBackTest();
    }

    private void runBackTest() throws FileNotFoundException {
        ClassLoader classLoader = getClass().getClassLoader();
        scan = new Scanner(new File(classLoader.getResource("BackTestDataReal.csv").getFile()));// order
        VaRPortfolioCalc varCalculator = new ParametricVaRCalculator(BATCH_SIZE, VAR_CI);
        ArrayList<Object[]> list = readBacktestData();
        int i=0;
        int j=0;
        int totalevents = (BATCH_SIZE+1)*NUMBER_OF_ASSETS + VAR_PER_SAMPLE*NUMBER_OF_ASSETS + 1;
        System.out.println(totalevents);

        while(i<totalevents) {
            // fill lists
            if (i < (BATCH_SIZE+1) * NUMBER_OF_ASSETS) {
                varCalculator.calculateValueAtRisk(list.get(i));
                i++;
            }
            else {
                if(i%NUMBER_OF_ASSETS==0){
                    String jsonString = (String) varCalculator.calculateValueAtRisk(list.get(i));
                    JSONObject jsonObject = new JSONObject(jsonString);
                    Double calculatedVar = (Double) jsonObject.get(PORTFOLIO_KEY);   // hardcoded for portfolio ID 1
                    calculatedVarList.add(calculatedVar); // should filter
                    calculateActualLoss(varCalculator.getPortfolioList().get(1), varCalculator.getAssetList());

                }else{
                    varCalculator.calculateValueAtRisk(list.get(i));
                }
                i++;
            }
        }
        System.out.println(calculatedVarList.size());
        System.out.println(actualVarList.size());
        System.out.println(standardCoverageTest());
    }

    private static boolean standardCoverageTest() {
//        System.out.println(actualVarList.size());
//        System.out.println(calculatedVarList.size());
        int numberOfExceptions = 0;
        double significanceLevelForBacktest=0.95;

        for (int i = 0; i < SAMPLE_SIZE; i++) {
            System.out.println(actualVarList.get(i) + " " + calculatedVarList.get(i));
            if(actualVarList.get(i)<=calculatedVarList.get(i))
                numberOfExceptions ++;
        }

        System.out.println("Exception Count : " + numberOfExceptions);

        NormalDistribution dist = new NormalDistribution();

        double leftEnd = dist.inverseCumulativeProbability(significanceLevelForBacktest / 2);
        leftEnd = leftEnd * Math.sqrt(SAMPLE_SIZE * VAR_CI * (1 - VAR_CI)) + (SAMPLE_SIZE * (1 - VAR_CI));
        leftEnd = Math.floor(leftEnd);

        double rightEnd = dist.inverseCumulativeProbability(1 - (significanceLevelForBacktest / 2));
        rightEnd = rightEnd * Math.sqrt(SAMPLE_SIZE * VAR_CI * (1 - VAR_CI)) + (SAMPLE_SIZE * (1 - VAR_CI));
        rightEnd = Math.ceil(rightEnd);

        if (rightEnd >= numberOfExceptions && leftEnd <= numberOfExceptions) {
            return true;
        } else {
            return false;
        }
    }

    private static void calculateActualLoss(Portfolio portfolio, Map<String, Asset> assetList) {
        Double currentPortfolioValue = 0.0;
        Asset temp;
        Object symbol;
        Set<String> keys = portfolio.getAssetListKeySet();
        Iterator itr = keys.iterator();
        while (itr.hasNext()) {
            symbol = itr.next();
            temp = assetList.get(symbol);
            currentPortfolioValue += temp.getCurrentStockPrice() * portfolio.getCurrentShare((String)symbol);
        }
        if(previousPortfolioValue!=null)
            actualVarList.add(currentPortfolioValue - previousPortfolioValue);
        previousPortfolioValue = currentPortfolioValue;
    }

    public static ArrayList<Object[]> readBacktestData() {
        ArrayList<Object[]> list = new ArrayList();
        Object[] data;
        String[] split;
        while (scan.hasNext()) {
            data = new Object[4];
            split = scan.nextLine().split(",");
            if (split.length == 2) {
                data[2] = split[0];
                data[3] = Double.parseDouble(split[1]);
            } else {
                data[0] = Integer.parseInt(split[0]); //portfolio id
                data[1] = Integer.parseInt(split[1]); //shares
                data[2] = split[2]; //symbol
                data[3] = Double.parseDouble(split[3]); //price
            }
            list.add(data);
        }
        return list;
    }

}
