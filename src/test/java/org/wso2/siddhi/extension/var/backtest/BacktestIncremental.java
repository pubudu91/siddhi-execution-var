package org.wso2.siddhi.extension.var.backtest;

import org.apache.commons.math3.distribution.BinomialDistribution;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.json.JSONObject;
import org.wso2.siddhi.extension.var.models.VaRCalculator;
import org.wso2.siddhi.extension.var.models.historical.HistoricalVaRCalculator;
import org.wso2.siddhi.extension.var.models.montecarlo.MonteCarloVarCalculator;
import org.wso2.siddhi.extension.var.models.parametric.ParametricVaRCalculator;
import org.wso2.siddhi.extension.var.models.util.Event;
import org.wso2.siddhi.extension.var.models.util.asset.Asset;
import org.wso2.siddhi.extension.var.models.util.portfolio.Portfolio;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

/**
 * Created by pubudu on 2/3/17.
 */
public class BacktestIncremental {
    private static final int BATCH_SIZE = 251;
    private static final double VAR_CI = 0.95;
    private static final double BACKTEST_CI = 0.05;
    private static final int NUMBER_OF_ASSETS = 25;
    private static final int SAMPLE_SIZE = 1;
    private static final int NO_OF_OBSERVATIONS = 391;
    private static final String PORTFOLIO_KEY = "Portfolio 1";
    private ArrayList<Double> calculatedVarList;
    private ArrayList<Double> actualLosses;
    private ArrayList<Double> meanViolations;
    private Double previousPortfolioValue;

    public BacktestIncremental() {
        calculatedVarList = new ArrayList();
        actualLosses = new ArrayList();
        meanViolations = new ArrayList();
        previousPortfolioValue = null;
    }

    public static void main(String[] args) throws FileNotFoundException {
        new BacktestIncremental().runTest();
    }

    public void runTest() throws FileNotFoundException {
//        VaRCalculator varCalculator = new HistoricalVaRCalculator(BATCH_SIZE, VAR_CI);
        VaRCalculator varCalculator = new ParametricVaRCalculator(BATCH_SIZE, VAR_CI);
//        VaRCalculator varCalculator = new MonteCarloVarCalculator(BATCH_SIZE, VAR_CI , 2500, 100, 0.01);
        ArrayList<Event> list = readBacktestData();

//        int totalEvents = ((BATCH_SIZE + 1) * NUMBER_OF_ASSETS) + (NO_OF_OBSERVATIONS * NUMBER_OF_ASSETS * SAMPLE_SIZE) + 1;
//        System.out.println("Read Total Events : " + totalEvents);

        Map<String, Integer> assets = initPortfolio();
        Portfolio portfolio = varCalculator.createPortfolio("1", assets);
        varCalculator.addPortfolio("1", portfolio);
        Map<String, Asset> assetPool = varCalculator.getAssetPool();

        for (int i = 0; i < list.size(); i++) {
            // process event only if the portfolio contains the symbol in question
            if (portfolio.containsAsset(list.get(i).getSymbol())) {
                System.out.print("Event " + (i) + " : ");

                String jsonString = (String) varCalculator.calculateValueAtRisk(list.get(i));

                if (jsonString != null) {
                    JSONObject jsonObject = new JSONObject(jsonString);
                    Double calculatedVar = (Double) jsonObject.get(PORTFOLIO_KEY);  // hardcoded for portfolio ID 1

                    System.out.printf("CV : %.2f", calculatedVar);

                    calculatedVarList.add(calculatedVar);                           // should filter

                    double actualLoss = getPortfolioValuation(portfolio, assetPool)
                            - getPreviousPortfolioValuation(portfolio, assetPool);
                    actualLosses.add(actualLoss);
                    System.out.printf(" Actual Loss : %.2f\n", actualLoss);
                } else {
                    System.out.println("Insufficient data for VaR calculation");
                }
            }
        }

        runStandardCoverageTest();
    }

