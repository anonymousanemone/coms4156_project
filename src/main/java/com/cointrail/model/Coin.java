package com.cointrail.model;

import java.time.Instant;

/**
 * Represents a coin held by a museum.
 */
public final class Coin {

    /** The coin ID, in format museumId-accNumber. */
    private String id;

    /** The identifier code, as assigned by the museum or catalogue. */
    private String identifierCode;

    /** The link to the museum's catalogue entry. */
    private String catalogueLink;

    /** The department, such as Greek, Roman, or Chinese. */
    private String category;

    /** The material. */
    private String material;

    /** The first year of the coin's period (negative for BCE). */
    private Integer periodStart;

    /** The last year of the coin's period (negative for BCE). */
    private Integer periodEnd;

    /** Whether the period was estimated because the museum gave none. */
    private boolean periodInferred;

    /**
     * The original credit line from the catalogue, 
     * or other raw acquisition information.
     */
    private String creditLine;

    /** The acquisition it is a part of. */
    private Acquisition acquisition;

    /** When the record was fetched from the museum or source. */
    private Instant accessedAt;

    /** Default constructor. */
    public Coin() {
    }

    /**
     * Constructs a coin from "raw" source fields. The processed fields
     * (category, material, period, acquisition, and access time) 
     * are set afterwards.
     *
     * @param id the coin ID
     * @param identifierCode the identifier code
     * @param catalogueLink the catalogue link
     * @param creditLine the original credit line
     */
    public Coin(
        final String id,
        final String identifierCode,
        final String catalogueLink,
        final String creditLine
    ) {
        this.id = id;
        this.identifierCode = identifierCode;
        this.catalogueLink = catalogueLink;
        this.creditLine = creditLine;
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
     * Gets the identifier code.
     *
     * @return the identifier code
     */
    public String getIdentifierCode() {
        return identifierCode;
    }

    /**
     * Sets the identifier code.
     *
     * @param identifierCode the identifier code
     */
    public void setIdentifierCode(final String identifierCode) {
        this.identifierCode = identifierCode;
    }

    /**
     * Gets the catalogue link.
     *
     * @return the catalogue link
     */
    public String getCatalogueLink() {
        return catalogueLink;
    }

    /**
     * Sets the catalogue link.
     *
     * @param catalogueLink the catalogue link
     */
    public void setCatalogueLink(final String catalogueLink) {
        this.catalogueLink = catalogueLink;
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
     * Gets the material.
     *
     * @return the material
     */
    public String getMaterial() {
        return material;
    }

    /**
     * Sets the material.
     *
     * @param material the material
     */
    public void setMaterial(final String material) {
        this.material = material;
    }

    /**
     * Gets the period start year.
     *
     * @return the period start year, or null
     */
    public Integer getPeriodStart() {
        return periodStart;
    }

    /**
     * Sets the period start year.
     *
     * @param periodStart the period start year
     */
    public void setPeriodStart(final Integer periodStart) {
        this.periodStart = periodStart;
    }

    /**
     * Gets the period end year.
     *
     * @return the period end year, or null
     */
    public Integer getPeriodEnd() {
        return periodEnd;
    }

    /**
     * Sets the period end year.
     *
     * @param periodEnd the period end year
     */
    public void setPeriodEnd(final Integer periodEnd) {
        this.periodEnd = periodEnd;
    }

    /**
     * Gets whether the period was estimated.
     *
     * @return true if the period was estimated
     */
    public boolean isPeriodInferred() {
        return periodInferred;
    }

    /**
     * Sets whether the period was estimated.
     *
     * @param periodInferred true if the period was estimated
     */
    public void setPeriodInferred(final boolean periodInferred) {
        this.periodInferred = periodInferred;
    }

    /**
     * Gets the original credit line.
     *
     * @return the credit line
     */
    public String getCreditLine() {
        return creditLine;
    }

    /**
     * Sets the original credit line.
     *
     * @param creditLine the credit line
     */
    public void setCreditLine(final String creditLine) {
        this.creditLine = creditLine;
    }

    /**
     * Gets the acquisition.
     *
     * @return the acquisition
     */
    public Acquisition getAcquisition() {
        return acquisition;
    }

    /**
     * Sets the acquisition.
     *
     * @param acquisition the acquisition
     */
    public void setAcquisition(final Acquisition acquisition) {
        this.acquisition = acquisition;
    }

    /**
     * Gets the access time.
     *
     * @return when the record was fetched
     */
    public Instant getAccessedAt() {
        return accessedAt;
    }

    /**
     * Sets the access time.
     *
     * @param accessedAt when the record was fetched
     */
    public void setAccessedAt(final Instant accessedAt) {
        this.accessedAt = accessedAt;
    }
}
