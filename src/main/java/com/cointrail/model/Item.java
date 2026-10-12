package com.cointrail.model;

/**
 * Represents an item in the tax API.
 */
public final class Item {

    /** The item ID. */
    private String id;

    /** The item name. */
    private String name;

    /** The item category. */
    private String category;

    /** The base price. */
    private double basePrice;

    /** Default constructor. */
    public Item() {
    }

    /**
     * Constructs an item.
     *
     * @param id the item ID
     * @param name the item name
     * @param category the item category
     * @param basePrice the base price
     */
    public Item(
        final String id,
        final String name,
        final String category,
        final double basePrice
    ) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.basePrice = basePrice;
    }

    /**
     * Gets the ID.
     *
     * @return the ID
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the ID.
     *
     * @param id the ID
     */
    public void setId(final String id) {
        this.id = id;
    }

    /**
     * Gets the name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name.
     *
     * @param name the name
     */
    public void setName(final String name) {
        this.name = name;
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
     * Gets the base price.
     *
     * @return the base price
     */
    public double getBasePrice() {
        return basePrice;
    }

    /**
     * Sets the base price.
     *
     * @param basePrice the base price
     */
    public void setBasePrice(final double basePrice) {
        this.basePrice = basePrice;
    }
}
