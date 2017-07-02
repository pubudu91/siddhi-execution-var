/*
 * Copyright (c)  2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.extension.siddhi.execution.var.models.util;

/**
 * A class representing a market update event
 */
public class Event {
    private String portfolioID;
    private int quantity;
    private String symbol;
    private double price;

    public Event() {
    }

    public Event(String portfolioID, int quantity, String symbol, double price) {
        this.portfolioID = portfolioID;
        this.quantity = quantity;
        this.symbol = symbol;
        this.price = price;
    }

    public void setPortfolioID(String portfolioID) {
        this.portfolioID = portfolioID;
    }

    public void setQuantity(int shares) {
        this.quantity = shares;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getPortfolioID() {
        return portfolioID;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getSymbol() {
        return symbol;
    }

    public double getPrice() {
        return price;
    }
}
