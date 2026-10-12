package com.cointrail.model;

/**
 * Represents a tax quote response.
 */
public final class TaxQuoteResponse {

    /** The price. */
    private double price;

    /** The tax rate. */
    private double taxRate;

    /** The tax amount. */
    private double taxAmount;

    /** The total. */
    private double total;

    /** Default constructor. */
    public TaxQuoteResponse() {
    }

    /**
     * Constructs a tax quote response.
     *
     * @param price the price
     * @param taxRate the tax rate
     * @param taxAmount the tax amount
     * @param total the total
     */
    public TaxQuoteResponse(
        final double price,
        final double taxRate,
        final double taxAmount,
        final double total
    ) {
        this.price = price;
        this.taxRate = taxRate;
        this.taxAmount = taxAmount;
        this.total = total;
    }

    /**
     * Gets the price.
     *
     * @return the price
     */
    public double getPrice() {
        return price;
    }

    /**
     * Sets the price.
     *
     * @param price the price
     */
    public void setPrice(final double price) {
        this.price = price;
    }

    /**
     * Gets the tax rate.
     *
     * @return the tax rate
     */
    public double getTaxRate() {
        return taxRate;
    }

    /**
     * Sets the tax rate.
     *
     * @param taxRate the tax rate
     */
    public void setTaxRate(final double taxRate) {
        this.taxRate = taxRate;
    }

    /**
     * Gets the tax amount.
     *
     * @return the tax amount
     */
    public double getTaxAmount() {
        return taxAmount;
    }

    /**
     * Sets the tax amount.
     *
     * @param taxAmount the tax amount
     */
    public void setTaxAmount(final double taxAmount) {
        this.taxAmount = taxAmount;
    }

    /**
     * Gets the total.
     *
     * @return the total
     */
    public double getTotal() {
        return total;
    }

    /**
     * Sets the total.
     *
     * @param total the total
     */
    public void setTotal(final double total) {
        this.total = total;
    }
}
