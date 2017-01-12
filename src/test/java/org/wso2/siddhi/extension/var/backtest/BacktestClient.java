package org.wso2.siddhi.extension.var.backtest;

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
    private static final int BATCH_SIZE = 252;
    private static final int NUMBER_OF_ASSETS = 25;
    private static final double CI = 0.70;
    private static final int SAMPLE_SIZE = 500;
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
        VaRPortfolioCalc varCalculator = new ParametricVaRCalculator(BATCH_SIZE, CI);
        ArrayList<Object[]> list = readBacktestData();

        for (int i = 0; i < 7000; i++) {
            if (i < BATCH_SIZE * NUMBER_OF_ASSETS) {
                varCalculator.newCalculateValueAtRisk(list.get(i));
                System.out.println(Arrays.toString(list.get(i)));
            }
            else {
                String jsonString = (String) varCalculator.newCalculateValueAtRisk(list.get(i));
                JSONObject jsonObject = new JSONObject(jsonString);
                Double calculatedVar = (Double) jsonObject.get(PORTFOLIO_KEY);   // hardcoded for portfolio ID 1
                calculatedVarList.add(calculatedVar); // should filter
                calculateActualRVar(varCalculator.getPortfolioList().get(1), varCalculator.getAssetList());
            }
        }
        compareBothVaR();
    }

    private static void compareBothVaR() {
//        System.out.println(actualVarList.size());
//        System.out.println(calculatedVarList.size());
        int passCount = 0;
        for (int i = 0; i < SAMPLE_SIZE; i++) {
            System.out.println(actualVarList.get(i) + " " + calculatedVarList.get(i));
            if(actualVarList.get(i)>=calculatedVarList.get(i))
                passCount ++;
        }
        System.out.println("Success Percentage : " + ((double)passCount/SAMPLE_SIZE)*100);
    }

    private static void calculateActualRVar(Portfolio portfolio, Map<String, Asset> assetList) {
        Double currentPortfolioValue = 0.0;
        int i = 0;
        Asset temp;
        Object symbol;
        Set<String> keys = portfolio.getAssets().keySet();
        Iterator itr = keys.iterator();
        while (itr.hasNext()) {
            symbol = itr.next();
            temp = assetList.get(symbol);
            currentPortfolioValue += temp.getCurrentStockPrice() * portfolio.getAssets().get(symbol);
            i++;
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
