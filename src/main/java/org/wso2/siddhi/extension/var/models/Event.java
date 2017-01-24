package org.wso2.siddhi.extension.var.models;

/**
 * Created by dilini92 on 1/24/17.
 */
public class Event {
    private String portfolioID;
    private int shares;
    private String symbol;
    private double price;

    public Event(String portfolioID, int shares, String symbol, double price) {
        this.portfolioID = portfolioID;
        this.shares = shares;
        this.symbol = symbol;
        this.price = price;
    }

    public String getPortfolioID() {
        return portfolioID;
    }

    public int getShares() {
        return shares;
    }

    public String getSymbol() {
        return symbol;
    }

    public double getPrice() {
        return price;
    }
}
