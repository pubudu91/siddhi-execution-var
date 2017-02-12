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
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by pubudu on 2/3/17.
 */
public class BacktestIncremental {
    private static final int BATCH_SIZE = 251;
    private static final double VAR_CI = 0.95;
    private static final String PORTFOLIO_KEY = "Portfolio 1";
    private double previousPortfolioValue;
    private double currentPortfolioValue;
    //private Double closePortfolioValue;

    private static int START_DATE = 23;
    private static int END_DATE = 32;

    public BacktestIncremental() {
        //closePortfolioValue = null;
    }

    public static void main(String[] args) throws FileNotFoundException {
        new BacktestIncremental().runTest();
    }

    public void runTest() throws FileNotFoundException {

        Formatter formatter = new Formatter(new File("HistoricalBacktestResults.csv"));
        formatter.format("%s%n", "date,varclose,varavg,varmax,corrloss,varmedian,varmode,lossclose,lossavg,lossmax," +
                "corrvar,lossmedian,lossmode");
        String[] dates = {"Jan 23", "Jan 24", "Jan 25", "Jan 26", "Jan 27", "Jan 30", "Jan 31", "Feb 1", "Feb 2",
                "Feb 3"};
        VaRCalculator varCalculator = new HistoricalVaRCalculator(BATCH_SIZE, VAR_CI);
//        VaRCalculator varCalculator = new ParametricVaRCalculator(BATCH_SIZE, VAR_CI);
//        VaRCalculator varCalculator = new MonteCarloVarCalculator(BATCH_SIZE, VAR_CI, 2500, 100, 0.01);

        Map<String, Integer> assets = initPortfolio();
        Portfolio portfolio = varCalculator.createPortfolio("1", assets);
        varCalculator.addPortfolio("1", portfolio);

        for (int d = START_DATE; d <= END_DATE; d++) {
            HashMap<Integer, Double> varMap = new HashMap();
            HashMap<Integer, Double> lossMap = new HashMap();
            Double var = null;
            Double loss = null;
            String corrVar;
            String corrLoss;
            String write = "%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s";
            ArrayList<Event> list = readBacktestData(d);
            System.out.println("\nDAY : " + dates[d - START_DATE] + "\n");

            //Double totalLoss = null;
            int counter = 0;

            for (int i = 0; i < list.size(); i++) {

                //System.out.print("Event " + (i + 1) + " : ");
                String jsonString = (String) varCalculator.calculateValueAtRisk(list.get(i));
                currentPortfolioValue = portfolio.getTotalPortfolioValue();

                if (jsonString != null) {

                    JSONObject jsonObject = new JSONObject(jsonString);
                    double tempVar = (Double) jsonObject.get(PORTFOLIO_KEY);        // hardcoded for portfolio ID 1
                    if (tempVar < 0) {
                        var = tempVar;
                        varMap.put(counter, var);
                        //System.out.printf("Var : %.4f", tempVar);
                        //varList.add(var);                                         // should filter
                    }

                    double tempLoss = currentPortfolioValue - previousPortfolioValue;

                    if (tempLoss < 0) {
                        loss = tempLoss;
                        lossMap.put(counter, loss);
                        //System.out.printf(" Loss : %.4f", tempLoss);
                        //lossList.add(actualLoss);
                    }
                    counter++;
                }

                previousPortfolioValue = currentPortfolioValue;
                //System.out.println();
            }


            double[] vars = Stream.of(varMap.values().toArray(new Double[varMap.size()])).mapToDouble
                    (Double::doubleValue).toArray();
            DescriptiveStatistics statVar = new DescriptiveStatistics(vars);

            double[] losses = Stream.of(lossMap.values().toArray(new Double[lossMap.size()])).mapToDouble
                    (Double::doubleValue).toArray();
            DescriptiveStatistics statLoss = new DescriptiveStatistics(losses);

            System.out.println("Daily VaR CLOSE  : " + var);
            System.out.println("Daily VaR AVG    : " + statVar.getMean());

            Double min = statVar.getMin();
            System.out.println("Daily VaR MAX    : " + min);

            Integer minIndex = null;
            for (Map.Entry<Integer, Double> e : varMap.entrySet()) {
                if (e.getValue().equals(min))
                    minIndex = e.getKey();
            }

            if(lossMap.get(minIndex) == null)
                corrLoss = "NO LOSS";
            else
                corrLoss = lossMap.get(minIndex).toString();

            System.out.println("Crspng Loss      : " + corrLoss);
            System.out.println("Daily VaR MEDIAN : " + statVar.getPercentile(50));
            System.out.println("Daily VaR MODE   : " + mode(statVar.getValues()));

            System.out.println();

            System.out.println("Daily Loss CLOSE   : " + loss);
            System.out.println("Daily Loss AVG     : " + statLoss.getMean());

            min = statLoss.getMin();
            System.out.println("Daily Loss MAX     : " + min);

            for (Map.Entry<Integer, Double> e : lossMap.entrySet()) {
                if (e.getValue().equals(min))
                    minIndex = e.getKey();
            }

            if(varMap.get(minIndex) == null)
                corrVar = "NO VAR";
            else
                corrVar = varMap.get(minIndex).toString();

            System.out.println("Crspng VaR         : " + corrVar);
            System.out.println("Daily Loss MEDIAN  : " + statLoss.getPercentile(50));
            System.out.println("Daily Loss MODE    : " + mode(statLoss.getValues()));

//            if (closePortfolioValue != null) {
//                totalLoss = currentPortfolioValue - closePortfolioValue;
//                System.out.println("Daily Loss TOTAL   : " + (totalLoss));
//            }

            formatter.format("%s%n", String.format(write, dates[d - START_DATE], var, statVar.getMean(), statVar
                            .getMin(), corrLoss, statVar.getPercentile(50), mode(statVar.getValues()), loss, statLoss.getMean(),
                    statLoss.getMin(),corrVar, statLoss.getPercentile(50), mode(statLoss.getValues())));

//            closePortfolioValue = currentPortfolioValue;

        }

        formatter.close();

        //runStandardCoverageTest();
    }

    public ArrayList<Event> readBacktestData(int id) throws FileNotFoundException {
        ClassLoader classLoader = getClass().getClassLoader();
        Scanner scan = new Scanner(new File(classLoader.getResource("without_duplicates/backtest-data-" + id + "" +
                ".csv").getFile()));
        ArrayList<Event> list = new ArrayList();
        Event event;
        String[] split;
        while (scan.hasNext()) {
            event = new Event();
            split = scan.nextLine().split(",");

            event.setSymbol(split[2]);
            event.setPrice(Double.parseDouble(split[1]));

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
