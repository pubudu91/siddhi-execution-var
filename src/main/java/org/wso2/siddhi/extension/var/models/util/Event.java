package org.wso2.siddhi.extension.var.models.util;

/**
 * Created by dilini92 on 1/24/17.
 */
public class Event {
    private String portfolioID;
    private int quantity;
    private String symbol;
    private double price;

    public Event(){}

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
