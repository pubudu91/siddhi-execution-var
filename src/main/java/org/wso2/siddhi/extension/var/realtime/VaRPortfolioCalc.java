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
    private Map<Integer, Portfolio> portfolioList;
    protected Map<String, Asset> assetList; // this is public because it is used in VarModelAssertion for backtesting
    protected double price;
    protected String symbol;
    private int portfolioID;
    private String pSymbol;
    private int shares;

    /**
     * @param limit
     * @param ci
     */
    public VaRPortfolioCalc(int limit, double ci) {
        confidenceInterval = ci;
        batchSize = limit;

        //ensures that there will be only one instance of each
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
        portfolioID = ((Number) data[0]).intValue();
        pSymbol = data[1].toString();
        shares = ((Number) data[2]).intValue();
        symbol = data[3].toString();
        price = ((Number) data[4]).doubleValue();

        double priceBeforeLastPrice;

        Asset temp = assetList.get(symbol);
        if(temp == null) {
            assetList.put(symbol, new Asset(symbol));
            temp = assetList.get(symbol);
        }

        priceBeforeLastPrice = temp.getCurrentStockPrice();
        temp.setCurrentStockPrice(price);

        //assume that all price values of assets cannot be zero or negative
        if(priceBeforeLastPrice > 0)
            temp.addReturnValue(Math.log(price/priceBeforeLastPrice));

        Portfolio portfolio = portfolioList.get(portfolioID);

        if(portfolio == null){//first time for the portfolio
            Map<String, Integer> assets = new HashMap<>();
            assets.put(symbol, shares);
            portfolio = new Portfolio(portfolioID, assets);
            portfolioList.put(portfolioID, portfolio);
        }else if(portfolio.getAssets().get(pSymbol) == null){//first time for the asset within portfolio
            portfolio.getAssets().put(pSymbol, shares);
        }else{//portfolio exists, asset within portfolio exists
            //check whether no of shares has been changed
            if(shares != portfolio.getAssets().get(pSymbol)){
                portfolio.getAssets().put(pSymbol, shares);
            }
        }
    }

    /**
     * removes the oldest element from a given portfolio
     *
     * @param symbol
     */
    public void removeEvent(String symbol) {
        ArrayList<Double> priceList = assetList.get(symbol).getLatestReturnValues();
        priceList.remove(0);

        portfolioList.get(portfolioID).getAssets().remove(pSymbol);
    }

    /**
     * @param portfolio
     * @return
     */
    protected abstract Object processData(Portfolio portfolio);

    public Object calculateValueAtRisk(Object data[]){
        addEvent(data);
        //if the number of historical value exceeds the batch size, remove the event
        if (assetList.get(data[3]) != null && assetList.get(data[3]).getNumberOfHistoricalValues() > batchSize) {
            removeEvent(data[3].toString());
        }

        //declare variables
        JSONObject result = new JSONObject();
        Set<Integer> keys = portfolioList.keySet();
        Iterator<Integer> iterator = keys.iterator();
        String resultString = "";
        int key;
        double var;
        Portfolio portfolio;

        //get the portfolio sent from the stream
        portfolio = portfolioList.get(portfolioID);

        //if this portfolio does not contain the symbol from the input stream, var should be calculated.
        if(portfolio.getAssets().get(symbol) == null){
            portfolio.setIncomingEventLabel(data[0].toString());
            Object temp = processData(portfolio);
            if(temp != null){
                var = Double.parseDouble(temp.toString());
                result.put(RealTimeVaRConstants.PORTFOLIO + portfolio.getID(), var);
            }
        }

        while(iterator.hasNext()){
            key = iterator.next();
            portfolio = portfolioList.get(key);

            //if the portfolio has the asset, calculate VaR
            if(portfolio.getAssets().get(data[0]) != null) {
                portfolio.setIncomingEventLabel(data[0].toString());
                Object temp = processData(portfolio);
                if(temp != null){
                    var = Double.parseDouble(temp.toString());
                    result.put(RealTimeVaRConstants.PORTFOLIO + portfolio.getID(), var);
                }
            }
        }

        //if no var has been calculated
        if (result.length() == 0)
            return null;
        return result.toString().concat(resultString);
    }
}

