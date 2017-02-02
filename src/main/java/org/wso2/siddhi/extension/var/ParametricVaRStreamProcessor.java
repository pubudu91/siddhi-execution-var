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
import org.wso2.siddhi.extension.var.models.util.RealTimeVaRConstants;
import org.wso2.siddhi.query.api.definition.AbstractDefinition;
import org.wso2.siddhi.query.api.definition.Attribute;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dilip on 30/06/16.
 */
public class ParametricVaRStreamProcessor extends StreamProcessor {
    private int batchSize = 251;                  // Maximum # of events, used for var calculation
    private double confidenceInterval = 0.95;
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
                Object inputData[] = new Object[RealTimeVaRConstants.NUMBER_OF_PARAMETERS];
                for (int i = 0; i < RealTimeVaRConstants.NUMBER_OF_PARAMETERS; i++) {
                    inputData[i] = attributeExpressionExecutors[i].execute(complexEvent);
                }

                Object outputData[] = new Object[1];
                outputData[0] = varCalculator.calculateValueAtRisk(inputData);

                if (outputData[0] == null) { //if there is no output
                    streamEventChunk.remove();
                } else {    //if there is an output, publish it to the output stream
                    complexEventPopulater.populateComplexEvent(complexEvent, outputData);
                }
            }
        }
        nextProcessor.process(streamEventChunk); //process the next stream event
    }

    /**
     * @param inputDefinition
     * @param attributeExpressionExecutors
     * @param executionPlanContext
     * @return
     */
    @Override
    protected List<Attribute> init(AbstractDefinition inputDefinition,
                                   ExpressionExecutor[] attributeExpressionExecutors,
                                   ExecutionPlanContext executionPlanContext) {
        // Capture constant inputs
        if (attributeExpressionExecutors[RealTimeVaRConstants.BATCH_SIZE_INDEX] instanceof ConstantExpressionExecutor) {
            try {
                batchSize = ((Integer) attributeExpressionExecutors[RealTimeVaRConstants.BATCH_SIZE_INDEX].execute
                        (null));
            } catch (ClassCastException c) {
                throw new ExecutionPlanCreationException("Batch size should be an integer");
            }
            try {
                confidenceInterval = ((Double) attributeExpressionExecutors[RealTimeVaRConstants.CI_INDEX].execute
                        (null));
            } catch (ClassCastException c) {
                throw new ExecutionPlanCreationException("Confidence interval should be a double value between 0 and " +
                        "1");
            }
        }

        // set the var calculator
        varCalculator = new ParametricVaRCalculator(batchSize, confidenceInterval);

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
}