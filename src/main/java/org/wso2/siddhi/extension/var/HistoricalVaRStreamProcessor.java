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
import org.wso2.siddhi.extension.var.models.VaRCalculator;
import org.wso2.siddhi.extension.var.models.historical.HistoricalVaRCalculator;
import org.wso2.siddhi.extension.var.models.util.Event;
import org.wso2.siddhi.extension.var.models.util.RealTimeVaRConstants;
import org.wso2.siddhi.query.api.definition.AbstractDefinition;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.exception.ExecutionPlanValidationException;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dilini92 on 6/26/16.
 */
public class HistoricalVaRStreamProcessor extends StreamProcessor {
    private int batchSize = 251;                           // Maximum # of events, used for var calculation
    private double confidenceInterval = 0.95;              // Confidence Interval
    private VaRCalculator varCalculator = null;

    /**
     * @param streamEventChunk      the event chunk that need to be processed
     * @param nextProcessor         the next processor to which the success events need to be passed
     * @param streamEventCloner     helps to clone the incoming event for local storage or modification
     * @param complexEventPopulater helps to populate the events with the resultant attributes
     */
    @Override
    protected void process(ComplexEventChunk<StreamEvent> streamEventChunk, Processor nextProcessor,
                           StreamEventCloner streamEventCloner, ComplexEventPopulater complexEventPopulater) {
        synchronized (this) {
            while (streamEventChunk.hasNext()) {
                ComplexEvent complexEvent = streamEventChunk.next();
                //get the portfolioID,symbol,shares and price attributes from the stream to process
                Event event = new Event();

                //Portfolio ID
                Object portfolioID;
                if ((portfolioID = attributeExpressionExecutors[RealTimeVaRConstants.PORTFOLIO_ID_INDEX].execute
                        (complexEvent)) != null) {
                    event.setPortfolioID(portfolioID.toString());
                }
                //Quantity
                Object quantity;
                if ((quantity = attributeExpressionExecutors[RealTimeVaRConstants.QUANTITY_INDEX].execute
                        (complexEvent)) != null) {
                    event.setQuantity((Integer) quantity);
                }
                //Symbol
                event.setSymbol(attributeExpressionExecutors[RealTimeVaRConstants.SYMBOL_INDEX].execute(complexEvent)
                        .toString());
                //Price
                event.setPrice((Double) attributeExpressionExecutors[RealTimeVaRConstants.PRICE_INDEX].execute
                        (complexEvent));

                Object outputData[] = new Object[1];
                outputData[0] = varCalculator.calculateValueAtRisk(event);

                if (outputData[0] == null) { //if there is no output
                    streamEventChunk.remove();
                } else {    //if there is an output, publish it to the output stream
                    complexEventPopulater.populateComplexEvent(complexEvent, outputData);
                }
            }
        }
        nextProcessor.process(streamEventChunk); //process the next stream event
    }

    @Override
    protected List<Attribute> init(AbstractDefinition inputDefinition,
                                   ExpressionExecutor[] attributeExpressionExecutors,
                                   ExecutionPlanContext executionPlanContext) {
        // Capture batch size
        if (attributeExpressionExecutors[RealTimeVaRConstants.BATCH_SIZE_INDEX] instanceof ConstantExpressionExecutor) {
            if (attributeExpressionExecutors[RealTimeVaRConstants.BATCH_SIZE_INDEX].execute(null) instanceof Integer) {
                batchSize =
                        ((Integer) attributeExpressionExecutors[RealTimeVaRConstants.BATCH_SIZE_INDEX].execute(null));
            } else {
                throw new ExecutionPlanCreationException("Batch size should be an integer");
            }
        } else {
            throw new ExecutionPlanValidationException("Dynamic batch size values are not supported. Batch size " +
                    "should be a constant.");
        }

        // Capture confidence level
        if (attributeExpressionExecutors[RealTimeVaRConstants.CI_INDEX] instanceof ConstantExpressionExecutor) {
            if (attributeExpressionExecutors[RealTimeVaRConstants.CI_INDEX].execute(null) instanceof Double) {
                confidenceInterval =
                        ((Double) attributeExpressionExecutors[RealTimeVaRConstants.CI_INDEX].execute(null));
            } else {
                throw new ExecutionPlanCreationException("Confidence interval should be a double value between 0 and 1");
            }
        } else {
            throw new ExecutionPlanValidationException("Dynamic confidence interval values are not supported. " +
                    "Confidence interval should be a constant");
        }

        //validate portfolioID
        if (!(RealTimeVaRConstants.STRING.equals(attributeExpressionExecutors[RealTimeVaRConstants.PORTFOLIO_ID_INDEX].
                getReturnType().toString()))) {
            throw new ExecutionPlanValidationException("Portfolio ID should be a String value");
        }

        //validate asset quantity
        if (!(RealTimeVaRConstants.INTEGER.equals(attributeExpressionExecutors[RealTimeVaRConstants.QUANTITY_INDEX]
                .getReturnType().toString()))) {
            throw new ExecutionPlanValidationException("Quantity should be a integer value");
        }

        //validate asset symbol
        if (!(RealTimeVaRConstants.STRING.equals(attributeExpressionExecutors[RealTimeVaRConstants.SYMBOL_INDEX]
                .getReturnType().toString()))) {
            throw new ExecutionPlanValidationException("Symbol should be a String value");
        }

        //validate price
        if (!(RealTimeVaRConstants.DOUBLE.equals(attributeExpressionExecutors[RealTimeVaRConstants.PRICE_INDEX]
                .getReturnType().toString()))) {
            throw new ExecutionPlanValidationException("Price should be a double value");
        }

        // set the var calculator
        varCalculator = new HistoricalVaRCalculator(batchSize, confidenceInterval);

        // Add attribute for var
        ArrayList<Attribute> attributes = new ArrayList<>(1);
        attributes.add(new Attribute(RealTimeVaRConstants.OUTPUT_NAME, Attribute.Type.STRING));

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

    @Override
    public Object[] currentState() {
        Object[] currentStateObjects = {varCalculator};
        return currentStateObjects;
    }

    @Override
    public void restoreState(Object[] state) {
        varCalculator = (HistoricalVaRCalculator) state[0];
    }
}
