package org.wso2.siddhi.extension.var;

import org.wso2.siddhi.core.config.ExecutionPlanContext;
import org.wso2.siddhi.core.event.ComplexEvent;
import org.wso2.siddhi.core.event.ComplexEventChunk;
import org.wso2.siddhi.core.event.stream.StreamEvent;
import org.wso2.siddhi.core.event.stream.StreamEventCloner;
import org.wso2.siddhi.core.event.stream.populater.ComplexEventPopulater;
import org.wso2.siddhi.core.exception.ExecutionPlanCreationException;
import org.wso2.siddhi.core.executor.ConstantExpressionExecutor;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.query.processor.Processor;
import org.wso2.siddhi.core.query.processor.stream.StreamProcessor;
import org.wso2.siddhi.extension.var.models.Asset;
import org.wso2.siddhi.extension.var.models.Portfolio;
import org.wso2.siddhi.extension.var.realtime.HistoricalVaRCalculator;
import org.wso2.siddhi.extension.var.realtime.VaRPortfolioCalc;
import org.wso2.siddhi.query.api.definition.AbstractDefinition;
import org.wso2.siddhi.query.api.definition.Attribute;

import javax.sql.DataSource;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

/**
 * Created by dilini92 on 6/26/16.
 */
public class HistoricalVaRStreamProcessor extends StreamProcessor {
    private int batchSize = 251;                                        // Maximum # of events, used for regression calculation
    private double ci = 0.95;                                           // Confidence Interval
    private VaRPortfolioCalc varCalculator = null;
    private int paramPosition = 0;
    private Map<String, Asset> portfolio = new HashMap<String, Asset>();
    private boolean hasWeight = false;
    private Map<Integer, Portfolio> portfolioList = new HashMap<>();
    /**
     *
     * @param streamEventChunk      the event chunk that need to be processed
     * @param nextProcessor         the next processor to which the success events need to be passed
     * @param streamEventCloner     helps to clone the incoming event for local storage or modification
     * @param complexEventPopulater helps to populate the events with the resultant attributes
     */
    @Override
    protected void process(ComplexEventChunk<StreamEvent> streamEventChunk, Processor nextProcessor, StreamEventCloner streamEventCloner, ComplexEventPopulater complexEventPopulater) {
        synchronized (this) {
            while (streamEventChunk.hasNext()) {
                ComplexEvent complexEvent = streamEventChunk.next();

                //get the symbol and price attributes from the stream to process
                Object inputData[] = new Object[2];
                inputData[0] = attributeExpressionExecutors[2].execute(complexEvent);
                inputData[1] = attributeExpressionExecutors[3].execute(complexEvent);

                Object outputData[] = new Object[1];
                outputData[0] = varCalculator.calculateValueAtRisk(inputData);

                // Skip processing if user has specified calculation interval
                if (outputData[0].toString().isEmpty()) { //if there is no output
                    streamEventChunk.remove();
                } else {    //if there is an output, publish it to the output stream
                    complexEventPopulater.populateComplexEvent(complexEvent, outputData);
                }
            }
        }
        nextProcessor.process(streamEventChunk); //process the next stream event
    }

    /**
     *
     * @param inputDefinition
     * @param attributeExpressionExecutors
     * @param executionPlanContext
     * @return
     */
    @Override
    protected List<Attribute> init(AbstractDefinition inputDefinition, ExpressionExecutor[] attributeExpressionExecutors, ExecutionPlanContext executionPlanContext) {
        // Capture constant inputs
        if (attributeExpressionExecutors[0] instanceof ConstantExpressionExecutor) {
            paramPosition = (attributeExpressionLength + 4)/2;
            getPortfolioValues(executionPlanContext);
            try {
                batchSize = ((Integer) attributeExpressionExecutors[0].execute(null));
            } catch (ClassCastException c) {
                throw new ExecutionPlanCreationException("Calculation interval, batch size and range should be of type int");
            }
            try {
                ci = ((Double) attributeExpressionExecutors[1].execute(null));
//                String symbol;
//                for (int i = 0; i < (attributeExpressionLength - 4)/2; i++) {
//                    symbol = attributeExpressionExecutors[i + 4].execute(null).toString();
//                    Asset asset = new Asset(((Integer)attributeExpressionExecutors[paramPosition + i].execute(null)));
//                    portfolio.put(symbol, asset);
//                }

                hasWeight = (Boolean)attributeExpressionExecutors[attributeExpressionLength - 1].execute(null);
            } catch (ClassCastException c) {
                throw new ExecutionPlanCreationException("Confidence interval should be of type double and a value between 0 and 1");
            }
        }

        // set the var calculator
        varCalculator = new HistoricalVaRCalculator(batchSize, ci, portfolioList, hasWeight);

        // Add attribute for var
        ArrayList<Attribute> attributes = new ArrayList<Attribute>(1);
        attributes.add(new Attribute("var", Attribute.Type.STRING));

        return attributes;
    }

    /**
     *
     */
    @Override
    public void start() {

    }

    /**
     *
     */
    @Override
    public void stop() {

    }

    /**
     *
     * @return
     */
    @Override
    public Object[] currentState() {
        return new Object[0];
    }

    /**
     *
     * @param state the stateful objects of the element as an array on
     */
    @Override
    public void restoreState(Object[] state) {

    }

    private void getPortfolioValues(ExecutionPlanContext executionPlanContext){
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