package org.wso2.siddhi.extension.var.realtime;

import org.json.JSONObject;
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
    public Map<String, Asset> assetList;
    protected double price;
    protected String symbol;

    /**
     *
     * @param limit
     * @param ci
     */
    public VaRPortfolioCalc(int limit, double ci) {
        confidenceInterval = ci;
        batchSize = limit;
        portfolioList = new HashMap<>();
        assetList = new HashMap<>();
    }

    /**
     *
     * @param data
     */
    public void addEvent(Object data[]){
        price = ((Number) data[1]).doubleValue();
        symbol = data[0].toString();

        //if portfolio does not have the given symbol, then we drop the event.
        if (assetList.get(symbol) != null) {
            assetList.get(symbol).addHistoricalValue(price);
        }
    }

    /**
     * removes the oldest element from a given portfolio
     * @param symbol
     */
    public void removeEvent(String symbol){
        LinkedList<Double> priceList = assetList.get(symbol).getHistoricalValues();
        priceList.remove(0);
    }
    protected abstract Object processData(Portfolio portfolio);

    public Object calculateValueAtRisk(Object data[]) {
        addEvent(data);
        //if the given portfolio has the symbol and number of historical value exceeds the batch size, remove the event
        if (assetList.get(data[0]) != null && assetList.get(data[0]).getHistoricalValues().size() > batchSize) {
            removeEvent(data[0].toString());
        }

        JSONObject result = new JSONObject();
        Set<Integer> keys = portfolioList.keySet();
        Iterator<Integer> iterator = keys.iterator();
        int key;
        double var = -1.0;

        //if the given symbol is in the assetList
        if(assetList.get(data[0]) != null){
            //for each portfolio
            while(iterator.hasNext()) {
                key = iterator.next();
                Portfolio portfolio = portfolioList.get(key);

                //if the portfolio has the asset, calculate VaR
                if(portfolio.getAssets().get(data[0]) != null) {
                    //counts the number of stock symbols which have already had the given batch size number of events
                    int count = 0;

                    Set assetsKeys = portfolio.getAssets().keySet();
                    Iterator<String> assetIterator = assetsKeys.iterator();
                    //for each asset
                    while (assetIterator.hasNext()) {
                        String assetKey = assetIterator.next();
                        count += assetList.get(assetKey).getHistoricalValues().size();
                    }

                    if (count == batchSize * portfolio.getAssets().size()) {
                        var = Double.parseDouble(processData(portfolio).toString());
                        result.put("Portfolio: " + portfolio.getID(), var);
                    }
                }
            }
        }
        return result.toString();
    }

    public void getPortfolioValues(ExecutionPlanContext executionPlanContext){
        //get the portfolio details from the database
        try {
            Connection connection = executionPlanContext.getSiddhiContext().getSiddhiDataSource("AnalyticsDataSource").getConnection();
            String sql = "SELECT distinct(portfolioID) FROM portfolio natural join portfolioDetails";
            Statement stm = connection.createStatement();
            ResultSet rst = stm.executeQuery(sql);

            int portfolioID;

            //for each portfolio
            while(rst.next()){
                portfolioID = rst.getInt(1);
                Statement stm1 = connection.createStatement();
                sql = "SELECT symbol, noOfShares from portfolioDetails where portfolioID = " + portfolioID;
                ResultSet symbolList = stm1.executeQuery(sql);
                Map<String, Integer> assets = new HashMap<>();
                Portfolio portfolio;

                while(symbolList.next()){
                    assets.put(symbolList.getString(1), symbolList.getInt(2));
                }

                portfolio = new Portfolio(portfolioID, assets);
                portfolioList.put(portfolioID, portfolio);
            }
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Map<Integer, Portfolio> getPortfolioList(){
        return portfolioList;
    }

    public void readAssetList(ExecutionPlanContext executionPlanContext){
        Connection connection;
        try {
            connection = executionPlanContext.getSiddhiContext().getSiddhiDataSource("AnalyticsDataSource").getConnection();
            String sql = "select distinct(symbol) from symbol natural join portfolioDetails";
            Statement stm = connection.createStatement();
            ResultSet rst = stm.executeQuery(sql);
            Asset asset;

            while(rst.next()){
                asset = new Asset(rst.getString(1));
                assetList.put(rst.getString(1), asset);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

