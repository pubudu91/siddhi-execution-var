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
     *
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
        HistoricalAsset asset = (HistoricalAsset)getAssetList().get(symbol);
        if(asset.getNumberOfReturnValues() > 0) {
            double var = historicalPortfolio.getHistoricalVarValue();

            double previousReturnValue = asset.getPreviousLossReturn();
            double currentReturnValue = asset.getCurrentLossReturn();

            int previousShares;
            if(portfolio.getID().equals(event.getPortfolioID()))
                previousShares = historicalPortfolio.getPreviousSharesCount(symbol);
            else
                previousShares = historicalPortfolio.getCurrentSharesCount(symbol);

            int currentShares = historicalPortfolio.getCurrentSharesCount(symbol);

            double previousPrice = asset.getPriceBeforeLastPrice();
            double currentPrice = asset.getCurrentStockPrice();

            var -= previousReturnValue * previousPrice * previousShares;
            var += currentReturnValue * currentPrice * currentShares;

            historicalPortfolio.setHistoricalVarValue(var);

            return var;
        }
        return null;
    }

    @Override
    public double replaceAssetSimulation(Double removedEvent, String symbol) {
        HistoricalAsset asset = (HistoricalAsset)getAssetList().get(symbol);
        if(asset.getNumberOfReturnValues() > 0) {
            asset.setPreviousLossReturn(asset.getCurrentLossReturn());
            double currentReturnValue = asset.getReturnValueSet().getPercentile((1 - getConfidenceInterval()) * 100);
            asset.setCurrentLossReturn(currentReturnValue);
        }
        return 0;
    }
}
