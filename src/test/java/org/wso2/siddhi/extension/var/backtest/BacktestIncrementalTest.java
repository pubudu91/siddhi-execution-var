
package org.wso2.siddhi.extension.var.backtest;

import org.apache.commons.math3.distribution.BinomialDistribution;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.json.JSONObject;
import org.wso2.siddhi.extension.var.models.VaRCalculator;
import org.wso2.siddhi.extension.var.models.historical.HistoricalVaRCalculator;
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
public class BacktestIncrementalTest {
    private static final int BATCH_SIZE = 251;
    private static final double VAR_CI = 0.99;
    private static final double BACKTEST_CI = 0.05;
    //    private static final int NUMBER_OF_ASSETS = 25;
    private static int NUMBER_OF_SAMPLES;
    private static final int VAR_PER_SAMPLE = 500;
    private static final String PORTFOLIO_KEY = "Portfolio 1";
    private ArrayList<Double> varList;
    private ArrayList<Double> lossList;
    private ArrayList<Double> meanViolations;
//    private double previousPortfolioValue;
//    private double currentPortfolioValue;

    public BacktestIncrementalTest() {
        varList = new ArrayList();
        lossList = new ArrayList();
        meanViolations = new ArrayList();
    }

    public static void main(String[] args) throws FileNotFoundException {
        new BacktestIncrementalTest().runTest();
    }

    public void runTest() throws FileNotFoundException {
        VaRCalculator varCalculator = new HistoricalVaRCalculator(BATCH_SIZE, VAR_CI);
//        VaRCalculator varCalculator = new ParametricVaRCalculator(BATCH_SIZE, VAR_CI);
//        VaRCalculator varCalculator = new MonteCarloVarCalculator(BATCH_SIZE, VAR_CI, 2500, 100, 0.01);

        ArrayList<Event> list = readBacktestData();

        Map<String, Integer> assets = initPortfolio();
        Portfolio portfolio = varCalculator.createPortfolio("1", assets);
        varCalculator.addPortfolio("1", portfolio);

        for (int i = 0; i < 216; i++) {
            varCalculator.calculateValueAtRisk(list.get(i));
        }

        for (int i = 216; i < list.size(); i++) {

            //System.out.print("Event " + (i) + " : ");

            String jsonString = (String) varCalculator.calculateValueAtRisk(list.get(i));
            //currentPortfolioValue = portfolio.getTotalPortfolioValue();

            if (jsonString != null) {
                JSONObject jsonObject = new JSONObject(jsonString);
                Double calculatedVar = (Double) jsonObject.get(PORTFOLIO_KEY);  // hardcoded for portfolio ID 1

                //System.out.printf("Var : %.3f", calculatedVar);

                varList.add(calculatedVar);                                     // should filter

                double actualLoss = getPortfolioValuation(portfolio, varCalculator.getAssetPool()) - getPreviousPortfolioValuation(portfolio, varCalculator.getAssetPool());
                lossList.add(actualLoss);
                //System.out.printf(" Loss : %.3f", actualLoss);
            }

            //previousPortfolioValue = currentPortfolioValue;
            //System.out.println();
        }
        //runStandardCoverageTest();
        runViolationTest();
    }

    private void runStandardCoverageTest() {

        BinomialDistribution dist = new BinomialDistribution(VAR_PER_SAMPLE, 1 - VAR_CI);
        double leftEnd = dist.inverseCumulativeProbability(BACKTEST_CI / 2);
        double rightEnd = dist.inverseCumulativeProbability(1 - (BACKTEST_CI / 2));

        System.out.println("Left End :" + leftEnd);
        System.out.println("Right End :" + rightEnd);

        NUMBER_OF_SAMPLES = lossList.size() / VAR_PER_SAMPLE;

        int numberOfExceptions;
        int failCount = 0;
        for (int j = 0; j < NUMBER_OF_SAMPLES; j++) {
            numberOfExceptions = 0;
            for (int i = j * VAR_PER_SAMPLE; i < (j + 1) * VAR_PER_SAMPLE; i++) {
                if (lossList.get(i + 1) < 0) {
                    if (lossList.get(i + 1) < varList.get(i))
                        numberOfExceptions++;
                }
            }
            System.out.println("Sample Set : " + (j + 1) + " Exceptions : " + numberOfExceptions);

            if (numberOfExceptions < leftEnd || rightEnd < numberOfExceptions) {
                failCount++;
            }
        }
        System.out.println("Success Rate : " + (((double) NUMBER_OF_SAMPLES - failCount) / (NUMBER_OF_SAMPLES)) * 100 + " %");
    }

    private void runViolationTest() {
        DescriptiveStatistics lossStat = new DescriptiveStatistics(lossList.stream().mapToDouble(Double::doubleValue).toArray());
        DescriptiveStatistics varStat = new DescriptiveStatistics(varList.stream().mapToDouble(Double::doubleValue).toArray());
        DescriptiveStatistics violationStat = new DescriptiveStatistics();
        int failCount = 0;
        int numberOfData = varList.size();
        for (int i = 0; i < numberOfData - 1; i++) {
            if (lossList.get(i + 1) < varList.get(i)) {
                violationStat.addValue(varList.get(i));
                failCount++;
            }
        }
        System.out.printf("Loss Mean : %.2f\n", lossStat.getMean());
        System.out.printf("Loss SD : %.2f\n", lossStat.getStandardDeviation());
        System.out.printf("Var Mean : %.2f\n", varStat.getMean());
        System.out.printf("Var SD : %.2f\n", varStat.getStandardDeviation());
        System.out.printf("Number of violations : %d\n", failCount);
        System.out.printf("Mean Violation : %.2f\n", violationStat.getMean());
        System.out.printf("Violation Rate : %.2f%%\n", ((double) failCount) / (numberOfData - 1) * 100);
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
        Scanner scan = new Scanner(new File(classLoader.getResource("backtest-data-aio.csv").getFile()));
        ArrayList<Event> list = new ArrayList();
        Event event;
        String[] split;
        while (scan.hasNext()) {
            event = new Event();
            split = scan.nextLine().split(",");

            event.setSymbol(split[2]);
            event.setPrice(Double.parseDouble(split[1]));

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