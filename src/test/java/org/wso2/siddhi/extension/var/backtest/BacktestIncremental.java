package org.wso2.siddhi.extension.var.backtest;

import org.apache.commons.math3.distribution.BinomialDistribution;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.json.JSONObject;
import org.wso2.siddhi.extension.var.models.VaRCalculator;
import org.wso2.siddhi.extension.var.models.historical.HistoricalVaRCalculator;
import org.wso2.siddhi.extension.var.models.montecarlo.MonteCarloVarCalculator;
import org.wso2.siddhi.extension.var.models.parametric.ParametricVaRCalculator;
import org.wso2.siddhi.extension.var.models.util.Event;
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
    //private static int NUMBER_OF_SAMPLES;
    //private static final int VAR_PER_SAMPLE = 500;
    private static final String PORTFOLIO_KEY = "Portfolio 1";
    private ArrayList<Double> varList;
    private ArrayList<Double> lossList;
    private double previousPortfolioValue;
    private double currentPortfolioValue;
    private Double closePortfolioValue;

    private static DescriptiveStatistics statVar;
    private static DescriptiveStatistics statLoss;
    private static int START_DATE = 23;
    private static int END_DATE = 32;

    public BacktestIncremental() {
        varList = new ArrayList();
        lossList = new ArrayList();
        statVar = new DescriptiveStatistics();
        statLoss = new DescriptiveStatistics();
        closePortfolioValue = null;
    }

    public static void main(String[] args) throws FileNotFoundException {
        new BacktestIncremental().runTest();
    }

    public void runTest() throws FileNotFoundException {


        Formatter formatter = new Formatter(new File("MonteCarloBacktestResults.csv"));
        formatter.format("%s%n", "date,varclose,varavg,varmax,varmin,varmedian,varmode,lossclose,lossavg,lossmax," +
                "lossmin,lossmedian,lossmode,losstotal");
        String[] dates = {"Jan 23", "Jan 24", "Jan 25", "Jan 26", "Jan 27", "Jan 30", "Jan 31", "Feb 1", "Feb 2",
                "Feb 3"};
//        VaRCalculator varCalculator = new HistoricalVaRCalculator(BATCH_SIZE, VAR_CI);
//        VaRCalculator varCalculator = new ParametricVaRCalculator(BATCH_SIZE, VAR_CI);
          VaRCalculator varCalculator = new MonteCarloVarCalculator(BATCH_SIZE, VAR_CI, 2500, 100, 0.01);
//
        Map<String, Integer> assets = initPortfolio();
        Portfolio portfolio = varCalculator.createPortfolio("1", assets);
        varCalculator.addPortfolio("1", portfolio);

        for (int d = START_DATE; d <= END_DATE; d++) {
            String write = "%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s";
            ArrayList<Event> list = readBacktestData(d);
            System.out.println("\nDAY : " + dates[d-START_DATE] + "\n");
            Double var = null;
            Double loss = null;
            Double totalLoss = null;

            for (int i = 0; i < list.size(); i++) {

                totalLoss = null;
                //System.out.print("Event " + (i+1) + " : ");
                String jsonString = (String) varCalculator.calculateValueAtRisk(list.get(i));
                currentPortfolioValue = portfolio.getTotalPortfolioValue();

                if (jsonString != null) {

                    JSONObject jsonObject = new JSONObject(jsonString);
                    var = (Double) jsonObject.get(PORTFOLIO_KEY);  // hardcoded for portfolio ID 1
                    //System.out.printf("Var : %.2f", var);
                    //varList.add(var);                           // should filter
                    statVar.addValue(var);

                    loss = currentPortfolioValue - previousPortfolioValue;
                    statLoss.addValue(loss);
                    //lossList.add(actualLoss);
                    //System.out.printf(" Loss : %.2f\n", actualLoss);

                } else {
                    //System.out.println("Insufficient data for VaR calculation");
                }

                previousPortfolioValue = currentPortfolioValue;
            }

            System.out.println("Daily VaR CLOSE  : " + var);
            System.out.println("Daily VaR AVG    : " + statVar.getMean());
            System.out.println("Daily VaR MAX    : " + statVar.getMax());
            System.out.println("Daily VaR MIN    : " + statVar.getMin());
            System.out.println("Daily VaR MEDIAN : " + statVar.getPercentile(50));
            System.out.println("Daily VaR MODE   : " + mode(statVar.getValues()));
            System.out.println();

            System.out.println("Daily Loss CLOSE  : " + loss);
            System.out.println("Daily Loss AVG    : " + statLoss.getMean());
            System.out.println("Daily Loss MAX    : " + statLoss.getMax());
            System.out.println("Daily Loss MIN    : " + statLoss.getMin());
            System.out.println("Daily Loss MEDIAN : " + statLoss.getPercentile(50));
            System.out.println("Daily Loss MODE    : " + mode(statLoss.getValues()));

            if (closePortfolioValue != null) {
                totalLoss = currentPortfolioValue - closePortfolioValue;
                System.out.println("Daily Loss TOTAL  : " + (totalLoss));
            }

            formatter.format("%s%n", String.format(write,dates[d - START_DATE], var, statVar.getMean(), statVar.getMax(), statVar
                    .getMin(), statVar.getPercentile(50), mode(statVar.getValues()), loss, statLoss.getMean(),
                    statLoss.getMax(), statLoss.getMin(), statLoss.getPercentile(50), mode(statLoss.getValues()),
                    totalLoss));

            statVar.clear();
            statLoss.clear();
            closePortfolioValue = currentPortfolioValue;

        }

        formatter.close();

        //runStandardCoverageTest();
    }

    public ArrayList<Event> readBacktestData(int id) throws FileNotFoundException {
        ClassLoader classLoader = getClass().getClassLoader();
        Scanner scan = new Scanner(new File(classLoader.getResource("backtest-data-" + id + ".csv").getFile()));
        ArrayList<Event> list = new ArrayList();
        Event event;
        String[] split;
        while (scan.hasNext()) {
            event = new Event();
            split = scan.nextLine().split(",");

            event.setSymbol(split[1]);
            event.setPrice(Double.parseDouble(split[2]));

//            if (!"NA".equals(split[3])) {    // assuming if pid is present, so is share volume
//                event.setPortfolioID(split[4]);                     //portfolio id
//                event.setQuantity(Integer.parseInt(split[3]));      //shares
//            }

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

    public double mode(double[] array) {
        Double mode = null;
        int maxCount = 1;
        for (int i = 0; i < array.length; i++) {
            double value = array[i];
            int count = 1;
            for (int j = 0; j < array.length; j++) {
                if (array[j] == value) count++;
                if (count > maxCount) {
                    mode = value;
                    maxCount = count;
                }
            }
        }
        return mode;
    }

    //    private void runStandardCoverageTest() {
//
//        BinomialDistribution dist = new BinomialDistribution(VAR_PER_SAMPLE, 1 - VAR_CI);
//        double leftEnd = dist.inverseCumulativeProbability(BACKTEST_CI / 2);
//        double rightEnd = dist.inverseCumulativeProbability(1 - (BACKTEST_CI / 2));
//
//        System.out.println("Left End :" + leftEnd);
//        System.out.println("Right End :" + rightEnd);
//
//        NUMBER_OF_SAMPLES = lossList.size() / VAR_PER_SAMPLE;
//
//        int numberOfExceptions;
//        int failureRate = 0;
//        for (int j = 0; j < NUMBER_OF_SAMPLES; j++) {
//            numberOfExceptions = 0;
//            for (int i = j * VAR_PER_SAMPLE; i < (j + 1) * VAR_PER_SAMPLE; i++) {
//                if (lossList.get(i + 1) < 0) {
//                    if (lossList.get(i + 1) <= varList.get(i))
//                        numberOfExceptions++;
//                }
//            }
//            System.out.println("Sample Set : " + (j + 1) + " Exceptions : " + numberOfExceptions);
//
//            if (numberOfExceptions < leftEnd || rightEnd < numberOfExceptions) {
//                failureRate++;
//            }
//        }
//        System.out.println("Failure Rate : " + (((double) failureRate) / (NUMBER_OF_SAMPLES)) * 100 + " %");
//    }

}
