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
    public Map<String, Asset> assetList; // this is public because it is used in VarModelAssertion for backtesting
    protected double price;
    protected String symbol;
    Asset temporaryAsset;

    /**
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
     * for testing purposes
     *
     * @param assetList
     */
    public void setAssetList(Map<String, Asset> assetList) {
        this.assetList = assetList;
    }

    /**
     * @param data
     */
    public void addEvent(Object data[]) {

        Asset temp = assetList.get(symbol);
        if (temp == null) {
            assetList.put(symbol, new Asset(symbol));
            temp = assetList.get(symbol);
        }

        temp.setPriceBeforeLastPrice(temp.getCurrentStockPrice());
        temp.setCurrentStockPrice(price);

        //assume that all price values of assets cannot be zero or negative
        if (temp.getPriceBeforeLastPrice() > 0)
            temp.addReturnValue(Math.log(temp.getCurrentStockPrice() / temp.getPriceBeforeLastPrice()));

    }

    /**
     * removes the oldest element from a given portfolio
     *
     * @param symbol
     */
    public void removeEvent(String symbol) {
        LinkedList<Double> priceList = assetList.get(symbol).getHistoricalValues();
        priceList.remove(0);
    }

    /**
     * @param portfolio
     * @return
     */
    protected abstract Object processData(Portfolio portfolio);

    /**
     * @param data
     * @return
     */
    public Object calculateValueAtRisk(Object data[]) {

        addEvent(data);
        temporaryAsset = assetList.get(data[0]);
        /***********************************************************************************************************************************/
        //if the given portfolio has the symbol and number of historical value exceeds the batch size, remove the event
        // since we don't keep historical values anymore we have to change this
        if (temporaryAsset != null && temporaryAsset.getHistoricalValues().size() > batchSize) {
            removeEvent(data[0].toString());
        }
/***********************************************************************************************************************************/
        JSONObject result = new JSONObject();
        Set<Integer> keys = portfolioList.keySet();
        Iterator<Integer> iterator = keys.iterator();
        String resultString = "";
        int key;
        double var;

        //if the given symbol is in the assetList
        if (temporaryAsset != null) {
            //for each portfolio
            while (iterator.hasNext()) {
                key = iterator.next();
                Portfolio portfolio = portfolioList.get(key);

                //if the portfolio has the asset, calculate VaR
                if (portfolio.getAssets().get(data[0]) != null) {
                    portfolio.setIncomingEventLabel(data[0].toString());
                    var = Double.parseDouble(processData(portfolio).toString());
                    result.put(RealTimeVaRConstants.PORTFOLIO + portfolio.getID(), var);
                }
            }
        }

        //if no var has been calculated
        if (result.length() == 0)
            return null;
        return result.toString().concat(resultString);
    }

    /**
     * @param executionPlanContext
     */
    public void getPortfolioValues(ExecutionPlanContext executionPlanContext) {
        //get the portfolio details from the database
        try {
            Connection connection = executionPlanContext.getSiddhiContext().
                    getSiddhiDataSource(RealTimeVaRConstants.DATA_SOURCE_NAME).getConnection();
            String sql;
            Statement stm = connection.createStatement();
            ResultSet rst = stm.executeQuery(RealTimeVaRConstants.PORTFOLIO_IDS_SQL);

            int portfolioID;

            //for each portfolio
            while (rst.next()) {
                portfolioID = rst.getInt(1);
                Statement stm1 = connection.createStatement();
                sql = RealTimeVaRConstants.PORTFOLIO_DETAILS_SQL + portfolioID;
                ResultSet symbolList = stm1.executeQuery(sql);
                Map<String, Integer> assets = new HashMap<>();
                Portfolio portfolio;

                while (symbolList.next()) {
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

    /**
     * @param executionPlanContext
     */
    public void readAssetList(ExecutionPlanContext executionPlanContext) {
        Connection connection;
        try {
            connection = executionPlanContext.getSiddhiContext().
                    getSiddhiDataSource(RealTimeVaRConstants.DATA_SOURCE_NAME).getConnection();
            String sql = RealTimeVaRConstants.SYMBOLS_SQL;
            Statement stm = connection.createStatement();
            ResultSet rst = stm.executeQuery(sql);
            Asset asset;

            while (rst.next()) {
                asset = new Asset(rst.getString(1));
                assetList.put(rst.getString(1), asset);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


}

