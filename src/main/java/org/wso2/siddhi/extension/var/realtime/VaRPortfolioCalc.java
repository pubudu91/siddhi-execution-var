package org.wso2.siddhi.extension.var.realtime;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.wso2.siddhi.core.config.ExecutionPlanContext;
import org.wso2.siddhi.extension.var.models.Asset;
import org.wso2.siddhi.extension.var.models.Portfolio;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

/**
 * Created by dilini92 on 6/26/16.
 */
public abstract class VaRPortfolioCalc {
    protected double confidenceInterval = 0.95;
    protected int batchSize = 1000000000;
    protected Map<Integer, Portfolio> portfolioList;
    protected double price;
    protected String symbol;
    protected DescriptiveStatistics stat = new DescriptiveStatistics();

    /**
     *
     * @param limit
     * @param ci
     */
    public VaRPortfolioCalc(int limit, double ci) {
        confidenceInterval = ci;
        batchSize = limit;
        portfolioList = new HashMap<>();
    }

    /**
     *
     * @param data
     */
    protected void addEvent(Object data[], Portfolio portfolio){
        price = ((Number) data[1]).doubleValue();
        symbol = data[0].toString();

        //if portfolio does not have the given symbol, then we drop the event.
        if (portfolio.getAssets().get(symbol) != null) {
            portfolio.getAssets().get(symbol).addHistoricalValue(price);
        }
    }

    /**
     * removes the oldest element from a given portfolio
     * @param symbol
     * @param portfolio
     */
    protected void removeEvent(String symbol, Portfolio portfolio){
        LinkedList<Double> priceList = portfolio.getAssets().get(symbol).getHistoricalValues();
        priceList.remove(0);
    }
    protected abstract Object processData(Portfolio portfolio);

    public Object calculateValueAtRisk(Object data[]) {
        String resultsString = "";
        Set<Integer> keys = portfolioList.keySet();
        Iterator<Integer> iterator = keys.iterator();
        int key;
        //for each portfolio
        while(iterator.hasNext()) {
            key = iterator.next();
            Portfolio portfolio = portfolioList.get(key);
            addEvent(data, portfolio);

            //if the given portfolio has the symbol and number of historical value exceeds the batch size, remove the event
            if(portfolio.getAssets().get(data[0]) != null && portfolio.getAssets().get(data[0]).getHistoricalValues().size() > batchSize){
                removeEvent(data[0].toString(), portfolio);
            }

            //counts the number of stock symbols which have already had the given batch size number of events
            int count = 0;
            Set assetsKeys = portfolio.getAssets().keySet();
            Iterator<String> assetIterator = assetsKeys.iterator();
            //for each asset
            while(assetIterator.hasNext()){
                String assetKey = assetIterator.next();
                count += portfolio.getAssets().get(assetKey).getHistoricalValues().size();
            }

            double var;
            if(count == batchSize * portfolio.getAssets().size()){
                if(portfolio.getAssets().get(data[0]) != null){
                    var = Double.parseDouble(processData(portfolio).toString());
                    resultsString = resultsString.concat("Portfolio " + portfolio.getID() + ": " + var + "\n");
                }
            }
        }
        return resultsString;
    }

    public void getPortfolioValues(ExecutionPlanContext executionPlanContext){
        //get the portfolio details from the database
        try {
            Connection connection = executionPlanContext.getSiddhiContext().getSiddhiDataSource("AnalyticsDataSource").getConnection();
            String sql = "SELECT distinct(portfolioID) FROM portfolio natural join portfolioDetails";
            Statement stm = connection.createStatement();
            ResultSet rst = stm.executeQuery(sql);

            int portfolioID;

            while(rst.next()){
                portfolioID = rst.getInt(1);
                Statement stm1 = connection.createStatement();
                sql = "SELECT symbol, noOfShares from portfolioDetails where portfolioID = " + portfolioID;
                ResultSet symbolList = stm1.executeQuery(sql);
                Map<String, Asset> assets = new HashMap<>();
                Portfolio portfolio;

                while(symbolList.next()){
                    Asset asset = new Asset(symbolList.getString(1), symbolList.getInt(2));
                    assets.put(symbolList.getString(1), asset);
                }

                portfolio = new Portfolio(portfolioID, assets);
                portfolioList.put(portfolioID, portfolio);
            }
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