    private void runStandardCoverageTest() {

        BinomialDistribution dist = new BinomialDistribution(NO_OF_OBSERVATIONS, 1 - VAR_CI);
        double leftEnd = dist.inverseCumulativeProbability(BACKTEST_CI / 2);
        double rightEnd = dist.inverseCumulativeProbability(1 - (BACKTEST_CI / 2));

        System.out.println("Left End :" + leftEnd);
        System.out.println("Right End :" + rightEnd);

        int numberOfExceptions = 0;
//        int successCount = 0;
        for (int j = 0; j < SAMPLE_SIZE * NUMBER_OF_ASSETS; j++) {
            for (int i = j * NO_OF_OBSERVATIONS; i < (j + 1) * NO_OF_OBSERVATIONS; i++) {
                //System.out.println(actualVarList.get(i) + " " + calculatedVarList.get(i));
                if (actualLosses.get(i) <= calculatedVarList.get(i))
                    numberOfExceptions++;
            }
            System.out.println("Sample Set : " + (j + 1) + " Exceptions : " + numberOfExceptions);

//            if (rightEnd >= numberOfExceptions && leftEnd <= numberOfExceptions) {
//                successCount++;
//            }
        }
        System.out.println("Failure Rate : " + (((double) numberOfExceptions) / (NO_OF_OBSERVATIONS)) * 100);

    }

//    private void runStandardCoverageTest() {
//
//        int numberOfExceptions = 0;
//
//        for (int i = 0; i < calculatedVarList.size(); i++) {
//            double loss = actualLosses.get(i);
//            double var = calculatedVarList.get(i);
//            if (loss <= var) {
//                numberOfExceptions++;
//                meanViolations.add(actualLosses.get(i) - calculatedVarList.get(i));
//            }
//        }
//
//        DescriptiveStatistics dsLoss = new DescriptiveStatistics(actualLosses.stream().mapToDouble
//                (Double::doubleValue).toArray());
//        DescriptiveStatistics dsVaR = new DescriptiveStatistics(calculatedVarList.stream().mapToDouble
//                (Double::doubleValue).toArray());
//        DescriptiveStatistics dsMV = new DescriptiveStatistics(meanViolations.stream().mapToDouble
//                (Double::doubleValue).toArray());
//
//        System.out.println("Loss mean : " + dsLoss.getMean());
//        System.out.println("VaR mean  : " + dsVaR.getMean());
//        System.out.println("No. of violations : " + numberOfExceptions);
//        System.out.println("Violation mean  : " + dsMV.getMean());
//        System.out.printf("Violation Rate : %.2f%%\n", (((double) numberOfExceptions) / (NO_OF_OBSERVATIONS)) * 100);
//    }

    private void calculateActualLoss(Portfolio portfolio, Map<String, Asset> assetList) {
        Double currentPortfolioValue = getPortfolioValuation(portfolio, assetList);

        if (previousPortfolioValue != null) {
            actualLosses.add(currentPortfolioValue - previousPortfolioValue);
            System.out.printf(" Actual Loss : %.2f", (currentPortfolioValue - previousPortfolioValue));
        }
        previousPortfolioValue = currentPortfolioValue;
    }

    private double getPortfolioValuation(Portfolio portfolio, Map<String, Asset> assetMap) {
        double currentPortfolioValue = 0;

        Set<String> keys = portfolio.getAssetListKeySet();

        for (String symbol : keys) {
            Asset asset = assetMap.get(symbol);
            currentPortfolioValue += asset.getCurrentStockPrice() * portfolio.getCurrentAssetQuantities(symbol);
        }

        return currentPortfolioValue;
    }

    private double getPreviousPortfolioValuation(Portfolio portfolio, Map<String, Asset> assetMap) {
        double prevPortfolioValue = 0;

        Set<String> keys = portfolio.getAssetListKeySet();

        for (String symbol : keys) {
            Asset asset = assetMap.get(symbol);
            prevPortfolioValue += asset.getPreviousStockPrice() * portfolio.getPreviousAssetQuantities(symbol);
        }

        return prevPortfolioValue;
    }

    public ArrayList<Event> readBacktestData() throws FileNotFoundException {
        ClassLoader classLoader = getClass().getClassLoader();
        Scanner scan = new Scanner(new File(classLoader.getResource("backtest-data-23rd.csv").getFile()));
        ArrayList<Event> list = new ArrayList<>();
        Event event;
        String[] split;
        while (scan.hasNext()) {
            event = new Event();
            split = scan.nextLine().split(",");

            event.setSymbol(split[1]);
            event.setPrice(Double.parseDouble(split[2]));

            if (!"NA".equals(split[3])) {    // assuming if pid is present, so is share volume
                event.setPortfolioID(split[3]);                     //portfolio id
                event.setQuantity(Integer.parseInt(split[4]));      //shares
            }

            list.add(event);
        }
        return list;
    }

    private Map<String, Integer> initPortfolio() throws FileNotFoundException {
        ClassLoader classLoader = getClass().getClassLoader();
        Scanner scanner = new Scanner(new File(classLoader.getResource("portfolio-init.csv").getFile()));
        HashMap<String, Integer> assets = new HashMap<>();
        String split[];

        while (scanner.hasNextLine()) {
            split = scanner.nextLine().split(",");
            assets.put(split[0], Integer.parseInt(split[1]));
        }

        return assets;
    }
}
