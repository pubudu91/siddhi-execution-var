/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.extension.siddhi.execution.var;

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
import org.wso2.extension.siddhi.execution.var.models.VaRCalculator;
import org.wso2.extension.siddhi.execution.var.models.parametric.ParametricVaRCalculator;
import org.wso2.extension.siddhi.execution.var.models.util.Event;
import org.wso2.extension.siddhi.execution.var.models.util.RealTimeVaRConstants;
import org.wso2.siddhi.query.api.definition.AbstractDefinition;
import org.wso2.siddhi.query.api.definition.Attribute;

import java.util.ArrayList;
import java.util.List;

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

    @Override
    public Object[] currentState() {
        Object[] currentStateObjects = {varCalculator};
        return currentStateObjects;
    }

    @Override
    public void restoreState(Object[] state) {
        varCalculator = (ParametricVaRCalculator) state[0];
    }
}