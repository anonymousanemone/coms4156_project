package com.cointrail.model;

import java.util.List;

/**
 * Represents supported states and categories.
 */
public final class SupportedResponse {

    /** The list of supported states. */
    private List<String> states;

    /** The list of supported categories. */
    private List<String> categories;

    /** Default constructor. */
    public SupportedResponse() {
    }

    /**
     * Constructs a supported response.
     *
     * @param states the supported states
     * @param categories the supported categories
     */
    public SupportedResponse(
        final List<String> states,
        final List<String> categories
    ) {
        this.states = states;
        this.categories = categories;
    }

    /**
     * Gets the states.
     *
     * @return the states
     */
    public List<String> getStates() {
        return states;
    }

    /**
     * Sets the states.
     *
     * @param states the states
     */
    public void setStates(final List<String> states) {
        this.states = states;
    }

    /**
     * Gets the categories.
     *
     * @return the categories
     */
    public List<String> getCategories() {
        return categories;
    }

    /**
     * Sets the categories.
     *
     * @param categories the categories
     */
    public void setCategories(
        final List<String> categories
    ) {
        this.categories = categories;
    }
}
