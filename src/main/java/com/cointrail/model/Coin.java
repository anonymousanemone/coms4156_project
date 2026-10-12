package com.cointrail.model;

/**
 * Represents a coin held by a museum.
 */
public final class Coin {

    /** The coin ID, in format museumId-accNumber. */
    private String id;

    /** The accession number, as assigned by the museum. */
    private String accessionNumber;

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

    /** The credit line exactly as the museum published it. */
    private String creditLine;

    /** The acquisition it is a part of. */
    private Acquisition acquisition;

    /** Default constructor. */
    public Coin() {
    }

    /**
     * Constructs a coin from "raw" source fields. The processed fields
     * (category, material, period, and acquisition) are set afterwards.
     *
     * @param id the coin ID
     * @param accessionNumber the accession number
     * @param catalogueLink the catalogue link
     * @param creditLine the original credit line
     */
    public Coin(
        final String id,
        final String accessionNumber,
        final String catalogueLink,
        final String creditLine
    ) {
        this.id = id;
        this.accessionNumber = accessionNumber;
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
     * Gets the accession number.
     *
     * @return the accession number
     */
    public String getAccessionNumber() {
        return accessionNumber;
    }

    /**
     * Sets the accession number.
     *
     * @param accessionNumber the accession number
     */
    public void setAccessionNumber(final String accessionNumber) {
        this.accessionNumber = accessionNumber;
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
}
