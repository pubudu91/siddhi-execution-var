package org.wso2.siddhi.extension.var.realtime.historical;

import org.wso2.siddhi.extension.var.models.Event;
import org.wso2.siddhi.extension.var.models.HistoricalAsset;
import org.wso2.siddhi.extension.var.models.HistoricalPortfolio;
import org.wso2.siddhi.extension.var.models.Portfolio;
import org.wso2.siddhi.extension.var.realtime.VaRCalculator;
import org.wso2.siddhi.extension.var.realtime.util.RealTimeVaRConstants;

/**
 * Created by dilini92 on 6/26/16.
 */
public class HistoricalVaRCalculator extends VaRCalculator {

    /**
     * @param batchSize
     * @param ci
     */
    public HistoricalVaRCalculator(int batchSize, double ci) {
        super(batchSize, ci);
        setType(RealTimeVaRConstants.HISTORICAL);
    }

    /**
     * @return the var of the portfolio
     * Calculate the contribution of the changed asset to the portfolio and then adjust the previous VaR value
     * using Historical data
     */
    @Override
    public Double processData(Portfolio portfolio, Event event) {

        HistoricalPortfolio historicalPortfolio = (HistoricalPortfolio) portfolio;
        String symbol = event.getSymbol();
        HistoricalAsset asset = (HistoricalAsset) getAssetList().get(symbol);

        //for historical simulation there should be at least one return value
        if (asset.getNumberOfReturnValues() > 0) {
            double var = historicalPortfolio.getHistoricalVarValue();

            double previousReturnValue = asset.getPreviousReturnValue();
            double currentReturnValue = asset.getCurrentReturnValue();

            int previousShares = historicalPortfolio.getPreviousSharesCount(symbol);
            int currentShares = historicalPortfolio.getCurrentSharesCount(symbol);

            double previousPrice = asset.getPreviousStockPrice();
            double currentPrice = asset.getCurrentStockPrice();

            //remove the contribution of the asset before the price was changed
            var -= previousReturnValue * previousPrice * previousShares;

            //add the new contribution of the asset after the price changed.
            var += currentReturnValue * currentPrice * currentShares;

            historicalPortfolio.setHistoricalVarValue(var);

            return var;
        }
        return null;
    }

    /**
     * simulate the changed asset once
     *
     * @param symbol
     */
    @Override
    public void simulateChangedAsset(String symbol) {
        HistoricalAsset asset = (HistoricalAsset) getAssetList().get(symbol);
        if (asset.getNumberOfReturnValues() > 0) {
            asset.setPreviousReturnValue(asset.getCurrentReturnValue());
            double currentReturnValue = asset.getPercentile((1 - getConfidenceInterval()) * 100);
            asset.setCurrentReturnValue(currentReturnValue);
        }
    }
}
