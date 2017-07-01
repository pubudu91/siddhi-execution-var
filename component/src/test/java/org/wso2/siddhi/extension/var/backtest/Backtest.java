package org.wso2.siddhi.extension.var.backtest;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.json.JSONObject;
import org.wso2.siddhi.extension.var.models.VaRCalculator;
import org.wso2.siddhi.extension.var.models.montecarlo.MonteCarloVarCalculator;
import org.wso2.siddhi.extension.var.models.parametric.ParametricVaRCalculator;
import org.wso2.siddhi.extension.var.models.util.Event;
import org.wso2.siddhi.extension.var.models.util.asset.Asset;
import org.wso2.siddhi.extension.var.models.util.portfolio.Portfolio;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

/**
 * Created by dilip on 09/01/17.
 */
public class Backtest {

    private static final int BATCH_SIZE = 251;
    private static final double VAR_CI = 0.95;
    private static final double BACKTEST_CI = 0.05;
    private static final int NUMBER_OF_ASSETS = 25;
    private static final int SAMPLE_SIZE = 1;
    private static final int VAR_PER_SAMPLE = 500;
    private static final String PORTFOLIO_KEY = "Portfolio 1";
    private ArrayList<Double> calculatedVarList;
    private ArrayList<Double> actualVarList;
    private ArrayList<Double> meanViolations;
    private Double previousPortfolioValue;

    public Backtest() {
        calculatedVarList = new ArrayList();
        actualVarList = new ArrayList();
        meanViolations = new ArrayList();
        previousPortfolioValue = null;
    }

    public static void main(String[] args) throws FileNotFoundException {
        new Backtest().runBackTest();
    }

    private void runBackTest() throws FileNotFoundException {

        //VaRCalculator varCalculator = new HistoricalVaRCalculator(BATCH_SIZE, VAR_CI);
//        VaRCalculator varCalculator = new ParametricVaRCalculator(BATCH_SIZE, VAR_CI);
        VaRCalculator varCalculator = new MonteCarloVarCalculator(BATCH_SIZE, VAR_CI, 2500, 100, 0.01);
        ArrayList<Event> list = readBacktestData();
        int i = 0;
        int totalEvents = ((BATCH_SIZE + 1) * NUMBER_OF_ASSETS) + (VAR_PER_SAMPLE * NUMBER_OF_ASSETS * SAMPLE_SIZE) + 1;
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
                    calculateActualLoss(varCalculator.getPortfolioPool().get("1"), varCalculator.getAssetPool());
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

        int numberOfExceptions = 0;

        for (int i = 0; i < VAR_PER_SAMPLE; i++) {
            if (actualVarList.get(i) <= calculatedVarList.get(i)) {
                numberOfExceptions++;
                meanViolations.add(actualVarList.get(i) - calculatedVarList.get(i));
            }
        }

        DescriptiveStatistics dsLoss = new DescriptiveStatistics(actualVarList.stream().mapToDouble
                (Double::doubleValue).toArray());
        DescriptiveStatistics dsVaR = new DescriptiveStatistics(calculatedVarList.stream().mapToDouble
                (Double::doubleValue).toArray());
        DescriptiveStatistics dsMV = new DescriptiveStatistics(meanViolations.stream().mapToDouble
                (Double::doubleValue).toArray());

        System.out.println("Loss mean : " + dsLoss.getMean());
        System.out.println("VaR mean  : " + dsVaR.getMean());
        System.out.println("No. of violations : " + numberOfExceptions);
        System.out.println("Violation mean  : " + dsMV.getMean());
        System.out.println("Violation Rate : " + (((double) numberOfExceptions) / (VAR_PER_SAMPLE)) * 100 + "%");
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
            currentPortfolioValue += temp.getCurrentStockPrice() * portfolio.getCurrentAssetQuantities((String) symbol);
        }
        if (previousPortfolioValue != null) {
            actualVarList.add(currentPortfolioValue - previousPortfolioValue);
            System.out.print(" AV : " + (currentPortfolioValue - previousPortfolioValue));
        }
        previousPortfolioValue = currentPortfolioValue;
    }

    public ArrayList<Event> readBacktestData() throws FileNotFoundException {
        ClassLoader classLoader = getClass().getClassLoader();
        Scanner scan = new Scanner(new File(classLoader.getResource("BackTestDataNew.csv").getFile()));
        ArrayList<Event> list = new ArrayList();
        Event event;
        String[] split;
        while (scan.hasNext()) {
            event = new Event();
            split = scan.nextLine().split(",");
            if (split.length == 2) {
                event.setSymbol(split[0]);
                event.setPrice(Double.parseDouble(split[1]));
            } else {
                event.setPortfolioID(split[0]);                     //portfolio id
                event.setQuantity(Integer.parseInt(split[1]));      //shares
                event.setSymbol(split[2]);                          //symbol
                event.setPrice(Double.parseDouble(split[3]));       //price
            }
            list.add(event);
        }
        return list;
    }

}