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
public class Backtest {

    private static final int BATCH_SIZE = 251;
    private static final double VAR_CI = 0.99;
    private static final double BACKTEST_CI = 0.05;
    private static final int NUMBER_OF_ASSETS = 25;
    private static final int SAMPLE_SIZE = 20;
    private static final int VAR_PER_SAMPLE = 500;
    private static final String PORTFOLIO_KEY = "Portfolio 1";
    private ArrayList<Double> calculatedVarList ;
    private ArrayList<Double> actualVarList ;
    private Double previousPortfolioValue ;

    public Backtest() {
        calculatedVarList = new ArrayList();
        actualVarList = new ArrayList();
        previousPortfolioValue = null;
    }

    public static void main(String[] args) throws FileNotFoundException {
        new Backtest().runBackTest();
    }

    private void runBackTest() throws FileNotFoundException {

        VaRPortfolioCalc varCalculator = new HistoricalVaRCalculator(BATCH_SIZE, VAR_CI);
        //VaRPortfolioCalc varCalculator = new ParametricVaRCalculator(BATCH_SIZE, VAR_CI);
        //VaRPortfolioCalc varCalculator = new MonteCarloSimulation().parallelSimulation(BATCH_SIZE, VAR_CI, 2500,100,0.01);

        ArrayList<Object[]> list = readBacktestData();
        int i = 0;
        int totalEvents = (BATCH_SIZE + 1) * NUMBER_OF_ASSETS + VAR_PER_SAMPLE * NUMBER_OF_ASSETS * SAMPLE_SIZE + 1;
        System.out.println("Read Total Events : " + totalEvents);

        while (i < totalEvents) {
            // fill lists
            if (i < (BATCH_SIZE + 1) * NUMBER_OF_ASSETS) {
                varCalculator.calculateValueAtRisk(list.get(i));
                i++;
            } else {
                if (i % NUMBER_OF_ASSETS == 0) {
                    System.out.print("Event " + (i) + " : ");
                    String jsonString = (String) varCalculator.calculateValueAtRisk(list.get(i));
                    JSONObject jsonObject = new JSONObject(jsonString);
                    Double calculatedVar = (Double) jsonObject.get(PORTFOLIO_KEY);  // hardcoded for portfolio ID 1
                    System.out.print("CV : " + calculatedVar);
                    calculatedVarList.add(calculatedVar);                           // should filter
                    calculateActualLoss(varCalculator.getPortfolioList().get(1), varCalculator.getAssetList());
                    System.out.println();
                } else {
                    varCalculator.calculateValueAtRisk(list.get(i));
                }
                i++;
            }
        }

        runStandardCoverageTest();
    }

    private void runStandardCoverageTest() {

        NormalDistribution dist = new NormalDistribution();
        double leftEnd = dist.inverseCumulativeProbability(BACKTEST_CI  / 2);
        leftEnd = leftEnd * Math.sqrt(VAR_PER_SAMPLE * VAR_CI * (1 - VAR_CI)) + (VAR_PER_SAMPLE * (1 - VAR_CI));
        leftEnd = Math.floor(leftEnd);

        double rightEnd = dist.inverseCumulativeProbability(1 - (BACKTEST_CI  / 2));
        rightEnd = rightEnd * Math.sqrt(VAR_PER_SAMPLE * VAR_CI * (1 - VAR_CI)) + (VAR_PER_SAMPLE * (1 - VAR_CI));
        rightEnd = Math.ceil(rightEnd);

        System.out.println("Left End :" + leftEnd);
        System.out.println("Right End :" + rightEnd);

        int numberOfExceptions;
        int successCount = 0;
        for (int j = 0; j < SAMPLE_SIZE; j++) {
            numberOfExceptions = 0;
            for (int i = j*VAR_PER_SAMPLE; i < (j+1)*VAR_PER_SAMPLE; i++) {
                //System.out.println(actualVarList.get(i) + " " + calculatedVarList.get(i));
                if (actualVarList.get(i) <= calculatedVarList.get(i))
                    numberOfExceptions++;
            }
            System.out.println("Sample Set : " + (j+1) + " Exceptions : " + numberOfExceptions);
            if (rightEnd >= numberOfExceptions && leftEnd <= numberOfExceptions) {
                successCount++;
            }
        }
        System.out.println("Success Percentage : " + (((double)successCount)/SAMPLE_SIZE)*100);
    }

    private void calculateActualLoss(Portfolio portfolio, Map<String, Asset> assetList) {
        Double currentPortfolioValue = 0.0;
        Asset temp;
        Object symbol;
        Set<String> keys = portfolio.getAssetListKeySet();
        Iterator itr = keys.iterator();
        while (itr.hasNext()) {
            symbol = itr.next();
            temp = assetList.get(symbol);
            currentPortfolioValue += temp.getCurrentStockPrice() * portfolio.getCurrentShare((String) symbol);
        }
        if (previousPortfolioValue != null) {
            actualVarList.add(currentPortfolioValue - previousPortfolioValue);
            System.out.print(" AV : " + (currentPortfolioValue - previousPortfolioValue));
        }
        previousPortfolioValue = currentPortfolioValue;
    }

    public ArrayList<Object[]> readBacktestData() throws FileNotFoundException {
        ClassLoader classLoader = getClass().getClassLoader();
        Scanner scan = new Scanner(new File(classLoader.getResource("BackTestDataReal.csv").getFile()));
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
                data[0] = Integer.parseInt(split[0]);   //portfolio id
                data[1] = Integer.parseInt(split[1]);   //shares
                data[2] = split[2];                     //symbol
                data[3] = Double.parseDouble(split[3]); //price
            }
            list.add(data);
        }
        return list;
    }

}
