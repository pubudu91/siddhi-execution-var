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
import org.wso2.siddhi.extension.var.realtime.montecarlo.MonteCarloVarCalculator;
import org.wso2.siddhi.extension.var.realtime.util.RealTimeVaRConstants;
import org.wso2.siddhi.extension.var.realtime.VaRCalculator;
import org.wso2.siddhi.query.api.definition.AbstractDefinition;
import org.wso2.siddhi.query.api.definition.Attribute;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by flash on 7/7/16.
 */
public class MonteCarloVaRStreamProcessor extends StreamProcessor {
    private int batchSize = 251;                                        // Maximum # of events, used for regression
    // calculation
    private double confidenceInterval = 0.95;                                           // Confidence Interval
    private VaRCalculator varCalculator = null;
    private int numberOfSimulationHorizontal = 2000;
    private int numberOfSimulationVertical = 100;
    private double timeSlice = 0.01;


    @Override
    protected void process(ComplexEventChunk<StreamEvent> streamEventChunk, Processor nextProcessor,
                           StreamEventCloner streamEventCloner, ComplexEventPopulater complexEventPopulater) {
        synchronized (this) {
            while (streamEventChunk.hasNext()) {
                ComplexEvent complexEvent = streamEventChunk.next();
                Object inputData[] = new Object[RealTimeVaRConstants.NUMBER_OF_PARAMETERS];
                //get the symbol and price attributes from the stream to process
                for (int i = 0; i < RealTimeVaRConstants.NUMBER_OF_PARAMETERS; i++) {
                    inputData[i] = attributeExpressionExecutors[i].execute(complexEvent);
                }

                Object outputData[] = new Object[1];
                outputData[0] = varCalculator.calculateValueAtRisk(inputData);
                // Skip processing if user has specified calculation interval
                if (outputData[0] == null) { //if there is no output
                    streamEventChunk.remove();
                } else {
                    complexEventPopulater.populateComplexEvent(complexEvent, outputData);
                }
            }
        }
        nextProcessor.process(streamEventChunk);
    }

    @Override
    protected List<Attribute> init(AbstractDefinition inputDefinition, ExpressionExecutor[]
            attributeExpressionExecutors, ExecutionPlanContext executionPlanContext) {
        if (attributeExpressionExecutors[0] instanceof ConstantExpressionExecutor) {
            try {
                this.batchSize = ((Integer) attributeExpressionExecutors[RealTimeVaRConstants.BATCH_SIZE_INDEX]
                        .execute(null));
                this.timeSlice = ((double) attributeExpressionExecutors[RealTimeVaRConstants
                        .MONTE_CARLO_TIME_SLICE_INDEX].execute(null));
                this.numberOfSimulationHorizontal = ((Integer) attributeExpressionExecutors[RealTimeVaRConstants
                        .MONTE_CARLO_HORIZONTAL_SIMULATION_COUNT_INDEX].execute(null));
                this.numberOfSimulationVertical = ((Integer) attributeExpressionExecutors[RealTimeVaRConstants
                        .MONTE_CARLO_VERTICAL_SIMULATION_COUNT_INDEX].execute(null));
            } catch (ClassCastException c) {
                throw new ExecutionPlanCreationException("Calculation interval, batch size and range should be of " +
                        "type int");
            }
            try {
                confidenceInterval = ((Double) attributeExpressionExecutors[1].execute(null));
            } catch (ClassCastException c) {
                throw new ExecutionPlanCreationException("Confidence interval should be of type double and a value " +
                        "between 0 and 1");
            }
        }

        // set the var calculator
        varCalculator = new MonteCarloVarCalculator(batchSize, confidenceInterval,
                numberOfSimulationHorizontal, numberOfSimulationVertical, timeSlice);

        // Add attribute for var
        ArrayList<Attribute> attributes = new ArrayList<>(1);
        attributes.add(new Attribute("var", Attribute.Type.STRING));

        return attributes;
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    @Override
    public Object[] currentState() {
        return new Object[0];
    }

    @Override
    public void restoreState(Object[] state) {

    }
}
