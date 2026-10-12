package com.cointrail.model;

/**
 * Represents a tax rate for a state and category.
 */
public final class TaxRate {

    /** The state. */
    private String state;

    /** The category. */
    private String category;

    /** The tax rate. */
    private double rate;

    /** Default constructor. */
    public TaxRate() {
    }

    /**
     * Constructs a tax rate.
     *
     * @param state the state
     * @param category the category
     * @param rate the tax rate
     */
    public TaxRate(
        final String state,
        final String category,
        final double rate
    ) {
        this.state = state;
        this.category = category;
        this.rate = rate;
    }

    /**
     * Gets the state.
     *
     * @return the state
     */
    public String getState() {
        return state;
    }

    /**
     * Sets the state.
     *
     * @param state the state
     */
    public void setState(final String state) {
        this.state = state;
    }

    /**
     * Gets the category.
     *
     * @return the category
     */
    public String getCategory() {
        return category;
    }

    /**
     * Sets the category.
     *
     * @param category the category
     */
    public void setCategory(final String category) {
        this.category = category;
    }

    /**
     * Gets the rate.
     *
     * @return the rate
     */
    public double getRate() {
        return rate;
    }

    /**
     * Sets the rate.
     *
     * @param rate the rate
     */
    public void setRate(final double rate) {
        this.rate = rate;
    }
}
